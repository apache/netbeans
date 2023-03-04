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
package org.netbeans.modules.web.common.sourcemap;

import java.util.Comparator;

/**
 * Mapping of a source map.
 *
 * @author Jan Stola
 */
public class Mapping {
    /** Mapping representing a new line. For internal purposes only. */
    static final Mapping NEW_LINE = new Mapping();

    /** Column in the compiled source. */
    private int column;
    /** Index of the source file. */
    private int sourceIndex = -1;
    /** Line in the source file. */
    private int originalLine = -1;
    /** Column in the source file. */
    private int originalColumn = -1;
    /** Index into the names array. */
    private int nameIndex = -1;

    /**
     * Sets the column in the compiled source.
     * 
     * @param column column in the compiled source.
     */
    void setColumn(int column) {
        this.column = column;
    }

    /**
     * Sets the index of the source file.
     * 
     * @param sourceIndex index of the source file.
     */
    void setSourceIndex(int sourceIndex) {
        this.sourceIndex = sourceIndex;
    }

    /**
     * Sets the line in the source file.
     * 
     * @param originalLine line in the source file.
     */
    void setOriginalLine(int originalLine) {
        this.originalLine = originalLine;
    }

    /**
     * Sets the column in the source file.
     * 
     * @param originalColumn column in the source file.
     */
    void setOriginalColumn(int originalColumn) {
        this.originalColumn = originalColumn;
    }

    void setNameIndex(int nameIndex) {
        this.nameIndex = nameIndex;
    }
    
    /**
     * Returns the column in the compiled source.
     * 
     * @return column in the compiled source.
     */
    int getColumn() {
        return column;
    }

    /**
     * Returns the index of the source file.
     * 
     * @return index of the source file.
     */
    public int getSourceIndex() {
        return sourceIndex;
    }

    /**
     * Returns the line in the source file.
     * 
     * @return line in the source file.
     */
    public int getOriginalLine() {
        return originalLine;
    }

    /**
     * Returns the column in the source file.
     * 
     * @return column in the source file.
     */
    public int getOriginalColumn() {
        return originalColumn;
    }
    
    /**
     * Returns an index of the name at this location.
     * @return An index, or <code>-1</code> when no name is available.
     */
    public int getNameIndex() {
        return nameIndex;
    }

    @Override
    public String toString() {
        return Mapping.class.getName()+"[column="+column+", orig:<"+originalLine+","+originalColumn+">]";
    }

    static class ColumnComparator implements Comparator<Mapping> {
        
        private static ColumnComparator INSTANCE = new ColumnComparator();
        
        private ColumnComparator() {}
        
        ColumnComparator getInstance() {
            return INSTANCE;
        }

        @Override
        public int compare(Mapping m1, Mapping m2) {
            return m1.getColumn() - m2.getColumn();
        }
        
    }

}
