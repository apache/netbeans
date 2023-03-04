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
