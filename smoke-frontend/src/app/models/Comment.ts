export interface Comment {
  userId: string;
  gameId: number;
  text: string;
  parentId: number;
  comentId: number;
}

export interface CommentThread {
  comment: Comment;
  replies: CommentThread[];
}