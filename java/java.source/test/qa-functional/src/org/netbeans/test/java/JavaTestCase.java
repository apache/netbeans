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
package org.netbeans.test.java;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;

/**
 *
 * @author Jiri Prox
 */
public class JavaTestCase extends JellyTestCase {
    
    public JavaTestCase(String name) {
        super(name);
    }
       
    private static final int OPENED_PROJECT_ACCESS_TIMEOUT = 1000;
        
    private String defaultProjectName = "default";
    private String defaultSamplePackage = "";
    private String defaultSampleName = "";

    private static final char treeSeparator = '|';
    private String projectName = null;
    private String treeSubPackagePathToFile = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        defaultSampleName = this.getName();
        defaultSamplePackage = this.getClass().getName();                
        openProject("default");
    }
    
    public void openProject(String projectName) {
        System.out.println("opening "+ projectName);
        this.projectName = projectName;
        File projectPath = new File(this.getDataDir() + "/projects", projectName);
                
        /* 1. check if project is open  */        
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        boolean isOpen = true;
        try {
            JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", OPENED_PROJECT_ACCESS_TIMEOUT); 
            ProjectRootNode prn = pto.getProjectRootNode(projectName);
        } catch (TimeoutExpiredException ex) {        
            isOpen = false;
        }
        
        if ( isOpen ) {
            log("Project is open!");
            return;
        }
        try {
            /* 2. open project */
            this.openDataProjects("projects/" + projectName);
        } catch (IOException ex) {
            fail("Project cannot be opened");
        }
        
        
    }
   
    /**
     * Get the default project name to be used
     * in {@link openDefaultProject()}.
     * <br>
     * The default value is "editor_test".
     *
     * @return default project name
     */
    protected String getDefaultProjectName() {
        return defaultProjectName;
    }
       
    /**
     * Open default project determined
     * by {@link #getDefaultProjectName()}.
     */
    protected void openDefaultProject() {
        openProject(getDefaultProjectName());
    }
    
    /**
     * Close the default project.
     */
    protected void closeDefaultProject() {
        closeProject(getDefaultProjectName());
    }
    
    protected void closeProject(String projectName) {       
    }
    
    
    /** Open file in open project
     *  @param treeSubPath e.g. "Source Packages|test","sample1" */
    public void openFile(String treeSubPackagePathToFile, String fileName) {
        // debug info, to be removed
        this.treeSubPackagePathToFile = treeSubPackagePathToFile;
        ProjectsTabOperator pto = new ProjectsTabOperator();
        pto.invoke();
        ProjectRootNode prn = pto.getProjectRootNode(projectName);
        prn.select();
        
        // fix of issue #51191
        // each of nodes is checked by calling method waitForChildNode 
        // before they are actually opened
        StringTokenizer st = new StringTokenizer(treeSubPackagePathToFile, 
                treeSeparator+"");
        String token = "";
        String oldtoken = "";
        // if there are more then one tokens process each of them
        if (st.countTokens()>1) {
            token = st.nextToken();
            String fullpath = token;
            while (st.hasMoreTokens()) {            
                token = st.nextToken();
                waitForChildNode(fullpath, token);
                fullpath += treeSeparator+token;
            }
        } 
        // last node
        waitForChildNode(treeSubPackagePathToFile, fileName);
        // end of fix of issue #51191
        
        Node node = new Node(prn,treeSubPackagePathToFile+treeSeparator+fileName);
        node.performPopupAction("Open");
    }
    
    /**
     * Waits for a child node to be shown in the IDE. Needed for test 
     * stabilization on slow machines. 
     * @param parentPath full path for parent, | used as a delimiter
     * @param childName name of the child node
     */
    public void waitForChildNode(String parentPath, String childName) {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(projectName);
        prn.select();
        Node parent = new Node(prn, parentPath);
        final String finalFileName = childName;
        try {
            // wait for max. 30 seconds for the file node to appear
            JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 30000);
            new Waiter(new Waitable() {
                public Object actionProduced(Object parent) {
                    return ((Node)parent).isChildPresent(finalFileName) ? 
                            Boolean.TRUE: null;
                }
                public String getDescription() {
                    return("Waiting for the tree to load.");
                }
            }).waitAction(parent);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }                
    }
                    
    /**
     * Open a source file located in the "Source packages" in the editor.
     *
     * @param dir directory path with "|" separator.
     * @param srcName source name without suffix.
     */
    protected void openSourceFile(String dir, String srcName) {
        openFile("Source packages|" + dir, srcName);
    }
    
    protected final String getDefaultSamplePackage() {
        return defaultSamplePackage;
    }
    
    protected final String getDefaultSampleName() {
        return defaultSampleName;
    }

    protected void openDefaultSampleFile() {
        openSourceFile(defaultSamplePackage, defaultSampleName);
    }

    protected EditorOperator getDefaultSampleEditorOperator() {
        return new EditorOperator(defaultSampleName);
    }

    /** Method will wait max. <code> maxMiliSeconds </code> miliseconds for the <code> requiredValue </code>
     *  gathered by <code> resolver </code>.
     *
     *  @param maxMiliSeconds maximum time to wait for requiredValue
     *  @param resolver resolver, which is gathering an actual value
     *  @param requiredValue if resolver value equals requiredValue the wait cycle is finished
     *
     *  @return false if the given maxMiliSeconds time elapsed and the requiredValue wasn't obtained
     */
    public static boolean waitMaxMilisForValue(int maxMiliSeconds, ValueResolver resolver, Object requiredValue){
        int time = (int) maxMiliSeconds / 100;
        while (time > 0) {
            Object resolvedValue = resolver.getValue();
            if (requiredValue == null && resolvedValue == null){
                return true;
            }
            if (requiredValue != null && requiredValue.equals(resolvedValue)){
                return true;
            }
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException ex) {
                time=0;
            }
            time--;
        }
        return false;
    }
    
    /** Interface for value resolver needed for i.e. waitMaxMilisForValue method.  
     *  For more details, please look at {@link #waitMaxMilisForValue()}.
     */
    public static interface ValueResolver{
        /** Returns checked value */
        Object getValue();
    }   
}
