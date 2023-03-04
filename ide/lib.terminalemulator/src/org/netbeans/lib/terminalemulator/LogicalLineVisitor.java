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

/*
 * "LogicalLineVisitor.java"
 * LogicalLineVisitor.java 1.1 01/07/24
 */

package org.netbeans.lib.terminalemulator;

/**
 * Passed to one of visitLogicalLines() or reverseVisitLogicalLines().
 */

public interface LogicalLineVisitor {
    /**
     * Called for each logical line.
     * <p>
     * 'text' contains the actual text of a complete line which may be 
     * wrapped multiple times. 'begin' and 'end' mark the region.
     * <p>
     * Note that for the first line 'begin' will match the 'begin' passed to
     * visitLogicalLines(), so watch out if you specify a visitation range
     * that starts in the middle of a line.
     * <p>
     * Normally end.row == begin.row, but if the logical line was wrapped,
     * end.row > begin.row.
     * <p>
     * 'line' is intended to represent a line number, however if the
     * visitation range isn't the whole document it should be interpreted only
     * as a serial number and in the case of reverseVisitLogicalLines() the
     * sequence of line numbers will be backwards.
     * <p>
     * If you locate something in 'text' and need to convert it back to 
     * a Coord use extentInLogicalLine().
     */

    public boolean visit(int line, Coord begin, Coord end, String text);
}
