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
package org.netbeans.modules.php.editor.actions;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.Occurence;
import org.netbeans.modules.php.editor.model.OccurencesSupport;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.php.project.api.PhpSourcePath.FileType;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Based on the Java one by Jan Lahoda && Tor Norbye clone in python.
 *
 * @author Radek Matous
 */
public class FastImportAction extends BaseAction {

    private static final String ACTION_NAME = "fast-import";

    /** Creates a new instance of FastImportAction. */
    public FastImportAction() {
        super(ACTION_NAME);
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        try {
            final Rectangle caretRectangle = target.modelToView(target.getCaretPosition());
            final Font font = target.getFont();
            final Point where = new Point(caretRectangle.x, caretRectangle.y + caretRectangle.height);
            SwingUtilities.convertPointToScreen(where, target);
            final int position = target.getCaretPosition();
            try {
                ParserManager.parse(Collections.singleton(Source.create(target.getDocument())), new UserTask() {

                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        ParserResult info = (ParserResult) resultIterator.getParserResult();
                        if (info != null) {
                            importItem(info, where, caretRectangle, font, position);
                        }
                    }
                });
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }


        } catch (BadLocationException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    private void importItem(final ParserResult info, final Point where, final Rectangle caretRectangle, final Font font, final int position) {
        PHPParseResult result = (PHPParseResult) info;
        final Model model = result.getModel();
        OccurencesSupport occurencesSupport = model.getOccurencesSupport(position);
        Occurence occurence = occurencesSupport.getOccurence();
        if (occurence != null) {
            FileObject baseFo = info.getSnapshot().getSource().getFileObject();
            File baseFile = FileUtil.toFile(baseFo);
            File baseFolder = baseFile.getParentFile();
            final LinkedHashSet<String> privileged = new LinkedHashSet<>();
            final LinkedHashSet<String> denied = new LinkedHashSet<>();
            Collection<? extends PhpElement> allDeclarations = occurence.getAllDeclarations();
            for (PhpElement declaration : allDeclarations) {
                FileObject includedFo = declaration.getFileObject();
                File includedFile = FileUtil.toFile(includedFo);
                FileType fileType = PhpSourcePath.getFileType(includedFo);
                String relativizeFile = PropertyUtils.relativizeFile(baseFolder, includedFile);
                StringBuilder sb = new StringBuilder();
                String properRelativePath = (relativizeFile != null && relativizeFile.startsWith(".")) ? relativizeFile : "./" + relativizeFile; //NOI18N
                sb.append("\"").append(properRelativePath).append("\";"); //NOI18N
                LinkedHashSet<String> list;
                if (fileType.equals(FileType.INTERNAL)) {
                    //list = denied;
                    String elementInfo = declaration.getPhpElementKind() + " " + declaration.getName(); //NOI18N
                    StatusDisplayer.getDefault().setStatusText(
                            NbBundle.getMessage(FastImportAction.class, "MSG_NO_IMPORTS_FOR_PLATFORM", elementInfo)); //NOI18N
                    continue;
                } else if (baseFo == includedFo) {
                    String elementInfo = declaration.getPhpElementKind() + " " + declaration.getName(); //NOI18N
                    StatusDisplayer.getDefault().setStatusText(
                            NbBundle.getMessage(FastImportAction.class,
                            "MSG_NO_IMPORTS_FOR_CURRENT_FILE", elementInfo, baseFile.getAbsolutePath())); //NOI18N
                    continue;
                } else {
                    list = privileged;
                }
                list.add("require_once " + sb.toString()); //NOI18N
                list.add("require " + sb.toString()); //NOI18N
                list.add("include " + sb.toString()); //NOI18N
                list.add("include_once " + sb.toString()); //NOI18N
            }
            if (privileged.size() > 0 || denied.size() > 0) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        ImportModulePanel panel = new ImportModulePanel(new ArrayList<>(privileged), new ArrayList<>(denied), font, info, position);
                        PopupUtil.showPopup(panel, "", where.x, where.y, true, caretRectangle.height);
                    }
                });
            }
        }
    }
}
