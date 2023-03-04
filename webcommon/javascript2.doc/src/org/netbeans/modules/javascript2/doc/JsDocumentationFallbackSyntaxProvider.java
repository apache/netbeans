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
package org.netbeans.modules.javascript2.doc;

import org.netbeans.modules.javascript2.doc.spi.SyntaxProvider;

/**
 * Default implementation of the syntax provider.
 * <p>
 * Supported syntax by this provider is JsDoc's (as a one of the most used) one.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class JsDocumentationFallbackSyntaxProvider implements SyntaxProvider {

    public JsDocumentationFallbackSyntaxProvider() {
    }

    @Override
    public String typesSeparator() {
        return "|"; //NOI18N
    }

    @Override
    public String paramTagTemplate() {
        return "@param {" + TYPE_PLACEHOLDER + "} " + NAME_PLACEHOLDER;
    }

    @Override
    public String returnTagTemplate() {
        return "@return {" + TYPE_PLACEHOLDER + "}";
    }

    @Override
    public String typeTagTemplate() {
        return "@type {" + TYPE_PLACEHOLDER + "}";
    }
}
