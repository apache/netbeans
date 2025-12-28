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

package org.netbeans.modules.javadoc.search;

import java.awt.EventQueue;
import java.io.Externalizable;
import java.io.ObjectStreamException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.javadoc.settings.DocumentationSettings;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor.Task;
import org.openide.windows.TopComponent;
import org.openide.util.RequestProcessor;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Main window for documentation index search
 *
 * @author Petr Hrebejk, Petr Suchomel
 */
public final class IndexSearch
            extends TopComponent
    implements Externalizable {

    private static final String INDEX_SEARCH_HELP_CTX_KEY = "javadoc.search.window"; //NOI18N
            
    private static final java.awt.Dimension PREFFERED_SIZE = new java.awt.Dimension( 580, 430 );
    static Logger LOG = Logger.getLogger(IndexSearch.class.getName());

    static final long serialVersionUID =1200348578933093459L;

    /** The only instance allowed in system */
    private static Reference<IndexSearch> refIndexSearch;
    
    /** cache of previously searched strings */
    private static Object[] MRU = new Object[0];
    private static final RequestProcessor RP = new RequestProcessor(IndexSearch.class.getName(), 1, false, false);

    /** Search engine */
    private final SearchTask searchTask = new SearchTask(this);
    /** search button state */
    private boolean stopState = false;

    /** The state of the window is stored in hidden options of DocumentationSettings */
    private DocumentationSettings ds = DocumentationSettings.getDefault();

    private String quickFind;

    /* Current sort mode */
    private String currentSort = "A"; // NOI18N

    /* Hand made components */
    private javax.swing.JScrollPane resultsScrollPane;
    private javax.swing.JList resultsList;
    private JSplitPane splitPanel;

    /** List models for different sorts */
    private List<DocIndexItem> results = new ArrayList<DocIndexItem>();

    private DefaultListModel referenceModel = null;
    private DefaultListModel typeModel = null;
    private DefaultListModel alphaModel = null;

    /* Holds split position if the quick view is disabled */
    private int oldSplit = DocumentationSettings.getDefault().getIdxSearchSplit();

    private final DefaultListModel<DocIndexItem> waitModel = new DefaultListModel<>();
    private final DefaultListModel<DocIndexItem> initModel = new DefaultListModel<>();
    private final DefaultListModel<DocIndexItem> notModel  = new DefaultListModel<>();
    private boolean setDividerLocation;

    /** Initializes the Form */
    public IndexSearch() {
        ResourceBundle b = NbBundle.getBundle(IndexSearch.class);
        DocIndexItem dii = new DocIndexItem( b.getString("CTL_SEARCH_Wait" ), "", null, "" );    //NOI18N
        dii.setIconIndex( DocSearchIcons.ICON_WAIT );
        waitModel.addElement( dii );

        dii = new DocIndexItem( b.getString("CTL_SEARCH_InitRoots"), "", null, "" );    //NOI18N
        dii.setIconIndex( DocSearchIcons.ICON_WAIT );
        initModel.addElement( dii );

        DocIndexItem diin = new DocIndexItem( b.getString("CTL_SEARCH_NotFound" ), "", null, "" );   //NOI18N
        diin.setIconIndex( DocSearchIcons.ICON_NOT_FOUND );
        notModel.addElement( diin );
        
        initComponents ();
        
        // Force winsys to not show tab when this comp is alone                                                                                                                 
        putClientProperty("TabPolicy", "HideWhenAlone"); // NOI18N

        javax.swing.ComboBoxEditor editor = searchComboBox.getEditor();
        editor.addActionListener (new java.awt.event.ActionListener () {
                                      public void actionPerformed (java.awt.event.ActionEvent evt) {
                                          searchButtonActionPerformed( evt );
                                      }
                                  }
                                 );

        // Split panel
        splitPanel = new JSplitPane (JSplitPane.VERTICAL_SPLIT);
        splitPanel.setPreferredSize(PREFFERED_SIZE);
        
        //splitPanel.setDividerLocation(oldSplit / 100.0);
        //previous line does not work
        //setDividerLocation must be set in open
        setDividerLocation = true;
        

        java.awt.GridBagConstraints gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets (5, 0, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add (splitPanel, gridBagConstraints1);

        // Results - SrollPane & JList
        resultsScrollPane = new javax.swing.JScrollPane ();

        resultsList = new javax.swing.JList ();
        resultsList.setSelectionMode (javax.swing.ListSelectionModel.SINGLE_SELECTION );
        resultsList.addKeyListener (new java.awt.event.KeyAdapter () {
                                        @Override
                                        public void keyPressed (java.awt.event.KeyEvent evt) {
                                            resultsListKeyPressed (evt);
                                        }
                                    }
                                   );
        resultsList.addMouseListener (new java.awt.event.MouseAdapter () {
                                          @Override
                                          public void mouseClicked (java.awt.event.MouseEvent evt) {
                                              resultsListMouseClicked (evt);
                                          }
                                      }
                                     );

        resultsScrollPane.setViewportView (resultsList);

        splitPanel.setTopComponent(resultsScrollPane);

        splitPanel.setBottomComponent(createBrowser());

        DefaultListModel listModel = new DefaultListModel(); // PENDING: Change to SortedArrayList
        resultsList.setModel( listModel );

        IndexListCellRenderer cr = new IndexListCellRenderer();
        resultsList.setCellRenderer( cr );

        resultsList.getSelectionModel().addListSelectionListener(
            new javax.swing.event.ListSelectionListener() {
                public void valueChanged( javax.swing.event.ListSelectionEvent evt ) {
                    if (!evt.getValueIsAdjusting()) {
                        showHelp();
                    }
                }
            });
        resultsScrollPane.validate();


        
        sourceButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/javadoc/resources/showSource.gif", false)); // NOI18N
        byReferenceButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/javadoc/resources/refSort.gif", false)); // NOI18N
        byTypeButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/javadoc/resources/typeSort.gif", false)); // NOI18N
        byNameButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/javadoc/resources/alphaSort.gif", false)); // NOI18N
        quickViewButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/javadoc/resources/list_only.gif", false)); // NOI18N
        quickViewButton.setSelectedIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/javadoc/resources/list_html.gif", false)); // NOI18N

        javax.swing.ButtonGroup bg = new javax.swing.ButtonGroup();
        bg.add( byReferenceButton );
        bg.add( byTypeButton );
        bg.add( byNameButton );

        // Adding ToolTips

        searchButton.setToolTipText(b.getString( "CTL_SEARCH_search_ToolTip" ));    //NOI18N
        byReferenceButton.setToolTipText(b.getString( "CTL_SEARCH_byReference_ToolTip" ));   //NOI18N
        byTypeButton.setToolTipText(b.getString( "CTL_SEARCH_byType_ToolTip" ));   //NOI18N
        byNameButton.setToolTipText(b.getString( "CTL_SEARCH_byName_ToolTip" ));   //NOI18N
        quickViewButton.setToolTipText(b.getString( "CTL_SEARCH_quickView_ToolTip" ));   //NOI18N
        sourceButton.setToolTipText(b.getString( "CTL_SEARCH_showSource_ToolTip" ));   //NOI18N
        searchComboBox.setToolTipText(b.getString( "ACS_SEARCH_SearchComboBoxA11yDesc" ));   //NOI18N
        resultsList.setToolTipText(b.getString( "ACS_SEARCH_ResultsListA11yDesc" ));   //NOI18N
        
        // Adding mnemonics
        if (!Utilities.isMac()) {
            byReferenceButton.setMnemonic(b.getString("CTL_SEARCH_byReference_Mnemonic").charAt(0));  // NOI18N
            byTypeButton.setMnemonic(b.getString("CTL_SEARCH_byType_Mnemonic").charAt(0));  // NOI18N
            byNameButton.setMnemonic(b.getString("CTL_SEARCH_byName_Mnemonic").charAt(0));  // NOI18N
            quickViewButton.setMnemonic(b.getString("CTL_SEARCH_quickView_Mnemonic").charAt(0));  // NOI18N
            sourceButton.setMnemonic(b.getString("CTL_SEARCH_showSource_Mnemonic").charAt(0));  // NOI18N
        }
        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(IndexSearch.class,"CTL_SEARCH_ButtonFind"));
        stopState = false;
        Mnemonics.setLocalizedText(helpButton, NbBundle.getMessage(IndexSearch.class,"CTL_SEARCH_ButtonHelp"));
        
        initAccessibility();
        resolveButtonState();
    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    @Override
    protected String preferredID() {
        return "JavaDocIndexSearch"; // NOI18N
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(INDEX_SEARCH_HELP_CTX_KEY);
    }
    
    private void initAccessibility() {
        ResourceBundle b = NbBundle.getBundle(IndexSearch.class);
        getAccessibleContext().setAccessibleName(b.getString("ACS_SEARCH_PanelA11yName"));  // NOI18N
        getAccessibleContext().setAccessibleDescription(b.getString("ACS_SEARCH_PanelA11yDesc"));  // NOI18N
        searchComboBox.getAccessibleContext().setAccessibleName(b.getString("ACS_SEARCH_SearchComboBoxA11yName"));  // NOI18N
        searchComboBox.getAccessibleContext().setAccessibleDescription(b.getString("ACS_SEARCH_SearchComboBoxA11yDesc")); // NOI18N
        resultsList.getAccessibleContext().setAccessibleName(b.getString("ACS_SEARCH_ResultsListA11yName"));  // NOI18N
        resultsList.getAccessibleContext().setAccessibleDescription(b.getString("ACS_SEARCH_ResultsListA11yDesc")); // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        searchComboBox = new javax.swing.JComboBox(MRU);
        searchButton = new javax.swing.JButton();
        sourceButton = new javax.swing.JButton();
        byNameButton = new javax.swing.JToggleButton();
        byReferenceButton = new javax.swing.JToggleButton();
        byTypeButton = new javax.swing.JToggleButton();
        quickViewButton = new javax.swing.JToggleButton();
        helpButton = new javax.swing.JButton();

        FormListener formListener = new FormListener();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        searchComboBox.setEditable(true);
        searchComboBox.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        jPanel1.add(searchComboBox, gridBagConstraints);

        searchButton.setText("Search");
        searchButton.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel1.add(searchButton, gridBagConstraints);

        sourceButton.setPreferredSize(new java.awt.Dimension(25, 25));
        sourceButton.setMinimumSize(new java.awt.Dimension(25, 25));
        sourceButton.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel1.add(sourceButton, gridBagConstraints);

        byNameButton.setSelected(true);
        byNameButton.setPreferredSize(new java.awt.Dimension(25, 25));
        byNameButton.setActionCommand("A");
        byNameButton.setMinimumSize(new java.awt.Dimension(25, 25));
        byNameButton.setRequestFocusEnabled(false);
        byNameButton.addActionListener(formListener);

        jPanel1.add(byNameButton, new java.awt.GridBagConstraints());

        byReferenceButton.setPreferredSize(new java.awt.Dimension(25, 25));
        byReferenceButton.setActionCommand("R");
        byReferenceButton.setMinimumSize(new java.awt.Dimension(25, 25));
        byReferenceButton.setRequestFocusEnabled(false);
        byReferenceButton.addActionListener(formListener);

        jPanel1.add(byReferenceButton, new java.awt.GridBagConstraints());

        byTypeButton.setPreferredSize(new java.awt.Dimension(25, 25));
        byTypeButton.setActionCommand("T");
        byTypeButton.setMinimumSize(new java.awt.Dimension(25, 25));
        byTypeButton.setRequestFocusEnabled(false);
        byTypeButton.addActionListener(formListener);

        jPanel1.add(byTypeButton, new java.awt.GridBagConstraints());

        quickViewButton.setSelected(true);
        quickViewButton.setRequestFocusEnabled(false);
        quickViewButton.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(quickViewButton, gridBagConstraints);

        helpButton.setToolTipText(org.openide.util.NbBundle.getBundle(IndexSearch.class).getString("CTL_SEARCH_ButtonHelp_tooltip"));
        helpButton.setText("Help");
        helpButton.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(helpButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jPanel1, gridBagConstraints);

    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == searchComboBox) {
                IndexSearch.this.searchComboBoxActionPerformed(evt);
            }
            else if (evt.getSource() == searchButton) {
                IndexSearch.this.searchButtonActionPerformed(evt);
            }
            else if (evt.getSource() == sourceButton) {
                IndexSearch.this.showSource(evt);
            }
            else if (evt.getSource() == byNameButton) {
                IndexSearch.this.sortButtonActionPerformed(evt);
            }
            else if (evt.getSource() == byReferenceButton) {
                IndexSearch.this.sortButtonActionPerformed(evt);
            }
            else if (evt.getSource() == byTypeButton) {
                IndexSearch.this.sortButtonActionPerformed(evt);
            }
            else if (evt.getSource() == quickViewButton) {
                IndexSearch.this.quickViewButtonActionPerformed(evt);
            }
            else if (evt.getSource() == helpButton) {
                IndexSearch.this.helpButtonActionPerformed(evt);
            }
        }
    }
    // </editor-fold>//GEN-END:initComponents

    private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonActionPerformed
        getHelpCtx().display();
    }//GEN-LAST:event_helpButtonActionPerformed

    private void showSource (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showSource
        showSource();
    }//GEN-LAST:event_showSource

    private void sortButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortButtonActionPerformed

        currentSort = evt.getActionCommand();
        ds.setIdxSearchSort( currentSort );
        sortResults();

    }//GEN-LAST:event_sortButtonActionPerformed

    private void quickViewButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quickViewButtonActionPerformed
        if ( quickViewButton.isSelected() ) {
            splitPanel.setDividerLocation( oldSplit == 100 ? 0.5 : oldSplit / 100.0 );
            ds.setIdxSearchSplit( oldSplit == 100 ? 50 : oldSplit );
            ds.setIdxSearchNoHtml( false );
            showHelp();
        }
        else {
            oldSplit = (int) (splitPanel.getDividerLocation() / splitPanel.getSize().getHeight() * 100);
            splitPanel.setDividerLocation( 1.0 );
            ds.setIdxSearchSplit( 100 );
            ds.setIdxSearchNoHtml( true );
        }
    }//GEN-LAST:event_quickViewButtonActionPerformed

    private void resultsListKeyPressed (java.awt.event.KeyEvent evt) {//GEN-FIRST:event_resultsListKeyPressed
        // Add your handling code here:
        if ( evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER ||
                evt.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE ) {
            /*
            if ( evt.isShiftDown() ) {
              showSource();      
              evt.consume();
        }
            else
            */
            showHelpExternal();
        }
    }//GEN-LAST:event_resultsListKeyPressed

    private void resultsListMouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resultsListMouseClicked
        // Add your handling code here:
        if ( evt.getClickCount() == 2 ) {
            if ( evt.isShiftDown() ) {
                showSource();
                evt.consume();
            }
            else
                showHelpExternal();
        }
    }//GEN-LAST:event_resultsListMouseClicked

    private Object loadingToken;
    private RequestProcessor.Task task=null;
    /** Invokes the browser with help */
    private void showHelp() {

        loadingToken = new Object();
        if( null != task ) {
            task.cancel();
        }
        task = RP.create( new Runnable() {
            public void run() {
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        doShowHelp( loadingToken );
                    }
                });
            }
        });
        task.schedule(150);
    }

    private final Object LOCK = new Object();
    private void doShowHelp( Object token ) {
        if( token != loadingToken )
            return;

        synchronized( LOCK ) {
            if (splitPanel.getDividerLocation() == 100 )
                return;

            if (  resultsList.getMinSelectionIndex() < 0 )
                return;


            DocIndexItem  dii = (DocIndexItem)resultsList.getModel().getElementAt( resultsList.getMinSelectionIndex() );

            try {
                URL url = dii.getURL();

                if ( url == null )
                    return;

                // Workaround for bug in FileSystems
                String strUrl = url.toString();

                if ( strUrl.startsWith( "nbfs:" ) && strUrl.charAt( 5 ) != '/' ){ // NOI18N
                    url = new URL( "nbfs:/" + strUrl.substring( 5 ) ); // NOI18N
                }

                HtmlBrowser browser = createBrowser();
                browser.setURL(url);
                int splitPosition = splitPanel.getDividerLocation();
                if( token != loadingToken )
                    return;
                splitPanel.setBottomComponent(browser);
                splitPanel.setDividerLocation(splitPosition);
            }
            catch ( java.net.MalformedURLException ex ) {
                // Do nothing if the URL isn't O.K.
            }
        }
    }

        /** Invokes the browser with help */
    private void showHelpExternal() {

        if (  resultsList.getMinSelectionIndex() < 0 )
            return;


        DocIndexItem  dii = (DocIndexItem)resultsList.getModel().getElementAt( resultsList.getMinSelectionIndex() );

        try {
            URL url = dii.getURL();

            if ( url == null )
                return;

            // Workaround for bug in FileSystems
            String strUrl = url.toString();

            if ( strUrl.startsWith( "nbfs:" ) && strUrl.charAt( 5 ) != '/' ){ // NOI18N
                url = new URL( "nbfs:/" + strUrl.substring( 5 ) ); // NOI18N
            }

            HtmlBrowser.URLDisplayer.getDefault().showURL( url );
        }
        catch ( java.net.MalformedURLException ex ) {
            // Do nothing if the URL isn't O.K.
        }
    }

    /** Tryies to find source code for the selected item in repository. If the
     * is foun opens the source 
     */
    private void showSource( ) {

        if ( resultsList.getMinSelectionIndex() < 0 ) {
            return;
        }

        DocIndexItem  dii = (DocIndexItem)resultsList.getModel().getElementAt( resultsList.getMinSelectionIndex() );

        try {
            if (dii.getURL() == null) {
                return;
            }
            Object[] e = SrcFinder.findSource( dii.getPackage(), dii.getURL() );

            if ( e != null ) {
                FileObject toOpen = (FileObject) e[0];
                ElementHandle eh = (ElementHandle) e[1];
                ElementOpen.open(toOpen, eh);
            }
            else {
                NotifyDescriptor.Message nd = new NotifyDescriptor.Message( NbBundle.getMessage(IndexSearch.class, "MSG_SEARCH_SrcNotFound" ) );   //NOI18N
                DialogDisplayer.getDefault().notify( nd );
            }

        }
        catch ( java.net.MalformedURLException e ) {
            //System.out.println( e  );
        }
    }

    private void searchComboBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchComboBoxActionPerformed
        /*if ( searchEngine == null )
          searchButtonActionPerformed( evt );*/
    }//GEN-LAST:event_searchComboBoxActionPerformed

    private void searchButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        if (evt != null && stopState) {
            searchTask.stopSearch();
        } else if ( searchComboBox.getEditor().getItem().toString() != null &&
                    searchComboBox.getEditor().getItem().toString().length() > 0 ) {
            go();
        }
    }//GEN-LAST:event_searchButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton byNameButton;
    private javax.swing.JToggleButton byReferenceButton;
    private javax.swing.JToggleButton byTypeButton;
    private javax.swing.JButton helpButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToggleButton quickViewButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JComboBox searchComboBox;
    private javax.swing.JButton sourceButton;
    // End of variables declaration//GEN-END:variables


    private void searchStoped(final List<DocIndexItem> newResults) {
        Mutex.EVENT.readAccess( new Runnable() {
                                                    public void run() {
                                                        results = newResults;
                                                        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(IndexSearch.class,"CTL_SEARCH_ButtonFind"));
                                                        stopState = false;
                                                        referenceModel = typeModel = alphaModel = null;
                                                        sortResults();
                                                        if ( resultsList.getModel().getSize() > 0 ) {
                                                            resultsList.setSelectedIndex( 0 );
                                                            resultsList.grabFocus();
                                                        }
                                                    }
                                                } );
    }

    void setTextToFind( String toFind ) {
        quickFind = toFind;
        /*
        if ( toFind != null ) {
          quickFind = toFind; 
    }
        */
    }


    @Override
    public void open() {
        super.open();

        if ( quickFind != null ) {
            searchComboBox.getEditor().setItem( quickFind );
            searchButtonActionPerformed( null );
        }

        quickFind = null;
        searchComboBox.getEditor().selectAll();
        
        if (setDividerLocation) {
            setDividerLocation = false;
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    splitPanel.setDividerLocation(oldSplit / 100.0);
                }
            });
        }
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        if (!quickViewButton.isSelected()) {
            int split = ds.getIdxSearchSplit();
            double proportional = (double) splitPanel.getDividerLocation() / (splitPanel.getHeight() - splitPanel.getDividerSize());
            final int currSplit = (int) (proportional * 100.0);
            if (split != currSplit) {
                ds.setIdxSearchSplit(currSplit);
            }
        }
    }

    java.awt.Dimension getPrefferedSize() {
        return PREFFERED_SIZE;
    }

    public static IndexSearch getDefault() {
        IndexSearch indexSearch;
        if (refIndexSearch == null || null == (indexSearch = refIndexSearch.get())) {
            indexSearch = new IndexSearch ();
            refIndexSearch = new SoftReference<IndexSearch>(indexSearch);

            indexSearch.setName( NbBundle.getMessage(IndexSearch.class, "CTL_SEARCH_WindowTitle") );   //NOI18N
            indexSearch.setIcon(ImageUtilities.loadImage("org/netbeans/modules/javadoc/resources/searchDoc.gif")); // NOI18N
        }
        return indexSearch;
    }

    public void resolveButtonState() {

        currentSort = ds.getIdxSearchSort();
        final boolean noHtml = ds.isIdxSearchNoHtml();

        byNameButton.setSelected(currentSort.equals("A")); // NOI18N
        byReferenceButton.setSelected(currentSort.equals("R")); // NOI18N
        byTypeButton.setSelected(currentSort.equals("T")); // NOI18N
        quickViewButton.setSelected(!noHtml);
    }
    
    /**
     * Replaces previously stored instances with the default one. Just due to
     * backward compatibility.
     * @return the default instance
     * @throws ObjectStreamException
     */ 
    private Object readResolve() throws ObjectStreamException {
        return getDefault();
    }

    void go() {
        String toFind = searchComboBox.getEditor().getItem().toString().trim();

        // Alocate array for results
        results = new ArrayList<DocIndexItem>();

        //Clear all models
        referenceModel = null;
        typeModel = null;
        alphaModel = null;

        // Try to find this string in Combo

        for ( int i = 0; i < searchComboBox.getItemCount(); i++ ) {
            if ( searchComboBox.getItemAt( i ).toString().equals( toFind ) || i >= 10 ) {
                searchComboBox.removeItemAt( i );
            }
        }

        searchComboBox.insertItemAt( toFind, 0 );
        mirrorMRUStrings();
        searchComboBox.getEditor().setItem( toFind );

        resultsList.setModel( waitModel );

        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(IndexSearch.class,"CTL_SEARCH_ButtonStop"));
        stopState = true;
        searchTask.addSearch(toFind);
    }

    private void mirrorMRUStrings() {
        ComboBoxModel model = searchComboBox.getModel();
        int size = model.getSize();
        MRU = new Object[size];
        for (int i = 0; i < size; i++) {
            MRU[i] = model.getElementAt(i);
        }
    }

    DefaultListModel generateModel( java.util.Comparator<DocIndexItem> comp ) {
        DefaultListModel model = new DefaultListModel();

        results.sort(comp);

        String pckg = null;

        for (DocIndexItem dii : results) {
            if ( comp == DocIndexItem.REFERENCE_COMPARATOR &&
                    !dii.getPackage().equals( pckg ) &&
                    dii.getIconIndex() != DocSearchIcons.ICON_PACKAGE ) {
                DocIndexItem ndii = new DocIndexItem(  "PACKAGE ", dii.getPackage(), null, "" ); // NOI18N
                ndii.setIconIndex( DocSearchIcons.ICON_PACKAGE );
                model.addElement( ndii );
                pckg = dii.getPackage();
            }

            model.addElement( dii );
        }
        return model;
    }

    void sortResults() {

        if ( results.size() < 1 ) {
            resultsList.setModel( notModel );
        }
        else if ( currentSort.equals( "R" ) ) { // NOI18N
            if ( referenceModel == null ) {
                resultsList.setModel( waitModel );
                resultsList.invalidate();
                resultsList.revalidate();
                referenceModel = generateModel( DocIndexItem.REFERENCE_COMPARATOR );
            }
            resultsList.setModel( referenceModel );
        }
        else if ( currentSort.equals( "T" ) ) { // NOI18N
            if ( typeModel == null ) {
                resultsList.setModel( waitModel );
                resultsList.invalidate();
                resultsList.revalidate();
                typeModel = generateModel( DocIndexItem.TYPE_COMPARATOR );
            }
            resultsList.setModel( typeModel );
        }
        else if ( currentSort.equals( "A" ) ) { // NOI18N
            if ( alphaModel == null ) {
                resultsList.setModel( waitModel );
                resultsList.invalidate();
                resultsList.revalidate();
                alphaModel = generateModel( DocIndexItem.ALPHA_COMPARATOR );
            }
            resultsList.setModel( alphaModel );
        }

        resultsList.invalidate();
        resultsList.revalidate();
        resultsList.repaint();
    }

    private HtmlBrowser createBrowser() {
        // Quick browser component
        HtmlBrowser quickBrowser = new HtmlBrowser( true, false );//.BrowserComponent( true, false );
        quickBrowser.setEnableLocation( false );
        quickBrowser.setEnableHome( false );
        //browser buttons without border are too top
        quickBrowser.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 0, 0, 0)));
        quickBrowser.setToolTipText(NbBundle.getMessage(IndexSearch.class, "ACS_SEARCH_QuickBrowserA11yDesc" ));   //NOI18N
        quickBrowser.getAccessibleContext().setAccessibleName(NbBundle.getMessage(IndexSearch.class, "ACS_SEARCH_QuickBrowserA11yName"));  // NOI18N
        quickBrowser.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(IndexSearch.class, "ACS_SEARCH_QuickBrowserA11yDesc"));  // NOI18N

        return quickBrowser;
    }

    private void stopWorld() {
        searchButton.setEnabled(false);
        searchComboBox.setEnabled(false);
        resultsList.setModel(initModel);
    }

    private void resumeWorld() {
        searchButton.setEnabled(true);
        searchComboBox.setEnabled(true);
        resultsList.setModel(waitModel);
    }

    private static final class SearchTask implements Runnable {
        private static final RequestProcessor RP = new RequestProcessor(IndexSearch.class.getName(), 1);
        private final IndexSearch indexSearch;
        private final Task task;
        private final List<String> queries;
        private boolean rootsInited = false;

        private JavadocSearchEngine searchEngine;

        private SearchTask(IndexSearch indexSearch) {
            this.indexSearch = indexSearch;
            task = RP.create(this);
            this.queries = new ArrayList<String>();
        }


        public void addSearch(String toFind) {
            synchronized(this) {
                queries.add(toFind);
            }
            LOG.fine("SearchTask.addSearch: " + toFind);
            task.schedule(0);
        }

        public void stopSearch() {
            searchEngine.stop();
        }

        public void run() {
            // init roots
            initRoots();
            String toFind;
            synchronized(this) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("SearchTask.run: " + queries.size() + ", " + queries.toString());
                }

                toFind = queries.remove(0);
                if (isCanceled()) {
                    LOG.fine("SearchTask.cancel");
                    return;
                }
            }
            // search
            searchEngine = JavadocSearchEngine.getDefault();
            search(toFind, searchEngine, new ArrayList<DocIndexItem>());
        }

        private synchronized boolean isCanceled() {
            return !queries.isEmpty();
        }

        private void initRoots() {
            if (rootsInited) {
                return;
            }
            try {
                EventQueue.invokeAndWait(new Runnable() {

                    public void run() {
                        indexSearch.stopWorld();
                    }
                });

                // init roots
                JavadocRegistry.getDefault().getDocRoots();
                LOG.fine("SearchTask.initRoots");
                rootsInited = true;

                EventQueue.invokeAndWait(new Runnable() {

                    public void run() {
                        indexSearch.resumeWorld();
                    }
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        private void search(final String toFind, final JavadocSearchEngine engine, final List<DocIndexItem> results) {
            try {
                engine.search(new String[]{toFind}, new JavadocSearchEngine.SearchEngineCallback(){
                    public void finished(){
                        LOG.fine("SearchTask.finished: " + toFind);
                        showResult(results);
                    }
                    public void addItem(DocIndexItem item){
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("SearchTask.addItem: " + toFind + ", item: " + item.toString());
                        }
                        results.add(item);
                        if (isCanceled()) {
                            LOG.fine("SearchTask.addItem.stopEngine: " + toFind + ", item: " + item.toString());
                            engine.stop();
                        }
                    }
                });
            }
            catch(NoJavadocException noJdc){
                DialogDisplayer.getDefault().notify( new NotifyDescriptor.Message( noJdc.getMessage() ) );   //NOI18N
                indexSearch.searchStoped(results);
            }
        }

        private void showResult(List<DocIndexItem> results) {
            if (!isCanceled()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "SearchTask.showResult: " + results.size(), new Exception());
                }
                indexSearch.searchStoped(results);
            }
        }

    }
}
