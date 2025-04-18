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


package org.netbeans.modules.editor.completion;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.spi.editor.completion.CompletionDocumentation;

import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 *  @author  Martin Roskanin, Dusan Balek
 */
public class DocumentationScrollPane extends JScrollPane {
    private static final String TEXT_ZOOM_PROPERTY = "text-zoom"; // Defined in DocumentView in editor.lib2

    private static final String BACK = "org/netbeans/modules/editor/completion/resources/back.png"; //NOI18N
    private static final String FORWARD = "org/netbeans/modules/editor/completion/resources/forward.png"; //NOI18N
    private static final String GOTO_SOURCE = "org/netbeans/modules/editor/completion/resources/open_source_in_editor.png"; //NOI18N
    private static final String SHOW_WEB = "org/netbeans/modules/editor/completion/resources/open_in_external_browser.png"; //NOI18N
    
    private static final String JAVADOC_ESCAPE = "javadoc-escape"; //NOI18N
    private static final String JAVADOC_BACK = "javadoc-back"; //NOI18N
    private static final String JAVADOC_FORWARD = "javadoc-forward"; //NOI18N    
    private static final String JAVADOC_OPEN_IN_BROWSER = "javadoc-open-in-browser"; //NOI18N    
    private static final String JAVADOC_OPEN_SOURCE = "javadoc-open-source"; //NOI18N    
    private static final String COPY_TO_CLIPBOARD = "copy-to-clipboard";
    
    private static final int ACTION_JAVADOC_ESCAPE = 0;
    private static final int ACTION_JAVADOC_BACK = 1;
    private static final int ACTION_JAVADOC_FORWARD = 2;
    private static final int ACTION_JAVADOC_OPEN_IN_BROWSER = 3;
    private static final int ACTION_JAVADOC_OPEN_SOURCE = 4;
    private static final int ACTION_JAVADOC_COPY = 5;

    private static final RequestProcessor RP = new RequestProcessor(DocumentationScrollPane.class);

    private JButton bBack, bForward, bGoToSource, bShowWeb;    
    private HTMLDocView view;
    
    // doc browser history
    private List<CompletionDocumentation> history = new ArrayList<CompletionDocumentation>(5);
    private int currentHistoryIndex = -1;
    protected CompletionDocumentation currentDocumentation = null;
    
    private Dimension documentationPreferredSize;
    private final JTextComponent editorComponent;

    /** Creates a new instance of ScrollJavaDocPane */
    public DocumentationScrollPane(JTextComponent editorComponent) {
        super();
 
        // Determine and use fixed preferred size
        documentationPreferredSize = CompletionSettings.getInstance(editorComponent).documentationPopupPreferredSize();
        setPreferredSize(null); // Use the documentationPopupPreferredSize
        
        Color bgColor = new JEditorPane().getBackground();
        bgColor = new Color(
                Math.max(bgColor.getRed() - 8, 0 ), 
                Math.max(bgColor.getGreen() - 8, 0 ), 
                bgColor.getBlue());
        
        // Add the completion doc view
        view = new HTMLDocView(bgColor);
        Integer textZoom = (Integer) editorComponent.getClientProperty(TEXT_ZOOM_PROPERTY);
        // Use the same logic as in o.n.editor.GlyphGutter.update().
        if (textZoom != null && textZoom != 0) {
            Font font = view.getFont();
            if (Math.max(font.getSize() + textZoom, 2) == 2) {
                textZoom = -(font.getSize() - 2);
            }
            view.setFont(new Font(font.getFamily(), font.getStyle(),
                    font.getSize() + textZoom));
        }
        view.addHyperlinkListener(new HyperlinkAction());
        setViewportView(view);

        installTitleComponent();
        installKeybindings(editorComponent);
        this.editorComponent = editorComponent;
        setFocusable(true);
    }
    
    public @Override void setPreferredSize(Dimension preferredSize) {
        if (preferredSize == null) {
            preferredSize = documentationPreferredSize;
        }
        super.setPreferredSize(preferredSize);
    }
    
    
    public void setData(CompletionDocumentation doc) {
        setDocumentation(doc);
        if (doc != null && doc != CompletionImpl.PLEASE_WAIT_DOC) {
            addToHistory(doc);
        }
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
            // For HiDPI support.
            bBack.setDisabledIcon(ImageUtilities.createDisabledIcon(icon));
            bBack.addMouseListener(new MouseEventListener(bBack));
            bBack.setEnabled(false);
            bBack.setFocusable(false);
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
            bForward.setDisabledIcon(ImageUtilities.createDisabledIcon(icon));
            bForward.addMouseListener(new MouseEventListener(bForward));
            bForward.setEnabled(false);
            bForward.setFocusable(false);
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
            bShowWeb.setDisabledIcon(ImageUtilities.createDisabledIcon(icon));
            bShowWeb.addMouseListener(new MouseEventListener(bShowWeb));
            bShowWeb.setEnabled(false);
            bShowWeb.setFocusable(false);
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
            bGoToSource.setDisabledIcon(ImageUtilities.createDisabledIcon(icon));
            bGoToSource.addMouseListener(new MouseEventListener(bGoToSource));
            bGoToSource.setEnabled(false);
            bGoToSource.setFocusable(false);
            bGoToSource.setContentAreaFilled(false);
            bGoToSource.setMargin(new Insets(0, 0, 0, 0));
            bGoToSource.setToolTipText(NbBundle.getMessage(DocumentationScrollPane.class, "HINT_doc_browser_goto_source_button")); //NOI18N
            toolbar.add(bGoToSource, gdc);
        }
        setColumnHeaderView(toolbar);
    }
    
    private synchronized void setDocumentation(CompletionDocumentation doc) {
        currentDocumentation = doc;
        if (currentDocumentation != null) {
            String text = currentDocumentation.getText();
            URL url = currentDocumentation.getURL();
            if (text != null){
                Document document = view.getDocument();
                document.putProperty(Document.StreamDescriptionProperty, null);
                if (url!=null){
                    // fix of issue #58658
                    if (document instanceof HTMLDocument){
                        ((HTMLDocument)document).setBase(url);
                    }
                }
                view.setContent(text, url != null ? url.getRef() : null);
            } else if (url != null){
                try{
                    view.setPage(url);
                }catch(IOException ioe){
                    StatusDisplayer.getDefault().setStatusText(ioe.toString());
                }
            }
            bShowWeb.setEnabled(url != null);
            bGoToSource.setEnabled(currentDocumentation.getGotoSourceAction() != null);
        }
    }
    
    private synchronized void addToHistory(CompletionDocumentation doc) {
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
        if (currentHistoryIndex > 0) {
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

    private void openInExternalBrowser(){
        CompletionDocumentation cd = currentDocumentation;
        if (cd != null) {
            URL url = cd.getURL();
            if (url != null)
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        }
    }
    
    private void goToSource() {
        CompletionDocumentation cd = currentDocumentation;
        if (cd != null) {
            Action action = cd.getGotoSourceAction();
            if (action != null)
                action.actionPerformed(new ActionEvent(cd, 0, null));
        }
    }
    
    private void copy() {
        Caret caret = view.getCaret();
        if (caret.getDot() != caret.getMark()) {
            view.copy();
        } else {
            editorComponent.copy();
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
            getInputMap().put(keys[i], actionName);
        }
        getActionMap().put(actionName, new DocPaneAction(action));
    }
    
    private void installKeybindings(JTextComponent component) {
	// Register Escape key
        registerKeybinding(ACTION_JAVADOC_ESCAPE, JAVADOC_ESCAPE,
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        ExtKit.escapeAction, component);

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
        
        //register copy action
        registerKeybinding(ACTION_JAVADOC_COPY, COPY_TO_CLIPBOARD,
        KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK),
        COPY_TO_CLIPBOARD, component);
        
        // Register movement keystrokes to be reachable through Ctrl+<orig-keystroke>
        mapWithCtrl(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
        mapWithCtrl(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
        mapWithCtrl(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
        mapWithCtrl(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
        mapWithCtrl(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
        mapWithCtrl(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
    }        
    
    private void mapWithCtrl(KeyStroke key) {
        InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        Object actionKey = inputMap.get(key);
        if (actionKey != null) {
            key = KeyStroke.getKeyStroke(key.getKeyCode(), key.getModifiers() | InputEvent.CTRL_MASK);
            getInputMap().put(key, actionKey);
        }
    }
    
    private class BrowserButton extends JButton {
        public BrowserButton() {
            setBorderPainted(false);
            setFocusPainted(false);
        }
        
        public BrowserButton(String text){
            super(text);
            setBorderPainted(false);
            setFocusPainted(false);
        }
        
        public BrowserButton(Icon icon){
            super(icon);
            setBorderPainted(false);
            setFocusPainted(false);
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
        
        public @Override void mouseClicked(MouseEvent evt) {
            if (button.equals(bBack)){
                backHistory();
            }else if(button.equals(bForward)){
                forwardHistory();
            }else if(button.equals(bGoToSource)){
                goToSource();
            }else if (button.equals(bShowWeb)){
                openInExternalBrowser();
            }
        }
    }

    private class HyperlinkAction implements HyperlinkListener {
        
        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e != null && e.getEventType() == HyperlinkEvent.EventType.ACTIVATED && e.getDescription().startsWith("copy.snippet")) {
                Clipboard systemClipboard = Lookup.getDefault().lookup(Clipboard.class);
                if (systemClipboard == null) {
                    systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                }
                String snippetCount = e.getDescription().replaceAll("[^0-9]", "");
                HTMLDocument HtmlDoc = (HTMLDocument) e.getSourceElement().getDocument();
                HTMLDocView source = (HTMLDocView) e.getSource();
                source.getText();
                Element snippetElement = HtmlDoc.getElement("snippet" + snippetCount);
                StringBuilder text = new StringBuilder();
                getTextToCopy(HtmlDoc, text, snippetElement);
                handleTextToCopy(HtmlDoc, snippetElement, text);
                systemClipboard.setContents(new StringSelection(text.toString()), null);
                view.getHighlighter().removeAllHighlights();
                try {
                    view.getHighlighter().addHighlight(e.getSourceElement().getStartOffset(), e.getSourceElement().getEndOffset(), new DefaultHighlightPainter(Color.GRAY));
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else if (e != null && HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                final String desc = e.getDescription();
                if (desc != null) {
                    RP.post(new Runnable() {
                        public @Override void run() {
                            CompletionDocumentation cd = currentDocumentation;
                            if (cd != null) {
                                final CompletionDocumentation doc = cd.resolveLink(desc);
                                if (doc != null) {
                                    EventQueue.invokeLater(new Runnable() {
                                        public @Override void run() {
                                            setData(doc);
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }
        }

        private void handleTextToCopy(HTMLDocument HtmlDoc, Element snippetElement, StringBuilder text) {
            HTMLDocument.Iterator it = HtmlDoc.getIterator(HTML.Tag.A);
            int startOffset = snippetElement.getStartOffset();
            int endOffset = snippetElement.getEndOffset();
            int count = 0;
            while (it.isValid()) {
                int startOffset1 = it.getStartOffset();
                int endOffset1 = it.getEndOffset();
                try {
                    String aTagText = HtmlDoc.getText(startOffset1, endOffset1 - startOffset1);
                    if (startOffset1 > startOffset && endOffset1 < endOffset
                            && aTagText.length() == 1 && Character.isWhitespace(aTagText.charAt(0))) {
                        text.deleteCharAt(startOffset1 - startOffset - count);
                        count++;
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                it.next();
            }
            text.delete(0, text.indexOf("\n") + 1); //removing first line of div i.e. "Copy"  
        }

        private void getTextToCopy(HTMLDocument HtmlDoc, StringBuilder sb, Element snippetElement) {
            int elementCount = snippetElement.getElementCount();
            if (elementCount == 0) {
                int startOffset = snippetElement.getStartOffset();
                int endOffset = snippetElement.getEndOffset();
                try {
                    sb.append(HtmlDoc.getText(startOffset, endOffset - startOffset));
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return;
            }
            for (int i = 0; i < elementCount; i++) {
                Element element = snippetElement.getElement(i);
                getTextToCopy(HtmlDoc, sb, element);
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
		case ACTION_JAVADOC_ESCAPE:
		    CompletionImpl.get().hideDocumentation(false);
		    break;
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
                case ACTION_JAVADOC_COPY:
                    copy();
                    break;
            }
            
        }
    }
}