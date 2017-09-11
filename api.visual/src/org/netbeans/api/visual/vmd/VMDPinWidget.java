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
package org.netbeans.api.visual.vmd;

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.List;

/**
 * This class represents a pin widget in the VMD visualization style.
 * The pin widget consists of a name and a glyph set.
 *
 * @author David Kaspar
 */
public class VMDPinWidget extends Widget {

    private VMDColorScheme scheme;

    private LabelWidget nameWidget;
    private VMDGlyphSetWidget glyphsWidget;
    private VMDNodeAnchor anchor;

    /**
     * Creates a pin widget.
     * @param scene the scene
     */
    public VMDPinWidget (Scene scene) {
        this (scene, VMDFactory.getOriginalScheme ());
    }

    /**
     * Creates a pin widget with a specific color scheme.
     * @param scene the scene
     * @param scheme the color scheme
     */
    public VMDPinWidget (Scene scene, VMDColorScheme scheme) {
        super (scene);
        assert scheme != null;
        this.scheme = scheme;

        setLayout (LayoutFactory.createHorizontalFlowLayout (LayoutFactory.SerialAlignment.CENTER, 8));
        addChild (nameWidget = new LabelWidget (scene));
        addChild (glyphsWidget = new VMDGlyphSetWidget (scene));

        scheme.installUI (this);
        notifyStateChanged (ObjectState.createNormal (), ObjectState.createNormal ());
    }

    /**
     * Called to notify about the change of the widget state.
     * @param previousState the previous state
     * @param state the new state
     */
    protected void notifyStateChanged (ObjectState previousState, ObjectState state) {
        scheme.updateUI (this, previousState, state);
    }

    /**
     * Returns a pin name widget.
     * @return the pin name widget
     */
    public Widget getPinNameWidget () {
        return nameWidget;
    }

    /**
     * Sets a pin name.
     * @param name the pin name
     */
    public void setPinName (String name) {
        nameWidget.setLabel (name);
    }

    /**
     * Returns a pin name.
     * @return the pin name
     */
    public String getPinName () {
        return nameWidget.getLabel();
    }

    /**
     * Sets pin glyphs.
     * @param glyphs the list of images
     */
    public void setGlyphs (List<Image> glyphs) {
        glyphsWidget.setGlyphs (glyphs);
    }

    /**
     * Sets all pin properties at once.
     * @param name the pin name
     * @param glyphs the pin glyphs
     */
    public void setProperties (String name, List<Image> glyphs) {
        setPinName (name);
        glyphsWidget.setGlyphs (glyphs);
    }

    /**
     * Creates a horizontally oriented anchor similar to VMDNodeWidget.createAnchorPin
     * @return the anchor
     */
    public Anchor createAnchor () {
        if (anchor == null)
            anchor = new VMDNodeAnchor (this, false);
        return anchor;
    }

}
