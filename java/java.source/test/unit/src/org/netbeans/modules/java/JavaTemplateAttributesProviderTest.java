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
