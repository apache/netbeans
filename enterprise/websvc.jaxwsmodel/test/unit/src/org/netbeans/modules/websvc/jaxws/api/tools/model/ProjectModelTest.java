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

package org.netbeans.modules.websvc.jaxws.api.tools.modelxws.api.tools.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import junit.framework.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModelProvider;
/*
 * ProjectModelTest.java
 * JUnit based test
 *
 * Created on February 13, 2006, 5:43 PM
 */

/**
 *
 * @author mkuchtiak
 */
public class ProjectModelTest extends NbTestCase {
    
    public ProjectModelTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    public void testModel() throws IOException{
        File fo = getFile("jax-ws.xml");
        File fo1 = getFile("jax-ws1.xml");
        InputStream is = new FileInputStream(fo);
        InputStream is1 = new FileInputStream(fo1);
        JaxWsModel jaxws = JaxWsModelProvider.getDefault().getJaxWsModel(is);
        is.close();
        assertNotNull("JaxWsModel1 isn't created",jaxws);
        JaxWsModel jaxws1 = JaxWsModelProvider.getDefault().getJaxWsModel(is1); 
        is1.close();
        assertNotNull("JaxWsModel2 isn't created",jaxws1);
        System.out.println("services.length = "+jaxws.getServices().length);
        assertEquals(2,jaxws.getServices().length);
        jaxws.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println("propertyChanged:"+evt.getPropertyName()+"   Old Value:"+evt.getOldValue()+"   New Value:"+evt.getNewValue());
            }
        });
        String orgWsdl = jaxws.findServiceByName("A").getWsdlUrl();
        jaxws.merge(jaxws1);
        String newWsdl = jaxws.findServiceByName("AA").getWsdlUrl();
        assertEquals(orgWsdl,newWsdl);
    }

    private File getFile(String file) {
        return new File(getDataDir(),file);
    }
    
    private File newFile(String file) {
        return new File(getDataDir(),file);
    }
    
}
