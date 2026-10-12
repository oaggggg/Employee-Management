import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 所有接口都从这里走：baseURL 用 /api，由 vite 代理转发到后端 8080
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器：自动带上 token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理后端的 { code, message, data } 信封
request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 成功时直接把 data 返回，业务代码里拿到的就是干净数据
    if (res.code === 200) {
      return res.data
    }

    // 401：未登录或登录过期 → 清 token 并回登录页
    if (res.code === 401) {
      localStorage.removeItem('token')
      ElMessage.error(res.message || '登录已过期，请重新登录')
      router.push('/login')
      return Promise.reject(new Error(res.message || '登录已过期'))
    }

    // 其它错误码：提示后端返回的 message
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    // 网络异常、超时、后端没启动等
    let message = '网络异常，请稍后重试'
    if (error.code === 'ECONNABORTED') {
      message = '请求超时，请稍后重试'
    } else if (error.response) {
      message = `请求失败（${error.response.status}）`
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
