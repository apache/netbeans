/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.parser.symtab;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 */
public final class SymTab {
    private final Map<CharSequence, SymTabEntry> entries = new TreeMap<>();
    
//    private final Map<CharSequence, SymTabEntry> imported = new TreeMap<CharSequence, SymTabEntry>();
    private final List<SymTab> imported = new ArrayList<>();  
    private boolean lookupMark;                                     // to avoid loops during lookup
    
    private final int nestingLevel;
    private final CharSequence name;

    SymTab(int nestingLevel, CharSequence name) {
        this.nestingLevel = nestingLevel;
        this.name = name;
    }

    public CharSequence getName() {
        return name;
    }
    
    SymTabEntry lookup(CharSequence entry) {
        SymTabEntry out = entries.get(entry);
        if (out == null && !lookupMark) {
            // out = imported.get(entry);
            lookupMark = true;
            
            try {            
                ListIterator<SymTab> iter = imported.listIterator(imported.size());
                while (out == null && iter.hasPrevious()) {
                    out = iter.previous().lookup(entry);
                }            
            } finally {
                lookupMark = false;
            }
        }
        return out;
    }

    SymTabEntry enter(CharSequence entry) {
        SymTabEntry newEntry = new SymTabEntry(entry, this);
        entries.put(entry, newEntry);
        return newEntry;
    }

    void importSymTab(SymTab symTab) {
//        imported.putAll(symTab.entries);
        imported.add(symTab);
    }

    int getNestingLevel() {
        return nestingLevel;
    }

    @Override
    public String toString() {
        return "SymTab{name=" + name + ", nestingLevel=" + nestingLevel + ", entries=" + entries + ", imported=" + imported + '}'; // NOI18N
    }
    
    
}
