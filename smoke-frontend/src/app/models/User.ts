export interface User {
  nickname: string;
  password: string;
  mail: string;
  birthdate: string; // Date as string in ISO format
  type: 'SYSTEM_ADMIN' | 'COMPANY_ADMIN' | 'GAMER';
}