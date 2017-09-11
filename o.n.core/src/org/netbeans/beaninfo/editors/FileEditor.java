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

package org.netbeans.beaninfo.editors;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.FilenameFilter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import org.openide.nodes.Node;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * PropertyEditor for <code>java.io.File</code>.
 *
 * @author Jaroslav Tulach, David Strupl, Peter Zavadsky, Jesse Glick
 */
public class FileEditor extends PropertyEditorSupport implements ExPropertyEditor, PropertyChangeListener {
    
    /** Name of the property obtained from the feature descriptor.*/
    static final String PROPERTY_SHOW_DIRECTORIES = "directories"; //NOI18N
    
    /** Name of the property obtained from the feature descriptor.*/
    static final String PROPERTY_SHOW_FILES = "files"; //NOI18N
    
    /** Name of the property obtained from the feature descriptor.*/
    static final String PROPERTY_FILTER = "filter"; //NOI18N
    
    /** Name of the property obtained from the feature descriptor.*/
    static final String PROPERTY_CURRENT_DIR = "currentDir"; //NOI18N

    /** Name of the property obtained from the feature descriptor. */
    static final String PROPERTY_BASE_DIR = "baseDir"; // NOI18N
    
    /** Name of the property obtained from the feature descriptor. */
    static final String PROPERTY_FILE_HIDING = "file_hiding"; // NOI18N
    
    /** Openning mode.*/
    private int mode = JFileChooser.FILES_AND_DIRECTORIES;
    
    /** Flag indicating whether to choose directories. Default value is <code>true</code>. */
    private boolean directories = true;
    /** Flag indicating whether to choose files. Default value is <code>true</code>. */
    private boolean files = true;
    /** Flag indicating whether to hide files marked as hidden. Default value is <code>false</code>. */
    private boolean fileHiding = false;
    /** Filter for files to show. */
    private javax.swing.filechooser.FileFilter fileFilter;
    /** Current firectory. */
    private File currentDirectory;
    /** Base directory to which to show relative path, if is set. */
    private File baseDirectory;

    /** Caches last used directory. */
    static File lastCurrentDir;

    private PropertyEnv env;
    
    /** Cached chooser.
     * If you don't cache it, MountIterator in core flickers and behaves weirdly,
     * because apparently PropertyPanel will call getCustomEditor repeatedly and
     * refresh the display each time.
     * XXX MountIterator is dead so is this still necessary? -jglick
     */
    private JFileChooser chooser;
    
    /** whether the value can be edited -- default to true */
    private boolean editable = true;
    
    /**
     * This method is called by the IDE to pass
     * the environment to the property editor.
     * @param env Environment passed by the ide.
     */
    public void attachEnv(PropertyEnv env) {
        this.env = env;

        // clearing to defaults
        directories = true;
        files = true;
        fileFilter = null;
        fileHiding = false;

        Object dirs = env.getFeatureDescriptor().getValue(PROPERTY_SHOW_DIRECTORIES);
        if (dirs instanceof Boolean) {
            directories = ((Boolean)dirs).booleanValue();
        } // XXX else if != null, warn
        Object fil = env.getFeatureDescriptor().getValue(PROPERTY_SHOW_FILES);
        if (fil instanceof Boolean) {
            files = ((Boolean)fil).booleanValue();
        } // XXX else if != null, warn
        Object filter = env.getFeatureDescriptor().getValue(PROPERTY_FILTER);
        if (filter instanceof FilenameFilter) {
            fileFilter = new DelegatingFilenameFilter((FilenameFilter)filter);
        } else if (filter instanceof javax.swing.filechooser.FileFilter) {
            fileFilter = (javax.swing.filechooser.FileFilter)filter;
        } else if (filter instanceof java.io.FileFilter) {
            fileFilter = new DelegatingFileFilter((java.io.FileFilter)filter);
        } // XXX else if != null, warn

        Object curDir = env.getFeatureDescriptor().getValue(PROPERTY_CURRENT_DIR);
        if (curDir instanceof File) {
            currentDirectory = (File)curDir;
            if(! currentDirectory.isDirectory()) {
                Logger.getAnonymousLogger().warning("java.io.File will not accept currentDir=" + currentDirectory); // NOI18N
                currentDirectory = null;
            }
        } // XXX else if != null, warn

        Object baseDir = env.getFeatureDescriptor().getValue(PROPERTY_BASE_DIR);
        if(baseDir instanceof File) {
            baseDirectory = (File)baseDir;
            // As baseDir accept only directories in their absolute form.
            if(!baseDirectory.isDirectory() || !baseDirectory.isAbsolute()) {
                Logger.getAnonymousLogger().warning("java.io.File will not accept baseDir=" + baseDirectory); // NOI18N
                baseDirectory = null;
            }
        } // XXX else if != null, warn
        if (files) {
            mode = directories ? JFileChooser.FILES_AND_DIRECTORIES : 
                JFileChooser.FILES_ONLY;
        } else {
            mode = directories ? JFileChooser.DIRECTORIES_ONLY :
                JFileChooser.FILES_AND_DIRECTORIES; // both false, what now? XXX warn
        }
        
        Object fileHide = env.getFeatureDescriptor().getValue(PROPERTY_FILE_HIDING);
        if (fileHide instanceof Boolean) {
            fileHiding = ((Boolean)fileHide).booleanValue();
        }
        
        if (env.getFeatureDescriptor() instanceof Node.Property){
            Node.Property prop = (Node.Property)env.getFeatureDescriptor();
            editable = prop.canWrite();
        }
    }

    /** Returns human readable form of the edited value.
     * @return string reprezentation
     */
    public String getAsText() {
        File file = (File)getValue();
        if (file == null) {
            return ""; // NOI18N
        }
        String path = file.getPath();
        // Dot is more friendly to people though Java itself would prefer blank:
        if ("".equals(path)) path = "."; // NOI18N
        return path;
    }
    
    /** Parses the given string and should create a new instance of the
     * edited object.
     * @param str string reprezentation of the file (used as a parameter for File).
     * @throws IllegalArgumentException If the given string cannot be parsed
     */
    public void setAsText(String str) throws IllegalArgumentException {
        if (str == null) {
            throw new IllegalArgumentException("null"); // NOI18N
        }
        if ("".equals(str)) { // NOI18N
            setValue(null);
            return;
        }
        // See getAsText.
        if (".".equals(str)) str = ""; // NOI18N
        setValue(new File(str));
    }

    /** Custon editor.
     * @return Returns custom editor component.
     */
    public Component getCustomEditor() {
        if (!editable) {
            String info = "";
            Object curVal = getValue();
            if (curVal instanceof java.io.File) {
                info = ((java.io.File)curVal).getAbsolutePath();
            }
            return new StringCustomEditor(info, false, true, null, this, env);
        }
        if (chooser == null) {
            chooser = createHackedFileChooser();
        
            File originalFile = (File)getValue ();
            if (originalFile != null && ! originalFile.isAbsolute() && baseDirectory != null) {
                originalFile = new File(baseDirectory, originalFile.getPath());
            }
            if (currentDirectory != null) {
                chooser.setCurrentDirectory (currentDirectory);
            } else if (originalFile != null && originalFile.getParentFile() != null) {
                chooser.setCurrentDirectory (originalFile.getParentFile());
                chooser.setSelectedFile (originalFile);
            } else if (lastCurrentDir != null) {
                chooser.setCurrentDirectory(lastCurrentDir);
            }
            chooser.setFileSelectionMode(mode);
            if (fileFilter != null) {
                chooser.setFileFilter(fileFilter);
            }
            switch (mode) {
                case JFileChooser.FILES_AND_DIRECTORIES:
                    chooser.setDialogTitle (getString ("CTL_DialogTitleFilesAndDirs"));
                    break;
                case JFileChooser.FILES_ONLY:
                    chooser.setDialogTitle (getString ("CTL_DialogTitleFiles"));
                    break;
                case JFileChooser.DIRECTORIES_ONLY:
                    chooser.setDialogTitle (getString ("CTL_DialogTitleDirs"));
                    break;
            }
            chooser.setFileHidingEnabled(fileHiding);

            chooser.setControlButtonsAreShown(false);

            chooser.addPropertyChangeListener(
                JFileChooser.SELECTED_FILE_CHANGED_PROPERTY,
                this
            );
            
            HelpCtx.setHelpIDString (chooser, getHelpCtx ().getHelpID ());
        }

        return chooser;
    }
    
    /** Implements PropertyEditor method.
     * @return Returns true.
     */
    public boolean supportsCustomEditor() {
        return true;
    }
    
    /** Should create a string insertable to the newly generated source code.
     * @return initialization string
     */
    public String getJavaInitializationString() {
        File value = (File) getValue ();
        if (value == null) {
            return "null"; // NOI18N
        } else {
            // [PENDING] not a full escape of filenames, but enough to at least
            // handle normal Windows backslashes
            if (baseDirectory != null && !value.isAbsolute()) {
                return "new java.io.File(" // NOI18N
                    + stringify(baseDirectory.getPath())
                    + ", " // NOI18N
                    + stringify(value.getPath())
                    + ")"; // NOI18N
            } else {
                return "new java.io.File(" // NOI18N
                    + stringify(value.getAbsolutePath())
                    + ")"; // NOI18N
            }
        }
    }
    static String stringify(String in) {
        StringBuilder buf = new StringBuilder(in.length() * 2 + 2);
        buf.append('"'); // NOI18N
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (c == '\\' || c == '"') { // NOI18N
                buf.append('\\'); // NOI18N
            }
            buf.append(c);
        }
        buf.append('"'); // NOI18N
        return buf.toString();
    }

    /** Gets help context. */
    private HelpCtx getHelpCtx () {
        return new HelpCtx (FileEditor.class);
    }
    
    /** Gets localized string. Helper method. */
    private static String getString(String key) {
        return NbBundle.getBundle(FileEditor.class).getString(key);
    }
    
    /** Gets relative path of file to specified directory only for case the file
     * is in directory tree.
     * @param baseDir base directory
     * @param file file which relative path to <code>baseDir</code> is needed
     * @return relative path or <code>null</code> can't be resolved 
     * or if the <code>file</code> is not under <code>baseDir</code> tree */
    static String getChildRelativePath(File baseDir, File file) {
        // Handle hypothetical weird situations where file is in baseDir
        // but the prefixes do not match. E.g.:
        // file=\foo\bar.txt (assumed to be on C:) baseDir=c:\foo
        if (file.equals(baseDir)) {
            // The empty pathname, not ".", is correct here I think...
            // Try making new File(new File("/tmp", x)) for x in {".", ""}
            return ""; // NOI18N
        }
        StringBuilder buf = new StringBuilder(file.getPath().length());
        buf.append(file.getName());
        for (File parent = file.getParentFile(); parent != null; parent = parent.getParentFile()) {
            if (parent.equals(baseDir)) {
                return buf.toString();
            }
            buf.insert(0, File.separatorChar);
            buf.insert(0, parent.getName());
        }
        return null;
    }
    
    /** Property change listaner attached to the JFileChooser chooser. */
    public void propertyChange(PropertyChangeEvent e) {
        JFileChooser chooser = (JFileChooser)e.getSource();
        File f = chooser.getSelectedFile();
        if (f == null) {
            return;
        }
        if (!files && f.isFile ()) return;
        if (!directories && f.isDirectory ()) return;

        if (baseDirectory != null) {
            String rel = getChildRelativePath(baseDirectory, f);
            if (rel != null) {
                f = new File(rel);
            }
        }

        // use to be setValue(f) - the next line is
        // workaround for JDK bug 4533419
        // it should be returned back to setValue(f) after the
        // mentioned bug is fixed in JDK.
        setValue(new File(f.getPath()));
        
        lastCurrentDir = chooser.getCurrentDirectory();
    }
    
    // XXX #18270. Enter doesn't work when expecting folder change,
    // Accessibility problem. We hack default behaviour here.
    /** Creates hacked fileChooser, responding on Enter the way it
     * performs folder change. */
    public static JFileChooser createHackedFileChooser() {
        JFileChooser chooser = new JFileChooser();
        hackFileChooser(chooser);
        return chooser;
    }
    
    /** Hacks fileChooser, responding on Enter the way it
     * performs folder change. */
    public static void hackFileChooser(final JFileChooser chooser) {
        chooser.getAccessibleContext().setAccessibleDescription( getString("ACSD_FileEditor") );
        
        //issue 31605 - make escape work properly
        //Get the existing action key on ESCAPE
        // XXX is this hack still necessary?
        final Object key = chooser.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        
        Action close = new AbstractAction() {
            public void actionPerformed(ActionEvent ae) {
                Component comp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (key != null) {
                    //if there was an action, do it first
                    Action a = chooser.getActionMap().get(key);
                    if (a != null) {
                        a.actionPerformed(ae);
                    }
                }
                if (comp.getParent() == null) {
                    //then we were editing a file name, and the editor
                    //was removed - we don't want to close the dialog
                    return;
                }
                
                Container c = chooser.getTopLevelAncestor();
                //The action *may* have already hidden the panel (works
                //intermittently)
                if (c instanceof Dialog) {
                    if (((Dialog) c).isVisible()) {
                        ((Dialog) c).setVisible (false);
                        ((Dialog) c).dispose();
                    }
                }
            }
        };
        chooser.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        chooser.getActionMap().put("close", close);
    }

    private static class ButtonHider implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent pce) {
            if (JFileChooser.CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY.equals(pce.getPropertyName())) {
                JFileChooser jfc = (JFileChooser) pce.getSource();
                try {
                    hideShowButtons(jfc, Boolean.TRUE.equals(pce.getNewValue()));
                } catch (Exception e) {
                    Logger.getLogger(FileEditor.class.getName()).log(Level.WARNING, null, e);
                }
            }
        }
        
        private void hideShowButtons (Container cont, boolean val) {
            if (cont instanceof JComboBox || cont instanceof JScrollBar) {
                return;
            }
            Component[] c = cont.getComponents();
            for (int i=0; i < c.length; i++) {
                if (c[i] instanceof Container) {
                    hideShowButtons ((Container) c[i], val);
                }
                if (c[i] instanceof AbstractButton) {
                    c[i].setVisible(val);
                }
            }
        }
    }
    
    /** Wraps java.io.FileFilter to javax.swing.filechooser.FileFilter. */
    static class DelegatingFileFilter extends javax.swing.filechooser.FileFilter {
        private java.io.FileFilter filter;
        
        public DelegatingFileFilter(java.io.FileFilter f) {
            this.filter = f;
        }
        
        public boolean accept(File f) {
            return filter.accept(f);
        }
        
        public String getDescription() {
            // [PENDING] what should we return?
            return null;
        }
        
    } // End of class DelegatingFileFilter.
    
    
    /** Wraps FilenameFilter to javax.swing.filechooser.FileFilter. */
    static class DelegatingFilenameFilter extends javax.swing.filechooser.FileFilter {
        private FilenameFilter filter;
        
        public DelegatingFilenameFilter(FilenameFilter f) {
            this.filter = f;
        }
        /** Calls the filenameFilter's accept method with arguments
         * created from the original object f.
         */
        public boolean accept(File f) {
            return filter.accept(f.getParentFile(), f.getName());
        }
        
        public String getDescription() {
            // [PENDING] what should we return?
            return null;
        }
    } // End of class DelegatingFilenameFilter.
    
}
