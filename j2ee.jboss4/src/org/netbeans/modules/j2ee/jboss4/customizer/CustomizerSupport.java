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

package org.netbeans.modules.j2ee.jboss4.customizer;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Server customizer support class. Provides default implementations of some 
 * common server manager customizer panes.
 *
 * @author sherold
 *
 * @since 1.19
 */
public final class CustomizerSupport {
    
    private static final String CLASSPATH = J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH;
    private static final String SOURCES = J2eeLibraryTypeProvider.VOLUME_TYPE_SRC;
    private static final String JAVADOC = J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC;
    
    /** Do not allow to create instances of this class */
    private CustomizerSupport() {
    }
    
    /**
     * Creates non-editable customizer classes pane.
     *
     * @param model A model prepresenting the class path entries.
     *
     * @return A Component representing the classes pane.
     *
     * @throws NullPointerException If null model is passed in.
     */
    public static Component createClassesCustomizer(PathModel model) {
        if (model == null) {
            throw new NullPointerException();
        }
        return new PathView(model, CLASSPATH, null);
    }
    
    /**
     * Creates an editable customizer sources pane.
     *
     * @param model A model prepresenting the source path entries.
     * @param currentDir Add sources file chooser current directory. Passing in 
     *                   a null represents the user's default directory.
     *
     * @return A Component representing the sources pane.
     *
     * @throws NullPointerException If null model is passed in.
     */
    public static Component createSourcesCustomizer(PathModel model, File currentDir) {
        if (model == null) {
            throw new NullPointerException();
        }
        return new PathView(model, SOURCES, currentDir);
    }
    
    /**
     * Creates an editable customizer javadoc pane.
     *
     * @param model A model prepresenting the javadoc entries.
     * @param currentDir Add javadoc file chooser current directory. Passing in 
     *                   a null represents the user's default directory.
     *
     * @return A Component representing the javadoc pane.
     *
     * @throws NullPointerException If null model is passed in.
     */
    public static Component createJavadocCustomizer(PathModel model, File currentDir) {
        if (model == null) {
            throw new NullPointerException();
        }
        return new PathView(model, JAVADOC, currentDir);
    }
    
    /**
     * Creates an Ant-style path specification from the specified list of URLs.
     *
     * @param The list of URLs.
     *
     * @return An Ant-style path specification.
     */
    public static String buildPath(List<URL> path) {
        String PATH_SEPARATOR = System.getProperty("path.separator"); // NOI18N
        StringBuffer sb = new StringBuffer(path.size() * 16);
        for (Iterator<URL> i = path.iterator(); i.hasNext(); ) {
            sb.append(urlToString(i.next()));
            if (i.hasNext()) {
                sb.append(PATH_SEPARATOR);
            }
        }
        return sb.toString();
    }
    
    /**
     * Splits an Ant-style path specification into the list of URLs.  Tokenizes on 
     * <code>:</code> and <code>;</code>, paying attention to DOS-style components 
     * such as <samp>C:\FOO</samp>. Also removes any empty components.
     *
     * @param path An Ant-style path (elements arbitrary) using DOS or Unix separators
     *
     * @return A tokenization of the specified path into the list of URLs.
     */
    public static List<URL> tokenizePath(String path) {
            List<URL> l = new ArrayList();
            StringTokenizer tok = new StringTokenizer(path, ":;", true); // NOI18N
            char dosHack = '\0';
            char lastDelim = '\0';
            int delimCount = 0;
            while (tok.hasMoreTokens()) {
                String s = tok.nextToken();
                if (s.length() == 0) {
                    // Strip empty components.
                    continue;
                }
                if (s.length() == 1) {
                    char c = s.charAt(0);
                    if (c == ':' || c == ';') {
                        // Just a delimiter.
                        lastDelim = c;
                        delimCount++;
                        continue;
                    }
                }
                if (dosHack != '\0') {
                    // #50679 - "C:/something" is also accepted as DOS path
                    if (lastDelim == ':' && delimCount == 1 && (s.charAt(0) == '\\' || s.charAt(0) == '/')) {
                        // We had a single letter followed by ':' now followed by \something or /something
                        s = "" + dosHack + ':' + s;
                        // and use the new token with the drive prefix...
                    } else {
                        // Something else, leave alone.
                        URL u = FileUtil.urlForArchiveOrDir(new File(Character.toString(dosHack)));
                        if (u != null) {
                            l.add(u);
                        }
                        // and continue with this token too...
                    }
                    dosHack = '\0';
                }
                // Reset count of # of delimiters in a row.
                delimCount = 0;
                if (s.length() == 1) {
                    char c = s.charAt(0);
                    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                        // Probably a DOS drive letter. Leave it with the next component.
                        dosHack = c;
                        continue;
                    }
                }
                URL u = FileUtil.urlForArchiveOrDir(new File(s));
                if (u != null) {
                    l.add(u);
                }
            }
            if (dosHack != '\0') {
                //the dosHack was the last letter in the input string (not followed by the ':')
                //so obviously not a drive letter.
                //Fix for issue #57304
                URL u = FileUtil.urlForArchiveOrDir(new File(Character.toString(dosHack)));
                if (u != null) {
                    l.add(u);
                }
            }
            return l;
    }
    
    /** Return string representation of the specified URL. */
    private static String urlToString(URL url) {
        if ("jar".equals(url.getProtocol())) { // NOI18N
            URL fileURL = FileUtil.getArchiveFile(url);
            if (FileUtil.getArchiveRoot(fileURL).equals(url)) {
                // really the root
                url = fileURL;
            } else {
                // some subdir, just show it as is
                return url.toExternalForm();
            }
        }
        if ("file".equals(url.getProtocol())) { // NOI18N
            File f = new File(URI.create(url.toExternalForm()));
            return f.getAbsolutePath();
        }
        else {
            return url.toExternalForm();
        }
    }
    
    /**
     * Path list model, supports adding, removing and moving URL entries in the list.
     */
    public static final class PathModel extends AbstractListModel {

        private final List<URL> data;

        /**
         * Creates a new PathModel initialized with a list of URL entries.
         *
         * @param data The list of URL entries.
         *
         * @throws NullPointerException If null data attribute is passed in.
         */
        public PathModel(List<URL> data) {
            if (data == null) {
                throw new NullPointerException("The data attribute must not be null."); // NOI18N
            }
            this.data = data;
        }

        /**
         * Returns the number of URL entries in the list.
         *
         * return The number of URL entries in the list.
         */
        public int getSize() {
            return data.size();
        }

        /**
         * Returns the element at the specified position in this list.
         *
         * @param index The element position in the list.
         *
         * @return The element at the specified position in this list.
         */
        public Object getElementAt(int index) {
            URL url = data.get(index);
            if ("jar".equals(url.getProtocol())) { // NOI18N
                URL fileURL = FileUtil.getArchiveFile(url);
                if (FileUtil.getArchiveRoot(fileURL).equals(url)) {
                    // really the root
                    url = fileURL;
                } else {
                    // some subdir, just show it as is
                    return url.toExternalForm();
                }
            }
            if ("file".equals(url.getProtocol())) { // NOI18N
                File f = new File(URI.create(url.toExternalForm()));
                return f.getAbsolutePath();
            }
            else {
                return url.toExternalForm();
            }
        }

        /**
         * Removes the URL entries denotated with their respective indices from the list.
         */
        public void removePath(int[] indices) {
            for (int i = indices.length - 1; i >= 0; i--) {
                data.remove(indices[i]);
            }
            fireIntervalRemoved(this, indices[0], indices[indices.length - 1]);
        }

        /**
         * Moves the URL entries denotated with their respective indices up in the list.
         */
        public void moveUpPath(int[] indices) {
            for (int i = 0; i < indices.length; i++) {
                URL p2 = data.get(indices[i]);
                URL p1 = data.set(indices[i] - 1, p2);
                data.set(indices[i], p1);
            }
            fireContentsChanged(this, indices[0] - 1, indices[indices.length - 1]);
        }

        /**
         * Moves the URL entries denotated with their respective indices down in the list.
         */
        public void moveDownPath(int[] indices) {
            for (int i = indices.length - 1; i >= 0; i--) {
                URL p1 = data.get(indices[i]);
                URL p2 = data.set(indices[i] + 1, p1);
                data.set(indices[i], p2);
            }
            fireContentsChanged(this, indices[0], indices[indices.length - 1] + 1);
        }

        /**
         * Appends the URL representing the specified file to the end of the list.
         * 
         * @return true if the URL was appended, false otherwise.
         */
        public boolean addPath(File f) {
            try {
                URL url = f.toURI().toURL();
                return this.addPath(url);
            } catch (MalformedURLException mue) {
                return false;
            }
        }

        /**
         * Appends the specified URL to the end of the list.
         * 
         * @return true if the URL was appended, false otherwise.
         */
        public boolean addPath(URL url) {
            if (FileUtil.isArchiveFile(url)) {
                url = FileUtil.getArchiveRoot(url);
            } else if (!url.toExternalForm().endsWith("/")) { // NOI18N
                try {
                    url = new URL(url.toExternalForm() + "/"); // NOI18N
                } catch (MalformedURLException mue) {
                    Logger.getLogger("global").log(Level.INFO, null, mue);
                }
            }
            int oldSize = data.size();
            data.add(url);
            fireIntervalAdded(this, oldSize, oldSize);
            return true;
        }

        /**
         * Returns the list of URL entries.
         * @return The list of URL entries.
         */
        public List<URL> getData() {
            return data;
        }
    }
    
    // private helper classes -------------------------------------------------
    
    private static class PathView extends JPanel {
        
        private JList resources;
        private JButton addButton;
        private JButton addURLButton;
        private JButton removeButton;
        private JButton moveUpButton;
        private JButton moveDownButton;
        private File currentDir;
        private String type;

        public PathView (PathModel model, String type, File currentDir) {
            this.type = type;
            this.currentDir = currentDir;
            initComponents(model);
        }

        private void initComponents(PathModel model) {
            setLayout(new GridBagLayout());
            JLabel label = new JLabel ();
            String key = null;
            String mneKey = null;
            String ad = null;
            if (type.equals(CLASSPATH)) {
                key = "TXT_Classes";       // NOI18N
                mneKey = "MNE_Classes";    // NOI18N
                ad = "AD_Classes";       // NOI18N                
            } else if (type.equals(SOURCES)) {
                key = "TXT_Sources";        // NOI18N
                mneKey = "MNE_Sources";     // NOI18N
                ad = "AD_Sources";          // NOI18N
            } else if (type.equals(JAVADOC)) {
                key = "TXT_Javadoc";        // NOI18N
                mneKey = "MNE_Javadoc";     // NOI18N
                ad = "AD_Javadoc";          // NOI18N                
            } else {
                assert false : "Illegal type of panel"; //NOI18N
                return;
            }
            label.setText(NbBundle.getMessage(CustomizerSupport.class,key));
            label.setDisplayedMnemonic(NbBundle.getMessage(CustomizerSupport.class,mneKey).charAt(0));
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets = new Insets (6,12,2,0);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            ((GridBagLayout)getLayout()).setConstraints(label,c);
            add(label);
            resources = new JList(model);
            label.setLabelFor(resources);
            resources.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSupport.class,ad));
            resources.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    selectionChanged ();
                }
            });
            JScrollPane spane = new JScrollPane (this.resources);            
            // set the preferred size so that the size won't be set according to
            // the longest row in the list by default
            spane.setPreferredSize(new java.awt.Dimension(200, 100));
            c = new GridBagConstraints();
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = 1;
            c.gridheight = 5;
            c.insets = new Insets (0,12,12,6);
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 1.0;
            ((GridBagLayout)this.getLayout()).setConstraints(spane,c);
            add(spane);
            if (type == SOURCES || type == JAVADOC) {
                this.addButton = new JButton ();
                String text;
                char mne;
                if (type == SOURCES) {
                    text = NbBundle.getMessage(CustomizerSupport.class, "CTL_Add");
                    mne = NbBundle.getMessage(CustomizerSupport.class, "MNE_Add").charAt(0);
                    ad = NbBundle.getMessage(CustomizerSupport.class, "AD_Add");
                }
                else {
                    text = NbBundle.getMessage(CustomizerSupport.class, "CTL_AddZip");
                    mne = NbBundle.getMessage(CustomizerSupport.class, "MNE_AddZip").charAt(0);
                    ad = NbBundle.getMessage(CustomizerSupport.class, "AD_AddZip");
                }
                this.addButton.setText(text);
                this.addButton.setMnemonic(mne);
                this.addButton.getAccessibleContext().setAccessibleDescription (ad);
                addButton.addActionListener( new ActionListener () {
                    public void actionPerformed(ActionEvent e) {
                        addPathElement ();
                    }
                });
                c = new GridBagConstraints();
                c.gridx = 1;
                c.gridy = 1;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.NORTHWEST;
                c.insets = new Insets (0,6,0,6);
                ((GridBagLayout)this.getLayout()).setConstraints(addButton,c);
                this.add (addButton);
//                if (this.type == JAVADOC) {
//                    addURLButton  = new JButton (NbBundle.getMessage(CustomizerSupport.class, "CTL_AddURL"));
//                    addURLButton.setMnemonic(NbBundle.getMessage(CustomizerSupport.class, "MNE_AddURL").charAt(0));
//                    addURLButton.addActionListener(new ActionListener () {
//                        public void actionPerformed(ActionEvent e) {
//                            addURLElement ();
//                        }
//                    });
//                    c = new GridBagConstraints();
//                    c.gridx = 1;
//                    c.gridy = 2;
//                    c.gridwidth = GridBagConstraints.REMAINDER;
//                    c.fill = GridBagConstraints.HORIZONTAL;
//                    c.anchor = GridBagConstraints.NORTHWEST;
//                    c.insets = new Insets (0,6,6,12);
//                    ((GridBagLayout)this.getLayout()).setConstraints(addURLButton,c);
//                    this.add (addURLButton);
//                }
                removeButton = new JButton (NbBundle.getMessage(CustomizerSupport.class, "CTL_Remove"));
                removeButton.setMnemonic(NbBundle.getMessage(CustomizerSupport.class, "MNE_Remove").charAt(0));
                removeButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(CustomizerSupport.class,"AD_Remove"));
                removeButton.addActionListener( new ActionListener () {
                    public void actionPerformed(ActionEvent e) {
                        removePathElement ();
                    }
                });
                removeButton.setEnabled(false);
                c = new GridBagConstraints();
                c.gridx = 1;
                c.gridy = 3;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.NORTHWEST;
                c.insets = new Insets (12,6,0,6);
                ((GridBagLayout)this.getLayout()).setConstraints(removeButton,c);
                this.add (removeButton);
                moveUpButton = new JButton (NbBundle.getMessage(CustomizerSupport.class, "CTL_Up"));
                moveUpButton.setMnemonic(NbBundle.getMessage(CustomizerSupport.class, "MNE_Up").charAt(0));
                moveUpButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(CustomizerSupport.class,"AD_Up"));
                moveUpButton.addActionListener( new ActionListener () {
                    public void actionPerformed(ActionEvent e) {
                        moveUpPathElement ();
                    }
                });
                moveUpButton.setEnabled(false);
                c = new GridBagConstraints();
                c.gridx = 1;
                c.gridy = 4;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.NORTHWEST;
                c.insets = new Insets (12,6,0,6);
                ((GridBagLayout)this.getLayout()).setConstraints(moveUpButton,c);
                this.add (moveUpButton);
                moveDownButton = new JButton (NbBundle.getMessage(CustomizerSupport.class, "CTL_Down"));
                moveDownButton.setMnemonic (NbBundle.getMessage(CustomizerSupport.class, "MNE_Down").charAt(0));
                moveDownButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSupport.class,"AD_Down"));
                moveDownButton.addActionListener( new ActionListener () {
                    public void actionPerformed(ActionEvent e) {
                        moveDownPathElement ();
                    }
                });
                moveDownButton.setEnabled(false);
                c = new GridBagConstraints();
                c.gridx = 1;
                c.gridy = 5;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.NORTHWEST;
                c.insets = new Insets (5,6,6,6);
                ((GridBagLayout)this.getLayout()).setConstraints(moveDownButton,c);
                this.add (moveDownButton);
            }
        }
        
//        private void addURLElement() {
//            JPanel p = new JPanel ();
//            GridBagLayout lm = new GridBagLayout();
//            p.setLayout (lm);
//            GridBagConstraints c = new GridBagConstraints ();
//            c.gridx = c.gridy = GridBagConstraints.RELATIVE;
//            c.insets = new Insets (12,12,12,6);
//            c.anchor = GridBagConstraints.NORTHWEST;
//            JLabel label = new JLabel (NbBundle.getMessage(CustomizerSupport.class,"CTL_AddJavadocURLMessage"));
//            label.setDisplayedMnemonic ('U');
//            lm.setConstraints(label,c);
//            p.add (label);
//            c = new GridBagConstraints ();
//            c.gridx = c.gridy = GridBagConstraints.RELATIVE;
//            c.gridwidth = GridBagConstraints.REMAINDER;
//            c.insets = new Insets (12,0,12,6);
//            c.fill = GridBagConstraints.HORIZONTAL;
//            c.anchor = GridBagConstraints.NORTHWEST;
//            JTextField text = new JTextField ();
//            text.setColumns(30);
//            text.setText (NbBundle.getMessage(CustomizerSupport.class,"TXT_DefaultProtocol"));
//            text.selectAll();
//            label.setLabelFor(text);
//            lm.setConstraints(text,c);
//            p.add (text);            
//            JButton[] options = new JButton[] {
//                new JButton (NbBundle.getMessage(CustomizerSupport.class,"CTL_AddJavadocURLTitle")),
//                new JButton (NbBundle.getMessage(CustomizerSupport.class,"CTL_Cancel"))
//            };
//            options[0].setMnemonic(NbBundle.getMessage(CustomizerSupport.class,"MNE_Add").charAt(0));
//            options[1].setMnemonic(NbBundle.getMessage(CustomizerSupport.class,"MNE_Cancel").charAt(0));
//            DialogDescriptor input = new DialogDescriptor (
//                p,
//                NbBundle.getMessage(CustomizerSupport.class,"CTL_AddJavadocURLTitle"),
//                true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);            
//            if (DialogDisplayer.getDefault().notify(input) == options[0]) {
//                try {
//                    String value = text.getText();
//                    URL url = new URL (value);
//                    ((PathModel)this.resources.getModel()).addPath(url);
//                    this.resources.setSelectedIndex (this.resources.getModel().getSize()-1);
//                } catch (MalformedURLException mue) {
//                    DialogDescriptor.Message message = new DialogDescriptor.Message (
//                        NbBundle.getMessage(CustomizerSupport.class,"CTL_InvalidURLFormat"),
//                        DialogDescriptor.ERROR_MESSAGE);
//                    DialogDisplayer.getDefault().notify(message);
//                }
//            }
//        }

        private void addPathElement () {
            JFileChooser chooser = new JFileChooser();
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setMultiSelectionEnabled (true);
            String title = null;
            String message = null;
            String approveButtonName = null;
            String approveButtonNameMne = null;
            if (SOURCES.equals(this.type)) {
                title = NbBundle.getMessage (CustomizerSupport.class,"TXT_OpenSources");
                message = NbBundle.getMessage (CustomizerSupport.class,"TXT_Sources");
                approveButtonName = NbBundle.getMessage (CustomizerSupport.class,"TXT_OpenSources");
                approveButtonNameMne = NbBundle.getMessage (CustomizerSupport.class,"MNE_OpenSources");
            } else if (JAVADOC.equals(this.type)) {
                title = NbBundle.getMessage (CustomizerSupport.class,"TXT_OpenJavadoc");
                message = NbBundle.getMessage (CustomizerSupport.class,"TXT_Javadoc");
                approveButtonName = NbBundle.getMessage (CustomizerSupport.class,"TXT_OpenJavadoc");
                approveButtonNameMne = NbBundle.getMessage (CustomizerSupport.class,"MNE_OpenJavadoc");
            } else {
                throw new IllegalStateException("Can't add element for classpath"); // NOI18N
            }
            chooser.setDialogTitle(title);
            chooser.setApproveButtonText(approveButtonName);
            chooser.setApproveButtonMnemonic (approveButtonNameMne.charAt(0));
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            //#61789 on old macosx (jdk 1.4.1) these two method need to be called in this order.
            chooser.setAcceptAllFileFilterUsed( false );
            chooser.setFileFilter (new SimpleFileFilter(message,new String[] {"ZIP","JAR"}));   //NOI18N
            if (this.currentDir != null && currentDir.exists()) {
                chooser.setCurrentDirectory(this.currentDir);
            }
            if (chooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
                File[] fs = chooser.getSelectedFiles();
                PathModel model = (PathModel) this.resources.getModel();
                boolean addingFailed = false;
                int firstIndex = this.resources.getModel().getSize();
                for (int i = 0; i < fs.length; i++) {
                    File f = fs[i];
                    //XXX: JFileChooser workaround (JDK bug #5075580), double click on folder returns wrong file
                    // E.g. for /foo/src it returns /foo/src/src
                    // Try to convert it back by removing last invalid name component
                    if (!f.exists()) {
                        File parent = f.getParentFile();
                        if (parent != null && f.getName().equals(parent.getName()) && parent.exists()) {
                            f = parent;
                        }
                    }
                    addingFailed|=!model.addPath (f);
                }
                if (addingFailed) {
                    new NotifyDescriptor.Message (NbBundle.getMessage(CustomizerSupport.class,"TXT_CanNotAddResolve"),
                            NotifyDescriptor.ERROR_MESSAGE);
                }
                int lastIndex = this.resources.getModel().getSize()-1;
                if (firstIndex<=lastIndex) {
                    int[] toSelect = new int[lastIndex-firstIndex+1];
                    for (int i = 0; i < toSelect.length; i++) {
                        toSelect[i] = firstIndex+i;
                    }
                    this.resources.setSelectedIndices(toSelect);
                }
                this.currentDir = FileUtil.normalizeFile(chooser.getCurrentDirectory());
            }
        }

        private void removePathElement () {
            int[] indices = this.resources.getSelectedIndices();
            if (indices.length == 0) {
                return;
            }
            PathModel model = (PathModel) this.resources.getModel();
            model.removePath (indices);
            if ( indices[indices.length-1]-indices.length+1 < this.resources.getModel().getSize()) {
                this.resources.setSelectedIndex (indices[indices.length-1]-indices.length+1);
            }
            else if (indices[0]>0) {
                this.resources.setSelectedIndex (indices[0]-1);
            }
        }

        private void moveDownPathElement () {
            int[] indices = this.resources.getSelectedIndices();
            if (indices.length == 0) {
                return;
            }
            PathModel model = (PathModel) this.resources.getModel();
            model.moveDownPath (indices);
            for (int i=0; i< indices.length; i++) {
                indices[i] = indices[i] + 1;
            }
            this.resources.setSelectedIndices (indices);
        }

        private void moveUpPathElement () {
            int[] indices = this.resources.getSelectedIndices();
            if (indices.length == 0) {
                return;
            }
            PathModel model = (PathModel) this.resources.getModel();
            model.moveUpPath (indices);
            for (int i=0; i< indices.length; i++) {
                indices[i] = indices[i] - 1;
            }
            this.resources.setSelectedIndices (indices);
        }

        private void selectionChanged () {
            if (this.type == CLASSPATH) {
                return;
            }
            int indices[] = this.resources.getSelectedIndices();
            this.removeButton.setEnabled (indices.length > 0);
            this.moveUpButton.setEnabled (indices.length > 0 && indices[0]>0);
            this.moveDownButton.setEnabled(indices.length > 0 && indices[indices.length-1]<this.resources.getModel().getSize()-1);
        }
    }
    
    private static class SimpleFileFilter extends FileFilter {

        private String description;
        private Collection extensions;


        public SimpleFileFilter (String description, String[] extensions) {
            this.description = description;
            this.extensions = Arrays.asList(extensions);
        }

        public boolean accept(File f) {
            if (f.isDirectory())
                return true;
            String name = f.getName();
            int index = name.lastIndexOf('.');   //NOI18N
            if (index <= 0 || index==name.length()-1)
                return false;
            String extension = name.substring (index+1).toUpperCase();
            return this.extensions.contains(extension);
        }

        public String getDescription() {
            return this.description;
        }
    }
}
