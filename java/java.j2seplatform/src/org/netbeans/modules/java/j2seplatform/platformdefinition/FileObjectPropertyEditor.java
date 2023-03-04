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
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.beans.PropertyEditorSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class FileObjectPropertyEditor extends PropertyEditorSupport {

    public String getAsText() {
        try {
            List<FileObject> fileobjs = (List<FileObject>)this.getValue();
            StringBuffer result = new StringBuffer ();
            boolean first = true;
            for (Iterator<FileObject> it = fileobjs.iterator(); it.hasNext();) {
                FileObject fo = it.next();
                File f = FileUtil.toFile(fo);
                if (f != null) {
                    if (!first) {
                        result.append (File.pathSeparator);
                    }
                    else {
                        first = false;
                    }
                    result.append(f.getAbsolutePath());
                }
            }
            return result.toString ();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void setAsText(String text) throws IllegalArgumentException {
        try {
            List<FileObject> fileObjs = new ArrayList<FileObject> ();
            if (text != null) {
                StringTokenizer tk = new StringTokenizer (text, File.pathSeparator);
                while (tk.hasMoreTokens()) {
                    String path = tk.nextToken();
                    File f = new File (path);
                    fileObjs.add(FileUtil.toFileObject(f));
                }
            }
            setValue (fileObjs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
