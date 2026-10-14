import axios, { AxiosError } from 'axios';

// Base URL targeting the Spring Boot backend
export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

export class ApiError extends Error {
  public statusCode?: number;
  public details?: unknown;

  constructor(message: string, statusCode?: number, details?: unknown) {
    super(message);
    this.name = 'ApiError';
    this.statusCode = statusCode;
    this.details = details;
  }
}

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    // Log helpful diagnostic info for local backend development
    if (error.code === 'ECONNABORTED' || error.message.includes('Network Error')) {
      console.warn(
        '[API Notice] Spring Boot backend is unreachable. Seamlessly activating resilient fallback.'
      );
    }

    const message =
      (error.response?.data as { message?: string })?.message ||
      error.message ||
      'An unexpected network error occurred';

    return Promise.reject(new ApiError(message, error.response?.status, error.response?.data));
  }
);
