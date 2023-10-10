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
package org.netbeans.modules.css.editor.module.main;

public class SizingModuleTest extends CssModuleTestBase {

    public SizingModuleTest(String testName) {
        super(testName);
    }

    public void testHeightWidth() {
        assertPropertyDeclaration("width: auto");
        assertPropertyDeclaration("height: 20px");
        assertPropertyDeclaration("width: 20%");
        assertPropertyDeclaration("height: min-content");
        assertPropertyDeclaration("width: max-content");
        assertPropertyDeclaration("height: fit-content");
        assertPropertyDeclaration("width: fit-content(30%)");
        assertPropertyDeclaration("height: inherit");
        assertPropertyDeclaration("width: initial");
        assertPropertyDeclaration("height: var(--test)");
    }

    public void testMaxHeightWidth() {
        assertPropertyDeclaration("max-width: none");
        assertPropertyDeclaration("max-height: 20px");
        assertPropertyDeclaration("max-width: 20%");
        assertPropertyDeclaration("max-height: min-content");
        assertPropertyDeclaration("max-width: max-content");
        assertPropertyDeclaration("max-height: fit-content");
        assertPropertyDeclaration("max-width: fit-content(30%)");
        assertPropertyDeclaration("max-height: inherit");
        assertPropertyDeclaration("max-width: initial");
        assertPropertyDeclaration("max-height: var(--test)");
    }

    public void testMinHeightWidth() {
        assertPropertyDeclaration("min-width: auto");
        assertPropertyDeclaration("min-height: 20px");
        assertPropertyDeclaration("min-width: 20%");
        assertPropertyDeclaration("min-height: min-content");
        assertPropertyDeclaration("min-width: max-content");
        assertPropertyDeclaration("min-height: fit-content");
        assertPropertyDeclaration("min-width: fit-content(30%)");
        assertPropertyDeclaration("min-height: inherit");
        assertPropertyDeclaration("min-width: initial");
        assertPropertyDeclaration("min-height: var(--test)");
    }
}
