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
package org.netbeans.modules.subversion.remote.util;

import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import java.awt.Color;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.remotefs.versioning.api.FileObjectIndexingBridgeProvider;
import org.netbeans.modules.subversion.remote.FileInformation;
import org.netbeans.modules.subversion.remote.FileStatusCache;
import org.netbeans.modules.subversion.remote.OutputLogger;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.SubversionVCS;
import org.netbeans.modules.subversion.remote.SvnFileNode;
import org.netbeans.modules.subversion.remote.SvnModuleConfig;
import org.netbeans.modules.subversion.remote.VersionsCache;
import org.netbeans.modules.subversion.remote.WorkingCopyAttributesCache;
import org.netbeans.modules.subversion.remote.api.ISVNInfo;
import org.netbeans.modules.subversion.remote.api.ISVNLogMessage;
import org.netbeans.modules.subversion.remote.api.ISVNStatus;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNInfoUnversioned;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNStatusUnversioned;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.api.SVNUrlUtils;
import org.netbeans.modules.subversion.remote.client.PropertiesClient;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.client.SvnProgressSupport;
import org.netbeans.modules.subversion.remote.options.AnnotationExpression;
import org.netbeans.modules.subversion.remote.ui.blame.BlameAction;
import org.netbeans.modules.subversion.remote.ui.commit.CommitOptions;
import org.netbeans.modules.subversion.remote.ui.diff.Setup;
import org.netbeans.modules.subversion.remote.ui.history.SearchHistoryAction;
import org.netbeans.modules.remotefs.versioning.api.FileSelector;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.util.ProjectUtilities;
import org.openide.cookies.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.windows.TopComponent;

/**
 * Subversion-specific utilities.
 *
 * 
 */
public class SvnUtils {

    public static final String SVN_ADMIN_DIR = ".svn"; //NOI18N
    public static final String SVN_ENTRIES_DIR = SVN_ADMIN_DIR + "/entries"; //NOI18N
    public static final String SVN_WC_DB = SVN_ADMIN_DIR + "/wc.db"; //NOI18N
    private static final Pattern metadataPattern;

    public static final HashSet<Character> autoEscapedCharacters = new HashSet<>(6);
    static {
        metadataPattern = Pattern.compile(".*\\" + "/" + SVN_ADMIN_DIR.replace(".", "\\.") + "(\\" + "/" + ".*|$)"); //NOI18N
        autoEscapedCharacters.add(';'); //NOI18N
        autoEscapedCharacters.add('?'); //NOI18N
        autoEscapedCharacters.add('#'); //NOI18N
        autoEscapedCharacters.add('%'); //NOI18N
        autoEscapedCharacters.add('['); //NOI18N
        autoEscapedCharacters.add(']'); //NOI18N
        autoEscapedCharacters.add(' '); //NOI18N
    }

    private static Reference<Context>  contextCached = new WeakReference<>(null);
    private static Reference<Node[]> contextNodesCached = new WeakReference<>(null);

    private static final VCSContext.FileFilter svnFileFilter = new VCSContext.FileFilter() {
        @Override
        public boolean accept(VCSFileProxy pathname) {
            if (isAdministrative(pathname)) {
                return false;
            }
            return SharabilityQuery.getSharability(VCSFileProxySupport.toURI(pathname)) != SharabilityQuery.Sharability.NOT_SHARABLE;
        }
    };

    /**
     * Creates annotation format string.
     * @param format format specified by the user, e.g. [{status}]
     * @return modified format, e.g. [{0}]
     */
    public static String createAnnotationFormat(final String format) {
        String string = format;
        string = SvnUtils.skipUnsupportedVariables(string, new String[]{"{status}", "{lock}", "{folder}", "{revision}", "{mime_type}", //NOI18N
                    "{commit_revision}", "{date}", "{author}" }); //NOI18N
        string = string.replaceAll("\\{revision\\}", "\\{0\\}");            // NOI18N
        string = string.replaceAll("\\{status\\}", "\\{1\\}");              // NOI18N
        string = string.replaceAll("\\{folder\\}", "\\{2\\}");              // NOI18N
        string = string.replaceAll("\\{lock\\}", "\\{3\\}");                // NOI18N
        string = string.replaceAll("\\{mime_type\\}", "\\{4\\}");           // NOI18N
        string = string.replaceAll("\\{commit_revision\\}", "\\{5\\}");     // NOI18N
        string = string.replaceAll("\\{date\\}", "\\{6\\}");                // NOI18N
        string = string.replaceAll("\\{author\\}", "\\{7\\}");              // NOI18N
        return string;
    }

    /**
     * Decodes and encodes given URL (e.g. xxx[] -> xxx%5B%5D)
     * @param url url to be encoded
     * @return encoded URL
     * @throws java.net.MalformedURLException
     */
    public static SVNUrl decodeAndEncodeUrl(SVNUrl url) throws MalformedURLException {
        return encodeUrl(decodeToString(url));
    }

    /**
     * Encodes the SVN url to an acceptable format. It encodes all non-standard characters to a '%XX' form.
     * Unescaped characters: '/', ':', '@' (peg revision).
     * @param url url to be encoded
     * @return encoded URL
     * @throws java.net.MalformedURLException encoded URL is of a bad format anyway
     */
    private static SVNUrl encodeUrl(String url) throws MalformedURLException {
        url = url.replace("%20", " "); //NOI18N
        StringBuilder sb = new StringBuilder(url.length());
        for (int i = 0; i < url.length(); ++i) {
            Character c = url.charAt(i);
            if (autoEscapedCharacters.contains(c)) {
                char[] chars = Character.toChars(c);
                for (int j = 0; j < chars.length; ++j) {
                    sb.append('%');
                    sb.append(Integer.toHexString(chars[j]).toUpperCase(Locale.ENGLISH));
                }
            } else {
                sb.append(c);
            }
        }
        return new SVNUrl(sb.toString());
    }

    /**
     * Semantics is similar to {@link org.openide.windows.TopComponent#getActivatedNodes()} except that this
     * method returns File objects instead od Nodes. Every node is examined for Files it represents. File and Folder
     * nodes represent their underlying files or folders. Project nodes are represented by their source groups. Other
     * logical nodes must provide FileObjects in their Lookup.
     *
     * @return File [] array of activated files
     * @param nodes or null (then taken from windowsystem, it may be wrong on editor tabs #66700).
     */
    public static Context getCurrentContext(Node[] nodes) {
        if (nodes == null) {
            nodes = TopComponent.getRegistry().getActivatedNodes();
        }
        if (Arrays.equals(contextNodesCached.get(), nodes)) {
            Context ctx = contextCached.get();
            if (ctx != null) return ctx;
        }
        VCSContext vcsCtx = VCSContext.forNodes(nodes);
        VCSFileProxy topmostManagedAncestor = null;
        for (VCSFileProxy root : vcsCtx.getRootFiles()) {
            topmostManagedAncestor = Subversion.getInstance().getTopmostManagedAncestor(root);
        }
        if (topmostManagedAncestor == null) {
            return Context.Empty;
        }
        Context ctx = new Context(new ArrayList<>(vcsCtx.computeFiles(svnFileFilter)), new ArrayList<>(vcsCtx.getRootFiles()), new ArrayList<>(vcsCtx.getExclusions()));
        contextCached = new WeakReference<>(ctx);
        contextNodesCached = new WeakReference<>(nodes);
        return ctx;
    }


    /**
     * Semantics is similar to {@link org.openide.windows.TopComponent#getActivatedNodes()} except that this
     * method returns File objects instead od Nodes. Every node is examined for Files it represents. File and Folder
     * nodes represent their underlying files or folders. Project nodes are represented by their source groups. Other
     * logical nodes must provide FileObjects in their Lookup.
     *
     * @param nodes null (then taken from windowsystem, it may be wrong on editor tabs #66700).
     * @param includingFileStatus if any activated file does not have this CVS status, an empty array is returned
     * @param includingFolderStatus if any activated folder does not have this CVS status, an empty array is returned
     * @param fromCache if set to <code>true</code> reads status from cache otherwise uses I/O operations.
     * @return File [] array of activated files, or an empty array if any of examined files/folders does not have given status
     */
    public static Context getCurrentContext(Node[] nodes, int includingFileStatus, int includingFolderStatus, boolean fromCache) {
        Context context = getCurrentContext(nodes);
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        VCSFileProxy [] files = context.getRootFiles();
        for (int i = 0; i < files.length; i++) {
            VCSFileProxy file = files[i];
            FileInformation fi = fromCache ? cache.getCachedStatus(file) : cache.getStatus(file);
            int status;
            Boolean isDirectory = null;
            if (fi != null) {
                // status got from the cache and is known or got through I/O
                status = fi.getStatus();
                isDirectory = fi.isDirectory();
            } else {
                // tried to get the cached value but it was not present in the cache
                status = FileInformation.STATUS_VERSIONED_UPTODATE;
            }
            if (Boolean.TRUE.equals(isDirectory) || (isDirectory == null && file.isDirectory())) {
                if ((status & includingFolderStatus) == 0) {
                    return Context.Empty;
                }
            } else {
                if ((status & includingFileStatus) == 0) {
                    return Context.Empty;
                }
            }
        }
        return context;
    }

    /**
     * Semantics is similar to {@link org.openide.windows.TopComponent#getActivatedNodes()} except that this
     * method returns File objects instead od Nodes. Every node is examined for Files it represents. File and Folder
     * nodes represent their underlying files or folders. Project nodes are represented by their source groups. Other
     * logical nodes must provide FileObjects in their Lookup.
     * Does not read statuses from cache, but through I/O, might take a long time to finish.
     *
     * @param nodes null (then taken from windowsystem, it may be wrong on editor tabs #66700).
     * @param includingFileStatus if any activated file does not have this CVS status, an empty array is returned
     * @param includingFolderStatus if any activated folder does not have this CVS status, an empty array is returned
     * @return File [] array of activated files, or an empty array if any of examined files/folders does not have given status
     */
    public static Context getCurrentContext(Node[] nodes, int includingFileStatus, int includingFolderStatus) {
        return getCurrentContext(nodes, includingFileStatus, includingFolderStatus, false);
    }

    /**
     * Validates annotation format text
     * @param format format to be validatet
     * @return <code>true</code> if the format is correct, <code>false</code> otherwise.
     */
    public static boolean isAnnotationFormatValid(final String format) {
        boolean retval = true;
        if (format != null) {
            try {
                new MessageFormat(format);
            } catch (IllegalArgumentException ex) {
                if (Subversion.LOG.isLoggable(Level.FINER)) {
                    Subversion.LOG.log(Level.FINER, "Bad user input - annotation format", ex);
                }
                retval = false;
            }
        }
        return retval;
    }

    /**
     * @return <code>true</code> if
     * <ul>
     *  <li> the node contains a project in its lookup and
     *  <li> the project contains at least one SVN versioned source group
     * </ul>
     * otherwise <code>false</code>.
     * @param project
     * @param checkStatus if set to true, cache.getStatus is called and can take significant amount of time
     */
    public static boolean isVersionedProject(Node node, boolean checkStatus) {
        Lookup lookup = node.getLookup();
        Project project = lookup.lookup(Project.class);
        return isVersionedProject(project, checkStatus);
    }

    /**
     * @return <code>true</code> if
     * <ul>
     *  <li> the project != null and
     *  <li> the project contains at least one SVN versioned source group
     * </ul>
     * otherwise <code>false</code>.
     * @param project
     * @param checkStatus if set to true, cache.getStatus is called and can take significant amount of time
     */
    public static boolean isVersionedProject(Project project, boolean checkStatus) {
        if (project != null) {
            FileStatusCache cache = Subversion.getInstance().getStatusCache();
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup [] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            for (int j = 0; j < sourceGroups.length; j++) {
                SourceGroup sourceGroup = sourceGroups[j];
                VCSFileProxy f = VCSFileProxy.createFileProxy(sourceGroup.getRootFolder());
                if (f != null) {
                    if (checkStatus && (cache.getStatus(f).getStatus() & FileInformation.STATUS_MANAGED) != 0) {
                        return true;
                    } else if (!checkStatus && SvnUtils.isManaged(f)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines all files and folders that belong to a given project and adds them to the supplied Collection.
     *
     * @param filteredFiles destination collection of Files
     * @param project project to examine
     */
    public static void addProjectFiles(Collection<VCSFileProxy> filteredFiles, Collection<VCSFileProxy> rootFiles, Collection<VCSFileProxy> rootFilesExclusions, Project project) {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup [] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        for (int j = 0; j < sourceGroups.length; j++) {
            SourceGroup sourceGroup = sourceGroups[j];
            FileObject srcRootFo = sourceGroup.getRootFolder();
            VCSFileProxy rootFile = VCSFileProxy.createFileProxy(srcRootFo);
            if (rootFile == null || (cache.getStatus(rootFile).getStatus() & FileInformation.STATUS_MANAGED) == 0) {
                continue;
            }
            rootFiles.add(rootFile);
            boolean containsSubprojects = false;
            FileObject [] rootChildren = srcRootFo.getChildren();
            Set<VCSFileProxy> projectFiles = new HashSet<>(rootChildren.length);
            for (int i = 0; i < rootChildren.length; i++) {
                FileObject rootChildFo = rootChildren[i];
                if (isAdministrative(rootChildFo.getNameExt())) {
                    continue;
                }
                VCSFileProxy child = VCSFileProxy.createFileProxy(rootChildFo);
                if (child == null) {
                    continue;
                }
                if (sourceGroup.contains(rootChildFo)) {
                    // TODO: #60516 deep scan is required here but not performed due to performace reasons
                    projectFiles.add(child);
                } else {
                    int status = cache.getStatus(child).getStatus();
                    if (status != FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
                        rootFilesExclusions.add(child);
                        containsSubprojects = true;
                    }
                }
            }
            if (containsSubprojects) {
                filteredFiles.addAll(projectFiles);
            } else {
                filteredFiles.add(rootFile);
            }
        }
    }

    /**
     * May take a long time for many projects, consider making the call from worker threads.
     *
     * @param projects projects to examine
     * @return Context context that defines list of supplied projects
     */
    public static Context getProjectsContext(Project [] projects) {
        List<VCSFileProxy> filtered = new ArrayList<>();
        List<VCSFileProxy> roots = new ArrayList<>();
        List<VCSFileProxy> exclusions = new ArrayList<>();
        for (int i = 0; i < projects.length; i++) {
            addProjectFiles(filtered, roots, exclusions, projects[i]);
        }
        return new Context(filtered, roots, exclusions);
    }

    public static VCSFileProxy [] toFileArray(Collection<FileObject> fileObjects) {
        Set<VCSFileProxy> files = new HashSet<>(fileObjects.size()*4/3+1);
        for (Iterator<FileObject> i = fileObjects.iterator(); i.hasNext();) {
            VCSFileProxy f = VCSFileProxy.createFileProxy(i.next());
            if (f != null) {
                files.add(f);
            }
        }
        files.remove(null);
        return files.toArray(new VCSFileProxy[files.size()]);
    }

    /**
     * Tests parent/child relationship of files.
     *
     * @param parent file to be parent of the second parameter
     * @param file file to be a child of the first parameter
     * @return true if the second parameter represents the same file as the first parameter OR is its descendant (child)
     */
    public static boolean isParentOrEqual(VCSFileProxy parent, VCSFileProxy file) {
        for (; file != null; file = file.getParentFile()) {
            if (file.equals(parent)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Evaluates if the given file is a svn administrative folder - [.svn|_svn]
     * @param file
     * @return true if the given file is a svn administrative folder, otherwise false
     */
    public static boolean isAdministrative(VCSFileProxy file) {
        String name = file.getName();
        boolean administrative = isAdministrative(name);
        return ( administrative && !file.exists() ) ||
               ( administrative && file.exists() && file.isDirectory() ); // lets suppose it's administrative if file doesnt exist
    }

    /**
     * Evaluates if the given fileName is a svn administrative folder name - [.svn|_svn]
     * @param fileName
     * @return true if the given fileName is a svn administrative folder name, otherwise false
     */
    public static boolean isAdministrative(String fileName) {
        return fileName.equals(SVN_ADMIN_DIR); // NOI18N
    }

    /**
     * Tests whether a file or directory should receive the STATUS_NOTVERSIONED_NOTMANAGED status.
     * All files and folders that have a parent with either .svn/entries or _svn/entries file are
     * considered versioned.
     *
     * @param file a file or directory
     * @return false if the file should receive the STATUS_NOTVERSIONED_NOTMANAGED status, true otherwise
     */
    public static boolean isManaged(VCSFileProxy file) {
        return VersioningSupport.getOwner(file) instanceof SubversionVCS && !isPartOfSubversionMetadata(file);
    }

    /**
     * Computes previous revision or <code>null</code>
     * for initial.
     *
     * @param revision num.dot revision or <code>null</code>
     */
    public static String previousRevision(String revision) {
        return revision == null ? null : Long.toString(Long.parseLong(revision) - 1);
    }

    /**
     * Compute relative path to repository root.
     * For not yet versioned files guess the URL
     * from parent context.
     *
     * <p>I/O intensive avoid calling it frnm AWT.
     *
     * @return the repository url or null for unknown
     */
    public static String getRelativePath(VCSFileProxy file) throws SVNClientException {
        String repositoryPath = null;

        List<String> path = new ArrayList<>();
        SVNUrl repositoryURL = null;
        boolean fileIsManaged = false;
        VCSFileProxy lastManaged = file;
        final Context context = new Context(file);
        SvnClient client = Subversion.getInstance().getClient(false, context);
        while (isManaged(file)) {
            fileIsManaged = true;

            ISVNInfo info = null;
            try {
                info = getInfoFromWorkingCopy(client, file, true);
            } catch (SVNClientException ex) {
                if (SvnClientExceptionHandler.isUnversionedResource(ex.getMessage()) == false) {
                    if (WorkingCopyAttributesCache.getInstance().isSuppressed(ex)) {
                        // log this exception if needed and break the execution
                        WorkingCopyAttributesCache.getInstance().logSuppressed(ex, file);
                    } else {
                        SvnClientExceptionHandler.notifyException(context, ex, false, false);
                    }
                }
            }

            if (info != null && info.getUrl() != null) {
                String fileLink = decodeToString(info.getUrl());
                repositoryURL = info.getRepository();
                String repositoryLink = decodeToString(repositoryURL);

                if (fileLink != null && repositoryLink !=  null) {
                    try {
                        repositoryPath = fileLink.substring(repositoryLink.length());
                    } catch (StringIndexOutOfBoundsException ex) {
                        // XXX delete try-catch after bugfix verification
                        Subversion.LOG.log(Level.INFO, "repoUrl: " + repositoryURL.toString() + "\nfileURL: " + fileLink, ex);
                        throw ex;
                    }

                    Iterator<String> it = path.iterator();
                    StringBuilder sb = new StringBuilder();
                    while (it.hasNext()) {
                        String segment = it.next();
                        sb.append("/"); // NOI18N
                        sb.append(segment);
                    }
                    repositoryPath += sb.toString();
                    break;
                }
            }

            path.add(0, file.getName());
            VCSFileProxy parent = file.getParentFile();
            lastManaged = file;
            if (parent == null) {
                // .svn in root folder
                break;
            } else {
                file = parent;
            }

        }
        if(repositoryURL == null && fileIsManaged) {
            Subversion.LOG.log(Level.WARNING, "no repository url found for managed file {0}", new Object[] { lastManaged });
            // The file is managed but we haven't found the repository URL in it's metadata -
            // this looks like the WC was created with a client < 1.3.0. I wouldn't mind for myself and
            // get the URL from the server, it's just that it could be quite a performance killer.
            // XXX and now i'm just currious how we will handle this if there will be some javahl or
            // pure java client suport -> without dispatching to our metadata parser
            if (VCSFileProxySupport.canRead(VCSFileProxy.createFileProxy(lastManaged, SvnUtils.SVN_WC_DB))) {
                throw new SVNClientException(NbBundle.getMessage(SvnUtils.class, "MSG_too_old_client", lastManaged));
            } else {
                throw new SVNClientException(NbBundle.getMessage(SvnUtils.class, "MSG_too_old_WC"));
            }
        } else if(!fileIsManaged) {
            Subversion.LOG.log(Level.INFO, "no repository url found for not managed file {0}", new Object[] { lastManaged });
        }
        return repositoryPath;
    }

    /**
     * Returns the repository root for the given file.
     * For not yet versioned files guess the URL
     * from parent context.
     *
     * <p>I/O intensive avoid calling it frnm AWT.
     *
     * @return the repository url or null for unknown
     */
    public static SVNUrl getRepositoryRootUrl(VCSFileProxy file) throws SVNClientException {
        return getRepositoryRootUrl(file, false);
    }
    
    public static SVNUrl getRepositoryRootUrl(VCSFileProxy file, boolean knownToBeManaged) throws SVNClientException {
        final Context context = new Context(file);
        SvnClient client = Subversion.getInstance().getClient(false, context);

        SVNUrl repositoryURL = null;
        boolean fileIsManaged = false;
        VCSFileProxy lastManaged = file;
        SVNClientException e = null;
        while (knownToBeManaged || isManaged(file)) {
            fileIsManaged = true;
            ISVNInfo info = null;
            try {
                e = null;
                info = getInfoFromWorkingCopy(client, file, true);
            } catch (SVNClientException ex) {
                if (SvnClientExceptionHandler.isUnversionedResource(ex.getMessage()) == false) {
                    if (WorkingCopyAttributesCache.getInstance().isSuppressed(ex)) {
                        // log this exception if needed and break the execution
                        WorkingCopyAttributesCache.getInstance().logSuppressed(ex, file);
                    } else {
                        SvnClientExceptionHandler.notifyException(context, ex, false, false);
                        e = ex;
                    }
                }
            }

            if (info != null) {
                repositoryURL = decode(info.getRepository());
                if (repositoryURL != null) {
                    break;
                }
            }

            VCSFileProxy parent = file.getParentFile();
            lastManaged = file;
            if (parent == null || knownToBeManaged) {
                // .svn in root folder
                break;
            } else {
                file = parent;
            }

        }
        if(repositoryURL == null && fileIsManaged) {
            if (e != null) {
                throw e;
            }
            Subversion.LOG.log(Level.WARNING, "no repository url found for managed file {0}", new Object[] { lastManaged });
            if (VCSFileProxySupport.canRead(VCSFileProxy.createFileProxy(lastManaged, SvnUtils.SVN_WC_DB))) {
                throw new SVNClientException(NbBundle.getMessage(SvnUtils.class, "MSG_too_old_client", lastManaged));
            } else {
                throw new SVNClientException(NbBundle.getMessage(SvnUtils.class, "MSG_too_old_WC"));
            }
        } else if(!fileIsManaged) {
            Subversion.LOG.log(Level.INFO, "no repository url found for not managed file {0}", new Object[] { lastManaged });
            // XXX #168094 logging
            Level oldLevel = Subversion.LOG.getLevel();
            Subversion.LOG.setLevel(Level.FINE);
            Subversion.LOG.log(Level.INFO, "getRepositoryRootUrl: file {0} {1}", new Object[] { lastManaged, VersioningSupport.getOwner(lastManaged) });
            Subversion.LOG.setLevel(oldLevel);
            if (!lastManaged.exists()) {
                Subversion.LOG.log(Level.INFO, "getRepositoryRootUrl: file {0} does not exist", new Object[] { lastManaged });
            }
        }
        return repositoryURL;
    }

    /**
     * Returns the repository URL for the given file.
     * For not yet versioned files guess the URL
     * from parent context.
     *
     * <p>I/O intensive avoid calling it frnm AWT.
     *
     * @return the repository url or null for unknown
     */
    public static SVNUrl getRepositoryUrl(VCSFileProxy file) throws SVNClientException {

        StringBuilder path = new StringBuilder();
        SVNUrl fileURL = null;
        SvnClient client = null;
        final Context context = new Context(file);
        try {
            client = Subversion.getInstance().getClient(false, context);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(context, ex, false, false);
            return null;
        }
        boolean fileIsManaged = false;
        VCSFileProxy lastManaged = file;
        SVNClientException e = null;
        while (isManaged(file)) {
            fileIsManaged = true;
            e = null;
            try {
                // it works with 1.3 workdirs and our .svn parser
                ISVNStatus status = getSingleStatus(client, file);
                if (status != null) {
                    fileURL = decode(status.getUrl());
                    if (fileURL != null) {
                        break;
                    }
                }
            } catch (SVNClientException ex) {
                if (SvnClientExceptionHandler.isUnversionedResource(ex.getMessage()) == false) {
                    if (WorkingCopyAttributesCache.getInstance().isSuppressed(ex)) {
                        // log this exception if needed and break the execution
                        WorkingCopyAttributesCache.getInstance().logSuppressed(ex, file);
                    } else {
                        SvnClientExceptionHandler.notifyException(context, ex, false, false);
                        e = ex;
                    }
                }
            }

            // slower fallback

            ISVNInfo info = null;
            try {
                info = getInfoFromWorkingCopy(client, file, true);
            } catch (SVNClientException ex) {
                if (SvnClientExceptionHandler.isUnversionedResource(ex.getMessage()) == false) {
                    SvnClientExceptionHandler.notifyException(context, ex, false, false);
                }
            }

            if (info != null) {
                fileURL = decode(info.getUrl());

                if (fileURL != null ) {
                    break;
                }
            }

            path.insert(0, file.getName()).insert(0, "/"); //NOI18N
            VCSFileProxy parent = file.getParentFile();
            lastManaged = file;
            if (parent == null) {
                // .svn in root folder
                break;
            } else {
                file = parent;
            }

        }
        if(fileURL == null && fileIsManaged) {
            if (e != null) {
                throw e;
            }
            Subversion.LOG.log(Level.WARNING, "no repository url found for managed file {0}", new Object[] { lastManaged });
            if (VCSFileProxySupport.canRead(VCSFileProxy.createFileProxy(lastManaged, SvnUtils.SVN_WC_DB))) {
                throw new SVNClientException(NbBundle.getMessage(SvnUtils.class, "MSG_too_old_client", lastManaged));
            } else {
                throw new SVNClientException(NbBundle.getMessage(SvnUtils.class, "MSG_too_old_WC"));
            }
        } else if(!fileIsManaged) {
            Subversion.LOG.log(Level.INFO, "no repository url found for not managed file {0}", new Object[] { lastManaged });
        }
        if (path.length() > 0) {
            fileURL = fileURL.appendPath(path.toString());
        }
        return fileURL;
    }

    /**
     * Returns repository urls for all versioned files underneath the given root
     *
     * @param root
     * @return
     * @throws SVNClientException
     */
    public static Map<VCSFileProxy, SVNUrl> getRepositoryUrls(VCSFileProxy root) throws SVNClientException {
        SVNUrl fileURL = null;
        SvnClient client = null;
        final Context context = new Context(root);
        try {
            client = Subversion.getInstance().getClient(false, context);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(context, ex, false, false);
            return null;
        }

        Map<VCSFileProxy, SVNUrl> ret = new HashMap<>();
        try {
            ISVNStatus[] statuses = client.getStatus(root, true, true);
            for (ISVNStatus status : statuses) {
                if (status != null) {
                    fileURL = decode(status.getUrl());
                    if (fileURL != null) {
                        ret.put(status.getFile(), fileURL);
                    }
                }
            }
        } catch (SVNClientException ex) {
            if (SvnClientExceptionHandler.isUnversionedResource(ex.getMessage()) == false) {
                if (WorkingCopyAttributesCache.getInstance().isSuppressed(ex)) {
                    // log this exception if needed and break the execution
                    WorkingCopyAttributesCache.getInstance().logSuppressed(ex, root);
                } else {
                    SvnClientExceptionHandler.notifyException(context, ex, false, false);
                }
            }
        }

        return ret;
    }

    public static ISVNStatus getSingleStatus(SvnClient client, VCSFileProxy file) throws SVNClientException {
        ISVNStatus status = null;
        Map<VCSFileProxy, ISVNStatus> cache = statusCache.get();
        if (cache != null) {
            status = cache.get(file);
            if (status != null) {
                return status;
            }
        }
        try {
            status = client.getSingleStatus(file);
        } catch (SVNClientException ex) {
            if (SvnClientExceptionHandler.isUnversionedResource(ex.getMessage())) {
                status = new SVNStatusUnversioned(file);
            } else {
                throw ex;
            }
        }
        if (cache != null) {
            cache.put(file, status);
        }
        return status;
    }

    /**
     * Decodes svn URI by decoding %XX escape sequences.
     *
     * @param url url to decode
     * @return decoded url
     */
    public static SVNUrl decode(SVNUrl url) {
        try {
            String decoded = decodeToString(url);
            return decoded == null ? null : new SVNUrl(decoded);
        } catch (java.net.MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Decodes svn URI by decoding %XX escape sequences.
     *
     * @param url url to decode
     * @return decoded url
     */
    public static String decodeToString (SVNUrl url) {
        if (url == null) {
            return null;
        }
        return decodeToString(url.toString());
    }
    
    /**
     * Decodes svn URI by decoding %XX escape sequences.
     *
     * @param url url to decode
     * @return decoded url
     */
    public static String decodeToString (String url) {
        if (url == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(url.length());

        boolean inQuery = false;
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c == '?') {
                inQuery = true;
            } else if (c == '+' && inQuery) {
                sb.append(' ');
            } else if (isEncodedByte(c, url, i)) {
                List<Byte> byteList = new ArrayList<>();
                do  {
                    byteList.add((byte) Integer.parseInt(url.substring(i + 1, i + 3), 16));
                    i += 3;
                    if (i >= url.length()) break;
                    c = url.charAt(i);
                } while(isEncodedByte(c, url, i));

                if(byteList.size() > 0) {
                    byte[] bytes = new byte[byteList.size()];
                    for(int ib = 0; ib < byteList.size(); ib++) {
                        bytes[ib] = byteList.get(ib);
                    }
                    try {
                        sb.append(new String(bytes, "UTF8")); //NOI18N
                    } catch (Exception e) {
                        Subversion.LOG.log(Level.INFO, null, e);  // oops
                    }
                    i--;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static boolean isEncodedByte(char c, String s, int i) {
        return c == '%' && i + 2 < s.length() && isHexDigit(s.charAt(i + 1)) && isHexDigit(s.charAt(i + 2));
    }

    private static boolean isHexDigit(char c) {
        return c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f';
    }

    /*
     * Determines a versioned file's repository path
     *
     * @param file versioned file
     * @return file's path in repository
     */
    public static String getRepositoryPath(VCSFileProxy file) throws SVNClientException {
        SVNUrl url = getRepositoryUrl(file);
        SVNUrl rootUrl = getRepositoryRootUrl(file);
        return decodeToString(SVNUrlUtils.getRelativePath(rootUrl, url, true));
    }

    /**
     * @return true if the buffer is almost certainly binary.
     * Note: Non-ASCII based encoding encoded text is binary,
     * newlines cannot be reliably detected.
     */
    public static boolean isBinary(byte[] buffer) {
        for (int i = 0; i<buffer.length; i++) {
            int ch = buffer[i];
            if (ch < 32 && ch != '\t' && ch != '\n' && ch != '\r') {
                return true;
            }
        }
        return false;
    }

    public static SVNUrl getCopiedUrl (VCSFileProxy f) {
        try {
            ISVNInfo info = getInfoFromWorkingCopy(Subversion.getInstance().getClient(false, new Context(f)), f);
            if (info != null) {
                return info.getCopyUrl();
            }
        } catch (SVNClientException e) {
            // at least log the exception
            if (!WorkingCopyAttributesCache.getInstance().isSuppressed(e)) {
                Subversion.LOG.log(Level.INFO, null, e);
            }
        }
        return null;
    }

    /**
     * Removes all occurances of '\r' and replaces them with '\n'
     * @param text
     * @return
     */
    public static String fixLineEndings(String text) {
        return text.replaceAll("\r\n", "\n").replace('\r', '\n'); //NOI18N
    }

    public static boolean hasMetadata (VCSFileProxy file) {
        if (file.isFile()) {
            return false;
        }
        //return VCSFileProxySupport.canRead(entries) || VCSFileProxySupport.canRead(db);
        final VCSFileProxy entries = VCSFileProxy.createFileProxy(file, SvnUtils.SVN_ENTRIES_DIR);
        if (entries.exists() && VCSFileProxySupport.canRead(entries)) {
            return true;
        }
        final VCSFileProxy db = VCSFileProxy.createFileProxy(file, SvnUtils.SVN_WC_DB);
        if (db.exists() && VCSFileProxySupport.canRead(db)) {
            return true;
        }
        return false;
    }

    private static final ThreadLocal<Map<VCSFileProxy, ISVNInfo>> infoCache = new ThreadLocal<>();
    private static final ThreadLocal<Map<VCSFileProxy, ISVNStatus>> statusCache = new ThreadLocal<>();
    
    public static void runWithInfoCache (Runnable runnable) {
        Map<VCSFileProxy, ISVNInfo> infos = infoCache.get();
        Map<VCSFileProxy, ISVNStatus> statuses = statusCache.get();
        if (infos == null || statuses == null) {
            infos = new HashMap<>();
            infoCache.set(infos);
            statuses = new HashMap<>();
            statusCache.set(statuses);
            try {
                runnable.run();
            } finally {
                infoCache.remove();
                statusCache.remove();
            }
        } else {
            runnable.run();
        }
    }
    
    public static ISVNInfo getInfoFromWorkingCopy (SvnClient client, VCSFileProxy file) throws SVNClientException {
        return getInfoFromWorkingCopy(client, file, false);
    }

    private static ISVNInfo getInfoFromWorkingCopy (SvnClient client, VCSFileProxy file, boolean cannonicalize) throws SVNClientException {
        ISVNInfo info = null;
        Map<VCSFileProxy, ISVNInfo> cache = infoCache.get();
        if (cache != null) {
            info = cache.get(file);
            if (info != null) {
                return info;
            }
        }
        try {
            info = client.getInfoFromWorkingCopy(file);
        } catch (SVNClientException ex) {
            if (SvnClientExceptionHandler.isUnversionedResource(ex.getMessage())) {
                if (!cannonicalize) {
                    info = new SVNInfoUnversioned(file);
                }
            } else {
                throw ex;
            }
        }
        if (info == null) {
            try {
                // this might be a symlink
                info = client.getInfoFromWorkingCopy(VCSFileProxySupport.getCanonicalFile(file));
            } catch (IOException ex) {
                Subversion.LOG.log(Level.INFO, "getInfoFromWorkingCopy", ex); //NOI18N
                // pfff, don't know what now
            } catch (SVNClientException ex) {
                if (SvnClientExceptionHandler.isUnversionedResource(ex.getMessage())) {
                    info = new SVNInfoUnversioned(file);
                } else {
                    throw ex;
                }
            }
        }
        if (cache != null && info != null) {
            cache.put(file, info);
        }
        return info;
    }

    public static void rollback (VCSFileProxy file, SVNUrl repoUrl, SVNUrl fileUrl, SVNRevision revision, boolean wasDeleted, OutputLogger logger) {
        if (wasDeleted) {
            // it was deleted, lets delete it again
            if (file.exists()) {
                try {
                    SvnClient client = Subversion.getInstance().getClient(false, new Context(file));
                    client.remove(new VCSFileProxy[]{file}, true);
                } catch (SVNClientException ex) {
                    Subversion.LOG.log(Level.SEVERE, null, ex);
                }
                Subversion.getInstance().getStatusCache().refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            }
            return;
        }
        VCSFileProxy parent = file.getParentFile();
        VCSFileProxySupport.mkdirs(parent);

        OutputStream os = null;
        InputStream is = null;
        try {
            File oldFile = VersionsCache.getInstance(VCSFileProxySupport.getFileSystem(file)).getFileRevision(repoUrl, fileUrl, revision.toString(), file.getName());
            FileObject fo = file.toFileObject();
            if (fo == null) {
                fo = parent.toFileObject().createData(file.getName());
            } else {
                saveIfModified(fo);
                try {
                    // this is weird, but FS needs some time between save and revert so it recognizes the change
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
            }
            os = fo.getOutputStream();
            is = FileUtil.toFileObject(oldFile).getInputStream();
            FileUtil.copy(is, os);
        } catch (IOException e) {
            if (refersToDirectory(e)) {
                if (Subversion.LOG.isLoggable(Level.FINE)) {
                    Subversion.LOG.log(Level.FINE, null, e);
                }
                logger.logError(NbBundle.getMessage(SearchHistoryAction.class, "MSG_SummaryView.refersToDirectory", fileUrl)); //NOI18N
            } else {
                Subversion.LOG.log(Level.SEVERE, null, e);
            }
        } finally {
            if (os != null) {
                try { os.close(); } catch (IOException ex) { }
            }
            if (is != null) {
                try { is.close(); } catch (IOException ex) { }
            }
        }
    }

    public static void saveIfModified (FileObject fo) {
        try {
            DataObject reqDO = DataObject.find(fo);
            Set<DataObject> modifs = DataObject.getRegistry().getModifiedSet();
            if (modifs.contains(reqDO)) {
                try {
                    SaveCookie sc = reqDO.getLookup().lookup(SaveCookie.class);
                    if (sc != null) {
                        sc.save();
                    }
                } catch (IOException ex) {
                    Subversion.LOG.log(Level.WARNING, null, ex);
                }
            }
        } catch (DataObjectNotFoundException ex) {
            // well, what can you do
            Subversion.LOG.log(Level.FINE, null, ex);
        }
    }
    
    private static boolean refersToDirectory (Exception ex) {
        Throwable t = ex;
        boolean dir = false;
        while (t != null && !(dir = t.getMessage().contains("refers to a directory"))) { //NOI18N
            t = t.getCause();
        }
        return dir;
    }
    
    /**
     * Compares two {@link FileInformation} objects by importance of statuses they represent.
     */
    public static class ByImportanceComparator<T> implements Comparator<FileInformation> {
        @Override
        public int compare(FileInformation i1, FileInformation i2) {
            return getComparableStatus(i1.getStatus()) - getComparableStatus(i2.getStatus());
        }
    }

    /**
     * Normalize flat files, Subversion treats folder as normal file
     * so it's necessary explicitly list direct descendants to
     * get classical flat behaviour.
     *
     * <p> E.g. revert on package node means:
     * <ul>
     *   <li>revert package folder properties AND
     *   <li>revert all modified (including deleted) files in the folder
     * </ul>
     *
     * @return files with given status and direct descendants with given status.
     */
    public static VCSFileProxy[] flatten(VCSFileProxy[] files, int status) {
        LinkedList<VCSFileProxy> ret = new LinkedList<>();

        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        for (int i = 0; i<files.length; i++) {
            VCSFileProxy dir = files[i];
            FileInformation info = cache.getStatus(dir);
            if ((status & info.getStatus()) != 0) {
                ret.add(dir);
            }
            VCSFileProxy[] entries = cache.listFiles(dir);  // comparing to dir.listFiles() lists already deleted too
            for (int e = 0; e<entries.length; e++) {
                VCSFileProxy entry = entries[e];
                info = cache.getStatus(entry);
                if ((status & info.getStatus()) != 0) {
                    ret.add(entry);
                }
            }
        }

        return ret.toArray(new VCSFileProxy[ret.size()]);
    }

    /**
     * Utility method that returns all non-excluded modified files that are
     * under given roots (folders) and have one of specified statuses.
     *
     * @param context context to search
     * @param includeStatus bit mask of file statuses to include in result
     * @return File [] array of Files having specified status
     */
    public static VCSFileProxy [] getModifiedFiles(Context context, int includeStatus) {
        VCSFileProxy[] all = Subversion.getInstance().getStatusCache().listFiles(context, includeStatus);
        List<VCSFileProxy> files = new ArrayList<>();
        for (int i = 0; i < all.length; i++) {
            VCSFileProxy file = all[i];
            String path = file.getPath();
            if (SvnModuleConfig.getDefault(context.getFileSystem()).isExcludedFromCommit(path) == false) {
                files.add(file);
            }
        }

        // ensure that command roots (files that were explicitly selected by user) are included in Diff
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        VCSFileProxy [] rootFiles = context.getRootFiles();
        for (int i = 0; i < rootFiles.length; i++) {
            VCSFileProxy file = rootFiles[i];
            if (file.isFile() && (cache.getStatus(file).getStatus() & includeStatus) != 0 && !files.contains(file)) {
                files.add(file);
            }
        }
        return files.toArray(new VCSFileProxy[files.size()]);
    }


    /**
     * Checks file location.
     *
     * @param file file to check
     * @return true if the file or folder is a part of subverion metadata, false otherwise
     */
    public static boolean isPartOfSubversionMetadata(VCSFileProxy file) {
        return metadataPattern.matcher(file.getPath()).matches();
    }

    /**
     * Gets integer status that can be used in comparators. The more important the status is for the user,
     * the lower value it has. Conflict is 0, unknown status is 100.
     *
     * @return status constant suitable for 'by importance' comparators
     */
    public static int getComparableStatus(int status) {
        if (0 != (status & FileInformation.STATUS_VERSIONED_CONFLICT)) {
            return 0;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MERGE)) {
            return 1;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            return 10;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            return 11;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            return 12;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            return 13;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            return 14;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
            return 30;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
            return 31;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY)) {
            return 32;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_UPTODATE)) {
            return 50;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            return 100;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            return 101;
        } else if (status == FileInformation.STATUS_UNKNOWN) {
            return 102;
        } else {
            throw new IllegalArgumentException("Uncomparable status: " + status); // NOI18N
        }
    }

    /**
     * Returns a symbolic branch/tag name if the given file lives
     * in a location specified by an AnnotationExpression
     *
     * @param file
     * @return name or null
     */
    public static String getCopy(VCSFileProxy file) {
        SVNUrl url;
        try {
            url = getRepositoryUrl(file);
        } catch (SVNClientException ex) {
            if (!WorkingCopyAttributesCache.getInstance().isSuppressed(ex)) {
                SvnClientExceptionHandler.notifyException(new Context(file), ex, false, false);
            }
            return null;
        }
        final FileSystem fileSystem = VCSFileProxySupport.getFileSystem(file);
        return getCopy(fileSystem, url, SvnModuleConfig.getDefault(fileSystem).getAnnotationExpresions());
    }

    /**
     * Returns a symbolic branch/tag name if the given url represents
     * a location specified by an AnnotationExpression
     *
     * @param url
     * @return name or null
     */
    public static String getCopy(Context context, SVNUrl url) {
        final FileSystem fileSystem = context.getFileSystem();
        return getCopy(fileSystem, url, SvnModuleConfig.getDefault(fileSystem).getAnnotationExpresions());
    }

    /**
     * Returns a symbolic branch/tag name if the given url represents
     * a location specified by an AnnotationExpression
     *
     * @param url
     * @param annotationExpressions
     * @return name or null
     */
    private static String getCopy(FileSystem fileSystem, SVNUrl url, List<AnnotationExpression> annotationExpressions) {
        if (url != null && SvnModuleConfig.getDefault(fileSystem).isDetermineBranchesEnabled()) {
            String urlString = decodeToString(url);
            for (Iterator<AnnotationExpression> it = annotationExpressions.iterator(); it.hasNext();) {
                String name = it.next().getCopyName(urlString);
                if(name != null) {
                    return name;
                }
            }
        }
        return null;
    }

    /**
     * Refreshes statuses of this folder and all its parent folders up to filesystem root.
     *
     * @param folder folder to refresh
     */
    public static void refreshParents(VCSFileProxy folder) {
        if (folder == null) {
            return;
        }
        // Performance problem on remote FS when refresh all parents to root.
        // Do not see needs to refresh all parents.
        // So do not refresh all parents.
        //refreshParents(folder.getParentFile());
        Subversion.getInstance().getStatusCache().refresh(folder, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
    }

    /**
     * Rips an eventual username off - e.g. user@svn.host.org
     *
     * @param host - hostname with a userneame
     * @return host - hostname without the username
     */
    public static String ripUserFromHost(String host) {
        int idx = host.indexOf('@');
        if(idx < 0) {
            return host;
        } else {
            return host.substring(idx + 1);
        }
    }

    public static SVNRevision getSVNRevision(String revisionString) {
        try {
            // HEAD, PREV, BASE, COMMITED, ...
            return SVNRevision.getRevision(revisionString);
        } catch (ParseException ex) {
            return new SVNRevision.Number(Long.parseLong(revisionString));
        }
    }

    /*
     * Returns the first pattern from the list which matches with the given value.
     * The patterns are interpreted as shell paterns.
     *
     * @param patterns
     * @param value
     * @return the first pattern matching with the given value
     */
    public static List<String> getMatchinIgnoreParterns(List<String> patterns, String value, boolean onlyFirstMatch)  {
        List<String> ret = new ArrayList<>();
        if(patterns == null) {
            return ret;
        }
        for (Iterator<String> i = patterns.iterator(); i.hasNext();) {
            try {
                // may contain shell patterns (almost identical to RegExp)
                String patternString  = i.next();
                String shellPatternString = regExpToFilePatterns(patternString);
                Pattern pattern =  Pattern.compile(shellPatternString);
                if (pattern.matcher(value).matches()) {
                    ret.add(patternString);
                    if(onlyFirstMatch) {
                        return ret;
                    }
                }
            } catch (PatternSyntaxException e) {
                // it's difference between shell and regexp
                // or user error (set invalid property), rethrow?
                Subversion.LOG.log(Level.INFO, null, e);
            }
        }
        return ret;
    }

    private static String regExpToFilePatterns(String exp) {
        exp = exp.replaceAll("\\.", "\\\\.");   // NOI18N
        exp = exp.replaceAll("\\*", ".*");      // NOI18N
        exp = exp.replaceAll("\\?", ".");       // NOI18N

        exp = exp.replaceAll("\\$", "\\\\\\$"); // NOI18N
        exp = exp.replaceAll("\\^", "\\\\^");   // NOI18N
        exp = exp.replaceAll("\\<", "\\\\<");   // NOI18N
        exp = exp.replaceAll("\\>", "\\\\>");   // NOI18N
        exp = patchRegExpClassCharacters(exp);
        exp = exp.replaceAll("\\{", "\\\\{");   // NOI18N
        exp = exp.replaceAll("\\}", "\\\\}");   // NOI18N
        exp = exp.replaceAll("\\(", "\\\\(");   // NOI18N
        exp = exp.replaceAll("\\)", "\\\\)");   // NOI18N
        exp = exp.replaceAll("\\+", "\\\\+");   // NOI18N
        exp = exp.replaceAll("\\|", "\\\\|");   // NOI18N

        return exp;
    }


    /*
     * Returns a string having characters <code>[</code> and <code>]</code> escaped if they do not represent
     * a character class definition.
     *
     * @param exp string to be escaped
     * @return string with escaped characters
     */
    private static String patchRegExpClassCharacters (String exp) {
        LinkedList<Integer> indexes = new LinkedList<>();
        StringBuilder builder = new StringBuilder(exp.length());

        for (int index = 0, builderIndex = 0; index < exp.length(); ++index, ++builderIndex) {
            char ch = exp.charAt(index);
            if (ch == '\\') {       // NOI18N
                // backslash is escaped and added
                builder.append(ch);
                if (++index < exp.length()) {
                    ++builderIndex;
                    builder.append(exp.charAt(index));
                }
            } else if (ch == '[') { // NOI18N
                // openning parenthesis is added and its position is saved for possible later escaping
                builder.append(ch);
                indexes.add(builderIndex);
            } else {
                if (ch == ']') { // NOI18N
                    // closing parenthesis consumes the last opening parenthesis (if that exists), otherwise escapes itself
                    if (indexes.isEmpty()) {
                        builder.append("\\");   // NOI18N
                        ++builderIndex;
                    } else {
                        indexes.removeLast();
                    }
                }
                builder.append(ch); // append the current character
            }
        }

        for (Integer index : indexes) {
            // escapes all opening parenthesis that have no closing
            builder.insert(index, "\\");   // NOI18N
        }

        return builder.toString();
    }

    /**
     * Reads the svn:mime-type property or uses content analysis for unversioned files.
     *
     * @param file file to examine
     * @return String mime type of the file (or best guess)
     */
    public static String getMimeType(VCSFileProxy file) {
        FileObject fo = file.toFileObject();
        String foMime;
        if (fo == null) {
            foMime = "content/unknown"; //NOI18N
        } else {
            foMime = fo.getMIMEType();
        }
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_VERSIONED) == 0) {
            if(foMime.startsWith("text/")) { //NOI18N
                return foMime;
            }
            return VCSFileProxySupport.isFileContentText(file) ? "text/plain" : "application/octet-stream"; //NOI18N
        } else {
            PropertiesClient client = new PropertiesClient(file);
            try {
                byte [] mimeProperty = client.getProperties().get("svn:mime-type"); //NOI18N
                if (mimeProperty != null) {
                    String mimePath = new String(mimeProperty, "UTF-8"); //NOI18N
                    int pos = mimePath.indexOf('/');
                    if (pos > 0) {
                        while (pos < mimePath.length()) {
                            try {
                                return MimePath.parse(mimePath).getPath();
                            } catch (IllegalArgumentException ex) {
                                // for paths in the form of text/plain;charset=UTF-8
                                mimePath = mimePath.substring(0, mimePath.length() - 1);
                            }
                        }
                    }
                }
                return VCSFileProxySupport.isFileContentText(file) ? foMime : "application/octet-stream"; //NOI18N
            } catch (IOException e) {
                return foMime;
            }
        }
    }

    public static <T> boolean equals(List<T> l1, List<T> l2) {

        if(l1 == null && l2 == null) {
            return true;
        }

        if(l1 != null && l2 == null) {
            return l1.isEmpty();
        }

        if(l1 == null && l2 != null) {
            return l2.isEmpty();
        }

        if(l1.size() != l2.size()) {
            return false;
        }

        for(T t : l1) {
            if(!l2.contains(t)) {
                return false;
            }
        }

        return true;
    }

    private static final int STATUS_RECURSIVELY_TRAVERSIBLE = FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;

    public static List<VCSFileProxy> listManagedRecursively(VCSFileProxy root) {
        List<VCSFileProxy> ret = new ArrayList<>();
        if(root == null) {
            return ret;
        }
        ret.add(root);
        VCSFileProxy[] files = root.listFiles();
        if(files != null) {
            FileStatusCache cache = Subversion.getInstance().getStatusCache();
            for (VCSFileProxy file : files) {
                FileInformation info = cache.getCachedStatus(file);
                if(!(isPartOfSubversionMetadata(file) || isAdministrative(file)
                        || info != null && (info.getStatus() & STATUS_RECURSIVELY_TRAVERSIBLE) == 0)) {
                    if(file.isDirectory()) {
                        ret.addAll(listManagedRecursively(file));
                    } else {
                        ret.add(file);
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Lists all children of a given dir with the exception of metadata files or folders
     * @param root given folder
     * @return list of all direct children
     */
    public static List<VCSFileProxy> listChildren (VCSFileProxy root) {
        List<VCSFileProxy> ret = new ArrayList<>();
        if(root == null) {
            return ret;
        }
        VCSFileProxy[] files = root.listFiles();
        if(files != null) {
            for (VCSFileProxy file : files) {
                if(!(isPartOfSubversionMetadata(file) || isAdministrative(file))) {
                    ret.add(file);
                }
            }
        }
        return ret;
    }

    public static SVNRevision toSvnRevision(String revision) {
        SVNRevision svnrevision;
        if (Setup.REVISION_HEAD.equals(revision) || SVNRevision.HEAD.toString().equals(revision)) {
            svnrevision = SVNRevision.HEAD;
        } else if (SVNRevision.BASE.toString().equals(revision)) {
            svnrevision = SVNRevision.BASE;
        } else {
            svnrevision = new SVNRevision.Number(Long.parseLong(revision));
        }
        return svnrevision;
    }

    // XXX JAVAHL
    public static ISVNLogMessage[] getLogMessages(SvnClient client, SVNUrl rootUrl, String[] paths,
            Map<String, SVNRevision> pegRevisions, SVNRevision fromRevision, SVNRevision toRevision,
            boolean stopOnCopy, boolean fetchChangePath, long limit) throws SVNClientException {
        Set<Long> alreadyHere = new HashSet<>();
        List<ISVNLogMessage> ret = new ArrayList<>();
        boolean sorted = true;
        long lastRevNum = -1;
        for (String path : paths) {
            SVNRevision pegRevision;
            if (pegRevisions == null) {
                pegRevision = null;
            } else {
                pegRevision = pegRevisions.get(path);
                if (pegRevision == null) {
                    // do not touch uncommitted files
                    continue;
                }
            }
            ISVNLogMessage[] logs = client.getLogMessages(rootUrl.appendPath(path), pegRevision, fromRevision, toRevision, stopOnCopy, fetchChangePath, limit);
            for (ISVNLogMessage log : logs) {
                long revNum = log.getRevision().getNumber();
                if(!alreadyHere.contains(revNum)) {
                    ret.add(log);
                    alreadyHere.add(revNum);
                    sorted &= (revNum > lastRevNum);
                    lastRevNum = revNum;
                }
            }
        }
        if (!sorted) {
            Collections.sort(ret, new Comparator<ISVNLogMessage>() {
                @Override
                public int compare(ISVNLogMessage m1, ISVNLogMessage m2) {
                    long revNum1 = m1.getRevision().getNumber();
                    long revNum2 = m2.getRevision().getNumber();
                    return (revNum1 == revNum2) ? 0
                                                : (revNum1 > revNum2) ? 1 : -1;
                }
            });
        }
        return ret.toArray(new ISVNLogMessage[ret.size()]);
    }

    private static Logger TY9_LOG = null;
    public static synchronized void logT9Y(String msg) {
        if(TY9_LOG == null) {
            TY9_LOG = Logger.getLogger("org.netbeans.modules.subversion.remote.t9y"); //NOI18N
        }
        TY9_LOG.log(Level.FINEST, msg);
    }

    /**
     * Either returns all root files from a context if they belong to the same DataObject or 
     * opens a dialog to pick a file from all managed roots in the given context.
     *
     * @param ctx the given context to choose the root from
     * @return might be one of the following:<br>
     *          <ul>
     *              <li>all root files from a context
     *              <li>the context root picked by the one picked by the user
     *              <li>null if there are more than one root file not belonging
     *                  to the same DataObject and none was chosen by the user
     *          </ul>
     */
    public static VCSFileProxy[] getActionRoots(Context ctx) {
        return getActionRoots(ctx, true);
    }

    public static VCSFileProxy[] getActionRoots (Context ctx, boolean showSelector) {
        VCSFileProxy[] roots = ctx.getRootFiles();
        List<VCSFileProxy> l = new ArrayList<>();

        // filter managed roots
        for (VCSFileProxy file : roots) {
            if(isManaged(file)) {
                l.add(file);
            }
        }

        roots = l.toArray(new VCSFileProxy[l.size()]);
        if(VCSFileProxySupport.shareCommonDataObject(roots)) {
            return roots;
        }

        if(roots.length > 1) {
            // more than one managed root => need a dlg
            FileSelector fs = new FileSelector(
                    NbBundle.getMessage(SvnUtils.class, "LBL_FileSelector_Title"),
                    NbBundle.getMessage(SvnUtils.class, "FileSelector.jLabel1.text"),
                    new HelpCtx("org.netbeans.modules.subversion.FileSelector"),
                    SvnModuleConfig.getDefault(ctx.getFileSystem()).getPreferences());
            if(showSelector && fs.show(roots)) {
                return new VCSFileProxy[ ]{ fs.getSelectedFile()};
            } else {
                return null;
            }
        } else {
            return new VCSFileProxy[] {roots[0]};
        }
    }

    /**
     * Returns the primary file from a DataObject if there is some
     * @param roots
     * @return
     */
    public static VCSFileProxy getPrimaryFile(VCSFileProxy file) {
        VCSFileProxy primaryFile = null;
        FileObject fo = file.toFileObject();
        if(fo != null) {
            DataObject dao = null;
            try {
                dao = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                Subversion.LOG.log(Level.INFO, "No DataObject found for " + file, ex);
            }
            if(dao != null) {
                primaryFile = VCSFileProxy.createFileProxy(dao.getPrimaryFile());
            }
        }
        if(primaryFile == null) {
            primaryFile = file; // consider it a fallback
        }
        return primaryFile;
    }

    /**
     * @return <code>true</code> if currently running Java Platform is 64-bit, <code>false</code> othrewise
     */
    public static boolean isJava64 () {
        String javaVMName = System.getProperty("java.vm.name"); // NOI18N
        if (javaVMName == null) {
            return false;
        }

        return javaVMName.toLowerCase(Locale.getDefault()).contains("64-bit"); //NOI18N
    }

    public static CommitOptions[] createDefaultCommitOptions(SvnFileNode[] nodes, boolean excludeNew) {
        // NOI18N
        CommitOptions[] commitOptions = new CommitOptions[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            SvnFileNode node = nodes[i];
            VCSFileProxy file = node.getFile();
            if (SvnModuleConfig.getDefault(VCSFileProxySupport.getFileSystem(file)).isExcludedFromCommit(file.getPath())) {
                commitOptions[i] = CommitOptions.EXCLUDE;
            } else {
                commitOptions[i] = getDefaultCommitOptions(node, excludeNew);
            }
        }
        return commitOptions;
    }

    /**
     * Parses the url and returns tunnel name which follows svn+ in protocol
     * @param urlString url with protocol starting with svn+
     * @return
     */
    public static String getTunnelName(String urlString) {
        assert urlString.startsWith("svn+");                            //NOI18N
        int idx = urlString.indexOf(':', 4);                            //NOI18N
        if (idx < 0) {
            idx = urlString.length();
        }
        return urlString.substring(4, idx);
    }

    public static void openInRevision(final VCSFileProxy originalFile, final SVNUrl repoUrl, final SVNUrl fileUrl, final SVNRevision svnRevision, final SVNRevision pegRevision, boolean showAnnotations) {
        File file;
        String rev = svnRevision.toString();
        try {
            file = VersionsCache.getInstance(VCSFileProxySupport.getFileSystem(originalFile)).getFileRevision(repoUrl, fileUrl, rev, pegRevision.toString(), originalFile.getName());
        } catch (IOException e) {
            SvnClientExceptionHandler.notifyException(new Context(originalFile), e, true, true);
            return;
        }

        final FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        EditorCookie ec = null;
        org.openide.cookies.OpenCookie oc = null;
        try {
            DataObject dobj = DataObject.find(fo);
            ec = dobj.getLookup().lookup(EditorCookie.class);
            oc = dobj.getLookup().lookup(org.openide.cookies.OpenCookie.class);
        } catch (DataObjectNotFoundException ex) {
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.log(Level.FINE, null, ex);
            }
        }
        org.openide.text.CloneableEditorSupport ces = null;
        if (ec == null && oc != null) {
            oc.open();
        } else {
            ces = org.netbeans.modules.versioning.util.Utils.openFile(fo, rev);
        }
        if (showAnnotations) {
            if (ces != null) {
                final org.openide.text.CloneableEditorSupport support = ces;
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        javax.swing.JEditorPane[] panes = support.getOpenedPanes();
                        if (panes != null) {
                            BlameAction.showAnnotations(panes[0], originalFile, svnRevision);
                        }
                    }
                });
            }
        }
    }

    /**
     * Similar to {@link #getDefaultCommitOptions(org.netbeans.modules.subversion.SvnFileNode, boolean) } but does not consider exclusions, so
     * the return value will always be a variation of {@link CommitOptions#COMMIT } (unless excludeNew is set to true)
     * @param node
     * @param excludeNew
     * @return
     */
    public static CommitOptions getDefaultCommitOptions (SvnFileNode node, boolean excludeNew) {
        CommitOptions commitOptions;
        switch (node.getInformation().getStatus()) {
            case FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY:
                commitOptions = excludeNew ? CommitOptions.EXCLUDE : getDefaultCommitOptions(node);
                break;
            case FileInformation.STATUS_VERSIONED_DELETEDLOCALLY:
            case FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY:
                commitOptions = CommitOptions.COMMIT_REMOVE;
                break;
            default:
                commitOptions = CommitOptions.COMMIT;
        }
        return commitOptions;
    }

    private static CommitOptions getDefaultCommitOptions(SvnFileNode node) {
        if (node.isFile()) {
            if (node.getMimeType().startsWith("text")) { //NOI18N
                return CommitOptions.ADD_TEXT;
            } else {
                return CommitOptions.ADD_BINARY;
            }
        } else {
            return CommitOptions.ADD_DIRECTORY;
        }
    }

    public void scanForProjects(VCSFileProxy workingFolder, String[] checkedOutFolders, SvnProgressSupport support) {

        Map<Project, Set<Project>> checkedOutProjects = new HashMap<>();
        checkedOutProjects.put(null, new HashSet<Project>()); // initialize root project container
        VCSFileProxy normalizedWorkingFolder = workingFolder.normalizeFile();
        // checkout creates new folders and cache must be aware of them
        refreshParents(normalizedWorkingFolder);
        FileObject fo = normalizedWorkingFolder.toFileObject();
        if (fo != null) {
            for (int i = 0; i < checkedOutFolders.length; i++) {
                if (support != null && support.isCanceled()) {
                    return;
                }
                String module = checkedOutFolders[i];
                if (".".equals(module)) {                   // NOI18N
                    // root folder is scanned, skip remaining modules
                    ProjectUtilities.scanForProjects(fo, checkedOutProjects);
                    break;
                } else {
                    FileObject subfolder = fo.getFileObject(module);
                    if (subfolder != null) {
                        ProjectUtilities.scanForProjects(subfolder, checkedOutProjects);
                    }
                }
            }
        }
        // open project selection
        org.netbeans.modules.remotefs.versioning.api.ProjectUtilities.openCheckedOutProjects(checkedOutProjects, workingFolder);
    }

    /*
     * Refreshes all parents for given files
     * XXX move to versioning.utils if needed in other modules
     */
    public static void refreshFS (VCSFileProxy... files) {
        final Set<VCSFileProxy> parents = new HashSet<>();
        for (VCSFileProxy f : files) {
            VCSFileProxy parent = f.getParentFile();
            if (parent != null) {
                parents.add(parent);
                if (Subversion.LOG.isLoggable(Level.FINE)) {
                    Subversion.LOG.log(Level.FINE, "scheduling for fs refresh: [{0}]", parent); // NOI18N
                }
            }
        }
        if (parents.size() > 0) {
            // let's give the filesystem some time to wake up and to realize that the file has really changed
            Subversion.getInstance().getParallelRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    VersioningSupport.refreshFor(parents.toArray(new VCSFileProxy[parents.size()]));
                }
            }, 100);
        }
    }

    static ThreadLocal<Set<VCSFileProxy>> indexingFiles = new ThreadLocal<>();
    public static <T> T runWithoutIndexing (Callable<T> callable, List<VCSFileProxy> files) throws SVNClientException {
        return runWithoutIndexing(callable, files.toArray(new VCSFileProxy[files.size()]));
    }

    public static <T> T runWithoutIndexing (Callable<T> callable, VCSFileProxy... files) throws SVNClientException {
        try {
            Set<VCSFileProxy> recursiveRoots = indexingFiles.get();
            if (recursiveRoots != null) {
                assert indexingFilesSubtree(recursiveRoots, files) 
                        : "Recursive call does not permit different roots: "  //NOI18N
                        + recursiveRoots + " vs. " + Arrays.asList(files); //NOI18N
                return callable.call();
            } else {
                try {
                    if (Subversion.LOG.isLoggable(Level.FINER)) {
                        Subversion.LOG.log(Level.FINER, "Running block with disabled indexing: on {0}", Arrays.asList(files)); //NOI18N
                    }
                    indexingFiles.set(new HashSet<>(Arrays.asList(files)));
                    return FileObjectIndexingBridgeProvider.getInstance().runWithoutIndexing(callable, files);
                } finally {
                    indexingFiles.remove();
                }
            }
        } catch (SVNClientException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SVNClientException("Cannot run without indexing due to: " + ex.getMessage(), ex); //NOI18N
        }
    }

    private static boolean indexingFilesSubtree (Set<VCSFileProxy> recursiveRoots, VCSFileProxy[] files) {
        for (VCSFileProxy f : files) {
            if (!recursiveRoots.contains(f)) {
                boolean contained = false;
                for (VCSFileProxy root : recursiveRoots) {
                    if (VCSFileProxySupport.isAncestorOrEqual(root, f)) {
                        contained = true;
                        break;
                    }
                }
                if (!contained) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String getColorString (Color c) {
        return "#" + getHex(c.getRed()) + getHex(c.getGreen()) + getHex(c.getBlue()); //NOI18N
    }

    private static String getHex (int i) {
        String hex = Integer.toHexString(i & 0x000000FF);
        if (hex.length() == 1) {
            hex = "0" + hex; //NOI18N
        }
        return hex;
    }
    
    /**
     * Checks and removes from the given string all patterns being a word in
     * braces unless they are listed in supportedVariables<br>
     *
     * e.g.:<br> string: [{status}{folder}{dil}]<br> supportedVariables:
     * "{status}", "{folder}" <br> will result to:<br> [{status}{folder}]<br>
     *
     * @param string to be checked string
     * @param vars supported variables
     * @return
     */
    public static String skipUnsupportedVariables(String string, String[] supportedVariables) {
        String ret = string;
        Pattern p = Pattern.compile("\\{\\w*\\}"); // NOI18N
        Matcher m = p.matcher(string);
        while (m.find()) {
            String g = m.group();
            boolean isVar = false;
            for (String var : supportedVariables) {
                if (var.equals(g)) {
                    isVar = true;
                    break;
                }
            }
            if (!isVar) {
                ret = ret.replace(g, "");
            }
        }
        return ret;
    }

    /**
     * Recursively deletes the file or directory.
     *
     * @param file file/directory to delete
     */
    public static void deleteRecursively(VCSFileProxy file) {
        VCSFileProxySupport.delete(file);
    }

}
