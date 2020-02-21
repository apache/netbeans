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
