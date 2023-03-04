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

package org.netbeans.lib.editor.bookmarks.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.bookmarks.api.Bookmark;
import org.netbeans.lib.editor.bookmarks.api.BookmarkList;
import org.openide.awt.Actions;
import org.openide.cookies.EditorCookie;
import org.openide.text.NbDocument;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;


/**
 * Toggles a bookmark in a line in an opened document.
 *
 * @author Vita Stejskal
 */
public final class ToggleBookmarkAction extends AbstractAction implements ContextAwareAction, Presenter.Toolbar {

    private static final String ACTION_NAME = "bookmark-toggle"; // NOI18N
    private static final String ACTION_ICON = "org/netbeans/modules/editor/bookmarks/resources/toggle_bookmark.png"; // NOI18N
        
    private final JTextComponent component;
    
    public ToggleBookmarkAction() {
        this(null);
    }

    public ToggleBookmarkAction(JTextComponent component) {
        super(
            NbBundle.getMessage(ToggleBookmarkAction.class, ACTION_NAME),ImageUtilities.loadImageIcon(ACTION_ICON, false));
        putValue(SHORT_DESCRIPTION, Actions.cutAmpersand((String) getValue(NAME)));
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        
        this.component = component;
        updateEnabled();
        PropertyChangeListener editorRegistryListener = new EditorRegistryListener(this);
        EditorRegistry.addPropertyChangeListener(editorRegistryListener);
    }

    private void updateEnabled() {
        setEnabled(isEnabled());
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        JTextComponent jtc = findComponent(actionContext);
        ToggleBookmarkAction toggleBookmarkAction = new ToggleBookmarkAction(jtc);
        toggleBookmarkAction.putValue(ACCELERATOR_KEY, this.getValue(ACCELERATOR_KEY));
        return toggleBookmarkAction;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (component != null) {
            // cloned action with context
            actionPerformed(component);
        } else {
            // global action, will have to find the current component
            JTextComponent jtc = findComponent(Utilities.actionsGlobalContext());
            if (jtc != null) {
                actionPerformed(jtc);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        if (component != null) {
            return true;
        } else {
            if (EditorRegistry.lastFocusedComponent() == null) {
                return false;
            } else {
                return true;
            }
        }
    }

    @Override
    public Component getToolbarPresenter() {
        AbstractButton b;
        
        if (component != null) {
            b = new MyGaGaButton();
            b.setModel(new BookmarkButtonModel(component));
        } else {
            b = new JButton();
        }
        
        b.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
        b.setAction(this);
        
        return b;
    }

    public static JTextComponent findComponent(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        return ec == null ? null : NbDocument.findRecentEditorPane(ec);
    }
    
    private static void actionPerformed(JTextComponent target) {
        if (target != null) {
            if (org.netbeans.editor.Utilities.getEditorUI(target).isGlyphGutterVisible()) {
                Caret caret = target.getCaret();
                BookmarkList bookmarkList = BookmarkList.get(target.getDocument());
                bookmarkList.toggleLineBookmark(caret.getDot());

            } else { // Glyph gutter not visible -> just beep
                target.getToolkit().beep();
            }
        }
    }
    
    private static final class BookmarkButtonModel extends JToggleButton.ToggleButtonModel implements PropertyChangeListener, ChangeListener {
        
        private final JTextComponent component;
        private Caret caret;
        private BookmarkList bookmarks;
        private int lastCurrentLineStartOffset = -1;
        
        private PropertyChangeListener bookmarksListener = null;
        private ChangeListener caretListener = null;
        
        @SuppressWarnings("LeakingThisInConstructor")
        public BookmarkButtonModel(JTextComponent component) {
            this.component = component;
            this.component.addPropertyChangeListener(WeakListeners.propertyChange(this, this.component));
            rebuild();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == null || 
                "document".equals(evt.getPropertyName()) || //NOI18N
                "caret".equals(evt.getPropertyName()) //NOI18N
            ) {
                rebuild();
            } else if ("bookmarks".equals(evt.getPropertyName())) { //NOI18N
                lastCurrentLineStartOffset = -1;
                updateState();
            }
        }

        @Override
        public void stateChanged(ChangeEvent evt) {
            updateState();
        }

        private static boolean isBookmarkOnTheLine(BookmarkList bookmarks, int lineStartOffset) {
            Bookmark bm = bookmarks.getNextBookmark(lineStartOffset - 1, false);
//            System.out.println("offset: " + lineStartOffset + " -> " + bm + (bm == null ? "" : "; bm.getOffset() = " + bm.getOffset()));
            return bm == null ? false : lineStartOffset == bm.getOffset();
        }
        
        private void rebuild() {
            // Hookup the bookmark list
            BookmarkList newBookmarks = BookmarkList.get(component.getDocument());
            if (newBookmarks != bookmarks) {
                if (bookmarksListener != null) {
                    bookmarks.removePropertyChangeListener (bookmarksListener);
                    bookmarksListener = null;
                }

                bookmarks = newBookmarks;

                if (bookmarks != null) {
                    bookmarksListener = WeakListeners.propertyChange(this, bookmarks);
                    bookmarks.addPropertyChangeListener (bookmarksListener);
                }
            }
            
            // Hookup the caret
            Caret newCaret = component.getCaret();
            if (newCaret != caret) {
                if (caretListener != null) {
                    caret.removeChangeListener(caretListener);
                    caretListener = null;
                }

                caret = newCaret;

                if (caret != null) {
                    caretListener = WeakListeners.change(this, caret);
                    caret.addChangeListener(caretListener);
                }
            }
            
            lastCurrentLineStartOffset = -1;
            updateState();
        }
        
        private void updateState() {
            Document doc = component.getDocument();
            if (caret != null && bookmarks != null && doc instanceof BaseDocument) {
                try {
                    int currentLineStartOffset = org.netbeans.editor.Utilities.getRowStart((BaseDocument) doc, caret.getDot());
                    if (currentLineStartOffset != lastCurrentLineStartOffset) {
                        lastCurrentLineStartOffset = currentLineStartOffset;
                        boolean selected = isBookmarkOnTheLine(bookmarks, currentLineStartOffset);
                        
//                        System.out.println("updateState: offset=" + currentLineStartOffset + ", hasBookmark=" + selected);
                        
                        setSelected(selected);
                    }
                } catch (BadLocationException e) {
                    // ignore
                    lastCurrentLineStartOffset = -1;
                }
            }
        }
    } // End of BookmarkButtonModel class
    
    private static final class MyGaGaButton extends JToggleButton implements ChangeListener {

        public MyGaGaButton() {

        }

        @Override
        public void setModel(ButtonModel model) {
            ButtonModel oldModel = getModel();
            if (oldModel != null) {
                oldModel.removeChangeListener(this);
            }

            super.setModel(model);

            ButtonModel newModel = getModel();
            if (newModel != null) {
                newModel.addChangeListener(this);
            }

            stateChanged(null);
        }

        @Override
        public void stateChanged(ChangeEvent evt) {
            boolean selected = isSelected();
            super.setContentAreaFilled(selected);
            super.setBorderPainted(selected);
        }

        @Override
        public void setBorderPainted(boolean arg0) {
            if (!isSelected()) {
                super.setBorderPainted(arg0);
            }
        }

        @Override
        public void setContentAreaFilled(boolean arg0) {
            if (!isSelected()) {
                super.setContentAreaFilled(arg0);
            }
        }
    } // End of MyGaGaButton class

    private static final class EditorRegistryListener implements PropertyChangeListener {
        
        private final Reference<ToggleBookmarkAction> actionRef;
        
        EditorRegistryListener(ToggleBookmarkAction action) {
            actionRef = new WeakReference<ToggleBookmarkAction>(action);
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            ToggleBookmarkAction action = actionRef.get();
            if (action != null) {
                action.updateEnabled();
            } else {
                EditorRegistry.removePropertyChangeListener(this); // EditorRegistry fires frequently so remove this way
            }
        }

    }

}

