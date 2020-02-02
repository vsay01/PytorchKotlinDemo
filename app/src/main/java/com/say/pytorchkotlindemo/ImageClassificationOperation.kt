package com.say.pytorchkotlindemo

import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils

const val TOP_K = 3

class ImageClassificationOperation(private val ioDispatcher: CoroutineDispatcher) : ImageAnalysis {
    private var analyzeImageErrorState: Boolean = false
    private var module: Module? = null
    private var inputTensor: Tensor? = null

    private val _result = MutableLiveData(AnalysisResult(arrayOf("Result"), floatArrayOf(0F), 0, 0))
    override val result: LiveData<AnalysisResult> = _result

    override suspend fun analyzeImage(
        bitmap: Bitmap,
        moduleFileAbsoluteFilePath: String
    ) {
        // Force Main thread
        withContext(Dispatchers.Main) {
            _result.value = AnalysisResult(arrayOf("Analyse Image..."), floatArrayOf(0F), 0, 0)
            _result.value = analyseImageImpl(bitmap, moduleFileAbsoluteFilePath)
        }
    }

    private suspend fun analyseImageImpl(
        bitmap: Bitmap,
        moduleFileAbsoluteFilePath: String
    ): AnalysisResult? = withContext(ioDispatcher) {
        if (analyzeImageErrorState) {
            null
        }

        try {

            module = Module.load(moduleFileAbsoluteFilePath)

            val startTime = SystemClock.elapsedRealtime()

            inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                bitmap,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                TensorImageUtils.TORCHVISION_NORM_STD_RGB
            )

            val moduleForwardStartTime = SystemClock.elapsedRealtime()
            val outputTensor = module?.forward(IValue.from(inputTensor))?.toTensor()
            val moduleForwardDuration = SystemClock.elapsedRealtime() - moduleForwardStartTime

            val scores = outputTensor?.dataAsFloatArray
            scores?.let {
                val ixs = Utils.topK(scores, TOP_K)
                val topKClassNames = Array(TOP_K) { i -> (i * i).toString() }
                val topKScores = FloatArray(TOP_K)
                for (i in 0 until TOP_K) {
                    val ix = ixs[i]
                    if (ix <= Constants.IMAGE_NET_CLASSNAME.size) {
                        topKClassNames[i] = Constants.IMAGE_NET_CLASSNAME[ix]
                    }
                    topKScores[i] = scores[ix]
                }
                val analysisDuration = SystemClock.elapsedRealtime() - startTime
                AnalysisResult(
                    topKClassNames,
                    topKScores,
                    moduleForwardDuration,
                    analysisDuration
                )
            }
        } catch (e: Exception) {
            Log.e("TAG", "Error during image analysis", e)
            analyzeImageErrorState = true
            AnalysisResult(
                arrayOf("Error during image analysis " + e.message),
                floatArrayOf(0F),
                0,
                0
            )
            null
        }
    }
}

interface ImageAnalysis {
    val result: LiveData<AnalysisResult>
    suspend fun analyzeImage(
        bitmap: Bitmap,
        moduleFileAbsoluteFilePath: String
    )
}