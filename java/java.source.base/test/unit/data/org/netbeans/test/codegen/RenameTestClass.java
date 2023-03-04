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
package org.netbeans.test.codegen;

/**
 * Source to rename, golden file
 *
 * @author  Pavel Flaska
 */
public class RenameTestClass {

    private RenameTestClass.ClassToRename renamedCl;

    /** Creates a new instance of RenameTestClass */
    public RenameTestClass() {
        renamedCl = new ClassToRename(5);
    }

    private final RenameTestClass.ClassToRename containsClassToRename(int x) {
        ClassToRename confusingVar;
        confusingVar = renamedCl;
        ClassToRename result = (ClassToRename) confusingVar;
        int y = result.getData();
        return new ClassToRename(y);
    }
    
    // class to rename
    static class ClassToRename {
        int a;
        
        public ClassToRename(int a) {
            this.a = a;
        }
        
        public int getData() {
            return a;
        }
    }
}
