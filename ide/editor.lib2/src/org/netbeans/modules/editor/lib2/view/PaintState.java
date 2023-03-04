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

package org.netbeans.modules.editor.lib2.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Save and restore painting info of a graphics.
 *
 * @author Miloslav Metelka
 */
public final class PaintState {

    public static PaintState save(Graphics2D g) {
        return new PaintState(g);
    }

    private final Graphics2D g;

    private final Font font;

    private final Color color;

    private PaintState(Graphics2D g) {
        this.g = g;
        font = g.getFont();
        color = g.getColor();
    }

    public void restore() {
        g.setColor(color);
        g.setFont(font);
    }

}
