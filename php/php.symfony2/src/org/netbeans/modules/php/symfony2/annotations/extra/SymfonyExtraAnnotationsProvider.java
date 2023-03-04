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
package org.netbeans.modules.php.symfony2.annotations.extra;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTag;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider;
import org.openide.util.NbBundle;

// http://symfony.com/doc/2.0/bundles/SensioFrameworkExtraBundle/index.html#annotations-for-controllers
public final class SymfonyExtraAnnotationsProvider extends AnnotationCompletionTagProvider {

    @NbBundle.Messages("SymfonyExtraAnnotationsProvider.name=Symfony 2/3 Extra")
    public SymfonyExtraAnnotationsProvider() {
        super("Symfony 2/3 Extra Annotations", // NOI18N
                Bundle.SymfonyExtraAnnotationsProvider_name(),
                null);
    }

    @Override
    public List<AnnotationCompletionTag> getFunctionAnnotations() {
        return Collections.emptyList();
    }

    @Override
    public List<AnnotationCompletionTag> getTypeAnnotations() {
        return Arrays.<AnnotationCompletionTag>asList(
                new RouteTag(),
                new CacheTag());
    }

    @Override
    public List<AnnotationCompletionTag> getFieldAnnotations() {
        return Collections.emptyList();
    }

    @Override
    public List<AnnotationCompletionTag> getMethodAnnotations() {
        return Arrays.<AnnotationCompletionTag>asList(
                new RouteTag(),
                new MethodTag(),
                new ParamConverterTag(),
                new TemplateTag(),
                new CacheTag());
    }

}
