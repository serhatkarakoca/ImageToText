package com.life4.imagetotext.feature.result

import android.content.*
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.navigation.fragment.navArgs
import com.life4.imagetotext.R
import com.life4.imagetotext.base.BaseFragment
import com.life4.imagetotext.databinding.FragmentResultBinding
import com.life4.imagetotext.util.createPdf
import com.life4.imagetotext.util.createTxtFile


class ResultFragment : BaseFragment<FragmentResultBinding>(R.layout.fragment_result) {
    private val args: ResultFragmentArgs by navArgs()

    override fun onCreateView() {
        binding.etResult.setText(args.textResult)
        binding.fabShare.setOnClickListener {

        }
    }

    private fun openDocument() {
        requireContext().filesDir.listFiles()?.firstOrNull { it.path.contains("pdf") }?.delete()
            ?.also {
                createPdf(requireContext(), binding.etResult.text.toString())
                val file = requireContext().filesDir.listFiles()?.let {
                    requireContext().filesDir.listFiles()?.firstOrNull {
                        it.absolutePath.contains("pdf")
                    }
                }

                val uri = file?.let {
                    FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().applicationContext.packageName + ".provider",
                        it
                    )
                }

                val sendIntent = Intent(Intent.ACTION_VIEW)
                sendIntent.setDataAndType(uri, "application/pdf")
                sendIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val intent = Intent.createChooser(sendIntent, "Seçim yapınız.")
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        requireContext(),
                        "PDF görüntüleyici bir uygulamanız bulunamadı.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun copyAllTextToClipboard() {
        val clipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", args.textResult)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(requireContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun sendEmail() {
        val path = createTxtFile(requireContext(), args.textResult.toString())
        if (path != null) {
            val file = requireContext().filesDir.listFiles()
                ?.firstOrNull { it.path.contains("output.txt") }
            val uri = file?.let {
                FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().applicationContext.packageName + ".provider",
                    it
                )
            }
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.type = "application/octet-stream"
            val to = arrayOf("")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to)
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri)
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Transaction")
            startActivity(Intent.createChooser(emailIntent, "Send email..."))
        }
    }

    private fun share() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            binding.etResult.text.toString()
        )
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }
}