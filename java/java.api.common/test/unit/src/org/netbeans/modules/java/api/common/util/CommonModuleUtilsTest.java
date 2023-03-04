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
package org.netbeans.modules.java.api.common.util;

import org.netbeans.modules.java.api.common.util.CommonModuleUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Tomas Zezula
 */
public class CommonModuleUtilsTest extends NbTestCase {

    private boolean print;

    public CommonModuleUtilsTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testSimplePattern() {
        testPattern(
                "src/{lin,sol,win}/classes",    //NOI18N
                "src/lin/classes",      //NOI18N
                "src/sol/classes",      //NOI18N
                "src/win/classes");     //NOI18N
    }

    public void testChanedPattern() {
        testPattern(
                "src/{lin,sol,win}/main/{classes,resources}",   //NOI18N
                "src/lin/main/classes",     //NOI18N
                "src/lin/main/resources",    //NOI18N
                "src/sol/main/classes",     //NOI18N
                "src/sol/main/resources",    //NOI18N
                "src/win/main/classes",     //NOI18N
                "src/win/main/resources");   //NOI18N
    }

    public void testNested() {
        testPattern(
                "src/{lin{32,64},sol,win}/classes",    //NOI18N
                "src/lin32/classes",     //NOI18N
                "src/lin64/classes",    //NOI18N
                "src/sol/classes",     //NOI18N
                "src/win/classes");     //NOI18N
    }

    public void testNested2() {
        testPattern(
                "src/{lin{32,64}ia,sol,win}/classes",    //NOI18N
                "src/lin32ia/classes",     //NOI18N
                "src/lin64ia/classes",    //NOI18N
                "src/sol/classes",     //NOI18N
                "src/win/classes");     //NOI18N
    }

    public void testChainedNested() {
        testPattern(
                "src/{lin{32,64},sol,win}/{classes,resources}",    //NOI18N
                "src/lin32/classes",     //NOI18N
                "src/lin32/resources",     //NOI18N
                "src/lin64/classes",    //NOI18N
                "src/lin64/resources",    //NOI18N
                "src/sol/classes",     //NOI18N
                "src/sol/resources",     //NOI18N
                "src/win/classes",     //NOI18N
                "src/win/resources");   //NOI18N
    }

    public void testNestedNested() {
        testPattern(
                "src/{lin_{ia{32,64},arm{32,64}},sol,win}/classes",    //NOI18N
                "src/lin_ia32/classes",     //NOI18N
                "src/lin_ia64/classes",     //NOI18N
                "src/lin_arm32/classes",     //NOI18N
                "src/lin_arm64/classes",    //NOI18N
                "src/sol/classes",     //NOI18N
                "src/win/classes");     //NOI18N
    }

    public void testSingletonGroup() {
        testPattern(
                "src/{lin,sol,win}{64}/classes",    //NOI18N
                "src/lin64/classes",      //NOI18N
                "src/sol64/classes",      //NOI18N
                "src/win64/classes");     //NOI18N
    }

    public void testEmptyGroup() {
        testPattern(
                "src/lin{}/classes",    //NOI18N
                "src/lin/classes");      //NOI18N
    }

    public void testEmptyGroup2() {
        testPattern(
                "src/lin{_amd,}/classes",    //NOI18N
                "src/lin_amd/classes",          //NOI18N
                "src/lin/classes");      //NOI18N
    }

    public void testBroken() {
        testPattern(
                "src/lin_{amd/classes",    //NOI18N
                "src/lin_amd/classes");      //NOI18N
    }

    public void testBroken2() {
        testPattern(
                "src/{lin{32,64,sol}/classes",    //NOI18N
                "src/lin32/classes",        //NOI18N
                "src/lin64/classes",        //NOI18N
                "src/linsol/classes");      //NOI18N
    }


    private void testPattern(
            @NonNull final String pattern,
            @NonNull String... expected) {
        final Collection<? extends String> roots = CommonModuleUtils.parseSourcePathVariants(pattern);
        if (print) {
            roots.forEach(System.out::println);
        }
        assertEquals(
                Arrays.stream(expected)
                .sorted()
                .collect(Collectors.toList()),
                roots.stream()
                .sorted()
                .collect(Collectors.toList()));
    }
}
