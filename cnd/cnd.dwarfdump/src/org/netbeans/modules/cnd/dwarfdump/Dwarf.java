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

package org.netbeans.modules.cnd.dwarfdump;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader.SharedLibraries;
import org.netbeans.modules.cnd.dwarfdump.reader.MyRandomAccessFile;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfDebugInfoSection;
import org.netbeans.modules.cnd.dwarfdump.section.ElfSection;
import org.netbeans.modules.cnd.dwarfdump.section.StabIndexSection;

/**
 *
 */
public class Dwarf {
    private DwarfReader dwarfReader;
    private List<MemberHeader> offsets;
    private FileMagic magic;
    private String fileName;
    private List<FileMagic> toDispose = new ArrayList<FileMagic>();
    public static final Logger LOG = Logger.getLogger(Dwarf.class.getName());
    
    private enum Mode {
        Normal, Archive, MachoLOF
    };
    private final Mode mode;
    
    public Dwarf(String objFileName) throws IOException {
        if (Dwarf.LOG.isLoggable(Level.FINE)) {
            Dwarf.LOG.log(Level.FINE, "\n**** Dwarfing {0}\n", objFileName); //NOI18N
        }
        fileName = objFileName;
        try {
            magic = new FileMagic(objFileName);
            if (magic.getMagic() == Magic.Arch){
                // support archives
                skipFirstHeader(magic.getReader());
                offsets = getObjectTable(magic.getReader());
                if (offsets.isEmpty()) {
                    throw new WrongFileFormatException("Not an ELF file"); // NOI18N
                }
                mode = Mode.Archive;
            } else {
                dwarfReader = new DwarfReader(objFileName, magic.getReader(), magic.getMagic(), 0, magic.getReader().length());
                if (dwarfReader.getLinkedObjectFiles().size() > 0) {
                    File dSYM = new File(objFileName+".dSYM/Contents/Resources/DWARF/"+new File(objFileName).getName()); // NOI18N
                    if (dSYM.exists()) {
                        dwarfReader.setdSYM(dSYM.getAbsolutePath());
                    }
                    // Mach-O left dwarf info in linked object files
                    mode = Mode.MachoLOF;
                } else {
                    mode = Mode.Normal;
                }

            }
        } catch (IOException ex) {
            dispose();
            throw ex;
        }
    }
    
    public final void dispose(){
        if (magic != null) {
            magic.dispose();
            magic = null;
        }
        for(FileMagic file : toDispose){
            file.dispose();
        }
        toDispose.clear();
    }
    
    public ElfSection getSection(String sectionName) throws IOException {
	return dwarfReader.getSection(sectionName);
    }

    public SharedLibraries readPubNames() throws IOException {
        if (dwarfReader != null) {
            return dwarfReader.readPubNames();
        } else {
            // archive does not have PubNames
            return new SharedLibraries();
        }
    }
    
    public String getFileName() {
	return fileName;
    }

    public CompilationUnitIterator iteratorCompilationUnits() throws IOException {
        if (mode == Mode.Archive) {
            return new ArchiveIterator(magic.getReader());
        } else if (mode == Mode.Normal) {
            return iteratorFileCompilationUnits();
        } else {// mode = Mode.MachoLOF
            return new MacArchiveIterator();
        }
    }
    
    private void skipFirstHeader(RandomAccessFile reader) throws IOException{
        reader.seek(8);
        byte[] next = new byte[60];
        reader.readFully(next);
        int length = 0;
        for (int i = 0; i < 10; i++){
            byte c = next[i+48];
            if (c == ' ' ){
                break;
            }
            length*=10;
            length+=(c-'0');
        }
        // Skip first header
        reader.skipBytes(length);
    }

    private List<MemberHeader> getObjectTable(RandomAccessFile reader) throws IOException{
        byte[] next = new byte[60];
        List<MemberHeader> offsetsList= new ArrayList<MemberHeader>();
        while(true) {
            if (reader.getFilePointer()+60 >= reader.length()){
                break;
            }
            reader.readFully(next);
            int length = readNumber(next, 48);
            int nameLength = 0;
            //System.out.println(new String(next, 0, 16));
            if (next[0] == '/' && next[1] == '/') {
                // skip long name section;
                reader.skipBytes(length);
                continue;
            } else if (next[0] == '#' && next[1] == '1' && next[2] == '/') {
                nameLength = readNumber(next, 3);
                reader.skipBytes(nameLength);
            } else if (next[0] == '\n') {
                break;
            }
            long pointer = reader.getFilePointer();
            byte[] bytes = new byte[8];
            reader.readFully(bytes);
            if (FileMagic.isElfMagic(bytes) || FileMagic.isCoffMagic(bytes) || FileMagic.isMachoMagic(bytes)) {
                offsetsList.add(new MemberHeader(pointer,length));
            }
            int skipBytes = reader.skipBytes(length-8-nameLength);
            if (length % 2 == 1){
                reader.skipBytes(1);
            }
        }
        return offsetsList;
    }

    private int readNumber(final byte[] next, int shift) {
        int length = 0;
        for (int i = 0; i < 10; i++){
            byte c = next[i+shift];
            if (c == ' '){
                break;
            }
            length*=10;
            length+=(c-'0');
        }
        return length;
    }

    private CompilationUnitIterator iteratorFileCompilationUnits() throws IOException {
        DwarfDebugInfoSection debugInfo = (DwarfDebugInfoSection)dwarfReader.getSection(SECTIONS.DEBUG_INFO);
        StabIndexSection stab = (StabIndexSection) dwarfReader.getSection(SECTIONS.STAB_INDEX);
        if (debugInfo != null && stab == null) {
            return debugInfo.iteratorCompilationUnits();
        } else if (debugInfo == null && stab != null) {
            return stab.compilationUnits();
        } else if (debugInfo != null && stab != null) {
            return new JoinIterator(stab.compilationUnits(), debugInfo.iteratorCompilationUnits());            
        } else {
            return new DummyIterator();
        }
    }
    
    /**
     * If project was relocated method tries to find source path of file against binary file or source root.
     * Method return best name that consist from binary prefix + common path + source suffix.
     * Example.
     * Binary path /net/server/home/user/projects/application/dist/Debug/GNU-MacOSX/main
     * Source path /home/user/projects/application/main.cc
     * method returns /net/server/ + home/user/projects/application/ + main.cc
     * @param path
     * @return
     */
    public static String fileFinder(String binaryOrRootPath, String path){
        binaryOrRootPath = binaryOrRootPath.replace('\\', '/'); //NOI18N
        boolean driver = false;
        if (binaryOrRootPath.startsWith("/")) { //NOI18N
            binaryOrRootPath = binaryOrRootPath.substring(1);
        } else {
            driver = true;
        }
        path = path.replace('\\', '/'); //NOI18N
        if (path.startsWith("/")) { //NOI18N
            path = path.substring(1);
        }
        String[] splitReal = binaryOrRootPath.split("/"); //NOI18N
        String[] splitVirtual = path.split("/"); //NOI18N
        for(int i = 0; i < splitReal.length; i++) {
            int startReal;
            int startVirtual;
            int len;
            loop2:for(int j = 0; j < splitVirtual.length; j++) {
                if (splitReal[i].equals(splitVirtual[j])) {
                    startReal = i;
                    startVirtual = j;
                    len = 1;
                    while(true) {
                        if (startReal+len < splitReal.length && startVirtual+len < splitVirtual.length) {
                            if (splitReal[startReal+len].equals(splitVirtual[startVirtual+len])) {
                                len++;
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    if (len > 1 || startVirtual == splitVirtual.length - 2 || startReal == splitReal.length - 2) {
                        StringBuilder buf = new StringBuilder();
                        for(int k = 0; k < startReal+len; k++) {
                            buf.append('/').append(splitReal[k]); //NOI18N
                        }
                        for(int k = startVirtual+len; k < splitVirtual.length; k++) {
                            buf.append('/').append(splitVirtual[k]); //NOI18N
                        }
                        if (path.equals(buf.toString().substring(1))) {
                            continue loop2;
                        }
                        if (driver) {
                            return buf.substring(1);
                        } else {
                            return buf.toString();
                        }
                    }
                }
            }
        }
        int common = 0;
        for(int i = 0; i < Math.min(splitReal.length, splitVirtual.length); i++) {
            if (splitReal[i].equals(splitVirtual[i])) {
                common++;
            } else {
                break;
            }
        }
        if (common >= 2 && common < splitReal.length - 1) {
            StringBuilder buf = new StringBuilder();
            for(int k = 0; k < common+1; k++) {
                buf.append('/').append(splitReal[k]); //NOI18N
            }
            for(int k = common+1; k < splitVirtual.length; k++) {
                buf.append('/').append(splitVirtual[k]); //NOI18N
            }
            if (driver) {
                return buf.substring(1);
            } else {
                return buf.toString();
            }
        }
        return null;
    }

    private class MacArchiveIterator implements CompilationUnitIterator {
        private List<String> objectFiles;
        private int archiveIndex = 0;
        private CompilationUnitIterator currentList;
        private Dwarf currentDwarf;

        public MacArchiveIterator() throws IOException{
            objectFiles = dwarfReader.getLinkedObjectFiles();
            advanceArchive();
        }

        @Override
        public boolean hasNext() throws IOException {
            if (currentList == null) {
                return false;
            }
            if (currentList.hasNext()) {
                return true;
            }
            advanceArchive();
            return currentList != null;
        }

        @Override
        public CompilationUnitInterface next() throws IOException {
            return currentList.next();
        }

        private void advanceArchive() throws IOException {
            boolean once = true;
            while (true) {
                if (currentDwarf != null) {
                    toDispose.remove(currentDwarf.magic);
                    currentDwarf.magic.dispose();
                    currentDwarf = null;
                }
                String member;
                if (archiveIndex < objectFiles.size()) {
                    member = objectFiles.get(archiveIndex);
                    if(!new File(member).exists()) {
                        String fileFinder = fileFinder(Dwarf.this.fileName, member);
                        if (fileFinder != null && new File(fileFinder).exists()) {
                            member = fileFinder;
                        } else {
                            archiveIndex++;
                            if (once) {
                                if (fileFinder != null) {
                                    LOG.log(Level.INFO, "File "+Dwarf.this.fileName+" Member "+member+" (and guess "+fileFinder+") does not exists.");
                                } else {
                                    LOG.log(Level.INFO, "File "+Dwarf.this.fileName+" Member "+member+" does not exists.");
                                }
                                once = false;
                            }
                            continue;
                        }
                    }
                    archiveIndex++;
                    try {
                        currentDwarf = new Dwarf(member);
                        toDispose.add(currentDwarf.magic);
                        currentList = currentDwarf.iteratorCompilationUnits();
                        if (!currentList.hasNext()) {
                            continue;
                        }
                    } catch (IOException ex) {
                        if (once) {
                            LOG.log(Level.INFO, "File "+Dwarf.this.fileName+" Member "+member, ex);
                            once = false;
                        }
                        continue;
                    }
                    break;
                } else {
                    currentList = null;
                    return;
                }
            }
        }
    }

    private class ArchiveIterator implements CompilationUnitIterator {
        private int archiveIndex = 0;
        private MyRandomAccessFile reader;
        private CompilationUnitIterator currentIterator;

        public ArchiveIterator(MyRandomAccessFile reader) throws IOException {
            this.reader = reader;
            advanceArchive();
        }

        @Override
        public boolean hasNext() throws IOException {
            if (currentIterator == null) {
                return false;
            }
            if (currentIterator.hasNext()) {
                return true;
            }
            advanceArchive();
            return currentIterator != null;
        }

        @Override
        public CompilationUnitInterface next() throws IOException {
            return currentIterator.next();
        }

        private void advanceArchive() throws IOException {
            while (true) {
                if (archiveIndex < offsets.size()) {
                    MemberHeader member = offsets.get(archiveIndex);
                    archiveIndex++;
                    long shiftIvArchive = member.getOffset();
                    int length = member.getLength();
                    reader.seek(shiftIvArchive);
                    byte[] bytes = new byte[8];
                    reader.readFully(bytes);
                    try {
                        if (FileMagic.isElfMagic(bytes)) {
                            dwarfReader = new DwarfReader(fileName, reader, Magic.Elf, shiftIvArchive, length);
                        } else if (FileMagic.isCoffMagic(bytes)) {
                            dwarfReader = new DwarfReader(fileName, reader, Magic.Coff, shiftIvArchive, length);
                        } else if (FileMagic.isMachoMagic(bytes)) {
                            dwarfReader = new DwarfReader(fileName, reader, Magic.Macho, shiftIvArchive, length);
                        }
                    } catch (Exception e) {
                        Dwarf.LOG.log(Level.INFO, fileName, e);
                        continue;
                    }
                    currentIterator = iteratorFileCompilationUnits();
                    if (!currentIterator.hasNext()) {
                        continue;
                    }
                    break;
                } else {
                    currentIterator = null;
                    return;
                }
            }
        }
    }
    
    public interface CompilationUnitIterator {
        boolean hasNext() throws IOException;
        CompilationUnitInterface next() throws IOException;
    }
    
    private static class JoinIterator implements  CompilationUnitIterator {

        private final CompilationUnitIterator[] delegates;
        private int curr;

        public JoinIterator(CompilationUnitIterator... delegates) {
            this.delegates = delegates;
            curr = 0;
        }
        
        public boolean hasNext() throws IOException {
            for (int i = curr; i < delegates.length; i++) {
                if (delegates[i].hasNext()) {
                    return true;
                }                
            }
            return false;
        }

        public CompilationUnitInterface next() throws IOException {
            while (true) {
                if (delegates[curr].hasNext()) {
                    return delegates[curr].next();
                } else {
                    if (curr < delegates.length - 1) {
                        curr++;
                    } else {
                        break;
                    }                    
                }
            }
            return null;
        }
    }
    
    private static class DummyIterator implements CompilationUnitIterator {
        public boolean hasNext() throws IOException{
            return false;
        }
        public CompilationUnit next() throws IOException{
            return null;
        }
    };
}

