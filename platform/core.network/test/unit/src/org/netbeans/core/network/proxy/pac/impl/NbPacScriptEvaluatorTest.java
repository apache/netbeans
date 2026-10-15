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
package org.netbeans.core.network.proxy.pac.impl;

import javax.script.ScriptEngine;
import org.junit.Assert;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;

public class NbPacScriptEvaluatorTest extends NbTestCase {

    public NbPacScriptEvaluatorTest(String name) {
        super(name);
    }

    public static final junit.framework.Test suite() {
        NbModuleSuite.Configuration cfg = NbModuleSuite.emptyConfiguration().
                honorAutoloadEager(true).
                enableClasspathModules(false).
                gui(false);
        
        return cfg.clusters("platform|webcommon|ide").addTest(NbPacScriptEvaluatorTest.class).suite();
    }

    public void testFindsAnEngineByDefault() {
        StringBuilder err = new StringBuilder();
        ScriptEngine eng = findDefaultEngineInTheSystem(err);
        assertNotNull(err.toString(), eng);
    }

    public static ScriptEngine findDefaultEngineInTheSystem(StringBuilder err) {
        return NbPacScriptEvaluator.newAllowedPacEngine(null, err);
    }

    public void testReportsAnErrorForNonExistingEngine() {
        StringBuilder err = new StringBuilder();
        ScriptEngine eng = NbPacScriptEvaluator.newAllowedPacEngine("NonExisting", err);
        assertNull("No engine should be found: " + err.toString(), eng);
        Assert.assertNotEquals("Some error was reported", 0, err.length());
    }

}
