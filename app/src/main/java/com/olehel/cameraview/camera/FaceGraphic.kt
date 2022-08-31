package com.olehel.cameraview.camera

import android.content.Context
import android.graphics.*
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.Landmark
import com.olehel.cameraview.R


/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic(overlay: GraphicOverlay, context: Context) : GraphicOverlay.Graphic(overlay) {
    private val marker: Bitmap
    var smilingProbability = -1f
        private set
    var eyeRightOpenProbability = -1f
        private set
    var eyeLeftOpenProbability = -1f
        private set
    private var leftEyePos: PointF? = null
    private var rightEyePos: PointF? = null
    private var noseBasePos: PointF? = null
    private var leftMouthCorner: PointF? = null
    private var rightMouthCorner: PointF? = null
    private var mouthBase: PointF? = null
    private var leftEar: PointF? = null
    private var rightEar: PointF? = null
    private var leftEarTip: PointF? = null
    private var rightEarTip: PointF? = null
    private var leftCheek: PointF? = null
    private var rightCheek: PointF? = null

    @Volatile
    private var mFace: Face? = null
    fun setId(id: Int) {}

    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    fun updateFace(face: Face?) {
        mFace = face
        postInvalidate()
    }

    fun goneFace() {
        mFace = null
        postInvalidate()
    }

    override fun draw(canvas: Canvas?) {
        val face: Face? = mFace
        if (face == null) {
            canvas?.drawColor(0, PorterDuff.Mode.CLEAR)
            smilingProbability = -1f
            eyeRightOpenProbability = -1f
            eyeLeftOpenProbability = -1f
            return
        }
        val facePosition =
            PointF(translateX(face.getPosition().x), translateY(face.getPosition().y))
        val faceWidth: Float = face.getWidth() * 4
        val faceHeight: Float = face.getHeight() * 4
        val faceCenter = PointF(
            translateX(face.getPosition().x + faceWidth / 8),
            translateY(face.getPosition().y + faceHeight / 8)
        )
        smilingProbability = face.getIsSmilingProbability()
        eyeRightOpenProbability = face.getIsRightEyeOpenProbability()
        eyeLeftOpenProbability = face.getIsLeftEyeOpenProbability()
        val eulerY: Float = face.getEulerY()
        val eulerZ: Float = face.getEulerZ()
        //DO NOT SET TO NULL THE NON EXISTENT LANDMARKS. USE OLDER ONES INSTEAD.
        for (landmark in face.getLandmarks()) {
            when (landmark.getType()) {
                Landmark.LEFT_EYE -> leftEyePos = PointF(
                    translateX(landmark.getPosition().x),
                    translateY(landmark.getPosition().y)
                )
                Landmark.RIGHT_EYE -> rightEyePos = PointF(
                    translateX(landmark.getPosition().x),
                    translateY(landmark.getPosition().y)
                )
                Landmark.NOSE_BASE -> noseBasePos = PointF(
                    translateX(landmark.getPosition().x),
                    translateY(landmark.getPosition().y)
                )
                Landmark.LEFT_MOUTH -> leftMouthCorner = PointF(
                    translateX(landmark.getPosition().x),
                    translateY(landmark.getPosition().y)
                )
                Landmark.RIGHT_MOUTH -> rightMouthCorner = PointF(
                    translateX(landmark.getPosition().x),
                    translateY(landmark.getPosition().y)
                )
                Landmark.BOTTOM_MOUTH -> mouthBase = PointF(
                    translateX(landmark.getPosition().x),
                    translateY(landmark.getPosition().y)
                )
                Landmark.LEFT_EAR -> leftEar = PointF(
                    translateX(landmark.getPosition().x),
                    translateY(landmark.getPosition().y)
                )
                Landmark.RIGHT_EAR -> rightEar = PointF(
                    translateX(landmark.getPosition().x),
                    translateY(landmark.getPosition().y)
                )
                Landmark.LEFT_EAR_TIP -> leftEarTip = PointF(
                    translateX(landmark.getPosition().x),
                    translateY(landmark.getPosition().y)
                )
                Landmark.RIGHT_EAR_TIP -> rightEarTip = PointF(
                    translateX(landmark.getPosition().x),
                    translateY(landmark.getPosition().y)
                )
                Landmark.LEFT_CHEEK -> leftCheek = PointF(
                    translateX(landmark.getPosition().x),
                    translateY(landmark.getPosition().y)
                )
                Landmark.RIGHT_CHEEK -> rightCheek = PointF(
                    translateX(landmark.getPosition().x),
                    translateY(landmark.getPosition().y)
                )
            }
        }
        val mPaint = Paint()
        mPaint.color = Color.WHITE
        mPaint.strokeWidth = 4f
        if (faceCenter != null) canvas?.drawBitmap(marker, faceCenter!!.x, faceCenter.y, null)
        if (noseBasePos != null) canvas?.drawBitmap(marker, noseBasePos!!.x, noseBasePos!!.y, null)
        if (leftEyePos != null) canvas?.drawBitmap(marker, leftEyePos!!.x, leftEyePos!!.y, null)
        if (rightEyePos != null) canvas?.drawBitmap(marker, rightEyePos!!.x, rightEyePos!!.y, null)
        if (mouthBase != null) canvas?.drawBitmap(marker, mouthBase!!.x, mouthBase!!.y, null)
        if (leftMouthCorner != null) canvas?.drawBitmap(
            marker,
            leftMouthCorner!!.x,
            leftMouthCorner!!.y,
            null
        )
        if (rightMouthCorner != null) canvas?.drawBitmap(
            marker,
            rightMouthCorner!!.x,
            rightMouthCorner!!.y,
            null
        )
        if (leftEar != null) canvas?.drawBitmap(marker, leftEar!!.x, leftEar!!.y, null)
        if (rightEar != null) canvas?.drawBitmap(marker, rightEar!!.x, rightEar!!.y, null)
        if (leftEarTip != null) canvas?.drawBitmap(marker, leftEarTip!!.x, leftEarTip!!.y, null)
        if (rightEarTip != null) canvas?.drawBitmap(marker, rightEarTip!!.x, rightEarTip!!.y, null)
        if (leftCheek != null) canvas?.drawBitmap(marker, leftCheek!!.x, leftCheek!!.y, null)
        if (rightCheek != null) canvas?.drawBitmap(marker, rightCheek!!.x, rightCheek!!.y, null)
    }

    init {
        val opt: BitmapFactory.Options = BitmapFactory.Options()
        opt.inScaled = false
        val resources = context.resources
        marker = BitmapFactory.decodeResource(resources, R.drawable.marker, opt)
    }
}