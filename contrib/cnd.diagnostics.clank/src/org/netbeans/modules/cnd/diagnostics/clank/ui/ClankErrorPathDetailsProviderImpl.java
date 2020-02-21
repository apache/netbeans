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
package org.netbeans.modules.cnd.diagnostics.clank.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.cnd.diagnostics.clank.ui.codesnippet.ClankDiagnosticsDetailsTopComponent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.clang.tools.services.ClankDiagnosticInfo;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.diagnostics.clank.ClankCsmErrorInfo;
import org.netbeans.modules.cnd.diagnostics.clank.ClankErrorPathDetailsProvider;
import org.netbeans.modules.cnd.diagnostics.clank.impl.ClankCsmErrorInfoAccessor;
import org.netbeans.modules.cnd.diagnostics.clank.ui.tooltip.ToolTipUI;
import org.netbeans.modules.cnd.diagnostics.clank.ui.tooltip.ViewFactory;
import org.netbeans.modules.cnd.diagnostics.clank.ui.views.DiagnosticsAnnotationProvider;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = ClankErrorPathDetailsProvider.class, position = 1000)
public class ClankErrorPathDetailsProviderImpl implements ClankErrorPathDetailsProvider {

    @Override
    public void implement(final ClankCsmErrorInfo errorInfo) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DiagnosticsAnnotationProvider.setCurrentDiagnostic( ClankCsmErrorInfoAccessor.getDefault().getDelegate(errorInfo));
                final ClankDiagnosticsDetailsTopComponent details = ClankDiagnosticsDetailsTopComponent.findInstance();
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            CsmFile csmErrorFile = ClankCsmErrorInfoAccessor.getDefault().getCsmFile(errorInfo);                            
                            FileSystem fSystem = csmErrorFile.getFileObject().getFileSystem();
                            DiagnosticsAnnotationProvider.clearAll();
                            final ClankDiagnosticInfo problem = ClankCsmErrorInfoAccessor.getDefault().getDelegate(errorInfo);
                            pinCreation(fSystem, problem, details);
                            for (ClankDiagnosticInfo note : problem.notes()) {
                                pinCreation(fSystem, note, details);
                            }
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    details.setData(errorInfo);
                                    details.open();
                                    details.requestActive();                                                                        
                                }
                            });
                            
                        } catch (FileStateInvalidException | BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                    }

                }
                );

            }

        }
        );

    }

    private void pinCreation(FileSystem fSystem, ClankDiagnosticInfo note, final PropertyChangeListener listener ) throws BadLocationException {
        FileObject fo = CndFileUtils.toFileObject(fSystem, note.getSourceFileName());
        CsmFile csmNoteFile = CsmUtilities.getCsmFile(fo, true, false);
        JTextComponent textComponent = EditorRegistry.findComponent(CsmUtilities.getDocument(csmNoteFile));
        JEditorPane editorPane = null;
        //JEditorPane editorPane =
        if (textComponent instanceof JEditorPane) {
            editorPane = ((JEditorPane) textComponent);
        }
        final JEditorPane ep = editorPane;
        Document document = ep.getDocument();
        if (ep == null) {
            return;
        }
        int offset = note.getEndOffsets()[0];
        if (document instanceof LineDocument) {
            offset = LineDocumentUtils.getNextNonWhitespace((LineDocument) document, offset);
        }
        final int locOffset = offset;
        final int[] lineColumnByOffset = CsmFileInfoQuery.getDefault().getLineColumnByOffset(csmNoteFile, note.getStartOffsets()[0]);
        //find last symbol at the line
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                EditorUI eui = Utilities.getEditorUI(ep);
                if (eui == null) {
                    return;
                }
                ToolTipUI noteTooltip = ViewFactory.getDefault().createToolTip(note.getMessage(),
                        null,
                        new ToolTipUI.Pinnable(note.getMessage(), lineColumnByOffset[0] - 1, note),
                        listener);
                noteTooltip.pin(ep, locOffset);
            }
        });
    }
}
