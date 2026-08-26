export default function Button({ children, variant = 'primary', ...props }) {
  return (
    <button
      className={variant === 'primary' ? 'btn-primary' : 'btn-secondary'}
      {...props}
    >
      {children}
    </button>
  )
}
