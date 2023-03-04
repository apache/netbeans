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
package org.netbeans.api.editor.fold.ui;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.fold.ui.CodeFoldingSideBar;
import org.netbeans.spi.editor.SideBarFactory;

/**
 *
 * @author
 * sdedic
 */
public class FoldingUISupport {
    private FoldingUISupport() {}
    
    /**
     * Creates a component for the code folding sidebar.
     * Returns a standard folding sidebar component, which displays code folds. This sidebar implementation 
     * can be created only once per text component.
     *
     * @param textComponent the text component which should work with the Sidebar
     * @return Sidebar instance.
     */
    public static JComponent sidebarComponent(JTextComponent textComponent) {
        return new CodeFoldingSideBar(textComponent);
    }

    private static SideBarFactory FACTORY = null;

    /**
     * Obtains an instance of folding sidebar factory. This method should
     * be used in layer, in the MIME lookup area, to register a sidebar with
     * an editor for a specific MIMEtype.
     * <p/>
     * There's a default sidebar instance registered for all MIME types. 
     *
     * @return shared sidebar factory instance
     */
    public static SideBarFactory foldingSidebarFactory() {
        if (FACTORY != null) {
            return FACTORY;
        }
        return FACTORY = new CodeFoldingSideBar.Factory();
    }

    public static void disableCodeFoldingSidebar(JTextComponent text) {
        text.putClientProperty(CodeFoldingSideBar.PROP_SIDEBAR_MARK, true);
    }
}
