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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.modelimpl.parser.apt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.APTAbstractWalker;
import org.netbeans.modules.cnd.apt.support.APTFileCacheEntry;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler.IncludeState;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.PostIncludeData;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * base walker to visit project files based APTs
 */
public abstract class APTProjectFileBasedWalker extends APTAbstractWalker {
    private final FileImpl file;
    private final ProjectBase startProject;
    private int mode;
    
    public APTProjectFileBasedWalker(ProjectBase startProject, APTFile apt, FileImpl file, PreprocHandler preprocHandler, APTFileCacheEntry cacheEntry) {
        super(apt, preprocHandler, cacheEntry);
        this.mode = ProjectBase.GATHERING_MACROS;
        this.file = file;
        this.startProject = startProject;
        assert startProject != null : "null start project for " + file.getAbsolutePath();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of abstract methods
    
    @Override
    protected boolean include(ResolvedPath resolvedPath, IncludeState pushIncludeState, APTInclude aptInclude, PostIncludeData postIncludeState) {
        FileImpl included = null;
        boolean error = false;
        if (pushIncludeState == IncludeState.Success) {
            CharSequence path = resolvedPath.getPath();
            ProjectBase aStartProject = this.getStartProject();
            if (aStartProject != null) {
                if (aStartProject.isValid()) {
                    ProjectBase inclFileOwner = aStartProject.getLibraryManager().resolveFileProjectOnInclude(aStartProject, getFile(), resolvedPath);
                    if (inclFileOwner == null) {
                        if (aStartProject.getFileSystem() == resolvedPath.getFileSystem()) {
                            inclFileOwner = aStartProject;
                        } else {
                            return false;
                        }
                    }
                    if (CndUtils.isDebugMode()) {
                        CndUtils.assertTrue(inclFileOwner.getFileSystem() == resolvedPath.getFileSystem(), "Different FS for " + path + ": " + inclFileOwner.getFileSystem() + " vs " + resolvedPath.getFileSystem()); // NOI18N
                    }
                    try {
                        PreprocHandler.State stateBefore = getPreprocHandler().getState();
                        assert !stateBefore.isCleaned();
                        included = includeAction(inclFileOwner, path, mode, aptInclude, postIncludeState);
                        if (/*true?*/TraceFlags.PARSE_HEADERS_WITH_SOURCES) {
                            if (included != null) {
                                putNodeProperty(aptInclude, PreprocHandler.State.class, stateBefore);
                                putNodeProperty(aptInclude, CsmFile.class, included);
                            }
                        }
                    } catch (FileNotFoundException ex) {
                        APTUtils.LOG.log(Level.WARNING, "APTProjectFileBasedWalker: file {0} not found", new Object[]{path});// NOI18N
                        DiagnosticExceptoins.register(ex);
                    } catch (IOException ex) {
                        APTUtils.LOG.log(Level.SEVERE, "APTProjectFileBasedWalker: error on including {0}:\n{1}", new Object[]{path, ex});
                        DiagnosticExceptoins.register(ex);
                    }
                }
            } else {
                APTUtils.LOG.log(Level.SEVERE, "APTProjectFileBasedWalker: file {0} without project!!!", new Object[]{file});// NOI18N
            }
        } else {
            // Recursive or failed include
            error = true;
        }
        postInclude(aptInclude, included, pushIncludeState);
        return ((postIncludeState == null) || !postIncludeState.hasPostIncludeMacroState()) && !error;
    }
    
    abstract protected FileImpl includeAction(ProjectBase inclFileOwner, CharSequence inclPath, int mode, APTInclude apt, PostIncludeData postIncludeState) throws IOException;

    protected void postInclude(APTInclude apt, FileImpl included, IncludeState includeState) {
    }
    
    protected FileImpl getFile() {
        return this.file;
    }

    protected ProjectBase getStartProject() {
	return this.startProject;
    }
    
    protected void setMode(int mode) {
        this.mode = mode;
    }
    
}
