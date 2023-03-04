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
package org.netbeans.modules.web.webkit.debugging.api.css;

import org.json.simple.JSONObject;

/**
 * Text range within a resource.
 *
 * @author Jan Stola
 */
public class SourceRange {
    /** Start offset of the range (inclusive). */
    private final int start;
    /** End offset of the range (exclusive). */
    private final int end;
    /** Start line of the range. */
    private final int startLine;
    /** Start column of the range (inclusive). */
    private final int startColumn;
    /** End line of the range. */
    private final int endLine;
    /** End column of the range (exclusive). */
    private final int endColumn;

    /**
     * Creates a new {@code SourceRange} that corresponds to the given JSONObject.
     *
     * @param range JSONObject describing the source range.
     */
    SourceRange(JSONObject range) {
        if (range.containsKey("start")) { // NOI18N
            start = ((Number)range.get("start")).intValue(); // NOI18N
            end = ((Number)range.get("end")).intValue(); // NOI18N
        } else {
            start = end = -1;
        }
        if (range.containsKey("startLine")) { // NOI18N
            startLine = ((Number)range.get("startLine")).intValue(); // NOI18N
            startColumn = ((Number)range.get("startColumn")).intValue(); // NOI18N
            endLine = ((Number)range.get("endLine")).intValue(); // NOI18N
            endColumn = ((Number)range.get("endColumn")).intValue(); // NOI18N
        } else {
            startLine = startColumn = endLine = endColumn = -1;
        }
    }

    /**
     * Returns the start offset of the range (inclusive).
     *
     * @return start of the range (inclusive).
     */
    public int getStart() {
        return start;
    }

    /**
     * Returns the end offset of the range (exclusive).
     *
     * @return end of the range (exclusive).
     */
    public int getEnd() {
        return end;
    }

    /**
     * Returns the start line of the range.
     * 
     * @return start line of the range.
     */
    public int getStartLine() {
        return startLine;
    }

    /**
     * Returns the start column of the range (inclusive).
     * 
     * @return start column of the range (inclusive).
     */
    public int getStartColumn() {
        return startColumn;
    }

    /**
     * Returns the end line of the range.
     * 
     * @return end line of the range.
     */
    public int getEndLine() {
        return endLine;
    }

    /**
     * Returns the end column of the range (exclusive).
     * 
     * @return end column of the range (exclusive).
     */
    public int getEndColumn() {
        return endColumn;
    }

}
