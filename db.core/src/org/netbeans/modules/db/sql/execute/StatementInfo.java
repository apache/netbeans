/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
