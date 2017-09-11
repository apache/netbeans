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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.project.ui;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle.Messages;


/** Iterator useful for NewFileWizard. Implements WD.InstantiatingIterator,
 * rest of methods delegates to WD.ArrayIterator created only with SimpleTargetChooserPanel.
 *
 * @author  Jiri Rechtacek
 *          Winston Prakash - Added optional Page Layout Chooser Panel
 */
public class NewFileIterator implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> {
    
    private static final long serialVersionUID = 1L;
    
    private transient WizardDescriptor.Iterator<WizardDescriptor> simpleIterator;
    private transient WizardDescriptor.Panel<WizardDescriptor> panel;
    private transient WizardDescriptor wiz;
    private transient Project currentProject;
    
    private final transient boolean isFolder;
    
    
    /** Create a new wizard iterator. */
    private NewFileIterator (boolean isFolderIterator) {
        isFolder = isFolderIterator;
    }
    
    public static NewFileIterator genericFileIterator () {
        return new NewFileIterator (false);
    }
    
    /** see issue #214254
     * @TemplateRegistration(
        folder="Other",
        position=2100,
        displayName="#folderIterator",
        iconBase="org/openide/loaders/defaultFolder.gif",
        description="templatesFolder.html",
        category="simple-files"
    )*/
    @Messages("folderIterator=Folder")
    public static NewFileIterator folderIterator () {
        return new NewFileIterator (true);
    }
    
    private WizardDescriptor.Iterator<WizardDescriptor> getSimpleIterator () {
        if (simpleIterator == null) {
            assert panel != null;
            simpleIterator = new WizardDescriptor.ArrayIterator<WizardDescriptor>(Collections.singletonList(panel));
        }
        return simpleIterator;
    }
            
    private WizardDescriptor.Panel<WizardDescriptor> getPanel (WizardDescriptor wizardDescriptor) {
        Project project = Templates.getProject( wizardDescriptor );
        //
        if (project == null) {
            FileObject folder = Templates.getTargetFolder(wizardDescriptor);
            if (folder == null) {
                //new file.. toolbar when no project opened. 
                //just come up with a random base folder and let users choose the right folder laters
                String home = System.getProperty("user.home");
                if (home != null && new File(home).isDirectory()) {
                    folder =  FileUtil.toFileObject(FileUtil.normalizeFile(new File(home)));
                }
                if (folder == null) {
                    folder = FileUtil.toFileObject(new File(""));
                }
            }
            final FileObject ffolder = folder;
            SourceGroup sg = new SourceGroup() {

                @Override
                public FileObject getRootFolder() {
                    return ffolder;
                }

                @Override
                public String getName() {
                    return "name";
                }

                @Override
                public String getDisplayName() {
                    return "dname";
                }

                @Override
                public Icon getIcon(boolean opened) {
                    return null;
                }

                @Override
                public boolean contains(FileObject file) {
                    return file.equals(ffolder) || FileUtil.isParentOf(ffolder, file);
                }

                @Override
                public void addPropertyChangeListener(PropertyChangeListener listener) {
                }

                @Override
                public void removePropertyChangeListener(PropertyChangeListener listener) {
                }
            };
            if (isFolder) {
                panel = new SimpleTargetChooserPanel(project, new SourceGroup[] {sg}, null, true, false);
            } else {
                panel = Templates.buildSimpleTargetChooser(project, new SourceGroup[] {sg}).create();
            }
            return panel;
                    
        }
        if (!project.equals (currentProject) || panel == null) {
            Sources sources = ProjectUtils.getSources(project);
            if (isFolder) {
                panel = new SimpleTargetChooserPanel(project, sources.getSourceGroups(Sources.TYPE_GENERIC), null, true, false);
            } else {
                panel = Templates.buildSimpleTargetChooser(project, sources.getSourceGroups(Sources.TYPE_GENERIC)).create();
            }
        }
        return panel;
    }
    
    private String[] createSteps (String[] before) {
        assert panel != null;
        
        if (before == null) {
            before = new String[0];
        }
        
        String[] res = new String[before.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - 1)) {
                res[i] = before[i];
            } else {
                res[i] = panel.getComponent().getName();
            }
        }
        return res;
    }
    
    @Override
    public Set/*<FileObject>*/ instantiate () throws IOException {
        FileObject dir = Templates.getTargetFolder( wiz );
        
        DataFolder df = DataFolder.findFolder( dir );
        FileObject template = isFolder ? FileUtil.createMemoryFileSystem().getRoot() : Templates.getTemplate( wiz );
        
        DataObject dTemplate = DataObject.find( template );                
        DataObject dobj = dTemplate.createFromTemplate( df, Templates.getTargetName( wiz )  );
        
        return Collections.singleton (dobj.getPrimaryFile ());
    }
    
    @Override
    public void initialize(WizardDescriptor wiz) {
        panel = getPanel(wiz);
        this.wiz = wiz;
        
        // Make sure list of steps is accurate.
        String[] beforeSteps = null;
        Object prop = wiz.getProperty (WizardDescriptor.PROP_CONTENT_DATA);
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps (beforeSteps);
        for (int i = 0; i < 1; i++) { // XXX what was this loop for, exactly? panels.length was always 1
            Component c = panel.getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }

    @Override
    public void uninitialize (WizardDescriptor wiz) {
        this.simpleIterator = null;
        this.wiz = null;
        panel = null;
    }
    
    @Override
    public String name() {
        return getSimpleIterator ().name ();
    }
    
    @Override
    public boolean hasNext() {
        return getSimpleIterator ().hasNext ();
    }
    @Override
    public boolean hasPrevious() {
        return getSimpleIterator ().hasPrevious ();
    }
    @Override
    public void nextPanel() {
        getSimpleIterator ().nextPanel ();
    }
    @Override
    public void previousPanel() {
        getSimpleIterator ().previousPanel ();
    }
    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return getSimpleIterator ().current ();
    }
    @Override
    public final void addChangeListener(ChangeListener l) {
        getSimpleIterator ().addChangeListener (l);
    }
    @Override
    public final void removeChangeListener(ChangeListener l) {
        getSimpleIterator ().removeChangeListener (l);
    }
}
