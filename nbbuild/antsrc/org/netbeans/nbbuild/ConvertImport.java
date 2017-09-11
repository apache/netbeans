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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.apache.tools.ant.*;

/**
 * It replaces paths in import element of buildscript. It is for
 *  converting xtest builscript of modules to test distribution layout. <br>
 * 
 * Parameters:
 *  <ul>
 *    <li> oldname  - name of build script (for example 'cfg-qa-functiona.xml') 
 *    <li> newpath  - new path if file  (for example '../templates/cfg-qa-functional.xml')
 *    <li> attribute prefix - property name (example fir dist 'dist.dir' it add ${dist.dir}/ prefix)  
 *    <li> file - build script
 *  </ul>
 */
public class ConvertImport extends Task {
    private String oldName;
    private String newPath;
    private String propertyPrefixName;
    private File file;
    int endOfComment;
    public void execute() throws BuildException {
        if (!file.exists()) {
            throw new BuildException("File " + file + " doesn't exist.");
        }
        byte bytes[] = new byte[(int)file.length()];
        try {
            FileInputStream fis = new FileInputStream(file);
            try {
                fis.read(bytes);
            } finally {
                fis.close();
            }
            String xml = new String(bytes);
            String oldXml = xml;
            int end = 0;
            // <import ... file=" "/>
            for (int offset = 0 ; offset < xml.length() ; offset = end + 1) {
                
                int start = xml.indexOf("<import ",offset);
                if (start == -1) {
                    break;
                }
                if (isComment(xml,offset,start)) {
                    end = endOfComment;
                    continue;
                }
                end = xml.indexOf("/>",start); 
                if (end == -1) {
                    continue;
                }
                int fileIndex = xml.indexOf("file",start);
                int q1 = xml.indexOf("\"",fileIndex);
                int q2 = xml.indexOf("\'",fileIndex);
                int qStart = (q1 != -1 && ( q2 > q1 || q2 == -1))  ? q1 : q2;
                if (qStart == -1 ) {
                    throw new BuildException("Invalid xml " + file);
                }
                char qCh = (qStart == q1) ? '"' : '\'';
                int qEnd = xml.indexOf(qCh,qStart + 1);
                if (qEnd == -1 || qEnd > end) {
                   throw new BuildException("Invalid xml : " + file);
                }
                
                int nameIdx = xml.indexOf(oldName,qCh);
                if (nameIdx != -1 && nameIdx < qEnd) {
                    xml = replaceFileName(xml,qStart,qEnd);
                    end = xml.indexOf("/>",start);
                } 
                
            } // while 
            if (oldXml != xml) {
                // changed file
                PrintStream ps = new PrintStream(file);
                try {
                    ps.print(xml);
                } finally {
                    ps.close();
                }
            }
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public String getNewPath() {
        return newPath;
    }

    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

    public String getPropertyPrefixName() {
        return propertyPrefixName;
    }

    public void setPropertyPrefixName(String propertyPrefixName) {
        this.propertyPrefixName = propertyPrefixName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    private String replaceFileName(String xml, int qStart, int qEnd) {
        StringBuffer sb = new StringBuffer();
        sb.append(xml.substring(0,qStart + 1));
        if (propertyPrefixName != null) {
            sb.append("${" + propertyPrefixName + "}/");
        }
        sb.append(getNewPath());
        sb.append(xml.substring(qEnd));
        return sb.toString();
    }

    /** check if position pos is inside xml comment
     */ 
    private boolean isComment(String xml, int offset, int position) {
        boolean isComment = false;
        while (offset < position) {
            int i = -1;
            if (isComment) {
                i = xml.indexOf("-->",offset);
                endOfComment = i + 2;
            } else {
                i = xml.indexOf("<!--",offset);
            }
            if (i < position && i != -1) {
                isComment = !isComment;
                offset = i;
            } else {
                break;
            }                 
        }
        return isComment;
    }
    
}
