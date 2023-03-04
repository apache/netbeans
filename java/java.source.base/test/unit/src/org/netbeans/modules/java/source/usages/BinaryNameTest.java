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
package org.netbeans.modules.java.source.usages;

import javax.lang.model.element.ElementKind;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Tomas Zezula
 */
public class BinaryNameTest extends NbTestCase {
    
    public BinaryNameTest(final String name) {
        super(name);
    }
    
    public void testRawName() throws Exception {
        BinaryName name = BinaryName.create("java.lang.String", ElementKind.CLASS);
        assertEquals("java.lang", name.getPackage());
        assertEquals("String", name.getClassName());
        assertEquals("String", name.getSimpleName());
        name = BinaryName.create("InUnnamedPkg", ElementKind.CLASS);
        assertEquals("", name.getPackage());
        assertEquals("InUnnamedPkg", name.getClassName());
        assertEquals("InUnnamedPkg", name.getSimpleName());
        name = BinaryName.create("java.util.Map$Entry", ElementKind.INTERFACE);
        assertEquals("java.util", name.getPackage());
        assertEquals("Map$Entry", name.getClassName());
        assertEquals("Entry", name.getSimpleName());
        name = BinaryName.create("ru.$stl.Class", ElementKind.CLASS);
        assertEquals("ru.$stl", name.getPackage());
        assertEquals("Class", name.getClassName());
        assertEquals("Class", name.getSimpleName());
        name = BinaryName.create("ru.$stl.Class$Inner", ElementKind.CLASS);
        assertEquals("ru.$stl", name.getPackage());
        assertEquals("Class$Inner", name.getClassName());
        assertEquals("Inner", name.getSimpleName());
    }
    
    public void testIndexName() throws Exception {
        BinaryName name = BinaryName.create(
                "ru.$stl.$vector",
                ElementKind.CLASS,
                false,
                "ru.$stl.$vector".length()-"$vector".length());
        assertEquals("ru.$stl", name.getPackage());
        assertEquals("$vector", name.getClassName());
        assertEquals("$vector", name.getSimpleName());
        name = BinaryName.create(
                "ru.$stl.$vector$iterator",
                ElementKind.CLASS,
                false,
                "ru.$stl.$vector$iterator".length()-"iterator".length());
        assertEquals("ru.$stl", name.getPackage());
        assertEquals("$vector$iterator", name.getClassName());
        assertEquals("iterator", name.getSimpleName());
        name = BinaryName.create(
                "ru.$stl.$vector$$iterator",
                ElementKind.CLASS,
                false,
                "ru.$stl.$vector$$iterator".length()-"$iterator".length());
        assertEquals("ru.$stl", name.getPackage());
        assertEquals("$vector$$iterator", name.getClassName());
        assertEquals("$iterator", name.getSimpleName());
    }
    
}
