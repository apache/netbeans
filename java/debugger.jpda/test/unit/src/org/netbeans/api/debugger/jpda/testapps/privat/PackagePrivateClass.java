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
package org.netbeans.api.debugger.jpda.testapps.privat;

/**
 * Test of accessing private classes/methods/fields by debugger evaluator.
 * 
 * @author Martin Entlicher
 */
class PackagePrivateClass {
    
    public static int publicStaticField = 123;
    static int staticField = 321;
    private static int privateStaticField = 111;
    
    public int publicField = 987123;
    int field = 987321;
    private int privateField = 987111;
    
    public PackagePrivateClass() {
        // public constructor
    }
    
    PackagePrivateClass(boolean b) {
        // package private constructor
    }
    
    private PackagePrivateClass(int i) {
        // private constructor
    }
    
    public static String getPublicInfo() {
        return "PublicStaticFromPackagePrivateClass";
    }
    
    static String getPPInfo() {
        return "StaticFromPackagePrivateClass";
    }
    
    private static String getPrivateInfo() {
        return "PrivateStaticFromPackagePrivateClass";
    }
    
    
    public long getPublicId() {
        return 123456789l;
    }
    
    long getPPId() {
        return 1234567890l;
    }
    
    private long getPrivateId() {
        return 1234567890123456789l;
    }
    
    private static class PrivateStaticEmptyClass {
        
    }
    
    private static class PrivateStaticClass2 {
        private PrivateStaticClass2() {}
    }
    
    private static class PrivateStaticClass {
        
        private static PrivateStaticClass INSTANCE = new PrivateStaticClass();
        
        private static int privateStaticField = 111111;
        private int privateField = 222222;
        
        private static PrivateStaticClass getDefault() {
            return INSTANCE;
        }
        
        private String privateString() {
            return "secret";
        }
    }
    
    private class PrivateEmptyClass {
        
    }
    
    private class PrivateClass {
        private PrivateClass() {}
    }
}
