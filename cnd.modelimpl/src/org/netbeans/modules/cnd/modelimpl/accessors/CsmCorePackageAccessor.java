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
package org.netbeans.modules.cnd.modelimpl.accessors;

import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.content.project.GraphContainer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FilePreprocessorConditionState;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.PreprocessorStatePair;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModel;

/**
 *
 */
public abstract class CsmCorePackageAccessor {
    private static CsmCorePackageAccessor INSTANCE;

    public static synchronized CsmCorePackageAccessor get() {
        if (INSTANCE == null) {
            Class<?> c = ModelImpl.class;
            try {
                Class.forName(c.getName(), true, c.getClassLoader());
            } catch (ClassNotFoundException e) {
                // ignore
            }
        }

        assert INSTANCE != null : "There is no Csm Core API package accessor available!"; //NOI18N
        return INSTANCE;
    }

    /**
     * Register the accessor. The method can only be called once - otherwise it
     * throws IllegalStateException.
     *
     * @param accessor instance.
     */
    public static void register(CsmCorePackageAccessor accessor) {
        if (INSTANCE != null) {
            throw new IllegalStateException("CsmCoreAccessor Already registered"); // NOI18N
        }
        INSTANCE = accessor;
    }   
    

    ////////////////////////////////////////////////////////////////////////////
    //  access to ModelImpl methods
    ////////////////////////////////////////////////////////////////////////////
    
    public abstract void notifyClosing(ModelImpl model);

    ////////////////////////////////////////////////////////////////////////////
    //  end of access to ModelImpl methods
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    //  access to FileImpl methods
    ////////////////////////////////////////////////////////////////////////////

    public abstract int getErrorCount(FileImpl fileImpl);
    public abstract FileContent getFileContent(CsmParserProvider.CsmParserParameters descr);
    public abstract FileContent prepareLazyStatementParsingContent(FileImpl fileImpl);
    public abstract void releaseLazyStatementParsingContent(FileImpl fileImpl, FileContent tmpFileContent);

    public abstract PreprocessorStatePair getCachedVisitedState(FileImpl csmFile, PreprocHandler.State newState);
    public abstract void cacheVisitedState(FileImpl csmFile, PreprocHandler.State newState, PreprocHandler preprocHandler, FilePreprocessorConditionState pcState);
    
    // access for tests
    public abstract void setFileImplTestHook(TraceModel.TestHook hook);
    public abstract void testFileImplErrors(FileImpl file, TraceModel.ErrorListener errorListener);

    ////////////////////////////////////////////////////////////////////////////
    //  end of access to FileImpl methods
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    //  access to ProjectBase methods
    ////////////////////////////////////////////////////////////////////////////

    public abstract GraphContainer getGraph(ProjectBase project);
    
    ////////////////////////////////////////////////////////////////////////////
    //  end of access to ProjectBase methods
    ////////////////////////////////////////////////////////////////////////////

    
    ////////////////////////////////////////////////////////////////////////////
    //  access to FilePreprocessorConditionState methods
    ////////////////////////////////////////////////////////////////////////////
    
    public abstract int[] getPCStateDeadBlocks(FilePreprocessorConditionState pcState);
    
    ////////////////////////////////////////////////////////////////////////////
    //  end of access to FilePreprocessorConditionState methods
    ////////////////////////////////////////////////////////////////////////////


}
