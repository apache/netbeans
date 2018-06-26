/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
