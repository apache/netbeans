/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
            Lookup.Template<IndentEngine> query = new Lookup.Template(IndentEngine.class, handle, null);
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
