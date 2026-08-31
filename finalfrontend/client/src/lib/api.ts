export const API_BASE = '/api';

export function getAuthHeader(): Record<string, string> {
  const token = localStorage.getItem('sih_token');
  return token ? { Authorization: "Bearer " + token } : {};
}

export async function fetchApi(endpoint: string, options: RequestInit = {}) {
  const headers: Record<string, string> = {
    ...getAuthHeader(),
    ...(options.body && !(options.body instanceof FormData) ? { 'Content-Type': 'application/json' } : {}),
    ...(options.headers as Record<string, string> | undefined),
  };

  const response = await fetch(API_BASE + endpoint, {
    ...options,
    headers,
  });

  if (!response.ok) {
    let errorMessage = 'An error occurred';
    try {
      const errorData = await response.json();
      errorMessage = errorData.message || errorMessage;
    } catch (e) {
      errorMessage = await response.text() || errorMessage;
    }
    throw new Error(errorMessage);
  }

  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

export const authApi = {
  login: (data: any) => fetchApi('/auth/login', {
    method: 'POST',
    body: JSON.stringify(data)
  }),
  register: (data: any) => fetchApi('/auth/register', {
    method: 'POST',
    body: JSON.stringify(data)
  })
};

export const dashboardApi = {
  getSummary: () => fetchApi('/dashboard/summary'),
  getViolations: () => fetchApi('/dashboard/violations')
};

export const productsApi = {
  getAll: () => fetchApi('/products'),
  getById: (id: string) => fetchApi('/products/' + id),
  create: (data: any) => fetchApi('/products', {
    method: 'POST',
    body: JSON.stringify(data)
  })
};

export const inspectionsApi = {
  getAll: () => fetchApi('/inspections'),
  getById: (id: string) => fetchApi('/inspections/' + id),
  create: (productId: string, files: FileList | File[]) => {
    const formData = new FormData();
    formData.append('productId', productId);
    Array.from(files).forEach(file => {
      formData.append('files', file);
    });
    
    return fetchApi('/inspections', {
      method: 'POST',
      body: formData
    });
  }
};

export const reportsApi = {
  getText: (id: string) => fetchApi('/reports/inspection/' + id),
  getPdfUrl: (id: string) => API_BASE + '/inspections/' + id + '/report/pdf',
  downloadPdf: async (id: string) => {
    const authHeader = getAuthHeader();
    const response = await fetch(API_BASE + '/inspections/' + id + '/report/pdf', {
      headers: { ...authHeader },
    });
    if (!response.ok) {
      throw new Error(`Failed to download PDF: ${response.status} ${response.statusText}`);
    }
    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `inspection_report_${id}.pdf`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }
};
