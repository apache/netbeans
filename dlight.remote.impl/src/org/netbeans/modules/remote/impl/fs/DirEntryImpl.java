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
package org.netbeans.modules.remote.impl.fs;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo.FileType;
import org.openide.util.Exceptions;

/**
 *
 */
public class DirEntryImpl extends DirEntry {

    private final String name;
    private final long size;
    private final String linkTarget;
    private final long lastModified;
    private final byte flags;
    private final char fileTypeChar;    
    private final long device;
    private final long inode;

    private static final byte MASK_CAN_READ = 1;
    private static final byte MASK_CAN_WRITE = 2;
    private static final byte MASK_CAN_EXECUTE = 4;
//    private static final byte MASK_IS_LINK = 8;
//    private static final byte MASK_IS_DIR = 16;
//    private static final byte MASK_IS_FILE = 32;

    public static DirEntryImpl create(FileInfoProvider.StatInfo statInfo, ExecutionEnvironment env) {
        return create(statInfo, statInfo.getName(), env);
    }

    public static DirEntryImpl create(FileInfoProvider.StatInfo statInfo, String cache, ExecutionEnvironment env) {

        return new DirEntryImpl(cache, statInfo.getName(), statInfo.getSize(), 
                statInfo.getLastModified().getTime(), 
                makeFlags(statInfo.canRead(env), statInfo.canWrite(env), statInfo.canExecute(env)), 
                statInfo.getFileType().toChar(),
                0, 0, statInfo.getLinkTarget());          
    }
    
    public static DirEntryImpl create(String name, long size, long lastModified, 
            boolean canRead, boolean canWrite, boolean canExec,
            char fileTypeChar, long device, long inode, String linkTarget) {
        
        return new DirEntryImpl(name, name, size,
                lastModified, makeFlags(canRead, canWrite, canExec), fileTypeChar,
                device, inode, linkTarget);
    }
    
    public static Collection<DirEntryImpl> createFromCacheDir(File cacheDir) {
        File[] files = cacheDir.listFiles();
        List<DirEntryImpl> entries = new ArrayList(files.length);
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            entries.add(new DirEntryImpl(file.getName(), file.getName(), file.length(),
                file.lastModified(), makeFlags(file.canRead(), file.canWrite(), file.canExecute()), 
                file.isDirectory() ? FileType.Directory.toChar() : FileType.Regular.toChar(),
                -1, -1, null));
        }
        return entries;
    }

    private static byte makeFlags(boolean canRead, boolean canWrite, boolean canExec) {
        byte flags = 0;
        if (canRead) {
            flags |= MASK_CAN_READ;
        }
        if (canWrite) {
            flags |= MASK_CAN_WRITE;
        }
        if (canExec) {
            flags |= MASK_CAN_EXECUTE;
        }
        return flags;
    }
    
    private DirEntryImpl(String cache, String name, long size, long lastModified, 
            byte flags, char fileTypeChar, long device, long inode, String linkTarget) {
        super(cache);
        this.name = name;
        this.size = size;
        this.linkTarget = linkTarget;
        this.lastModified = lastModified;
        this.flags = flags;
        this.fileTypeChar = fileTypeChar;
        this.device = device;
        this.inode = inode;
    }
    
    private boolean getFlag(byte mask) {
        return (flags & mask) == mask;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public boolean canRead() {
        return getFlag(MASK_CAN_READ);
    }

    @Override
    public boolean canExecute() {
        return getFlag(MASK_CAN_EXECUTE);
    }

    @Override
    public boolean canWrite() {
        return getFlag(MASK_CAN_WRITE);
    }

    @Override
    public long getDevice() {
        return device;
    }

    @Override
    public long getINode() {
        return inode;
    }

    @Override
    public Date getLastModified() {
        return new Date(lastModified);
    }

    @Override
    public boolean isLink() {
        //return getFlag(MASK_IS_LINK);
        return fileTypeChar == FileInfoProvider.StatInfo.FileType.SymbolicLink.toChar();
    }

    @Override
    public boolean isDirectory() {
        //return getFlag(MASK_IS_DIR);
        return fileTypeChar == FileInfoProvider.StatInfo.FileType.Directory.toChar();
    }

    @Override
    public boolean isPlainFile() {
        //return getFlag(MASK_IS_FILE);
        return fileTypeChar == FileInfoProvider.StatInfo.FileType.Regular.toChar();
    }

    @Override
    public FileInfoProvider.StatInfo.FileType getFileType() {
        return FileInfoProvider.StatInfo.FileType.fromChar(fileTypeChar);
    }

    @Override
    public String getLinkTarget() {
        return linkTarget;
    }

    // fs_server format example:
    // 11 vmlinuz.old 0 0 41471 29 1414305981621 --- 2049 6723 29 boot/vmlinuz-3.2.0-70-generic
    //
    // .rfs_cache current format example:
    // bin bin rwxrwxrwx l 0 0 1397625099000 9 .%2Fusr%2Fbin 
    //
    // .rfs_cache new format example:
    // cache name type size  date          access dev   inode  link 
    // bin   bin  l    1024  1397625099000 rwx    2049  24   .%2Fusr%2Fbin
    // 0     1    2    3     4             5      6     7    8

    @Override
    public String toExternalForm() {
        StringBuilder sb = new StringBuilder();
        sb.append(escape(name)).append(' '); // 0
        sb.append(escape(getCache())).append(' '); // 1
        sb.append(getFileType().toChar()).append(' '); // 2      
        sb.append(size).append(' '); // 3        
        sb.append(lastModified).append(' '); // 4
        sb.append(accessAsString()).append(' '); // 5       
        sb.append(device).append(' '); // 6
        sb.append(inode).append(' '); // 7
        if (linkTarget != null) {
            sb.append(escape(linkTarget)).append(' '); // 8
        }
        return sb.toString();
    }
    
    public static DirEntryImpl fromExternalForm(String externalForm) throws FormatException {
        String[] parts = externalForm.split(" +"); // NOI18N
        if (parts.length != 8 && parts.length != 9) {
            throw new FormatException("Wrong format: " + externalForm, false); // NOI18N
        }
        String name = unescape(parts[0]);
        String cache = unescape(parts[1]);
        if (parts[2].length() != 1) {
            throw new FormatException("Wrong file type format: " + externalForm, false); // NOI18N
        }        
        FileInfoProvider.StatInfo.FileType type = FileInfoProvider.StatInfo.FileType.fromChar(parts[2].charAt(0));
        long size = Long.parseLong(parts[3]);
        long lastModified = Long.parseLong(parts[4]);
        String access = parts[5];
        if (access.length() != 3 || 
                ((access.charAt(0) != 'r' && access.charAt(0) != '-')) ||
                ((access.charAt(1) != 'w' && access.charAt(1) != '-')) ||
                ((access.charAt(2) != 'x' && access.charAt(2) != '-'))) {
            throw new FormatException("Wrong file access format: " + externalForm, false); // NOI18N
        }
        byte flags = 0;
        if (access.charAt(0) == 'r') {
            flags |= MASK_CAN_READ;
        }
        if (access.charAt(1) == 'w') {
            flags |= MASK_CAN_WRITE;
        }
        if (access.charAt(2) == 'x') {
            flags |= MASK_CAN_EXECUTE;
        }
        long device = Long.parseLong(parts[6]);
        long inode = Long.parseLong(parts[7]);
        String linkTarget = (parts.length > 8) ? unescape(parts[8]) : null;
        return new DirEntryImpl(cache, name, size, lastModified, flags, type.toChar(), device, inode, linkTarget);
    }

    @Override
    public boolean isValid() {
        return true;
    }
    
    private static String escape(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
            return text.replace(" ", "\\ "); // NOI18N
        }
    }

    private static String unescape(String text) {
        try {
            return URLDecoder.decode(text, "UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
            return text.replace("\\ ", " "); // NOI18N
        }
    }    
    
    @Override
    public String toString() {
        return name + ' ' + accessAsString() + " dir=" + isDirectory() + " date=" + lastModified + ' ' // NOI18N
                + (isLink() ? " -> " + linkTarget : size) + // NOI18N
                " dev=" + device + " inode=" + inode + // NOI18N
                " (" + getCache() + ") " + // NOI18N
                (isValid() ? "[valid]" : "[invalid]"); // NOI18N
    }

    private String accessAsString() {
        return "" + (canRead() ? 'r' : '-') + (canWrite() ? 'w' : '-') + (canExecute() ? 'x' : '-');
    }
}
