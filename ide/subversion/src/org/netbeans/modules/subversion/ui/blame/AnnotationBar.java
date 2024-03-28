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

package org.netbeans.modules.subversion.ui.blame;

import java.text.ParseException;
import org.netbeans.editor.*;
import org.netbeans.editor.Utilities;
import org.netbeans.api.editor.fold.*;
import org.netbeans.api.diff.*;
import org.netbeans.spi.diff.*;
import org.netbeans.modules.subversion.ui.update.RevertModifications;
import org.netbeans.modules.subversion.ui.update.RevertModificationsAction;
import org.netbeans.modules.subversion.ui.diff.DiffAction;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.text.*;
import org.openide.util.*;
import org.openide.xml.*;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.SVNRevision;
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
import java.io.*;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.logging.Level;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNClientException;

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
     * Rendering hints for annotations sidebar inherited from editor settings.
     */
    private final Map renderingHints;

    private File file;
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

    /**
     * Creates new instance initializing final fields.
     */
    public AnnotationBar(JTextComponent target) {
        this.textComponent = target;
        this.editorUI = Utilities.getEditorUI(target);
        this.foldHierarchy = FoldHierarchy.get(editorUI.getComponent());
        this.doc = editorUI.getDocument();
        if (textComponent instanceof JEditorPane) {
            String mimeType = org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(textComponent);
            FontColorSettings fcs = MimeLookup.getLookup(mimeType).lookup(FontColorSettings.class);
            renderingHints = (Map) fcs.getFontColors(FontColorNames.DEFAULT_COLORING).getAttribute(EditorStyleConstants.RenderingHints);
        } else {
            renderingHints = null;
        }
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        elementAnnotationsSubstitute = "";                              //NOI18N
    }

    public File getFile() {
        return file;
    }

    Document getdDocument() {
        return doc;
    }
    
    // public contract ~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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
        if (message == null) {
            throw new NullPointerException("Parameter cannot be null"); //NOI18N
        }
        elementAnnotationsSubstitute = message;
        revalidate();
    }
    
    /**
     * Result computed show it...
     * Takes AnnotateLines and shows them.
     */
    public void annotationLines(final File file, List<AnnotateLine> annotateLines) {
        this.file = file;
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
                Subversion.LOG.log(Level.INFO, "Cannot compute local diff required for annotations, ignoring...", e);
            }
        }

        doc.render(new Runnable() {
            @Override
            public void run() {
                StyledDocument sd = (StyledDocument) doc;
                Iterator<AnnotateLine> it = lines.iterator();
                elementAnnotations = Collections.synchronizedMap(new HashMap<Element, AnnotateLine>(lines.size()));
                while (it.hasNext()) {
                    AnnotateLine line = it.next();
                    int lineNum = ann2editorPermutation[line.getLineNum() -1];
                    try {
                        int lineOffset = NbDocument.findLineOffset(sd, lineNum -1);
                        Element element = sd.getParagraphElement(lineOffset);                        
                        elementAnnotations.put(element, line);
                    } catch (IndexOutOfBoundsException ex) {
                        // TODO how could I get line behind document end?
                        // furtunately user does not spot it
                        Subversion.LOG.log(Level.INFO, null, ex);
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

    private ISVNNotifyListener svnClientListener;
    
    void setSVNClienListener(ISVNNotifyListener svnClientListener) {
        this.svnClientListener = svnClientListener;
        
        File file = getCurrentFile();        
        Subversion.getInstance().addSVNNotifyListener(svnClientListener);        
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
    
    /**
     * Registers "close" popup menu, tooltip manager
     * and repaint on documet change manager.
     */
    @Override
    public void addNotify() {
        super.addNotify();


        this.addMouseListener(new MouseAdapter() {
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
        if (al != null && al.getCommitMessage() != null) {
            TooltipWindow ttw = new TooltipWindow(this, al);
            ttw.show(new Point(p.x - p2.x, p.y));
        }
    }

    @NbBundle.Messages({
        "# {0} - revision number", "MSG_RollbackTo.menuItem=Rollback to {0}",
        "MSG_RollbackToPrevious.menuItem=Rollback to previous Revision"
    })
    private JPopupMenu createPopup(MouseEvent e) {
        final ResourceBundle loc = NbBundle.getBundle(AnnotationBar.class);
        final JPopupMenu popupMenu = new JPopupMenu();

        final JMenuItem diffMenu = new JMenuItem(loc.getString("CTL_MenuItem_DiffToRevision"));

        // annotation for target line
        AnnotateLine al = null;
        if (elementAnnotations != null) {
            al = getAnnotateLine(getLineFromMouseEvent(e));
        }
        // revision previous to target line's revision
        final String revisionPerLine = al == null ? null : al.getRevision();
        // used in menu Revert
        final File file = getCurrentFile();
        boolean revisionCanBeReverted = al == null || referencedFile != null ? false : al.canBeRolledBack();
        boolean revisionCanBeRolledBack = al == null || file == null ? false : al.canBeRolledBack();
        
        diffMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (revisionPerLine != null) {
                    if (getPreviousRevision(revisionPerLine) != null) {
                        DiffAction.diff(file, getPreviousRevision(revisionPerLine), revisionPerLine);
                    }
                }
            }
        });
        popupMenu.add(diffMenu);

        JMenuItem revertMenu = new JMenuItem(loc.getString("CTL_MenuItem_Revert"));
        revertMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                revert(file, revisionPerLine);
            }
        });
        popupMenu.add(revertMenu);
        revertMenu.setEnabled(revisionCanBeReverted);
        
        // an action reverting file's content
        JMenuItem rollbackMenu = new JMenuItem();
        rollbackMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Subversion.getInstance().getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run() {
                        if (revisionPerLine != null) {
                            rollback(file, revisionPerLine);
                        }
                    }
                });
            }
        });
        popupMenu.add(rollbackMenu);
        rollbackMenu.setVisible(false);
        JMenuItem rollbackToPreviousMenu = new JMenuItem(Bundle.MSG_RollbackToPrevious_menuItem());
        rollbackToPreviousMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Subversion.getInstance().getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run() {
                        if (revisionPerLine != null) {
                            rollback(file, getPreviousRevision(revisionPerLine));
                        }
                    }
                });
            }
        });
        popupMenu.add(rollbackToPreviousMenu);
        rollbackToPreviousMenu.setVisible(false);

        // an action showing annotation for previous revisions
        final JMenuItem previousAnnotationsMenu = new JMenuItem();
        previousAnnotationsMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Subversion.getInstance().getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run() {
                        showPreviousAnnotations(file, revisionPerLine);
                    }
                });
            }
        });
        popupMenu.add(previousAnnotationsMenu);
        previousAnnotationsMenu.setVisible(false);

        JMenuItem menu;
        menu = new JMenuItem(loc.getString("CTL_MenuItem_CloseAnnotations"));
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hideBar();
            }
        });
        popupMenu.addSeparator();
        popupMenu.add(menu);

        diffMenu.setVisible(false);
        revertMenu.setVisible(false);
        if (revisionPerLine != null) {
            String previousRevision;
            if ((previousRevision = getPreviousRevision(revisionPerLine)) != null) {
                String format = loc.getString("CTL_MenuItem_DiffToRevision");
                diffMenu.setText(MessageFormat.format(format, new Object [] { revisionPerLine, getPreviousRevision(revisionPerLine) }));
                diffMenu.setVisible(true);
                previousAnnotationsMenu.setText(loc.getString("CTL_MenuItem_ShowAnnotationsPrevious")); //NOI18N
                previousAnnotationsMenu.setVisible(file != null);
            }
            revertMenu.setVisible(true);
            rollbackMenu.setText(Bundle.MSG_RollbackTo_menuItem(revisionPerLine));
            rollbackMenu.setVisible(true);
            rollbackToPreviousMenu.setVisible(revisionCanBeRolledBack);
        }

        return popupMenu;
    }

    private void revert(File file, String revision) {
        final Context ctx = new Context(file);

        final SVNUrl url;
        try {
            url = SvnUtils.getRepositoryRootUrl(file);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }
        final RepositoryFile repositoryFile = new RepositoryFile(url, url, SVNRevision.HEAD);

        final RevertModifications revertModifications = new RevertModifications(repositoryFile, revision);
        if(!revertModifications.showDialog()) {
            return;
        }

        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(url);
        SvnProgressSupport support = new SvnProgressSupport() {
            @Override
            public void perform() {
                RevertModificationsAction.performRevert(revertModifications.getRevisionInterval(), revertModifications.revertNewFiles(), !revertModifications.revertRecursively(), ctx, this);
            }
        };
        support.start(rp, url, NbBundle.getMessage(AnnotationBar.class, "MSG_Revert_Progress")); // NOI18N
    }

    @NbBundle.Messages("MSG_Rollback_Progress=Rolling back...")
    private void rollback (final File file, String revision) {
        final SVNUrl repoUrl;
        final SVNUrl fileUrl;
        final SVNRevision svnRev;
        try {
            repoUrl = SvnUtils.getRepositoryRootUrl(file);
            fileUrl = SvnUtils.getRepositoryUrl(file);
            svnRev = SVNRevision.getRevision(revision);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        } catch (ParseException ex) {
            Subversion.LOG.log(Level.WARNING, null, ex);
            return;
        }
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repoUrl);
        SvnProgressSupport support = new SvnProgressSupport() {
            @Override
            public void perform() {
                SvnUtils.rollback(file, repoUrl, fileUrl, svnRev, false, getLogger());
            }
        };
        support.start(rp, repoUrl, Bundle.MSG_Rollback_Progress());
    }

    private void showPreviousAnnotations(final File file, String revision) {
        final SVNRevision svnRevision;
        final SVNUrl repositoryRoot;
        final SVNUrl repositoryUrl;
        try {
            repositoryRoot = SvnUtils.getRepositoryRootUrl(file);
            repositoryUrl = SvnUtils.getRepositoryUrl(file);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }
        try {
            svnRevision = SVNRevision.getRevision(getPreviousRevision(revision));
        } catch (ParseException ex) {
            Subversion.LOG.log(Level.SEVERE, "Previous revision: " + getPreviousRevision(revision), ex); //NOI18N
            return;
        }

        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repositoryRoot);
        SvnProgressSupport support = new SvnProgressSupport() {
            @Override
            public void perform() {
                try {
                    SvnClient client = Subversion.getInstance().getClient(repositoryRoot);
                    ISVNInfo info = client.getInfo(repositoryUrl, svnRevision, SVNRevision.HEAD);
                } catch (SVNClientException ex) {
                    if (SvnClientExceptionHandler.isFileNotFoundInRevision(ex.getMessage())) {
                        SvnClientExceptionHandler.annotate(NbBundle.getMessage(AnnotationBar.class, "MSG_NoFileInRevision", svnRevision.toString())); //NOI18N
                        return;
                    }
                }
                SvnUtils.openInRevision(file, repositoryRoot, repositoryUrl, svnRevision, SVNRevision.HEAD, true);
            }
        };
        support.start(rp, repositoryRoot, NbBundle.getMessage(AnnotationBar.class, "MSG_Annotation_Progress")); //NOI18N
    }

    private String getPreviousRevision(String revision) {
        return Long.toString(Long.parseLong(revision) - 1);
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
    private RequestProcessor getRequestProcessor() {
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
        // give status bar "wait" indication
        StatusBar statusBar = editorUI.getStatusBar();
        recentStatusMessage = loc.getString("CTL_StatusBar_WaitFetchAnnotation");
        statusBar.setText(StatusBar.CELL_MAIN, recentStatusMessage);
        
        // determine current line
        int line = -1;
        int offset = carett.getDot();
        try {
            line = Utilities.getLineOffset(doc, offset);
        } catch (BadLocationException ex) {
            Subversion.LOG.log(Level.SEVERE, "Can not get line for caret at offset " + offset, ex); // NOI18N
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
            statusBar.setText(StatusBar.CELL_MAIN, al.getAuthor() + ": " + recentStatusMessage); // NOI18N
        } else {
            clearRecentFeedback();
        };
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
        if (elementAnnotations == null) {
            longestString = elementAnnotationsSubstitute;
        } else {
            synchronized(elementAnnotations) {
                Iterator<AnnotateLine> it = elementAnnotations.values().iterator();
                while (it.hasNext()) {
                    AnnotateLine line = it.next();
                    String displayName = getDisplayName(line); // NOI18N
                    if (displayName.length() > longestString.length()) {
                        longestString = displayName;
                    }
                }
            }
        }
        char[] data = longestString.toCharArray();
        Graphics g = getGraphics();
        if( g != null) {
            int w = g.getFontMetrics().charsWidth(data, 0,  data.length);
            return w + 4;
        } else {
            return 0;
        }
        
    }

    private String getDisplayName(AnnotateLine line) {
        return line.getRevision() + "  " + line.getAuthor(); // NOI18N
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
        // cancel running annotation task if active
        if(latestAnnotationTask != null) {
            latestAnnotationTask.cancel();
        }
        AnnotationMarkProvider amp = AnnotationMarkInstaller.getMarkProvider(textComponent);
        if (amp != null) {
            amp.setMarks(Collections.<AnnotationMark>emptyList());
        }

        Subversion.getInstance().removeSVNNotifyListener(svnClientListener);

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
        if (annotation == null) {
            annotation = "";                                            //NOI18N
            Subversion.LOG.log(Level.WARNING, "Annotation line text is null: {0}:{1}, {2}", new Object[] {file, line, elementAnnotationsSubstitute}); //NOI18N
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
        if (elementAnnotations != null) {
            AnnotateLine al = getAnnotateLine(line);

            if (al != null) {
                String escapedAuthor = NbBundle.getMessage(AnnotationBar.class, "TT_Annotation"); // NOI18N
                try {
                    escapedAuthor = XMLUtil.toElementContent(al.getAuthor());
                } catch (CharConversionException e1) {
                    Subversion.LOG.log(Level.INFO, " can not HTML escape: " + al.getAuthor(), e1);
                }

                // always return unique string to avoid tooltip sharing on mouse move over same revisions -->
                annotation.append("<html><!-- line=").append(line++).append(" -->").append(al.getRevision()).append(" - <b>").append(escapedAuthor).append("</b>"); // NOI18N
                if (al.getDate() != null) {
                    annotation.append(" ").append(DateFormat.getDateInstance().format(al.getDate())); // NOI18N
                }
                if (al.getCommitMessage() != null) {
                    String escaped = null;
                    try {
                        escaped = XMLUtil.toElementContent(al.getCommitMessage());
                    } catch (CharConversionException e1) {
                        Subversion.LOG.log(Level.INFO, " can not HTML escape: " + al.getCommitMessage(), e1); // NOI18N
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
                Subversion.LOG.log(Level.INFO, " can not locate line annotation.", e); // NOI18N
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

        if (renderingHints != null) {
            ((Graphics2D) g).addRenderingHints(renderingHints);
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
            Subversion.LOG.log(Level.SEVERE, null, ble);
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

    /**
     * Sets the file for which the annotations are displayed. This file can differ from the displayed one when showing annotations
     * for a file in the past.
     * @param file
     */
    void setReferencedFile(File file) {
        this.referencedFile = FileUtil.normalizeFile(file);
        this.referencedFileObject = FileUtil.toFileObject(file);
    }

    boolean isAnnotated() {
        return annotated;
    }
}
