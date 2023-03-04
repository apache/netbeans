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
package org.netbeans.modules.editor.lib2.document;

import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;

/**
 * 
 *
 * @author mmetelka
 */
public abstract class EditorCharacterServices {

    /**
     * Get identifier's end offset for forward direction (or identifier's start offset for backward direction).
     *
     * @param doc non-null document to check.
     * @param offset offset where search should start (for backward direction char at offset-1 is the first to check).
     * @param backward false for forward direction or true for backward direction.
     * @return identifier's end offset (or start offset for backward direction).
     */
    public abstract int getIdentifierEnd(@NonNull Document doc, int offset, boolean backward);
    
}
