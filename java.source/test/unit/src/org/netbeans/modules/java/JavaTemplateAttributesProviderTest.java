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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.java;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Pokorsky
 */
public class JavaTemplateAttributesProviderTest extends NbTestCase {
    
    public JavaTemplateAttributesProviderTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        CPP.clear();
        MockServices.setServices(JavaDataLoader.class, CPP.class, JavaTemplateAttributesProvider.class);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAttributesFor() throws Exception {
        this.clearWorkDir();
        File wd = this.getWorkDir();
        FileObject froot = FileUtil.toFileObject(wd);
        
        FileObject ftarget = FileUtil.createFolder(froot, "pkg");
        CPP.register(ftarget, ClassPath.SOURCE, ClassPathSupport.createClassPath(new FileObject[] {froot}));
        FileObject ftemplate = FileUtil.createData(ftarget, "EmptyClass.java");
        ftemplate.setAttribute("javax.script.ScriptEngine", "freemarker");
        
        
        DataObject template = DataObject.find(ftemplate);
        DataFolder target = DataFolder.findFolder(ftarget);
        String name = "TargetClass";
        JavaTemplateAttributesProvider instance = new JavaTemplateAttributesProvider();
        Map<String, ? extends Object> result = instance.attributesFor(template, target, name);
        
        assertEquals("pkg", result.get("package"));
    }
    
    public static class CPP implements ClassPathProvider {
        
        private static final Map<FileObject,Map<String,ClassPath>> data = new HashMap<FileObject,Map<String,ClassPath>>();
        
        static void clear () {
            data.clear();
        }
        
        static void register (FileObject fo, String type, ClassPath cp) {
            Map<String,ClassPath> m = data.get (fo);
            if (m == null) {
                m = new HashMap<String,ClassPath>();
                data.put (fo,m);
            }
            m.put (type,cp);
        }
            
        public ClassPath findClassPath(FileObject file, String type) {
            
            Map<String,ClassPath> m = data.get (file);
            if (m == null) {
                return null;
            }
            return m.get (type);
        }        
    }

}
