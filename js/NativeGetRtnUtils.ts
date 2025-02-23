import type { TurboModule } from "react-native/Libraries/TurboModule/RCTExport";
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
}

export default TurboModuleRegistry.get<Spec>("RTNUtils") as Spec | null;
