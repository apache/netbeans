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
package org.netbeans.modules.gradle.java.api.output;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author OmniBene, s.r.o.
 */
public class LocationTest {

    @Test
    public void testFilenameFromClassOnly() {
        String src = "A";
        Location cut = Location.parseLocation(src);
        Assert.assertFalse(cut.isLine());
        Assert.assertFalse(cut.isMethod());
        Assert.assertEquals(src, cut.toString());
    }

    @Test
    public void testFilenameFromClassAndExtension() {
        String src = "A.java";
        Location cut = Location.parseLocation(src);
        Assert.assertFalse(cut.isLine());
        Assert.assertFalse(cut.isMethod());
        Assert.assertEquals(src, cut.toString());
    }

    @Test
    public void testFilenameFromPackageAndClassOnly() {
        String src = "my/package.A";
        Location cut = Location.parseLocation(src);
        Assert.assertFalse(cut.isLine());
        Assert.assertFalse(cut.isMethod());
        Assert.assertEquals(src, cut.toString());
    }

    @Test
    public void testFilenameFromPackageAndClassAndExtension() {
        String src = "my/package/A.java";
        Location cut = Location.parseLocation(src);
        Assert.assertFalse(cut.isLine());
        Assert.assertFalse(cut.isMethod());
        Assert.assertEquals(src, cut.toString());
    }

    @Test
    public void testLine() {
        String src = ":123";
        Location cut = Location.parseLocation(src);
        Assert.assertTrue(cut.isLine());
        Assert.assertFalse(cut.isMethod());
        Assert.assertEquals(src, cut.toString());
    }

    @Test
    public void testLineWithClass() {
        String src = "A:123";
        Location cut = Location.parseLocation(src);
        Assert.assertTrue(cut.isLine());
        Assert.assertFalse(cut.isMethod());
        Assert.assertEquals(src, cut.toString());
    }

    @Test
    public void testLineWithClassAndExt() {
        String src = "Class.java:123";
        Location cut = Location.parseLocation(src);
        Assert.assertTrue(cut.isLine());
        Assert.assertFalse(cut.isMethod());
        Assert.assertEquals(src, cut.toString());
    }

    @Test
    public void testLineWithPackageAndClassAndExt() {
        String src = "a/Class.java:123";
        Location cut = Location.parseLocation(src);
        Assert.assertTrue(cut.isLine());
        Assert.assertFalse(cut.isMethod());
        Assert.assertEquals(src, cut.toString());
    }

    @Test
    public void testLineWithPackageAndClass() {
        String src = "a/Class:123";
        Location cut = Location.parseLocation(src);
        Assert.assertTrue(cut.isLine());
        Assert.assertFalse(cut.isMethod());
        Assert.assertEquals(src, cut.toString());
    }

    @Test
    public void testMethod() {
        String src = ":myMethod()";
        Location cut = Location.parseLocation(src);
        Assert.assertFalse(cut.isLine());
        Assert.assertTrue(cut.isMethod());
        Assert.assertEquals(src, cut.toString());
    }

    @Test
    public void testMethodWithClass() {
        String src = "A:myMethod()";
        Location cut = Location.parseLocation(src);
        Assert.assertFalse(cut.isLine());
        Assert.assertTrue(cut.isMethod());
        Assert.assertEquals(src, cut.toString());
    }

    @Test
    public void testMethodWithClassAndExt() {
        String src = "Class.java:myMethod()";
        Location cut = Location.parseLocation(src);
        Assert.assertFalse(cut.isLine());
        Assert.assertTrue(cut.isMethod());
        Assert.assertEquals(src, cut.toString());
    }

    @Test
    public void testMethodWithPackageAndClassAndExt() {
        String src = "a/Class.java:myMethod()";
        Location cut = Location.parseLocation(src);
        Assert.assertFalse(cut.isLine());
        Assert.assertTrue(cut.isMethod());
        Assert.assertEquals(src, cut.toString());
    }

    @Test
    public void testMethodWithPackageAndClass() {
        String src = "a/Class:myMethod()";
        Location cut = Location.parseLocation(src);
        Assert.assertFalse(cut.isLine());
        Assert.assertTrue(cut.isMethod());
        Assert.assertEquals(src, cut.toString());
    }

    @Test
    public void testNestedClassNoPackage() {
        String src = "A$B";
        Location cut = Location.parseLocation(src);
        Assert.assertEquals("A", cut.classNames[0]);
        Assert.assertEquals("B", cut.classNames[1]);
        Assert.assertEquals(src, cut.toString());
    }

    @Test
    public void testNestedClassWithPackageAndExtension() {
        String src = "x/y/A$B.java";
        Location cut = Location.parseLocation(src);
        Assert.assertEquals("A", cut.classNames[0]);
        Assert.assertEquals("B", cut.classNames[1]);
        Assert.assertEquals(src, cut.toString());
    }

    @Test
    public void testNestedClassWithPackageAndGroovyExtension() {
        String src = "x/y/A$B.groovy";
        Location cut = Location.parseLocation(src);
        Assert.assertEquals("A", cut.classNames[0]);
        Assert.assertEquals("B", cut.classNames[1]);
        Assert.assertEquals(src, cut.toString());
    }

    @Test
    public void testNestedClassWithPackageAndKotlinExtension() {
        String src = "x/y/A$B.kt";
        Location cut = Location.parseLocation(src);
        Assert.assertEquals("A", cut.classNames[0]);
        Assert.assertEquals("B", cut.classNames[1]);
        Assert.assertEquals(src, cut.toString());
    }

    @Test
    public void testNestedClassWithPackageAndKotlinExtension2() {
        String src = "x/y/A$B.kt";
        Location cut = Location.parseLocation(src);
        Assert.assertEquals("A", cut.classNames[0]);
        Assert.assertEquals("B", cut.classNames[1]);
        Assert.assertEquals(src, cut.toString());
    }

    @Test
    public void testPackageWithNoExtension() {
        String src = "x/y/A";
        Location cut = Location.parseLocation(src);
        Assert.assertEquals("A", cut.classNames[0]);
    }

}
