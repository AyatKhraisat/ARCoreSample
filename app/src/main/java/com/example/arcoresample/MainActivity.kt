package com.example.arcoresample

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.rendering.Light
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.SkeletonNode
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Light.Type.FOCUSED_SPOTLIGHT
import com.google.ar.sceneform.rendering.PlaneRenderer
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.TransformableNode
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment

    private lateinit var modelLoader: ModelLoader
    private lateinit var animator: ModelAnimator
    private lateinit var marioRenderable: ModelRenderable
    private var anchorNode: AnchorNode? = null
    private lateinit var marioSketlonNode: SkeletonNode
    private lateinit var spotLightYellow: Light

    companion object {
        private val TAG = "ModelLoader"
        private val MARIO_MODLE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.frag_sceneform) as ArFragment


        arFragment.setOnTapArPlaneListener({ hitResult, unusedPlane, unusedMotionEvent ->
            this.onPlaneTap(
                hitResult
            )
        })

        fab_jump.setOnClickListener({ animate(JUMP) })



        modelLoader = ModelLoader(this)

        modelLoader.loadModel(MARIO_MODLE, R.raw.mario,
            { id, modelRenderable ->
                Log.d(TAG, "model loaded")
                marioRenderable = modelRenderable!!
                val data = marioRenderable.getAnimationData(WALKING)
                animator = ModelAnimator(data, marioRenderable)
                animator.start()
                null
            },
            { id, throwable ->
                Log.d(TAG, "\"Can't load Model\"" + throwable.message)
                Toast.makeText(this, "Can't load Model", Toast.LENGTH_LONG).show()
                null
            })

        customizeLight()
        customizeTexture()


    }

    private fun customizeLight() {
        spotLightYellow = Light.builder(FOCUSED_SPOTLIGHT)
            .setColor(com.google.ar.sceneform.rendering.Color(Color.YELLOW))
            .setShadowCastingEnabled(true)
            .build()
    }

    private fun customizeTexture() {
        val sampler = Texture.Sampler.builder()
            .setMinFilter(Texture.Sampler.MinFilter.LINEAR)
            .setWrapMode(Texture.Sampler.WrapMode.REPEAT)
            .build()

        Texture.builder()
            .setSource(this, R.drawable.spot)
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

    private fun animate(animationName :Int){
        if (animator.isRunning()) {
            animator.end()
        }
            animator = ModelAnimator(marioRenderable.getAnimationData(0), marioRenderable)
            animator.start()
        }


    private fun onPlaneTap(hitResult: HitResult) {
        val anchor = hitResult.createAnchor()

        if (anchorNode == null) {
            anchorNode = AnchorNode(anchor)
            // Create the Anchor.
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)

            val marioNode = TransformableNode(arFragment.transformationSystem)
            marioNode.setParent(anchorNode)
            marioNode.getScaleController().setMinScale(0.01f);
            marioNode.getScaleController().setMaxScale(0.2f);
            marioNode.localRotation = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 180f)
            marioNode.renderable = marioRenderable
            marioNode.select()



        }
    }
}
