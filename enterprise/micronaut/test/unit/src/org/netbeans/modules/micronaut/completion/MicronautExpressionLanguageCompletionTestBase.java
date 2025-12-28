/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.micronaut.completion;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.text.Document;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.lsp.Completion;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.micronaut.NbSuiteTestBase;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Dusan Balek
 */
public class MicronautExpressionLanguageCompletionTestBase extends NbSuiteTestBase {

    private Project project;

    public MicronautExpressionLanguageCompletionTestBase(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        FileObject dataFO = FileUtil.toFileObject(getDataDir());
        FileObject testApp = dataFO.getFileObject("maven/micronaut4/simple");
        project = openPrimeAndIndexProject(testApp);
    }

    protected void performTest(String text, int offset, String goldenFileName) throws Exception {
        FileObject testSourceFO = project.getProjectDirectory().getFileObject("src/main/java/com/example/expression/Example.java");
        assertNotNull(testSourceFO);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        assertNotNull(testSourceDO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        assertNotNull(ec);
        final Document doc = ec.openDocument();
        assertNotNull(doc);
        if (text.length() > 0) {
            doc.insertString(175, text, null);
        }
        List<Completion> items = new ArrayList<>();
        new MicronautDataCompletionCollector().collectCompletions(doc, 175 + offset, null, completion -> {
            items.add(completion);
        });
        if (text.length() > 0) {
            doc.remove(175, text.length());
        }
        items.sort((c1, c2) -> {
            return c1.getSortText().compareTo(c2.getSortText());
        });
        File output = new File(getWorkDir(), getName() + ".out");
        try (Writer out = new FileWriter(output)) {
            for (Completion item : items) {
                if (!(org.openide.util.Utilities.isMac() && item.getLabel().equals("apple") //ignoring 'apple' package
                        || item.getLabel().equals("jdk")        //ignoring 'jdk' package introduced by jdk1.7.0_40
                        || item.getLabel().equals("netscape")   //ignoring 'netscape' package present in some JDK builds
                        || item.getLabel().equals("nbjavac")   //ignoring 'nbjavac' package from nbjavac
                        || item.getLabel().equals("oracle"))) { //ignoring 'oracle' package present in some JDK builds
                    out.write(item.getLabel());
                    if (item.getLabelDetail() != null) {
                        out.write(item.getLabelDetail());
                    }
                    if (item.getLabelDescription() != null) {
                        out.write(" : " + item.getLabelDescription());
                    }
                    out.write("\n");
                }
            }
        }
        File goldenFile = getGoldenFile(goldenFileName);
        File diffFile = new File(getWorkDir(), getName() + ".diff");
        assertFile(output, goldenFile, diffFile);
    }

    private Project openPrimeAndIndexProject(FileObject prjCopy) throws Exception {
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        assertNotNull(p);
        OpenProjects.getDefault().open(new Project[] { p }, true);
        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        assertNotNull(ap);

        CountDownLatch primingLatch = new CountDownLatch(1);
        ActionProgress progress = new ActionProgress() {
            @Override
            protected void started() {
            }

            @Override
            public void finished(boolean success) {
                primingLatch.countDown();
            }

        };
        ap.invokeAction(ActionProvider.COMMAND_PRIME, Lookups.fixed(progress));
        primingLatch.await(10, TimeUnit.MINUTES);

        CountDownLatch indexingLatch = new CountDownLatch(1);
        JavaSource.create(ClasspathInfo.create(prjCopy)).runWhenScanFinished(info -> {
            indexingLatch.countDown();
        }, true);
        indexingLatch.await(10, TimeUnit.MINUTES);

        return p;
    }
}
