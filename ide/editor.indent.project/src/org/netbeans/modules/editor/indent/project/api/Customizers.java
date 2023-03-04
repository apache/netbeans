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

package org.netbeans.modules.editor.indent.project.api;

import java.util.Map;
import org.netbeans.modules.editor.indent.project.FormattingCustomizerPanel.Factory;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;

/**
 *
 * @author Vita Stejskal
 */
public final class Customizers {

    private Customizers() {
        // no-op
    }

    /**
     * Creates an instance of the 'Formatting' category in the project properties dialog.
     * This method is meant to be used from XML layers by modules that wish to add
     * the 'Formatting' category to their project type's properties dialog.
     *
     * <p>The method recognizes 'allowedMimeTypes' XML layer attribute, which should
     * contain the comma separated list of mime types, which formatting settings
     * customizers should be made available for the project. If the attribute is
     * not specified all registered customizers are shown. If the attribute specifies
     * an empty list only the 'All Languages' customizer is shown.
     *
     * <p>May also be used from an annotation on a method in any class in your module, e.g.:
     * <pre>
    &#64;ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType="...", position=1000,
        category="Formatting", categoryLabel="#LBL_CategoryFormatting")
    &#64;NbBundle.Messages("LBL_CategoryFormatting=Formatting")
    public static ProjectCustomizer.CompositeCategoryProvider formatting() {
        return Customizers.createFormattingCategoryProvider(Collections.emptyMap());
    }
     * </pre>
     *
     * @param attrs The map of <code>FileObject</code> attributes
     *
     * @return A new 'Formatting' category provider.
     * @since 1.0
     */
    public static ProjectCustomizer.CompositeCategoryProvider createFormattingCategoryProvider(Map attrs) {
        return new Factory((String)attrs.get("allowedMimeTypes")); //NOI18N
    }
}
