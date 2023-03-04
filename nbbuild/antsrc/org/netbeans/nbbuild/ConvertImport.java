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
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(bytes);
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
                try ( PrintStream ps = new PrintStream(file)) {
                    ps.print(xml);
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
