import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { LoginResponse } from '../utils/types'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const role = ref(localStorage.getItem('role') || '')

  function setLogin(data: LoginResponse) {
    token.value = data.token
    username.value = data.username
    role.value = data.role
    localStorage.setItem('token', data.token)
    localStorage.setItem('username', data.username)
    localStorage.setItem('role', data.role)
  }

  function logout() {
    token.value = ''
    username.value = ''
    role.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
  }

  const isAdmin = () => role.value === 'ADMIN'

  return { token, username, role, setLogin, logout, isAdmin }
})
