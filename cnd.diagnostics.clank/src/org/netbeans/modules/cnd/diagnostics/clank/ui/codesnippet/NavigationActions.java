/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
