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
package org.netbeans.modules.visual.apichanges;

import org.netbeans.modules.visual.framework.VisualTestCase;
import org.netbeans.api.visual.animator.AnimatorEvent;
import org.netbeans.api.visual.animator.AnimatorListener;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.junit.RandomlyFails;

/**
 * Test for #99048 - Animator listener is needed
 * @author David Kaspar
 */
public class AnimatorListenerTest extends VisualTestCase {

    public AnimatorListenerTest (String name) {
        super (name);
    }

    @Override
    protected boolean runInEQ() {
        return false;
    }

    @RandomlyFails
    public void testAnimatorListener () throws InterruptedException, InvocationTargetException {
        Scene[] scene = { null };
        SwingUtilities.invokeAndWait (new Runnable() {
            @Override
            public void run () {
                scene[0] = initScene();
            }
        });

        final JFrame[] frame = new JFrame[1];
        SwingUtilities.invokeAndWait (new Runnable() {
            @Override
            public void run () {
                frame[0] = showFrame (scene[0]);
            }
        });

        Thread.sleep (2000);

        SwingUtilities.invokeAndWait (new Runnable() {
            @Override
            public void run () {
                frame[0].setVisible (false);
                frame[0].dispose ();
            }
        });

        compareReferenceFiles ();
    }

    private Scene initScene() {
        final Scene scene = new Scene ();
        Widget widget = new Widget (scene);
        scene.addChild (widget);
        AnimatorListener listener = new AnimatorListener() {
            public void animatorStarted (AnimatorEvent event) {
                getRef ().println ("Animator started");
            }
            public void animatorReset (AnimatorEvent event) {
                getRef ().println ("Animator reset");
            }
            public void animatorFinished (AnimatorEvent event) {
                getRef ().println ("Animator finished");
            }
            public void animatorPreTick (AnimatorEvent event) {
                if (event.getProgress () >= 1.0)
                    getRef ().println ("Animator pre-tick: " + event.getProgress ());
            }
            public void animatorPostTick (AnimatorEvent event) {
                if (event.getProgress () >= 1.0)
                    getRef ().println ("Animator post-tick: " + event.getProgress ());
            }
        };
        scene.getSceneAnimator ().getPreferredLocationAnimator ().addAnimatorListener (listener);
        widget.setPreferredLocation (new Point (0, 0));
        scene.getSceneAnimator ().animatePreferredLocation (widget, new Point (100, 100));
        return scene;
    }

}
