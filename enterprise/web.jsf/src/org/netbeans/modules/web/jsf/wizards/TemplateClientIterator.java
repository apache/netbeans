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

package org.netbeans.modules.web.jsf.wizards;

import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.jsf.JSFFrameworkProvider;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.JsfVersionUtils;
import org.netbeans.modules.web.jsf.palette.JSFPaletteUtilities;
import org.netbeans.modules.web.jsf.wizards.TemplateClientPanel.TemplateEntry;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.netbeans.modules.web.jsfapi.api.NamespaceUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.MapFormat;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */

public class TemplateClientIterator implements TemplateWizard.Iterator {

    private static final long serialVersionUID = 1L;
    
    private int index;
    private transient WizardDescriptor.Panel[] panels;
    
    private TemplateClientPanel templateClientPanel;
    private static final String ENCODING = "UTF-8"; //NOI18N
    
    private static String END_LINE = System.getProperty("line.separator"); //NOI18N
    
    /** Creates a new instance of TemplateClientIterator */
    public TemplateClientIterator() {
    }
    
    @Override
    public Set instantiate(final TemplateWizard wiz) throws IOException {
        final org.openide.filesystems.FileObject dir = Templates.getTargetFolder( wiz );
        final String targetName =  Templates.getTargetName(wiz);
        final DataFolder df = DataFolder.findFolder( dir );
        
        df.getPrimaryFile().getFileSystem().runAtomicAction(new FileSystem.AtomicAction(){

            @Override
            public void run() throws IOException {
                InputStream is = templateClientPanel.getTemplateClient();
                String content = JSFFrameworkProvider.readResource(is, ENCODING);
                FileObject target = df.getPrimaryFile().createData(targetName, "xhtml");
                TemplateEntry templateEntry = templateClientPanel.getTemplate();
                String relativePath = getTemplatePath(target, templateEntry);
                String definedTags = createDefineTags(templateClientPanel.getTemplateDataToGenerate(),
                        ((content.indexOf("<html") == -1)?1:3));    //NOI18N

                Project project = Templates.getProject(wiz);
                final JsfVersion jsfVersion = JsfVersionUtils.forProject(project);
                String namespaceLocation = (jsfVersion != null && jsfVersion.isAtLeast(JsfVersion.JSF_2_2))
                        ? NamespaceUtils.JCP_ORG_LOCATION : NamespaceUtils.SUN_COM_LOCATION;
                HashMap args = new HashMap();
                args.put("TEMPLATE", relativePath); //NOI18N
                args.put("DEFINE_TAGS", definedTags);   //NOI18N
                args.put("NS_LOCATION", namespaceLocation);   //NOI18N
                
                MapFormat formater = new MapFormat(args);
                formater.setLeftBrace("__");    //NOI18N
                formater.setRightBrace("__");   //NOI18N
                formater.setExactMatch(false);
                
                content = formater.format(content);
                
                JSFFrameworkProvider.createFile(target, content, ENCODING);
            }
            
        });

        FileObject target = df.getPrimaryFile().getFileObject(targetName, "xhtml");
        DataObject dob = DataObject.find(target);
        if (dob != null) {
            JSFPaletteUtilities.reformat(dob);
        }
        return Collections.singleton(dob);
    }

    private static String getTemplatePath(FileObject target, TemplateEntry templateEntry) {
        if (!templateEntry.isResourceLibraryContract()) {
            return JSFUtils.getRelativePath(target, templateEntry.getTemplate());
        } else {
            String fullpath = templateEntry.getTemplate().getPath();
            return TemplateClientPanelVisual.getRelativePathInsideResourceLibrary(fullpath);
        }
    }

    @Override
    public void initialize(TemplateWizard wiz) {
        index = 0;
        Project project = Templates.getProject( wiz );
        panels = createPanels(project, wiz);
        
        // Creating steps.
        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps(beforeSteps, panels);
        
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
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }

    @Override
    public void uninitialize(TemplateWizard wiz) {
        panels = null;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    @Override
    public String name() {
        return NbBundle.getMessage(TemplateIterator.class, "TITLE_x_of_y", index + 1, panels.length);
    }

    @Override
    public void previousPanel() {
        if (! hasPrevious()) throw new NoSuchElementException();
        index--;
    }

    @Override
    public void nextPanel() {
        if (! hasNext()) throw new NoSuchElementException();
        index++;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
    
    protected WizardDescriptor.Panel[] createPanels(Project project, TemplateWizard wiz) {
        Sources sources = (Sources) ProjectUtils.getSources(project);
        SourceGroup[] docSourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
        SourceGroup[] sourceGroups;
        if (docSourceGroups.length == 0) {
            sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        } else {
            sourceGroups = docSourceGroups;
        }

        templateClientPanel = new TemplateClientPanel(wiz);
        // creates simple wizard panel with bottom panel
        return new WizardDescriptor.Panel[] {
            Templates.buildSimpleTargetChooser(project, sourceGroups).bottomPanel(templateClientPanel).create()
        };
    }
    
    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }
    
    private String createDefineTags(Collection<String> data, int indent) {
        StringBuffer sb = new StringBuffer();
        final String basicIndent = "    ";
        
        for (String temp : data) {
            sb.append(END_LINE);
            for (int i = 0; i < indent; i++)
                sb.append(basicIndent);
            sb.append("<ui:define name=\"").append(temp).append("\">"); //NOI18N
            sb.append(END_LINE);
            for (int i = 0; i < (indent + 1); i++)
                sb.append(basicIndent);
            sb.append(temp);
            sb.append(END_LINE);
            for (int i = 0; i < indent; i++)
                sb.append(basicIndent);
            sb.append("</ui:define>");  //NOI18N
            sb.append(END_LINE);
        }
        
        return sb.toString();
    }
}
