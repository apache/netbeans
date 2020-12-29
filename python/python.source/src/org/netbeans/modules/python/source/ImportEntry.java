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
