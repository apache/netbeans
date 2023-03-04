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

package org.netbeans.core.windows.view.ui.toolbars;

/** 
 * Simple value class holding some toolbar properties.
 *
 * @author S. Aubrecht
 */
class ToolbarConstraints {

    static enum Align {
        left,
        right;

        static Align fromString( String s ) {
            if( "right".equals(s) )
                return right;
            return left;
        }
    }

    /** Toolbar name */
    private final String name;

    private Align align;

    private boolean visible;

    private final boolean draggable;

    public ToolbarConstraints( String name, Align align, boolean visible, boolean draggable ) {
        this.name = name;
        this.draggable = draggable;
        this.align = align;
        this.visible = visible;
    }

    public Align getAlign() {
        return align;
    }

    public void setAlign(Align align) {
        this.align = align;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getName() {
        return name;
    }
} // end of class ToolbarConstraints

