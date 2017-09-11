/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.languages.dataobject;

import java.util.Map;
import javax.swing.text.Document;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.TextAction;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;

import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.features.AnnotationManager;
import org.netbeans.modules.languages.features.BraceCompletionDeleteAction;
import org.netbeans.modules.languages.features.BraceCompletionInsertAction;
import org.netbeans.modules.languages.features.InstantRenameAction;
import org.netbeans.modules.languages.features.MarkOccurrencesSupport;
import org.netbeans.modules.languages.features.CollapseFoldTypeAction;
import org.netbeans.modules.languages.features.ExpandFoldTypeAction;
import org.netbeans.modules.languages.features.HyperlinkListener;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.settings.storage.spi.StorageFilter;
import org.netbeans.modules.editor.settings.storage.spi.TypedValue;
import org.netbeans.modules.languages.features.DatabaseManager;
import org.netbeans.modules.languages.features.LanguagesGenerateFoldPopupAction;
import org.netbeans.modules.languages.features.SyntaxErrorHighlighter;
import org.netbeans.modules.languages.parser.Pattern;


/**
 *
 * @author Jan Jancura
 */
public class LanguagesEditorKit extends NbEditorKit {

    private final String mimeType;
    
    /** 
     * Creates a new instance of LanguagesEditorKit 
     */
    public LanguagesEditorKit (String mimeType) { 
        this.mimeType = mimeType;
        if (mimeType == null) {
            throw new NullPointerException ();
        }
    }
    
    protected @Override Action[] createActions() {
        Action[] myActions = new Action[] {
            new BraceCompletionInsertAction (),
            new BraceCompletionDeleteAction (),
            //new IndentAction (),
            new InstantRenameAction(),
            new LanguagesGenerateFoldPopupAction (),
            new org.netbeans.modules.languages.features.ToggleCommentAction(),
            new org.netbeans.modules.languages.features.CodeCommentAction(),
            new org.netbeans.modules.languages.features.CodeUncommentAction()
        };
        return TextAction.augmentList (
            super.createActions (), 
            myActions
        );
    }
    
    public @Override Action getActionByName(String name) {
        if (name == null)
            return super.getActionByName (name);
        if (name.startsWith(LanguagesGenerateFoldPopupAction.EXPAND_PREFIX)) {
            name = name.substring(LanguagesGenerateFoldPopupAction.EXPAND_PREFIX.length(), name.length());
            return new ExpandFoldTypeAction (name);
        }
        if (name.startsWith(LanguagesGenerateFoldPopupAction.COLLAPSE_PREFIX)) {
            name = name.substring(LanguagesGenerateFoldPopupAction.COLLAPSE_PREFIX.length(), name.length());
            return new CollapseFoldTypeAction (name);
        }
        return super.getActionByName (name);
    }
    
    public @Override Document createDefaultDocument() {
        Document doc = new LanguagesDocument(mimeType);
        initDocument (doc);
        return doc;
    }
    
    protected void initDocument (Document doc) {
        new AnnotationManager (doc);
        new SyntaxErrorHighlighter (doc);
        new DatabaseManager (doc);
    }
    
//    public Syntax createSyntax (Document doc) {
//        LanguagesSyntax syntax = (LanguagesSyntax) documentToSyntax.get (doc);
//        if (syntax == null) {
//            syntax = new LanguagesSyntax (doc);
//            documentToSyntax.put (doc, syntax);
//            syntax.init ();
//        }
//        return syntax;
//    }

// Not neccessary, PlainSyntax is delivered by default, braces matching is done
// through the new SPI
//    public Syntax createSyntax(Document doc) {
//        return new PlainSyntax();
//    }
//
//    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
//        return new BraceHighlighting (doc);
//    }
//    
    public @Override void install (JEditorPane c) {
        super.install (c);
        HyperlinkListener hl = new HyperlinkListener ();
        c.addMouseMotionListener (hl);
        c.addMouseListener (hl);
        c.addKeyListener(hl);
        c.addCaretListener (new MarkOccurrencesSupport (c));
    }

    public @Override String getContentType() {
        return mimeType;
    }
    
    public @Override Object clone () {
        return new LanguagesEditorKit (mimeType);
    }

    private static final class LanguagesDocument extends NbEditorDocument {
        
        public LanguagesDocument(String mimeType) {
            super(mimeType);
        }

        public @Override boolean isIdentifierPart(char ch) {
            try {
                String mimeType = (String) getProperty("mimeType"); //NOI18N
                Language language = LanguagesManager.getDefault ().getLanguage (mimeType);
                Feature f = language.getFeatureList ().getFeature ("SELECTION"); //NOI18N
                if (f != null) {
                    Pattern pat = f.getPattern();
                    if (pat != null) {
                        StringBuffer buf = new StringBuffer();
                        buf.append(ch);
                        return pat.matches(buf.toString());
                    }
                }
            } catch (LanguageDefinitionNotFoundException e) {
            }
            return super.isIdentifierPart(ch);
        }
    } // End of LanguagesDocument class
    
    public static final class EditorSettings extends StorageFilter<String, TypedValue> {
        public EditorSettings() {
            super("Preferences"); //NOI18N
        }

        // -----------------------------------------------------------------------
        // StorageFilter implementation
        // -----------------------------------------------------------------------

        @Override
        public void afterLoad(Map<String, TypedValue> map, MimePath mimePath, String profile, boolean defaults) {
            if (mimePath.size() == 1) {
                if (LanguagesManager.getDefault().isSupported(mimePath.getPath())) {
                    // this is a Schliemann language

                    if (!map.containsKey(SimpleValueNames.CODE_FOLDING_ENABLE)) {
                        map.put(SimpleValueNames.CODE_FOLDING_ENABLE, new TypedValue("true", Boolean.class.getName())); //NOI18N
                    }
                }
            }
        }

        @Override
        public void beforeSave(Map<String, TypedValue> map, MimePath mimePath, String profile, boolean defaults) {
            // save everything
        }
    } // End of EditorSettings class
}

