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
package org.netbeans.modules.editor.java;

import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.support.ReferencesCount;

/**
 * Characters are lazily computed in charAt(), avoid toString().
 * 
 * Use link() to combine lazy sequences.
 *
 * @author Dusan Balek
 */
final class LazySortText implements CharSequence {

    private final String simpleName;
    private final String enclName;
    private final ElementHandle<TypeElement> handle;
    private final ReferencesCount referencesCount;
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
    @SuppressWarnings("AssignmentToMethodParameter")
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

    @Override
    public String toString() {
        return new StringBuilder(this).toString();
    }

    private String getImportanceLevel() {
        if (importanceLevel == null) {
            importanceLevel = String.format("%8d", Utilities.getImportanceLevel(referencesCount, handle)); //NOI18N
        }
        return importanceLevel;
    }
    
    public static CharSequence link(CharSequence first, CharSequence second) {
        return new LinkedLazyCharSequence(first, second);
    }
    
    public static CharSequence link(CharSequence first, CharSequence second, CharSequence third) {
        return new LinkedLazyCharSequence(first, new LinkedLazyCharSequence(second, third));
    }

    private static final class LinkedLazyCharSequence implements CharSequence {

        private final CharSequence first;
        private final CharSequence second;
        private final int length;

        private LinkedLazyCharSequence(CharSequence first, CharSequence second) {
            this.first = first;
            this.second = second;
            this.length = first.length() + second.length() + 1;
        }

        @Override
        @SuppressWarnings("AssignmentToMethodParameter")
        public char charAt(int index) {
            if (index < first.length()) {
                return first.charAt(index);
            }
            index -= first.length();
            if (index-- == 0) {
                return '#';
            }
            return second.charAt(index);
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

    }

}
