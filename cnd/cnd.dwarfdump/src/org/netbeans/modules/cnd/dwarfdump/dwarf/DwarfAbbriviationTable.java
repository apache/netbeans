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
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 *
 */
public class DwarfAbbriviationTable {
    private List<DwarfAbbriviationTableEntry> entries = null;
    private int numOfEntries = 0;
    //private long offset;

    public DwarfAbbriviationTable(long offset) {
        //this.offset = offset;
    }

    public void setEntries(List<DwarfAbbriviationTableEntry> entries) {
        this.entries = entries;
        this.numOfEntries = entries.size();
    }

    public DwarfAbbriviationTableEntry getEntry(long idx) {
        return (idx > 0 && idx <= numOfEntries) ? entries.get((int)idx - 1) : null;
    }

    public int size() {
        return numOfEntries;
    }

    public void dump(PrintStream out) {
        out.println("  Number TAG"); // NOI18N

        for (DwarfAbbriviationTableEntry entry : entries) {
            entry.dump(out);
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
