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
package org.netbeans.api.visual.border;

import org.netbeans.modules.visual.border.ResizeBorder;
import org.netbeans.modules.visual.border.SwingBorder;

/**
 * This class contains support method for working with borders.
 *
 * @author David Kaspar
 */
public final class BorderSupport {

    private BorderSupport () {
    }

    /**
     * Returns whether a resize border is outer.
     * @param border the border created by
     * @return true if the border is created the createResizeBorder method as outer parameter set to true; false otherwise
     */
    public static boolean isOuterResizeBorder (Border border) {
        return border instanceof ResizeBorder  &&  ((ResizeBorder) border).isOuter ();
    }
    
    /**
     * Returns a swing border of a border created using BorderFactory.createSwingBorder or Widget.setBorder(javax.swing.border.Border).
     * @param border the widget border
     * @return Swing border if possible; otherwise null
     * @since 2.6
     */
    public static javax.swing.border.Border getSwingBorder (Border border) {
        return border instanceof SwingBorder ? ((SwingBorder) border).getSwingBorder () : null;
    }

}
