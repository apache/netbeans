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
