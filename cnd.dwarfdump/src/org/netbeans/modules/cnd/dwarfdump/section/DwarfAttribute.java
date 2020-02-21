/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

