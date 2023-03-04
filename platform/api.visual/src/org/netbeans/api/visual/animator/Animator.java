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
package org.netbeans.api.visual.animator;

import org.netbeans.api.visual.widget.Scene;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents an animator. An animator is registed to a scene animator and could be started.
 * From that moment the scene animator automatically calls Animator.tick method for a solid period of time set by the scene animator.
 * In the tick method the animation has to implemented. The animation should be independent on time-duration.
 * <p>
 * Since 2.2, it is possible to listener on important events of the animator using <code>AnimatorListener</code> interface.
 *
 * @author David Kaspar
 */
public abstract class Animator {

    private CopyOnWriteArrayList<AnimatorListener> listeners = new CopyOnWriteArrayList<AnimatorListener> ();
    private SceneAnimator sceneAnimator;

    /**
     * Creates an animator and assigns a scene animator.
     * @param sceneAnimator the scene animator
     */
    protected Animator (SceneAnimator sceneAnimator) {
        assert sceneAnimator != null;
        this.sceneAnimator = sceneAnimator;
    }

    /**
     * Returns a scene that is related to the scene animator.
     * @return the scene
     */
    protected final Scene getScene () {
        return sceneAnimator.getScene ();
    }

    /**
     * Registers and starts the animation.
     */
    protected final void start () {
        if (! listeners.isEmpty ()) {
            AnimatorEvent event = new AnimatorEvent (this);
            for (AnimatorListener listener : listeners)
                listener.animatorStarted (event);
        }
        sceneAnimator.start (this);
    }

    /**
     * Returns whether the animation is running.
     * @return true if still running
     */
    public final boolean isRunning () {
        return sceneAnimator.isRunning (this);
    }

    final void reset () {
        if (! listeners.isEmpty ()) {
            AnimatorEvent event = new AnimatorEvent (this);
            for (AnimatorListener listener : listeners)
                listener.animatorReset (event);
        }
    }

    final void performTick (double progress) {
        if (! listeners.isEmpty ()) {
            AnimatorEvent event = new AnimatorEvent (this, progress);
            for (AnimatorListener listener : listeners)
                listener.animatorPreTick (event);
        }

        tick (progress);

        if (! listeners.isEmpty ()) {
            AnimatorEvent event = new AnimatorEvent (this, progress);
            for (AnimatorListener listener : listeners)
                listener.animatorPostTick (event);
        }

        if (progress >= 1.0) {
            if (! listeners.isEmpty ()) {
                AnimatorEvent event = new AnimatorEvent (this);
                for (AnimatorListener listener : listeners)
                    listener.animatorFinished (event);
            }
        }
    }

    /**
     * Called for performing the animation based on a progress value. The value is a double number in interval from 0.0 to 1.0 (including).
     * The 0.0 value represents beginning, the 1.0 value represents the end.
     * @param progress the progress
     */
    protected abstract void tick (double progress);

    /**
     * Adds an animator listener to the animator.
     * @param listener the animator listener
     * @since 2.2
     */
    public void addAnimatorListener (AnimatorListener listener) {
        listeners.add (listener);
    }

    /**
     * Removes an animator listener from the animator.
     * @param listener the animator listener
     * @since 2.2
     */
    public void removeAnimatorListener (AnimatorListener listener) {
        listeners.remove (listener);
    }

}
