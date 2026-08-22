'use client';

import { ChangeEvent, useEffect, useMemo, useState } from 'react';
import { FileText, Loader2, RefreshCw, Save, Trash2, Upload } from 'lucide-react';
import { getCurrentUserOrganization } from '@/services/organizationService';
import { deleteWorkspaceDocument, getWorkspaceAnalytics, getWorkspaceConfiguration, getWorkspaceDocuments, regenerateDocument, saveWorkspaceConfiguration, uploadWorkspaceDocument } from '@/services/workspaceAdminService';
import type { Document, WorkspaceAnalytics, WorkspaceConfiguration } from '@/types';

type Tab = 'documents' | 'configuration' | 'analytics' | 'members';
const tabs: Tab[] = ['documents', 'configuration', 'analytics', 'members'];

export default function AdminPage() {
  const [workspaceId, setWorkspaceId] = useState<string>();
  const [documents, setDocuments] = useState<Document[]>([]);
  const [config, setConfig] = useState<WorkspaceConfiguration>();
  const [analytics, setAnalytics] = useState<WorkspaceAnalytics>();
  const [analyticsLoading, setAnalyticsLoading] = useState(false);
  const [tab, setTab] = useState<Tab>('documents');
  const [loading, setLoading] = useState(true); const [saving, setSaving] = useState(false); const [error, setError] = useState('');
  const load = async () => { try { const id = await getCurrentUserOrganization(); setWorkspaceId(id); const [docs, settings] = await Promise.all([getWorkspaceDocuments(id), getWorkspaceConfiguration(id)]); setDocuments(docs); setConfig(settings); } catch { setError('Could not load this workspace. Owner access is required.'); } finally { setLoading(false); } };
  useEffect(() => { load(); }, []);
  useEffect(() => {
    if (tab !== 'analytics' || !workspaceId || analytics || analyticsLoading) return;
    setAnalyticsLoading(true);
    getWorkspaceAnalytics(workspaceId)
      .then(setAnalytics)
      .catch(() => setError('Could not load analytics for this workspace.'))
      .finally(() => setAnalyticsLoading(false));
  }, [tab, workspaceId, analytics, analyticsLoading]);
  const totals = useMemo(() => ({ documents: documents.length, chunks: documents.reduce((n, d) => n + d.chunkCount, 0), embeddings: documents.reduce((n, d) => n + d.embeddingCount, 0) }), [documents]);
  const upload = async (event: ChangeEvent<HTMLInputElement>) => { if (!workspaceId || !event.target.files?.length) return; try { setSaving(true); const added = await Promise.all(Array.from(event.target.files).map((file) => uploadWorkspaceDocument(workspaceId, file))); setDocuments((current) => [...added, ...current]); } catch { setError('Upload failed. Only readable PDFs are supported.'); } finally { setSaving(false); event.target.value = ''; } };
  const set = <K extends keyof WorkspaceConfiguration>(key: K, value: WorkspaceConfiguration[K]) => setConfig((current) => current && { ...current, [key]: value });
  if (loading) return <div className="flex h-full items-center justify-center"><Loader2 className="animate-spin text-croi-teal-bright" /></div>;
  return <div className="h-full overflow-y-auto p-6 text-croi-text-light"><h1 className="text-2xl font-bold">Workspace admin</h1><p className="mt-1 text-sm text-gray-400">Manage your knowledge base and AI behaviour.</p>{error && <p className="mt-4 rounded bg-red-500/10 p-3 text-sm text-red-300">{error}</p>}<div className="mt-6 flex gap-2 border-b border-white/10">{tabs.map((item) => <button key={item} onClick={() => setTab(item)} className={`px-4 py-3 text-sm capitalize ${tab === item ? 'border-b-2 border-croi-teal-bright text-croi-teal-bright' : 'text-gray-400'}`}>{item}</button>)}</div>
    {tab === 'documents' && <section className="mt-6 space-y-5"><div className="grid grid-cols-3 gap-3">{Object.entries(totals).map(([key, value]) => <div key={key} className="rounded-lg bg-croi-bg-card p-4"><p className="text-2xl font-bold">{value}</p><p className="capitalize text-sm text-gray-400">{key}</p></div>)}</div><label className="flex cursor-pointer items-center justify-center gap-2 rounded-lg border border-dashed border-croi-teal-bright/60 p-7 text-croi-teal-bright"><Upload size={18} />{saving ? 'Uploading…' : 'Upload PDFs'}<input className="hidden" type="file" accept="application/pdf" multiple onChange={upload} /></label><div className="overflow-hidden rounded-lg border border-white/10">{documents.map((doc) => <div key={doc.id} className="flex items-center gap-3 border-b border-white/5 p-4 last:border-0"><FileText className="text-croi-teal-bright" /><div className="min-w-0 flex-1"><p className="truncate font-medium">{doc.fileName}</p><p className="text-xs text-gray-400">{doc.chunkCount} chunks · {doc.embeddingCount} embeddings {doc.errorMessage && `· ${doc.errorMessage}`}</p></div><span className={doc.status === 'READY' ? 'text-xs text-emerald-400' : doc.status === 'FAILED' ? 'text-xs text-red-400' : 'text-xs text-amber-400'}>{doc.status}</span><button onClick={async () => { if (workspaceId) { const updated = await regenerateDocument(workspaceId, doc.id); setDocuments((items) => items.map((item) => item.id === doc.id ? updated : item)); } }}><RefreshCw size={16} /></button><button onClick={async () => { if (workspaceId && confirm(`Delete ${doc.fileName}?`)) { await deleteWorkspaceDocument(workspaceId, doc.id); setDocuments((items) => items.filter((item) => item.id !== doc.id)); } }}><Trash2 size={16} className="text-red-400" /></button></div>)}{!documents.length && <p className="p-6 text-sm text-gray-400">No documents uploaded yet.</p>}</div></section>}
    {tab === 'configuration' && config && <section className="mt-6 max-w-3xl space-y-5"><label className="block text-sm font-medium">System prompt<textarea className="mt-2 h-36 w-full rounded bg-croi-bg-card p-3" value={config.systemPrompt} onChange={(e) => set('systemPrompt', e.target.value)} /></label><label className="block text-sm font-medium">Tone<select className="mt-2 block rounded bg-croi-bg-card p-3" value={config.tone} onChange={(e) => set('tone', e.target.value as WorkspaceConfiguration['tone'])}>{['FORMAL','FRIENDLY','PROFESSIONAL','CASUAL'].map((tone) => <option key={tone}>{tone}</option>)}</select></label><div className="grid grid-cols-2 gap-4">{(['similarityThreshold', 'maxChunksInPrompt', 'temperature', 'maxTokens'] as const).map((key) => <label key={key} className="text-sm">{key}<input className="mt-2 block w-full rounded bg-croi-bg-card p-3" type="number" step={key === 'maxTokens' || key === 'maxChunksInPrompt' ? 1 : 0.1} value={config[key]} onChange={(e) => set(key, Number(e.target.value))} /></label>)}</div><button disabled={saving} onClick={async () => { if (workspaceId) { setSaving(true); try { setConfig(await saveWorkspaceConfiguration(workspaceId, config)); } catch { setError('Configuration could not be saved.'); } finally { setSaving(false); } } }} className="flex items-center gap-2 rounded bg-croi-gradient px-4 py-2 font-semibold text-croi-bg-dark"><Save size={16} />Save configuration</button></section>}
    {tab === 'analytics' && <section className="mt-6 space-y-5">
      {analyticsLoading && <div className="flex justify-center p-6"><Loader2 className="animate-spin text-croi-teal-bright" /></div>}
      {!analyticsLoading && analytics && <>
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
          <div className="rounded-lg bg-croi-bg-card p-4"><p className="text-2xl font-bold">{analytics.totalConversations}</p><p className="text-sm text-gray-400">Conversations</p></div>
          <div className="rounded-lg bg-croi-bg-card p-4"><p className="text-2xl font-bold">{analytics.totalMessages}</p><p className="text-sm text-gray-400">Messages</p></div>
          <div className="rounded-lg bg-croi-bg-card p-4"><p className="text-2xl font-bold">{analytics.ragHitRate === null ? '—' : `${Math.round(analytics.ragHitRate * 100)}%`}</p><p className="text-sm text-gray-400">RAG hit rate</p></div>
          <div className="rounded-lg bg-croi-bg-card p-4"><p className="text-2xl font-bold">{analytics.avgResponseTimeMs === null ? '—' : `${Math.round(analytics.avgResponseTimeMs)}ms`}</p><p className="text-sm text-gray-400">Avg response time</p></div>
        </div>
        <div className="overflow-hidden rounded-lg border border-white/10">
          <p className="border-b border-white/10 p-4 text-sm font-medium">Most-cited documents</p>
          {analytics.topDocuments.map((doc) => <div key={doc.documentId} className="flex items-center justify-between border-b border-white/5 p-4 text-sm last:border-0"><span className="truncate">{doc.title}</span><span className="text-gray-400">{doc.citations} citation{doc.citations === 1 ? '' : 's'}</span></div>)}
          {!analytics.topDocuments.length && <p className="p-6 text-sm text-gray-400">No documents have been cited in an AI reply yet.</p>}
        </div>
      </>}
    </section>}
    {tab === 'members' && <section className="mt-6 rounded-lg bg-croi-bg-card p-5 text-sm text-gray-400">Owner member management uses the workspace members API and will be expanded with invitations next.</section>}
  </div>;
}
