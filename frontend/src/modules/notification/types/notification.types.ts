export interface NotificationLog {
  id: number;
  eventType: string;
  referenceName: string | null;
  recipient: string;
  subject: string;
  sentAt: string;
  success: boolean;
  errorMessage: string | null;
}
