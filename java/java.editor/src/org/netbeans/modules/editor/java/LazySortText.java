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
package org.netbeans.modules.editor.java;

import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.support.ReferencesCount;

/**
 *
 * @author Dusan Balek
 */
class LazySortText implements CharSequence {

    private String simpleName;
    private String enclName;
    private ElementHandle<TypeElement> handle;
    private ReferencesCount referencesCount;
    private String importanceLevel = null;

    LazySortText(String simpleName, String enclName, ElementHandle<TypeElement> handle, ReferencesCount referencesCount) {
        this.simpleName = simpleName;
        this.enclName = enclName != null ? Utilities.getImportanceLevel(enclName) + "#" + enclName : ""; //NOI18N
        this.handle = handle;
        this.referencesCount = referencesCount;
    }

    @Override
    public int length() {
        return simpleName.length() + enclName.length() + 10;
    }

    @Override
    public char charAt(int index) {
        if ((index < 0) || (index >= length())) {
            throw new StringIndexOutOfBoundsException(index);
        }
        if (index < simpleName.length()) {
            return simpleName.charAt(index);
        }
        index -= simpleName.length();
        if (index-- == 0) {
            return '#';
        }
        if (index < 8) {
            return getImportanceLevel().charAt(index);
        }
        index -= 8;
        if (index-- == 0) {
            return '#';
        }
        return enclName.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        throw new UnsupportedOperationException("Not supported yet."); //NOI18N
    }

    private String getImportanceLevel() {
        if (importanceLevel == null) {
            importanceLevel = String.format("%8d", Utilities.getImportanceLevel(referencesCount, handle)); //NOI18N
        }
        return importanceLevel;
    }
}
