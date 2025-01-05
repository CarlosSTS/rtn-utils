import type { TurboModule } from "react-native/Libraries/TurboModule/RCTExport";
import { TurboModuleRegistry } from "react-native";

export interface Spec extends TurboModule {
  isDeviceSecure(): Promise<boolean>;
  authenticate(map: { reason?: string; description?: string }): Promise<string>;
  openGlobalSettings(action: string): Promise<boolean>;
}

export default TurboModuleRegistry.get<Spec>("RTNUtils") as Spec | null;
