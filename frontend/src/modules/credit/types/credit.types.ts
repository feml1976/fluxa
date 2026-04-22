export type CreditType = 'CREDIT_CARD' | 'PERSONAL' | 'MORTGAGE' | 'VEHICLE';
export type CreditStatus = 'ACTIVE' | 'PAID' | 'REFINANCED' | 'CANCELLED';
export type CardBrand = 'VISA' | 'MASTERCARD' | 'AMEX' | 'DINERS' | 'OTHER';
export type CreditAlertLevel = 'GREEN' | 'YELLOW' | 'RED';

export interface CreditCardDetailRequest {
  cardNumberLast4: string;
  brand: CardBrand;
  creditLimitPurchases: number;
  creditLimitAdvances: number;
  availablePurchases: number;
  availableAdvances: number;
  previousBalance: number;
  minimumPayment: number;
  alternateMinimumPayment: number;
  lateInterest: number;
  paymentDueDay: number;
}

export interface CreditCardDetailResponse extends CreditCardDetailRequest {
  utilizationPct: number;
}

export interface CreditRequest {
  type: CreditType;
  status?: CreditStatus;
  name: string;
  description?: string;
  interestRateMv: number;
  currentBalance: number;
  monthlyInstallment?: number;
  totalInstallments?: number;
  paidInstallments?: number;
  openingDate: string;
  closingDate?: string;
  cardDetail?: CreditCardDetailRequest;
}

export interface CreditResponse {
  id: number;
  type: CreditType;
  status: CreditStatus;
  name: string;
  description: string | null;
  interestRateMv: number;
  interestRateEa: number;
  currentBalance: number;
  monthlyInstallment: number | null;
  totalInstallments: number | null;
  paidInstallments: number;
  remainingInstallments: number | null;
  openingDate: string;
  closingDate: string | null;
  alertLevel: CreditAlertLevel;
  cardDetail: CreditCardDetailResponse | null;
}

export interface AmortizationRow {
  installmentNumber: number;
  installmentAmount: number;
  interestPortion: number;
  capitalPortion: number;
  remainingBalance: number;
}

export interface CreditAnalysisResponse {
  creditId: number;
  alertLevel: CreditAlertLevel;
  alerts: string[];
  utilizationPct: number | null;
  monthsToPayMinimum: number | null;
  totalInterestWithMinimum: number | null;
  alternateMinimumWarning: boolean;
  remainingInstallments: number | null;
  projectedPayoffDate: string | null;
  totalRemainingInterest: number | null;
  amortizationTable: AmortizationRow[] | null;
}

export interface CreditPaymentRequest {
  month: number;
  year: number;
  amount: number;
  paymentDate: string;
  notes?: string;
}

export interface CreditPaymentResponse {
  id: number;
  creditId: number;
  periodMonth: number;
  periodYear: number;
  amount: number;
  paymentDate: string;
  notes: string | null;
}

export interface CreditSummaryResponse {
  totalCredits: number;
  activeCredits: number;
  totalDebt: number;
  totalMonthlyObligations: number;
  cardUsedBalance: number;
  cardMinimumPayments: number;
  cardsWithLateInterest: number;
  cardsAtMaxCapacity: number;
}
