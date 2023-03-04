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
package org.netbeans.modules.ws.qaf.rest;

import java.awt.Container;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JCheckBox;
import junit.framework.Test;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests for New REST from Patterns wizard
 *
 * @author lukas
 */
public class PatternsTest extends RestTestBase {

    private enum Pattern {

        CcContainerItem,
        ContainerItem,
        Singleton;

        @Override
        public String toString() {
            switch (this) {
                case Singleton:
                    //Singleton
                    return Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_SingletonResource");
                case ContainerItem:
                    //Container-Item
                    return Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ContainerItem");
                case CcContainerItem:
                    //Client-Controlled Container-Item
                    return Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ClientControl");
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        /**
         * Method for getting correct index of the container resource class
         * select button in the new RESTful web service from patterns wizard for
         * given type of the resource
         *
         * @return index of the container resource class select button
         */
        public int getRepresentationClassSelectIndex() {
            switch (this) {
                case Singleton:
                    return 1;
                case ContainerItem:
                case CcContainerItem:
                    return 3;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        /**
         * Method for getting correct index of the container resource
         * representation class select button in the new RESTful web service
         * from patterns wizard for given type of the resource
         *
         * @return index of the container resource representation class select
         * button
         */
        public int getContainerRepresentationClassSelectIndex() {
            switch (this) {
                case Singleton:
                    return -1;
                case ContainerItem:
                case CcContainerItem:
                    return 4;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }
    }

    /**
     * Def constructor.
     *
     * @param testName name of particular test case
     */
    public PatternsTest(String name) {
        super(name, Server.GLASSFISH);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Constructor.
     *
     * @param testName name of particular test case
     * @param server type of server to be used
     */
    public PatternsTest(String name, Server server) {
        super(name, server);
    }

    @Override
    public String getProjectName() {
        return "FromPatterns"; //NOI18N
    }

    protected String getRestPackage() {
        return "o.n.m.ws.qaf.rest.patterns"; //NOI18N
    }

    /**
     * Test default setting for Singleton pattern
     */
    public void testSingletonDef() {
        Set<File> files = createWsFromPatterns(null, Pattern.Singleton, null);
    }

    /**
     * Test application/json mime setting for Singleton pattern
     */
    public void testSingleton1() {
        String name = "Singleton1"; //NOI18N
        Set<File> files = createWsFromPatterns(name, Pattern.Singleton, MimeType.APPLICATION_JSON);
    }

    /**
     * Test text/plain mime setting for Singleton pattern
     */
    public void testSingleton2() {
        String name = "Singleton2"; //NOI18N
        Set<File> files = createWsFromPatterns(name, Pattern.Singleton, MimeType.TEXT_PLAIN);
    }

    /**
     * Test text/html mime setting for Singleton pattern
     */
    public void testSingleton3() {
        String name = "Singleton3"; //NOI18N
        Set<File> files = createWsFromPatterns(name, Pattern.Singleton, MimeType.TEXT_HTML);
    }

    /**
     * Test default setting for Container Item pattern
     */
    public void testContainerIDef() {
        Set<File> files = createWsFromPatterns(null, Pattern.ContainerItem, null);
    }

    /**
     * Test application/json mime setting for Container Item pattern
     */
    public void testContainerI1() {
        String name = "CI1"; //NOI18N
        Set<File> files = createWsFromPatterns(name, Pattern.ContainerItem, MimeType.APPLICATION_JSON);
    }

    /**
     * Test text/plain mime setting for Container Item pattern
     */
    public void testContainerI2() {
        String name = "CI2"; //NOI18N
        Set<File> files = createWsFromPatterns(name, Pattern.ContainerItem, MimeType.TEXT_PLAIN);
    }

    /**
     * Test text/html mime setting for Container Item pattern
     */
    public void testContainerI3() {
        String name = "CI3"; //NOI18N
        Set<File> files = createWsFromPatterns(name, Pattern.ContainerItem, MimeType.TEXT_HTML);
    }

    /**
     * Test default setting for Client Controlled Container Item pattern
     */
    public void testCcContainerIDef() {
        String name = "Item1"; //NOI18N
        Set<File> files = createWsFromPatterns(name, Pattern.CcContainerItem, null);
    }

    /**
     * Test application/json mime setting for Client Controlled Container Item
     * pattern
     */
    public void testCcContainerI1() {
        String name = "CcCI1"; //NOI18N
        Set<File> files = createWsFromPatterns(name, Pattern.CcContainerItem, MimeType.APPLICATION_JSON);
    }

    /**
     * Test text/plain mime setting for Client Controlled Container Item pattern
     */
    public void testCcContainerI2() {
        String name = "CcCI2"; //NOI18N
        Set<File> files = createWsFromPatterns(name, Pattern.CcContainerItem, MimeType.TEXT_PLAIN);
    }

    /**
     * Test text/html mime setting for Client Controlled Container Item pattern
     */
    public void testCcContainerI3() {
        String name = "CcCI3"; //NOI18N
        Set<File> files = createWsFromPatterns(name, Pattern.CcContainerItem, MimeType.TEXT_HTML);
    }

    private Set<File> createWsFromPatterns(String name, Pattern pattern, MimeType mimeType) {
        //RESTful Web Services from Patterns
        String patternsTypeName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "Templates/WebServices/RestServicesFromPatterns");
        createNewWSFile(getProject(), patternsTypeName);
        WizardOperator wo = new WizardOperator(patternsTypeName);
        new JRadioButtonOperator(wo, pattern.toString()).changeSelection(true);
        wo.next();
        wo.stepsWaitSelectedValue("Specify Resource Classes");
        //set resource package
        JComboBoxOperator jcbo = new JComboBoxOperator(wo, new Pkg());
        jcbo.clickMouse();
        jcbo.clearText();
        jcbo.typeText(getRestPackage());

        if (name != null) {
            //we're not using Defs when name != null !!!
            //set resource class name
            JTextFieldOperator jtfo = new JTextFieldOperator(wo, new ClsName());
            jtfo.clickMouse();
            jtfo.clearText();
            jtfo.typeText(name + "Cl"); //NOI18N
            //set mimeType
            if (mimeType != null) {
                jcbo = new JComboBoxOperator(wo, new Mime());
                jcbo.clickMouse();
                jcbo.selectItem(mimeType.toString());
            }
            //set resource representation class
            if (MimeType.APPLICATION_JSON.equals(mimeType)) {
                /* not always on classpath - consider usage of javax.json.JsonString
                jtfo = new JTextFieldOperator(wo, new RCls());
                jtfo.clickMouse();
                jtfo.clearText();
                jtfo.typeText("org.codehaus.jettison.json.JSONString"); //NOI18N
                */
            } else if (MimeType.TEXT_PLAIN.equals(mimeType)) {
                new JButtonOperator(wo, pattern.getRepresentationClassSelectIndex()).pushNoBlock();
                //"Find Type"
                String fTypeLbl = Bundle.getStringTrimmed("org.netbeans.modules.java.source.ui.Bundle", "DLG_FindType");
                NbDialogOperator nbo = new NbDialogOperator(fTypeLbl);
                new JTextFieldOperator(nbo, 0).typeText("Level"); //NOI18N
                nbo.getTimeouts().setTimeout("ComponentOperator.WaitComponentEnabledTimeout", 30000);
                nbo.ok();
            }
            if (Pattern.Singleton.equals(pattern)) {
                //set resource Path
                jtfo = new JTextFieldOperator(wo, new Path());
                jtfo.clickMouse();
                jtfo.clearText();
                jtfo.typeText(name + "URI"); //NOI18N
            } else {
                //set resource Path
                jtfo = new JTextFieldOperator(wo, new Path());
                jtfo.clickMouse();
                jtfo.clearText();
                jtfo.typeText("{" + name + "URI}"); //NOI18N
                //set container resource class name
                jtfo = new JTextFieldOperator(wo, new CClsName());
                jtfo.clickMouse();
                jtfo.clearText();
                jtfo.typeText(name + "CClass"); //NOI18N
                //set container resource Path
                jtfo = new JTextFieldOperator(wo, new CPath());
                jtfo.clickMouse();
                jtfo.clearText();
                jtfo.typeText("/" + name + "ContainerURI"); //NOI18N
                //set container resource representation class
                if (MimeType.APPLICATION_JSON.equals(mimeType)) {
                    /* not always on classpath - consider usage of javax.json.JsonObject
                    jtfo = new JTextFieldOperator(wo, new CRCls());
                    jtfo.clickMouse();
                    jtfo.clearText();
                    jtfo.typeText("org.codehaus.jettison.json.JSONObject"); //NOI18N
                    */
                } else if (MimeType.TEXT_PLAIN.equals(mimeType)) {
                    new JButtonOperator(wo, pattern.getContainerRepresentationClassSelectIndex()).pushNoBlock();
                    //"Find Type"
                    String fTypeLbl = Bundle.getStringTrimmed("org.netbeans.modules.java.source.ui.Bundle", "DLG_FindType");
                    NbDialogOperator nbo = new NbDialogOperator(fTypeLbl);
                    new JTextFieldOperator(nbo, 0).typeText("Preferences"); //NOI18N
                    nbo.ok();
                }
            }
        }
        // add Jersey libraries neeed for JSONObject (also see #206526)
        JCheckBox useJerseyCheckBox = JCheckBoxOperator.findJCheckBox((Container) wo.getSource(), "Use Jersey specific features", true, true);
        if (useJerseyCheckBox != null) {
            new JCheckBoxOperator(useJerseyCheckBox).setSelected(true);
        }
        wo.btFinish().requestFocus();
        wo.finish();
        Set<File> createdFiles = new HashSet<File>();
        switch (pattern) {
            case Singleton:
                if (name != null) {
                    createdFiles.add(getFileFromProject(name + "Cl")); //NOI18N
                } else {
                    createdFiles.add(getFileFromProject("GenericResource")); //NOI18N
                }
                break;
            case ContainerItem:
                if (name != null) {
                    createdFiles.add(getFileFromProject(name + "Cl")); //NOI18N
                    createdFiles.add(getFileFromProject(name + "CClass")); //NOI18N
                } else {
                    createdFiles.add(getFileFromProject("ItemResource")); //NOI18N
                    createdFiles.add(getFileFromProject("ItemsResource")); //NOI18N
                }
                break;
            case CcContainerItem:
                if (name != null) {
                    createdFiles.add(getFileFromProject(name + "Cl")); //NOI18N
                    createdFiles.add(getFileFromProject(name + "CClass")); //NOI18N
                } else {
                    createdFiles.add(getFileFromProject("ItemResource_1")); //NOI18N
                    createdFiles.add(getFileFromProject("ItemsResource_1")); //NOI18N
                }
                break;
        }
        closeCreatedFiles(createdFiles);
        checkNodes(createdFiles);
        checkFiles(createdFiles);
        return createdFiles;
    }

    private File getFileFromProject(String fileName) {
        final FileObject fo = getProjectSourceRoot();
        final String location = getRestPackage().replace('.', '/') + "/" + fileName + ".java"; //NOI18N
        try {
            FileObject file = (FileObject) new Waiter(new Waitable() {
                @Override
                public Object actionProduced(Object obj) {
                    return fo.getFileObject(location);
                }

                @Override
                public String getDescription() {
                    return FileUtil.toFile(fo).getAbsolutePath() + File.separator + location + " exists"; //NOI18N
                }
            }).waitAction(null);
            return FileUtil.toFile(file);
        } catch (InterruptedException ie) {
            throw new JemmyException("Interrupted.", ie); //NOI18N
        }
    }

    protected void closeCreatedFiles(Set<File> files) {
        for (File f : files) {
            EditorOperator eo = new EditorOperator(f.getName());
            eo.close();
        }
    }
    
    private void checkNodes(Set<File> files) {
        Node restNode = getRestNode();
        for (File f : files) {
            Node node = new Node(restNode, f.getName().replace(".java", ""));
        }
    }

    /**
     * Creates suite from particular test cases. You can define order of
     * testcases here.
     */
    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, PatternsTest.class,
                "testSingletonDef", //NOI18N
                "testContainerIDef", //NOI18N
                "testCcContainerIDef", //NOI18N
                "testSingleton1", //NOI18N
                "testCcContainerI1", //NOI18N
                "testSingleton2", //NOI18N
                "testContainerI1", //NOI18N
                "testContainerI2", //NOI18N
                "testSingleton3", //NOI18N
                "testContainerI3", //NOI18N
                "testCcContainerI2", //NOI18N
                "testCcContainerI3", //NOI18N
                "testDeploy", //NOI18N
                "testUndeploy" //NOI18N
                );
    }

    static class Pkg extends JComponentByLabelFinder {

        public Pkg() {
            //Resource Package:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_Package"));
        }
    }

    static class ClsName extends JComponentByLabelFinder {

        public ClsName() {
            //Class Name:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ClassName"));
        }
    }

    static class Mime extends JComponentByLabelFinder {

        public Mime() {
            //MIME Type:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_MimeType"));
        }
    }

    static class RCls extends JComponentByLabelFinder {

        public RCls() {
            //Representation Class:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_RepresentationClass"));
        }
    }

    static class Path extends JComponentByLabelFinder {

        public Path() {
            //Path:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_UriTemplate"));
        }
    }

    static class CClsName extends JComponentByLabelFinder {

        public CClsName() {
            //Container Class Name:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ContainerClass"));
        }
    }

    static class CPath extends JComponentByLabelFinder {

        public CPath() {
            //Container Path:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ContainerUriTemplate"));
        }
    }

    static class CRCls extends JComponentByLabelFinder {

        public CRCls() {
            //Container Representation Class:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ContainerRepresentationClass"));
        }
    }

    static class Loc extends JComponentByLabelFinder {

        public Loc() {
            //Location:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_SrcLocation"));
        }
    }
}
