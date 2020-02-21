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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.FileMagic;
import org.netbeans.modules.cnd.dwarfdump.Magic;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ElfConstants;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.elf.ElfHeader;
import org.netbeans.modules.cnd.dwarfdump.elf.SectionHeader;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.netbeans.modules.cnd.dwarfdump.section.ElfSection;
import org.netbeans.modules.cnd.dwarfdump.section.StringTableSection;
import org.netbeans.modules.cnd.dwarfdump.section.SymTabSection;

/**
 *
 */
public class ElfReader extends ByteStreamReader {
    private boolean isCoffFormat;
    private boolean isMachoFormat;
    private ElfHeader elfHeader = null;
    private SectionHeader[] sectionHeadersTable;
    private ElfSection[] sections = null;
    private HashMap<String, Integer> sectionsMap = new HashMap<String, Integer>();
    private StringTableSection stringTableSection = null;
    private SharedLibraries sharedLibraries = null;
    private SymTabSection symTabSection = null;
    private long shiftIvArchive = 0;
    private long lengthIvArchive = 0;
    
    public ElfReader(String fname, MyRandomAccessFile reader, Magic magic, long shift, long length) throws IOException {
        super(fname, reader);
        shiftIvArchive = shift;
        lengthIvArchive = length;
        if (!readHeader(magic)) {
            return;
        }
        readProgramHeaderTable();
        readSectionHeaderTable();
        if (sectionHeadersTable == null) {
            sectionHeadersTable = new SectionHeader[0];
        }
        
        sections = new ElfSection[sectionHeadersTable.length];
        
        if (!isCoffFormat) {
              // Before reading all sections need to read ElfStringTable section.
              int elfStringTableIdx = elfHeader.getELFStringTableSectionIndex();
              if (sections.length > elfStringTableIdx) {
                  stringTableSection = new StringTableSection(this, elfStringTableIdx);
                  sections[elfStringTableIdx] = stringTableSection;
              }
        }
        
        // Initialize Name-To-Idx map
        for (int i = 0; i < sections.length; i++) {
            sectionsMap.put(getSectionName(i), i);
        }
        if (isCoffFormat) {
            // string table already read
            Integer idx = sectionsMap.get(SECTIONS.DEBUG_STR);
            if (idx != null) {
                sections[idx] = stringTableSection;
            }
        }
        Integer idx = sectionsMap.get(SECTIONS.SYM_TAB);
        if (idx != null) {
            symTabSection = new SymTabSection(this, idx);
            sections[idx] = symTabSection;
        }
    }
    
    public final String getSectionName(int sectionIdx) {
        if (!isCoffFormat && !isMachoFormat) {
            if (stringTableSection == null) {
                return ".shstrtab"; // NOI18N
            }
            
            long nameOffset = sectionHeadersTable[sectionIdx].sh_name;
            String name = stringTableSection.getString(nameOffset);
            //sectionHeadersTable[sectionIdx].name = name;
            //System.err.println("Section "+name+" offset "+sectionHeadersTable[sectionIdx].sh_offset + " address "+sectionHeadersTable[sectionIdx].sh_addr);
            return name;
        } else {
            return sectionHeadersTable[sectionIdx].getSectionName();
        }
    }
    
    /**
     * 
     * @param sectionIndex in symtab
     * @return section index in elf
     */
    public Integer getSymtabSectionIndex(int sectionIndex) {
        if (symTabSection != null) {
            return symTabSection.getSectionIndex(sectionIndex);
        }
        return null;
    }
    
    public final boolean readHeader(Magic magic) throws WrongFileFormatException, IOException {
        elfHeader = new ElfHeader();
        seek(shiftIvArchive);
        byte[] bytes = new byte[16];
        read(bytes);
        switch (magic) {
            case Elf:
                readElfHeader(bytes);
                return true;
            case Coff:
                readCoffHeader(shiftIvArchive);
                return true;
            case Exe:
                readPeHeader(true);
                return true;
            case Pe:
                readPeHeader(false);
                return true;
            case Macho:
                return readMachoHeader();
        }
        throw new WrongFileFormatException("Not an ELF/PE/COFF/MACH-O file"); // NOI18N
    }
    
    private void readElfHeader( byte[] bytes) throws IOException{
        elfHeader.elfClass = bytes[4];
        elfHeader.elfData  = bytes[5];
        elfHeader.elfVersion = bytes[6];
        elfHeader.elfOs  = bytes[7];
        elfHeader.elfAbi = bytes[8];
        
        setDataEncoding(elfHeader.elfData);
        setFileClass(elfHeader.elfClass);
        
        elfHeader.e_type      = readShort();
        elfHeader.e_machine   = readShort();
        elfHeader.e_version   = readInt();
        elfHeader.e_entry     = read3264()+shiftIvArchive;
        elfHeader.e_phoff     = read3264()+shiftIvArchive;
        elfHeader.e_shoff     = read3264()+shiftIvArchive;
        elfHeader.e_flags     = readInt();
        elfHeader.e_ehsize    = readShort();
        elfHeader.e_phentsize = readShort();
        elfHeader.e_phnum     = readShort();
        elfHeader.e_shentsize = readShort();
        elfHeader.e_shnum     = readShort();
        elfHeader.e_shstrndx  = readShort();
    }
    
    private void readPeHeader(boolean isExe) throws IOException{
        elfHeader.elfData = LSB;
        elfHeader.elfClass = ElfConstants.ELFCLASS32;
        setDataEncoding(elfHeader.elfData);
        setFileClass(elfHeader.elfClass);
        int peOffset = 0;
        if (isExe) {
            seek(0x3c);
            peOffset = readInt();
            seek(peOffset);
            byte[] bytes = new byte[4];
            read(bytes);
            if (!FileMagic.isPeMagic(bytes)) {
                throw new WrongFileFormatException("Not an PE/COFF file"); // NOI18N
            }
        }
        // skip PE magic
        readCoffHeader(peOffset+4);
    }
    
    private void readCoffHeader(long shift) throws IOException{
        isCoffFormat = true;
        // Skip magic
        seek(2+shift);
        elfHeader.elfData = LSB;
        elfHeader.elfClass = ElfConstants.ELFCLASS32;
        setDataEncoding(elfHeader.elfData);
        setFileClass(elfHeader.elfClass);
        elfHeader.e_shnum = readShort();
        //skip time stump
        readInt();
        // read string table
        int startSymbolTable = readInt();
        long symbolTableOffset = shiftIvArchive+startSymbolTable;
        int symbolTableEntries = readInt();
        long stringTableOffset = symbolTableOffset+symbolTableEntries*18;
        int stringTableLength = (int)(shiftIvArchive + lengthIvArchive - stringTableOffset);
        //
        int optionalHeaderSize = readShort();
        // flags
        /*int flags =*/ readShort();
        if (optionalHeaderSize > 0) {
            skipBytes(optionalHeaderSize);
        }
        elfHeader.e_shoff = getFilePointer();
        // read string table
        long pointer = getFilePointer();
        if (startSymbolTable != 0) {
            seek(stringTableOffset);
            byte[] strings = new byte[stringTableLength];
            read(strings);
            stringTableSection = new StringTableSection(this, strings);
            sharedLibraries = new SharedLibraries();
            for(String string : stringTableSection.getStrings()){
                //_libhello4lib_dll_iname
                //libhello3lib_dll_iname (mingw and cygwin start to use without _)
                if (string.endsWith("_dll_iname")) { //NOI18N
                    String lib = string.substring(0,string.length()-10)+".dll"; //NOI18N
                    if (string.startsWith("_")) { //NOI18N
                        lib = lib.substring(1);
                    }
                    sharedLibraries.addDll(lib);
                }
            }
            seek(pointer);
        } else {
            stringTableSection = new StringTableSection(this, new byte[0]);
            sharedLibraries = new SharedLibraries();
        }
    }
    
    private boolean readMachoHeader() throws IOException{
        // 32  ce fa ed fe
        // 64  cf fa ed fe 
        // fat ca fe ba be
        isMachoFormat = true;
        elfHeader.elfData = LSB;
        elfHeader.elfClass = ElfConstants.ELFCLASS32;
        setDataEncoding(elfHeader.elfData);
        setFileClass(elfHeader.elfClass);
        seek(shiftIvArchive);
        byte byte0 = readByte();
        byte byte1 = readByte();
        byte byte2 = readByte();
        byte byte3 = readByte();
        boolean is64 = byte0 == (byte)0xcf || byte0 == (byte)0xca;
        boolean isFat = byte0 == (byte)0xca;
        if (byte0 == (byte)0xfe) {
            elfHeader.elfData = MSB;
            setDataEncoding(elfHeader.elfData);
        }
        if (isFat) {
            elfHeader.elfData = MSB;
            setDataEncoding(elfHeader.elfData);
            seek(shiftIvArchive+4);
            int archCount = readInt();
            for(int j = 0; j < archCount; j++) {
                int arch = readInt();
                int i2 = readInt();
                int offset = readInt();
                int size = readInt();
                int i4 = readInt();
                if (arch == 0x01000007) { // 64 bit
                    shiftIvArchive += offset; // shift to 64 binary
                    break;
                }
            }
            elfHeader.elfData = LSB;
            elfHeader.elfClass = ElfConstants.ELFCLASS32;
            setDataEncoding(elfHeader.elfData);
            setFileClass(elfHeader.elfClass);
            seek(shiftIvArchive);
            byte0 = readByte();
            byte1 = readByte();
            byte2 = readByte();
            byte3 = readByte();
            is64 = byte0 == (byte)0xcf;
            if (byte0 == (byte)0xfe) {
                elfHeader.elfData = MSB;
                setDataEncoding(elfHeader.elfData);
            }
        }
        seek(shiftIvArchive+16);
        int ncmds = readInt();
        int sizeOfCmds = readInt();
        int flags = readInt();
        if (is64){
            skipBytes(4);
        }
        List<SectionHeader> headers = new ArrayList<SectionHeader>();
        sharedLibraries = new SharedLibraries();
        for (int j = 0; j < ncmds; j++){
            int cmd = readInt();
            int cmdSize = readInt();
            if (Dwarf.LOG.isLoggable(Level.FINE)) {
                Dwarf.LOG.log(Level.FINE, "Load command: {0} ({1})", new Object[]{LoadCommand.valueOf(cmd), cmd}); //NOI18N
            }
            if (LoadCommand.LC_SEGMENT.is(cmd) || LoadCommand.LC_SEGMENT_64.is(cmd) ) { //LC_SEGMENT LC_SEGMENT64
                skipBytes(16);
                if (is64) {
                    /*long vmAddr =*/ readLong();
                    /*long vmSize =*/ readLong();
                    /*long fileOff =*/ readLong();
                    /*long fileSize =*/ readLong();
                } else {
                    /*int vmAddr =*/ readInt();
                    /*int vmSize =*/ readInt();
                    /*int fileOff =*/ readInt();
                    /*int fileSize =*/ readInt();
                }
                /*int vmMaxPort =*/ readInt();
                /*int vmInitPort =*/ readInt();
                int nSects = readInt();
                /*int cmdFlags =*/ readInt();
                for (int i = 0; i < nSects; i++){
                    SectionHeader h = readMachoSection(is64);
                    if (h != null){
                        headers.add(h);
                    }
                }
            } else if (LoadCommand.LC_SYMTAB.is(cmd)){ //LC_SYMTAB
                /*int symOffset =*/ readInt();
                /*int nsyms =*/ readInt();
                long strOffset = readInt()+shiftIvArchive;
                int strSize = readInt();
                // read string table
                long pointer = getFilePointer();
                seek(strOffset);
                byte[] strings = new byte[strSize];
                read(strings);
                stringTableSection = new StringTableSection(this, strings);
                seek(pointer);
            } else if (LoadCommand.LC_LOAD_DYLIB.is(cmd)){ //LC_LOAD_DYLIB
                int offset = readInt();
                long pointer = getFilePointer();
                seek(pointer+offset-12);
                String readString = readString();
                sharedLibraries.addDll(readString);
                seek(pointer);
                skipBytes(cmdSize - 12);
            } else {
                skipBytes(cmdSize - 8);
            }
        }
        if (stringTableSection!=null ) {
            if (Dwarf.LOG.isLoggable(Level.FINE)) {
                stringTableSection.dump(System.out);
            }
        }
        if (headers.isEmpty() || stringTableSection == null){
            if (isThereAnyLinkedObjectFiles(stringTableSection)) {
                // we got situation when Mac's linker put dwarf information not in the executable file
                // but just put links to object files instead
                return false;
            }
            throw new WrongFileFormatException("Dwarf section not found in Mach-O file"); // NOI18N
        }
        // clear string section, another string section will be read late
        stringTableSection = null;
        sectionHeadersTable = new SectionHeader[headers.size()];
        for(int i = 0; i < headers.size(); i++){
            sectionHeadersTable[i] = headers.get(i);
            if (SECTIONS.DEBUG_STR.equals(sectionHeadersTable[i].name)) {
                elfHeader.e_shstrndx = (short)i;
            }
        }
        //elfHeader.e_shstrndx = (short)(headers.size()-1);
        return true;
    }
    
    private boolean isThereAnyLinkedObjectFiles(StringTableSection stringTableSection) {
        if (stringTableSection == null) {
            return false;
        }
        int offset = 1;
        while (offset < stringTableSection.getStringTable().length) {
            String string = stringTableSection.getString(offset);
            // XXX: find out how gdb determines object files link
            // but this way would work 90% of times
            if (string.length() > 2 && ".o".equals(string.substring(string.length()-2))) { //NOI18N
                linkedObjectFiles.add(string);
            } else if (string.indexOf(".a(") > 0) { //NOI18N
                // TODO: read onlu mentioned object files from static library
                String lib = string.substring(0, string.lastIndexOf(".a(")+2); //NOI18N
                if (!linkedObjectFiles.contains(lib)) {
                    linkedObjectFiles.add(lib);
                }
            }
            offset += string.length() + 1;
        }

        return linkedObjectFiles.size() > 0;
    }
    
    private List<String> linkedObjectFiles = new ArrayList<String>();
    
    public List<String> getLinkedObjectFiles() {
        return linkedObjectFiles;
    }
    
    public void setdSYM(String path) {
        linkedObjectFiles.clear();
        linkedObjectFiles.add(path); 
    }
    
    private SectionHeader readMachoSection(boolean is64) throws IOException {
        byte[] sectName = new byte[16];
        read(sectName);
        String section = getName(sectName, 0);
        byte[] segName = new byte[16];
        read(segName);
        String segment = getName(segName, 0);
        long size;
        if (is64) {
            /*long addr =*/ readLong();
            size = readLong();
        } else {
            /*long addr =*/ readInt();
            size = readInt();
        }
        long offset = readInt()+shiftIvArchive;
        /*int align =*/ readInt();
        /*int reloff =*/ readInt();
        /*int nreloc =*/ readInt();
        int segFlags = readInt();
        readInt();
        readInt();
        if (is64) {
            readInt();
        }
        if ("__DWARF".equals(segment)){// NOI18N
            SectionHeader h = new SectionHeader();
            if (section.startsWith("__debug")){// NOI18N
                // convert to elf standard
                section = "."+section.substring(2);// NOI18N
            }
            h.name = section;
            h.sh_size = size;
            h.sh_offset = offset;
            h.sh_flags = segFlags;
            return h;
        } else if (Dwarf.LOG.isLoggable(Level.FINE)) {
            Dwarf.LOG.log(Level.FINE, "Segment,Section: {0},{1}", new Object[]{segment, section}); //NOI18N
        }
        return null;
    }
    
    
    private String getName(byte[] stringtable, int offset){
        StringBuilder str = new StringBuilder();
        for (int i = offset; i < stringtable.length; i++) {
            if (stringtable[i] == 0) {
                break;
            }
            str.append((char)stringtable[i]);
        }
        return str.toString();
    }

    public SharedLibraries readPubNames() throws IOException{
        if (sharedLibraries != null) {
            return sharedLibraries;
        }
        sharedLibraries = new SharedLibraries();
        long save = getFilePointer();
        if (true) {
            // another way to find shared libraries
            Integer dynamic = sectionsMap.get(SECTIONS.DYNAMIC);
            if (dynamic != null) {
                long start = sectionHeadersTable[dynamic].sh_offset;
                long size = sectionHeadersTable[dynamic].sh_size;
                seek(start);
                List<Long> libs = new ArrayList<Long>();
                List<Long> paths = new ArrayList<Long>();
                while( getFilePointer() < start+size) {
                    long tag;
                    if (elfHeader.is64Bit()) {
                        tag = readLong();
                    } else {
                        tag = readInt();
                    }
                    if (tag == 0) {
                        //break;
                    }
                    long ptr;
                    if (elfHeader.is64Bit()) {
                        ptr = readLong();
                    } else {
                        ptr = readInt();
                    }
                    //System.err.println("tag "+tag+" "+ptr);
                    if (tag == 1) { //DT_NEEDED /* a needed object */
                        libs.add(ptr);
                    } else if (tag == 29) { //DT_RUNPATH /* run-time search path */
                        if (!paths.contains(ptr)) {
                            paths.add(ptr);
                        }
                    } else if (tag == 15) { //DT_RPATH /* run-time search path */
                        if (!paths.contains(ptr)) {
                            paths.add(ptr);
                        }
                    }
                }
                Integer idx = sectionsMap.get(SECTIONS.DYN_STR);
                if (idx != null) {
                    long s = sectionHeadersTable[idx].sh_offset;
                    for(long l : libs){
                        seek(s+l);
                        sharedLibraries.addDll(readString());
                    }
                    for(long l : paths){
                        seek(s+l);
                        sharedLibraries.addPath(readString());
                    }
                }
            }
        }
        seek(save);
        return sharedLibraries;
    }

    private void readProgramHeaderTable() throws IOException {
        // TODO: Add code
    }
    
    private void readSectionHeaderTable() throws IOException {
        long sectionHeaderOffset = elfHeader.getSectionHeaderOffset();
        
        if (sectionHeaderOffset > 0) {
            seek(sectionHeaderOffset);
            int sectionsNum = elfHeader.getNumberOfSectionHeaders();
            
            sectionHeadersTable = new SectionHeader[sectionsNum];
            
            for (int i = 0; i < sectionsNum; i++) {
                if (isCoffFormat) {
                    sectionHeadersTable[i] = readCoffSectionHeader();
                } else {
                    sectionHeadersTable[i] = readSectionHeader();
                }
            }
        }
    }
    
    private SectionHeader readSectionHeader() throws IOException {
        SectionHeader h = new SectionHeader();
        
        h.sh_name      = ByteStreamReader.uintToLong(readInt());
        h.sh_type      = ByteStreamReader.uintToLong(readInt());
        h.sh_flags     = read3264();
        h.sh_addr      = read3264();
        h.sh_offset    = read3264()+shiftIvArchive;
        h.sh_size      = read3264();
        h.sh_link      = ByteStreamReader.uintToLong(readInt());
        h.sh_info      = ByteStreamReader.uintToLong(readInt());
        h.sh_addralign = read3264();
        h.sh_entsize   = read3264();
        
        return h;
    }
    
    private SectionHeader readCoffSectionHeader() throws IOException {
        SectionHeader h = new SectionHeader();
        
        byte[] bytes = new byte[8];
        read(bytes);
        String name;
        if (bytes[0] == '/'){
            int length = 0;
            for (int j = 1; j < 8; j++){
                byte c = bytes[j];
                if (c < '0' ){
                    break;
                }
                length*=10;
                length+=(c-'0');
            }
            name = stringTableSection.getString(length);
        } else {
            name = getName(bytes,0);
        }
        h.name = name;
        //System.out.println("Section: "+name);
        /*int phisicalAddres =*/ readInt();
        /*int virtualAddres =*/ readInt();
        h.sh_size = readInt();
        h.sh_offset = shiftIvArchive + readInt();
        /*int relocationOffset =*/ readInt();
        /*int lineNumberOffset =*/ readInt();
        /*int mumberRelocations =*/ readShort();
        /*int mumberLineNumbers =*/ readShort();
        h.sh_flags = readInt();
        return h;
    }
    
    public ElfSection getSection(String sectionName) throws IOException {
        Integer sectionIdx = sectionsMap.get(sectionName);
        
        if (sectionIdx == null) {
            return null;
        }
        
        if (sections[sectionIdx] == null) {
            sections[sectionIdx] = initSection(sectionIdx, sectionName);
        }
        
        return sections[sectionIdx];
    }

    public Integer getSectionIndex(String sectionName) {
        return sectionsMap.get(sectionName);
    }
    
    ElfSection initSection(Integer sectionIdx, String sectionName) throws IOException {
        return null;
    }
    
    public SectionHeader getSectionHeader(int sectionIdx) {
        return sectionHeadersTable[sectionIdx];
    }

    public static final class SharedLibraries {
        private final List<String> dlls = new ArrayList<String>();
        private final List<String> searchPaths = new ArrayList<String>();
        public void addDll(String dll) {
            dlls.add(dll);
        }
        public void addPath(String path) {
            searchPaths.add(path);
        }
        public List<String> getDlls() {
            return new ArrayList<String>(dlls);
        }
        public List<String> getPaths() {
            return new ArrayList<String>(searchPaths);
        }
    }
    
    enum LoadCommand {
        UNKNOWN, LC_SEGMENT, LC_SYMTAB, LC_SYMSEG, LC_THREAD, LC_UNIXTHREAD, LC_LOADFVMLIB, LC_IDFVMLIB, LC_IDENT, LC_FVMFILE, LC_PREPAGE, LC_DYSYMTAB, LC_LOAD_DYLIB, LC_ID_DYLIB, LC_LOAD_DYLINKER, LC_ID_DYLINKER, LC_PREBOUND_DYLIB, LC_ROUTINES, LC_SUB_FRAMEWORK, LC_SUB_UMBRELLA, LC_SUB_CLIENT, LC_SUB_LIBRARY, LC_TWOLEVEL_HINTS, LC_PREBIND_CKSUM, LC_LOAD_WEAK_DYLIB, LC_SEGMENT_64, LC_ROUTINES_64, LC_UUID, UNDEFINED, LC_CODE_SIGNATURE;

        public boolean is(int value) {
            return ordinal() == value;
        }

        public static LoadCommand valueOf(int k) {
            for (LoadCommand command : values()) {
                if (command.is(k)) {
                    return command;
                }
            }
            return UNKNOWN;
        }

    }
}

