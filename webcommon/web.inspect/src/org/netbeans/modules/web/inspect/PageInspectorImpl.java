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
package org.netbeans.modules.web.inspect;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.netbeans.modules.web.browser.spi.MessageDispatcher;
import org.netbeans.modules.web.browser.spi.MessageDispatcher.MessageListener;
import org.netbeans.modules.web.browser.spi.PageInspectionHandle;
import org.netbeans.modules.web.browser.spi.PageInspectorCustomizer;
import org.netbeans.modules.web.inspect.actions.Resource;
import org.netbeans.modules.web.inspect.ui.DomTCController;
import org.netbeans.modules.web.inspect.webkit.WebKitPageModel;
import org.netbeans.modules.web.inspect.webkit.knockout.KnockoutTCController;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the {@code PageInspector}.
 *
 * @author Jan Stola
 */
@ServiceProvider(service=PageInspector.class)
public class PageInspectorImpl extends PageInspector {
    /** Name of the toolbar component responsible for selection mode switching. */
    private static final String SELECTION_MODE_COMPONENT_NAME = "selectionModeSwitch"; // NOI18N
    /** Request processor for this class. */
    static final RequestProcessor RP = new RequestProcessor(PageInspectorImpl.class.getName());
    /** Property change support. */
    private final PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);
    /** Current inspected page. */
    private PageModel pageModel;
    /** Message listener for the inspected page. */
    private MessageListener messageListener;
    /** Message dispatcher for the inspected page. */
    private MessageDispatcher messageDispatcher;
    /** Lock guarding access to modifiable fields. */
    private final Object LOCK = new Object();
    /** Lock guarding access to {@code pageModel} field. */
    private final Object PAGE_MODEL_LOCK = new Object();
    /** Page inspector customizer for the inspected page. */
    private PageInspectorCustomizer pageInspectorCustomizer;
    /** Listener for a page inspector customizer. */
    private PropertyChangeListener pageInspectorCustomizerListener;

    /**
     * Creates a new {@code PageInspectorImpl}.
     */
    public PageInspectorImpl() {
    }

    /**
     * Helper method that just casts {@code PageInspector.getDefault()}
     * to {@code PageInspectorImpl}.
     * 
     * @return value of {@code PageInspector.getDefault()} casted to {@code PageInspectorImpl}.
     */
    public static PageInspectorImpl getDefault() {
        return (PageInspectorImpl)PageInspector.getDefault();
    }
    
    @Override
    public void inspectPage(Lookup pageContext) {
        DomTCController.getDefault(); // Making sure that DOMTCController is initialized
        KnockoutTCController.getDefault(); // Making sure that KnockoutTCController is initialized
        synchronized (LOCK) {
            PageModel oldModel = getPage();
            if (oldModel != null) {
                oldModel.dispose();
                if (messageDispatcher != null) {
                    messageDispatcher.removeMessageListener(messageListener);
                }
                if (pageInspectorCustomizer != null) {
                    pageInspectorCustomizer.removePropertyChangeListener(pageInspectorCustomizerListener);
                }
                synchronized (PAGE_MODEL_LOCK) {
                    pageModel = null;
                }
                messageDispatcher = null;
                messageListener = null;
                pageInspectorCustomizer = null;
                pageInspectorCustomizerListener = null;
            }
            Resource.clearCache();
            WebKitDebugging webKit = pageContext.lookup(WebKitDebugging.class);
            if (webKit != null) {
                PageModel newModel = new WebKitPageModel(pageContext);
                messageDispatcher = pageContext.lookup(MessageDispatcher.class);
                if (messageDispatcher != null) {
                    messageListener = new InspectionMessageListener(newModel, pageContext);
                    messageDispatcher.addMessageListener(messageListener);
                }
                initSelectionMode(pageContext.lookup(JToolBar.class), pageContext.lookup(JPopupMenu.class), newModel);
                Project p = pageContext.lookup(Project.class);
                if (p != null) {
                    pageInspectorCustomizer = p.getLookup().lookup(PageInspectorCustomizer.class);
                    if (pageInspectorCustomizer != null) {
                        pageInspectorCustomizerListener = createPageInspectorCustomizerListener(newModel, pageInspectorCustomizer);
                        pageInspectorCustomizer.addPropertyChangeListener(pageInspectorCustomizerListener);
                        newModel.setSynchronizeSelection(pageInspectorCustomizer.isHighlightSelectionEnabled());
                    }
                }
                final PageInspectionHandle inspectionHandle = pageContext.lookup(PageInspectionHandle.class);
                if (inspectionHandle != null) {
                    final PageModel model = newModel;
                    newModel.addPropertyChangeListener(new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            String propertyName = evt.getPropertyName();
                            if (PageModel.PROP_SELECTION_MODE.equals(propertyName)) {
                                inspectionHandle.setSelectionMode(model.isSelectionMode());
                            } else if (PageModel.PROP_SYNCHRONIZE_SELECTION.equals(propertyName)) {
                                inspectionHandle.setSynchronizeSelection(model.isSynchronizeSelection());
                            }
                        }
                    });
                    inspectionHandle.setSelectionMode(model.isSelectionMode());
                    inspectionHandle.setSynchronizeSelection(model.isSynchronizeSelection());
                }
                synchronized (PAGE_MODEL_LOCK) {
                    pageModel = newModel;
                }
            }
        }
        firePropertyChange(PROP_MODEL, null, null);
    }

    /**
     * Creates a listener for a page inspector customizer changes.
     *
     * @param pageModel page model the customizer belongs to.
     * @param customizer customizer to observe.
     * @return listener for the specified page inspector customizer.
     */
    PropertyChangeListener createPageInspectorCustomizerListener(final PageModel pageModel,
            final PageInspectorCustomizer customizer) {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if (PageInspectorCustomizer.PROPERTY_HIGHLIGHT_SELECTION.equals(propertyName)) {
                    boolean synchronize = customizer.isHighlightSelectionEnabled();
                    pageModel.setSynchronizeSelection(synchronize);
                }
            }
        };
    }

    /**
     * Adds 'Selection Mode' toggle-button into the specified toolbar.
     *
     * @param toolBar toolbar to insert the toggle-button into (can be {@code null}).
     * @param contextMenu popup menu to insert the toggle-button into (can be {@code null}).
     * @param pageModel mode that the inserted toggle-button should affect.
     */
    private void initSelectionMode(JToolBar toolBar, JPopupMenu contextMenu, final PageModel pageModel) {
        if (toolBar != null) {
            Icon selectionModeIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/web/inspect/resources/selectionMode.png", true); // NOI18N
            final JToggleButton selectionModeButton = new JToggleButton(selectionModeIcon);
            //hardcoded shortcut for Selection Mode
            KeyStroke ks = KeyStroke.getKeyStroke( KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK );
            selectionModeButton.putClientProperty( Action.ACCELERATOR_KEY, ks );
            String selectionModeTooltip = NbBundle.getMessage(PageInspectorImpl.class, "PageInspectorImpl.selectionMode"); // NOI18N
            selectionModeButton.setToolTipText(selectionModeTooltip);
            selectionModeButton.setName(SELECTION_MODE_COMPONENT_NAME);
            selectionModeButton.setFocusPainted(false);

            final JCheckBoxMenuItem selectionModeMenu = new JCheckBoxMenuItem(NbBundle.getMessage(PageInspectorImpl.class, "PageInspectorImpl.selectionModeShort")); //NOI18N
            selectionModeMenu.setAccelerator( ks );
            selectionModeMenu.setIcon( selectionModeIcon );
            ItemListener listener = new ItemListener() {
                @Override
                public void itemStateChanged( ItemEvent e ) {
                    final boolean selectionMode = e.getStateChange() == ItemEvent.SELECTED;
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            pageModel.setSelectionMode(selectionMode);
                        }
                    });
                }
            };
            selectionModeButton.addItemListener( listener );
            selectionModeMenu.addItemListener( listener );
            pageModel.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String propName = evt.getPropertyName();
                    if (PageModel.PROP_SELECTION_MODE.equals(propName)) {
                        final boolean selectionMode = pageModel.isSelectionMode();
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                selectionModeButton.setSelected(selectionMode);
                                selectionModeMenu.setSelected(selectionMode);
                            }
                        });
                    }
                }
            });
            if (toolBar.getComponentCount() == 0) {
                toolBar.addSeparator();
            }
            int gapSize = LayoutStyle.getInstance().getPreferredGap(
                    selectionModeButton, selectionModeButton,
                    LayoutStyle.ComponentPlacement.RELATED,
                    SwingConstants.WEST, toolBar);
            toolBar.add(Box.createHorizontalStrut(gapSize));
            toolBar.add(selectionModeButton);
            selectionModeButton.setSelected(pageModel.isSelectionMode());
            selectionModeMenu.setSelected(pageModel.isSelectionMode());

            if( null != contextMenu ) {
                contextMenu.addSeparator();
                contextMenu.add( selectionModeMenu );
            }
        }
    }

    /**
     * Uninitializes the selection mode (removes the page inspection
     * component/s from the toolbar).
     * 
     * @param toolBar toolBar to remove the buttons from.
     */
    void uninitSelectionMode(JToolBar toolBar) {
        if (toolBar != null) {
            for (Component component : toolBar.getComponents()) {
                if (SELECTION_MODE_COMPONENT_NAME.equals(component.getName())) {
                    toolBar.remove(component);
                    break;
                }
            }
        }
    }

    /**
     * Returns the current inspected page.
     * 
     * @return current inspected page.
     */
    @Override
    public PageModel getPage() {
        synchronized (PAGE_MODEL_LOCK) {
            return pageModel;
        }
    }

    /**
     * Adds a property change listener.
     * 
     * @param listener listener to add.
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener.
     * 
     * @param listener listener to remove.
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Fires the specified property change.
     * 
     * @param propName name of the property.
     * @param oldValue old value of the property or {@code null}.
     * @param newValue new value of the property or {@code null}.
     */
    private void firePropertyChange(String propName, Object oldValue, Object newValue) {
        propChangeSupport.firePropertyChange(propName, oldValue, newValue);
    }

    /**
     * Message listener of the inspected page.
     */
    private class InspectionMessageListener implements MessageListener {
        /** Name of the message type attribute. */
        private static final String MESSAGE_TYPE = "message"; // NOI18N
        /** Value of the message type attribute for the selection mode message. */
        private static final String MESSAGE_SELECTION_MODE = "selection_mode"; // NOI18N
        /** Name of the attribute holding the new value of the selection mode. */
        private static final String MESSAGE_SELECTION_MODE_ATTR = "selectionMode"; // NOI18N
        /** Page model this message listener is related to. */
        private final PageModel pageModel;
        /** Context of the page this listener is related to. */
        private final Lookup pageContext;

        /**
         * Creates a new {@code InspectionMessageListener}.
         * 
         * @param pageModel page model the listener is related to.
         * @param pageContext context of the page.
         */
        InspectionMessageListener(PageModel pageModel, Lookup pageContext) {
            this.pageModel = pageModel;
            this.pageContext = pageContext;
        }
        
        @Override
        public void messageReceived(String featureId, final String messageTxt) {
            if ((messageTxt == null) && !PageInspector.MESSAGE_DISPATCHER_FEATURE_ID.equals(featureId)) {
                return;
            }
            if (messageTxt == null) {
                // Invoke the cancelation of the inspection synchronously.
                // The transport would be detached sooner otherwise.
                processMessage(messageTxt);
            } else {
                // When the message comes from the external browser then
                // this method is called in the thread that is processing all
                // messages from the WebSocket server. We have to avoid blocking
                // of this thread => we process the message in another thread.
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        processMessage(messageTxt);
                    }
                });
            }
        }

        /**
         * Processes incoming message for the inspected page.
         * 
         * @param messageTxt message to process.
         */
        private void processMessage(final String messageTxt) {
            if (messageTxt == null) {
                synchronized (LOCK) {
                    uninitSelectionMode(pageContext.lookup(JToolBar.class));
                    if (pageModel == getPage()) {
                        inspectPage(Lookup.EMPTY);
                    }
                }
            } else {
                try {
                    JSONObject message = (JSONObject)JSONValue.parseWithException(messageTxt);
                    Object type = message.get(MESSAGE_TYPE);
                    // Message about selection mode modification
                    if (MESSAGE_SELECTION_MODE.equals(type)) {
                        boolean selectionMode = (Boolean)message.get(MESSAGE_SELECTION_MODE_ATTR);
                        pageModel.setSelectionMode(selectionMode);
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(PageInspectorImpl.class.getName())
                            .log(Level.INFO, "Ignoring message that is not in JSON format: {0}", messageTxt); // NOI18N
                }
            }
        }

    }

}
