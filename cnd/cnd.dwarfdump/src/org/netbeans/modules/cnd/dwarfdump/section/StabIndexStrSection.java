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
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader;

/**
 *
 */
public class StabIndexStrSection extends ElfSection {
    private byte[] stringtable = null;

    public StabIndexStrSection(ElfReader reader, int sectionIdx) throws IOException {
        super(reader, sectionIdx);
        read();
    }

    @Override
    public final StabIndexStrSection read() throws IOException {
        long filePos = reader.getFilePointer();
        reader.seek(header.getSectionOffset());
        stringtable = new byte[(int)header.getSectionSize()];
        reader.read(stringtable);
        reader.seek(filePos);
        return this;
    }

    public byte[] getStringTable() {
        return stringtable;
    }

    public List<String> getStrings() {
        List<String> res = new ArrayList<String>();
        if (stringtable == null) {
            return res;
        }
        int offset = 0;
        while (offset < stringtable.length) {
            String string = getString(offset);
            res.add(string);
            offset += string.length() + 1;
        }
        return res;
    }

    public String getString(long offset) {
        StringBuilder str = new StringBuilder();

        for (int i = (int)offset; i < stringtable.length; i++) {
            if (stringtable[i] == 0) {
                break;
            }
            str.append((char)stringtable[i]);
        }

        return str.toString();
    }

    @Override
    public void dump(PrintStream out) {
        super.dump(out);

        if (stringtable == null) {
            out.println("<Empty table>"); // NOI18N
            return;
        }

        int offset = 0;
        int idx = 0;

        out.printf("No.\tOffset\tString%n"); // NOI18N

        while (offset < stringtable.length) {
            String string = getString(offset);
            out.printf("%d.\t%d\t%s%n", ++idx, offset, string); // NOI18N
            offset += string.length() + 1;
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
