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
package es.ucm.demoarcore


import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import java.util.concurrent.CompletableFuture;


/**
 * Node for rendering an augmented image. The image is framed by placing the virtual picture frame
 * at the corners of the augmented image trackable.
 */
class AugmentedImageNode(context: Context) : AnchorNode() {

    private val TAG = "AugmentedImageNode"

    // The augmented image represented by this node.
    private var image: AugmentedImage? = null

    // Models of the 4 corners.  We use completable futures here to simplify
    // the error handling and asynchronous loading.  The loading is started with the
    // first construction of an instance, and then used when the image is set.
    private var ulCorner: CompletableFuture<ModelRenderable>? = null
    private var urCorner: CompletableFuture<ModelRenderable>? = null
    private var lrCorner: CompletableFuture<ModelRenderable>? = null
    private var llCorner: CompletableFuture<ModelRenderable>? = null

    fun constructor(context: Context) {
        // Upon construction, start loading the models for the corners of the frame.
        if (ulCorner == null) {
            ulCorner = ModelRenderable.builder()
                    .setSource(context, Uri.parse("models/frame_upper_left.sfb"))
                    .build()
            urCorner = ModelRenderable.builder()
                    .setSource(context, Uri.parse("models/frame_upper_right.sfb"))
                    .build()
            llCorner = ModelRenderable.builder()
                    .setSource(context, Uri.parse("models/frame_lower_left.sfb"))
                    .build()
            lrCorner = ModelRenderable.builder()
                    .setSource(context, Uri.parse("models/frame_lower_right.sfb"))
                    .build()
        }
    }

    /**
     * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
     * created based on an Anchor created from the image. The corners are then positioned based on the
     * extents of the image. There is no need to worry about world coordinates since everything is
     * relative to the center of the image, which is the parent node of the corners.
     */
    fun setImage(image: AugmentedImage) {
        this.image = image

        // If any of the models are not loaded, then recurse when all are loaded.
        if (!ulCorner!!.isDone || !urCorner!!.isDone || !llCorner!!.isDone || !lrCorner!!.isDone) {
            CompletableFuture.allOf(ulCorner, urCorner, llCorner, lrCorner)
                    .thenAccept { aVoid: Void -> setImage(image) }
                    .exceptionally { throwable ->
                        Log.e(TAG, "Exception loading", throwable)
                        null
                    }
        }

        // Set the anchor based on the center of the image.
        anchor = image.createAnchor(image.centerPose)

        // Make the 4 corner nodes.
        val localPosition = Vector3()
        var cornerNode: Node

        // Upper left corner.
        localPosition.set(-0.5f * image.extentX, 0.0f, -0.5f * image.extentZ)
        cornerNode = Node()
        cornerNode.setParent(this)
        cornerNode.localPosition = localPosition
        cornerNode.renderable = ulCorner!!.getNow(null)

        // Upper right corner.
        localPosition.set(0.5f * image.extentX, 0.0f, -0.5f * image.extentZ)
        cornerNode = Node()
        cornerNode.setParent(this)
        cornerNode.localPosition = localPosition
        cornerNode.renderable = urCorner!!.getNow(null)

        // Lower right corner.
        localPosition.set(0.5f * image.extentX, 0.0f, 0.5f * image.extentZ)
        cornerNode = Node()
        cornerNode.setParent(this)
        cornerNode.localPosition = localPosition
        cornerNode.renderable = lrCorner!!.getNow(null)

        // Lower left corner.
        localPosition.set(-0.5f * image.extentX, 0.0f, 0.5f * image.extentZ)
        cornerNode = Node()
        cornerNode.setParent(this)
        cornerNode.localPosition = localPosition
        cornerNode.renderable = llCorner!!.getNow(null)
    }

    fun getImage(): AugmentedImage? {
        return image
    }
}