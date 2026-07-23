export type UserRole = 'CLIENTE' | 'PROPIETARIO' | 'ADMINISTRADOR';
export type UserStatus = 'ACTIVO' | 'SUSPENDIDO' | 'INACTIVO';
export type OwnerVerificationStatus = 'NO_SOLICITADA' | 'PENDIENTE' | 'APROBADA' | 'RECHAZADA';
export type OwnerRequestDecision = 'APROBAR' | 'RECHAZAR';
export type IdentityDocumentType = 'CEDULA' | 'PASAPORTE' | 'LICENCIA_CONDUCIR';

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

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  phone: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
  user: User;
}

export interface ProfileUpdateRequest {
  fullName: string;
  phone: string;
}

export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
}

export interface OwnerVerificationRequest {
  id: string;
  userId: string;
  userFullName: string;
  userEmail: string;
  documentType: IdentityDocumentType | null;
  identification: string;
  documentReference: string;
  hasDocument: boolean;
  notes: string;
  status: Exclude<OwnerVerificationStatus, 'NO_SOLICITADA'>;
  submittedAt: string;
  reviewedAt: string | null;
  reviewerName: string | null;
  adminComment: string | null;
}

export interface OwnerRequestReviewRequest {
  decision: OwnerRequestDecision;
  comment: string;
}
