import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CommentThread } from '../../models';
import { RouterModule } from '@angular/router';

@Component({
  standalone: true,
  selector: 'app-comment-thread',
  templateUrl: './comment-thread.component.html',
  styleUrls: ['./comment-thread.component.css'],
  imports: [CommonModule, RouterModule]
})
export class CommentThreadComponent {
  @Input() thread!: CommentThread;
  @Input() depth: number = 0;
  @Input() allowReply = false;
  @Input() nicknames: Record<string, string> | null = null;
  @Output() reply = new EventEmitter<number>();

  onReply(): void {
    if (this.thread?.comment?.comentId !== undefined) {
      this.reply.emit(this.thread.comment.comentId);
    }
  }
}
