package com.example.arcoresample

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.media.CamcorderProfile
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Light
import com.google.ar.sceneform.rendering.Light.Type.FOCUSED_SPOTLIGHT
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.animation_group
import kotlinx.android.synthetic.main.activity_main.fab_capture
import kotlinx.android.synthetic.main.activity_main.pb_capture
import kotlinx.android.synthetic.main.activity_main.view.*


class MainActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private lateinit var modelLoader: ModelLoader
    private lateinit var animator: ModelAnimator
    private lateinit var marioRenderable: ModelRenderable
    private var anchorNode: AnchorNode? = null
    private lateinit var spotLightWhite: Light
    private lateinit var videoRecorder: VideoRecorder


    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val MARIO_MODLE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment
        initArScene()
        setViewsListener()
        loadModels()
        initArScene()
        customizeLight()
        initVideoRecorder()

    }

    private fun initVideoRecorder() {
        videoRecorder = VideoRecorder(this)
        videoRecorder.setSceneView(arFragment.arSceneView)
    }

    private fun setViewsListener() {
        fab_excited.setOnClickListener({ animate(EXCITED) })
        fab_dancing.setOnClickListener({ animate(DANCE) })
        fab_sad.setOnClickListener({ animate(SAD) })
        fab_surprised.setOnClickListener({ animate(SURPRISED) })
        ib_switch_camera.setOnClickListener { switchCamera() }
        fab_capture.run {
            setOnClickListener({
                hideControls()
                takePhoto(this@MainActivity, arFragment.arSceneView, {
                    showControls()
                })
            })

            setOnLongClickListener({
                hideControls()
                fab_capture.setImageResource(R.drawable.ic_stop)
                videoRecorder.toggleRecording()
                true
            })
            setOnTouchListener({ view, motionEvent ->
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    val eventDuration = motionEvent.getEventTime() - motionEvent.getDownTime();
                    if (eventDuration > 500) {
                        fab_capture.setImageResource(R.drawable.ic_camera)
                        videoRecorder.stopRecording()
                        showControls()
                    }
                }
                false
            })
        }
    }

    private fun showControls() {
        pb_capture.visibility = GONE
        fab_capture.isClickable = true
        animation_group.visibility = VISIBLE
        ib_switch_camera.visibility = VISIBLE
    }

    private fun hideControls() {
        pb_capture.visibility = VISIBLE
        fab_capture.isClickable = false
        animation_group.visibility = GONE
        ib_switch_camera.visibility = GONE
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


    private fun customizeLight() {
        spotLightWhite = Light.builder(FOCUSED_SPOTLIGHT)
            .setColor(com.google.ar.sceneform.rendering.Color(Color.WHITE))
            .setShadowCastingEnabled(true)
            .build()
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
