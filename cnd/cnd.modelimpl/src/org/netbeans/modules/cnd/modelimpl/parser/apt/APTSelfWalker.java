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
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.APTAbstractWalker;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTFileCacheEntry;
import org.netbeans.modules.cnd.apt.support.APTFileCacheManager;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler.IncludeState;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTWalker;
import org.netbeans.modules.cnd.apt.support.PostIncludeData;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.openide.filesystems.FileObject;

/**
 * APT Walker which only gathers macromap. Shouldn't be used directly but
 * only overridden by walkers which don't need to gather any info in the includes
 * just want macromap from them.
 * Also this walker holds plumbing code for blocks gathering due to it's being
 * used only for semantic highlighting walkers. This should be refactored out if
 * more uses for SelfWalking would appear.
 * 
 */
public class APTSelfWalker extends APTAbstractWalker {

    protected APTSelfWalker(APTFile apt, PreprocHandler preprocHandler, APTFileCacheEntry cacheEntry) {
        super(apt, preprocHandler, cacheEntry);
    }
    
    @Override
    protected boolean include(ResolvedPath resolvedPath, IncludeState inclState, APTInclude aptInclude, PostIncludeData postIncludeState) {
        if (inclState == IncludeState.Success) {
            CharSequence path = resolvedPath.getPath();
            FileObject fileObject = resolvedPath.getFileObject();
            if (fileObject != null) {
                try {
                    PreprocHandler preprocHandler = getPreprocHandler();
                    APTFile apt = APTDriver.findAPTLight(ModelSupport.createFileBuffer(fileObject), APTHandlersSupport.getAPTFileKind(preprocHandler));
                    APTFileCacheEntry cache = APTFileCacheManager.getInstance(resolvedPath.getFileSystem()).getEntry(path, preprocHandler.getState(), null);
                    createIncludeWalker(apt, this, path, cache).visit();
                    // does not remember walk to safe memory
                    // APTFileCacheManager.setAPTCacheEntry(resolvedPath.getPath(), preprocHandler, cache, false);
                } catch (FileNotFoundException ex) {
                    APTUtils.LOG.log(Level.WARNING, "APTSelfWalker: file {0} not found", new Object[] {path});// NOI18N
                    DiagnosticExceptoins.register(ex);
                } catch (IOException ex) {
                    APTUtils.LOG.log(Level.SEVERE, "APTSelfWalker: error on including {0}:\n{1}", new Object[] {path, ex});
                    DiagnosticExceptoins.register(ex);
                }
            }
            return (postIncludeState == null) || !postIncludeState.hasPostIncludeMacroState();
        }
        return false;
    }
    
    protected APTWalker createIncludeWalker(APTFile apt, APTSelfWalker parent, CharSequence includePath, APTFileCacheEntry cache) {
        return new APTSelfWalker(apt, parent.getPreprocHandler(), cache) {

            @Override
            protected boolean isStopped() {
                return super.isStopped() || APTSelfWalker.this.isStopped();
            }
       };
    }

    @Override
    protected boolean hasIncludeActionSideEffects() {
        return false;
    }
}

