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
package org.netbeans.modules.java.source.parsing;

import com.sun.tools.javac.code.Symbol;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.lang.model.element.Element;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.Task;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Petr Hejl
 * @author Tomas Zezula
 */
public class CtSymArchiveTest extends NbTestCase {

    public CtSymArchiveTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        clearWorkDir();
        FileUtil.setMIMEType(FileObjects.CLASS, ClassParser.MIME_TYPE);
        MockMimeLookup.setInstances(MimePath.get(ClassParser.MIME_TYPE), new ClassParserFactory());
        TestJavaPlatformProviderImpl.ALLOW_INSTALL_FOLDERS = true;
//        System.setProperty("CachingArchiveProvider.disableCtSym", "true");
    }

    @Override
    protected void tearDown() throws Exception {
        TestJavaPlatformProviderImpl.ALLOW_INSTALL_FOLDERS = false;
        super.tearDown();
    }

    public void testIssue247469() throws IOException {
        final JavaPlatform jp = JavaPlatformManager.getDefault().getDefaultPlatform();
        assertNotNull(jp);
        final ClasspathInfo cpInfo = ClasspathInfo.create(jp.getBootstrapLibraries(), ClassPath.EMPTY, ClassPath.EMPTY);
        assertNotNull(cpInfo);
        final JavaSource js = JavaSource.create(cpInfo);
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(final CompilationController cc) throws Exception {
                final PackageElement packageElement = cc.getElements().getPackageElement("java.lang"); // NOI18N
                for (Element elem : packageElement.getEnclosedElements()) {
                    if ("ProcessBuilder$1".equals(elem.getSimpleName().toString())) { // NOI18N
                        TypeElement te = (TypeElement) elem;
                        assertEquals(NestingKind.ANONYMOUS, te.getNestingKind());
                        break;
                    }
                }
            }
        }, true);
    }

    public void testCtSym() throws Exception {
        final JavaPlatform jp = JavaPlatform.getDefault();
        assertNotNull(jp);
        if (jp.getInstallFolders().iterator().next().getFileObject("lib/modules") != null) {
            //the semantics of ct.sym is changed since JDK 9, disable this test for now:
            log("Running on JDK 9+, passed vacuously.");  //NOI18N
            return ;
        }
        final FileObject ctSym = jp.getInstallFolders().iterator().next().getFileObject("lib/ct.sym");  //NOI18N
        if (ctSym == null) {
            log(String.format("No ct.sym for platform: %s installed in: %s",jp.getDisplayName(), jp.getInstallFolders()));  //NOI18N
            return;
        }
        final ClassPath boot = jp.getBootstrapLibraries();
        assertNotNull(boot);
        FileObject base = null;
        for (FileObject root : boot.getRoots()) {
            if (root.getFileObject("java/lang/Object.class") != null) { //NOI18N
                base = root;
                break;
            }
        }
        assertNotNull(base);
        final Map<String,List<String>> ctContent = createMap(ctSym, "META-INF/sym/rt.jar/");
        final Map<String,List<String>> baseContent = createMap(FileUtil.getArchiveFile(base), null);
        final Archive arch = CachingArchiveProvider.getDefault().getArchive(
                base.toURL(),
                true);
        assertNotNull(arch);
        for (Map.Entry<String,List<String>> e : baseContent.entrySet()) {
            final String folder = e.getKey();
            List<String> folderContent = ctContent.get(folder);
            if (folderContent == null) {
                folderContent = e.getValue();
            }
            final List<String> archContent = asList(arch.getFiles(folder, null, EnumSet.of(JavaFileObject.Kind.CLASS), null, false));
            compare(folderContent,archContent);
        }
    }

    private static void compare(List<String> expected, List<String> res) {
        Collections.sort(expected);
        Collections.sort(res);
        assertEquals(expected, res);
    }

    private static Map<String,List<String>> createMap(FileObject root, String pathIn) throws IOException {
        final Map<String,List<String>> result = new HashMap<>();
        try (ZipFile zf = new ZipFile(FileUtil.toFile(root))) {
            final Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry e = entries.nextElement();
                if (e.isDirectory()) {
                    continue;
                }
                String name = e.getName();
                if (pathIn != null) {
                    if(!name.startsWith(pathIn)) {
                        continue;
                    } else {
                        name = name.substring(pathIn.length());
                    }
                }
                final String[] names = FileObjects.getFolderAndBaseName(name, '/');    //NOI18N
                if (JavaFileObject.Kind.CLASS.equals(FileObjects.getKind(FileObjects.getExtension(names[1])))) {
                    List<String> c = result.get(names[0]);
                    if (c == null) {
                        result.put(names[0],c = new ArrayList<>());
                    }
                    c.add(names[1]);
                }
            }
        }
        return result;
    }

    private static List<String> asList(Iterable<? extends JavaFileObject> it) {
        final List<String> res = new ArrayList<>();
        for (JavaFileObject fo : it) {
            res.add(fo.getName());
        }
        return res;
    }
}
