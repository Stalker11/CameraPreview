package com.olehel.cameraview.ui

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.olehel.cameraview.R
import com.olehel.cameraview.camera.Camera2Source
import com.olehel.cameraview.camera.FaceGraphic
import com.olehel.cameraview.camera.GraphicOverlay
import com.olehel.cameraview.databinding.FragmentCameraBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CameraFragment : Fragment(R.layout.fragment_camera) {
    private var binding: FragmentCameraBinding? = null
    private var mCamera2Source: Camera2Source? = null
    private val usingFrontCamera = true

    // COMMON TO BOTH CAMERAS
    private var previewFaceDetector: FaceDetector? = null
    private var mFaceGraphic: FaceGraphic? = null
    val REQUEST_CAMERA_PERMISSION = 101
    val REQUEST_STORAGE_PERMISSION = 102

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        createCameraSourceFront()
        binding?.takeNewPicture?.setOnClickListener {
            mCamera2Source?.takePicture(null, camera2SourcePictureCallback)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        previewFaceDetector?.release()
    }

    private fun createCameraSourceFront() {
        previewFaceDetector = FaceDetector.Builder(requireContext())
            .setClassificationType(com.google.android.gms.vision.face.FaceDetector.ALL_CLASSIFICATIONS)
            .setLandmarkType(com.google.android.gms.vision.face.FaceDetector.ALL_LANDMARKS)
            .setMode(com.google.android.gms.vision.face.FaceDetector.FAST_MODE)
            .setProminentFaceOnly(true)
            .setTrackingEnabled(true)
            .build()
        if (previewFaceDetector?.isOperational == true) {
            previewFaceDetector?.setProcessor(
                MultiProcessor.Builder<Face>(GraphicFaceTrackerFactory())
                    .build()
            )
        } else {
            Toast.makeText(context, "FACE DETECTION NOT AVAILABLE", Toast.LENGTH_SHORT).show()
            //binding.status.setText("face detector not available")
        }
        mCamera2Source = Camera2Source.Builder(context, previewFaceDetector)
            .setFocusMode(Camera2Source.CAMERA_AF_CONTINUOUS_PICTURE)
            .setFlashMode(Camera2Source.CAMERA_FLASH_AUTO)
            .setFacing(Camera2Source.CAMERA_FACING_FRONT)
            .build()

        //IF CAMERA2 HARDWARE LEVEL IS LEGACY, CAMERA2 IS NOT NATIVE.
        //WE WILL USE CAMERA1.
        if (mCamera2Source?.isCamera2Native == true) {
            startCameraSource()
        } else {
            //  if (usingFrontCamera) createCameraSourceFront()// else createCameraSourceBack()
        }
    }

    override fun onResume() {
        super.onResume()
        requestPermissionThenOpenCamera()
    }

    fun startCameraSource() {
        if (mCamera2Source != null) {
            binding!!.mPreview.start(
                mCamera2Source!!,
                binding!!.mGraphicOverlay,
                camera2SourceErrorCallback
            )
        }
    }

    val camera2SourceErrorCallback: Camera2Source.CameraError = object : Camera2Source.CameraError {
        override fun onCameraOpened() {
        }

        override fun onCameraDisconnected() {}
        override fun onCameraError(errorCode: Int) {
            requireActivity().runOnUiThread(Runnable {
                val builder =
                    AlertDialog.Builder(requireActivity())
                builder.setCancelable(false)
                builder.setTitle(getString(R.string.cameraError))
                builder.setMessage(
                    String.format(
                        getString(R.string.errorCode) + " ",
                        errorCode
                    )
                )
                builder.setPositiveButton(
                    getString(R.string.ok)
                ) { dialog: DialogInterface?, which: Int ->

                }
                val alertDialog = builder.create()
                alertDialog.show()
            })
        }
    }

    inner class GraphicFaceTrackerFactory : MultiProcessor.Factory<Face> {
        override fun create(face: Face): Tracker<Face> {
            return GraphicFaceTracker(binding!!.mGraphicOverlay)
        }
    }

    inner class GraphicFaceTracker internal constructor(overlay: GraphicOverlay) :
        Tracker<Face>() {
        private val mOverlay: GraphicOverlay

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        override fun onNewItem(faceId: Int, item: Face) {
            mFaceGraphic?.setId(faceId)
             Log.d("MainActivity", "NEW FACE ID: $faceId")
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        override fun onUpdate(detectionResults: Detections<Face>, face: Face) {
            mOverlay.add(mFaceGraphic!!)
            mFaceGraphic!!.updateFace(face)
            setButtonClickable(true)
             Log.d("MainActivity", "NEW KNOWN FACE UPDATE: " + face.id)
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        override fun onMissing(detectionResults: Detections<Face>) {
            mFaceGraphic?.goneFace()
            mOverlay.remove(mFaceGraphic!!)
            setButtonClickable(false)
            Log.d("MainActivity", "FACE MISSING")
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        override fun onDone() {
            mFaceGraphic?.goneFace()
            setButtonClickable(false)
            mOverlay.remove(mFaceGraphic!!)
            mOverlay.clear()
             Log.d("MainActivity", "FACE GONE")
        }

        init {
            mOverlay = overlay
            mFaceGraphic = FaceGraphic(overlay, requireContext())
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestPermissionThenOpenCamera()
            } else {
                Toast.makeText(requireContext(), "CAMERA PERMISSION REQUIRED", Toast.LENGTH_LONG)
                    .show()
                requireActivity().finish()
            }
        }
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestPermissionThenOpenCamera()
            } else {
                requireActivity().finish()
                Toast.makeText(requireContext(), "STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun requestPermissionThenOpenCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                createCameraSourceFront()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_STORAGE_PERMISSION
                )
            }
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }
    val camera2SourcePictureCallback: Camera2Source.PictureCallback = object : Camera2Source.PictureCallback {
        override fun onPictureTaken(image: Bitmap) {
            Log.d("MainActivity", "onPictureTaken: ")
            var out: FileOutputStream? = null
            try {
                val file = File(
                    Environment.getExternalStorageDirectory(),
                    "/camera2_picture.png"
                )
                out = FileOutputStream(
                    file
                )
                image.compress(Bitmap.CompressFormat.JPEG, 95, out)
                val photoURI = FileProvider.getUriForFile(requireContext(), "${activity?.applicationContext?.packageName}.fileprovider", file)
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    .apply { putExtra(MediaStore.EXTRA_OUTPUT, photoURI) }
                (requireActivity() as MainActivity).takePicture.launch(photoURI)
                findNavController()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    out?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
    private fun setButtonClickable(state: Boolean){
        binding?.takeNewPicture?.isEnabled = state
        binding?.takeNewPicture?.isClickable = state
    }
}
