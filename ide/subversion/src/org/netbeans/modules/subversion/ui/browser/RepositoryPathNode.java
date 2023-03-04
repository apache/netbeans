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

package org.netbeans.modules.subversion.ui.browser;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;

import javax.swing.*;
import java.util.Collections;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.ui.search.SvnSearch;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Represents a path in the repository.
 *
 * @author Tomas Stupka
 *
 */
public class RepositoryPathNode extends AbstractNode {

    private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon";                         // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER = "Tree.openIcon";                    // NOI18N
    private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon";              // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.openedIcon"; // NOI18N

    private RepositoryPathEntry entry;
    private final BrowserClient client;
    private boolean repositoryFolder;

    private boolean isListed = false;

    static RepositoryPathNode createRepositoryPathNode(BrowserClient client, RepositoryFile file) {
        return createRepositoryPathNode(client, new RepositoryPathEntry(file, SVNNodeKind.DIR, new SVNRevision(0), null, ""));
    }

    static RepositoryPathNode createRepositoryPathNode(BrowserClient client, RepositoryPathEntry entry) {
        RepositoryPathNode node = new RepositoryPathNode(client, entry, true);
        return node;
    }

    static RepositoryPathNode createPreselectedPathNode(BrowserClient client, RepositoryFile file) {
        return createDelayedExpandNode(client, file);
    }

    static RepositoryPathNode createRepositoryRootNode(BrowserClient client, RepositoryFile file) {
        return createDelayedExpandNode(client, file);
    }

    private static RepositoryPathNode createDelayedExpandNode(BrowserClient client, RepositoryFile file) {
        return new DelayedExpandNode(client, new RepositoryPathEntry(file, SVNNodeKind.DIR, new SVNRevision(0), null, ""), true);
    }

    private RepositoryPathNode(BrowserClient client, RepositoryPathEntry entry, boolean repositoryFolder) {
        super(entry.getSvnNodeKind() == SVNNodeKind.DIR ? new RepositoryPathChildren(client) : Children.LEAF);
        this.entry = entry;
        this.client = client;
        this.repositoryFolder = repositoryFolder;
        initProperties();
    }

    @Override
    public Image getIcon(int type) {
        if (entry.getSvnNodeKind() == SVNNodeKind.DIR) {
            return getTreeFolderIcon(false);
        } else {
            return super.getIcon(type);
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        if (entry.getSvnNodeKind() == SVNNodeKind.DIR) {
            return getTreeFolderIcon(true);
        } else {
            return super.getOpenedIcon(type);
        }
    }

    /**
     * Returns default folder icon as {@link java.awt.Image}. Never returns
     * <code>null</code>.
     * Inspired by org.netbeans.modules.apisupport.project.ui.UIUtil.getTreeFolderIcon()
     *
     * @param opened wheter closed or opened icon should be returned.
     */
    private Image getTreeFolderIcon(boolean opened) {
        Image base;
        Icon baseIcon = UIManager.getIcon(opened ? OPENED_ICON_KEY_UIMANAGER : ICON_KEY_UIMANAGER);
        if (baseIcon != null) {
           base = ImageUtilities.icon2Image(baseIcon);
        } else {
            base = (Image) UIManager.get(opened ? OPENED_ICON_KEY_UIMANAGER_NB : ICON_KEY_UIMANAGER_NB);
            if (base == null) { // fallback to our owns
                base = ImageUtilities.loadImage("org/openide/loaders/defaultFolder.gif");        //NOI18N
            }
        }
        assert base != null;
        return base;
    }

    private void initProperties() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();

        ps.put(new RevisionProperty());
        ps.put(new DateProperty());
        ps.put(new AuthorProperty());
        ps.put(new HistoryProperty());

        sheet.put(ps);
        setSheet(sheet);
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public String getName() {
        if(entry.getRepositoryFile().isRepositoryRoot()) {
            return SvnUtils.decodeToString(entry.getRepositoryFile().getRepositoryUrl());
        } else {
            return entry.getRepositoryFile().getName();
        }
    }

    @Override
    public void setName(String name) {
        String oldName = getName();
        if(!oldName.equals(name)) {
            renameNode (this, name, 0);
            this.fireNameChange(oldName, name);
        }
    }

    private void renameNode (RepositoryPathNode node, String newParentsName, int level) {
        node.entry = new RepositoryPathEntry(
                        node.entry.getRepositoryFile().replaceLastSegment(newParentsName, level),
                        node.entry.getSvnNodeKind(),
                        node.entry.getLastChangedRevision(),
                        node.entry.getLastChangedDate(),
                        node.entry.getLastChangedAuthor()
                    );
        Children childern = node.getChildren();
        Node[] childernNodes = childern.getNodes();
        level++;
        for (Node childernNode : childernNodes) {
            if (childernNode instanceof RepositoryPathNode) {
                renameNode((RepositoryPathNode) childernNode, newParentsName, level);
            }
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        return client.getActions();
    }

    RepositoryPathEntry getEntry() {
        return entry;
    }

    public BrowserClient getClient() {
        return client;
    }

    @Override
    public boolean canRename() {
        return !repositoryFolder;
    }

    private void setRepositoryFolder(boolean bl) {
        repositoryFolder = bl;
    }

    /**
     * List the repository path from entry and sets up the Nodes children with the retrieved values
     */
    void expand() {
        if(isListed) {
            return;
        }
        isListed = true;
        Children ch = getChildren();
        if(ch instanceof RepositoryPathChildren) {
            ((RepositoryPathChildren) getChildren()).listRepositoryPath(entry);
        }
    }

    private static class RepositoryPathChildren extends Children.Keys {

        private final BrowserClient client;
        private Node[] previousNodes = null;

        public RepositoryPathChildren(BrowserClient client) {
            this.client = client;
        }

        @Override
        protected void addNotify() {
            super.addNotify();
        }

        @Override
        protected void removeNotify() {
            setKeys(Collections.emptyList());
            super.removeNotify();
        }

        @Override
        protected Node[] createNodes(Object key) {
            if (key instanceof Node) {
                return new Node[] {(Node) key};
            }

            RepositoryPathEntry entry = (RepositoryPathEntry) key;

            // reuse nodes
            if(previousNodes != null) {
                for(Node n : previousNodes) {
                    if(n instanceof RepositoryPathNode) {
                        if(((RepositoryPathNode)n).entry.getRepositoryFile().getName().equals(entry.getRepositoryFile().getName())) {
                            return null;
                        }
                    }
                }
            }

            Node pathNode = RepositoryPathNode.createRepositoryPathNode(client, entry);
            return new Node[] {pathNode};
        }

        public void listRepositoryPath(final RepositoryPathEntry pathEntry) {
            
            previousNodes = getNodes();
            AbstractNode waitNode = new WaitNode(org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "BK2001")); // NOI18N
            setKeys(Collections.singleton(waitNode));

            RequestProcessor rp = Subversion.getInstance().getRequestProcessor(pathEntry.getRepositoryFile().getRepositoryUrl());
            SvnProgressSupport support = new SvnProgressSupport() {
                @Override
                public void perform() {
                    try {
                        Collection<RepositoryPathEntry> listedEntries = client.listRepositoryPath(pathEntry, this);
                        if(isCanceled()) {
                            return;
                        }

                        Collection<RepositoryPathEntry> previousEntries = getPreviousNodeEntries();
                        // entries to add
                        Collection<RepositoryPathEntry> accepptedNewEntries = new ArrayList<RepositoryPathEntry>();
                        if(listedEntries == null) {
                            // is not a folder in the repository
                            RepositoryPathNode node = (RepositoryPathNode) getNode();
                            node.setRepositoryFolder(false);
                        } else {
                            if(!isCreativeBrowser(client)) {
                                // remove all preselected which are not contained in listedEntries
                                removePreselectedFolders(listedEntries, false);
                            }

                            // collection of nodes which are to be deleted, e.g. shown as folders but in fact they are files
                            Collection<RepositoryPathEntry> deletedEntries = new ArrayList<RepositoryPathEntry>();
                            // keep nodes which were created in the browser
                            for(RepositoryPathEntry listedEntry : listedEntries) {
                                boolean found = false;
                                for(RepositoryPathEntry previousEntry : previousEntries) {
                                    if(previousEntry.getRepositoryFile().getName().equals(listedEntry.getRepositoryFile().getName())) {
                                        if (!listedEntry.getSvnNodeKind().equals(previousEntry.getSvnNodeKind())) {
                                            // remove this false entry and add it as new (it is FILE, not a DIR)
                                            deletedEntries.add(listedEntry);
                                        } else {
                                            found = true;
                                        }
                                        break;
                                    }
                                }
                                if(!found) {
                                    // add only entries which are not yet added as a childnodes
                                    accepptedNewEntries.add(listedEntry);
                                }
                            }
                            // remove entries contained in deletedEntries
                            removePreselectedFolders(deletedEntries, true);
                        }
                        // MUST BE SET TO NULL, otherwise deleted nodes might be reused in createNodes
                        previousNodes = null;
                        setKeys(accepptedNewEntries);

                    } catch (SVNClientException ex) {
                        Collection entries = getPreviousNodeEntries();
                        if(entries.isEmpty()) {
                            setKeys(Collections.singleton(errorNode(ex)));
                        }
                    } finally {
                        previousNodes = null;
                    }
                }
            };
            support.start(rp, pathEntry.getRepositoryFile().getRepositoryUrl(), org.openide.util.NbBundle.getMessage(Browser.class, "BK2001")); // NOI18N
            // expand parents nodes also, this will expand also repository entries along the initial selected path
            // and may hide not allowed preselected paths (e.g. in import, switch etc.)
            Node parentNode = getNode().getParentNode();
            if (parentNode != null) {
                ((RepositoryPathNode)parentNode).expand();
            }
        }

        private Collection<RepositoryPathEntry> getPreviousNodeEntries() {
            List<RepositoryPathEntry> l = new ArrayList<RepositoryPathEntry>();
            if(previousNodes != null) {
                for(Node node : previousNodes) {
                    if(node instanceof RepositoryPathNode) {
                        l.add( ((RepositoryPathNode)node).entry);
                    }
                }
            }
            return l;
        }

        private String getLastPathSegment(RepositoryPathEntry entry) {
            String[] childSegments = entry.getRepositoryFile().getPathSegments();
            return childSegments.length > 0 ? childSegments[childSegments.length-1] : null;
        }

        private static Node errorNode(Exception ex) {
            AbstractNode errorNode = new AbstractNode(Children.LEAF);
            errorNode.setDisplayName(org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "BK2002")); // NOI18N
            errorNode.setShortDescription(ex.getLocalizedMessage());
            return errorNode;
        }

        private boolean isCreativeBrowser(BrowserClient client) {
            Action[] actions = client.getActions();
            for (Action action : actions) {
                if (action instanceof CreateFolderAction) {
                    return true;
                }
            }
            return false;
        }

        /**
         *
         * @param cl
         * @param removeContained if true then entries contained in cl will be removed, otherwise those not contained in cl
         */
        private void removePreselectedFolders(final Collection cl, boolean removeContained) {
            Node[] childNodes = getNodes();
            for(int i=0; i < childNodes.length; i++) {
                if(childNodes[i] instanceof RepositoryPathNode) {
                    String lastChildSegment = getLastPathSegment( ((RepositoryPathNode) childNodes[i]).getEntry() );
                    if(lastChildSegment!=null) {
                        boolean pathExists = false;
                        for(Iterator it = cl.iterator(); it.hasNext(); ) {
                            String lastNewChildSegment = getLastPathSegment((RepositoryPathEntry) it.next());
                            if(lastNewChildSegment!=null) {
                                if(lastNewChildSegment.equals(lastChildSegment)) {
                                    pathExists = true;
                                    break;
                                }
                            }
                        }
                        if(pathExists == removeContained) {
                            remove(new Node[] { childNodes[i] });
                        }
                    }
                }
            }
        }

    }

    static final String PROPERTY_NAME_REVISION = "revision";    // NOI18N
    static final String PROPERTY_NAME_DATE     = "date";        // NOI18N
    static final String PROPERTY_NAME_AUTHOR   = "author";      // NOI18N
    static final String PROPERTY_NAME_HISTORY  = "history";     // NOI18N

    private static final String HISTORY_DISPLAY_NAME = org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "LBL_BrowserTree_History_Name");
    private static final String HISTORY_SHORT_DESC = org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "LBL_BrowserTree_History_Short_Desc");

    private class RevisionProperty extends NodeProperty<Object> {
        public RevisionProperty() {
            super(PROPERTY_NAME_REVISION, Object.class, PROPERTY_NAME_REVISION, PROPERTY_NAME_REVISION);
        }

        @Override
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            SVNRevision r = entry.getLastChangedRevision();
            if (r instanceof SVNRevision.Number) {
                return ((SVNRevision.Number) r).getNumber();
            } else if (r == null) {
                return "";
            } else {
                return r.toString();
            }
        }
    }

    private class DateProperty extends NodeProperty<Object> {

        public DateProperty() {
            super(PROPERTY_NAME_DATE, Object.class, PROPERTY_NAME_DATE, PROPERTY_NAME_DATE);
        }

        @Override
        public Object getValue() {
            Date date = entry.getLastChangedDate();
            return date == null ? "" : date; //NOI18N
        }
    }

    private class AuthorProperty extends NodeProperty<String> {

        public AuthorProperty() {
            super(PROPERTY_NAME_AUTHOR, String.class, PROPERTY_NAME_AUTHOR, PROPERTY_NAME_AUTHOR);
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return entry.getLastChangedAuthor();
        }
    }

    private class HistoryProperty extends PropertySupport.ReadOnly<String> {

        public HistoryProperty() {
            super(PROPERTY_NAME_HISTORY, String.class, HISTORY_DISPLAY_NAME, HISTORY_SHORT_DESC);
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return "";
        }

        @Override
        public String toString() {
            try {
                Object obj = getValue();
                return obj != null ? obj.toString() : "";
            } catch (Exception e) {
                Subversion.LOG.log(Level.INFO, null, e);
                return e.getLocalizedMessage();
            }
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return new HistoryPropertyEditor();
        }
    }

    private abstract class NodeProperty<T> extends PropertySupport.ReadOnly<T> {
        protected NodeProperty(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        @Override
        public String toString() {
            try {
                Object obj = getValue();
                if (obj instanceof Date) {
                    obj = DateFormat.getDateTimeInstance().format((Date) obj);
                }
                return obj != null ? obj.toString() : "";
            } catch (Exception e) {
                Subversion.LOG.log(Level.INFO, null, e);
                return e.getLocalizedMessage();
            }
        }

        @Override
        public boolean canWrite() {
            return false;
        }

        @Override
        public PropertyEditor getPropertyEditor () {
            try {
                return new RevisionPropertyEditor(getValue());
            } catch (Exception e) {
                return new PropertyEditorSupport();
            }
        }
    }

    private class HistoryPropertyEditor extends PropertyEditorSupport {

        public HistoryPropertyEditor() {
            setValue("");
        }

        @Override
        public boolean supportsCustomEditor () {
            return true;
        }

        @Override
        public Component getCustomEditor() {
            SVNRevision revision = entry.getLastChangedRevision();
            SVNUrl repositoryUrl = entry.getRepositoryFile().getRepositoryUrl();
            SVNUrl fileUrl = entry.getRepositoryFile().getFileUrl();
            final SvnSearch svnSearch = new SvnSearch(new RepositoryFile(repositoryUrl, fileUrl, revision));
            return svnSearch.getSearchPanel();
        }
    }

    public static class RepositoryPathEntry {
        private final SVNNodeKind svnNodeKind;
        private final RepositoryFile file;
        private final SVNRevision revision;
        private final Date date;
        private final String author;
        RepositoryPathEntry (RepositoryFile file, SVNNodeKind svnNodeKind, SVNRevision revision, Date date, String author) {
            this.svnNodeKind = svnNodeKind;
            this.file = file;
            this.revision = revision;
            this.date = date;
            this.author = author;
        }
        public SVNNodeKind getSvnNodeKind() {
            return svnNodeKind;
        }
        RepositoryFile getRepositoryFile() {
            return file;
        }
        SVNRevision getLastChangedRevision() {
            return revision;
        }
        Date getLastChangedDate() {
            return date;
        }
        String getLastChangedAuthor() {
            return author != null ? author : "";
        }
    }

    /**
     * Lists it's children from the repository after the second expand in the browser
     */
    private static class DelayedExpandNode extends RepositoryPathNode {
        private final int IGNORE_EXPANDS = 0;
        private int expanded = 0;
        public DelayedExpandNode(BrowserClient client, RepositoryPathEntry entry, boolean repositoryFolder) {
            super(client, entry, repositoryFolder);
        }
        @Override
        void expand() {
            try {
                if(expanded < IGNORE_EXPANDS) {
                    return;
                }
                super.expand();
            } finally {
                if(expanded <= IGNORE_EXPANDS) {
                    ++expanded;
                }
            }
        }
    }
    
    private static class RevisionPropertyEditor extends PropertyEditorSupport {

        private static final JLabel renderer = new JLabel();

        static {
            renderer.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        }

        public RevisionPropertyEditor(Object value) {
            setValue(value);
        }

        @Override
        public void paintValue (Graphics gfx, Rectangle box) {
            renderer.setForeground(gfx.getColor());
            Object val = getValue();
            if (val instanceof Date) {
                val = DateFormat.getDateTimeInstance().format((Date) val);
            }
            renderer.setText(val == null ? "" : val.toString());
            renderer.setBounds(box);
            renderer.paint(gfx);
        }

        @Override
        public boolean isPaintable () {
            return true;
        }
    }

}
