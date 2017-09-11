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
