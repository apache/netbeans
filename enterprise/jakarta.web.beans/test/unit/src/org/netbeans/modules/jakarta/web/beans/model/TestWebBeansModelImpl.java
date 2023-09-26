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
package org.netbeans.modules.jakarta.web.beans.model;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;
import org.netbeans.modules.jakarta.web.beans.api.model.BeansModel;
import org.netbeans.modules.jakarta.web.beans.api.model.ModelUnit;
import org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.jakarta.web.beans.impl.model.WebBeansModelImplementation;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
public class TestWebBeansModelImpl extends WebBeansModelImplementation {
    
    TestWebBeansModelImpl(ModelUnit unit){
        this(unit, false);
        myProvider = new TestWebBeansModelProviderImpl( this );
    }
    
    TestWebBeansModelImpl(ModelUnit unit, boolean fullModel ){
        super(unit);
        myProvider = new TestWebBeansModelProviderImpl( this );
        isFullModel = fullModel;
        if ( fullModel){
            ClassPath path = getModelUnit().getSourcePath();
            FileObject[] roots = path.getRoots();
            assert roots.length == 1;
            myBeansModel=  new TestBeansModelImpl( roots[0]);
        }
    }
    
    public MetadataModel<WebBeansModel> createTestModel( ){
        return MetadataModelFactory.createMetadataModel( this );
    }
    
    @Override
    public BeansModel getBeansModel() {
        return myBeansModel;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.WebBeansModelImplementation#getHelper()
     */
    @Override
    protected AnnotationModelHelper getHelper() {
        return super.getHelper();
    }
    
   /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.AbstractModelImplementation#getProvider()
     */
    @Override
    protected TestWebBeansModelProviderImpl getProvider() {
        return myProvider;
    }
    
    protected boolean isFull(){
        return isFullModel;
    }
    
    private TestWebBeansModelProviderImpl myProvider; 
    private boolean isFullModel;
    private TestBeansModelImpl myBeansModel;
}
