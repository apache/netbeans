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

package org.netbeans.modules.editor.impl;

import java.util.ResourceBundle;
import java.awt.*;
import java.util.List;
import java.util.List;
import org.openide.util.NbBundle;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.lib2.EditorImplementationProvider;
import org.openide.windows.TopComponent;

/** 
 * 
 * @author Vita Stejskal
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.editor.lib2.EditorImplementationProvider.class)
public final class NbEditorImplementationProvider implements EditorImplementationProvider {

    private static final Action [] NO_ACTIONS = new Action[0];
    
    public NbEditorImplementationProvider() {
        
    }
    
    /** Ask NbBundle for the resource bundle */
    public ResourceBundle getResourceBundle(String localizer) {
        return NbBundle.getBundle(localizer);
    }
    
    public Action[] getGlyphGutterActions(JTextComponent target) {
        String mimeType = NbEditorUtilities.getMimeType(target);
        if (mimeType != null) {
            List actions = GlyphGutterActionsProvider.getGlyphGutterActions(mimeType);
            return (Action []) actions.toArray(new Action [0]);
        } else {
            return NO_ACTIONS;
        }
    }
    
    public boolean activateComponent(JTextComponent c) {
        Container container = SwingUtilities.getAncestorOfClass(TopComponent.class, c);
        if (container != null) {
            ((TopComponent)container).requestActive();
            return true;
        }
        return false;
    }
}
