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
package org.netbeans.modules.php.symfony2.annotations.validators;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTag;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider;
import org.openide.util.NbBundle;

// http://symfony.com/doc/current/book/validation.html#validation-constraints
public class SymfonyValidatorsAnnotationsProvider extends AnnotationCompletionTagProvider {

    @NbBundle.Messages("SymfonyValidatorsAnnotationsProvider.name=Symfony 2/3 Validators")
    public SymfonyValidatorsAnnotationsProvider() {
        super("Symfony 2/3 Validators Annotations", // NOI18N
                Bundle.SymfonyValidatorsAnnotationsProvider_name(),
                null);
    }

    @Override
    public List<AnnotationCompletionTag> getFunctionAnnotations() {
        return Collections.emptyList();
    }

    @Override
    public List<AnnotationCompletionTag> getTypeAnnotations() {
        return Arrays.<AnnotationCompletionTag>asList(
                new UniqueEntityTag(),
                new CallbackTag());
    }

    @Override
    public List<AnnotationCompletionTag> getFieldAnnotations() {
        return getFieldOrMethodValidatorTags();
    }

    @Override
    public List<AnnotationCompletionTag> getMethodAnnotations() {
        return getFieldOrMethodValidatorTags();
    }

    private List<AnnotationCompletionTag> getFieldOrMethodValidatorTags() {
        return Arrays.<AnnotationCompletionTag>asList(
                new NotBlankTag(),
                new BlankTag(),
                new NotNullTag(),
                new NullTag(),
                new TrueTag(),
                new FalseTag(),
                new TypeTag(),
                new EmailTag(),
                new MinLengthTag(),
                new MaxLengthTag(),
                new LengthTag(),
                new UrlTag(),
                new RegexTag(),
                new IpTag(),
                new MaxTag(),
                new MinTag(),
                new RangeTag(),
                new DateTag(),
                new DateTimeTag(),
                new TimeTag(),
                new ChoiceTag(),
                new CollectionTag(),
                new CountTag(),
                new LanguageTag(),
                new LocaleTag(),
                new CountryTag(),
                new FileTag(),
                new ImageTag(),
                new AllTag(),
                new UserPasswordTag(),
                new ValidTag());
    }

}
