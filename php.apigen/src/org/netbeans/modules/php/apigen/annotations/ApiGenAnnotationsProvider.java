/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
