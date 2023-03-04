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

package org.netbeans.modules.web.freeform.ui;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.ant.freeform.spi.support.NewFreeformProjectSupport;
import org.netbeans.modules.java.freeform.spi.support.NewJavaFreeformProjectSupport;
import org.netbeans.modules.j2ee.common.FileSearchUtility;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author  Radko Najman
 */
public class WebLocationsWizardPanel implements WizardDescriptor.Panel {

    private WebLocationsPanel component;
    private WizardDescriptor wizardDescriptor;
    private File baseFolder;

    public WebLocationsWizardPanel() {
        getComponent().setName(NbBundle.getMessage(NewWebFreeformProjectWizardIterator.class, "TXT_NewWebFreeformProjectWizardIterator_WebSources")); // NOI18N
    }

    public Component getComponent() {
        if (component == null) {
            component = new WebLocationsPanel(wizardDescriptor);
        }
        return component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx( WebLocationsWizardPanel.class );
    }

    public boolean isValid() {
        getComponent();
        return true;
    }

    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Set<ChangeListener> listenersCopy;
        synchronized (listeners) {
            listenersCopy = new HashSet<ChangeListener>(listeners);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : listenersCopy) {
            l.stateChanged(ev);
        }
    }

    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        wizardDescriptor.putProperty("NewProjectWizard_Title", component.getClientProperty("NewProjectWizard_Title")); // NOI18N

        //guess webapps well-known locations and preset them
        File baseFolder = (File)wizardDescriptor.getProperty(NewFreeformProjectSupport.PROP_PROJECT_LOCATION);
        File nbProjectFolder = (File)wizardDescriptor.getProperty(NewFreeformProjectSupport.PROP_PROJECT_FOLDER);
        final String webPages;
        final String webInf;
        final String srcPackages;
        if(baseFolder.equals(this.baseFolder)) {
            webPages = component.getWebPagesLocation().getAbsolutePath();
            webInf = component.getWebInfLocation().getAbsolutePath();
            srcPackages = component.getSrcPackagesLocation().getAbsolutePath();
        } else {
            this.baseFolder = baseFolder;
            FileObject fo = FileUtil.toFileObject(baseFolder);
            if (fo != null) {
                FileObject webPagesFO = FileSearchUtility.guessDocBase(fo);
                if (webPagesFO == null)
                    webPages = ""; //NOI18N
                else
                    webPages = FileUtil.toFile(webPagesFO).getAbsolutePath();
                
                FileObject webInfFO = FileSearchUtility.guessWebInf(fo);
                if (webInfFO == null)
                    webInf = ""; //NOI18N
                else
                    webInf = FileUtil.toFile(webInfFO).getAbsolutePath();
                
                srcPackages = guessJavaRoot(fo);
            } else {
                webPages = ""; // NOI18N
                webInf = ""; //NOI18N
                srcPackages = ""; // NOI18N
            }
        }
        component.setFolders(baseFolder, nbProjectFolder);
        component.setWebPages(webPages);
        component.setWebInf(webInf);
        component.setSrcPackages(srcPackages);
    }

    public void storeSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        wizardDescriptor.putProperty(NewWebFreeformProjectWizardIterator.PROP_WEB_WEBMODULES, component.getWebModules());
        
        List<String> l = component.getJavaSrcFolder();
        wizardDescriptor.putProperty(NewJavaFreeformProjectSupport.PROP_EXTRA_JAVA_SOURCE_FOLDERS, l);
        
        wizardDescriptor.putProperty(NewWebFreeformProjectWizardIterator.PROP_WEB_SOURCE_FOLDERS, component.getWebSrcFolder());
        wizardDescriptor.putProperty(NewWebFreeformProjectWizardIterator.PROP_WEB_INF_FOLDER, component.getWebInfFolder());
        wizardDescriptor.putProperty("NewProjectWizard_Title", null); // NOI18N
    }

    private String guessJavaRoot (FileObject dir) {
        Enumeration<? extends FileObject> ch = dir.getChildren (true);
        try {
            while (ch.hasMoreElements ()) {
                FileObject f = (FileObject) ch.nextElement ();
                if (f.getExt ().equals ("java")) { // NOI18N
                    String pckg = guessPackageName (f);
                    String pkgPath = f.getParent ().getPath ();
                    if (pckg != null && pkgPath.endsWith (pckg.replace ('.', '/'))) {
                        String rootName = pkgPath.substring (0, pkgPath.length () - pckg.length ());
                        return FileUtil.toFile(f.getFileSystem().findResource(rootName)).getAbsolutePath();
                    }
                }
            }
        } catch (FileStateInvalidException fsie) {
            Logger.getLogger("global").log(Level.INFO, null, fsie);
        }
        return ""; // NOI18N
    }

    private String guessPackageName(FileObject f) {
        java.io.Reader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(f.getInputStream (), "utf-8")); //NOI18N
            boolean noPackage = false;
            for (;;) {
                String line = ((BufferedReader) r).readLine();
                if (line == null) {
                    if (noPackage)
                        return "";
                    else
                        break;
                }
                line = line.trim();
                //try to find package
                if (line.trim().startsWith("package")) { // NOI18N
                    int idx = line.indexOf(";");  // NOI18N
                    if (idx >= 0)
                        return line.substring("package".length(), idx).trim(); // NOI18N
                }
                //an easy check if it is class
                if (line.indexOf("class") != -1)
                    noPackage = true;
            }
        } catch (java.io.IOException ioe) {
            Logger.getLogger("global").log(Level.INFO, null, ioe);
        } finally {
            try {
                if (r != null)
                    r.close ();
            } catch (java.io.IOException ioe) {
                // ignore this
            }
        }
        
        return null;
    }
}
