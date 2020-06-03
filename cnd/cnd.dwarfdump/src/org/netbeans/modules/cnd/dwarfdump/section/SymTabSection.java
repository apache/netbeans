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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.STT;
import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader;

/**
 *
 */
public class SymTabSection extends ElfSection {
    // .symtab section index -> elf section index
    private Map<Integer,Integer> sectionMap = new HashMap<Integer, Integer>();

    public SymTabSection(ElfReader reader, int sectionIdx) throws IOException {
        super(reader, sectionIdx);
        read();
    }

    @Override
    public final SymTabSection read() throws IOException {
        if (header.sh_entsize == 0) {
            return this;
        }
        int entries = (int)(header.sh_size/header.sh_entsize);
        long filePos = reader.getFilePointer();
        reader.seek(header.getSectionOffset());
        int i = 0;
        while (i < entries) {
            reader.seek(header.getSectionOffset()+i*header.sh_entsize);
            long name = 0;
            long addr = 0;
            long size = 0;
            int info = 0;
            int other = 0;
            int index = 0;
            if (reader.is32Bit()) {
                name = reader.readInt();
                addr = reader.readInt();
                size = reader.readInt();
                info = reader.readByte();
                other = reader.readByte();
                index = reader.readShort();
            } else {
                name = reader.readInt();
                info = reader.readByte();
                other = reader.readByte();
                index = reader.readShort();
                addr = reader.read3264();
                size = reader.read3264();
            }
            if (info == STT.STT_SECTION.value()) {
                sectionMap.put(index, i);
                //if (reader.is32Bit()) {
                //    System.out.println("32 bit "+i+"->"+index+" info="+info+" other="+other);
                //} else {
                //    System.out.println("64 bit "+i+"->"+index+" info="+info+" other="+other);
                //}
            }
            i++;
        }
        reader.seek(filePos);
        return this;
    }

    /**
     *
     * @param sectionIndex in symtab
     * @return section index in elf
     */
    public Integer getSectionIndex(int sectionIndex) {
        return sectionMap.get(sectionIndex);
    }

    @Override
    public void dump(PrintStream out) {
        super.dump(out);

        out.printf("Elf section\tSymtab Section%n"); // NOI18N
        for(Map.Entry<Integer, Integer> entry: sectionMap.entrySet()) {
            out.printf("%d\t%d%n", entry.getKey(), entry.getValue()); // NOI18N
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
