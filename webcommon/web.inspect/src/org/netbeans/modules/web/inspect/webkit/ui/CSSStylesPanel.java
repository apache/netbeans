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
package org.netbeans.modules.web.inspect.webkit.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.Expression;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.Property;
import org.netbeans.modules.css.model.api.PropertyDeclaration;
import org.netbeans.modules.css.model.api.PropertyValue;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.css.visual.api.CssStylesTC;
import org.netbeans.modules.css.visual.api.DeclarationInfo;
import org.netbeans.modules.css.visual.api.RuleEditorController;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.netbeans.modules.web.inspect.CSSUtils;
import org.netbeans.modules.web.inspect.PageInspectorImpl;
import org.netbeans.modules.web.inspect.PageModel;
import org.netbeans.modules.web.inspect.actions.Resource;
import org.netbeans.modules.web.inspect.webkit.RemoteStyleSheetCache;
import org.netbeans.modules.web.inspect.webkit.Utilities;
import org.netbeans.modules.web.inspect.webkit.WebKitPageModel;
import org.netbeans.modules.web.webkit.debugging.api.css.CSS;
import org.netbeans.modules.web.webkit.debugging.api.css.Rule;
import org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetBody;
import org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetHeader;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * WebKit-based CSS Styles view.
 *
 * @author Jan Stola
 */
public class CSSStylesPanel extends JPanel implements PageModel.CSSStylesView {
    /** Request processor used by this class. */
    private static final RequestProcessor RP = new RequestProcessor(CSSStylesPanel.class);
    /** The default instance of this class. */
    private static CSSStylesPanel DEFAULT;
    /** Selection section of CSS Styles view. */
    private final CSSStylesSelectionPanel selectionPanel = new CSSStylesSelectionPanel();
    /** The current inspected page. */
    transient WebKitPageModel pageModel;
    /** Lookup of this panel. */
    private final transient CSSStylesLookup lookup = new CSSStylesLookup();
    /** Node lookup of this panel. */
    private final transient CSSStylesNodeLookup nodeLookup = new CSSStylesNodeLookup();
    /** Lookup result with rules selected in the panel. */
    transient Lookup.Result<Rule> ruleLookupResult;
    /** Determines whether the view is active (i.e. whether it manages the rule controller). */
    boolean active = true;

    /**
     * Creates a new {@code CSSStylesPanel}.
     */
    private CSSStylesPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(400,400));
        PageInspectorImpl.getDefault().addPropertyChangeListener(getListener());
        lookup.updateLookup(selectionPanel.getLookup());
        ruleLookupResult = lookup.lookupResult(Rule.class);
        updatePageModel();
        add(selectionPanel, BorderLayout.CENTER);
        ruleLookupResult.addLookupListener(getListener());
    }

    /**
     * Returns the default instance of this class.
     *
     * @return the default instance of this class.
     */
    public static CSSStylesPanel getDefault() {
        boolean initialize;
        synchronized (CSSStylesPanel.class) {
            initialize = (DEFAULT == null);
        }
        if (initialize) {
            initialize();
        }
        synchronized (CSSStylesPanel.class) {
            return DEFAULT;
        }
    }

    /**
     * Ensures that the {@code DEFAULT} instance is initialized.
     */
    static void initialize() {
        if (EventQueue.isDispatchThread()) {
            synchronized (CSSStylesPanel.class) {
                if (DEFAULT == null) {
                    DEFAULT = new CSSStylesPanel();
                }
            }
        } else {
            try {
                EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        initialize();
                    }
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /** Listener for various events this instance is interested in. */
    private transient Listener listener;
    /**
     * Returns the listener.
     *
     * @return the listener.
     */
    private Listener getListener() {
        if (listener == null) {
            listener = new Listener();
        }
        return listener;
    }

    /**
     * Updates the panel to match the currently inspected page.
     */
    public final void updatePageModel() {
        PageModel page = PageInspectorImpl.getDefault().getPage();
        if (pageModel == page) {
            return;
        }
        if (pageModel != null) {
            pageModel.removePropertyChangeListener(getListener());
            pageModel.getWebKit().getCSS().removeListener(getListener());
        }
        RemoteStyleSheetCache.getDefault().clear();
        if (page instanceof WebKitPageModel) {
            pageModel = (WebKitPageModel)page;
        } else {
            pageModel = null;
        }
        if (pageModel != null) {
            pageModel.addPropertyChangeListener(getListener());
            pageModel.getWebKit().getCSS().addListener(getListener());
        }
        updateContent(false);
    }

    /**
     * Updates the content of this panel.
     *
     * @param keepSelection if {@code true} then an attempt to keep the current
     * selection is made, otherwise the selection is cleared.
     */
    void updateContent(final boolean keepSelection) {
        try {
            contentUpdateInProgress.incrementAndGet();
            nodeLookup.setPageModel(pageModel);
            selectionPanel.updateContent(pageModel, keepSelection);
            updateTitle();
        } finally {
            // Ugly hack that ensures that contentUpdateInProgress
            // is not set to false before the update of Document
            // and Selection panes is finished
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    CSSStylesDocumentPanel.RP.post(new Runnable() {
                        @Override
                        public void run() {
                            CSSStylesSelectionPanel.RP.post(new Runnable() {
                                @Override
                                public void run() {
                                    EventQueue.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (contentUpdateInProgress.decrementAndGet() == 0) {
                                                updateRulesEditor(keepSelection);
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    /** Determines whether the content update is in progress. */
    AtomicInteger contentUpdateInProgress = new AtomicInteger();

    /**
     * Updates the rules editor window to show information about the selected rule.
     */
    void updateRulesEditor(final boolean keepSelection) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                if (pageModel != null) {
                    Collection<? extends Rule> rules = ruleLookupResult.allInstances();
                    String selector = null;
                    if  (rules.size() == 1) {
                        Rule rule = rules.iterator().next();
                        selector = rule.getSelector();
                    }
                    pageModel.setSelectedSelector(selector);
                }
            }
        });
        if (!active) {
            return;
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (keepSelection && cssFileFocused()) {
                    return; // Issue 230897
                }
                final Collection<? extends Rule> rules = ruleLookupResult.allInstances();
                final RuleInfo ruleInfo = (rules.size() == 1) ? lookup.lookup(RuleInfo.class) : null;
                CssStylesTC ruleEditor = (CssStylesTC)WindowManager.getDefault().findTopComponent(CssStylesTC.ID);
                final RuleEditorController controller = ruleEditor.getRuleEditorController();
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        if (rules.size() == 1) {
                            Rule rule = rules.iterator().next();
                            String resourceName = rule.getSourceURL();
                            Project project = null;
                            if (pageModel != null) {
                                project = pageModel.getProject();
                            }
                            FileObject fob = new Resource(project, resourceName).toFileObject();
                            if (fob == null || fob.isFolder() /* issue 233463 */) {
                                StyleSheetBody body = rule.getParentStyleSheet();
                                if (body != null && body.getText() != null) {
                                    fob = RemoteStyleSheetCache.getDefault().getFileObject(body);
                                }
                            }
                            if (fob != null) {
                                try {
                                    Source source = Source.create(fob);
                                    if (source != null) {
                                        ParserManager.parse(Collections.singleton(source), new RuleEditorTask(rule, ruleInfo, controller));
                                        return;
                                    }
                                } catch (ParseException ex) {
                                    Logger.getLogger(CSSStylesPanel.class.getName()).log(Level.INFO, null, ex);
                                }
                            }
                        }
                        controller.setNoRuleState();
                    }
                });
            }
        });
    }

    private boolean cssFileFocused() {
        WindowManager manager = WindowManager.getDefault();
        Frame mainWindow = manager.getMainWindow();
        if (mainWindow.isActive()) {
            TopComponent.Registry registry = manager.getRegistry();
            TopComponent activeTC = registry.getActivated();
            if (activeTC != null) {
                if (activeTC instanceof CssStylesTC) {
                    return true;
                }
                if (manager.isOpenedEditorTopComponent(activeTC)) {
                    FileObject fob = activeTC.getLookup().lookup(FileObject.class);
                    if ((fob != null) && "text/css".equals(fob.getMIMEType())) { // NOI18N
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Updates the title of the enclosing view.
     */
    void updateTitle() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (active) {
                    PageModel page = pageModel;
                    String title = null; // NOI18N
                    if (page != null) {
                        List<? extends Node> nodes = page.getSelectedNodes();
                        if (nodes.size() == 1) {
                            title = nodes.get(0).getDisplayName();
                        }
                    }
                    TopComponent tc = WindowManager.getDefault().findTopComponent("CssStylesTC"); // NOI18N
                    ((CssStylesTC)tc).setTitle(title);
                }
            }
        });
    }
    
    @Override
    public JComponent getView() {
        return this;
    }

    @Override
    public Lookup getLookup() {
        return nodeLookup;
    }

    @Override
    public void activated() {
        active = true;
        updateTitle();
        updateRulesEditor(false);
    }

    @Override
    public void deactivated() {
        active = false;
    }

    /**
     * Listener for various events important for {@code CSSStylesPanel}.
     */
    class Listener implements PropertyChangeListener, LookupListener, CSS.Listener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (PageInspectorImpl.PROP_MODEL.equals(propName)) {
                updatePageModel();
            } else if (PageModel.PROP_DOCUMENT.equals(propName)) {
                updateContent(false);
            } else if (PageModel.PROP_SELECTED_NODES.equals(propName)) {
                updateTitle();
            }
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            // Trying to avoid unwanted flashing of Rule Editor
            if (contentUpdateInProgress.get() == 0) {
                updateRulesEditor(false);
            }
        }

        @Override
        public void mediaQueryResultChanged() {
            updateContentInRP();
        }

        @Override
        public void styleSheetChanged(String styleSheetId) {
            updateContentInRP();
        }

        /**
         * Invokes {@code updateContent()} in a request processor.
         */
        private void updateContentInRP() {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    updateContent(true);
                }
            });
        }

        @Override
        public void styleSheetAdded(StyleSheetHeader header) {
        }

        @Override
        public void styleSheetRemoved(String styleSheetId) {
        }

    }

    /**
     * Lookup of {@code CSSStylesPanel}.
     */
    static class CSSStylesLookup extends ProxyLookup {
        protected final void updateLookup(Lookup lookup) {
            if (lookup == null) {
                setLookups();
            } else {
                setLookups(lookup);
            }
        }
    }

    /**
     * User task that updates the rules editor window (to show the specified rule).
     */
    class RuleEditorTask extends UserTask {
        /** Rule to show in the rules editor. */
        private final Rule rule;
        /** Additional rule information. */
        private final RuleInfo ruleInfo;
        /** Controller of the rule editor where the rule should be shown. */
        private final RuleEditorController controller;

        /**
         * Creates a new {@code RuleEditorTask}.
         *
         * @param rule rule to show in the rules editor.
         * @param ruleInfo additional rule information.
         * @param controller controller of the rule editor where the rule
         * should be shown.
         */
        RuleEditorTask(Rule rule, RuleInfo ruleInfo, RuleEditorController controller) {
            this.rule = rule;
            this.ruleInfo = ruleInfo;
            this.controller = controller;
        }

        /**
         * Determines whether the property with the specified name and value
         * has been parsed without problems.
         *
         * @param propertyName name of the property to check.
         * @param propertyValue value of the property to check.
         * @return {@code true} if the property has been parsed without problems,
         * returns {@code false} otherwise.
         */
        private boolean isParsedOk(String propertyName, String propertyValue) {
            for (org.netbeans.modules.web.webkit.debugging.api.css.Property property : rule.getStyle().getProperties()) {
                if (!property.isParsedOk()
                        && property.getName().equals(propertyName)
                        && property.getValue().equals(propertyValue)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            final boolean[] found = new boolean[1];
            for (final CssParserResult result : Utilities.cssParserResults(resultIterator)) {
                final Model sourceModel = Model.getModel(result);
                sourceModel.runReadTask(new Model.ModelTask() {
                    @Override
                    public void run(StyleSheet styleSheet) {
                        org.netbeans.modules.css.model.api.Rule modelRule = Utilities.findRuleInStyleSheet(sourceModel, styleSheet, rule);
                        if (modelRule != null) {
                            found[0] = true;
                            if (!active) {
                                return;
                            }
                            controller.setModel(sourceModel);
                            controller.setRule(modelRule);
                            if (ruleInfo != null) {
                                if (ruleInfo.getMetaSourceFile() != null) {
                                    controller.setMessage(NbBundle.getMessage(CSSStylesPanel.class, "CSSStylesPanel.generatedRule")); // NOI18N
                                }
                                List<String> active = new ArrayList<String>();
                                Declarations decls = modelRule.getDeclarations();
                                if (decls != null) {
                                    List<Declaration> declarations = decls.getDeclarations();
                                    for (int i=declarations.size()-1; i>=0; i--) {
                                        Declaration declarationElement = declarations.get(i);
                                        PropertyDeclaration declaration = declarationElement.getPropertyDeclaration();
                                        Property property = declaration.getProperty();
                                        String propertyName = property.getContent().toString().trim();
                                        PropertyValue propertyValue = declaration.getPropertyValue();
                                        Expression expression = propertyValue.getExpression();
                                        String value = expression.getContent().toString().trim();
                                        if (isIEHackIgnoredByWebKit(property, result.getSnapshot())) {
                                            controller.setDeclarationInfo(declaration, DeclarationInfo.INACTIVE);
                                        } else if (isParsedOk(propertyName, value)) {
                                            if (!ruleInfo.isInherited() || CSSUtils.isInheritedProperty(propertyName)) {
                                                if (ruleInfo.isOverriden(propertyName) || active.contains(propertyName)) {
                                                    controller.setDeclarationInfo(declaration, DeclarationInfo.OVERRIDDEN);
                                                } else {
                                                    active.add(propertyName);
                                                }
                                            } else {
                                                // Inherited rule but a property that is not inherited
                                                controller.setDeclarationInfo(declaration, DeclarationInfo.INACTIVE);
                                            }
                                        } else {
                                            controller.setDeclarationInfo(declaration, DeclarationInfo.ERRONEOUS);
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
                if (found[0]) {
                    break;
                }
            }
            if (active && !found[0]) {
                controller.setNoRuleState();
            }
        }

        /**
         * Determines whether the given property uses star or underscore
         * hack to affect Internet Explorer only.
         * 
         * @param property property to check.
         * @param snapshot snapshot of the styleSheet.
         * @return {@code true} when the property uses star or underscore hack.
         */
        private boolean isIEHackIgnoredByWebKit(Property property, Snapshot snapshot) {
            boolean isHack = false;
            String styleSheetText = snapshot.getText().toString();
            int startOffset = property.getStartOffset();
            if (startOffset != -1) {
                char c = styleSheetText.charAt(startOffset-1);
                isHack = (c == '_' || c == '*');
            }
            return isHack;
        }

    }

    /**
     * Node lookup of this panel.
     */
    static class CSSStylesNodeLookup extends ProxyLookup {

        /**
         * Updates the lookup.
         * 
         * @param pageModel current page model.
         */
        void setPageModel(final WebKitPageModel pageModel) {
            if (EventQueue.isDispatchThread()) {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        setPageModel(pageModel);
                    }
                });
                return;
            }
            URL url = null;
            FileObject fob = null;
            Project project = null;
            DataObject dob = null;
            if (pageModel != null) {
                try {
                    project = pageModel.getProject();
                    if (project != null) {
                        String documentURL = pageModel.getDocumentURL();
                        url = new URL(documentURL);
                        fob = ServerURLMapping.fromServer(project, url);
                        if (fob != null) {
                            dob = DataObject.find(fob);
                        }
                    }
                } catch (MalformedURLException ex) {
                } catch (DataObjectNotFoundException dnfex) {}
            }
            Lookup lkp;
            if (dob == null) {
                lkp = Lookup.EMPTY;
            } else {
                lkp = Lookups.fixed(url, fob, project, dob);
            }
            Node node = new AbstractNode(Children.LEAF, lkp);
            setLookups(Lookups.singleton(node));
        }
        
    }

}
