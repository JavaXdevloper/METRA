// Field Ledger direction: evidence-first hierarchy, ink navy + paper + compliance amber, editorial serif with sans body, mono metadata, and no generic rounded dashboard treatment.
import axios from 'axios';

export const api = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL || '/api', timeout: 20000 });

export const authApi = { login: (officerId: string, password: string) => api.post('/auth/login', { officerId, password }) };
export const dashboardApi = { getStats: () => api.get('/dashboard/stats') };
export const inspectionsApi = { list: (params?: Record<string, string>) => api.get('/inspections', { params }), get: (id: string) => api.get(`/inspections/${id}`), create: (formData: FormData) => api.post('/inspections', formData, { headers: { 'Content-Type': 'multipart/form-data' } }) };
export const productsApi = { search: (query: string) => api.get('/products', { params: { query } }) };
export const reportsApi = { list: () => api.get('/reports'), download: (id: string) => api.get(`/reports/${id}/pdf`, { responseType: 'blob' }), exportEditable: (id: string) => api.get(`/reports/${id}/editable`, { responseType: 'blob' }) };
