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

package org.netbeans.modules.debugger.jpda.ui.ts;

import junit.framework.Test;
import org.netbeans.modules.debugger.jpda.ui.FieldBreakpointsTest;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Peter, Revision Petr Cyhelsky
 */
public class Breakpoints_FieldTestSuite extends JellyTestCase {
    
    public Breakpoints_FieldTestSuite(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        System.out.println("### " + getName() + " ###");
    }
    
    public static Test suite() {
//        String os = System.getProperty("os.name");
//        String jdk = System.getProperty("java.version");
//        if ( jdk.contains("1.5") && os.contains("Windows") && !os.contains("Vista") ) {
//            return NbModuleSuite.create(NbModuleSuite.emptyConfiguration());
//        } else {
            return NbModuleSuite.create(NbModuleSuite.emptyConfiguration()
                .addTest(FieldBreakpointsTest.class,
                    "testFieldBreakpointCreation",
                    "testFieldBreakpointPrefilledValues",
                    "testFieldBreakpointFunctionalityAccess",
                    "testFieldBreakpointFunctionalityModification",
                    "testConditionalFieldBreakpointFunctionality",
                    "testFieldBreakpointsValidation"
                )
            .enableModules(".*").clusters(".*"));
//        }
    }
}
