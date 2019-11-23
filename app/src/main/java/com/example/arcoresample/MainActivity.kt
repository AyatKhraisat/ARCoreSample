package com.example.arcoresample

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.PixelCopy
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.ar.core.AugmentedFace
import com.google.ar.core.HitResult
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.rendering.Light.Type.FOCUSED_SPOTLIGHT
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.AugmentedFaceNode
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private lateinit var faceArFragment: FaceArFragment

    private lateinit var modelLoader: ModelLoader
    private lateinit var animator: ModelAnimator
    private lateinit var marioRenderable: ModelRenderable
    private lateinit var marioHatRenderable: ModelRenderable
    private var anchorNode: AnchorNode? = null
    private lateinit var spotLightYellow: Light

    private var currentCamera = FRONT_CAMERA


    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val MARIO_MODLE = 1001
        private val MARIO_HAT_MODLE = 1002
        private val FRONT_CAMERA = 12
        private val REAR_CAMERA = 13

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment
        setViewsListener()
        loadModels()
        customizeLight()
        customizeTexture()


    }

    private fun setViewsListener() {
        fab_greeting.setOnClickListener({ animate(GREETING) })
        fab_excited.setOnClickListener({ animate(EXCITED) })
        fab_dancing.setOnClickListener({ animate(DANCE) })
        fab_sad.setOnClickListener({ animate(SAD) })
        fab_surprised.setOnClickListener({ animate(SURPRISED) })
        ib_switch_camera.setOnClickListener { switchCamera() }
        fab_capture.run {
            setOnClickListener({ takePhoto(this@MainActivity,arFragment.arSceneView) })
            //  setOnLongClickListener({})
        }
    }

    private fun loadModels() {
        modelLoader = ModelLoader(this)

        modelLoader.loadModel(MARIO_MODLE, R.raw.mario,
            { id, modelRenderable ->
                marioRenderable = modelRenderable!!
                val data = marioRenderable.getAnimationData(SURPRISED)
                animator = ModelAnimator(data, marioRenderable)
                animator.start()
                null
            },
            { id, throwable ->
                Toast.makeText(
                    this, "Can't load Mario",
                    Toast.LENGTH_LONG
                ).show()
                null
            })

        modelLoader.loadModel(
            MARIO_HAT_MODLE, R.raw.mario_hat,
            { id, modelRenderable ->
                marioHatRenderable = modelRenderable!!
                marioHatRenderable.isShadowReceiver = false
                marioHatRenderable.isShadowCaster = false
                null

            },
            { id, throwable ->
                Toast.makeText(
                    this, "Can't load Mario Hat",
                    Toast.LENGTH_LONG
                ).show()
                null
            })
    }

    private fun initArScene() {
        arFragment.setOnTapArPlaneListener({ hitResult,
                                             unusedPlane,
                                             unusedMotionEvent ->
            this.onPlaneTap(
                hitResult
            )
        })
    }

    private fun switchCamera() {
        intent = Intent(this, FaceArActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0);
    }


    private fun changeFragment(arFragment: ArFragment) {
        supportFragmentManager.beginTransaction().replace(
            R.id.container,
            arFragment
        ).commit()
    }

    /*
   * Used as a handler for onClick, so the signature must match onClickListener.
   */
    private fun toggleRecording(unusedView: View) {
//        if (!arFragment.hasWritePermission()) {
//            Log.e(
//                com.google.ar.sceneform.samples.videorecording.VideoRecordingActivity.TAG,
//                "Video recording requires the WRITE_EXTERNAL_STORAGE permission"
//            )
//            Toast.makeText(
//                this,
//                "Video recording requires the WRITE_EXTERNAL_STORAGE permission",
//                Toast.LENGTH_LONG
//            )
//                .show()
//            arFragment.launchPermissionSettings()
//            return
//        }
//        val recording: Boolean = videoRecorder.onToggleRecord()
//        if (recording) {
//            recordButton.setImageResource(R.drawable.round_stop)
//        } else {
//            recordButton.setImageResource(R.drawable.round_videocam)
//            val videoPath: String = videoRecorder.getVideoPath().getAbsolutePath()
//            Toast.makeText(this, "Video saved: $videoPath", Toast.LENGTH_SHORT).show()
//            Log.d(
//                com.google.ar.sceneform.samples.videorecording.VideoRecordingActivity.TAG,
//                "Video saved: $videoPath"
//            )
//            // Send  notification of updated content.
//            val values = ContentValues()
//            values.put(MediaStore.Video.Media.TITLE, "Sceneform Video")
//            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
//            values.put(MediaStore.Video.Media.DATA, videoPath)
//            contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
//        }
    }

    private fun customizeLight() {
        spotLightYellow = Light.builder(FOCUSED_SPOTLIGHT)
            .setColor(com.google.ar.sceneform.rendering.Color(Color.WHITE))
            .setShadowCastingEnabled(true)
            .build()
    }


    private fun customizeTexture() {
        val sampler = Texture.Sampler.builder()
            .setMinFilter(Texture.Sampler.MinFilter.LINEAR)
            .setWrapMode(Texture.Sampler.WrapMode.REPEAT)
            .build()

        Texture.builder()
            .setSource(this, R.drawable.texture)
            .setSampler(sampler)
            .build()
            .thenAccept({ texture ->
                arFragment.arSceneView.getPlaneRenderer()
                    .getMaterial().thenAccept({ material ->
                        material.setTexture(
                            PlaneRenderer.MATERIAL_TEXTURE,
                            texture
                        )
                    })
            })
    }


    private fun animate(animationId: Int) {
        if (animator.isRunning) {
            animator.end()
        }
        animator = ModelAnimator(marioRenderable.getAnimationData(animationId), marioRenderable)
        animator.start()
    }


    private fun onPlaneTap(hitResult: HitResult) {
        val anchor = hitResult.createAnchor()

        if (anchorNode == null) {
            anchorNode = AnchorNode(anchor)
            // Create the Anchor.
            anchorNode!!.setParent(arFragment.arSceneView.scene)

            TransformableNode(arFragment.transformationSystem)
                .run {
                    setParent(anchorNode)
                    getScaleController().setMinScale(0.5f);
                    getScaleController().setMaxScale(0.7f);
                    localRotation = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 180f)
                    renderable = marioRenderable
                    select()
                }


        }
    }
}
