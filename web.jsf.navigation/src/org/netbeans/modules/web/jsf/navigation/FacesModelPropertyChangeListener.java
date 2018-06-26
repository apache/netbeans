/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.jsf.navigation;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.xml.xam.Model.State;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author joelle
 */
public class FacesModelPropertyChangeListener implements PropertyChangeListener {

    private final PageFlowController pfc;
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.web.jsf.navigation");
    
    public FacesModelPropertyChangeListener(PageFlowController pfc) {
        this.pfc = pfc;
    }

    public void propertyChange(final PropertyChangeEvent ev) {
        LogRecord record = new LogRecord(Level.FINE, "Faces Config File Changed:" + pfc.getConfigDataObject().getName());
        record.setSourceClassName("FacesModelPropertyChangeListener");
        record.setSourceMethodName("propertyChangeEvent.");
        record.setParameters(new Object[]{ev.getPropertyName(), ev.getOldValue(), ev.getNewValue()});
        LOGGER.log(record);
        if (pfc.getView().isShowing()) {
            runEventNow(ev);
        } else {
            setGraphDirty(ev);
        }

    }
    
    private boolean isWellFormed = true;
    private final void setGraphDirty(PropertyChangeEvent ev) {
        if (!(ev.getPropertyName().equals("managed-bean-class")) && !(ev.getPropertyName().equals("managed-bean-name")) && !(ev.getNewValue() == State.NOT_SYNCED)) {

            if (ev.getOldValue() == State.NOT_WELL_FORMED) {
                isWellFormed = true;
            } else if (ev.getNewValue() == State.NOT_WELL_FORMED) {
                isWellFormed = false;
            }
            pfc.setGraphDirtyWellFormed(isWellFormed);
        }
    }
    
    protected final void runEventNow(final PropertyChangeEvent ev) {
        if (ev.getOldValue() == State.NOT_WELL_FORMED) {
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    PageFlowView view = pfc.getView();
                    if (view == null) {
                        // XXX #145996 PageFlowController got destroyed in the meantime, revise that.
                        return;
                    }
                    view.removeUserMalFormedFacesConfig(); // Does clear graph take care of this?
                    setupGraph(ev);
                }
            });
        } else if (ev.getPropertyName().equals("managed-bean-class") || ev.getPropertyName().equals("managed-bean-name") || ev.getNewValue() == State.NOT_SYNCED) {
        /* Do Nothing */
        } else if (ev.getPropertyName().equals("navigation-case")) {
            final NavigationCase myNewCase = (NavigationCase) ev.getNewValue(); //Should also check if the old one is null.
            final NavigationCase myOldCase = (NavigationCase) ev.getOldValue();
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    if (pfc.getView() == null) {
                        // XXX #145996 PageFlowController got destroyed in the meantime, revise that.
                        return;
                    }
                    navigationCaseEventHandler(myNewCase, myOldCase);
                }
            });
        } else if (ev.getPropertyName().equals("navigation-rule")) {
            //You can actually do nothing.
            final NavigationRule myNewRule = (NavigationRule) ev.getNewValue();
            final NavigationRule myOldRule = (NavigationRule) ev.getOldValue();
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    if (pfc.getView() == null) {
                        // XXX #145996 PageFlowController got destroyed in the meantime, revise that.
                        return;
                    }
                    navigationRuleEventHandler(myNewRule, myOldRule);
                }
            });
        } else if (ev.getNewValue() == State.NOT_WELL_FORMED) {
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    PageFlowView view = pfc.getView();
                    if (view == null) {
                        // XXX #145996 PageFlowController got destroyed in the meantime, revise that.
                        return;
                    }
                    view.clearGraph();
                    view.warnUserMalFormedFacesConfig();
                }
            });
        } else if (ev.getPropertyName().equals("textContent")) {
            setupGraphInAWTThread(ev);
        } else if (ev.getPropertyName().equals("from-view-id") || ev.getPropertyName().equals("to-view-id")) {

            final String oldName = FacesModelUtility.getViewIdFiltiered((String) ev.getOldValue());
            final String newName = FacesModelUtility.getViewIdFiltiered((String) ev.getNewValue());

            final Object source = ev.getSource();


            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    if (pfc.getView() == null) {
                        // XXX #145996 PageFlowController got destroyed in the meantime, revise that.
                        return;
                    }
                    replaceFromViewIdToViewIdEventHandler(ev, source, oldName, newName);
                //                    replaceFromViewIdToViewIdEventHandler(oldName, newName, refactoringIsLikely);
                }
            });
        } else if (ev.getPropertyName().equals("from-outcome")) {
            final String oldName = (String) ev.getOldValue();
            final String newName = (String) ev.getNewValue();
            final NavigationCase navCase = (NavigationCase) ev.getSource();
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    if (pfc.getView() == null) {
                        // XXX #145996 PageFlowController got destroyed in the meantime, revise that.
                        return;
                    }
                    replaceFromOutcomeEventHandler(navCase, oldName, newName);
                }
            });
        } else {
            // System.out.println("Did not catch this event.: " + ev.getPropertyName());
            setupGraphInAWTThread(ev);
        }

    }

    private final void replaceFromOutcomeEventHandler(NavigationCase navCase, String oldName, String newName) {
        final NavigationCaseEdge edge = pfc.getNavCase2NavCaseEdge(navCase);
        PageFlowView view = pfc.getView();
        view.renameEdgeWidget(edge, newName, oldName);
        view.validateGraph();
    }

//    private final void replaceFromViewIdToViewIdEventHandler(String oldName, String newName, boolean possibleRefactor) {
    private final void replaceFromViewIdToViewIdEventHandler(PropertyChangeEvent ev, Object source, String oldName, String newName) {

        LOGGER.entering("\n\nFacesModelPropertyChangeListener", "replaceFromViewIdToViewIdEventHandler");
        final NavigationCase navCase = source instanceof NavigationCase ? (NavigationCase) source : null;
        final NavigationRule navRule = source instanceof NavigationRule ? (NavigationRule) source : null;

        /* Going to have to do this another day. */
        final Page oldPageNode = pfc.getPageName2Page(oldName);
        final Page newPageNode = pfc.getPageName2Page(newName);
        LOGGER.finest("OldPageNode: " + oldPageNode + "\n" + "NewPageNode: " + newPageNode + "\n");
        boolean isNewPageLinked = false;
        if (newPageNode != null && pfc.getView().getNodeEdges(newPageNode).size() > 0) {
            /* This tells me that the new page already exists.*/
            isNewPageLinked = true;
        }
        /* The below code is only necessary if Refactor calls rename on page before it modifies the file.  This is not the case right now so this never really gets executed
        if( possibleRefactor && !isNewPageLinked && oldPageNode != null && newPageNode != null && newPageNode.isDataNode()){
        // This means that we should replace the new node back to the old because refactoring has likely occured
        Node node = newPageNode.getWrappedNode();
        if ( node != null ) {
        oldPageNode.replaceWrappedNode(node);
        view.removeNodeWithEdges(newPageNode);
        pfc.removePageName2Node(newPageNode, true); // Use this instead of replace because I want it to destroy the old node
        pfc.putPageName2Node(newName, oldPageNode);
        view.resetNodeWidget(oldPageNode, true);
        return;
        }
        } */

        if (oldPageNode != null && !pfc.isPageInAnyFacesConfig(oldName) && !isNewPageLinked) {
            LOGGER.finest("CASE 1: OldPage is not null and does not exist in the facesconfig anymore.  This is the firsttime the new page is linked.");
            final FileObject fileObj = pfc.getWebFolder().getFileObject(newName);
            if (fileObj != null && pfc.containsWebFile(fileObj)) {
                try {
                    final Node delegate = DataObject.find(fileObj).getNodeDelegate();
                    oldPageNode.replaceWrappedNode(delegate);
                    pfc.getView().resetNodeWidget(oldPageNode, true);
                    /*** JUST PUT TRUE HERE AS A HOLDER */
                    pfc.getView().validateGraph();
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }

            } else {
                pfc.changeToAbstractNode(oldPageNode, newName);
            }

        } else if (oldPageNode == null && !pfc.isPageInAnyFacesConfig(oldName)) {
            //This means that oldPage has already been removed.  Do nothing.
            LOGGER.finest("CASE 2: OldPage was removed before.");
        } else if (navCase != null && pfc.isPageInAnyFacesConfig(oldName)) {
            LOGGER.finest("CASE 3: NavCase is not null");
            NavigationCaseEdge oldCaseEdge = pfc.removeNavCase2NavCaseEdge(navCase);
            NavigationCaseEdge newCaseEdge = new NavigationCaseEdge(pfc.getView().getPageFlowController(), navCase);
            pfc.putNavCase2NavCaseEdge(navCase, newCaseEdge);
            navigationCaseEdgeEventHandler(newCaseEdge, oldCaseEdge);
        //            if ( !pfc.isPageInFacesConfig(oldName) ){
        //                LOGGER.finest("CASE 3b: OldPage no longer exists in faces config.");
        //                view.removeNodeWithEdges(oldPageNode);
        //                pfc.removePageName2Node(oldPageNode, true);
        //                view.validateGraph();
        //            }
        } else if (navRule != null && pfc.isPageInAnyFacesConfig(oldName)) {
            LOGGER.finest("CASE 4: NavRule is not null.");
            final List<NavigationCase> navCases = navRule.getNavigationCases();
            pfc.putNavRule2String(navRule, FacesModelUtility.getViewIdFiltiered(newName));
            for (NavigationCase thisNavCase : navCases) {
                LOGGER.finest("CASE 4: Redrawing NavRules Case.");
                NavigationCaseEdge newCaseEdge = null;
                NavigationCaseEdge oldCaseEdge = null;
                //                oldCaseEdge = pfc.getCase2Node(thisNavCase);
                oldCaseEdge =
                        pfc.removeNavCase2NavCaseEdge(thisNavCase);
                newCaseEdge =
                        new NavigationCaseEdge(pfc.getView().getPageFlowController(), thisNavCase);
                pfc.putNavCase2NavCaseEdge(navCase, newCaseEdge);
                navigationCaseEdgeEventHandler(newCaseEdge, oldCaseEdge);
            }

        } else {
            LOGGER.finest("CASE 5: Setup Graph");
            setupGraph(ev);
        }

        LOGGER.exiting("FacesModelPropertyChangeListener", "replaceFromViewIdToViewIdEventHandler");
    }
    private static final String NEWLINE = "\n";

    private void setupGraph(PropertyChangeEvent ev) {
        LOGGER.fine(NEWLINE + NEWLINE + "Re-setting Page Flow Editor because of change in faces config xml file." + NEWLINE + "Source Class:  org.netbeans.modules.web.jsf.navigation.FacesModelPropertyChangeListener" + NEWLINE + "Method Name: setupGraph(PropertyChangeEvent ev)" + NEWLINE + "Event: " + ev + NEWLINE + "PropertyName:" + ev.getPropertyName() + NEWLINE + "New Value: " + ev.getNewValue() + NEWLINE + "Old Value: " + ev.getOldValue() + NEWLINE + "Source: " + ev.getSource());

        final LogRecord record = new LogRecord(Level.FINE, "Faces Config Change Re-Setting Graph");
        record.setSourceClassName("org.netbeans.modules.web.jsf.navigation.FacesModelPropertyChangeListener");
        record.setSourceMethodName("setupGraph(PropertyChangeEvent)");
        record.setParameters(new Object[]{ev});
        LOGGER.log(record);
        pfc.setupGraph();
    }

    private final void navigationCaseEventHandler(NavigationCase myNewCase, NavigationCase myOldCase) {
        NavigationCaseEdge newCaseEdge = null;
        NavigationCaseEdge oldCaseEdge = null;
        if (myNewCase != null) {
            newCaseEdge = new NavigationCaseEdge(pfc.getView().getPageFlowController(), myNewCase);
            pfc.putNavCase2NavCaseEdge(myNewCase, newCaseEdge);
        //            pfc.createEdge(newCaseEdge);
        }

        if (myOldCase != null) {
            oldCaseEdge = pfc.removeNavCase2NavCaseEdge(myOldCase);
        }

        navigationCaseEdgeEventHandler(newCaseEdge, oldCaseEdge);
    //        view.validateGraph();
    }

    private final void navigationCaseEdgeEventHandler(NavigationCaseEdge newCaseEdge, NavigationCaseEdge oldCaseEdge) {
        PageFlowView view = pfc.getView();
        if (newCaseEdge != null && newCaseEdge.getToViewId() != null && newCaseEdge.getFromViewId() != null) {
            //            NavigationCaseEdge newCaseEdge = new NavigationCaseEdge(view.getPageFlowController(), newCase);
            //            pfc.putCase2Node(newCase, newCaseEdge);//     case2Node.put(myNewCase, node);
            Page fromPage = pfc.getPageName2Page(newCaseEdge.getFromViewId());
            Page toPage = pfc.getPageName2Page(newCaseEdge.getToViewId());

            if (fromPage == null) {
                fromPage = pfc.createPage(newCaseEdge.getFromViewId());
                view.createNode(fromPage, null, null);
            }

            if (toPage == null) {
                toPage = pfc.createPage(newCaseEdge.getToViewId());
                view.createNode(toPage, null, null);
            }

            pfc.createEdge(newCaseEdge);
        }

        if (oldCaseEdge != null) {
            view.removeEdge(oldCaseEdge);
            removePageIfNoReference(oldCaseEdge.getToViewId());
        }

        view.validateGraph();
    }

    private final void removePageIfNoReference(String page) {
        if (page != null) {
            final Page pageNode = pfc.getPageName2Page(page);
            if (pageNode != null && !pfc.isPageInAnyFacesConfig(page)) {
                if (!pageNode.isDataNode() || pfc.isCurrentScope(PageFlowToolbarUtilities.Scope.SCOPE_FACESCONFIG)) {
                    PageFlowView view = pfc.getView();
                    if (pfc.isCurrentScope(PageFlowToolbarUtilities.Scope.SCOPE_ALL_FACESCONFIG) && !pfc.isPageInAnyFacesConfig(page)) {
                        view.removeNodeWithEdges(pageNode);
                        pfc.removePageName2Page(pageNode, true);
                        view.validateGraph();
                    } else if (!pfc.isCurrentScope(PageFlowToolbarUtilities.Scope.SCOPE_ALL_FACESCONFIG)) {
                        view.removeNodeWithEdges(pageNode);
                        pfc.removePageName2Page(pageNode, true);
                        view.validateGraph();
                    }

                }
            }
        }
    }

    private final void navigationRuleEventHandler(NavigationRule myNewRule, NavigationRule myOldRule) {
        //This has side effects in PageFlowNode destroy.
        //Because it does not consistantly work, I can't account for reactions.
        if (myOldRule != null) {
            final String fromPage = pfc.removeNavRule2String(myOldRule);

            List<NavigationCase> cases = myOldRule.getNavigationCases();
            for (NavigationCase navCase : cases) {
                navigationCaseEventHandler(null, navCase);
            }

            removePageIfNoReference(fromPage);

        //Must account for cases being removed with in the rule.
        }

        if (myNewRule != null) {
            pfc.putNavRule2String(myNewRule, FacesModelUtility.getFromViewIdFiltered(myNewRule));
        }

    }

    private final void setupGraphInAWTThread(final PropertyChangeEvent ev) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (pfc.getView() == null) {
                    // XXX #145996 PageFlowController got destroyed in the meantime, revise that.
                    return;
                }
                setupGraph(ev);
            }
        });
    }
}
