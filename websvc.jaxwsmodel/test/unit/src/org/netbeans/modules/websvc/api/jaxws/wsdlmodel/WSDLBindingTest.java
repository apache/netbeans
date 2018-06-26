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

package org.netbeans.modules.websvc.api.jaxws.wsdlmodel;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author mkuchtiak
 */
public class WSDLBindingTest extends NbTestCase {
    
    private List<String> serviceNames, serviceJavaNames, portNames, portJavaNames, portGetters, opNames, opJavaNames, opTypes, paramNames, paramTypes;
    private static final String BINDING_1="custom-client.xml";
    private static final String BINDING_11="custom-client1.xml";
    private static final String BINDING_2="custom-schema.xml";
    private static final String PACKAGE_NAME="netbeans.org.ws";
    
    
    private Object expectedValue, realValue;
    private int numberOfEvents;
    
    private static final String[][] SERVICE_NAMES={{"AddNumbersService"},{"AddNumbersService"}};
    private static final String[][] SERVICE_JAVA_NAMES={{"external_customize.client.MathUtilService"},{PACKAGE_NAME+".MathUtilService"}};
    private static final String[][] PORT_NAMES={{"AddNumbersImplPort"},{"AddNumbersImplPort"}};
    private static final String[][] PORT_JAVA_NAMES={{"external_customize.client.MathUtil"},{PACKAGE_NAME+".MathUtil"}};
    private static final String[][] PORT_GETTERS={{"getMathUtil"},{"getMathUtil"}};
    private static final String[][] OP_NAMES={{"addNumbers"},{"addNumbers"}};
    private static final String[][] OP_JAVA_NAMES={{"add"},{"add"}};
    private static final String[][] OP_TYPES={{"external_customize.schema.AddNumbersResponse"},{"int"}};
    private static final String[][] PARAM_NAMES={{"num1"},{"arg0","arg1"}};
    private static final String[][] PARAM_TYPES={{"external_customize.schema.AddNumbers"},{"int","int"}};
    
    public WSDLBindingTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    /** Testing both wsdl and schema bindings
     *  Used external files that customize binding process 
     */
    public void testExternalBindings() throws java.net.MalformedURLException {
        System.out.println("Test 1 : External Bindings");
        initResults();
        final WsdlModeler wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(getUrl("AddNumbers2.wsdl"));
        assertNotNull("WsdlModelerFactory failed to create object", wsdlModeler);
        URL binding1 = getUrl(BINDING_1);
        URL binding2 = getUrl(BINDING_2);
        wsdlModeler.setJAXBBindings(new URL[]{binding1,binding2});
        //wsdlModeler.setPackageName("netbeans.ws.org");
        wsdlModeler.generateWsdlModel(new WsdlModelListener() {
                public void modelCreated(WsdlModel model) {
                    compareWsdl(model,0);
                }
        });
        wsdlModeler.task.waitFinished();
        Thread.currentThread().yield();
        URL binding11 = getUrl(BINDING_11);
        wsdlModeler.setJAXBBindings(new URL[]{binding11});
        wsdlModeler.setPackageName(PACKAGE_NAME);
        wsdlModeler.generateWsdlModel(new WsdlModelListener() {
                public void modelCreated(WsdlModel model) {
                    compareWsdl(model,1);
                }
        },true);
        wsdlModeler.task.waitFinished();
        System.out.println("Test 1 : FINISHED "+expectedValue+":"+realValue);
        if (expectedValue!=null) assertEquals (expectedValue,realValue);
        assertEquals(2,numberOfEvents);
    }
    
    private void compareWsdl(WsdlModel model, int testNumber) {
        if (expectedValue!=null || realValue!=null) return;
        initLists();
        List<WsdlService> services = model.getServices();
        for (Iterator<WsdlService> it = services.iterator(); it.hasNext();) {
            WsdlService s = it.next();
            serviceNames.add(s.getName());
            serviceJavaNames.add(s.getJavaName());
            List<WsdlPort> ports = s.getPorts();
            for (Iterator<WsdlPort> it1 = ports.iterator(); it1.hasNext();) {
                WsdlPort port = it1.next();
                portNames.add(port.getName());
                portJavaNames.add(port.getJavaName());
                portGetters.add(port.getPortGetter());
                List<WsdlOperation> operations = port.getOperations();
                for (Iterator<WsdlOperation> it2 = operations.iterator(); it2.hasNext();) {
                    WsdlOperation op = it2.next();
                    opNames.add(op.getName());
                    opJavaNames.add(op.getJavaName());
                    opTypes.add(op.getReturnTypeName());
                    List<WsdlParameter> parameters = op.getParameters();
                    for (Iterator<WsdlParameter> it3 = parameters.iterator(); it3.hasNext();) {
                        WsdlParameter param = it3.next();
                        paramNames.add(param.getName());
                        paramTypes.add(param.getTypeName());
                    }
                }
            }
        }
        compareResults(testNumber);
        numberOfEvents++;
    }
    
    private URL getUrl(String file) throws java.net.MalformedURLException {
        return new File(getDataDir(),file).toURI().toURL();
    }
    
    private File getFile(String file) {
        return new File(getDataDir(),file);
    }
    
    private void compareResults(int testNumber) {
        
        if (!comp(SERVICE_NAMES[testNumber].length,serviceNames.size())) return;
        if (!comp(SERVICE_JAVA_NAMES[testNumber].length,serviceJavaNames.size())) return;
        
        if (!comp(PORT_NAMES[testNumber].length,portNames.size())) return;
        if (!comp(PORT_JAVA_NAMES[testNumber].length,portJavaNames.size())) return;
        if (!comp(PORT_GETTERS[testNumber].length,portGetters.size())) return;
        
        if (!comp(OP_NAMES[testNumber].length,opNames.size())) return;
        if (!comp(OP_JAVA_NAMES[testNumber].length,opJavaNames.size())) return;
        if (!comp(OP_TYPES[testNumber].length,opTypes.size())) return;
        
        if (!comp(PARAM_NAMES[testNumber].length,paramNames.size())) return;
        if (!comp(PARAM_TYPES[testNumber].length,paramTypes.size())) return;
        
        for (int i=0;i<SERVICE_NAMES[testNumber].length;i++) {
            if (!comp(SERVICE_NAMES[testNumber][i],serviceNames.get(i))) return;
        }
        for (int i=0;i<SERVICE_JAVA_NAMES[testNumber].length;i++) {
            if (!comp(SERVICE_JAVA_NAMES[testNumber][i],serviceJavaNames.get(i))) return;
        }
        
        for (int i=0;i<PORT_NAMES[testNumber].length;i++) {
            if (!comp(PORT_NAMES[testNumber][i],portNames.get(i))) return;
        }
        for (int i=0;i<PORT_JAVA_NAMES[testNumber].length;i++) {
            if (!comp(PORT_JAVA_NAMES[testNumber][i],portJavaNames.get(i))) return;
        }
        for (int i=0;i<PORT_GETTERS[testNumber].length;i++) {
            if (!comp(PORT_GETTERS[testNumber][i],portGetters.get(i))) return;
        }
        
        for (int i=0;i<OP_NAMES[testNumber].length;i++) {
            if (!comp(OP_NAMES[testNumber][i],opNames.get(i))) return;
        }
        for (int i=0;i<OP_JAVA_NAMES[testNumber].length;i++) {
            if (!comp(OP_JAVA_NAMES[testNumber][i],opJavaNames.get(i))) return;
        }
        for (int i=0;i<OP_TYPES[testNumber].length;i++) {
            if (!comp(OP_TYPES[testNumber][i],opTypes.get(i))) return;
        }
        
        for (int i=0;i<PARAM_NAMES[testNumber].length;i++) {
            if (!comp(PARAM_NAMES[testNumber][i],paramNames.get(i))) return;
        }
        for (int i=0;i<PARAM_TYPES[testNumber].length;i++) {
            if (!comp(PARAM_TYPES[testNumber][i],paramTypes.get(i))) return;
        }
    }
    
    private void initLists() {
        serviceNames = new ArrayList<String>();
        serviceJavaNames = new ArrayList<String>();
        portNames = new ArrayList<String>();
        portJavaNames = new ArrayList<String>();
        portGetters = new ArrayList<String>();
        opNames = new ArrayList<String>();
        opJavaNames = new ArrayList<String>();
        opTypes = new ArrayList<String>();
        paramNames = new ArrayList<String>();
        paramTypes = new ArrayList<String>();
    }
    
    private void initResults() {
        expectedValue=null;realValue=null;
        numberOfEvents=0;
    }
    
    private boolean comp(int x, int y) {
        if (x!=y) {
            expectedValue = new Integer(x);
            realValue = new Integer(y);
            Thread.dumpStack();
            return false;
        } else {
            return true;
        }
    }

    private boolean comp(Object x, Object y) {
        if (!x.equals(y)) {
            expectedValue = x;
            realValue = y;
            return false;
        } else {
            return true;
        }
    }
}
