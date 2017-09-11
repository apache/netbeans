/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor;

import java.net.URL;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.csl.spi.support.ModificationResult.Difference;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.html.editor.refactoring.ExtractInlinedStyleRefactoringPlugin;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.spi.lexer.MutableTextInput;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class HtmlSourceUtils {
    
    /**
     * Causes the file to reindexed. 
     * 
     * If the file is opened in the editor,  editor content will be used 
     * instead of the file content.
     * 
     * @param file 
     */
    public static void forceReindex(FileObject file) {
//        FileObject parent = file.getParent();
//        
//        URL fileURL = URLMapper.findURL(file, URLMapper.EXTERNAL);
//        URL parentURL = URLMapper.findURL(parent, URLMapper.EXTERNAL);
//        
//        IndexingManager.getDefault().refreshIndex(
//                parentURL,
//                Collections.<URL>singleton(fileURL),
//                true,
//                true);
    }
    
    /**
     * Forces to rebuild the document's {@link TokenHierarchy}.
     * 
     * @param doc a swing document
     */
    public  static void rebuildTokenHierarchy(final Document doc) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                NbEditorDocument nbdoc = (NbEditorDocument) doc;
                nbdoc.runAtomic(new Runnable() {
                    @Override
                    public void run() {
                        MutableTextInput mti = (MutableTextInput) doc.getProperty(MutableTextInput.class);
                        if (mti != null) {
                            mti.tokenHierarchyControl().rebuild();
                        }
                    }
                });
            }
        });
    }
     
    
    /**
     * Adds a link to the given stylesheet {@link FileObject}
     * 
     * @param modificationResult
     * @param result
     * @param targetFile
     * @return true if the import was successful
     */
    public static boolean importStyleSheet(ModificationResult modificationResult, 
            HtmlParserResult result, 
            FileObject targetFile) {
        
        try {
            //create a new html link to the stylesheet
            final AtomicInteger insertPositionRef = new AtomicInteger(-1);
            final AtomicBoolean increaseIndent = new AtomicBoolean();
            final AtomicBoolean isLinkTagEmpty = new AtomicBoolean();

            //jsf hack - we need to put the generated <link/> or <style/> sections to the proper place,
            //which is <h:head> tag in case of JSF. Ideally there should be an SPI which the frameworks
            //would implement and which would provide a default places for such elements.
            Node jsfHtmlLibRoot = result.root("http://java.sun.com/jsf/html"); //NOI18N
            if (jsfHtmlLibRoot != null) {
                ElementUtils.visitChildren(jsfHtmlLibRoot, new ElementVisitor() {

                    @Override
                    public void visit(Element node) {
                        OpenTag tag = (OpenTag)node;
                        //assume <h:head>
                        if (LexerUtils.equals("head",tag.unqualifiedName(), false, false)) { //NOI18N
                            //append the section as first head's child if there are
                            //no existing link attribute
                            insertPositionRef.set(node.to()); //end of the open tag offset
                            increaseIndent.set(true);
                        }
                    }
                }, ElementType.OPEN_TAG);

            }

            Node root = result.root();
            ElementUtils.visitChildren(root, new ElementVisitor() {

                @Override
                public void visit(Element node) {
                    OpenTag tag = (OpenTag)node;
                    CharSequence name = tag.name();
                    if(LexerUtils.equals("html", name, true, true)) {
                        if(insertPositionRef.get() == -1) { //h:head already found?
                            //append the section as first html's child if there are
                            //no existing link attribute and head tag
                            insertPositionRef.set(node.to()); //end of the open tag offset
                            increaseIndent.set(true);
                        }
                    } else if (LexerUtils.equals("head", name, true, true)) { //NOI18N
                        //append the section as first head's child if there are
                        //no existing link attribute
                        insertPositionRef.set(node.to()); //end of the open tag offset
                        increaseIndent.set(true);
                    } else if (LexerUtils.equals("link", name, true, true)) {
                        //NOI18N
                        //existing link => append the new section after the last one
                        insertPositionRef.set(tag.semanticEnd()); //end of the end tag offset
                        increaseIndent.set(false);
                        isLinkTagEmpty.set(tag.isEmpty());
                    }
                }
            }, ElementType.OPEN_TAG);
            int embeddedInsertOffset = insertPositionRef.get();
            if (embeddedInsertOffset == -1) {
                //TODO probably missing head tag? - generate? html tag may be missing as well
                return false;
            }
            int insertOffset = result.getSnapshot().getOriginalOffset(embeddedInsertOffset);
            if (insertOffset == -1) {
                return false; //cannot properly map back
            }
            int baseIndent = Utilities.getRowIndent((BaseDocument) result.getSnapshot().getSource().getDocument(true), insertOffset);
            if (baseIndent == -1) {
                //in case of empty line
                baseIndent = 0;
            }
            
            Document document = result.getSnapshot().getSource().getDocument(true);
            if (increaseIndent.get()) {
                //add one indent level (after HEAD open tag)
                baseIndent += IndentUtils.indentLevelSize(document);
            }

            //generate the embedded id selector section
            FileObject file = result.getSnapshot().getSource().getFileObject();
            String baseIndentString = IndentUtils.createIndentString(document, baseIndent);
            String linkRelativePath = WebUtils.getRelativePath(file, targetFile);
            String linkText = new StringBuilder().append('\n').
                    append(baseIndentString).
                    append("<link rel=\"stylesheet\" href=\"").
                    append(linkRelativePath).
                    append("\" type=\"text/css\"").
                    append(isLinkTagEmpty.get() ? "" : "/").
                    append(">\n").toString(); //NOI18N

            CloneableEditorSupport editor = GsfUtilities.findCloneableEditorSupport(file);
            Difference diff = new Difference(Difference.Kind.INSERT,
                    editor.createPositionRef(insertOffset, Bias.Forward),
                    editor.createPositionRef(insertOffset, Bias.Backward),
                    null,
                    linkText,
                    NbBundle.getMessage(ExtractInlinedStyleRefactoringPlugin.class, "MSG_InsertStylesheetLink")); //NOI18N

            modificationResult.addDifferences(file, Collections.singletonList(diff));

            return true;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        return false;

    }
}
