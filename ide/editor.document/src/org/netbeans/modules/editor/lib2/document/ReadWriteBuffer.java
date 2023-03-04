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

import org.netbeans.api.annotations.common.NonNull;

/**
 * Character data and possibly a line separator.
 *
 * @author Miloslav Metelka
 * @since 1.46
 */
public final class ReadWriteBuffer {

    char[] text;
    
    int length;
    
    public ReadWriteBuffer(@NonNull char[] text, int length) {
        assert (length >= 0) : "length=" + length + " < 0"; // NOI18N
        this.text = text;
        this.length = length;
    }

    /**
     * Length of the valid data in {@link #text()} (starting from index 0).
     *
     * @return length of valid data.
     */
    public int length() {
        return length;
    }

    /**
     * Length of the valid data in {@link #text()} (starting from index 0).
     *
     * @return length of valid data.
     */
    public char[] text() {
        return text;
    }

    @Override
    public String toString() {
        return new String(text, 0, length);
    }

}
