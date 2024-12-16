package com.software.security.zeroday.service;

import com.software.security.zeroday.dto.enumeration.LastAction;
import com.software.security.zeroday.dto.post.*;
import com.software.security.zeroday.dto.user.UserDTO;
import com.software.security.zeroday.entity.File;
import com.software.security.zeroday.entity.Post;
import com.software.security.zeroday.entity.User;
import com.software.security.zeroday.entity.enumeration.FileType;
import com.software.security.zeroday.repository.PostRepository;
import com.software.security.zeroday.security.util.AuthorizationUtil;
import com.software.security.zeroday.security.util.SanitizationUtil;
import com.software.security.zeroday.service.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Transactional
@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final FileService fileService;
    private final AuthorizationUtil authorizationUtil;
    private final SanitizationUtil sanitizationUtil;

    @Value("${server.servlet.context-path}")
    private String CONTEXT_PATH;

    public PostDTO createPost(PostCreationDTO postDTO) {
        String sanitizedContent = this.sanitizationUtil.sanitizeString(postDTO.getContent());
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Post parent = null;
        if (postDTO.getParentId() != null)
            parent = this.findById(postDTO.getParentId());

        Instant now = Instant.now();

        Post post = Post.builder()
            .content(sanitizedContent)
            .createdAt(now)
            .updatedAt(now)
            .user(user)
            .parent(parent)
            .build();

        post = this.postRepository.save(post);

        File file = null;
        if (postDTO.getFileId() != null)
            file = this.fileService.finalizeFileUpload(postDTO.getFileId(), post);

        return toPostDTO(post, file, user, parent);
    }

    public Page<PostDTO> getAllPosts(Pageable pageable) {
        return this.postRepository.findAll(pageable)
                .map(post -> toPostDTO(post, post.getFile(), post.getUser(), post.getParent()));
    }

    public PostDTO getPost(Long id) {
        Post post = this.findById(id);

        return toPostDTO(post, post.getFile(), post.getUser(), post.getParent());
    }

    public PostDTO updatePost(Long id, PostModificationDTO postDTO) {
        Post post = this.findById(id);

        String sanitizedContent = this.sanitizationUtil.sanitizeString(postDTO.getContent());
        this.authorizationUtil.verifyAuthorization(post.getUser().getId());

        post.setContent(sanitizedContent);
        post.setUpdatedAt(Instant.now());
        post = this.postRepository.save(post);

        File file = post.getFile();
        if (postDTO.getFileId() != null)
            file = this.fileService.finalizeFileUpload(postDTO.getFileId(), post);

        return toPostDTO(post, file, post.getUser(), post.getParent());
    }

    public void deletePost(Long id) {
        Post post = this.findById(id);

        this.authorizationUtil.verifyAuthorization(post.getUser().getId());

        this.fileService.removePostFileAndAllDescendantsFiles(post);

        this.postRepository.delete(post);
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

    private PostDTO toPostDTO(Post post, File file, User user, Post parent) {
        String userPicture = this.fileService.getProfilePicture(user.getId());

        UserDTO userDTO = UserDTO.builder()
            .username(user.getUsername())
            .pictureUrl(this.CONTEXT_PATH + "/file/" + FileType.IMAGE.getFolder() + "/" + userPicture)
            .role(user.getRole())
            .build();

        ParentDTO parentDTO = null;
        if (parent != null) {
            String parentUserPicture = this.fileService.getProfilePicture(user.getId());

            UserDTO parentUser = UserDTO.builder()
                .username(parent.getUser().getUsername())
                .pictureUrl(this.CONTEXT_PATH + "/file/" + FileType.IMAGE.getFolder() + "/" + parentUserPicture)
                .role(parent.getUser().getRole())
                .build();

            String parentFileUrl = null;
            if (parent.getFile() != null) {
                parentFileUrl = this.CONTEXT_PATH + "/file/" +
                    parent.getFile().getType().getFolder() + "/" +
                    parent.getFile().getName() + "." +
                    parent.getFile().getExtension().name().toLowerCase();
            }

            parentDTO = ParentDTO.builder()
                .user(parentUser)
                .content(parent.getContent())
                .fileUrl(parentFileUrl)
                .build();
        }

        String fileUrl = null;
        if (file != null) {
            fileUrl = this.CONTEXT_PATH + "/file/" +
                file.getType().getFolder() + "/" +
                file.getName() + "." +
                file.getExtension().name().toLowerCase();
        }

        LastAction lastAction = post.getCreatedAt().equals(post.getUpdatedAt()) ?
            LastAction.CREATED : LastAction.MODIFIED;

        return PostDTO.builder()
            .id(post.getId())
            .user(userDTO)
            .parent(parentDTO)
            .content(post.getContent())
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
