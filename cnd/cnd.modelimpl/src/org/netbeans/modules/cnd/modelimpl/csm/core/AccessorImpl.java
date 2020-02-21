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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.modelimpl.accessors.CsmCorePackageAccessor;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.content.project.GraphContainer;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModel;

/**
 *
 */
final class AccessorImpl extends CsmCorePackageAccessor {
    ////////////////////////////////////////////////////////////////////////////
    //  access to ModelImpl methods
    ////////////////////////////////////////////////////////////////////////////
    
    @Override
    public void notifyClosing(ModelImpl model) {
        model.notifyClosing();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //  end of access to ModelImpl methods
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    //  access to FileImpl methods
    ////////////////////////////////////////////////////////////////////////////

    @Override
    public int getErrorCount(FileImpl fileImpl) {
        return fileImpl.getErrorCount();
    }

    @Override
    public FileContent getFileContent(CsmParserProvider.CsmParserParameters descr) {
        if (descr instanceof FileImpl.ParseDescriptor) {
            return ((FileImpl.ParseDescriptor) descr).getFileContent();
        } else {
            return null;
        }
    }

    @Override
    public FileContent prepareLazyStatementParsingContent(FileImpl fileImpl) {
        return fileImpl.prepareLazyStatementParsingContent();
    }

    @Override
    public void releaseLazyStatementParsingContent(FileImpl fileImpl, FileContent tmpFileContent) {
        fileImpl.releaseLazyStatementParsingContent(tmpFileContent);
    }

    @Override
    public PreprocessorStatePair getCachedVisitedState(FileImpl csmFile, PreprocHandler.State newState) {
        return csmFile.getCachedVisitedState(newState);
    }

    @Override
    public void cacheVisitedState(FileImpl csmFile, PreprocHandler.State newState, PreprocHandler preprocHandler, FilePreprocessorConditionState pcState) {
        csmFile.cacheVisitedState(newState, preprocHandler, pcState);
    }
        
    @Override
    public void setFileImplTestHook(TraceModel.TestHook hook) {
        FileImpl.setTestHook(hook);
    }

    @Override
    public void testFileImplErrors(FileImpl file, TraceModel.ErrorListener errorListener) {
        file.testErrors(errorListener);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //  end of access to ProjectBase methods
    ////////////////////////////////////////////////////////////////////////////

    public AccessorImpl() {
    }

    ////////////////////////////////////////////////////////////////////////////
    //  end of access to FileImpl methods
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    //  access to ProjectBase methods
    ////////////////////////////////////////////////////////////////////////////

    @Override
    public GraphContainer getGraph(ProjectBase project) {
        return project.getGraph();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //  end of access to ProjectBase methods
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    //  access to FilePreprocessorConditionState methods
    ////////////////////////////////////////////////////////////////////////////    
        
    @Override
    public int[] getPCStateDeadBlocks(FilePreprocessorConditionState pcState) {
        return FilePreprocessorConditionState.getDeadBlocks(pcState);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //  end of access to FilePreprocessorConditionState methods
    ////////////////////////////////////////////////////////////////////////////    

}
