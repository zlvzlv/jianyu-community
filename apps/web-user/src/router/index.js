import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/u/1' },
  { path: '/login', name: 'login', component: () => import('../views/LoginView.vue') },
  {
    path: '/u/:userId',
    name: 'profile',
    component: () => import('../views/ProfileView.vue'),
    props: (route) => ({ userId: Number(route.params.userId) })
  },
  {
    path: '/u/:userId/followers',
    name: 'followers',
    component: () => import('../views/FollowListView.vue'),
    props: (route) => ({ userId: Number(route.params.userId), tab: 'followers' })
  },
  {
    path: '/u/:userId/following',
    name: 'following',
    component: () => import('../views/FollowListView.vue'),
    props: (route) => ({ userId: Number(route.params.userId), tab: 'following' })
  }
]

export default createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})
