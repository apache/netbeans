/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.editor.lib.drawing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Map;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.EditorUI;

/**
 *
 * @author vita
 */
public abstract class EditorUiAccessor {

    private static EditorUiAccessor ACCESSOR = null;

    public static synchronized void register(EditorUiAccessor accessor) {
        assert ACCESSOR == null : "Can't register two package accessors!"; //NOI18N
        ACCESSOR = accessor;
    }

    public static synchronized EditorUiAccessor get() {
        // Trying to wake up EditorUI ...
        try {
            Class<?> clazz = Class.forName(EditorUI.class.getName());
        } catch (ClassNotFoundException e) {
            // ignore
        }

        assert ACCESSOR != null : "There is no package accessor available!"; //NOI18N
        return ACCESSOR;
    }

    protected EditorUiAccessor() {
    }

    public abstract boolean isLineNumberVisible(EditorUI eui);
    public abstract Coloring getColoring(EditorUI eui, String coloringName);
    public abstract int getLineNumberMaxDigitCount(EditorUI eui);
    public abstract int getLineNumberWidth(EditorUI eui);
    public abstract int getLineNumberDigitWidth(EditorUI eui);
    public abstract Insets getLineNumberMargin(EditorUI eui);
    public abstract int getLineHeight(EditorUI eui);
    public abstract Coloring getDefaultColoring(EditorUI eui);
    public abstract int getDefaultSpaceWidth(EditorUI eui);
    public abstract Map<?, ?> getRenderingHints(EditorUI eui);
    public abstract Rectangle getExtentBounds(EditorUI eui);
    public abstract Insets getTextMargin(EditorUI eui);
    public abstract int getTextLeftMarginWidth(EditorUI eui);
    public abstract boolean getTextLimitLineVisible(EditorUI eui);
    public abstract Color getTextLimitLineColor(EditorUI eui);
    public abstract int getTextLimitWidth(EditorUI eui);
    public abstract int getLineAscent(EditorUI eui);
    public abstract void paint(EditorUI eui, Graphics g);
    public abstract DrawLayerList getDrawLayerList(EditorUI eui);

}
