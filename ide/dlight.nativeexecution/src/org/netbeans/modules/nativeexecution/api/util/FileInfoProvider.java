/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.nativeexecution.api.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import java.util.concurrent.Future;

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Kvashin
 */
public class FileInfoProvider {
    public static final class StatInfo {

        public enum FileType {

            NamedPipe(S_IFIFO_C, StatInfo.S_IFIFO & StatInfo.S_IFMT),
            CharacterSpecial(S_IFCHR_C, StatInfo.S_IFCHR & StatInfo.S_IFMT),
            MultiplexedCharacterSpecial(S_IFMPC_C, StatInfo.S_IFMPC & StatInfo.S_IFMT),
            Directory(S_IFDIR_C, StatInfo.S_IFDIR & StatInfo.S_IFMT),
            SpecialNamed(S_IFNAM_C, StatInfo.S_IFNAM & StatInfo.S_IFMT),
            BlockSpecial(S_IFBLK_C, StatInfo.S_IFBLK & StatInfo.S_IFMT),
            MultiplexedBlockSpecial(S_IFMPB_C, StatInfo.S_IFMPB & StatInfo.S_IFMT),
            Regular(S_IFREG_C, StatInfo.S_IFREG & StatInfo.S_IFMT),
            NetworkSpecial(S_IFCMP_C, StatInfo.S_IFCMP & StatInfo.S_IFMT),
            SymbolicLink(S_IFLNK_C, StatInfo.S_IFLNK & StatInfo.S_IFMT),
            Shadow(S_IFSHAD_C, StatInfo.S_IFSHAD & StatInfo.S_IFMT),
            Socket(S_IFSOCK_C, StatInfo.S_IFSOCK & StatInfo.S_IFMT),
            Door(S_IFDOOR_C, StatInfo.S_IFDOOR & StatInfo.S_IFMT),
            EventPort(S_IFPORT_C, StatInfo.S_IFPORT & StatInfo.S_IFMT),
            Undefined(S_UNDEF_C, 0);

            private final char letter;
            private final int fileType;

            private FileType(char letter, int fileType) {
                this.letter = letter;
                this.fileType = fileType;
            }

            public char toChar() {
                return letter;
            }

            public int toInt() {
                return letter;
            }

            public static FileType fromChar(char letter) {
                for (FileType type : values()) {
                    if (type.letter == letter) {
                        return type;
                    }
                }
                return Undefined;
            }

            public static FileType fromInt(int fileType) {
                for (FileType type : values()) {
                    if (type.fileType == fileType) {
                        return type;
                    }
                }
                return Undefined;
            }
        }

        private static final int  S_IFMT     =  0xF000; //bitmask for the file type bitfields
        private static final int  S_IFIFO    =  0x1000; // named pipe (fifo)
        private static final char S_IFIFO_C  =  'p';    // p|
        private static final int  S_IFCHR    =  0x2000; // character device (Solaris character special)
        private static final char S_IFCHR_C  =  'c';    // c
        private static final int  S_IFMPC    =  0x3000; // multiplexed character special (V7)
        private static final char S_IFMPC_C  =  'm';    // undefined char, just to have code
        private static final int  S_IFDIR    =  0x4000; // directory
        private static final char S_IFDIR_C  =  'd';    // d/
        private static final int  S_IFNAM    =  0x5000; // XENIX special named file
        private static final char S_IFNAM_C  =  'N';    // undefined char, just to have code
        private static final int  S_IFBLK    =  0x6000; // block device (Solaris block special)
        private static final char S_IFBLK_C  =  'b';    // b
        private static final int  S_IFMPB    =  0x7000; // multiplexed block special
        private static final char S_IFMPB_C  =  'M';    // undefined char, just to have code
        private static final int  S_IFREG    =  0x8000; // regular file
        private static final char S_IFREG_C  =  '-';    // -
        private static final int  S_IFCMP    =  0x9000; // network special (HP-UX)
        private static final char S_IFCMP_C  =  'n';    // n
        private static final int  S_IFLNK    =  0xA000; // symbolic link
        private static final char S_IFLNK_C  =  'l';    // l
        private static final int  S_IFSHAD   =  0xB000; // Solaris shadow inode for ACL (not seen by userspace)
        private static final char S_IFSHAD_C =  'S';    // undefined char, just to have code
        private static final int  S_IFSOCK   =  0xC000; // socket
        private static final char S_IFSOCK_C =  's';    // s=
        private static final int  S_IFDOOR   =  0xD000; // D> Solaris door
        private static final char S_IFDOOR_C =  'D';    // D>
        private static final int  S_IFPORT   =  0xE000; // Solaris event port (BSD whiteot)
        private static final char S_IFPORT_C =  'P';    // P (w%)
        private static final char S_UNDEF_C  =  'u';    // for other stat info (0x0 and 0xF) to have a default in swithces

        private final String name;

        private final int gid;
        private final int uid;
        private final long size;

        private final String linkTarget;

        private final int access;
        private final Date lastModified;

        public StatInfo(String name, int uid, int gid, long size, String linkTarget, int mode, Date lastModified) {
            this(name, uid, gid, size, isDir(mode), isLink(mode), linkTarget, mode, lastModified);
        }

        public StatInfo(String name, int uid, int gid, long size, boolean directory, boolean link, String linkTarget, int access, Date lastModified) {
            this.name = name;
            this.gid = gid;
            this.uid = uid;
            this.size = size;
            if (directory) {
                access = (access & ~S_IFMT) | S_IFDIR;
            }
            if (link) {
                access = (access & ~S_IFMT) | S_IFLNK;
            }
            this.access = access;
            this.linkTarget = linkTarget;
            this.lastModified = lastModified;
            assert directory == isDirectory();
            assert link == isLink();
        }

        private static boolean isLink(int mode) {
            return (mode & S_IFMT) == S_IFLNK;
        }

        private static boolean isDir(int mode) {
            return (mode & S_IFMT) == S_IFDIR;
        }

        public int getAccess() {
            return access & ACCESS_MASK;
        }

        public String getAccessAsString() {
            return accessToString(getAccess());
        }

        public long getSize() {
            return size;
        }

        public int getGropupId() {
            return gid;
        }

        public Date getLastModified() {
            return lastModified;
        }

        public String getLinkTarget() {
            return linkTarget;
        }

        public String getName() {
            return name;
        }

        public int getUserId() {
            return uid;
        }

        public boolean isDirectory() {
            return (access & S_IFMT) == S_IFDIR;
        }

        public boolean isLink() {
            return (access & S_IFMT) == S_IFLNK;
        }

        public boolean isPlainFile() {
            return (access & S_IFMT) == S_IFREG;
        }

        public FileType getFileType() {
            return FileType.fromInt(access & S_IFMT);
        }

        public String toExternalForm() {
            StringBuilder sb = new StringBuilder();
            sb.append(escape(name)).append(' '); // 0
            sb.append(accessToString(getAccess())).append(' '); // 1
            sb.append(getFileType().toChar()).append(' '); // 2
            // old style
            //sb.append(directory).append(' '); // 2
            //sb.append(link).append(' '); // 3
            sb.append(gid).append(' '); // +0
            sb.append(uid).append(' '); // +1
            sb.append(lastModified.getTime()).append(' '); // +2
            sb.append(size).append(' '); // +3
            if (linkTarget != null) {
                sb.append(escape(linkTarget)).append(' '); // +4
            }
            return sb.toString();
        }

        public static StatInfo fromExternalForm(String externalForm) {
            String[] parts = externalForm.split(" +"); // NOI18N
            String name = unescape(parts[0]);
            int acc = stringToAcces(parts[1]);
            int next;
            boolean dir = false;
            boolean link = false;
            if (parts[2].length() > 1) {
                // This should work for smoothly migration from old storage format to new
                dir = Boolean.parseBoolean(parts[2]);
                link = Boolean.parseBoolean(parts[3]);
                if (dir) {
                    acc += S_IFDIR;
                } else if (link) {
                    acc += S_IFLNK;
                } else {
                    acc += S_IFREG;
                }
                next = 4;
            } else {
                FileType fromChar = FileType.fromChar(parts[2].charAt(0));
                acc += fromChar.fileType;
                switch(fromChar) {
                    case Directory:
                        dir = true;
                        break;
                    case SymbolicLink:
                        link = true;
                        break;
                }
                assert fromChar != FileType.Undefined;
                next = 3;
            }
            int gid = Integer.parseInt(parts[next]);
            int uid = Integer.parseInt(parts[next+1]);
            long time = Long.parseLong(parts[next+2]);
            long size = Long.parseLong(parts[next+3]);
            String linkTarget = (parts.length < next+5) ? null : unescape(parts[next+4]);
            return new StatInfo(name, uid, gid, size, dir, link, linkTarget, acc, new Date(time));
        }

        private boolean can(ExecutionEnvironment env, short all_mask, short grp_mask, short usr_mask) {

            int userId = -1;
            int[] groups = null;

            if (HostInfoUtils.isHostInfoAvailable(env)) {
                try {
                    HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                    userId = hostInfo.getUserId();
                    groups = hostInfo.getAllGroupIDs();
                } catch (IOException ex) {
                    // should be never thrown, since we checked isHostInfoAvailable() first
                    Exceptions.printStackTrace(ex);
                } catch (CancellationException ex) {
                    // should be never thrown, since we checked isHostInfoAvailable() first
                    // however we never report CancellationException
                }
            }
            if (userId == -1) {
                userId = HostInfoCache.getInstance().getUserId(env);
            }
            if (groups == null) {
                groups = HostInfoCache.getInstance().getAllGroupIDs(env);
            }

            if (this.uid == userId) {
                return (getAccess() & usr_mask) > 0;
            } else if (groups != null) {
                boolean isGroupClass = false;
                for (int currGid : groups) {
                    if (gid == currGid) {
                        isGroupClass = true;
                        break;
                    }
                }
                if (isGroupClass) {
                    return (getAccess() & grp_mask) > 0;
                }
            }
            return (getAccess() & all_mask) > 0;
        }

        public boolean canRead(ExecutionEnvironment env) {
            return can(env, ALL_R, GRP_R, USR_R);
        }


        public boolean canWrite(ExecutionEnvironment env) {
            return can(env, ALL_W, GRP_W, USR_W);
        }

        public boolean canExecute(ExecutionEnvironment env) {
            return can(env, ALL_X, GRP_X, USR_X);
        }

        @Override
        public String toString() {
            return name + ' ' + uid + ' ' + gid + ' '+ accessToString(getAccess()) + ' ' + isDirectory() + ' ' + lastModified + ' ' + (isLink() ? " -> " + linkTarget : size); // NOI18N
        }
    }

    public static Future<StatInfo> stat(ExecutionEnvironment env, String absPath) {
        return SftpSupport.getInstance(env).stat(absPath);
    }

    public static Future<StatInfo> lstat(ExecutionEnvironment env, String absPath) {
        return SftpSupport.getInstance(env).lstat(absPath);
    }

    public static Future<StatInfo[]> ls(ExecutionEnvironment env, String absPath) {
        return SftpSupport.getInstance(env).ls(absPath);
    }

    public static Future<StatInfo> move(ExecutionEnvironment env, String from, String to) {
        return SftpSupport.getInstance(env).move(from, to);
    }

    private static final short ACCESS_MASK = 0x1FF;
    private static final short USR_R = 256;
    private static final short USR_W = 128;
    private static final short USR_X = 64;
    private static final short GRP_R = 32;
    private static final short GRP_W = 16;
    private static final short GRP_X = 8;
    private static final short ALL_R = 4;
    private static final short ALL_W = 2;
    private static final short ALL_X = 1;

    private static short stringToAcces(String accessString) {
        if (accessString.length() < 9) {
            throw new IllegalArgumentException("wrong access string: " + accessString); // NOI18N
        }
        short result = 0;

        result |= (accessString.charAt(0) == 'r') ? USR_R : 0;
        result |= (accessString.charAt(1) == 'w') ? USR_W : 0;
        result |= (accessString.charAt(2) == 'x') ? USR_X : 0;

        result |= (accessString.charAt(3) == 'r') ? GRP_R : 0;
        result |= (accessString.charAt(4) == 'w') ? GRP_W : 0;
        result |= (accessString.charAt(5) == 'x') ? GRP_X : 0;

        result |= (accessString.charAt(6) == 'r') ? ALL_R : 0;
        result |= (accessString.charAt(7) == 'w') ? ALL_W : 0;
        result |= (accessString.charAt(8) == 'x') ? ALL_X : 0;

        return result;
    }

    private static String accessToString(int access) {
        char[] accessChars = new char[9];

        accessChars[0] = ((access & USR_R) == 0) ? '-' : 'r';
        accessChars[1] = ((access & USR_W) == 0) ? '-' : 'w';
        accessChars[2] = ((access & USR_X) == 0) ? '-' : 'x';

        accessChars[3] = ((access & GRP_R) == 0) ? '-' : 'r';
        accessChars[4] = ((access & GRP_W) == 0) ? '-' : 'w';
        accessChars[5] = ((access & GRP_X) == 0) ? '-' : 'x';

        accessChars[6] = ((access & ALL_R) == 0) ? '-' : 'r';
        accessChars[7] = ((access & ALL_W) == 0) ? '-' : 'w';
        accessChars[8] = ((access & ALL_X) == 0) ? '-' : 'x';

        return new String(accessChars);
    }

    private static String escape(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
            text = text.replace(" ", "\\ "); // NOI18N
            return text;
        }
    }

    private static String unescape(String text) {
        try {
            return URLDecoder.decode(text, "UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
            text = text.replace("\\ ", " "); // NOI18N
            return text;
        }
    }

    public static final class SftpIOException extends IOException {
        // from jsch
        //public static final int SSH_FX_OK=                            0;
        public static final int SSH_FX_EOF=                           1;
        public static final int SSH_FX_NO_SUCH_FILE=                  2;
        public static final int SSH_FX_PERMISSION_DENIED=             3;
        public static final int SSH_FX_FAILURE=                       4;
        public static final int SSH_FX_BAD_MESSAGE=                   5;
        public static final int SSH_FX_NO_CONNECTION=                 6;
        public static final int SSH_FX_CONNECTION_LOST=               7;
        public static final int SSH_FX_OP_UNSUPPORTED=                8;

        private final int id;
        private final String path;
        SftpIOException(int id, String message, String path, Throwable cause) {
            super(message, cause);
            this.id = id;
            this.path = path;
        }

        public int getId() {
            return id;
        }

        public String getPath() {
            return path;
        }
    }
}
