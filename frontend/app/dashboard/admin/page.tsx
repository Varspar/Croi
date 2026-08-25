'use client';

import { ChangeEvent, useEffect, useMemo, useState } from 'react';
import { FileText, Loader2, RefreshCw, Trash2, Upload } from 'lucide-react';
import { getCurrentUserOrganization } from '@/services/organizationService';
import { deleteWorkspaceDocument, getWorkspaceDocuments, regenerateDocument, uploadWorkspaceDocument } from '@/services/workspaceAdminService';
import type { Document } from '@/types';

type Tab = 'documents' | 'members';
const tabs: Tab[] = ['documents', 'members'];

export default function AdminPage() {
  const [workspaceId, setWorkspaceId] = useState<string>();
  const [documents, setDocuments] = useState<Document[]>([]);
  const [tab, setTab] = useState<Tab>('documents');
  const [loading, setLoading] = useState(true); const [saving, setSaving] = useState(false); const [error, setError] = useState('');
  const load = async () => { try { const id = await getCurrentUserOrganization(); setWorkspaceId(id); setDocuments(await getWorkspaceDocuments(id)); } catch { setError('Could not load this workspace. Owner access is required.'); } finally { setLoading(false); } };
  useEffect(() => { load(); }, []);
  const totals = useMemo(() => ({ documents: documents.length, chunks: documents.reduce((n, d) => n + d.chunkCount, 0), embeddings: documents.reduce((n, d) => n + d.embeddingCount, 0) }), [documents]);
  const upload = async (event: ChangeEvent<HTMLInputElement>) => { if (!workspaceId || !event.target.files?.length) return; try { setSaving(true); const added = await Promise.all(Array.from(event.target.files).map((file) => uploadWorkspaceDocument(workspaceId, file))); setDocuments((current) => [...added, ...current]); } catch { setError('Upload failed. Only readable PDFs are supported.'); } finally { setSaving(false); event.target.value = ''; } };
  if (loading) return <div className="flex h-full items-center justify-center"><Loader2 className="animate-spin text-croi-teal-bright" /></div>;
  return <div className="h-full overflow-y-auto p-6 text-croi-text-light"><h1 className="text-2xl font-bold">Workspace admin</h1><p className="mt-1 text-sm text-gray-400">Manage your knowledge base.</p>{error && <p className="mt-4 rounded bg-red-500/10 p-3 text-sm text-red-300">{error}</p>}<div className="mt-6 flex gap-2 border-b border-white/10">{tabs.map((item) => <button key={item} onClick={() => setTab(item)} className={`px-4 py-3 text-sm capitalize ${tab === item ? 'border-b-2 border-croi-teal-bright text-croi-teal-bright' : 'text-gray-400'}`}>{item}</button>)}</div>
    {tab === 'documents' && <section className="mt-6 space-y-5"><div className="grid grid-cols-3 gap-3">{Object.entries(totals).map(([key, value]) => <div key={key} className="rounded-lg bg-croi-bg-card p-4"><p className="text-2xl font-bold">{value}</p><p className="capitalize text-sm text-gray-400">{key}</p></div>)}</div><label className="flex cursor-pointer items-center justify-center gap-2 rounded-lg border border-dashed border-croi-teal-bright/60 p-7 text-croi-teal-bright"><Upload size={18} />{saving ? 'Uploading…' : 'Upload PDFs'}<input className="hidden" type="file" accept="application/pdf" multiple onChange={upload} /></label><div className="overflow-hidden rounded-lg border border-white/10">{documents.map((doc) => <div key={doc.id} className="flex items-center gap-3 border-b border-white/5 p-4 last:border-0"><FileText className="text-croi-teal-bright" /><div className="min-w-0 flex-1"><p className="truncate font-medium">{doc.fileName}</p><p className="text-xs text-gray-400">{doc.chunkCount} chunks · {doc.embeddingCount} embeddings {doc.errorMessage && `· ${doc.errorMessage}`}</p></div><span className={doc.status === 'READY' ? 'text-xs text-emerald-400' : doc.status === 'FAILED' ? 'text-xs text-red-400' : 'text-xs text-amber-400'}>{doc.status}</span><button onClick={async () => { if (workspaceId) { const updated = await regenerateDocument(workspaceId, doc.id); setDocuments((items) => items.map((item) => item.id === doc.id ? updated : item)); } }}><RefreshCw size={16} /></button><button onClick={async () => { if (workspaceId && confirm(`Delete ${doc.fileName}?`)) { await deleteWorkspaceDocument(workspaceId, doc.id); setDocuments((items) => items.filter((item) => item.id !== doc.id)); } }}><Trash2 size={16} className="text-red-400" /></button></div>)}{!documents.length && <p className="p-6 text-sm text-gray-400">No documents uploaded yet.</p>}</div></section>}
    {tab === 'members' && <section className="mt-6 rounded-lg bg-croi-bg-card p-5 text-sm text-gray-400">Owner member management uses the workspace members API and will be expanded with invitations next.</section>}
  </div>;
}
