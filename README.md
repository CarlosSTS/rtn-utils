# `rtn-utils`

`rtn-utils` is a toolkit and utilities library for React Native, built using Turbo Modules. It provides authentication methods, access to global settings (such as location and Bluetooth), location app integration and installed-app insights for Android devices.
## Local development

How to test local changes to the library inside a React Native app (New Architecture enabled) before publishing.

### Packed tarball

Installs exactly what would be published to npm, so it also validates the `files` field in `package.json`.

```bash
# in the library folder
cd path/to/rtn-utils
npm pack
# generates carlossts-rtn-utils-<version>.tgz

# in the React Native app folder
cd path/to/your-app
npm install ../rtn-utils/carlossts-rtn-utils-<version>.tgz
# or
yarn add ../rtn-utils/carlossts-rtn-utils-<version>.tgz

npx react-native run-android
```

Repeat `npm pack` + install after every change to the library.

### When to rebuild

| Change                                  | What to do                                                  |
| --------------------------------------- | ----------------------------------------------------------- |
| `js/index.ts` only                      | Reload the app (Metro picks it up)                          |
| `js/NativeGetRtnUtils.ts` (Turbo Module spec) | Rebuild the Android app, so codegen regenerates `NativeGetRtnUtilsSpec` |
| Kotlin files / `AndroidManifest.xml`    | Rebuild the Android app (`npx react-native run-android`)    |

If Gradle keeps stale generated code, clean it:

```bash
cd android && ./gradlew clean && cd ..
npx react-native run-android
```

## Features

- Support for fingerprint, PIN, and pattern authentication on Android.
- Open global device settings screens.
- List location apps and open them with coordinates.
- List installed apps with metadata, usage time and storage size, plus a device-wide RAM snapshot.
- Fully compatible with React Native's Turbo Module system.

## Installation

### Prerequisites

Ensure your React Native project is properly configured to use Turbo Modules. For more details, follow the official [React Native Turbo Modules documentation](https://reactnative.dev/docs/the-new-architecture/landing-page).

### Install the package

```bash
npm install @carlossts/rtn-utils
or
yarn add @carlossts/rtn-utils
```

## UI

### authenticate method

<table>
  <tr>
<td><img src="https://res.cloudinary.com/dbw8igay3/image/upload/v1767028349/image01_hia3w7.png" alt="fingerprintOrPin" width="300" /></td>
<td><img src="https://res.cloudinary.com/dbw8igay3/image/upload/v1767028347/image02_pqry7x.png" alt="PIN" width="300" /></td>
<td><img src="https://res.cloudinary.com/dbw8igay3/image/upload/v1767028345/image03_yraxmp.png" alt="pattern" width="300" /></td>
</tr>
</table>

### isDeviceSecure method
<table>
  <tr>
<td><img src="https://res.cloudinary.com/dbw8igay3/image/upload/v1767028344/image04_shvpgp.png" alt="isDeviceSecure" width="300" /></td>
</tr>
</table>

### openGlobalSettings method
<table>
  <tr>
<td><img src="https://res.cloudinary.com/dbw8igay3/image/upload/v1767030091/image06_f2bb9t.png" alt="openGlobalSettings" width="300" /></td>
</tr>
</table>

### getLocationApps method
<img src="https://res.cloudinary.com/dbw8igay3/image/upload/v1767030091/image07_lwntgw.png" width="300" />

### openAppWithLocation method
<img src="https://res.cloudinary.com/dbw8igay3/image/upload/v1767030092/image08_u1yacs.png" width="300" />

## API Reference

## Methods

### `authenticate(map: { reason?: string; description?: string }): Promise<string>()`

Local device authentication process (using password, PIN, pattern or fingerprint), verifying that the device is protected by some security method, such as a password or biometrics.

Observation: If the device does not have local authentication, return success with the code WITHOUT_AUTHENTICATION.

## Options

| Option                | Description                                       |
| --------------------- | ------------------------------------------------- |
| reason                | Action title                                      |
| description           | Action description                                |

## Usage

```js
import React, { useCallback, useEffect } from 'react';
import { Alert, View } from 'react-native';
import { RTNUtils } from '@carlossts/rtn-utils';

const App = () => {
  const authenticationLocal = useCallback(async () => {
    try {
      await RTNUtils.authenticate({
        reason: 'Please authenticate yourself',
        description: 'Enter your password or fingerprint',
      });
    } catch (error) {
      Alert.alert('Authentication Failed', error.message);
    }
  }, []);

  useEffect(() => {
    authenticationLocal();
  }, [authenticationLocal]);

  return <View />;
};

export default App;
```

## ErrorCode

| Code                  | Description                                       |
| --------------------- | ------------------------------------------------- |
| E_AUTH_CANCELLED      | User canceled the authentication                  |
| E_ONE_REQ_AT_A_TIME   | Authentication already in progress                |
| E_FAILED_TO_SHOW_AUTH | Failed to create authentication intent            |
| E_ACTIVITY_DOES_NOT_EXIST | No current activity to show the authentication |

##

### `isDeviceSecure(): Promise<boolean>`

Checks if the device has some type of authentication.

## Usage

```js
import React, { useCallback, useEffect } from 'react';
import { Alert, View } from 'react-native';
import { RTNUtils } from '@carlossts/rtn-utils';

const App = () => {
  const isDeviceSecure = useCallback(async () => {
    try {
     const result = await RTNUtils?.isDeviceSecure();
    Alert.alert('Is it a secure device ?', result ? 'Yes' : 'No');
    } catch (error) {
      Alert.alert('isDeviceSecure Failed', error.message);
    }
  }, []);

  useEffect(() => {
    isDeviceSecure();
  }, [isDeviceSecure]);

  return <View />;
};

export default App;
```

##

### `openGlobalSettings(action: string): Promise<boolean>`

Open a global device setting. 

> Verify [Android Settings Reference](https://developer.android.com/reference/android/provider/Settings)

## Usage

```js
import React, { useCallback, useEffect } from 'react';
import { Alert, View } from 'react-native';
import { RTNUtils } from '@carlossts/rtn-utils';

const App = () => {
  const openGlobalSettings = useCallback(async () => {
    try {
      const result = await RTNUtils?.openGlobalSettings('android.settings.LOCATION_SOURCE_SETTINGS');
      return result;
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'An unknown error occurred';
      Alert.alert('openGlobalSettings Failed', errorMessage);
    }
  }, []);

  useEffect(() => {
    openGlobalSettings();
  }, [openGlobalSettings]);

  return <View />;
};

export default App;
```

## ErrorCode

| Code                      | Description                                   |
| ------------------------- | --------------------------------------------- |
| E_ACTIVITY_DOES_NOT_EXIST | No current activity to open the settings from |
| E_ACTION_IS_EMPTY         | Action is empty                               |
| E_FAILED_TO_OPEN_SETTINGS | Failed to open the settings screen            |

##

### `getLocationApps(options: { includesBase64: boolean }): Promise<{ name: string; package: string; icon?: string; }[]>;`

Retrieves a list of location-related apps with optional icons in base64 encoding.

> [Verify example](https://github.com/CarlosSTS/OpenMapsApp)

## Options

| Option                | Description                                       |
| --------------------- | ------------------------------------------------- |
| includesBase64        | returns the app's icon in base64 encoding         |

## Usage

```js
import React, { useCallback, useEffect } from 'react';
import { Alert, View } from 'react-native';
import { RTNUtils } from '@carlossts/rtn-utils';

const App = () => {
   const getLocationApps = useCallback(async () => {
    try {
      const apps = await RTNUtils?.getLocationApps({
        includesBase64: true
      });
      Alert.alert('Total applications found', String(apps?.length ?? 0));
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'An unknown error occurred';
      Alert.alert('getLocationApps Failed', errorMessage);
    }
  }, []);

  useEffect(() => {
    getLocationApps();
  }, [getLocationApps]);

  return <View />;
};

export default App;
```

## ErrorCode

| Code                  | Description                                       |
| --------------------- | ------------------------------------------------- |
| E_INTENT_IS_NULL      | Intent is null                                    |
| E_GET_ICON_APP        | Failed to get icon app                            |

##

### `openAppWithLocation(options: { url: string; packageName: string }): Promise<string>;`

Opens a location-related app based on the provided URL and package name.

> [Verify example](https://github.com/CarlosSTS/OpenMapsApp)

## Options

| Option                | Description                                       |
| --------------------- | ------------------------------------------------- |
| url                   | URL to open the app with parameters               |
| packageName           | App package name                                  |

## Usage

```js
import React, { useCallback, useEffect } from 'react';
import { Alert, View } from 'react-native';
import { RTNUtils } from '@carlossts/rtn-utils';

const App = () => {
   const openAppWithLocation = useCallback(async () => {
    try {
      const lat = -4.128489;
      const lng = -38.2593854;
      const label = 'My Location Test';
      const scheme = `geo:0,0?q=${lat},${lng}(${label})`;

      const apps = await RTNUtils?.openAppWithLocation({
        packageName: "com.google.android.apps.maps",
        url: scheme,
      });
      Alert.alert('App opened successfully');
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'An unknown error occurred';
      Alert.alert('openAppWithLocation Failed', errorMessage);
    }
  }, []);

  useEffect(() => {
    openAppWithLocation();
  }, [openAppWithLocation]);

  return <View />;
};

export default App;
```

## ErrorCode

| Code                  | Description                                       |
| --------------------- | ------------------------------------------------- |
| E_INTENT_IS_NULL      | App does not support the URL scheme               |
| E_VALIDATION_FAILS    | Fields are required                               |
| E_PACKAGE_NOT_FOUND   | Package not found                                 |

##

### App insights (`getInstalledApps`, memory & usage access)

List the launchable apps installed on the device with metadata and — when the
**Usage access** permission is granted — per-app foreground time and storage
footprint.

> **About "memory consumption":** Android does **not** expose the real-time RAM
> usage of third-party apps to regular (non-root, Play-compliant) apps.
> `getRunningAppProcesses()` only returns your own process, and `/proc` is
> `hidepid`-restricted since Android 7. So this API reports **storage size**
> (`totalSizeBytes`) and **foreground time** (`usageTimeMs`) as the per-app
> "consumption" metrics, plus a device-wide RAM snapshot via
> [`getDeviceMemoryInfo`](#getdevicememoryinfo).

> **Play Store compliance:** the library only enumerates apps that expose a
> launcher activity, declaring a `<queries>` element for `MAIN`/`LAUNCHER`
> intents. It does **not** use `QUERY_ALL_PACKAGES`, so no Play Console
> declaration is required. It does declare `android.permission.PACKAGE_USAGE_STATS`
> (a special "appop" permission the user grants manually); if your app targets
> Google Play, disclose this in your listing and privacy policy.

#### `hasUsageAccessPermission(): Promise<boolean>`

Resolves `true` when the app already holds the `PACKAGE_USAGE_STATS` permission.
This permission cannot be requested with a runtime dialog.

#### `openUsageAccessSettings(): Promise<boolean>`

Opens the system **Settings → Usage access** screen so the user can grant the
permission. Resolves `true` if the screen was opened.

#### `openAppSettings(packageName: string): Promise<boolean>`

Opens the system **App info** screen of the given package (storage, permissions,
force stop, uninstall). Resolves `true` if the screen was opened.

```js
await RTNUtils?.openAppSettings('com.google.android.youtube');
```

| Code                      | Description                                   |
| ------------------------- | --------------------------------------------- |
| E_VALIDATION_FAILS        | Package name is empty                         |
| E_PACKAGE_NOT_FOUND       | App not installed (or not visible to the app) |
| E_FAILED_TO_OPEN_SETTINGS | Failed to open the App info screen            |

<a id="getdevicememoryinfo"></a>

#### `getDeviceMemoryInfo(): Promise<{ ... }>`

Device-wide RAM snapshot from `ActivityManager.MemoryInfo`. No permission needed.

| Field            | Type    | Description                                        |
| ---------------- | ------- | ------------------------------------------------- |
| `totalBytes`     | number  | Total physical RAM                                |
| `availableBytes` | number  | RAM available to start new processes             |
| `usedBytes`      | number  | `totalBytes - availableBytes`                    |
| `lowMemory`      | boolean | Whether the system is under memory pressure      |
| `thresholdBytes` | number  | Low-memory threshold used by the system          |

#### `getInstalledApps(options?): Promise<{ usageAccessGranted, totalCount, apps }>`

## Options

| Option              | Type    | Default       | Description                                                        |
| ------------------- | ------- | ------------- | ---------------------------------------------------------------- |
| `includeSystemApps` | boolean | `false`       | Include system apps that also have a launcher                     |
| `includeIcons`      | boolean | `false`       | Attach each icon as a base64 data URI (noticeably heavier)        |
| `sortBy`            | string  | `"totalSize"` | `"totalSize"` \| `"usageTime"` \| `"lastUsed"` \| `"name"`        |
| `usagePeriod`       | string  | `"week"`      | Usage window: `"day"` \| `"week"` \| `"month"` \| `"year"`        |
| `limit`             | number  | `0`           | Max apps returned after sorting (`0` = all)                       |

### Result

`usageAccessGranted` (boolean), `totalCount` (number, before `limit`) and `apps[]`:

| Field              | Type    | Description                                                             |
| ------------------ | ------- | -------------------------------------------------------------------- |
| `packageName`      | string  | Application id                                                         |
| `appName`          | string  | User-visible label                                                    |
| `versionName`      | string  | May be empty when the app declares none                              |
| `versionCode`      | number  | `longVersionCode` on API 28+                                          |
| `icon`             | string? | base64 data URI, only when `includeIcons` is `true`                  |
| `isSystemApp`      | boolean | `FLAG_SYSTEM` / `FLAG_UPDATED_SYSTEM_APP`                            |
| `enabled`          | boolean | Whether the app is currently enabled                                 |
| `firstInstallTime` | number  | Epoch ms                                                             |
| `lastUpdateTime`   | number  | Epoch ms                                                             |
| `targetSdkVersion` | number  | App `targetSdkVersion`                                               |
| `minSdkVersion`    | number  | App `minSdkVersion` (`0` on API < 24)                                |
| `category`         | string  | `"game"`, `"audio"`, `"productivity"`, … or `"undefined"`            |
| `permissionsCount` | number  | Number of permissions the app requests                              |
| `usageTimeMs`      | number  | Foreground time in the period (`0` if not granted / unused)          |
| `lastUsedTime`     | number  | Epoch ms of last use (`0` if never / not granted)                    |
| `launchCount`      | number  | Times moved to foreground in the period                             |
| `appSizeBytes`     | number  | APKs (including splits), compiled code and native libs (`-1` when unavailable) |
| `dataSizeBytes`    | number  | App data size, excluding cache (`-1` when unavailable)               |
| `cacheSizeBytes`   | number  | Cache size (`-1` when unavailable)                                   |
| `totalSizeBytes`   | number  | Sum of the three (`-1` when all unavailable)                         |

## Usage

```js
import React, { useCallback, useEffect, useState } from 'react';
import { Alert, View } from 'react-native';
import { RTNUtils } from '@carlossts/rtn-utils';

const App = () => {
  const [apps, setApps] = useState([]);

  const loadApps = useCallback(async () => {
    try {
      const granted = await RTNUtils?.hasUsageAccessPermission();
      if (!granted) {
        Alert.alert(
          'Permission needed',
          'Enable "Usage access" to see per-app usage time and size.',
          [{ text: 'Open settings', onPress: () => RTNUtils?.openUsageAccessSettings() }],
        );
      }

      const result = await RTNUtils?.getInstalledApps({
        includeIcons: true,
        sortBy: 'totalSize',
        usagePeriod: 'week',
        limit: 30,
      });
      setApps(result?.apps ?? []);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown error';
      Alert.alert('getInstalledApps Failed', message);
    }
  }, []);

  useEffect(() => {
    loadApps();
  }, [loadApps]);

  return <View />;
};

export default App;
```

## ErrorCode

| Code                   | Description                                       |
| ---------------------- | ------------------------------------------------- |
| E_GET_INSTALLED_APPS   | Failed to list installed apps or read the usage access state (`hasUsageAccessPermission`) |
| E_GET_MEMORY_INFO      | Failed to read device memory info                 |
| E_FAILED_TO_OPEN_SETTINGS | Failed to open usage access settings           |

## License

[MIT](LICENSE)

