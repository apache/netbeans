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
package org.netbeans.modules.php.codeception.annotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTag;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider;
import org.openide.util.NbBundle;

public final class CodeceptionAnnotationsProvider extends AnnotationCompletionTagProvider {

    private static final CodeceptionAnnotationsProvider INSTANCE = new CodeceptionAnnotationsProvider();


    @NbBundle.Messages("CodeceptionAnnotationsProvider.name=Codeception")
    private CodeceptionAnnotationsProvider() {
        super("Codeception Annotations", // NOI18N
                Bundle.CodeceptionAnnotationsProvider_name(),
                null);
    }

    @AnnotationCompletionTagProvider.Registration(position = 300)
    public static CodeceptionAnnotationsProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public List<AnnotationCompletionTag> getFunctionAnnotations() {
        return Collections.emptyList();
    }

    @Override
    public List<AnnotationCompletionTag> getTypeAnnotations() {
        return Collections.emptyList();
    }

    @Override
    public List<AnnotationCompletionTag> getFieldAnnotations() {
        return Collections.emptyList();
    }

    @Override
    public List<AnnotationCompletionTag> getMethodAnnotations() {
        return Arrays.<AnnotationCompletionTag>asList(
                new AfterTag(),
                new BeforeTag(),
                new EnvTag(),
                new GroupTag(),
                new SkipTag(),
                new TestTag());
    }

}
