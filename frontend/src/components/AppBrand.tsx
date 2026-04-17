type AppBrandProps = {
  className?: string
}

export function AppBrand({ className }: AppBrandProps) {
  const rootClass = ['dash-brand', className].filter(Boolean).join(' ')
  return (
    <div className={rootClass}>
      <img className="dash-brand__mark" src="/autoconfig-mark.png" alt="" width={40} height={40} decoding="async" />
      <span className="dash-brand__name">AutoConfig</span>
    </div>
  )
}
