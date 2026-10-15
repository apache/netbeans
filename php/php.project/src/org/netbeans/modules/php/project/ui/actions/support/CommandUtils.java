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
package org.netbeans.modules.php.project.ui.actions.support;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpActionProvider;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.actions.Command;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.XDebugUrlArguments;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.spi.project.SingleMethod;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.windows.TopComponent;

/**
 * @author Radek Matous, Tomas Mysik
 */
public final class CommandUtils {

    private static final Logger LOGGER = Logger.getLogger(CommandUtils.class.getName());

    private static final String HTML_MIME_TYPE = "text/html"; // NOI18N

    private CommandUtils() {
    }

    public static boolean isPhpOrHtmlFile(FileObject file) {
        assert file != null;
        return FileUtil.getMIMEType(file, FileUtils.PHP_MIME_TYPE, HTML_MIME_TYPE, null) != null;
    }

    /** Return <code>true</code> if user wants to restart the current debug session. */
    public static boolean warnNoMoreDebugSession() {
        String message = NbBundle.getMessage(CommandUtils.class, "MSG_NoMoreDebugSession");
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(message, NotifyDescriptor.OK_CANCEL_OPTION);
        return DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.OK_OPTION;
    }

    public static String encodeMethod(String className, String methodName) {
        return className + "::" + methodName; // NOI18N
    }

    public static Pair<String, String> decodeMethod(String encodedMethodName) {
        List<String> decoded = StringUtils.explode(encodedMethodName, "::"); // NOI18N
        assert decoded.size() == 2 : encodedMethodName + " -> " + decoded;
        return Pair.of(decoded.get(0), decoded.get(1));
    }

    /**
     * Get <b>valid</b> {@link FileObject}s for given nodes.
     * @param nodes nodes to get {@link FileObject}s from.
     * @return list of <b>valid</b> {@link FileObject}s, never <code>null</code>.
     */
    public static List<FileObject> getFileObjects(final Node[] nodes) {
        if (nodes.length == 0) {
            return Collections.<FileObject>emptyList();
        }

        final List<FileObject> files = new ArrayList<>(nodes.length);
        for (Node node : nodes) {
            FileObject fo = getFileObject(node);
            // #156939
            if (fo != null) {
                files.add(fo);
            }
        }
        return files;
    }

    /**
     * Get a <b>valid</b> {@link FileObject} for given node.
     * @param node node to get {@link FileObject}s from.
     * @return a <b>valid</b> {@link FileObject}, <code>null</code> otherwise.
     */
    public static FileObject getFileObject(Node node) {
        assert node != null;

        FileObject fileObj = node.getLookup().lookup(FileObject.class);
        if (fileObj != null && fileObj.isValid()) {
            return fileObj;
        }
        DataObject dataObj = node.getCookie(DataObject.class);
        if (dataObj == null) {
            return null;
        }
        fileObj = dataObj.getPrimaryFile();
        if (fileObj != null && fileObj.isValid()) {
            return fileObj;
        }
        return null;
    }

    /**
     * Return <code>true</code> if {@link FileObject} is underneath project sources directory
     * or sources directory itself.
     * @param project project to get sources directory from.
     * @param fileObj {@link FileObject} to check.
     * @return <code>true</code> if {@link FileObject} is underneath project sources directory
     *         or sources directory itself.
     * @see #isUnderAnySourceGroup(PhpProject, FileObject, boolean)
     */
    public static boolean isUnderSources(PhpProject project, FileObject fileObj) {
        assert project != null;
        assert fileObj != null;
        FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(project);
        if (sources == null) {
            return false;
        }
        return sources.equals(fileObj) || FileUtil.isParentOf(sources, fileObj);
    }

    /**
     * Return <code>true</code> if {@link FileObject} is underneath project tests directory
     * or tests directory itself.
     * @param project project to get tests directory from.
     * @param fileObj {@link FileObject} to check.
     * @return <code>true</code> if {@link FileObject} is underneath project tests directory
     *         or tests directory itself.
     * @see #isUnderAnySourceGroup(PhpProject, FileObject, boolean)
     */
    public static boolean isUnderTests(PhpProject project, FileObject fileObj, boolean showFileChooser) {
        assert project != null;
        assert fileObj != null;
        List<FileObject> testDirectories = ProjectPropertiesSupport.getTestDirectories(project, showFileChooser);
        for (FileObject testDir : testDirectories) {
            assert testDir != null;
            if (fileObj.equals(testDir)
                    || FileUtil.isParentOf(testDir, fileObj)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return <code>true</code> if {@link FileObject} is underneath project Selenium tests directory
     * or Selenium tests directory itself.
     * @param project project to get tests directory from.
     * @param fileObj {@link FileObject} to check.
     * @return <code>true</code> if {@link FileObject} is underneath project Selenium tests directory
     *         or Selenium tests directory itself.
     * @see #isUnderAnySourceGroup(PhpProject, FileObject, boolean)
     */
    public static boolean isUnderSelenium(PhpProject project, FileObject fileObj, boolean showFileChooser) {
        assert project != null;
        assert fileObj != null;
        FileObject selenium = ProjectPropertiesSupport.getSeleniumDirectory(project, showFileChooser);
        return selenium != null && (selenium.equals(fileObj) || FileUtil.isParentOf(selenium, fileObj));
    }

    /**
     * Return <code>true</code> if {@link FileObject} is underneath project sources or tests or Selenium directory
     * or any of that directories itself.
     * @param project project to get "parent" directories from.
     * @param fileObj {@link FileObject} to check.
     * @return <code>true</code> if {@link FileObject} is underneath project sources or tests or Selenium directory
     *         or any of that directories itself.
     */
    public static boolean isUnderAnySourceGroup(PhpProject project, FileObject fileObj, boolean showFileChooser) {
        return isUnderSources(project, fileObj)
                || isUnderTests(project, fileObj, showFileChooser)
                || isUnderSelenium(project, fileObj, showFileChooser);
    }

    /**
     * Get {@link SingleMethod single method} for context.
     * @param context context to search in.
     * @return {@link SingleMethod single method} for context, {@code null} if not found
     */
    @CheckForNull
    public static SingleMethod singleMethodForContext(Lookup context) {
        return context.lookup(SingleMethod.class);
    }

    /**
     * Get {@link FileObject}s for context.
     * @param context context to search in.
     * @return {@link FileObject}s for context.
     */
    public static FileObject[] filesForContext(Lookup context) {
        assert context != null;
        Collection<? extends FileObject> files = context.lookupAll(FileObject.class);
        return files.toArray(new FileObject[0]);
    }

    /**
     * Get <b>valid</b> {@link FileObject}s for context and base directory.
     * Return <code>null</code> if any {@link FileObject} is invalid or if
     * the base directory is not parent folder of all found {@link FileObject}s.
     * @param context context to search in.
     * @param baseDirectory a directory that must be a parent folder of all the found {@link FileObject}s.
     * @return <b>valid</b> {@link FileObject}s for context and base directory or <code>null</code>.
     */
    public static FileObject[] filesForContext(Lookup context, FileObject baseDirectory) {
        return filterValidFiles(filesForContext(context), baseDirectory);
    }

    /**
     * Get an array of {@link FileObject}s for currently selected nodes.
     * @return an array of {@link FileObject}s for currently selected nodes, never <code>null</code>.
     * @see #filesForSelectedNodes(FileObject)
     */
    public static FileObject[] filesForSelectedNodes() {
        Node[] nodes = getSelectedNodes();
        if (nodes == null) {
            return new FileObject[0];
        }
        List<FileObject> fileObjects = getFileObjects(nodes);
        return fileObjects.toArray(new FileObject[0]);
    }

    /**
     * Get <b>valid</b> {@link FileObject}s for selected nodes and base directory.
     * @param baseDirectory a directory that must be a parent folder of all the found {@link FileObject}s.
     * @return <b>valid</b> {@link FileObject}s for selected nodes and base directory or <code>null</code>.
     * @see #filesForSelectedNodes()
     */
    public static FileObject[] filesForSelectedNodes(FileObject baseDirectory) {
        return filterValidFiles(Arrays.asList(filesForSelectedNodes()), baseDirectory);
    }

    /**
     * Get a <b>valid</b> {@link FileObject} for context or selected nodes and base directory.
     * Return <code>null</code> if any {@link FileObject} is invalid or if
     * the base directory is not parent folder of all found {@link FileObject}s.
     * @param context context to search in.
     * @param baseDirectory a directory that must be a parent folder of all the found {@link FileObject}s.
     * @return a <b>valid</b> {@link FileObject} for context or selected nodes and base directory or <code>null</code>.
     * @see #fileForContextOrSelectedNodes(Lookup)
     */
    public static FileObject fileForContextOrSelectedNodes(Lookup context, FileObject baseDirectory) {
        assert baseDirectory != null;
        assert baseDirectory.isFolder() : "Folder must be given: " + baseDirectory;

        FileObject[] files = filesForContext(context, baseDirectory);
        if (files == null || files.length == 0) {
            files = filesForSelectedNodes(baseDirectory);
        }
        return (files != null && files.length > 0) ? files[0] : null;
    }

    /**
     * Get an array of <b>valid</b> {@link FileObject}s for context or selected nodes.
     * @param context context to search in.
     * @return an array of <b>valid</b> {@link FileObject}s for context or selected nodes or an empty array, never <code>null</code>.
     * @see #fileForContextOrSelectedNodes(Lookup, FileObject)
     */
    public static FileObject[] filesForContextOrSelectedNodes(Lookup context) {

        FileObject[] files = filesForContext(context);
        if (files.length == 0) {
            files = filesForSelectedNodes();
        }
        return files;
    }

    /**
     * Get a <b>valid</b> {@link FileObject} for context or selected nodes.
     * Return <code>null</code> if any {@link FileObject} is invalid.
     * @param context context to search in.
     * @return a <b>valid</b> {@link FileObject} for context or selected nodes or <code>null</code>.
     * @see #fileForContextOrSelectedNodes(Lookup, FileObject)
     */
    public static FileObject fileForContextOrSelectedNodes(Lookup context) {
        FileObject[] files = filesForContextOrSelectedNodes(context);
        return files.length > 0 ? files[0] : null;
    }

    /**
     * Get {@link URL} for running a project.
     * @param project a project to get {@link URL} for.
     * @return {@link URL} for running a project.
     * @throws MalformedURLException if any error occurs.
     */
    public static URL urlForProject(PhpProject project) throws MalformedURLException {
        FileObject webRoot = ProjectPropertiesSupport.getWebRootDirectory(project);
        FileObject indexFile = fileForProject(project, webRoot);
        return urlForFile(project, webRoot, indexFile);
    }

    /**
     * Get {@link URL} for debugging a project.
     * @param project a project to get {@link URL} for.
     * @return {@link URL} for debugging a project.
     * @throws MalformedURLException if any error occurs.
     */
    public static URL urlForDebugProject(PhpProject project) throws MalformedURLException {
        return urlForDebugProject(project, XDebugUrlArguments.XDEBUG_SESSION_START);
    }
    /**
     * Get {@link URL} for debugging a project.
     * @param project a project to get {@link URL} for.
     * @param xDebugArgument xdebug specific argument for starting or stopping debugging
     * @return {@link URL} for debugging a project.
     * @throws MalformedURLException if any error occurs.
     */
    public static URL urlForDebugProject(PhpProject project, XDebugUrlArguments xDebugArgument) throws MalformedURLException {
        URL debugUrl = urlForProject(project);
        debugUrl = appendQuery(debugUrl, getDebugArguments(xDebugArgument));
        return debugUrl;
    }

    /**
     * Create {@link URL} for debugging from the given {@link URL}.
     * @param url original URL
     * @return {@link URL} for debugging
     * @throws MalformedURLException if any error occurs
     */
    public static URL createDebugUrl(URL url) throws MalformedURLException {
        return createDebugUrl(url, XDebugUrlArguments.XDEBUG_SESSION_START);
    }

    /**
     * Create {@link URL} for debugging from the given {@link URL}.
     * @param url original URL
     * @param xDebugArgument
     * @return {@link URL} for debugging
     * @throws MalformedURLException if any error occurs
     */
    public static URL createDebugUrl(final URL url, final XDebugUrlArguments xDebugArgument) throws MalformedURLException {
        return appendQuery(url, getDebugArguments(xDebugArgument));
    }

    /**
     * Get {@link URL} for running a project context (specific file).
     * @param project a project to get {@link URL} for.
     * @param context a context to get {@link URL} for.
     * @return {@link URL} for running a project context (specific file).
     * @throws MalformedURLException if any error occurs.
     */
    public static URL urlForContext(PhpProject project, Lookup context) throws MalformedURLException {
        FileObject webRoot = ProjectPropertiesSupport.getWebRootDirectory(project);
        FileObject selectedFile = fileForContextOrSelectedNodes(context, webRoot);
        return urlForFile(project, webRoot, selectedFile);
    }

    /**
     * Get {@link URL} for debugging a project context (specific file).
     * @param project a project to get {@link URL} for.
     * @param context a context to get {@link URL} for.
     * @return {@link URL} for debugging a project context (specific file).
     * @throws MalformedURLException if any error occurs.
     */
    public static URL urlForDebugContext(PhpProject project, Lookup context) throws MalformedURLException {
        return urlForDebugContext(project, context, XDebugUrlArguments.XDEBUG_SESSION_START);
    }
    /**
     *
     * Get {@link URL} for debugging a project context (specific file).
     * @param project a project to get {@link URL} for.
     * @param context a context to get {@link URL} for.
     * @param xDebugArgument xdebug specific argument for starting or stopping debugging
     * @return {@link URL} for debugging a project context (specific file).
     * @throws MalformedURLException if any error occurs.
     */
    public static URL urlForDebugContext(PhpProject project, Lookup context, XDebugUrlArguments xDebugArgument) throws MalformedURLException {
        URL debugUrl = urlForContext(project, context);
        debugUrl = appendQuery(debugUrl, getDebugArguments(xDebugArgument));
        return debugUrl;
    }

    /**
     * Get the index file (start file) for a project.
     * @param project a project to get index file for.
     * @param baseDirectory base directory to which is index file resolved (sources, tests, web root).
     * @return the index file (start file) for a project, can be <code>null</code> if file is invalid or not found.
     */
    public static FileObject fileForProject(PhpProject project, FileObject baseDirectory) {
        assert baseDirectory != null;
        assert baseDirectory.isFolder() : "Folder must be given: " + baseDirectory;

        String indexFile = ProjectPropertiesSupport.getIndexFile(project);
        if (indexFile != null) {
            return baseDirectory.getFileObject(indexFile);
        }
        return baseDirectory;
    }

    public static URL getBaseURL(PhpProject project) throws MalformedURLException {
        return getBaseURL(project, false);
    }

    public static URL getBaseURL(PhpProject project, boolean addEndingSlash) throws MalformedURLException {
        String baseURLPath = ProjectPropertiesSupport.getUrl(project);
        if (baseURLPath == null) {
            throw new MalformedURLException();
        }
        if (addEndingSlash && !baseURLPath.endsWith("/")) { // NOI18N
            baseURLPath += "/"; // NOI18N
        }
        return new URL(baseURLPath);
    }

    public static String urlToString(URL url, boolean pathOnly) {
        URI uri;
        try {
            uri = url.toURI();
        } catch (URISyntaxException ex) {
            // fallback:
            LOGGER.log(Level.FINE, "URL ''{0}'' cannot be converted to URI.", url);
            String res = url.toExternalForm();
            int end = res.lastIndexOf('?'); // NOI18N
            if (end == -1) {
                end = res.lastIndexOf('#'); // NOI18N
            }
            if (pathOnly && end != -1) {
                res = res.substring(0, end);
            }
            return res;
        }
        StringBuilder sb = new StringBuilder(100);
        sb.append(uri.getScheme());
        sb.append("://"); // NOI18N
        if (uri.getAuthority() != null) {
            sb.append(uri.getAuthority());
        }
        sb.append(uri.getPath());
        if (!pathOnly && uri.getQuery() != null) {
            sb.append("?"); // NOI18N
            sb.append(uri.getQuery());
        }
        if (!pathOnly && uri.getFragment() != null) {
            sb.append("#"); // NOI18N
            sb.append(uri.getFragment());
        }
        return sb.toString();
    }

    /**
     * Get {@link Command} for given project and command name (identifier).
     * @param project project to get a command for.
     * @param commandName command name (identifier).
     * @return {@link Command} for given project and command name (identifier), never <code>null</code>.
     */
    public static Command getCommand(PhpProject project, String commandName) {
        PhpActionProvider provider = project.getLookup().lookup(PhpActionProvider.class);
        assert provider != null;
        return provider.getCommand(commandName);
    }

    private static Node[] getSelectedNodes() {
        return TopComponent.getRegistry().getCurrentNodes();
    }

    private static URL urlForFile(PhpProject project, FileObject webRoot, FileObject file) throws MalformedURLException {
        String relativePath = null;
        if (file == null) {
            relativePath = ""; // NOI18N
        } else {
            relativePath = FileUtil.getRelativePath(webRoot, file);
            assert relativePath != null : String.format("WebRoot %s must be parent of file %s", webRoot, file);
        }
        URL retval = new URL(getBaseURL(project, StringUtils.hasText(relativePath)), encodeRelativeUrl(relativePath));
        String arguments = ProjectPropertiesSupport.getArguments(project);
        return (arguments != null) ? appendQuery(retval, arguments) : retval;
    }

    // because of unit tests
    static String encodeRelativeUrl(String relativeUrl) {
        if (!StringUtils.hasText(relativeUrl)) {
            return relativeUrl;
        }
        StringBuilder sb = new StringBuilder(relativeUrl.length() * 2);
        try {
            for (String part : StringUtils.explode(relativeUrl, "/")) { // NOI18N
                if (sb.length() > 0) {
                    sb.append('/'); // NOI18N
                }
                sb.append(URLEncoder.encode(part, "UTF-8")); // NOI18N
            }
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
            return relativeUrl;
        }
        return sb.toString();
    }

    private static URL appendQuery(URL originalURL, String queryWithoutQMark) throws MalformedURLException {
        assert StringUtils.hasText(queryWithoutQMark);
        assert !queryWithoutQMark.startsWith("&");
        assert !queryWithoutQMark.startsWith("?");

        String urlExternalForm = originalURL.toExternalForm();
        if (StringUtils.hasText(originalURL.getQuery())) {
            urlExternalForm += "&" + queryWithoutQMark; // NOI18N
        } else {
            urlExternalForm += "?" + queryWithoutQMark; // NOI18N
        }
        return new URL(urlExternalForm);
    }

    private static String getDebugArguments(XDebugUrlArguments xDebugArgument) {
        return xDebugArgument.toString() + "=" + PhpOptions.getInstance().getDebuggerSessionId(); // NOI18N
    }

    private static FileObject[] filterValidFiles(FileObject[] files, FileObject dir) {
        return filterValidFiles(Arrays.asList(files), dir);
    }

    private static FileObject[] filterValidFiles(Collection<? extends FileObject> files, FileObject dir) {
        Collection<FileObject> retval = new LinkedHashSet<>();
        for (FileObject file : files) {
            if (!FileUtil.isParentOf(dir, file) || FileUtil.toFile(file) == null) {
                return null;
            }
            retval.add(file);
        }
        return (!retval.isEmpty()) ? retval.toArray(new FileObject[0]) : null;
    }

}
