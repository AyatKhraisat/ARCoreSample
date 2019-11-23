package com.example.arcoresample

import android.content.Intent
import android.os.Bundle
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
        ib_switch_camera.setOnClickListener { switchCamera() }
        fab_capture.setOnClickListener {
            takePhoto(
                this@FaceArActivity,
                faceArFragment.arSceneView
            )
        }
    }


    private fun loadModels() {
        modelLoader = ModelLoader(this)

        modelLoader.loadModel(
            FaceArActivity.MARIO_HAT_MODLE, R.raw.mario_hat,
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

