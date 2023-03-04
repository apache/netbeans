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
package org.netbeans.modules.php.phpunit.annotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTag;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider;
import org.openide.util.NbBundle;

public class PhpUnitAnnotationsProvider extends AnnotationCompletionTagProvider {

    private static final PhpUnitAnnotationsProvider INSTANCE = new PhpUnitAnnotationsProvider();


    @NbBundle.Messages("PhpUnitAnnotationsProvider.name=PHPUnit")
    private PhpUnitAnnotationsProvider() {
        super("PhpUnit Annotations", // NOI18N
                Bundle.PhpUnitAnnotationsProvider_name(),
                null);
    }

    @AnnotationCompletionTagProvider.Registration(position=200)
    public static PhpUnitAnnotationsProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public List<AnnotationCompletionTag> getFunctionAnnotations() {
        return Arrays.<AnnotationCompletionTag>asList(
                new AssertTag(),
                new BackupGlobalsTag(),
                new BackupStaticAttributesTag(),
                new CodeCoverageIgnoreTag(),
                new CodeCoverageIgnoreStartTag(),
                new CodeCoverageIgnoreEndTag(),
                new RunInSeparateProcessTag(),
                new TestdoxTag());
    }

    @Override
    public List<AnnotationCompletionTag> getTypeAnnotations() {
        return Arrays.<AnnotationCompletionTag>asList(
                new BackupGlobalsTag(),
                new BackupStaticAttributesTag(),
                new CodeCoverageIgnoreTag(),
                new CodeCoverageIgnoreStartTag(),
                new CodeCoverageIgnoreEndTag(),
                new CoversTag(),
                new CoversDefaultClassTag(),
                new CoversNothingTag(),
                new OutputBufferingTag(),
                new RequiresTag(),
                new RunTestsInSeparateProcessesTag(),
                new TestdoxTag());
    }

    @Override
    public List<AnnotationCompletionTag> getFieldAnnotations() {
        return Collections.emptyList();
    }

    @Override
    public List<AnnotationCompletionTag> getMethodAnnotations() {
        return Arrays.<AnnotationCompletionTag>asList(
                new AfterClassTag(),
                new AfterTag(),
                new AssertTag(),
                new AuthorTag(),
                new BackupGlobalsTag(),
                new BackupStaticAttributesTag(),
                new BeforeTag(),
                new BeforeClassTag(),
                new CodeCoverageIgnoreTag(),
                new CodeCoverageIgnoreStartTag(),
                new CodeCoverageIgnoreEndTag(),
                new CoversTag(),
                new CoversNothingTag(),
                new DataProviderTag(),
                new DependsTag(),
                new ExpectedExceptionTag(),
                new ExpectedExceptionCodeTag(),
                new ExpectedExceptionMessageTag(),
                new ExpectedExceptionMessageRegExpTag(),
                new GroupTag(),
                new LargeTag(),
                new MediumTag(),
                new OutputBufferingTag(),
                new PreserveGlobalStateTag(),
                new RunInSeparateProcessTag(),
                new SmallTag(),
                new TestTag(),
                new TestdoxTag(),
                new TicketTag(),
                new UsesTag());
    }

}
