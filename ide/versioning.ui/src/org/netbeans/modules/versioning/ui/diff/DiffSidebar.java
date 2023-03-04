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
package org.netbeans.modules.versioning.ui.diff;

import java.beans.PropertyChangeEvent;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import org.netbeans.editor.*;
import org.netbeans.editor.Utilities;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.diff.*;
import org.netbeans.spi.diff.DiffProvider;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.netbeans.modules.versioning.core.util.Utils;
import org.openide.ErrorManager;
import org.openide.nodes.CookieSet;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.filesystems.*;
import org.openide.awt.UndoRedo;
import org.openide.windows.TopComponent;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.ChoiceFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.swing.plaf.TextUI;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.Mutex;
import org.openide.util.UserQuestionException;

/**
 * Left editor sidebar showing changes in the file against the base version.
 * 
 * @author Maros Sandor
 */
class DiffSidebar extends JPanel implements DocumentListener, ComponentListener, FoldHierarchyListener, FileChangeListener {
    
    private static final int BAR_WIDTH = 9;
    private static final java.util.logging.Logger LOG = DiffSidebarManager.LOG;
    
    private final JTextComponent  textComponent;
    /**
     * We must keep FileObject here because a File may change if the FileObject is renamed.
     * The fileObejct can be DELETED TOO!
     */
    private FileObject            fileObject;

    private final FoldHierarchy   foldHierarchy;
    private final BaseDocument    document;
    
    private boolean                 sidebarVisible;
    private boolean                 sidebarTemporarilyDisabled; // flag disallowing the sidebar to ask for file's content
    private boolean                 sidebarInComponentHierarchy;
    private Difference []           currentDiff;
    private DiffMarkProvider        markProvider;

    private Color colorAdded =      new Color(150, 255, 150);
    private Color colorChanged =    new Color(160, 200, 255);
    private Color colorRemoved =    new Color(255, 160, 180);
    private Color colorBorder =     new Color(102, 102, 102);
    
    private int     originalContentSerial;
    private int     originalContentBufferSerial = -1;
    private String  originalContentBuffer;

    private RequestProcessor.Task   refreshDiffTask;
    private VersioningSystem ownerVersioningSystem;

    public DiffSidebar(JTextComponent target, FileObject file) {
        LOG.log(Level.FINE, "creating DiffSideBar for {0}", file != null ? file.getPath() : null);
        this.textComponent = target;
        this.fileObject = file;
        this.foldHierarchy = FoldHierarchy.get(target);
        this.document = (BaseDocument) textComponent.getDocument();
        this.markProvider = new DiffMarkProvider();
        setToolTipText(""); // NOI18N
        refreshDiffTask = DiffSidebarManager.getInstance().createDiffSidebarTask(new RefreshDiffTask());
        setMaximumSize(new Dimension(BAR_WIDTH, Integer.MAX_VALUE));
    }
    
    FileObject getFileObject() {
        return fileObject;
    }

    private void refreshOriginalContent() {
        originalContentSerial++;
        sidebarTemporarilyDisabled = false;
        LOG.log(Level.FINE, "refreshOriginalContent(): {0}", fileObject != null ? fileObject.getPath() : null);
        refreshDiff();
    }
    
    JTextComponent getTextComponent() {
        return textComponent;
    }

    Difference[] getCurrentDiff() {
        return currentDiff;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        Difference diff = getDifferenceAt(event);
        return getShortDescription(diff);
    }

    static String getShortDescription(Difference diff) {
        if (diff == null) {
            return null;
        }
        int n;
        switch (diff.getType()) {
            case Difference.ADD:
                n = diff.getSecondEnd() - diff.getSecondStart() + 1;
                return MessageFormat.format(new ChoiceFormat(NbBundle.getMessage(DiffSidebar.class, "TT_LinesAdded")).format(n), n); // NOI18N      
            case Difference.CHANGE:
                n = diff.getFirstEnd() - diff.getFirstStart() + 1;
                return MessageFormat.format(new ChoiceFormat(NbBundle.getMessage(DiffSidebar.class, "TT_LinesChanged")).format(n), n); // NOI18N      
            case Difference.DELETE:
                n = diff.getFirstEnd() - diff.getFirstStart() + 1;
                return MessageFormat.format(new ChoiceFormat(NbBundle.getMessage(DiffSidebar.class, "TT_LinesDeleted")).format(n), n); // NOI18N      
            default:
                throw new IllegalStateException("Unknown difference type: " + diff.getType()); // NOI18N
        }
    }

    @Override
    protected void processMouseEvent(MouseEvent event) {
        if (event.getID() == MouseEvent.MOUSE_CLICKED || event.isPopupTrigger()) {
            Difference diff = getDifferenceAt(event);
            if (diff == null) {
                return;
            }
            onClick(event, diff);
        } else {
            super.processMouseEvent(event);
        }
    }
    
    private void onClick(MouseEvent event, Difference diff) {
        Point p = new Point(event.getPoint());
        SwingUtilities.convertPointToScreen(p, this);
        showTooltipWindow(p, diff);
    }

    private void showTooltipWindow(Point p, Difference diff) {
        DiffActionTooltipWindow ttw = new DiffActionTooltipWindow(this, diff);
        ttw.show(new Point(p.x, p.y));
    }
    
    private Difference getDifferenceAt(MouseEvent event) {
        Difference[] paintDiff = currentDiff;
        if (paintDiff == null) {
            return null;
        }
        int line = getLineFromMouseEvent(event);
        if (line == -1) {
            return null;
        }
        Difference diff = getDifference(line + 1, paintDiff);
        if (diff == null) {
            // delete annotations (arrows) are rendered between lines
            diff = getDifference(line, paintDiff);
            if ((diff != null) && (diff.getType() != Difference.DELETE)) {
                diff = null;
            }
        } else if (diff.getType() == Difference.DELETE) {
            Difference diffPrev = getDifference(line, paintDiff);
            if (diffPrev != null && diffPrev.getType() == Difference.DELETE) {
                // two delete arrows next to each other cause some selection problems, select the closer one
                diff = getCloserDifference(event, diffPrev, diff);
            }
        }
        return diff;
    }

    /**
     * In case of two neighboring DELETE differences which meet on the same line, this method returns a difference
     * which' annotation is closer (in y-axis) to the click-point
     * @param event event with the click-point
     * @param previous a difference reaching out from the previous line
     * @param next a difference reaching out from the next line
     * @return
     */
    private Difference getCloserDifference(MouseEvent event, Difference previous, Difference next) {
        Difference returnedDiff = next;
        JTextComponent component = textComponent;
        if (component != null) {
            TextUI textUI = component.getUI();
            try {
                Rectangle rec = textUI.modelToView(component, textUI.viewToModel(component, new Point(0, event.getY())));
                if (rec != null && event.getY() < rec.getY() + rec.getHeight() / 2) {
                    // previous difference is closer to the click
                    returnedDiff = previous;
                }
            } catch (BadLocationException ex) {
                // not interested, default next is returned
            }
        }
        return returnedDiff;
    }

    void onDiff(final Difference diff) {
        try {
            final DiffController view = DiffController.create(new SidebarStreamSource(true), new SidebarStreamSource(false));
            DiffTopComponent tc = new DiffTopComponent(view);
            tc.setName(NbBundle.getMessage(DiffSidebar.class, "CTL_DiffPanel_Title", new Object[] {fileObject.getNameExt()})); // NOI18N
            tc.open();
            tc.requestActive();
            // cannot set the difference index immediately, diff is computed asynchronously, we have to wait for an event
            PropertyChangeListener list = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (DiffController.PROP_DIFFERENCES.equals(evt.getPropertyName())) {
                        // no need to listen any more, removing listener to enable GC
                        view.removePropertyChangeListener(this);
                        int diffIndex = getDiffIndex(diff);
                        if (diffIndex != -1 && diffIndex < view.getDifferenceCount()) {
                            view.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, diffIndex);
                        }
                    }
                }
            };
            view.addPropertyChangeListener(list);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    private int getDiffIndex(Difference diff) {
        Difference[] diffs = currentDiff;
        if (diffs != null) {
            for (int i = 0; i < diffs.length; i++) {
                if (diff == diffs[i]) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int computeDocumentOffset(int lineOffset) {
        int end = Utilities.getRowStartFromLineOffset(document, lineOffset);
        if (end == -1) {
            Element lineRoot = document.getParagraphElement(0).getParentElement();
            for (end = lineRoot.getElement(lineOffset - 1).getEndOffset(); end > document.getLength(); end--) {
            }
        }
        return end;
    }

    void onRollback(Difference diff) {
        try {
            if (diff.getType() == Difference.ADD) {
                int start = Utilities.getRowStartFromLineOffset(document, diff.getSecondStart() - 1);
                int end = computeDocumentOffset(diff.getSecondEnd());
                document.remove(start, end - start);
            } else if (diff.getType() == Difference.CHANGE) {
                int start = Utilities.getRowStartFromLineOffset(document, diff.getSecondStart() - 1);
                int end = computeDocumentOffset(diff.getSecondEnd());
                document.replace(start, end - start, diff.getFirstText(), null);
            } else {
                int start = computeDocumentOffset(diff.getSecondStart());
                String newline = Utilities.getRowStartFromLineOffset(document, diff.getSecondStart()) == -1 ? "\n" : "";
                document.insertString(start, newline + diff.getFirstText(), null);
            }
            LOG.finer("refreshing diff in onRollback");
            refreshDiff();
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    boolean canRollback(final Difference diff) {
        if (!(document instanceof GuardedDocument)) {
            return true;
        }
        final boolean[] modifiable = new boolean[1];
        document.runAtomic(new Runnable() {
            @Override
            public void run () {
                boolean canModify = document.isModifiable();
                if (canModify) {
                    int start, end;
                    if (diff.getType() == Difference.DELETE) {
                        start = end = Utilities.getRowStartFromLineOffset(document, diff.getSecondStart());
                    } else {
                        start = Utilities.getRowStartFromLineOffset(document, diff.getSecondStart() - 1);
                        end = Utilities.getRowStartFromLineOffset(document, diff.getSecondEnd());
                    }
                    MarkBlockChain mbc = ((GuardedDocument) document).getGuardedBlockChain();
                    canModify = (mbc.compareBlock(start, end) & MarkBlock.OVERLAP) == 0;
                }
                modifiable[0] = canModify;
            }
        });
        return modifiable[0];
    }
    
    void onPrevious(Difference diff) {
        Difference[] diffs = currentDiff;
        int diffIndex;
        if (diffs != null && (diffIndex = getDiffIndex(diff)) > -1 && diffIndex < diffs.length) {
            diff = diffs[diffIndex - 1];
            Point location = scrollToDifference(diff);
            showTooltipWindow(location, diff);
            textComponent.repaint();
        }
    }

    void onNext(Difference diff) {
        Difference[] diffs = currentDiff;
        int diffIndex;
        if (diffs != null && (diffIndex = getDiffIndex(diff)) > -1 && diffIndex < diffs.length - 1) {
            diff = diffs[diffIndex + 1];
            Point location = scrollToDifference(diff);
            showTooltipWindow(location, diff);
            textComponent.repaint();
        }
    }

    private Point scrollToDifference(Difference diff) {
        int lineStart = diff.getSecondStart() - 1;
        int lineEnd = diff.getSecondEnd() - 1;
        if (lineStart == -1) {
            // the change was delete starting on the first line, show the diff on the next line
            // since in this case the file cannot be empty, 0 index does not throw BLE
            lineStart = 0;
        }
        if (diff.getType() == Difference.DELETE) {
            lineEnd = lineStart;
        }
        try {
            EditorUI editorUI = Utilities.getEditorUI(textComponent);
            int visibleBorder = editorUI.getLineHeight() * 5;
            int startOffset = Utilities.getRowStartFromLineOffset((BaseDocument) textComponent.getDocument(), lineStart);
            int endOffset = Utilities.getRowStartFromLineOffset((BaseDocument) textComponent.getDocument(), lineEnd);
            Rectangle startRect = textComponent.getUI().modelToView(textComponent, startOffset);
            Rectangle endRect = textComponent.getUI().modelToView(textComponent, endOffset);
            Rectangle visibleRect = new Rectangle(startRect.x - visibleBorder, startRect.y - visibleBorder, 
                                                  startRect.x, endRect.y - startRect.y + endRect.height + visibleBorder * 2);
            textComponent.scrollRectToVisible(visibleRect);
           
            //make sure the y coordinate isn't outside the editor bounds otherwise the popup will 'float' beneath the editor
            Rectangle extent = editorUI.getExtentBounds();
            int maxVisibleY = extent.y + extent.height;
            
            Point p = new Point(endRect.x, Math.min(maxVisibleY, endRect.y + endRect.height + 1));
            
            //XXX: The resulting screen coordinates could still be outside the main screen
            SwingUtilities.convertPointToScreen(p, textComponent);
            return p;
        } catch (BadLocationException e) {
            LOG.log(Level.WARNING, "scrollToDifference", e); // NOI18N
        }
        return null;
    }
    
    String getMimeType() {
        if (textComponent instanceof JEditorPane) {
            return ((JEditorPane) textComponent).getContentType();
        }
        return "text/plain"; // NOI18N
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        // should not happen
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        // should not happen
    }

    @Override
    public void fileChanged(FileEvent fe) {
        // not interested
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        if (fileObject != null) {
            // needed since we are changing the fileObject instance
            fileObject.removeFileChangeListener(this);
            fileObject = null;
        }
        DataObject dobj = (DataObject) document.getProperty(Document.StreamDescriptionProperty);
        if (dobj != null) {
            fileObject = dobj.getPrimaryFile();
        }
        fileRenamed(null);
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        });
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        // not interested
    }

    private int getPosFromY(JTextComponent component, TextUI textUI, int y) throws BadLocationException {
        if(textUI instanceof BaseTextUI) {
            return ((BaseTextUI) textUI).getPosFromY(y);
        } else {
            // fallback to ( less otimized than ((BaseTextUI) textUI).getPosFromY(y) )
            return textUI.modelToView(component, textUI.viewToModel(component, new Point(0, y))).y;
        }
    }

    private static class DiffTopComponent extends TopComponent {
        
        private JComponent diffView;

        public DiffTopComponent() {
        }

        public DiffTopComponent(DiffController c) {
            this.diffView = c.getJComponent();
            setLayout(new BorderLayout());
            diffView.putClientProperty(TopComponent.class, this);
            
            DiffSidebarDiffPanel dsdp = new  DiffSidebarDiffPanel(c);
            add(dsdp);
            
//            getAccessibleContext().setAccessibleName(NbBundle.getMessage(DiffTopComponent.class, "ACSN_Diff_Top_Component")); // NOI18N
//            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DiffTopComponent.class, "ACSD_Diff_Top_Component")); // NOI18N
        }

        @Override
        public UndoRedo getUndoRedo() {
            UndoRedo undoredo = (UndoRedo) diffView.getClientProperty(UndoRedo.class);
            return undoredo == null ? UndoRedo.NONE : undoredo;
        }
        
        @Override
        public int getPersistenceType(){
            return TopComponent.PERSISTENCE_NEVER;
        }

        @Override
        protected String preferredID(){
            return "DiffSidebarTopComponent";    // NOI18N
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(getClass());
        }
    }

    private int getLineFromMouseEvent(MouseEvent e){
        int line = -1;
        EditorUI editorUI = Utilities.getEditorUI(textComponent);
        if (editorUI != null) {
            try{
                JTextComponent component = editorUI.getComponent();
                if (component != null) {
                    TextUI textUI = component.getUI();
                    int clickOffset = textUI.viewToModel(component, new Point(0, e.getY()));
                    line = Utilities.getLineOffset(document, clickOffset);
                }
            }catch (BadLocationException ble){
                LOG.log(Level.WARNING, "getLineFromMouseEvent", ble); // NOI18N
            }
        }
        return line;
    }

    void refresh() {
        if (!sidebarInComponentHierarchy) {
            return;
        }
        shutdown();
        ownerVersioningSystem = null;
        initialize();
        LOG.finer("refreshing diff in refresh");
        refreshDiff();
        revalidate();  // resize the component
    }
        
    public void setSidebarVisible(boolean visible) {
        if (sidebarVisible == visible) {
            return;
        }
        sidebarVisible = visible;
        LOG.finer("refreshing diff in setSidebarVisible");
        refreshDiff();
        revalidate();  // resize the component
    }

    @Override
    public void addNotify() {
        super.addNotify();
        sidebarInComponentHierarchy = true;
        initialize();
    }

    @Override
    public void removeNotify() {
        shutdown();
        sidebarInComponentHierarchy = false;
        super.removeNotify();
    }
    
    private void initialize() {
        assert SwingUtilities.isEventDispatchThread();

        document.addDocumentListener(this);
        textComponent.addComponentListener(this);
        foldHierarchy.addFoldHierarchyListener(this);
        refreshOriginalContent();
        final FileObject fo = fileObject;
        if (fo != null) {
            DiffSidebarManager.getInstance().createDiffSidebarTask(new Runnable() {
                @Override
                public void run () {
                    fo.addFileChangeListener(DiffSidebar.this);
                }
            }).schedule(0);
        }
    }

    private void shutdown() {
        assert SwingUtilities.isEventDispatchThread();
        refreshDiffTask.cancel();
        final FileObject fo = fileObject;
        if (fo != null) {
            DiffSidebarManager.getInstance().createDiffSidebarTask(new Runnable() {
                @Override
                public void run () {
                    fo.removeFileChangeListener(DiffSidebar.this);
                }
            }).schedule(0);
        }
        foldHierarchy.removeFoldHierarchyListener(this);
        textComponent.removeComponentListener(this);
        document.removeDocumentListener(this);
    }

    private Reader getDocumentReader() {
        return getDocumentReader(textComponent.getDocument());
    }
    
    private void refreshDiff() {
        refreshDiffTask.schedule(50);
    }
        
    MarkProvider getMarkProvider() {
        return markProvider;
    }

    static void copyStreamsCloseAll(OutputStream writer, InputStream reader) throws IOException {
        byte [] buffer = new byte[2048];
        int n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
        writer.close();
        reader.close();
    }
    
    @Override
    public Dimension getPreferredSize() {
        Dimension dim = textComponent.getSize();
        dim.width = sidebarVisible ? BAR_WIDTH : 0;
        return dim;
    }
    
    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        Utilities.runViewHierarchyTransaction(textComponent, true,
            new Runnable() {
                @Override
                public void run() {
                    paintComponentUnderLock(g);
                }
            }
        );
    }

    private void paintComponentUnderLock (Graphics g) {
        Rectangle clip = g.getClipBounds();
        if (clip.y >= 16) {
            // compensate for scrolling: marks on bottom/top edges are not drawn completely while scrolling
            clip.y -= 16;
            clip.height += 16;
        }

        g.setColor(backgroundColor());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        JTextComponent component = textComponent;
        TextUI textUI = component.getUI();
        EditorUI editorUI = Utilities.getEditorUI(textComponent);
        if (editorUI == null) {
            LOG.log(Level.WARNING, "No editor UI for file {0}, has {1} text UI", new Object[] { //NOI18N
                fileObject == null ? null : fileObject.getPath(),
                textComponent.getUI() });
            return;
        }
        View rootView = Utilities.getDocumentView(component);
        if (rootView == null) {
            return;
        }
        
        Difference [] paintDiff = currentDiff;
        if (paintDiff == null || paintDiff.length == 0) {
            return;
        }

        try{
            int startPos = getPosFromY(component, textUI, clip.y);
            int startViewIndex = rootView.getViewIndex(startPos,Position.Bias.Forward);
            int rootViewCount = rootView.getViewCount();

            if (startViewIndex >= 0 && startViewIndex < rootViewCount) {
                // find the nearest visible line with an annotation
                Rectangle rec = textUI.modelToView(component, rootView.getView(startViewIndex).getStartOffset());
                int y = (rec == null) ? 0 : rec.y;
                int [] yCoords = new int[3];

                int clipEndY = clip.y + clip.height;
                Element rootElem = textUI.getRootView(component).getElement();

                View view = rootView.getView(startViewIndex);
                int line = rootElem.getElementIndex(view.getStartOffset());
                line++; // make it 1-based
                if (line == 1 && paintDiff[0].getSecondStart() == 0 && paintDiff[0].getType() == Difference.DELETE) {
                    g.setColor(getColor(paintDiff[0]));
                    yCoords[0] = y - editorUI.getLineAscent() / 2;
                    yCoords[1] = y;
                    yCoords[2] = y + editorUI.getLineAscent() / 2;
                    g.fillPolygon(new int [] { 0, BAR_WIDTH, 0 }, yCoords, 3);
                }

                for (int i = startViewIndex; i < rootViewCount; i++){
                    view = rootView.getView(i);
                    if (view == null) {
                        LOG.log(Level.WARNING, "View {0} null? View count = {1}/{2}. Root view: {3}, root elem: {4}", new Object[] { i, rootView.getViewCount(), rootViewCount, rootView, rootElem });
                    }
                    line = rootElem.getElementIndex(view.getStartOffset());
                    line++; // make it 1-based
                    Difference ad = getDifference(line, paintDiff);
                    Rectangle rec1 = component.modelToView(view.getStartOffset());
                    Rectangle rec2 = component.modelToView(view.getEndOffset() - 1);
                    if (rec2  == null || rec1 == null) break;
                    y = rec1.y;
                    double height = (rec2.getY() + rec2.getHeight() - rec1.getY());
                    if (ad != null) {
                        LOG.log(Level.FINEST, "painting difference {0} on line {1}", new Object[]{ad.getType(), line});      
                        g.setColor(getColor(ad));
                        if (ad.getType() == Difference.DELETE) {
                            yCoords[0] = (int) rec2.getY() + editorUI.getLineAscent();
                            yCoords[1] = (int) rec2.getY() + editorUI.getLineAscent() * 3 / 2;
                            yCoords[2] = (int) rec2.getY() + editorUI.getLineAscent() * 2;
                            g.fillPolygon(new int[]{2, BAR_WIDTH, 2}, yCoords, 3);
                            g.setColor(colorBorder);
                            g.drawLine(2, yCoords[0], 2, yCoords[2] - 1);
                        } else {
                            g.fillRect(3, (int) y, BAR_WIDTH - 3, (int) height);
                            g.setColor(colorBorder);
                            int y1 = (int) (y + height);
                            g.drawLine(2, (int) y, 2, y1);
                            if (ad.getSecondStart() == line) {
                                g.drawLine(2, (int) y, BAR_WIDTH - 1, (int) y);
                            }
                            g.drawLine(2, y1, BAR_WIDTH - 1, y1);
                        }
                    }
                    y += height;
                    if (y >= clipEndY) {
                        break;
                    }
                }
            }
        } catch (BadLocationException ble){
            LOG.log(Level.INFO, null, ble);
        }
    }

    private Color getColor(Difference ad) {
        if (ad.getType() == Difference.ADD) {
            return colorAdded;
        }
        if (ad.getType() == Difference.CHANGE) {
            return colorChanged;
        }
        return colorRemoved;
    }

    private Difference getDifference(int line, Difference[] paintDiff) {
        if (line < 0 || paintDiff == null) {
            return null;
        }
        for (int i = 0; i < paintDiff.length; i++) {
            Difference difference = paintDiff[i];
            if (line < difference.getSecondStart()) {
                return null;
            }
            if ((difference.getType() == Difference.DELETE) && (line == difference.getSecondStart())) {
                return difference;
            }
            if (line <= difference.getSecondEnd()) {
                return difference;
            }
        }
        return null;
    }

    private Color backgroundColor() {
        Container c = getParent();
        if (c == null) {
            return defaultBackground();
        } else {
            return c.getBackground();
        }
    }

    private Color defaultBackground () {
        if (textComponent != null) {
            return textComponent.getBackground();
        }
        return Color.WHITE;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        LOG.finer("refreshing diff in insertUpdate");
        refreshDiff();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        LOG.finer("refreshing diff in removeUpdate");
        refreshDiff();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        LOG.finer("refreshing diff in changedUpdate");
        refreshDiff();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        Mutex.EVENT.readAccess(new Runnable () {
            @Override
            public void run() {
                revalidate();
            }
        });
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void foldHierarchyChanged(FoldHierarchyEvent evt) {
        Mutex.EVENT.readAccess(new Runnable () {
            @Override
            public void run() {
                repaint();
            }
        });
    }

    /**
     * Integration provider for the error stripe.
     */
    private class DiffMarkProvider extends MarkProvider {

        private List<DiffMark> marks;

        public DiffMarkProvider() {
            marks = getMarksForDifferences();
        }

        @Override
        public List getMarks() {
            return marks;
        }

        void refresh() {
            List<DiffMark> oldMarks = marks;
            marks = getMarksForDifferences();
            LOG.log(Level.FINER, "refreshing marks for {0}", fileObject != null ? fileObject.getPath() : null);
            firePropertyChange(PROP_MARKS, oldMarks, marks);
        }

        private List<DiffMark> getMarksForDifferences() {
            Difference[] diffs = currentDiff;
            if ((diffs == null) || !isVisible() || (getWidth() <= 0)) {
                return Collections.emptyList();
            }
            List<DiffMark> marksList = new ArrayList<DiffMark>(diffs.length);
            for (int i = 0; i < diffs.length; i++) {
                Difference difference = diffs[i];
                marksList.add(new DiffMark(difference, getColor(difference)));
            }
            return marksList;
        }
    }

    /**
     * RP task to compute new diff after a change in the document or a change in the base text.
     */
    public class RefreshDiffTask implements Runnable {

        @Override
        public void run() {
            if(ownerVersioningSystem == null) {
                VCSFileProxy file = fileObject != null ? VCSFileProxy.createFileProxy(fileObject) : null;
                ownerVersioningSystem = file != null ? Utils.getOwner(file) : null;

                LOG.log(
                    Level.FINE, 
                    "owner for file {0} is {1}", 
                    new Object[]{
                        fileObject != null ? fileObject.getPath() : null, 
                        ownerVersioningSystem != null ? ownerVersioningSystem.getDisplayName() : "null"});
            }
            
            computeDiff();
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    repaint();
                    markProvider.refresh();
                }
            });
        }

        private void computeDiff() {
            if (!sidebarVisible || sidebarTemporarilyDisabled || !sidebarInComponentHierarchy) {
                currentDiff = null;
                return;
            }
            fetchOriginalContent();
            if (originalContentBuffer == null) {
                currentDiff = null;
                return;
            }
            Reader working = getDocumentReader();
            if (working == null) {
                // TODO: what to do in this case? let's keep the old dirrerence set for now
                return;
            }
            DiffProvider diff = Lookup.getDefault().lookup(DiffProvider.class);
            if (diff == null) {
                LOG.log(Level.WARNING, "no diff provider found for {0}", fileObject != null ? fileObject.getPath() : null);
                currentDiff = null;
                return;
            }
            try {
                currentDiff = diff.computeDiff(new StringReader(originalContentBuffer), working);
            } catch (IOException e) {
                currentDiff = null;
            }
        }

        private void fetchOriginalContent() {
            int serial = originalContentSerial;
            if ((originalContentBuffer != null) && (originalContentBufferSerial == serial)) {
                return;
            }
            originalContentBufferSerial = serial;

            LOG.log(Level.FINER, "fetching original contet for {0}", fileObject != null ? fileObject.getPath() : null);
            originalContentBuffer = getText(ownerVersioningSystem);
            if (originalContentBuffer == null) {
                // no content for the file, setting sidebar visibility to false eliminates repeated asking for the content
                // file can be deleted, or new?
                sidebarTemporarilyDisabled = true;
                LOG.log(Level.FINE, "Disabling diffsidebar for {0}, no content available", fileObject != null ? fileObject.getPath() : null); //NOI18N
            }
        }
    }

    /**
     * Gets the original content of the working copy. This method is typically only called after the OriginalContent
     * object is created and once for every property change event. 
     * 
     * @param oc current OriginalContent
     * @return Reader original content of the working copy or null if the original content is not available
     */ 
    private String getText(VersioningSystem vs) {
        if (vs == null) {
            return null;
        }

        Collection<FileObject> filesToCheckout = getFiles(fileObject);
        if (filesToCheckout.isEmpty()) {
            return null;
        }

        File tempFolder = getTempFolder();
        FileObject tempFileObj = null;

        try {            
            Collection<File> originalFiles = checkoutOriginalFiles(filesToCheckout, tempFolder, vs);

            Charset encoding = FileEncodingQuery.getEncoding(fileObject);
            for (File f : originalFiles) {
                org.netbeans.modules.versioning.util.Utils.associateEncoding(f, encoding);
            }
            File tempFile = new File(tempFolder, fileObject.getNameExt());
            tempFileObj = FileUtil.toFileObject(tempFile);     //can be null
            return getText(tempFile, tempFileObj, encoding);
        } catch (Exception e) {
            // let providers raise errors when they feel appropriate
            return null;
        } finally {
            deleteTempFolder(tempFolder, tempFileObj);
        }
    }

    /**
     * Returns files that belong to the same {@code DataObject} as the given
     * file.
     * @param  fileObj  file whose siblings are to be found
     * @return  list of files that belong to the {@code DataObject} determined
     *              by the given {@code FileObject},
     *          or a singleton collection containing just the {@code File}
     *              corresponding to the given {@code FileObject},
     *          or an empty collection if the given {@code FileObject} could
     *              not be translated to a {@code File}
     */
    private static Collection<FileObject> getFiles(FileObject fileObj) {
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fileObj);
        if (proxy == null) {
            // could be e.g. a FileObject from a jar filesystem
            return Collections.emptyList();
        }

        Set<FileObject> fileObjects;
        try {
            fileObjects = DataObject.find(fileObj).files();
            assert fileObjects.contains(fileObj);
            if (fileObjects.size() == 1) {
                fileObjects = Collections.singleton(fileObj);
            } 
        } catch (DataObjectNotFoundException e) {
            // no dataobject, never mind
            fileObjects = Collections.singleton(fileObj);
        }
        return fileObjects;
    }

    /**
     * Checks out original (unmodified) content of the given files to the given
     * target folder.
     * @param  filesToCheckout  files whose original content is to be checked out
     * @param  targetTempFolder  target folder where unmodified files should be stored
     * @param  vs  versioning system to be used for the checkout
     * @return  collection of files containing unmodified content of the given
     *          files; these files are named after the given files
     */
    private Collection<File> checkoutOriginalFiles(Collection<FileObject> filesToCheckout,
                                                   File targetTempFolder,
                                                   VersioningSystem vs) {
        Collection<File> originalFiles = new ArrayList<File>(filesToCheckout.size());

        for (FileObject fo : filesToCheckout) {
            LOG.log(Level.FINE, "checking out original file {0}", fo != null ? fo.getPath() : null);
            File originalFile = new File(targetTempFolder, fo.getNameExt());
            vs.getOriginalFile(VCSFileProxy.createFileProxy(fo), VCSFileProxy.createFileProxy(originalFile)); // XXX refactor the whole diff
            originalFiles.add(originalFile);
        }

        return originalFiles;
    }

    private static String getText(File file,
                                  FileObject fileObj,
                                  Charset charset) throws IOException {
        if (fileObj != null) {
            try {
                /*
                 * Text returned by EditorCookie.openDocument.getText(...)
                 * may differ from the raw text contained in the file.
                 * For example, this is the case of FormDataObject, which trims
                 * some special comments (tags) while the Java source file is
                 * being loaded to the editor. When the file is being saved,
                 * the special comments are written to the raw file.
                 * This is the reason why text for the diff is read using
                 * the EditorCookie, instead of reading it directly from the
                 * File or FileObject.
                 */
                EditorCookie edCookie = getEditorCookie(fileObj);
                if (edCookie != null) {
                    try {
                        Document doc;
                        try {
                            doc = edCookie.openDocument();
                        } catch (UserQuestionException ex) {
                            /* the document is large - confirm automatically */
                            ex.confirmed();
                            doc = edCookie.openDocument();
                        }
                        return doc.getText(0, doc.getLength());
                    } finally {
                        edCookie.close();
                    }
                } else {
                    return getRawText(fileObj, charset);
                }
            } catch (IOException ex) {
                throw ex;
            } catch (Exception e) {
                // something's wrong, read the file from disk
            }
        }

        return getRawText(file, charset);
    }

    /**
     * Tries to obtain an {@code EditorCookie} representing the given file.
     * @param  fileObj  file to get an {@code EditorCookie} from
     * @return  {@code EditorCookie} representing the file, or {@code null}
     * @throws  java.io.IOException
     *          if there was some I/O error while reading the file's content
     */
    private static EditorCookie getEditorCookie(FileObject fileObj) throws IOException {
        DataObject dao;
        try {
            dao = DataObject.find(fileObj);
        } catch (DataObjectNotFoundException ex) {
            return null;
        }

        if (dao instanceof MultiDataObject) {
            MultiDataObject.Entry entry = findEntryForFile((MultiDataObject) dao, fileObj);
            if (entry instanceof CookieSet.Factory) {
                CookieSet.Factory factory = (CookieSet.Factory) entry;
                return factory.createCookie(EditorCookie.class);   //can be null
            }
        }

        return dao.getCookie(EditorCookie.class);                  //can be null
    }

    /**
     * Finds entry of the given {@code MultiDataObject} that corresponds to the
     * to the given {@code FileObject}.
     * @param  dataObj  {@code MultiDataObject} to search for the corresponding
     *                  entry
     * @param  fileObj  file for which the entry is to be found
     * @return  found entry, or {@code null} if not found
     */
    private static MultiDataObject.Entry findEntryForFile(MultiDataObject dataObj,
                                                          FileObject fileObj) {
        MultiDataObject.Entry primaryEntry;

        primaryEntry = dataObj.getPrimaryEntry();
        if (fileObj.equals(primaryEntry.getFile())) {
            return primaryEntry;
        }

        for (MultiDataObject.Entry entry : dataObj.secondaryEntries()) {
            if (fileObj.equals(entry.getFile())) {
                return primaryEntry;
            }
        }

        return null;
    }

    private static String getRawText(FileObject fileObj, Charset charset) throws IOException {
        //return fileObj.asText(charset);

        char[] chars = new StringDecoder(charset).decode(fileObj.asBytes());
        return new String(chars);
    }

    private static String getRawText(File file, Charset charset) throws IOException {
        final long size = file.length();
        if (size == 0L) {
            return file.exists() ? "" : null;                           //NOI18N
        }

        FileInputStream inputStream = new FileInputStream(file);
        FileChannel inputChannel = inputStream.getChannel();

        try {
            int inputSize = (int) inputChannel.size();
            ByteBuffer inBuf = ByteBuffer.allocate(inputSize);
            inputChannel.read(inBuf);
            char[] chars = new StringDecoder(charset).decode(inBuf);
            return new String(chars);
        } finally {
            inputChannel.close();
        }
    }

    private static void deleteTempFolder(File tempFolder, FileObject tempFileObj) {
        boolean fullyDeleted = false;

        if (tempFileObj != null) {
            try {
                FileObject tempFolderObj = tempFileObj.getParent();
                if (tempFolderObj != null) {
                    tempFolderObj.delete();
                    fullyDeleted = true;
                } else {
                    tempFileObj.delete();
                }
            } catch (IOException ex) {
                //'fullyDeleted' remains 'false'
            }
        }

        if (!fullyDeleted) {
            deleteRecursively(tempFolder);
        }
    }

   private static Reader getDocumentReader(final Document doc) {
        final String[] str = new String[1];
        Runnable run = new Runnable() {
            @Override
            public void run () {
                try {
                    str[0] = doc.getText(0, doc.getLength());
                } catch (javax.swing.text.BadLocationException e) {
                    // impossible
                    DiffSidebarManager.LOG.log(Level.WARNING, null, e);
                }
            }
        };
        doc.render(run);
        return new StringReader(str[0]);
    }  
   
    /**
     * Recursively deletes the file or directory.
     *
     * @param file file/directory to delete
     * @param level log level
     */
    private static void deleteRecursively (File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) return;
        try {
            fo.delete();
        } catch (IOException e) {
            DiffSidebarManager.LOG.log(Level.INFO, "", e);
            file.deleteOnExit();
        }
    }
    
    private static File getTempFolder() {
        File tmpDir = DiffSidebarManager.getInstance().getMainTempDir();
        for (;;) {
            File dir = new File(tmpDir, "vcs-" + Long.toString(System.currentTimeMillis())); // NOI18N
            if (!dir.exists() && dir.mkdirs()) {
                dir.deleteOnExit();
                return FileUtil.normalizeFile(dir);
            }
        }
    }
    
    private static final class StringDecoder {

        private final CharsetDecoder charsetDecoder;

        StringDecoder(Charset charset) {
	    charsetDecoder = charset.newDecoder()
                             .onMalformedInput(CodingErrorAction.REPLACE)
                             .onUnmappableCharacter(CodingErrorAction.REPLACE);
        }

        char[] decode(byte[] bytes) {
            if (bytes.length == 0) {
                return new char[0];
            }

           return decode(ByteBuffer.wrap(bytes));
        }

        char[] decode(ByteBuffer inBuf) {
	    char[] chars = new char[computeOutBufSize(inBuf.limit())];

	    CharBuffer outBuf = CharBuffer.wrap(chars);

	    charsetDecoder.reset();
	    try {
		CoderResult result;

		result = charsetDecoder.decode(inBuf, outBuf, true);
		if (!result.isUnderflow()) {
		    result.throwException();
                }

		result = charsetDecoder.flush(outBuf);
		if (!result.isUnderflow()) {
		    result.throwException();
                }

	    } catch (CharacterCodingException x) {
                assert false;                       //should not happen
		throw new Error(x);
	    }

            if (chars.length != outBuf.position()) {
                chars = trimToSize(chars, outBuf.position());
            }
            return chars;
        }

        private int computeOutBufSize(int inBufSize) {
            float ratio = charsetDecoder.maxCharsPerByte();
            return (int) Math.ceil(inBufSize * (double) ratio);
        }

        private static char[] trimToSize(char[] chars, int requestedLength) {
            if (requestedLength > chars.length) {
                throw new IllegalArgumentException();
            }

            if (requestedLength == chars.length) {
                return chars;
            }

            char[] result = new char[requestedLength];
            System.arraycopy(chars, 0, result, 0, requestedLength);
            return result;
        }

    }

    private class SidebarStreamSource extends StreamSource {

        private final boolean isFirst;

        public SidebarStreamSource(boolean isFirst) {
            this.isFirst = isFirst;
        }

        @Override
        public boolean isEditable() {
            return !isFirst;
        }

        @Override
        public Lookup getLookup() {
            if (isFirst) {
                return super.getLookup();
            }
            return Lookups.fixed(document);
        }

        @Override
        public String getName() {
            return fileObject.getNameExt();
        }

        @Override
        public String getTitle() {
            if (isFirst) {
                return NbBundle.getMessage(DiffSidebar.class, "LBL_DiffPane_Original"); // NOI18N
            } else {
                return NbBundle.getMessage(DiffSidebar.class, "LBL_DiffPane_WorkingCopy"); // NOI18N
            }
        }

        @Override
        public String getMIMEType() {
            return getMimeType();
        }

        @Override
        public Reader createReader() throws IOException {
            return isFirst ? new StringReader(originalContentBuffer) : getDocumentReader();
        }

        @Override
        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
    }
}
