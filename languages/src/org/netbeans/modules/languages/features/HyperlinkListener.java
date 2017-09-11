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

package org.netbeans.modules.languages.features;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.text.StyledDocument;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.languages.ASTItem;

import org.netbeans.api.languages.Highlighting.Highlight;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.Highlighting;
import org.netbeans.api.languages.database.DatabaseContext;
import org.netbeans.api.languages.database.DatabaseUsage;
import org.netbeans.api.languages.database.DatabaseDefinition;
import org.netbeans.api.languages.database.DatabaseItem;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.ParserManagerImpl;

import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.text.NbDocument;


/**
 *
 * @author Administrator
 */
public class HyperlinkListener implements MouseMotionListener, MouseListener,
    KeyListener {

    private Highlight   highlight;
    private Runnable    runnable = null;

    public void mouseMoved (MouseEvent e) {
        JEditorPane c = (JEditorPane) e.getComponent ();
        final NbEditorDocument doc = (NbEditorDocument) c.getDocument ();
        if (highlight != null) highlight.remove ();
        highlight = null;
        runnable = null;
        if (((e.getModifiers() | e.getModifiersEx()) & InputEvent.CTRL_DOWN_MASK) != InputEvent.CTRL_DOWN_MASK) {
            return;
        }

        int offset = c.viewToModel (e.getPoint ());
        highlight (doc, offset);
        c.repaint ();
    }
    
    public void mouseReleased (MouseEvent e) {
        if (runnable != null) {
            runnable.run ();
            runnable = null;
        }
    }

    public void keyReleased(KeyEvent e) {
        if (!e.isControlDown()) {
            if (highlight != null) {
                highlight.remove();
                highlight = null;
            }
            runnable = null;
        }
    }
    
    public void mouseClicked (MouseEvent e) {}
    public void mousePressed (MouseEvent e) {}
    public void mouseExited (MouseEvent e) {}
    public void mouseEntered (MouseEvent e) {}
    public void mouseDragged (MouseEvent e) {}
    public void keyPressed(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
    
    private void highlight (
        final NbEditorDocument  document,
        int                     offset
    ) {
        try {
            ASTNode ast = ParserManagerImpl.getImpl (document).getAST ();
            if (ast == null) {
                String mimeType = (String) document.getProperty ("mimeType");
                TokenHierarchy tokenHierarchy = TokenHierarchy.get (document);
                document.readLock ();
                try {
                    TokenSequence tokenSequence = tokenHierarchy.tokenSequence ();
                    tokenSequence.move (offset);
                    tokenSequence.moveNext ();
                    Language language = LanguagesManager.getDefault ().getLanguage (mimeType);
                    Token token = tokenSequence.token ();
                    if (token == null) return;
                    Feature hyperlinkFeature = language.getFeatureList ().getFeature 
                        ("HYPERLINK", token.id ().name ());
                    if (hyperlinkFeature == null) return;
                    ASTToken stoken = ASTToken.create (
                        language,
                        token.id ().ordinal (),
                        token.text ().toString (),
                        tokenSequence.offset ()
                    );
                    highlight = Highlighting.getHighlighting (document).highlight (
                        tokenSequence.offset (),
                        tokenSequence.offset () + token.length (),
                        getHyperlinkAS ()
                    );
                    runnable = (Runnable) hyperlinkFeature.getValue (Context.create (document, offset));
                } finally {
                    document.readUnlock ();
                }
                return;
            }
            ASTPath path = ast.findPath (offset);
            if (path == null) return;
            int i, k = path.size ();
            for (i = 0; i < k; i++) {
                ASTPath p = path.subPath (i);
                Language language = (Language) p.getLeaf ().getLanguage ();
                if (language == null) continue;
                Feature hyperlinkFeature = language.getFeatureList ().getFeature ("HYPERLINK", p);
                if (hyperlinkFeature == null) continue;
                highlight = Highlighting.getHighlighting (document).highlight (
                    p.getLeaf ().getOffset (),
                    p.getLeaf ().getEndOffset (),
                    getHyperlinkAS ()
                );
                runnable = (Runnable) hyperlinkFeature.getValue (SyntaxContext.create (document, p));
            }
            DatabaseContext root = DatabaseManager.getRoot (ast);
            if (root != null) {
                final DatabaseItem item = root.getDatabaseItem (offset);
                if (item != null && item instanceof DatabaseUsage) {
                    highlight = Highlighting.getHighlighting (document).highlight (
                        path.getLeaf ().getOffset (),
                        path.getLeaf ().getEndOffset (),
                        getHyperlinkAS ()
                    );
                    runnable = new Runnable () {
                        public void run () {
                            DatabaseDefinition definition = ((DatabaseUsage) item).getDefinition();
                            int offset = definition.getOffset();
                            DataObject dobj = null;
                            StyledDocument docToGo = null;
                            URL url = definition.getSourceFileUrl();
                            if (url == null) {
                                dobj = NbEditorUtilities.getDataObject(document);
                                docToGo = document;
                            } else {
                                File file = null;
                                try {
                                    file = new File(url.toURI());
                                } catch (URISyntaxException ex) {
                                    ex.printStackTrace();
                                }

                                if (file != null && file.exists()) {
                                    /** convert file to an uni absolute pathed file (../ etc will be coverted) */
                                    file = FileUtil.normalizeFile(file);
                                    FileObject fobj = FileUtil.toFileObject(file);
                                    try {
                                        dobj = DataObject.find(fobj);
                                    } catch (DataObjectNotFoundException ex) {
                                        ex.printStackTrace();
                                    }
                                    if (dobj != null) {
                                        Node nodeOfDobj = dobj.getNodeDelegate();
                                        EditorCookie ec = nodeOfDobj.getCookie(EditorCookie.class);
                                        try {
                                            docToGo = ec.openDocument();
                                        } catch (IOException ex) {
                                            ex.printStackTrace();
                                        }
                                    }

                                }
                            }

                            if (dobj == null) {
                                return;
                            }

                            LineCookie lc = (LineCookie) dobj.getCookie(LineCookie.class);
                            Line.Set lineSet = lc.getLineSet();
                            Line line = lineSet.getCurrent(NbDocument.findLineNumber(docToGo, offset));
                            int column = NbDocument.findLineColumn(docToGo, offset);
                            line.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS, column);
                        }
                    };
                }
                if (item == null) {
                    FileObject fileObject = NbEditorUtilities.getFileObject (document);
                    if (fileObject != null) {
                        ASTItem leaf = path.getLeaf ();
                        if (!(leaf instanceof ASTToken)) return;
                        String name = ((ASTToken) leaf).getIdentifier ();
                        try {
                            Map<FileObject,List<DatabaseDefinition>> map = Index.getGlobalItem (fileObject, name, false);
                            if (!map.isEmpty ()) {
                                final FileObject fo = map.keySet ().iterator ().next ();
                                final DatabaseDefinition definition = map.get (fo).iterator ().next ();
                                highlight = Highlighting.getHighlighting (document).highlight (
                                    path.getLeaf ().getOffset (),
                                    path.getLeaf ().getEndOffset (),
                                    getHyperlinkAS ()
                                );
                                runnable = new Runnable () {
                                    public void run () {
                                        int definitionOffset = definition.getOffset ();
                                        try {
                                            DataObject dobj = DataObject.find (fo);
                                            EditorCookie ec = dobj.getCookie (EditorCookie.class);
                                            StyledDocument doc2 = ec.openDocument ();
                                            LineCookie lc = dobj.getCookie (LineCookie.class);
                                            Line.Set lineSet = lc.getLineSet ();
                                            Line line = lineSet.getCurrent (NbDocument.findLineNumber (doc2, definitionOffset));
                                            int column = NbDocument.findLineColumn (doc2, definitionOffset);
                                            line.show (ShowOpenType.OPEN, ShowVisibilityType.FOCUS, column);
                                        } catch (IOException ex) {
                                            ex.printStackTrace ();
                                        }
                                    }
                                };
                            }
                        } catch (FileNotParsedException ex) {
                        }
                    }
                }
            }
        } catch (ParseException ex) {
        }
        return;
    }
    
    private static AttributeSet hyperlinkAS = null;
    
    private static AttributeSet getHyperlinkAS () {
        if (hyperlinkAS == null) {
            SimpleAttributeSet as = new SimpleAttributeSet ();
            as.addAttribute (StyleConstants.Foreground, Color.blue);
            as.addAttribute (StyleConstants.Underline, Color.blue);
            hyperlinkAS = as;
        }
        return hyperlinkAS;
    }
    
    private static AttributeSet hyperlinkPressedAS = null;
    
    private static AttributeSet getHyperlinkPressedAS () {
        if (hyperlinkPressedAS == null) {
            SimpleAttributeSet as = new SimpleAttributeSet ();
            as.addAttribute (StyleConstants.Foreground, Color.red);
            as.addAttribute (StyleConstants.Underline, Color.red);
            hyperlinkPressedAS = as;
        }
        return hyperlinkPressedAS;
    }
}

