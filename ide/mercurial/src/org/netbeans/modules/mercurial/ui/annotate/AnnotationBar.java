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

package org.netbeans.modules.mercurial.ui.annotate;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.accessibility.Accessible;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.StatusBar;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.WorkingCopyInfo;
import org.netbeans.modules.mercurial.ui.diff.DiffAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.mercurial.ui.log.LogAction;
import org.netbeans.modules.mercurial.ui.update.RevertModifications;
import org.netbeans.modules.mercurial.ui.update.RevertModificationsAction;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;

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
    private Map<Element, AnnotateLine> elementAnnotations = Collections.<Element, AnnotateLine>emptyMap();

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
    private Map<String, HgRevision> previousRevisions;

    /*
     * Holds original file names for each revision.
     * File may be renamed in previous revision and cat and blame do not work when called on the new filename.
     */
    private Map<String, File> originalFiles;
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
    private String annotatedRevision;
    private WorkingCopyInfo wcInfo;
    private RequestProcessor.Task refreshAnnotationsTask;
    private boolean refreshing;
    
    /**
     * Rendering hints for annotations sidebar inherited from editor settings.
     */
    private final Map renderingHints;

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
        elementAnnotations = Collections.<Element, AnnotateLine>emptyMap();

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
    public void annotationLines(File file, List<AnnotateLine> annotateLines) {
        // set repository root for popup menu, now should be the right time
        repositoryRoot = Mercurial.getInstance().getRepositoryRoot(getCurrentFile());
        if (referencedFile == null) {
            wcInfo = WorkingCopyInfo.getInstance(repositoryRoot);
            wcInfo.removePropertyChangeListener(this);
            wcInfo.addPropertyChangeListener(this);
        }
        final List<AnnotateLine> lines = new LinkedList<AnnotateLine>(annotateLines);
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

                for (Difference d : differences) {
                    int offset, editorStart;
                    if (d.getType() == Difference.ADD) {
                        offset = d.getSecondEnd() - d.getSecondStart() + 1;
                        editorStart = d.getFirstStart();
                    } else if (d.getType() == Difference.DELETE) {
                        offset = d.getFirstEnd() - d.getFirstStart() + 1;
                        editorStart = d.getFirstEnd();
                        for (int c = editorStart - offset; c < editorStart; c++) {
                            ann2editorPermutation[c] = -1;
                        }
                        offset = -offset;
                    } else {
                        // change
                        int firstLen = d.getFirstEnd() - d.getFirstStart();
                        int secondLen = d.getSecondEnd() - d.getSecondStart();
                        offset = secondLen - firstLen;
                        if (offset == 0) continue;
                        editorStart = d.getFirstEnd();
                        for (int c = d.getFirstStart(); c < editorStart; c++) {
                            ann2editorPermutation[c] += -1;
                        }
                    }
                    for (int c = editorStart; c < lineCount; c++) {
                        ann2editorPermutation[c] += offset;
                    }
                }

            } catch (IOException e) {
                Mercurial.LOG.log(Level.INFO, "Cannot compute local diff required for annotations, ignoring...");  // NOI18N 
            }
        }

        doc.render(new Runnable() {
            @Override
            public void run() {
                StyledDocument sd = (StyledDocument) doc;
                Iterator<AnnotateLine> it = lines.iterator();
                previousRevisions = Collections.synchronizedMap(new HashMap<String, HgRevision>());
                originalFiles = Collections.synchronizedMap(new HashMap<String, File>());
                elementAnnotations = Collections.synchronizedMap(new HashMap<Element, AnnotateLine>(lines.size()));
                while (it.hasNext()) {
                    AnnotateLine line = it.next();
                    int lineNum = ann2editorPermutation[line.getLineNum() -1];
                    if (lineNum == -1) {
                        continue;
                    }
                    try {
                        int lineOffset = NbDocument.findLineOffset(sd, lineNum -1);
                        Element element = sd.getParagraphElement(lineOffset);                        
                        elementAnnotations.put(element, line);
                    } catch (IndexOutOfBoundsException ex) {
                        // TODO how could I get line behind document end?
                        // furtunately user does not spot it
                        Mercurial.LOG.log(Level.INFO, null, ex);
                    }
                }
            }
        });

        final String url = HgUtils.getRemoteRepository(repositoryRoot);
        // lazy listener registration
        caret = textComponent.getCaret();
        if (caret != null) {
            caret.addChangeListener(this);
        }
        textComponent.addPropertyChangeListener(this);
        this.caretTimer = new Timer(500, this);
        caretTimer.setRepeats(false);

        elementAnnotationsSubstitute = "";
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
        if (!elementAnnotations.isEmpty()) {
            al = getAnnotateLine(getLineFromMouseEvent(event));
        }

        /**
         * al.getCommitMessage() != null - since commit messages are initialized separately from the AL constructor
         */
        if (al != null && al.getCommitMessage() != null) {
            TooltipWindow ttw = new TooltipWindow(this, al);
            ttw.show(new Point(p.x - p2.x, p.y));
        }
    }

    @NbBundle.Messages({
        "# {0} - changeset id", "CTL_AnnotationBar.action.showCommit=Show Revision {0}"
    })
    private JPopupMenu createPopup(MouseEvent e) {
        final ResourceBundle loc = NbBundle.getBundle(AnnotationBar.class);
        final JPopupMenu popupMenu = new JPopupMenu();

        final JMenuItem diffMenu = new JMenuItem(loc.getString("CTL_MenuItem_DiffToRevision")); // NOI18N

        // annotation for target line
        AnnotateLine al = null;
        if (!elementAnnotations.isEmpty()) {
            al = getAnnotateLine(getLineFromMouseEvent(e));
        }
        // revision previous to target line's revision
        final String revisionPerLine = al == null ? null : al.getRevision();
        final String changesetIdPerLine = al == null ? null : al.getId();
        final int lineNumber = al == null ? -1 : al.getPreviousLineNumber();
        // used in menu Revert
        final File file = getCurrentFile();
        // used in diff menu, repository root set while computing revision
        // denotes the path of the file in the showing revision
        final File originalFile = al == null ? null : new File(repositoryRoot, al.getFileName());
        final boolean revisionCanBeRolledBack = al == null || referencedFile != null ? false : al.canBeRolledBack();
        
        diffMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final PreviousRevisionInvoker pri = new PreviousRevisionInvoker(revisionPerLine, originalFile);
                pri.runWithRevision(new Runnable() {
                    @Override
                    public void run() {
                        DiffAction.diff(pri.getOriginalFile(), pri.getPreviousRevision(), originalFile, new HgRevision(changesetIdPerLine, revisionPerLine),
                                lineNumber > 1 ? lineNumber - 1 : lineNumber);
                    }
                }, true);
            }
        });
        popupMenu.add(diffMenu);
        if (changesetIdPerLine != null) {
            JMenuItem showCommitMenu = new JMenuItem(Bundle.CTL_AnnotationBar_action_showCommit(revisionPerLine));
            showCommitMenu.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    LogAction.openHistory(repositoryRoot, new File[] { file }, changesetIdPerLine);
                }
            });
            popupMenu.add(showCommitMenu);
        }

        JMenuItem rollbackMenu = new JMenuItem(loc.getString("CTL_MenuItem_Revert")); // NOI18N
        rollbackMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                revert(file, revisionPerLine);
            }
        });
        popupMenu.add(rollbackMenu);
        rollbackMenu.setEnabled(revisionCanBeRolledBack);

        // an action showing annotation for line's revisions
        final JMenuItem annotationsForSelectedItem = new JMenuItem(NbBundle.getMessage(AnnotationBar.class, "CTL_MenuItem_ShowAnnotationsPrevious", revisionPerLine)); //NOI18N
        annotationsForSelectedItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Mercurial.getInstance().getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HgUtils.openInRevision(originalFile, lineNumber > 0 ? lineNumber - 1 : -1, new HgRevision(changesetIdPerLine, revisionPerLine), true);
                        } catch (IOException ex) {
                            //
                        }
                    }
                });
            }
        });
        popupMenu.add(annotationsForSelectedItem);

        // an action showing annotation for previous revisions
        final JMenuItem previousAnnotationsMenu = new JMenuItem();
        previousAnnotationsMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final PreviousRevisionInvoker pri = new PreviousRevisionInvoker(revisionPerLine, originalFile);
                pri.runWithRevision (new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (lineNumber > 0 && !"-1".equals(pri.getPreviousRevision().getRevisionNumber())) {
                                HgUtils.openInRevision(originalFile, new HgRevision(changesetIdPerLine, revisionPerLine), lineNumber - 1,
                                        pri.getOriginalFile(), pri.getPreviousRevision(), true); //NOI18N
                            } else {
                                HgUtils.openInRevision(pri.getOriginalFile(), -1,
                                    pri.getPreviousRevision(), !"-1".equals(pri.getPreviousRevision().getRevisionNumber())); //NOI18N
                            }
                        } catch (IOException ex) {
                            //
                        }
                    }
                }, false);
            }
        });
        popupMenu.add(previousAnnotationsMenu);

        JMenuItem menu;
        menu = new JMenuItem(loc.getString("CTL_MenuItem_CloseAnnotations")); // NOI18N
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hideBar();
            }
        });
        JSeparator separator = new JPopupMenu.Separator();
        popupMenu.add(separator);
        popupMenu.add(menu);

        diffMenu.setVisible(false);
        annotationsForSelectedItem.setVisible(false);
        previousAnnotationsMenu.setVisible(false);
        rollbackMenu.setVisible(false);
        separator.setVisible(false);
        if (revisionPerLine != null && changesetIdPerLine != null) {
            String key = getPreviousRevisionKey(originalFile.getAbsolutePath(), revisionPerLine);
            HgRevision previousRevision = getPreviousRevisions().get(key); // get from cache
            if (al.canBeRolledBack() && (previousRevision != null || !getPreviousRevisions().containsKey(key))) {
                if (!getPreviousRevisions().containsKey(key)) {
                    // get revision in a bg thread and cache the value
                    Mercurial.getInstance().getRequestProcessor().post(new Runnable() {
                        @Override
                        public void run() {
                            getParentRevision(originalFile, revisionPerLine);
                        }
                    });
                }
                String format = loc.getString("CTL_MenuItem_DiffToRevision"); // NOI18N
                String previousRevisionNumber = previousRevision == null ? null : previousRevision.getRevisionNumber();
                diffMenu.setText(MessageFormat.format(format, new Object [] { revisionPerLine, previousRevisionNumber == null ? loc.getString("LBL_PreviousRevision") : previousRevisionNumber})); //NOI18N
                diffMenu.setVisible(originalFile != null);
                rollbackMenu.setVisible(true);
                separator.setVisible(true);
                format = loc.getString("CTL_MenuItem_ShowAnnotationsPrevious"); // NOI18N
                previousAnnotationsMenu.setText(MessageFormat.format(format, new Object [] { previousRevisionNumber == null ? loc.getString("LBL_PreviousRevision") : previousRevisionNumber})); //NOI18N
                previousAnnotationsMenu.setVisible(originalFile != null);
                previousAnnotationsMenu.setEnabled(!"-1".equals(previousRevisionNumber)); //NOI18N
                annotationsForSelectedItem.setVisible(originalFile != null && revisionPerLine != null && !revisionPerLine.equals(annotatedRevision));
            }
        }

        return popupMenu;
    }

    void setAnnotatedRevision (String revision) {
        this.annotatedRevision = revision;
    }

    private RequestProcessor.Task getRefreshAnnotationsTask () {
        assert EventQueue.isDispatchThread();
        if (refreshAnnotationsTask == null) {
            refreshAnnotationsTask = getRequestProcessor().create(new Runnable() {
                @Override
                public void run () {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            if (textComponent instanceof JEditorPane && refreshing) {
                                AnnotateAction.showAnnotations((JEditorPane) textComponent, getCurrentFile(), null, false);
                            }
                        }
                    });
                }
            });
        }
        return refreshAnnotationsTask;
    }

    boolean isAnnotated() {
        return annotated;
    }

    /**
     * Class for running a code after the previous revision is determined, which may take some time
     */
    private class PreviousRevisionInvoker {
        private final String revisionPerLine;
        private final File file;
        private HgRevision previousRevision;
        private File originalFile;

        private PreviousRevisionInvoker(String revisionPerLine, File originalFile) {
            this.revisionPerLine = revisionPerLine;
            this.file = originalFile;
        }

        /**
         * Runs the given runnable after the previous revision is determined
         * @param runnable
         */
        private void runWithRevision (final Runnable runnable, final boolean inAWT) {
            if (revisionPerLine != null) {
                // getting the prevoius revision may take some time, running in bg
                new HgProgressSupport() {
                    @Override
                    protected void perform() {
                        previousRevision = getParentRevision(file, revisionPerLine);
                        if (!isCanceled() && previousRevision != null) {
                            originalFile = AnnotationBar.this.getOriginalFile(file, revisionPerLine);
                            if (!isCanceled() && file != null) {
                                if (inAWT) {
                                    EventQueue.invokeLater(runnable);
                                } else {
                                    getRequestProcessor().post(runnable);
                                }
                            }
                        }
                    }
                }.start(Mercurial.getInstance().getRequestProcessor(), repositoryRoot,
                        NbBundle.getMessage(AnnotationBar.class, "MSG_GettingPreviousRevision")); //NOI18N
            }
        }

        private HgRevision getPreviousRevision() {
            return previousRevision;
        }

        private File getOriginalFile () {
            return originalFile;
        }
    }

    private void revert(final File file, String revision) {
        final File root = Mercurial.getInstance().getRepositoryRoot(file);
        if(root == null) return;
        
        File[] files = new File [1];
        files[0] = file; 
        final RevertModifications revertModifications = new RevertModifications(root, files, revision);
        if(!revertModifications.showDialog()) {
            return;
        }
        final String revStr =  revertModifications.getSelectionRevision();
        final boolean doBackup = revertModifications.isBackupRequested();

        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {                 
                RevertModificationsAction.performRevert(root, revStr, file, doBackup, this.getLogger());
            }
        };
        support.start(rp, root, NbBundle.getMessage(AnnotationBar.class, "MSG_Revert_Progress")); // NOI18N
    }

    private HgRevision getParentRevision(File file, String revision) {
        String key = getPreviousRevisionKey(file.getAbsolutePath(), revision);
        HgRevision parent = getPreviousRevisions().get(key);
        if (parent == null) {
            File originalFile = getOriginalFile(file, revision);
            try {
                parent = HgCommand.getParent(repositoryRoot, originalFile, revision);
            } catch (HgException ex) {
                Mercurial.LOG.log(Level.INFO, null, ex);
            }
            getPreviousRevisions().put(key, parent);
        }
        return parent;
    }

    private File getOriginalFile (File file, String revision) {
        String key = getPreviousRevisionKey(file.getAbsolutePath(), revision);
        File originalFile = getOriginalFiles().get(key);
        if (originalFile == null) {
            HgLogMessage[] msg = HgCommand.getLogMessages(repositoryRoot, Collections.singleton(file), revision, revision, true, true, false, 1, 
                    Collections.<String>emptyList(), OutputLogger.getLogger(null), true);
            if (msg.length > 0) {
                originalFile = msg[0].getOriginalFile(repositoryRoot, file);
                if (originalFile != null) {
                    getOriginalFiles().put(key, originalFile);
                }
            }
        }
        return originalFile == null ? file : originalFile;
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
            Mercurial.LOG.log(Level.SEVERE, "Can not get line for caret at offset ", offset); // NOI18N
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
        String revision = al.getRevision();
        if (revision.equals(recentRevision) == false) {
            recentRevision = revision;
            repositoryRoot = Mercurial.getInstance().getRepositoryRoot(getCurrentFile());
            repaint();

            AnnotationMarkProvider amp = AnnotationMarkInstaller.getMarkProvider(textComponent);
            if (amp != null) {
            
                List<AnnotationMark> marks = new ArrayList<AnnotationMark>(elementAnnotations.size());
                // I cannot affort to lock elementAnnotations for long time
                // it's accessed from editor thread too
                Iterator<Map.Entry<Element, AnnotateLine>> it2;
                synchronized(elementAnnotations) {
                    it2 = new HashSet<Map.Entry<Element, AnnotateLine>>(elementAnnotations.entrySet()).iterator();
                }
                while (it2.hasNext()) {
                    Map.Entry<Element, AnnotateLine> next = it2.next();                        
                    AnnotateLine annotateLine = next.getValue();
                    if (revision.equals(annotateLine.getRevision())) {
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

        if (al.getCommitMessage() != null) {
            recentStatusMessage = al.getCommitMessage();
            statusBar.setText(StatusBar.CELL_MAIN, al.getRevision() + ":" + al.getId() + " - " + al.getAuthor() + ": " + recentStatusMessage); // NOI18N
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
        String longestString = "";  // NOI18N
        if (elementAnnotations.isEmpty()) {
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
        int w = getGraphics().getFontMetrics().charsWidth(data, 0,  data.length);
        return w + 4;
    }

    private String getDisplayName(AnnotateLine line) {
        return line.getRevision() + "  " + line.getUsername(); // NOI18N
    }

    /**
     * Pair method to {@link #annotate}. It releases
     * all resources.
     */
    private void release() {
        refreshing = false;
        if (refreshAnnotationsTask != null) {
            refreshAnnotationsTask.cancel();
        }
        if (wcInfo != null) {
            wcInfo.removePropertyChangeListener(this);
        }
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
        elementAnnotations = Collections.<Element, AnnotateLine>emptyMap();
        previousRevisions = null;
        originalFiles = null;
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
        if (!elementAnnotations.isEmpty()) {
            al = getAnnotateLine(line);
            if (al != null) {
                annotation = getDisplayName(al);  // NOI18N
            }
        } else {
            annotation = elementAnnotationsSubstitute;
        }

        if (al != null && al.getRevision().equals(recentRevision)) {
            g.setColor(selectedColor());
        } else {
            g.setColor(foregroundColor());
        }
        int texty = yBase + editorUI.getLineAscent();
        int textx = 2;
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
        if (!elementAnnotations.isEmpty()) {
            AnnotateLine al = getAnnotateLine(line);

            if (al != null) {
                String escapedAuthor = NbBundle.getMessage(AnnotationBar.class, "TT_Annotation"); // NOI18N
                try {
                    escapedAuthor = XMLUtil.toElementContent(al.getAuthor());
                } catch (CharConversionException e1) {
                    Mercurial.LOG.log(Level.INFO, "HG.AB: can not HTML escape: ", al.getAuthor());  // NOI18N
                }

                // always return unique string to avoid tooltip sharing on mouse move over same revisions -->
                annotation.append("<html><!-- line=").append(line++).append(" -->").append(al.getRevision()).append(":").append(al.getId()).append(" - <b>").append(escapedAuthor).append("</b>"); // NOI18N
                if (al.getDate() != null) {
                    annotation.append(" ").append(DateFormat.getDateInstance().format(al.getDate())); // NOI18N                    
                }
                if (al.getCommitMessage() != null) {
                    String escaped = null;
                    try {
                        escaped = XMLUtil.toElementContent(al.getCommitMessage());
                    } catch (CharConversionException e1) {
                        Mercurial.LOG.log(Level.INFO, "HG.AB: can not HTML escape: ", al.getCommitMessage());  // NOI18N
                    }
                    if (escaped != null) {
                        String lined = escaped.replace("\r\n", "\n").replace("\r", "\n").replace("\n", "<br>");  // NOI18N
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
                Mercurial.LOG.log(Level.INFO, "HG.AB: can not locate line annotation.");  // NOI18N
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
            Mercurial.LOG.log(Level.WARNING, null, ble);
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
        } else if (WorkingCopyInfo.PROPERTY_WORKING_COPY_PARENT.equals(id)) {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run () {
                    if (annotated) {
                        hideBar();
                        refreshing = true;
                        getRefreshAnnotationsTask().schedule(1000);
                    }
                }
            });
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
        if (!elementAnnotations.isEmpty()) {
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
    public void removeUpdate(final DocumentEvent e) {
        final int length = e.getDocument().getLength();
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run () {
                if (length == 0) { // external reload
                    hideBar();
                    if (referencedFile == null) {
                        refreshing = true;
                        getRefreshAnnotationsTask().schedule(4000);
                    }
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

    private static String getPreviousRevisionKey(String filePath, String revision) {
        return filePath + "#" + revision;                               //NOI18N
    }

    private Map<String, HgRevision> getPreviousRevisions () {
        Map<String, HgRevision> revisions = previousRevisions;
        return revisions == null ? new HashMap<String, HgRevision>(0) : revisions;
    }

    private Map<String, File> getOriginalFiles () {
        Map<String, File> files = originalFiles;
        return files == null ? new HashMap<String, File>(0) : files;
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
}

