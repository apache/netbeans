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

import org.netbeans.modules.cnd.dwarfdump.reader.MyRandomAccessFile;
import java.io.IOException;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;

/**
 *
 */
public class FileMagic {
    private MyRandomAccessFile reader;
    private Magic magic;
    
    public FileMagic(String objFileName) throws WrongFileFormatException, IOException {
        reader = new MyRandomAccessFile(objFileName);
        try {
            readMagic();
        } catch (WrongFileFormatException ex){
            throw new WrongFileFormatException(ex.getMessage()+":"+objFileName);

        }
    }

    public MyRandomAccessFile getReader() {
        return reader;
    }

    public Magic getMagic() {
        return magic;
    }

    private void readMagic() throws WrongFileFormatException {
        byte[] bytes = new byte[8];
        try {
            reader.readFully(bytes);
        } catch (IOException ex) {
            dispose();
            throw new WrongFileFormatException("Not an ELF/PE/COFF/MACH-O file"); // NOI18N
        }
        if (isElfMagic(bytes)) {
            magic = Magic.Elf;
        } else if (isCoffMagic(bytes)) {
            magic = Magic.Coff;
        } else if (isExeMagic(bytes)) {
            magic = Magic.Exe;
        } else if (isPeMagic(bytes)) {
            magic = Magic.Pe;
        } else if (isMachoMagic(bytes)) {
            magic = Magic.Macho;
        } else if (isArchiveMagic(bytes)) {
            magic = Magic.Arch;
        } else {
            dispose();
            throw new WrongFileFormatException("Not an ELF/PE/COFF/MACH-O file"); // NOI18N
        }
    }

    public void dispose(){
        if (reader != null) {
            reader.dispose();
            reader = null;
        }
    }
    
    public static boolean isExeMagic(byte[] bytes){
        return bytes[0] == 'M' && bytes[1] == 'Z'; // NOI18N
    }

    public static boolean isPeMagic(byte[] bytes){
        return bytes[0] == 'P' && bytes[1] == 'E' && bytes[2] == 0 && bytes[3] == 0; // NOI18N
    }

    public static boolean isCoffMagic(byte[] bytes){
        // it depend from provider
        return bytes[0] == 0x4c && bytes[1] == 0x01 ||
        // mingw and cygwin start to use new magic:
               bytes[0] == 0x64 && bytes[1] == (byte)0x86;
    }
    
    public static boolean isElfMagic(byte[] bytes){
        return bytes[0] == 0x7f && bytes[1] == 'E' && bytes[2] == 'L' && bytes[3] == 'F'; // NOI18N
    }
    
    public static boolean isMachoMagic(byte[] bytes){
        return (bytes[0] == (byte)0xce || bytes[0] == (byte)0xcf) && bytes[1] == (byte)0xfa && bytes[2] == (byte)0xed && bytes[3] == (byte)0xfe ||
                bytes[0] == (byte)0xfe && bytes[1] == (byte)0xed && bytes[2] == (byte)0xfa && bytes[3] == (byte)0xce ||
                bytes[0] == (byte)0xca && bytes[1] == (byte)0xfe && bytes[2] == (byte)0xba && bytes[3] == (byte)0xbe ||
                bytes[0] == 'J' && bytes[1] == 'o' && bytes[2] == 'y' && bytes[3] == '!' && bytes[4] == 'p' && bytes[5] == 'e' && bytes[6] == 'f' && bytes[7] == 'f';
    }

    public static boolean isArchiveMagic(byte[] bytes){
        return bytes[0] == '!' && bytes[1] == '<' && bytes[2] == 'a' && bytes[3] == 'r' && // NOI18N
                bytes[4] == 'c' && bytes[5] == 'h' && bytes[6] == '>' && bytes[7] == '\n'; // NOI18N
    }
}
