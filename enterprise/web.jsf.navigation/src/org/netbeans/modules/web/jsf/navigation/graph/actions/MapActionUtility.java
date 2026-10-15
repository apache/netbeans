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

package org.netbeans.modules.web.jsf.navigation.graph.actions;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JOptionPane;
import org.netbeans.modules.web.jsf.navigation.PageFlowController;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import java.util.List;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectDecorator;
import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.InplaceEditorProvider.EditorController;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.vmd.VMDConnectionWidget;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.modules.web.jsf.navigation.NavigationCaseEdge;
import org.netbeans.modules.web.jsf.navigation.Page;
import org.netbeans.modules.web.jsf.navigation.Pin;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowSceneElement;
import org.netbeans.modules.web.jsf.navigation.graph.layout.SceneElementComparator;
import org.openide.cookies.OpenCookie;

/**
 *
 * @author joelle
 */
public class MapActionUtility {

    /** Creates a new instance of MapActionUtility */
    private MapActionUtility() {
    }

    public static ActionMap initActionMap() {
        ActionMap actionMap = new ActionMap();
        // Install the actions
        actionMap.put("handleTab", handleTabAction);
        actionMap.put("handleCtrlTab", handleCtrlTab);
        actionMap.put("handleEscape", handleEscape);

        actionMap.put("handleLinkStart", handleLinkStart);
        actionMap.put("handleLinkEnd", handleLinkEnd);
        //
        actionMap.put("handleZoomPage", handleZoomPage);
        actionMap.put("handleUnZoomPage", handleZoomPage);
        actionMap.put("handleOpenPage", handleOpenPage);
        //
        //        actionMap.put("handleNewWebForm", new TestAction("handleNewWebForm"));
        //
        actionMap.put("handleLeftArrowKey", handleCtrlTab);
        actionMap.put("handleRightArrowKey", handleTabAction);
        actionMap.put("handleUpArrowKey", handleUpArrow);
        actionMap.put("handleDownArrowKey", handleDownArrow);


        actionMap.put("handleRename", handleRename);
        actionMap.put("handlePopup", handlePopup);

        actionMap.put("handleLeftCtrlArrowKey", handleLeftCtrlArrowKey);
        actionMap.put("handleRightCtrlArrowKey", handleRightCtrlArrowKey);
        actionMap.put("handleDownCtrlArrowKey", handleDownCtrlArrowKey);
        actionMap.put("handleUpCtrlArrowKey", handleUpCtrlArrowKey);
        return actionMap;
    }

    public static InputMap initInputMap() {
        InputMap inputMap = new InputMap();
        // Tab Key
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "handleTab");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK), "handleCtrlTab");
        // Esc Key
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "handleEscape");
        //
        //        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A,InputEvent.CTRL_MASK), "handleNewWebForm");
        //
        //Lower Case s,e,z,u
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "handleLinkStart");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "handleLinkEnd");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0), "handleZoomPage");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0), "handleUnZoomPage");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "handlePopup");
        //
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "handleOpenPage");

        // Upper Case S,E,Z,U
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_MASK), "handleLinkStart");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.SHIFT_MASK), "handleLinkEnd");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.SHIFT_MASK), "handleZoomPage");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.SHIFT_MASK), "handleUnZoomPage");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.SHIFT_MASK), "handlePopup");

        // Upper and Lower Case R (rename)
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK), "handleRename");

        //        // Non Numeric Key Pad arrow keys
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "handleLeftArrowKey");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "handleRightArrowKey");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "handleUpArrowKey");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "handleDownArrowKey");
        //
        // Numeric Key Pad arrow keys
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0), "handleLeftArrowKey");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0), "handleRightArrowKey");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), "handleUpArrowKey");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), "handleDownArrowKey");

        // CTRL ARROW to move pages.
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK), "handleLeftCtrlArrowKey");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, InputEvent.CTRL_MASK), "handleLeftCtrlArrowKey");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK), "handleRightCtrlArrowKey");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, InputEvent.CTRL_MASK), "handleRightCtrlArrowKey");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK), "handleUpCtrlArrowKey");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK), "handleUpCtrlArrowKey");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK), "handleDownCtrlArrowKey");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK), "handleDownCtrlArrowKey");
        //        // SHIFT + F10
        //        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10,InputEvent.SHIFT_MASK), "handlePopupMenu");
        //
        //
        //        //Add File
        //        inputMap.put(KeyStroke.getKeyStroke( KeyEvent.VK_A, 0, false), "handleNewWebForm");
        //        //DELETE
        //        inputMap.put(KeyStroke.getKeyStroke( KeyEvent.VK_DELETE , 0), "handleDeleteKey");
        return inputMap;
    }
    // Handle Escape - cancels the link action
    public static Action handleEscape = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            /* Cancel A11y Linking */
            Object sourceObj = e.getSource();
            if (!(sourceObj instanceof PageFlowScene)) {
                return;
            }
            PageFlowScene scene = (PageFlowScene) sourceObj;
            if (CONNECT_WIDGET != null) {
                CONNECT_WIDGET.removeFromParent();
                CONNECT_WIDGET = null;
            }
        }
    };
    // Handle Rename
    public static Action handleRename = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            /* Cancel A11y Linking */
            Object sourceObj = e.getSource();
            if (sourceObj instanceof PageFlowScene) {
                PageFlowScene scene = (PageFlowScene) sourceObj;
                PageFlowSceneElement selElement = getSelectedPageFlowSceneElement(scene);
                Widget widget = scene.findWidget(selElement);
                assert widget != null;
                EditorController controller;
                if (widget instanceof VMDNodeWidget) {
                    LabelWidget labelWidget = ((VMDNodeWidget) widget).getNodeNameWidget();
                    controller = findEditorController(labelWidget.getActions().getActions());
                    if (controller != null) {
                        controller.openEditor(labelWidget);
                    }
                } else if (widget instanceof VMDConnectionWidget) {
                    List<Widget> childWidgets = widget.getChildren();
                    for (Widget childWidget : childWidgets) {
                        if (childWidget instanceof LabelWidget) {
                            controller = findEditorController(childWidget.getActions().getActions());
                            if (controller != null) {
                                controller.openEditor(childWidget);
                            }
                        }
                    }
                }
            }
        }

        public EditorController findEditorController(List<WidgetAction> actionList) {
            for (WidgetAction action : actionList) {
                if (action instanceof InplaceEditorProvider.EditorController) {
                    EditorController controller = ActionFactory.getInplaceEditorController(action);
                    return controller;
                }
            }
            return null;
        }
    };
    public static final Action handleTabAction = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            boolean reverse = false;
            handleTabActionEvent(e, reverse);
        }
    };
    public static final Action handleCtrlTab = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            boolean reverse = true;
            handleTabActionEvent(e, reverse);
        }
    };

    private static final void handleTabActionEvent(ActionEvent e, boolean reverse) {

        Object sourceObj = e.getSource();
        if (!(sourceObj instanceof PageFlowScene)) {
            return;
        }
        PageFlowScene scene = (PageFlowScene) sourceObj;
        handleTab(scene, reverse);
    }

    private static final void handleTab(PageFlowScene scene, boolean reverse) {

        PageFlowSceneElement nextElement = SceneElementComparator.getNextSelectableElement(scene, reverse, true, true, false);
        if (nextElement != null) {
            if (CONNECT_WIDGET != null && scene.getConnectionLayer().getChildren().contains(CONNECT_WIDGET)) {
                Anchor targetAnchor;
                if (nextElement instanceof Page) {
                    assert CONNECT_DECORATOR_DEFAULT != null;
                    targetAnchor = CONNECT_DECORATOR_DEFAULT.createTargetAnchor(scene.findWidget(nextElement));
                    CONNECT_WIDGET.setTargetAnchor(targetAnchor);
                    scene.validate();
                } else if (nextElement instanceof Pin) {
                    Widget pageWidget = scene.findWidget(((Pin) nextElement).getPage());
                    targetAnchor = CONNECT_DECORATOR_DEFAULT.createTargetAnchor(pageWidget);
                    CONNECT_WIDGET.setTargetAnchor(targetAnchor);
                    scene.validate();
                }
            }
            Set<PageFlowSceneElement> set = new HashSet<PageFlowSceneElement>();
            set.add(nextElement);
            scene.setHoveredObject(nextElement); //Do this because the popup action is looking for hovered.
            scene.setSelectedObjects(set);
            scene.setFocusedObject(nextElement);
        } else {
            scene.setSelectedObjects(new HashSet<>());
            scene.setHoveredObject(null); //Not sure if I can do this yet.
        }
    }
    public static final Action handleDownArrow = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            Object sourceObj = e.getSource();
            if (!(sourceObj instanceof PageFlowScene)) {
                return;
            }
            PageFlowScene scene = (PageFlowScene) sourceObj;
            boolean reverse = false;
            handleArrow(scene, reverse);
        }
    };

    private static final void handleArrow(PageFlowScene scene, boolean reverse) {
        PageFlowSceneElement nextElement = SceneElementComparator.getNextSelectableElement(scene, reverse, false, false, true);
        if (nextElement != null) {
            if (CONNECT_WIDGET != null && scene.getConnectionLayer().getChildren().contains(CONNECT_WIDGET)) {
                Anchor targetAnchor = null;
                if (nextElement instanceof Page) {
                    assert CONNECT_DECORATOR_DEFAULT != null;
                    targetAnchor = CONNECT_DECORATOR_DEFAULT.createTargetAnchor(scene.findWidget(nextElement));
                } else if (nextElement instanceof Pin) {
                    Widget pageWidget = scene.findWidget(((Pin) nextElement).getPage());
                    targetAnchor = CONNECT_DECORATOR_DEFAULT.createTargetAnchor(pageWidget);
                }
                if (targetAnchor != null) {
                    CONNECT_WIDGET.setTargetAnchor(targetAnchor);
                    scene.validate();
                }
            }
            Set<PageFlowSceneElement> set = new HashSet<PageFlowSceneElement>();
            set.add(nextElement);
            scene.setSelectedObjects(set);
        }
    }
    public static final Action handleUpArrow = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            Object sourceObj = e.getSource();
            if (!(sourceObj instanceof PageFlowScene)) {
                return;
            }
            PageFlowScene scene = (PageFlowScene) sourceObj;
            boolean reverse = true;
            handleArrow(scene, reverse);
        }
    };
    private static ConnectDecorator CONNECT_DECORATOR_DEFAULT = null;
    private static ConnectionWidget CONNECT_WIDGET = null;
// Handle Link Start Key Stroke
    public static Action handleLinkStart = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            Object sourceObj = e.getSource();
            if (sourceObj instanceof PageFlowScene) {
                PageFlowScene scene = (PageFlowScene) sourceObj;
                PageFlowSceneElement selElement = getSelectedPageFlowSceneElement(scene);
                if (selElement != null) {
                    Widget selWidget = null;
                    if (selElement instanceof Page) {
                        selWidget = scene.findWidget(selElement);
                    } else if (selElement instanceof Pin) {
                        selWidget = scene.findWidget((Pin) selElement);
                    }
                    if (selWidget != null) {
                        CONNECT_DECORATOR_DEFAULT = ActionFactory.createDefaultConnectDecorator();
                        CONNECT_DECORATOR_DEFAULT.createTargetAnchor(selWidget);
                        CONNECT_WIDGET = CONNECT_DECORATOR_DEFAULT.createConnectionWidget(scene);
                        CONNECT_WIDGET.setSourceAnchor(CONNECT_DECORATOR_DEFAULT.createSourceAnchor(selWidget));
                        CONNECT_WIDGET.setTargetAnchor(CONNECT_DECORATOR_DEFAULT.createSourceAnchor(selWidget));
                        scene.getConnectionLayer().addChild(CONNECT_WIDGET);
                        scene.validate();
                    }
                }
            }
        }
    };
// Handle Escape - cancels the link action
    public static Action handleLinkEnd = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            /* Cancel A11y Linking */
            Object sourceObj = e.getSource();
            if (sourceObj instanceof PageFlowScene) {
                PageFlowScene scene = (PageFlowScene) sourceObj;
                if (CONNECT_WIDGET != null) {

                    Anchor sourceAnchor = CONNECT_WIDGET.getSourceAnchor();
                    Anchor targetAnchor = CONNECT_WIDGET.getTargetAnchor();
                    if (sourceAnchor != null && targetAnchor != null) {
                        /* Figure out source */
                        Object sourceObject = scene.findObject(CONNECT_WIDGET.getSourceAnchor().getRelatedWidget());
                        Page sourcePage = null;
                        Pin sourcePin = null;
                        if (scene.isPin(sourceObject)) {
                            sourcePin = (Pin) sourceObject;
                            sourcePage = (sourcePin).getPage();
                        }
                        if (scene.isNode(sourceObject)) {
                            sourcePage = (Page) sourceObject;
                        }

                        /* Figure out target */
                        Object targetObject = scene.findObject(CONNECT_WIDGET.getTargetAnchor().getRelatedWidget());
                        Page targetPage = null;
                        if (scene.isPin(targetObject)) {
                            targetPage = ((Pin) targetObject).getPage();
                        }
                        if (scene.isNode(targetObject)) {
                            targetPage = (Page) targetObject;
                        }

                        if (sourcePage != null && targetPage != null) {
                            scene.getPageFlowView().getPageFlowController().createLink(sourcePage, targetPage, sourcePin);
                        }
                        CONNECT_WIDGET.removeFromParent();
                        CONNECT_WIDGET = null;
                        scene.validate();
                    }
                }
            }
        }
    };
    public static final Action handleOpenPage = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            Object sourceObj = e.getSource();
            if (sourceObj instanceof PageFlowScene) {
                PageFlowScene scene = (PageFlowScene) sourceObj;
                Set<Object> selectedObjs = new HashSet<Object>(scene.getSelectedObjects());

                for (Object obj : selectedObjs) {
                    if (obj instanceof PageFlowSceneElement) {
                        openPageFlowSceneElement((PageFlowSceneElement) obj);
                    }
                }
            }
        }
    };

    public static void openPageFlowSceneElement(PageFlowSceneElement element) {
        OpenCookie openCookie = (element).getNode().getCookie(OpenCookie.class);
        if (openCookie != null) {
            openCookie.open();
        }
    }
    public static Action handleNewWebForm = new AbstractAction() {

        PageFlowScene scene;

        public void actionPerformed(ActionEvent e) {
            //            This would work if we wanted to use the wizard.
            //            Action newFileAction = CommonProjectActions.newFileAction();
            //            JOptionPane.showMessageDialog(null, "Source: " + e.getSource());
            Object obj = e.getSource();
            if (obj instanceof PageFlowScene) {
                try {
                    scene = (PageFlowScene) obj;
                    PageFlowController pfc = scene.getPageFlowView().getPageFlowController();

                    FileObject webFileObject = pfc.getWebFolder();

                    String name = FileUtil.findFreeFileName(webFileObject, "Templates/JSP_Servlet/JSP.jsp", "jsp");
                    name = JOptionPane.showInputDialog("Select Page Name", name);

                    createIndexJSP(webFileObject, name);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        /**
         * Creates a JSP in the
         * @param name
         * @throws java.io.IOException
         */
        private void createIndexJSP(FileObject targetFolder, String name) throws IOException {
            //            FileOwnerQuery.getOwner(webFolder)
            //            FileObject webFO = fo.createFolder(DEFAULT_DOC_BASE_FOLDER);
            //            FileObject parentFolder = project.getProjectDirectory();
            //            FileObject webFileObject = parentFolder.getFileObject("web");
            FileObject jspTemplate = FileUtil.getConfigFile("Templates/JSP_Servlet/JSP.jsp"); // NOI18N
            if (jspTemplate == null) {
                return; // Don't know the template
            }

            DataObject mt = DataObject.find(jspTemplate);
            DataFolder webDf = DataFolder.findFolder(targetFolder);
            mt.createFromTemplate(webDf, name); // NOI18N
        }
        private static final String DEFAULT_DOC_BASE_FOLDER = "web"; //NOI18N
    };
// Handle Zoom Key Stroke
    public static final Action handleZoomPage = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            Object obj = e.getSource();
            if (obj instanceof PageFlowScene) {
                PageFlowScene scene = (PageFlowScene) obj;
                Page selPage = getSelectedPage(scene);
                if (selPage != null) {
                    VMDNodeWidget pageWidget = (VMDNodeWidget) scene.findWidget(selPage);
                    if (pageWidget.isMinimized()) {
                        pageWidget.expandWidget();
                    } else {
                        pageWidget.collapseWidget();
                    }
                }
            }
        }
    };
// Handle UnZoom Key Stroke
    public static final Action handleUnZoomPage = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            //            GraphEvent selectedEvent  = graphDocument.getSelectedComponents();
            //            IGraphNode[] selectedNodes = selectedEvent.getNodes();
            //            for( IGraphNode node : selectedNodes ){
            //                if( node instanceof NavigationGraphNode ) {
            //                    ((NavigationGraphNode)node).setZoomed(false);
            //                }
            //            }
        }
    };
    public static Action handlePopup = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            Object obj = e.getSource();
            if (!(obj instanceof PageFlowScene)) {
                return;
            }
            PageFlowScene scene = (PageFlowScene) obj;
            PopupMenuProvider provider = scene.getPopupMenuProvider();
            PageFlowSceneElement selElement = getSelectedPageFlowSceneElement(scene);
            Widget selectedWidget;
            Point popupPoint;
            if (selElement instanceof PageFlowSceneElement) {
                selectedWidget = scene.findWidget(selElement);
                assert selectedWidget != null;

                /* Because you cannot use getLocation on a connectionwidget, I need to grab it's source pin for the location */
                if (selElement instanceof NavigationCaseEdge) {
                    NavigationCaseEdge edge = (NavigationCaseEdge) selElement;
                    VMDConnectionWidget connectionWidget = (VMDConnectionWidget) scene.findWidget(edge);
                    popupPoint = connectionWidget.getFirstControlPoint();
                } else {
                    popupPoint = selectedWidget.getLocation();
                }
            } else {
                Rectangle rectangleScene = scene.getClientArea();
                popupPoint = scene.convertSceneToLocal(new Point(rectangleScene.width / 2, rectangleScene.height / 2));
                selectedWidget = scene;
            }
            assert selectedWidget != null;
            assert popupPoint != null;
            JPopupMenu popupMenu = provider.getPopupMenu(selectedWidget, popupPoint);
            if (popupMenu != null) {
                popupMenu.show(scene.getView(), popupPoint.x, popupPoint.y);
            }
        }
    };
    public static Action handleRightCtrlArrowKey = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {

            Object obj = e.getSource();
            if (obj instanceof PageFlowScene) {
                PageFlowScene scene = (PageFlowScene) obj;
                Page page = getSelectedPage(scene);
                if (page != null) {
                    movePage(scene, page, 5, 0);
                }
            }
        }
    };
    public static Action handleLeftCtrlArrowKey = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {

            Object obj = e.getSource();
            if (obj instanceof PageFlowScene) {
                PageFlowScene scene = (PageFlowScene) obj;
                Page page = getSelectedPage(scene);
                if (page != null) {
                    movePage(scene, page, -5, 0);
                }
            }
        }
    };
    public static Action handleUpCtrlArrowKey = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {

            Object obj = e.getSource();
            if (obj instanceof PageFlowScene) {
                PageFlowScene scene = (PageFlowScene) obj;
                Page page = getSelectedPage(scene);
                if (page != null) {
                    movePage(scene, page, 0, -5);
                }
            }
        }
    };
    public static Action handleDownCtrlArrowKey = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {

            Object obj = e.getSource();
            if (obj instanceof PageFlowScene) {
                PageFlowScene scene = (PageFlowScene) obj;
                Page page = getSelectedPage(scene);
                if (page != null) {
                    movePage(scene, page, 0, 5);
                }
            }
        }
    };

    /**
     * Get the first selected page
     * @return the page selected or null if no page is selected.
     **/
    public static Page getSelectedPage(PageFlowScene scene) {
        assert scene != null;

        PageFlowSceneElement element = getSelectedPageFlowSceneElement(scene);
        if (element instanceof Page) {
            return (Page) element;
        }
        return null;
    }

    /**
     * Get the first selected page element
     * @return a page, navigationedge or pin... null if no page is selected
     **/
    public static PageFlowSceneElement getSelectedPageFlowSceneElement(PageFlowScene scene) {
        assert scene != null;

        for (Object selObj : scene.getSelectedObjects()) {
            return (PageFlowSceneElement) selObj;
        }
        return null;
    }

    /**
     * Move a given page in the scene.  This allows arrow keys to move a page.
     **/
    public static void movePage(PageFlowScene scene, Page page, int horizontal, int vertical) {
        assert scene != null;
        assert page != null;

        Widget pageWidget = scene.findWidget(page);
        Point currentLocation = pageWidget.getLocation();
        currentLocation.translate(horizontal, vertical);
        pageWidget.setPreferredLocation(currentLocation);
        scene.validate();
    }
}
