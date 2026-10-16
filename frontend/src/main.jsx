import React, { useEffect, useState } from 'react'
import { createRoot } from 'react-dom/client'
import './styles.css'

const money = new Intl.NumberFormat('tr-TR', { style: 'currency', currency: 'TRY' })
const dateTime = new Intl.DateTimeFormat('tr-TR', { dateStyle: 'medium', timeStyle: 'short' })

async function api(path, options = {}) {
  const response = await fetch(path, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options
  })
  if (response.status === 204) return null
  const data = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(data.message || (response.status === 401 ? 'Invalid username or password' : 'Something went wrong'))
  return data
}

function App() {
  const [account, setAccount] = useState(null)
  const [checking, setChecking] = useState(true)
  const [page, setPage] = useState('dashboard')

  useEffect(() => {
    api('/api/auth/me').then(setAccount).catch(() => {}).finally(() => setChecking(false))
  }, [])

  if (checking) return <div className="loading">Opening NovaBank…</div>
  if (!account) return <AuthPage onAuthenticated={setAccount} />

  async function logout() {
    await api('/api/auth/logout', { method: 'POST' })
    setAccount(null)
    setPage('dashboard')
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <Brand />
        <nav>
          <NavButton active={page === 'dashboard'} onClick={() => setPage('dashboard')} icon="⌂">Dashboard</NavButton>
          <NavButton active={page === 'transfer'} onClick={() => setPage('transfer')} icon="↗">Transfer</NavButton>
          <NavButton active={page === 'history'} onClick={() => setPage('history')} icon="≡">History</NavButton>
        </nav>
        <button className="logout" onClick={logout}>Sign out</button>
      </aside>
      <main>
        <header className="topbar">
          <button className="mobile-brand" onClick={() => setPage('dashboard')}>N</button>
          <div>
            <span className="eyebrow">PERSONAL BANKING</span>
            <h1>{page === 'dashboard' ? `Welcome, ${account.username}` : page === 'transfer' ? 'Send money' : 'Transactions'}</h1>
          </div>
          <div className="avatar">{account.username.charAt(0).toUpperCase()}</div>
        </header>

        {page === 'dashboard' && <Dashboard account={account} setAccount={setAccount} goTo={setPage} />}
        {page === 'transfer' && <Transfer account={account} setAccount={setAccount} />}
        {page === 'history' && <History />}

        <nav className="mobile-nav">
          <NavButton active={page === 'dashboard'} onClick={() => setPage('dashboard')} icon="⌂">Home</NavButton>
          <NavButton active={page === 'transfer'} onClick={() => setPage('transfer')} icon="↗">Transfer</NavButton>
          <NavButton active={page === 'history'} onClick={() => setPage('history')} icon="≡">History</NavButton>
          <NavButton onClick={logout} icon="×">Exit</NavButton>
        </nav>
      </main>
    </div>
  )
}

function Brand() {
  return <div className="brand"><span>N</span><strong>NovaBank</strong></div>
}

function NavButton({ active, icon, children, onClick }) {
  return <button className={`nav-button ${active ? 'active' : ''}`} onClick={onClick}><i>{icon}</i>{children}</button>
}

function AuthPage({ onAuthenticated }) {
  const [mode, setMode] = useState('login')
  const [form, setForm] = useState({ username: '', password: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function submit(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      const result = await api(`/api/auth/${mode}`, { method: 'POST', body: JSON.stringify(form) })
      onAuthenticated(result)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  function switchMode() {
    setMode(mode === 'login' ? 'register' : 'login')
    setError('')
  }

  return (
    <div className="auth-layout">
      <section className="auth-hero">
        <Brand />
        <div className="hero-copy">
          <span className="eyebrow">BANKING, SIMPLIFIED</span>
          <h1>Move money.<br /><em>Move forward.</em></h1>
          <p>A straightforward place for your everyday banking.</p>
        </div>
        <div className="security-note"><span>✓</span> Secure account access</div>
      </section>
      <section className="auth-panel">
        <form className="auth-card" onSubmit={submit}>
          <div className="auth-heading">
            <span className="mini-mark">N</span>
            <h2>{mode === 'login' ? 'Good to see you' : 'Open your account'}</h2>
            <p>{mode === 'login' ? 'Enter your details to continue.' : 'Start with a 1.000 TL welcome balance.'}</p>
          </div>
          {error && <div className="alert error">{error}</div>}
          <label>Username<input autoFocus value={form.username} minLength="3" maxLength="50" pattern="[A-Za-z0-9_]+" required onChange={e => setForm({ ...form, username: e.target.value })} placeholder="Your username" /></label>
          <label>Password<input type="password" value={form.password} minLength="6" maxLength="100" required onChange={e => setForm({ ...form, password: e.target.value })} placeholder="At least 6 characters" /></label>
          <button className="primary wide" disabled={busy}>{busy ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create account'}</button>
          <p className="switch-copy">{mode === 'login' ? 'New to NovaBank?' : 'Already have an account?'} <button type="button" onClick={switchMode}>{mode === 'login' ? 'Create one' : 'Sign in'}</button></p>
        </form>
      </section>
    </div>
  )
}

function Dashboard({ account, setAccount, goTo }) {
  const [action, setAction] = useState(null)
  return (
    <div className="page-content">
      <section className="balance-card">
        <div>
          <span>AVAILABLE BALANCE</span>
          <strong>{money.format(account.balance)}</strong>
          <small>Turkish Lira</small>
        </div>
        <div className="card-mark">N</div>
        <footer><span>ACCOUNT NUMBER</span><b>{account.accountNumber}</b></footer>
      </section>
      <section className="quick-section">
        <div className="section-title"><div><span className="eyebrow">QUICK ACTIONS</span><h2>What would you like to do?</h2></div></div>
        <div className="action-grid">
          <ActionCard icon="＋" title="Deposit" text="Add money to your balance" onClick={() => setAction('deposit')} />
          <ActionCard icon="−" title="Withdraw" text="Take money from your balance" onClick={() => setAction('withdraw')} />
          <ActionCard icon="↗" title="Transfer" text="Send money to another account" onClick={() => goTo('transfer')} />
          <ActionCard icon="≡" title="History" text="Review your transactions" onClick={() => goTo('history')} />
        </div>
      </section>
      {action && <MoneyDialog action={action} onClose={() => setAction(null)} onComplete={setAccount} />}
    </div>
  )
}

function ActionCard({ icon, title, text, onClick }) {
  return <button className="action-card" onClick={onClick}><span>{icon}</span><div><strong>{title}</strong><small>{text}</small></div><i>→</i></button>
}

function MoneyDialog({ action, onClose, onComplete }) {
  const [amount, setAmount] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  async function submit(event) {
    event.preventDefault(); setBusy(true); setError('')
    try {
      const result = await api(`/api/account/${action}`, { method: 'POST', body: JSON.stringify({ amount }) })
      onComplete(result); onClose()
    } catch (err) { setError(err.message) } finally { setBusy(false) }
  }
  return <div className="modal-backdrop" onMouseDown={e => e.target === e.currentTarget && onClose()}>
    <form className="modal" onSubmit={submit}>
      <button type="button" className="close" onClick={onClose}>×</button>
      <span className="modal-icon">{action === 'deposit' ? '＋' : '−'}</span>
      <h2>{action === 'deposit' ? 'Deposit money' : 'Withdraw money'}</h2>
      <p>Enter the amount in Turkish Lira.</p>
      {error && <div className="alert error">{error}</div>}
      <label>Amount<input autoFocus type="number" min="0.01" step="0.01" required value={amount} onChange={e => setAmount(e.target.value)} placeholder="0,00" /></label>
      <button className="primary wide" disabled={busy}>{busy ? 'Processing…' : 'Confirm'}</button>
    </form>
  </div>
}

function Transfer({ account, setAccount }) {
  const [form, setForm] = useState({ targetAccountNumber: '', amount: '' })
  const [message, setMessage] = useState(null)
  const [busy, setBusy] = useState(false)
  async function submit(event) {
    event.preventDefault(); setBusy(true); setMessage(null)
    try {
      const result = await api('/api/account/transfer', { method: 'POST', body: JSON.stringify(form) })
      setAccount(result); setForm({ targetAccountNumber: '', amount: '' }); setMessage({ type: 'success', text: 'Transfer completed successfully.' })
    } catch (err) { setMessage({ type: 'error', text: err.message }) } finally { setBusy(false) }
  }
  return <div className="page-content narrow">
    <section className="form-card">
      <div className="form-card-head"><span>↗</span><div><h2>New transfer</h2><p>Send Turkish Lira to another NovaBank account.</p></div></div>
      {message && <div className={`alert ${message.type}`}>{message.text}</div>}
      <form onSubmit={submit}>
        <label>Recipient account number<input value={form.targetAccountNumber} required onChange={e => setForm({ ...form, targetAccountNumber: e.target.value })} placeholder="TR0000000000000000" /></label>
        <label>Amount (TL)<input type="number" min="0.01" step="0.01" value={form.amount} required onChange={e => setForm({ ...form, amount: e.target.value })} placeholder="0,00" /></label>
        <div className="available"><span>Available to send</span><b>{money.format(account.balance)}</b></div>
        <button className="primary wide" disabled={busy}>{busy ? 'Sending…' : 'Send money'}</button>
      </form>
    </section>
  </div>
}

function History() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  useEffect(() => { api('/api/account/transactions').then(setItems).catch(e => setError(e.message)).finally(() => setLoading(false)) }, [])
  return <div className="page-content">
    <section className="history-card">
      <div className="section-title"><div><span className="eyebrow">ACCOUNT ACTIVITY</span><h2>Transaction history</h2></div><span className="count">{items.length} entries</span></div>
      {loading && <div className="empty">Loading transactions…</div>}
      {error && <div className="alert error">{error}</div>}
      {!loading && !error && items.length === 0 && <div className="empty">No transactions yet.</div>}
      <div className="transaction-list">
        {items.map(item => {
          const incoming = item.direction === 'IN'
          const label = item.type === 'INITIAL_BALANCE' ? 'Opening balance' : item.type === 'TRANSFER' ? (incoming ? 'Transfer received' : 'Transfer sent') : item.type.charAt(0) + item.type.slice(1).toLowerCase()
          return <article className="transaction" key={item.id}>
            <span className={`transaction-icon ${incoming ? 'in' : 'out'}`}>{incoming ? '↓' : '↑'}</span>
            <div className="transaction-copy"><strong>{label}</strong><small>{item.otherAccountNumber || 'NovaBank'} · {dateTime.format(new Date(item.createdAt))}</small></div>
            <b className={incoming ? 'positive' : 'negative'}>{incoming ? '+' : '−'}{money.format(item.amount)}</b>
          </article>
        })}
      </div>
    </section>
  </div>
}

createRoot(document.getElementById('root')).render(<React.StrictMode><App /></React.StrictMode>)

