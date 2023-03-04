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
