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
package org.netbeans.spi.java.project.support.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.FileChooser;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import static org.netbeans.spi.java.project.support.ui.Bundle.*;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbCollections;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 * Utility methods related to sharable libraries UI.
 * @since org.netbeans.modules.java.project 1.15
 * 
 */ 
public final class SharableLibrariesUtils {

    static final String PROP_LOCATION = "location"; //NOI18N
    static final String PROP_ACTIONS = "actions"; //NOI18N
    static final String PROP_HELPER = "helper"; //NOI18N
    static final String PROP_REFERENCE_HELPER = "refhelper"; //NOI18N
    static final String PROP_LIBRARIES = "libraries"; //NOI18N
    static final String PROP_JAR_REFS = "jars"; //NOI18N
    
    /**
     * The default filename for sharable library definition file.
     */
    public static final String DEFAULT_LIBRARIES_FILENAME = "nblibraries.properties"; //NOI18N
    
    private static String PROP_LAST_SHARABLE = "lastSharable"; //NOI18N
    
    /**
     * boolean value representing the state of library sharability of the last created project.
     * To be used by new project wizards.
     * @return true if last created project was created sharable, false if not.
     */
    public static boolean isLastProjectSharable() {
        return NbPreferences.forModule(SharableLibrariesUtils.class).getBoolean(PROP_LAST_SHARABLE,
                // For compatibility with incorrect old location:
                NbPreferences.root().node("org.netbeans.modules.java.project.share").getBoolean(PROP_LAST_SHARABLE, false)); // NOI18N
    }
    /**
     * Setter for boolean value representing the state of library sharability of the last created project.
     * To be used by new project wizards.
     * 
     * @param sharable
     */
    public static void setLastProjectSharable(boolean sharable) {
        NbPreferences.forModule(SharableLibrariesUtils.class).putBoolean(PROP_LAST_SHARABLE, sharable);
    }

    /**
     * File chooser implementation for browsing for shared library location.
     * @param current
     * @param comp
     * @param projectLocation
     * @return relative or absolute path to project libraries folder.
     */
    @Messages({
        "LBL_Browse_Libraries_Title=Select Libraries Folder",
        "ASCD_Browse_Libraries_Title=Browse for the Folder with Library Definitions."
    })
    public static String browseForLibraryLocation(String current, Component comp, File projectLocation) {
        File lib = PropertyUtils.resolveFile(projectLocation, current);
        if (!lib.exists()) {
            lib = lib.getParentFile();
        }
        lib = FileUtil.normalizeFile(lib);
        FileChooser chooser = new FileChooser(projectLocation, null);
        // for now variable based paths are disabled for reference to libraries folder
        // can be revisit if it is needed
        chooser.setCurrentDirectory(lib);
        chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        chooser.setDialogTitle(LBL_Browse_Libraries_Title());
        chooser.getAccessibleContext().setAccessibleDescription(ASCD_Browse_Libraries_Title());
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(comp)) {
            String[] files;
            try {
                files = chooser.getSelectedPaths();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
            if (files.length == 1) {
                String currentLibrariesLocation = files[0];
                return currentLibrariesLocation;
            }
        }
        return null;
    }    

    /**
     * Show a multistep wizard for converting a non-sharable project to a sharable, self-contained one.
     * @param helper
     * @param ref
     * @param libraryNames
     * @param jarReferences
     * @return true is migration was performed, false when aborted.
     */
    @Messages({
        "TIT_MakeSharableWizard=New Libraries Folder",
        "ACSD_MakeSharableWizard=Wizard dialog that guides you through the process of making the project self-contained and sharable in respect to binary dependencies."
    })
    public static boolean showMakeSharableWizard(final AntProjectHelper helper, ReferenceHelper ref, List<String> libraryNames, List<String> jarReferences) {

        final CopyIterator cpIt = new CopyIterator(helper);
        final WizardDescriptor wizardDescriptor = new WizardDescriptor(cpIt);
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(TIT_MakeSharableWizard());
        wizardDescriptor.putProperty(PROP_HELPER, helper);
        wizardDescriptor.putProperty(PROP_REFERENCE_HELPER, ref);
        wizardDescriptor.putProperty(PROP_LIBRARIES, libraryNames);
        wizardDescriptor.putProperty(PROP_JAR_REFS, jarReferences);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(ACSD_MakeSharableWizard());
        dialog.setVisible(true);
        dialog.toFront();
        return wizardDescriptor.getValue() == WizardDescriptor.FINISH_OPTION && cpIt.isSuccess();
    }

    @Messages({
        "LBL_ExecuteProblems=Import Problems:"
    })
    private static void execute(final WizardDescriptor wizardDescriptor, final AntProjectHelper helper, final ProgressHandle handle) throws IOException {
        final String loc = (String) wizardDescriptor.getProperty(PROP_LOCATION);
        final List<Action> actions = NbCollections.checkedListByCopy((List) wizardDescriptor.getProperty(PROP_ACTIONS), Action.class, true);
        assert loc != null;
        handle.start(Math.max(1, actions.size() + 1));
        try {
            // create libraries property file if it does not exist:
            File f = new File(loc);
            if (!f.isAbsolute()) {
                f = new File(FileUtil.toFile(helper.getProjectDirectory()), loc);
            }
            f = FileUtil.normalizeFile(f);
            if (!f.exists()) {
                FileUtil.createData(f);
            }
            if (!f.isFile()) {
                throw new IllegalArgumentException("Library definition has to be file, got: " + f.getAbsolutePath());   //NOI18N
            }

            try {
                final Queue<String> errors = new ArrayDeque<>();
                ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                    public Void run() throws IOException {
                        try {
                            helper.getProjectDirectory().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                                public void run() throws IOException {
                                    helper.setLibrariesLocation(loc);
                                    int count = 1;
                                    // TODO or make just runnables?
                                    for (Action act : actions) {
                                        handle.progress(count);
                                        count = count + 1;
                                        act.actionPerformed(null);
                                        if (act instanceof ErrorProvider) {
                                            try {
                                                final String err = ((ErrorProvider)act).getError();
                                                if (err != null) {
                                                    errors.offer(err);
                                                }
                                            } catch (Exception e) {
                                                Exceptions.printStackTrace(e);
                                            }
                                        }
                                    }
                                    ProjectManager.getDefault().saveProject(FileOwnerQuery.getOwner(helper.getProjectDirectory()));
                                }
                            });
                        } catch (IllegalArgumentException ex) {
                            throw new IOException(ex);
                        }
                        return null;
                    }
                });
                if (!errors.isEmpty()) {
                   final Object content;
                   if (errors.size() == 1) {
                       content = errors.iterator().next();
                   } else {
                       final JPanel panel = new JPanel();
                       panel.setLayout(new GridBagLayout());
                       final JLabel label = new JLabel(Bundle.LBL_ExecuteProblems());
                       GridBagConstraints c = new GridBagConstraints();
                       c.gridx = 0;
                       c.gridy = 0;
                       c.gridwidth = GridBagConstraints.REMAINDER;
                       c.gridheight = 1;
                       c.fill = GridBagConstraints.HORIZONTAL;
                       c.weightx = 1.0;
                       c.weighty = 0.0;
                       c.anchor = GridBagConstraints.WEST;
                       c.insets = new Insets(6,6,6,6);
                       panel.add(label,c);
                       final JList list = new JList(errors.toArray());
                       c = new GridBagConstraints();
                       c.gridx = 0;
                       c.gridy = 1;
                       c.gridwidth = GridBagConstraints.REMAINDER;
                       c.gridheight = 1;
                       c.fill = GridBagConstraints.HORIZONTAL;
                       c.weightx = 1.0;
                       c.weighty = 1.0;
                       c.anchor = GridBagConstraints.WEST;
                       c.insets = new Insets(0,6,6,6);
                       panel.add(new JScrollPane(list),c);
                       content = panel;
                   }
                   DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                       content,
                       NotifyDescriptor.WARNING_MESSAGE));
                }
            } catch (MutexException ex) {
                throw (IOException) ex.getException();
            }
        } finally {
            handle.finish();
        }
    }
    
    private static class CopyIterator extends WizardDescriptor.ArrayIterator<WizardDescriptor> implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {
        private AntProjectHelper helper;
        private WizardDescriptor desc;
        private volatile boolean success;

        private CopyIterator(AntProjectHelper helper) {
            super(getPanels());
            this.helper = helper;
            
        }

        public Set instantiate(ProgressHandle handle) throws IOException {
            execute(desc, helper, handle);
            success = true;
            return Collections.EMPTY_SET;
        }

        public Set instantiate() throws IOException {
            throw new UnsupportedOperationException("Not supported");
        }

        public void initialize(WizardDescriptor wizard) {
            this.desc = wizard;
        }

        public void uninitialize(WizardDescriptor wizard) {
            this.desc = wizard;
        }

        boolean isSuccess() {
            return success;
        }
        
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private static List<WizardDescriptor.Panel<WizardDescriptor>> getPanels() {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new MakeSharableWizardPanel1());
        panels.add(new MakeSharableWizardPanel2());
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel. Mainly useful
            // for getting the name of the target chooser to appear in the
            // list of steps.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Sets step number of a component
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Sets steps names for a panel
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                // Turn on subtitle creation on each step
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                // Show steps on the left side with the image on the background
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
                // Turn on numbering of all steps
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
            }
        }
        return panels;
    }

    static interface ErrorProvider {
        @CheckForNull
        String getError();
    }

    static class CopyJars extends AbstractAction {
        private ReferenceHelper refhelper;
        private AntProjectHelper ahelper;
        private String reference;

        public CopyJars(ReferenceHelper helper, AntProjectHelper anthelper, String ref) {
            this.reference = ref;
            this.ahelper = anthelper;
            refhelper = helper;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                String value = ahelper.getStandardPropertyEvaluator().evaluate(reference);
                File absFile = ahelper.resolveFile(value);
                String location = ahelper.getLibrariesLocation();
                File libraryFile = ahelper.resolveFile(location);
                File directory = libraryFile.getParentFile();
                final FileObject dir = FileUtil.createFolder(directory);
                if (!absFile.exists()) {
                    //#131535 is a broken reference probably, ignore.
                    return;
                }
                updateReference(absFile, reference, true, dir);
                //now process source reference
                String source = reference.replace("${file.reference", "${source.reference"); //NOI18N
                value = ahelper.getStandardPropertyEvaluator().evaluate(source);
                if (!value.startsWith("${source.")) { //NOI18N
                    absFile = ahelper.resolveFile(value);
                    updateReference(absFile, source.replace("${", "").replace("}", ""), false, dir);
                }
                //now process javadoc reference
                String javadoc = reference.replace("${file.reference", "${javadoc.reference"); //NOI18N
                value = ahelper.getStandardPropertyEvaluator().evaluate(javadoc);
                if (!value.startsWith("${javadoc.")) { //NOI18N
                    absFile = ahelper.resolveFile(value);
                    updateReference(absFile, javadoc.replace("${", "").replace("}", ""), false, dir);
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        @Messages("TXT_AlreadyExists=<html>The file {0} already exists in the library folder.<br>Using the existing file.")
        private void updateReference(File oldFile, String key, boolean main, FileObject dir) {
            final FileObject src = FileUtil.toFileObject(oldFile);
            if (src == null) {
                assert !oldFile.exists() : "The file: " + oldFile.getAbsolutePath() + " exists but FileObject cannot be found.";    //NOI18N
                return;
            }
            FileObject newFile;
            try {
                newFile = dir.getFileObject(src.getNameExt());
                if (newFile != null) {
                    final Runnable action = new Runnable() {
                        @Override
                        public void run() {                            
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                Bundle.TXT_AlreadyExists(src.getNameExt()),
                                NotifyDescriptor.WARNING_MESSAGE));
                                
                        }
                    };
                    if (SwingUtilities.isEventDispatchThread()) {
                        action.run();
                    } else {
                        SwingUtilities.invokeLater(action);
                    }
                } else {
                    newFile = FileUtil.copyFile(src, dir, src.getName());
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                newFile = src;
            }
            File absFile = FileUtil.toFile(newFile);
        //always relative when possible.
        // assume we have relative path to library as well, thus relative path is
        //good default value. 
            String newVal = PropertyUtils.relativizeFile(FileUtil.toFile(ahelper.getProjectDirectory()), absFile);
            if (newVal == null) {
                //fallback
                newVal = absFile.getAbsolutePath();
            }
            if (main) {
                // mkleint: why isn't there a way to set the reference, but one always have to create it
                // creating will create a unique key..
                refhelper.destroyReference(key);
                refhelper.createForeignFileReferenceAsIs(newVal, key);
            } else {
                refhelper.destroyReference(key);
                refhelper.createExtraForeignFileReferenceAsIs(newVal, key);
            }
        }
        
    }

    static class KeepJarAtLocation extends AbstractAction {
        private ReferenceHelper refhelper;
        private AntProjectHelper ahelper;
        private String reference;
        private boolean relative;

        public KeepJarAtLocation(String ref, boolean b, AntProjectHelper anthelper, ReferenceHelper helper) {
            this.reference = ref;
            this.ahelper = anthelper;
            refhelper = helper;
            relative = b;
        }

        public void actionPerformed(ActionEvent e) {
            String value = ahelper.getStandardPropertyEvaluator().evaluate(reference);
            updateReference(value, reference, true);
            //now process source reference
            String source = reference.replace("${file.reference", "${source.reference"); //NOI18N
            value = ahelper.getStandardPropertyEvaluator().evaluate(source);
            if (!value.startsWith("${source.")) { //NOI18N
                updateReference(value, source.replace("${", "").replace("}", ""), false);
            }
            //now process javadoc reference
            String javadoc = reference.replace("${file.reference", "${javadoc.reference"); //NOI18N
            value = ahelper.getStandardPropertyEvaluator().evaluate(javadoc);
            if (!value.startsWith("${javadoc.")) { //NOI18N
                updateReference(value, javadoc.replace("${", "").replace("}", ""), false);
            }
        }

        private void updateReference(String value, String key, boolean main) {
            File absFile = ahelper.resolveFile(value);
            String newVal = relative ? PropertyUtils.relativizeFile(FileUtil.toFile(ahelper.getProjectDirectory()), absFile) : absFile.getAbsolutePath();
            if (newVal == null) {
                //fallback
                newVal = absFile.getAbsolutePath();
            }
            if (!newVal.equals(value)) {
                if (main) {
                    refhelper.createForeignFileReferenceAsIs(newVal, key);
                } else {
                    refhelper.createExtraForeignFileReferenceAsIs(newVal, key);
                }
            }
        }
    }
    
    static class KeepLibraryAtLocation extends AbstractAction implements ErrorProvider {
        private boolean keepRelativeLocations;
        private Library library;
        private AntProjectHelper helper;
        private String errMsg;

        KeepLibraryAtLocation(Library l , boolean relative, AntProjectHelper h) {
            library = l;
            keepRelativeLocations = relative;
            helper = h;
        }
        @Messages("ERR_LibraryExists=The library {0} already exists, reusing the old definition.")
        public void actionPerformed(ActionEvent e) {
            String loc = helper.getLibrariesLocation();
            assert loc != null;
            File mainPropertiesFile = helper.resolveFile(loc);
            try {
                LibraryManager man = LibraryManager.forLocation(Utilities.toURI(mainPropertiesFile).toURL());
                Map<String, List<URI>> volumes = new HashMap<String, List<URI>>();
                LibraryTypeProvider provider = LibrariesSupport.getLibraryTypeProvider(library.getType());
                assert provider != null;
                for (String volume : provider.getSupportedVolumeTypes()) {
                    List<URL> urls = library.getContent(volume);
                    List<URI> newurls = new ArrayList<URI>();
                    for (URL url : urls) {
                        String jarFolder = null;
                        if ("jar".equals(url.getProtocol())) { // NOI18N
                            jarFolder = getJarFolder(URI.create(url.toExternalForm()));
                            url = FileUtil.getArchiveFile(url);
                        }
                        FileObject fo = URLMapper.findFileObject(url);

                        URI uri;
                        if (fo != null) {
                            if (keepRelativeLocations) {
                                File path = FileUtil.toFile(fo);
                                String str = PropertyUtils.relativizeFile(mainPropertiesFile.getParentFile(), path);
                                if (str == null) {
                                    // the relative path cannot be established, different drives?
                                    uri = Utilities.toURI(path);
                                } else {
                                    uri = LibrariesSupport.convertFilePathToURI(str);
                                }
                            } else {
                                File path = FileUtil.toFile(fo);
                                uri = Utilities.toURI(path);
                            }
                            if (FileUtil.isArchiveFile(fo)) {
                                uri = appendJarFolder(uri, jarFolder);
                            }
                            newurls.add(uri);
                        } else {
                            try {
                                newurls.add(url.toURI());
                            } catch (URISyntaxException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                    volumes.put(volume, newurls);
                }
                if (man.getLibrary(library.getName())!=null) {
                  errMsg = ERR_LibraryExists(library.getDisplayName());
                } else {
                    final String name = library.getName();
                    String displayName = library.getDisplayName();
                    if (name.equals(displayName)) {
                        //No need to set displayName when it's same as name
                        displayName = null;
                    }
                    man.createURILibrary(
                            library.getType(),
                            name,
                            displayName,
                            library.getDescription(),
                            volumes);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @CheckForNull
        @Override
        public String getError() {
            return errMsg;
        }
        
    }
    
    static class CopyLibraryJars extends AbstractAction {
        private Library library;
        private ReferenceHelper refHelper;
 
        CopyLibraryJars(ReferenceHelper h, Library l) {
            refHelper = h;
            library = l;
        }

        public void actionPerformed(ActionEvent e) {
            assert library.getManager() == LibraryManager.getDefault() : "Only converting from non-sharable to sharable is supported."; //NOi18N
            try {
                refHelper.copyLibrary(library);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
    }
    

    
    
    private static String getJarFolder(URI uri) {
        String u = uri.toString();
        int index = u.indexOf("!/"); //NOI18N
        if (index != -1 && index + 2 < u.length()) {
            return u.substring(index+2);
        }
        return null;
    }
    
    /** append path to given jar root uri */
    private static URI appendJarFolder(URI u, String jarFolder) {
        try {
            if (u.isAbsolute()) {
                return new URI("jar:" + u.toString() + "!/" + (jarFolder == null ? "" : jarFolder.replace('\\', '/'))); // NOI18N
            } else {
                return new URI(u.toString() + "!/" + (jarFolder == null ? "" : jarFolder.replace('\\', '/'))); // NOI18N
            }
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }
}
