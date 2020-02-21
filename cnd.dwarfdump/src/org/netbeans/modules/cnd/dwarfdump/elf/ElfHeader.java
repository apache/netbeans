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
