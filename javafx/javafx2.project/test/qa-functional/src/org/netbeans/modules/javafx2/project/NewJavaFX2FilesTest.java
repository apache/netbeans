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
package org.netbeans.modules.javafx2.project;

import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;

/**
 *
 * @author stezeb
 */
public class NewJavaFX2FilesTest extends JellyTestCase {

    /* Test project names */
    private final String PLAINAPP_NAME = "FXApp";

    /**
     * Constructor required by JUnit
     */
    public NewJavaFX2FilesTest(String testName) {
        super(testName);
    }

    /**
     * Creates suite from particular test cases.
     */
    public static Test suite() {
        return createModuleTest(NewJavaFX2FilesTest.class,
                "createJavaFX2Application",
                "createFXMLFile",
                "createFXMainFile",
                "createPreloaderFile",
                "createSwingMainFile",
                "closeJavaFX2Application");
    }

    @Override
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }

    public void createJavaFX2Application() {
        TestUtils.createJavaFX2Project(TestUtils.JAVAFX_PROJECT_TYPE_PLAIN,
                PLAINAPP_NAME, getWorkDirPath());
    }

    public void closeJavaFX2Application() {
        TestUtils.closeJavaFX2Project(PLAINAPP_NAME);
    }
    
    public void createFXMLFile() {
        TestUtils.createJavaFX2FXMLFile(TestUtils.JAVAFX_FILE_TYPE_FXML,
                PLAINAPP_NAME, "FXMLPage");
    }
    
    public void createFXMainFile() {
        TestUtils.createJavaFX2File(TestUtils.JAVAFX_FILE_TYPE_MAIN,
                PLAINAPP_NAME, "FXAppStart");
    }
    
    public void createPreloaderFile() {
        TestUtils.createJavaFX2File(TestUtils.JAVAFX_FILE_TYPE_PRELOADER,
                PLAINAPP_NAME, "FXAppPreload");
    }
    
    public void createSwingMainFile() {
        TestUtils.createJavaFX2File(TestUtils.JAVAFX_FILE_TYPE_SWINGMAIN,
                PLAINAPP_NAME, "FXSwingAppStart");
    }
    
}
