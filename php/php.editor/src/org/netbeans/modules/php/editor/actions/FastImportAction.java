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
