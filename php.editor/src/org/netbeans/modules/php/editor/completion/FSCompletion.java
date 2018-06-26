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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.php.editor.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.beans.BeanInfo;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.editor.NavUtils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.ParenthesisExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar.Type;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * Base on code from contrib/editor.fscompletion.
 * @author Jan Lahoda
 */
public class FSCompletion implements CompletionProvider {

    public FSCompletion() {
    }

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(final CompletionResultSet resultSet, final Document doc, final int caretOffset) {
                try {
                    FileObject file = NavUtils.getFile(doc);

                    if (file == null || caretOffset == -1) {
                        return;
                    }

                    final List<FileObject> includePath = PhpSourcePath.getIncludePath(file);
                    try {
                        Source source = Source.create(file);

                        if (source == null) {
                            // the create source checks, whether the file is valid and whether is not a folder
                            // in such case returns null.
                            return;
                        }
                        ParserManager.parse(Collections.singleton(source), new UserTask() {

                            @Override
                            public void run(ResultIterator resultIterator) throws Exception {
                                Parser.Result parserResult = resultIterator.getParserResult();
                                if (parserResult instanceof ParserResult) {
                                    ParserResult parameter = (ParserResult) parserResult;
                                    List<ASTNode> path = NavUtils.underCaret(parameter, caretOffset);
                                    if (path.size() < 2) {
                                        return;
                                    }
                                    ASTNode d1 = path.get(path.size() - 1);
                                    ASTNode d2 = path.get(path.size() - 2);
                                    if (d2 instanceof ParenthesisExpression) {
                                        if (path.size() < 3) {
                                            return;
                                        }
                                        d2 = path.get(path.size() - 3);
                                    }
                                    if (!(d1 instanceof Scalar) || !(d2 instanceof Include)) {
                                        return;
                                    }
                                    Scalar s = (Scalar) d1;
                                    if (s.getScalarType() != Type.STRING || !NavUtils.isQuoted(s.getStringValue())) {
                                        return;
                                    }
                                    int startOffset = s.getStartOffset() + 1;
                                    if (startOffset > caretOffset || startOffset < 0 || caretOffset < 0) {
                                        return;
                                    }
                                    final String prefix = parameter.getSnapshot().getText().subSequence(startOffset, caretOffset).toString();
                                    List<FileObject> relativeTo = new LinkedList<>();
                                    if (!prefix.startsWith("../")) { //NOI18N
                                        relativeTo.addAll(includePath);
                                    }
                                    final PHPIncludesFilter filter = new PHPIncludesFilter(parameter.getSnapshot().getSource().getFileObject());
                                    final FileObject parent = parameter.getSnapshot().getSource().getFileObject().getParent();
                                    if (parent != null) {
                                        relativeTo.add(parent);
                                    }
                                    resultSet.addAllItems(computeRelativeItems(relativeTo, prefix, startOffset, filter));
                                }
                            }
                        });
                    } catch (ParseException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } finally {
                    resultSet.finish();
                }
            }
        }, component);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings({"DMI_HARDCODED_ABSOLUTE_FILENAME"})
    private static List<? extends CompletionItem> computeRelativeItems(
            Collection<? extends FileObject> relativeTo,
            final String prefix,
            int anchor,
            FileObjectFilter filter) throws IOException {
        final String goUp = "../";
        assert relativeTo != null;

        List<CompletionItem> result = new LinkedList<>();

        int lastSlash = prefix.lastIndexOf('/');
        String pathPrefix;
        String filePrefix;

        if (lastSlash != (-1)) {
            pathPrefix = prefix.substring(0, lastSlash);
            filePrefix = prefix.substring(lastSlash + 1);
        } else {
            pathPrefix = null;
            filePrefix = prefix;
        }

        Set<FileObject> directories = new HashSet<>();
        File prefixFile = null;
        if (pathPrefix != null && !pathPrefix.startsWith(".")) { //NOI18N
            if (pathPrefix.length() == 0 && prefix.startsWith("/")) {
                prefixFile = new File("/"); //NOI18N
            } else {
                prefixFile = new File(pathPrefix);
            }
        }
        if (prefixFile != null && prefixFile.exists()) {
            //absolute path
            File normalizeFile = FileUtil.normalizeFile(prefixFile);
            FileObject fo = FileUtil.toFileObject(normalizeFile);
            if (fo != null) {
                directories.add(fo);
            }
        } else {
            //relative path
            for (FileObject f : relativeTo) {
                if (pathPrefix != null) {
                    File toFile = FileUtil.toFile(f);
                    if (toFile != null) {
                        URI resolve = Utilities.toURI(toFile).resolve(pathPrefix).normalize();
                        File normalizedFile = FileUtil.normalizeFile(Utilities.toFile(resolve));
                        f = FileUtil.toFileObject(normalizedFile);
                    } else {
                        f = f.getFileObject(pathPrefix);
                    }
                }

                if (f != null) {
                    directories.add(f);
                }
            }
        }

        for (FileObject dir : directories) {
            FileObject[] children = dir.getChildren();

            for (int cntr = 0; cntr < children.length; cntr++) {
                FileObject current = children[cntr];

                if (VisibilityQuery.getDefault().isVisible(current) && current.getNameExt().toLowerCase().startsWith(filePrefix.toLowerCase()) && filter.accept(current)) {
                    result.add(new FSCompletionItem(current, pathPrefix != null ? pathPrefix + "/" : "./", anchor)); //NOI18N
                }
            }
        }
        if (goUp.startsWith(filePrefix) && directories.size() == 1) {
            final FileObject parent = directories.iterator().next();
            if (parent.getParent() != null && VisibilityQuery.getDefault().isVisible(parent.getParent()) && filter.accept(parent.getParent())) {
                result.add(new FSCompletionItem(parent, "", anchor) {
                    @Override
                    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
                        CompletionUtilities.renderHtml(super.icon, goUp, null, g, defaultFont, defaultColor, width, height, selected);
                    }

                    @Override
                    protected String getText() {
                        return (!prefix.equals("..") && !prefix.equals(".") ? prefix : "") + goUp; //NOI18N
                    }
                });
            }
        }

        return result;
    }

    private static class PHPIncludesFilter implements FileObjectFilter {
        private FileObject currentFile;

        public PHPIncludesFilter(FileObject currentFile) {
            this.currentFile = currentFile;
        }

        @Override
        public boolean accept(FileObject file) {
            if (file.equals(currentFile) || isNbProjectMetadata(file)) {
                return false; //do not include self in the cc result
            }

            if (file.isFolder()) {
                return true;
            }

            String mimeType = FileUtil.getMIMEType(file);

            return mimeType != null && mimeType.startsWith("text/");
        }

        private static boolean isNbProjectMetadata(FileObject fo) {
            final String metadataName = "nbproject"; //NOI18N
            if (fo.getPath().indexOf(metadataName) != -1) {
                while (fo != null) {
                    if (fo.isFolder()) {
                        if (metadataName.equals(fo.getNameExt())) {
                            return true;
                        }
                    }
                    fo = fo.getParent();
                }
            }
            return false;
        }
    }

    static class FSCompletionItem implements CompletionItem {

        private FileObject file;
        private ImageIcon  icon;
        private int        anchor;
        private String     prefix;

        public FSCompletionItem(FileObject file, String prefix, int anchor) throws IOException {
            this.file = file;

            DataObject od = DataObject.find(file);

            icon = new ImageIcon(od.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));

            this.anchor = anchor;

            this.prefix = prefix;
        }

        private void doSubstitute(final JTextComponent component, String toAdd, final int backOffset) {
            final BaseDocument doc = (BaseDocument) component.getDocument();
            final int caretOffset = component.getCaretPosition();
            final String value = getText() + (toAdd != null && (!toAdd.equals("/") || (toAdd.equals("/") && !getText().endsWith(toAdd))) ? toAdd : ""); //NOI18N
            // Update the text
            doc.runAtomic(new Runnable() {
                @Override
                public void run() {
                    try {
                        String pfx = doc.getText(anchor, caretOffset - anchor);
                        doc.remove(caretOffset - pfx.length(), pfx.length());
                        doc.insertString(caretOffset - pfx.length(), value, null);
                        component.setCaretPosition(component.getCaretPosition() - backOffset);
                    } catch (BadLocationException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            });
        }

        @Override
        public void defaultAction(JTextComponent component) {
            doSubstitute(component, null, 0);
            if (!file.isFolder()) {
                Completion.get().hideAll();
            }
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
            if (evt.getID() == KeyEvent.KEY_TYPED) {
                String strToAdd = "/";
                if (evt.getKeyChar() == '/') {
                    doSubstitute((JTextComponent) evt.getSource(), strToAdd, strToAdd.length() - 1);
                    evt.consume();
                }
            }
        }

        @Override
        public int getPreferredWidth(Graphics g, Font defaultFont) {
            return CompletionUtilities.getPreferredWidth(file.getNameExt(), null, g, defaultFont);
        }

        @Override
        public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
            CompletionUtilities.renderHtml(icon, file.getNameExt(), null, g, defaultFont, defaultColor, width, height, selected);
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return null;
        }

        @Override
        public CompletionTask createToolTipTask() {
            return null;
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false; //????
        }

        @Override
        public int getSortPriority() {
            return -1000;
        }

        @Override
        public CharSequence getSortText() {
            return getText();
        }

        @Override
        public CharSequence getInsertPrefix() {
            return getText();
        }

        protected String getText() {
            return prefix + file.getNameExt() + (file.isFolder() ? "/" : "");
        }

        @Override
        public int hashCode() {
            return getText().hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof FSCompletionItem)) {
                return false;
            }

            FSCompletionItem remote = (FSCompletionItem) o;

            return getText().equals(remote.getText());
        }

    }

    interface FileObjectFilter {

        boolean accept(FileObject file);

    }
}
