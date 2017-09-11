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
