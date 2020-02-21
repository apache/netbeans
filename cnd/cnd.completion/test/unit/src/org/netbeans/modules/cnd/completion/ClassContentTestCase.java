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
package org.netbeans.modules.cnd.completion;

import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionBaseTestCase;

/**
 *
 *
 */
public class ClassContentTestCase extends CompletionBaseTestCase {

    /**
     * Creates a new instance of ClassContentTestCase
     */
    public ClassContentTestCase(String testName) {
        super(testName, true);
    }
       
    public void testConstructorDereference() throws Exception {
        super.performTest("file.h", 6, 9, "D(1).");
    }
    
    public void testNewDConstructor() throws Exception {
        super.performTest("file.cc", 6, 5, "new D()", -1);
    }
    
    public void testDestructorByClassPrefix() throws Exception {
        super.performTest("file.h", 20, 9, "D::");
    } 
     
    public void testDestructor() throws Exception {
        super.performTest("file.cc", 6, 5, "d.");
    }
    
    public void testDestructorByClassPrefixAndTilda() throws Exception {
        super.performTest("file.h", 20, 9, "D::~");
    } 
    
    public void testDestructorTilda() throws Exception {
        super.performTest("file.cc", 6, 5, "pD->~");
    }  

    public void testUnnamedDefinitions() throws Exception {
        super.performTest("file.cc", 6, 5, "f.");
    }
    
    public void testUnnamedEnums() throws Exception {
        super.performTest("file.cc", 6, 5, "F::");
    }
   
    public void testStaticUnnamedUnions() throws Exception {
        super.performTest("file2.cc", 15, 4);
    }
    
    public void testCompletionAfterNew() throws Exception {
        super.performTest("file.cc", 6, 5, "d = new ");
    }

    public void testCompletionAfterNewWithClassPrefix() throws Exception {
        //  #204910 - Auto complete misses c++ constructors
        super.performTest("file.cc", 6, 5, "d = new D");
    }

    public void testCompletionOnUnfinishedConstructor1() throws Exception {
        // IZ 138291 : Completion does not work for unfinished constructor
        super.performTest("file.h", 18, 5, "E(const )", -1);
    }

    public void testCompletionOnUnfinishedConstructor2() throws Exception {
        // IZ 138291 : Completion does not work for unfinished constructor
        super.performTest("file.h", 18, 5, "E(const E)", -1);
    }

    public void testDefaultConstructorWithNew() throws Exception {
        // IZ 108191 : Code Completion and Hyperlinks work wrong with class methods in some cases
        super.performTest("file.h", 6, 5, "new F().");
    }

    public void testDefaultConstructorWithoutNew() throws Exception {
        // IZ 108191 : Code Completion and Hyperlinks work wrong with class methods in some cases
        super.performTest("file.h", 6, 5, "F().");
    }

    public void testConstructorDefinition() throws Exception {
        // IZ 108191 : Code Completion and Hyperlinks work wrong with class methods in some cases
        super.performTest("file.cc", 10, 4);
    }
}
