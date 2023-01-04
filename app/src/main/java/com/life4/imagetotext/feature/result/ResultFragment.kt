package com.life4.imagetotext.feature.result

import android.content.*
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.life4.imagetotext.R
import com.life4.imagetotext.base.BaseFragment
import com.life4.imagetotext.databinding.BottomSheetResultBinding
import com.life4.imagetotext.databinding.FragmentResultBinding
import com.life4.imagetotext.model.ResultModel
import com.life4.imagetotext.util.createPdf
import com.life4.imagetotext.util.createTxtFile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class ResultFragment : BaseFragment<FragmentResultBinding>(R.layout.fragment_result) {
    private val viewModel: ResultViewModel by viewModels()
    private val args: ResultFragmentArgs by navArgs()
    private var job: Job? = null

    override fun onCreateView() {
        binding.progressBar.isVisible = false
        binding.etResult.setText(args.textResult)
        viewModel.lastInsertedResultID = if (args.id != 0L) args.id else null
        if (args.id == 0L) {
            viewModel.insertResultToRoom(
                ResultModel(
                    content = binding.etResult.text.toString(),
                    date = dateToString()
                )
            )
        }

        binding.etResult.addTextChangedListener {
            job?.cancel()
            job = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                binding.progressBar.isVisible = true
                delay(1200)
                binding.progressBar.isVisible = false
                viewModel.updateResultToRoom(
                    ResultModel(
                        content = binding.etResult.text.toString(),
                        date = dateToString()
                    )
                )
            }
        }

        binding.fabShare.setOnClickListener {
            BottomSheetDialog(requireContext()).apply {
                val bindingDialog = DataBindingUtil.inflate<BottomSheetResultBinding>(
                    LayoutInflater.from(requireContext()), R.layout.bottom_sheet_result, null, false
                )
                bindingDialog.shareLayout.setOnClickListener {
                    share()
                    dismiss()
                }
                bindingDialog.shareTxtLayout.setOnClickListener {
                    sendEmail()
                    dismiss()
                }
                bindingDialog.sharePdfLayout.setOnClickListener {
                    openDocument()
                    dismiss()
                }
                bindingDialog.sharePdfLayoutAll.setOnClickListener {
                    sharePdf()
                    dismiss()
                }
                setContentView(bindingDialog.root)
            }.show()
        }

        binding.back.setOnClickListener { findNavController().popBackStack() }

        binding.copyAll.setOnClickListener {
            copyAllTextToClipboard()
        }
    }

    private fun openDocument() {
        requireContext().filesDir.listFiles()?.firstOrNull { it.path.contains("pdf") }?.delete()
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
        val intent = Intent.createChooser(sendIntent, getString(R.string.choose))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                getString(R.string.nothing_pdf_viewer),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun copyAllTextToClipboard() {
        val clipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", binding.etResult.text.toString())
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(requireContext(), getString(R.string.copied_text), Toast.LENGTH_SHORT).show()
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
            emailIntent.type = "text/plain"
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri)
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Image To Text")
            startActivity(Intent.createChooser(emailIntent, ""))
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

    private fun sharePdf() {
        requireContext().filesDir.listFiles()?.firstOrNull { it.path.contains("pdf") }?.delete()
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

        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.type = "application/pdf"
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri)
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Image To Text")
        val intent = Intent.createChooser(sendIntent, getString(R.string.choose))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                getString(R.string.nothing_pdf_viewer),
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun dateToString(): String {
        val date = Date()
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return format.format(date)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job = null
    }
}