# `rtn-utils`

`rtn-utils` It is a toolkit and utilities library for React Native, built using Turbo Modules. It provides authentication methods and access to global settings, such as location and Bluetooth configuration, for Android devices.

## Features

- Support for fingerprint, PIN, and pattern authentication on Android.
- Fully compatible with React Native's Turbo Module system.
- Simple API to integrate local authentication into your React Native application.

## Installation

### Prerequisites

Ensure your React Native project is properly configured to use Turbo Modules. For more details, follow the official [React Native Turbo Modules documentation](https://reactnative.dev/docs/the-new-architecture/landing-page).

### Install the package

```bash
npm install @carlossts/rtn-utils
or
yarn add @carlossts/rtn-utils
```

## Local development (using the library in another React Native project)

To test unpublished changes in a React Native app, use a local package. The examples below assume this layout:

```
projects/
├── rtn-utils/       # this library
└── my-app/          # React Native app that consumes it
```

### Tarball (`npm pack`)

`npm pack` produces the same package that npm would publish, so the app uses the library exactly as it would after a release.

1. Generate the tarball in the library folder:

   ```bash
   cd rtn-utils
   npm pack
   # -> carlossts-rtn-utils-<version>.tgz (version from package.json, e.g. 1.1.0)
   ```

2. Point the dependency to the file in the app's `package.json`:

   ```json
   "dependencies": {
     "@carlossts/rtn-utils": "file:../rtn-utils/carlossts-rtn-utils-1.1.0.tgz"
   }
   ```

3. Install and rebuild the app. Codegen and the native code run at build time, so a clean build is required:

   ```bash
   cd ../my-app
   yarn install            # or npm install
   cd android && ./gradlew clean && rm -rf app/.cxx && cd ..
   yarn android
   ```

To pick up new changes:

1. Run `npm pack` again in the library folder.
2. Reinstall the tarball in the app:
   - If the version in `package.json` changed, run `yarn add file:../rtn-utils/carlossts-rtn-utils-<new-version>.tgz`. This command also updates the path in the app's `package.json`.
   - If the version didn't change, Yarn 1 keeps installing the old copy, even after `yarn install` or `yarn cache clean @carlossts/rtn-utils`. Delete its cached copies (including the extracted ones in `.tmp`), then add the file again:

     ```bash
     cd ../my-app
     CACHE="$(yarn cache dir)"
     find "$CACHE" -maxdepth 1 -name 'npm-@carlossts-rtn-utils-*' -exec rm -rf {} +
     find "$CACHE/.tmp" -mindepth 2 -maxdepth 2 -name package.json -exec grep -l '"@carlossts/rtn-utils"' {} + 2>/dev/null | xargs -r -n1 dirname | xargs -r rm -rf
     yarn add file:../rtn-utils/carlossts-rtn-utils-<version>.tgz
     ```

     Check the result: the `resolved` hash for `@carlossts/rtn-utils` in `yarn.lock` must change whenever the tarball content changes.
3. Clean and rebuild the app (step 3 above).

> `*.tgz` files are ignored by this repo's `.gitignore`. Don't commit them.

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
      Alert.alert('total applications found:' apps?.length);
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'An unknown error occurred';
      Alert.alert('openGlobalSettings Failed', errorMessage);
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
> [`getDeviceMemoryInfo`](#getdevicememoryinfo-promise-).

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

Opens the system **App info** screen for `packageName`
(`Settings.ACTION_APPLICATION_DETAILS_SETTINGS`). Resolves `true` if the screen
was opened.

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
| `appSizeBytes`     | number  | APK + OBB + compiled code size; install directory without compiled code on Xiaomi, matching MIUI/HyperOS Settings (`-1` when unavailable) |
| `dataSizeBytes`    | number  | App data size, cache included (`-1` when unavailable)                |
| `cacheSizeBytes`   | number  | Cache size, already part of `dataSizeBytes` (`-1` when unavailable)  |
| `totalSizeBytes`   | number  | `appSizeBytes + dataSizeBytes` (`-1` when both unavailable)          |

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
| E_GET_INSTALLED_APPS   | Failed to list installed apps                     |
| E_GET_MEMORY_INFO      | Failed to read device memory info                 |
| E_FAILED_TO_OPEN_SETTINGS | Failed to open usage access settings           |

## License

[MIT](LICENSE.md)

