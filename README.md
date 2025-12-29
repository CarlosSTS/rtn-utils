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


## License

[MIT](LICENSE.md)

