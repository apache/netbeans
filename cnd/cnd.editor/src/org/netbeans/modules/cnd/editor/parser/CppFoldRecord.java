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

package org.netbeans.modules.cnd.editor.parser;

import org.netbeans.modules.cnd.editor.folding.CppFile;

public final class CppFoldRecord {

    // Fold types
    public final static int INITIAL_COMMENT_FOLD = CppFile.INITIAL_COMMENT_FOLD;
    public final static int BLOCK_COMMENT_FOLD = CppFile.BLOCK_COMMENT_FOLD;
    public final static int COMMENTS_FOLD = CppFile.COMMENTS_FOLD;    
    public final static int INCLUDES_FOLD = CppFile.INCLUDES_FOLD;
    public final static int IFDEF_FOLD = CppFile.IFDEF_FOLD;
    public final static int CLASS_FOLD = CppFile.CLASS_FOLD;
    public final static int FUNCTION_FOLD = CppFile.FUNCTION_FOLD;
    public final static int CONSTRUCTOR_FOLD = CppFile.CONSTRUCTOR_FOLD;
    public final static int DESTRUCTOR_FOLD = CppFile.DESTRUCTOR_FOLD;
    public final static int NAMESPACE_FOLD = CppFile.NAMESPACE_FOLD;
    public final static int COMPOUND_BLOCK_FOLD = CppFile.COMPOUND_BLOCK_FOLD;
    
    private final int type;
    private final int startOffset;
    private final int endOffset;

    public CppFoldRecord(int type, int startLnum, int startOffset, int endLnum, int endOffset)
    {
        this.type = type;
	this.startOffset = startOffset;
	this.endOffset = endOffset;
    }

    public CppFoldRecord(int type, int startOffset, int endOffset)
    {
	this.type = type;
	this.startOffset = startOffset;
	this.endOffset = endOffset;
    }
    
    public int getType() {
	return type;
    }

    public int getStartOffset() {
	return startOffset;
    }

    public int getEndOffset() {
	return endOffset;
    }
    
    @Override
    public String toString() {
        // I'm considering this as a debug function and making all
	// strings NOI18N
        
        String kind = "Unknown Fold"; // NOI18N
        switch (type) {
            case INITIAL_COMMENT_FOLD:
                kind = "INITIAL_COMMENT_FOLD"; // NOI18N
                break;
            case BLOCK_COMMENT_FOLD:
                kind = "BLOCK_COMMENT_FOLD"; // NOI18N
                break;
            case COMMENTS_FOLD:
                kind = "COMMENTS_FOLD"; // NOI18N
                break;
            case INCLUDES_FOLD:
                kind = "INCLUDES_FOLD"; // NOI18N
                break;
            case IFDEF_FOLD:
                kind = "IFDEF_FOLD"; // NOI18N
                break;
            case CLASS_FOLD:
                kind = "CLASS_FOLD"; // NOI18N
                break;
            case FUNCTION_FOLD:
                kind = "FUNCTION_FOLD"; // NOI18N
                break;
            case CONSTRUCTOR_FOLD:
                kind = "CONSTRUCTOR_FOLD"; // NOI18N
                break;
            case DESTRUCTOR_FOLD:
                kind = "DESTRUCTOR_FOLD"; // NOI18N
                break;
            case NAMESPACE_FOLD:
                kind = "NAMESPACE_FOLD"; // NOI18N
                break;
            case COMPOUND_BLOCK_FOLD:
                kind = "COMPOUND_BLOCK_FOLD"; // NOI18N
                break;
            default:
        }
	return kind + " (" + + startOffset + ", " + endOffset + ")"; // NOI18N
    }
}
