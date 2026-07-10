import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory('/mail/'),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue'),
    },
    {
      path: '/',
      component: () => import('../components/Layout.vue'),
      redirect: '/compose',
      children: [
        {
          path: 'contacts',
          name: 'Contacts',
          component: () => import('../views/Contacts.vue'),
          meta: { title: '通讯录' },
        },
        {
          path: 'templates',
          name: 'Templates',
          component: () => import('../views/Templates.vue'),
          meta: { title: '模板管理' },
        },
        {
          path: 'compose',
          name: 'Compose',
          component: () => import('../views/Compose.vue'),
          meta: { title: '新建邮件' },
        },
        {
          path: 'records',
          name: 'Records',
          component: () => import('../views/Records.vue'),
          meta: { title: '发送记录' },
        },
        {
          path: 'settings',
          name: 'Settings',
          component: () => import('../views/Settings.vue'),
          meta: { title: '系统设置' },
        },
      ],
    },
  ],
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const user = localStorage.getItem('user')
  if (to.path !== '/login' && !user) {
    next('/login')
  } else if (to.path === '/login' && user) {
    next('/compose')
  } else {
    next()
  }
})

export default router
