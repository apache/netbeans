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

package org.netbeans.modules.git.ui.blame;

import org.netbeans.editor.*;
import org.netbeans.editor.Utilities;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.text.*;
import org.openide.util.*;
import org.openide.xml.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.accessibility.Accessible;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.io.*;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.checkout.CheckoutPathsAction;
import org.netbeans.modules.git.ui.diff.DiffAction;
import org.netbeans.modules.git.ui.history.SearchHistoryAction;
import org.netbeans.modules.git.ui.repository.Revision;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.awt.Actions;
import org.openide.util.actions.SystemAction;

/**
 * Represents annotation sidebar componnet in editor. It's
 * created by {@link AnnotationBarManager}.
 *
 * <p>It reponds to following external signals:
 * <ul>
 *   <li> {@link #annotate} message
 * </ul>
 *
 * @author Petr Kuzel
 */
final class AnnotationBar extends JComponent implements Accessible, PropertyChangeListener, DocumentListener, ChangeListener, ActionListener, Runnable, ComponentListener {
    private static boolean fieldsInitialized;

    /**
     * Target text component for which the annotation bar is aiming.
     */
    private final JTextComponent textComponent;

    /**
     * User interface related to the target text component.
     */
    private final EditorUI editorUI;

    /**
     * Fold hierarchy of the text component user interface.
     */
    private final FoldHierarchy foldHierarchy;

    /** 
     * Document related to the target text component.
     */
    private final BaseDocument doc;

    /**
     * Caret of the target text component.
     */
    private Caret caret;

    /**
     * Caret batch timer launched on receiving
     * annotation data structures (AnnotateLine).
     */
    private Timer caretTimer;

    /**
     * Controls annotation bar visibility.
     */
    private boolean annotated;

    /**
     * Maps document {@link javax.swing.text.Element}s (representing lines) to
     * {@link AnnotateLine}. <code>null</code> means that
     * no data are available, yet. So alternative
     * {@link #elementAnnotationsSubstitute} text shoudl be used.
     *
     * @thread it is accesed from multiple threads all mutations
     * and iterations must be under elementAnnotations lock,
     */
    private Map<Element, AnnotateLine> elementAnnotations;

    /**
     * Represents text that should be displayed in
     * visible bar with yet <code>null</code> elementAnnotations.
     */
    private String elementAnnotationsSubstitute;
    
    private Color backgroundColor = Color.WHITE;
    private Color foregroundColor = Color.BLACK;
    private Color selectedColor = Color.BLUE;

    /**
     * Most recent status message.
     */
    private String recentStatusMessage;
    
    /**
     * Revision associated with caret line.
     */
    private String recentRevision;
    
    /**
     * Request processor to create threads that may be cancelled.
     */
    static RequestProcessor requestProcessor = null;
    
    /**
     * Latest annotation comment fetching task launched.
     */
    private RequestProcessor.Task latestAnnotationTask = null;

    /**
     * Repository root of annotated file
     */
    private File repositoryRoot;

    /*
     * Holds parent/previous revisions for each line revision
     */
    private Map<String, String> previousRevisions;
    /**
     * This is not null when the displayed annotations do not belong directly to the displayed file but to another.
     * This can happen e.g. when showing annotations for file in a certain past revision - the displayed file is in fact a temporary file.
     */
    private File referencedFile;
    /**
     * This is not null when the displayed annotations do not belong directly to the displayed file but to another.
     * This can happen e.g. when showing annotations for file in a certain past revision - the displayed file is in fact a temporary file.
     */
    private FileObject referencedFileObject;
    static final Logger LOG = Logger.getLogger(AnnotationBar.class.getName());
    private String annotatedRevision;
    private static final DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
    
    /**
     * Rendering hints for annotations sidebar inherited from editor settings.
     */
    private final Map renderingHints;
    
    @NbBundle.Messages({
        "LBL_Blame.menu.show.commitId=Revision",
        "LBL_Blame.menu.show.date=Date",
        "LBL_Blame.menu.show.author=Author"
    })
    private static enum DisplayField {
        
        COMMIT_ID(1, Bundle.LBL_Blame_menu_show_commitId(), true) {

            @Override
            String format (AnnotateLine line) {
                return line.getRevisionInfo().getRevision().substring(0, 7);
            }
            
        },
        DATE(1 << 1, Bundle.LBL_Blame_menu_show_date(), false) {

            @Override
            String format (AnnotateLine line) {
                return dateFormat.format(new Date(line.getRevisionInfo().getCommitTime()));
            }
            
        },
        AUTHOR(1 << 2, Bundle.LBL_Blame_menu_show_author(), true) {

            @Override
            String format (AnnotateLine line) {
                return line.getAuthorShort();
            }
            
        };
        
        private final int value;
        private final String label;
        private boolean visible;
        
        private DisplayField (int val, String label, boolean visibleByDefault) {
            this.value = val;
            this.label = label;
            this.visible = visibleByDefault;
        }

        abstract String format (AnnotateLine line);
    };

    /**
     * Creates new instance initializing final fields.
     */
    public AnnotationBar(JTextComponent target) {
        this.textComponent = target;
        this.editorUI = Utilities.getEditorUI(target);
        this.foldHierarchy = FoldHierarchy.get(editorUI.getComponent());
        this.doc = editorUI.getDocument();
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        elementAnnotationsSubstitute = "";                              //NOI18N
        if (textComponent instanceof JEditorPane) {
            String mimeType = org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(textComponent);
            FontColorSettings fcs = MimeLookup.getLookup(mimeType).lookup(FontColorSettings.class);
            renderingHints = (Map) fcs.getFontColors(FontColorNames.DEFAULT_COLORING).getAttribute(EditorStyleConstants.RenderingHints);
        } else {
            renderingHints = null;
        }
    }


    // public contract ~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public File getRepositoryRoot() {
        return repositoryRoot;
    }


    /**
     * Makes the bar visible and sensitive to
     * LogOutoutListener events that should deliver
     * actual content to be displayed.
     */
    public void annotate() {
        annotated = true;
        elementAnnotations = null;

        doc.addDocumentListener(this);
        textComponent.addComponentListener(this);
        editorUI.addPropertyChangeListener(this);

        revalidate();  // resize the component
    }

    public void setAnnotationMessage(String message) {
        elementAnnotationsSubstitute = message;
        revalidate();
    }
    
    /**
     * Result computed show it...
     * Takes AnnotateLines and shows them.
     */
    public void annotationLines (File file, List<AnnotateLine> annotateLines) {
        // set repository root for popup menu, now should be the right time
        repositoryRoot = Git.getInstance().getRepositoryRoot(getCurrentFile());
        final List<AnnotateLine> lines = new LinkedList<>(annotateLines);
        int lineCount = lines.size();
        /** 0 based line numbers => 1 based line numbers*/
        final int ann2editorPermutation[] = new int[lineCount];
        for (int i = 0; i< lineCount; i++) {
            ann2editorPermutation[i] = i+1;
        }

        DiffProvider diff = (DiffProvider) Lookup.getDefault().lookup(DiffProvider.class);
        if (diff != null) {
            Reader r = new LinesReader(lines);
            Reader docReader = Utils.getDocumentReader(doc);
            try {

                Difference[] differences = diff.computeDiff(r, docReader);

                // customize annotation line numbers to match different reality
                // compule line permutation

                for (int i = 0; i < differences.length; i++) {
                    Difference d = differences[i];
                    if (d.getType() == Difference.ADD) continue;

                    int editorStart;
                    int firstShift = d.getFirstEnd() - d.getFirstStart() +1;
                    if (d.getType() == Difference.CHANGE) {
                        int firstLen = d.getFirstEnd() - d.getFirstStart();
                        int secondLen = d.getSecondEnd() - d.getSecondStart();
                        if (secondLen >= firstLen) continue; // ADD or pure CHANGE
                        editorStart = d.getSecondStart();
                        firstShift = firstLen - secondLen;
                    } else {  // DELETE
                        editorStart = d.getSecondStart() + 1;
                    }

                    for (int c = editorStart + firstShift -1; c<lineCount; c++) {
                        ann2editorPermutation[c] -= firstShift;
                    }
                }

                for (int i = differences.length -1; i >= 0; i--) {
                    Difference d = differences[i];
                    if (d.getType() == Difference.DELETE) continue;

                    int firstStart;
                    int firstShift = d.getSecondEnd() - d.getSecondStart() +1;
                    if (d.getType() == Difference.CHANGE) {
                        int firstLen = d.getFirstEnd() - d.getFirstStart();
                        int secondLen = d.getSecondEnd() - d.getSecondStart();
                        if (secondLen <= firstLen) continue; // REMOVE or pure CHANGE
                        firstShift = secondLen - firstLen;
                        firstStart = d.getFirstStart();
                    } else {
                        firstStart = d.getFirstStart() + 1;
                    }

                    for (int k = firstStart-1; k<lineCount; k++) {
                        ann2editorPermutation[k] += firstShift;
                    }
                }

            } catch (IOException e) {
                LOG.log(Level.INFO, "Cannot compute local diff required for annotations, ignoring...");  // NOI18N 
            }
        }

        doc.render(new Runnable() {
            @Override
            public void run() {
                StyledDocument sd = (StyledDocument) doc;
                previousRevisions = Collections.synchronizedMap(new HashMap<>());
                elementAnnotations = Collections.synchronizedMap(new HashMap<>(lines.size()));
                for (AnnotateLine line : lines) {
                    int lineNum = ann2editorPermutation[line.getLineNum() -1];
                    try {
                        int lineOffset = NbDocument.findLineOffset(sd, lineNum -1);
                        Element element = sd.getParagraphElement(lineOffset);                        
                        elementAnnotations.put(element, line);
                    } catch (IndexOutOfBoundsException ex) {
                        LOG.log(Level.INFO, null, ex);
                    }
                }
            }
        });
               
        // lazy listener registration
        caret = textComponent.getCaret();
        if (caret != null) {
            caret.addChangeListener(this);
        }
        textComponent.addPropertyChangeListener(this);
        this.caretTimer = new Timer(500, this);
        caretTimer.setRepeats(false);

        onCurrentLine();
        revalidate();
        repaint();
    }

    // implementation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Gets a the file related to the document
     *
     * @return the file related to the document, <code>null</code> if none
     * exists.
     */
    File getCurrentFile() {
        File result = referencedFile;
        if (result == null) {
            DataObject dobj = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
            if (dobj != null) {
                FileObject fo = dobj.getPrimaryFile();
                result = FileUtil.toFile(fo);
            }
        }
        return result;
    }

    FileObject getCurrentFileObject () {
        FileObject result = referencedFileObject;
        if (result == null) {
            Object sdp = doc.getProperty(Document.StreamDescriptionProperty);
            if (sdp instanceof FileObject) {
                result = (FileObject)sdp;
            } else if (sdp instanceof DataObject) {
                result = ((DataObject)sdp).getPrimaryFile();
            }
        }
        return result;
    }

    Document getDocument() {
        return doc;
    }

    private MouseListener mouseListener;
    /**
     * Registers "close" popup menu, tooltip manager // NOI18N
     * and repaint on documet change manager.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        this.addMouseListener(mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    e.consume();
                    createPopup(e).show(e.getComponent(),
                               e.getX(), e.getY());
                } else if (e.getID() == MouseEvent.MOUSE_RELEASED && e.getButton() == MouseEvent.BUTTON1) {
                    e.consume();
                    showTooltipWindow(e);
                }
            }
        });

        // register with tooltip manager
        setToolTipText(""); // NOI18N

    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (mouseListener != null) {
            this.removeMouseListener(mouseListener);
            mouseListener = null;
        }
    }

    /**
     *
     * @return
     */
    JTextComponent getTextComponent () {
        return textComponent;
    }

    /**
     *
     * @param event
     */
    private void showTooltipWindow (MouseEvent event) {
        Point p = new Point(event.getPoint());
        SwingUtilities.convertPointToScreen(p, this);
        Point p2 = new Point(p);
        SwingUtilities.convertPointFromScreen(p2, textComponent);
        
        // annotation for target line
        AnnotateLine al = null;
        if (elementAnnotations != null) {
            al = getAnnotateLine(getLineFromMouseEvent(event));
        }

        /**
         * al.getCommitMessage() != null - since commit messages are initialized separately from the AL constructor
         */
        if (al != null && al.getRevisionInfo() != null) {
            TooltipWindow ttw = new TooltipWindow(this, al);
            ttw.show(new Point(p.x - p2.x, p.y));
        }
    }

    @NbBundle.Messages({
        "# {0} - commit id", "CTL_AnnotationBar.action.showCommit=Show Commit {0}"
    })
    private JPopupMenu createPopup(MouseEvent e) {
        final ResourceBundle loc = NbBundle.getBundle(AnnotationBar.class);
        final JPopupMenu popupMenu = new JPopupMenu();

        // annotation for target line
        AnnotateLine al = null;
        if (elementAnnotations != null) {
            al = getAnnotateLine(getLineFromMouseEvent(e));
        }
        // revision previous to target line's revision
        final GitRevisionInfo revisionPerLine = al == null ? null : al.getRevisionInfo();
        // used in menu Revert
        final File file = getCurrentFile();
        // used in diff menu, repository root set while computing revision
        // denotes the path of the file in the showing revision
        final File originalFile = al == null ? null : al.getFile();
        final boolean revisionCanBeRolledBack = al == null || referencedFile != null ? false : al.canBeRolledBack();
        // source line in the original file this line annotation represents
        final int sourceLine = al == null ? -1 : al.getSourceLineNum();
        
        if (revisionPerLine != null) {
            final JMenuItem diffMenu = new JMenuItem(loc.getString("CTL_MenuItem_DiffToPrevious")); // NOI18N
            diffMenu.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    final PreviousRevisionInvoker pri = new PreviousRevisionInvoker(originalFile, revisionPerLine);
                    pri.runWithRevision(Git.getInstance().getRequestProcessor(), new Runnable() {
                        @Override
                        public void run() {
                            SystemAction.get(DiffAction.class).diff(originalFile, new Revision(pri.getPreviousRevision(), pri.getPreviousRevision()),
                                    new Revision(revisionPerLine.getRevision(), revisionPerLine.getRevision()), sourceLine);
                        }
                    }, true, null);
                }
            });
            popupMenu.add(diffMenu);

            JMenuItem showCommitMenu = new JMenuItem(Bundle.CTL_AnnotationBar_action_showCommit(revisionPerLine.getRevision().substring(0, 7)));
            showCommitMenu.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SearchHistoryAction.openSearch(repositoryRoot, file, file.getName(),
                            revisionPerLine.getRevision(), revisionPerLine.getRevision());
                }
            });
            popupMenu.add(showCommitMenu);

            final JMenuItem checkoutMenu = new JMenuItem();
            checkoutMenu.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    checkout(file, revisionPerLine.getRevision());
                }
            });
            popupMenu.add(checkoutMenu);
            checkoutMenu.setEnabled(revisionCanBeRolledBack);

            final JMenuItem checkoutPrevItem = new JMenuItem();
            checkoutPrevItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    final PreviousRevisionInvoker pri = new PreviousRevisionInvoker(originalFile, revisionPerLine);
                    pri.runWithRevision (Git.getInstance().getRequestProcessor(repositoryRoot), new Runnable() {
                        @Override
                        public void run() {
                            String previousRevision = pri.getPreviousRevision();
                            checkout(originalFile, previousRevision);
                        }
                    }, false, null);
                }
            });
            popupMenu.add(checkoutPrevItem);
            checkoutPrevItem.setVisible(false);

            // an action showing annotation for line's revisions
            final JMenuItem annotationsForSelectedItem = new JMenuItem(NbBundle.getMessage(AnnotationBar.class, "CTL_MenuItem_ShowAnnotationsFor", revisionPerLine.getRevision().substring(0, 7))); //NOI18N
            annotationsForSelectedItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new GitProgressSupport() {
                        @Override
                        protected void perform () {
                            try {
                                GitUtils.openInRevision(originalFile, sourceLine , revisionPerLine.getRevision(), true, getProgressMonitor());
                            } catch (IOException ex) {
                                //
                            }
                        }
                    }.start(Git.getInstance().getRequestProcessor(), repositoryRoot, NbBundle.getMessage(AnnotationBar.class, "MSG_Annotation_Progress")); //NOI18N
                }
            });
            popupMenu.add(annotationsForSelectedItem);

            // an action showing annotation for previous revisions
            final JMenuItem previousAnnotationsMenu = new JMenuItem();
            previousAnnotationsMenu.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    final PreviousRevisionInvoker pri = new PreviousRevisionInvoker(originalFile, revisionPerLine);
                    pri.runWithRevision (Git.getInstance().getRequestProcessor(repositoryRoot), new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String previousRevision = pri.getPreviousRevision();
                                if (sourceLine > 0) {
                                    GitUtils.openInRevision(originalFile, revisionPerLine.getRevision(), sourceLine,
                                            previousRevision, true, pri.getProgressMonitor());
                                } else {
                                    GitUtils.openInRevision(originalFile, -1, previousRevision, true, pri.getProgressMonitor());
                                }
                            } catch (IOException ex) {
                                //
                            }
                        }
                    }, false, NbBundle.getMessage(AnnotationBar.class, "MSG_Annotation_Progress")); //NOI18N
                }
            });
            popupMenu.add(previousAnnotationsMenu);
            previousAnnotationsMenu.setVisible(false);

            JSeparator separator = new JPopupMenu.Separator();
            popupMenu.add(separator);

            String key = getKeyFor(originalFile, revisionPerLine.getRevision());
            String previousRevision = getPreviousRevisions().get(key); // get from cache
            boolean hasPrevious = previousRevision != null || !getPreviousRevisions().containsKey(key);
            if (previousRevision == null && hasPrevious) {
                // get revision in a bg thread and cache the value
                final PreviousRevisionInvoker pri = new PreviousRevisionInvoker(originalFile, revisionPerLine);
                pri.runWithRevision(Git.getInstance().getRequestProcessor(), new Runnable() {
                    @Override
                    public void run () {
                        String prevRev = pri.getPreviousRevision();
                        boolean showing = popupMenu.isShowing();
                        if (prevRev != null && showing) {
                            popupMenu.setVisible(false);
                            prevRev = prevRev.substring(0, 7);
                            String format = loc.getString("CTL_MenuItem_ShowAnnotationsPrevious.revision"); // NOI18N
                            previousAnnotationsMenu.setText(MessageFormat.format(format, new Object [] { prevRev })); //NOI18N
                            format = loc.getString("CTL_MenuItem_DiffToPrevious.revision"); // NOI18N
                            diffMenu.setText(MessageFormat.format(format, new Object [] { prevRev })); //NOI18N
                            checkoutPrevItem.setText(NbBundle.getMessage(AnnotationBar.class, 
                                    "CTL_MenuItem_CheckoutPrevious.revision", new Object [] { prevRev })); //NOI18N
                            popupMenu.setVisible(true);
                        }
                    }
                }, true, null);
            } else if (hasPrevious) {
                previousRevision = previousRevision.substring(0, 7);
            }
            String format = loc.getString(previousRevision == null ? "CTL_MenuItem_DiffToPrevious" : "CTL_MenuItem_DiffToPrevious.revision"); // NOI18N
            diffMenu.setText(MessageFormat.format(format, previousRevision)); //NOI18N
            diffMenu.setVisible(originalFile != null);
            format = loc.getString("CTL_MenuItem_Checkout"); // NOI18N
            checkoutMenu.setText(MessageFormat.format(format, new Object [] { revisionPerLine.getRevision().substring(0, 7) }));
            checkoutMenu.setVisible(true);
            separator.setVisible(true);
            annotationsForSelectedItem.setVisible(originalFile != null && revisionPerLine != null && !revisionPerLine.getRevision().equals(annotatedRevision));
            if (hasPrevious && originalFile != null) {
                format = loc.getString(previousRevision == null 
                        ? "CTL_MenuItem_ShowAnnotationsPrevious" : "CTL_MenuItem_ShowAnnotationsPrevious.revision"); // NOI18N
                previousAnnotationsMenu.setText(MessageFormat.format(format, previousRevision)); //NOI18N
                previousAnnotationsMenu.setVisible(true);
                previousAnnotationsMenu.setEnabled(!"-1".equals(previousRevision)); //NOI18N
                if (file.equals(originalFile)) {
                    format = loc.getString(previousRevision == null 
                            ? "CTL_MenuItem_CheckoutPrevious" : "CTL_MenuItem_CheckoutPrevious.revision"); // NOI18N
                    checkoutPrevItem.setText(MessageFormat.format(format, previousRevision)); //NOI18N
                    checkoutPrevItem.setVisible(true);
                    checkoutPrevItem.setEnabled(true);
                }
            }
        }
        JMenu showMenu = createShowSubmenu();
        popupMenu.add(showMenu);
        popupMenu.add(new JPopupMenu.Separator());
        
        JMenuItem closeMenu;
        closeMenu = new JMenuItem(loc.getString("CTL_MenuItem_CloseAnnotations")); // NOI18N
        closeMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hideBar();
            }
        });
        popupMenu.add(closeMenu);

        return popupMenu;
    }

    @NbBundle.Messages({
        "LBL_Blame.menu.show=Show"
    })
    private JMenu createShowSubmenu () {
        JMenu showMenu = new JMenu(Bundle.LBL_Blame_menu_show());
        for (final DisplayField field : DisplayField.values()) {
            final JMenuItem mItem = new JCheckBoxMenuItem(Bundle.LBL_Blame_menu_show_commitId(), field.visible);
            Actions.connect(mItem, new AbstractAction(field.label) {

                @Override
                public void actionPerformed (ActionEvent e) {
                    field.visible = mItem.isSelected();
                    int toStore = 0;
                    for (DisplayField field : DisplayField.values()) {
                        if (field.visible) {
                            toStore += field.value;
                        }
                    }
                    final int toStoreFinal = toStore;
                    Utils.post(new Runnable() {

                        @Override
                        public void run () {
                            GitModuleConfig.getDefault().setAnnotationDisplayedFields(toStoreFinal);
                        }
                    });
                    revalidate();
                    repaint();
                }
                
            }, annotated);
            showMenu.add(mItem);
        }
        return showMenu;
    }

    void setAnnotatedRevision (String revision) {
        this.annotatedRevision = revision;
    }

    private String getKeyFor (File file, String revision) {
        return file.getAbsolutePath() + "#" + revision; //NOI18N
    }

    boolean isAnnotated() {
        return annotated;
    }

    /**
     * Class for running a code after the previous revision is determined, which may take some time
     */
    private class PreviousRevisionInvoker extends GitProgressSupport.NoOutputLogging {
        private final GitRevisionInfo revisionPerLine;
        private String parent;
        private boolean inAWT;
        private Runnable runnable;
        private boolean progressNameSet;
        private final File originalFile;

        private PreviousRevisionInvoker (File originalFile, GitRevisionInfo revisionPerLine) {
            this.revisionPerLine = revisionPerLine;
            this.originalFile = originalFile;
        }

        public void runWithRevision (RequestProcessor rp, Runnable runnable, boolean inAWT, String progressName) {
            this.runnable = runnable;
            this.inAWT = inAWT;
            this.progressNameSet = progressName != null;
            start(rp, repositoryRoot, progressNameSet ? progressName : NbBundle.getMessage(AnnotationBar.class, "MSG_GettingPreviousRevision")); //NOI18N
        }
        
        @Override
        protected void perform () {
            if (progressNameSet) {
                setProgress(NbBundle.getMessage(AnnotationBar.class, "MSG_GettingPreviousRevision")); //NOI18N
            }
            String previousRevision = getParentRevision(originalFile, revisionPerLine);
            if (!isCanceled() && previousRevision != null) {
                if (inAWT) {
                    EventQueue.invokeLater(runnable);
                } else {
                    if (progressNameSet) {
                        setProgress(null);
                    }
                    runnable.run();
                }
            }
        }

        private String getParentRevision (File file, GitRevisionInfo revision) {
            String key = getKeyFor(file, revisionPerLine.getRevision());
            parent = getPreviousRevisions().get(key);
            if (parent == null) {
                GitRevisionInfo parentInfo = null;
                try {
                    if (revision.getParents().length == 1) {
                        parentInfo = getClient().getPreviousRevision(file, revision.getRevision(), getProgressMonitor());
                    }
                    if (parentInfo == null) {
                        // fallback for merges and initial revisoin
                        parentInfo = getClient().getCommonAncestor(revision.getParents(), getProgressMonitor());
                    }
                } catch (GitException ex) {
                    LOG.log(Level.INFO, null, ex);
                }
                if (parentInfo != null) {
                    parent = parentInfo.getRevision();
                }
                getPreviousRevisions().put(key, parent);
            }
            return parent;
        }

        private String getPreviousRevision () {
            return parent;
        }
    }

    private void checkout (final File file, String revision) {
        final File root = Git.getInstance().getRepositoryRoot(file);
        if(root == null) return;
        
        File[] files = new File [1];
        files[0] = file; 
        SystemAction.get(CheckoutPathsAction.class).checkoutFiles(repositoryRoot, new File[] { file }, revision);
    }

    /**
     * Hides the annotation bar from user. 
     */
    void hideBar() {
        annotated = false;
        revalidate();
        release();
    }

    /**
     * Gets a request processor which is able to cancel tasks.
     */
    private static synchronized RequestProcessor getRequestProcessor() {
        if (requestProcessor == null) {
            requestProcessor = new RequestProcessor("AnnotationBarRP", 1, true);  // NOI18N
        }
        
        return requestProcessor;
    }
    
    /**
     * Shows commit message in status bar and or revision change repaints side
     * bar (to highlight same revision). This process is started in a
     * seperate thread.
     */
    private void onCurrentLine() {
        if (latestAnnotationTask != null) {
            latestAnnotationTask.cancel();
        }
        
        if (isAnnotated()) {
            latestAnnotationTask = getRequestProcessor().post(this);
        }
    }

    // latestAnnotationTask business logic
    @Override
    public void run() {
        Caret carett = this.caret;
        if (carett == null || !isAnnotated()) {
            // closed in the meantime
            return;
        }
        // get resource bundle
        ResourceBundle loc = NbBundle.getBundle(AnnotationBar.class);
        // give status bar "wait" indication // NOI18N
        StatusBar statusBar = editorUI.getStatusBar();
        recentStatusMessage = loc.getString("CTL_StatusBar_WaitFetchAnnotation"); // NOI18N
        statusBar.setText(StatusBar.CELL_MAIN, recentStatusMessage);
        
        // determine current line
        int line = -1;
        int offset = carett.getDot();
        try {
            line = Utilities.getLineOffset(doc, offset);
        } catch (BadLocationException ex) {
            LOG.log(Level.SEVERE, "Can not get line for caret at offset ", offset); // NOI18N
            clearRecentFeedback();
            return;
        }

        // handle locally modified lines
        AnnotateLine al = getAnnotateLine(line);
        if (al == null) {
            AnnotationMarkProvider amp = AnnotationMarkInstaller.getMarkProvider(textComponent);
            if (amp != null) {
                amp.setMarks(Collections.<AnnotationMark>emptyList());
            }
            clearRecentFeedback();
            if (recentRevision != null) {
                recentRevision = null;
                repaint();
            }
            return;
        }

        // handle unchanged lines
        String revision = al.getRevisionInfo().getRevision();
        if (!revision.equals(recentRevision)) {
            recentRevision = revision;
            repositoryRoot = Git.getInstance().getRepositoryRoot(getCurrentFile());
            repaint();

            AnnotationMarkProvider amp = AnnotationMarkInstaller.getMarkProvider(textComponent);
            if (amp != null) {
            
                List<AnnotationMark> marks = new ArrayList<>(elementAnnotations.size());
                // I cannot affort to lock elementAnnotations for long time
                // it's accessed from editor thread too
                Iterator<Map.Entry<Element, AnnotateLine>> it2;
                synchronized(elementAnnotations) {
                    it2 = new HashSet<>(elementAnnotations.entrySet()).iterator();
                }
                while (it2.hasNext()) {
                    Map.Entry<Element, AnnotateLine> next = it2.next();                        
                    AnnotateLine annotateLine = next.getValue();
                    if (annotateLine.getRevisionInfo() != null && revision.equals(annotateLine.getRevisionInfo().getRevision())) {
                        Element element = next.getKey();
                        if (elementAnnotations.containsKey(element) == false) {
                            continue;
                        }
                        int elementOffset = element.getStartOffset();
                        int lineNumber = NbDocument.findLineNumber((StyledDocument)doc, elementOffset);
                        AnnotationMark mark = new AnnotationMark(lineNumber, revision);
                        marks.add(mark);
                    }

                    if (Thread.interrupted()) {
                        clearRecentFeedback();
                        return;
                    }
                }
                amp.setMarks(marks);
            }
        }

        if (al.getRevisionInfo() != null) {
            recentStatusMessage = al.getRevisionInfo().getShortMessage();
            statusBar.setText(StatusBar.CELL_MAIN, al.getRevisionInfo().getRevision().substring(0, 7) + " - " + al.getAuthor().toString() + ": " + recentStatusMessage); // NOI18N
        } else {
            clearRecentFeedback();
        }
    }
    
    /**
     * Clears the status bar if it contains the latest status message
     * displayed by this annotation bar.
     */
    private void clearRecentFeedback() {
        StatusBar statusBar = editorUI.getStatusBar();
        if (statusBar.getText(StatusBar.CELL_MAIN) == recentStatusMessage) {
            statusBar.setText(StatusBar.CELL_MAIN, "");  // NOI18N
        }
    }

    /**
     * Components created by SibeBarFactory are positioned
     * using a Layout manager that determines componnet size
     * by retireving preferred size.
     *
     * <p>Once componnet needs resizing it simply calls
     * {@link #revalidate} that triggers new layouting
     * that consults prefered size.
     */
    @Override
    public Dimension getPreferredSize() {
        Dimension dim = textComponent.getSize();
        int width = annotated ? getBarWidth() : 0;
        dim.width = width;
        dim.height *=2;  // XXX
        return dim;
    }

    /**
     * Gets the preferred width of this component.
     *
     * @return the preferred width of this component
     */
    private int getBarWidth() {
        JTextComponent component = editorUI.getComponent();
        if (component == null) {
            return 0;
        }
        String longestString = "";  // NOI18N
        if (elementAnnotations == null) {
            longestString = elementAnnotationsSubstitute;
        } else {
            synchronized(elementAnnotations) {
                for (AnnotateLine line : elementAnnotations.values()) {
                    String displayName = getDisplayName(line); // NOI18N
                    if (displayName.length() > longestString.length()) {
                        longestString = displayName;
                    }
                }
            }
        }
        char[] data = longestString.toCharArray();
        int w = getGraphics().getFontMetrics(component.getFont()).charsWidth(data, 0,  data.length);
        return w + 4;
    }

    @NbBundle.Messages({
        "MSG_Blame.noDisplayField=Select item to show"
    })
    private String getDisplayName(AnnotateLine line) {
        StringBuilder sb = new StringBuilder(20);
        if (line.getRevisionInfo() != null) {
            for (DisplayField field : getDisplayedFields()) {
                sb.append(field.format(line)).append(" ");
            }
            if (sb.length() == 0) {
                sb.append(Bundle.MSG_Blame_noDisplayField());
            } else {
                sb.delete(sb.length() - 1, sb.length());
            }
        }
        return sb.toString();
    }

    /**
     * Pair method to {@link #annotate}. It releases
     * all resources.
     */
    private void release() {
        editorUI.removePropertyChangeListener(this);
        textComponent.removeComponentListener(this);
        textComponent.removePropertyChangeListener(this);
        doc.removeDocumentListener(this);
        if (caret != null) {
            caret.removeChangeListener(this);
        }
        if (caretTimer != null) {
            caretTimer.removeActionListener(this);
        }
        elementAnnotations = null;
        previousRevisions = null;
        // cancel running annotation task if active
        if(latestAnnotationTask != null) {
            latestAnnotationTask.cancel();
        }
        AnnotationMarkProvider amp = AnnotationMarkInstaller.getMarkProvider(textComponent);
        if (amp != null) {
            amp.setMarks(Collections.<AnnotationMark>emptyList());
        }

        clearRecentFeedback();
    }

    /**
     * Paints one view that corresponds to a line (or
     * multiple lines if folding takes effect).
     */
    private void paintView(View view, Graphics g, int yBase) {
        JTextComponent component = editorUI.getComponent();
        if (component == null) return;
        BaseTextUI textUI = (BaseTextUI)component.getUI();

        Element rootElem = textUI.getRootView(component).getElement();
        int line = rootElem.getElementIndex(view.getStartOffset());

        String annotation = "";  // NOI18N
        AnnotateLine al = null;
        if (elementAnnotations != null) {
            al = getAnnotateLine(line);
            if (al != null) {
                annotation = getDisplayName(al);  // NOI18N
            }
        } else {
            annotation = elementAnnotationsSubstitute;
        }

        if (al != null && al.getRevisionInfo().getRevision().equals(recentRevision)) {
            g.setColor(selectedColor());
        } else {
            g.setColor(foregroundColor());
        }
        int texty = yBase + editorUI.getLineAscent();
        int textx = 2;
        g.setFont(component.getFont());
        g.drawString(annotation, textx, texty);
    }

    /**
     * Presents commit message as tooltips.
     */
    @Override
    public String getToolTipText (MouseEvent e) {
        if (editorUI == null)
            return null;
        int line = getLineFromMouseEvent(e);

        StringBuilder annotation = new StringBuilder();
        if (elementAnnotations != null) {
            AnnotateLine al = getAnnotateLine(line);

            if (al != null && al.getRevisionInfo() != null) {
                String escapedAuthor = NbBundle.getMessage(AnnotationBar.class, "TT_Annotation"); // NOI18N
                try {
                    escapedAuthor = XMLUtil.toElementContent(al.getAuthor().toString());
                } catch (CharConversionException e1) {
                    LOG.log(Level.INFO, "HG.AB: can not HTML escape: ", al.getAuthor());  // NOI18N
                }

                // always return unique string to avoid tooltip sharing on mouse move over same revisions -->
                annotation.append("<html><!-- line=").append(line++).append(" -->").append(al.getRevisionInfo().getRevision().substring(0, 7)).append(" - <b>").append(escapedAuthor).append("</b>"); // NOI18N
                annotation.append(" ").append(DateFormat.getDateInstance().format(new Date(al.getRevisionInfo().getCommitTime()))); // NOI18N
                String message = al.getRevisionInfo().getFullMessage();
                if (message != null) {
                    String escaped = null;
                    try {
                        escaped = XMLUtil.toElementContent(message);
                    } catch (CharConversionException e1) {
                        LOG.log(Level.INFO, "HG.AB: can not HTML escape: ", message);  // NOI18N
                    }
                    if (escaped != null) {
                        String lined = escaped.replace("\n", "<br>");  // NOI18N
                        annotation.append("<p>").append(lined); // NOI18N
                    }
                }
            }
        } else {
            annotation.append(elementAnnotationsSubstitute);
        }

        return annotation.toString();
    }

    /**
     * Locates AnnotateLine associated with given line. The
     * line is translated to Element that is used as map lookup key.
     * The map is initially filled up with Elements sampled on
     * annotate() method.
     *
     * <p>Key trick is that Element's identity is maintained
     * until line removal (and is restored on undo).
     *
     * @param line
     * @return found AnnotateLine or <code>null</code>
     */
    private AnnotateLine getAnnotateLine(int line) {
        StyledDocument sd = (StyledDocument) doc;
        int lineOffset = NbDocument.findLineOffset(sd, line);
        Element element = sd.getParagraphElement(lineOffset);
        AnnotateLine al = elementAnnotations.get(element);

        if (al != null) {
            int startOffset = element.getStartOffset();
            int endOffset = element.getEndOffset();
            try {
                int len = endOffset - startOffset;
                String text = doc.getText(startOffset, len -1);
                String content = al.getContent();
                if (text.equals(content)) {
                    return al;
                }
            } catch (BadLocationException e) {
                LOG.log(Level.INFO, "HG.AB: can not locate line annotation.");  // NOI18N
            }
        }

        return null;
    }

    /**
     * GlyphGutter copy pasted bolerplate method.
     * It invokes {@link #paintView} that contains
     * actual business logic.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Rectangle clip = g.getClipBounds();

        JTextComponent component = editorUI.getComponent();
        if (component == null) return;

        BaseTextUI textUI = (BaseTextUI)component.getUI();
        View rootView = Utilities.getDocumentView(component);
        if (rootView == null) return;

        g.setColor(backgroundColor());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
        if (g instanceof Graphics2D && renderingHints != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.addRenderingHints(renderingHints);
        }

        AbstractDocument doc = (AbstractDocument)component.getDocument();
        doc.readLock();
        try{
            foldHierarchy.lock();
            try{
                int startPos = textUI.getPosFromY(clip.y);
                int startViewIndex = rootView.getViewIndex(startPos,Position.Bias.Forward);
                int rootViewCount = rootView.getViewCount();

                if (startViewIndex >= 0 && startViewIndex < rootViewCount) {
                    int clipEndY = clip.y + clip.height;
                    for (int i = startViewIndex; i < rootViewCount; i++){
                        View view = rootView.getView(i);
                        Rectangle rec = component.modelToView(view.getStartOffset());
                        if (rec == null) {
                            break;
                        }
                        int y = rec.y;
                        paintView(view, g, y);
                        if (y >= clipEndY) {
                            break;
                        }
                    }
                }

            } finally {
                foldHierarchy.unlock();
            }
        } catch (BadLocationException ble){
            LOG.log(Level.WARNING, null, ble);
        } finally {
            doc.readUnlock();
        }
    }

    private Color backgroundColor() {
        if (textComponent != null) {
            return textComponent.getBackground();
        }
        return backgroundColor;
    }

    private Color foregroundColor() {
        if (textComponent != null) {
            return textComponent.getForeground();
        }
        return foregroundColor;
    }

    private Color selectedColor() {
        if (backgroundColor.equals(backgroundColor())) {
            return selectedColor;
        }
        if (textComponent != null) {
            return textComponent.getCaretColor();
        }
        return selectedColor;

    }


    /** GlyphGutter copy pasted utility method. */
    private int getLineFromMouseEvent(MouseEvent e){
        int line = -1;
        if (editorUI != null) {
            try{
                JTextComponent component = editorUI.getComponent();
                BaseTextUI textUI = (BaseTextUI)component.getUI();
                int clickOffset = textUI.viewToModel(component, new Point(0, e.getY()));
                line = Utilities.getLineOffset(doc, clickOffset);
            }catch (BadLocationException ble){
            }
        }
        return line;
    }

    /** Implementation */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null) return;
        String id = evt.getPropertyName();
        if (evt.getSource() == textComponent) {
            if ("caret".equals(id)) {
                if (caret != null) {
                    caret.removeChangeListener(this);
                }
                caret = textComponent.getCaret();
                if (caret != null) {
                    caret.addChangeListener(this);
                }
            }
            return;
        }
        if (EditorUI.COMPONENT_PROPERTY.equals(id)) {  // NOI18N
            if (evt.getNewValue() == null){
                // component deinstalled, lets uninstall all isteners
                release();
            }
        }

    }

    /** Implementation */
    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    /** Implementation */
    @Override
    public void insertUpdate(DocumentEvent e) {
        // handle new lines,  Enter hit at end of line changes
        // the line element instance
        // XXX Actually NB document implementation triggers this method two times
        //  - first time with one removed and two added lines
        //  - second time with two removed and two added lines
        if (elementAnnotations != null) {
            Element[] elements = e.getDocument().getRootElements();
            synchronized(elementAnnotations) { // atomic change
                for (int i = 0; i < elements.length; i++) {
                    Element element = elements[i];
                    DocumentEvent.ElementChange change = e.getChange(element);
                    if (change == null) continue;
                    Element[] removed = change.getChildrenRemoved();
                    Element[] added = change.getChildrenAdded();

                    if (removed.length == added.length) {
                        for (int c = 0; c<removed.length; c++) {
                            AnnotateLine recent = elementAnnotations.get(removed[c]);
                            if (recent != null) {
                                elementAnnotations.remove(removed[c]);
                                elementAnnotations.put(added[c], recent);
                            }
                        }
                    } else if (removed.length == 1 && added.length > 0) {
                        Element key = removed[0];
                        AnnotateLine recent = elementAnnotations.get(key);
                        if (recent != null) {
                            elementAnnotations.remove(key);
                            elementAnnotations.put(added[0], recent);
                        }
                    }
                }
            }
        }
        repaint();
    }

    /** Implementation */
    @Override
    public void removeUpdate(DocumentEvent e) {
        final int length = e.getDocument().getLength();
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run () {
                if (length == 0) { // external reload
                    hideBar();
                }
                repaint();
            }
        });
    }

    /** Caret */
    @Override
    public void stateChanged(ChangeEvent e) {
        assert e.getSource() == caret;
        caretTimer.restart();
    }

    /** Timer */
    @Override
    public void actionPerformed(ActionEvent e) {
        assert e.getSource() == caretTimer;
        onCurrentLine();
    }

    /** on JTextPane */
    @Override
    public void componentHidden(ComponentEvent e) {
    }

    /** on JTextPane */
    @Override
    public void componentMoved(ComponentEvent e) {
    }

    /** on JTextPane */
    @Override
    public void componentResized(ComponentEvent e) {
        revalidate();
    }

    /** on JTextPane */
    @Override
    public void componentShown(ComponentEvent e) {
    }

    private Map<String, String> getPreviousRevisions () {
        Map<String, String> revisions = previousRevisions;
        return revisions == null ? new HashMap<>(0) : revisions;
    }

    /**
     * Sets the file for which the annotations are displayed. This file can differ from the displayed one when showing annotations
     * for a file in the past.
     * @param file
     */
    void setReferencedFile(File file) {
        this.referencedFile = FileUtil.normalizeFile(file);
        this.referencedFileObject = FileUtil.toFileObject(file);
    }

    private static Set<DisplayField> getDisplayedFields () {
        assert EventQueue.isDispatchThread();
        EnumSet<DisplayField> fields = EnumSet.noneOf(DisplayField.class);
        if (!fieldsInitialized) {
            fieldsInitialized = true;
            int stored = GitModuleConfig.getDefault().getAnnotationDisplayedFields(DisplayField.COMMIT_ID.value + DisplayField.AUTHOR.value);
            for (DisplayField field : DisplayField.values()) {
                field.visible = (stored & field.value) != 0;
            }
        }
        for (DisplayField field : DisplayField.values()) {
            if (field.visible) {
                fields.add(field);
            }
        }
        return fields;
    }
}

