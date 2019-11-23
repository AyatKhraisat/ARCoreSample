/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.arcoresample

import android.util.Log
import android.util.SparseArray
import androidx.appcompat.app.AppCompatActivity

import com.google.ar.sceneform.rendering.ModelRenderable

import java.lang.ref.WeakReference
import java.util.concurrent.CompletableFuture

/**
 * Model loader class to avoid memory leaks from the activity. Activity and Fragment controller
 * classes have a lifecycle that is controlled by the UI thread. When a reference to one of these
 * objects is accessed by a background thread it is "leaked". Using that reference to a
 * lifecycle-bound object after Android thinks it has "destroyed" it can produce bugs. It also
 * prevents the Activity or Fragment from being garbage collected, which can leak the memory
 * permanently if the reference is held in the singleton scope.
 *
 *
 * To avoid this, use a non-nested class which is not an activity nor fragment. Hold a weak
 * reference to the activity or fragment and use that when making calls affecting the UI.
 */
class ModelLoader internal constructor(owner: AppCompatActivity) {

    companion object {
        private val TAG = ModelLoader::class.java.simpleName
    }

    private val futureSet = SparseArray<CompletableFuture<ModelRenderable>>()
    private val owner: WeakReference<AppCompatActivity> = WeakReference(owner)


     fun loadModel(
        id: Int,
        resourceId: Int,
        setRenderable: (Int, ModelRenderable?) -> ModelRenderable?,
        onException: (a: Int, t: Throwable) -> ModelRenderable?
    ): Boolean {
        val activity = owner.get()
        if (activity == null) {
            Log.d(TAG, "Activity is null.  Cannot load model.")
            return false
        }
        val future  = ModelRenderable.builder()
            .setSource(owner.get(), resourceId)
            .build()
            .thenApply<ModelRenderable> { renderable ->

                    futureSet.remove(id)
                    setRenderable(id, renderable)

            }.exceptionally { throwable ->

                    futureSet.remove(id)
                    onException(id, throwable)

            }
        if (future != null) {
            futureSet.put(id, future)
        }
        return future != null
    }


}
