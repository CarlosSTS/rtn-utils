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

![fingerprintOrPin](https://firebasestorage.googleapis.com/v0/b/portfolio-web-7fbff.appspot.com/o/libs_npm%2Fcarlossts-rtn-local-authentication%2Fimage01.png?alt=media&token=293510fd-170a-42bc-b1e9-d9bd06a888ec)
![PIN](https://firebasestorage.googleapis.com/v0/b/portfolio-web-7fbff.appspot.com/o/libs_npm%2Fcarlossts-rtn-local-authentication%2Fimage02.png?alt=media&token=81209a98-48e0-4ebe-830b-3c1aa1a54d8f)
![fingerprintOrPattern](https://firebasestorage.googleapis.com/v0/b/portfolio-web-7fbff.appspot.com/o/libs_npm%2Fcarlossts-rtn-local-authentication%2Fimage03.png?alt=media&token=e27c55c1-c3f4-44b7-99b7-f5dd2e069425)
![pattern](https://firebasestorage.googleapis.com/v0/b/portfolio-web-7fbff.appspot.com/o/libs_npm%2Fcarlossts-rtn-local-authentication%2Fimage04.png?alt=media&token=712ac8eb-8c3e-44a5-9f47-57297bffb685)

### isDeviceSecure method
![isDeviceSecure](https://firebasestorage.googleapis.com/v0/b/portfolio-web-7fbff.appspot.com/o/libs_npm%2Fcarlossts-rtn-local-authentication%2Fimage05.png?alt=media&token=2f76b088-23c5-4f14-b248-649f3065299d)

### openGlobalSettings method
![openGlobalSettings](https://firebasestorage.googleapis.com/v0/b/portfolio-web-7fbff.appspot.com/o/libs_npm%2Frtn-utils%2Fimage06.jpeg?alt=media&token=823f08fc-ac03-4d00-bc21-6c8e71c1cb1d)

### getLocationApps method
<img src="https://firebasestorage.googleapis.com/v0/b/portfolio-web-7fbff.appspot.com/o/libs_npm%2Frtn-utils%2Fimage07.jpeg?alt=media&token=455c652f-a1d9-41c1-8e7c-8099b9c18c0f" width="200" />

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
      Alert.alert('total applications found:' apps.length);
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
| includesBase64        | returns the app's icon in base64 encoding         |

## Usage

```js
import React, { useCallback, useEffect } from 'react';
import { Alert, View } from 'react-native';
import { RTNUtils } from '@carlossts/rtn-utils';

const App = () => {
   const openAppWithLocation = useCallback(async () => {
    try {
      const apps = await RTNUtils?.openAppWithLocation({
        includesBase64: true
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
| E_INTENT_IS_NULL      | Intent is null                                    |
| E_VALIDATION_FAILS    | Fields are required                               |
| E_PACKAGE_NOT_FOUND   | Package not found                                 |


## License

[MIT](LICENSE.md)

