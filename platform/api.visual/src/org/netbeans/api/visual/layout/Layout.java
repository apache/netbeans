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
package org.netbeans.api.visual.layout;

import org.netbeans.api.visual.widget.Widget;

/**
 * This class is responsible for layout and justification of children widgets of a widget where the layout is assigned.
 * Built-in layouts could be created by LayoutFactory class.
 *
 * @author David Kaspar
 */
public interface Layout {

    /**
     * Resolve bounds of widget children based in their preferred locations and bounds.
     * @param widget the widget
     */
    public void layout (Widget widget);

    /**
     * Resolve whether a widget requires justification after whole scene layout.
     * @param widget the widget
     * @return true if requires justification
     */
    public boolean requiresJustification (Widget widget);

    /**
     * Justify bounds of widget children based on a widget client area.
     * @param widget the widget
     */
    public void justify (Widget widget);

}
