export default function Avatar({ name, size = 40 }) {
  const initials = name
    ?.split(' ')
    .map(n => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2) || '??'

  return (
    <div className="avatar" style={{ width: size, height: size, fontSize: size * 0.35 }}>
      {initials}
    </div>
  )
}
