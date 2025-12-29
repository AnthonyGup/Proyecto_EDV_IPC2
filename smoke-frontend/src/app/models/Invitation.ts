export interface Invitation {
  invitationId?: number;
  userId: string;
  familyGroupId: number;
  status?: 'PENDING' | 'ACCEPTED' | 'REJECTED';
  createdAt?: string;
}
