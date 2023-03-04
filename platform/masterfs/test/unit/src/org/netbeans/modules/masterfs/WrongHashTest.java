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
package org.netbeans.modules.masterfs;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class WrongHashTest extends NbTestCase {

    public WrongHashTest(String name) {
        super(name);
    }

    public void testTricksWithHas() throws Exception {
        final String MATH_FILE_NAME = "definition.math";
        
        clearWorkDir();
        File sbn = new File(getWorkDir(), "superbugName");
        sbn.mkdirs();
        
        {
            File fold3 = new File(sbn, "ADXACT_SLT.label");
            fold3.mkdirs();
            File math3 = new File(fold3, "definition.math");
            math3.createNewFile();
        }
        
        
        final File fold1 = new File(sbn, "ADZ#CT#SLT.label");
        fold1.mkdirs();
        File file = FileUtil.normalizeFile(fold1);
        File math1 = new File(file, MATH_FILE_NAME);
        math1.createNewFile();
        
        FileObject myFoMath;
        
        File fileChild = math1;
        FileObject myFo = FileUtil.toFileObject(file);
        System.out.println("File parent: " + file.getAbsolutePath());
        System.out.println("File child : " + fileChild.getAbsolutePath());
        System.out.println(String.format("HASH : %d \t- parent %s ", file.hashCode(), file.getName() ));
        System.out.println(String.format("HASH : %d \t- child  %s/%s",fileChild.hashCode(), file.getName(), fileChild.getName()));
        
        System.out.println("");
        myFoMath = myFo.getFileObject(MATH_FILE_NAME);
        System.out.println("FileObject parent : " + myFo.getPath());
        System.out.println("FileObject child  : " + myFoMath.getPath());
        assertEquals("The right parent", myFo, myFoMath.getParent());

        File fold2 = new File(sbn, "ADXACT#SLT.label");
        fold2.mkdir();
        File math2 = new File(fold2, MATH_FILE_NAME);
        math2.createNewFile();
        file = FileUtil.normalizeFile(fold2);
        fileChild = new File(file, MATH_FILE_NAME);
        myFo = FileUtil.toFileObject(file);
        System.out.println("File parent: " + file.getAbsolutePath());
        System.out.println("File child : " + fileChild.getAbsolutePath());
        System.out.println(String.format("HASH : %d \t- parent %s ", file.hashCode(), file.getName() ));
        System.out.println(String.format("HASH : %d \t- child  %s/%s",fileChild.hashCode(), file.getName(), fileChild.getName()));
        
        myFoMath = myFo.getFileObject(MATH_FILE_NAME);
        System.out.println("FileObject parent : " + myFo.getPath());
        System.out.println("FileObject child  : " + myFoMath.getPath());
        assertEquals("Correct parent for the second time", myFo, myFoMath.getParent());
      
    }
}
