import { z } from 'zod';
import { nanoid } from 'nanoid';
import { COOKIE_NAME } from "@shared/const";
import { getSessionCookieOptions } from "./_core/cookies";
import { systemRouter } from "./_core/systemRouter";
import { protectedProcedure, publicProcedure, router } from "./_core/trpc";
import { createInspection, createStoredFile, getInspectionById, listInspections, listStoredFiles } from './db';
import { storagePut } from './storage';

const declarationInput = z.object({ label: z.string(), value: z.string(), status: z.enum(['detected', 'missing']) });
const violationInput = z.object({ type: z.string(), description: z.string(), rule: z.string(), severity: z.enum(['high', 'medium']) });
const uploadInput = z.object({ filename: z.string().min(1).max(255), contentType: z.enum(['image/jpeg', 'image/png', 'image/webp', 'image/heic']), size: z.number().int().positive().max(10 * 1024 * 1024), dataBase64: z.string().min(32) });
const inspectionInput = z.object({ productName: z.string().min(1).max(255), manufacturer: z.string().min(1).max(255), status: z.enum(['COMPLIANT', 'NON-COMPLIANT', 'NEEDS REVIEW']), declarations: z.array(declarationInput), violations: z.array(violationInput), confidence: z.number().int().min(0).max(100), location: z.string().min(1).max(255), storedFileIds: z.array(z.number().int().positive()).max(12) });

export const appRouter = router({
  system: systemRouter,
  auth: router({
    me: publicProcedure.query(opts => opts.ctx.user),
    logout: publicProcedure.mutation(({ ctx }) => { const cookieOptions = getSessionCookieOptions(ctx.req); ctx.res.clearCookie(COOKIE_NAME, { ...cookieOptions, maxAge: -1 }); return { success: true } as const; }),
  }),
  storage: router({
    uploadEvidence: protectedProcedure.input(uploadInput).mutation(async ({ input, ctx }) => {
      const bytes = Buffer.from(input.dataBase64, 'base64');
      if (bytes.byteLength !== input.size) throw new Error('Uploaded file size did not match its metadata.');
      const safeName = input.filename.replace(/[^a-zA-Z0-9._-]/g, '_');
      const upload = await storagePut(`inspections/${ctx.user.id}/${Date.now()}-${safeName}`, bytes, input.contentType);
      const record = await createStoredFile({ ownerId: ctx.user.id, fileKey: upload.key, url: upload.url, filename: input.filename, contentType: input.contentType, size: input.size });
      return { ...record, url: upload.url };
    }),
    listMine: protectedProcedure.query(({ ctx }) => listStoredFiles(ctx.user.id)),
  }),
  inspections: router({
    create: protectedProcedure.input(inspectionInput).mutation(({ input, ctx }) => createInspection({ id: `INS-${Date.now()}-${nanoid(6).toUpperCase()}`, ownerId: ctx.user.id, productName: input.productName, manufacturer: input.manufacturer, status: input.status, declarations: JSON.stringify(input.declarations), violations: JSON.stringify(input.violations), confidence: input.confidence, location: input.location }, input.storedFileIds)),
    list: protectedProcedure.query(({ ctx }) => listInspections(ctx.user.id)),
    byId: protectedProcedure.input(z.object({ id: z.string().min(1) })).query(({ input, ctx }) => getInspectionById(ctx.user.id, input.id)),
  }),
});

export type AppRouter = typeof appRouter;
