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
package org.netbeans.modules.apisupport.project.ui.wizard.winsys;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.spi.ExecProject;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.util.TaskListener;

public final class DesignSupport implements TaskListener, Runnable {
    private final Project project;
    private final JButton toEnable;
    private final AtomicReference<FileObject> userDir;


    private DesignSupport(Project p, JButton toEnable, AtomicReference<FileObject> ud) {
        this.project = p;
        this.toEnable = toEnable;
        this.userDir = ud;
    }
    
    static boolean isDesignModeSupported(NbModuleProvider info) {
        try {
            SpecificationVersion current = info.getDependencyVersion("org.openide.windows");
            if (current == null) {
                return false;
            }
            return current.compareTo(new SpecificationVersion("6.45")) >= 0; // NOI18N
        } catch (IOException ex) {
            Logger.getLogger(NewTCIterator.class.getName()).log(Level.INFO, null, ex);
            return false;
        }
        
    }
    
    static JComponent warningPanel() throws MissingResourceException {
        JTextArea a = new JTextArea();
        a.setEditable(false);
        a.setText(org.openide.util.NbBundle.getMessage(BasicSettingsPanel.class, "MSG_ReallyLaunch", new Object[]{}));
        a.setOpaque(false);
        return a;
    }
    
    public static Task invokeDesignMode(
        Project prj, AtomicReference<FileObject> userDir
    ) throws IOException {
        return invokeDesignMode(prj, userDir, true, true);
    }
    
    
    static Task invokeDesignMode(
        Project prj, AtomicReference<FileObject> userDir, boolean warn, boolean warnPrevResult
    ) throws IOException {
        ExecProject es = prj.getLookup().lookup(ExecProject.class);
        if (es == null) {
            throw new IOException("Project " + prj.getProjectDirectory() + " does not support execution!"); // NOI18N
        }
        if (warn) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(warningPanel());
            nd.setOptions(new Object[]{NotifyDescriptor.YES_OPTION, NotifyDescriptor.CANCEL_OPTION});
            if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.YES_OPTION) {
                return null;
            }
        }
        // XXX better would be an API in NbModuleProvider for finding generic build dir (${build.dir} or ${project.basedir}/target/)
        File path = new File(prj.getLookup().lookup(NbModuleProvider.class).getClassesDirectory().getParentFile(), "designdir");
        FileObject fo = FileUtil.toFileObject(path);
        if (fo != null) {
            if (warnPrevResult) {
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(org.openide.util.NbBundle.getMessage(BasicSettingsPanel.class, "MSG_AlreadyLaunched", new Object[]{}));
                nd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
                if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
                    return null;
                }
            }
            fo.delete();
        }
        fo = FileUtil.createFolder(path);
        userDir.set(fo);
        return es.execute(
            "--nosplash", // NOI18N
            "-J-Dorg.netbeans.core.WindowSystem.designMode=true", // NOI18N
            "--userdir " + path // NOI18N
        );
    }

    static @CheckForNull Set<String> existingModes(NewTCIterator.DataModel data) throws IOException {
        FileSystem fs = data.getProject().getLookup().lookup(NbModuleProvider.class).getEffectiveSystemFilesystem();
        data.setSFS(fs);
        FileObject foRoot = fs.getRoot().getFileObject("Windows2/Modes"); //NOI18N
        if (foRoot != null) {
            FileObject[] fos = foRoot.getChildren();
            Set<String> col = new TreeSet<String>();
            for (FileObject fo : fos) {
                if (fo.isData() && "wsmode".equals(fo.getExt())) { //NOI18N
                    col.add(fo.getName());
                    data.existingMode(fo.getName());
                }
            }
            return col;
        } else {
            return null;
        }
    }
    
    public static String readMode(FileObject fo) throws IOException {
        final InputStream is = fo.getInputStream();
        try {
            StringWriter w = new StringWriter();
            Source t = new StreamSource(DesignSupport.class.getResourceAsStream("polishing.xsl")); // NOI18N
            Transformer tr = TransformerFactory.newInstance().newTransformer(t);
            Source s = new StreamSource(is);
            Result r = new StreamResult(w);
            tr.transform(s, r);
            return w.toString();
        } catch (TransformerException ex) {
            throw new IOException(ex);
        } finally {
            is.close();
        }
    }
    public static void redefineLayout(Project p, JButton toEnable) {
        try {
            AtomicReference<FileObject> userDir = new AtomicReference<FileObject>();
            Task task = invokeDesignMode(p, userDir);
            if (task == null) {
                toEnable.setEnabled(true);
            }
            task.addTaskListener(new DesignSupport(p, toEnable, userDir));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void taskFinished(Task task) {
        FileObject modeDir = userDir.get().getFileObject("config/Windows2Local/Modes");
        OutputStream os;
        if (modeDir != null) {
            StringBuilder sb = new StringBuilder();
            try {
                
                FileSystem layer = findLayer(project);
                if (layer == null) {
                    throw new IOException("Cannot find layer in " + project); // NOI18N
                }
                for (FileObject m : modeDir.getChildren()) {
                    if (m.isData() && "wsmode".equals(m.getExt())) { 
                        final String name = "Windows2/Modes/" + m.getNameExt(); // NOI18N
                        FileObject mode = FileUtil.createData(layer.getRoot(), name); // NOI18N
                        os = mode.getOutputStream();
                        os.write(DesignSupport.readMode(m).getBytes(StandardCharsets.UTF_8));
                        os.close();
                        sb.append(name).append("\n");
                    }
                }
                NotifyDescriptor nd = new NotifyDescriptor.Message(
                    NbBundle.getMessage(DesignSupport.class, "MSG_ModesGenerated", new Object[] {sb}), 
                    NotifyDescriptor.INFORMATION_MESSAGE
                );
                DialogDisplayer.getDefault().notifyLater(nd);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        EventQueue.invokeLater(this);
    }

    @Override
    public void run() {
        toEnable.setEnabled(true);
    }
    
    static FileSystem findLayer(Project p) throws IOException {
        NbModuleProvider nbmp = p.getLookup().lookup(NbModuleProvider.class);
        return nbmp != null ? nbmp.getEffectiveSystemFilesystem() : null;
    }
}
