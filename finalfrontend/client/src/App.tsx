// Field Ledger direction: evidence-first hierarchy, ink navy + paper + compliance amber, editorial serif with sans body, mono metadata, and no generic rounded dashboard treatment.
import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, Route, Switch, useLocation, useRoute } from 'wouter';
import { Toaster, toast } from 'sonner';
import { ArrowLeft, ArrowRight, BarChart3, Bell, Check, ChevronRight, ClipboardCheck, Clock3, Download, FileCheck2, FileText, Filter, History, Image as ImageIcon, LayoutDashboard, LogOut, Menu, PackageCheck, Plus, Search, ShieldCheck, Sparkles, TriangleAlert, Upload, X, ZoomIn } from 'lucide-react';
import { authApi, dashboardApi, inspectionsApi, productsApi, reportsApi } from './lib/api';

const heroImage = '/sih-ledger-hero.jpg';
const evidenceImage = '/sih-evidence-texture.jpg';
const logoImage = '/sih-field-ledger-mark.png';

type Status = 'COMPLIANT' | 'NON_COMPLIANT' | 'NEEDS_REVIEW';
type Declaration = { label: string; value: string; status: 'detected' | 'missing' };
type Violation = { type: string; description: string; ruleReference?: string; rule?: string; severity?: 'high' | 'medium' };
type Inspection = { id: string; productId: string; product?: string; manufacturer?: string; createdAt: string; complianceStatus: Status; images: string[]; extractedDeclarations: Record<string, string>; violations: Violation[]; userId: string; location?: string; ocrData?: { confidence: number } };

const statusMeta: Record<Status, { label: string; className: string; icon: typeof Check }> = {
  COMPLIANT: { label: 'Compliant', className: 'status-good', icon: Check },
  'NON_COMPLIANT': { label: 'Non-compliant', className: 'status-bad', icon: TriangleAlert },
  'NEEDS_REVIEW': { label: 'Needs review', className: 'status-warn', icon: Clock3 },
};

const USERNAME_KEY = 'sih_username';
function getSessionUsername() { return localStorage.getItem(USERNAME_KEY) || 'Officer'; }
function getInitials(username: string) { const cleaned = username.trim(); if (!cleaned) return 'OF'; const parts = cleaned.split(/\s+/).filter(Boolean); return (parts.length > 1 ? `${parts[0][0]}${parts[parts.length - 1][0]}` : cleaned.slice(0, 2)).toUpperCase(); }

function StatusBadge({ status }: { status: Status }) { const m = statusMeta[status] || statusMeta['NEEDS_REVIEW']; const Icon = m.icon; return <span className={`status-badge ${m.className}`}><Icon size={13} />{m.label}</span>; }
function Mono({ children }: { children: React.ReactNode }) { return <span className="mono">{children}</span>; }
function PageHeading({ kicker, title, description, action }: { kicker: string; title: string; description?: string; action?: React.ReactNode }) { return <div className="page-heading"><div><div className="eyebrow">{kicker}</div><h1>{title}</h1>{description && <p>{description}</p>}</div>{action}</div>; }
function Sidebar({ onLogout }: { onLogout: () => void }) { const [location] = useLocation(); const [profileOpen, setProfileOpen] = useState(false); const profileRef = useRef<HTMLDivElement>(null); const username = getSessionUsername(); useEffect(() => { if (!profileOpen) return; const handlePointerDown = (event: PointerEvent) => { if (!profileRef.current?.contains(event.target as Node)) setProfileOpen(false); }; document.addEventListener('pointerdown', handlePointerDown); return () => document.removeEventListener('pointerdown', handlePointerDown); }, [profileOpen]); const items = [{ href: '/dashboard', label: 'Dashboard', icon: LayoutDashboard }, { href: '/inspection/new', label: 'New inspection', icon: Plus }, { href: '/inspections', label: 'Inspection history', icon: History }, { href: '/products', label: 'Products', icon: PackageCheck }, { href: '/reports', label: 'Reports', icon: FileText }]; return <aside className="sidebar"><div className="brand"><img src={logoImage} /><div><strong>METRO<span>LOG</span></strong><small>SIH 2026 · 26034</small></div></div><div className="rail-label">Enforcement workspace</div><nav>{items.map(({ href, label, icon: Icon }) => <Link key={href} href={href} className={`nav-item ${location === href || (href !== '/dashboard' && location.startsWith(href)) ? 'active' : ''}`}><Icon size={17} /><span>{label}</span>{location === href && <ChevronRight size={15} className="nav-arrow" />}</Link>)}</nav><div className="sidebar-bottom"><div className="profile-anchor" ref={profileRef}>{profileOpen && <div className="profile-menu" role="menu"><button className="profile-menu-item" role="menuitem" onClick={() => { setProfileOpen(false); toast.info('Change password is available from account settings.'); }}>Change password</button><button className="profile-menu-item logout-item" role="menuitem" onClick={() => { setProfileOpen(false); onLogout(); }}><LogOut size={16} /> Sign out</button></div>}<button type="button" className="officer" onClick={() => setProfileOpen(open => !open)} aria-expanded={profileOpen} aria-haspopup="menu"><div className="avatar">{getInitials(username)}</div><div><strong>{username}</strong></div><Bell size={16} /></button></div></div></aside>; }
function Topbar({ title }: { title: string }) { return <header className="topbar"><div className="topbar-title"><span className="mobile-menu"><Menu size={20} /></span><span>{title}</span></div><div className="topbar-right"><div className="live-dot"><span /> System operational</div><div className="topbar-divider" /><Mono>30 AUG 2026</Mono></div></header>; }
function Shell({ children, title, onLogout }: { children: React.ReactNode; title: string; onLogout: () => void }) { return <div className="app-shell"><Sidebar onLogout={onLogout} /><div className="main-shell"><Topbar title={title} /><main className="content">{children}</main></div></div>; }
function Card({ children, className = '', ...props }: React.HTMLAttributes<HTMLElement> & { children: React.ReactNode; className?: string }) { return <section className={`card ${className}`} {...props}>{children}</section>; }
function DashboardCard({ label, value, note, icon: Icon, tone }: { label: string; value: string; note: string; icon: typeof Check; tone: string }) { const [active, setActive] = useState(false); return <Card className={`metric-card ${tone}`} tabIndex={0} onMouseEnter={() => setActive(true)} onMouseLeave={() => setActive(false)} onFocus={() => setActive(true)} onBlur={() => setActive(false)} style={{ transform: active ? 'translateY(-5px) scale(1.015)' : 'translateY(0) scale(1)', boxShadow: active ? '0 14px 28px rgba(16,37,53,.16)' : undefined, transition: 'transform 180ms ease-out, box-shadow 180ms ease-out' }}><div className="metric-top"><span className="eyebrow">{label}</span><span className="metric-icon"><Icon size={17} /></span></div><strong className="metric-value">{value}</strong><span className="metric-note">{note}</span></Card>; }
function InspectionTable({ rows = inspections }: { rows?: Inspection[] }) { return <div className="table-wrap"><table><thead><tr><th>Inspection ID</th><th>Product / manufacturer</th><th>Recorded</th><th>Status</th><th></th></tr></thead><tbody>{rows.map((item) => <tr key={item.id}><td><Link href={`/inspection/${item.id}`} className="table-id"><Mono>{item.id}</Mono></Link></td><td><div className="product-cell"><strong>{item.product}</strong><small>{item.manufacturer}</small></div></td><td><span className="muted">{item.date}</span></td><td><StatusBadge status={item.status} /></td><td><Link href={`/inspection/${item.id}`} className="row-arrow"><ArrowRight size={17} /></Link></td></tr>)}</tbody></table></div>; }
function Dashboard() { 
  const [summary, setSummary] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    dashboardApi.getSummary().then(data => {
      setSummary(data);
      setLoading(false);
    }).catch(err => {
      toast.error('Failed to load dashboard data: ' + err.message);
      setLoading(false);
    });
  }, []);

  return <Shell title="Dashboard" onLogout={() => { localStorage.removeItem('sih_token'); window.location.href = '/login'; }}><PageHeading kicker="Operations overview" title={`Hello, ${getSessionUsername()}.`} description="A concise view of package compliance activity across your enforcement circle." action={<Link href="/inspection/new" className="button primary"><Plus size={17} /> New inspection</Link>} /><div className="signal-strip"><div><span className="signal-mark" /> <strong>Field operations active</strong><span>Last synchronization just now</span></div><Mono>RULESET / PC-2011</Mono></div>
    
  {loading ? <div style={{padding: '40px', textAlign: 'center'}}><span className="spinner" /> Loading dashboard...</div> : 
  <><div className="metric-grid">
    <DashboardCard label="Total inspections" value={summary?.totalInspections?.toString() || '0'} note="Recorded in system" icon={ClipboardCheck} tone="ink" />
    <DashboardCard label="Compliant" value={summary?.compliantCount?.toString() || '0'} note="Verified passing" icon={ShieldCheck} tone="green" />
    <DashboardCard label="Non-compliant" value={summary?.nonCompliantCount?.toString() || '0'} note="Action required" icon={TriangleAlert} tone="red" />
    <DashboardCard label="Needs review" value="-" note="Awaiting decision" icon={Clock3} tone="amber" />
  </div>
  <div className="dashboard-grid"><Card className="recent-card"><div className="card-heading"><div><div className="eyebrow">Latest records</div><h2>Recent inspections</h2></div><Link href="/inspections" className="text-link">View all <ArrowRight size={14} /></Link></div><InspectionTable rows={summary?.recentInspections || []} /></Card><Card className="violation-summary"><div className="card-heading"><div><div className="eyebrow">Attention required</div><h2>Violation summary</h2></div><TriangleAlert size={20} className="summary-icon" /></div><div className="violation-stat"><div className="donut"><span>{summary?.nonCompliantCount || 0}</span><small>flags</small></div><div className="summary-copy"><strong>Compliance Check</strong><span>System-detected findings</span><div className="bar"><i style={{ width: '68%' }} /></div><small>Check reports for details</small></div></div><div className="mini-list"><div><span className="mini-dot red" />Missing declarations</div><div><span className="mini-dot amber" />Quantity mismatch</div><div><span className="mini-dot blue" />Address / origin</div></div><Link href="/reports" className="button secondary full">Open compliance report <ArrowRight size={15} /></Link></Card></div></>}
  </Shell>; 
}

function Login({ onLogin }: { onLogin: () => void }) { 
  const [mode, setMode] = useState<'signin' | 'signup'>('signin'); 
  const [username, setUsername] = useState(''); 
  const [password, setPassword] = useState(''); 
  const [confirmPassword, setConfirmPassword] = useState(''); 
  const [error, setError] = useState(''); 
  const [loading, setLoading] = useState(false); 
  
  const submit = async (e: React.FormEvent) => { 
    e.preventDefault(); 
    setLoading(true); 
    setError(''); 
    
    const normalizedUsername = username.trim(); 
    if (!normalizedUsername) { setError('Enter a username to continue.'); setLoading(false); return; } 
    if (!password) { setError('Enter a password to continue.'); setLoading(false); return; } 
    
    if (mode === 'signup') { 
      if (!confirmPassword) { setError('Confirm your password to continue.'); setLoading(false); return; } 
      if (password !== confirmPassword) { setError('Password and Confirm Password must match.'); setLoading(false); return; } 
      
      try {
        await authApi.register({ username: normalizedUsername, password, role: 'INSPECTOR' });
        setMode('signin'); 
        setPassword(''); 
        setConfirmPassword(''); 
        setError('');
        toast.success('Registration complete. Sign in with your new account.'); 
      } catch (err: any) {
        setError(err.message || 'Registration failed.');
      }
      setLoading(false); 
      return; 
    } 
    
    try {
      const res = await authApi.login({ username: normalizedUsername, password });
      if (res && res.token) {
        localStorage.setItem('sih_token', res.token); 
        localStorage.setItem(USERNAME_KEY, normalizedUsername); 
        onLogin();
      } else {
        setError('Invalid response from server.');
      }
    } catch (err: any) {
      setError(err.message || 'The username or password is incorrect.');
    }
    setLoading(false);
  }; 
  
  const isSignup = mode === 'signup'; 
  return <div className="login-page"><div className="login-visual" style={{ backgroundImage: `linear-gradient(90deg, rgba(13,29,44,.96) 0%, rgba(13,29,44,.78) 48%, rgba(13,29,44,.25) 100%), url(${heroImage})` }}><div className="login-brand"><img src={logoImage} /><div><strong>METROLOG</strong><small>Legal Metrology Enforcement Workspace</small></div></div><div className="login-quote"><Mono>SIH 2026 / PROBLEM 26034</Mono><h1>Turn package evidence into a decision.</h1><p>A focused workspace for authorized officials checking declarations under the Packaged Commodities Rules, 2011.</p></div><div className="login-footer"><span>Department of Consumer Affairs</span><span>Secure field operations</span></div></div><div className="login-panel"><div className="login-panel-inner"><div className="eyebrow">Officer access</div><h2>{isSignup ? 'Create your workspace account' : 'Sign in to the workspace'}</h2><p className="login-intro">{isSignup ? 'Register a local officer account to access inspection records and compliance reports.' : 'Use your authorized credentials to access inspection records and compliance reports.'}</p><form onSubmit={submit}><label>Username<input value={username} onChange={e => setUsername(e.target.value)} placeholder="e.g. Kavita Nair" autoComplete="username" /></label><label>Password<input type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="Enter password" autoComplete={isSignup ? 'new-password' : 'current-password'} /></label>{isSignup && <label>Confirm Password<input type="password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} placeholder="Re-enter password" autoComplete="new-password" /></label>}{error && <div className="form-error"><TriangleAlert size={15} /> {error}</div>}<div style={{ display: 'grid', gap: 10 }}><button className="button primary" style={{ width: '100%' }} disabled={loading}>{loading ? <><span className="spinner" /> {isSignup ? 'Creating account' : 'Verifying credentials'}</> : <>{isSignup ? 'Create account' : 'Continue securely'} <ArrowRight size={16} /></>}</button><button type="button" className="button secondary" style={{ width: '100%' }} onClick={() => { setMode(isSignup ? 'signin' : 'signup'); setError(''); setConfirmPassword(''); }}>{isSignup ? 'Sign in' : 'Sign up'}</button></div></form><div className="login-note"><ShieldCheck size={16} /><span>Session protected with JWT authentication.<br />Authorized use only.</span></div></div><div className="login-bottom"><Mono>v1.0.0 / SIH-26034</Mono><span>Need access support? Contact your administrator.</span></div></div></div>; 
}function NewInspection() { 
  const [files, setFiles] = useState<{ name: string; url: string; file: File }[]>([]); 
  const [productName, setProductName] = useState('');
  const [manufacturer, setManufacturer] = useState('');
  const [processing, setProcessing] = useState(false); 
  const [step, setStep] = useState(0); 
  
  const addFiles = (list: FileList | null) => { 
    if (!list) return; 
    const accepted = Array.from(list).filter(f => f.type.startsWith('image/')).map(f => ({ name: f.name, url: URL.createObjectURL(f), file: f })); 
    if (accepted.length < list.length) toast.error('Only image files can be added to an inspection.'); 
    setFiles(prev => [...prev, ...accepted]); 
  }; 
  
  const start = async () => { 
    if (!files.length) { toast.error('Add at least one package image before starting the scan.'); return; } 
    if (!productName.trim()) { toast.error('Enter a product name.'); return; }
    
    setProcessing(true); 
    setStep(1); 
    
    try {
      // Create product first
      const productData = await productsApi.create({ name: productName, manufacturer });
      
      setStep(2);
      
      // Upload inspection with images
      const inspectionData = await inspectionsApi.create(productData.id, files.map(f => f.file));
      
      setStep(3);
      toast.success('Inspection complete');
      setTimeout(() => { window.location.href = `/inspection/${inspectionData.id}/result`; }, 500); 
    } catch (err: any) {
      toast.error('Failed to process inspection: ' + err.message);
      setProcessing(false);
      setStep(0);
    }
  }; 
  
  return <Shell title="New inspection" onLogout={() => { localStorage.removeItem('sih_token'); window.location.href = '/login'; }}><PageHeading kicker="Primary workflow" title="Start a new inspection" description="Upload clear package images. The system will extract declarations and check them against the current ruleset." /><div className="inspection-layout"><div>
  
  <Card className="upload-card"><div className="section-index"><span>01</span><div><div className="eyebrow">Product details</div><h2>Enter package information</h2></div></div>
    <div style={{ display: 'grid', gap: '1rem', marginBottom: '1.5rem' }}>
      <label>Product Name<input type="text" value={productName} onChange={e => setProductName(e.target.value)} placeholder="e.g. Premium Basmati Rice - 5 kg" /></label>
      <label>Manufacturer<input type="text" value={manufacturer} onChange={e => setManufacturer(e.target.value)} placeholder="e.g. Kaveri Foods Pvt. Ltd." /></label>
    </div>
  </Card>
  
  <Card className="upload-card"><div className="section-index"><span>02</span><div><div className="eyebrow">Evidence intake</div><h2>Upload package images</h2></div></div><label className={`dropzone ${files.length ? 'has-files' : ''}`}><input type="file" accept="image/*" multiple onChange={e => addFiles(e.target.files)} /><div className="drop-icon"><Upload size={22} /></div><strong>{files.length ? 'Add another image' : 'Drop package images here'}</strong><span>or browse from your device · JPG, PNG up to 10 MB each</span></label>{files.length > 0 && <div className="preview-grid">{files.map((file, i) => <div className="preview-tile" key={file.url}><img src={file.url} /><button onClick={() => setFiles(files.filter((_, n) => n !== i))}><X size={14} /></button><span>{file.name}</span></div>)}</div>}<div className="upload-foot"><span><ImageIcon size={15} /> {files.length} image{files.length !== 1 ? 's' : ''} ready for analysis</span><span>Multiple angles improve extraction accuracy</span></div></Card><Card className="process-card"><div className="section-index"><span>03</span><div><div className="eyebrow">Analysis pipeline</div><h2>Inspection processing</h2></div></div><div className="process-steps">{['Images uploaded', 'Uploading evidence', 'OCR processing & Compliance', 'Generating result'].map((label, i) => <div className={`process-step ${processing && i < step ? 'done' : ''} ${processing && i === step ? 'current' : ''}`} key={label}><span className="step-dot">{processing && i < step ? <Check size={13} /> : processing && i === step ? <span className="pulse-dot" /> : <span />}</span><div><strong>{label}</strong><small>{i === 0 ? 'Evidence is ready' : i === 1 ? 'Sending to server' : i === 2 ? 'Applying PC Rules, 2011' : 'Preparing official record'}</small></div></div>)}</div>{processing && <div className="progress-line"><i style={{ width: `${Math.min(step * 25 + 12, 88)}%` }} /></div>}</Card><button className="button primary large" onClick={start} disabled={processing}>{processing ? <><span className="spinner" /> Processing inspection</> : <>Start compliance scan <ArrowRight size={17} /></>}</button></div><aside className="workflow-aside"><div className="aside-number">A</div><div className="eyebrow">Before you submit</div><h3>Capture the package, not the paperwork.</h3><p>Include the front panel and any side or back panel carrying mandatory declarations. Avoid glare, shadows, and folded packaging.</p><div className="aside-check"><div><Check size={14} /> Product name and quantity visible</div><div><Check size={14} /> MRP panel in focus</div><div><Check size={14} /> Manufacturer address included</div></div><div className="aside-rule"><Mono>PC RULES / 2011</Mono><span>Images remain attached to this inspection record as evidence.</span></div></aside></div></Shell>; 
}
function EvidenceViewer({ images }: { images: string[] }) { const [active, setActive] = useState(0); const [zoom, setZoom] = useState(false); return <><div className="evidence-viewer"><div className="evidence-main"><img src={images[active] || evidenceImage} /><button className="zoom-button" onClick={() => setZoom(true)}><ZoomIn size={16} /> View full size</button><div className="evidence-counter"><Mono>{String(active + 1).padStart(2, '0')} / {String(images.length || 1).padStart(2, '0')}</Mono></div></div><div className="evidence-thumbs">{images.map((src, i) => <button key={src + i} className={active === i ? 'selected' : ''} onClick={() => setActive(i)}><img src={src} /></button>)}</div></div>{zoom && <div className="modal" onClick={() => setZoom(false)}><button className="modal-close"><X size={20} /></button><img src={images[active]} onClick={e => e.stopPropagation()} /></div>}</>; }

function DeclarationTable({ items }: { items: Declaration[] }) {
  if (!items || items.length === 0) return <div className="declarations"><span className="muted" style={{padding:'14px 0',display:'block',fontSize:12}}>No declarations extracted.</span></div>;
  return (
    <div className="declarations">
      {items.map((d, i) => (
        <div key={d.label + i} className="declaration-row">
          <div className="decl-label"><Mono>{d.label}</Mono></div>
          <div className={`decl-value ${d.status === 'missing' ? 'missing' : ''}`}>{d.value || '—'}</div>
          <div className={`decl-status ${d.status}`}>
            <span />
            <span>{d.status === 'detected' ? 'Detected' : 'Missing'}</span>
          </div>
        </div>
      ))}
    </div>
  );
}

function ViolationCard({ violation }: { violation: Violation }) {
  return (
    <div className={`violation-card ${violation.severity || 'high'}`}>
      <div className="violation-icon"><TriangleAlert size={18} /></div>
      <div className="violation-body">
        <div className="violation-title">
          {violation.type || 'Violation'}
          {violation.severity && <span>{violation.severity.toUpperCase()}</span>}
        </div>
        <p>{violation.description}</p>
        {(violation.ruleReference || violation.rule) && (
          <div className="rule-ref"><Mono>{violation.ruleReference || violation.rule}</Mono></div>
        )}
      </div>
    </div>
  );
}

function ScanResult({ details = false }: { details?: boolean }) { 
  const [, params] = useRoute('/inspection/:id/:result?');
  const id = params?.id || '';
  const [item, setItem] = useState<Inspection | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    inspectionsApi.getById(id).then(data => {
      // transform declarations map into array
      const declArr = Object.entries(data.extractedDeclarations || {}).map(([key, value]) => ({
        label: key,
        value: String(value),
        status: (value && String(value).toUpperCase() !== 'NOT DETECTED') ? 'detected' as const : 'missing' as const
      }));
      
      setItem({
        ...data,
        images: data.images?.length ? data.images : [evidenceImage],
        declarations: declArr,
        product: data.productId, // should resolve from product API ideally
        date: data.createdAt,
        status: data.complianceStatus,
        confidence: data.ocrData?.confidence || 0,
        officer: data.userId || 'System',
        location: 'Karnal Circle, HR' // Default mock location
      });
      setLoading(false);
    }).catch(err => {
      toast.error('Failed to load inspection details: ' + err.message);
      setLoading(false);
    });
  }, [id]);

  if (loading) return <Shell title="Loading..." onLogout={() => { localStorage.removeItem('sih_token'); window.location.href = '/login'; }}><div style={{padding: '40px', textAlign: 'center'}}><span className="spinner" /> Loading inspection record...</div></Shell>;
  if (!item) return <Shell title="Error" onLogout={() => { localStorage.removeItem('sih_token'); window.location.href = '/login'; }}>Record not found.</Shell>;

  return <Shell title={details ? 'Inspection details' : 'Scan result'} onLogout={() => { localStorage.removeItem('sih_token'); window.location.href = '/login'; }}><div className="back-row"><Link href={details ? '/inspections' : '/inspection/new'}><ArrowLeft size={15} /> {details ? 'Back to inspection history' : 'Start another inspection'}</Link><Mono>{item.id}</Mono></div><PageHeading kicker={details ? 'Complete inspection record' : 'Analysis complete'} title={item.product || 'Inspection result'} description={`${item.id} · ${item.date} · ${item.location}`} action={<StatusBadge status={item.complianceStatus} />} /><div className={`result-banner ${item.complianceStatus === 'COMPLIANT' ? 'compliant' : ''}`}><div className="result-seal"><Check size={31} /></div><div><Mono>COMPLIANCE DECISION</Mono><h2>{item.complianceStatus === 'COMPLIANT' ? 'Package declarations appear compliant.' : 'Package requires enforcement action.'}</h2><p>Confidence score <strong>{Math.round(item.confidence || 0)}%</strong> · Evaluated against Packaged Commodities Rules, 2011</p></div><div className="result-mark">{item.complianceStatus === 'COMPLIANT' ? 'CLEAR' : 'REVIEW'}</div></div><div className="result-grid"><div><Card><div className="card-heading"><div><div className="eyebrow">Machine extraction</div><h2>Extracted declarations</h2></div><FileCheck2 size={20} className="muted-icon" /></div><DeclarationTable items={(item as any).declarations || []} /></Card>{item.violations.length > 0 && <Card className="violations-panel"><div className="card-heading"><div><div className="eyebrow">Findings</div><h2>Violations detected</h2></div><span className="violation-count">{item.violations.length}</span></div>{item.violations.map(v => <ViolationCard key={v.type || v.description} violation={v} />)}</Card>}{item.violations.length === 0 && <Card className="clear-panel"><Check size={19} /><div><strong>No violations recorded</strong><span>All required declarations were detected in the submitted evidence.</span></div></Card>}</div><div><Card className="evidence-card"><div className="card-heading"><div><div className="eyebrow">Supporting evidence</div><h2>Package images</h2></div><span className="image-count"><ImageIcon size={14} /> {item.images.length}</span></div><EvidenceViewer images={item.images} /></Card><Card className="record-card"><div className="eyebrow">Record metadata</div><div className="record-row"><span>Inspection officer</span><strong>{item.userId || 'System'}</strong></div><div className="record-row"><span>Jurisdiction</span><strong>{item.location || 'HQ'}</strong></div><div className="record-row"><span>Ruleset applied</span><strong>PC Rules, 2011</strong></div><a href={reportsApi.getPdfUrl(item.id)} target="_blank" className="button secondary full" style={{textAlign: 'center', textDecoration: 'none'}}><FileText size={15} /> View generated report</a></Card></div></div></Shell>; 
}

function Inspections() { 
  const [query, setQuery] = useState(''); 
  const [filter, setFilter] = useState<Status | 'ALL'>('ALL'); 
  const [inspectionsList, setInspectionsList] = useState<Inspection[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    inspectionsApi.getAll().then(data => {
      setInspectionsList(data.map((item: any) => ({
        ...item,
        product: item.productId,
        date: item.createdAt,
        status: item.complianceStatus
      })));
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  const rows = useMemo(() => inspectionsList.filter(i => (filter === 'ALL' || i.complianceStatus === filter) && `${i.id} ${i.product} ${i.manufacturer || ''}`.toLowerCase().includes(query.toLowerCase())), [query, filter, inspectionsList]); 
  
  return <Shell title="Inspection history" onLogout={() => { localStorage.removeItem('sih_token'); window.location.href = '/login'; }}><PageHeading kicker="Records archive" title="Inspection history" description="Search and review inspection records created by your enforcement circle." action={<Link href="/inspection/new" className="button primary"><Plus size={17} /> New inspection</Link>} /><Card className="history-card"><div className="toolbar"><div className="search-field"><Search size={17} /><input placeholder="Search ID, product, or manufacturer" value={query} onChange={e => setQuery(e.target.value)} /></div><div className="filter-group"><Filter size={15} />{(['ALL', 'COMPLIANT', 'NON_COMPLIANT', 'NEEDS_REVIEW'] as const).map(f => <button key={f} className={filter === f ? 'selected' : ''} onClick={() => setFilter(f)}>{f === 'ALL' ? 'All records' : (statusMeta[f as Status]?.label || f)}</button>)}</div></div>
  {loading ? <div style={{padding: '20px', textAlign: 'center'}}><span className="spinner" /> Loading records...</div> : rows.length ? <InspectionTable rows={rows} /> : <div className="empty-state"><Search size={22} /><h3>No matching records</h3><p>Try a different search term or clear the status filter.</p></div>}</Card></Shell>; 
}

function Products() { 
  const [query, setQuery] = useState(''); 
  const [productsList, setProductsList] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    productsApi.getAll().then(data => {
      setProductsList(data);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  const rows = productsList.filter(i => `${i.name} ${i.manufacturer} ${i.id}`.toLowerCase().includes(query.toLowerCase())); 
  
  return <Shell title="Products" onLogout={() => { localStorage.removeItem('sih_token'); window.location.href = '/login'; }}><PageHeading kicker="Product register" title="Search products" description="Find previously scanned products by product name, manufacturer, or inspection ID." /><Card className="product-search-card"><div className="search-hero"><div className="search-hero-icon"><PackageCheck size={23} /></div><div><h2>Find a product record</h2><p>Search the linked inspection evidence and compliance history.</p></div></div><div className="search-field large"><Search size={18} /><input placeholder="Product name, manufacturer, or inspection ID" value={query} onChange={e => setQuery(e.target.value)} /><button className="button primary" onClick={() => toast.success(`${rows.length} product records found`)}>Search</button></div></Card><div className="product-results"><div className="eyebrow">{rows.length} linked records</div>
  {loading ? <div style={{padding: '20px', textAlign: 'center'}}><span className="spinner" /> Loading products...</div> : 
    rows.map(item => <div className="product-result" key={item.id}><div className="product-result-icon"><PackageCheck size={18} /></div><div style={{flex: 1}}><strong>{item.name}</strong><span>{item.manufacturer} · <Mono>{item.id}</Mono></span></div></div>)
  }
  </div></Shell>; 
}

function Reports() { 
  const [inspectionsList, setInspectionsList] = useState<Inspection[]>([]);
  
  useEffect(() => {
    inspectionsApi.getAll().then(data => {
      setInspectionsList(data.map((item: any) => ({
        ...item,
        product: item.productId,
        status: item.complianceStatus
      })));
    }).catch(console.error);
  }, []);

  const latest = inspectionsList.length ? inspectionsList[0] : null;

  return <Shell title="Reports" onLogout={() => { localStorage.removeItem('sih_token'); window.location.href = '/login'; }}><PageHeading kicker="Official records" title="Compliance reports" description="Access backend-generated reports for completed inspections and circle-level summaries." /><div className="reports-grid">
  {latest && <Card className="report-feature"><div className="report-paper"><div className="report-paper-head"><img src={logoImage} /><Mono>OFFICIAL RECORD</Mono></div><div className="eyebrow">Inspection report</div><h2>{latest.id}</h2><p>{latest.product || latest.productId}</p><div className="report-rule" /><div className="report-lines"><span /><span /><span /></div><div className="report-stamp">{latest.complianceStatus}</div></div><div className="report-feature-copy"><div className="eyebrow">Latest generated report</div><h2>Inspection compliance record</h2><p>Generated from the completed evidence review. Includes declarations, confidence score, applied rules, and supporting images.</p><div className="report-actions"><a href={reportsApi.getPdfUrl(latest.id)} target="_blank" className="button primary" style={{textDecoration: 'none'}}><FileText size={16} /> View report</a></div></div></Card>}
  
  <Card className="report-list"><div className="card-heading"><div><div className="eyebrow">Available files</div><h2>Report archive</h2></div><BarChart3 size={20} className="muted-icon" /></div>{inspectionsList.slice(0, 10).map((i, idx) => <div className="report-row" key={i.id}><div className="report-file-icon"><FileText size={16} /></div><div><strong>{i.id} · Compliance report</strong><span>{i.product || i.productId} · {idx === 0 ? 'Generated today' : 'Generated recently'}</span></div><StatusBadge status={i.complianceStatus} /><a href={reportsApi.getPdfUrl(i.id)} target="_blank" className="icon-button"><Download size={16} /></a></div>)}</Card></div></Shell>; 
}
function App() { const [logged, setLogged] = useState(() => !!localStorage.getItem('sih_token')); const logout = () => { localStorage.removeItem('sih_token'); localStorage.removeItem(USERNAME_KEY); setLogged(false); }; if (!logged) return <Switch><Route path="/login"><Login onLogin={() => setLogged(true)} /></Route><Route><Login onLogin={() => setLogged(true)} /></Route></Switch>; return <Switch><Route path="/dashboard"><Dashboard /></Route><Route path="/inspection/new"><NewInspection /></Route><Route path="/inspection/:id/result"><ScanResult /></Route><Route path="/inspection/:id"><ScanResult details /></Route><Route path="/inspections"><Inspections /></Route><Route path="/products"><Products /></Route><Route path="/reports"><Reports /></Route><Route><Dashboard /></Route></Switch>; }
export default function AppWithToaster() { return <><App /><Toaster position="bottom-right" toastOptions={{ style: { fontFamily: 'Times New Roman, Times, serif' } }} /></>; }
