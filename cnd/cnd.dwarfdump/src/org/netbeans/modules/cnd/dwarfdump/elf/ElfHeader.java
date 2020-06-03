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

package org.netbeans.modules.cnd.dwarfdump.elf;

import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ElfConstants;
import org.netbeans.modules.cnd.dwarfdump.reader.ByteStreamReader;

public class ElfHeader {
    public int elfClass = 0;       /* File class */
    public int elfData = 0;        /* Data encoding */
    public int elfVersion = 0;     /* File version */
    public int elfOs = 0;          /* Operating system/ABI identification */
    public int elfAbi = 0;         /* ABI version */
    
    // Elf Header
    public short e_type = 0;       /* file type uint16_t */
    public short e_machine = 0;    /* target machine uint16_t */
    public int   e_version = 0;    /* file version uint32_t */
    public long  e_entry = 0;      /* start address ElfN_Addr(uintN_t) */
    public long  e_phoff = 0;      /* phdr file offset ElfN_Off(uintN_t) */
    public long  e_shoff = 0;      /* shdr file offset ElfN_Off(uintN_t) */
    public int   e_flags = 0;      /* file flags uint32_t */
    public short e_ehsize = 0;     /* sizeof ehdr uint16_t */
    public short e_phentsize = 0;  /* sizeof phdr uint16_t */
    public short e_phnum = 0;      /* number phdrs uint16_t */
    public short e_shentsize = 0;  /* sizeof shdr uint16_t */
    public short e_shnum = 0;      /* number shdrs uint16_t */
    public short e_shstrndx = 0;   /* shdr string index uint16_t */
      
    public boolean isMSBData() {
        return elfData == ElfConstants.ELFDATA2MSB;
    }

    public boolean isLSBData() {
        return elfData == ElfConstants.ELFDATA2LSB;
    }
    
    public boolean is32Bit() {
        return elfClass == ElfConstants.ELFCLASS32;
    }
    
    public boolean is64Bit() {
        return elfClass == ElfConstants.ELFCLASS64;
    }
    
    public long getSectionHeaderOffset() {
        return e_shoff;
    }
    
    public int getDataEncoding() {
        return elfData;
    }

    public int getFileClass() {
        return elfClass;
    }
    
    public int getNumberOfSectionHeaders() {
        return ByteStreamReader.ushortToInt(e_shnum);
    }
    
    public int getELFStringTableSectionIndex() {
        return ByteStreamReader.ushortToInt(e_shstrndx);
    }
}
