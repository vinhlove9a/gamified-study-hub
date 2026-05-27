export interface ApiFieldError {
  field: string;
  message: string;
  rejectedValue?: unknown;
}

export class ApiError extends Error {
  code: string;
  status: number;
  fieldErrors?: ApiFieldError[];

  constructor(params: { code: string; message: string; status: number; fieldErrors?: ApiFieldError[] }) {
    super(params.message);
    this.name = 'ApiError';
    this.code = params.code;
    this.status = params.status;
    this.fieldErrors = params.fieldErrors;
  }
}

