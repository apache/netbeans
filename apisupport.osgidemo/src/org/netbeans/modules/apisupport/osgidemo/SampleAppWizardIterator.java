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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
package org.netbeans.modules.apisupport.osgidemo;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class SampleAppWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    private final boolean netbinox;
    
    public SampleAppWizardIterator(boolean netbinox) {
        this.netbinox = netbinox;
    }
    
    public static SampleAppWizardIterator createIterator(Map<?,?> params) {
        return new SampleAppWizardIterator(params.containsKey("netbinox")); // NOI18N
    }
    
    private WizardDescriptor.Panel[] createPanels() {
        List<WizardDescriptor.Panel<?>> arr = new ArrayList<WizardDescriptor.Panel<?>>();
        arr.add(new SampleAppWizardPanel());
        return arr.toArray(new WizardDescriptor.Panel[0]);
    }
    
    private String[] createSteps() {
        List<String> arr = new ArrayList<String>();
        arr.add(NbBundle.getMessage(SampleAppWizardIterator.class, "LBL_CreateProjectStep"));
        return arr.toArray(new String[0]);
    }
    
    @Override
    public Set/*<FileObject>*/ instantiate() throws IOException {
        Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
        File dirF = FileUtil.normalizeFile((File) wiz.getProperty("projdir")); // NOI18N
        dirF.mkdirs();
        
        FileObject template = Templates.getTemplate(wiz);
        FileObject dir = FileUtil.toFileObject(dirF);
        Map<String,Object> params = new HashMap<String, Object>();
        if (netbinox) {
            params.put("netbinox", "true"); // NOI18N
        }
        unZipFile(null, template.getInputStream(), params, dir);
        
        // Always open top dir as a project:
        resultSet.add(dir);
        // Look for nested projects to open as well:
        Enumeration e = dir.getFolders(true);
        while (e.hasMoreElements()) {
            FileObject subfolder = (FileObject) e.nextElement();
            if (ProjectManager.getDefault().isProject(subfolder)) {
                resultSet.add(subfolder);
            }
        }

        File parent = dirF.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }
        
        return resultSet;
    }
    
    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        if (netbinox) {
            wiz.putProperty("name", NbBundle.getMessage(SampleAppWizardIterator.class, "CTL_NetbinoxProjectName"));
        } else {
            wiz.putProperty("name", NbBundle.getMessage(SampleAppWizardIterator.class, "CTL_FelixProjectName"));
        }
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
    }
    
    @Override
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty("projdir",null); // NOI18N
        this.wiz.putProperty("name",null); // NOI18N
        this.wiz = null;
        panels = null;
    }
    
    @Override
    public String name() {
        return NbBundle.getMessage(SampleAppWizardIterator.class, "SampleAppWizardIterator.name.format",
                new Object[] {new Integer(index + 1), new Integer(panels.length)});
    }
    
    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    
    @Override
    public boolean hasPrevious() {
        return index > 0;
    }
    
    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    
    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    @Override
    public final void addChangeListener(ChangeListener l) {}
    @Override
    public final void removeChangeListener(ChangeListener l) {}

    static void unZipFile(FileObject template, InputStream source, Map<String,Object> params, FileObject projectRoot) throws IOException {
        try {
            ZipInputStream str = new ZipInputStream(source);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(projectRoot, entry.getName());
                } else {
                    FileObject fo = FileUtil.createData(projectRoot, entry.getName());
                    String mime = fo.getMIMEType();
                    OutputStream out = fo.getOutputStream();
                    try {
                        if (mime.startsWith("text/")) {
                            StringBuilder sb = new StringBuilder();
                            for (;;) {
                                int r = str.read();
                                if (r == -1) {
                                    break;
                                }
                                if (r == '$') {
                                    sb.append("${r\"$\"}");
                                } else {
                                    sb.append((char)r);
                                }
                            }
                            ScriptEngineManager m = new ScriptEngineManager();
                            ScriptEngine engine = m.getEngineByName("FreeMarker");
                            assert engine != null : "FreeMarker engine needs to be present";
                            engine.getBindings(ScriptContext.ENGINE_SCOPE).putAll(params);
                            if (template != null) {
                                engine.getContext().setAttribute("org.openide.filesystems.FileObject", template, ScriptContext.ENGINE_SCOPE); // NOI18N
                            }
                            Writer w = new OutputStreamWriter(out, "UTF-8");
                            engine.getContext().setWriter(w);
                            try {
                                engine.eval(sb.toString());
                            } catch (ScriptException ex) {
                               throw new IOException(sb.toString(), ex);
                            }
                            w.flush();
                        } else {
                            FileUtil.copy(str, out);
                        }
                    } finally {
                        out.close();
                    }
                }
            }
        } finally {
            source.close();
        }
    }
    
}
