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

package org.netbeans.modules.cnd.dwarfdump.elf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 *
 */
public class SectionHeader {
    public String name;            /* section name */
    public long sh_name = 0;       /* section name uint32_t */
    public long sh_type = 0;       /* SHT_... uint32_t */
    public long sh_flags = 0;      /* SHF_... uint32_t */
    public long sh_addr = 0;       /* x virtual address ElfN_Addr(uintN_t) */
    public long sh_offset = 0;     /* x file offset ElfN_Off(uintN_t) */
    public long sh_size = 0;       /* x section size uintN_t */
    public long sh_link = 0;       /* misc info uint32_t */
    public long sh_info = 0;       /* misc info uint32_t */
    public long sh_addralign = 0;  /* x memory alignment uintN_t */
    public long sh_entsize = 0;    /* x entry size if table uintN_t */

    public long getSectionSize() {
        return sh_size;
    }

    public long getSectionOffset() {
        return sh_offset;
    }

    public long getSectionEntrySize() {
        return sh_entsize;
    }

    public String getSectionName(){
        return name;
    }

    public void dump(PrintStream out) {
        out.println("Elf section header:"); // NOI18N
        out.printf("  %-20s %s%n", "Offset:", sh_offset); // NOI18N
        out.printf("  %-20s %s%n", "Length:", sh_size); // NOI18N
        out.printf("  %-20s %s%n", "Memory alignment:", sh_addralign); // NOI18N
        out.println();
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
