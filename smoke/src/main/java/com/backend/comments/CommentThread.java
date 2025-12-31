package com.backend.comments;

import com.backend.entities.Comment;
import java.util.ArrayList;
import java.util.List;

public class CommentThread {
    private Comment comment;
    private List<CommentThread> replies;

    public CommentThread(Comment comment) {
        this.comment = comment;
        this.replies = new ArrayList<>();
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public List<CommentThread> getReplies() {
        return replies;
    }

    public void setReplies(List<CommentThread> replies) {
        this.replies = replies;
    }

    public void addReply(CommentThread reply) {
        this.replies.add(reply);
    }
}
