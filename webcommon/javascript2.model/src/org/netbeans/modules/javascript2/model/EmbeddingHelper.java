/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javascript2.model;

/**
 *
 * @author Petr Hejl
 */
// FIXME remove this class somehow
public class EmbeddingHelper {

    private static final String GENERATED_IDENTIFIER = "__UNKNOWN__"; // NOI18N

    private static final String NETBEANS_IMPORT_FILE = "__netbeans_import__"; // NOI18N

    public static boolean isGeneratedIdentifier(String ident) {
        return GENERATED_IDENTIFIER.equals(ident) || ident.contains(NETBEANS_IMPORT_FILE);
    }

    public static boolean containsGeneratedIdentifier(String ident) {
        return ident.contains(GENERATED_IDENTIFIER) || ident.contains(NETBEANS_IMPORT_FILE);
    }

}
