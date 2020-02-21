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

import java.io.IOException;
import java.util.LinkedList;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.APTFileCacheEntry;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler.IncludeInfo;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler.IncludeState;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTWalker;
import org.netbeans.modules.cnd.apt.support.PostIncludeData;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.modelimpl.accessors.CsmCorePackageAccessor;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;

/**
 * special walker to restore preprocessor state from include stack
 */
public class APTRestorePreprocStateWalker extends APTProjectFileBasedWalker {
    private final String interestedFile;
    private final LinkedList<IncludeInfo> inclStack;
    private final APTIncludeHandler.IncludeInfo stopDirective;
    private final boolean searchInterestedFile;
    
    /** Creates a new instance of APTRestorePreprocStateWalker */
    public APTRestorePreprocStateWalker(ProjectBase base, APTFile apt, FileImpl file, PreprocHandler preprocHandler, LinkedList<IncludeInfo> inclStack, String interestedFile, APTFileCacheEntry cacheEntry) {
        super(base, apt, file, preprocHandler, cacheEntry);
        this.searchInterestedFile = true;
        this.interestedFile = interestedFile;
        this.inclStack = inclStack;
        assert (!inclStack.isEmpty());
        this.stopDirective = this.inclStack.removeLast();
        assert (stopDirective != null);
    }
    
    @Override
    protected FileImpl includeAction(ProjectBase inclFileOwner, CharSequence inclPath, int mode, APTInclude apt, PostIncludeData postIncludeState) throws IOException {
        FileImpl csmFile = null;
        boolean foundDirective = false;
        if (searchInterestedFile) {
            // check if stop directive is met
            if (super.getCurIncludeDirectiveFileIndex() == stopDirective.getIncludeDirectiveIndex()) {
                if (inclPath.equals(stopDirective.getIncludedPath())) {
                    foundDirective = true;
                } else {
                    // we restored by incorrect include stack, see comment to this.visit()
                    APTHandlersSupport.invalidatePreprocHandler(getPreprocHandler());
                    super.stop();
                    return null;
                }
            }
        }
        try {
            csmFile = inclFileOwner.getFile(inclPath, false);
            if( csmFile == null ) {
		// this might happen if the file has been just deleted from project
		return null;
	    }
            if (foundDirective) {
                // we met candidate to stop on #include directive
                assert inclStack != null;
                // look if it the real target or target is in sub-#include
                if (!inclStack.isEmpty()) {
                    // this is not the target
                    // need to continue restoring in sub-#includes
                    APTFile aptLight = APTTokenStreamProducer.getFileAPT(csmFile, false);
                    if (aptLight != null) {
                        PreprocHandler preprocHandler = getPreprocHandler();
                        // only ask for cached entry
                        APTFileCacheEntry cacheEntry = csmFile.getAPTCacheEntry(preprocHandler.getState(), null);
                        APTWalker walker = new APTRestorePreprocStateWalker(getStartProject(), aptLight, csmFile, preprocHandler, inclStack, interestedFile, cacheEntry){

                            @Override
                            protected boolean isStopped() {
                                return super.isStopped() || APTRestorePreprocStateWalker.this.isStopped();
                            }
                        };
                        walker.visit();
                        // we do not remember cache entry as serial because stopped before #include directive
                        // csmFile.setAPTCacheEntry(preprocHandler, cacheEntry, false);
                    } else {
                        // expected #included file was deleted
                        csmFile = null;
                    }
                }
            } else {
                // usual gathering macro map without check on #include directives
                APTFile aptLight = APTTokenStreamProducer.getFileAPT(csmFile, false);
                if (aptLight != null) {
                    PreprocHandler preprocHandler = getPreprocHandler();
                    // only ask for cached entry and visit with it #include directive
                    APTFileCacheEntry cacheEntry = csmFile.getAPTCacheEntry(preprocHandler.getState(), null);
                    APTWalker walker = new APTSelfWalker(aptLight, preprocHandler, cacheEntry) {

                        @Override
                        protected boolean isStopped() {
                            return super.isStopped() || APTRestorePreprocStateWalker.this.isStopped();
                        }
                        
                    };
                    walker.visit();
                    // does not remember walk info to safe memory
                    // csmFile.setAPTCacheEntry(preprocHandler, cacheEntry, false);
                } else {
                    // expected #included file was deleted
                    csmFile = null;
                }                
            }
        } finally {
            if (foundDirective) {
                // we restored everything. Time to stop
                super.stop();
            }
        }
        return csmFile;
    }

    @Override
    protected boolean hasIncludeActionSideEffects() {
        return searchInterestedFile;
    }

    @Override
    public void visit() {
        super.visit();
        if (searchInterestedFile && !super.isStopped()) {
            // For now we don't escalate changes to library headers which means 
            // you can get get obsolete preprocessor state for library headers.
            // In those cases library headers should be "presented" in their 
            // natural state with preprocessor state got from project properties only
            APTHandlersSupport.invalidatePreprocHandler(getPreprocHandler());
            // See IZ119620, IZ120478
        }
    }

    @Override
    protected boolean include(ResolvedPath resolvedPath, IncludeState inclState, APTInclude aptInclude, PostIncludeData postIncludeState) {
        boolean ret = super.include(resolvedPath, inclState, aptInclude, postIncludeState);
        // does not allow to store post include state if we stopped before #include directive
        if (isRestored()) {
            ret = false;
        }
        return ret;
    }        

    @Override
    protected void popInclude(APTInclude aptInclude, ResolvedPath resolvedPath, IncludeState pushState) {
        // do not clear includes path if restored => no popInclude
        if (!isRestored()) {
            super.popInclude(aptInclude, resolvedPath, pushState);
        }
    }
    
    private boolean isRestored() {
        return searchInterestedFile && isStopped();
    }
}
