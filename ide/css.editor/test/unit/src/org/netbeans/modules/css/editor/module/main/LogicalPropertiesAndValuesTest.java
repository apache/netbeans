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

public class LogicalPropertiesAndValuesTest extends CssModuleTestBase {

    public LogicalPropertiesAndValuesTest(String testName) {
        super(testName);
    }

    public void testMarginBlock() {
        assertPropertyDeclaration("margin-block: 1px");
        assertPropertyDeclaration("margin-block: var(--var1)");
        assertPropertyDeclaration("margin-block-start: 1px");
        assertPropertyDeclaration("margin-block-end: 2px");
    }

    public void testMarginInline() {
        assertPropertyDeclaration("margin-inline: 1px");
        assertPropertyDeclaration("margin-inline: var(--var1)");
        assertPropertyDeclaration("margin-inline-start: 1px");
        assertPropertyDeclaration("margin-inline-end: 2px");
    }

    public void testPaddingBlock() {
        assertPropertyDeclaration("padding-block: 1px");
        assertPropertyDeclaration("padding-block: var(--var1)");
        assertPropertyDeclaration("padding-block-start: 1px");
        assertPropertyDeclaration("padding-block-end: 2px");
    }

    public void testPaddingInline() {
        assertPropertyDeclaration("padding-inline: 1px");
        assertPropertyDeclaration("padding-inline: var(--var1)");
        assertPropertyDeclaration("padding-inline-start: 1px");
        assertPropertyDeclaration("padding-inline-end: 2px");
    }
}
