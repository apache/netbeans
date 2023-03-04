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

/*
 * SourceGenerator.java
 *
 * Created on June 26, 2000, 9:29 AM
 */

package org.netbeans.test.java.generating;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;

/** <B>Java Module General API Test: SourceGenerator</B>
 * <BR><BR><I>What it tests:</I><BR>
 * This test is more complex and checks adding Elements (especially order of adding).
 * Test is focused on checking of correctness of generated code.
 * <BR><BR><I>How it works:</I><BR>
 * New class is created using DataObject.createFromTemplate().
 * Then all possible Elements are added.
 * These actions cause generating of .java code. This code is compared with supposed one.
 * <BR><BR><I>Output:</I><BR>
 * Generated Java code.
 * <BR><BR><I>Possible reasons of failure:</I><BR>
 * <BR><BR><U>Elements are added into bad positions or even not at all</U><BR>
 * See .diff file to get which ones
 * <BR><BR><U>Bad indentation</U><BR>
 * This is probably not a bug of Java Module. (Editor Bug)
 * In .diff file could be some whitespaces.
 * <BR><BR><I>Exception occured:</I><BR>
 * See .out file for StackTrace
 *
 *
 * @author Jan Becicka <Jan.Becicka@sun.com>
 */


public class SourceGenerator extends org.netbeans.test.java.XRunner {
    
    public SourceGenerator() {
        super("");
    }
    
    public SourceGenerator(java.lang.String testName) {
        super(testName);
    }
        
    /** "body" of this TestCase
     * @param o SourceElement - target for generating
     * @param log log is used for logging StackTraces
     * @throws Exception
     * @return true if test passed
     * false if failed
     */
    public boolean go(Object o, java.io.PrintWriter log) throws Exception {
//        org.openide.src.ClassElement clazz = ((org.openide.src.SourceElement) o).getClasses()[0];
//        org.netbeans.test.java.Common.simpleJavaSourceEtalonGenerator(clazz);
        return true;
    }
    
    /**
     */
    protected void setUp() {
        super.setUp();
        name = "JavaTestSourceSourceGenerator";
        packageName = "org.netbeans.test.java.testsources";
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(SourceGenerator.class).enableModules(".*").clusters(".*"));
    }
    
}
