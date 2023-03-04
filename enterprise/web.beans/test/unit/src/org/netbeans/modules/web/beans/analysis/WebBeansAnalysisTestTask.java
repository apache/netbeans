/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.beans.analysis;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.beans.MetaModelSupport;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.testutilities.CdiTestUtilities;



/**
 * @author ads
 *
 */
public class WebBeansAnalysisTestTask extends WebBeansAnalysisTask {

    public WebBeansAnalysisTestTask( CdiTestUtilities utilities ){
        super(null);
        myUtilities = utilities;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.CdiAnalysisTask#run(org.netbeans.api.java.source.CompilationInfo)
     */
    @Override
    protected void run( CompilationInfo compInfo ) {
        super.run(compInfo);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.CdiAnalysisTask#getResult()
     */
    @Override
    protected WebBeansAnalysisTestResult getResult() {
        return (WebBeansAnalysisTestResult)super.getResult();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.CdiAnalysisTask#createResult(org.netbeans.api.java.source.CompilationInfo)
     */
    @Override
    protected WebBeansAnalysisTestResult createResult( CompilationInfo info ) {
        return new WebBeansAnalysisTestResult(info);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.WebBeansAnalysisTask#getModel(CompilationInfo)
     */
    protected MetadataModel<WebBeansModel> getModel(CompilationInfo compInfo){
        try {
            return myUtilities.createBeansModel();
        }
        catch ( Exception e ){
            e.printStackTrace();
            assert false : e.getMessage();
        }
        return null;
    }

    private CdiTestUtilities myUtilities;
}
