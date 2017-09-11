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

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

import org.openide.nodes.Node;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * PropertyEditor for <code>[java.io.File</code>.
 *
 * @author David Strupl
 */
public class FileArrayEditor extends PropertyEditorSupport implements ExPropertyEditor, PropertyChangeListener {
    
    private static final String ANCESTOR = "ancestor";              // NOI18N

    /** Openning mode.*/
    private int mode = JFileChooser.FILES_AND_DIRECTORIES;
    
    /** Flag indicating whether to choose directories. Default value is <code>true</code>. */
    private boolean directories = true;
    /** Flag indicating whether to choose files. Default value is <code>true</code>. */
    /** Flag indicating whether to hide files marked as hidden. Default value is <code>false</code>. */
    private boolean fileHiding = false;
    private boolean files = true;
    /** Filter for files to show. */
    private javax.swing.filechooser.FileFilter fileFilter;
    /** Current firectory. */
    private File currentDirectory;
    /** Base directory to which to show relative path, if is set. */
    private File baseDirectory;
    

    private ThreadLocal<String> myPropertyFired = new ThreadLocal<String>();
    
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
        // clearing to defaults
        directories = true;
        files = true;
        fileFilter = null;
        fileHiding = false;

        Object dirs = env.getFeatureDescriptor().getValue(FileEditor.PROPERTY_SHOW_DIRECTORIES);
        if (dirs instanceof Boolean) {
            directories = ((Boolean)dirs).booleanValue();
        } // XXX else if != null, warn
        Object fil = env.getFeatureDescriptor().getValue(FileEditor.PROPERTY_SHOW_FILES);
        if (fil instanceof Boolean) {
            files = ((Boolean)fil).booleanValue();
        } // XXX else if != null, warn
        Object filter = env.getFeatureDescriptor().getValue(FileEditor.PROPERTY_FILTER);
        if (filter instanceof FilenameFilter) {
            fileFilter = new FileEditor.DelegatingFilenameFilter((FilenameFilter)filter);
        } else if (filter instanceof javax.swing.filechooser.FileFilter) {
            fileFilter = (javax.swing.filechooser.FileFilter)filter;
        } else if (filter instanceof java.io.FileFilter) {
            fileFilter = new FileEditor.DelegatingFileFilter((java.io.FileFilter)filter);
        } // XXX else if != null, warn

        Object curDir = env.getFeatureDescriptor().getValue(FileEditor.PROPERTY_CURRENT_DIR);
        if (curDir instanceof File) {
            currentDirectory = (File)curDir;
            if(! currentDirectory.isDirectory()) {
                Logger.getAnonymousLogger().warning("java.io.File will not accept currentDir=" + baseDirectory); // NOI18N
                currentDirectory = null;
            }
        } // XXX else if != null, warn

        Object baseDir = env.getFeatureDescriptor().getValue(FileEditor.PROPERTY_BASE_DIR);
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
        
        Object fileHide = env.getFeatureDescriptor().getValue(FileEditor.PROPERTY_FILE_HIDING);
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
        File[] file = (File[])getValue();
        if (file == null) {
            return ""; // NOI18N
        }
        StringBuilder path = new StringBuilder("[");
        for (int i = 0; i < file.length; i++) {
            path.append(file[i].getPath());
        }
        // Dot is more friendly to people though Java itself would prefer blank:
        if (file.length == 0) path.append('.'); // NOI18N
        return path.append(']').toString(); // NOI18N
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
        
        // [PENDING] Add tokenizer here !!!
    }

    /** Custon editor.
     * @return Returns custom editor component.
     */
    public Component getCustomEditor() {
        if (!editable) {
            return new StringCustomEditor(getAsText(), false);
        }
        if (chooser == null) {
            chooser = FileEditor.createHackedFileChooser();
            chooser.setMultiSelectionEnabled(true);
        
            Object vv = getValue ();
            File originalFile = null;
            
            if (vv instanceof File[]) {
                File[] ofile = (File[]) vv;
                if (ofile.length > 0) {
                    originalFile = ofile[0];
                    if (originalFile != null && ! originalFile.isAbsolute() && baseDirectory != null) {
                        originalFile = new File(baseDirectory, originalFile.getPath());
                    }
                }
            }
            if (currentDirectory != null) {
                chooser.setCurrentDirectory (currentDirectory);
            } else if (originalFile != null && originalFile.getParentFile() != null) {
                chooser.setCurrentDirectory (originalFile.getParentFile());
                chooser.setSelectedFile (originalFile);
            } else if (FileEditor.lastCurrentDir != null) {
                chooser.setCurrentDirectory(FileEditor.lastCurrentDir);
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

            chooser.addPropertyChangeListener(this);
            
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
        File[] value = (File[]) getValue ();
        if (value == null) {
            return "null"; // NOI18N
        } else {
            // [PENDING] not a full escape of filenames, but enough to at least
            // handle normal Windows backslashes
            StringBuilder retVal = new StringBuilder("new java.io.File[] { "); // NOI18N
            for (int i = 0; i < value.length; i++) {
                if (baseDirectory != null && !value[i].isAbsolute()) {
                    retVal.append("new java.io.File(") // NOI18N
                        .append(FileEditor.stringify(baseDirectory.getPath()))
                        .append( ", " ) // NOI18N
                        .append(FileEditor.stringify(value[i].getPath()))
                        .append( "), " ); // NOI18N
                } else {
                    retVal.append("new java.io.File(") // NOI18N
                        .append(FileEditor.stringify(value[i].getAbsolutePath()))
                        .append("), "); // NOI18N
                }
            }
            return retVal.append(" }").toString();
        }
    }
    
    /** Gets help context. */
    private HelpCtx getHelpCtx () {
        return new HelpCtx (FileEditor.class);
    }
    
    /** Gets localized string. Helper method. */
    private static String getString(String key) {
        return NbBundle.getBundle(FileArrayEditor.class).getString(key);
    }
    
    /** Property change listaner attached to the JFileChooser chooser. */
    public void propertyChange(PropertyChangeEvent e) {
        // Fix for IZ#36742 - FileArrayEditor fires each selection change twice
        if ( ANCESTOR.equals( e.getPropertyName()) ){
            myPropertyFired.set( null );
        }
        
        if (e.getSource() instanceof JFileChooser) {
            JFileChooser jfc = (JFileChooser) e.getSource();
            if (mode == jfc.DIRECTORIES_ONLY && jfc.DIRECTORY_CHANGED_PROPERTY.equals(e.getPropertyName())) {
                if (jfc.getSelectedFile() == null) {
                    File dir = jfc.getCurrentDirectory();
                    if (dir != null) {
                        setValue (new File[] {new File(dir.getAbsolutePath())});
                        return;
                    }
                }
            }
        }
        
        if (( ! JFileChooser.SELECTED_FILES_CHANGED_PROPERTY.equals(e.getPropertyName())) && 
            ( ! JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(e.getPropertyName()))) {
                return;
        }
        if (! (e.getSource() instanceof JFileChooser)) {
            return;
        }
        JFileChooser chooser = (JFileChooser)e.getSource();
        File[] f = (File[])chooser.getSelectedFiles();
        if (f == null) {
            return;
        }
        
        // Fix for IZ#36742 - FileArrayEditor fires each selection change twice
        if ( isAlreadyHandled ( chooser , f , e.getPropertyName())){
            return;
        }
        
        if ((f.length == 0) && (chooser.getSelectedFile() != null)) {
            f = new File[] { chooser.getSelectedFile() };
        }
        
        for (int i = 0; i < f.length; i++) {
            if (!files && f[i].isFile ()) return;
            if (!directories && f[i].isDirectory ()) return;
        }

        if (baseDirectory != null) {
            for (int i = 0; i < f.length; i++) {
                String rel = FileEditor.getChildRelativePath(baseDirectory, f[i]);
                if (rel != null) {
                    f[i] = new File(rel);
                }
            }
        }
        
        File[] nf = new File[f.length];
        for (int i = 0; i < f.length; i++) {
            // the next line is
            // workaround for JDK bug 4533419
            // it should be returned back to f[i] after the
            // mentioned bug is fixed in JDK.
            nf[i] = new File(f[i].getAbsolutePath());
        }
        setValue(nf);
        
        FileEditor.lastCurrentDir = chooser.getCurrentDirectory();
    }

    // Fix for IZ#36742 FileArrayEditor fires each selection change twice 
    private boolean isAlreadyHandled( JFileChooser chooser, File[] files ,
            String property ) 
    {
        Set<File> fileSet = new HashSet<File>( Arrays.asList( files ));
        File file = chooser.getSelectedFile();
        boolean contains = (file == null && files.length ==0 ) 
            || (file!= null && fileSet.contains( file ));
        if ( !contains || property.equals( myPropertyFired.get())){
            myPropertyFired.set( null );
            return false;
        }
        else {
            if ( isFired() ){
                myPropertyFired.set( null );
                return true;
            }
            else {
                myPropertyFired.set( property );
                return false;
            }
        }
    }
    
    private boolean isFired(){
        return myPropertyFired.get() != null;
    }
    
}
