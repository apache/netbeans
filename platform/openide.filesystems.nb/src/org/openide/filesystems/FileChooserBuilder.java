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
package org.openide.filesystems;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import org.netbeans.modules.openide.filesystems.FileFilterSupport;
import org.openide.filesystem.spi.FileChooserBuilderProvider;
import org.openide.util.*;

/**
 * Utility class for working with JFileChoosers.  In particular, remembering
 * the last-used directory for a given file is made transparent.  You pass an
 * ad-hoc string key to the constructor (the fully qualified name of the
 * calling class is good for uniqueness, and there is a constructor that takes
 * a <code>Class</code> object as an argument for this purpose).  That key is
 * used to look up the most recently-used directory from any previous invocations
 * with the same key.  This makes it easy to have your user interface
 * &ldquo;remember&rdquo; where the user keeps particular types of files, and
 * saves the user from having to navigate through the same set of directories
 * every time they need to locate a file from a particular place.
 * <p/>
 * <code>FileChooserBuilder</code>'s methods each return <code>this</code>, so
 * it is possible to chain invocations to simplify setting up a file chooser.
 * Example usage:
 * <pre>
 *      <font color="gray">//The default dir to use if no value is stored</font>
 *      File home = new File (System.getProperty("user.home") + File.separator + "lib");
 *      <font color="gray">//Now build a file chooser and invoke the dialog in one line of code</font>
 *      <font color="gray">//&quot;libraries-dir&quot; is our unique key</font>
 *      File toAdd = new FileChooserBuilder ("libraries-dir").setTitle("Add Library").
 *              setDefaultWorkingDirectory(home).setApproveText("Add").showOpenDialog();
 *      <font color="gray">//Result will be null if the user clicked cancel or closed the dialog w/o OK</font>
 *      if (toAdd != null) {
 *          //do something
 *      }
 *</pre>
 * <p/>
 * Instances of this class are intended to be thrown away after use.  Typically
 * you create a builder, set it to create file choosers as you wish, then
 * use it to show a dialog or create a file chooser you then do something
 * with.
 * <p/>
 * Supports the most common subset of JFileChooser functionality;  if you
 * need to do something exotic with a file chooser, you are probably better
 * off creating your own.
 * <p/>
 * <b>Note:</b> If you use the constructor that takes a <code>Class</code> object,
 * please use <code>new FileChooserBuilder(MyClass.class)</code>, not
 * <code>new FileChooserBuilder(getClass())</code>.  This avoids unexpected
 * behavior in the case of subclassing.
 *
 * @author Tim Boudreau
 */
public class FileChooserBuilder {
    private boolean dirsOnly;
    private BadgeProvider badger;
    private String title;
    private String approveText;
    //Just in case...
    private static boolean PREVENT_SYMLINK_TRAVERSAL =
            !Boolean.getBoolean("allow.filechooser.symlink.traversal"); //NOI18N
    private final String dirKey;
    private File failoverDir;
    private FileFilter filter;
    private boolean fileHiding;
    private boolean controlButtonsShown = true;
    private String aDescription;
    private boolean filesOnly;
    private static final boolean DONT_STORE_DIRECTORIES =
            Boolean.getBoolean("forget.recent.dirs");
    private SelectionApprover approver;
    private final List<FileFilter> filters = new ArrayList<FileFilter>(3);
    private boolean useAcceptAllFileFilter = true;
    
    /**
     * Creates a new FileChooserBuilder which can interact with the given file system.
     * @param fileSystem A virtual file system
     * @return FileChooserBuilder related to the given file system
     * @since 9.11
     */
    public static FileChooserBuilder create(FileSystem fileSystem) {
        Collection<? extends FileChooserBuilderProvider> providers = Lookup.getDefault().lookupAll(FileChooserBuilderProvider.class);
        for (FileChooserBuilderProvider provider : providers) {
            FileChooserBuilder builder =  provider.createFileChooserBuilder(fileSystem);
            if (builder != null) {
                return builder;
            }
        }
        return new FileChooserBuilder(fileSystem.getDisplayName());
    }
    
    /**
     * Create a new FileChooserBuilder using the name of the passed class
     * as the metadata for looking up a starting directory from previous
     * application sessions or invocations.
     * @param type A non-null class object, typically the calling class
     */
    public FileChooserBuilder(Class type) {
        this(type.getName());
    }

    /**
     * Create a new FileChooserBuilder.  The passed key is used as a key
     * into NbPreferences to look up the directory the file chooser should
     * initially be rooted on.
     *
     * @param dirKey A non-null ad-hoc string.  If a FileChooser was previously
     * used with the same string as is passed, then the initial directory
     */
    public FileChooserBuilder(String dirKey) {
        Parameters.notNull("dirKey", dirKey);
        this.dirKey = dirKey;
    }

    /**
     * Set whether or not any file choosers created by this builder will show
     * only directories.
     * @param val true if files should not be shown
     * @return this
     */
    public FileChooserBuilder setDirectoriesOnly(boolean val) {
        dirsOnly = val;
        assert !filesOnly : "FilesOnly and DirsOnly are mutually exclusive";
        return this;
    }

    public FileChooserBuilder setFilesOnly(boolean val) {
        filesOnly = val;
        assert !dirsOnly : "FilesOnly and DirsOnly are mutually exclusive";
        return this;
    }

    /**
     * Provide an implementation of BadgeProvider which will "badge" the
     * icons of some files.
     *
     * @param provider A badge provider which will alter the icon of files
     * or folders that may be of particular interest to the user
     * @return this
     */
    public FileChooserBuilder setBadgeProvider(BadgeProvider provider) {
        this.badger = provider;
        return this;
    }

    /**
     * Set the dialog title for any JFileChoosers created by this builder.
     * @param val A localized, human-readable title
     * @return this
     */
    public FileChooserBuilder setTitle(String val) {
        title = val;
        return this;
    }

    /**
     * Set the text on the OK button for any file chooser dialogs produced
     * by this builder.
     * @param val A short, localized, human-readable string
     * @return this
     */
    public FileChooserBuilder setApproveText(String val) {
        approveText = val;
        return this;
    }

    /**
     * Set a file filter which filters the list of selectable files.
     * @param filter
     * @return this
     */
    public FileChooserBuilder setFileFilter (FileFilter filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Determines whether the <code>AcceptAll FileFilter</code> is used
     * as an available choice in the choosable filter list.
     * If false, the <code>AcceptAll</code> file filter is removed from
     * the list of available file filters.
     * If true, the <code>AcceptAll</code> file filter will become the
     * the actively used file filter.
     * @param accept whether the <code>AcceptAll FileFilter</code> is used
     * @return this
     * @since 8.3
     */
    public FileChooserBuilder setAcceptAllFileFilterUsed(boolean accept) {
        useAcceptAllFileFilter = accept;
        return this;
    }

    /**
     * Set the current directory which should be used <b>only if</b>
     * a last-used directory cannot be found for the key string passed
     * into this builder's constructor.
     * @param dir A directory to root any created file choosers on if
     * there is no stored path for this builder's key
     * @return this
     */
    public FileChooserBuilder setDefaultWorkingDirectory (File dir) {
        failoverDir = dir;
        return this;
    }

    /**
     * Enable file hiding in any created file choosers
     * @param fileHiding Whether or not to hide files.  Default is no.
     * @return this
     */
    public FileChooserBuilder setFileHiding(boolean fileHiding) {
        this.fileHiding = fileHiding;
        return this;
    }

    /**
     * Show/hide control buttons
     * @param val Whether or not to hide files.  Default is no.
     * @return this
     */
    public FileChooserBuilder setControlButtonsAreShown(boolean val) {
        this.controlButtonsShown = val;
        return this;
    }

    /**
     * Set the accessible description for any file choosers created by this
     * builder
     * @param aDescription The description
     * @return this
     */
    public FileChooserBuilder setAccessibleDescription(String aDescription) {
        this.aDescription = aDescription;
        return this;
    }

    /**
     * Create a JFileChooser that conforms to the parameters set in this
     * builder.
     * @return A file chooser
     */
    public JFileChooser createFileChooser() {
        JFileChooser result = new SavedDirFileChooser(dirKey, failoverDir,
                force, approver);
        prepareFileChooser(result);
        return result;
    }

    private boolean force = false;
    /**
     * Force use of the failover directory - i.e. ignore the directory key
     * passed in.
     * @param val
     * @return this
     */
    public FileChooserBuilder forceUseOfDefaultWorkingDirectory(boolean val) {
        this.force = val;
        return this;
    }

    /**
     * Show an open dialog that allows multiple selection.
     * @return An array of files, or null if the user cancelled the dialog
     */
    public File[] showMultiOpenDialog() {
        JFileChooser chooser = createFileChooser();
        chooser.setMultiSelectionEnabled(true);
        int result = chooser.showOpenDialog(Utilities.findDialogParent());
        if (JFileChooser.APPROVE_OPTION == result) {
            File[] files = chooser.getSelectedFiles();
            return files == null ? new File[0] : files;
        } else {
            return null;
        }
    }

    /**
     * Show an open dialog with a file chooser set up according to the
     * parameters of this builder.
     * @return A file if the user clicks the accept button and a file or
     * folder was selected at the time the user clicked cancel.
     */
    public File showOpenDialog() {
        JFileChooser chooser = createFileChooser();
        if( Boolean.getBoolean("nb.native.filechooser") ) { //NOI18N
            FileDialog fileDialog = createFileDialog( chooser.getCurrentDirectory() );
            if( null != fileDialog ) {
                return showFileDialog(fileDialog, FileDialog.LOAD );
            }
        }
        chooser.setMultiSelectionEnabled(false);
        int dlgResult = chooser.showOpenDialog(Utilities.findDialogParent());
        if (JFileChooser.APPROVE_OPTION == dlgResult) {
            File result = chooser.getSelectedFile();
            if (result != null && !result.exists()) {
                result = null;
            }
            return result;
        } else {
            return null;
        }

    }

    /**
     * Show a save dialog with the file chooser set up according to the
     * parameters of this builder.
     * @return A file if the user clicks the accept button and a file or
     * folder was selected at the time the user clicked cancel.
     */
    public File showSaveDialog() {
        JFileChooser chooser = createFileChooser();
        if( Boolean.getBoolean("nb.native.filechooser") ) { //NOI18N
            FileDialog fileDialog = createFileDialog( chooser.getCurrentDirectory() );
            if( null != fileDialog ) {
                return showFileDialog( fileDialog, FileDialog.SAVE );
            }
        }
        int result = chooser.showSaveDialog(Utilities.findDialogParent());
        if (JFileChooser.APPROVE_OPTION == result) {
            return chooser.getSelectedFile();
        } else {
            return null;
        }
    }
    
    private File showFileDialog( FileDialog fileDialog, int mode ) {
        String oldFileDialogProp = System.getProperty("apple.awt.fileDialogForDirectories"); //NOI18N
        if( dirsOnly ) {
            System.setProperty("apple.awt.fileDialogForDirectories", "true"); //NOI18N
        }
        fileDialog.setMode( mode );
        fileDialog.setVisible(true);
        if( dirsOnly ) {
            if( null != oldFileDialogProp ) {
                System.setProperty("apple.awt.fileDialogForDirectories", oldFileDialogProp); //NOI18N
            } else {
                System.clearProperty("apple.awt.fileDialogForDirectories"); //NOI18N
            }
        }
        if( fileDialog.getDirectory() != null && fileDialog.getFile() != null ) {
            String selFile = fileDialog.getFile();
            File dir = new File( fileDialog.getDirectory() );
            return new File( dir, selFile );
        }
        return null;
    }
    
    private void prepareFileChooser(JFileChooser chooser) {
        chooser.setFileSelectionMode(dirsOnly ? JFileChooser.DIRECTORIES_ONLY
                : filesOnly ? JFileChooser.FILES_ONLY :
                JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setFileHidingEnabled(fileHiding);
        chooser.setControlButtonsAreShown(controlButtonsShown);
        chooser.setAcceptAllFileFilterUsed(useAcceptAllFileFilter);
        if (title != null) {
            chooser.setDialogTitle(title);
        }
        if (approveText != null) {
            chooser.setApproveButtonText(approveText);
        }
        if (badger != null) {
            chooser.setFileView(new CustomFileView(new BadgeIconProvider(badger),
                    chooser.getFileSystemView()));
        }
        if (PREVENT_SYMLINK_TRAVERSAL) {
            FileUtil.preventFileChooserSymlinkTraversal(chooser,
                    chooser.getCurrentDirectory());
        }
        if (filter != null) {
            chooser.setFileFilter(filter);
        }
        if (aDescription != null) {
            chooser.getAccessibleContext().setAccessibleDescription(aDescription);
        }
        if (!filters.isEmpty()) {
            for (FileFilter f : filters) {
                chooser.addChoosableFileFilter(f);
            }
        }
    }

    private FileDialog createFileDialog( File currentDirectory ) {
        if( badger != null )
            return null;
        if( !Boolean.getBoolean("nb.native.filechooser") )
            return null;
        if( dirsOnly && !BaseUtilities.isMac() )
            return null;
        Component parentComponent = Utilities.findDialogParent();
        Frame parentFrame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parentComponent);
        FileDialog fileDialog = new FileDialog(parentFrame);
        if (title != null) {
            fileDialog.setTitle(title);
        }
        if( null != currentDirectory )
            fileDialog.setDirectory(currentDirectory.getAbsolutePath());
        return fileDialog;
    }
    
    /**
     * Equivalent to calling <code>JFileChooser.addChoosableFileFilter(filter)</code>.
     * Adds another file filter that can be displayed in the file filters combo
     * box in the file chooser.
     *
     * @param filter The file filter to add
     * @return this
     * @since 7.26.0
     */
    public FileChooserBuilder addFileFilter (FileFilter filter) {
        filters.add (filter);
        return this;
    }

    /**
     * Add all default file filters to the file chooser.
     *
     * @see MIMEResolver.Registration#showInFileChooser()
     * @see MIMEResolver.ExtensionRegistration#showInFileChooser()
     * @return this
     * @since 8.1
     */
    public FileChooserBuilder addDefaultFileFilters() {
        filters.addAll(FileFilterSupport.findRegisteredFileFilters());
        return this;
    }

    /**
     * Set a selection approver which can display an &quot;Overwrite file?&quot;
     * or similar dialog if necessary, when the user presses the accept button
     * in the file chooser dialog.
     *
     * @param approver A SelectionApprover which will determine if the selection
     * is valid
     * @return this
     * @since 7.26.0
     */
    public FileChooserBuilder setSelectionApprover (SelectionApprover approver) {
        this.approver = approver;
        return this;
    }

    /**
     * Object which can approve the selection (enabling the OK button or
     * equivalent) in a JFileChooser.  Equivalent to overriding
     * <code>JFileChooser.approveSelection()</code>
     * @since 7.26.0
     */
    public interface SelectionApprover {
        /**
         * Approve the selection, enabling the dialog to be closed.  Called by
         * the JFileChooser's <code>approveSelection()</code> method.  Use this
         * interface if you want to, for example, show a dialog asking
         * &quot;Overwrite File X?&quot; or similar.
         *
         * @param selection The selected file(s) at the time the user presses
         * the Open, Save or OK button
         * @return true if the selection is accepted, false if it is not and
         * the dialog should not be closed
         */
        public boolean approve (File[] selection);
    }

    private static final class SavedDirFileChooser extends JFileChooser {
        private final String dirKey;
        private final SelectionApprover approver;
        SavedDirFileChooser(String dirKey, File failoverDir, boolean force, SelectionApprover approver) {
            this.dirKey = dirKey;
            this.approver = approver;
            if (force && failoverDir != null && failoverDir.exists() && failoverDir.isDirectory()) {
                setCurrentDirectory(failoverDir);
            } else {
                String path = DONT_STORE_DIRECTORIES ? null :
                    NbPreferences.forModule(FileChooserBuilder.class).get(dirKey, null);
                if (path != null) {
                    File f = new File(path);
                    if (f.exists() && f.isDirectory()) {
                        setCurrentDirectory(f);
                    } else if (failoverDir != null) {
                        setCurrentDirectory(failoverDir);
                    }
                } else if (failoverDir != null) {
                    setCurrentDirectory(failoverDir);
                }
            }
        }

        @Override
        public void approveSelection() {
            if (approver != null) {
                File[] selected = getSelectedFiles();
                final File sf = getSelectedFile();
                if ((selected == null || selected.length == 0) && sf != null) {
                    selected = new File[] { sf };
                }
                boolean approved = approver.approve(selected);
                if (approved) {
                    super.approveSelection();
                }
            } else {
                super.approveSelection();
            }
        }

        @Override
        public int showDialog(Component parent, String approveButtonText) throws HeadlessException {
            int result = super.showDialog(parent, approveButtonText);
            if (result == APPROVE_OPTION) {
                saveCurrentDir();
            }
            return result;
        }

        private void saveCurrentDir() {
            File dir = super.getCurrentDirectory();
            if (!DONT_STORE_DIRECTORIES && dir != null && dir.exists() && dir.isDirectory()) {
                NbPreferences.forModule(FileChooserBuilder.class).put(dirKey, dir.getPath());
            }
        }
    }

    //Can open this API later if there is a use-case
    interface IconProvider {
        public Icon getIcon(File file, Icon orig);
    }

    /**
     * Provides "badges" for icons that indicate files or folders of particular
     * interest to the user.
     * @see FileChooserBuilder#setBadgeProvider
     */
    public interface BadgeProvider {
        /**
         *  Get the badge the passed file should use.  <b>Note:</b> this method
         * is called for every visible file.  The negative test (deciding
         * <i>not</i> to badge a file) should be very, very fast and immediately
         * return null.
         * @param file The file in question
         * @return an icon or null if no change to the appearance of the file
         * is needed
         */
        public Icon getBadge(File file);

        /**
         * Get the x offset for badges produced by this provider.  This is
         * the location of the badge icon relative to the real icon for the
         * file.
         * @return  a rightward pixel offset
         */
        public int getXOffset();

        /**
         * Get the y offset for badges produced by this provider.  This is
         * the location of the badge icon relative to the real icon for the
         * file.
         * @return  a downward pixel offset
         */
        public int getYOffset();
    }

    private static final class BadgeIconProvider implements IconProvider {

        private final BadgeProvider badger;

        public BadgeIconProvider(BadgeProvider badger) {
            this.badger = badger;
        }

        public Icon getIcon(File file, Icon orig) {
            Icon badge = badger.getBadge(file);
            if (badge != null && orig != null) {
                return new MergedIcon(orig, badge, badger.getXOffset(),
                        badger.getYOffset());
            }
            return orig;
        }
    }

    private static final class CustomFileView extends FileView {

        private final IconProvider provider;
        private final FileSystemView view;

        CustomFileView(IconProvider provider, FileSystemView view) {
            this.provider = provider;
            this.view = view;
        }

        @Override
        public Icon getIcon(File f) {
            Icon result = view.getSystemIcon(f);
            result = provider.getIcon(f, result);
            return result;
        }
    }

    private static class MergedIcon implements Icon {

        private Icon icon1;
        private Icon icon2;
        private int xMerge;
        private int yMerge;

        MergedIcon(Icon icon1, Icon icon2, int xMerge, int yMerge) {
            assert icon1 != null;
            assert icon2 != null;
            this.icon1 = icon1;
            this.icon2 = icon2;

            if (xMerge == -1) {
                xMerge = icon1.getIconWidth() - icon2.getIconWidth();
            }

            if (yMerge == -1) {
                yMerge = icon1.getIconHeight() - icon2.getIconHeight();
            }

            this.xMerge = xMerge;
            this.yMerge = yMerge;
        }

        public int getIconHeight() {
            return Math.max(icon1.getIconHeight(), yMerge + icon2.getIconHeight());
        }

        public int getIconWidth() {
            return Math.max(icon1.getIconWidth(), yMerge + icon2.getIconWidth());
        }

        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            icon1.paintIcon(c, g, x, y);
            icon2.paintIcon(c, g, x + xMerge, y + yMerge);
        }
    }
}
