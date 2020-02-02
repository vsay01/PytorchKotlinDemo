package com.say.pytorchkotlindemo

import android.app.Application
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.say.pytorchkotlindemo.Utils.assetFilePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class LiveDataViewModel(
    private val app: Application,
    private val imageAnalysis: ImageAnalysis
) : AndroidViewModel(app) {

    private var moduleAssetName: String = "model_1.pt"

    fun analyseTheImage(imageView: ImageView) {
        viewModelScope.launch {
            val drawable = imageView.drawable as BitmapDrawable
            val bitmap = drawable.bitmap
            val moduleFileAbsoluteFilePath = File(
                assetFilePath(app, moduleAssetName)
            ).absolutePath
            imageAnalysis.analyzeImage(bitmap, moduleFileAbsoluteFilePath)
        }
    }

    val result = imageAnalysis.result
}

/**
 * Factory for [LiveDataViewModel].
 */
class LiveDataVMFactory(private val application: Application) :
    ViewModelProvider.AndroidViewModelFactory(application) {

    private val imageClassificationOperation = ImageClassificationOperation(Dispatchers.IO)

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return LiveDataViewModel(
            application, imageClassificationOperation
        ) as T
    }
}
