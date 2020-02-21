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
