package com.erns.alertauni.screen.utils

import android.content.Context
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

/**
 * Envoltura del escáner de QR de Google Play services.
 *
 * Se eligió frente a CameraX + ML Kit porque la interfaz de cámara la provee
 * Play services: la app no necesita el permiso CAMERA ni mantener un visor propio.
 * Restricción: requiere Google Play services (no funciona en dispositivos sin ellos).
 */
class QrCodeScanner(private val context: Context) {

    private val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .enableAutoZoom()
        .build()

    fun scan(
        onResult: (String?) -> Unit,
        onCancel: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        GmsBarcodeScanning.getClient(context, options)
            .startScan()
            .addOnSuccessListener { barcode -> onResult(barcode.rawValue) }
            .addOnCanceledListener(onCancel)
            .addOnFailureListener(onError)
    }
}
