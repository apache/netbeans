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
package org.netbeans.test.java.hints.HintsTest;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;



public class Surround {

    public void test1() {
        System.out.println("line1");
        URL u = new URL("a");
        System.out.println("line2");
    }
    
    public void test2() {
        FileReader fr = new FileReader("file");
    }
    
    public void test3() {
        try {
            new FileReader("b");
            new URL("c");
        } catch (FileNotFoundException exception) {
            
        }
    }
    
    public void test4() {
        try {
            new FileReader("d");
        } catch(FileNotFoundException exception) {
            new FileReader("e");
        }
    }
}
