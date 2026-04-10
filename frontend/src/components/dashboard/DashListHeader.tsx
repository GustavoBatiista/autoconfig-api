import type { ReactNode } from 'react'

type DashListHeaderProps = {
  title: string
  actions?: ReactNode
}

export function DashListHeader({ title, actions }: DashListHeaderProps) {
  return (
    <div className="dash-list-header">
      <h2 className="dash-page__heading dash-list-header__title">{title}</h2>
      {actions ? <div className="dash-list-header__actions">{actions}</div> : null}
    </div>
  )
}
