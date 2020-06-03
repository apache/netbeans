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

package org.netbeans.modules.cnd.dwarfdump.dwarf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.MACINFO;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 *
 */
public class DwarfMacinfoEntry {
    public final MACINFO type;
    public int lineNum;
    public String definition;
    public int fileIdx;

    public DwarfMacinfoEntry(MACINFO type) {
        this.type = type;
    }

    public void dump(PrintStream out) {
        out.printf("%s\t%d\t[%d]\t%s%n", type, fileIdx, lineNum, definition); // NOI18N
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
