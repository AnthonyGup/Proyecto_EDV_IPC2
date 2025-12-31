import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/appi.config';
import { FamilyGroup, GroupMember, Invitation } from '../models';

interface CreateFamilyGroupResponse {
  message: string;
  groupId?: number;
}

interface MessageResponse {
  message: string;
}

@Injectable({ providedIn: 'root' })
export class FamilyGroupService {
  constructor(private http: HttpClient) {}

  createGroup(groupName: string, ownerId: string, description?: string): Observable<CreateFamilyGroupResponse> {
    const payload: Partial<FamilyGroup> & { ownerId: string } = {
      // Enviar ambos campos de nombre para compatibilidad con backend/DB
      name: groupName,
      groupName,
      ownerId,
      description
    };
    return this.http.post<CreateFamilyGroupResponse>(`${API_BASE_URL}/family-group`, payload);
  }

  getGroupsByUser(userId: string): Observable<FamilyGroup[]> {
    const params = new HttpParams().set('userId', userId).set('ownerId', userId);
    return this.http.get<FamilyGroup[]>(`${API_BASE_URL}/family-group`, { params });
  }

  sendInvitation(groupId: number, email: string, inviter: string): Observable<MessageResponse> {
    const payload: Partial<Invitation> & { email: string; inviter: string; groupId: number } = {
      familyGroupId: groupId,
      groupId,
      email,
      inviter,
      userId: email
    };
    return this.http.post<MessageResponse>(`${API_BASE_URL}/family-group/invite`, payload);
  }

  getInvitations(userEmail: string): Observable<(Invitation & { groupName?: string; ownerId?: string })[]> {
    return this.http.get<(Invitation & { groupName?: string; ownerId?: string })[]>(
      `${API_BASE_URL}/family-group/invitations`,
      { params: { userEmail } }
    );
  }

  respondInvitation(invitationId: number, action: 'accept' | 'reject', userEmail: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${API_BASE_URL}/family-group/invitations`, {
      invitationId,
      action,
      userEmail
    });
  }

  getMembers(groupId: number): Observable<GroupMember[]> {
    const params = new HttpParams().set('groupId', groupId);
    return this.http.get<GroupMember[]>(`${API_BASE_URL}/family-group/members`, { params });
  }

  deleteGroup(groupId: number, userId: string): Observable<MessageResponse> {
    const params = new HttpParams().set('groupId', groupId).set('userId', userId);
    return this.http.delete<MessageResponse>(`${API_BASE_URL}/family-group/delete`, { params });
  }

  removeMember(groupId: number, memberEmail: string, ownerEmail: string): Observable<MessageResponse> {
    const params = new HttpParams()
      .set('groupId', groupId)
      .set('memberEmail', memberEmail)
      .set('ownerEmail', ownerEmail);
    return this.http.delete<MessageResponse>(`${API_BASE_URL}/family-group/member`, { params });
  }
}
