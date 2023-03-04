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
package org.netbeans.modules.php.project.annotations;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTag;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider;
import org.openide.util.NbBundle;

/**
 * Provider for user global PHP annotations.
 */
public final class GlobalUserAnnotationsProvider extends AnnotationCompletionTagProvider {

    private static final GlobalUserAnnotationsProvider INSTANCE = new GlobalUserAnnotationsProvider();


    @NbBundle.Messages("GlobalUserAnnotationsProvider.name=Custom")
    private GlobalUserAnnotationsProvider() {
        super("Global User Annotations", // NOI18N
                Bundle.GlobalUserAnnotationsProvider_name(),
                null);
    }

    @AnnotationCompletionTagProvider.Registration(position=1000)
    public static GlobalUserAnnotationsProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public List<AnnotationCompletionTag> getFunctionAnnotations() {
        return getAnnotationsForType(UserAnnotationTag.Type.FUNCTION);
    }

    @Override
    public List<AnnotationCompletionTag> getTypeAnnotations() {
        return getAnnotationsForType(UserAnnotationTag.Type.TYPE);
    }

    @Override
    public List<AnnotationCompletionTag> getFieldAnnotations() {
        return getAnnotationsForType(UserAnnotationTag.Type.FIELD);
    }

    @Override
    public List<AnnotationCompletionTag> getMethodAnnotations() {
        return getAnnotationsForType(UserAnnotationTag.Type.METHOD);
    }

    private List<AnnotationCompletionTag> getAnnotationsForType(UserAnnotationTag.Type type) {
        List<UserAnnotationTag> annotations = UserAnnotations.getGlobal().getAnnotations();
        List<AnnotationCompletionTag> result = new ArrayList<>(annotations.size());
        for (UserAnnotationTag userAnnotationTag : annotations) {
            if (userAnnotationTag.getTypes().contains(type)) {
                result.add(userAnnotationTag);
            }
        }
        return result;
    }

}
