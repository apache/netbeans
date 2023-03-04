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


package org.netbeans.modules.java.navigation;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.html.HTMLDocument;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ui.ElementJavadoc;

import org.netbeans.editor.*;

import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 *  @author  Martin Roskanin, Dusan Balek
 */
public class DocumentationScrollPane extends JScrollPane {

    private static final String BACK = "org/netbeans/modules/java/navigation/resources/back.png"; //NOI18N
    private static final String FORWARD = "org/netbeans/modules/java/navigation/resources/forward.png"; //NOI18N
    private static final String GOTO_SOURCE = "org/netbeans/modules/java/navigation/resources/open_source_in_editor.png"; //NOI18N
    private static final String SHOW_WEB = "org/netbeans/modules/java/navigation/resources/open_in_external_browser.png"; //NOI18N

    private static final String JAVADOC_BACK = "javadoc-back"; //NOI18N
    private static final String JAVADOC_FORWARD = "javadoc-forward"; //NOI18N    
    private static final String JAVADOC_OPEN_IN_BROWSER = "javadoc-open-in-browser"; //NOI18N    
    private static final String JAVADOC_OPEN_SOURCE = "javadoc-open-source"; //NOI18N    
    
    private static final int ACTION_JAVADOC_ESCAPE = 0;
    private static final int ACTION_JAVADOC_BACK = 1;
    private static final int ACTION_JAVADOC_FORWARD = 2;
    private static final int ACTION_JAVADOC_OPEN_IN_BROWSER = 3;
    private static final int ACTION_JAVADOC_OPEN_SOURCE = 4;

    private static final RequestProcessor RP = new RequestProcessor(DocumentationScrollPane.class);

    private JButton bBack, bForward, bGoToSource, bShowWeb;    
    private HTMLDocView view;
    
    // doc browser history
    private List<ElementJavadoc> history = new ArrayList<ElementJavadoc>(5);
    private int currentHistoryIndex = -1;
    //@GuardedBy("this")
    protected ElementJavadoc currentDocumentation = null;
    private Reference<FileObject> currentFile;
    //@GuardedBy("this")
    private RequestProcessor.Task task;    
    
    /** Creates a new instance of ScrollJavaDocPane */
    public DocumentationScrollPane( boolean keepDefaultBorder ) {
        super();
 
        // Add the completion doc view
        //XXX fix bg color
        view = new HTMLDocView( getDefaultBackground() );
        view.addHyperlinkListener(new HyperlinkAction());
        setViewportView(view);
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(DocumentationScrollPane.class, "ACSN_DocScrollPane"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DocumentationScrollPane.class, "ACSD_DocScrollPane"));
        view.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DocumentationScrollPane.class, "ACSN_DocScrollPane"));
        view.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DocumentationScrollPane.class, "ACSD_DocScrollPane"));
        installTitleComponent();
//        installKeybindings(view);
        setFocusable(true);
        
        if( !keepDefaultBorder )
            setBorder( BorderFactory.createEmptyBorder() );
    }
    
    void setData(
        final FileObject owner,
        final ElementJavadoc doc) {
        assert SwingUtilities.isEventDispatchThread();
        currentFile = owner == null ? null : new WeakReference<FileObject>(owner);
        setData(doc, true);
    }

    void clearContent(final FileObject owner) {
        assert SwingUtilities.isEventDispatchThread();
        if (owner != null && currentFile != null && owner.equals(currentFile.get())) {
            setData(null, true);
        }
    }
    
    private void setData(ElementJavadoc doc, boolean clearHistory) {
        if ( doc == null || clearHistory ) {
            history = new ArrayList<ElementJavadoc>(5);
        }
        setDocumentation(doc);
        if( null != doc )
            addToHistory(doc);
    }
    
    private ImageIcon resolveIcon(String res){
        return ImageUtilities.loadImageIcon(res, false);
    }

    private void installTitleComponent() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);        
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("controlDkShadow"))); //NOI18N
        toolbar.setLayout(new GridBagLayout());

        GridBagConstraints gdc = new GridBagConstraints();
        gdc.gridx = 0;
        gdc.gridy = 0;
        gdc.anchor = GridBagConstraints.WEST;        
        ImageIcon icon = resolveIcon(BACK);
        if (icon != null) {            
            bBack = new BrowserButton(icon);
            bBack.addMouseListener(new MouseEventListener(bBack));
            bBack.addActionListener(new DocPaneAction(ACTION_JAVADOC_BACK));
            bBack.setEnabled(false);
            //bBack.setFocusable(false);
            bBack.setContentAreaFilled(false);
            bBack.setMargin(new Insets(0, 0, 0, 0));
            bBack.setToolTipText(NbBundle.getMessage(DocumentationScrollPane.class, "HINT_doc_browser_back_button")); //NOI18N
            toolbar.add(bBack, gdc);
        }
        
        gdc.gridx = 1;
        gdc.gridy = 0;
        gdc.anchor = GridBagConstraints.WEST;        
        icon = resolveIcon(FORWARD);
        if (icon != null) {
            bForward = new BrowserButton(icon);
            bForward.addMouseListener(new MouseEventListener(bForward));
            bForward.addActionListener(new DocPaneAction(ACTION_JAVADOC_FORWARD));
            bForward.setEnabled(false);
            //bForward.setFocusable(false);
            bForward.setContentAreaFilled(false);
            bForward.setToolTipText(NbBundle.getMessage(DocumentationScrollPane.class, "HINT_doc_browser_forward_button")); //NOI18N
            bForward.setMargin(new Insets(0, 0, 0, 0));
            toolbar.add(bForward, gdc);
        }
        
        gdc.gridx = 2;
        gdc.gridy = 0;
        gdc.anchor = GridBagConstraints.WEST;        
        icon = resolveIcon(SHOW_WEB);
        if (icon != null) {            
            bShowWeb = new BrowserButton(icon);
            bShowWeb.addMouseListener(new MouseEventListener(bShowWeb));
            bShowWeb.addActionListener(new DocPaneAction(ACTION_JAVADOC_OPEN_IN_BROWSER));
            bShowWeb.setEnabled(false);
            //bShowWeb.setFocusable(false);
            bShowWeb.setContentAreaFilled(false);
            bShowWeb.setMargin(new Insets(0, 0, 0, 0));
            bShowWeb.setToolTipText(NbBundle.getMessage(DocumentationScrollPane.class, "HINT_doc_browser_show_web_button")); //NOI18N
            toolbar.add(bShowWeb, gdc);
        }
        
        gdc.gridx = 3;
        gdc.gridy = 0;
        gdc.weightx = 1.0;
        gdc.anchor = GridBagConstraints.WEST;                
        icon = resolveIcon(GOTO_SOURCE);
        if (icon != null) {
            bGoToSource = new BrowserButton(icon);
            bGoToSource.addMouseListener(new MouseEventListener(bGoToSource));
            bGoToSource.addActionListener(new DocPaneAction(ACTION_JAVADOC_OPEN_SOURCE));
            bGoToSource.setEnabled(false);
            //bGoToSource.setFocusable(false);
            bGoToSource.setContentAreaFilled(false);
            bGoToSource.setMargin(new Insets(0, 0, 0, 0));
            bGoToSource.setToolTipText(NbBundle.getMessage(DocumentationScrollPane.class, "HINT_doc_browser_goto_source_button")); //NOI18N
            toolbar.add(bGoToSource, gdc);
        }
        setColumnHeaderView(toolbar);
        installKeybindings(view);
    }
    
    private synchronized void setDocumentation(final ElementJavadoc doc) {
        currentDocumentation = doc;
        if( null != doc ) {
            if (task != null) {
                task.cancel();
            }
            view.setContent(NbBundle.getMessage(DocumentationScrollPane.class, "TXT_LoadingJavadoc"), null);
            task = RP.create(new Runnable(){
                @Override
                public void run() {
                    
                    final ElementJavadoc ed;
                    synchronized (DocumentationScrollPane.this) {
                        if (currentDocumentation != doc) {
                            return;
                        }                        
                        ed = currentDocumentation;
                    }
                    final String text = ed.getText();
                    final URL url = ed.getURL();
                    final boolean gtsEnabled = ed.getGotoSourceAction() != null;
                    synchronized (DocumentationScrollPane.this) {
                        if (currentDocumentation != doc) {
                            //Changed, do nothing.
                            return;
                        }                        
                    }
                    if (text != null){
                        SwingUtilities.invokeLater(new Runnable(){
                            @Override
                            public void run() {
                                Document document = view.getDocument();
                                document.putProperty(Document.StreamDescriptionProperty, null);
                                if (url!=null){
                                    // fix of issue #58658
                                    if (document instanceof HTMLDocument){
                                        ((HTMLDocument)document).setBase(url);
                                    }
                                }
                                view.setContent(text, null);
                                bShowWeb.setEnabled(url != null);
                                bGoToSource.setEnabled(gtsEnabled);
                            }
                        });
                    } else if (url != null){
                        SwingUtilities.invokeLater(new Runnable(){
                            @Override
                            public void run() {
                                try{
                                    view.setPage(url);
                                    bShowWeb.setEnabled(url != null);
                                    bGoToSource.setEnabled(gtsEnabled);
                                }catch(IOException ioe){
                                    StatusDisplayer.getDefault().setStatusText(ioe.toString());
                                }
                            }
                        });
                    } else {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                bShowWeb.setEnabled(false);
                                bGoToSource.setEnabled(false);
                            }
                        });
                    }
                }
            });
            task.schedule(0);
        } else {
            bShowWeb.setEnabled( false );
            bGoToSource.setEnabled( false );
            view.setContent("", null);//NOI18N
        }
    }
    
    private synchronized void addToHistory(ElementJavadoc doc) {
        int histSize = history.size();
        for (int i = currentHistoryIndex + 1; i < histSize; i++){
            history.remove(history.size() - 1);
        }
        history.add(doc);
        currentHistoryIndex = history.size() - 1;
        if (currentHistoryIndex > 0)
            bBack.setEnabled(true);
        bForward.setEnabled(false);
    }
    
    private synchronized void backHistory() {
        if (currentHistoryIndex > 0 && currentHistoryIndex <= history.size()) {
            currentHistoryIndex--;
            setDocumentation(history.get(currentHistoryIndex));            
            if (currentHistoryIndex == 0)
                bBack.setEnabled(false);
            bForward.setEnabled(true);
        }
    }
    
    private synchronized void forwardHistory(){
        if (currentHistoryIndex <history.size()-1){
            currentHistoryIndex++;
            setDocumentation(history.get(currentHistoryIndex));
            if (currentHistoryIndex == history.size() - 1)
                bForward.setEnabled(false); 
            bBack.setEnabled(true);
        }
    }
    
    synchronized void clearHistory(){
        currentHistoryIndex = -1;
        history.clear();
        bBack.setEnabled(false);
        bForward.setEnabled(false);
    }

    private void openInExternalBrowser() {
        final ElementJavadoc cd;
        synchronized (this) {
            cd = currentDocumentation;
        }
        if (cd != null) {
            URL url = cd.getURL();
            if (url != null) {
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
            }
        }
    }

    private void goToSource() {
        final ElementJavadoc cd;
        synchronized (this) {
            cd = currentDocumentation;
        }
        if (cd != null) {
            final Action action = cd.getGotoSourceAction();
            if (action != null) {
                action.actionPerformed(new ActionEvent(cd, 0, null));
            }
        }
    }

    /** Attempt to find the editor keystroke for the given editor action. */
    private KeyStroke[] findEditorKeys(String editorActionName, KeyStroke defaultKey, JTextComponent component) {
        // This method is implemented due to the issue
        // #25715 - Attempt to search keymap for the keybinding that logically corresponds to the action
        KeyStroke[] ret = new KeyStroke[] { defaultKey };
        if (component != null) {
            TextUI componentUI = component.getUI();
            Keymap km = component.getKeymap();
            if (componentUI != null && km != null) {
                EditorKit kit = componentUI.getEditorKit(component);
                if (kit instanceof BaseKit) {
                    Action a = ((BaseKit)kit).getActionByName(editorActionName);
                    if (a != null) {
                        KeyStroke[] keys = km.getKeyStrokesForAction(a);
                        if (keys != null && keys.length > 0) {
                            ret = keys;
                        }
                    }
                }
            }
        }
        return ret;
    }

    private void registerKeybinding(int action, String actionName, KeyStroke stroke, String editorActionName, JTextComponent component){
        KeyStroke[] keys = findEditorKeys(editorActionName, stroke, component);
        for (int i = 0; i < keys.length; i++) {
            view.getInputMap().put(keys[i], actionName);
        }
        view.getActionMap().put(actionName, new DocPaneAction(action));
    }
    
    private void installKeybindings(JTextComponent component) {
        // Register javadoc back key
        registerKeybinding(ACTION_JAVADOC_BACK, JAVADOC_BACK,
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_MASK),
        null, component);

        // Register javadoc forward key
        registerKeybinding(ACTION_JAVADOC_FORWARD, JAVADOC_FORWARD,
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_MASK),
        null, component);

        // Register open in external browser key
        registerKeybinding(ACTION_JAVADOC_OPEN_IN_BROWSER, JAVADOC_OPEN_IN_BROWSER,
        KeyStroke.getKeyStroke(KeyEvent.VK_F1, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK),
        null, component);

        // Register open the source in editor key
        registerKeybinding(ACTION_JAVADOC_OPEN_SOURCE, JAVADOC_OPEN_SOURCE,
        KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.ALT_MASK | KeyEvent.CTRL_MASK),
        null, component);
        
        // Register movement keystrokes to be reachable through Shift+<orig-keystroke>
        mapWithShift(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
        mapWithShift(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
        mapWithShift(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
        mapWithShift(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
        mapWithShift(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, KeyEvent.CTRL_MASK));
        mapWithShift(KeyStroke.getKeyStroke(KeyEvent.VK_END, KeyEvent.CTRL_MASK));
        mapWithShift(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
        mapWithShift(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
    }        
    
    private void mapWithShift(KeyStroke key) {
        InputMap inputMap = view.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        Object actionKey = inputMap.get(key);
        if (actionKey != null) {
            key = KeyStroke.getKeyStroke(key.getKeyCode(), key.getModifiers() | InputEvent.SHIFT_MASK);
            view.getInputMap().put(key, actionKey);
        }
    }
    
    private Color getDefaultBackground() {
        Color bgColor = new JEditorPane().getBackground();
        bgColor = new Color(
                Math.max(bgColor.getRed() - 8, 0 ), 
                Math.max(bgColor.getGreen() - 8, 0 ), 
                bgColor.getBlue());
        
        return bgColor;
    }
    
    private class BrowserButton extends JButton {
        public BrowserButton() {
            setBorderPainted(false);
            //setFocusPainted(false);
        }
        
        public BrowserButton(String text){
            super(text);
            setBorderPainted(false);
            //setFocusPainted(false);
        }
        
        public BrowserButton(Icon icon){
            super(icon);
            setBorderPainted(false);
            //setFocusPainted(false);
        }

        public @Override void setEnabled(boolean b) {
            super.setEnabled(b);
        }
        
        
    }

    private class MouseEventListener extends MouseAdapter {        
        private JButton button;
        
        MouseEventListener(JButton button) {
            this.button = button;
        }
        
        public @Override void mouseEntered(MouseEvent ev) {
            if (button.isEnabled()){
                button.setContentAreaFilled(true);
                button.setBorderPainted(true);
            }
        }
        public @Override void mouseExited(MouseEvent ev) {
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
        }          
    }

    private class HyperlinkAction implements HyperlinkListener {
        
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e != null && HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                final String desc = e.getDescription();
                if (desc != null) {
                    RP.post(new Runnable() {
                        public @Override void run() {
                            final ElementJavadoc cd;
                            synchronized (DocumentationScrollPane.this) {
                                cd = currentDocumentation;
                            }
                            if (cd != null) {
                                final ElementJavadoc doc = cd.resolveLink(desc);
                                if (doc != null) {
                                    EventQueue.invokeLater(new Runnable() {
                                        public @Override void run() {
                                            setData(doc, false);
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    private class DocPaneAction extends AbstractAction {
        private int action;
        
        private DocPaneAction(int action) {
            this.action = action;
        }
        
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
            switch (action) {
                case ACTION_JAVADOC_BACK:
                    backHistory();
                    break;
                case ACTION_JAVADOC_FORWARD:
                    forwardHistory();
                    break;
                case ACTION_JAVADOC_OPEN_IN_BROWSER:
                    openInExternalBrowser();
                    break;
                case ACTION_JAVADOC_OPEN_SOURCE:
                    goToSource();
                    break;
            }
        }
    }
}
