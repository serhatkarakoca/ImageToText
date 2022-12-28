package com.life4.imagetotext.feature.home

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.life4.imagetotext.R
import com.life4.imagetotext.base.BaseFragment
import com.life4.imagetotext.databinding.FragmentHomeBinding
import com.life4.imagetotext.model.ResultModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {
    private val viewModel: HomeViewModel by viewModels()
    private val historyAdapter: HistoryAdapter by lazy {
        HistoryAdapter(
            ::historyClickListener,
            ::deleteHistoryItem
        )
    }
    private var isAllFabVisible = false
    private var pictureImagePath: String? = null

    private fun startCrop(imageUri: Uri) {
        cropImage.launch(
            CropImageContractOptions(
                uri = imageUri,
                CropImageOptions(
                    guidelines = CropImageView.Guidelines.ON,
                    outputCompressFormat = Bitmap.CompressFormat.JPEG,
                    imageSourceIncludeCamera = true,
                    outputCompressQuality = 100
                )
            )
        )
    }

    private fun textRecognizerOCR(imageUri: Uri) {
        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromFilePath(requireContext(), imageUri)
        textRecognizer.process(image).addOnCompleteListener {
            viewModel.setTextResult(it.result.text)
            clearCacheFiles()
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToResultFragment(
                    it.result.text
                )
            )
        }
    }

    private fun historyClickListener(item: ResultModel) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToResultFragment(
                item.content,
                item.id
            )
        )
    }

    private fun deleteHistoryItem(itemID: Long) {
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.warning))
            .setMessage(getString(R.string.want_to_remove))
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                viewModel.deleteResult(itemID)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onCreateView() {
        binding.rvHistory.adapter = historyAdapter
        getHistory()
        binding.addFab.shrink()
        binding.addFab.setOnClickListener {
            isAllFabVisible = if (!isAllFabVisible) {
                binding.cameraFab.show()
                binding.galleryFab.show()
                binding.addAlarmActionText.isVisible = true
                binding.addPersonActionText.isVisible = true
                binding.addFab.extend()
                true
            } else {
                binding.galleryFab.hide()
                binding.cameraFab.hide()
                binding.addAlarmActionText.isVisible = false
                binding.addPersonActionText.isVisible = false
                binding.addFab.shrink()
                false
            }
        }

        binding.galleryFab.setOnClickListener {
            requestGalleryPermission {
                if (it)
                    openGallery()
                else
                    Snackbar.make(
                        requireView(),
                        getString(R.string.allow_permission),
                        Snackbar.ANIMATION_MODE_SLIDE
                    ).show()
            }
        }

        binding.cameraFab.setOnClickListener {
            requestCameraPermission {
                if (it)
                    startCamera()
                else
                    Snackbar.make(
                        requireView(),
                        getString(R.string.allow_permission),
                        Snackbar.ANIMATION_MODE_SLIDE
                    ).show()
            }
        }
    }

    private fun getHistory() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.getAllResults().collectLatest {
                binding.isEmpty = it.isEmpty()
                historyAdapter.submitList(it.reversed())
            }
        }
    }

    private fun clearCacheFiles() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            withContext(Dispatchers.IO) {
                requireContext().filesDir.listFiles()
                    ?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }
                    ?.forEach { it.delete() }
            }
        }
    }

    private fun startCamera() {
        val imageFileName = System.currentTimeMillis().toString() + ".jpg"
        val storageDir = requireContext().filesDir
        pictureImagePath = storageDir.absolutePath + "/" + imageFileName
        val file = File(pictureImagePath ?: "")
        val outputFileUri = file.let {
            FileProvider.getUriForFile(
                requireContext(),
                requireContext().applicationContext.packageName + ".provider",
                it
            )
        }
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
        resultCameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private fun requestCameraPermission(onResult: (Boolean) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
            onResult(true)
        else {
            cameraRequest.launch(Manifest.permission.CAMERA)
        }
    }

    private fun requestGalleryPermission(onResult: (Boolean) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        )
            onResult(true)
        else {
            galleryRequest.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private val cameraRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted)
                startCamera()
            else
                Snackbar.make(
                    requireView(),
                    getString(R.string.allow_permission),
                    Snackbar.ANIMATION_MODE_SLIDE
                ).show()
        }

    private val galleryRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted)
                openGallery()
            else
                Snackbar.make(
                    requireView(),
                    getString(R.string.allow_permission),
                    Snackbar.ANIMATION_MODE_SLIDE
                ).show()
        }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                try {
                    if (data?.data != null) {
                        val myData = data.data
                        myData?.let {
                            startCrop(it)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    private var resultCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val imgFile = pictureImagePath?.let { File(it) }
                    val uri = Uri.fromFile(imgFile)
                    startCrop(uri)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                pictureImagePath = null
            }
        }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let {
                textRecognizerOCR(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isAllFabVisible = false
        pictureImagePath = null
    }
}