/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
