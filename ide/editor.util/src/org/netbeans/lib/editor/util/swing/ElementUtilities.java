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

package org.netbeans.lib.editor.util.swing;

import javax.swing.text.Element;

/**
 * Various utility methods related to elements.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class ElementUtilities {

    private ElementUtilities() {
        // No instances
    }

    public static void updateOffsetRange(Element[] elements, int[] offsetRange) {
        int elementsLength = elements.length;
        if (elementsLength > 0) {
            offsetRange[0] = Math.min(offsetRange[0], elements[0].getStartOffset());
            offsetRange[1] = Math.max(offsetRange[1], elements[elementsLength - 1].getEndOffset());
        }
    }

}
