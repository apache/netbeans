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
 * A variation on the Windows 8 editor tab cell renderer that uses scalable icons for HiDPI screens.
 * See {@link Windows8VectorTabControlIcon}. The icons should otherwise look the same.
 */
final class Windows8VectorEditorTabCellRenderer extends Windows8EditorTabCellRenderer {
    @Override
    Icon findIcon() {
        if( inCloseButton() && isPressed() ) {
            return Windows8VectorTabControlIcon.get(TabControlButton.ID_CLOSE_BUTTON, TabControlButton.STATE_PRESSED);
        } else if( inCloseButton() ) {
            return Windows8VectorTabControlIcon.get(TabControlButton.ID_CLOSE_BUTTON, TabControlButton.STATE_ROLLOVER);
        } else {
            return Windows8VectorTabControlIcon.get(TabControlButton.ID_CLOSE_BUTTON, TabControlButton.STATE_DEFAULT);
        }
    }
}
