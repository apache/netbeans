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
 * DwardAttribute.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ATTR;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.FORM;
import java.io.PrintStream;
import java.nio.charset.Charset;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ACCESS;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.INL;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.VIRTUALITY;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.VIS;

/**
 *
 */
public class DwarfAttribute {
    public final ATTR attrName;
    public final FORM valueForm;

    public DwarfAttribute(int nameOrdinal, int formOrdinal) {
        this.attrName = ATTR.get(nameOrdinal);
        this.valueForm = FORM.get(formOrdinal);
    }

    public void dump(PrintStream out, Object value) {
        out.print("\t" + attrName + " [" + valueForm + "]"); // NOI18N

        if (value != null) {
            if (valueForm == FORM.DW_FORM_ref4) {
                out.printf(" <%x>", value); // NOI18N
            } else if (valueForm == FORM.DW_FORM_addr) {
                if (value instanceof byte[]) {
                    byte[] data = (byte[])value;
                    out.printf("0x"); // NOI18N
                    for (int i = 0; i < data.length; i++) {
                        out.printf("%x", data[i]); // NOI18N
                    }
                } else if (value instanceof Number){
                    out.printf(" 0x%x", ((Number)value).longValue()); // NOI18N
                } else {
                    out.printf(" %s", value.toString()); // NOI18N
                }
            } else if (valueForm == FORM.DW_FORM_block1) {
                byte[] data = (byte[])value;
                out.printf(" %d bytes: ", data.length); // NOI18N
                for (int i = 0; i < data.length; i++) {
                    out.printf(" 0x%x", data[i]); // NOI18N
                }
            } else if (attrName == ATTR.DW_AT_inline && valueForm == FORM.DW_FORM_data1) {
                out.printf(" %s", value.toString()); // NOI18N
                INL inl = INL.get((Byte)value);
                if (inl != null) {
                    out.printf(" %s", inl.toString()); // NOI18N
                }
            } else if (attrName == ATTR.DW_AT_visibility && valueForm == FORM.DW_FORM_data1) {
                out.printf(" %s", value.toString()); // NOI18N
                VIS vis = VIS.get((Byte)value);
                if (vis != null) {
                    out.printf(" %s", vis.toString()); // NOI18N
                }
            } else if (attrName == ATTR.DW_AT_virtuality && valueForm == FORM.DW_FORM_data1) {
                out.printf(" %s", value.toString()); // NOI18N
                VIRTUALITY virt = VIRTUALITY.get((Byte)value);
                if (virt != null) {
                    out.printf(" %s", virt.toString()); // NOI18N
                }
            } else if (attrName == ATTR.DW_AT_accessibility && valueForm == FORM.DW_FORM_data1) {
                out.printf(" %s", value.toString()); // NOI18N
                ACCESS access = ACCESS.get((Byte)value);
                if (access != null) {
                    out.printf(" %s", access.toString()); // NOI18N
                }
            } else {
                out.printf(" %s", value.toString()); // NOI18N
            }

            out.printf("%n"); // NOI18N
        } else {
            out.println(""); // NOI18N
        }
    }

    public void dump(PrintStream out) {
        dump(out, null);
    }

    public void dump() {
        dump(System.out, null);
    }

    @Override
    public String toString() {
        return toString(null);
    }

    public String toString(Object value) {
        try {
            ByteArrayOutputStream st = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(st, false, "UTF-8"); // NOI18N
            dump(out, value);
            return st.toString("UTF-8"); //NOI18N
        } catch (IOException ex) {
            return ""; // NOI18N
        }
    }
}

