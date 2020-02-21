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
package org.netbeans.modules.cnd.editor.filecreation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.CreateFromTemplateHandler;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

public class CCFSrcFileIterator implements TemplateWizard.ProgressInstantiatingIterator<WizardDescriptor> {

    /** Holds list of event listeners */
    private static final List<SrcFileWizardListener> listenerList = new ArrayList<SrcFileWizardListener>(0);
    protected WizardDescriptor.Panel<WizardDescriptor> targetChooserDescriptorPanel;
    protected TemplateWizard templateWizard;
    // special mime type for C Headers extensions
    private static final String C_HEADER_MIME_TYPE = "text/x-c/text/x-h"; // NOI18N

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return targetChooserDescriptorPanel;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public synchronized void nextPanel() {
    }

    @Override
    public synchronized void previousPanel() {
    }


    @Override
    public void initialize(WizardDescriptor wiz) {
        this.templateWizard = (TemplateWizard) wiz;
        targetChooserDescriptorPanel = createPanel(templateWizard);
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
    }

    protected WizardDescriptor.Panel<WizardDescriptor> createPanel(TemplateWizard wiz) {
        DataObject dobj = wiz.getTemplate();
        FileObject fobj = dobj.getPrimaryFile();
        String mimeType = fobj.getMIMEType();
        MIMEExtensions extensions = MIMEExtensions.get(mimeType);
        if (extensions != null) {
            Project project = Templates.getProject(wiz);
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            if (MIMENames.HEADER_MIME_TYPE.equals(extensions.getMIMEType())) {
                // this is the only place where we want to differ c headers from cpp headers (creation of new one)
                if (dobj.getPrimaryFile().getAttribute(C_HEADER_MIME_TYPE) != null) {
                    MIMEExtensions cHeaderExtensions = MIMEExtensions.get(C_HEADER_MIME_TYPE);
                    if ((cHeaderExtensions == null) || !C_HEADER_MIME_TYPE.equals(cHeaderExtensions.getMIMEType())) {
                        System.err.println("not found extensions for C Headers"); // NOI18N
                    } else {
                        extensions = cHeaderExtensions;
                    }
                }
            }
            String defaultExt = null; // let the chooser panel decide default extension
            if (mimeType.equals(MIMENames.SHELL_MIME_TYPE)) {
                // for shell scripts set default extension explicitly
                defaultExt = fobj.getExt();
            } else if (mimeType.equals(MIMENames.HEADER_MIME_TYPE) && fobj.getExt().length() == 0) {
                // for standard header without extension
                defaultExt = fobj.getExt();
            }

            NewCndFileChooserPanel panel = new NewCndFileChooserPanel(project, groups, null, extensions, defaultExt);
            return panel;
        } else {
            return wiz.targetChooser();
        }
    }

    @Override
    public Set<DataObject> instantiate(ProgressHandle handle) throws IOException {
        try {
            handle.start();
            return instantiate();
        } finally {
            handle.finish();
        }
    }

    @Override
    public Set<DataObject> instantiate() throws IOException {
        TemplateWizard wiz = templateWizard;
        DataFolder targetFolder = wiz.getTargetFolder();
        DataObject template = wiz.getTemplate();

        String filename = wiz.getTargetName();

        DataObject result = template.createFromTemplate(targetFolder, filename, Collections.singletonMap(CreateFromTemplateHandler.FREE_FILE_EXTENSION, Boolean.TRUE));

        if (result != null) {
            fireWizardEvent(new EventObject(result));
            OpenCookie open = result.getLookup().lookup(OpenCookie.class);
            if (open != null) {
                open.open();
            }
        }

        return Collections.<DataObject>singleton(result);
    }
    private final /*transient*/ Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // Set<ChangeListener>

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;

        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            (it.next()).stateChanged(ev);
        }
    }

    @Override
    public String name() {
        return ""; // NOI18N ?????
    }

    /* ------------------------------------------*/
    protected static void fireWizardEvent(EventObject e) {
        List<SrcFileWizardListener> listeners;
        synchronized (listenerList) {
            listeners = new ArrayList<SrcFileWizardListener>(listenerList);
        }

        for (int i = listeners.size() - 1; i >= 0; i--) {
            (listeners.get(i)).srcFileCreated(e);
        }
    }

    public static void addSrcFileWizardListener(SrcFileWizardListener l) {
        synchronized (listenerList) {
            listenerList.add(l);
        }
    }

    public static void removeSrcFileWizardListener(SrcFileWizardListener l) {
        synchronized (listenerList) {
            listenerList.remove(l);
        }
    }
}
