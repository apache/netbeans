/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.j2seplatform.platformdefinition.jrtfs;

import org.netbeans.modules.java.j2seplatform.platformdefinition.jrtfs.NBJRTFileSystem;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 *
 * @author lahvac
 */
public class NBJRTFileSystemTest extends NbTestCase {

    public NBJRTFileSystemTest(String name) {
        super(name);
    }

    public void testOpen() throws IOException {
        String jigsawHome = System.getProperty("jigsaw.home");
        if (jigsawHome == null)
            return;
        File jdkHome = new File(jigsawHome);
        FileSystem fs = NBJRTFileSystem.create(jdkHome);
        assertNotNull(fs);
        FileObject jlObjectClass = fs.getRoot().getFileObject("java.base/java/lang/Object.class");
        assertNotNull(jlObjectClass);

        try (InputStream in = jlObjectClass.getInputStream()) {
            while (in.read() != (-1))
                ;
        }

        //list all:
        Enumeration<? extends FileObject> list = fs.getRoot().getChildren(true);

        while (list.hasMoreElements())
            list.nextElement();
    }

    public void testToPath() throws IOException {
        final String jigsawHome = System.getProperty("jigsaw.home");    //NOI18N
        if (jigsawHome == null) {
            return;
        }
        final File jdkHome = new File(jigsawHome);
        final FileSystem fs = NBJRTFileSystem.create(jdkHome);
        assertNotNull(fs);
        final FileObject jlObjectClass = fs.getRoot().getFileObject("java.base/java/lang/Object.class");    //NOI18N
        assertNotNull(jlObjectClass);
        final Object path = jlObjectClass.getAttribute(Path.class.getName());
        assertNotNull(path);
        assertTrue (path instanceof Path);
        assertEquals("/java.base/java/lang/Object.class", path.toString()); //NOI18N
    }

}
