import React, { useMemo, useState } from 'react';
import { ArrowLeft, ArrowRight, Pin, Plus, Trash2 } from 'lucide-react';
import { useSukoonStore } from '../../stores/useSukoonStore';
import { getDict } from '../../design-system/i18n';
import {
  Chip,
  ConfirmDialog,
  EmptyState,
  SearchInput,
  SectionHeader,
} from '../../design-system/components';

export const NotesScreen: React.FC = () => {
  const language = useSukoonStore((s) => s.language);
  const notes = useSukoonStore((s) => s.notes);
  const createNote = useSukoonStore((s) => s.createNote);
  const updateNote = useSukoonStore((s) => s.updateNote);
  const toggleNotePin = useSukoonStore((s) => s.toggleNotePin);
  const deleteNote = useSukoonStore((s) => s.deleteNote);

  const d = getDict(language);
  const [activeNoteId, setActiveNoteId] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null);

  const activeNote = useMemo(
    () => notes.find((n) => n.id === activeNoteId) ?? null,
    [notes, activeNoteId]
  );

  const sortedNotes = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    const filtered = q
      ? notes.filter(
          (n) =>
            n.title.toLowerCase().includes(q) || n.body.toLowerCase().includes(q)
        )
      : notes;
    return [...filtered].sort((a, b) => {
      if (a.pinned !== b.pinned) return a.pinned ? -1 : 1;
      return b.updatedAt - a.updatedAt;
    });
  }, [notes, searchQuery]);

  const formatTimestamp = (ms: number) => {
    try {
      return new Intl.DateTimeFormat(d.isArabic ? 'ar-EG' : 'en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      }).format(new Date(ms));
    } catch {
      return '';
    }
  };

  const BackIcon = d.isArabic ? ArrowRight : ArrowLeft;

  if (activeNote) {
    return (
      <div className="h-full flex flex-col px-page-x pt-5 pb-3 gap-3 animate-fade-in">
        {/* Editor Top Bar */}
        <div className="flex items-center justify-between gap-2">
          <Chip onClick={() => setActiveNoteId(null)}>
            <BackIcon className="w-3.5 h-3.5" strokeWidth={1.7} />
            <span>{d.back}</span>
          </Chip>

          <div className="flex items-center gap-1.5">
            <Chip
              active={activeNote.pinned}
              onClick={() => toggleNotePin(activeNote.id)}
            >
              <Pin className="w-3.5 h-3.5" strokeWidth={1.7} />
              <span>{activeNote.pinned ? d.pinned : d.pin}</span>
            </Chip>
            <Chip
              danger
              onClick={() => setConfirmDeleteId(activeNote.id)}
              aria-label={d.delete}
            >
              <Trash2 className="w-3.5 h-3.5" strokeWidth={1.7} />
            </Chip>
          </div>
        </div>

        {/* Borderless Title Input */}
        <input
          type="text"
          value={activeNote.title}
          onChange={(e) =>
            updateNote(activeNote.id, e.target.value, activeNote.body)
          }
          placeholder={d.noteTitlePlaceholder}
          className="w-full bg-transparent text-[22px] font-medium text-ink placeholder:text-subtle py-1 focus:outline-none"
        />

        <div className="h-[1px] bg-border" />

        {/* Full-Height Body Editor */}
        <textarea
          value={activeNote.body}
          onChange={(e) =>
            updateNote(activeNote.id, activeNote.title, e.target.value)
          }
          placeholder={d.noteBodyPlaceholder}
          className="flex-1 w-full bg-transparent resize-none text-[15px] leading-[1.85] text-ink placeholder:text-subtle focus:outline-none"
        />

        <div className="flex items-center justify-between text-[11px] text-subtle pt-1">
          <span>{d.autoSaved}</span>
          <span>{formatTimestamp(activeNote.updatedAt)}</span>
        </div>

        <ConfirmDialog
          open={confirmDeleteId !== null}
          title={d.confirmDeleteNoteTitle}
          description={d.confirmDeleteNoteDesc}
          confirmLabel={d.delete}
          cancelLabel={d.cancel}
          onConfirm={() => {
            if (confirmDeleteId) {
              deleteNote(confirmDeleteId);
              setActiveNoteId(null);
            }
            setConfirmDeleteId(null);
          }}
          onCancel={() => setConfirmDeleteId(null)}
        />
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col px-page-x pt-5 pb-3 gap-3.5">
      <SectionHeader
        eyebrow={d.notesEyebrow}
        title={d.notesTitle}
        action={
          <Chip
            active
            onClick={() => {
              const id = createNote();
              setActiveNoteId(id);
            }}
          >
            <Plus className="w-3.5 h-3.5" strokeWidth={1.8} />
            <span>{d.newNote}</span>
          </Chip>
        }
      />

      {notes.length > 2 && (
        <SearchInput
          value={searchQuery}
          onChange={setSearchQuery}
          placeholder={d.searchNotesPlaceholder}
        />
      )}

      <div className="flex-1 min-h-0 overflow-y-auto space-y-2.5 pe-0.5">
        {sortedNotes.length === 0 ? (
          <EmptyState message={d.noNotesEmpty} />
        ) : (
          sortedNotes.map((note) => (
            <button
              key={note.id}
              type="button"
              onClick={() => setActiveNoteId(note.id)}
              className={`w-full text-start p-4 rounded-lg border transition-colors space-y-1.5 ${
                note.pinned
                  ? 'bg-surface border-accent/45 hover:border-accent'
                  : 'bg-surface border-border hover:border-border-strong'
              }`}
            >
              <div className="flex items-center justify-between gap-2">
                <span className="text-[15px] font-medium text-ink truncate">
                  {note.title.trim() || d.untitledNote}
                </span>
                <span className="text-[11px] text-accent shrink-0">
                  {note.pinned ? `${d.pinned} • ` : ''}
                  {formatTimestamp(note.updatedAt)}
                </span>
              </div>
              <p className="text-[13px] leading-relaxed text-muted line-clamp-2">
                {note.body.trim() || d.tapToWrite}
              </p>
            </button>
          ))
        )}
      </div>
    </div>
  );
};
