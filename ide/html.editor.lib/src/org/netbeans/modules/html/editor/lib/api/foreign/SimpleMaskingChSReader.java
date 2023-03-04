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
package org.netbeans.modules.html.editor.lib.api.foreign;

import java.io.IOException;

/**
 * An implementations which reads a CharSequence, and masks all @@@ chars by
 * whistespaces
 *
 * Note: if one skips the reader into middle of the templating mark, it will not
 * be masked!
 *
 * @author marekfukala
 */
public class SimpleMaskingChSReader extends CharSequenceReader {

    private int maskPos = 0;
    private int markMaskPos = 0;

    private char PATTERN_CHAR = '@'; //NOI18N
    protected char MASK_CHAR = ' '; //NOI18N
    
    public SimpleMaskingChSReader(CharSequence immutableCharSequence) {
        super(immutableCharSequence);
    }

    @Override
    protected char processReadChar(char c) throws IOException {
        if (c == PATTERN_CHAR) {
            char r[] = new char[2];
            r[0] = next < length ? source.charAt(next) : 0;
            r[1] = next + 1 < length ? source.charAt(next + 1) : 0;
            
            switch (maskPos) {
                case 0:
                    if (r[0] == PATTERN_CHAR && r[1] == PATTERN_CHAR) {
                        c = MASK_CHAR;
                        maskPos = 1;
                    }
                    break;
                case 1:
                    if (r[0] == PATTERN_CHAR) {
                        c = MASK_CHAR;
                        maskPos = 2;
                    }
                    break;
                case 2:
                    c = MASK_CHAR;
                    maskPos = 0;
                    break;
            }
        } else {
            maskPos = 0;
        }

        return c;

    }

    @Override
    protected void markedAt(int mark) {
        markMaskPos = maskPos;
    }

    @Override
    protected void inputReset() {
        maskPos = markMaskPos;
    }
}
