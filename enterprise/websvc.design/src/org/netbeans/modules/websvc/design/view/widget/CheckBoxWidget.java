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

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;

/**
 *
 * @author Ajit
 */
public class CheckBoxWidget extends ButtonWidget {
    
    public static final String ACTION_COMMAND_SELECTED = "toggle-button-selected";
    public static final String ACTION_COMMAND_DESELECTED = "toggle-button-deselected";

    /**
     *
     * @param scene
     * @param text
     */
    public CheckBoxWidget(Scene scene, String text) {
        super(scene, null, text);
        setImage(new UncheckedImageWidget(scene, 8));
        setSelectedImage(new CheckedImageWidget(scene, 8));
        setBorder(BorderFactory.createEmptyBorder(1));
    }

    @Override
    public void performAction() {
        setSelected(!isSelected());
        super.performAction();
    }
    
    @Override
    public String getActionCommand() {
        return isSelected()?ACTION_COMMAND_SELECTED:ACTION_COMMAND_DESELECTED;
    }

    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        if (previousState.isFocused() != state.isFocused()) {
            setBorder(state.isFocused()?BorderFactory.createDashedBorder
                    (BORDER_COLOR, 2, 2, true):BorderFactory.createEmptyBorder(1));
        }
        super.notifyStateChanged(previousState,state);
    }
    
    private static class CheckedImageWidget extends ImageLabelWidget.PaintableImageWidget {

        public CheckedImageWidget(Scene scene, int size) {
            super(scene, Color.LIGHT_GRAY, size, size);
            setBackground(Color.GRAY);
            setBorder(BorderFactory.createLineBorder(1, Color.GRAY));
            setOpaque(true);
        }

        protected Shape createImage(int width, int height) {
            return new Rectangle2D.Double(0,0,width-2,height-2);
        }
    }

    private static class UncheckedImageWidget extends ImageLabelWidget.PaintableImageWidget {

        public UncheckedImageWidget(Scene scene, int size) {
            super(scene, Color.LIGHT_GRAY, size, size);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(1, Color.GRAY));
            setOpaque(true);
        }

        protected Shape createImage(int width, int height) {
            return new Rectangle2D.Double(0,0,width-2,height-2);
        }
    }
}
