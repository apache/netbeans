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
package org.netbeans.modules.javascript2.doc.spi;

import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/**
 * Encapsulates a JS annotations completion provider.
 *
 * <p>This class allows providing support for completion of JS annotations.</p>
 *
 * <p>Annotations are available per every {@code JsDocumentationProvider}. For <b>framework specific</b> annotations, use
 * {@link org.netbeans.modules.javascript2.editor.doc.spi.JsDocumentationProvider#getAnnotationsProvider()}.</p>

 * @see org.netbeans.modules.javascript2.editor.doc.spi.JsDocumentationProvider#getAnnotationsProvider()
 */
public abstract class AnnotationCompletionTagProvider {

    private final String name;

    /**
     * Create a new JS annotations provider with a name.
     *
     * @param  name <b>short, localized</b> name of this JS annotations provider (e.g., "JsDoc");
     *         never {@code null}
     * @throws NullPointerException if the {@code name} parameter is {@code null}
     */
    public AnnotationCompletionTagProvider(@NonNull String name) {
        Parameters.notNull("name", name); // NOI18N
        this.name = name;
    }

    /**
     * Get the <b>short, localized</b> name of this JS annotations provider.
     *
     * @return name; never {@code null}
     */
    public final String getName() {
        return name;
    }

    /**
     * Get all supported annotations.
     * 
     * @return all supported annotations
     */
    public abstract List<AnnotationCompletionTag> getAnnotations();

}