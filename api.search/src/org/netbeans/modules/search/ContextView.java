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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.search;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import static java.lang.Thread.NORM_PRIORITY;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import javax.swing.tree.TreePath;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Panel for displaying context of a matching string within a file.
 * When a node representing a matching string is selected in the tree
 * of results, this panel displays a part of the file surrounding the selected
 * matching string, with the matching string highlighted.
 * When a node representing the whole file is selected, the beginning
 * of the file is displayed.
 *
 * @author  Tim Boudreau
 * @author  Marian Petras
 */
public final class ContextView extends JPanel {
    
    /** */
    private static final String FILE_VIEW = "file view";                //NOI18N
    /** */
    private static final String MESSAGE_VIEW = "message view";          //NOI18N
    private static final int FILE_SIZE_LIMIT = 8 * 1024 * 1024; // 8 MB
    
    /** */
    private final CardLayout cardLayout;
    /** editor pane actually displaying (part of) the file */
    private final JEditorPane editorPane = new JEditorPane();
    /** scroll pane containing the editor pane */
    private final JScrollPane editorScroll;
    /** displays location of the file above the editor pane */
    private final JLabel lblPath = new JLabel();
    /** displays message if no file is displayed */
    private final JLabel lblMessage = new JLabel();
    /**
     * displays content of file after it has been asynchronously loaded
     * by the {@link #requestProcessor}
     */
    private final Displayer displayer = new Displayer();
    /** used for asynchronous loading of files' contents */
    private final RequestProcessor requestProcessor
            = new RequestProcessor("TextView", NORM_PRIORITY, true);    //NOI18N
    
    /** */
    private ResultModel resultModel;
    /** */
    private RequestProcessor.Task task = null;
    /** */
    private TextFetcher textFetcher = null;
    /** */
    private String displayedCard = null;
    /** */
    private String msgNoFileSelected = null;
    /** */
    private String msgMultipleFilesSelected = null;
    /** the current MIME-type set for the {@link #editorPane} */
    private String editorMimeType = null;
    ExplorerManager explorerManager;
    
    /** */
    private Boolean allApproved = null;
    /** Last selected option was to show big file. */
    private static boolean approveApplyToAllSelected = false;
    /** Apply to all big files was selected. */
    private static boolean lastApproveOption = false;

    /** Map of approved/rejected files. */
    private final Map<FileObject, Boolean> APPROVED_FILES =
            new WeakHashMap<FileObject, Boolean>();

    /**
     * 
     * @author  Tim Boudreau
     * @author  Marian Petras
     */
    public ContextView(ResultModel resultModel,
            ExplorerManager explorerManager) {
        Border b = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(        //outside border
                                0, 0, 1, 0, 
                                UIManager.getColor("controlShadow")),   //NOI18N
                BorderFactory.createEmptyBorder(        //inside border
                                5, 5, 1, 5));
        lblPath.setBorder(b);
        
        editorPane.setEditable(false);
        editorPane.getCaret().setBlinkRate(0);
        
        editorScroll = new JScrollPane(editorPane);
        editorScroll.setViewportBorder(BorderFactory.createEmptyBorder());
        editorScroll.setBorder(BorderFactory.createEmptyBorder());
        
        JPanel fileViewPanel = new JPanel();
        fileViewPanel.setLayout(new BorderLayout());
        fileViewPanel.add(lblPath, BorderLayout.NORTH);
        fileViewPanel.add(editorScroll, BorderLayout.CENTER);
        
        Box messagePanel = Box.createVerticalBox();
        messagePanel.add(Box.createVerticalGlue());
        messagePanel.add(lblMessage);
        messagePanel.add(Box.createVerticalGlue());
        lblMessage.setAlignmentX(0.5f);
        lblMessage.setHorizontalAlignment(SwingConstants.CENTER);
        lblMessage.setEnabled(false);
        
        setLayout(cardLayout = new CardLayout());
        add(fileViewPanel, FILE_VIEW);
        add(messagePanel, MESSAGE_VIEW);
        
        setResultModel(resultModel);

        this.explorerManager = explorerManager;
        explorerManager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("selectedNodes")) {
                    updateForSelection();
                }
            }
        });
    }
    
    @Override
    public Dimension getMinimumSize() {
        /*
         * Without this, the minimum width would be equal to the width
         * of the {@linkplain #lblPath file path label}.
         */
        Dimension minSize = super.getMinimumSize();
        minSize.width = 0;
        return minSize;
    }
    
    /**
     */
    void setResultModel(ResultModel resultModel) {
        if (resultModel == this.resultModel) {
            return;
        }
        
        synchronized (this) {           //PENDING - review synchronization
            if (textFetcher != null) {
                textFetcher.cancel();
                textFetcher = null;
            }
        }
        this.resultModel = resultModel;
    }
    
    /**
     * Displays file(s) selected in the given tree.
     * 
     * @author  Marian Petras
     */
    private void displaySelectedFiles(final JTree tree) {
        final TreePath[] selectedPaths = tree.getSelectionPaths();
        if ((selectedPaths == null) || (selectedPaths.length == 0)) {
            displayNoFileSelected();
        } else if (selectedPaths.length > 1) {
            displayMultipleItemsSelected();
        } else {
            assert selectedPaths.length == 1;
            
            final TreePath path = selectedPaths[0];
            int pathCount = path.getPathCount();
            if (pathCount == 1) {                   //root node selected
                displayNoFileSelected();
            } else {
                assert pathCount == 2 || pathCount == 3;
                MatchingObject matchingObj;
                int matchIndex;
                if (pathCount == 2) {               //file node selected
                    matchingObj = (MatchingObject) path.getLastPathComponent();
                    matchIndex = -1;
                } else {                            //match node selected
                    TreePath matchingObjPath = path.getParentPath();
                    matchingObj = (MatchingObject)
                                  matchingObjPath.getLastPathComponent();
                    int matchingObjRow = tree.getRowForPath(matchingObjPath);
                    int matchRow = tree.getRowForPath(path);
                    matchIndex = matchRow - matchingObjRow - 1;
                }
                displayFile(matchingObj, matchIndex);
            }
        }
    }
    
    /**
     */
    private void displayNoFileSelected() {
        if (msgNoFileSelected == null) {
            msgNoFileSelected = NbBundle.getMessage(
                                            getClass(),
                                            "MsgNoFileSelected");       //NOI18N
        }
        displayMessage(msgNoFileSelected);
    }
    
    /**
     */
    private void displayMultipleItemsSelected() {
        if (msgMultipleFilesSelected == null) {
            msgMultipleFilesSelected = NbBundle.getMessage(
                                            getClass(),
                                            "MsgMultipleFilesSelected");//NOI18N
        }
        displayMessage(msgMultipleFilesSelected);
    }
    
    /**
     */
    private void displayMessage(String message) {
        lblMessage.setText(message);
        if (displayedCard != MESSAGE_VIEW) {
            cardLayout.show(this, displayedCard = MESSAGE_VIEW);
        }
    }
    
    /**
     * @author  Tim Boudreau
     * @author  Marian Petras
     */
    @NbBundle.Messages({"MSG_ContextView_fileTooBig=File is too big"})
    private void displayFile(final MatchingObject matchingObj,
                             final int partIndex) {
        assert EventQueue.isDispatchThread();
        
        synchronized (displayer) {          //PENDING - review synchronization
            if (task != null) {
                task.cancel();
                task = null;
            }
            
            FileObject fo = matchingObj.getFileObject();
            if (fo.getSize() > FILE_SIZE_LIMIT) {
                Boolean fileApproved = APPROVED_FILES.get(fo);
                if (allApproved == null && fileApproved == null) {
                    approveFetchingOfBigFile(matchingObj, partIndex);
                    return;
                } else if (Boolean.FALSE.equals(fileApproved)
                        || Boolean.FALSE.equals(allApproved)) {
                    displayMessage(Bundle.MSG_ContextView_fileTooBig());
                    return;
                }
            }
            final Item item = new Item(resultModel, matchingObj, partIndex);
            
            MatchingObject.InvalidityStatus invalidityStatus
                                            = matchingObj.checkValidity();
            if (invalidityStatus != null) {
                displayMessage(invalidityStatus.getDescription(
                                            matchingObj.getFileObject().getPath()));
                return;
            }
            
            requestText(item, displayer);
            String description = matchingObj.getDescription();
            lblPath.setText(description);
            lblPath.setToolTipText(description);        //in case it doesn't fit
        }
    }
    
    /**
     * Fetch the text of an {@code Item}. Since the text is retrieved
     * asynchronously, this method is passed a {@code TextDisplayer},
     * which will get its {@code setText()} method called on the event thread
     * after it has been loaded on a background thread.
     * 
     * @param  item  item to be displayed by the text displayer
     * @param  textDisplayer  displayer that should display the item
     * 
     * @author  Tim Boudreau
     */
    private void requestText(Item item, TextDisplayer textDisplayer) {
        assert EventQueue.isDispatchThread();
        
        synchronized (this) {           //PENDING - review synchronization
            if (textFetcher != null) {
                if (textFetcher.replaceLocation(item, textDisplayer)) {
                    return;
                } else {
                    textFetcher.cancel();
                    textFetcher = null;
                }
            }
            if (textFetcher == null) {
                textFetcher = new TextFetcher(item,
                                              textDisplayer,
                                              requestProcessor);
            }
        }
    }

    private void updateForSelection() {
        Node[] nodes = explorerManager.getSelectedNodes();
        if (nodes.length == 0) {
            displayNoFileSelected();
        } else if (nodes.length == 1) {
            Node n = nodes[0];
            MatchingObject mo = n.getLookup().lookup(MatchingObject.class);
            if (mo != null) {
                displayFile(mo, -1);
            } else {
                Node parent = n.getParentNode();
                TextDetail td = n.getLookup().lookup(TextDetail.class);
                if (td != null && parent != null) {
                    mo = parent.getLookup().lookup(
                            MatchingObject.class);
                    if (mo != null) {
                        // TODO pass TextDetail directly
                        int index = -1;
                        for (int i = 0; i < mo.getTextDetails().size(); i++) {
                            if (mo.getTextDetails().get(i) == td) {
                                index = i;
                                break;
                            }
                        }
                        displayFile(mo, index);
                    }
                } else {
                    displayNoFileSelected();
                }
            }
        } else {
            displayMultipleItemsSelected();
        }
    }

    @NbBundle.Messages({
        "TTL_ContextView_showBigFile=Show Big File?",
        "# {0} - file name",
        "# {1} - file size in kilobytes",
        "MSG_ContextView_showBigFile=File {0} is quite big ({1} kB).\n"
        + "Showing it can cause memory and performance problems.\n"
        + "Do you want to show content of this file?",
        "LBL_ContextView_Show=Show",
        "LBL_ContextView_Skip=Do Not Show",
        "LBL_ContextView_ApplyAll=Apply to all big files"
    })
    private void approveFetchingOfBigFile(final MatchingObject mo,
            final int partIndex) {
        FileObject fo = mo.getFileObject();
        long fileSize = fo.getSize() / 1024;
        JButton showButton = new JButton(Bundle.LBL_ContextView_Show());
        JButton skipButton = new JButton(Bundle.LBL_ContextView_Skip());
        JCheckBox all = new JCheckBox(Bundle.LBL_ContextView_ApplyAll());
        all.setSelected(approveApplyToAllSelected);
        JPanel allPanel = new JPanel();
        allPanel.add(all); //Add to panel not to be handled as standard button.
        NotifyDescriptor nd = new NotifyDescriptor(
                Bundle.MSG_ContextView_showBigFile(
                fo.getNameExt(), fileSize),
                Bundle.TTL_ContextView_showBigFile(),
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.WARNING_MESSAGE,
                new Object[]{skipButton, showButton},
                lastApproveOption ? showButton : skipButton);
        nd.setAdditionalOptions(new Object[]{allPanel});
        DialogDisplayer.getDefault().notify(nd);
        boolean app = nd.getValue() == showButton;
        APPROVED_FILES.put(fo, app);
        if (all.isSelected()) {
            allApproved = app;
        }
        approveApplyToAllSelected = all.isSelected();
        lastApproveOption = app;
        displayFile(mo, partIndex);
    }

    /**
     * Implementation of {@code TextDisplayer} which is passed to get the text
     * of an item.  The text is fetched from the file asynchronously, and then
     * passed to {@link #setText()} to set the text, select the text the item
     * represents and scroll it into view.
     * 
     * @see  TextReceiver
     * @author  Tim Boudreau
     * @author  Marian Petras
     */
    private class Displayer implements TextDisplayer, Runnable {
        
        private TextDetail location;
        
        /**
         * @author  Tim Boudreau
         */
        @Override
        public void setText(final String text,
                            String mimeType,
                            final TextDetail location) {
            assert EventQueue.isDispatchThread();
            
            if ("content/unknown".equals(mimeType)) {                   //NOI18N
                mimeType = "text/plain";  //Good idea? Bad? Hmm...      //NOI18N
            }
            
            /*
             * Changing content type clears the text - so the content type
             * (in this case, MIME-type only) must be set _before_ the text
             * is set.
             */
            if ((editorMimeType == null) || !editorMimeType.equals(mimeType)) {
                editorPane.setContentType(mimeType);
                editorMimeType = mimeType;
            }
            editorPane.setText(text);
            
            if (displayedCard != FILE_VIEW) {
                cardLayout.show(ContextView.this, displayedCard = FILE_VIEW);
            }
            
            if (location != null) {
                //Let the L&F do anything it needs to do before we try to fiddle
                //with it - get out of its way.  Some Swing View classes don't
                //have accurate position data until they've painted once.
                this.location = location;
                EventQueue.invokeLater(this);
            } else {
                scrollToTop();
            }
        }

        /**
         * 
         * @author  Tim Boudreau
         * @author  Marian Petras
         */
        public void run() {
            assert EventQueue.isDispatchThread();
            
            boolean scrolled = false;
            try {
                if (!editorPane.isShowing()) {
                    return;
                }
                
                if (location != null) {
                    final Document document = editorPane.getDocument();
                    if (document instanceof StyledDocument) {
                        StyledDocument styledDocument
                                = (StyledDocument) document;
                        int cursorOffset = getCursorOffset(
                                                    (StyledDocument) document,
                                                    location.getLine() - 1);
                        int startOff = cursorOffset + location.getColumn() - 1;
                        int endOff = startOff + location.getMarkLength();
                        editorPane.setSelectionStart(startOff);
                        editorPane.setSelectionEnd(endOff);
                        Rectangle r = editorPane.modelToView(startOff);
                        if (r != null) {
                            //Editor kit not yet updated, what to do
                            editorPane.scrollRectToVisible(r);
                            scrolled = true;
                        }
                    }
                    editorPane.getCaret().setBlinkRate(0);
                    editorPane.repaint();
                }
            } catch (BadLocationException e) {
                //Maybe not even notify this - not all editors
                //will have a 1:1 correspondence to file positions -
                //it's perfectly reasonable for this to be thrown
                ErrorManager.getDefault().notify(      //PENDING - ErrorManager?
                        ErrorManager.INFORMATIONAL, e);
            }
            if (!scrolled) {
                scrollToTop();
            }
        }
        
        /**
         * Computes cursor offset of a given line of a document.
         * The line number must be non-negative.
         * If the line number is greater than number of the last line,
         * the returned offset corresponds to the last line of the document.
         *
         * @param  doc  document to computer offset for
         * @param  line  line number (first line = <code>0</code>)
         * @return  cursor offset of the beginning of the given line
         * 
         * @author  Marian Petras
         */
        private int getCursorOffset(StyledDocument doc, int line) {
            assert EventQueue.isDispatchThread();
            assert line >= 0;

            try {
                return NbDocument.findLineOffset(doc, line);
            } catch (IndexOutOfBoundsException ex) {
                /* probably line number out of bounds */

                Element lineRootElement = NbDocument.findLineRootElement(doc);
                int lineCount = lineRootElement.getElementCount();
                if (line >= lineCount) {
                    return NbDocument.findLineOffset(doc, lineCount - 1);
                } else {
                    throw ex;
                }
            }
        }
    
        /**
         */
        private void scrollToTop() {
            JScrollBar scrollBar;

            scrollBar = editorScroll.getHorizontalScrollBar();
            scrollBar.setValue(scrollBar.getMinimum());

            scrollBar = editorScroll.getVerticalScrollBar();
            scrollBar.setValue(scrollBar.getMinimum());
        }
        
    }

}
