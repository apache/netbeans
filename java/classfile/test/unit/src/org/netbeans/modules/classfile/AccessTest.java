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
package org.netbeans.modules.classfile;
/*
 * AccessTest.java
 * JUnit based test
 *
 * Created on September 3, 2004, 2:09 PM
 */

import junit.framework.TestCase;

/**
 *
 * @author tball
 */
public class AccessTest extends TestCase {
    
    public AccessTest(String testName) {
        super(testName);
    }

    /**
     * Test of isStatic method, of class org.netbeans.modules.classfile.Access.
     */
    public void testIsStatic() {
        int access = Access.STATIC;
        assertTrue(Access.isStatic(access));
        assertFalse(Access.isStatic(~access));
    }

    /**
     * Test of isPublic method, of class org.netbeans.modules.classfile.Access.
     */
    public void testIsPublic() {
        int access = Access.PUBLIC;
        assertTrue(Access.isPublic(access));
        assertFalse(Access.isPublic(~access));
    }

    /**
     * Test of isProtected method, of class org.netbeans.modules.classfile.Access.
     */
    public void testIsProtected() {
        int access = Access.PROTECTED;
        assertTrue(Access.isProtected(access));
        assertFalse(Access.isProtected(~access));
    }

    /**
     * Test of isPackagePrivate method, of class org.netbeans.modules.classfile.Access.
     */
    public void testIsPackagePrivate() {
        assertTrue(Access.isPackagePrivate(0));
        assertFalse(Access.isPackagePrivate(Access.PUBLIC));
        assertFalse(Access.isPackagePrivate(Access.PROTECTED));
        assertFalse(Access.isPackagePrivate(Access.PRIVATE));
    }

    /**
     * Test of isPrivate method, of class org.netbeans.modules.classfile.Access.
     */
    public void testIsPrivate() {
        int access = Access.PRIVATE;
        assertTrue(Access.isPrivate(access));
        assertFalse(Access.isPrivate(~access));
    }
}
