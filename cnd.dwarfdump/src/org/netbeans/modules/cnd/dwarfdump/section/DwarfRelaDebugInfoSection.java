/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;

/**
 *
 */
public class DwarfRelaDebugInfoSection extends ElfSection {
    private final Map<Long, Long> table = new HashMap<Long,Long>();
    private int abbrTableIndex = -1;
    private final Map<Long, Long> abbrTable = new HashMap<Long,Long>();

    public DwarfRelaDebugInfoSection(DwarfReader reader, int sectionIdx) throws IOException {
        super(reader, sectionIdx);
        Integer section = reader.getSectionIndex(SECTIONS.DEBUG_ABBREV);
        if (section != null) {
            Integer symtabSectionIndex = reader.getSymtabSectionIndex(section);
            if (symtabSectionIndex != null) {
                abbrTableIndex = symtabSectionIndex;
            }
        }
        read();
    }

    public Long getAddend(long offset){
        return table.get(offset);
    }

    public Long getAbbrAddend(long offset){
        return abbrTable.get(offset);
    }

    @Override
    public final DwarfArangesSection read() throws IOException {
        long sectionStart = header.getSectionOffset();
        long sectionEnd = header.getSectionSize() + sectionStart;
        reader.seek(sectionStart);
        int i = 0;
        //System.out.println("N\tr_offset\tr_info\tr_addend");
        while(reader.getFilePointer()<sectionEnd) {
            long r_offset = reader.read3264();
            long r_info = reader.read3264();
            long r_addend = reader.read3264();
            //System.out.println(""+i+"\t"+r_offset+"\t0x"+Long.toHexString(r_info)+"\t"+r_addend);
            if (abbrTableIndex >=0 && getR_SYM(r_info) == abbrTableIndex) {
                abbrTable.put(r_offset, r_addend);
            } else {
                table.put(r_offset, r_addend);
            }
            i++;
        }
        return null;
    }
    
    private int getR_SYM(long r_info) {
        if (reader.is64Bit()) {
            return (int)(r_info>>32);
        } else {
            return (int)(r_info>>8);
        }
    }

    private int getR_TYPE(long r_info) {
        return (int)(r_info & 0xFFL);
    }
}
