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

package org.netbeans.editor.view.spi;

import javax.swing.text.Element;
import javax.swing.text.View;
import org.netbeans.lib.editor.view.ViewUtilitiesImpl;

/**
 * Various utility methods related to views.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class ViewUtilities {

    private ViewUtilities() {
    }


    /**
     * Create view that will cover the whole document.
     */
    public static View createDocumentView(Element elem) {
        return new org.netbeans.lib.editor.view.GapDocumentView(elem);
    }

    /**
     * Check correctness of the hierarchy under the given view.
     * <br>
     * Current checks:
     * <ul>
     *     <li>
     *         Children are Parents are checked to have correct parent info.
     * </ul>
     */
    public static void checkViewHierarchy(View v) {
        ViewUtilitiesImpl.checkViewHierarchy(v);
    }

    /**
     * Test whether the axis is valid.
     *
     * @param axis integer axis
     * @return true if the axis is either <code>View.X_AXIS</code>
     *  or <code>View.Y_AXIS</code>. False is returned otherwise.
     */
    public static boolean isAxisValid(int axis) {
        switch (axis) {
            case View.X_AXIS:
            case View.Y_AXIS:
                return true;
        }
        
        return false;
    }

}
