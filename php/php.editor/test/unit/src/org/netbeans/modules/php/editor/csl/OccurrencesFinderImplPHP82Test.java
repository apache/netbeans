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

public class OccurrencesFinderImplPHP82Test extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImplPHP82Test(String testName) {
        super(testName);
    }

    @Override
    protected String getBaseTestFolderPath() {
        return super.getBaseTestFolderPath() + "php82/";
    }

    public void testConstantsInTraits_01a() throws Exception {
        checkOccurrences(getTestPath(), "    const IMPLICIT^_PUBLIC_TRAIT = 'ExampleTrait implicit public';", true);
    }

    public void testConstantsInTraits_01b() throws Exception {
        checkOccurrences(getTestPath(), "       echo self::IMPLICIT_PUBLIC^_TRAIT . PHP_EOL;", true);
    }

    public void testConstantsInTraits_01c() throws Exception {
        checkOccurrences(getTestPath(), "        echo self::IMPLICIT_P^UBLIC_TRAIT . PHP_EOL; // use", true);
    }

    public void testConstantsInTraits_01d() throws Exception {
        checkOccurrences(getTestPath(), "        echo self::IMPLICIT_P^UBLIC_TRAIT . PHP_EOL; // class", true);
    }

    public void testConstantsInTraits_01e() throws Exception {
        checkOccurrences(getTestPath(), "        echo self::IMPLI^CIT_PUBLIC_TRAIT . PHP_EOL; // child", true);
    }

    public void testConstantsInTraits_01f() throws Exception {
        checkOccurrences(getTestPath(), "echo ExampleClass::IMPLI^CIT_PUBLIC_TRAIT . PHP_EOL;", true);
    }

    public void testConstantsInTraits_02a() throws Exception {
        checkOccurrences(getTestPath(), "    private const PR^IVATE_TRAIT = 'ExampleTrait private';", true);
    }

    public void testConstantsInTraits_02b() throws Exception {
        checkOccurrences(getTestPath(), "        echo static::PRIV^ATE_TRAIT . PHP_EOL;", true);
    }

    public void testConstantsInTraits_02c() throws Exception {
        checkOccurrences(getTestPath(), "        echo static::PRIV^ATE_TRAIT . PHP_EOL; // use", true);
    }

    public void testConstantsInTraits_02d() throws Exception {
        checkOccurrences(getTestPath(), "        echo static::PR^IVATE_TRAIT . PHP_EOL; // class", true);
    }

    public void testConstantsInTraits_03a() throws Exception {
        checkOccurrences(getTestPath(), "    protected const PROTECTED_TRA^IT = 'ExampleTrait protected';", true);
    }

    public void testConstantsInTraits_03b() throws Exception {
        checkOccurrences(getTestPath(), "        echo $this::PROT^ECTED_TRAIT . PHP_EOL;", true);
    }

    public void testConstantsInTraits_03c() throws Exception {
        checkOccurrences(getTestPath(), "        echo $this::PROTE^CTED_TRAIT . PHP_EOL; // use", true);
    }

    public void testConstantsInTraits_03d() throws Exception {
        checkOccurrences(getTestPath(), "        echo $this::PROTECT^ED_TRAIT . PHP_EOL; // class", true);
    }

    public void testConstantsInTraits_04a() throws Exception {
        checkOccurrences(getTestPath(), "    public const PUBLIC^_TRAIT = 'ExampleTrait public';", true);
    }

    public void testConstantsInTraits_04b() throws Exception {
        checkOccurrences(getTestPath(), "        echo parent::PU^BLIC_TRAIT . PHP_EOL; // child", true);
    }

}
