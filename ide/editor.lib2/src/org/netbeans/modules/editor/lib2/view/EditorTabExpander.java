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

package org.netbeans.modules.editor.lib2.view;

import java.util.logging.Logger;
import javax.swing.text.TabExpander;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;

/**
 * Tab expander of the editor.
 * <br>
 * It aligns visually (not by number of characters) since it's a desired behavior
 * for asian languages characters that visually occupy two regular characters and they should
 * be treated in that way in terms of tab aligning.
 *
 * @author Miloslav Metelka
 */

public final class EditorTabExpander implements TabExpander {

    // -J-Dorg.netbeans.modules.editor.lib2.view.EditorTabExpander.level=FINE
    private static final Logger LOG = Logger.getLogger(EditorTabExpander.class.getName());

    private final DocumentView documentView;

    private int tabSize;

    public EditorTabExpander(DocumentView documentView) {
        this.documentView = documentView;
        updateTabSize();
    }

    /* package */ void updateTabSize() {
        Integer tabSizeInteger = (Integer) documentView.getDocument().getProperty(SimpleValueNames.TAB_SIZE);
        tabSize = (tabSizeInteger != null) ? tabSizeInteger : EditorPreferencesDefaults.defaultTabSize;
    }

    @Override
    public float nextTabStop(float x, int tabOffset) {
        float defaultCharWidth = documentView.op.getDefaultCharWidth();
        int charIndex = (int) (x / defaultCharWidth);
        charIndex = (charIndex + tabSize) / tabSize * tabSize;
        return charIndex * defaultCharWidth;
    }

}
