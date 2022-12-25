package com.life4.imagetotext.feature.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
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
import com.life4.imagetotext.util.saveImageToInternal
import java.io.File


class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {
    private val viewModel: HomeViewModel by viewModels()
    private var isAllFabVisible = false


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
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToResultFragment(
                    it.result.text
                )
            )
        }
    }

    override fun onCreateView() {

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
                        "Please allow the permission",
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
                        "Please allow the permission",
                        Snackbar.ANIMATION_MODE_SLIDE
                    ).show()
            }
        }
    }


    private fun startCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
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
                    "Please allow the permission",
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
                    "Please allow the permission",
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
                val data: Intent? = result.data
                try {
                    if (data?.extras != null) {
                        val myData = data.extras?.get("data") as Bitmap
                        requireContext().filesDir.listFiles()
                            ?.firstOrNull { it.path.endsWith(".jpeg") }?.delete()
                        val path = saveImageToInternal(requireContext(), myData)
                        if (path != null) {
                            val file = File(path)
                            val uri = Uri.fromFile(file)
                            startCrop(uri)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
    }
}