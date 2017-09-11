/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
