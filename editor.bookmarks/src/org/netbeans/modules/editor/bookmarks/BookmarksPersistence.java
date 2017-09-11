/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.editor.bookmarks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Services to update or save bookmarks to persistent format.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class BookmarksPersistence implements PropertyChangeListener, Runnable {
    
    private static final String EDITOR_BOOKMARKS_1_NAMESPACE_URI = "http://www.netbeans.org/ns/editor-bookmarks/1"; // NOI18N
    
    private static final String EDITOR_BOOKMARKS_2_NAMESPACE_URI = "http://www.netbeans.org/ns/editor-bookmarks/2"; // NOI18N

    private static final BookmarksPersistence INSTANCE = new BookmarksPersistence();
    
    private static RequestProcessor RP = new RequestProcessor("Bookmarks loader and saver", 1, false, false); // NOI18N
    
    // -J-Dorg.netbeans.modules.editor.bookmarks.BookmarksPersistence.level=FINE
    private static final Logger LOG = Logger.getLogger(BookmarksPersistence.class.getName());

    public static BookmarksPersistence get() {
        return INSTANCE;
    }

    /**
     * All project instances that have active clients
     * (bookmark list(s) and/or bookmarks view component).
     */
    private final Set<Project> activeProjects;
    
    /**
     * Once a project listener will be fired this list will be used for finding out
     * which projects were just closed and so need their bookmarks to be written to their private.xml.
     */
    private final List<Project> lastOpenProjects;

    /**
     * Whether listening on open projects which also means that the projects
     * were already loaded.
     */
    private boolean openProjectsListening;
    
    private boolean keepOpenProjectsBookmarksLoaded;
    
    private BookmarksPersistence() {
        activeProjects = new HashSet<Project>();
        lastOpenProjects = new ArrayList<Project>();
    }
    
    /**
     * Post a task that will be processed once all open projects will finish loading
     * (and will have AuxiliaryConfiguration available which will allow bookmarks loading).
     *
     * @param run non-null runnable.
     * @return non-null task.
     */
    RequestProcessor.Task postTask(Runnable run) {
        boolean listening;
        synchronized (lastOpenProjects) {
            listening = openProjectsListening;
            openProjectsListening = true;
        }
        if (!listening) {
            // Ensure projects will be loaded before processing other bookmark-related tasks
            RP.post(new Runnable() {
                @Override
                public void run() {
                    Future<Project[]> openProjectsFuture = OpenProjects.getDefault().openProjects();
                    try {
                        // Wait until projects get loaded
                        openProjectsFuture.get();
                    } catch (InterruptedException ex) {
                        // Stay silent. Exceptions.printStackTrace(ex);
                    } catch (ExecutionException ex) {
                        // Stay silent. Exceptions.printStackTrace(ex);
                    }

                    // Start listening on open projects
                    OpenProjects openProjects = OpenProjects.getDefault();
                    List<Project> projects = Arrays.asList(openProjects.getOpenProjects());
                    synchronized (lastOpenProjects) {
                        lastOpenProjects.addAll(projects);
                    }
                    openProjects.addPropertyChangeListener(BookmarksPersistence.this);
                }
            });
        }

        return RP.post(run);
    }
    
    /**
     * Provides access to the bookmarks load/save RP.
     */
    RequestProcessor.Task createTask(Runnable r) {
        return RP.create(r, true);
    }

    public void endProjectsListening() {
        boolean listening;
        synchronized (lastOpenProjects) {
            keepOpenProjectsBookmarksLoaded = false;
            listening = openProjectsListening;
        }
        if (listening) {
            OpenProjects.getDefault().removePropertyChangeListener (this);
            postTask(new Runnable() {
                @Override
                public void run() {
                    BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
                    try {
                        List<ProjectBookmarks> activeProjectBookmarks = lockedBookmarkManager.activeProjectBookmarks();
                        for (ProjectBookmarks projectBookmarks : activeProjectBookmarks) {
                            URI projectURI = projectBookmarks.getProjectURI();
                            Project project = BookmarkUtils.findProject(projectURI);
                            if (project != null) {
                                saveProjectBookmarks(project, projectBookmarks);
                                lockedBookmarkManager.releaseProjectBookmarks(projectBookmarks);
                            }
                        }
                    } finally {
                        lockedBookmarkManager.unlock();
                    }
                    synchronized (lastOpenProjects) {
                        activeProjects.clear();
                    }
                }
            });
        } // else: When not listening no tasks were done yet
    }

    List<Project> lastOpenProjects() {
        synchronized (lastOpenProjects) {
            return new ArrayList<Project>(lastOpenProjects);
        }
    }

    void keepOpenProjectsBookmarksLoaded() {
        synchronized (lastOpenProjects) {
            keepOpenProjectsBookmarksLoaded = true;
        }
        // Post task to ensure open projects listening (and load currently opened projects)
        postTask(new Runnable() {
            @Override
            public void run() {
                loadBookmarksUnderLock(lastOpenProjects());
            }
        });
    }

    private void loadBookmarksUnderLock(List<Project> prjs) {
        BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
        try {
            for (Project project : prjs) {
                // Force project's bookmarks loading
                lockedBookmarkManager.getProjectBookmarks(project, true, true);
            }
        } finally {
            lockedBookmarkManager.unlock();
        }
    }
    
    private void checkRPThread() {
        assert (RP.isRequestProcessorThread()) : "Not a dedicated RequestProcessor thread."; // NOI18N
    }
    
    void loadProjectBookmarks(final ProjectBookmarks projectBookmarks, final Project project) {
        // Loading of project bookmarks should only be done in (dedicated) RP
        checkRPThread();
        if (projectBookmarks.isLoaded()) {
            return;
        }
        projectBookmarks.markLoaded();

        final BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
        try {
            ProjectManager.mutex().readAccess(new Runnable() {
                @Override
                public void run() {
                    loadProjectBookmarksImpl(lockedBookmarkManager, projectBookmarks, project);
                }
            });
        } finally {
            lockedBookmarkManager.unlock();
        }
    }

    private void loadProjectBookmarksImpl(BookmarkManager lockedBookmarkManager,
            ProjectBookmarks projectBookmarks, Project project)
    {
        int version = 2;
        URI projectURI = project.getProjectDirectory().toURI();
        AuxiliaryConfiguration ac = ProjectUtils.getAuxiliaryConfiguration (project);
        if (LOG.isLoggable(Level.FINE)) {
            int pbIHC = System.identityHashCode(projectBookmarks);
            int projectHashCode = (project != null) ? project.hashCode() : 0;
            int ihc = System.identityHashCode(project);
            LOG.log(Level.FINE, "Loading ProjectBookmarks(IHC={0,number,#}) for project={1} " +
                    "hashCode={2,number,#}, IHC={3,number,#}\n  URI={4}, AuxiliaryConfiguration: {5}\n",
                    new Object[]{ pbIHC, project, projectHashCode, ihc, projectURI, ac});
        }
        Element bookmarksElement = ac.getConfigurationFragment(
            "editor-bookmarks",
            EDITOR_BOOKMARKS_2_NAMESPACE_URI,
            false
        );
        int lastBookmarkId = 0;
        if (bookmarksElement != null) {
            String lastBookmarkIdText = bookmarksElement.getAttribute("lastBookmarkId");
            try {
                lastBookmarkId = Integer.parseInt(lastBookmarkIdText);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "  Found lastBookmarkId={0}\n", lastBookmarkId);
                }
            } catch (NumberFormatException ex) {
                // Leave lastBookmarkId == 0
            }

        } else { // Attempt older version
            version = 1;
            bookmarksElement = ac.getConfigurationFragment(
                "editor-bookmarks",
                EDITOR_BOOKMARKS_1_NAMESPACE_URI,
                false
            );
            if (bookmarksElement == null) {
                return;
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "  Will use lastBookmarkId={0}\n", lastBookmarkId);
        }

        projectBookmarks.setLastBookmarkId(lastBookmarkId);
        Node fileElem = skipNonElementNode (bookmarksElement.getFirstChild ());
        while (fileElem != null) {
            assert "file".equals (fileElem.getNodeName ());
            Node urlElem = skipNonElementNode (fileElem.getFirstChild ());
            assert "url".equals (urlElem.getNodeName ());
            Node lineOrBookmarkElem = skipNonElementNode (urlElem.getNextSibling ());
            ArrayList<BookmarkInfo> bookmarks = new ArrayList<BookmarkInfo>();
            while (lineOrBookmarkElem != null) {
                String nodeName = lineOrBookmarkElem.getNodeName();
                try {
                    BookmarkInfo bookmarkInfo;
                    if (version == 2) {
                        assert "bookmark".equals(nodeName);
                        assert (lineOrBookmarkElem.getNodeType() == Node.ELEMENT_NODE);
                        Element bookmarkElem = (Element) lineOrBookmarkElem;
                        int id = -1;
                        if (bookmarkElem.hasAttributes()) {
                            String idText = bookmarkElem.getAttribute("id");
                            try {
                                id = Integer.parseInt(idText);
                                projectBookmarks.ensureBookmarkIdIsSkipped(id);
                                if (LOG.isLoggable(Level.FINER)) {
                                    LOG.log(Level.FINER, "  id={0}\n", id);
                                }
                            } catch (NumberFormatException ex) {
                                // Leave id == -1
                            }
                        }
                        if (id == -1) {
                            id = projectBookmarks.generateBookmarkId();
                            if (LOG.isLoggable(Level.FINER)) {
                                LOG.log(Level.FINER, "  id-generated:{0}\n", id);
                            }
                        }
                        Node nameElem = skipNonElementNode(lineOrBookmarkElem.getFirstChild());
                        assert "name".equals(nameElem.getNodeName());
                        Node nameTextNode = nameElem.getFirstChild();
                        String name = (nameTextNode != null) ? nameTextNode.getNodeValue() : "";
                        Node lineElem = skipNonElementNode(nameElem.getNextSibling());
                        int lineIndex = parseLineIndex(lineElem);
                        Node keyElem = skipNonElementNode(lineElem.getNextSibling());
                        Node keyTextNode = keyElem.getFirstChild();
                        String key = (keyTextNode != null) ? keyTextNode.getNodeValue() : "";
                        bookmarkInfo = BookmarkInfo.create(id, name, lineIndex, key);
                    } else {
                        int lineIndex = parseLineIndex(lineOrBookmarkElem);
                        int id = projectBookmarks.getLastBookmarkId();
                        bookmarkInfo = BookmarkInfo.create(id, "", lineIndex, "");
                    }
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "    Bookmark found: {0}\n", bookmarkInfo);
                    }
                    bookmarks.add(bookmarkInfo);

                } catch (DOMException e) {
                    ErrorManager.getDefault().notify(e);
                } catch (NumberFormatException e) {
                    ErrorManager.getDefault().notify(e);
                }
                lineOrBookmarkElem = skipNonElementNode(lineOrBookmarkElem.getNextSibling());
            }
            bookmarks.trimToSize();

            try {
                try {
                    Node urlElemText = urlElem.getFirstChild();
                    String relOrAbsURLString = urlElemText.getNodeValue();
                    URI uri = new URI(relOrAbsURLString);
                    FileBookmarks fileBookmarks = lockedBookmarkManager.getOrAddFileBookmarks(projectBookmarks, uri);
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE,
                                "  File URI: {0} found and paired with preceding bookmarks\n", uri);
                    }
                    for (BookmarkInfo bookmark : bookmarks) {
                        lockedBookmarkManager.addBookmark(fileBookmarks, bookmark);
                    }
                } catch (URISyntaxException e) {
                    ErrorManager.getDefault().notify(e);
                }
            } catch (DOMException e) {
                ErrorManager.getDefault ().notify (e);
            }

            fileElem = skipNonElementNode (fileElem.getNextSibling ());
        } // while element
    }

    static int parseLineIndex(Node lineElem) {
        assert "line".equals(lineElem.getNodeName());
        // Fetch the line number from the node
        Node lineElemText = lineElem.getFirstChild();
        String lineIndexString = lineElemText.getNodeValue();
        return Integer.parseInt(lineIndexString);
    }
    
    private static Node skipNonElementNode (Node node) {
        while (node != null && node.getNodeType () != Node.ELEMENT_NODE) {
            node = node.getNextSibling ();
        }
        return node;
    }
    
    private void saveProjectBookmarks(Project project, ProjectBookmarks projectBookmarks) {
        // Only save bookmarks in dedicated RP
        checkRPThread();
        
        if (!projectBookmarks.isModified()) {
            return;
        }
        if (!projectBookmarks.isLoaded()) { // saving of bookmarks done synchronously => check that loading finished
            return;
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Saving bookmarks for project={0} ...\n", projectBookmarks.getProjectURI());
        }
        if (project == null) { // Bookmarks that do not belong to any project
            return;
        }
        if (!ProjectManager.getDefault().isValid(project)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "  Bookmarks not saved! Project is not valid.\n");
            }
            return; // cannot modify it now anyway
        }
        AuxiliaryConfiguration auxiliaryConfiguration = ProjectUtils.getAuxiliaryConfiguration(project);
        boolean legacy = false;
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document document = builder.newDocument();
            String namespaceURI = legacy
                    ? EDITOR_BOOKMARKS_1_NAMESPACE_URI
                    : EDITOR_BOOKMARKS_2_NAMESPACE_URI;
            Element bookmarksElem = document.createElementNS(namespaceURI, "editor-bookmarks");
            bookmarksElem.setAttribute("lastBookmarkId", String.valueOf(projectBookmarks.getLastBookmarkId()));
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "  lastBookmarkId: {0}\n", projectBookmarks.getLastBookmarkId());
            }
            for (FileBookmarks fileBookmarks : projectBookmarks.getFileBookmarks()) {
                List<BookmarkInfo> bookmarkInfos = fileBookmarks.getBookmarks();
                if (bookmarkInfos.isEmpty()) {
                    continue;
                }
                Element fileElem = document.createElementNS(namespaceURI, "file");
                Element urlElem = document.createElementNS(namespaceURI, "url");
                String uri = fileBookmarks.getRelativeURI().toString();
                urlElem.appendChild (document.createTextNode(uri));
                fileElem.appendChild (urlElem);
                for (BookmarkInfo bookmarkInfo : bookmarkInfos) {
                    if (legacy) { // Use legacy mode
                        Element lineElem = document.createElementNS(namespaceURI, "line");
                        lineElem.appendChild(document.createTextNode(Integer.toString(bookmarkInfo.getLineIndex())));
                        fileElem.appendChild(lineElem);
                    } else { // New mode
                        Element nameElem = document.createElementNS(namespaceURI, "name");
                        nameElem.appendChild(document.createTextNode(bookmarkInfo.getName()));
                        Element lineElem = document.createElementNS(namespaceURI, "line");
                        lineElem.appendChild(document.createTextNode (Integer.toString(bookmarkInfo.getLineIndex())));
                        Element keyElem = document.createElementNS(namespaceURI, "key");
                        keyElem.appendChild(document.createTextNode(String.valueOf(bookmarkInfo.getKey())));
                        Element bookmarkElem = document.createElementNS(namespaceURI, "bookmark");
                        bookmarkElem.setAttribute("id", String.valueOf(bookmarkInfo.getId()));
                        bookmarkElem.appendChild(nameElem);
                        bookmarkElem.appendChild(lineElem);
                        bookmarkElem.appendChild(keyElem);
                        fileElem.appendChild(bookmarkElem);
                    }
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "    Bookmark written: {0}\n", bookmarkInfo);
                    }
                }
                bookmarksElem.appendChild(fileElem);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "  File URI written: {0}\n", uri);
                }
            }
            auxiliaryConfiguration.putConfigurationFragment (
                bookmarksElem, false
            );
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "  Bookmarks for project={0} written successfully\n", projectBookmarks.getProjectURI());
            }
        } catch (ParserConfigurationException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    @Override
    public void run() {
        final BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
        try {
            ProjectManager.mutex().writeAccess(new Runnable() {
                @Override
                public void run() {
                    List<Project> openProjects = Arrays.asList(OpenProjects.getDefault().getOpenProjects());
                    // lastOpenProjects will contain the just closed projects
                    List<Project> projectsToSave;
                    boolean keepLoaded;
                    synchronized (lastOpenProjects) {
                        lastOpenProjects.removeAll(openProjects);
                        projectsToSave = new ArrayList<Project>(lastOpenProjects);
                        lastOpenProjects.clear();
                        lastOpenProjects.addAll(openProjects);
                        keepLoaded = keepOpenProjectsBookmarksLoaded;
                    }

                    for (Project p : projectsToSave) {
                        ProjectBookmarks projectBookmarks = lockedBookmarkManager.getProjectBookmarks(p, false, false);
                        if (projectBookmarks != null && !projectBookmarks.hasActiveClients()) {
                            saveProjectBookmarks(p, projectBookmarks); // Write into private.xml under project's mutex acquired
                            // Releasing currently disabled (open projects change e.g. at startup so releasing is undesirable in such case)
                            // lockedBookmarkManager.releaseProjectBookmarks(projectBookmarks);
                        }
                    }
                    if (keepLoaded) {
                        postTask(new Runnable() {
                            @Override
                            public void run() {
                                // Use 
                                loadBookmarksUnderLock(lastOpenProjects());
                            }
                        });
                    }
                }
            });
        } finally {
            lockedBookmarkManager.unlock();
        }
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        postTask(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("Opened Projects:\n"); // NOI18N
        synchronized (lastOpenProjects) {
            for (Project p : lastOpenProjects) {
                sb.append("Project ").append(p).append('\n'); // NOI18N
            }
        }
        sb.append("------------------------"); // NOI18N
        return sb.toString();
    }

}

