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
package org.netbeans.modules.php.doctrine2.annotations.orm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTag;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider;
import org.openide.util.NbBundle;

// http://docs.doctrine-project.org/projects/doctrine-orm/en/latest/reference/annotations-reference.html
public final class Doctrine2OrmAnnotationsProvider extends AnnotationCompletionTagProvider {

    @NbBundle.Messages("Doctrine2OrmAnnotationsProvider.name=Doctrine2 ORM")
    public Doctrine2OrmAnnotationsProvider() {
        super("Doctrine2 ORM Annotations", // NOI18N
                Bundle.Doctrine2OrmAnnotationsProvider_name(),
                null);
    }

    @Override
    public List<AnnotationCompletionTag> getFunctionAnnotations() {
        return Collections.emptyList();
    }

    @Override
    public List<AnnotationCompletionTag> getTypeAnnotations() {
        return Arrays.<AnnotationCompletionTag>asList(
                new ChangeTrackingPolicyTag(),
                new DiscriminatorColumnTag(),
                new DiscriminatorMapTag(),
                new EntityTag(),
                new HasLifecycleCallbacksTag(),
                new IndexTag(),
                new InheritanceTypeTag(),
                new ManyToManyTag(),
                new MappedSuperclassTag(),
                new TableTag(),
                new UniqueConstraintTag());
    }

    @Override
    public List<AnnotationCompletionTag> getFieldAnnotations() {
        return Arrays.<AnnotationCompletionTag>asList(
                new ColumnTag(),
                new GeneratedValueTag(),
                new IdTag(),
                new JoinColumnTag(),
                new JoinColumnsTag(),
                new JoinTableTag(),
                new ManyToOneTag(),
                new ManyToManyTag(),
                new OneToOneTag(),
                new OneToManyTag(),
                new OrderByTag(),
                new SequenceGeneratorTag(),
                new VersionTag());
    }

    @Override
    public List<AnnotationCompletionTag> getMethodAnnotations() {
        return Arrays.<AnnotationCompletionTag>asList(
                new PostLoadTag(),
                new PostPersistTag(),
                new PostRemoveTag(),
                new PostUpdateTag(),
                new PrePersistTag(),
                new PreRemoveTag(),
                new PreUpdateTag());
    }

}
