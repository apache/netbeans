/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.test;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;

/**
 * IMPORTANT NOTE:
 * If This class is not compiled with the notification about not resolved
 * NbTestSuite class => NB JUnit module is absent in target platform 
 * 
 * To solve this problem NB JUnit must be installed 
 * For instance from Netbeans Update Center Beta:
 * - start target(!) platform as IDE from command line (/opt/NBDEV/bin/netbeans)
 * - in opened IDE go into Tools->Update Center
 * - select "Netbeans Update Center Beta" 
 * -- if absent => configure it using the following url as example
 *    http://www.netbeans.org/updates/beta/55_{$netbeans.autoupdate.version}_{$netbeans.autoupdate.regnum}.xml?{$netbeans.hash.code}
 * - press Next
 * - in Libraries subfoler found NB JUnit module
 * - Add it and install
 * - close target IDE and reload development IDE to update the information of 
 *         available modules in target's platform
 */

/**
 * base class to isolate using of NbJUnit library
 */
public class CndBaseTestSuite extends NativeExecutionBaseTestSuite {
    static {
        Logger.getLogger("org.netbeans.modules.editor.settings.storage.Utils").setLevel(Level.SEVERE);
        Logger.getLogger("org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager").setLevel(Level.SEVERE);
        Logger.getLogger("org.openide.filesystems.FileUtil").setLevel(Level.OFF);

//        System.setProperty("cnd.pp.condition.comparision.trace", "true");
//        System.setProperty("cnd.modelimpl.trace.file", "gmodule-dl.c");
    }
    /**
     * Constructs an empty TestSuite.
     */
    public CndBaseTestSuite() {
        super();
    }

    /**
     * Constructs a TestSuite from the given class. Adds all the methods
     * starting with "test" as test cases to the suite.
     *
     */
    public CndBaseTestSuite(Class<? extends CndBaseTestCase> theClass) {
        super(theClass);
    }

    /**
     * Constructs an empty TestSuite.
     */
    public CndBaseTestSuite(String name) {
        super(name);
    }

    /**
     * Constructs TestSuite that takes platforms (mspecs) from the given section,
     * and performs tests specified by classes parameters for each of them
     * @param name suite name
     * @param mspecSection section of the .cndtestrc that contains platforms as keys
     * @param testClasses test classes
     */
    public CndBaseTestSuite(String name, String mspecSection, Class... testClasses) {
        super(name, mspecSection, testClasses);
    }

}
