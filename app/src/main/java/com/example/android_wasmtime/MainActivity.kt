package com.example.android_wasmtime

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.android_wasmtime.ui.theme.AndroidwasmtimeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.security.MessageDigest
import uniffi.wasmtime_host.main

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Launch concurrent coroutines to prove Kotlin is driving the futures

        // Coroutine 1: Call Rust FFI main function (will sleep for 10s)
        // Using Dispatchers.Main to prove it's coroutine-driven, not OS thread pool
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                Log.d("WasmtimeTest", "Preparing to call Rust main function...")

                // Copy WASM file from assets to internal storage
                val wasmPath = copyAssetToInternalStorage("plugin.wasm")

                Log.d("WasmtimeTest", "Calling Rust main function with path: $wasmPath")
                Log.d("WasmtimeTest", "File exists: ${java.io.File(wasmPath).exists()}")
                Log.d("WasmtimeTest", "File size: ${java.io.File(wasmPath).length()} bytes")

                main(wasmPath)

                Log.d("WasmtimeTest", "Rust main function completed successfully!")
            } catch (e: Exception) {
                Log.e("WasmtimeTest", "Error calling Rust main function", e)
            }
        }

        // Coroutine 2: Concurrent task to prove Kotlin coroutines are working
        // Also on Main dispatcher to show true cooperative concurrency
        lifecycleScope.launch(Dispatchers.Main) {
            Log.d("WasmtimeTest", "Starting concurrent Kotlin coroutine...")

            for (i in 1..15) {
                delay(1000) // 1 second delay
                Log.d("WasmtimeTest", "Concurrent coroutine tick $i - Rust main is still running!")
            }

            Log.d("WasmtimeTest", "Concurrent coroutine completed!")
        }

        setContent {
            AndroidwasmtimeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun copyAssetToInternalStorage(assetFileName: String): String {
        val outputFile = java.io.File(filesDir, assetFileName)

        try {
            // Check if file needs to be copied
            val needsCopy = if (outputFile.exists()) {
                // Compare file hashes to detect changes
                val assetHash = getAssetFileHash(assetFileName)
                val existingHash = getFileHash(outputFile)

                if (assetHash != existingHash) {
                    Log.d("WasmtimeTest", "File content changed - Asset hash: $assetHash, Existing hash: $existingHash")
                    true
                } else {
                    Log.d("WasmtimeTest", "WASM file already exists and content matches, skipping copy")
                    false
                }
            } else {
                Log.d("WasmtimeTest", "WASM file doesn't exist, needs copy")
                true
            }

            if (needsCopy) {
                assets.open(assetFileName).use { inputStream ->
                    outputFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Log.d("WasmtimeTest", "WASM file copied to: ${outputFile.absolutePath}")
            }

            return outputFile.absolutePath
        } catch (e: Exception) {
            Log.e("WasmtimeTest", "Failed to copy WASM file from assets", e)
            throw e
        }
    }

    private fun getAssetFileHash(assetFileName: String): String {
        return assets.open(assetFileName).use { inputStream ->
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        }
    }

    private fun getFileHash(file: java.io.File): String {
        return file.inputStream().use { inputStream ->
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidwasmtimeTheme {
        Greeting("Android")
    }
}