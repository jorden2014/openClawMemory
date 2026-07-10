import request from '../utils/request'

// 图片信息接口
export interface ImageInfo {
  url: string
  filename: string
  size: number
  uploadTime: string
}

// 图片列表响应
export interface ImageListResponse {
  items: ImageInfo[]
  total: number
  page: number
  pageSize: number
}

// 获取图片列表
export function getImageList(page: number = 1, pageSize: number = 12) {
  return request({
    url: '/upload/list',
    method: 'get',
    params: { page, pageSize }
  })
}

// 删除图片
export function deleteImage(imageUrl: string) {
  return request({
    url: '/upload/delete',
    method: 'post',
    data: { url: imageUrl }
  })
}

// 上传图片（使用 FormData，直接调用，不走 request 封装）
export function uploadImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return fetch('/api/upload/image', {
    method: 'POST',
    body: formData
  }).then(res => res.json())
}
