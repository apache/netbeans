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
 * DwarfNameLookupTableSection.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfNameLookupTable;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;

/**
 *
 */
public class DwarfNameLookupTableSection extends ElfSection {
    private List<DwarfNameLookupTable> tables = null;

    public DwarfNameLookupTableSection(DwarfReader reader, int sectionIdx) {
        super(reader, sectionIdx);
    }

    public List<DwarfNameLookupTable> getNameLookupTables() throws IOException {
        if (tables == null) {
            tables = readNameLookupTables();
        }

        return tables;
    }

    private List<DwarfNameLookupTable> readNameLookupTables() throws IOException {
        List<DwarfNameLookupTable> result = new ArrayList<DwarfNameLookupTable>();

        long currPos = reader.getFilePointer();
        reader.seek(header.getSectionOffset());

        DwarfDebugInfoSection debugInfo = (DwarfDebugInfoSection)reader.getSection(SECTIONS.DEBUG_INFO);

        long bytesToRead = header.getSectionSize();

        while (bytesToRead > 0) {
            DwarfNameLookupTable table = new DwarfNameLookupTable();

            long before = reader.getFilePointer();
            table.unit_length = reader.readDWlen();
            long after = reader.getFilePointer();
            long delta = after - before;
            bytesToRead -= table.unit_length + delta;

            table.version = reader.readShort();
            table.debug_info_offset = reader.read3264();
            table.debug_info_length = reader.read3264();
            long savePosition = reader.getFilePointer();
            CompilationUnit cu = debugInfo.getCompilationUnit(table.debug_info_offset);
            reader.seek(savePosition);

            if (cu == null) {
		continue;
	    }

            for (;;) {
                long entryOffset = reader.read3264();

                if (entryOffset == 0) {
                    break;
                }
                table.addEntry(entryOffset, reader.readString());
            }

            result.add(table);
        }

        reader.seek(currPos);

        return result;
    }

    @Override
    public void dump(PrintStream out) {
        super.dump(out);

        for (DwarfNameLookupTable table : tables) {
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

    public DwarfNameLookupTable getNameLookupTableFor(long info_offset) throws IOException {
        for (DwarfNameLookupTable table : getNameLookupTables()) {
            if (table.debug_info_offset == info_offset) {
                return table;
            }
        }

        return null;
    }

}
