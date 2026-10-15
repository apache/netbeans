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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.prefs.Preferences;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.lib.SettingsConversions;
import org.openide.text.IndentEngine;
import org.openide.util.Lookup;

/**
 * This class contains static methods that provide values of some deprecated settings.
 * Nobody should never need to call this class directly. There are a few places in
 * the editor infrastructure that still read these settings to preserve backwards
 * compatibility.
 * 
 * @author vita
 */
public final class ComplexValueSettingsFactory {

    private ComplexValueSettingsFactory() {
        // no-op
    }
    
    // -----------------------------------------------------------------------
    // 'rendering-hints' setting
    // -----------------------------------------------------------------------
    
    public static final Object getRenderingHintsValue(MimePath mimePath, String settingName) {
        assert settingName.equals("rendering-hints") : "The getRenderingHints factory called for '" + settingName + "'"; //NOI18N
        FontColorSettings fcs = MimeLookup.getLookup(mimePath).lookup(FontColorSettings.class);
        return fcs.getFontColors(FontColorNames.DEFAULT_COLORING).getAttribute(EditorStyleConstants.RenderingHints);
    }
    
    // -----------------------------------------------------------------------
    // 'formatter' setting
    // -----------------------------------------------------------------------

// This was moved to editor.deprecated.pre65formatting module as well as the layer preferences.xml
// which accesses this method.
//
//    public static final Object getFormatterValue(MimePath mimePath, String settingName) {
//        assert settingName.equals(NbEditorDocument.FORMATTER) : "The getFormatter factory called for '" + settingName + "'"; //NOI18N
//
//        IndentEngine eng = getIndentEngine(mimePath);
//
//        if (eng != null) {
//            if (eng instanceof FormatterIndentEngine) {
//                return ((FormatterIndentEngine)eng).getFormatter();
//            } else {
//                EditorKit kit = MimeLookup.getLookup(mimePath).lookup(EditorKit.class);
//                if (kit != null) {
//                    return new IndentEngineFormatter(kit.getClass(), eng);
//                }
//            }
//        }
//        
//        return null;
//    }

    // This is not called from a layer, but from NbEditorDocument and e.d.pre65formatting/.../ComplexValueSettingsFactory.getFormatterValue
    public static final IndentEngine getIndentEngine(MimePath mimePath) {
        IndentEngine eng = null;

        Preferences prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
        String handle = prefs.get(NbEditorDocument.INDENT_ENGINE, null);
        if (handle != null && handle.indexOf('.') == -1) { //NOI18N
            // looks like Lookup handle from previous version
            Lookup.Template<IndentEngine> query = new Lookup.Template<>(IndentEngine.class, handle, null);
            Collection<? extends IndentEngine> all = Lookup.getDefault().lookup(query).allInstances();
            if (!all.isEmpty()) {
                eng = all.iterator().next();
            }
        } else {
            eng = (IndentEngine) SettingsConversions.callFactory(prefs, mimePath, NbEditorDocument.INDENT_ENGINE, null);
        }
        
        if (eng == null) {
            EditorKit kit = MimeLookup.getLookup(mimePath).lookup(EditorKit.class);
            Object legacyFormatter = null;
            if (kit != null) {
                try {
                    Method createFormatterMethod = kit.getClass().getDeclaredMethod("createFormatter"); //NOI18N
                    legacyFormatter = createFormatterMethod.invoke(kit);
                } catch (Exception e) {
                }
            }
            if (legacyFormatter == null) {
                eng = new DefaultIndentEngine();
            }
        }

        return eng;
    }

    // -----------------------------------------------------------------------
    // 'identifier-acceptor' setting
    // -----------------------------------------------------------------------
    
    public static final Object getIdentifierAcceptorValue(MimePath mimePath, String settingName) {
        assert settingName.equals("identifier-acceptor") : "The getIdentifierAcceptorValue factory called for '" + settingName + "'"; //NOI18N
        return AcceptorFactory.LETTER_DIGIT;
    }
    
    // -----------------------------------------------------------------------
    // 'whitespace-acceptor' setting
    // -----------------------------------------------------------------------
    
    public static final Object getWhitespaceAcceptorValue(MimePath mimePath, String settingName) {
        assert settingName.equals("whitespace-acceptor") : "The getWhitespaceAcceptorValue factory called for '" + settingName + "'"; //NOI18N
        return AcceptorFactory.WHITESPACE;
    }
    
}
