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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.schema2beans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Placing this annotation on a {@code package-info.java} generates classes from a schema.
 * The classes will be generated into the same package.
 * @since org.netbeans.modules.schema2beans/1 1.24
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PACKAGE)
public @interface Schema2Beans {

    /**
     * The schema to process.
     * Might be DTD or XSD format.
     * <p>The file may be specified as a relative or absolute resource path,
     * as in {@link Class#getResource} ({@code ../resources} syntax allowed).
     */
    String schema();

    /**
     * Type of schema being processed.
     */
    SchemaType schemaType();

    /**
     * Type of classes to generate.
     */
    OutputType outputType(); // cannot set default value due to JRE #6512707

    /**
     * The meta-DD file to use ({@code *.mdd}).
     * <p>The file may be specified as a relative or absolute resource path,
     * as in {@link Class#getResource} ({@code ../resources} syntax allowed).
     */
    String mddFile() default "";

    /**
     * Whether to validate.
     */
    boolean validate() default false;

    /**
     * Whether to remove unreferenced nodes.
     */
    boolean removeUnreferencedNodes() default false;

    /**
     * XXX document
     */
    boolean attrProp() default false;

    /**
     * XXX document
     */
    boolean generateHasChanged() default false;

    /**
     * Generate a common interface between all beans.
     */
    String commonInterface() default "";

    /**
     * XXX document
     */
    boolean useInterfaces() default false;

    /**
     * XXX document
     */
    boolean extendBaseBean() default false;

    /**
     * XXX document
     */
    String[] finder() default {};

    /**
     * XXX document
     */
    String docRoot() default "";

    /**
     * XXX document
     */
    boolean generateInterfaces() default false;

    /**
     * XXX document
     */
    boolean standalone() default false;

    /**
     *
     * @return
     * @since 1.36
     */
    boolean java5() default false;

    /**
     * Type of schema being processed.
     * @see #schemaType
     */
    enum SchemaType {
        /** Document type definition. */
        DTD,
        /** XML Schema. */
        XML_SCHEMA
    }

    /**
     * Type of classes to generate.
     * @see #outputType
     */
    enum OutputType {
        /** Force use of {@link BaseBean}. Runtime required. */
        TRADITIONAL_BASEBEAN,
        /** Generate pure JavaBeans that do not need any runtime library support (no {@link BaseBean}). */
        JAVABEANS
    }

    /**
     * Permits multiple schemas to be generated into the same package.
     * <p>The processor must refuse to recreate any given class, so if there might
     * be some conflict, put the preferred schema first in the list.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.PACKAGE)
    @interface Multiple {
        Schema2Beans[] value();
    }

}
