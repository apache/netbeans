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
package org.netbeans.modules.cnd.highlight.hints.formatstring;

/**
 *
 */
class FormatError {
    private final FormatErrorType type;
    private final String flag;
    private final String specifier;
    private final int startOffset;
    private final int endOffset;

    public FormatError(FormatErrorType type, String flag, String specifier, int startOffset, int endOffset) {
        this.type = type;
        this.flag = flag;
        this.specifier = specifier;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public FormatErrorType getType() {
        return type;
    }

    public int startOffset() {
        return startOffset;
    }

    public int endOffset() {
        return endOffset;
    }

    public String getFlag() {
        return flag;
    }

    public String getSpecifier() {
        return specifier;
    }
    
    public static enum FormatErrorType {
        FLAG,           // usage of a flag character that is incompatible with the conversion specifier
        LENGTH,         // usage of a length modifier that is incompatible with the conversion specifier
        TYPE_MISMATCH,  // mismatching the argument type and conversion specifier
        TYPE_WILDCARD,  // type of width or pressision not int
        TYPE_NOTEXIST,  // wrong conversion specifier
        ARGS            // incorrect number of arguments for the format string
    }
}
