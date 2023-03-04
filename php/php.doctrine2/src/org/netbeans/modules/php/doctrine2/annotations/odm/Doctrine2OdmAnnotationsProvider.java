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
package org.netbeans.modules.php.doctrine2.annotations.odm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTag;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider;
import org.openide.util.NbBundle;

// http://docs.doctrine-project.org/projects/doctrine-mongodb-odm/en/latest/reference/annotations-reference.html
public final class Doctrine2OdmAnnotationsProvider extends AnnotationCompletionTagProvider {

    @NbBundle.Messages("Doctrine2OdmAnnotationsProvider.name=Doctrine2 ODM")
    public Doctrine2OdmAnnotationsProvider() {
        super("Doctrine2 ODM Annotations", // NOI18N
                Bundle.Doctrine2OdmAnnotationsProvider_name(),
                null);
    }

    @Override
    public List<AnnotationCompletionTag> getFunctionAnnotations() {
        return Collections.emptyList();
    }

    @Override
    public List<AnnotationCompletionTag> getTypeAnnotations() {
        return Arrays.<AnnotationCompletionTag>asList(
                new DiscriminatorFieldTag(),
                new DiscriminatorMapTag(),
                new DocumentTag(),
                new EmbeddedDocumentTag(),
                new IndexTag(),
                new InheritanceTypeTag(),
                new MappedSuperclassTag());
    }

    @Override
    public List<AnnotationCompletionTag> getFieldAnnotations() {
        return Arrays.<AnnotationCompletionTag>asList(
                new AlsoLoadTag(),
                new BinTag(),
                new BinCustomTag(),
                new BinFuncTag(),
                new BinMd5Tag(),
                new BinUuidTag(),
                new BooleanTag(),
                new CollectionTag(),
                new DateTag(),
                new DistanceTag(),
                new EmbedManyTag(),
                new EmbedOneTag(),
                new FieldTag(),
                new FileTag(),
                new FloatTag(),
                new HashTag(),
                new IdTag(),
                new IncrementTag(),
                new IndexTag(),
                new IntTag(),
                new KeyTag(),
                new NotSavedTag(),
                new ReferenceManyTag(),
                new ReferenceOneTag(),
                new StringTag(),
                new TimestampTag(),
                new UniqueIndexTag());
    }

    @Override
    public List<AnnotationCompletionTag> getMethodAnnotations() {
        return Arrays.<AnnotationCompletionTag>asList(
                new PreLoadTag(),
                new PostLoadTag(),
                new PostPersistTag(),
                new PostRemoveTag(),
                new PostUpdateTag(),
                new PrePersistTag(),
                new PreRemoveTag(),
                new PreUpdateTag());
    }

}
