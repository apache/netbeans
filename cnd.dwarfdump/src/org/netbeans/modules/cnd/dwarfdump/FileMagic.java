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
