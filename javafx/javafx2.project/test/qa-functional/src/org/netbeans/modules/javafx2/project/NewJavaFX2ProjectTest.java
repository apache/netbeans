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
public class NewJavaFX2ProjectTest extends JellyTestCase {

    /* Test project names */
    private final String PLAINAPP_NAME = "FXApp";
    private final String PRELOADER_NAME = "FXPreloader";
    private final String FXMLAPP_NAME = "FXMLApp";
    private final String SWINGAPP_NAME = "FXSwingApp";

    /**
     * Constructor required by JUnit
     */
    public NewJavaFX2ProjectTest(String testName) {
        super(testName);
    }

    /**
     * Creates suite from particular test cases.
     */
    public static Test suite() {
        return createModuleTest(NewJavaFX2ProjectTest.class,
                "createJavaFX2Application",
                "createJavaFX2Preloader",
                "createJavaFX2FXMLApp",
                "createJavaFX2SwingApp",
                "closeJavaFX2Application",
                "closeJavaFX2Preloader",
                "closeJavaFX2FXMLApp",
                "closeJavaFX2SwingApp");
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

    public void createJavaFX2Preloader() {
        TestUtils.createJavaFX2Project(TestUtils.JAVAFX_PROJECT_TYPE_PRELOADER,
                PRELOADER_NAME, getWorkDirPath());
    }

    public void closeJavaFX2Preloader() {
        TestUtils.closeJavaFX2Project(PRELOADER_NAME);
    }

    public void createJavaFX2FXMLApp() {
        TestUtils.createJavaFX2Project(TestUtils.JAVAFX_PROJECT_TYPE_FXMLAPP,
                FXMLAPP_NAME, getWorkDirPath());
    }

    public void closeJavaFX2FXMLApp() {
        TestUtils.closeJavaFX2Project(FXMLAPP_NAME);
    }

    public void createJavaFX2SwingApp() {
        TestUtils.createJavaFX2Project(TestUtils.JAVAFX_PROJECT_TYPE_SWINGAPP,
                SWINGAPP_NAME, getWorkDirPath());
    }

    public void closeJavaFX2SwingApp() {
        TestUtils.closeJavaFX2Project(SWINGAPP_NAME);
    }
    
}
