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
package org.netbeans.modules.python.source;

import org.python.antlr.PythonTree;

/**
 * State about a single atomic import. (A single import statement in Python
 *  can correspond to many atomic imports: one for each symbol or module
 *  imported)
 *
 */
public class ImportEntry implements Comparable<ImportEntry> {
    public final String module;
    public final String asName;
    public final String symbol;
    public final boolean isSystem;
    public final boolean isFromImport;
    /**
     * Natural order of import statements (in the source). This will be non zero if
     * we want to preserve the original order rather than the alphabetical order.
     */
    public int ordinal;
    /** 
     * Corresponding import statement node. This will be non null if we want to consider
     * duplicate import statements as different 
     */
    public PythonTree node;
    /**
     * Whether we have a symbol import AND we're sorting by symbol imports.
     * Will be false even for symbol imports when we're not sorting by symbols.
     */
    public boolean sortedFrom;

    public ImportEntry(String module, String symbol, String asName, boolean isSystem, PythonTree node, int ordinal) {
        super();
        this.module = module;
        this.symbol = symbol;
        this.asName = asName;
        this.isSystem = isSystem;
        this.isFromImport = symbol != null;
        this.node = node;
        this.ordinal = ordinal;

        this.sortedFrom = symbol != null;
    }

    public ImportEntry(String module, String asName, boolean isSystem, PythonTree node, int ordinal) {
        this(module, null, asName, isSystem, node, ordinal);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ImportEntry other = (ImportEntry)obj;
        if (this.node != other.node) {
            return false;
        }
        if ((this.module == null) ? (other.module != null) : !this.module.equals(other.module)) {
            return false;
        }
        if ((this.asName == null) ? (other.asName != null) : !this.asName.equals(other.asName)) {
            return false;
        }
        if ((this.symbol == null) ? (other.symbol != null) : !this.symbol.equals(other.symbol)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.module != null ? this.module.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(ImportEntry other) {
        boolean thisIsFuture = "__future__".equals(module); // NOI18N
        boolean otherIsFuture = "__future__".equals(other.module); // NOI18N
        if (thisIsFuture != otherIsFuture) {
            return thisIsFuture ? -1 : 1;
        }
        if (isSystem != other.isSystem) {
            return isSystem ? -1 : 1;
        }
        if (sortedFrom != other.sortedFrom) {
            return sortedFrom ? 1 : -1;
        }
        if (ordinal != other.ordinal) {
            return ordinal - other.ordinal;
        }
        // Then we sort by module name
        int result = module.compareTo(other.module);
        if (result != 0) {
            return result;
        }
        // And then, for each module, first the imports, then the from imports
        if (isFromImport != other.isFromImport) {
            return isFromImport ? 1 : -1;
        }
        if (symbol != null) {
            assert other.symbol != null;
            // since isFromImport==
            result = symbol.compareTo(other.symbol);
            if (result != 0) {
                return result;
            }
        }
        if (asName == null) {
            return (other.asName == null) ? 0 : 1;
        }
        if (other.asName == null) {
            return -1;
        }
        return asName.compareTo(other.asName);
    }

    @Override
    public String toString() {
        return "ImportEntry(" + module + ", " + symbol + ", " + asName + ")"; // NOI18N
    }
}
