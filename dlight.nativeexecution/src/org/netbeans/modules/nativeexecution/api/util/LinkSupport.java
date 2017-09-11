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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.api.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class LinkSupport {

    private LinkSupport() {
    }

    public static String getOriginalFile(String linkPath) {
        return getOriginalFile(linkPath, 10);
    }

    public static String resolveWindowsLink(String linkPath) {
        if (Utilities.isWindows()){
            File file = new File(linkPath);
            if (file.exists()){
                if (!isLinkFile(linkPath)) {
                    return linkPath;
                }
            } else {
                file = new File(linkPath+".lnk"); // NOI18N
                if (!file.exists()) {
                    return linkPath;
                }
            }
            String resolved = getOriginalFile(file.getAbsolutePath());
            if (resolved != null) {
                return resolved;
            }
        }
        return linkPath;
    }

    public static boolean isLinkFile(String linkPath) {
        try {
            new LinkReader(linkPath);
            return true;
        } catch (FileNotFoundException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
    }

    private static String getOriginalFile(String linkPath, int level) {
        level--;
        if (level == 0){
            return null;
        }
        LinkReader lr;
        try {
            lr = new LinkReader(linkPath);
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
        linkPath = lr.getSource();
        if (linkPath == null){
            return null;
        }
        final File linkFile = new File(linkPath);
        if (linkFile.exists()) {
            if (linkPath.endsWith(".lnk")) { // NOI18N
                return getOriginalFile(linkPath, level);
            } else if (isLinkFile(linkPath)) {
                return getOriginalFile(linkPath, level);
            }
            return linkFile.getAbsolutePath();
        } else if (new File(linkPath+".lnk").exists()){ // NOI18N
            return getOriginalFile(linkPath+".lnk", level); // NOI18N
        }
        return linkPath;
    }

    private static class LinkReader {

        private RandomAccessFile reader;
        private String sourcePath;
        private String path;
        private boolean isLSB = true;

        /** Creates a new instance of LinkReader */
        public LinkReader(String objFileName) throws FileNotFoundException, IOException {
            reader = new RandomAccessFile(objFileName, "r"); // NOI18N
            path = objFileName;
            readMagic();
        }

        public String getSource() {
            return sourcePath;
        }

        private boolean readCygwinLink() throws IOException {
            StringBuilder buf = new StringBuilder();
            int first = reader.readShort() & 0xFFFF; // FF FE
            if (first == 0xFFFE) {
                int length = (int) (reader.length() - 12) / 2;
                if (length > 512) {
                    return false;
                }
                while (length > 0) {
                    length--;
                    int ch1 = reader.readByte();
                    int ch2 = reader.readByte();
                    if (ch1 == 0 && ch2 == 0) {
                        break;
                    }
                    char c = (char) (ch1 + (ch2 << 8));
                    buf.append(c);
                }
            } else {
                reader.seek(10);
                int length = (int) (reader.length() - 10);
                if (length > 512) {
                    return false;
                }
                while (length > 0) {
                    length--;
                    int ch = reader.readByte();
                    char c = (char) (ch);
                    if (c == 0 || c == 0xD || c == 0xA){
                        break;
                    }
                    buf.append(c);
                }
            }
            sourcePath = buf.toString();
            // Resolve cygwin path to windows file path
            if (sourcePath.startsWith("/")) { // NOI18N
                int i = path.indexOf("\\bin\\"); // NOI18N
                if (i < 0) {
                    i = path.indexOf("/bin/"); // NOI18N
                }
                if (i < 0) {
                    i = path.indexOf("\\etc\\"); // NOI18N
                }
                if (i < 0) {
                    i = path.indexOf("/etc/"); // NOI18N
                }
                if (i > 0) {
                    sourcePath = path.substring(0, i) + sourcePath;
                }
                i = sourcePath.indexOf("/usr/bin/"); // NOI18N
                if (i > 0) {
                    sourcePath = sourcePath.substring(0, i + 1) + sourcePath.substring(i + 5);
                }
            } else if (sourcePath.length() > 2 && sourcePath.charAt(1)==':') {
                // already absolute path
            } else {
                int i = path.lastIndexOf("\\"); // NOI18N
                if (i < 0) {
                    i = path.lastIndexOf("/"); // NOI18N
                }
                if (i > 0) {
                    sourcePath = path.substring(0, i + 1) + sourcePath;
                }
            }
            return true;
        }

        private void readMagic() throws IOException {
            byte[] bytes = new byte[4];
            try {
//Size	Contents	Description
//4 bytes 	Always 4C 00 00 00 	This is how windows knows it is a shortcut file
//16 bytes 	GUID for shortcut files 	The current GUID for shortcuts. It may change in the future. 01 14 02 00 00 00 00 00 C0 00 00 00 00 00 46
//1 dword 	Shortcut flags 	Shortcut flags are explained below
//1 dword 	Target file flags 	Flags are explained below
//1 qword 	Creation time
//1 qword 	Last access time
//1 qword 	Modification time
//1 dword 	File length 	The length of the target file. 0 if the target is not a file. This value is used to find the target when the link is broken.
//1 dword 	Icon number 	If the file has a custom icon (set by the flags bit 6), then this long integer indicates the index of the icon to use. Otherwise it is zero.
//1 dword 	Show Window 	the ShowWnd value to pass to the target application when starting it. 1:Normal Window 2:Minimized 3:Maximized
//1 dword 	Hot Key 	The hot key assigned for this shortcut
//1 dword 	Reserved 	Always 0
//1 dword 	Reserved 	Always 0
                reader.readFully(bytes);
                if (isWindowsLinkMagic(bytes)) {
                    if (readWindowsLink(bytes)) {
                        return;
                    }
                } else if (isCygwinLinkMagic(bytes)) {
                    if (readCygwinLink()){
                        return;
                    }
                }
                throw new IOException(); // NOI18N
            } finally {
                dispose();
            }
        }

        public void dispose() {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
                reader = null;
            }
        }

        private boolean isWindowsLinkMagic(byte[] bytes) {
            return bytes[0] == 'L' && bytes[1] == 0 && bytes[2] == 0 && bytes[3] == 0;
        }

        private boolean isCygwinLinkMagic(byte[] bytes) throws IOException {
            //First symbol is '!'
            //Then follow string '<symlink>'
            //Then follow path
            //Last symbol is '\0'
            if (bytes[0] == '!' && bytes[1] == '<' && bytes[2] == 's' && bytes[3] == 'y'){
                bytes = new byte[6];
                reader.readFully(bytes);
                if (bytes[0] == 'm' && bytes[1] == 'l' && bytes[2] == 'i' && bytes[3] == 'n' && bytes[4] == 'k' && bytes[5] == '>'){
                    return true;
                }
            }
            return false;
        }
        private boolean isShellItemPresent;
        //private boolean isFileLocationItemPresent;
        private boolean isDescriptionPresent;
        private boolean isRelativePathPresent;

        private void readFlags(byte[] bytes) {
//Shortcut flags
//Bit	Meaning
//0 	Shell item id list is present
//1 	Target is a file or directory
//2 	Has a description
//3 	Has a relative path
//4 	Has a working directory
//5 	Has command line arguments
//6 	Has a custom icon.
            int flag = bytes[0];
            if ((flag & 1) != 0) {
                isShellItemPresent = true;
            //System.out.println("The shell item id list is present.");
            }
//        if ((flag&2) != 0){
//            isFileLocationItemPresent = true;
//            //System.out.println("Points to a file or directory.");
//        }
            if ((flag & 4) != 0) {
                isDescriptionPresent = true;
            //System.out.println("Has a description string.");
            }
            if ((flag & 8) != 0) {
                isRelativePathPresent = true;
            //System.out.println("Has a relative path string.");
            }
//        if ((flag&16) != 0){
//            System.out.println("Has a working directory.");
//        }
//        if ((flag&32) != 0){
//            System.out.println("Has command line arguments.");
//        }
//        if ((flag&64) != 0){
//            System.out.println("Has a custom icon.");
//        }
        }

        private long readNumber(int size) throws IOException {
            byte[] bytes = new byte[size];
            long n = 0;
            reader.readFully(bytes);
            for (int i = 0; i < size; i++) {
                long u;
                if (isLSB) {
                    u = (0xff & bytes[i]);
                } else {
                    u = (0xff & bytes[size - i - 1]);
                }
                n |= (u << (i * 8));
            }
            return n;
        }

        private String getString(int length) throws IOException {
            byte[] bytes = new byte[length];
            reader.readFully(bytes);
            StringBuilder str = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                if (bytes[i] == 0) {
                    break;
                }
                str.append((char) bytes[i]);
            }
            return str.toString();
        }

        private boolean readWindowsLink(byte[] bytes) throws IOException {
            // skip GUID
            reader.seek(0x14);
            reader.readFully(bytes);
            readFlags(bytes);
            int position = 0x4C;
            int size;
            reader.seek(position);
            if (isShellItemPresent) {
                size = (int) readNumber(2);
                position += size;
                reader.seek(position);
            }
            // file location always present
            size = (int) readNumber(2);
            if (size == 0) {
                position += 2;
                reader.seek(position);
            } else {
                reader.seek(position);
            }
            if (isDescriptionPresent) {
                size = (int) readNumber(2);
                String description = getString(size);
                position += size + 2;
                reader.seek(position);
                if (reader.length() == position) {
                    sourcePath = description;
                    return true;
                }
            }
            if (isRelativePathPresent) {
                size = (int) readNumber(2);
                sourcePath = getString(size);
                if (sourcePath.length() > 1 && sourcePath.charAt(1) != ':') {
                    int i = path.lastIndexOf('\\');
                    if (i < 0) {
                        i = path.lastIndexOf('/');
                    }
                    if (i > 0) {
                        sourcePath = path.substring(0, i + 1) + sourcePath;
                    }
                }
                return true;
            }
            return false;
        }
    }
}
