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

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class DwarfNameLookupTable {
    public long unit_length;
    public int  version;
    public long debug_info_offset;
    public long debug_info_length;

    public HashMap<Long, String> entries = new HashMap<Long, String>();

    public void dump(PrintStream out) {
        out.println("\nPublic names:\n"); // NOI18N
        out.printf("  %-40s %s%n", "Length:", unit_length); // NOI18N
        out.printf("  %-40s %s%n", "Version:", version); // NOI18N
        out.printf("  %-40s %s%n", "Offset into .debug_info section:", debug_info_offset); // NOI18N
        out.printf("  %-40s %s%n", "Size of area in .debug_info section:", debug_info_length); // NOI18N
        out.println("%n    Offset      Name"); // NOI18N


        for (Map.Entry<Long, String> entry : entries.entrySet()) {
            long offset = entry.getKey();
            out.printf("%d (0x%x)\t%s%n", offset, offset, entry.getValue()); // NOI18N
        }
    }

    public void addEntry(long offset, String name) {
        entries.put(offset, name);
    }

    public String getName(long offset) {
        return entries.get(offset);
    }

}
