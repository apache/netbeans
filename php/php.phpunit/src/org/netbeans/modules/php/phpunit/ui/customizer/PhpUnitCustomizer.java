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
package org.netbeans.modules.php.phpunit.ui.customizer;

import javax.swing.JComponent;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.phpunit.PhpUnitTestingProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Project customizer for PhpUnit.
 */
public class PhpUnitCustomizer implements ProjectCustomizer.CompositeCategoryProvider {

    public static final String IDENTIFIER = PhpUnitTestingProvider.getInstance().getIdentifier();

    private final PhpModule phpModule;


    public PhpUnitCustomizer(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
    }

    @NbBundle.Messages("PhpUnitCustomizer.name=PHPUnit")
    @Override
    public Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(
                IDENTIFIER,
                Bundle.PhpUnitCustomizer_name(),
                null,
                (ProjectCustomizer.Category[]) null);
    }

    @Override
    public JComponent createComponent(Category category, Lookup context) {
        return new CustomizerPhpUnit(category, phpModule);
    }

}
