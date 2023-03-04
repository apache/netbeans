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

package org.netbeans.lib.editor.view;

import javax.swing.text.Element;
import javax.swing.text.View;

/**
 * Extension of <code>GapBoxView</code> encapsulating
 * one line of a text capable of doing baseline layout
 * and line wrapping.
 *
 * <p>
 * The view returns the
 * <code>minorAxisMinSpan</code> variable is reused
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class GapLineView extends GapBoxView {

    public GapLineView(Element lineElement) {
        super(lineElement, View.X_AXIS);
    }

    GapBoxViewChildren createChildren() {
        return new GapLineViewChildren(this);
    }

}
