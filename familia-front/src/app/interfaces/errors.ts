export interface ErrorResponse {
  status: string;
  code: number;
  timestamp: string;
  path: string;
  message?: string;
  errors?: { [key: string]: string };
}

export interface ValidationErrors {
  [key: string]: string;
}