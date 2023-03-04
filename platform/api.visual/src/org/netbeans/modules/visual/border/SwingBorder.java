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
package org.netbeans.modules.visual.border;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.border.Border;

import java.awt.*;

/**
 * @author David Kaspar
 */
// TODO - Scene.getView can return null before Scene.createView is called
public final class SwingBorder implements Border {

    private Scene scene;
    private javax.swing.border.Border swingBorder;

    public SwingBorder (Scene scene, javax.swing.border.Border swingBorder) {
        assert scene != null  &&  swingBorder != null;
        this.scene = scene;
        this.swingBorder = swingBorder;
    }

    public Insets getInsets () {
        return swingBorder.getBorderInsets (scene.getView ());
    }

    public void paint (Graphics2D gr, Rectangle bounds) {
        swingBorder.paintBorder (scene.getView (), gr, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public boolean isOpaque () {
        return false;
    }
    
    public javax.swing.border.Border getSwingBorder () {
        return swingBorder;
    }

}
