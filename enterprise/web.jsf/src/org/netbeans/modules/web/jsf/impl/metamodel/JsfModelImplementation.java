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
package org.netbeans.modules.web.jsf.impl.metamodel;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.netbeans.modules.web.jsf.api.metamodel.ModelUnit;
import org.netbeans.api.java.source.ClasspathInfo;


/**
 * @author ads
 *
 */
public class JsfModelImplementation implements MetadataModelImplementation<JsfModel> {
    
    private JsfModelImplementation ( ModelUnit unit ){
        ClasspathInfo classpathInfo = ClasspathInfo.create(unit.getBootPath(), 
                unit.getCompilePath(), unit.getSourcePath());
        myHelper = AnnotationModelHelper.create(classpathInfo);
        myModel = new JsfModelImpl( unit , getHelper() );
    }

    
    public static MetadataModelImplementation<JsfModel> create( ModelUnit unit )
    {
        return new JsfModelImplementation( unit );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation#isReady()
     */
    public boolean isReady() {
        return !getHelper().isJavaScanInProgress();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation#runReadAction(org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction)
     */
    public <R> R runReadAction(final  MetadataModelAction<JsfModel, R> action )
            throws MetadataModelException, IOException
    {
        return getHelper().runJavaSourceTask(new Callable<R>() {
            public R call() throws Exception {
                return action.run(getModel());
            }
        });
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation#runReadActionWhenReady(org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction)
     */
    public <R> Future<R> runReadActionWhenReady(
            final MetadataModelAction<JsfModel, R> action )
            throws MetadataModelException, IOException
    {
        return getHelper().runJavaSourceTaskWhenScanFinished(new Callable<R>() {
            public R call() throws Exception {
                return action.run(getModel());
            }
        });
    }
    
    AnnotationModelHelper getHelper(){
        return myHelper;
    }
    
    private JsfModelImpl getModel(){
        return myModel;
    }
    
    private JsfModelImpl myModel;
    private final AnnotationModelHelper myHelper;

}
