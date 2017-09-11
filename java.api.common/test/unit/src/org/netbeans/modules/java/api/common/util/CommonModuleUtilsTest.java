/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
