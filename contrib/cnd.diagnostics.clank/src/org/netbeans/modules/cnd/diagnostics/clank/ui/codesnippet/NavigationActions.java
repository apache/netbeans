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
package org.netbeans.modules.cnd.diagnostics.clank.ui.codesnippet;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.clang.tools.services.ClankDiagnosticInfo;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.diagnostics.clank.ClankCsmErrorInfo;
import org.netbeans.modules.cnd.diagnostics.clank.impl.ClankCsmErrorInfoAccessor;
import org.netbeans.modules.cnd.diagnostics.clank.ui.Utilities;
import org.netbeans.modules.cnd.diagnostics.clank.ui.views.DiagnosticsAnnotationProvider;
import static org.netbeans.modules.cnd.diagnostics.clank.ui.views.DiagnosticsAnnotationProvider.DIAGNOSTIC_CHANGED;
import static org.netbeans.modules.cnd.diagnostics.clank.ui.views.DiagnosticsAnnotationProvider.setCurrentDiagnostic;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class NavigationActions {

    public static Action createNextAction(ClankDiagnosticInfo diagnosticInfo, final ClankCsmErrorInfo error, PropertyChangeListener listener) {
        final ArrayList<ClankDiagnosticInfo> notes = diagnosticInfo.getParent() == null ? diagnosticInfo.notes() : diagnosticInfo.getParent().notes();
        final int currentDiagnosticIndex = diagnosticInfo.getParent() == null ? - 1 : notes.indexOf(diagnosticInfo);

        Action nextAction = new AbstractAction("next", ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/diagnostics/clank/resources/nextmatch.png", false)) {//NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    CsmFile csmErrorFile = ClankCsmErrorInfoAccessor.getDefault().getCsmFile(error);
                    FileSystem fSystem = csmErrorFile.getFileObject().getFileSystem();

                    goTo(notes.get(currentDiagnosticIndex + 1), fSystem, listener);
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };        
        return nextAction;

    }

    public static Action createPrevAction(ClankDiagnosticInfo rootProblem) {
        return null;
    }

    private static void goTo(final ClankDiagnosticInfo diagnosticInfo, FileSystem fSystem, PropertyChangeListener listener) {
        final FileObject fo = CndFileUtils.toFileObject(fSystem, diagnosticInfo.getSourceFileName());
        CsmFile csmNoteFile = CsmUtilities.getCsmFile(fo, false, false);
        final int[] lineColumnByOffset = CsmFileInfoQuery.getDefault().getLineColumnByOffset(csmNoteFile, diagnosticInfo.getStartOffsets()[0]);
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(DiagnosticsAnnotationProvider.class, "OpeningFile"));//NOI18N
        setCurrentDiagnostic(diagnosticInfo);
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                if (fo == null) {
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(DiagnosticsAnnotationProvider.class, "CannotOpen", diagnosticInfo.getSourceFileName()));//NOI18N
                } else {
                    Utilities.show(fo, lineColumnByOffset[0]);
                    listener.propertyChange(new PropertyChangeEvent(this, DIAGNOSTIC_CHANGED, null, diagnosticInfo));
                }
            }
        });

    }

}
