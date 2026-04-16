export default function PageHeader({ icon, title, subtitle, actions }) {
  return (
    <div className="d-flex flex-wrap justify-content-between align-items-center mb-4 pb-2 border-bottom">
      <div>
        <h2 className="text-coffee mb-0">
          {icon && <i className={`bi ${icon} me-2`}></i>}
          {title}
        </h2>
        {subtitle && <p className="text-muted mb-0 mt-1 small">{subtitle}</p>}
      </div>
      {actions && <div className="d-flex gap-2">{actions}</div>}
    </div>
  )
}
