import type { QuranPage } from '../types';

const DB_NAME = 'sukoon_quran_cache';
const STORE_NAME = 'pages';
const KEY = 'mushaf_604_v1';

let memoryCache: QuranPage[] | null = null;
let inflight: Promise<QuranPage[]> | null = null;

function openDb(): Promise<IDBDatabase | null> {
  return new Promise((resolve) => {
    if (typeof indexedDB === 'undefined') {
      resolve(null);
      return;
    }
    try {
      const req = indexedDB.open(DB_NAME, 1);
      req.onupgradeneeded = () => {
        const db = req.result;
        if (!db.objectStoreNames.contains(STORE_NAME)) {
          db.createObjectStore(STORE_NAME);
        }
      };
      req.onsuccess = () => resolve(req.result);
      req.onerror = () => resolve(null);
    } catch {
      resolve(null);
    }
  });
}

async function readFromIdb(): Promise<QuranPage[] | null> {
  const db = await openDb();
  if (!db) return null;
  return new Promise((resolve) => {
    try {
      const tx = db.transaction(STORE_NAME, 'readonly');
      const req = tx.objectStore(STORE_NAME).get(KEY);
      req.onsuccess = () => resolve((req.result as QuranPage[]) ?? null);
      req.onerror = () => resolve(null);
    } catch {
      resolve(null);
    }
  });
}

async function writeToIdb(pages: QuranPage[]): Promise<void> {
  const db = await openDb();
  if (!db) return;
  return new Promise((resolve) => {
    try {
      const tx = db.transaction(STORE_NAME, 'readwrite');
      tx.objectStore(STORE_NAME).put(pages, KEY);
      tx.oncomplete = () => resolve();
      tx.onerror = () => resolve();
    } catch {
      resolve();
    }
  });
}

export async function loadQuranPages(): Promise<QuranPage[]> {
  if (memoryCache) return memoryCache;
  if (inflight) return inflight;

  inflight = (async () => {
    const cached = await readFromIdb();
    if (cached && cached.length === 604) {
      memoryCache = cached;
      return cached;
    }
    const res = await fetch('/quran_pages.json');
    if (!res.ok) throw new Error('Failed to load Mushaf data');
    const data = (await res.json()) as QuranPage[];
    memoryCache = data;
    void writeToIdb(data);
    return data;
  })();

  try {
    return await inflight;
  } finally {
    inflight = null;
  }
}
