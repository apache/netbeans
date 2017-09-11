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

    @RandomlyFails
    public void testAnimatorListener () {
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

        final JFrame[] frame = new JFrame[1];
        try {
            SwingUtilities.invokeAndWait (new Runnable() {
                public void run () {
                    frame[0] = showFrame (scene);
                }
            });

            Thread.sleep (2000);

            SwingUtilities.invokeAndWait (new Runnable() {
                public void run () {
                    frame[0].setVisible (false);
                    frame[0].dispose ();
                }
            });
        } catch (InterruptedException e) {
            throw new AssertionError (e);
        } catch (InvocationTargetException e) {
            throw new AssertionError (e);
        }

        compareReferenceFiles ();
    }

}
