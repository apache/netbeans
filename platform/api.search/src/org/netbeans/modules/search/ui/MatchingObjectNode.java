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
package org.netbeans.modules.search.ui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.UIManager;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.search.MatchingObject;
import org.netbeans.modules.search.MatchingObject.InvalidityStatus;
import org.netbeans.modules.search.Removable;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author jhavlin
 */
public class MatchingObjectNode extends AbstractNode implements Removable {

    @StaticResource
    private static final String INVALID_ICON =
            "org/netbeans/modules/search/res/invalid.png";              //NOI18N
    @StaticResource
    private static final String WARNING_ICON =
            "org/netbeans/modules/search/res/warning.gif";              //NOI18N
    private static RequestProcessor RP = new RequestProcessor(
            "MatchingObjectNode");                                      //NOI18N

    private MatchingObject matchingObject;
    private Node original;
    private OrigNodeListener origNodeListener;
    private boolean valid = true;
    private PropertyChangeListener validityListener;
    private PropertyChangeListener selectionListener;
    private final boolean replacing;
    PropertySet[] propertySets;

    public MatchingObjectNode(Node original,
            org.openide.nodes.Children children, MatchingObject matchingObject,
            final boolean replacing) {
        this(original, children, matchingObject,
                new ReplaceCheckableNode(matchingObject, replacing));
    }

    private MatchingObjectNode(Node original,
            org.openide.nodes.Children children,
            final MatchingObject matchingObject,
            ReplaceCheckableNode checkableNode) {
        super(children, createLookup(matchingObject, checkableNode));
        replacing = checkableNode.isCheckable();
        Parameters.notNull("original", original);                       //NOI18N
        this.matchingObject = matchingObject;
        if (matchingObject.isObjectValid()) {
            this.original = original;
            setValidOriginal();
            origNodeListener = new OrigNodeListener();
            original.addNodeListener(origNodeListener);
        } else {
            setInvalidOriginal();
        }
        validityListener = new ValidityListener(matchingObject);
        matchingObject.addPropertyChangeListener(
                MatchingObject.PROP_INVALIDITY_STATUS,
                validityListener);
        selectionListener = new SelectionListener();
        matchingObject.addPropertyChangeListener(selectionListener);
    }

    private static Lookup createLookup(MatchingObject mo,
            ReplaceCheckableNode checkableNode) {

        // TODO consider using new ProxyLookup(fixedLookup,
        // mo.getDataObject().getLookup()).
        // It could be reasonable, but current solution is simpler and thus
        // should perform better.

        ArrayList<Object> items = new ArrayList<>();
        items.add(mo);
        items.add(checkableNode);
        items.add(mo.getFileObject());
        items.add(mo.getDataObject());
        Openable openable = mo.getDataObject().getLookup().lookup(Openable.class);
        if (openable != null) {
            items.add(openable);
        }
        return Lookups.fixed(items.toArray());
    }

    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        return null;
    }

    @Override
    protected void createPasteTypes(Transferable t, List<PasteType> s) {
    }

    @Override
    public Transferable clipboardCopy() throws IOException {
        return original.clipboardCopy();
    }

    @Override
    public Transferable clipboardCut() throws IOException {
        return original.clipboardCut();
    }

    @Override
    public boolean canCopy() {
        return original.canCopy();
    }

    @Override
    public boolean canCut() {
        return original.canCut();
    }

    @Override
    public boolean canRename() {
        return original.canRename();
    }

    @Override
    public void setName(String s) {
        original.setName(s);
    }

    @Override
    public String getName() {
        return original.getName();
    }

    @Override
    public Transferable drag() throws IOException {
        return UiUtils.DISABLE_TRANSFER;
    }

    @Override
    public Image getIcon(int type) {
        if (valid) {
            return original.getIcon(type);
        } else {
            InvalidityStatus is = matchingObject.getInvalidityStatus();
            String icon = (is == null || is == InvalidityStatus.DELETED)
                    ? INVALID_ICON : WARNING_ICON;
            return ImageUtilities.loadImage(icon);
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        if (!context) {
            Action copyPath = Actions.forID("Edit", //NOI18N
                    "org.netbeans.modules.utilities.CopyPathToClipboard"); //NOI18N
            return new Action[]{
                        SystemAction.get(OpenMatchingObjectsAction.class),
                        replacing && !matchingObject.isObjectValid()
                            ? new RefreshAction(matchingObject) : null,
                        null,
                        copyPath == null ? new CopyPathAction() : copyPath,
                        SystemAction.get(HideResultAction.class),
                        null,
                        SystemAction.get(SelectInAction.class),
                        SystemAction.get(MoreAction.class)
                    };
        } else {
            return new Action[0];
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public String getHtmlDisplayName() {
        if (valid) {
            return original.getHtmlDisplayName();
        } else {
            return getInvalidHtmlDisplayName();
        }
    }

    @Override
    public String getDisplayName() {
        return original.getDisplayName();
    }

    @Override
    public String getShortDescription() {
        return matchingObject.getFileObject().getPath();
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(OpenMatchingObjectsAction.class);
    }

    private void setValidOriginal() {
        fireIconChange();
        fireDisplayNameChange(null, null);
    }

    private void resetValidOriginal() {
        try {
            valid = true;
            original = matchingObject.getDataObject().getNodeDelegate();
            setValidOriginal();
        } catch (NullPointerException npe) {
            setInvalidOriginal();
        }
    }

    private void setInvalidOriginal() {
        if (valid) {
            valid = false;
        } else {
            fireIconChange();
            return; // already invalid
        }
        if (origNodeListener != null && original != null) {
            original.removeNodeListener(origNodeListener);
            origNodeListener = null;
        }
        String oldDisplayName = original == null
                ? null : original.getDisplayName();
        original = new AbstractNode(Children.LEAF) {
            @Override
            public String getHtmlDisplayName() {
                return getInvalidHtmlDisplayName();
            }
        };
        original.setDisplayName(matchingObject.getFileObject().getNameExt());
        fireIconChange();
        fireDisplayNameChange(oldDisplayName,
                matchingObject.getFileObject().getNameExt());
    }

    public void clean() {
        if (original != null && origNodeListener != null && valid) {
            original.removeNodeListener(origNodeListener);
        }
        if (validityListener != null) {
            matchingObject.removePropertyChangeListener(
                    MatchingObject.PROP_INVALIDITY_STATUS, validityListener);
            validityListener = null;
        }
        if (selectionListener != null) {
            matchingObject.removePropertyChangeListener(
                    MatchingObject.PROP_SELECTED, selectionListener);
            selectionListener = null;
        }
    }

    private String getInvalidHtmlDisplayName() {
        Color colorMngr = UIManager.getColor(
                "nb.search.sandbox.regexp.wrong");                      //NOI18N
        Color color = colorMngr == null ? Color.RED : colorMngr;
        String stringHex = Integer.toHexString(color.getRGB());
        String stringClr = stringHex.substring(2, 8);
        return "<html><font color='#" + stringClr + "'>" //NOI18N
                + getDisplayName() + "</font></html>"; //NOI18N
    }

    @Override
    public synchronized PropertySet[] getPropertySets() {

        if (propertySets == null) {

            propertySets = new PropertySet[2];
            PropertySet set = new PropertySet() {
                @Override
                public Property<?>[] getProperties() {
                    Property<?>[] properties = new Property<?>[]{
                        new DetailsCountProperty(),};
                    return properties;
                }
            };
            propertySets[0] = set;
            propertySets[1] = new FileObjectPropertySet(
                    matchingObject.getFileObject());
        }
        return propertySets;
    }

    @Override
    public void remove() {
        // when removing the node, the node's content is removed from model
        this.matchingObject.remove();
    }

    @Override
    public boolean canDestroy() {
        return original.canDestroy();
    }

    @Override
    public void destroy() throws IOException {
        original.destroy();
    }

    /**
     * Check whether the file object is valid and a valid data object can be
     * found for it. It should be checked after original node is destroyed. It
     * does not have to mean the the file was deleted, but also that a module
     * that contain data loader was enabled. In the letter case, the node should
     * be updated for new data object.
     */
    private void checkFileObjectValid() {
        FileObject fo = matchingObject.getFileObject();
        if (fo != null && fo.isValid()) {
            try {
                DataObject reloaded = DataObject.find(fo);
                matchingObject.updateDataObject(reloaded);
                valid = reloaded.isValid();
                if (valid) {
                    EventQueue.invokeLater(this::resetValidOriginal);
                }
            } catch (DataObjectNotFoundException ex) {
                // still invalid, the file was probably really deleted
            }
        }
    }

    private class DetailsCountProperty extends Property<Integer> {

        public DetailsCountProperty() {
            super(Integer.class);
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public Integer getValue() throws IllegalAccessException,
                InvocationTargetException {
            return matchingObject.getDetailsCount();
        }

        @Override
        public boolean canWrite() {
            return false;
        }

        @Override
        public void setValue(Integer val) throws IllegalAccessException,
                IllegalArgumentException, InvocationTargetException {
            throw new UnsupportedOperationException();                  //NOI18N
        }

        @Override
        public String getName() {
            return "detailsCount";                                      //NOI18N
        }
    }

    private class OrigNodeListener implements NodeListener {

        public OrigNodeListener() {
        }

        @Override
        public void childrenAdded(NodeMemberEvent ev) {
        }

        @Override
        public void childrenRemoved(NodeMemberEvent ev) {
        }

        @Override
        public void childrenReordered(NodeReorderEvent ev) {
        }

        @Override
        public void nodeDestroyed(NodeEvent ev) {
            EventQueue.invokeLater(() -> {
                setInvalidOriginal();
                /**
                 * Check if the object is valid again. It can happen when a
                 * module with real data loader is enabled.
                 */
                RP.post(() -> checkFileObjectValid(), 2500);
            });
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            setValidOriginal();
        }
    }

    /**
     * Fallback action for copying of file path if CopyPathToClipboard action is
     * not available (it is in different module).
     */
    private class CopyPathAction extends AbstractAction {

        public CopyPathAction() {
            super(UiUtils.getText("LBL_CopyFilePathAction"));           //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            File f = FileUtil.toFile(
                    matchingObject.getFileObject());
            if (f != null) {
                String path = f.getPath();
                Clipboard clipboard = Lookup.getDefault().lookup(
                        ExClipboard.class);
                if (clipboard == null) {
                    Toolkit toolkit = Toolkit.getDefaultToolkit();
                    if (toolkit != null) {
                        clipboard = toolkit.getSystemClipboard();
                    }
                }
                if (clipboard != null) {
                    StringSelection strSel = new StringSelection(path);
                    clipboard.setContents(strSel, null);
                }
            }
        }
    }

    private class ValidityListener implements PropertyChangeListener {

        private final MatchingObject matchingObject;

        public ValidityListener(MatchingObject matchingObject) {
            this.matchingObject = matchingObject;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            EventQueue.invokeLater(() -> {
                if (matchingObject.getInvalidityStatus() == null) {
                    resetValidOriginal();
                    setChildren(matchingObject.getDetailsChildren(true));
                } else {
                    setInvalidOriginal();
                }
            });
        }
    }

    private class SelectionListener implements PropertyChangeListener {

        public SelectionListener() {
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {

            fireIconChange();
            ResultsOutlineSupport.toggleParentSelected(
                    MatchingObjectNode.this.getParentNode());
        }
    }
}
