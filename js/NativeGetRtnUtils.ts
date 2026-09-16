import type { TurboModule } from "react-native";
import { TurboModuleRegistry } from "react-native";

export interface Spec extends TurboModule {
  isDeviceSecure(): Promise<boolean>;
  authenticate(map: { reason?: string; description?: string }): Promise<string>;
  openGlobalSettings(action: string): Promise<boolean>;

  /**
   * Retrieves a list of location-related apps with optional icons in base64 encoding.
   * 
   * If the `includesBase64` option is set to `true`, the method returns the app's icon in base64 encoding. 
   *
   * @param options Optional parameters to customize the returned app list:
   * - `includesBase64` (boolean): If `true`, includes the app's icon in base64 format. Default is `false`.
   * 
   * @returns A Promise that resolves to an array of objects representing the apps, each containing:
   * - `name` (string): The name of the app.
   * - `package` (string): The package name of the app.
   * - `icon` (string, optional): The app's icon in base64 format if `includesBase64` is `true`.
   * 
   * @example
   * getLocationApps({
   *   includesBase64: true,
   * }).then(apps => {
   *   apps.forEach(app => {
   *     console.log(app);
   *   });
   * });
   */
  getLocationApps(options?: { 
    includesBase64?: boolean; 
  }): Promise<
    { name: string; package: string; icon?: string; }[]
  >;

  /**
   * Opens a location-related app based on the provided URL and package name.
   * 
   * The method will check if the app with the given package name is installed.
   * If the app is installed, it will open the app using the provided URL.
   * If the app is not installed, the promise will be rejected with an error message.
   *
   * @param options Object containing:
   * - `url` (string): The URL to be opened in the app.
   * - `packageName` (string): The package name of the app to be opened.
   * 
   * @returns A Promise that resolves if the app was opened successfully or rejects if the app is not installed or an error occurs.
   * 
   * @example
   * openAppWithLocation({
   *   url: "geo:37.7749,-122.4194?q=San+Francisco",
   *   packageName: "com.google.android.apps.maps"
   * }).then(result => {
   *   console.log(result); // "App opened successfully"
   * }).catch(error => {
   *   console.error(error); // Handle error if app is not installed
   * });
   */
  openAppWithLocation(options: { url: string; packageName: string }): Promise<string>;

  /**
   * Checks whether the app has been granted the "Usage access" special permission
   * (`android.permission.PACKAGE_USAGE_STATS`).
   *
   * This permission is required for the per-app usage time and storage size fields
   * returned by {@link getInstalledApps}. It cannot be requested with a runtime
   * dialog — the user must enable it manually in Settings. Use
   * {@link openUsageAccessSettings} to send them there.
   *
   * @returns A Promise resolving to `true` when the permission is granted.
   */
  hasUsageAccessPermission(): Promise<boolean>;

  /**
   * Opens the system "Usage access" settings screen
   * (`Settings.ACTION_USAGE_ACCESS_SETTINGS`) so the user can grant the
   * `PACKAGE_USAGE_STATS` permission to the app.
   *
   * @returns A Promise resolving to `true` if the settings screen was opened.
   */
  openUsageAccessSettings(): Promise<boolean>;

  /**
   * Returns a device-wide RAM snapshot from `ActivityManager.MemoryInfo`.
   *
   * Android does not expose per-app RAM usage of third-party apps to regular
   * (non-root, Play-compliant) apps, so this is a whole-device view only.
   *
   * @returns A Promise resolving to:
   * - `totalBytes` (number): total physical RAM.
   * - `availableBytes` (number): RAM currently available to start new processes.
   * - `usedBytes` (number): `totalBytes - availableBytes`.
   * - `lowMemory` (boolean): whether the system considers itself low on memory.
   * - `thresholdBytes` (number): the low-memory threshold used by the system.
   */
  getDeviceMemoryInfo(): Promise<{
    totalBytes: number;
    availableBytes: number;
    usedBytes: number;
    lowMemory: boolean;
    thresholdBytes: number;
  }>;

  /**
   * Lists the launchable apps installed on the device with metadata and, when the
   * "Usage access" permission is granted, per-app usage time and storage size.
   *
   * Only apps that expose a launcher activity are returned. The library declares a
   * `<queries>` element for `MAIN`/`LAUNCHER` intents, so it works on Android 11+
   * without the sensitive `QUERY_ALL_PACKAGES` permission (and therefore without a
   * Play Console declaration).
   *
   * Real per-app RAM consumption is intentionally not included — Android provides
   * no Play-compliant API for it on non-rooted devices. Storage footprint
   * (`totalSizeBytes`) and foreground time (`usageTimeMs`) are the closest per-app
   * "consumption" metrics available.
   *
   * @param options Optional parameters:
   * - `includeSystemApps` (boolean): include apps flagged as system apps that also
   *   have a launcher. Default `false`.
   * - `includeIcons` (boolean): include each app icon as a base64 data URI. This is
   *   significantly heavier — enable only when needed. Default `false`.
   * - `sortBy` (string): `"totalSize"` (default), `"usageTime"`, `"lastUsed"` or
   *   `"name"`.
   * - `usagePeriod` (string): window for the usage stats — `"day"`, `"week"`
   *   (default), `"month"` or `"year"`.
   * - `limit` (number): max number of apps to return after sorting. `0` (default)
   *   returns all.
   *
   * @returns A Promise resolving to:
   * - `usageAccessGranted` (boolean): whether usage/storage fields are populated.
   * - `totalCount` (number): number of apps found before `limit` was applied.
   * - `apps` (array): one entry per app with:
   *   - `packageName` (string)
   *   - `appName` (string)
   *   - `versionName` (string): may be empty if the app declares none.
   *   - `versionCode` (number)
   *   - `icon` (string, optional): base64 data URI, only when `includeIcons` is true.
   *   - `isSystemApp` (boolean)
   *   - `enabled` (boolean)
   *   - `firstInstallTime` (number): epoch milliseconds.
   *   - `lastUpdateTime` (number): epoch milliseconds.
   *   - `targetSdkVersion` (number)
   *   - `minSdkVersion` (number)
   *   - `category` (string): e.g. `"game"`, `"audio"`, `"productivity"`,
   *     `"undefined"`.
   *   - `permissionsCount` (number): number of permissions the app requests.
   *   - `usageTimeMs` (number): foreground time in the selected period. `0` when the
   *     permission is not granted or the app was not used.
   *   - `lastUsedTime` (number): epoch milliseconds of last use, `0` if never / not
   *     granted.
   *   - `launchCount` (number): number of times moved to foreground in the period.
   *   - `appSizeBytes` (number): APK + OBB + compiled code size. On Xiaomi devices,
   *     APK files only, matching MIUI/HyperOS Settings. `-1` when unavailable.
   *   - `dataSizeBytes` (number): app data size, cache included. `-1` when unavailable.
   *   - `cacheSizeBytes` (number): cache size (already part of `dataSizeBytes`). `-1` when unavailable.
   *   - `totalSizeBytes` (number): `appSizeBytes + dataSizeBytes`.
   *     `-1` when unavailable.
   *
   * @example
   * const granted = await RTNUtils.hasUsageAccessPermission();
   * if (!granted) {
   *   await RTNUtils.openUsageAccessSettings();
   * }
   * const { apps } = await RTNUtils.getInstalledApps({
   *   includeIcons: true,
   *   sortBy: 'totalSize',
   *   usagePeriod: 'week',
   *   limit: 30,
   * });
   */
  getInstalledApps(options?: {
    includeSystemApps?: boolean;
    includeIcons?: boolean;
    sortBy?: string;
    usagePeriod?: string;
    limit?: number;
  }): Promise<{
    usageAccessGranted: boolean;
    totalCount: number;
    apps: {
      packageName: string;
      appName: string;
      versionName: string;
      versionCode: number;
      icon?: string;
      isSystemApp: boolean;
      enabled: boolean;
      firstInstallTime: number;
      lastUpdateTime: number;
      targetSdkVersion: number;
      minSdkVersion: number;
      category: string;
      permissionsCount: number;
      usageTimeMs: number;
      lastUsedTime: number;
      launchCount: number;
      appSizeBytes: number;
      dataSizeBytes: number;
      cacheSizeBytes: number;
      totalSizeBytes: number;
    }[];
  }>;
}

export default TurboModuleRegistry.get<Spec>("RTNUtils") as Spec | null;
