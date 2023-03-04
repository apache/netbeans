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
 * EditorTestCase.java
 *
 * Created on 24. srpen 2004, 12:32
 */

package org.netbeans.test.editor.lib;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestSuite;

/**
 * Dummy test that just tries to open the default project.
 * <br>
 * It should exclude problems related to project opening.
 * <br>
 * It will also eliminate the extra time for the project opening
 * to be added into the first test being executed.
 *
 * @author Miloslav Metelka
 */
public class DummyTest extends EditorTestCase {

    public DummyTest() {
        super("test");
    }
       
    public void test() {
        openDefaultProject();      
        openSourceFile("dummy","sample1");
        
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(DummyTest.class));
    }

  
}
