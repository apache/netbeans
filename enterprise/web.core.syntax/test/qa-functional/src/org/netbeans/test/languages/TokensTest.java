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
package org.netbeans.test.languages;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.lib.BasicTokensTest;

/**
 *
 * @author Jindrich Sedek
 */
public class TokensTest extends BasicTokensTest {

    public TokensTest(String name) {
        super(name);
    }

    public static Test suite(){
        return NbModuleSuite.allModules(TokensTest.class);
    }
    
    public void testDIFF(){
        testRun("sample.diff");
    }
    
    public void testBAT(){ 
        testRun("sample.bat");
    }

    public void testSH(){
        testRun("sample.sh");
    }
    
    public void testMF(){
        testRun("sample.mf");
    }
    
    protected boolean generateGoldenFiles() {
        return false;
    }
    
}
