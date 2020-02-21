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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

/*
 * DwarfAbbriviationTableSection.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import java.io.ByteArrayOutputStream;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfAbbriviationTable;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfAbbriviationTableEntry;
import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 */
public class DwarfAbbriviationTableSection extends ElfSection {
    private final HashMap<Long, DwarfAbbriviationTable> tables = new HashMap<Long, DwarfAbbriviationTable>();

    /** Creates a new instance of DwarfAbbriviationTableSection */
    public DwarfAbbriviationTableSection(ElfReader reader, int sectionIdx) {
        super(reader, sectionIdx);
    }

    @Override
    public void dump(PrintStream out) {
        super.dump(out);

        for (DwarfAbbriviationTable table : tables.values()) {
            table.dump(out);
        }
    }

    @Override
    public String toString() {
        try {
            ByteArrayOutputStream st = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(st, false, "UTF-8"); // NOI18N
            dump(out);
            return st.toString("UTF-8"); //NOI18N
        } catch (IOException ex) {
            return ""; // NOI18N
        }
    }

    public DwarfAbbriviationTable getAbbriviationTable(long offset) throws IOException {
        Long lOffset = Long.valueOf(offset);
        DwarfAbbriviationTable table = tables.get(lOffset);

        if (table == null) {
            table = readTable(offset);
            tables.put(lOffset, table);
        }

        return table;
    }

    private DwarfAbbriviationTable readTable(long offset) throws IOException {
        long currPos = reader.getFilePointer();

        reader.seek(header.getSectionOffset() + offset);

        long idx = -1;
        List<DwarfAbbriviationTableEntry> entries = new ArrayList<DwarfAbbriviationTableEntry>();
        DwarfAbbriviationTable table = new DwarfAbbriviationTable(offset);

        while (idx != 0) {
            idx = reader.readUnsignedLEB128();

            if (idx == 0) {
                break;
            }

            long aTag = reader.readUnsignedLEB128();
            boolean hasChildren = reader.readBoolean();

            DwarfAbbriviationTableEntry entry = new DwarfAbbriviationTableEntry(idx, aTag, hasChildren);

            int name = -1;
            int form = -1;

            while (name != 0 && form != 0) {
                name = reader.readUnsignedLEB128();
                form = reader.readUnsignedLEB128();
                entry.addAttribute(name, form);
            }

            entries.add(entry);
        }

        table.setEntries(entries);

        reader.seek(currPos);
        return table;
    }

}
