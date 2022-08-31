package com.olehel.cameraview.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.images.Size
import com.google.android.gms.vision.CameraSource
import com.olehel.cameraview.utils.Utils
import java.io.IOException

class CameraSourcePreview : ViewGroup {
    //PREVIEW VISUALIZERS FOR BOTH CAMERA1 AND CAMERA2 API.
    private val mSurfaceView: SurfaceView
    private val mAutoFitTextureView: AutoFitTextureView
    private var mStartRequested: Boolean
    private var mSurfaceAvailable: Boolean
    private var viewAdded = false

    private var mCamera2Source: Camera2Source? = null
    private var mCamera2SourceErrorHandler: Camera2Source.CameraError? = null
    private var mOverlay: GraphicOverlay? = null
    private val screenWidth: Int
    private val screenHeight: Int
    private val screenRotation: Int

    constructor(context: Context?) : super(context) {
        screenHeight = Utils.getScreenHeight(context)
        screenWidth = Utils.getScreenWidth(context)
        screenRotation = Utils.getScreenRotation(context)
        mStartRequested = false
        mSurfaceAvailable = false
        mSurfaceView = SurfaceView(context)
        mSurfaceView.getHolder().addCallback(mSurfaceViewListener)
        mAutoFitTextureView = AutoFitTextureView(context)
        mAutoFitTextureView.setSurfaceTextureListener(mSurfaceTextureListener)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        screenHeight = Utils.getScreenHeight(context)
        screenWidth = Utils.getScreenWidth(context)
        screenRotation = Utils.getScreenRotation(context)
        mStartRequested = false
        mSurfaceAvailable = false
        mSurfaceView = SurfaceView(context)
        mSurfaceView.getHolder().addCallback(mSurfaceViewListener)
        mAutoFitTextureView = AutoFitTextureView(context)
        mAutoFitTextureView.setSurfaceTextureListener(mSurfaceTextureListener)
    }

    fun start(cameraSource: CameraSource, overlay: GraphicOverlay) {
        mOverlay = overlay
        start(cameraSource)
    }

    fun start(camera2Source: Camera2Source, overlay: GraphicOverlay, errorHandler: Camera2Source.CameraError) {
        mOverlay = overlay
        start(camera2Source, errorHandler)
    }

    private fun start(cameraSource: CameraSource) {
        mStartRequested = true
        if (!viewAdded) {
            addView(mSurfaceView)
            viewAdded = true
        }
        try {
            startIfReady()
        } catch (e: IOException) {
            Log.e(TAG, "Could not start camera source.", e)
        }
    }

    private fun start(camera2Source: Camera2Source, errorHandler: Camera2Source.CameraError) {
        mCamera2Source = camera2Source
        mCamera2SourceErrorHandler = errorHandler
        mStartRequested = true
        if (!viewAdded) {
            addView(mAutoFitTextureView)
            viewAdded = true
        }
        try {
            startIfReady()
        } catch (e: IOException) {
            Log.e(TAG, "Could not start camera source.", e)
        }
    }

    fun stop() {
        mStartRequested = false
            if (mCamera2Source != null) {
                    mCamera2Source!!.stop()
            }
    }

    @Throws(IOException::class)
    private fun startIfReady() {
        if (mStartRequested && mSurfaceAvailable) {
            try {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                mCamera2Source!!.start(
                            mAutoFitTextureView,
                            screenRotation,
                            mCamera2SourceErrorHandler
                        )
                        if (mOverlay != null) {
                            val size = mCamera2Source!!.previewSize
                            if (size != null) {
                                val min = Math.min(size.width, size.height)
                                val max = Math.max(size.width, size.height)
                                // FOR GRAPHIC OVERLAY, THE PREVIEW SIZE WAS REDUCED TO QUARTER
                                // IN ORDER TO PREVENT CPU OVERLOAD
                                mOverlay!!.setCameraInfo(
                                    min / 4,
                                    max / 4,
                                    mCamera2Source!!.cameraFacing
                                )
                                mOverlay!!.clear()
                            } else {
                                stop()
                            }
                        }
                        mStartRequested = false
                }
             catch (e: SecurityException) {
                Log.d(TAG, "SECURITY EXCEPTION: $e")
            }
        }
    }

    private val mSurfaceViewListener: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(surface: SurfaceHolder) {
            mSurfaceAvailable = true
            mOverlay?.bringToFront()
            try {
                startIfReady()
            } catch (e: IOException) {
                Log.e(TAG, "Could not start camera source.", e)
            }
        }

        override fun surfaceDestroyed(surface: SurfaceHolder) {
            mSurfaceAvailable = false
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    }
    private val mSurfaceTextureListener: TextureView.SurfaceTextureListener = object :
        TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            mSurfaceAvailable = true
            mOverlay?.bringToFront()
            try {
                startIfReady()
            } catch (e: IOException) {
                Log.e(TAG, "Could not start camera source.", e)
            }
        }

        override fun onSurfaceTextureSizeChanged(
            texture: SurfaceTexture,
            width: Int,
            height: Int
        ) {
        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
            mSurfaceAvailable = false
            return true
        }

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {}
    }

    protected override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        var height = 720
            if (mCamera2Source != null) {
                var size: Size? = null
                    size = mCamera2Source!!.previewSize
                if (size != null) {
                    // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
                    height = size.width
                }
        }

        //RESIZE PREVIEW IGNORING ASPECT RATIO. THIS IS ESSENTIAL.
        val newWidth = height * screenWidth / screenHeight
        val layoutWidth = right - left
        val layoutHeight = bottom - top
        // Computes height and width for potentially doing fit width.
        var childWidth = layoutWidth
        var childHeight = (layoutWidth.toFloat() / newWidth.toFloat() * height).toInt()
        // If height is too tall using fit width, does fit height instead.
        if (childHeight > layoutHeight) {
            childHeight = layoutHeight
            childWidth = (layoutHeight.toFloat() / height.toFloat() * newWidth).toInt()
        }
        for (i in 0 until getChildCount()) {
            getChildAt(i).layout(0, 0, childWidth, childHeight)
        }
        try {
            startIfReady()
        } catch (e: IOException) {
            Log.e(TAG, "Could not start camera source.", e)
        }
    }

    companion object {
        private const val TAG = "CameraSourcePreview"
    }
}