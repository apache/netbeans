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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ATTR;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfAttribute;

/**
 *
 */
public class DwarfAbbriviationTableEntry {
    private final long index;
    private final long tag;
    private final boolean hasChildren;
    private final List<DwarfAttribute> attributes = new ArrayList<DwarfAttribute>();

    public DwarfAbbriviationTableEntry(long index, long tag, boolean hasChildren) {
        this.index = index;
        this.tag = tag;
        this.hasChildren = hasChildren;
    }

    public void addAttribute(int attrName, int valueForm) {
        if (attrName != 0 && valueForm != 0) {
            attributes.add(new DwarfAttribute(attrName, valueForm));
        }
    }

    public long getTableIndex() {
        return index;
    }

    public int getAttribute(ATTR attrName) {
        for (int i = 0; i < attributes.size(); i++) {
            DwarfAttribute attr = attributes.get(i);
            if (attr.attrName.equals(attrName)) {
                return i;
            }
        }

        return -1;
    }

    public DwarfAttribute getAttribute(int idx) {
        return attributes.get(idx);
    }

    public int getAttributesCount() {
        return attributes.size();
    }

    public boolean hasChildren() {
        return hasChildren;
    }

    public void dump() {
        dump(System.out, null);
    }

    public void dump(PrintStream out) {
        dump(out, null);
    }

    public void dump(PrintStream out, DwarfEntry dwarfEntry) {
        out.println("Abbrev Number: " + index + " (" + getKind() + ") " + " : " + (hasChildren ? "[has children]" : "[no children]")); // NOI18N

        if (dwarfEntry != null) {
            try {
                String qname = dwarfEntry.getQualifiedName();
                if (qname != null) {
                    out.println("\tQualified Name: " + qname); // NOI18N
                }
            } catch (IOException ex) {
                Dwarf.LOG.log(Level.SEVERE, null, ex);
            }
            dumpAttributes(out, dwarfEntry.getValues());
        }

    }

    public TAG getKind() {
        return TAG.get((int)tag);
    }

    private void dumpAttributes(PrintStream out, List<Object> values) {
        for (int i = 0; i < getAttributesCount(); i++) {
            if (values == null) {
                getAttribute(i).dump(out);
            } else {
                getAttribute(i).dump(out, values.get(i));
            }
        }
    }

    @Override
    public String toString() {
        return toString(null);
    }

    public String toString(DwarfEntry dwarfEntry) {
        try {
            ByteArrayOutputStream st = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(st, false, "UTF-8"); // NOI18N
            dump(out, dwarfEntry);
            return st.toString("UTF-8"); //NOI18N
        } catch (IOException ex) {
            return ""; // NOI18N
        }
    }
}
