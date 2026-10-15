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

package org.netbeans.editor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.text.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.TextUI;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.Action;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.Position.Bias;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.editor.lib2.EditorApiPackageAccessor;
import org.netbeans.editor.view.spi.LockView;
import org.netbeans.lib.editor.view.GapDocumentView;
import org.netbeans.modules.editor.lib2.EditorPreferencesKeys;
import org.netbeans.modules.editor.lib.SettingsConversions;
import org.netbeans.modules.editor.lib.drawing.DrawEngineDocView;
import org.netbeans.modules.editor.lib.drawing.DrawEngineLineView;
import org.netbeans.modules.editor.lib2.view.LockedViewHierarchy;
import org.netbeans.modules.editor.lib2.view.ViewHierarchy;
import org.netbeans.spi.lexer.MutableTextInput;

/**
* Text UI implementation
* 
* @author  Miloslav Metelka, Martin Roskanin
* @version 1.00
*/

public class BaseTextUI extends BasicTextUI implements
        PropertyChangeListener, DocumentListener, AtomicLockListener {

    // -J-Dorg.netbeans.editor.BaseTextUI.level=FINEST
    private static final Logger LOG = Logger.getLogger(BaseTextUI.class.getName());

    /* package */ static final String PROP_DEFAULT_CARET_BLINK_RATE = "nbeditor-default-swing-caret-blink-rate"; //NOI18N

    /**
     * How many modifications inside atomic section is considered a lengthy operation (e.g. reformat).
     */
    private static final int LENGTHY_ATOMIC_EDIT_THRESHOLD = 80;
    
    /** Extended UI */
    private EditorUI editorUI;

    private boolean needsRefresh = false;
    
    /** ID of the component in registry */
    int componentID = -1;
    
    private Document lastDocument;

    private int atomicModCount = -1;
    
    /** Instance of the <tt>GetFocusedComponentAction</tt> */
    private static final GetFocusedComponentAction gfcAction
    = new GetFocusedComponentAction();

    private Preferences prefs = null;
    
    public BaseTextUI() {
    }
    
    protected String getPropertyPrefix() {
        return "EditorPane"; //NOI18N
    }

    public static JTextComponent getFocusedComponent() {
        return gfcAction.getFocusedComponent2();
    }

    protected boolean isRootViewReplaceNecessary() {
        boolean replaceNecessary = false;
        
        Document doc = getComponent().getDocument();
        if (doc != lastDocument) {
            replaceNecessary = true;
        }
        
        return replaceNecessary;
    }

    protected void rootViewReplaceNotify() {
        // update the newly used document
        lastDocument = getComponent().getDocument();
    }

    /** Called when the model of component is changed */
    protected @Override void modelChanged() {
        JTextComponent component = getComponent();
        Document doc = component != null ? component.getDocument() : null;
        
        if (component != null && doc != null) {
            boolean documentReplaced = isRootViewReplaceNecessary();

            component.removeAll();
            if (documentReplaced) {
                ViewFactory f = getRootView(component).getViewFactory();
                rootViewReplaceNotify();
                Element elem = doc.getDefaultRootElement();
                View v = f.create(elem);
                setView(v);
            }
            component.revalidate();
            
            if (documentReplaced) {
                // Execute actions related to document installaction into the component
                BaseKit baseKit = Utilities.getKit(component);
                if (baseKit != null && prefs != null) {
                    List<String> actionNamesList = new  ArrayList<String>();
                    String actionNames = prefs.get(EditorPreferencesKeys.DOC_INSTALL_ACTION_NAME_LIST, ""); //NOI18N
                    for(StringTokenizer t = new StringTokenizer(actionNames, ","); t.hasMoreTokens(); ) { //NOI18N
                        String actionName = t.nextToken().trim();
                        actionNamesList.add(actionName);
                    }
                
                    List<Action> actionsList = baseKit.translateActionNameList(actionNamesList); // translate names to actions
                    for(Action a : actionsList) {
                        a.actionPerformed(new ActionEvent(component, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                    }
                }
            }
        }
    }

    
    /* XXX - workaround bugfix of issue #45487 and #45678 
     * The hack can be removed if JDK bug
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5067948
     * will be fixed.
     */
    protected @Override void installKeyboardActions() {
        String mapName = getPropertyPrefix() + ".actionMap"; //NOI18N
        // XXX - workaround bugfix of issue #45487
        // Because the ActionMap is cached in method BasicTextUI.getActionMap()
        // the property 'mapName' is set to null to force new actionMap creation
        UIManager.getLookAndFeelDefaults().put(mapName, null);        
        UIManager.getDefaults().put(mapName, null); //#45678
        super.installKeyboardActions();
    }

    /** Installs the UI for a component. */
    public @Override void installUI(JComponent c) {
        super.installUI(c);
        
        if (!(c instanceof JTextComponent)) {
            return;
        }
        
        JTextComponent component = getComponent();
        prefs = MimeLookup.getLookup(org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(component)).lookup(Preferences.class);

        
        // set margin
        String value = prefs.get(SimpleValueNames.MARGIN, null);
        Insets margin = value != null ? SettingsConversions.parseInsets(value) : null;
        component.setMargin(margin != null ? margin : EditorUI.NULL_INSETS);

        getEditorUI().installUI(component);
        
        // attach to the model and component
        //component.addPropertyChangeListener(this); already done in super class
        if (component.getClientProperty(UIWatcher.class) == null) {
            UIWatcher uiWatcher = new UIWatcher(this.getClass());
            component.addPropertyChangeListener(uiWatcher);
            component.putClientProperty(UIWatcher.class, uiWatcher);
        }
        
        BaseKit kit = (BaseKit)getEditorKit(component);
        ViewFactory vf = kit.getViewFactory();
        // Create and attach caret
        Caret defaultCaret = component.getCaret();
        Caret caret = kit.createCaret();
        component.setCaretColor(Color.black); // will be changed by settings later
        component.setCaret(caret);
        component.putClientProperty(PROP_DEFAULT_CARET_BLINK_RATE, defaultCaret.getBlinkRate());
        component.setKeymap(kit.getKeymap());
        
        // assign blink rate
        int br = prefs.getInt(SimpleValueNames.CARET_BLINK_RATE, -1);
        if (br == -1) {
            br = defaultCaret.getBlinkRate();
        }
        caret.setBlinkRate(br);

        SwingUtilities.replaceUIInputMap(c, JComponent.WHEN_FOCUSED, null);
        
        EditorApiPackageAccessor.get().register(component);
        component.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    }

    /** Deinstalls the UI for a component */
    public @Override void uninstallUI(JComponent c) {
        if (prefs == null) {
            // already uninstalled or not installed at all
            return;
        }

        if ((getComponent() != null) && getComponent().getDocument() != null) {
            super.uninstallUI(c);
        }

        prefs = null;
        
        if (c instanceof JTextComponent){        
            JTextComponent comp = (JTextComponent)c;
            BaseDocument doc = Utilities.getDocument(comp);
            if (doc != null) {
                doc.removeDocumentListener(this);
                doc.removeAtomicLockListener(this);
            }

            comp.setKeymap(null);
            comp.setCaret(null);

            getEditorUI().uninstallUI(comp);
        }
    }
    
    /**
     * Return y coordinate value for given offset.
     *
     * @param pos offset in a read-locked document
     * @return y
     * @throws BadLocationException in case offset is not in document's bounds.
     */
    public int getYFromPos(int pos) throws BadLocationException {
        JTextComponent component = getComponent();
        int y = 0;
        if (component != null) {
            ViewHierarchy vh = ViewHierarchy.get(component);
            LockedViewHierarchy lvh = vh.lock();
            try {
                y = (int) lvh.modelToY(pos);
            } finally {
                lvh.unlock();
            }
        }
        return y;
    }

    public int getPosFromY(int y) throws BadLocationException {
        JTextComponent component = getComponent();
        int offset = 0;
        if (component != null) {
            ViewHierarchy vh = ViewHierarchy.get(component);
            LockedViewHierarchy lvh = vh.lock();
            try {
                offset = lvh.viewToModel(0, y, null);
            } finally {
                lvh.unlock();
            }
        }
        return offset;
    }

    public int getBaseX(int y) {
        return getEditorUI().getTextMargin().left;
    }

    public int viewToModel(JTextComponent c, int x, int y) {
        return viewToModel(c, new Point(x, y));
    }

    @Override
    public void damageRange(JTextComponent t, int p0, int p1, Bias p0Bias, Bias p1Bias) {
        View rootView = getRootView(getComponent());
        boolean doDamageRange = true;
        if (rootView.getViewCount() > 0) {
            View view = rootView.getView(0);
            if (view instanceof LockView) {
                LockView lockView = (LockView) view;
                lockView.lock();
                try {
                    GapDocumentView docView = (GapDocumentView)view.getView(0);
                    doDamageRange = docView.checkDamageRange(p0, p1, p0Bias, p1Bias);
                } finally {
                    lockView.unlock();
                }
            }
        }
        if (doDamageRange) {
            // Patch since this used to be a fallback and the original views' impl cleared char area at p1 too
            Document doc = t.getDocument();
            if (doc != null && p1 < doc.getLength()) {
                p1++;
            }
            super.damageRange(t, p0, p1, p0Bias, p1Bias);
        }
    }

    /** Next visually represented model location where caret can be placed.
    * This version works without placing read lock on the document.
    */
    public @Override int getNextVisualPositionFrom(JTextComponent t, int pos,
                                         Position.Bias b, int direction, Position.Bias[] biasRet)
    throws BadLocationException{
        if (biasRet == null) {
            biasRet = new Position.Bias[1];
            biasRet[0] = Position.Bias.Forward;
        }
        return super.getNextVisualPositionFrom(t, pos, b, direction, biasRet);
    }



    /** Fetches the EditorKit for the UI.
    *
    * @return the component capabilities
    */
    public @Override EditorKit getEditorKit(JTextComponent c) {
        JEditorPane pane = (JEditorPane)getComponent();
        return (pane == null) ? null : pane.getEditorKit();
    }


    /** Get extended UI. This is called from views to get correct extended UI. */
    public EditorUI getEditorUI() {
        if (editorUI == null) {
            JTextComponent c = getComponent();
            BaseKit kit = (BaseKit)getEditorKit(c);
            if (kit != null) {
                editorUI = kit.createEditorUI();
                editorUI.initLineHeight(c);
            }
        }
        return editorUI;
    }

    /**
    * This method gets called when a bound property is changed.
    * We are looking for document changes on the component.
    */
    public @Override void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if ("document".equals(propName)) { // NOI18N
            BaseDocument oldDoc = (evt.getOldValue() instanceof BaseDocument)
                                  ? (BaseDocument)evt.getOldValue() : null;
                                  
            if (oldDoc != null) {
                oldDoc.removeDocumentListener(this);
                oldDoc.removeAtomicLockListener(this);
            }

            BaseDocument newDoc = (evt.getNewValue() instanceof BaseDocument)
                                  ? (BaseDocument)evt.getNewValue() : null;
                                  
            if (newDoc != null) {
                newDoc.addDocumentListener(this);
                atomicModCount = -1;
                newDoc.addAtomicLockListener(this);
            }
        } else if ("ancestor".equals(propName)) { // NOI18N
            JTextComponent comp = (JTextComponent)evt.getSource();
            if (comp.isDisplayable() && editorUI != null && editorUI.hasExtComponent()) {
                // #41209: In case extComponent was retrieved set the ancestorOverride
                // to true and expect that the editor kit that installed
                // this UI will be deinstalled explicitly.
                if (!Boolean.TRUE.equals(comp.getClientProperty("ancestorOverride"))) { // NOI18N
                    comp.putClientProperty("ancestorOverride", Boolean.TRUE); // NOI18N
                }
            }
        }
    }

    /** Insert to document notification. */
    public void insertUpdate(DocumentEvent evt) {
        checkLengthyAtomicEdit(evt);
        // No longer trigger syntax update related repaint
//        try {
//            BaseDocumentEvent bevt = (BaseDocumentEvent)evt;
//            EditorUI eui = getEditorUI();
//            int y = getYFromPos(evt.getOffset());
//            int lineHeight = eui.getLineHeight();
//            int syntaxY = getYFromPos(bevt.getSyntaxUpdateOffset());
//            // !!! patch for case when DocMarksOp.eolMark is at the end of document
//            if (bevt.getSyntaxUpdateOffset() == evt.getDocument().getLength()) {
//                syntaxY += lineHeight;
//            }
//            if (getComponent().isShowing()) {
//                eui.repaint(y, Math.max(lineHeight, syntaxY - y));
//            }
//        } catch (BadLocationException ex) {
//            Utilities.annotateLoggable(ex);
//        }
    }
    
    /** Remove from document notification. */
    public void removeUpdate(DocumentEvent evt) {
        checkLengthyAtomicEdit(evt);
        // No longer trigger syntax update related repaint
//        try {
//            BaseDocumentEvent bevt = (BaseDocumentEvent)evt;
//            EditorUI eui = getEditorUI();
//            int y = getYFromPos(evt.getOffset());
//            int lineHeight = eui.getLineHeight();
//            int syntaxY = getYFromPos(bevt.getSyntaxUpdateOffset());
//            // !!! patch for case when DocMarksOp.eolMark is at the end of document
//            if (bevt.getSyntaxUpdateOffset() == evt.getDocument().getLength()) {
//                syntaxY += lineHeight;
//            }
//            if (getComponent().isShowing()) {
//                eui.repaint(y, Math.max(lineHeight, syntaxY - y));
//            }
//
//        } catch (BadLocationException ex) {
//            Utilities.annotateLoggable(ex);
//        }
    }

    /** The change in document notification.
    *
    * @param evt  The change notification from the currently associated document.
    */
    public void changedUpdate(DocumentEvent evt) {
        if (evt instanceof BaseDocumentEvent) {
            try {
                JTextComponent comp = getComponent();
                if (comp!=null && comp.isShowing()) {
                    getEditorUI().repaintBlock(evt.getOffset(), evt.getOffset() + evt.getLength());
                }
            } catch (BadLocationException ex) {
                Utilities.annotateLoggable(ex);
            }
        }
    }

    private void checkLengthyAtomicEdit(DocumentEvent evt) {
        if (atomicModCount != -1) {
            if (++atomicModCount == LENGTHY_ATOMIC_EDIT_THRESHOLD) {
                Document doc = evt.getDocument();
                // Deactivate view hierarchy
                View rootView = getRootView(getComponent());
                View view;
                if (rootView != null && rootView.getViewCount() > 0 &&
                        (view = rootView.getView(0)) instanceof org.netbeans.modules.editor.lib2.view.DocumentView)
                {
                    ((org.netbeans.modules.editor.lib2.view.DocumentView)view).updateLengthyAtomicEdit(+1);
                }
                // Inactivate lexer's token hierarchy
                // Commented out due to #200270
//                MutableTextInput input = (MutableTextInput) doc.getProperty(MutableTextInput.class);
//                if (input != null) {
//                    input.tokenHierarchyControl().setActive(false);
//                }
            }
        }
    }

    @Override
    public void atomicLock(AtomicLockEvent evt) {
        assert (atomicModCount == -1);
        atomicModCount = 0;
    }

    @Override
    public void atomicUnlock(AtomicLockEvent evt) {
        if (atomicModCount != -1) {
            if (atomicModCount >= LENGTHY_ATOMIC_EDIT_THRESHOLD) {
                // Activate view hierarchy
                View rootView = getRootView(getComponent());
                View view;
                if (rootView != null && rootView.getViewCount() > 0 &&
                        (view = rootView.getView(0)) instanceof org.netbeans.modules.editor.lib2.view.DocumentView)
                {
                    ((org.netbeans.modules.editor.lib2.view.DocumentView)view).updateLengthyAtomicEdit(-1);
                }
                // Activate lexer's token hierarchy
                Document doc = getComponent().getDocument();
                MutableTextInput input = (MutableTextInput) doc.getProperty(MutableTextInput.class);
                if (input != null) {
                    input.tokenHierarchyControl().setActive(true);
                }
            }
            atomicModCount = -1;
        }
    }

    
    
    /** Creates a view for an element.
    *
    * @param elem the element
    * @return the newly created view or null
    */
    public @Override View create(Element elem) {
	    String kind = elem.getName();
    if (kind != null) {
		if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new LabelView(elem);
		} else if (kind.equals(AbstractDocument.ParagraphElementName)) {
//                    System.out.println("creating DrawEngineLineView for elem=" + elem);
		    return new DrawEngineLineView(elem);//.createFragment(elem.getStartOffset()+10,elem.getStartOffset()+30);
		} else if (kind.equals(AbstractDocument.SectionElementName)) {
//                   return new LockView(new EditorUIBoxView(elem, View.Y_AXIS));
//                    System.out.println("creating DrawEngineDocView for elem=" + elem);
//		    return new DrawEngineDocView(getComponent()); // EditorUIBoxView(elem, View.Y_AXIS);
		    return new LockView(new DrawEngineDocView(elem)); // EditorUIBoxView(elem, View.Y_AXIS);
		} else if (kind.equals(StyleConstants.ComponentElementName)) {
		    return new ComponentView(elem);
		} else if (kind.equals(StyleConstants.IconElementName)) {
		    return new IconView(elem);
		}
	    }
	
	    // default to text display
            return new DrawEngineLineView(elem);        
    }

    /** Creates a view for an element.
    * @param elem the element
    * @param p0 the starting offset >= 0
    * @param p1 the ending offset >= p0
    * @return the view
    */
    public @Override View create(Element elem, int p0, int p1) {
        return null;
    }

    /** Specifies that some preference has changed. */
    public void preferenceChanged(boolean width, boolean height) {
        modelChanged();
    }

    public void invalidateStartY() {
        // no longer available
    }

    protected void refresh(){
        if (getComponent().isShowing() && needsRefresh){
            modelChanged();
            needsRefresh = false;
        }
    }

    private static class GetFocusedComponentAction extends TextAction {

        private GetFocusedComponentAction() {
            super("get-focused-component"); // NOI18N
        }

        public void actionPerformed(ActionEvent evt) {
        }

        JTextComponent getFocusedComponent2() {
            return super.getFocusedComponent();
        }

    }
    
    static void uninstallUIWatcher(JTextComponent c) {
        UIWatcher uiWatcher = (UIWatcher)c.getClientProperty(UIWatcher.class);
        if (uiWatcher != null) {
            c.removePropertyChangeListener(uiWatcher);
            c.putClientProperty(UIWatcher.class, null);
        }
    }
    
    /** Class that returns back BaseTextUI after its change
     * by changing look-and-feel.
     */
    static class UIWatcher implements PropertyChangeListener {
        
        private Class uiClass;

        UIWatcher(Class uiClass) {
            this.uiClass = uiClass;
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Object newValue = evt.getNewValue();
            if ("UI".equals(evt.getPropertyName())) {
                LOG.log(Level.FINE, "UI property changed for text component {0}\nOldUI: {1}\nNewUI: {2}\n", new Object[] {
                    evt.getSource(), evt.getOldValue(), evt.getNewValue()
                });
                
                if ((newValue != null) && !(newValue instanceof BaseTextUI)) {
                    JTextComponent c = (JTextComponent)evt.getSource();
                    EditorKit kit = ((TextUI)newValue).getEditorKit(c);
                    if (kit instanceof BaseKit) {
                        // BaseKit but not BaseTextUI -> restore BaseTextUI
                        try {
                            BaseTextUI newUI = (BaseTextUI) uiClass.getDeclaredConstructor().newInstance();
                            c.setUI(newUI);
                            if (evt.getOldValue() instanceof BaseTextUI) {
                                BaseTextUI oldUI = (BaseTextUI) evt.getOldValue();
                                if (oldUI.getEditorUI().hasExtComponent()) {
                                    // Remove and re-parent the new ext component in place of original one.
                                    JComponent oldExtComponent = oldUI.getEditorUI().getExtComponent();
                                    Container parent = oldExtComponent.getParent();
                                    if (parent != null) {
                                        // According to CloneableEditor's code add as BorderLayout.CENTER
                                        parent.remove(oldExtComponent);
                                        parent.add(newUI.getEditorUI().getExtComponent());
                                    }
                                }
                            }

                        } catch (ReflectiveOperationException ignored) {}
                    }
                }
            }
        }
        
    }
    
}
