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
package org.netbeans.modules.php.editor.csl;

public class GotoDeclarationPHP82Test extends GotoDeclarationTestBase {

    public GotoDeclarationPHP82Test(String testName) {
        super(testName);
    }

    @Override
    protected String getBaseTestFolderPath() {
        return super.getBaseTestFolderPath() + "php82/";
    }

    public void testConstantsInTraits_01a() throws Exception {
        checkDeclaration(getTestPath(), "        echo self::IMPLIC^IT_PUBLIC_TRAIT . PHP_EOL;", "    const ^IMPLICIT_PUBLIC_TRAIT = 'ExampleTrait implicit public';");
    }

    public void testConstantsInTraits_01b() throws Exception {
        checkDeclaration(getTestPath(), "        echo self::IMPL^ICIT_PUBLIC_TRAIT . PHP_EOL; // use", "    const ^IMPLICIT_PUBLIC_TRAIT = 'ExampleTrait implicit public';");
    }

    public void testConstantsInTraits_01c() throws Exception {
        checkDeclaration(getTestPath(), "        echo self::IMPL^ICIT_PUBLIC_TRAIT . PHP_EOL; // class", "    const ^IMPLICIT_PUBLIC_TRAIT = 'ExampleTrait implicit public';");
    }

    public void testConstantsInTraits_01d() throws Exception {
        checkDeclaration(getTestPath(), "        echo self::IMPLICIT_P^UBLIC_TRAIT . PHP_EOL; // child", "    const ^IMPLICIT_PUBLIC_TRAIT = 'ExampleTrait implicit public';");
    }

    public void testConstantsInTraits_01e() throws Exception {
        checkDeclaration(getTestPath(), "echo ExampleClass::I^MPLICIT_PUBLIC_TRAIT . PHP_EOL;", "    const ^IMPLICIT_PUBLIC_TRAIT = 'ExampleTrait implicit public';");
    }

    public void testConstantsInTraits_02a() throws Exception {
        checkDeclaration(getTestPath(), "        echo static::PRIV^ATE_TRAIT . PHP_EOL;", "    private const ^PRIVATE_TRAIT = 'ExampleTrait private';");
    }

    public void testConstantsInTraits_02b() throws Exception {
        checkDeclaration(getTestPath(), "        echo static::PRIVATE_T^RAIT . PHP_EOL; // use", "    private const ^PRIVATE_TRAIT = 'ExampleTrait private';");
    }

    public void testConstantsInTraits_02c() throws Exception {
        checkDeclaration(getTestPath(), "        echo static::PRIVATE_TRA^IT . PHP_EOL; // class", "    private const ^PRIVATE_TRAIT = 'ExampleTrait private';");
    }

    public void testConstantsInTraits_03a() throws Exception {
        checkDeclaration(getTestPath(), "        echo $this::PROTECTED_TRA^IT . PHP_EOL;", "    protected const ^PROTECTED_TRAIT = 'ExampleTrait protected';");
    }

    public void testConstantsInTraits_03b() throws Exception {
        checkDeclaration(getTestPath(), "        echo $this::P^ROTECTED_TRAIT . PHP_EOL; // use", "    protected const ^PROTECTED_TRAIT = 'ExampleTrait protected';");
    }

    public void testConstantsInTraits_03c() throws Exception {
        checkDeclaration(getTestPath(), "        echo $this::P^ROTECTED_TRAIT . PHP_EOL; // class", "    protected const ^PROTECTED_TRAIT = 'ExampleTrait protected';");
    }

    public void testConstantsInTraits_04a() throws Exception {
        checkDeclaration(getTestPath(), "        echo parent::PUBLI^C_TRAIT . PHP_EOL; // child", "    public const ^PUBLIC_TRAIT = 'ExampleTrait public';");
    }

}
