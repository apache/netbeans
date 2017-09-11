/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
