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
package org.netbeans.api.visual.action;

import org.netbeans.api.visual.widget.Widget;

/**
 * This is an inteface for text-field based in-place editor.
 *
 * @author David Kaspar
 */
public interface TextFieldInplaceEditor {

    /**
     * Returns whether the in-place editing is allowed.
     * @param widget the widget where the editor will be invoked
     * @return true, if enabled; false if disabled
     */
    boolean isEnabled (Widget widget);

    /**
     * Returns an initial text of the in-place editor.
     * @param widget the edited widget
     * @return the initial text
     */
    String getText (Widget widget);

    /**
     * Sets a new text approved by an user.
     * @param widget the edited widget
     * @param text the new text entered by an user
     */
    void setText (Widget widget, String text);

}
