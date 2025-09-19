package io.unbyte.sandbox.domain.model;

import java.util.List;

/**
 * Domain model for Post
 * Represents a post with its comments
 */
public class Post {
    private final String id;
    private final List<Comment> comments;

    public Post(String id, List<Comment> comments) {
        this.id = id;
        this.comments = comments;
    }

    public String getId() {
        return id;
    }

    public List<Comment> getComments() {
        return comments;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id='" + id + '\'' +
                ", comments=" + comments +
                '}';
    }
}
