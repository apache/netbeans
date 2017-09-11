/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.completion;

import org.netbeans.modules.java.source.parsing.JavacParser;

/**
 *
 * @author Dusan Balek
 */
public class JavaCompletionTask17FeaturesTest extends CompletionTestBase {

    public JavaCompletionTask17FeaturesTest(String testName) {
        super(testName);
    }

    // Java 1.7 try-with-resources tests ---------------------------------------
    
    public void testEmptyFileBeforeTypingVarResouce() throws Exception {
        performTest("TWRStart", 114, "try (", "finalAndAutoCloseables.pass", "1.7");
    }

    public void testBeforeTypingVarResouce() throws Exception {
        performTest("TWRNoRes", 127, null, "finalAndAutoCloseables.pass", "1.7");
    }

    public void testBeforeVarResouce() throws Exception {
        performTest("TWR", 127, null, "finalAndAutoCloseables.pass", "1.7");
    }

    public void testEmptyFileTypingVarResouce() throws Exception {
        performTest("TWRStart", 114, "try (f", "finalKeyword.pass", "1.7");
    }

    public void testTypingVarResouce() throws Exception {
        performTest("TWRNoRes", 127, "f", "finalKeyword.pass", "1.7");
    }

    public void testOnVarResouce() throws Exception {
        performTest("TWR", 128, null, "finalKeyword.pass", "1.7");
    }

    public void testEmptyFileAfterFinalInResource() throws Exception {
        performTest("TWRStart", 114, "try (final ", "autoCloseables.pass", "1.7");
    }

    public void testTypingAfterFinalInResouce() throws Exception {
        performTest("TWRNoRes", 127, "final ", "autoCloseables.pass", "1.7");
    }

    public void testAfterFinalInResouce() throws Exception {
        performTest("TWR", 133, null, "autoCloseables.pass", "1.7");
    }

    public void testEmptyFileTypingTypeInVarResouce() throws Exception {
        performTest("TWRStart", 114, "try (final F", "autoCloseablesStartingWithF.pass", "1.7");
    }

    public void testTypingTypeInVarResouce() throws Exception {
        performTest("TWRNoRes", 127, "final F", "autoCloseablesStartingWithF.pass", "1.7");
    }

    public void testOnTypeInVarResouce() throws Exception {
        performTest("TWR", 134, null, "autoCloseablesStartingWithF.pass", "1.7");
    }

    public void testEmptyFileBeforeTypingNameInVarResouce() throws Exception {
        performTest("TWRStart", 114, "try (final FileWriter ", "resourceNames.pass", "1.7");
    }

    public void testBeforeTypingNameInVarResouce() throws Exception {
        performTest("TWRNoRes", 127, "final FileWriter ", "resourceNames.pass", "1.7");
    }

    public void testBeforeNameInVarResouce() throws Exception {
        performTest("TWR", 144, null, "resourceNames.pass", "1.7");
    }

    public void testEmptyFileAfterTypingNameInVarResouce() throws Exception {
        performTest("TWRStart", 114, "try (final FileWriter fw ", "empty.pass", "1.7");
    }

    public void testAfterTypingNameInVarResouce() throws Exception {
        performTest("TWRNoRes", 127, "final FileWriter fw ", "empty.pass", "1.7");
    }

    public void testAfterNameInVarResouce() throws Exception {
        performTest("TWR", 147, null, "empty.pass", "1.7");
    }

    public void testEmptyFileBeforeVarResouceInit() throws Exception {
        performTest("TWRStart", 114, "try (final FileWriter fw = ", "resourceInit.pass", "1.7");
    }

    public void testBeforeTypingVarResouceInit() throws Exception {
        performTest("TWRNoRes", 127, "final FileWriter fw = ", "resourceInit.pass", "1.7");
    }

    public void testBeforeVarResouceInit() throws Exception {
        performTest("TWR", 149, null, "resourceInit.pass", "1.7");
    }

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
