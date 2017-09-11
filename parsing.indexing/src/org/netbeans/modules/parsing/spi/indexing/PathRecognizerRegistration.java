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

package org.netbeans.modules.parsing.spi.indexing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.netbeans.api.java.classpath.GlobalPathRegistry;

/**
 * Registers a <code>PathRecognizer</code> in the default <code>Lookup</code>.
 * 
 * <p class="nonnormative">
 * This annotation can be added to any type, but typically you should add it to an
 * indexer factory or a CSL language definition.
 *
 * @author Vita Stejskal
 * @since 1.32
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface PathRecognizerRegistration {

    /**
     * Gets classpath IDs for source paths registered in
     * the {@link GlobalPathRegistry}.
     *
     * @return The list of source path IDs; <code>"ANY"</code> means any source path ID
     *   and an empty array (<code>{}</code>) means no source path ID.
     */
    public String [] sourcePathIds() default "ANY"; //NOI18N

    /**
     * Gets classpath IDs for library paths registered in
     * the {@link GlobalPathRegistry}.
     *
     * @return The list of source path IDs; <code>"ANY"</code> means any source path ID
     *   and an empty array (<code>{}</code>) means no source path ID.
     */
    public String [] libraryPathIds() default "ANY"; //NOI18N

    /**
     * Gets classpath IDs for binray library paths registered in
     * the {@link GlobalPathRegistry}.
     *
     * @return The list of source path IDs; <code>"ANY"</code> means any source path ID
     *   and an empty array (<code>{}</code>) means no source path ID.
     */
    public String [] binaryLibraryPathIds() default "ANY"; //NOI18N

    /**
     * Gets mime types of files relevant for the paths identified by the other methods.
     *
     * @return The list of mime types;  <code>null</code>, an empty array (<code>{}</code>)
     *   and empty strings (<code>""</code>) are ignored.
     */
    public String [] mimeTypes() default {};
}
