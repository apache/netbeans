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
