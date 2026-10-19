# WASIL Driver App — Integration Audit

**Audit branch:** `ops/wasil-integration-audit`  
**Base:** `fix/wasil-supabase-auth-review`  
**Production status:** Not approved for release. `main` is unchanged.

## Verified in repository
- Android app creates a `SupabaseDriverRepository` and passes it to `DriverViewModel`.
- Entry flow uses `SupabaseRoleAuthenticator(context, "driver")`; server profile role and active status are checked before granting access.
- Driver flow calls Supabase-backed refresh and location publication methods.
- Location tracking is tied to lifecycle/session/login/driver status, and is intended to stop when the driver is offline or logged out.
- Build configuration reads Supabase URL, publishable key, and Google Web OAuth client ID from local properties. Never embed a service-role/secret key in the client.

## Not yet verified
- Android build/test result for this audit branch.
- Real device location permissions, background/lifecycle behavior, battery use, and denied-permission paths.
- Live driver onboarding, online/offline updates, active-order exclusivity, assignment visibility, and location authorization under RLS.
- Full order lifecycle with customer, merchant, and admin.

## Release gates
1. Run Android build and unit tests for this branch.
2. Test role rejection, encrypted session restore/refresh/sign-out, location permission denial, online/offline, location update failures, and lifecycle cleanup.
3. Verify a driver can only read/update their own location and assigned orders, and cannot self-assign or bypass active-order limits.
4. Test order assignment and cancellation against disposable Supabase data.
5. Review findings before merge. No live migration or merge to `main` has been performed by this audit.
