import { int, mysqlEnum, mysqlTable, text, timestamp, varchar } from "drizzle-orm/mysql-core";

export const users = mysqlTable("users", {
  id: int("id").autoincrement().primaryKey(),
  openId: varchar("openId", { length: 64 }).notNull().unique(),
  name: text("name"),
  email: varchar("email", { length: 320 }),
  loginMethod: varchar("loginMethod", { length: 64 }),
  role: mysqlEnum("role", ["user", "admin"]).default("user").notNull(),
  createdAt: timestamp("createdAt").defaultNow().notNull(),
  updatedAt: timestamp("updatedAt").defaultNow().onUpdateNow().notNull(),
  lastSignedIn: timestamp("lastSignedIn").defaultNow().notNull(),
});

export type User = typeof users.$inferSelect;
export type InsertUser = typeof users.$inferInsert;

/** Metadata only; binary evidence lives in managed object storage. */
export const storedFiles = mysqlTable("stored_files", {
  id: int("id").autoincrement().primaryKey(),
  ownerId: int("ownerId").notNull(),
  fileKey: varchar("fileKey", { length: 512 }).notNull().unique(),
  url: varchar("url", { length: 768 }).notNull(),
  filename: varchar("filename", { length: 255 }).notNull(),
  contentType: varchar("contentType", { length: 128 }).notNull(),
  size: int("size").notNull(),
  createdAt: timestamp("createdAt").defaultNow().notNull(),
});

export type StoredFile = typeof storedFiles.$inferSelect;
export type InsertStoredFile = typeof storedFiles.$inferInsert;

export const inspections = mysqlTable("inspections", {
  id: varchar("id", { length: 64 }).primaryKey(),
  ownerId: int("ownerId").notNull(),
  productName: varchar("productName", { length: 255 }).notNull(),
  manufacturer: varchar("manufacturer", { length: 255 }).notNull(),
  status: mysqlEnum("status", ["COMPLIANT", "NON-COMPLIANT", "NEEDS REVIEW"]).notNull(),
  declarations: text("declarations").notNull(),
  violations: text("violations").notNull(),
  confidence: int("confidence").notNull(),
  location: varchar("location", { length: 255 }).notNull(),
  createdAt: timestamp("createdAt").defaultNow().notNull(),
});

export const inspectionFiles = mysqlTable("inspection_files", {
  id: int("id").autoincrement().primaryKey(),
  inspectionId: varchar("inspectionId", { length: 64 }).notNull(),
  storedFileId: int("storedFileId").notNull(),
  createdAt: timestamp("createdAt").defaultNow().notNull(),
});

export type Inspection = typeof inspections.$inferSelect;
export type InsertInspection = typeof inspections.$inferInsert;
export type InspectionFile = typeof inspectionFiles.$inferSelect;
export type InsertInspectionFile = typeof inspectionFiles.$inferInsert;
