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
package org.netbeans.modules.web.jsf.navigation;

import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import javax.swing.SwingUtilities;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.filesystems.FileChangeListener;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.netbeans.modules.web.jsf.navigation.PageFlowToolbarUtilities.Scope;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentModelProvider;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.util.NbPreferences;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;

/**
 *
 * @author joelle lam
 */
public class PageFlowController {

    private PageFlowView view;
    private JSFConfigModel configModel;
    private DataObject configDataObj;
    private final Map<NavigationCase, NavigationCaseEdge> navCase2NavCaseEdge = new WeakHashMap<NavigationCase, NavigationCaseEdge>();
    private final Map<NavigationRule, String> navRule2String = new WeakHashMap<NavigationRule, String>();
    private final HashMap<String, WeakReference<Page>> pageName2Page = new HashMap<String, WeakReference<Page>>(); //Should this be synchronized.
    //    public static final String DEFAULT_DOC_BASE_FOLDER = "web"; //NOI18NF
    private static final String NO_WEB_FOLDER_WARNING = NbBundle.getMessage(PageFlowController.class, "MSG_NoWebFolder");
    private static final String NO_WEB_FOLDER_TITLE = NbBundle.getMessage(PageFlowController.class, "TLE_NoWebFolder");
    private volatile FileObject webFolder;
    private AtomicBoolean isListenerRegistered = new AtomicBoolean(false);

    /** Creates a new instance of PageFlowController
     * @param context
     * @param view
     */
    public PageFlowController(JSFConfigEditorContext context, PageFlowView view) {
        this.view = view;
        FileObject configFile = context.getFacesConfigFile();

        try {
            configDataObj = DataObject.find(configFile);
        } catch (DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
        }
        configModel = ConfigurationUtils.getConfigModel(configFile, true);

        assert configModel != null;
        //  Project project = FileOwnerQuery.getOwner(configFile);
        //        webFolder = project.getProjectDirectory().getFileObject(DEFAULT_DOC_BASE_FOLDER);
        webFolder = PageFlowView.getWebFolder(configFile);
        webFiles = setupWebFiles(webFolder);
    }
    private Collection<FileObject> webFiles;

    private Collection<FileObject> setupWebFiles(FileObject webFolder) {
        final Collection<FileObject> myWebFiles = new LinkedList<FileObject>();
        if (webFolder == null) {
            ifNecessaryShowNoWebFolderDialog();
        } else {
            // loading all the relevant files may take quite a while - see #177459
            // they're also loaded again every time the page flow editor is opened;
            // would be better to hold onto them and listen for changes in the web dir(s).
            // for now fixing it this way as it is safer (i'm not familiar
            // with the page flow editor, and this is probably not the best place for this code.
            // but in any case this should be a pretty safe fix).
            AtomicBoolean canceled = new AtomicBoolean();
            ProgressUtils.runOffEventDispatchThread(new Runnable() {

                @Override
                public void run() {
                    myWebFiles.addAll(getAllProjectRelevantFilesObjects());
                }
            }, NbBundle.getMessage(PageFlowController.class, "MSG_LoadingWebFiles"), canceled, false);
        }
        return myWebFiles;
    }

    protected void ifNecessaryShowNoWebFolderDialog() {
        if (isShowNoWebFolderDialog()) {

            final NotWebFolder panel = new NotWebFolder(NO_WEB_FOLDER_WARNING);
            DialogDescriptor descriptor = new DialogDescriptor(panel, NO_WEB_FOLDER_TITLE, true, NotifyDescriptor.PLAIN_MESSAGE, NotifyDescriptor.YES_OPTION, null);
            JButton okButton = new JButton(
                    NbBundle.getMessage(PageFlowController.class, "MSG_OkButtonText")); //NOI18N
            descriptor.setOptions(new Object[]{okButton});
            descriptor.setMessageType(NotifyDescriptor.PLAIN_MESSAGE);
            descriptor.setClosingOptions(new Object[]{okButton});
            descriptor.setOptionsAlign(DialogDescriptor.BOTTOM_ALIGN);
            final Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
            d.setSize(400, 200);
            d.setVisible(true);

            setShowNoWebFolderDialog(panel.getShowDialog());
        }
    }

    public void destroy() {
        webFolder = null;
        configModel = null;
        view = null;
        webFiles.clear();
        navCase2NavCaseEdge.clear();
        navRule2String.clear();
        pageName2Page.clear();

    }
    private static final String PROP_SHOW_NO_WEB_FOLDER = "showNoWebFolder"; // NOI18N

    public final void setShowNoWebFolderDialog(boolean show) {
        getPreferences().putBoolean(PROP_SHOW_NO_WEB_FOLDER, show);
    }

    private static Preferences getPreferences() {
        return NbPreferences.forModule(PageFlowController.class);
    }

    public final boolean isShowNoWebFolderDialog() {
        return getPreferences().getBoolean(PROP_SHOW_NO_WEB_FOLDER, true);
    }
    private PropertyChangeListener pcl;
    private FileChangeListener fcl;

    public synchronized boolean isListenerRegistered() {
        return isListenerRegistered.get();
    }

    //    private ComponentListener cl;
    public synchronized void registerListeners() {
        if (isListenerRegistered.get()) {
            return;
        }

        if (pcl == null && configModel != null) {
            pcl = new FacesModelPropertyChangeListener(this);
            configModel.addPropertyChangeListener(pcl);
            isListenerRegistered.set(true);
        } else {
            return;
        }

        FileObject myWebFolder = getWebFolder();
        if (fcl == null) {
            fcl = new WebFolderListener(this);
            if (myWebFolder != null) {
                try {
                    FileSystem fileSystem = myWebFolder.getFileSystem();
                    fileSystem.addFileChangeListener(fcl);

                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    /**
     * Unregister any listeners.
     */
    public void unregisterListeners() {

        if (pcl != null) {
            if (configModel != null) {
                configModel.removePropertyChangeListener(pcl);
            }
            pcl = null;
        }

        FileObject myWebFolder = getWebFolder();
        if (fcl != null && myWebFolder != null) {
            try {
                FileSystem fileSystem = myWebFolder.getFileSystem();
                fileSystem.removeFileChangeListener(fcl);
                fcl = null;
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    FileChangeListener getFCL() {
        return fcl;
    }

    void flushGraphIfDirty() {

        if (isFilesDirty) {
            webFiles = setupWebFiles(webFolder);
            isFilesDirty = false;
        }
        if (isGraphDirty) {
            if (isWellFormed) {
                EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        if (view == null) {
                            // XXX #145074 It is destroyed already, revise that pattern.
                            return;
                        }
                        view.removeUserMalFormedFacesConfig(); // Does clear graph take care of this?
                        setupGraph();
                    }
                });
            } else {
                EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        if (view == null) {
                            // XXX #145074 It is destroyed already, revise that pattern.
                            return;
                        }
                        view.clearGraph();
                        view.warnUserMalFormedFacesConfig();
                    }
                });

            }
            isGraphDirty = false;
        }
    }
    private boolean isWellFormed = true;
    private boolean isGraphDirty = false;
    private boolean isFilesDirty = false;

    protected void setGraphDirtyWellFormed(boolean isWellFormed) {
        isGraphDirty = true;
        this.isWellFormed = isWellFormed;
    }

    protected void setGraphDirty() {
        isGraphDirty = true;
    }

    protected void setFilesDirty() {
        this.isFilesDirty = true;
        isGraphDirty = true;
    }

    public boolean isCurrentScope(Scope scope) {
        return PageFlowToolbarUtilities.getInstance(view).getCurrentScope().equals(scope);
    }

    /**
     * Creates a Link in the FacesConfiguration
     * @param source from page, if null an NPE will be thrown.
     * @param target to page, if null an NPE will be thrown.
     * @param pinNode if null then it was not conntect to a pin.
     * @return
     */
    public NavigationCase createLink(Page source, Page target, Pin pinNode) {
        if (source == null) {
            throw new NullPointerException("Source page should not be null.");
        } else if (target == null) {
            throw new NullPointerException("Target page should not be null");
        }

        String sourceName = source.getDisplayName();
        int caseNum = 1;

        configModel.startTransaction();
        FacesConfig facesConfig = configModel.getRootComponent();
        NavigationRule navRule = getRuleWithFromViewID(facesConfig, source.getDisplayName());
        NavigationCase navCase = configModel.getFactory().createNavigationCase();
        if (navRule == null) {
            navRule = configModel.getFactory().createNavigationRule();
            FacesModelUtility.setFromViewId(navRule, source.getDisplayName());
            facesConfig.addNavigationRule(navRule);
            navRule2String.put(navRule, FacesModelUtility.getFromViewIdFiltered(navRule));
        } else {
            caseNum = getNewCaseNumber(navRule);
        }
        String caseName = CASE_STRING + Integer.toString(caseNum);

        if (pinNode != null) {
            pinNode.setFromOutcome(caseName);
        }
        navCase.setFromOutcome(caseName);

        FacesModelUtility.setToViewId(navCase, target.getDisplayName());
        navRule.addNavigationCase(navCase);

        try {
            configModel.endTransaction();
            configModel.sync();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalStateException ise) {
            Exceptions.printStackTrace(ise);
        }

        return navCase;
    }

    public void updatePageItems(Page pageNode) {
        view.resetNodeWidget(pageNode, true);
        view.validateGraph();
    }
    private static final String CASE_STRING = "case";

    private int getNewCaseNumber(NavigationRule navRule) {
        Collection<String> caseOutcomes = new HashSet<String>();
        List<NavigationCase> navCases = navRule.getNavigationCases();
        for (NavigationCase navCase : navCases) {
            caseOutcomes.add(navCase.getFromOutcome());
        //            caseOutcomes.add(navCase.getFromAction());
        }

        int caseNum = 1;
        while (true) {
            if (!caseOutcomes.contains(CASE_STRING + Integer.toString(caseNum))) {
                return caseNum;
            }
            caseNum++;
        }
    }

    /**
     * @return the navigation rule.  This will be null if none was found
     **/
    private NavigationRule getRuleWithFromViewID(FacesConfig facesConfig, String fromViewId) {

        for (NavigationRule navRule : facesConfig.getNavigationRules()) {
            String rulefromViewId = FacesModelUtility.getFromViewIdFiltered(navRule);
            if (rulefromViewId != null && rulefromViewId.equals(fromViewId)) {
                //  Match Found
                return navRule;
            }
        }

        return null;
    }

    private final Collection<FileObject> getAllProjectRelevantFilesObjects() {
        return getProjectKnownFileOjbects(getWebFolder());
    }

    private Collection<FileObject> getProjectKnownFileOjbects(FileObject folder) {
        Collection<FileObject> projectKnownFiles = new LinkedList<FileObject>();

        FileObject[] childrenFiles = new FileObject[]{};
        if (folder != null) {
            childrenFiles = folder.getChildren();
        }
        for (FileObject file : childrenFiles) {
            if (!file.isFolder()) {
                if (isKnownFile(file)) {
                    projectKnownFiles.add(file);
                }
            } else if (isKnownFolder(file)) {
                projectKnownFiles.addAll(getProjectKnownFileOjbects(file));
            }
        }

        return projectKnownFiles;
    }

    /**
     * Check if the file type in known.
     * @param file the fileobject type to check. If null, throws NPE.
     * @return if it is of type jsp, jspf, or html it will return true.
     */
    public final boolean isKnownFile(FileObject file) {
        String[] knownMimeTypes = {"text/x-jsp", "text/html", "text/xhtml"}; //NOI18N
        String mimeType = file.getMIMEType(knownMimeTypes);
        if (mimeType.equals("text/x-jsp") && !file.getExt().equals("jspf")) { //NOI18N
            return true;
        } else if (mimeType.equals("text/html") || mimeType.equals("text/xhtml")) { //NOI18N
            return true;
        }
        return false;
    }

    public final boolean isKnownFolder(FileObject folder) {
        /* If it is not a folder return false*/
        if (!folder.isFolder()) {
            return false;
        }
        /* If it does not exist within WebFolder return false */
        if (!folder.getPath().contains(getWebFolder().getPath())) {
            return false;
        }
        /* If it exists withing WEB-INF or META-INF return false */
        if (folder.getPath().contains("WEB-INF") || folder.getPath().contains("META-INF")) {
            return false;
        }
        return true;
    }

    /**
     * Setup The Graph
     * Should only be called by init();
     *
     **/
    public boolean setupGraph() {
        view.saveLocations();
        return setupGraphNoSaveData();
    }
    private PropertyChangeListener otherFacesConfigListener = null;

    private PropertyChangeListener getOtherFacesConfigListener() {
        if (otherFacesConfigListener == null) {
            return new OtherFacesModelListener();
        }
        return otherFacesConfigListener;
    }

    private void removeOtherFacesConfigListener() {
        WebModule webModule = WebModule.getWebModule(getWebFolder());
        FileObject[] configFiles = ConfigurationUtils.getFacesConfigFiles(webModule);
        for (FileObject aConfigFile : configFiles) {
            JSFConfigModel aConfigModel = ConfigurationUtils.getConfigModel(aConfigFile, true);
            aConfigModel.removePropertyChangeListener(otherFacesConfigListener);
        }
        otherFacesConfigListener = null;
    }

    protected void releaseGraphInfo() {
        /* This listener is only created when it was a All_FACES scope */
        if (otherFacesConfigListener != null) {
            removeOtherFacesConfigListener();
        }

        view.clearGraph();
        clearPageName2Page();
        navCase2NavCaseEdge.clear();
        navRule2String.clear();

    }

    public boolean setupGraphNoSaveData() {
        LOGGER.entering(PageFlowController.class.toString(), "setupGraphNoSaveData()");

        assert configModel != null;
        //        assert webFolder != null;
        assert webFiles != null;
        releaseGraphInfo();

        FacesConfig facesConfig = configModel.getRootComponent();

        if (facesConfig == null) {
            return false;
        }

        /* If the most recently saved xml doc is malformed, we should know about it through this try statement. */
        try {
            List<NavigationRule> rules = null;
            if (isCurrentScope(Scope.SCOPE_FACESCONFIG)) {
                rules = facesConfig.getNavigationRules();
                for (NavigationRule navRule : rules) {
                    navRule2String.put(navRule, FacesModelUtility.getFromViewIdFiltered(navRule));
                }
                Collection<String> pagesInConfig = getFacesConfigPageNames(rules);
                createFacesConfigPages(pagesInConfig);
            } else if (isCurrentScope(Scope.SCOPE_PROJECT)) {
                rules = facesConfig.getNavigationRules();
                for (NavigationRule navRule : rules) {
                    navRule2String.put(navRule, FacesModelUtility.getFromViewIdFiltered(navRule));
                }
                Collection<String> pagesInConfig = getFacesConfigPageNames(rules);
                createAllProjectPages(pagesInConfig);
            } else if (isCurrentScope(Scope.SCOPE_ALL_FACESCONFIG)) {
                List<NavigationRule> allRules = new ArrayList<NavigationRule>();
                FileObject myWebFolder = getWebFolder();
                if (myWebFolder != null) {
                    WebModule webModule = WebModule.getWebModule(myWebFolder);
                    FileObject[] configFiles = ConfigurationUtils.getFacesConfigFiles(webModule);
                    for (FileObject aConfigFile : configFiles) {
                        JSFConfigModel aConfigModel = ConfigurationUtils.getConfigModel(aConfigFile, true);
                        if (aConfigModel != null) {
                            allRules.addAll(aConfigModel.getRootComponent().getNavigationRules());
                            if (!configModel.equals(aConfigModel)) {
                                aConfigModel.addPropertyChangeListener(getOtherFacesConfigListener());
                            }
                        }
                    }
                    for (NavigationRule navRule : allRules) {
                        navRule2String.put(navRule, FacesModelUtility.getFromViewIdFiltered(navRule));
                    }
                    Collection<String> pagesInConfig = getFacesConfigPageNames(allRules);
                    createFacesConfigPages(pagesInConfig);
                    rules = allRules;
                } else {
                    /* If no web module exists don't worry about other faces-config files */
                    rules = facesConfig.getNavigationRules();
                    for (NavigationRule navRule : rules) {
                        navRule2String.put(navRule, FacesModelUtility.getFromViewIdFiltered(navRule));
                    }
                    Collection<String> pagesInConfig = getFacesConfigPageNames(rules);
                    createAllProjectPages(pagesInConfig);
                }
            }
            createAllEdges(rules);
            view.validateGraph();
            LOGGER.log(new LogRecord(Level.FINE, "PageFlowEditor # Rules: " + rules.size() + "\n" + "               # WebPages: " + webFiles.size() + "\n" + "               # TotalPages: " + pageName2Page.size()));
        } catch (IllegalStateException ise) {
            view.warnUserMalFormedFacesConfig();
            view.validateGraph();
            LOGGER.log(new LogRecord(Level.FINE, "Illegal SateException thrown: " + ise.toString()));
        }
        LOGGER.exiting(PageFlowController.class.toString(), "setupGraphNoSaveData()");
        return true;
    }

    private class OtherFacesModelListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {

            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    if (view == null) {
                        // XXX #145074 It is destroyed already, revise that pattern.
                        return;
                    }
                    setupGraph();
                }
            });
        }
    }

    private void createAllEdges(List<NavigationRule> rules) {

        List<NavigationRule> editableRules = configModel.getRootComponent().getNavigationRules();
        for (NavigationRule rule : rules) {
            List<NavigationCase> navCases = rule.getNavigationCases();

            /* this is for ALL_FACES_CONFIG scope*/
            boolean isModifableEdge = editableRules.contains(rule);

            for (NavigationCase navCase : navCases) {

                NavigationCaseEdge navEdge = new NavigationCaseEdge(this, navCase);
                navCase2NavCaseEdge.put(navCase, navEdge);
                navEdge.setModifiable(isModifableEdge);
                if (navEdge.getFromViewId() != null && navEdge.getToViewId() != null) {
                    createEdge(navEdge);
                }
            }
        }
    }

    /**
     * Creates and edge in the scene, this method does not add an reference in
     * the faces configuration.  In general it is best to call createLink
     * as that will call createEdge indirectly through the faces model listener.
     * @param caseNode a NavigationCaseEdge.  If null, will throw NPE.
     */
    protected void createEdge(NavigationCaseEdge caseNode) {
        String fromPage = caseNode.getFromViewId();
        String toPage = caseNode.getToViewId();
        if (getPageName2Page(fromPage) == null || getPageName2Page(toPage) == null) {
            System.err.println("Why is this node null? CaseNode: " + caseNode);
            System.err.println("FromPage: " + fromPage);
            System.err.println("ToPage: " + toPage);
            Thread.dumpStack();
        } else {
            view.createEdge(caseNode, getPageName2Page(fromPage), getPageName2Page(toPage));
        }
    }

    private Collection<String> getFacesConfigPageNames(Collection<NavigationRule> navRules) {
        // Get all the pages in the faces config.  But don't list them twice.
        Collection<String> pages = new HashSet<String>();
        for (NavigationRule navRule : navRules) {
            String pageName = FacesModelUtility.getFromViewIdFiltered(navRule);
            pages.add(pageName);
            Collection<NavigationCase> navCases = navRule.getNavigationCases();
            for (NavigationCase navCase : navCases) {
                //                String toPage = navCase.getToViewId();
                String toPage = FacesModelUtility.getToViewIdFiltered(navCase);
                if (toPage != null) {
                    pages.add(toPage);
                }
            }
        }
        return pages;
    }
    public java.util.Stack<String> PageFlowCreationStack = new java.util.Stack<String>();
    private int PageFlowCreationCount = 0;

    /**
     * Create a Page from a node
     *  This method
     * does not actually add the pages to the scene.  It just creates the
     * component.  You will need to call scene.createNode(page) if you want.
     * @param node the node or dataobject node delegate of a given fileobject.
     *             Use dataObject.find(fileObject).getNodeDelegate for the given
     *             page. If no dataObject backing the page, call createPage(String)
     * @return page the Page that was created.
     */
    public Page createPage(Node node) {
        Page pageNode = new Page(this, node);
        Calendar rightNow = Calendar.getInstance();
        PageFlowCreationStack.push("\n" + PageFlowCreationCount + ". " + rightNow.get(Calendar.MINUTE) + ":" + rightNow.get(Calendar.SECOND) + " -  " + pageNode);
        PageFlowCreationCount++;
        return pageNode;
    }

    /*
     * Create PageFlow from a string with no backing page. This method
     * does not actually add the pages to the scene.  It just creates the
     * component.  You will need to call scene.createNode(page) if you want
     * to add it to the scene.
     * @param name the string of the name of the page to create
     *             If null is passed, NPE thrown.
     *             If empty string assertion thrown and null returned.
     * @return page the Page that was created.
     */
    public Page createPage(String pageName) {
        Page node = null;
        if (pageName == null) {
            throw new NullPointerException("Page name string is null");
        }
        assert pageName.length() != 0;
        Node tmpNode = new AbstractNode(Children.LEAF);
        tmpNode.setName(pageName);
        node = createPage(tmpNode);
        return node;
    }
    public java.util.Stack<String> PageFlowDestroyStack = new java.util.Stack<String>();
    private int PageFlowDestroyCount = 0;

    /**
     * Destroys the page in the scene (removing the page content model and
     * the page content listeners).  This odes not actual destroy the dataobject
     * or the backing file object.
     * @param page Page to be deleted.
     */
    private void destroyPageFlowNode(Page page) {
        if (page != null) {
            page.destroy2();
            Calendar rightNow = Calendar.getInstance();
            PageFlowDestroyStack.push("\n" + PageFlowDestroyCount + ". " + rightNow.get(Calendar.MINUTE) + ":" + rightNow.get(Calendar.SECOND) + " -  " + page);
            PageFlowDestroyCount++;
        }
    }

    private void createAllProjectPages(Collection<String> pagesInConfig) {

        Collection<String> pages = new HashSet<String>(pagesInConfig);

        //Create all pages in the project...
        FileObject[] webFilesTmp = webFiles.toArray(new FileObject[0]);//Use copy because you may need to remove these files.
        for (FileObject webFile : webFilesTmp) {
            //DISPLAYNAME:
            String webFileName = Page.getFolderDisplayName(getWebFolder(), webFile);
            Page node = null;
            try {
                node = createPage((DataObject.find(webFile)).getNodeDelegate());
                view.createNode(node, null, null);
                //Do not remove the webFile page until it has been created with a data Node.  If the dataNode throws and exception, then it can be created with an Abstract node.
                pages.remove(webFileName);
            } catch (DataObjectNotFoundException ex) {
                webFiles.remove(webFile); //Remove this file because it may have been deleted.
            }
        }

        //Create any pages that don't actually exist but are defined specified by the config file.
        for (String pageName : pages) {
            if (pageName != null) {
                Node tmpNode = new AbstractNode(Children.LEAF);
                tmpNode.setName(pageName);
                Page node = createPage(tmpNode);
                view.createNode(node, null, null);
            }
        }
    }

    /**
     * Givena pageName, look through the list of predefined webFiles and return the matching fileObject
     * @return FileObject for which the match was found or null of none was found.
     **/
    private FileObject getFileObject(String pageName) {
        for (FileObject webFile : webFiles) {
            //DISPLAYNAME:
            String webFileName = Page.getFolderDisplayName(getWebFolder(), webFile);
            //            String webFileName = webFile.getNameExt();
            if (webFileName.equals(pageName)) {
                return webFile;
            }
        }
        return null;
    }

    private void createFacesConfigPages(Collection<String> pagesInConfig) {
        Collection<String> pages = new HashSet<String>(pagesInConfig);

        for (String pageName : pages) {
            if (pageName != null) {
                FileObject file = getFileObject(pageName);
                Node wrapNode = null;
                if (file == null) {
                    wrapNode = new AbstractNode(Children.LEAF);
                    wrapNode.setName(pageName);
                } else {
                    try {
                        wrapNode = (DataObject.find(file)).getNodeDelegate();
                    } catch (DataObjectNotFoundException donfe) {
                        Exceptions.printStackTrace(donfe);
                    }
                }
                Page node = createPage(wrapNode);
                view.createNode(node, null, null);
            }
        }
    }
    private static final Logger LOGGER = Logger.getLogger(PageFlowController.class.getName());

    /**
     * Remove the page from the hashtable of string (or pages names ) to actual
     * pages.  Use permDestroy value to destroy the page in the scene completely.
     * @param page that you want to remove.
     * @param permDestroy true - destroys the page in the scene (removing the
     *                    page content model and the page content listeners).
     *                    This does not actual destroy the dataobject
     *                    or the backing file object.
     *                    false - if you just want to remove it from the list
     *                    with the associated name.
     * @return page that was removed.
     */
    public Page removePageName2Page(Page page, boolean permDestroy) {
        return removePageName2Page(page.getDisplayName(), permDestroy);
    }

    /**
     * Refer to removePageName2Page(Page page, boolean permDestroy) for details
     * @param pageName the string value of the page name that you want removed.
     * @param destroy
     * @return
     */
    public Page removePageName2Page(String pageName, boolean permDestroy) {
        LOGGER.finest("PageName2Page: remove " + pageName);
        checkAWTThread();
        synchronized (pageName2Page) {
            Page node = null;
            WeakReference<Page> nodeRef = pageName2Page.remove(pageName);
            if (nodeRef != null) {
                node = nodeRef.get();
                if (permDestroy) {
                    destroyPageFlowNode(node);
                }
            }
            return node;
        }
    }

    /**
     * Replace page name in PageName2Node HasMap. This is general used in a
     * page rename.  In general this removes the old Page and add the new one with
     * the given name.
     * @param page Page that should be added into the map.  If null, NPE thrown
     *             and nothing removed from the map.
     * @param String newName String that you want to assign to the page.
     * @param String oldName String that was assigned to the page.
     * @return true if page was found to replace, false is page was not found.
     **/
    public boolean replacePageName2Page(Page page, String newName, String oldName) {

        LOGGER.finest("PageName2Page: replace " + oldName + " to " + newName);
        //assert (newName.length() > 0);
        //assert (oldName.length() > 0);

        if (page == null) {
            throw new NullPointerException("Page can not be null.");
        }

        checkAWTThread();
        synchronized (pageName2Page) {
            WeakReference<Page> page2Ref = pageName2Page.remove(oldName);
            if (page2Ref != null) {
                Page pageFound = page2Ref.get();

                if (pageFound != null) {
                    LOGGER.finest("Trying to replace page in map, but page not found:" + page);
                }
                pageName2Page.put(newName, new WeakReference<Page>(page));
                return true;
            }
            return false;
        }
    }

    /**
     * Clears the pageName 2 Page mapping.  Generally you want do this when you
     * are about to throw everything in the scene away.  This keeps references
     * from being kept.
     */
    protected void clearPageName2Page() {
        LOGGER.finest("PageName2Page: clear");
        Set<String> keys;
        synchronized (pageName2Page) {
            keys = new HashSet<String>(pageName2Page.keySet());
        }
        for (String key : keys) {
            removePageName2Page(key, true);
        }
    }

    /**
     * Associate a page with a given string name for future reference.  In general
     * this method is called by a Page object to add itself.  Really no other classes
     * should use this method.
     * @param displayName name of the page you would like to reference it with (key)
     *                    displayName can not be an empty string.
     * @param page Page to be associated with the string. If null, NPE thrown.
     */
    protected void putPageName2Page(String displayName, Page page) {

        LOGGER.finest("PageName2Page: put " + displayName);
        //assert displayName.length() != 0;
        if (page == null) {
            throw new NullPointerException("putPageName2Page does not accept null pages.");
        }

        checkAWTThread();
        synchronized (pageName2Page) {
            pageName2Page.put(displayName, new WeakReference<Page>(page));
        }
    }

    /**
     * Get a page in the map given it's key.  This is a basic lookup table.
     * @param displayName String or associated key.
     * @return the Page that is associated with the given key.
     */
    protected Page getPageName2Page(String displayName) {


        checkAWTThread();
        if (displayName == null) {
            throw new NullPointerException("Displayname should not be null. You may be using this method incorrectly.");
        }
        // assert displayName.length() != 0;
        synchronized (pageName2Page) {
            /*
             * Begin Test
             */
            /* Page pageNode = pageName2Page.remove(displayName);
            if (pageNode != null) {
            Page pageNode2 = pageName2Page.get(displayName);
            if (pageNode2 != null) {
            throw new RuntimeException("Why are there two of the same page?: " + displayName + "\n PageNode1: " + pageNode + "\n PageNode2:" + pageNode2);
            }
            putPageName2Page(displayName, pageNode);
            } */
            /*
             * End Test
             */
            Page page = null;
            WeakReference<Page> pageRef = pageName2Page.get(displayName);
            if (pageRef != null) {
                page = pageRef.get();
            }
            return page;
        }
    }

    /* This methods makes sure that the call if from the AWT Thread.
     * If not it will dump the thread stack
     */
    private void checkAWTThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            Thread.dumpStack();
            throw new RuntimeException("Not a Dispatched Thread");
        }
    }

    /**
     * Rename all references to a given page int eh faces config file.
     * @param oldName String old name, if null thrown npe.
     * @param newName String new name, if null thrown npe.
     */
    public void renamePageInModel(String oldName, String newName) {
        FacesModelUtility.renamePageInModel(configModel, oldName, newName);
    }

    /**
     * Remove page from the scene.
     * @param pageNode
     */
    public void removeSceneNodeEdges(Page pageNode) {

        Collection<NavigationCaseEdge> navCaseNodes = view.getNodeEdges(pageNode);
        for (NavigationCaseEdge navCaseNode : navCaseNodes) {
            try {
                navCaseNode.destroy();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        //            view.removeEdge(navCaseNode);
        }
    }

    /**
     * Remove all rules and cases with this pagename.
     * @param displayName
     */
    public void removePageInModel(String displayName) {
        configModel.startTransaction();
        FacesConfig facesConfig = configModel.getRootComponent();
        List<NavigationRule> navRules = facesConfig.getNavigationRules();
        for (NavigationRule navRule : navRules) {
            String fromViewId = FacesModelUtility.getFromViewIdFiltered(navRule);
            if (fromViewId != null && fromViewId.equals(displayName)) {
                //if the rule is removed, don't check the cases.
                facesConfig.removeNavigationRule(navRule);
            } else {
                List<NavigationCase> navCases = navRule.getNavigationCases();
                for (NavigationCase navCase : navCases) {
                    //                    String toViewId = navCase.getToViewId();
                    String toViewId = FacesModelUtility.getToViewIdFiltered(navCase);
                    if (toViewId != null && toViewId.equals(displayName)) {
                        navRule.removeNavigationCase(navCase);
                    }
                }
            }
        }

        try {
            configModel.endTransaction();
            configModel.sync();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalStateException ise) {
            Exceptions.printStackTrace(ise);
        }
    }

    /**
     * Gets the WebFolder which contains the jsp pages.
     * @return FileObject webfolder
     */
    public FileObject getWebFolder() {
        //        assert webFolder.isValid();
        return webFolder;
    }

    public boolean isPageInAnyFacesConfig(String name) {
        WebModule webModule = WebModule.getWebModule(getWebFolder());
        FileObject[] configFiles = ConfigurationUtils.getFacesConfigFiles(webModule);
        for (FileObject aConfigFile : configFiles) {
            JSFConfigModel aConfigModel = ConfigurationUtils.getConfigModel(aConfigFile, true);
            List<NavigationRule> rules = aConfigModel.getRootComponent().getNavigationRules();
            Collection<String> pagesInConfig = getFacesConfigPageNames(rules);
            if (pagesInConfig.contains(name)) {
                return true; /* Return as soon as you find one. */
            }
        }
        return false;
    }

    public boolean isNavCaseInFacesConfig(NavigationCaseEdge navEdge) {
        NavigationCase navCase = getNavCase2NavCaseEdge(navEdge);
        JSFConfigComponent navRule = navCase.getParent();
        if (configModel.getRootComponent().getNavigationRules().contains(navRule)) {
            return true;
        }
        return false;
    }

    public void changeToAbstractNode(Page oldNode, String displayName) {
        //1. Make Old Node an abstract node
        Node tmpNode = new AbstractNode(Children.LEAF);
        tmpNode.setName(displayName);
        oldNode.replaceWrappedNode(tmpNode); //Does this take care of pageName2Node?
        view.resetNodeWidget(oldNode, true);
    }

    public DataObject getConfigDataObject() {
        return configDataObj;
    }

    public void saveLocation(String oldDisplayName, String newDisplayName) {
        view.saveLocation(oldDisplayName, newDisplayName);
    }

    /* WebFiles Wrappers */
    public final boolean removeWebFile(FileObject fileObj) {
        return webFiles.remove(fileObj);
    }

    /* WebFile Wrapper that adds a file to the webFile collection */
    public final boolean addWebFile(FileObject fileObj) {
        return webFiles.add(fileObj);
    }

    public final boolean containsWebFile(FileObject fileObj) {
        return webFiles.contains(fileObj);
    }

    public final void putNavCase2NavCaseEdge(NavigationCase navCase, NavigationCaseEdge navCaseEdge) {
        navCase2NavCaseEdge.put(navCase, navCaseEdge);
    }

    public final NavigationCaseEdge getNavCase2NavCaseEdge(NavigationCase navCase) {
        return navCase2NavCaseEdge.get(navCase);
    }

    private final NavigationCase getNavCase2NavCaseEdge(NavigationCaseEdge navEdge) {
        Set<Entry<NavigationCase, NavigationCaseEdge>> entries = navCase2NavCaseEdge.entrySet();
        for (Entry entry : entries) {
            if (entry.getValue().equals(navEdge)) {
                return (NavigationCase) entry.getKey();
            }
        }
        return null;
    }

    public final NavigationCaseEdge removeNavCase2NavCaseEdge(NavigationCase navCase) {
        return navCase2NavCaseEdge.remove(navCase);
    }

    //NavRule2String wrappers
    public final String removeNavRule2String(NavigationRule navRule) {
        return navRule2String.remove(navRule);
    }

    public final String putNavRule2String(NavigationRule navRule, String navRuleName) {
        return navRule2String.put(navRule, navRuleName);
    }

    public PageFlowView getView() {
        return view;
    }

    public void setModelNavigationCaseName(NavigationCase navCase, String newName) {
        configModel.startTransaction();

        //By default check from outcome first.  Maybe this should be the expectation.
        if (navCase.getFromOutcome() != null) {
            navCase.setFromOutcome(newName);
        }
        if (navCase.getFromAction() != null) {
            navCase.setFromAction(newName);
        }

        try {
            configModel.endTransaction();
            configModel.sync();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalStateException ise) {
            Exceptions.printStackTrace(ise);
        }
    }

    public void removeModelNavigationCase(NavigationCase navCase) throws IOException {
        configModel.startTransaction();
        NavigationRule navRule = (NavigationRule) navCase.getParent();
        if (navRule != null && navRule.getNavigationCases().contains(navCase)) {
            //Only delete if it is still valid.
            navRule.removeNavigationCase(navCase);
            if (navRule.getNavigationCases().size() < 1) {
                configModel.removeChildComponent(navRule); //put this back once you remove hack
            }
        }

        try {
            configModel.endTransaction();
        } catch (IllegalStateException ise) {
            Exceptions.printStackTrace(ise);
        }
        configModel.sync();
    }

    public void serializeNodeLocations() {
        if (view != null && configDataObj !=null) {
            view.serializeNodeLocations(PageFlowView.getStorageFile(configDataObj.getPrimaryFile()));
        } else {
            LOGGER.log(Level.WARNING, "Either Page Flow TopComponent of Faces Config DataObject is null" ); //NOI18N
        }
    }

    public void openNavigationCase(NavigationCaseEdge navCaseEdge) {

        final NavigationCase navCase = getNavCase2NavCaseEdge(navCaseEdge);
        if (navCase == null) {
            // XXX #152419 Possible NPE.
            log("There is null NavigationCase for NavigationCaseEdge, navCaseEdge=" + navCaseEdge); // NOI18N
            return;
        }

        //FileObject fobj = NbEditorUtilities.getFileObject(navCase.getModel().getDocument());
        //DataObject dobj = DataObject.find(fobj);
        DataObject dobj = getConfigDataObject();
        if (dobj != null) {
            final EditCookie ec2 = dobj.getCookie(EditCookie.class);
            if (ec2 != null) {

                final EditorCookie.Observable ec = dobj.getCookie(EditorCookie.Observable.class);
                if (ec != null) {
                    StatusDisplayer.getDefault().setStatusText("otvirani"); // NOI18N
                    EventQueue.invokeLater(new Runnable() {

                        public void run() {

                            ec2.edit();
                            JEditorPane[] panes = ec.getOpenedPanes();
                            if (panes != null && panes.length > 0) {
                                openPane(panes[0], navCase);
                            //ec.open();
                            } else {
                                ec.addPropertyChangeListener(new PropertyChangeListener() {

                                    public void propertyChange(PropertyChangeEvent evt) {
                                        if (EditorCookie.Observable.PROP_OPENED_PANES.equals(evt.getPropertyName())) {
                                            final JEditorPane[] panes = ec.getOpenedPanes();
                                            if (panes != null && panes.length > 0) {
                                                openPane(panes[0], navCase);
                                            }
                                            ec.removePropertyChangeListener(this);
                                        }
                                    }
                                });
                                ec.open();
                            }
                        }
                    });
                }
            }
        }
    }

    private void openPane(JEditorPane pane, NavigationCase navCase) {
        final Cursor editCursor = pane.getCursor();
        pane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        pane.setCaretPosition(navCase.findPosition() + 2);
        pane.setCursor(editCursor);
        StatusDisplayer.getDefault().setStatusText(""); //NOI18N
    }

    /**
     * Moved this out of Page.java so that WebFolderListener also has an opportunity  to
     * access the providers so that it can listen and decide wether or not to update
     * contents should be updated given a page.
     **/
    public static final Collection<? extends PageContentModelProvider> getPageContentModelProviders() {
        Lookup.Template<PageContentModelProvider> templ = new Lookup.Template<PageContentModelProvider>(PageContentModelProvider.class);
        final Lookup.Result<PageContentModelProvider> result = Lookup.getDefault().lookup(templ);
        Collection<? extends PageContentModelProvider> impls = result.allInstances();
        return impls;
    }

    static class TestAccessor {

        static Collection<String> getPagesInFacesConfig(final PageFlowController controller) {
            Set<NavigationRule> rules = TestAccessor.getAllNavigationRules(controller);
            return controller.getFacesConfigPageNames(rules);
        }

        static Collection<FileObject> getAllRelevantFiles(PageFlowController controller) {
            return controller.getAllProjectRelevantFilesObjects();
        }

        static Set<NavigationRule> getAllNavigationRules(PageFlowController controller) {
            return controller.navRule2String.keySet();
        }

        static Set<NavigationCase> getAllNavigationCases(PageFlowController controller) {
            return controller.navCase2NavCaseEdge.keySet();
        }
    }


    private static void log(String message) {
        Logger.getLogger(PageFlowController.class.getName()).log(Level.INFO, message);
    }
}
