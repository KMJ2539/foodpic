import { test, expect } from '@playwright/test';

test('homepage renders title', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'Foodpic' })).toBeVisible();
});
