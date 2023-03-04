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
package org.netbeans.test.syntax;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.lib.BasicOpenFileTest;

/**
 *
 * @author Jindrich Sedek
 */
public class OpenFileTest extends BasicOpenFileTest {

    public OpenFileTest(String name) {
        super(name);
    }
    
    public static Test suite(){
        return NbModuleSuite.allModules(OpenFileTest.class);
    }   
    
    public void testCSS() throws Exception {
        openStandaloneTokenFile("tokensCSS.css");
        edit("h1{ color:red;}");
        closeFile();
    }

    public void testTLD() throws Exception {
        openStandaloneTokenFile("tokensTLD.tld");
        closeFile();
    }

    public void testIssue131552() throws Exception {
        openStandaloneTokenFile("testIssue131552.html");
        closeFile();
    }

    public void testIssue144605() throws Exception {
        openStandaloneTokenFile("testIssue144605.html");
        closeFile();
    }
}
