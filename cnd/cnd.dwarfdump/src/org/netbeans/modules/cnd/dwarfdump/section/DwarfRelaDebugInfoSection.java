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

package org.netbeans.modules.cnd.dwarfdump.section;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;

/**
 *
 */
public class DwarfRelaDebugInfoSection extends ElfSection {
    private final Map<Long, Long> table = new HashMap<Long,Long>();
    private int abbrTableIndex = -1;
    private final Map<Long, Long> abbrTable = new HashMap<Long,Long>();

    public DwarfRelaDebugInfoSection(DwarfReader reader, int sectionIdx) throws IOException {
        super(reader, sectionIdx);
        Integer section = reader.getSectionIndex(SECTIONS.DEBUG_ABBREV);
        if (section != null) {
            Integer symtabSectionIndex = reader.getSymtabSectionIndex(section);
            if (symtabSectionIndex != null) {
                abbrTableIndex = symtabSectionIndex;
            }
        }
        read();
    }

    public Long getAddend(long offset){
        return table.get(offset);
    }

    public Long getAbbrAddend(long offset){
        return abbrTable.get(offset);
    }

    @Override
    public final DwarfArangesSection read() throws IOException {
        long sectionStart = header.getSectionOffset();
        long sectionEnd = header.getSectionSize() + sectionStart;
        reader.seek(sectionStart);
        int i = 0;
        //System.out.println("N\tr_offset\tr_info\tr_addend");
        while(reader.getFilePointer()<sectionEnd) {
            long r_offset = reader.read3264();
            long r_info = reader.read3264();
            long r_addend = reader.read3264();
            //System.out.println(""+i+"\t"+r_offset+"\t0x"+Long.toHexString(r_info)+"\t"+r_addend);
            if (abbrTableIndex >=0 && getR_SYM(r_info) == abbrTableIndex) {
                abbrTable.put(r_offset, r_addend);
            } else {
                table.put(r_offset, r_addend);
            }
            i++;
        }
        return null;
    }
    
    private int getR_SYM(long r_info) {
        if (reader.is64Bit()) {
            return (int)(r_info>>32);
        } else {
            return (int)(r_info>>8);
        }
    }

    private int getR_TYPE(long r_info) {
        return (int)(r_info & 0xFFL);
    }
}
