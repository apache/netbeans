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

package org.netbeans.modules.project.ui;

import java.lang.ref.WeakReference;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.TemplateChooserPanelGUI.FileChooserBuilder;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class TemplateChooserPanelGUITest extends NbTestCase {
    TemplateChooserPanelGUI instance;
    
    public TemplateChooserPanelGUITest(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    protected @Override int timeOut() {
        return 300000;
    }

    public void testReadValues() {
        Project p = new P();
        String category = "";
        String template = "";
        instance = new TemplateChooserPanelGUI(true);
        instance.readValues(p, category, template);
        
        instance.addNotify();
        
        instance.removeNotify();
        
        WeakReference<Project> ref = new WeakReference<Project>(p);
        p = null;
        assertGC("Panel does not hold ref", ref);
    }

    public void testFileChooserBuilder() throws Exception {
        FileObject r = FileUtil.createMemoryFileSystem().getRoot();
        FileObject f = r.createFolder("Licenses");
        f.setAttribute("templateCategory", "invisible");
        FileObject t = f.createData("irrelevant");
        t.setAttribute("template", true);
        f = r.createFolder("Projects");
        f.setAttribute("simple", false);
        t = f.createData("irrelevant");
        t.setAttribute("template", true);
        f = r.createFolder("Main");
        t = f.createData("t1");
        t.setAttribute("template", true);
        t.setAttribute("templateCategory", "main");
        t = f.createData("t2");
        t.setAttribute("template", true);
        t.setAttribute("templateCategory", "misc");
        t = f.createData("t3");
        t.setAttribute("template", true);
        t.setAttribute("templateCategory", "other");
        f.createData("data");
        f = f.createFolder("Snippets");
        f.setAttribute("templateCategory", "always-hidden");
        t = f.createData("snippet");
        t.setAttribute("template", true);
        t.setAttribute("templateCategory", "main");
        f.createData("data");
        f = r.createFolder("Other");
        t = f.createData("t4");
        t.setAttribute("template", true);
        t.setAttribute("templateCategory", "other");
        f = r.createFolder("Samples").createFolder("Main");
        t = f.createData("t5");
        t.setAttribute("template", true);
        t.setAttribute("templateCategory", "main");
        TemplateChooserPanelGUI gui = new TemplateChooserPanelGUI(true);
        gui.construct();
        gui.finished();
        gui.readValues(new P(), null, null);
        FileChooserBuilder builder = gui.new FileChooserBuilder();
        assertChildren("Main, Samples[Main]", builder.createCategoriesChildren(DataFolder.findFolder(r), null));
        assertChildren("t1, t2", builder.createTemplatesChildren(DataFolder.findFolder(r.getFileObject("Main")), null));
        assertChildren("t5", builder.createTemplatesChildren(DataFolder.findFolder(r.getFileObject("Samples/Main")), null));
        assertChildren("t1, t2", builder.createTemplatesChildren(DataFolder.findFolder(r.getFileObject("Main")), "t"));
        assertChildren("t2", builder.createTemplatesChildren(DataFolder.findFolder(r.getFileObject("Main")), "2"));
    }

    private static void assertChildren(String repn, Children c) {
        StringBuilder b = new StringBuilder();
        representationOf(c, b);
        assertEquals(repn, b.toString());
    }
    private static void representationOf(Children c, StringBuilder b) {
        boolean first = true;
        for (Node n : c.getNodes(true)) {
            if (first) {
                first = false;
            } else {
                b.append(", ");
            }
            b.append(n.getDisplayName());
            if (!n.isLeaf()) {
                b.append('[');
                representationOf(n.getChildren(), b);
                b.append(']');
            }
        }
    }
        
    private static class P implements Project {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        
        public @Override FileObject getProjectDirectory() {
            return root;
        }

        public @Override Lookup getLookup() {
            return Lookups.singleton(new RecommendedTemplates() {
                public @Override String[] getRecommendedTypes() {
                    return new String[] {"main", "misc"};
                }
            });
        }
        
    }
}
