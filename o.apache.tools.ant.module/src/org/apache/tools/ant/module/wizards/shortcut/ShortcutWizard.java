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

package org.apache.tools.ant.module.wizards.shortcut;

import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataShadow;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * The shortcut wizard itself.
 * @author Jesse Glick
 */
public final class ShortcutWizard extends WizardDescriptor {

    private static final Logger LOG = Logger.getLogger(ShortcutWizard.class.getName());
    
    /**
     * Show the shortcut wizard for a given Ant target.
     * @param project the Ant script to make a target to
     * @param target the particular target in it
     */
    public static void show(AntProjectCookie project, Element target) {
        final ShortcutWizard wiz = new ShortcutWizard(project, target, new ShortcutIterator());
        DialogDisplayer.getDefault().createDialog(wiz).setVisible(true);
        if (wiz.getValue().equals(WizardDescriptor.FINISH_OPTION)) {
            try {
                wiz.finish();
            } catch (IOException ioe) {
                AntModule.err.notify(ioe);
            }
        }
    }

    public static void remove(AntProjectCookie project, Element element) { // #151632
        FileObject build = FileUtil.getConfigFile("Actions/Build"); // NOI18N
        if (build != null) {
            File file = project.getFile();
            if (file != null) {
                for (FileObject kid : build.getChildren()) {
                    if (isAntScript(kid)) {
                        try {
                            Document doc = XMLUtil.parse(new InputSource(kid.getURL().toString()), false, false, /*XXX*/ null, null);
                            NodeList nl = doc.getElementsByTagName("ant"); // NOI18N
                            if (nl.getLength() == 1) {
                                Element ael = (Element) nl.item(0);
                                if (ael.getAttribute("antfile").equals(file.getAbsolutePath()) && // NOI18N
                                        ael.getAttribute("target").equals(element.getAttribute("name"))) { // NOI18N
                                    doRemove(kid);
                                    return;
                                }
                            }
                        } catch (java.lang.Exception x) {
                            LOG.log(Level.INFO, "Failed to parse or remove " + kid, x);
                        }
                    }
                }
                String message = NbBundle.getMessage(ShortcutWizard.class, "MSG_delete_all_shortcuts");
                String title = NbBundle.getMessage(ShortcutWizard.class, "TITLE_delete_all_shortcuts");
                if (DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Confirmation(message, title, OK_CANCEL_OPTION)).equals(NotifyDescriptor.OK_OPTION)) {
                    for (FileObject kid : build.getChildren()) {
                        if (isAntScript(kid)) {
                            try {
                                doRemove(kid);
                            } catch (IOException x) {
                                LOG.log(Level.INFO, "Failed to remove " + kid, x);
                            }
                        }
                    }
                }
                return;
            }
        }
        Toolkit.getDefaultToolkit().beep(); // not a disk file, or no Build category
    }
    private static boolean isAntScript(FileObject f) {
        try {
            return DataObject.find(f).getLookup().lookup(AntProjectCookie.class) != null;
        } catch (DataObjectNotFoundException x) {
            return f.hasExt("xml"); // NOI18N
        }
    }
    private static void doRemove(FileObject shortcut) throws IOException {
        for (String place : new String[] {"Menu", "Toolbars", "Shortcuts", "Keymaps"}) { // NOI18N
            FileObject top = FileUtil.getConfigFile(place);
            if (top != null) {
                for (FileObject f : NbCollections.iterable(top.getChildren(true))) {
                    DataObject d;
                    try {
                        d = DataObject.find(f);
                    } catch (DataObjectNotFoundException x) {
                        LOG.log(Level.INFO, "Loading " + f, x);
                        continue;
                    }
                    if (d instanceof DataShadow && ((DataShadow) d).getOriginal().getPrimaryFile() == shortcut) {
                        delete(f);
                    }
                }
            }
        }
        delete(shortcut);
    }
    private static void delete(FileObject file) throws IOException { // cf. #162526
        if (file.canRevert()) {
            file.revert();
        } else {
            throw new IOException("Could not delete " + file);
        }
    }

    // Attributes stored on the template wizard:
    
    /** type String */
    private static final String PROP_CONTENTS = "wizdata.contents"; // NOI18N
    /** type String */
    static final String PROP_DISPLAY_NAME = "wizdata.displayName"; // NOI18N
    /** type Boolean */
    static final String PROP_SHOW_CUST = "wizdata.show.cust"; // NOI18N
    /** type Boolean */
    static final String PROP_SHOW_MENU = "wizdata.show.menu"; // NOI18N
    /** type Boolean */
    static final String PROP_SHOW_TOOL = "wizdata.show.tool"; // NOI18N
    /** type Boolean */
    static final String PROP_SHOW_KEYB = "wizdata.show.keyb"; // NOI18N
    /** type DataFolder */
    static final String PROP_FOLDER_MENU = "wizdata.folder.menu"; // NOI18N
    /** type DataFolder */
    static final String PROP_FOLDER_TOOL = "wizdata.folder.tool"; // NOI18N
    /** type KeyStroke */
    static final String PROP_STROKE = "wizdata.stroke"; // NOI18N

    private final AntProjectCookie project;
    private final Element target;
    private final ShortcutIterator it;

    ShortcutWizard(AntProjectCookie project, Element target, ShortcutIterator it) {
        this.project = project;
        this.target = target;
        this.it = it;
        it.initialize(this);
        setPanelsAndSettings(it, this);
        setTitle(NbBundle.getMessage(ShortcutWizard.class, "TITLE_wizard"));
        putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
        putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
        putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
        String desc = target.getAttribute("description"); // NOI18n
        putProperty(PROP_DISPLAY_NAME, desc);
        // XXX deal with toolbar short desc somehow: #39985
        // Need to have another field in toolbar panel, and also patch AntActionInstance
        // to respond to Action.SHORT_DESCRIPTION, presumably as the <description>.
    }
    
    /**
     * Get the current XML contents of the shortcut.
     */
    String getContents() {
        String c = (String)getProperty(PROP_CONTENTS);
        if (c == null) {
            c = generateContents();
            putContents(c);
        }
        return c;
    }
    
    /**
     * Put the XML contents.
     */
    void putContents(String c) {
        putProperty(PROP_CONTENTS, c);
    }
    
    /**
     * Create XML contents of the shortcut to be generated, based on current data.
     */
    private String generateContents() {
        try {
            Document doc = XMLUtil.createDocument("project", null, null, null); // NOI18N
            Element pel = doc.getDocumentElement();
            String displayName = (String)getProperty(PROP_DISPLAY_NAME);
            if (displayName != null && displayName.length() > 0) {
                pel.setAttribute("name", displayName); // NOI18N
            }
            pel.setAttribute("default", "run"); // NOI18N
            Element tel = doc.createElement("target"); // NOI18N
            tel.setAttribute("name", "run"); // NOI18N
            Element ael = doc.createElement("ant"); // NOI18N
            ael.setAttribute("antfile", project.getFile().getAbsolutePath()); // NOI18N
            // #34802: let the child project decide on the basedir:
            ael.setAttribute("inheritall", "false"); // NOI18N
            ael.setAttribute("target", target.getAttribute("name")); // NOI18N
            tel.appendChild(ael);
            pel.appendChild(tel);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
            XMLUtil.write(doc, baos, "UTF-8"); // NOI18N
            return baos.toString("UTF-8"); // NOI18N
        } catch (IOException e) {
            AntModule.err.notify(e);
            return ""; // NOI18N
        }
    }

    void finish() throws IOException {
        final FileObject actionsBuild = FileUtil.createFolder(FileUtil.getConfigRoot(), "Actions/Build"); // NOI18N
        actionsBuild.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                // First create Actions/Build/*.xml, so it appears in action pool:
                String fname = FileUtil.findFreeFileName(actionsBuild, getTargetBaseName(), "xml") + ".xml"; // NOI18N
                FileObject shortcut = actionsBuild.createData(fname); // NOI18N
                OutputStream os = shortcut.getOutputStream();
                try {
                    os.write(getContents().getBytes("UTF-8")); // NOI18N
                } finally {
                    os.close();
                }
                // Now create *.shadow links to it from appropriate places in GUI:
                DataObject shortcutFO = DataObject.find(shortcut);
                if (it.showing(PROP_SHOW_MENU)) {
                    makeShadow((DataFolder) getProperty(PROP_FOLDER_MENU), shortcutFO);
                }
                if (it.showing(PROP_SHOW_TOOL)) {
                    makeShadow((DataFolder) getProperty(PROP_FOLDER_TOOL), shortcutFO);
                }
                if (it.showing(PROP_SHOW_KEYB)) {
                    FileObject currentKeymapDir = FileUtil.getConfigFile("Shortcuts"); // NOI18N
                    String stroke = Utilities.keyToString((KeyStroke) getProperty(PROP_STROKE));
                    DataShadow.create(DataFolder.findFolder(currentKeymapDir), stroke, shortcutFO);
                }
            }
        });
    }
    private void makeShadow(DataFolder folder, DataObject shortcutFO) throws IOException {
        DataShadow shadow = DataShadow.create(folder, shortcutFO);
        assert shadow.getFolder() == folder;
        List<DataObject> children = new ArrayList<DataObject>(Arrays.asList(folder.getChildren()));
        if (children.remove(shadow)) {
            children.add(shadow);
            folder.setOrder(children.toArray(new DataObject[children.size()]));
        } else {
            LOG.warning("#175981: could not find " + shadow + " among " + children);
        }
    }
    
    String getTargetBaseName() {
        String projname = ""; // NOI18N
        Document doc = project.getDocument();
        if (doc != null) {
            projname = doc.getDocumentElement().getAttribute("name"); // NOI18N
        }
        return (projname + '-' + target.getAttribute("name")).replaceAll("[^a-zA-Z0-9_-]", "-"); // NOI18N
    }

}
