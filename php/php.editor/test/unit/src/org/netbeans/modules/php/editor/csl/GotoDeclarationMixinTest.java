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
package org.netbeans.modules.php.editor.csl;


public class GotoDeclarationMixinTest extends GotoDeclarationTestBase {

    public GotoDeclarationMixinTest(String testName) {
        super(testName);
    }

    public void testMixin_01() throws Exception {
        checkDeclaration(getTestPath(), " * @mixin \\Mixin\\A\\Mixin^A1", "class ^MixinA1");
    }

    public void testMixin_02() throws Exception {
        checkDeclaration(getTestPath(), " * @mixin Mi^xinA2", "class ^MixinA2");
    }

    public void testMixin_03() throws Exception {
        checkDeclaration(getTestPath(), " * @mixin \\Mixin\\A\\MixinA^2|MixinB1", "class ^MixinA2");
    }

    public void testMixin_04() throws Exception {
        checkDeclaration(getTestPath(), " * @mixin \\Mixin\\A\\MixinA2|Mi^xinB1", "class ^MixinB1");
    }

}
