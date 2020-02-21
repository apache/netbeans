/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
