export interface ImportRowResult {
  rowNumber: number;
  rawData: string;
  valid: boolean;
  errors: string[];
  resolvedType: string | null;
  resolvedDescription: string | null;
  resolvedAmount: string | null;
  resolvedDate: string | null;
}

export interface ImportPreviewResponse {
  totalRows: number;
  validRows: number;
  invalidRows: number;
  rows: ImportRowResult[];
}

export interface ImportExecuteResponse {
  imported: number;
  skipped: number;
  message: string;
}
