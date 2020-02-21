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
 * DwarfLineInfostmt_list.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import java.io.ByteArrayOutputStream;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfStatementList;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.LNE;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.LNS;
import org.netbeans.modules.cnd.dwarfdump.reader.ByteStreamReader;

/**
 *
 */
public class DwarfLineInfoSection extends ElfSection {
    private HashMap<Long, DwarfStatementList> statementLists = new HashMap<Long, DwarfStatementList>();

    public DwarfLineInfoSection(DwarfReader reader, int sectionIdx) {
        super(reader, sectionIdx);
    }

    public DwarfStatementList getStatementList(long offset) throws IOException {
        Long lOffset = Long.valueOf(offset);
        DwarfStatementList statementList = statementLists.get(lOffset);

        if (statementList == null) {
            statementList = readStatementList(offset);
            statementLists.put(lOffset, statementList);
        }

        return statementList;
    }

    private DwarfStatementList readStatementList(long offset) throws IOException {
        reader.seek(header.getSectionOffset() + offset);

        DwarfStatementList stmt_list = new DwarfStatementList(offset);

        stmt_list.total_length = reader.readDWlen();
        stmt_list.version = reader.readShort();
        stmt_list.prologue_length = reader.read3264();
        stmt_list.minimum_instruction_length = ByteStreamReader.ubyteToInt(reader.readByte());
        stmt_list.default_is_stmt = ByteStreamReader.ubyteToInt(reader.readByte());
        stmt_list.line_base = reader.readByte();
        stmt_list.line_range = ByteStreamReader.ubyteToInt(reader.readByte());
        stmt_list.opcode_base = ByteStreamReader.ubyteToInt(reader.readByte());

        stmt_list.standard_opcode_lengths = new long[stmt_list.opcode_base - 1];

        for (int i = 0; i < stmt_list.opcode_base - 1; i++) {
            stmt_list.standard_opcode_lengths[i] = reader.readUnsignedLEB128();
        }

        String dirname = reader.readString();

        while (dirname.length() > 0) {
            stmt_list.includeDirs.add(dirname);
            dirname = reader.readString();
        }

        String fname = reader.readString();

        while(fname.length() > 0) {
            stmt_list.fileEntries.add(new FileEntry(fname, reader.readUnsignedLEB128(), reader.readUnsignedLEB128(), reader.readUnsignedLEB128()));
            fname = reader.readString();
        }
        return stmt_list;
    }

    @Override
    public void dump(PrintStream out) {
        super.dump(out);
        for (DwarfStatementList statementList : statementLists.values()) {
            statementList.dump(out);
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

    public LineNumber getLineNumber(long shift, long target) throws IOException{
        long currPos = reader.getFilePointer();
        try {
            DwarfStatementList statementList = getStatementList(shift);
            reader.seek(header.getSectionOffset() + shift + statementList.prologue_length + (reader.is32Bit()?10:22));
            Set<LineNumber> res = interpret(statementList, shift, target);
            if (!res.isEmpty()) {
                return res.iterator().next();
            }
            return null;
        } finally {
            reader.seek(currPos);
        }
    }

    public Set<LineNumber> getLineNumbers(long shift) throws IOException{
        long currPos = reader.getFilePointer();
        try {
            DwarfStatementList statementList = getStatementList(shift);
            reader.seek(header.getSectionOffset() + shift + statementList.prologue_length + (reader.is32Bit()?10:22));
            return interpret(statementList, shift, 0);
        } finally {
            reader.seek(currPos);
        }
    }

    private Set<LineNumber> interpret(DwarfStatementList section, long shift, long target) throws IOException {
        long address = 0;
        long base_address = 0;
        long prev_base_address = 0;
        String define_file = null;
        int fileno = 0;
        int lineno = 1;
        int prev_fileno = 0;
        int prev_lineno = 1;
        final int const_pc_add = 245 / section.line_range * section.minimum_instruction_length;
        int lineNumber = -1;
        int fileNumber = -1;
        String sourceFile = null;
        Set<LineNumber> result = new HashSet<LineNumber>();

        while (reader.getFilePointer() < header.getSectionOffset() + shift + section.total_length) {

            int opcode = ByteStreamReader.ubyteToInt(reader.readByte());

            if (opcode < section.opcode_base) {
                switch (LNS.get(opcode)) {
                    case DW_LNS_extended_op: {
                        int insn_len = reader.readUnsignedLEB128();
                        opcode = reader.readByte();

                        switch (LNE.get(opcode)) {
                            case DW_LNE_end_sequence:
                                lineNumber = prev_lineno;
                                sourceFile = ((prev_fileno >= 0 && prev_fileno < section.getFileEntries().size()) ? section.getFilePath(prev_fileno + 1) : define_file);
                                if (sourceFile != null) {
                                    if (target > 0) {
                                        if (target >= prev_base_address && target < address) {
                                            LineNumber res = new LineNumber(sourceFile, lineNumber, prev_base_address, address);
                                            result.add(res);
                                            return result;
                                        }
                                    } else {
                                        LineNumber res = new LineNumber(sourceFile, lineNumber, prev_base_address, address);
                                        result.add(res);
                                    }
                                }
                                prev_lineno = lineno = 1;
                                prev_fileno = fileno = 0;
                                base_address = address = 0;
                                break;

                            case DW_LNE_set_address:
                                prev_base_address = base_address;
                                if (insn_len == 9) {
                                    base_address = reader.readLong();
                                } else if (insn_len == 5) {
                                    base_address = reader.readInt();
                                }
                                address = base_address;
                                if (prev_base_address == 0) {
                                    prev_base_address = base_address;
                                }
                                break;

                            case DW_LNE_define_file:
                                define_file = reader.readString();
                                reader.readUnsignedLEB128();
                                reader.readUnsignedLEB128();
                                reader.readUnsignedLEB128();
                                break;

                            default:
                                reader.seek(reader.getFilePointer() + insn_len);
                                break;
                        }
                        break;
                    }
                    case DW_LNS_copy:
                        lineNumber = prev_lineno == 1 ? lineno : prev_lineno;
                        fileNumber = prev_fileno == 0 ? fileno : prev_fileno;
                        sourceFile = ((fileNumber >= 0 && fileNumber < section.getFileEntries().size()) ? section.getFilePath(fileNumber + 1) : define_file);
                        if (sourceFile != null) {
                            if (target > 0) {
                                if (target >= prev_base_address && target < address) {
                                    LineNumber res = new LineNumber(sourceFile, lineNumber, prev_base_address, address);
                                    result.add(res);
                                    return result;
                                }
                            } else {
                                LineNumber res = new LineNumber(sourceFile, lineNumber, prev_base_address, address);
                                result.add(res);
                            }
                        }
                        prev_lineno = lineno;
                        prev_fileno = fileno;
                        break;

                    case DW_LNS_advance_pc:
                        {
                            long amt = reader.readUnsignedLEB128();
                            address += amt * section.minimum_instruction_length;
                        }
                        break;

                    case DW_LNS_advance_line:
                        {
                            long amt = reader.readSignedLEB128();
                            prev_lineno = lineno;
                            lineno += (int) amt;
                        }
                        break;

                    case DW_LNS_set_file:
                        prev_fileno = fileno;
                        fileno = (reader.readUnsignedLEB128() - 1);
                        break;

                    case DW_LNS_set_column:
                        reader.readUnsignedLEB128();
                        break;

                    case DW_LNS_negate_stmt:
                        break;

                    case DW_LNS_set_basic_block:
                        break;

                    case DW_LNS_const_add_pc:
                        address += const_pc_add;
                        break;

                    case DW_LNS_fixed_advance_pc:
                        {
                            int amt = ByteStreamReader.ushortToInt(reader.readShort());
                            address += amt;
                        }
                        break;
                }
            } else {
                int adj = (opcode & 0xFF) - section.opcode_base;
                int addr_adv = adj / section.line_range * section.minimum_instruction_length;
                int line_adv = section.line_base + (adj % section.line_range);
                long new_addr = address + addr_adv;
                int new_line = lineno + line_adv;
                sourceFile = ((prev_fileno >= 0 && prev_fileno < section.getFileEntries().size()) ? section.getFilePath(prev_fileno + 1) : define_file);
                if (sourceFile != null) {
                    if (target > 0) {
                        if (target >= prev_base_address && target < new_addr) {
                            LineNumber res = new LineNumber(sourceFile, lineno, prev_base_address, new_addr);
                            result.add(res);
                            return result;
                        }
                    } else {
                        LineNumber res = new LineNumber(sourceFile, lineno, prev_base_address, new_addr);
                        result.add(res);
                    }
                }

                prev_base_address = new_addr;
                prev_lineno = lineno;
                prev_fileno = fileno;
                lineno = new_line;
                address = new_addr;
            }
        }
        if (prev_base_address != 0 && address != 0 && sourceFile != null) {
            if (target > 0) {
                if (target >= prev_base_address && target < address) {
                    LineNumber res = new LineNumber(sourceFile, lineno, prev_base_address, address);
                    result.add(res);
                    return result;
                }
            } else {
                LineNumber res = new LineNumber(sourceFile, lineno, prev_base_address, address);
                result.add(res);
            }
        }
        return result;
    }

    public static final class LineNumber implements Comparable<LineNumber> {
        public final String file;
        public final int line;
        public final long startOffset;
        public final long endOffset;
        private LineNumber(String file, int line, long startOffset, long endOffset){
            assert file != null;
            this.file = file;
            this.line = line;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LineNumber) {
                LineNumber other = (LineNumber) obj;
                return file.equals(other.file) && line == other.line && startOffset == other.endOffset && endOffset == other.endOffset;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + (this.file != null ? this.file.hashCode() : 0);
            hash = 67 * hash + this.line;
            hash = 67 * hash + (int)(this.startOffset & 0xFFFFFFFF);
            hash = 67 * hash + (int)(this.endOffset & 0xFFFFFFFF);
            return hash;
        }

        @Override
        public String toString() {
            return file+":"+line+"\t(0x"+Long.toHexString(startOffset)+"-0x"+Long.toHexString(endOffset)+")"; // NOI18N
        }

        @Override
        public int compareTo(LineNumber o) {
            int res = file.compareTo(o.file);
            if (res == 0) {
                res = (int) ((startOffset - o.startOffset) & 0xFFFFFFFF);
            }
            if (res == 0) {
                res = (int) ((endOffset - o.endOffset) & 0xFFFFFFFF);
            }
            return res;
        }
    }
}

