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
package org.netbeans.modules.xml.core;

import java.io.*;
import junit.framework.*;

public class AbstractUtilTest extends TestCase {
    
    public AbstractUtilTest(java.lang.String testName) {
        super(testName);
    }
    
    public void testGetCallerPackage() {
        System.out.println("testGetCallerPackage");
//        try {
//            String pack = getClass().getPackage().getName();
//
//            assertTrue("Class package detection failed! " + testPackage(), testPackage().equals(pack));
//            assertTrue("Inner class package detection failed! " + Inner.testPackage(), Inner.testPackage().equals(pack));
//        } catch (Exception ex) {
//            ex.printStackTrace(new PrintWriter(System.out));
//        }
    }
    
//    private String testPackage() {
//        return AbstractUtilImpl.getCallerPackage();
//    }
//    
//    private class AbstractUtilImpl extends AbstractUtil {
//        
//    }
//    
//    private static class Inner {
//        static String testPackage() {
//            return AbstractUtilImpl.getCallerPackage();
//        }
//    }
}
