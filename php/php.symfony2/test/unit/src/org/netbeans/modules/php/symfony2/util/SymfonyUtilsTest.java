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
package org.netbeans.modules.php.symfony2.util;

import org.netbeans.junit.NbTestCase;

public class SymfonyUtilsTest extends NbTestCase {

    public SymfonyUtilsTest(String name) {
        super(name);
    }

    public void testCapitalize() {
        assertEquals("Test", SymfonyUtils.capitalize("test", true));
        assertEquals("test", SymfonyUtils.capitalize("test", false));
        assertEquals("MyTest", SymfonyUtils.capitalize("my_test", true));
        assertEquals("myTest", SymfonyUtils.capitalize("my_test", false));
        assertEquals("MyLongTest", SymfonyUtils.capitalize("my_long_test", true));
        assertEquals("myLongTest", SymfonyUtils.capitalize("my_long_test", false));
    }

    public void testDecapitalize() {
        assertEquals("test", SymfonyUtils.decapitalize("Test"));
        assertEquals("my_test", SymfonyUtils.decapitalize("MyTest"));
        assertEquals("my_long_test", SymfonyUtils.decapitalize("myLongTest"));
    }

}
