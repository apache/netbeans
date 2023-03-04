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
package org.netbeans.modules.html.knockout.navigate;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.html.knockout.GeneralKnockout;

/**
 *
 * @author vriha
 */
public class PureComputedGoToTest extends GeneralKnockout {

    public PureComputedGoToTest(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(PureComputedGoToTest.class).addTest(
                        "createApplication",
                        "testFirstBinding",
                        "testSecondBinding",
                        "testTemplate"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void createApplication() {
        try {
            startTest();
            openDataProjects("sample");
            openFile("purecomp.html", "sample");
            waitScanFinished();
            endTest();
        } catch (IOException ex) {
            Logger.getLogger(PureComputedGoToTest.class.getName()).log(Level.INFO, "Opening project", ex);
        }
    }

    public void testFirstBinding() {
        startTest();
        navigate("purecomp.html", "purecomp.js", 24, 35, 1, 1);
        endTest();
    }

    public void testSecondBinding() {
        startTest();
        navigate("purecomp.html", "purecomp.js", 24, 54, 1, 1);
        endTest();
    }

    public void testTemplate() {
        startTest();
        navigate("purecomp.html", "purecomp.js", 22, 70, 1, 1);
        endTest();
    }
}
