# RxPermissions

Reactive implementation of Android runtime permissions using RxJava3.

## API

This library provides simple and lightweight apis that returns a `PermissionResult`.

Requesting for permissions:

```java
RxPermissions(this).request(...)
```

Request for permission results (skips granted permission results):

```java
RxPermissions(this).requestSkipGranted(...)
```

Request for simple permission (Just `true` or `false`):

```java
RxPermissions(this).requestSimple(...)
```

The `PermissionResult` model:

```java
PermissionResult.index      // The index of requesting permission order
PermissionResult.permission // Manifest.permission.XXX
PermissionResult.granted    // true or false
PermissionResult.rationale  // true or false
```

> Incase of Android version < M, result is always true.

#### Points to remember

1. Already granted permission results are emitted as soon as requested with `.request(...)` method. (Eg. If camera permission is already granted, you can start the camera related work, while Android system is asking for remaining permission)
2. Use `.requestSkipGranted(...)` method to skip the results of already granted permission results.
3. Use `.requestSimple(...)` method to get just `true` or `false` values, if you don't care about rationale permission results.

## License

```
Copyright 2020 Abarajithan Lv

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
