// Small shared helper so every page calls the backend the same way.
const api = {
  async request(method, path, body) {
    const token = localStorage.getItem('loqma_token');
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers.Authorization = `Bearer ${token}`;

    const res = await fetch(API_BASE_URL + path, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
    if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
    return res.status === 204 ? null : res.json();
  },
  get:    (path)       => api.request('GET', path),
  post:   (path, body) => api.request('POST', path, body),
  put:    (path, body) => api.request('PUT', path, body),
  delete: (path)       => api.request('DELETE', path),
};
