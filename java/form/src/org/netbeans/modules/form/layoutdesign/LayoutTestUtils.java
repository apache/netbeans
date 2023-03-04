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

package org.netbeans.modules.form.layoutdesign;

import java.awt.Rectangle;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.form.FormDataObject;
import org.netbeans.modules.form.FormDesigner;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * This class collects various static methods for examining the layout.
 * For modifying methods see LayoutOperations class.
 *
 * @author Martin Grebac
 */

public class LayoutTestUtils implements LayoutConstants {
    
    static void writeString(List<String> codeList, String name, String value) {
        if (value != null) {
            codeList.add("String " + name + "= \"" + value + "\";"); //NOI18N
        } else {
            codeList.add("String " + name + " = null;"); //NOI18N
        }
    }

    static void writeLayoutComponentArray(List<String> codeList, String arrayName, String lcName) {
        codeList.add("LayoutComponent[] " + arrayName + " = new LayoutComponent[] { " + lcName + " };"); //NOI18N
    }
    
    static void writeCollection(List<String> codeList, String name, Collection c) {
        codeList.add("Collection " + name + " = new ArrayList();"); //NOI18N
	Iterator i = c.iterator();
        while (i.hasNext()) {
            codeList.add(name + ".add(\"" + (String)i.next() + "\");"); // NOI18N
        }
    }

    static void writeStringArray(List<String> codeList, String name, String[] compIds) {
        codeList.add("String[] " + name + " = new String[] {"); //NOI18N
        for (int i=0; i < compIds.length; i++) {
            codeList.add("\"" + compIds[i] + "\"" + (i+1 < compIds.length ? "," : "")); // NOI18N
        }
        codeList.add("};"); //NOI18N
    }

    static void writeIntArray(List<String> codeList, String name, int[] values) {
        codeList.add("int[] " + name + " = new int[] {"); //NOI18N
        for (int i=0; i < values.length; i++) {
            codeList.add(Integer.toString(values[i]) + (i+1 < values.length ? "," : "")); // NOI18N
        }
        codeList.add("};"); //NOI18N
    }
    
    static void writeRectangleArray(List<String> codeList, String name, Rectangle[] bounds) {
        codeList.add("Rectangle[] " + name + " = new Rectangle[] {"); //NOI18N
        for (int i=0; i < bounds.length; i++) {
            codeList.add("new Rectangle(" + bounds[i].x + ", " // NOI18N
                                                        + bounds[i].y + ", " // NOI18N
                                                        + bounds[i].width + ", " // NOI18N
                                                        + bounds[i].height + (i+1 < bounds.length ? "), " : ")")); // NOI18N
        }
        codeList.add("};"); // NOI18N
    }
    
    static void dumpTestcode(List codeList, DataObject form, final int modelCounter) {
        FileWriter fw = null;
        StringBuilder template = new StringBuilder();
        
        if (form == null) return;
        try {

            FileObject primaryFile = form.getPrimaryFile();

            //Read the template for test class
            InputStream in = LayoutTestUtils.class.getResourceAsStream("/org/netbeans/modules/form/resources/LayoutModelAutoTest_template"); //NOI18N
            LineNumberReader lReader = new LineNumberReader(new InputStreamReader(in));
            while (lReader.ready()) {
                template.append(lReader.readLine()).append('\n');
            }
            lReader.close();

            //Get the code into one string
            final StringBuffer code = new StringBuffer();
            Iterator i = codeList.iterator();
            while (i.hasNext()) {
                String line = (String)i.next();
                code.append(line).append('\n');
            }
	    
            //Find a name for the test file
            String testClassName = primaryFile.getName() + "Test"; //NOI18N
            
            FileObject testFO = primaryFile.getParent().getFileObject(testClassName, "java");//NOI18N
            if (testFO == null) {
                testFO = primaryFile.getParent().createData(testClassName, "java"); //NOI18N
                
                //Rename the class in template to correct class name
                String output = template.toString().replace("${CLASS_NAME}", testFO.getName()); //NOI18N

                //Write the file to disc
                fw = new FileWriter(FileUtil.toFile(testFO));
                fw.write(output);
                fw.close();
            }

            //8. Add the method to test class
            String oldContent = testFO.asText();
            int idx = oldContent.lastIndexOf('}');
            StringBuilder sb = new StringBuilder();
            sb.append(oldContent.substring(0,idx));
            sb.append("public void doChanges"); // NOI18N
            sb.append(modelCounter);
            sb.append("() {\n"); // NOI18N
            sb.append(code);
            sb.append("}\n\n"); // NOI18N
            sb.append(oldContent.substring(idx));
            FileLock lock = testFO.lock();
            try {
                Writer writer = new OutputStreamWriter(testFO.getOutputStream(lock));
                writer.write(sb.toString());
                writer.close();
            } finally {
                lock.releaseLock();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
    }
    
    public static FileObject getTargetFolder(FileObject file) {
	FileObject targetFolder = file.getParent();
	try {
	    FileObject folder = file.getParent().getParent().getParent().getParent().getParent().getParent().getParent().getFileObject("data/goldenfiles"); //NOI18N
	    if (folder != null) {
		targetFolder = folder;
	    }
	} catch (NullPointerException npe) {
	    // just ignore, it means the path doesn't exist
	}
	return targetFolder;
    }
    
    public static void writeTest(FormDesigner fd, FormDataObject formDO, Map<String,String> idToNameMap, LayoutModel lm) {
	FileObject formFO = formDO.getFormFile();

	fd.getLayoutDesigner().dumpTestcode(formDO);

	FileWriter fw = null;
	try {
	    FileObject targetFolder = getTargetFolder(formFO);
	    FileObject fo = targetFolder.createData(formFO.getName() + "Test-ExpectedEndModel" + Integer.toString(fd.getLayoutDesigner().getModelCounter()), "txt"); //NOI18N
	    fw = new FileWriter(FileUtil.toFile(fo));
	    fw.write(lm.dump(idToNameMap));
	    StatusDisplayer.getDefault().setStatusText("The test was successfully written: " + FileUtil.getFileDisplayName(fo)); // NOI18N
	} catch (IOException ex) {
	    ex.printStackTrace();
	    return;
	} finally {
	    try {
		if (fw != null) fw.close();
	    } catch (IOException io) {
		//TODO
	    }
	}
    }
    
}
