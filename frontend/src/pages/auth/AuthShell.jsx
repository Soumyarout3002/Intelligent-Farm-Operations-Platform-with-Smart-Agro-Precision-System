import { Sprout } from 'lucide-react';

export default function AuthShell({ children, wide }) {
  return (
    <div className="auth">
      <section className="auth-brand">
        <Sprout size={54} />
        <h1>FarmVerse Nexus</h1>
        <p className="tag">Grow smarter. Farm better.</p>
        <p className="sub">Intelligent Farm Operations Platform with Smart Agro Precision System</p>
      </section>
      <section className="auth-panel">
        <div className={`auth-card ${wide ? 'wide' : ''}`}>{children}</div>
      </section>
    </div>
  );
}
