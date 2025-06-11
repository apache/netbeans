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

package org.netbeans.modules.editor.hints;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.GuardedException;
import org.netbeans.editor.JumpList;
import org.netbeans.editor.StatusBar;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.hints.borrowed.ListCompletionView;
import org.netbeans.modules.editor.hints.borrowed.ScrollCompletionPane;
import org.netbeans.modules.editor.lib2.highlighting.HighlightingManager;
import org.netbeans.spi.editor.highlighting.HighlightAttributeValue;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Context;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.PositionRefresher;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.StatusDisplayer.Message;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotation;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;


/**
 * Responsible for painting the things the user sees that indicate available
 * hints.
 *
 * @author Tim Boudreau
 */
public final class HintsUI implements MouseListener, MouseMotionListener, KeyListener, PropertyChangeListener, AWTEventListener, CaretListener, FocusListener  {

    //-J-Dorg.netbeans.modules.editor.hints.HintsUI.always.show.error=true
    private static final boolean ALWAYS_SHOW_ERROR_MESSAGE = Boolean.getBoolean(HintsUI.class.getName() + ".always.show.error");
    private static HintsUI INSTANCE;
    private static final String POPUP_NAME = "hintsPopup"; // NOI18N
    private static final String SUB_POPUP_NAME = "subHintsPopup"; // NOI18N
    private static final int POPUP_VERTICAL_OFFSET = 5;
    private static final RequestProcessor WORKER = new RequestProcessor(HintsUI.class.getName(), 1, false, false);


    public static synchronized HintsUI getDefault() {
        if (INSTANCE == null)
            INSTANCE = new HintsUI();
        
        return INSTANCE;
    }
    
    static final Logger UI_GESTURES_LOGGER = Logger.getLogger("org.netbeans.ui.editor.hints");
    
    private Reference<JTextComponent> compRef;
    private Popup listPopup;
    private Popup sublistPopup;
    private Popup tooltipPopup;
    private JLabel hintIcon;
    private ScrollCompletionPane hintListComponent;
    private ScrollCompletionPane subhintListComponent;
    private JTextArea errorTooltip;
    private AtomicBoolean cancel;

    private boolean altEnterPressed = false;
    private boolean altReleased = false;

    @SuppressWarnings("LeakingThisInConstructor")
    private HintsUI() {
        EditorRegistry.addPropertyChangeListener(this);
        propertyChange(null);
        cancel = new AtomicBoolean(false);
    }
    
    public JTextComponent getComponent() {
        return compRef == null ? null : compRef.get();
    }
    
    public void removeHints() {
        removePopups();
        setComponent(null);
    }
    
    public void setComponent (JTextComponent comp) {
        JTextComponent thisComp = getComponent();
        boolean change = thisComp != comp;
        if (change) {
            unregister();
            this.compRef = new WeakReference<JTextComponent>(comp);
            register();
            caretUpdate(null);
        }
    }

    private AnnotationHolder getAnnotationHolder(Document doc) {
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        if (od == null) {
            return null;
        }
        return AnnotationHolder.getInstance(od.getPrimaryFile());
    }

    private void register() {
        JTextComponent comp = getComponent(); 
        if (comp == null) {
            return;
        }
        comp.addKeyListener (this);
        comp.addCaretListener(this);
    }
    
    private void unregister() {
        JTextComponent comp = getComponent(); 
        if (comp == null) {
            return;
        }
        comp.removeKeyListener (this);
        comp.removeCaretListener(this);
    }
    
    
    public void removePopups() {
        JTextComponent comp = getComponent(); 
        if (comp == null) {
            return;
        }
        removeIconHint();
        removePopup();
    }
    
    private void removeIconHint() {
        if (hintIcon != null) {
            Container cont = hintIcon.getParent();
            if (cont != null) {
                Rectangle bds = hintIcon.getBounds();
                cont.remove (hintIcon);
                cont.repaint (bds.x, bds.y, bds.width, bds.height);
            }
        }
    }
    
    private void removePopup() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        if (listPopup != null) {
            closeSubList();
            if( tooltipPopup != null )
                tooltipPopup.hide();
            tooltipPopup = null;
            listPopup.hide();
            if (hintListComponent != null) {
                hintListComponent.getView().removeMouseListener(this);
                hintListComponent.getView().removeMouseMotionListener(this);
            }
            if (errorTooltip != null) {
                errorTooltip.removeMouseListener(this);
            }
            hintListComponent = null;
            errorTooltip = null;
            listPopup = null;
            if (hintIcon != null)
                hintIcon.setToolTipText(NbBundle.getMessage(HintsUI.class, "HINT_Bulb")); // NOI18N
        }
    }

    public void closeSubList() {
        if (sublistPopup != null) {
            sublistPopup.hide();
            if (subhintListComponent != null) {
                subhintListComponent.getView().removeMouseListener(this);
                subhintListComponent.getView().removeMouseMotionListener(this);
            }

            subhintListComponent = null;
            sublistPopup = null;
        }
    }

    public void openSubList(Iterable<? extends Fix> fixes, Point p) {
        JTextComponent comp = getComponent();

        if (comp == null) return;

        if (subhintListComponent != null) {
            closeSubList();
        }
        
        List<Fix> ff = new LinkedList<Fix>();

        for (Fix f : fixes) {
            ff.add(f);
        }

//        SwingUtilities.convertPointToScreen(p, comp);

        Rectangle maxSize = getScreenBounds();
        maxSize.width -= p.x;
        maxSize.height -= p.y;

        subhintListComponent =
                new ScrollCompletionPane(comp, new FixData(ErrorDescriptionFactory.lazyListForFixes(ff), ErrorDescriptionFactory.lazyListForFixes(Arrays.<Fix>asList())), null, null, new Dimension(maxSize.width, maxSize.height));

        subhintListComponent.getView().addMouseListener (this);
        subhintListComponent.getView().addMouseMotionListener(this);
        subhintListComponent.setName(SUB_POPUP_NAME);

        assert sublistPopup == null;
        sublistPopup = getPopupFactory().getPopup(
                comp, subhintListComponent, p.x, p.y);
        sublistPopup.show();
    }
    
    boolean isKnownComponent(Component c) {
        JTextComponent comp = getComponent(); 
        return c != null && 
               (c == comp 
                || c == hintIcon 
                || c == hintListComponent
                || (c instanceof Container && ((Container)c).isAncestorOf(hintListComponent))
                )
        ;
    }
    
    public void showPopup(FixData hints) {
        JTextComponent comp = getComponent(); 
        if (comp == null || (hints.isComputed() && hints.getFixes().isEmpty())) {
            return;
        }
        if (hintIcon != null)
            hintIcon.setToolTipText(null);
        // be sure that hint will hide when popup is showing
        ToolTipManager.sharedInstance().setEnabled(false);
        ToolTipManager.sharedInstance().setEnabled(true);
        assert hintListComponent == null;
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK);
        
        try {
            int pos = javax.swing.text.Utilities.getRowStart(comp, comp.getCaret().getDot());
            Rectangle r = comp.modelToView (pos);

            Point p = new Point (r.x + 5, r.y + 20);
            SwingUtilities.convertPointToScreen(p, comp);
            
            Rectangle maxSize = getScreenBounds();
            maxSize.width -= p.x;
            maxSize.height -= p.y;
            hintListComponent = 
                    new ScrollCompletionPane(comp, hints, null, null, new Dimension(maxSize.width, maxSize.height));

            hintListComponent.getView().addMouseListener (this);
            hintListComponent.getView().addMouseMotionListener(this);
            hintListComponent.setName(POPUP_NAME);
            
            assert listPopup == null;
            listPopup = getPopupFactory().getPopup(
                    comp, hintListComponent, p.x, p.y);
            listPopup.show();
        } catch (BadLocationException ble) {
            ErrorManager.getDefault().notify (ble);
            removeHints();
        }
    }
    
    public void showPopup(FixData fixes, String description, JTextComponent component, Point position) {
        removeHints();
        setComponent(component);
        JTextComponent comp = getComponent(); 
        
        if (comp == null || fixes == null)
            return ;

        Point p = new Point(position);
        SwingUtilities.convertPointToScreen(p, comp);
        
        if (hintIcon != null)
            hintIcon.setToolTipText(null);
        // be sure that hint will hide when popup is showing
        ToolTipManager.sharedInstance().setEnabled(false);
        ToolTipManager.sharedInstance().setEnabled(true);
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK);
        Rectangle screen = getScreenBounds();
        Dimension screenDim = new Dimension(screen.width, screen.height);

        errorTooltip = new JTextArea(description); // NOI18N
        errorTooltip.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            BorderFactory.createEmptyBorder(0, 3, 0, 3)
        ));
        errorTooltip.setEditable(false);
        Dimension pref = errorTooltip.getPreferredSize();
        // Since PopupFactory can display the resulting component so that
        // it goes off-screen on the right instead count with component's usable width.
        int usableWidth = getUsableWidth(component);
        if (pref.width > usableWidth) { // Too wide -> wrap
            errorTooltip.setLineWrap(true);
            errorTooltip.setWrapStyleWord(true);
            // Force wrapping
            errorTooltip.setSize(new Dimension(usableWidth, screenDim.height));
            // Re-read preferred size to determine proper height
            pref = errorTooltip.getPreferredSize();
        }
        if (pref.height > screenDim.height) {
            pref.height = screenDim.height;
        }
        errorTooltip.setSize(pref);
        errorTooltip.addMouseListener(this);
        
        if (!fixes.isComputed() || fixes.getFixes().isEmpty()) {
            //show tooltip:
            assert listPopup == null;
            
            listPopup = getPopupFactory().getPopup(
                    comp, errorTooltip, p.x, p.y);
        } else {
            assert hintListComponent == null;
            
            int rowHeight = 14; //default

            hintListComponent =
                    new ScrollCompletionPane(comp, fixes, null, null, screenDim);
            Dimension hintPopup = hintListComponent.getPreferredSize();
            int ySpaceWhenPlacedUp = p.y - (rowHeight + POPUP_VERTICAL_OFFSET);
            boolean exceedsHeight = p.y + hintPopup.height > screen.height;
            boolean placeUp = exceedsHeight && (ySpaceWhenPlacedUp > screenDim.height - p.y);

            if (ALWAYS_SHOW_ERROR_MESSAGE) {
            try {
                int pos = javax.swing.text.Utilities.getRowStart(comp, comp.getCaret().getDot());
                Rectangle r = comp.modelToView (pos);
                rowHeight = r.height;

                int y;
                final Dimension errorPopup = errorTooltip.getPreferredSize();
                if (placeUp)
                    y = p.y + POPUP_VERTICAL_OFFSET;
                else
                    y = p.y-rowHeight-errorPopup.height-POPUP_VERTICAL_OFFSET;

                //shift error popup left if necessary
                int xPos = p.x;
                if (p.x - screen.x + errorPopup.width > screen.width) {
                    xPos -= p.x - screen.x + errorPopup.width - screen.width;
                }

                tooltipPopup = getPopupFactory().getPopup(
                        comp, errorTooltip, xPos, y);
            } catch( BadLocationException blE ) {
                ErrorManager.getDefault().notify (blE);
                errorTooltip = null;
            }
            }

            if (placeUp) {
                hintListComponent =
                    new ScrollCompletionPane(comp, fixes, null, null, new Dimension(screenDim.width, Math.min(ySpaceWhenPlacedUp, hintPopup.height)));
                hintPopup = hintListComponent.getPreferredSize();
                p.y -= hintPopup.height + rowHeight + POPUP_VERTICAL_OFFSET;
                assert p.y >= 0;
            } else if (exceedsHeight) {
                hintListComponent =
                        new ScrollCompletionPane(comp, fixes, null, null, new Dimension(screenDim.width, Math.min(screenDim.height - p.y, hintPopup.height)));
            }

            //shift hint popup left if necessary
            if(p.x - screen.x + hintPopup.width > screen.width) {
                p.x -= p.x - screen.x + hintPopup.width - screen.width;
            }

            hintListComponent.getView().addMouseListener (this);
            hintListComponent.getView().addMouseMotionListener(this);
            hintListComponent.setName(POPUP_NAME);
            assert listPopup == null;
            listPopup = getPopupFactory().getPopup(
                    comp, hintListComponent, p.x, p.y);
        }
        
        if( tooltipPopup != null )
            tooltipPopup.show();
        listPopup.show();
    }
    
    private PopupFactory pf = null;
    private PopupFactory getPopupFactory() {
        if (pf == null) {
            pf = PopupFactory.getSharedInstance();
        }
        return pf;
    }

    private Rectangle getScreenBounds() throws HeadlessException {
      Rectangle virtualBounds = new Rectangle();
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice[] gs = ge.getScreenDevices();

      if (gs.length == 0 || gs.length == 1) {
          return new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
      }
     
      for (GraphicsDevice gd : gs) {
          virtualBounds = virtualBounds.union(gd.getDefaultConfiguration().getBounds());
      }

      return virtualBounds;
    }

    private int getUsableWidth(JTextComponent component) {
        Container parent = component.getParent();
        if (parent instanceof JLayeredPane) {
            parent = parent.getParent();
        }
        return (parent instanceof JViewport)
            ? ((JViewport)parent).getExtentSize().width
            : component.getSize().width;
    }

//    private Dimension getMaxSizeAt( Point p ) {
//        Rectangle screenBounds = getScreenBounds();
//        Dimension maxSize = screenBounds.getSize();
//        maxSize.width -= p.x - screenBounds.x;
//        maxSize.height -= p.y - screenBounds.y;
//        return maxSize;
//    }

    public void mouseClicked(java.awt.event.MouseEvent e) {
        if (   e.getSource() == hintListComponent.getView()
            && hintListComponent.getView().getSize().width - ListCompletionView.arrowSpan() <= e.getPoint().x) {
            if (hintListComponent.getView().right()) {
                e.consume();
                return;
            }
        }

        if (e.getSource() instanceof ListCompletionView) {
            Fix f = null;
            Object selected = ((ListCompletionView) e.getSource()).getSelectedValue();
            
            if (selected instanceof Fix) {
                f = (Fix) selected;
            }
            
            if (f != null) {
                e.consume();
                JTextComponent tc = getComponent();
                invokeHint (f);
                if (tc != null && org.openide.util.Utilities.isMac()) {
                    // see issue #65326
                    tc.requestFocus();
                }
                removeHints();
                //the component was reset when setHints was called, set it back so further hints will work:
                setComponent(tc);
            }
        }
    }

    public void mouseEntered(java.awt.event.MouseEvent e) {
    }

    public void mouseExited(java.awt.event.MouseEvent e) {
    }

    public void mousePressed(java.awt.event.MouseEvent e) {
    }

    public void mouseReleased(java.awt.event.MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
        if (e.getSource() instanceof ListCompletionView) {
            ListCompletionView view = (ListCompletionView) e.getSource();
            int wasSelected = view.getSelectedIndex();

            view.setSelectedIndex(view.locationToIndex(e.getPoint()));

            if (wasSelected != view.getSelectedIndex() && view == hintListComponent.getView()) {
                closeSubList();
            }

            if (sublistPopup != null && e.getSource() == hintListComponent.getView()
                    && hintListComponent.getView().getSize().width - ListCompletionView.arrowSpan() > e.getPoint().x) {
                closeSubList();
            }

            if (sublistPopup == null && e.getSource() == hintListComponent.getView()
                    && hintListComponent.getView().getSize().width - ListCompletionView.arrowSpan() <= e.getPoint().x) {
                if (hintListComponent.getView().right()) {
                    e.consume();
                }
            }
        }
    }

    public boolean isActive() {
        boolean bulbShowing = hintIcon != null && hintIcon.isShowing();
        boolean popupShowing = hintListComponent != null && hintListComponent.isShowing();
        return bulbShowing || popupShowing;
    }

    public boolean isPopupActive() {
        return hintListComponent != null && hintListComponent.isShowing();
    }

    private ParseErrorAnnotation findAnnotation(Document doc, AnnotationDesc desc, int lineNum) {
        AnnotationHolder annotations = getAnnotationHolder(doc);

        if (annotations != null) {
            for (Annotation a : annotations.getAnnotations()) {
                if (a instanceof ParseErrorAnnotation) {
                    ParseErrorAnnotation pa = (ParseErrorAnnotation) a;

                    if (lineNum == pa.getLineNumber() && desc != null
                            && org.openide.util.Utilities.compareObjects(desc.getShortDescription(), a.getShortDescription())) {
                        return pa;
                    }
                }
            }
        }

        return null;
    }

    boolean invokeDefaultAction(boolean onlyActive) {
        JTextComponent comp = getComponent();
        if (comp == null) {
            Logger.getLogger(HintsUI.class.getName()).log(Level.WARNING, "HintsUI.invokeDefaultAction called, but comp == null");
            return false;
        }

        Document doc = comp.getDocument();

        cancel.set(false);
        
        if (doc instanceof BaseDocument) {
            try {
                Rectangle carretRectangle = comp.modelToView(comp.getCaretPosition());
                int line = Utilities.getLineOffset((BaseDocument) doc, comp.getCaretPosition());
                FixData fixes;
                String description;

                if (!onlyActive) {
                    refresh(doc, comp.getCaretPosition());
                    AnnotationHolder holder = getAnnotationHolder(doc);
                    Pair<FixData, String> fixData = holder != null ? holder.buildUpFixDataForLine(line) : null;

                    if (fixData == null) return false;

                    fixes = fixData.first();
                    description = fixData.second();
                } else {
                    AnnotationDesc activeAnnotation = ((BaseDocument) doc).getAnnotations().getActiveAnnotation(line);
                    if (activeAnnotation == null) {
                        return false;
                    }
                    String type = activeAnnotation.getAnnotationType();
                    if (!FixAction.getFixableAnnotationTypes().contains(type) && onlyActive) {
                        return false;
                    }
                    if (onlyActive) {
                        refresh(doc, comp.getCaretPosition());
                    }
                    Annotations annotations = ((BaseDocument) doc).getAnnotations();
                    AnnotationDesc desc = annotations.getAnnotation(line, type);
                    ParseErrorAnnotation annotation = null;
                    if (desc != null) {
                        annotations.frontAnnotation(desc);
                        annotation = findAnnotation(doc, desc, line);
                    }

                    if (annotation == null) {
                        return false;
                    }
                    
                    fixes = annotation.getFixes();
                    description = annotation.getDescription();
                }

                Point p = comp.modelToView(Utilities.getRowStartFromLineOffset((BaseDocument) doc, line)).getLocation();
                p.y += carretRectangle.height;
                if(comp.getParent() instanceof JViewport) {
                    p.x += ((JViewport)comp.getParent()).getViewPosition().x;
                }
                if(comp.getParent() instanceof JLayeredPane &&
                        comp.getParent().getParent() instanceof JViewport) {
                    p.x += ((JViewport)comp.getParent().getParent()).getViewPosition().x;
                }

                showPopup(fixes, description, comp, p);

                return true;
            } catch (BadLocationException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }

        return false;
    }

    public void keyPressed(KeyEvent e) {
        JTextComponent comp = getComponent(); 
        if (comp == null || e.isConsumed()) {
            return;
        }
        
        boolean codeComplationShowing = comp.getClientProperty("completion-visible") == Boolean.TRUE; //NOI18N
        if (codeComplationShowing) {
            return;
        }
        
//        boolean bulbShowing = hintIcon != null && hintIcon.isShowing();
        boolean errorTooltipShowing = errorTooltip != null && errorTooltip.isShowing();
        boolean popupShowing = hintListComponent != null && hintListComponent.isShowing();
        
        if (errorTooltipShowing && !popupShowing) {
            //any key should disable the errorTooltip:
            removePopup();
            return ;
        }
        if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {
            if (e.getModifiersEx() == KeyEvent.ALT_DOWN_MASK) {
                if ( !popupShowing) {
                    if (org.openide.util.Utilities.isWindows()
                            && !Boolean.getBoolean("HintsUI.disable.AltEnter.hack")) { // NOI18N
                        // Fix (workaround) for issue #186557
                        altEnterPressed = true;
                        altReleased = false;
                    }
                    invokeDefaultAction(false);
                    e.consume();
                }
            } else if ( (e.getModifiersEx() & ((1 << 14) - 1)) == 0 ) {
                if (popupShowing) {
                    Fix f = null;
                    ScrollCompletionPane listPane = subhintListComponent != null ? subhintListComponent : hintListComponent;
                    Object selected = listPane.getView().getSelectedValue();
                    
                    if (selected instanceof Fix) {
                        f = (Fix) selected;
                    }
                    
                    if (f != null) {
                        invokeHint(f);
                    }
                    
                    e.consume();
                }
            }
        } else if ( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
            if ( popupShowing ) {
                removePopup();
                e.consume();
            } else {
                //user is tired of waiting for refresh before popup is shown
                cancel.set(true);
            }
        } else if ( popupShowing ) {
            ScrollCompletionPane listPane = subhintListComponent != null ? subhintListComponent : hintListComponent;
            InputMap input = listPane.getInputMap();
            Object actionTag = input.get(KeyStroke.getKeyStrokeForEvent(e));
            if (actionTag != null) {
                Action a = listPane.getActionMap().get(actionTag);
                a.actionPerformed(null);
                e.consume();
                return ;
            } else {
                removePopup();
            }
        } 
    }    

    public void keyReleased(KeyEvent e) {
        // Fix (workaround) for issue #186557
        if (org.openide.util.Utilities.isWindows()) {
            if (Boolean.getBoolean("HintsUI.disable.AltEnter.hack")) { // NOI18N
                return;
            }

            if (altEnterPressed && e.getKeyCode() == KeyEvent.VK_ALT) {
                e.consume();
                altReleased = true;
            } else if (altEnterPressed && e.getKeyCode() == KeyEvent.VK_ENTER) {
                altEnterPressed = false;
                if (altReleased) {
                    try {
                        java.awt.Robot r = new java.awt.Robot();
                        r.keyRelease(KeyEvent.VK_ALT);
                    } catch (AWTException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }

    public void keyTyped(KeyEvent e) {
    }
    
    private ChangeInfo changes;
    
    private void invokeHint (final Fix f) {
        if (UI_GESTURES_LOGGER.isLoggable(Level.FINE)) {
            LogRecord rec = new LogRecord(Level.FINE, "GEST_HINT_INVOKED");
            
            rec.setResourceBundle(NbBundle.getBundle(HintsUI.class));
            rec.setParameters(new Object[] {f.getText()});
            UI_GESTURES_LOGGER.log(rec);
        }
        
        removePopups();
        final JTextComponent component = getComponent(); 
        JumpList.checkAddEntry(component);
        final Cursor cur = component.getCursor();
        component.setCursor (Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
        Task t = null;
        try {
            t = RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        changes = f.implement();
                    } catch (GuardedException ge) {
                            reportGuardedException(component, ge);
                    } catch (IOException e) {
                        if (e.getCause() instanceof GuardedException) {
                            reportGuardedException(component, e);
                        } else {
                            Exceptions.printStackTrace(e);
                        }
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            });
        } finally {
            if (t != null) {
                t.addTaskListener(new TaskListener() {
                    public void taskFinished(Task task) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                open(changes, component);
                                component.setCursor (cur);
                            }
                        });
                    }
                });
            }
        }
    }
    
    private static void reportGuardedException(final JTextComponent component, final Exception e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String message = NbBundle.getMessage(HintsUI.class, "ERR_CannotApplyGuarded");

                Utilities.setStatusBoldText(component, message);
                Logger.getLogger(HintsUI.class.getName()).log(Level.FINE, null, e);
            }
        });
    }
    
    private static void open(ChangeInfo changes, JTextComponent component) {
        JTextComponent tc = component;
        if (changes != null && changes.size() > 0) {
            ChangeInfo.Change change = changes.get(0);
            FileObject file = change.getFileObject();
            if (file != null) {
                try {
                    DataObject dob = 
                        DataObject.find (file);

                    EditCookie ck = dob.getCookie(EditCookie.class);

                    if (ck != null) {
                        //Try EditCookie first so we don't open the form
                        //editor
                        ck.edit();
                    } else {
                        OpenCookie oc = dob.getCookie(OpenCookie.class);

                        oc.open();
                    }
                    EditorCookie edit = dob.getCookie(EditorCookie.class);

                    JEditorPane[] panes = edit.getOpenedPanes();
                    if (panes != null && panes.length > 0) {
                        tc = panes[0];
                    } else {
                        return;
                    }

                } catch (DataObjectNotFoundException donfe) {
                    Logger.getLogger(HintsUI.class.getName()).log(Level.FINE, null, donfe);
                    return;
                }
            }
            Position start = change.getStart();
            Position end = change.getEnd();
            if (start != null) {
                tc.setSelectionStart(start.getOffset());
            }
            if (end != null) {
                tc.setSelectionEnd(end.getOffset());
            }
        }
    }

    public void propertyChange(PropertyChangeEvent e) {
        JTextComponent active = EditorRegistry.lastFocusedComponent();
        
        if (getComponent() != active) {
            removeHints();
            setComponent(active);

            if (getComponent() != null) {
                getComponent().removeFocusListener(this);
            }

            if (active != null) {
                active.addFocusListener(this);
            }
        }
    }
    
    public void eventDispatched(java.awt.AWTEvent aWTEvent) {
        if (aWTEvent instanceof MouseEvent) {
            MouseEvent mv = (MouseEvent)aWTEvent;
            if (mv.getID() == MouseEvent.MOUSE_CLICKED && mv.getClickCount() > 0) {
                //#118828
                if (! (aWTEvent.getSource() instanceof Component)) {
                    removePopup();
                    return;
                }
                
                Component comp = (Component)aWTEvent.getSource();
                Container par1 = SwingUtilities.getAncestorNamed(POPUP_NAME, comp); //NOI18N
                Container par2 = SwingUtilities.getAncestorNamed(SUB_POPUP_NAME, comp); //NOI18N
                // Container barpar = SwingUtilities.getAncestorOfClass(PopupUtil.class, comp);
                // if (par == null && barpar == null) {
                if ( par1 == null && par2 == null ) {
                    removePopup();
                }
            }
        }
    }

    private static String[] c = new String[] {"&", "<", ">", "\n", "\""}; // NOI18N
    private static String[] tags = new String[] {"&amp;", "&lt;", "&gt;", "<br>", "&quot;"}; // NOI18N
    
    private String translate(String input) {
        for (int cntr = 0; cntr < c.length; cntr++) {
            input = input.replaceAll(c[cntr], tags[cntr]);
        }
        
        return input;
    }

    private void refresh(Document doc, int pos) {
        AnnotationHolder holder = getAnnotationHolder(doc);

        if (holder == null) {
            Logger.getLogger(HintsUI.class.getName()).log(Level.FINE, "No AnnotationHolder associated to: {0} (stream description property: {1})", new Object[] {doc, doc.getProperty(Document.StreamDescriptionProperty)});
            return ;
        }
        
        Context context = ContextAccessor.getDefault().newContext(pos, cancel);
        String mimeType = org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(doc);
        Lookup lookup = MimeLookup.getLookup(mimeType);
        Collection<? extends PositionRefresher> refreshers = lookup.lookupAll(PositionRefresher.class);
        //set errors from all available refreshers
        for (PositionRefresher ref : refreshers) {
            Map<String, List<ErrorDescription>> layer2Errs = ref.getErrorDescriptionsAt(context, doc);
            holder.setErrorsForLine(pos, layer2Errs);
        }
    }

    public void undoOnePopup() {
        if (sublistPopup != null) {
            closeSubList();
        } else {
            removePopups();
        }
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        JTextComponent currentComponent = compRef != null ? compRef.get() : null;

        if (currentComponent == null) return ;
        
        final HighlightingManager hm = HighlightingManager.getInstance(currentComponent);
        final Document doc = currentComponent.getDocument();
        final Caret caretInstance = currentComponent.getCaret();

        if (caretInstance == null) return ;

        final int caret = caretInstance.getDot();

        WORKER.post(new Runnable() {
            @Override public void run() {
                final String[] warning = new String[] {AnnotationHolder.resolveWarnings(doc, caret, caret)};

                if (warning[0] == null || warning[0].trim().isEmpty()) {
                    final HighlightAttributeValue[] hav = new HighlightAttributeValue[1];
                    doc.render(new Runnable() {
                        @Override public void run() {
                            HighlightsSequence hit = hm.getBottomHighlights().getHighlights(caret, caret + 1);

                            if (hit.moveNext()) {
                                AttributeSet attrs = hit.getAttributes();
                                if (attrs != null && attrs.containsAttribute("unused-browseable", Boolean.TRUE)) {
                                    Object tp = hit.getAttributes().getAttribute(EditorStyleConstants.Tooltip);
                                    if (tp instanceof HighlightAttributeValue) {
                                        hav[0] = (HighlightAttributeValue) tp;
                                    }
                                }
                            }
                        }
                    });
                    
                    if (hav[0] != null) {
                        Object res = hav[0].getValue(errorTooltip, doc, hav[0], caret, caret);

                        if (res instanceof String) {
                            warning[0] = (String) res;
                        }
                    }
                } else {
                    warning[0] = warning[0].replace('\n', ' ');
                }

                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        JTextComponent currentComponent = compRef.get();

                        if (currentComponent == null) return;

                        if (warning[0] == null || warning[0].trim().isEmpty()) {
                            CaretLocationAndMessage clam = (CaretLocationAndMessage) currentComponent.getClientProperty(CaretLocationAndMessage.class);

                            if (clam != null) {
                                clam.message.clear(0);
                                currentComponent.putClientProperty(CaretLocationAndMessage.class, null);
                            }
                            
                            return;
                        }

                        CaretLocationAndMessage clam = (CaretLocationAndMessage) currentComponent.getClientProperty(CaretLocationAndMessage.class);

                        if (clam != null && clam.caret == caret && warning[0].equals(clam.lastMessage)) {
                            return ;
                        }

                        EditorUI editorUI = Utilities.getEditorUI(currentComponent);
                        StatusBar sb = editorUI != null ? editorUI.getStatusBar() : null;

                        if (sb != null && sb.isVisible()) {
                            Utilities.setStatusText(currentComponent, warning[0], StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
                        } else {
                            Message m = StatusDisplayer.getDefault().setStatusText(warning[0], StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
                            currentComponent.putClientProperty(CaretLocationAndMessage.class, new CaretLocationAndMessage(caret, warning[0], m));

                            //TODO: so that messages with lower priority have chance to be displayed, ideally should not be needed:
                            m.clear(5000);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
        removePopups();
    }

    private static final class CaretLocationAndMessage {
        final int caret;
        final String lastMessage;
        final Message message;

        public CaretLocationAndMessage(int caret, String lastMessage, Message message) {
            this.caret = caret;
            this.lastMessage = lastMessage;
            this.message = message;
        }

    }

}
