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
