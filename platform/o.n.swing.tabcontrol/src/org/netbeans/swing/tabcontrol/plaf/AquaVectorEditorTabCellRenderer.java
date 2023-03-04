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
package org.netbeans.swing.tabcontrol.plaf;

import javax.swing.Icon;

/**
 * A variation on the Aqua editor tab cell renderer that uses scalable icons for Retina screens.
 * See {@link AquaVectorTabControlIcon}.
 */
final class AquaVectorEditorTabCellRenderer extends AquaEditorTabCellRenderer {
    @Override
    protected Icon findIcon() {
        /* The "mac_close_(enabled|pressed|rollover).png" files were confirmed to be identical to
        the mac_bigclose_(enabled|pressed|rollover).png ones. So we can use the same icons as in the
        tab control here. */
        if( inCloseButton() && isPressed() ) {
            return AquaVectorTabControlIcon.get(TabControlButton.ID_CLOSE_BUTTON, TabControlButton.STATE_PRESSED);
        } else if( inCloseButton() ) {
            return AquaVectorTabControlIcon.get(TabControlButton.ID_CLOSE_BUTTON, TabControlButton.STATE_ROLLOVER);
        } else {
            return AquaVectorTabControlIcon.get(TabControlButton.ID_CLOSE_BUTTON, TabControlButton.STATE_DEFAULT);
        }
    }
}
