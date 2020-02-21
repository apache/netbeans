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
 * ElfSection.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import java.io.ByteArrayOutputStream;
import org.netbeans.modules.cnd.dwarfdump.elf.SectionHeader;
import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 *
 */
public class ElfSection {
    final ElfReader reader;
    final SectionHeader header;
    private final int sectionIdx;
    private final String sectionName;

    public ElfSection(ElfReader reader, int sectionIdx) {
        this.reader = reader;
        this.sectionIdx = sectionIdx;
        this.header = reader.getSectionHeader(sectionIdx);
        this.sectionName = reader.getSectionName(sectionIdx);
    }

    public ElfSection(ElfReader reader, int sectionIdx, SectionHeader header, String sectionName) {
        this.reader = reader;
        this.sectionIdx = sectionIdx;
        this.header = header;
        this.sectionName = sectionName;
    }

    public void dump(PrintStream out) {
        out.println("\n** Section " + sectionName); // NOI18N
        if (header != null) {
            header.dump(out);
        }
        out.println("\nContent of the section " + sectionName + "\n"); // NOI18N
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

    public ElfSection read() throws IOException {
        return null;
    }
}
