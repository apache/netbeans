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
package org.netbeans.editor.example;

import java.util.*;

import java.net.URL;
import java.io.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.MenuItem;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.undo.UndoManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;


/**
 *
 * @author  Petr Nejedly
 * @version 0.2
 */
public class Editor extends javax.swing.JFrame {

    private static final File distributionDirectory;
    
    static {
	URL url = Editor.class.getProtectionDomain().getCodeSource().getLocation();
	String protocol = url.getProtocol();
	File file = new File(url.getFile());
	if (!file.isDirectory()) file = file.getParentFile();
	distributionDirectory = file;
    }
    
    /** Document property holding String name of associated file */
    private static final String FILE = "file"; // NOI18N
    /** Document property holding Boolean if document was created or opened */
    private static final String CREATED = "created"; // NOI18N
    /** Document property holding Boolean modified information */
    private static final String MODIFIED = "modified"; // NOI18N
        
    private ResourceBundle settings = ResourceBundle.getBundle( "settings" ); // NOI18N

    private JFileChooser fileChooser;

    private boolean createBackups;
    private boolean safeSave;

    private int fileCounter = -1;
    private Map<Component, JTextComponent> com2text = new HashMap<>();
    
    private Impl impl = new Impl("org.netbeans.editor.Bundle"); // NOI18N
    
    private class Impl extends FileView implements WindowListener,
                                    ActionListener, LocaleSupport.Localizer {
                                        
        private ResourceBundle bundle;
	
	public Impl( String bundleName ) {
	    bundle = ResourceBundle.getBundle( bundleName );
	}

        // FileView implementation
	public String getName( File f ) { return null; }
	public String getDescription( File f ) { return null; }
	public String getTypeDescription( File f ) { return null; }
	public Boolean isTraversable( File f ) { return null; }
        public Icon getIcon( File f ) {
            if( f.isDirectory() ) return null;
            KitInfo ki = KitInfo.getKitInfoForFile( f );
            return ki == null ? null : ki.getIcon();
        }
        
        // Localizer
        public String getString( String key ) {
	    return bundle.getString( key );
	}
        
        // Mostly no-op WindowListener for close
        public void windowActivated(WindowEvent evt) {}
        public void windowClosed(WindowEvent evt) {}
        public void windowDeactivated(WindowEvent evt) {}
        public void windowDeiconified(WindowEvent evt) {}
        public void windowIconified(WindowEvent evt) {}
        public void windowOpened(WindowEvent evt) {}
        public void windowClosing(java.awt.event.WindowEvent evt) {
            doExit();
        }

        // ActionListener for menu items
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            Object src = evt.getSource();

            if (!handleOpenRecent(src)) {
                if (src == openItem) {
                    fileChooser.setMultiSelectionEnabled(true);
                    int returnVal = fileChooser.showOpenDialog(Editor.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File[] files = fileChooser.getSelectedFiles();
                        for (int i = 0; i < files.length; i++) openFile(files[i], i == 0);
                    }
                    fileChooser.setMultiSelectionEnabled(false);
                } else if (src == closeItem) {
                    Component editor = tabPane.getSelectedComponent();
                    if (checkClose(editor)) {
                        doCloseEditor(editor);
                    }
                } else if (src == saveItem) {
                    saveFile(tabPane.getSelectedComponent());
                } else if (src == saveAsItem) {
                    saveAs(tabPane.getSelectedComponent());
                } else if (src == saveAllItem) {
                    int index = tabPane.getSelectedIndex();
                    for (int i = 0; i < tabPane.getComponentCount(); i++) {
                        saveFile(tabPane.getComponentAt(i));
                    }
                    tabPane.setSelectedIndex(index);
                } else if (src == exitItem) {
                    doExit();
                } else if (src instanceof JMenuItem) {
                    Object ki = ((JMenuItem) src).getClientProperty("kitInfo"); // NOI18N

                    if (ki instanceof KitInfo) {
                        createNewFile((KitInfo) ki);
                    }
                }
            }
        }
    }
    
    public Editor() {
        super( "NetBeans Editor" ); // NOI18N
        LocaleSupport.addLocalizer(impl);

        // Feed our kits with their default Settings
        Settings.addInitializer(new BaseSettingsInitializer(), Settings.CORE_LEVEL);
        Settings.addInitializer(new ExtSettingsInitializer(), Settings.CORE_LEVEL);
        Settings.reset();
        
        // Create visual hierarchy
        initComponents ();
        openItem.addActionListener(impl);
        closeItem.addActionListener(impl);
        saveItem.addActionListener(impl);
        saveAsItem.addActionListener(impl);
        saveAllItem.addActionListener(impl);
        exitItem.addActionListener(impl);
        addWindowListener(impl);
        
        // Prepare the editor kits and such things
        readSettings();
                
        // Do the actual layout
        setLocation( 150, 150 );
        pack ();

        fileToMenu = new HashMap();
        menuToFile = new HashMap();
        recentFiles = new Vector();
        maxRecent = 4;

        createBackups = false;
        safeSave = true;
    }
    
    public Dimension getPreferredSize() {
        Dimension size = new Dimension( 640,480 );
        return size;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        tabPane = new javax.swing.JTabbedPane();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newMenu = new javax.swing.JMenu();
        openItem = new javax.swing.JMenuItem();
        closeItem = new javax.swing.JMenuItem();
        sep1 = new javax.swing.JSeparator();
        saveItem = new javax.swing.JMenuItem();
        saveAsItem = new javax.swing.JMenuItem();
        saveAllItem = new javax.swing.JMenuItem();
        sep2 = new javax.swing.JSeparator();
        exitItem = new javax.swing.JMenuItem();

        getContentPane().setLayout(new java.awt.GridLayout(1, 1));

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        getContentPane().add(tabPane);

        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.setText("File"); // NOI18N
        newMenu.setMnemonic(KeyEvent.VK_N);
        newMenu.setText("New..."); // NOI18N
        fileMenu.add(newMenu);
        openItem.setMnemonic(KeyEvent.VK_O);
        openItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openItem.setText("Open File..."); // NOI18N
        fileMenu.add(openItem);
        closeItem.setMnemonic(KeyEvent.VK_C);
        closeItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.CTRL_MASK));
        closeItem.setText("Close"); // NOI18N
        fileMenu.add(closeItem);
        fileMenu.add(sep1);
        saveItem.setMnemonic(KeyEvent.VK_S);
        saveItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveItem.setText("Save"); // NOI18N
        fileMenu.add(saveItem);
        saveAsItem.setMnemonic(KeyEvent.VK_A);
        saveAsItem.setText("Save As..."); // NOI18N
        fileMenu.add(saveAsItem);
        saveAllItem.setMnemonic(KeyEvent.VK_L);
        saveAllItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        saveAllItem.setText("Save All"); // NOI18N
        fileMenu.add(saveAllItem);
        fileMenu.add(sep2);
        exitItem.setMnemonic(KeyEvent.VK_E);
        exitItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        exitItem.setText("Exit"); // NOI18N
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

    }//GEN-END:initComponents
                
    private boolean saveFile( Component comp, File file, boolean checkOverwrite ) {
        if( comp == null ) return false;
        tabPane.setSelectedComponent( comp );
        JTextComponent edit = com2text.get( comp );
        Document doc = edit.getDocument();
        
        if( checkOverwrite && file.exists() ) {
            tabPane.setSelectedComponent( comp );
            int choice = JOptionPane.showOptionDialog(this,
            "File " + file.getName() + " already exists, overwrite?", // NOI18N
            "File exists", // NOI18N
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,     // don't use a custom Icon
            null,     // use standard button titles
            null      // no default selection
            );
            if( choice != 0 ) return false;
        }

        File safeSaveFile = new File(file.getAbsolutePath() + "~~"); // NOI18N
        File backupFile = new File(file.getAbsolutePath() + "~"); // NOI18N

        if (safeSave || createBackups) {
            file.renameTo(safeSaveFile);
        }

        FileWriter output = null;

        try {
            output = new FileWriter( file );
            edit.write( output );

            if (createBackups) {
                safeSaveFile.renameTo(backupFile);
            } else {
                if (safeSave) {
                    safeSaveFile.delete();
                }
            }
        } catch( IOException exc ) {
            JOptionPane.showMessageDialog( this, "Can't write to file '" + // NOI18N
            file.getName() + "'.", "Error", JOptionPane.ERROR_MESSAGE ); // NOI18N

            if (safeSave)
                safeSaveFile.renameTo(file);

            return false;
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        }

        doc.putProperty( MODIFIED, Boolean.FALSE );
        doc.putProperty( CREATED, Boolean.FALSE );
        doc.putProperty( FILE, file );
        doc.addDocumentListener( new MarkingDocumentListener( comp ) );
        
        int index = tabPane.indexOfComponent( comp );
        tabPane.setTitleAt( index, file.getName() );
        
        return true;
    }
    
    private boolean saveFile( Component comp ) {
        if( comp == null ) return false;
        JTextComponent edit = com2text.get( comp );
        Document doc = edit.getDocument();
        File file = (File)doc.getProperty( FILE );
        boolean created = ((Boolean)doc.getProperty( CREATED )).booleanValue();
        
        return saveFile( comp, file, created );
    }
    
    private boolean saveAs( Component comp ) {
        if( comp == null ) return false;
        JTextComponent edit = com2text.get( comp );
        File file = (File)edit.getDocument().getProperty( FILE );
        
        fileChooser.setCurrentDirectory( file.getParentFile() );
        fileChooser.setSelectedFile( file );
        KitInfo fileInfo = KitInfo.getKitInfoOrDefault( file );
        
        if( fileInfo != null ) fileChooser.setFileFilter( fileInfo );
        
        // show the dialog, test the result
        if( fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
            return saveFile( comp, fileChooser.getSelectedFile(), true );
        else
            return false; // Cancel was pressed - not saved
    }

    private void openFile( File file, boolean focus ) {
        KitInfo info = KitInfo.getKitInfoOrDefault( file );
        
        final JEditorPane pane = new JEditorPane( info.getType(), "" );
        try {
            pane.read( new FileInputStream( file ), file.getCanonicalPath() );
        } catch( IOException exc ) {
            JOptionPane.showMessageDialog( this, "Can't read from file '" + // NOI18N
            file.getName() + "'.", "Error", JOptionPane.ERROR_MESSAGE ); // NOI18N
            return;
        }
        addEditorPane( pane, info.getIcon(), file, false, focus );

        removeFromRecent(file.getAbsolutePath());
    }

    private void doExit() {
        boolean exit = true;
        int components = tabPane.getComponentCount();

        for (int cntr = 0; cntr < components; cntr++) {
            Component editor = tabPane.getComponentAt( cntr );

            if( ! checkClose( editor ) ) {
                exit = false;
                return;
            }
        }

        if (!exit) {
            System.err.println("keeping");
            return;
        }

        writeUserConfiguration();

        while( tabPane.getComponentCount() > 0 ) {
            Component editor = tabPane.getComponentAt( 0 );

            if ((editor != null) && (com2text.get(editor) != null))
                doCloseEditor(editor);
        }

        if( exit ) System.exit (0);
    }

    private void doCloseEditor(Component editor) {
        JTextComponent editorPane = com2text.get(editor);
        if (editorPane != null) {
            File file = (File) editorPane.getDocument().getProperty(FILE);

            addToRecent(file.getAbsolutePath());
        }

        tabPane.remove( editor );
        com2text.remove( editor );
    }

    private boolean checkClose( Component comp ) {
        if( comp == null ) return false;
        JTextComponent edit = com2text.get( comp );
        Document doc = edit.getDocument();
        
        Object mod = doc.getProperty( MODIFIED );
        if( mod == null || ! ((Boolean)mod).booleanValue()) return true;
        
        tabPane.setSelectedComponent( comp );
        File file = (File)doc.getProperty( FILE );
        
        for( ;; ) {
            int choice = JOptionPane.showOptionDialog(this,
            "File " + file.getName() + " was modified, save it?", // NOI18N
            "File modified", // NOI18N
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,     // don't use a custom Icon
            new String[] { "Save", "Save As...", "Discard", "Cancel" },     // use standard button titles // NOI18N
            "Cancel"      //default selection // NOI18N
            );
            
            switch( choice ) {
                case JOptionPane.CLOSED_OPTION:
                case 4:
                    return false; // Cancel or Esc pressed
                case 1:
                    if( !saveAs( comp ) ) continue;  // Ask for fileName, then save
                    return true;
                case 0:
                    if( !saveFile( comp ) ) continue; // else fall through
                case 2:
                    return true;  // Discard changes, close window
            }
            return false;
        }
    }
    
    private void addEditorPane( JEditorPane pane, Icon icon, File file, boolean created, boolean focus ) {
        final JComponent c = (pane.getUI() instanceof BaseTextUI) ?
        Utilities.getEditorUI(pane).getExtComponent() : new JScrollPane( pane );
        Document doc = pane.getDocument();
        
        doc.addDocumentListener( new MarkingDocumentListener( c ) );
        doc.putProperty( FILE, file );
        doc.putProperty( CREATED, created  ? Boolean.TRUE : Boolean.FALSE );
        
        UndoManager um = new UndoManager();
        doc.addUndoableEditListener( um );
        doc.putProperty( BaseDocument.UNDO_MANAGER_PROP, um );
        
        com2text.put( c, pane );
        tabPane.addTab( file.getName(), icon, c, file.getAbsolutePath() );
        if (focus) {
            tabPane.setSelectedComponent( c );
            pane.requestFocus();
        }
    }
    
    
    private void createNewFile( KitInfo info ) {
        final String fileName = ((++fileCounter == 0 ) ?
            "unnamed" : // NOI18N
            ("unnamed" + fileCounter)) + info.getDefaultExtension(); // NOI18N
        final File file = new File( fileName ).getAbsoluteFile();

        final JEditorPane pane = new JEditorPane( info.getType(), "" );
        URL template = info.getTemplate();
        if( template != null ) {
            try {
                pane.read( template.openStream(), file.getCanonicalPath() );
            } catch( IOException e ) {
                JOptionPane.showMessageDialog( this, "Can't read template", "Error", JOptionPane.ERROR_MESSAGE ); // NOI18N
            }
        }
        addEditorPane( pane, info.getIcon(), file, true, true );
    }
    
    
    
    public static File getDistributionDirectory() {
       return distributionDirectory;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main (String args[]) {
        if (!getDistributionDirectory().canRead()) {
            System.err.println("Fatal error while startup - can read from distribution directory.");
            System.exit(0);
        }
      
        Editor editor = new Editor ();

        editor.show ();

        editor.readUserConfiguration();

        for( int i = 0; i < args.length; i++ ) {
            String fileName = args[i];
            editor.openFile( new File( fileName ), i == 0 );
        }
    }
    
    private Map        fileToMenu;
    private Map        menuToFile;
    private Vector     recentFiles;
    private int        maxRecent;
    private JSeparator recentSeparator;
    private int        separatorIndex;
    
    private String[] getOpenedFiles() {
        List<String> opened = new ArrayList<>();

        int components = tabPane.getComponentCount();

        for (int cntr = 0; cntr < components; cntr++) {
            Component editorComponent = tabPane.getComponentAt( cntr );

            JTextComponent editor = com2text.get(editorComponent);
	    
	    if (editor == null) {
	        continue;
	    }
	    
            Document doc = editor.getDocument();
            File file = (File) doc.getProperty(FILE);
            
            if (file != null) {
                opened.add(file.getAbsolutePath());
            }
        }
	
        return opened.toArray(new String[opened.size()]);
    }
    
    private int findInRecent(String fileToFind) {
        for (int cntr = 0; cntr < recentFiles.size(); cntr++) {
            String file = (String) recentFiles.get(cntr);
            
            if (fileToFind.equals(file))
                return cntr;
        }
        
        return -1;
    }

    private boolean handleOpenRecent(Object source) {
        String fileName = (String) menuToFile.get(source);

        if (fileName == null)
            return false;

        openFile(new File(fileName), true);

        return true;
    }

    private String generateMenuItemName(int index, String file) {
        return "" + index + ". " + file; // NOI18N
    }

    private void addToRecent(String fileToAdd) {
        //Remove possible previous occurence:
        removeFromRecent(fileToAdd);
        
        if (recentFiles.size() >= maxRecent) {
            while (recentFiles.size() >= maxRecent) {
                removeFromRecent(recentFiles.size() - 1);
            }
        }
        
        recentFiles.add(0, fileToAdd);
        
        JMenuItem newItem = new JMenuItem(generateMenuItemName(1, fileToAdd));
	
	if (recentFiles.size() == 1) {
	    recentSeparator = new JSeparator();
	    fileMenu.add(recentSeparator);
	    separatorIndex = fileMenu.getMenuComponentCount();
	}

        newItem.addActionListener(impl);

        fileMenu.insert(newItem, separatorIndex);
        fileToMenu.put(fileToAdd, newItem);
        menuToFile.put(newItem, fileToAdd);
	
	correctItemNumbers();
    }

    private void correctItemNumbers() {
        for (int cntr = 0; cntr < recentFiles.size(); cntr++) {
            JMenuItem item = (JMenuItem ) fileToMenu.get(recentFiles.get(cntr));

            item.setText(generateMenuItemName(cntr + 1, (String) recentFiles.get(cntr)));
        }
    }

    private void removeFromRecent(String fileToRemove) {
        int position = findInRecent(fileToRemove);
        
        if (position != (-1))
            removeFromRecent(position);
    }

    private void removeFromRecent(int indexToRemove) {
        String file = (String) recentFiles.get(indexToRemove);
        
        recentFiles.remove(indexToRemove);
        
        JMenuItem fileItem = (JMenuItem) fileToMenu.get(file);
        
        fileMenu.remove(fileItem);

        fileToMenu.remove(file);
        menuToFile.remove(fileItem);

        correctItemNumbers();

	if (recentFiles.size() == 0) {
	    fileMenu.remove(recentSeparator);
	    recentSeparator = null;
	    separatorIndex = -1;
	}
    }
    
    private String[] readStrings(ResourceBundle bundle, String prefix) {
        int count = 0;
        boolean finish = false;
        ArrayList result = new ArrayList();
        
        while (!finish) {
            try {
                String current = bundle.getString(prefix + "_" + count);
                
                result.add(current);
                count++;
            } catch (MissingResourceException e) {
                finish = true;
            }
        }
        
        return (String []) result.toArray(new String[result.size()]);
    }
    
    private void readUserConfiguration(ResourceBundle bundle) {
        String[] openedFiles = readStrings(bundle, "Open-File"); // NOI18N
        String[] recentFiles = readStrings(bundle, "Recent-File"); // NOI18N
        String   recentFilesMaxCount = bundle.getString("Max-Recent-Files");
        String   safeSaveString = bundle.getString("Safe-Save");
        String   createBackupsString = bundle.getString("Create-Backups");

        this.maxRecent = Integer.parseInt(recentFilesMaxCount);
        this.safeSave  = Boolean.valueOf(safeSaveString).booleanValue();
        this.createBackups = Boolean.valueOf(createBackupsString).booleanValue();

        for (int cntr = recentFiles.length; cntr > 0; cntr--) {
            addToRecent(recentFiles[cntr - 1]);
        }

        for (int cntr = 0; cntr < openedFiles.length; cntr++) {
            openFile(new File(openedFiles[cntr]), false);
        }
    }

    private void writeUserConfiguration(PrintWriter output) {
        output.println("Max-Recent-Files=" + maxRecent); // NOI18N
        output.println("Safe-Save=" + safeSave); // NOI18N
        output.println("Create-Backups=" + createBackups); // NOI18N

        for (int cntr = 0; cntr < recentFiles.size(); cntr++) {
            output.println("Recent-File_" + cntr + "=" + recentFiles.get(cntr)); // NOI18N
        }
        String[] openFiles = getOpenedFiles();

        for (int cntr = 0; cntr < openFiles.length; cntr++) {
            output.println("Open-File_" + cntr + "=" + openFiles[cntr]); // NOI18N
        }
    }

    private File getConfigurationFileName() {
        File homedir = new File( System.getProperty( "user.home" ) ).getAbsoluteFile();
        File configurationFile = new File(homedir, ".nb-editor"); // NOI18N

        return configurationFile;
    }

    private void writeUserConfiguration() {
        File configurationFile = getConfigurationFileName();
        File configurationFileBackup = new File(configurationFile.getAbsolutePath() + "~"); // NOI18N
        boolean backup = false;

        if (configurationFile.exists()) {
            backup = true;
            configurationFile.renameTo(configurationFileBackup);
        }

        PrintWriter output = null;
        try {
            output = new PrintWriter(new FileWriter(configurationFile));

            writeUserConfiguration(output);

            if (backup) {
                if (!output.checkError()) {
                    configurationFileBackup.delete();
                } else {
                    //Put back to original configuration:
                    configurationFileBackup.renameTo(configurationFile);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    private void readUserConfiguration() {
        File        configurationFileName = getConfigurationFileName();
        InputStream in = null;
        try {
            in = new FileInputStream(configurationFileName);
            readUserConfiguration(new PropertyResourceBundle(in));
        } catch (FileNotFoundException e) {
            //The file containing user-defined configuration not found.
            //This is nothing really important.
            try {
                System.err.println("User configuration not found in \"" + configurationFileName.getCanonicalPath() + "\".");
            } catch (IOException f) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator sep2;
    private javax.swing.JSeparator sep1;
    private javax.swing.JMenu newMenu;
    private javax.swing.JMenuItem saveAllItem;
    private javax.swing.JMenuItem closeItem;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem exitItem;
    private javax.swing.JTabbedPane tabPane;
    private javax.swing.JMenuItem saveAsItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem openItem;
    private javax.swing.JMenuItem saveItem;
    // End of variables declaration//GEN-END:variables

    
    private void readSettings() throws MissingResourceException {
        File currentPath = new File( System.getProperty( "user.dir" ) ).getAbsoluteFile();
        fileChooser = new JFileChooser( currentPath );
        
        fileChooser.setFileView(impl);
        
        String kits = settings.getString( "InstalledEditors" );
        String defaultKit = settings.getString( "DefaultEditor" );
        
        StringTokenizer st = new StringTokenizer( kits, "," ); // NOI18N
        while( st.hasMoreTokens() ) {
            String kitName = st.nextToken();
            // At the first, we have to read ALL info about kit
            String contentType = settings.getString( kitName + "_ContentType" );
            String extList = settings.getString( kitName + "_ExtensionList" );
            String menuTitle = settings.getString( kitName + "_NewMenuTitle" );
            char menuMnemonic = settings.getString( kitName + "_NewMenuMnemonic" ).charAt( 0 );
            String templateURL = settings.getString( kitName + "_Template" );
            String iconName = settings.getString( kitName + "_Icon" );
            String filterTitle = settings.getString( kitName + "_FileFilterTitle" );
            String kit = settings.getString( kitName + "_KitClass" );

            // At the second, we surely need an instance of kitClass
            Class kitClass;
            try {
                kitClass = Class.forName( kit );
            } catch( ClassNotFoundException exc ) { // we really need it
                throw new MissingResourceException( "Missing class", kit, "KitClass" ); // NOI18N
            }

            // At the third, it is nice to have icon although we could live without one
            Icon icon = null;
            ClassLoader loader = kitClass.getClassLoader();
            if( loader == null ) loader = ClassLoader.getSystemClassLoader();
            URL resource = loader.getResource( iconName );
            if( resource == null ) resource = ClassLoader.getSystemResource( iconName );
            if( resource != null ) icon = new ImageIcon( resource );

            // At the fourth, try to get URL for template
            URL template = loader.getResource( templateURL );
            if( resource == null ) template = ClassLoader.getSystemResource( templateURL );

            // Finally, convert the list of extensions to, ehm, List :-)
            List l = new ArrayList( 5 );
            StringTokenizer extST = new StringTokenizer( extList, "," ); // NOI18N
            while( extST.hasMoreTokens() ) l.add( extST.nextToken() );            
            
            // Actually create the KitInfo from provided informations
            KitInfo ki = new KitInfo( contentType, l, template, icon, filterTitle, kitClass, loader, defaultKit.equals( kitName ) );

            // Make the MenuItem for it
            JMenuItem item = new JMenuItem( menuTitle, icon );
            item.setMnemonic( menuMnemonic );
            item.putClientProperty( "kitInfo", ki ); // NOI18N
            item.addActionListener( impl );
	    newMenu.add( item );

            // Register a FileFilter for given type of file
            fileChooser.addChoosableFileFilter( ki );
        }
        
        // Finally, add fileFilter that would recognize files of all kits

        fileChooser.addChoosableFileFilter( new FileFilter() {
            public String getDescription() {
                return "All recognized files"; // NOI18N
            }
        
            public boolean accept( File f ) {
                return f.isDirectory() || KitInfo.getKitInfoForFile( f ) != null;
            }
        });
        
        if( KitInfo.getDefault() == null ) throw new MissingResourceException( "Missing default kit definition", defaultKit, "DefaultEditor" ); // NOI18N
    }
            
    private static final class KitInfo extends FileFilter{
        
        private static List kits = new ArrayList();
        private static KitInfo defaultKitInfo;
        
        public static List getKitList() {
            return new ArrayList( kits );
        }
        
        public static KitInfo getDefault() {
            return defaultKitInfo;
        }
        
        public static KitInfo getKitInfoOrDefault( File f ) {
            KitInfo ki = getKitInfoForFile( f );
            return ki == null ? defaultKitInfo : ki;
        }
        
        public static KitInfo getKitInfoForFile( File f ) {
            for( int i = 0; i < kits.size(); i++ ) {
                if( ((KitInfo)kits.get(i)).accept( f ) )
                    return (KitInfo)kits.get(i);
            }
            return null;
        }

        private String type;
        private String[] extensions;
        private URL template;
        private Icon icon;
        private Class kitClass;
        private String description;
        
        public KitInfo( String type, List exts, URL template, Icon icon, String description, Class kitClass, ClassLoader loader, boolean isDefault ) {
            // Fill in the structure
            this.type = type;
            this.extensions = (String[])exts.toArray( new String[0] );
            this.template = template;
            this.icon = icon;
            this.description = description;
            this.kitClass = kitClass;
            
            // Register us
            JEditorPane.registerEditorKitForContentType( type, kitClass.getName(), loader );
            kits.add( this );
            if( isDefault ) defaultKitInfo = this;
        }
        

        
        public String getType() {
            return type;
        }
        
        public String getDefaultExtension() {
            return extensions[0];
        }

        public URL getTemplate() {
            return template;
        }
        
        public Icon getIcon() {
            return icon;
        }

        public Class getKitClass() {
            return kitClass;
        }
                        
        public String getDescription() {
            return description;
        }
        
        public boolean accept( File f ) {
            if( f.isDirectory() ) return true;
            String fileName = f.getName();
            for( int i=0; i<extensions.length; i++ ) {
                if( fileName.endsWith( extensions[i] ) ) return true;
            }
            return false;
        }
    }   
    
    /** Listener listening for document changes on opened documents. There is
     * initially one instance per opened document, but this listener is
     * one-fire only - as soon as it gets fired, markes changes and removes
     * itself from document. On save, new Listener is hooked again.
     */
    private class MarkingDocumentListener implements DocumentListener {
        private Component comp;
        
        public MarkingDocumentListener( Component comp ) {
            this.comp = comp;
        }
        
        private void markChanged( DocumentEvent evt ) {
            Document doc = evt.getDocument();
            doc.putProperty( MODIFIED, Boolean.TRUE );
            
            File file = (File)doc.getProperty( FILE );
            int index = tabPane.indexOfComponent( comp );
            
            tabPane.setTitleAt( index, file.getName() + '*' );
            
            doc.removeDocumentListener( this );
        }
        
        public void changedUpdate( DocumentEvent e ) {
        }
        
        public void insertUpdate( DocumentEvent evt ) {
            markChanged( evt );
        }
        
        public void removeUpdate( DocumentEvent evt ) {
            markChanged( evt );
        }
    }
}
