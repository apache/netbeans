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
package org.netbeans.modules.refactoring.php.findusages;


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position.Bias;
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
                int sta = Utilities.getRowFirstNonWhite(bdoc, start);

                if (sta == -1) {
                    sta = Utilities.getRowStart(bdoc, start);
                }

                int en = Utilities.getRowLastNonWhite(bdoc, start);

                if (en == -1) {
                    en = Utilities.getRowEnd(bdoc, start);
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
