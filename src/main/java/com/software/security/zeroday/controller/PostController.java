package com.software.security.zeroday.controller;

import com.software.security.zeroday.dto.post.*;
import com.software.security.zeroday.service.PostService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("posts")
public class PostController {
    private final PostService postService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void createPost(@Valid @RequestBody PostCreationDTO postDTO) {
        this.postService.createPost(postDTO);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<PostDTO> getAllPosts(Pageable pageable) {
        return this.postService.getAllPosts(pageable);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PostDTO getPost(@PathVariable Long id) {
        return this.postService.getPost(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "raw/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RawPostDTO getRawPost(@PathVariable Long id) {
        return this.postService.getRawPost(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(path = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void updatePost(@PathVariable Long id, @Valid @RequestBody PostModificationDTO postDTO) {
        this.postService.updatePost(id, postDTO);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}")
    public void deletePost(@PathVariable Long id) {
        this.postService.deletePost(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "link/preview", produces = MediaType.APPLICATION_JSON_VALUE)
    public LinkPreviewDTO getLinkPreview(@RequestParam String url) {
        return this.postService.getLinkPreview(url);
    }
}
