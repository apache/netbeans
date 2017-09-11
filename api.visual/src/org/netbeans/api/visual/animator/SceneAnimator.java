/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.animator.PreferredBoundsAnimator;
import org.netbeans.modules.visual.animator.PreferredLocationAnimator;
import org.netbeans.modules.visual.animator.ZoomAnimator;
import org.netbeans.modules.visual.animator.ColorAnimator;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.awt.*;

/**
 * Manages all animations on a scene. An animation can be registered and started by calling Animator.start method.
 * The class contains a few built-in animators: preferredLocation, preferredBounds, background, foreground, zoomFactor.
 * 
 * @author David Kaspar
 */
public final class SceneAnimator {

    private static final long TIME_PERIOD = 500;
    private static final int SLEEP = 16;

    private Scene scene;

    private final HashMap<Animator, Long> animators = new HashMap<Animator, Long> ();
    private HashMap<Animator, Double> cache;
    private final Runnable task = new UpdateTask ();
    private volatile boolean taskAlive;

    private PreferredLocationAnimator preferredLocationAnimator = new PreferredLocationAnimator (this);
    private PreferredBoundsAnimator preferredBoundsAnimator = new PreferredBoundsAnimator (this);
    private ZoomAnimator zoomAnimator = new ZoomAnimator (this);
    private ColorAnimator colorAnimator = new ColorAnimator (this);
    
    private static final RequestProcessor RP = new RequestProcessor(SceneAnimator.class.toString(), 50);

    /**
     * Creates a scene animator.
     * @param scene the scene
     */
    public SceneAnimator (Scene scene) {
        this.scene = scene;
    }

    /**
     * Returns an assigned scene.
     * @return the scene
     */
    public Scene getScene () {
        return scene;
    }
    
    void start (Animator animator) {
        synchronized (animators) {
            animators.put (animator, System.currentTimeMillis ());
            animator.reset ();
            if (! taskAlive) {
                taskAlive = true;
                RP.post (task);
            }
        }
    }

    boolean isRunning (Animator animator) {
        synchronized (animators) {
            if (animators.containsKey (animator))
                return true;
            if (cache != null  &&  cache.containsKey (animator))
                return true;
        }
        return false;
    }

    /**
     * Returns whether a preferredLocation animator for a specified widget is running.
     * @param widget the widget
     * @return true if running
     */
    public boolean isAnimatingPreferredLocation (Widget widget) {
        return isRunning (preferredLocationAnimator);
    }

    /**
     * Starts preferredLocation animation for a specified widget.
     * @param widget the widget
     * @param targetPreferredLocation the target preferred location
     */
    public void animatePreferredLocation (Widget widget, Point targetPreferredLocation) {
        preferredLocationAnimator.setPreferredLocation (widget, targetPreferredLocation);
    }

    /**
     * Returns whether a preferredBounds animator for a specified widget is running.
     * @param widget the widget
     * @return true if running
     */
    public boolean isAnimatingPreferredBounds (Widget widget) {
        return isRunning (preferredBoundsAnimator);
    }

    /**
     * Starts preferredBounds animation for a specified widget.
     * @param widget the widget
     * @param targetPreferredBounds the target preferred bounds
     */
    public void animatePreferredBounds (Widget widget, Rectangle targetPreferredBounds) {
        preferredBoundsAnimator.setPreferredBounds (widget, targetPreferredBounds);
    }

    /**
     * Returns whether a zoomFactor animator is running.
     * @return true if running
     */
    public boolean isAnimatingZoomFactor () {
        return isRunning (zoomAnimator);
    }

    /**
     * Returns a target zoom factor.
     * @return the target zoom factor
     */
    public double getTargetZoomFactor () {
        return zoomAnimator.getTargetZoom ();
    }

    /**
     * Starts zoomFactor animation.
     * @param targetZoomFactor the target zoom factor
     */
    public void animateZoomFactor (double targetZoomFactor) {
        zoomAnimator.setZoomFactor (targetZoomFactor);
    }

    /**
     * Returns whether a backgroundColor animator for a specified widget is running.
     * @param widget the widget
     * @return true if running
     */
    public boolean isAnimatingBackgroundColor (Widget widget) {
        return isRunning (colorAnimator);
    }

    /**
     * Starts backgroundColor animation for a specified widget.
     * @param widget the widget
     * @param targetBackgroundColor the target background color
     */
    public void animateBackgroundColor (Widget widget, Color targetBackgroundColor) {
        colorAnimator.setBackgroundColor (widget, targetBackgroundColor);
    }

    /**
     * Returns whether a foregroundColor animator for a specified widget is running.
     * @param widget the widget
     * @return true if running
     */
    public boolean isAnimatingForegroundColor (Widget widget) {
        return isRunning (colorAnimator);
    }

    /**
     * Starts foregroundColor animation for a specified widget.
     * @param widget the widget
     * @param targetForegroundColor the target foreground color
     */
    public void animateForegroundColor (Widget widget, Color targetForegroundColor) {
        colorAnimator.setForegroundColor (widget, targetForegroundColor);
    }

    /**
     * Returns the preferred location animator which animates preferred location of all widgets in the scene.
     * @return the preferred location animator
     * @since 2.2
     */
    public Animator getPreferredLocationAnimator () {
        return preferredLocationAnimator;
    }

    /**
     * Returns the preferred bounds animator which animates preferred bounds of all widgets in the scene.
     * @return the preferred bounds animator
     * @since 2.2
     */
    public Animator getPreferredBoundsAnimator () {
        return preferredBoundsAnimator;
    }

    /**
     * Returns the zoom animator.
     * @return the zoom animator
     * @since 2.2
     */
    public Animator getZoomAnimator () {
        return zoomAnimator;
    }

    /**
     * Returns the color animator which animates background and foreground colors of all widgets in the scene.
     * @return the preferred location animator
     * @since 2.2
     */
    public Animator getColorAnimator () {
        return colorAnimator;
    }

    private class UpdateTask implements Runnable {

        public void run () {
            synchronized (animators) {
                long currentTime = System.currentTimeMillis ();
                Set<Map.Entry<Animator, Long>> entries = animators.entrySet ();
                cache = new HashMap<Animator, Double> ();

                for (Iterator<Map.Entry<Animator, Long>> iterator = entries.iterator (); iterator.hasNext ();) {
                    Map.Entry<Animator, Long> entry = iterator.next ();
                    long diff = currentTime - entry.getValue ();
                    double progress;
                    if (diff < 0  ||  diff > TIME_PERIOD) {
                        iterator.remove ();
                        progress = 1.0;
                    } else
                        progress = (double) diff / (double) TIME_PERIOD;
                    cache.put (entry.getKey (), progress);
                }
            }

            try {
                SwingUtilities.invokeAndWait (new Runnable () {
                    public void run () {
                        for (final Map.Entry<Animator, Double> entry : cache.entrySet ())
                            entry.getKey ().performTick (entry.getValue ());
                        scene.validate ();
                    }
                });
            } catch (InterruptedException e) {
                ErrorManager.getDefault ().notify (e);
            } catch (InvocationTargetException e) {
                ErrorManager.getDefault ().notify (e);
            }

            synchronized (animators) {
                cache = null;
                taskAlive = animators.size () > 0;
                if (taskAlive)
                    RP.post (task, SLEEP);
            }
        }

    }

}
