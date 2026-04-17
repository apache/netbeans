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

public class FilterEffectsModuleTest extends CssModuleTestBase {

    public FilterEffectsModuleTest(String testName) {
        super(testName);
    }

    public void testBackdropFilter() {
        assertPropertyDeclaration("backdrop-filter: none");
        assertPropertyDeclaration("backdrop-filter: url(\"common-filters.svg#filter\")");
        assertPropertyDeclaration("backdrop-filter: var(--filter-val)");
        assertPropertyDeclaration("backdrop-filter: blur(2px)");
        assertPropertyDeclaration("backdrop-filter: brightness(60%)");
        assertPropertyDeclaration("backdrop-filter: contrast(40%)");
        assertPropertyDeclaration("backdrop-filter: drop-shadow(4px 4px 10px blue)");
        assertPropertyDeclaration("backdrop-filter: drop-shadow(red 1rem 1rem 10px)");
        assertPropertyDeclaration("backdrop-filter: grayscale(var(--percentage))");
        assertPropertyDeclaration("backdrop-filter: grayscale(30%)");
    }

}
