package com.software.security.zeroday.service;

import com.software.security.zeroday.dto.enumeration.LastAction;
import com.software.security.zeroday.dto.post.*;
import com.software.security.zeroday.dto.user.UserDTO;
import com.software.security.zeroday.entity.File;
import com.software.security.zeroday.entity.Post;
import com.software.security.zeroday.entity.User;
import com.software.security.zeroday.entity.enumeration.FileType;
import com.software.security.zeroday.entity.enumeration.LogAction;
import com.software.security.zeroday.entity.object.EvaluatedPostContent;
import com.software.security.zeroday.repository.PostRepository;
import com.software.security.zeroday.security.util.AuthorizationUtil;
import com.software.security.zeroday.security.util.SanitizationUtil;
import com.software.security.zeroday.service.exception.PostNotFoundException;
import com.software.security.zeroday.service.exception.SpelInjectionDetectedException;
import com.software.security.zeroday.service.exception.UrlNotReadableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final FileService fileService;
    private final AuthorizationUtil authorizationUtil;
    private final SanitizationUtil sanitizationUtil;
    private final UserActionLogger userActionLogger;

    public void createPost(PostCreationDTO postDTO) {
        String sanitizedContent = this.sanitizationUtil.sanitizeString(postDTO.getContent());
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Post parent = null;
        if (postDTO.getParentId() != null)
            parent = this.postRepository.findById(postDTO.getParentId())
                .orElseThrow(() -> new PostNotFoundException("Parent not found"));

        else postDTO.setContent(postDTO.getContent().replace("#{reply_to}#", ""));

        Instant now = Instant.now();

        Post post = Post.builder()
            .content(sanitizedContent)
            .createdAt(now)
            .updatedAt(now)
            .user(user)
            .parent(parent)
            .build();

        if (containsSpecialExpression(sanitizedContent)) {
            EvaluatedPostContent evaluatedPostContent = this.evaluatePostContent(post);
            throw new SpelInjectionDetectedException("CTF{SPEL_INJECTION_DETECTED} content: " + evaluatedPostContent.getContent());
        }

        post = this.postRepository.save(post);

        if (postDTO.getFileId() != null)
            this.fileService.finalizeFileUpload(postDTO.getFileId(), post);

        this.userActionLogger.log(LogAction.CREATE_POST, user.getUsername());
    }

    private boolean containsSpecialExpression(String content) {
        return content != null && content
            .matches(".*#\\{.*\\.concat\\(T\\(.*?\\)\\.toString\\(\\)\\).*}#.*");
    }

    public Page<PostDTO> getAllPosts(Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by(Sort.Direction.DESC, "id")
        );

        return this.postRepository.findAll(sortedPageable)
                .map(post -> {
                    EvaluatedPostContent evaluatedPostContent = this.evaluatePostContent(post);
                    return toPostDTO(
                        post,
                        evaluatedPostContent.getContent(),
                        evaluatedPostContent.getParentContent(),
                        post.getFile(),
                        post.getUser(),
                        post.getParent()
                    );
                });
    }

    public PostDTO getPost(Long id) {
        Post post = this.findById(id);
        EvaluatedPostContent evaluatedPostContent = this.evaluatePostContent(post);

        return toPostDTO(
            post,
            evaluatedPostContent.getContent(),
            evaluatedPostContent.getParentContent(),
            post.getFile(),
            post.getUser(),
            post.getParent()
        );
    }

    public RawPostDTO getRawPost(Long id) {
        Post post = this.findById(id);

        File file = post.getFile();
        String fileUrl = null;
        if (file != null) {
            fileUrl = file.getType().getFolder() + "/" +
                file.getName() + "." +
                file.getExtension().name().toLowerCase();
        }

        return RawPostDTO.builder()
            .content(post.getContent())
            .fileUrl(fileUrl)
            .build();
    }


    public void updatePost(Long id, PostModificationDTO postDTO) {
        Post post = this.findById(id);

        String sanitizedContent = this.sanitizationUtil.sanitizeString(postDTO.getContent());
        User user = this.authorizationUtil.verifyAuthorization(post.getUser().getId());

        post.setContent(sanitizedContent);
        post.setUpdatedAt(Instant.now());

        if (containsSpecialExpression(sanitizedContent)) {
            EvaluatedPostContent evaluatedPostContent = this.evaluatePostContent(post);
            throw new SpelInjectionDetectedException("CTF{SPEL_INJECTION_DETECTED} content: " + evaluatedPostContent.getContent());
        }

        post = this.postRepository.save(post);

        if (postDTO.getFileId() != null)
            this.fileService.finalizeFileUpload(postDTO.getFileId(), post);

        this.userActionLogger.log(LogAction.UPDATE_POST, user.getUsername());
    }

    public void deletePost(Long id) {
        Post post = this.findById(id);

        User user = this.authorizationUtil.verifyAuthorization(post.getUser().getId());

        this.fileService.removePostFileAndAllDescendantsFiles(post);

        this.postRepository.delete(post);

        this.userActionLogger.log(LogAction.DELETE_POST, user.getUsername());
    }

    public void createWelcomePost(User user, Instant instant) {
        Post post = Post.builder()
            .content(user.getUsername() + " has joined ZeroDay network!")
            .createdAt(instant)
            .updatedAt(instant)
            .user(user)
            .build();

        this.postRepository.save(post);
    }

    public void createProfilePictureUpdatePost(User user, Long pictureId, Instant instant) {
        Post post = Post.builder()
            .content(user.getUsername() + " updated their profile picture!")
            .createdAt(instant)
            .updatedAt(instant)
            .user(user)
            .build();

        this.postRepository.save(post);

        this.fileService.finalizeProfilePictureUpload(pictureId, user, post);
    }

    public LinkPreviewDTO getLinkPreview(String url) {
        try {
            Document document = Jsoup.connect(url).get();

            String description = extractMetaTagContent(document, "description");
            if (description == null) {
                description = extractMetaTagContent(document, "og:description");
            }
            if (description == null) {
                description = extractMetaTagContent(document, "twitter:description");
            }

            String imageUrl = extractMetaTagContent(document, "og:image");
            if (imageUrl == null) {
                imageUrl = extractMetaTagContent(document, "twitter:image");
            }

            return LinkPreviewDTO.builder()
                .title(document.title())
                .description(description)
                .image(imageUrl)
                .content(document.body().html())
                .build();
        } catch (IOException e) {
            throw new UrlNotReadableException("Failed to fetch URL: " + url + ", " + e.getMessage());
        }
    }

    private String extractMetaTagContent(Document document, String attributeName) {
        Element metaTag = document.selectFirst("meta[name=" + attributeName + "], meta[property=" + attributeName + "]");
        return metaTag != null ? metaTag.attr("content") : null;
    }

    private EvaluatedPostContent evaluatePostContent(Post post) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("current_date", LocalDate.now());
        context.setVariable("username", currentUser.getUsername());
        context.setVariable("reply_to", post.getParent() != null ? post.getParent().getUser().getUsername() : null);

        String content = post.getContent();

        if (content != null && content.contains("#{") && content.contains("}#"))
            content = this.evaluateDynamicContent(parser, context, content);

        String parentContent = null;
        if (post.getParent() != null && post.getParent().getContent() != null) {
            parentContent = post.getParent().getContent();
            if (parentContent.contains("#{") && parentContent.contains("}#"))
                parentContent = this.evaluateDynamicContent(parser, context, parentContent);

        }

        return EvaluatedPostContent.builder()
            .content(content)
            .parentContent(parentContent)
            .build();
    }

    private String evaluateDynamicContent(ExpressionParser parser, StandardEvaluationContext context, String content) {
        try {
            return parser.parseExpression("'" + content.replace("#{", "'+#").replace("}#", "+'") + "'").getValue(context, String.class);
        } catch (Exception e) {
            log.warn("Failed to evaluate content expression: {}, {}", content, e.getMessage());
            return content;
        }
    }

    private PostDTO toPostDTO(Post post, String content, String parentContent, File file, User user, Post parent) {
        String userPicture = this.fileService.getProfilePicture(user.getId());

        UserDTO userDTO = UserDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .pictureUrl(FileType.IMAGE.getFolder() + "/" + userPicture)
            .role(user.getRole())
            .build();

        ParentDTO parentDTO = null;
        if (parent != null) {
            String parentUserPicture = this.fileService.getProfilePicture(user.getId());

            UserDTO parentUser = UserDTO.builder()
                .id(parent.getUser().getId())
                .username(parent.getUser().getUsername())
                .pictureUrl(FileType.IMAGE.getFolder() + "/" + parentUserPicture)
                .role(parent.getUser().getRole())
                .build();

            String parentFileUrl = null;
            if (parent.getFile() != null) {
                parentFileUrl = parent.getFile().getType().getFolder() + "/" +
                    parent.getFile().getName() + "." +
                    parent.getFile().getExtension().name().toLowerCase();
            }

            parentDTO = ParentDTO.builder()
                .user(parentUser)
                .content(parentContent)
                .fileUrl(parentFileUrl)
                .build();
        }

        String fileUrl = null;
        if (file != null) {
            fileUrl = file.getType().getFolder() + "/" +
                file.getName() + "." +
                file.getExtension().name().toLowerCase();
        }

        LastAction lastAction = post.getCreatedAt().equals(post.getUpdatedAt()) ?
            LastAction.CREATED : LastAction.MODIFIED;

        return PostDTO.builder()
            .id(post.getId())
            .user(userDTO)
            .parent(parentDTO)
            .content(content)
            .fileUrl(fileUrl)
            .lastAction(lastAction)
            .instant(post.getUpdatedAt())
            .build();
    }

    private Post findById(Long id) {
        return this.postRepository.findById(id)
            .orElseThrow(() -> new PostNotFoundException("Post not found"));
    }
}
