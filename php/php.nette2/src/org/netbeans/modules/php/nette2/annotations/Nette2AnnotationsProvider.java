/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.nette2.annotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTag;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class Nette2AnnotationsProvider extends AnnotationCompletionTagProvider {

    @NbBundle.Messages({
        "Nette2AnnotationsName=Nette2",
        "Nette2AnnotationsDescription=Annotations for Nette2 Framework"
    })
    public Nette2AnnotationsProvider() {
        super("Nette2 Annotations", Bundle.Nette2AnnotationsName(), Bundle.Nette2AnnotationsDescription()); //NOI18N
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
        List<AnnotationCompletionTag> result = new ArrayList<AnnotationCompletionTag>();
        result.add(new AnnotationCompletionTag("persistent", "@persistent")); //NOI18N
        return result;
    }

    @Override
    public List<AnnotationCompletionTag> getMethodAnnotations() {
        return Collections.emptyList();
    }

}
