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

package org.netbeans.modules.db.sql.execute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class StatementInfo {

    private final String sql;
    private final int rawStartOffset;
    private final int startOffset;
    private final int startLine;
    private final int startColumn;
    private final int rawEndOffset;
    private final int endOffset;
    private final Map<Integer,Integer> sqlPosToRawPos;
    private final List<Integer> newLineOffsets;

    public StatementInfo(String sql, int rawStartOffset, int startOffset, int startLine, int startColumn, int endOffset, int rawEndOffset, Map<Integer,Integer> sqlPosToRawPos, List<Integer> newLineOffsets) {
        this.sql = sql;
        this.rawStartOffset = rawStartOffset;
        this.startOffset = startOffset;
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endOffset = endOffset;
        this.rawEndOffset = rawEndOffset;
        this.sqlPosToRawPos = Collections.unmodifiableMap(new TreeMap<>(sqlPosToRawPos));
        this.newLineOffsets = Collections.unmodifiableList(new ArrayList<>(newLineOffsets));
    }

    /**
     * Returns the SQL text statement with comments and leading and trailing
     * whitespace removed.
     */
    public String getSQL() {
        return sql;
    }

    /**
     * Returns the start offset of the raw SQL text (including comments and leading whitespace).
     */
    public int getRawStartOffset() {
        return rawStartOffset;
    }

    /**
     * Returns the start offset of the text returned by {@link #getSQL}.
     */
    public int getStartOffset() {
        return startOffset;
    }

    /**
     * Returns the zero-based number of the line corresponding to {@link #getStartOffset}.
     */
    public int getStartLine() {
        return startLine;
    }

    /**
     * Returns the zero-based number of the column corresponding to {@link #getStartOffset}.
     */
    public int getStartColumn() {
        return startColumn;
    }

    /**
     * Returns the end offset of the text returned by {@link #getSQL}.
     */
    public int getEndOffset() {
        return endOffset;
    }

    /**
     * Returns the end offset of the raw SQL text (including comments and trailing whitespace).
     */
    public int getRawEndOffset() {
        return rawEndOffset;
    }
    
    private int translateToRawPos(int sqlPos) {
        int rawOffset = 0;
        int locicalOffset = 0;
        for(Entry<Integer,Integer> entry: sqlPosToRawPos.entrySet()) {
            if(entry.getKey() <= sqlPos) {
                locicalOffset = entry.getKey();
                rawOffset = entry.getValue();
            } else {
                break;
            }
        }
        return rawOffset + (sqlPos - locicalOffset);
    }

    // Package private for unittesting
    Map<Integer, Integer> getSqlPosToRawPos() {
        return sqlPosToRawPos;
    }

    // Package private for unittesting
    List<Integer> getNewLineOffsets() {
        return newLineOffsets;
    }
    
    /**
     * Translate a logicalOffset (an offset in the sql) into a line/column
     * pair in the complete script environment.
     * 
     * <p>Both values are zero-based</p>
     * 
     * @param logicalOffset
     * @return int array with two components, first denotes line, second column
     */
    public int[] translateToRawPosLineColumn(int logicalOffset) {
        int rawOffset = translateToRawPos(logicalOffset);
        int line = 0;
        int newLineOffset = -1;
        for (Integer offset : getNewLineOffsets()) {
            if (offset > rawOffset) {
                break;
            }
            newLineOffset = offset;
            line++;
        }
        return new int[] {line, rawOffset - newLineOffset - 1};
    }
}
