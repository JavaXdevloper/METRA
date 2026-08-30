import { eq, desc } from "drizzle-orm";
import { drizzle } from "drizzle-orm/mysql2";
import { InsertInspection, InsertStoredFile, InsertUser, inspectionFiles, inspections, storedFiles, users } from "../drizzle/schema";
import { ENV } from './_core/env';

let _db: ReturnType<typeof drizzle> | null = null;

export async function getDb() {
  if (!_db && process.env.DATABASE_URL) {
    try { _db = drizzle(process.env.DATABASE_URL); }
    catch (error) { console.warn("[Database] Failed to connect:", error); _db = null; }
  }
  return _db;
}

export async function upsertUser(user: InsertUser): Promise<void> {
  if (!user.openId) throw new Error("User openId is required for upsert");
  const db = await getDb();
  if (!db) { console.warn("[Database] Cannot upsert user: database not available"); return; }
  const values: InsertUser = { openId: user.openId };
  const updateSet: Record<string, unknown> = {};
  const textFields = ["name", "email", "loginMethod"] as const;
  for (const field of textFields) { if (user[field] !== undefined) { values[field] = user[field] ?? null; updateSet[field] = user[field] ?? null; } }
  if (user.lastSignedIn !== undefined) { values.lastSignedIn = user.lastSignedIn; updateSet.lastSignedIn = user.lastSignedIn; }
  if (user.role !== undefined) { values.role = user.role; updateSet.role = user.role; }
  else if (user.openId === ENV.ownerOpenId) { values.role = 'admin'; updateSet.role = 'admin'; }
  values.lastSignedIn ??= new Date();
  if (!Object.keys(updateSet).length) updateSet.lastSignedIn = new Date();
  await db.insert(users).values(values).onDuplicateKeyUpdate({ set: updateSet });
}

export async function getUserByOpenId(openId: string) {
  const db = await getDb();
  if (!db) return undefined;
  const result = await db.select().from(users).where(eq(users.openId, openId)).limit(1);
  return result[0];
}

export async function createStoredFile(file: InsertStoredFile) {
  const db = await getDb();
  if (!db) throw new Error('Database is not available');
  await db.insert(storedFiles).values(file);
  const result = await db.select().from(storedFiles).where(eq(storedFiles.fileKey, file.fileKey)).limit(1);
  return result[0];
}

export async function listStoredFiles(ownerId: number) {
  const db = await getDb();
  if (!db) throw new Error('Database is not available');
  return db.select().from(storedFiles).where(eq(storedFiles.ownerId, ownerId)).orderBy(desc(storedFiles.createdAt));
}

export async function createInspection(inspection: InsertInspection, storedFileIds: number[]) {
  const db = await getDb();
  if (!db) throw new Error('Database is not available');
  await db.insert(inspections).values(inspection);
  if (storedFileIds.length) await db.insert(inspectionFiles).values(storedFileIds.map(storedFileId => ({ inspectionId: inspection.id, storedFileId })));
  return getInspectionById(inspection.ownerId, inspection.id);
}

export async function listInspections(ownerId: number) {
  const db = await getDb();
  if (!db) throw new Error('Database is not available');
  return db.select().from(inspections).where(eq(inspections.ownerId, ownerId)).orderBy(desc(inspections.createdAt));
}

export async function getInspectionById(ownerId: number, id: string) {
  const db = await getDb();
  if (!db) throw new Error('Database is not available');
  const inspection = (await db.select().from(inspections).where(eq(inspections.id, id)).limit(1))[0];
  if (!inspection || inspection.ownerId !== ownerId) return undefined;
  const files = await db.select({ id: storedFiles.id, fileKey: storedFiles.fileKey, url: storedFiles.url, filename: storedFiles.filename, contentType: storedFiles.contentType, size: storedFiles.size }).from(inspectionFiles).leftJoin(storedFiles, eq(inspectionFiles.storedFileId, storedFiles.id)).where(eq(inspectionFiles.inspectionId, id));
  return { ...inspection, files: files.filter(file => file.id !== null) };
}
