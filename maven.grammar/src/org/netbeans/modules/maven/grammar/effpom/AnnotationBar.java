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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.maven.grammar.effpom;

import org.netbeans.editor.*;
import org.netbeans.editor.Utilities;
import org.openide.text.*;
import org.openide.util.*;
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
import java.util.logging.Logger;
import org.apache.maven.model.InputLocation;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.grammar.effpom.LocationAwareMavenXpp3Writer.Location;
import org.netbeans.modules.maven.hyperlinks.HyperlinkProviderImpl;
import static org.netbeans.modules.maven.grammar.effpom.Bundle.*;
import org.openide.util.NbBundle.Messages;

/**
 * strongly inspired by git's implementation
 * @author mkleint@netbeans.org
 * 
 */
public final class AnnotationBar extends JComponent implements Accessible, PropertyChangeListener, ChangeListener, ActionListener, Runnable, ComponentListener {

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
    private final Caret caret;

    /**
     * Caret batch timer launched on receiving
     * annotation data structures (AnnotateLine).
     */
    private Timer caretTimer;


    /**
     * Maps document {@link javax.swing.text.Element}s (representing lines) to
     * {@link AnnotateLine}. <code>null</code> means that
     * no data are available, yet. So alternative
     * {@link #elementAnnotationsSubstitute} text shoudl be used.
     *
     * @thread it is accesed from multiple threads all mutations
     * and iterations must be under elementAnnotations lock,
     */
    private Map<Integer, Location> elementAnnotations;

    private Color backgroundColor = Color.GRAY.brighter();
    private Color foregroundColor = Color.BLACK;
    private Color selectedColor = Color.GREEN.darker();

    /**
     * Revision associated with caret line.
     */
    private String recentLocationName;
    
    /**
     * Request processor to create threads that may be cancelled.
     */
    static RequestProcessor requestProcessor = null;
    
    /**
     * Latest annotation comment fetching task launched.
     */
    private RequestProcessor.Task latestAnnotationTask = null;



    static final Logger LOG = Logger.getLogger(AnnotationBar.class.getName());

    /**
     * Creates new instance initializing final fields.
     */
    public AnnotationBar(JTextComponent target) {
        this.textComponent = target;
        this.editorUI = Utilities.getEditorUI(target);
        this.foldHierarchy = FoldHierarchy.get(editorUI.getComponent());
        this.doc = editorUI.getDocument();
        this.caret = textComponent.getCaret();
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }
    
    void annotate(final List<Location> locations) {
        doc.render(new Runnable() {
            @Override
            public void run() {
                StyledDocument sd = (StyledDocument) doc;
                elementAnnotations = new HashMap<Integer, Location>();
                for (Location loc : locations) {
                    int line = NbDocument.findLineNumber(sd, loc.startOffset);
                    elementAnnotations.put(line, loc);
                    //for multiline values like <parent> or <organization>
                    int endline = NbDocument.findLineNumber(sd, loc.endOffset);
                    if (endline != line && !elementAnnotations.containsKey(endline)) {
                        elementAnnotations.put(endline, loc);
                    }
                }
            }
        });
        caret.addChangeListener(this);
        this.caretTimer = new Timer(500, this);
        caretTimer.setRepeats(false);

        onCurrentLine();
        revalidate();        
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

            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.isConsumed()) {
                    return;
                }
                if (me.getClickCount() > 1 && !me.isPopupTrigger()) {
                    if (elementAnnotations != null) {
                        Location al = getAnnotateLine(getLineFromMouseEvent(me));
                        if (al != null) {
                            ModelUtils.openAtSource(al.loc);
                        }
                    }
                    
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
        Location al = null;
        if (elementAnnotations != null) {
            al = getAnnotateLine(getLineFromMouseEvent(event));
        }

//        /**
//         * al.getCommitMessage() != null - since commit messages are initialized separately from the AL constructor
//         */
//        if (al != null && al.getRevisionInfo() != null) {
//            TooltipWindow ttw = new TooltipWindow(this, al);
//            ttw.show(new Point(p.x - p2.x, p.y));
//        }
    }

    @NbBundle.Messages("ACT_GoToSource=Go to Source")
    private JPopupMenu createPopup(MouseEvent e) {
        final JPopupMenu popupMenu = new JPopupMenu();

        // annotation for target line
        if (elementAnnotations != null) {
            final Location al = getAnnotateLine(getLineFromMouseEvent(e));
            if (al != null) {
                JMenuItem item = new JMenuItem(ACT_GoToSource());
                popupMenu.add(item);
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        InputLocation loc = al.loc;
                        if (loc != null) {
                            ModelUtils.openAtSource(loc);
                        }
                    }
                });
            }
        }
        return popupMenu;
    }

//    private String getKeyFor (File file, String revision) {
//        return file.getAbsolutePath() + "#" + revision; //NOI18N
//    }

  
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
        
        latestAnnotationTask = getRequestProcessor().post(this);
    }

    // latestAnnotationTask business logic
    @Override
    public void run() {
        // determine current line
        int line;
        int offset = caret.getDot();
        try {
            line = Utilities.getLineOffset(doc, offset);
        } catch (BadLocationException ex) {
            LOG.log(Level.SEVERE, "Can not get line for caret at offset ", offset); // NOI18N
            return;
        }

        Location al = getAnnotateLine(line);
        if (al == null) {
            AnnotationMarkProvider amp = AnnotationMarkInstaller.getMarkProvider(textComponent);
            if (amp != null) {
                amp.setMarks(Collections.<AnnotationMark>emptyList());
            }
            if (recentLocationName != null) {
                recentLocationName = null;
                repaint();
            }
            return;
        }

        // handle unchanged lines
        String locat = getDisplayName(al);
        if (!locat.equals(recentLocationName)) {
            recentLocationName = locat;
            repaint();
        }
            AnnotationMarkProvider amp = AnnotationMarkInstaller.getMarkProvider(textComponent);
            if (amp != null) {
            
                List<AnnotationMark> marks = new ArrayList<AnnotationMark>(elementAnnotations.size());
                for (Map.Entry<Integer, Location> loca : elementAnnotations.entrySet()) {
                    Location loc = loca.getValue();
                    if (loc.loc.getSource().equals(al.loc.getSource())) {
                        marks.add(new AnnotationMark(loca.getKey(), loc.loc.getSource().getModelId()));
                    }
                }
                amp.setMarks(marks);
            }
//        }
//
//        if (al.getRevisionInfo() != null) {
//            recentStatusMessage = al.getRevisionInfo().getShortMessage();
//            statusBar.setText(StatusBar.CELL_MAIN, al.getRevisionInfo().getRevision().substring(0, 7) + " - " + al.getAuthor().toString() + ": " + recentStatusMessage); // NOI18N
//        } else {
//            clearRecentFeedback();
//        }
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
        int width = getBarWidth();
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
        if (elementAnnotations == null) {
            return 0;
        }
        String longestString = "";  // NOI18N
        Iterator<Location> it = elementAnnotations.values().iterator();
        while (it.hasNext()) {
            Location line = it.next();
            String displayName = getDisplayName(line); // NOI18N
            if (displayName.length() > longestString.length()) {
                longestString = displayName;
            }
        }
        char[] data = longestString.toCharArray();
        int w = getGraphics().getFontMetrics(editorUI.getComponent().getFont()).charsWidth(data, 0,  data.length);
        return w + 4;
    }

    private String getDisplayName(Location line) {
        if (line.loc.getSource() == null) {
            return ""; //NOI18N
        } else {
            String modelId = line.loc.getSource().getModelId();
            return modelId.substring(modelId.indexOf(':') + 1, modelId.lastIndexOf(":"));
        }
    }

    /**
     * Pair method to {@link #annotate}. It releases
     * all resources.
     */
    private void release() {
        editorUI.removePropertyChangeListener(this);
        textComponent.removeComponentListener(this);
        caret.removeChangeListener(this);
        if (caretTimer != null) {
            caretTimer.removeActionListener(this);
        }
        elementAnnotations = null;
        // cancel running annotation task if active
        if(latestAnnotationTask != null) {
            latestAnnotationTask.cancel();
        }
//        AnnotationMarkProvider amp = AnnotationMarkInstaller.getMarkProvider(textComponent);
//        if (amp != null) {
//            amp.setMarks(Collections.<AnnotationMark>emptyList());
//        }

    }

    /**
     * Paints one view that corresponds to a line (or
     * multiple lines if folding takes effect).
     */
    private void paintView(View view, Graphics g, int yBase) {
        JTextComponent component = editorUI.getComponent();
        if (component == null) {
            return;
        }
        BaseTextUI textUI = (BaseTextUI)component.getUI();

        Element rootElem = textUI.getRootView(component).getElement();
        int line = rootElem.getElementIndex(view.getStartOffset());

        String annotation = "";  // NOI18N
        Location al = null;
        if (elementAnnotations != null) {
            al = getAnnotateLine(line);
            if (al != null) {
                annotation = getDisplayName(al);  // NOI18N
            }
        } 

        if (al != null && getDisplayName(al).equals(recentLocationName)) {
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
    @Messages({
        "AnnBar.Line=Line:{0}", 
        "AnnBar.File=File:{0}", 
        "AnnBar.Model=Model: <b>{0}</b>",
        "AnnBar.Value=Value originating from",
        "AnnBar.NonDetermined=Line's origin cannot be determined.<br/>Either it's coming from the superpom, or Maven doesn't provide the location information for the element."
    })
    public String getToolTipText (MouseEvent e) {
        if (editorUI == null) {
            return null;
        }
        int line = getLineFromMouseEvent(e);

        StringBuilder annotation = new StringBuilder();
        if (elementAnnotations != null) {
            Location al = getAnnotateLine(line);

            if (al != null && al.loc.getSource() != null) {
                annotation.append("<html>").append(AnnBar_Value()).append("<br/>");
                annotation.append(AnnBar_Model(al.loc.getSource().getModelId())).append("<br/>");
                annotation.append(AnnBar_Line(al.loc.getLineNumber())).append("<br/>");
                if (al.loc.getSource().getLocation() != null) {
                    annotation.append(AnnBar_File(al.loc.getSource().getLocation()));
                }
                annotation.append("</html>");
            } else {
                annotation.append("<html>").append(AnnBar_NonDetermined()).append("</html>");
            }
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
    private Location getAnnotateLine(int line) {
        if (elementAnnotations != null) {
            return  elementAnnotations.get(line);
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
        if (component == null) {
            return;
        }

        BaseTextUI textUI = (BaseTextUI)component.getUI();
        View rootView = Utilities.getDocumentView(component);
        if (rootView == null) {
            return;
        }

        g.setColor(backgroundColor());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        AbstractDocument docum = (AbstractDocument)component.getDocument();
        docum.readLock();
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
            docum.readUnlock();
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
        return selectedColor;
        //TODO don't really understand in which situation the selected color is picked
        //it seems like textcomponent is never null
//        if (backgroundColor.equals(backgroundColor())) {
//            return selectedColor;
//        }
//        if (textComponent != null) {
//            return textComponent.getForeground();
//        }
//        return selectedColor;

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
        if (evt == null) {
            return;
        }
        String id = evt.getPropertyName();
        if (EditorUI.COMPONENT_PROPERTY.equals(id)) {  // NOI18N
            if (evt.getNewValue() == null){
                // component deinstalled, lets uninstall all isteners
                release();
            }
        }

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

}

