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

package org.netbeans.lib.lexer.token;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.CharSequenceUtilities;

/**
 * Char sequence over join token parts.
 * 
 * @author Miloslav Metelka
 */

public final class JoinTokenText<T extends TokenId> implements CharSequence {
    
    private static final Logger LOG = Logger.getLogger(JoinTokenText.class.getName());

    private final List<PartToken<T>> joinedParts;

    private final int length;
    
    private int activePartIndex;
    
    private CharSequence activeInputText;

    private int activeStartCharIndex;
    
    private int activeEndCharIndex;
    
    public JoinTokenText(List<PartToken<T>> joinedParts, int length) {
        this.joinedParts = joinedParts;
        this.activeInputText = joinedParts.get(0).text();
        // Implicit: this.activeStartCharIndex = 0;
        this.activeEndCharIndex = activeInputText.length();
        this.length = length;
    }

    @Override
    public synchronized char charAt(int index) {
        if (index < activeStartCharIndex) { // Find non-empty previous
            if (index < 0)
                throw new IndexOutOfBoundsException("index=" + index + " < 0");
            do {
                activePartIndex--;
                if (activePartIndex < 0) { // Should never happen
                    LOG.log(Level.WARNING, "Internal error: index=" + index + ", " + dumpState());
                }
                activeInputText = joinedParts.get(activePartIndex).text();
                int len = activeInputText.length();
                activeEndCharIndex = activeStartCharIndex;
                activeStartCharIndex -= len;
            } while (index < activeStartCharIndex);
        } else if (index >= activeEndCharIndex) { // Find non-empty next
            if (index >= length)
                throw new IndexOutOfBoundsException("index=" + index + " >= length()=" + length);
            do {
                activePartIndex++;
                activeInputText = joinedParts.get(activePartIndex).text();
                int len = activeInputText.length();
                activeStartCharIndex = activeEndCharIndex;
                activeEndCharIndex += len;
            } while (index >= activeEndCharIndex);
        }

        // Valid char within current segment
        return activeInputText.charAt(index - activeStartCharIndex);
    }

    @Override
    public int length() {
        return length;
    }
    
    @Override
    public CharSequence subSequence(int start, int end) {
        return CharSequenceUtilities.toString(this, start, end);
    }
    
    @Override
    public synchronized String toString() {
        return CharSequenceUtilities.toString(this);
    }
    
    private String dumpState() {
        return "activeTokenListIndex=" + activePartIndex +
                ", activeStartCharIndex=" + activeStartCharIndex +
                ", activeEndCharIndex=" + activeEndCharIndex +
                ", length=" + length;
    }

}
