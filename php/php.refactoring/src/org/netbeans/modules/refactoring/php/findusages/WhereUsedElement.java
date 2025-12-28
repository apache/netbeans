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
package org.netbeans.modules.refactoring.php.findusages;


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position.Bias;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.refactoring.php.RefactoringUtils;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.UserQuestionException;
import org.openide.util.lookup.Lookups;

/**
 * An element in the refactoring preview list which holds information about the find-usages-match.
 *
 * @author Tor Norbye
 */

public class WhereUsedElement extends SimpleRefactoringElementImplementation {
    private final PositionBounds bounds;
    private final String displayText;
    private final FileObject parentFile;
    private final Icon icon;
    private final String name;

    private static final Logger LOGGER = Logger.getLogger(WhereUsedElement.class.getName());

    public WhereUsedElement(PositionBounds bounds, String displayText, FileObject parentFile, String name,
        OffsetRange range, Icon icon) {
        this.bounds = bounds;
        this.displayText = displayText;
        this.parentFile = parentFile;
        this.name = name;
        this.icon = icon;
    }

    @Override
    public String getDisplayText() {
        return displayText;
    }

    @Override
    public Lookup getLookup() {
        Object composite = parentFile;
        return Lookups.singleton(composite);
    }

    @Override
    public PositionBounds getPosition() {
        return bounds;
    }

    @Override
    public String getText() {
        return displayText;
    }

    @Override
    public void performChange() {
    }

    public FileObject getFile() {
        return parentFile;
    }

    @Override
    public FileObject getParentFile() {
        return getFile();
    }


    public static String extractVariableName(Variable var) {
        if (var.getName() instanceof Identifier) {
            Identifier id = (Identifier) var.getName();
            StringBuilder varName = new StringBuilder();

            if (var.isDollared()) {
                varName.append("$");
            }

            varName.append(id.getName());
            return varName.toString();
        } else if (var.getName() instanceof Variable) {
            Variable name = (Variable) var.getName();
            return extractVariableName(name);
        }

        return null;
    }

    public static WhereUsedElement create(String name, FileObject fo,  OffsetRange range, Icon icon) {
        WhereUsedElement result = null;
        int start = range.getStart();
        int end = range.getEnd();
        try {
            DataObject od = DataObject.find(fo);
            EditorCookie ec = od.getLookup().lookup(EditorCookie.class);
            if (ec != null) {
                BaseDocument bdoc = (BaseDocument) ec.openDocument();
                // I should be able to just call tree.getInfo().getText() to get cached
                // copy - but since I'm playing fast and loose with compilationinfos
                // for for example find subclasses (using a singly dummy FileInfo) I need
                // to read it here instead
                String content = bdoc.getText(0, bdoc.getLength());
                int sta = LineDocumentUtils.getLineFirstNonWhitespace(bdoc, start);

                if (sta == -1) {
                    sta = LineDocumentUtils.getLineStartOffset(bdoc, start);
                }

                int en = LineDocumentUtils.getLineLastNonWhitespace(bdoc, start);

                if (en == -1) {
                    en = LineDocumentUtils.getLineEndOffset(bdoc, start);
                } else {
                    // Last nonwhite - left side of the last char, not inclusive
                    en++;
                }

                // Sometimes the node we get from the AST is for the whole block
                // (e.g. such as the whole class), not the argument node. This happens
                // for example in Find Subclasses out of the index. In this case
                if (end > en) {
                    end = start + name.length();

                    if (end > bdoc.getLength()) {
                        end = bdoc.getLength();
                    }
                }


                StringBuilder sb = new StringBuilder();
                if (end < sta) {
                    // XXX Shouldn't happen, but I still have AST offset errors
                    sta = end;
                }
                if (start < sta) {
                    // XXX Shouldn't happen, but I still have AST offset errors
                    start = sta;
                }
                if (en < end) {
                    // XXX Shouldn't happen, but I still have AST offset errors
                    en = end;
                }
                sb.append(RefactoringUtils.getHtml(content.subSequence(sta, start).toString()));
                sb.append("<b>"); // NOI18N
                sb.append(content.subSequence(start, end));
                sb.append("</b>"); // NOI18N
                sb.append(RefactoringUtils.getHtml(content.subSequence(end, en).toString()));

                CloneableEditorSupport ces = RefactoringUtils.findCloneableEditorSupport(fo);
                PositionRef ref1 = ces.createPositionRef(start, Bias.Forward);
                PositionRef ref2 = ces.createPositionRef(end, Bias.Forward);
                PositionBounds bounds = new PositionBounds(ref1, ref2);

                result = new WhereUsedElement(bounds, sb.toString().trim(), fo, name, new OffsetRange(start, end), icon);
            }
        } catch (UserQuestionException ex) {
            LOGGER.log(Level.INFO, "Was not possible to obtain document for " + fo.getPath(), ex); //NOI18N
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }

    /**
     * @return the icon
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
}
