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

package org.netbeans.modules.cnd.dwarfdump.reader;

import java.io.IOException;
import org.netbeans.modules.cnd.dwarfdump.Magic;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ATE;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ATTR;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.FORM;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.LANG;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfAbbriviationTableSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfArangesSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfAttribute;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfDebugInfoSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfLineInfoSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfMacroInfoSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfNameLookupTableSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfRelaDebugInfoSection;
import org.netbeans.modules.cnd.dwarfdump.section.ElfSection;
import org.netbeans.modules.cnd.dwarfdump.section.StabIndexSection;
import org.netbeans.modules.cnd.dwarfdump.section.StabIndexStrSection;
import org.netbeans.modules.cnd.dwarfdump.section.StringTableSection;

/**
 *
 */
public class DwarfReader extends ElfReader {
    
    public DwarfReader(String fname, MyRandomAccessFile reader, Magic magic, long shift, long length) throws IOException {
        super(fname, reader, magic, shift, length);
        getSection(SECTIONS.DEBUG_STR); 
    }

    public Object readAttrValue(DwarfAttribute attr) throws IOException {
        Object value = readForm(attr.valueForm);
        
        if (attr.attrName.equals(ATTR.DW_AT_language)) {
            return LANG.get(((Number)value).intValue());
        }
        
        if (attr.attrName.equals(ATTR.DW_AT_encoding)) {
            return ATE.get(((Byte)value).byteValue());
        }
        
        if (attr.attrName.equals(ATTR.DW_AT_decl_line)) {
//            if (attr.valueForm.equals(FORM.DW_FORM_data2)) {
//                byte[] val = (byte[])value;
//                return new Integer((0xFF & val[0]) | ((0xFF & val[1]) << 8));
//            }
            return Integer.valueOf(((Number)value).intValue());
        }
        
        return value;
    }
    
    public Object readForm(FORM form) throws IOException {
        switch(form) {
            case DW_FORM_addr:
                return read(new byte[getAddressSize()]);
            case DW_FORM_block2:
                return read(new byte[readShort()]);
            case DW_FORM_block4:
                return read(new byte[readInt()]);
            case DW_FORM_data2:
                //TODO: check on all architectures!
                //return read(new byte[2]);
                return readShort();
            case DW_FORM_data4:
                //TODO: check on all architectures!
                //return read(new byte[4]);
                return readInt();
            case DW_FORM_data8:
                //TODO: check on all architectures!
                //return read(new byte[8]);
                return readLong();
            case DW_FORM_sec_offset:
                return readInt();
            case DW_FORM_string:
                return readString();
            case DW_FORM_exprloc:
            case DW_FORM_block:
                return read(new byte[readUnsignedLEB128()]);
            case DW_FORM_block1:
                return read(new byte[readUnsignedByte()]);
            case DW_FORM_data1:
                return readByte();
            case DW_FORM_flag:
                return readBoolean();
            case DW_FORM_sdata:
                return readSignedLEB128();
            case DW_FORM_strp:
                return ((StringTableSection)getSection(SECTIONS.DEBUG_STR)).getString(readInt());
            case DW_FORM_udata:
                return readUnsignedLEB128();
            case DW_FORM_ref_addr:
                return read(new byte[getAddressSize()]);
            case DW_FORM_ref1:
                return read(new byte[readUnsignedByte()]);
            case DW_FORM_ref2:
                return read(new byte[2]);
            case DW_FORM_ref4:
                return readInt();
            case DW_FORM_ref8:
                return readLong();
            case DW_FORM_ref_udata:
                return read(new byte[readUnsignedLEB128()]);
            case DW_FORM_indirect:
                return readForm(FORM.get(readUnsignedLEB128()));
            case DW_FORM_flag_present:
                return true;
            case DW_FORM_sig8:
                return readLong();
            default:
            throw new IOException("unknown type " + form); // NOI18N
        }
    }

    @Override
    ElfSection initSection(Integer sectionIdx, String sectionName) throws IOException {
        if (sectionName.equals(SECTIONS.DEBUG_STR)) {
            return new StringTableSection(this, sectionIdx);
        }
        
        if (sectionName.equals(SECTIONS.DEBUG_ARANGES)) {
            return new DwarfArangesSection(this, sectionIdx);
        }
        
        if (sectionName.equals(SECTIONS.DEBUG_INFO)) {
            return new DwarfDebugInfoSection(this, sectionIdx);
        }

        if (sectionName.equals(SECTIONS.STAB_INDEXSTR)) {
            return new StabIndexStrSection(this, sectionIdx);
        }

        if (sectionName.equals(SECTIONS.STAB_INDEX)) {
            return new StabIndexSection(this, sectionIdx);
        }

        if (sectionName.equals(SECTIONS.RELA_DEBUG_INFO)) {
            return new DwarfRelaDebugInfoSection(this, sectionIdx);
        }
        
        if (sectionName.equals(SECTIONS.DEBUG_ABBREV)) {
            return new DwarfAbbriviationTableSection(this, sectionIdx);
        }
        
        if (sectionName.equals(SECTIONS.DEBUG_LINE)) {
            return new DwarfLineInfoSection(this, sectionIdx);
        }
        
        if (sectionName.equals(SECTIONS.DEBUG_MACINFO)) {
            return new DwarfMacroInfoSection(this, sectionIdx, false);
        }

        if (sectionName.equals(SECTIONS.DEBUG_MACRO)) {
            return new DwarfMacroInfoSection(this, sectionIdx, true);
        }
        
        if (sectionName.equals(SECTIONS.DEBUG_PUBNAMES)) {
            return new DwarfNameLookupTableSection(this, sectionIdx);
        }

        return null;
    }
}
