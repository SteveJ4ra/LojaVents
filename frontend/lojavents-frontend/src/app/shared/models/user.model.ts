export type UserRole = 'CLIENTE' | 'PROPIETARIO' | 'ADMIN';
export type UserStatus = 'ACTIVO' | 'SUSPENDIDO' | 'INACTIVO';
export type OwnerVerificationStatus = 'NO_SOLICITADA' | 'PENDIENTE' | 'APROBADA' | 'RECHAZADA';

export interface User {
  id: string;
  fullName: string;
  email: string;
  phone: string;
  roles: UserRole[];
  status: UserStatus;
  ownerVerificationStatus: OwnerVerificationStatus;
  createdAt: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  phone: string;
  password: string;
}

export interface OwnerVerificationRequest {
  userId: string;
  identification: string;
  documentReference: string;
  notes: string;
  submittedAt: string;
}
