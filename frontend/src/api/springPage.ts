/** Spring Data Page<T> JSON shape from the backend. */
export type SpringPage<T> = {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}
