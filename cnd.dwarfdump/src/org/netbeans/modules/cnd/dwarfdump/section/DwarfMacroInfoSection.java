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
 * DwarfMacroInfoSection.java
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
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoTable;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.MACINFO;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.reader.ByteStreamReader;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;

/**
 *
 */
public class DwarfMacroInfoSection extends ElfSection {
    private final HashMap<Long, DwarfMacinfoTable> macinfoTables = new HashMap<Long, DwarfMacinfoTable>();
    private final boolean isMacro;
    private int offstSize = 4;
    private long headerSize = 0;

    public DwarfMacroInfoSection(DwarfReader reader, int sectionIdx, boolean isMacro) {
        super(reader, sectionIdx);
        this.isMacro = isMacro;
    }

    public DwarfMacinfoTable getMacinfoTable(long offset) {
        Long lOffset = Long.valueOf(offset);
        DwarfMacinfoTable table = macinfoTables.get(lOffset);

        if (table == null) {
            table = new DwarfMacinfoTable(this, offset);
            macinfoTables.put(lOffset, table);
        }

        return table;
    }

    // Fills the table
    // Returns how many bytes have been read.

    public long readMacinfoTable(DwarfMacinfoTable table, long offset, boolean baseOnly) throws IOException {
        long currPos = reader.getFilePointer();

        reader.seek(header.getSectionOffset() + offset);
        offstSize = 4;
        long labelSectionAdress = -1;
        if (isMacro) {
            int dwarfVersion = reader.readShort();
            byte bitness = reader.readByte(); // 2 - 32, 3 - 64
            if ((bitness & 1)==1) {
                offstSize = 8;
            }
            headerSize = reader.getFilePointer() - (header.getSectionOffset() + offset);
            if ((bitness & 2)==2) {
                if (offstSize == 4) {
                    labelSectionAdress = reader.readInt();
                } else {
                    labelSectionAdress = reader.readLong();
                }
            }
            if ((bitness & 4)==4) {
                int count = ByteStreamReader.ubyteToInt(reader.readByte());
                for(int i = 0; i < count; i++) {
                    byte opcode = reader.readByte();
                    long arg = reader.readUnsignedLEB128();
                    reader.seek(reader.getFilePointer() + arg);
                }
            }
        }
        MACINFO type = MACINFO.get(reader.readByte());
        if (baseOnly && type != null) {
            if (type.equals(MACINFO.DW_MACINFO_start_file)) {
                long pos = reader.getFilePointer();
                long lineNum = reader.readUnsignedLEB128();
                if (lineNum == 0) {
                    long fileIdx = reader.readUnsignedLEB128();
                } else {
                    reader.seek(pos);
                }
                type = MACINFO.get(reader.readByte());
            }
        }
        Stack<Integer> fileIndeces = new Stack<Integer>();
        int fileIdx = -1;
        Stack<Long> indirect = new Stack<Long>();
        loop:while(type != null && (!baseOnly || fileIdx == -1)) {
            switch (type) {
                case DW_MACINFO_define:
                case DW_MACINFO_undef:
                {
                    DwarfMacinfoEntry entry = new DwarfMacinfoEntry(type);
                    entry.lineNum = reader.readUnsignedLEB128();
                    entry.definition = reader.readString();
                    entry.fileIdx = fileIdx;
                    table.addEntry(entry);
                    break;
                }
                case DW_MACINFO_start_file:
                {
                    if (baseOnly) {
                        break loop;
                    }
                    DwarfMacinfoEntry entry = new DwarfMacinfoEntry(type);
                    entry.lineNum = reader.readUnsignedLEB128();
                    entry.fileIdx = reader.readUnsignedLEB128();
                    fileIndeces.push(fileIdx);
                    fileIdx = entry.fileIdx;
                    table.addEntry(entry);
                    break;
                }
                case DW_MACINFO_end_file:
                {
                    /*
                     * Stack COULD be empty. This happens when readMacinfoTable() is
                     * invoked twice - first time for base definitions only and the
                     * second one for others. In this case on the second invokation
                     * at the end we will get DW_MACINFO_end_file for file with idx
                     * -1 (base).
                     */
                    DwarfMacinfoEntry entry = new DwarfMacinfoEntry(type);
                    entry.fileIdx = fileIdx;
                    table.addEntry(entry);
                    if (!fileIndeces.empty()) {
                        fileIdx = fileIndeces.pop();
                    }
                    break;
                }
                case DW_MACINFO_vendor_ext:
                {
                    // Just skip...
                    reader.readUnsignedLEB128();
                    reader.readString();
                    break;
                }
                case DW_MACRO_define_indirect_alt:
                case DW_MACRO_define_indirect:
                case DW_MACRO_undef_indirect_alt:
                case DW_MACRO_undef_indirect:
                {
                    DwarfMacinfoEntry entry = new DwarfMacinfoEntry(type);
                    entry.lineNum = reader.readUnsignedLEB128();
                    long adress;
                    if (offstSize == 4) {
                        adress = reader.readInt();
                    } else {
                        adress = reader.readLong();
                    }
                    if (type == MACINFO.DW_MACRO_define_indirect || type == MACINFO.DW_MACRO_undef_indirect) {
                        entry.definition = ((StringTableSection)reader.getSection(SECTIONS.DEBUG_STR)).getString(adress);
                    } else {
                        //TODO: read from alt strings
                    }
                    entry.fileIdx = fileIdx;
                    table.addEntry(entry);
                    break;
                }
                case DW_MACRO_transparent_include_alt:
                {
                    long index;
                    if (offstSize == 4) {
                        index = reader.readInt();
                    } else {
                        index = reader.readLong();
                    }
                    //TODO: read from alt strings?
                    break;
                }
                case DW_MACRO_transparent_include:
                {
                    long index;
                    if (offstSize == 4) {
                        index = reader.readInt();
                    } else {
                        index = reader.readLong();
                    }
                    if (index > 0) {
                        long savePosition = reader.getFilePointer();
                        if (indirect.contains(savePosition)) {
                            System.err.println("infinite indirection in macro section of "+reader.getFileName()); // NOI18N
                        } else {
                            indirect.push(savePosition);
                            reader.seek(header.getSectionOffset() + index + headerSize);
                        }
                    }
                    break;
                }
            }
            byte readByte = reader.readByte();
            while(readByte == 0  && !indirect.empty()) {
                long savePosition = indirect.pop();
                reader.seek(savePosition);
                readByte = reader.readByte();
            }
            type = MACINFO.get(readByte);
        }

        long readBytes = reader.getFilePointer() - (header.getSectionOffset() + offset + 1);
        reader.seek(currPos);

        return readBytes;
    }

    public List<Integer> getCommandIncudedFiles(DwarfMacinfoTable table, long offset, long base) throws IOException{
        List<Integer> res = new ArrayList<Integer>();
        reader.seek(header.getSectionOffset() + offset);
        offstSize = 4;
        long labelSectionAdress = -1;
        if (isMacro) {
            int dwarfVersion = reader.readShort();
            byte bitness = reader.readByte(); // 2 - 32, 3 - 64
            if ((bitness & 1)==1) {
                offstSize = 8;
            }
            headerSize = reader.getFilePointer() - (header.getSectionOffset() + offset);
            if ((bitness & 2)==2) {
                if (offstSize == 4) {
                    labelSectionAdress = reader.readInt();
                } else {
                    labelSectionAdress = reader.readLong();
                }
            }
            if ((bitness & 4)==4) {
                int count = ByteStreamReader.ubyteToInt(reader.readByte());
                for(int i = 0; i < count; i++) {
                    byte opcode = reader.readByte();
                    long arg = reader.readUnsignedLEB128();
                    reader.seek(reader.getFilePointer() + arg);
                }
            }
        }
        int level = 0;
        int lineNum;
        int  fileIdx;
        Stack<Long> indirect = new Stack<Long>();
        loop:while (true) {
            byte readByte = reader.readByte();
            while(readByte == 0  && !indirect.empty()) {
                long savePosition = indirect.pop();
                reader.seek(savePosition);
                readByte = reader.readByte();
            }
            MACINFO type = MACINFO.get(readByte);
            if (type == null) {
                break;
            }
            switch (type) {
                case DW_MACINFO_start_file:
                    level++;
                    lineNum = reader.readUnsignedLEB128();
                    fileIdx = reader.readUnsignedLEB128();
                    if (level == 1) {
                        if (lineNum == 0) {
                            res.add(fileIdx);
                        } else {
                            break loop;
                        }
                    }
                    break;
                case DW_MACINFO_end_file:
                    if (level>0) {
                        level--;
                    }
                    break;
                case DW_MACINFO_vendor_ext:
                    reader.readUnsignedLEB128();
                    reader.readString();
                    break;
                case DW_MACINFO_define:
                case DW_MACINFO_undef:
                    lineNum = reader.readUnsignedLEB128();
                    reader.readString();
                    break;
                case DW_MACRO_define_indirect_alt:
                case DW_MACRO_define_indirect:
                case DW_MACRO_undef_indirect_alt:
                case DW_MACRO_undef_indirect:
                    lineNum = reader.readUnsignedLEB128();
                    long adress;
                    if (offstSize == 4) {
                        adress = reader.readInt();
                    } else {
                        adress = reader.readLong();
                    }
                    break;
                case DW_MACRO_transparent_include_alt:
                {
                    long index;
                    if (offstSize == 4) {
                        index = reader.readInt();
                    } else {
                        index = reader.readLong();
                    }
                    //TODO: read from alt strings?
                    break;
                }
                case DW_MACRO_transparent_include:
                    long index;
                    if (offstSize == 4) {
                        index = reader.readInt();
                    } else {
                        index = reader.readLong();
                    }
                    if (index > 0) {
                        long savePosition = reader.getFilePointer();
                        if (indirect.contains(savePosition)) {
                            System.err.println("infinite indirection in macro section of "+reader.getFileName()); // NOI18N
                        } else {
                            indirect.push(savePosition);
                            reader.seek(header.getSectionOffset() + offset + index + headerSize);
                        }
                    }
                    break;
            }
        }
        return res;
    }

    @Override
    public void dump(PrintStream out) {
        super.dump(out);

        for (DwarfMacinfoTable macinfoTable : macinfoTables.values()) {
            macinfoTable.dump(out);
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
}
