export interface FamilyGroup {
  groupId?: number;
  /** Nombre de visualización; algunos endpoints usan groupName, otros name. */
  name?: string;
  groupName?: string;
  description?: string;
  ownerId: string;
}