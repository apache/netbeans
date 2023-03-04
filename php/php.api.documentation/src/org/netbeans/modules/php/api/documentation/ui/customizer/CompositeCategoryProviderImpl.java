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
package org.netbeans.modules.php.api.documentation.ui.customizer;

import javax.swing.JComponent;
import org.netbeans.modules.php.api.documentation.PhpDocumentations;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public final class CompositeCategoryProviderImpl  implements ProjectCustomizer.CompositeCategoryProvider {

    @NbBundle.Messages("CompositeCategoryProviderImpl.documentation.title=Documentation")
    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        PhpModule phpModule = PhpModule.Factory.lookupPhpModule(context);
        if (phpModule == null) {
            throw new IllegalStateException("PHP module must be found in context: " + context);
        }
        return ProjectCustomizer.Category.create(
                PhpDocumentations.CUSTOMIZER_IDENT,
                Bundle.CompositeCategoryProviderImpl_documentation_title(),
                null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        if (PhpDocumentations.CUSTOMIZER_IDENT.equals(category.getName())) {
            PhpModule phpModule = PhpModule.Factory.lookupPhpModule(context);
            assert phpModule != null : "Cannot find php module in lookup: " + context;
            return new CustomizerDocumentation(category, phpModule);
        }
        return null;
    }

}
