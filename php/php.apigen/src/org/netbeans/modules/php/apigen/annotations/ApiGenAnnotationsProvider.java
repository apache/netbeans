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
package org.netbeans.modules.php.apigen.annotations;

import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTag;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ApiGenAnnotationsProvider extends AnnotationCompletionTagProvider {

    private static final ApiGenAnnotationsProvider INSTANCE = new ApiGenAnnotationsProvider();

    @Messages("ApiGenAnnotationsProvider.name=ApiGen")
    private ApiGenAnnotationsProvider() {
        super("ApiGen Annotations", // NOI18N
                Bundle.ApiGenAnnotationsProvider_name(),
                null);
    }

    @AnnotationCompletionTagProvider.Registration(position=100)
    public static ApiGenAnnotationsProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public List<AnnotationCompletionTag> getFunctionAnnotations() {
        return Arrays.<AnnotationCompletionTag>asList(
                new AccessTag(),
                AuthorTag.create(),
                new CategoryTag(),
                new CopyrightTag(),
                new DeprecatedTag(),
                new ExampleTag(),
                new GlobalTag(),
                new IgnoreTag(),
                new InternalTag(),
                new LicenseTag(),
                new LinkTag(),
                new NameTag(),
                new PackageTag(),
                new ParamTag(),
                new ReturnTag(),
                new SeeTag(),
                new SinceTag(),
                new StaticvarTag(),
                new SubpackageTag(),
                new ThrowsTag(),
                new TodoTag(),
                new TutorialTag(),
                new UsesTag(),
                new VersionTag());
    }

    @Override
    public List<AnnotationCompletionTag> getTypeAnnotations() {
        return Arrays.<AnnotationCompletionTag>asList(
                new AbstractTag(),
                new AccessTag(),
                AuthorTag.create(),
                new CategoryTag(),
                new CopyrightTag(),
                new DeprecatedTag(),
                new ExampleTag(),
                new FilesourceTag(),
                new GlobalTag(),
                new IgnoreTag(),
                new InheritDocTag(),
                new InternalTag(),
                new LicenseTag(),
                new LinkTag(),
                new MethodTag(),
                new NameTag(),
                new PackageTag(),
                new PropertyTag(),
                new PropertyReadTag(),
                new PropertyWriteTag(),
                new SeeTag(),
                new SinceTag(),
                new SubpackageTag(),
                new TodoTag(),
                new TutorialTag(),
                new UsesTag(),
                new VersionTag());
    }

    @Override
    public List<AnnotationCompletionTag> getFieldAnnotations() {
        return Arrays.<AnnotationCompletionTag>asList(
                new AbstractTag(),
                new AccessTag(),
                AuthorTag.create(),
                new CategoryTag(),
                new CopyrightTag(),
                new DeprecatedTag(),
                new ExampleTag(),
                new GlobalTag(),
                new IgnoreTag(),
                new InheritDocTag(),
                new InternalTag(),
                new LicenseTag(),
                new LinkTag(),
                new NameTag(),
                new SeeTag(),
                new SinceTag(),
                new StaticTag(),
                new TodoTag(),
                new TutorialTag(),
                new UsesTag(),
                new VarTag(),
                new VersionTag());
    }

    @Override
    public List<AnnotationCompletionTag> getMethodAnnotations() {
        return Arrays.<AnnotationCompletionTag>asList(
                new AbstractTag(),
                new AccessTag(),
                AuthorTag.create(),
                new CategoryTag(),
                new CopyrightTag(),
                new DeprecatedTag(),
                new ExampleTag(),
                new FinalTag(),
                new GlobalTag(),
                new IgnoreTag(),
                new InheritDocTag(),
                new InternalTag(),
                new LicenseTag(),
                new LinkTag(),
                new NameTag(),
                new ParamTag(),
                new ReturnTag(),
                new SeeTag(),
                new SinceTag(),
                new StaticTag(),
                new StaticvarTag(),
                new ThrowsTag(),
                new TodoTag(),
                new TutorialTag(),
                new UsesTag(),
                new VersionTag());
    }

}
