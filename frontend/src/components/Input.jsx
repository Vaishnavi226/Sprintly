export default function Input({ label, ...props }) {
  return (
    <div className="form-group">
      {label && <label className="form-label">{label}</label>}
      <input className="input-field" {...props} />
    </div>
  )
}
