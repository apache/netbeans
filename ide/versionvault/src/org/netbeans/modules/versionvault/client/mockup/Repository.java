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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.netbeans.modules.versionvault.client.mockup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Stupka
 */
class Repository {
    
    private static Repository instance;    
    
    private Map<File, Map<File, FileEntry>> map = new HashMap<File, Map<File, FileEntry>>();    
            
    static Repository getInstance() {
        if(instance == null) {
            instance = new Repository();
        }
        return instance;
    }

    void add(File file, boolean checkin) {        
        ci(file, true);
        if(!checkin) {
            co(file, true);
        }
    }
    
    void ci(File file, boolean add) {
        try {
            File parent = file.getParentFile();
            Map<File, FileEntry> entries = map.get(parent);
            if (entries == null) {
                if(!add) {
                    CleartoolMockup.LOG.warning("No entry for to be checkedin file " + file);
                }
                entries = new HashMap<File, FileEntry>();
                map.put(parent, entries);
            }
            FileEntry fe = entries.get(file);
            if (fe == null) {
                if(!add) {
                    CleartoolMockup.LOG.warning("No entry for to be checkedin file " + file);
                }                
                fe = new FileEntry(file);
                entries.put(file, fe);
            }
            fe.setCheckedout(false);
            fe.setReserved(false);
            fe.setVersion(fe.getVersion() + 1);
            if(file.isFile()) {
                File data = File.createTempFile("clearcase-", ".data");
                data.deleteOnExit();
                fe.getVersions().add(data);
                Utils.copyStreamsCloseAll(new FileOutputStream(data), new FileInputStream(file));
                setFileReadOnly(file, true);
            }            
        } catch (IOException ex) {
            CleartoolMockup.LOG.log(Level.WARNING, null, ex);
        }
    }

    void co(File file, boolean reserved) {
        File parent = file.getParentFile();
        Map<File, FileEntry> entries = map.get(parent);
        if(entries == null) {
            CleartoolMockup.LOG.warning("No entry for to be checkedout file " + file);
            return; 
        }
        FileEntry fe = entries.get(file);
        if(fe == null) {
            CleartoolMockup.LOG.warning("No entry for to be checkedout file " + file);
            return;            
        }
        fe.setCheckedout(true);
        fe.setReserved(reserved);
        setFileReadOnly(file, false);
    }

    FileEntry getEntry(File file) {
        File parent = file.getParentFile();
        Map<File, FileEntry> entries = map.get(parent);
        if(entries == null) {
            return null;
        }
        return entries.get(file);        
    }

    void removeEntry(File file) {
        File parent = file.getParentFile();
        Map<File, FileEntry> entries = map.get(parent);
        if(entries == null) {
            return;
        }
        entries.remove(file);
    }

    void reserve(File file, boolean value) {
        File parent = file.getParentFile();
        Map<File, FileEntry> entries = map.get(parent);
        if(entries == null) {
            CleartoolMockup.LOG.warning("No entry for to be " + (value ? "reserved" : "unresered") + " file " + file);
            return; 
        }
        FileEntry fe = entries.get(file);
        if(fe == null) {
            CleartoolMockup.LOG.warning("No entry for to be " + (value ? "reserved" : "unresered") + " file " + file);
            return;            
        } else if(fe.isReserved() && value) {
            CleartoolMockup.LOG.warning("Trying to reserve already reserved file " + file);          
        } else if(!fe.isReserved() && !value) {
            CleartoolMockup.LOG.warning("Trying to unreserve already unreserved file " + file);
        }
        fe.setReserved(value);                
    }    
    
    void unco(File file) {
        File parent = file.getParentFile();
        Map<File, FileEntry> entries = map.get(parent);
        if(entries == null) {
            CleartoolMockup.LOG.warning("No entry for to be uncheckedout file " + file);
            return; 
        }
        FileEntry fe = entries.get(file);
        if(fe == null) {
            CleartoolMockup.LOG.warning("No entry for to be uncheckedout file " + file);
            return;            
        }
        fe.setCheckedout(false);
        fe.setReserved(false);        
        if(file.isFile()) {
            setFileReadOnly(file, true);            
        }                
    }
    
    static void setFileReadOnly(File file, boolean readOnly) {
        String [] command = new String[3];
        if (Utilities.isWindows()) {
            command[0] = "attrib";
            command[1] = readOnly ? "+R" : "-R";
        } else {
            command[0] = "chmod";
            command[1] = readOnly ? "u-w" : "u+w";
        }
        command[2] = file.getAbsolutePath();
        try {
            Runtime.getRuntime().exec(command);
        } catch (Exception e) {
            CleartoolMockup.LOG.log(Level.WARNING, null, e);
        }
    }    
}
