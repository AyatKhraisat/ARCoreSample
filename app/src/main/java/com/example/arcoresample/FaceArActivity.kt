package com.example.arcoresample

import android.content.Intent
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.media.CamcorderProfile
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.AugmentedFaceNode
import kotlinx.android.synthetic.main.activity_face_ar.*
import kotlinx.android.synthetic.main.activity_face_ar.ib_switch_camera
import kotlinx.android.synthetic.main.activity_face_ar.fab_capture
import kotlinx.android.synthetic.main.activity_main.*
import java.util.HashMap

class FaceArActivity : AppCompatActivity() {

    private lateinit var faceArFragment: FaceArFragment
    private val faceNodeMap = HashMap<AugmentedFace, AugmentedFaceNode>()
    private lateinit var modelLoader: ModelLoader
    private lateinit var marioHatRenderable: ModelRenderable
    private lateinit var videoRecorder: VideoRecorder


    companion object {
        private val TAG = FaceArActivity::class.java.simpleName
        private val MARIO_HAT_MODLE = 1002
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_ar)
        faceArFragment = supportFragmentManager.findFragmentById(R.id.face_ar_fragment)
                as FaceArFragment
        initArFaceScene()
        loadModels()
        initVideoRecorder()
        ib_switch_camera.setOnClickListener { switchCamera() }
        fab_capture.run {
            setOnClickListener({
            hideControls()
                takePhoto(this@FaceArActivity, faceArFragment.arSceneView,{
                 showControls()
                })
            })

            setOnLongClickListener({
                videoRecorder.toggleRecording()
                true
            })
            setOnTouchListener({ view, motionEvent ->
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    val eventDuration = motionEvent.getEventTime() - motionEvent.getDownTime();
                    if (eventDuration > 500) {
                        videoRecorder.stopRecording()
                    }
                }
                false
            })
        }
    }


    private fun showControls() {

        pb_capture_face.visibility = View.GONE
        fab_capture.isClickable = true
        ib_switch_camera.visibility = View.VISIBLE
    }

    private fun hideControls() {
        pb_capture_face.visibility = View.VISIBLE
        fab_capture.isClickable = false
        ib_switch_camera.visibility = View.GONE
    }
    private fun initVideoRecorder() {
        videoRecorder = VideoRecorder(this)
        videoRecorder.setSceneView(faceArFragment.arSceneView)
    }

    private fun loadModels() {
        modelLoader = ModelLoader(this)

        modelLoader.loadModel(MARIO_HAT_MODLE, R.raw.mario_hat,
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

    private fun switchCamera() {
        intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0);
    }

    private fun initArFaceScene() {
        val sceneView = faceArFragment.arSceneView
            sceneView.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST

            val scene = sceneView.scene

            scene.addOnUpdateListener { frameTime: FrameTime? ->
                val faceList =
                    sceneView.session!!.getAllTrackables(
                        AugmentedFace::class.java
                    )
                // Make new AugmentedFaceNodes for any new faces.
                for (face in faceList) {
                    if (!faceNodeMap.containsKey(face)) {
                        val faceNode = AugmentedFaceNode(face)
                        faceNode.setParent(scene)
                        faceNode.faceRegionsRenderable = marioHatRenderable
                        faceNodeMap.put(face, faceNode)
                    }
                }
                val iter: MutableIterator<Map.Entry<AugmentedFace, AugmentedFaceNode>> =
                    faceNodeMap.entries.iterator()
                while (iter.hasNext()) {
                    val entry =
                        iter.next()
                    val face = entry.key
                    if (face.trackingState == TrackingState.STOPPED) {
                        val faceNode = entry.value
                        faceNode.setParent(null)
                        iter.remove()
                    }
                }
            }
        }
    }

