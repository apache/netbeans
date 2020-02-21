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
