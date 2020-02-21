/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
