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
package org.netbeans.api.visual.vmd;

import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This widget represents a list of glyphs rendered horizontally one after another. A glyph is a small image - usually 16x16px.
 *
 * @author David Kaspar
 */
public class VMDGlyphSetWidget extends Widget {

    /**
     * Creates a glyph set widget.
     * @param scene the scene
     */
    public VMDGlyphSetWidget (Scene scene) {
        super (scene);
        setLayout (LayoutFactory.createHorizontalFlowLayout ());
    }

    /**
     * Sets glyphs as a list of images.
     * @param glyphs the list of images used as glyphs
     */
    public void setGlyphs (List<Image> glyphs) {
        List<Widget> children = new ArrayList<Widget> (getChildren ());
        for (Widget widget : children)
            removeChild (widget);
        if (glyphs != null)
            for (Image glyph : glyphs) {
                ImageWidget imageWidget = new ImageWidget (getScene ());
                imageWidget.setImage (glyph);
                addChild (imageWidget);
            }
    }

}
