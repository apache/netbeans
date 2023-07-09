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
package org.netbeans.modules.jakarta.web.beans.analysis;

import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
class WebBeansEditorAnalysisTask extends CancellableAnalysysTask {
    
    
    WebBeansEditorAnalysisTask(FileObject javaFile, 
            CdiEditorAwareJavaSourceTaskFactory factory ) 
    {
        super( javaFile , factory );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.CancellableAnalysysTask#getLayerName()
     */
    @Override
    protected String getLayerName() {
        return "Web Beans Model Analyzer";      // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.CancellableAnalysysTask#createTask()
     */
    @Override
    protected AbstractAnalysisTask createTask() {
        return new WebBeansAnalysisTask( getFactory() );
    }


}
