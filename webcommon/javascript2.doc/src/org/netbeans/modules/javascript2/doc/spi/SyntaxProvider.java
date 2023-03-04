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
package org.netbeans.modules.javascript2.doc.spi;

/**
 * Provides information about documentation tools syntax.
 * <p>
 * Knows i.e. delimiters in more possible typed types, brackets at various tags, etc.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public interface SyntaxProvider {

    static String TYPE_PLACEHOLDER = "[type]";
    static String NAME_PLACEHOLDER = "[name]";

    /**
     * Gets the types separator.
     * <p>
     * I.e.: "|" in JsDoc or "/" in extDoc
     *
     * @return separator if any, {@code null) in case that multi-types are not supported by the doc tool
     */
    String typesSeparator();

    /**
     * Returns the param tag template.
     * <p>
     * There are patterns which will be replaced by the real value:
     * [type] - parameter type or types separated by {@link #typesSeparator()}
     * [name] - parameter name
     *
     * <i>The final string could look like this one: "@param {[type]} [name]"</i>
     * @return template for the parameter tag
     */
    String paramTagTemplate();

    /**
     * Returns the return tag template.
     * <p>
     * There are patterns which will be replaced by the real value:
     * [type] - parameter type or types separated by {@link #typesSeparator()}
     *
     * <i>The final string could look like this one: "@return {[type]}"</i>
     * @return template for the return tag
     */
    String returnTagTemplate();

    /**
     * Returns the type tag template.
     * <p>
     * There are patterns which will be replaced by the real value:
     * [type] - parameter type or types separated by {@link #typesSeparator()}
     *
     * <i>The final string could look like this one: "@type {[type]}"</i>
     * @return template for the type tag
     */
    String typeTagTemplate();

}
