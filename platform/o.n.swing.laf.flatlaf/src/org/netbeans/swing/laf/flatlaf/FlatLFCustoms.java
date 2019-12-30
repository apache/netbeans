/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.swing.laf.flatlaf;

import com.formdev.flatlaf.util.UIScale;
import javax.swing.UIManager;
import org.netbeans.swing.plaf.LFCustoms;

/**
 * LFCustoms for FlatLaf based LAFs (light, dark, etc).
 * <p>
 * Do not add colors here.
 * Instead put colors into {@code FlatLightLaf.properties} or {@code FlatDarkLaf.properties}.
 * <p>
 * Public only to be accessible by ProxyLazyValue, please don't abuse.
 */
public class FlatLFCustoms extends LFCustoms {

    @Override
    public Object[] createApplicationSpecificKeysAndValues() {
        return new Object[] {
            EDITOR_TAB_DISPLAYER_UI, "org.netbeans.swing.laf.flatlaf.ui.FlatEditorTabDisplayerUI", // NOI18N
            VIEW_TAB_DISPLAYER_UI, "org.netbeans.swing.laf.flatlaf.ui.FlatViewTabDisplayerUI", // NOI18N

            EDITOR_TAB_CONTENT_BORDER, DPISafeBorder.matte(0, 1, 1, 1, UIManager.getColor("TabbedContainer.editor.contentBorderColor")), // NOI18N
            VIEW_TAB_CONTENT_BORDER, DPISafeBorder.matte(0, 1, 1, 1, UIManager.getColor("TabbedContainer.view.contentBorderColor")), // NOI18N

            // scale on Java 8 and Linux
            SPLIT_PANE_DIVIDER_SIZE_VERTICAL, UIScale.scale(4),
            SPLIT_PANE_DIVIDER_SIZE_HORIZONTAL, UIScale.scale(4),
        };
    }
}
