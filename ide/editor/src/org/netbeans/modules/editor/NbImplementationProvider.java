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

package org.netbeans.modules.editor;

import java.util.ResourceBundle;
import java.awt.*;
import org.netbeans.editor.ImplementationProvider;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.impl.GlyphGutterActionsProvider;
import org.netbeans.modules.editor.impl.NbEditorImplementationProvider;

/** This is NetBeans specific provider of functionality.
 * See base class for detailed comments.
 *
 * @author David Konecny
 * @since 10/2001
 * @deprecated Without any replacement.
 */
@Deprecated
public class NbImplementationProvider extends ImplementationProvider {

    public static final String GLYPH_GUTTER_ACTIONS_FOLDER_NAME = 
        GlyphGutterActionsProvider.GLYPH_GUTTER_ACTIONS_FOLDER_NAME;
    
    private transient NbEditorImplementationProvider provider;
    
    public NbImplementationProvider() {
        provider = new NbEditorImplementationProvider();
    }
    
    /** Ask NbBundle for the resource bundle */
    public ResourceBundle getResourceBundle(String localizer) {
        return provider.getResourceBundle(localizer);
    }

    public Action[] getGlyphGutterActions(JTextComponent target) {
        return provider.getGlyphGutterActions(target);
    }
    
    public boolean activateComponent(JTextComponent c) {
        return provider.activateComponent(c);
    }
}
