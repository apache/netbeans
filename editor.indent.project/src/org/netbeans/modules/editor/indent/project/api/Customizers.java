/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
