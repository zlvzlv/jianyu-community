/**
 * 极简 HTTP 客户端（本周演示用 fetch 实现）。
 *
 * 包 1 / packages/shared 的 Axios 封装落地后，把 request 换成 Axios 实例即可，
 * 上层的 api/、views/ 不用改动：这里已经统一处理了「统一响应体 + 业务错误码」。
 */
const TOKEN_KEY = 'jianyu.demo.token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

export class ApiError extends Error {
  constructor(code, message, status) {
    super(message)
    this.name = 'ApiError'
    this.code = code
    this.status = status
  }
}

export async function request(path, { method = 'GET', body, query } = {}) {
  const url = new URL(`/api${path}`, window.location.origin)
  if (query) {
    Object.entries(query).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        url.searchParams.set(key, value)
      }
    })
  }

  const headers = {}
  const token = getToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }

  let response
  try {
    response = await fetch(url, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body)
    })
  } catch (networkError) {
    throw new ApiError(-1, '网络异常，请确认后端已启动', 0)
  }

  let payload = null
  try {
    payload = await response.json()
  } catch (parseError) {
    throw new ApiError(-1, '服务返回内容不是合法 JSON', response.status)
  }

  if (!payload || payload.code !== 0) {
    throw new ApiError(payload?.code ?? -1, payload?.message || '请求失败', response.status)
  }
  return payload.data
}
