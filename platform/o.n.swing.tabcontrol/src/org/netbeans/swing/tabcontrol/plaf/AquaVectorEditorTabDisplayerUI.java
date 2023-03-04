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
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import org.netbeans.swing.tabcontrol.TabDisplayer;

/**
 * A variation on the Aqua editor tab displayer UI that uses scalable icons for Retina screens.
 * See {@link AquaVectorTabControlIcon}.
 */
public final class AquaVectorEditorTabDisplayerUI extends AquaEditorTabDisplayerUI {
    public AquaVectorEditorTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
    }

    public static ComponentUI createUI(JComponent c) {
        return new AquaVectorEditorTabDisplayerUI((TabDisplayer) c);
    }

    @Override
    protected TabCellRenderer createDefaultRenderer() {
        return new AquaVectorEditorTabCellRenderer();
    }

    @Override
    public Icon getButtonIcon(int buttonId, int buttonState) {
        Icon ret = AquaVectorTabControlIcon.get(buttonId, buttonState);
        return ret != null ? ret : super.getButtonIcon(buttonId, buttonState);
    }
}
