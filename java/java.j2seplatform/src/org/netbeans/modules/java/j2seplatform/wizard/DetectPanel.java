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

package org.netbeans.modules.java.j2seplatform.wizard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.java.j2seplatform.platformdefinition.PlatformConvertor;
import org.netbeans.modules.java.j2seplatform.platformdefinition.Util;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.HelpCtx;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.SpecificationVersion;
import org.openide.util.ChangeSupport;
import org.openide.util.Utilities;

/**
 * This Panel launches autoconfiguration during the New J2SE Platform sequence.
 * The UI views properties of the platform, reacts to the end of detection by
 * updating itself. It triggers the detection task when the button is pressed.
 * The inner class WizardPanel acts as a controller, reacts to the UI completness
 * (jdk name filled in) and autoconfig result (passed successfully) - and manages
 * Next/Finish button (valid state) according to those.
 *
 * @author Svata Dedic
 */
public class DetectPanel extends javax.swing.JPanel {

    private enum PlatformState {
        UNKNOWN,
        VALID,
        INVALID,
        UNSUPPORTED
    }

    private static final int COLS = 30;
    private static final RequestProcessor RP = new RequestProcessor(DetectPanel.class.getName(), 1, false, false);

    private final ChangeSupport cs = new ChangeSupport(this);
            
    private static final String INNER_SEPARATOR = "!/";     //NOI18N
    private static final String PATH_SEPARATOR = ";";       //NOI18N

    /**
     * Creates a detect panel
     * start the task and update on its completion
     */
    public DetectPanel() {
        initComponents();
        postInitComponents ();
        putClientProperty(WizardDescriptor.PROP_CONTENT_DATA,
            new String[] {
                NbBundle.getMessage(DetectPanel.class,"TITLE_PlatformName"),
        });
        this.setName (NbBundle.getMessage(DetectPanel.class,"TITLE_PlatformName"));
    }

    @Override
    public void addNotify() {
        super.addNotify();        
    }    

    private void postInitComponents () {        
        final DocumentListener docListener = new DocumentListener () {
            @Override
            public void insertUpdate(DocumentEvent e) {
                cs.fireChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                cs.fireChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                cs.fireChange();
            }
        };
        this.jdkName.getDocument().addDocumentListener(docListener);
        this.javadoc.getDocument().addDocumentListener(docListener);
        this.sources.getDocument().addDocumentListener(docListener);
        this.progressLabel.setVisible(false);
        this.progressPanel.setVisible(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel3 = new javax.swing.JLabel();
        jdkName = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        javadoc = new javax.swing.JTextField();
        sources = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        progressLabel = new javax.swing.JLabel();
        progressPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jLabel3.setLabelFor(jdkName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, NbBundle.getBundle(DetectPanel.class).getString("LBL_DetailsPanel_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(jdkName, gridBagConstraints);
        jdkName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(DetectPanel.class).getString("AD_PlatformName")); // NOI18N

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(sources);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getBundle(DetectPanel.class).getString("TXT_Sources")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(jLabel1, gridBagConstraints);

        jLabel4.setLabelFor(javadoc);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, NbBundle.getBundle(DetectPanel.class).getString("TXT_JavaDoc")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        jPanel1.add(jLabel4, gridBagConstraints);

        javadoc.setColumns(COLS);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        jPanel1.add(javadoc, gridBagConstraints);
        javadoc.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(DetectPanel.class).getString("AD_PlatformJavadoc")); // NOI18N

        sources.setColumns(COLS);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(sources, gridBagConstraints);
        sources.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(DetectPanel.class).getString("AD_PlatformSources")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getBundle(DetectPanel.class).getString("LBL_BrowseSources")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectSources(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(jButton1, gridBagConstraints);
        jButton1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(DetectPanel.class).getString("AD_SelectSources")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getBundle(DetectPanel.class).getString("LBL_BrowseJavadoc")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectJavadoc(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        jPanel1.add(jButton2, gridBagConstraints);
        jButton2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(DetectPanel.class).getString("AD_SelectJavadoc")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(jPanel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel2, gridBagConstraints);

        progressLabel.setLabelFor(progressPanel);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seplatform/wizard/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(progressLabel, bundle.getString("TXT_PlatfromDetectProgress")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 6, 0);
        add(progressLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(progressPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(DetectPanel.class).getString("AD_DetectPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void selectJavadoc(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectJavadoc
        String newValue = this.browse(this.javadoc.getText(),NbBundle.getMessage(DetectPanel.class,"TXT_SelectJavadoc"));
        if (newValue != null) {
            this.javadoc.setText(newValue);
        }
        
    }//GEN-LAST:event_selectJavadoc

    private void selectSources(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectSources
        String newValue = this.browse(this.sources.getText(),NbBundle.getMessage(DetectPanel.class,"TXT_SelectSources"));
        if (newValue != null) {
            this.sources.setText(newValue);
        }
    }//GEN-LAST:event_selectSources
    
    public final synchronized void addChangeListener (ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    public final synchronized void removeChangeListener (ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

    public String getPlatformName() {
	    return jdkName.getText().trim();
    }
    
    String getSources () {
        return sources.getText();
    }

    void setSources (String sources) {
        this.sources.setText (sources == null ? "" : sources);      //NOI18N
    }

    String getJavadoc () {
        return this.javadoc.getText();
    }

    void setJavadoc (@NonNull final String jdoc) {
        this.javadoc.setText(jdoc);
    }

    /**
     * Updates name of the platform from NewJ2SEPlatform
     * threading: Has to be called in the EDT
     */
    private void updatePlatformName(@NonNull final NewJ2SEPlatform platform, String name) {
        assert platform != null;
        assert SwingUtilities.isEventDispatchThread();
        final Map<String,String> m = platform.getSystemProperties();
        if ("".equals(jdkName.getText())) { //NOI18N
            if (name != null) {
                jdkName.setText(name);
            } else {
                jdkName.setText(getInitialName (m));
            }
            jdkName.selectAll();
        }
    }


    private static String getInitialName (Map<String,String> m) {        
        String vmVersion = m.get("java.specification.version");        //NOI18N
        StringBuilder result = new StringBuilder(NbBundle.getMessage(DetectPanel.class,"TXT_DetectPanel_Java"));        
        if (vmVersion != null) {
            result.append (vmVersion);
        }
        return result.toString();
    }
    
    
    private String browse (String oldValue, String title) {
        JFileChooser chooser = new JFileChooser ();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (Utilities.isMac()) {
            //New JDKs and JREs are bundled into package, allow JFileChooser to navigate in
            chooser.putClientProperty("JFileChooser.packageIsTraversable", "always");   //NOI18N
        }
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter (new FileFilter () {
            @Override
            public boolean accept(File f) {
                return (f.exists() && f.canRead() && (f.isDirectory() || (f.getName().endsWith(".zip") || f.getName().endsWith(".jar"))));
            }

            @Override
            public String getDescription() {
                return NbBundle.getMessage(DetectPanel.class,"TXT_ZipFilter");
            }
        });
        File f = new File (oldValue);
        chooser.setSelectedFile(f);
        chooser.setDialogTitle (title);
        if (chooser.showOpenDialog (this) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField javadoc;
    private javax.swing.JTextField jdkName;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JPanel progressPanel;
    private javax.swing.JTextField sources;
    // End of variables declaration//GEN-END:variables

    /**
     * Controller for the outer class: manages wizard panel's valid state
     * according to the user's input and detection state.
     */
    static class WizardPanel implements WizardDescriptor.AsynchronousValidatingPanel<WizardDescriptor>, TaskListener, ChangeListener {

        private DetectPanel         component;
        private RequestProcessor.Task task;
        private final J2SEWizardIterator  iterator;
        private final ChangeSupport cs = new ChangeSupport(this);
        private final AtomicReference<PlatformState> detected = new AtomicReference<PlatformState>(PlatformState.UNKNOWN);
        private volatile boolean             valid;        
        private boolean             firstPass=true;
        private WizardDescriptor    wiz;
        private ProgressHandle      progressHandle;
        
        private volatile String sourcesString;
        private volatile List<PathResourceImplementation> sources;
        private volatile String javadocString;
        private volatile List<URL> javadoc;

        WizardPanel(J2SEWizardIterator iterator) {            
	    this.iterator = iterator;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        @Override
        public java.awt.Component getComponent() {
            if (component == null) {
                final NewJ2SEPlatform primaryPlatform = this.iterator.getPlatform();
                final NewJ2SEPlatform secondaryPlatform = this.iterator.getSecondaryPlatform();
                component = new DetectPanel();
                component.addChangeListener (this);
                task = RP.create(
                    new Runnable() {
                        @Override
                        public void run() {
                            primaryPlatform.run();
                            if (secondaryPlatform != null) {
                                secondaryPlatform.run();
                            }
                        }
                });
                task.addTaskListener(this);
            }
            return component;
        }

        void setValid(boolean v) {
            if (v == valid)
                return;
            valid = v;
            cs.fireChange();
        }

        @Override
        public HelpCtx getHelp() {
            return new HelpCtx (DetectPanel.class);
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        @Override
        public void readSettings(WizardDescriptor settings) {           
            sources = null;
            sourcesString = null;
            javadoc = null;
            javadocString = null;
            this.wiz = settings;
            this.component.progressPanel.setVisible (true);
            this.component.progressLabel.setVisible (true);
            
            this.progressHandle = ProgressHandle.createHandle(NbBundle.getMessage(DetectPanel.class,"TXT_PlatfromDetectProgress"));
            this.component.progressPanel.removeAll();
            this.component.progressPanel.setLayout (new GridBagLayout ());
            GridBagConstraints c = new GridBagConstraints ();
            c.gridx = c.gridy = GridBagConstraints.RELATIVE;
            c.gridheight = c.gridwidth = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            JComponent pc = ProgressHandleFactory.createProgressComponent(this.progressHandle);
            ((GridBagLayout)this.component.progressPanel.getLayout ()).setConstraints(pc,c);
            this.component.progressPanel.add (pc);
            this.progressHandle.start ();
            task.schedule(0);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

	/**
	 Updates the Platform's display name with the one the user
	 has entered. Stores user-customized display name into the Platform.
	 */
        @Override
        public void storeSettings(WizardDescriptor settings) {
            final List<? extends PathResourceImplementation> src = sources;
            final List<URL> jdoc = javadoc;
            if (isValid() && src != null && jdoc != null) {
                String name = component.getPlatformName();
                NewJ2SEPlatform platform = this.iterator.getPlatform();
                platform.setDisplayName (name);
                platform.setAntName (PlatformConvertor.getFreeAntName (name));
                platform.setSourceFolders (ClassPathSupport.createClassPath(src));
                if (!jdoc.isEmpty()) {
                    platform.setJavadocFolders(jdoc);
                }

                platform = this.iterator.getSecondaryPlatform();
                if (platform != null) {
                    name = NbBundle.getMessage(DetectPanel.class,"FMT_64BIT", name);
                    platform.setDisplayName (name);
                    platform.setAntName (PlatformConvertor.getFreeAntName(name));
                    platform.setSourceFolders (ClassPathSupport.createClassPath(src));
                    if (!jdoc.isEmpty()) {
                        platform.setJavadocFolders(jdoc);
                    }
                }
            }
        }


        @Override
        public void prepareValidation() {
            sourcesString = component.getSources();
            javadocString = component.getJavadoc();
        }

        @Override
        public void validate() throws WizardValidationException {
            assert valid;
            try {
                sources = stringToSourcePath(sourcesString);
            } catch (IllegalStateException ise) {
                throw new WizardValidationException(
                        component,
                        "ERROR_Sources",    //NOI18N
                        NbBundle.getMessage(DetectPanel.class,"ERROR_Sources"));
            }
            try {
                javadoc = stringToJavadoc(javadocString);
            } catch (IllegalStateException ise) {
                throw new WizardValidationException(
                        component,
                        "ERROR_Javadoc",    //NOI18N
                        NbBundle.getMessage(DetectPanel.class,"ERROR_Javadoc"));
            }
        }

        /**
         * Revalidates the Wizard Panel
         */
        @Override
        public void taskFinished(Task task) {
            final NewJ2SEPlatform platform = iterator.getPlatform();
            List<URL> jdoc = platform.getJavadocFolders();
            if (jdoc.isEmpty()) {
                jdoc = platform.defaultJavadoc();
            }
            final String jdocStr = urlsToString(jdoc);
            List<URL> src = cpToUrls(platform.getSourceFolders());
            if (src.isEmpty()) {
                src = platform.defaultSources();
            }
            final String srcStr = urlsToString(src);
            detected.set(
                platform.isValid() ?
                    (isSupported(platform.getSpecification().getVersion()) ? PlatformState.VALID : PlatformState.UNSUPPORTED):
                    PlatformState.INVALID);
            Object attr = iterator.getInstallFolder()
                    .getAttribute(NewJ2SEPlatform.DISPLAY_NAME_FILE_ATTR);
            final String name = (attr instanceof String) ? (String) attr : null;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run () {
                    component.updatePlatformName(platform, name);
                    component.setJavadoc(jdocStr);
                    component.setSources(srcStr);
                    assert progressHandle != null;
                    progressHandle.finish ();
                    component.progressPanel.setVisible (false);
                    component.progressLabel.setVisible (false);
                    checkValid ();
                }
            });
        }


        @Override
        public void stateChanged(ChangeEvent e) {
             this.checkValid();
        }

        private void checkValid () {
            this.wiz.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE, ""); //NOI18N
            final String name = this.component.getPlatformName ();
            boolean vld;
            switch (detected.get()) {
                case INVALID:
                    this.wiz.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE,NbBundle.getMessage(DetectPanel.class,"ERROR_NoSDKRegistry"));         //NOI18N
                    vld = false;
                    break;
                case UNKNOWN:
                    vld = false;
                    break;
                case VALID:
                    vld = true;
                    break;
                case UNSUPPORTED:
                    this.wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(DetectPanel.class,"ERROR_UnsupportedPlatform", SourceLevelQuery.MINIMAL_SOURCE_LEVEL));         //NOI18N
                    vld = false;
                    break;
                default:
                    throw new IllegalStateException();
            }
            if (vld && name.length() == 0) {
                vld = false;
                this.wiz.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE,NbBundle.getMessage(DetectPanel.class,"ERROR_InvalidDisplayName"));    //NOI18N
            }
            if (vld) {
                for (JavaPlatform platform : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
                    if (name.equals (platform.getDisplayName())) {
                        vld = false;
                        this.wiz.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE,NbBundle.getMessage(DetectPanel.class,"ERROR_UsedDisplayName"));    //NOI18N
                        break;
                    }
                }
            }
            setValid(vld);
        }

        @NonNull
        private static List<PathResourceImplementation> stringToSourcePath(@NonNull final String path) {
            assert path != null;
            final List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>();
            final StringTokenizer tk = new StringTokenizer(path,PATH_SEPARATOR);
            while (tk.hasMoreTokens()) {
                try {
                    final String token =  tk.nextToken().trim();
                    if (!token.isEmpty()) {

                        int index = token.lastIndexOf(INNER_SEPARATOR);
                        if (index > 0) {
                            final String outherPath = token.substring(0, index);
                            final String innerPath = index+2 == token.length() ? "" : token.substring(index+2);
                            final File f = new File (outherPath);
                            if (f.exists()) {
                                result.add (ClassPathSupport.createResource(
                                    new URL (FileUtil.getArchiveRoot(Utilities.toURI(f).toURL()).toExternalForm() + innerPath)));
                            } else {
                                throw new IllegalStateException();
                            }
                        } else {
                            final File f = new File(token);
                            if (f.exists()) {
                                try {
                                    URL url = Utilities.toURI(f).toURL();
                                    if (FileUtil.isArchiveFile(url)) {
                                        url = FileUtil.getArchiveRoot(url);
                                        FileObject fo = URLMapper.findFileObject(url);
                                        if (fo != null) {
                                            fo = fo.getFileObject("src");   //NOI18N
                                            if (fo != null) {
                                                url = fo.toURL();
                                            }
                                        }
                                        result.add (ClassPathSupport.createResource(url));
                                    } else {
                                        result.add (ClassPathSupport.createResource(url));
                                    }
                                } catch (IllegalArgumentException | MalformedURLException mue) {
                                    throw new IllegalStateException(mue);
                                }
                            } else {
                                throw new IllegalStateException();
                            }
                        }
                    }
                } catch (MalformedURLException e) {
                    throw new IllegalStateException(e);
                }
            }
            return result;
        }
        
        @NonNull
        private static List<URL> stringToJavadoc(@NonNull final String path) {
            assert path != null;
            final List<URL> result = new ArrayList<URL>();
            final StringTokenizer tk = new StringTokenizer(path,PATH_SEPARATOR);
            while (tk.hasMoreTokens()) {
                try {
                    final String token =  tk.nextToken().trim();
                    if (token.startsWith(Util.PROTO_HTTP) || token.startsWith(Util.PROTO_HTTPS)) {
                        //Http(s) URL add directly
                        result.add(new URI(token).toURL());
                    } else {
                        //File or folder
                        //1st) /jdk/docs/
                        //2nd) /jdk/docs.zip
                        //3rd) /jdk/docs.zip!/docs/api/
                        int index = token.lastIndexOf(INNER_SEPARATOR);
                        if (index > 0) {
                            final String outherPath = token.substring(0, index);
                            final String innerPath = index+2 == token.length() ? "" : token.substring(index+2);
                            final File f = new File (outherPath);
                            if (f.exists()) {
                                result.add (new URL (FileUtil.getArchiveRoot(Utilities.toURI(f).toURL()).toExternalForm() + innerPath));
                            } else {
                                throw new IllegalStateException();
                            }
                        } else {
                            final File f = new File(token);
                            final URL url = FileUtil.urlForArchiveOrDir(f);
                            if (url != null) {
                                result.add(url);
                            } else {
                                throw new IllegalStateException();
                            }
                        }
                    }
                } catch (MalformedURLException | URISyntaxException e) {
                    throw new IllegalStateException(e);
                }
            }
            return result;
        }

        @NonNull
        private static List<URL> cpToUrls(@NonNull final ClassPath cp) {
            final List<URL> result = new ArrayList<>();
            for (ClassPath.Entry entry : cp.entries()) {
                result.add(entry.getURL());
            }
            return result;
        }
        
        @NonNull
        private static String urlsToString(@NonNull final List<URL> path) {
            final StringBuilder result = new StringBuilder();
            for (final URL jdocRoot : path) {
                try {
                    final String extUrl = jdocRoot.toExternalForm(); 
                    URL url = FileUtil.getArchiveFile(jdocRoot);
                    String relative;
                    String userName;
                    if (url == null) {
                        url = jdocRoot;
                        relative = "";  //NOI18N
                    } else {                
                        int index = extUrl.lastIndexOf(INNER_SEPARATOR);
                        relative = index < 0 ? "" : extUrl.substring(index+INNER_SEPARATOR.length());    //NOI18N
                    }
                    final String protocol = url.getProtocol();
                    if (Util.PROTO_FILE.equals(protocol)) {
                        final String absolutePath = Utilities.toFile(url.toURI()).getAbsolutePath();
                        final StringBuilder sb = new StringBuilder(absolutePath.length() + INNER_SEPARATOR.length() + relative.length());
                        sb.append(absolutePath);
                        if (!relative.isEmpty()) {
                            sb.append(INNER_SEPARATOR);
                            sb.append(relative);
                        }
                        userName = sb.toString();
                    } else if (Util.isRemoteProtocol(protocol)) {
                        userName = extUrl;
                    } else {
                        //Other protocols are unsupported
                        continue;
                    }
                    if (result.length() > 0) {
                        result.append(PATH_SEPARATOR);
                    }
                    result.append(userName);
                } catch (URISyntaxException e) {
                    Exceptions.printStackTrace(e);
                }
            }
            return result.toString();
        }

        private static boolean isSupported(@NonNull final SpecificationVersion specVersion) {
            return SourceLevelQuery.MINIMAL_SOURCE_LEVEL.compareTo(specVersion) <= 0;
        }
    }
}
