# Info
This builds an android APK that runs a rust wasmtime binary. The binary loads a wasm component model plugin, and their futures are driven by kotlin's coroutine 'executor'.

# Steps
1. Follow the steps [here](https://github.com/tlsnotary/tlsn/tree/poc/wasmtime-plugin-android/crates/wasmtime-plugin) to build the wasm component model plugin.
2. Follow the steps [here](https://github.com/tlsnotary/tlsn/tree/poc/wasmtime-plugin-android/crates/wasmtime-host) to build the wasmtime binary.
3. Build and run this app via an emulator in [Android Studio](https://developer.android.com/studio/intro).
4. Open Logcat in Android Studio to see logs from the app, the wasmtime binary as well as the wasm plugin.
