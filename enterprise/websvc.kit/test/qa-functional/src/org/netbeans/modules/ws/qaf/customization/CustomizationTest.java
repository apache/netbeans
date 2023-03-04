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
package org.netbeans.modules.ws.qaf.customization;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import junit.framework.Test;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.ws.qaf.wsdl.FromWSDLTSuite;
import org.openide.util.Exceptions;

/**
 *
 * @author lukas
 */
public class CustomizationTest extends FromWSDLTSuite {

    public CustomizationTest(String name) {
        super(name);
    }

    @Override
    protected String getWsClientPackage() {
        return "o.n.m.ws.qaf.client.customization"; //NOI18N

    }

    @Override
    protected String getWsClientProjectName() {
        return "WsClientCustomization"; //NOI18N

    }

    @Override
    protected String getWsName() {
        return "WsCustom";
    }

    @Override
    protected String getWsPackage() {
        return "o.n.m.ws.qaf.ws.customization"; //NOI18N

    }

    @Override
    protected String getWsProjectName() {
        return "WsCustomization"; //NOI18N

    }

    public void testCreateService() throws IOException {
        File localWsdl = new File(getDataDir(), "resources" + File.separator + "norefAddNumbers.wsdl");
        String path = localWsdl.getAbsolutePath();
        createNewWSFromWSDL(getProject(), getWsName(), getWsPackage(), path);
    }

    public void testChangePortType() {
        customizeJavaName("AddNumbersPortType", "Add");
    }

    public void testChangePortTypeOperation() {
        customizeJavaName("oneWayInt", "storeInt");
    }

    public void testChangePortTypeFault() {
        customizeJavaName("AddNumbersFault", "AddFault");
    }
    
    public static Test suite() {
        return NbModuleSuite.create(addServerTests(NbModuleSuite.createConfiguration(CustomizationTest.class), "testCreateService", "testChangePortType", "testChangePortTypeOperation", "testChangePortTypeFault").enableModules(".*").clusters(".*"));
    }

    /** Creates suite from particular test cases. You can define order of testcases here. */
//    public static TestSuite suite() {
//        TestSuite suite = new NbTestSuite(); 
//        suite.addTest(new CustomizationTest("testCreateService"));
//        suite.addTest(new CustomizationTest("testChangePortType"));
//        suite.addTest(new CustomizationTest("testChangePortTypeOperation"));
//        suite.addTest(new CustomizationTest("testChangePortTypeFault"));
//        return suite;
//    }
//
//    /* Method allowing test execution directly from the IDE. */
//    public static void main(java.lang.String[] args) {
//        TestRunner.run(suite());
//    }

    private void customizeJavaName(String compName, String newName) {    
        Node n = new Node(getProjectRootNode(), "Web Services|AddNumbersService");
        n.performPopupAction("Edit Web Service Attributes");
        JButtonOperator.setDefaultStringComparator(new DefaultStringComparator(true, true));
        NbDialogOperator o = new NbDialogOperator(getWsName());
        JTabbedPaneOperator jtpo = new JTabbedPaneOperator(o, 1);
        jtpo.selectPage("WSDL Customization");
        JButtonOperator jbo = new JButtonOperator(jtpo, compName);
        jbo.doClick();
        JCheckBoxOperator jcbo = new JCheckBoxOperator(jtpo, "Use Default");
        jcbo.setSelected(false);
        jcbo = new JCheckBoxOperator(jtpo, "Enable Wrapper Style");
        jcbo.setSelected(false);
        JTextFieldOperator jtfo = new JTextFieldOperator(jtpo);
        jtfo.clearText();
        jtfo.typeText(newName);
        try {
            Thread.sleep(2500);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        o.ok();
        try {
            Thread.sleep(2500);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        dumpThreads();
        new NbDialogOperator("Information").ok();
        try {
            boolean isAnt = getProjectType().isAntBasedProject();
            waitForWsImport("(wsimport-service-clean-" + getWsName(), isAnt); //NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    
    private void dumpThreads() {
        Map<Thread, StackTraceElement[]> m = Thread.getAllStackTraces();
        for (Thread t: m.keySet()) {
            System.err.println("Thread: " + t.toString());
            StackTraceElement[] s = m.get(t);
            for (int i = 0; i < s.length; i++) {
                System.err.println("   " + s[i].toString());
            }
        }
    }

}
