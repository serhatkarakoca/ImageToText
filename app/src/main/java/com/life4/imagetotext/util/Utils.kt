package com.life4.imagetotext.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


fun saveFile(inputStream: InputStream, fileName: String, context: Context): String {
    lateinit var newDirection: File

    val direction = File(context.filesDir.toString()) // default folder /files

    newDirection = File(direction, "/outputs") // if you want to create new folder
    if (!newDirection.exists())
        newDirection.createNewFile()


    val file = File(newDirection, fileName)
    context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
        it.write(inputStream.readBytes())
        it.close()
    }
    return file.absolutePath
}

fun createTxtFile(context: Context, text: String): String? {
    lateinit var newDirection: File
    val fileName = "output.txt"
    val direction = File(context.filesDir.toString()) // default folder /files

    newDirection = File(direction, "/outputs") // if you want to create new folder
    if (!newDirection.exists())
        newDirection.createNewFile()


    val file = File(newDirection, fileName)
    context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
        it.write(text.toByteArray())
        it.close()
    }
    return file.absolutePath
}

fun saveImageToInternal(context: Context, bitmap: Bitmap): String? {
    val fol = File(context.getExternalFilesDir(null)?.absolutePath.toString())
    if (!fol.exists())
        fol.mkdir()
    val folder = File(fol, "images")
    if (!folder.exists())
        folder.mkdir()

    folder.listFiles()?.firstOrNull { it.absolutePath.endsWith(".jpg") }?.delete()

    var fOut: FileOutputStream? = null
    try {
        val file = File(folder, "${System.currentTimeMillis()}.jpg")
        fOut = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
        return file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        try {
            fOut?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return null
}

fun createPdf(context: Context, text: String) {
    val texts: ArrayList<String> = ArrayList()
    var totCharCount = 0
    for (i in text.indices) {
        totCharCount++
    }
    val perPageWords = 4900
    var pages = totCharCount / perPageWords
    val remainderPagesExtra = totCharCount % perPageWords
    if (remainderPagesExtra > 0) {
        pages++
    }
    var k = pages
    var count = 0
    while (k != 0) {
        val eachPageText = StringBuilder()
        for (y in 0 until perPageWords - 1) {
            if (count < totCharCount) {
                eachPageText.append(text[count])
                if (y == perPageWords - 1 && text[count] != ' ') {
                    while (text[count] != '\n') {
                        count++
                        eachPageText.append(text[count])
                    }
                } else {
                    count++
                }
            }
        }
        texts.add(eachPageText.toString())
        k--
    }

    val pdfDocument = PdfDocument()
    var pageNumber = 0
    try {
        pageNumber++
        for (eachPageText in texts) {
            val mypageInfo = PageInfo.Builder(595, 842, pageNumber).create()
            val myPage = pdfDocument.startPage(mypageInfo)
            val canvas = myPage.canvas
            val mTextPaint = TextPaint()
            mTextPaint.textSize = 11f

            /*
            val mTextLayout = StaticLayout(
                eachPageText,
                mTextPaint,
                canvas.width - 60,
                Layout.Alignment.ALIGN_NORMAL,
                1.0f,
                0.0f,
                false
            )
             */
            val mTextLayout =
                StaticLayout.Builder.obtain(
                    eachPageText,
                    0,
                    eachPageText.length,
                    mTextPaint,
                    canvas.width - 60
                )
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setIncludePad(false)
                    .setLineSpacing(0f, 1f)
                    .build()

            canvas.save()
            val textX = 30
            val textY = 30
            canvas.translate(textX.toFloat(), textY.toFloat())
            mTextLayout.draw(canvas)
            canvas.restore()
            pdfDocument.finishPage(myPage)
        }
        val file = File(context.filesDir, "myFile.pdf")
        if (!file.exists())
            file.createNewFile()
        val fOut = FileOutputStream(file)
        pdfDocument.writeTo(fOut)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    pdfDocument.close()
}

fun getMimeTypeExtension(uri: Uri, context: Context): String? {
    val cR: ContentResolver = context.contentResolver
    val mime = MimeTypeMap.getSingleton()
    return mime.getExtensionFromMimeType(cR.getType(uri))
}

fun getMimeType(uri: Uri, context: Context): String? {
    val cR: ContentResolver = context.contentResolver
    return cR.getType(uri);
}