import { request } from './http'

/** 【临时】演示登录：正式实现由包 1 提供 POST /api/v1/auth/login。 */
export function demoLogin(username) {
  return request('/v1/auth/demo-login', { method: 'POST', body: { username } })
}

/** 个人主页聚合：用户资料 + 关注数/粉丝数 + 相对访问者的关注状态。 */
export function fetchProfile(userId) {
  return request(`/v1/users/${userId}/profile`)
}

/** 关注某人（幂等）。 */
export function followUser(userId) {
  return request(`/v1/users/${userId}/follow`, { method: 'POST' })
}

/** 取关某人（幂等）。 */
export function unfollowUser(userId) {
  return request(`/v1/users/${userId}/follow`, { method: 'DELETE' })
}

/** 关注/粉丝列表。tab: followers | following */
export function fetchFollowList(userId, tab, page = 1, pageSize = 20) {
  const path = tab === 'followers' ? 'followers' : 'following'
  return request(`/v1/users/${userId}/${path}`, { query: { page, pageSize } })
}
