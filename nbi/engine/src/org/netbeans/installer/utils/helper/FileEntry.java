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

package org.netbeans.installer.utils.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.StringUtils;

public class FileEntry {
    private File file;
    private String name;
    
    private boolean metaDataReady;
    
    private boolean directory;
    private boolean empty;
    
    private long size;
    private String md5;
    
    private boolean jar;
    private boolean packed;
    private boolean signed;
    
    private long modified;
    
    private int permissions;
    
    // constructors /////////////////////////////////////////////////////////////////
    public FileEntry(
            final File file) {
        this.file = file;
        this.name = file.
                getAbsolutePath().
                replace(FileUtils.BACKSLASH, FileUtils.SLASH);

        this.metaDataReady = false;
    }
    
    public FileEntry(
            final File file,
            final boolean empty,
            final long modified,
            final int permissions) {
        this(file);
        
        this.directory   = true;
        this.empty       = empty;
        this.modified    = modified;
        this.permissions = permissions;
        
        this.metaDataReady = true;
    }
    
    public FileEntry(
            final File file,
            final long size,
            final String md5,
            final boolean jar,
            final boolean packed,
            final boolean signed,
            final long modified,
            final int permissions) {
        this(file);
        
        this.directory   = false;
        this.size        = size;
        this.md5         = md5;
        this.jar         = jar;
        this.packed      = packed;
        this.signed      = signed;
        this.modified    = modified;
        this.permissions = permissions;
        
        this.metaDataReady = true;
    }
    
    // getters/setters //////////////////////////////////////////////////////////////
    public String getName() {
        return name;
    }
    
    public File getFile() {
        return file;
    }
    
    public boolean isMetaDataReady() {
        return metaDataReady;
    }
    
    public boolean isDirectory() {
        return directory;
    }
    
    public boolean isEmpty() {
        return empty;
    }
    
    public long getSize() {
        return size;
    }
    
    public String getMd5() {
        return md5;
    }
    
    public boolean isJarFile() {
        return jar;
    }
    
    public boolean isPackedJarFile() {
        return packed;
    }
    
    public boolean isSignedJarFile() {
        return signed;
    }
    
    public long getLastModified() {
        return modified;
    }
    
    public int getPermissions() {
        return permissions;
    }
    
    // object -> string /////////////////////////////////////////////////////////////
    public String toString() {
        if (directory) {
            return
                    name + StringUtils.LF +
                    directory + StringUtils.LF +
                    empty + StringUtils.LF +
                    modified + StringUtils.LF +
                    Integer.toString(permissions, 8) + StringUtils.LF;
        } else {
            return
                    name + StringUtils.LF +
                    directory + StringUtils.LF +
                    size + StringUtils.LF +
                    md5 + StringUtils.LF +
                    jar + StringUtils.LF +
                    packed + StringUtils.LF +
                    signed + StringUtils.LF +
                    modified + StringUtils.LF +
                    Integer.toString(permissions, 8) + StringUtils.LF;
        }
    }
    
    private String escapeXmlTags(final String str) {        
        return (str == null) ? null : str.
                replace("&", "&amp;").//NOI18N
                replace("\'","&apos;").//NOI18N
                replace("\"","&quot;").//NOI18N
                replace("<", "&lt;").//NOI18N
                replace(">", "&gt;");//NOI18N        
    }
    
    public String toXml() {        
        if (directory) {
            return "<entry " +
                    "type=\"directory\" " +
                    "empty=\"" + empty + "\" " +
                    "modified=\"" + modified + "\" " +
                    "permissions=\"" + Integer.toString(permissions, 8) + "\">" + escapeXmlTags(name) + "</entry>";
        } else {
            return "<entry " +
                    "type=\"file\" " +
                    "size=\"" + size + "\" " +
                    "md5=\"" + md5 + "\" " +
                    "jar=\"" + jar + "\" " +
                    "packed=\"" + packed + "\" " +
                    "signed=\"" + signed + "\" " +
                    "modified=\"" + modified + "\" " +
                    "permissions=\"" + Integer.toString(permissions, 8) + "\">" + escapeXmlTags(name) + "</entry>";
        }
    }
    
    // miscellanea //////////////////////////////////////////////////////////////////
    public void calculateMetaData() throws IOException {
        if (file.exists()) {
            directory = file.isDirectory();
            
            if (!directory) {
                size = file.length();
                md5  = FileUtils.getMd5(file);
                jar  = FileUtils.isJarFile(file);
                
                if (jar) {
                    packed = false; // we cannot determine this
                    signed = FileUtils.isSigned(file);
                }
            } else {
                empty = FileUtils.isEmpty(file);
            }
            
            modified = file.lastModified();
            
            metaDataReady = true;
        } else {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
    }
}
