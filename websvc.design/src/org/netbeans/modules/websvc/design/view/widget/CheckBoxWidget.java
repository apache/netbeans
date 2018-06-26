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
