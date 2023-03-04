/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.api.visual.layout;

import org.netbeans.api.visual.widget.Scene;

/**
 * This is used for a one-time operations that had to be invoked after the scene is initialized and/or validated.
 * This is usually used for applying graph-oriented layouts where the layout requires to calculate boundaries
 * of widgets before the layout is invokes.
 * <p>
 * The SceneLayout can be invoked by SceneLayout.invokeLayout method. This method just schedules the scene layout
 * to be performed after the scene validation is done.
 *
 * @author David Kaspar
 */
public abstract class SceneLayout {

    private Scene.SceneListener listener = new LayoutSceneListener ();
    private Scene scene;
    private volatile boolean attached;

    /**
     * Creates a scene layout that is related to a specific scene.
     * @param scene the related scene
     */
    protected SceneLayout (Scene scene) {
        assert scene != null;
        this.scene = scene;
    }

    private void attach () {
        synchronized (this) {
            if (attached)
                return;
            attached = true;
        }
        scene.addSceneListener (listener);
    }

    private void detach () {
        synchronized (this) {
            if (! attached)
                return;
            attached = false;
        }
        scene.removeSceneListener (listener);
    }

    /**
     * Schedules the performing of this scene layout just immediately after the scene validation.
     * It also calls scene revalidation. The Scene.validate method has to be manually called after.
     */
    public final void invokeLayout () {
        attach ();
        scene.revalidate ();
    }

    /**
     * Schedules the performing of this scene layout just immediately after the scene validation.
     * It also calls scene revalidation. The Scene.validate method is called automatically at the end.
     */
    public final void invokeLayoutImmediately () {
        attach ();
        scene.revalidate ();
        scene.validate ();
    }

    /**
     * Called immediately after the scene validation and is responsible for performing the logic e.g. graph-oriented layout.
     */
    protected abstract void performLayout ();

    private final class LayoutSceneListener implements Scene.SceneListener {

        public void sceneRepaint () {
        }

        public void sceneValidating () {
        }

        public void sceneValidated () {
            detach ();
            performLayout ();
        }
    }

}
