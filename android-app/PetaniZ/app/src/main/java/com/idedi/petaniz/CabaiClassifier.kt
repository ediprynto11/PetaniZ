package com.idedi.petaniz

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer


class CabaiClassifier(context: Context) {

    private val interpreter: Interpreter
    private val labels: List<String>

    init {
        interpreter = Interpreter(loadModelFile(context))
//        val options = Interpreter.Options()
//        options.addDelegate(FlexDelegate())
//
//        interpreter = Interpreter(loadModelFile(context), options)
        labels = context.assets.open("labels.txt").bufferedReader().readLines()
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("cabai_mobilenetv2_85.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    fun classify(bitmap: Bitmap): Pair<String, Float> {

        // 1. ImageProcessor (resize + normalize + cast ke FLOAT32)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .add(CastOp(DataType.FLOAT32))
            .add(NormalizeOp(0f, 255f)) // pixel / 255
            .build()

        // 2. Convert bitmap → TensorImage FLOAT32
        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // 3. Output buffer
        val outputBuffer = TensorBuffer.createFixedSize(
            intArrayOf(1, labels.size),
            DataType.FLOAT32
        )

        // 4. Run inference
        interpreter.run(tensorImage.buffer, outputBuffer.buffer)

        // 5. Ambil hasil
        val scores = outputBuffer.floatArray
        val maxIndex = scores.indices.maxBy { scores[it] } ?: 0

        return labels[maxIndex] to scores[maxIndex]
    }

}
