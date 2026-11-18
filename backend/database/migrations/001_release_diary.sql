-- Upgrade the original local schema to the current backend contract.
-- Back up the database before applying. Existing rows are preserved.
BEGIN;
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS updated_at timestamp with time zone;
ALTER TABLE public.diary_entries ADD COLUMN IF NOT EXISTS meal_type varchar(20);
ALTER TABLE public.diary_entries ADD COLUMN IF NOT EXISTS eaten_at timestamp with time zone;
ALTER TABLE public.diary_entries ADD COLUMN IF NOT EXISTS product_updated boolean NOT NULL DEFAULT false;
UPDATE public.diary_entries SET meal_type = 'snack' WHERE meal_type IS NULL;
UPDATE public.diary_entries SET eaten_at = COALESCE(logged_at, logged_date::timestamp AT TIME ZONE 'Europe/Moscow') WHERE eaten_at IS NULL;
ALTER TABLE public.diary_entries ALTER COLUMN meal_type SET NOT NULL;
ALTER TABLE public.diary_entries ALTER COLUMN eaten_at SET NOT NULL;
DO $$ BEGIN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.diary_entries'::regclass AND conname = 'chk_diary_meal_type') THEN
ALTER TABLE public.diary_entries ADD CONSTRAINT chk_diary_meal_type CHECK (meal_type IN ('breakfast', 'lunch', 'dinner', 'snack'));
END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_diary_stats ON public.diary_entries (user_id, eaten_at, meal_type);
COMMIT;
