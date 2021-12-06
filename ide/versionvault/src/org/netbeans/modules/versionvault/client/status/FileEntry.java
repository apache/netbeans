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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.versionvault.client.status;

import java.io.File;


/**
 * Holds detailed information about a managed file, ie VersionSelector-s, annotation, ...
 * 
 * @author Maros Sandor
 */
public class FileEntry { 
                       
    public static String ANNOTATION_ECLIPSED                = "eclipsed";           
    public static String ANNOTATION_HIJACKED                = "hijacked";        
    public static String ANNOTATION_LOADED_BUT_MISSING      = "loaded but missing";       
    public static String ANNOTATION_CHECKEDOUT_BUT_REMOVED  = "checkedout but removed";       
    
    public static String TYPE_VERSION                  = "version";
    public static String TYPE_DIRECTORY_VERSION        = "directory version";
    public static String TYPE_FILE_ELEMENT             = "file element";
    public static String TYPE_DIRECTORY_ELEMENT        = "directory element";
    public static String TYPE_VIEW_PRIVATE_OBJECT      = "view private object";
    public static String TYPE_DERIVED_OBJECT           = "derived object";
    public static String TYPE_DERIVED_OBJECT_VERSION   = "derived object version";
    public static String TYPE_SYMBOLIC_LINK            = "symbolic link";       
    public static String TYPE_UNKNOWN                  = null;
    
    final private String type;    
    final private File file;    
    final private FileVersionSelector originVersion;
    final private FileVersionSelector version;
    final private String annotation;
    final private boolean reserved;
    final private String user;
    final private String rule; 
    
    private String stringValue;
        
    public FileEntry(
           String type, 
           File file, 
           FileVersionSelector originVersion, 
           FileVersionSelector version, 
           String annotation,
           String rule,
           boolean reserved,
           String user) 
    {
        assert file != null;
        assert type != null : "null clearcase type for file " + file;
        
        this.type = type;
        this.file = file;
        this.originVersion = originVersion;
        this.version = version;
        this.annotation = annotation;        
        this.rule = rule;                
        this.reserved = reserved;
        this.user = user;
    }

    public String getAnnotation() {
        return annotation;
    }
        
    public File getFile() {
        return file;
    }

    public String getType() {
        return type;
    }

    public String getRule() {
        return rule;
    }

    public boolean isEclipsed() {
        return annotation != null && annotation.indexOf(ANNOTATION_ECLIPSED) > -1;
    }

    public boolean isHijacked() {
        return annotation != null && annotation.indexOf(ANNOTATION_HIJACKED) > -1;
    }

    public boolean isLoadedButMissing() {
        return annotation != null && annotation.indexOf(ANNOTATION_LOADED_BUT_MISSING) > -1;
    }

    public boolean isRemoved() {
        return annotation != null && annotation.indexOf(ANNOTATION_CHECKEDOUT_BUT_REMOVED) > -1;
    }

    public boolean isReserved() {
        return reserved;
    }
    
    public FileVersionSelector getVersion() {
        return version;
    }    

    public FileVersionSelector getOriginVersion() {
        return originVersion;
    }
    
    public boolean isCheckedout() {
        if(version == null) {
            return false;
        }        
        return version.getVersionNumber() == FileVersionSelector.CHECKEDOUT_VERSION;
    }
    
    public String getVersionSelector() {
        // XXX a bit strange. isn't it?
        if(version == null) {
            return null;
        }
        if(isCheckedout() && originVersion != null) {
            return originVersion.getVersionSelector();
        } else {
            return version.getVersionSelector();
        }
    }

    public boolean isViewPrivate() {
        return type.equals(TYPE_VIEW_PRIVATE_OBJECT);
    }
    
    @Override
    public String toString() { 
        if(stringValue == null) {
            StringBuffer sb = new StringBuffer();
            sb.append("[");        
            sb.append(file.getAbsolutePath());
            sb.append(",");
            sb.append(type);
            if(version != null) {
                sb.append(",");
                sb.append(version.getPath());
                sb.append(File.pathSeparator);
                sb.append(version.getVersionNumber());
            }              
            if(originVersion != null) {
                sb.append(",");
                sb.append(originVersion.getPath());
                sb.append(File.pathSeparator);
                sb.append(originVersion.getVersionNumber());
            }               
            sb.append(",");
            sb.append(annotation);
            sb.append(",");
            sb.append(reserved ? ",reserved" : ",unreserved");            
            sb.append(",");
            sb.append(user);            
            sb.append("]");        
            stringValue = sb.toString();
            
        }
        return stringValue;
    }

    @Override
    public int hashCode() {        
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileEntry other = (FileEntry) obj;
        if (this.stringValue != other.stringValue && (this.stringValue == null || !this.stringValue.equals(other.stringValue))) {
            return false;
        }
        return true;
    }
    
}
