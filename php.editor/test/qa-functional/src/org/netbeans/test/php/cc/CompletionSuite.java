/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.test.php.cc;

import junit.framework.Test;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Vladimir Riha
 */
public class CompletionSuite {

    public static Test suite() {
        
        NbModuleSuite.Configuration conf = emptyConfiguration();
        conf = conf.addTest(testCC.class,
                "CreateApplication",
                "Create_a_PHP_source_file",
                "Verify_automatic_code_completion_invocation",
                "Verify_local_variable_code_completion",
                "Verify_global_variable_code_completion",
                "Verify_variable_from_included_file_code_completion",
                "Verify_variable_from_required_file_code_completion",
                "Verify_code_completion_inside_the_identifier",
                "Verify_keywords_code_completion",
                "Verify_code_completion_after_extends_keyword",
                "Verify_code_completion_with_a_single_option",
                "Verify_code_completion_after_EXTENDS",
                "Verify_code_completion_in_slash_slash_comments",
                "Verify_code_completion_in_slash_star_comments",
                "Verify_code_completion_in_slash_star_star_comments")
                .addTest(testCCAliases.class, "VerifyAliases")
                .addTest(testCCClever.class, "CreateApplication", "CleverTryCatch")
                .addTest(testCCConstructorsAndDestructors.class, "CreateApplication", "Issue141873")
                .addTest(testCCExceptionAfterInvokation.class, "CreateApplication", "Issue141855")
                .addTest(testCCIfSpacesInExpression.class, "CreateApplication", "Issue141881")
                .addTest(testCCInDetail.class, "CreateApplication",
                "CreatePHPFile",
                "testPhp54ArrayDereferencing",
                "DetailedCodeCompletionTestingPartOne",
                "DetailedCodeCompletionTestingPartTwo",
                "DetailedCodeCompletionTestingPartThree",
                "testPhp54Callable",
                "testPhp54AnonymousObject")
                .addTest(testCCNamespaces.class, "CreateApplication",
                "testCCNamespaceSameFile",
                "testCCClassNamespaceSameFile",
                "testCCNamespaceDiffFile",
                "testCCClassNamespaceDiffFile")
                .addTest(testCCPhpDoc.class, "automaticCommentGenerationOnFunction", "automaticCommentGenerationOnClassVariable")
                .addTest(testCCReturnAnotation.class, "CreateApplication", "CreatePHPSourceFile", "testReturnSelf", "testReturnStatic", "testReturnThis")
                .addTest(testCCSorting.class, "CreateApplication", "Issue141866")
                .addTest(testCCTraits.class, "CreateApplication",
                "testPhp54TraitsSameFile",
                "testPhp54TraitsDifferentFile")
                .addTest(testCConEmptyLine.class, "CreateApplication", "Issue141854")
                .addTest(testCodeCompletionInsideQuotes.class, "CreateApplication", "Issue141880");
        return conf.suite();
    }
}
