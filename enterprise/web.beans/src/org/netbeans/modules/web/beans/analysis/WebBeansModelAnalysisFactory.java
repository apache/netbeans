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

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;


/**
 * @author ads
 *
 */
@ServiceProvider(service=JavaSourceTaskFactory.class)
public class WebBeansModelAnalysisFactory extends
        CdiEditorAwareJavaSourceTaskFactory
{

    public WebBeansModelAnalysisFactory( ) {
        super(Priority.LOW);    
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.java.source.JavaSourceTaskFactory#createTask(org.openide.filesystems.FileObject)
     */
    @Override
    protected CancellableTask<CompilationInfo> createTask( FileObject fileObject ) {
        return new WebBeansEditorAnalysisTask( fileObject, this );
    }
    
}
