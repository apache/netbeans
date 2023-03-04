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
package org.netbeans.modules.css.editor.module.main;

public class PositioningModuleTest extends CssModuleTestBase {

    public PositioningModuleTest(String testName) {
        super(testName);
    }

    public void testBaseDefinitions() {
        for (String propertyName : new String[]{"top", "right", "left", "bottom", "inset-block-start", "inset-inline-start", "inset-block-end", "inset-inline-end"}) {
            for (String value : new String[]{"auto", "15.2px", "10%", "inherit", "initial", "unset"}) {
                assertPropertyDeclaration(propertyName + ": " + value);
            }
        }
    }

    public void testInset() {
        assertPropertyDeclaration("inset: auto");
        assertPropertyDeclaration("inset: initial");
        assertPropertyDeclaration("inset: inherit");
        assertPropertyDeclaration("inset: 12px");
        assertPropertyDeclaration("inset: 12px auto");
        assertPropertyDeclaration("inset: 12px auto 42em");
        assertPropertyDeclaration("inset: 12px auto 42em 10%");
    }

    public void testInsetBlockInline() {
        assertPropertyDeclaration("inset-block: auto");
        assertPropertyDeclaration("inset-inline: initial");
        assertPropertyDeclaration("inset-block: inherit");
        assertPropertyDeclaration("inset-inline: 12px");
        assertPropertyDeclaration("inset-block: 12px auto");
    }

}
