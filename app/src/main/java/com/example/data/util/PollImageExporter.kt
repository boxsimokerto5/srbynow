package com.example.data.util

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.CitizenPoll
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PollImageExporter {

    private const val TAG = "PollImageExporter"

    private fun showToastOnMain(context: Context, message: String, length: Int = Toast.LENGTH_SHORT) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context.applicationContext, message, length).show()
        }
    }

    /**
     * Renders a high-resolution, professional visual infographic Bitmap for a CitizenPoll.
     */
    fun generatePollBitmap(context: Context, poll: CitizenPoll): Bitmap {
        val width = 1080
        val padding = 64f
        val contentWidth = (width - padding * 2).toInt()

        // 1. Text Paints
        val headerTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 42f
            isFakeBoldText = true
        }
        val headerSubPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(220, 255, 255, 255)
            textSize = 26f
        }
        val pollTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(24, 32, 28)
            textSize = 48f
            isFakeBoldText = true
        }
        val pollDescPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(70, 80, 75)
            textSize = 32f
        }
        val optionTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(20, 30, 25)
            textSize = 34f
            isFakeBoldText = true
        }
        val percentTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 107, 94) // Toska primary
            textSize = 36f
            isFakeBoldText = true
        }
        val voteCountPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 115, 110)
            textSize = 28f
        }
        val watermarkPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(120, 135, 130)
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }

        // 2. Measure heights using StaticLayout
        val titleLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(poll.title, 0, poll.title.length, pollTitlePaint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(4f, 1.15f)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(poll.title, pollTitlePaint, contentWidth, Layout.Alignment.ALIGN_NORMAL, 1.15f, 4f, false)
        }

        val descText = poll.description.ifBlank { "Aspirasi warga Surabaya untuk kemajuan kota tercinta." }
        val descLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(descText, 0, descText.length, pollDescPaint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(4f, 1.15f)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(descText, pollDescPaint, contentWidth, Layout.Alignment.ALIGN_NORMAL, 1.15f, 4f, false)
        }

        val headerHeight = 220f
        val infoBlockHeight = titleLayout.height + descLayout.height + 120f
        val optionItemHeight = 120f
        val optionsTotalHeight = (poll.options.size * optionItemHeight) + 40f
        val footerHeight = 160f

        val totalHeight = (headerHeight + infoBlockHeight + optionsTotalHeight + footerHeight).toInt()

        // 3. Create Bitmap & Canvas
        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply { color = Color.rgb(248, 251, 249) }
        canvas.drawRect(0f, 0f, width.toFloat(), totalHeight.toFloat(), bgPaint)

        // Header Banner with Gradient
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), headerHeight,
                Color.rgb(0, 107, 94), Color.rgb(15, 118, 110),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), headerHeight, headerPaint)

        // Header Content
        canvas.drawText("AREK SURABAYA", padding, 75f, headerTitlePaint)
        canvas.drawText("Suara & Aspirasi Warga Kota Pahlawan", padding, 120f, headerSubPaint)

        // Category & District Pill
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(60, 255, 255, 255)
        }
        val categoryPill = "${poll.category.replace('_', ' ')} • KEC. ${poll.district.uppercase()}"
        val pillWidth = headerSubPaint.measureText(categoryPill) + 32f
        val pillRect = RectF(padding, 142f, padding + pillWidth, 186f)
        canvas.drawRoundRect(pillRect, 22f, 22f, pillPaint)
        canvas.drawText(categoryPill, padding + 16f, 174f, headerSubPaint)

        var currentY = headerHeight + 50f

        // Draw Title
        canvas.save()
        canvas.translate(padding, currentY)
        titleLayout.draw(canvas)
        canvas.restore()
        currentY += titleLayout.height + 24f

        // Draw Description
        canvas.save()
        canvas.translate(padding, currentY)
        descLayout.draw(canvas)
        canvas.restore()
        currentY += descLayout.height + 36f

        // Total Votes Badge Card
        val statsCardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(235, 245, 240)
        }
        val statsBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(180, 215, 200)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val statsRect = RectF(padding, currentY, width - padding, currentY + 68f)
        canvas.drawRoundRect(statsRect, 20f, 20f, statsCardPaint)
        canvas.drawRoundRect(statsRect, 20f, 20f, statsBorderPaint)

        val statsText = "📊 Total Partisipasi: ${poll.totalVotes} Suara Warga"
        val statsTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 107, 94)
            textSize = 28f
            isFakeBoldText = true
        }
        canvas.drawText(statsText, padding + 24f, currentY + 45f, statsTextPaint)

        currentY += 100f

        // Draw Options & Progress Bars
        val barBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(225, 235, 230)
        }
        val barFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 107, 94)
        }

        for (opt in poll.options) {
            val percentage = poll.percentageForOption(opt.id)
            val percentStr = "%.1f%%".format(Locale.US, percentage)
            val votesStr = "(${opt.voteCount} suara)"

            // Option Title
            canvas.drawText(opt.text, padding, currentY + 30f, optionTextPaint)

            // Percentage & Vote count on right
            val percentWidth = percentTextPaint.measureText(percentStr)
            canvas.drawText(percentStr, width - padding - percentWidth, currentY + 30f, percentTextPaint)

            val voteCountWidth = voteCountPaint.measureText(votesStr)
            canvas.drawText(votesStr, width - padding - percentWidth - voteCountWidth - 16f, currentY + 30f, voteCountPaint)

            // Progress Bar
            val barY = currentY + 45f
            val barHeight = 24f
            val barRect = RectF(padding, barY, width - padding, barY + barHeight)
            canvas.drawRoundRect(barRect, 12f, 12f, barBgPaint)

            if (percentage > 0f) {
                val fillWidth = ((width - padding * 2) * (percentage / 100f)).coerceAtLeast(18f)
                val fillRect = RectF(padding, barY, padding + fillWidth, barY + barHeight)
                canvas.drawRoundRect(fillRect, 12f, 12f, barFillPaint)
            }

            currentY += optionItemHeight
        }

        // Draw Divider
        currentY += 10f
        val divPaint = Paint().apply {
            color = Color.rgb(220, 230, 225)
            strokeWidth = 2f
        }
        canvas.drawLine(padding, currentY, width - padding, currentY, divPaint)

        // Footer Watermark
        val dateStr = SimpleDateFormat("d MMMM yyyy, HH:mm 'WIB'", Locale("id", "ID")).format(Date())
        canvas.drawText("Hasil Polling Warga Surabaya • $dateStr", width / 2f, currentY + 45f, watermarkPaint)
        canvas.drawText("Aplikasi Media Sosial Warga Arek Surabaya • areksurabaya.id", width / 2f, currentY + 80f, watermarkPaint)

        return bitmap
    }

    /**
     * Exports the poll bitmap as a JPEG file in the application's shared cache directory.
     */
    fun exportPollToJpegFile(context: Context, poll: CitizenPoll): File {
        val pollsDir = File(context.cacheDir, "polls")
        if (!pollsDir.exists()) {
            pollsDir.mkdirs()
        }
        val file = File(pollsDir, "polling_arek_surabaya_${poll.id.takeLast(8)}.jpg")
        val bitmap = generatePollBitmap(context, poll)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        return file
    }

    /**
     * Shares the rendered poll infographic directly to other apps (WhatsApp, Story, Telegram, etc.) as a JPG.
     */
    fun sharePollAsImage(context: Context, poll: CitizenPoll) {
        try {
            val file = exportPollToJpegFile(context, poll)
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri("poll_infographic", uri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "📊 Hasil Polling Warga Surabaya: \"${poll.title}\"\nIkuti polling & suarakan aspirasimu di aplikasi Arek Surabaya!",
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Bagikan Hasil Polling (Gambar JPG)").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share poll image: ${e.message}", e)
            showToastOnMain(context, "Gagal membagikan gambar: ${e.localizedMessage ?: "Error"}")
        }
    }

    /**
     * Saves the rendered poll infographic to the public Gallery (Pictures/ArekSurabaya) or app folder.
     */
    fun savePollImageToGallery(context: Context, poll: CitizenPoll): Boolean {
        return try {
            val bitmap = generatePollBitmap(context, poll)
            val fileName = "Polling_Surabaya_${poll.id.takeLast(6)}_${System.currentTimeMillis()}.jpg"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/ArekSurabaya")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    resolver.openOutputStream(imageUri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                    showToastOnMain(context, "Gambar hasil polling tersimpan di Galeri Foto (Pictures/ArekSurabaya)", Toast.LENGTH_LONG)
                    return true
                }
            }

            // Fallback for older Android or direct file save
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDir = File(picturesDir, "ArekSurabaya").apply { if (!exists()) mkdirs() }
            val destFile = File(appDir, fileName)
            FileOutputStream(destFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            showToastOnMain(context, "Gambar tersimpan di Galeri Foto: ${destFile.name}", Toast.LENGTH_LONG)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save poll image to gallery: ${e.message}", e)
            showToastOnMain(context, "Gagal menyimpan gambar: ${e.localizedMessage ?: "Error"}")
            false
        }
    }

    /**
     * Shares standard poll invitation text to WhatsApp or social apps.
     */
    fun sharePollAsText(context: Context, poll: CitizenPoll) {
        val shareText = buildString {
            appendLine("📢 *POLLING WARGA SURABAYA*")
            appendLine("\"${poll.title}\"")
            if (poll.description.isNotBlank()) {
                appendLine(poll.description)
            }
            appendLine()
            appendLine("Pilihan opsi voting:")
            poll.options.forEachIndexed { idx, opt ->
                val pct = poll.percentageForOption(opt.id)
                appendLine("${idx + 1}. ${opt.text} (%.1f%% - ${opt.voteCount} suara)".format(Locale.US, pct))
            }
            appendLine()
            appendLine("Total: ${poll.totalVotes} warga telah bersuara.")
            appendLine("Ikuti polling warga selengkapnya di aplikasi *Arek Surabaya*!")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Bagikan Polling Warga").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
