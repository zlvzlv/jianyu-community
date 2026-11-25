import { defineStore } from 'pinia'
import { demoLogin } from '../api/social'
import { getToken, setToken } from '../api/http'

const USER_KEY = 'jianyu.demo.user'

function readUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  } catch (error) {
    return null
  }
}

/**
 * 当前登录会话（本周为演示登录）。
 *
 * 正式版由包 1 提供 JWT 双 Token + 刷新逻辑，届时只替换 login / refresh 两个 action。
 */
export const useSessionStore = defineStore('session', {
  state: () => ({
    token: getToken(),
    user: readUser()
  }),

  getters: {
    isLoggedIn: (state) => Boolean(state.token && state.user),
    currentUserId: (state) => state.user?.userId ?? null
  },

  actions: {
    async login(username) {
      const data = await demoLogin(username)
      this.token = data.token
      this.user = data
      setToken(data.token)
      localStorage.setItem(USER_KEY, JSON.stringify(data))
      return data
    },

    logout() {
      this.token = ''
      this.user = null
      setToken('')
      localStorage.removeItem(USER_KEY)
    }
  }
})
