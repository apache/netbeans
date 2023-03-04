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
package org.netbeans.modules.web.common.api;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.web.common.spi.ProjectWebRootProvider;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 * Various web utilities
 *
 * @author marekfukala
 */
public class WebUtils {

    private static final Logger LOGGER = Logger.getLogger(WebUtils.class.getName());
    static boolean UNIT_TESTING = false;
    static FileObject WEB_ROOT;

    /**
     * Resolves the relative or absolute link from the base file
     *
     * @param source The base file
     * @param importedFileName the link
     * @return
     */
    public static FileObject resolve(FileObject source, String importedFileName) {
        FileReference ref = resolveToReference(source, importedFileName);
        return ref == null ? null : ref.target();
    }

    /**
     * Resolves the relative or absolute link from the base file
     *
     * @param source The base file
     * @param importedFileName the file link
     * @return FileReference instance which is a reference descriptor
     */
    public static FileReference resolveToReference(FileObject source, String importedFileName) {
        Parameters.notNull("source", source);
        Parameters.notNull("importedFileName", importedFileName);

        //possibly remove the query part of the link
        int qmIndex = importedFileName.indexOf("?"); //NOI18N
        if (qmIndex >= 0) {
            importedFileName = importedFileName.substring(0, qmIndex);
        }
        //possibly remove the fragment part of the link
        int fragmentIndex  = importedFileName.indexOf("#");
        if (fragmentIndex >= 0) {
            importedFileName = importedFileName.substring(0, fragmentIndex);
        }
        
        try {
            URI u = new URI(importedFileName);
            File file = null;

            //does the uri have a scheme component?
            if (u.isAbsolute()) {
                //do refactor only file resources
                if ("file".equals(u.getScheme())) { //NOI18N
                    try {
                        //the IAE is thrown for invalid URIs quite frequently
                        file = new File(u);
                    } catch (IllegalArgumentException iae) {
                        //no-op
                    }
                }
            } else {
                //no schema specified
                file = new File(importedFileName);
            }

            if (file != null) {
                if (!isAbsolute(file, importedFileName)) {
                    //relative to the current file's folder - let's resolve
                    FileObject parent = source.getParent();
                    if(parent != null) {
                        FileObject resolvedFileObject = parent.getFileObject(URLDecoder.decode(importedFileName, "UTF-8")); //NOI18N
                        //test if the link is resolved to something else than the parent file,
                        //which may happen at least in the case of empty importedFileName string
                        if (resolvedFileObject != null &&
                                resolvedFileObject.isValid() &&
                                !resolvedFileObject.equals(parent)) {
                            //normalize the file (may contain xxx/../../yyy parts which
                            //causes that fileobject representing the same file are not equal
                            File resolvedFile = FileUtil.toFile(resolvedFileObject);
                            if(resolvedFile != null) {
                                FileObject resolvedFileObjectInCanonicalForm = FileUtil.toFileObject(resolvedFile);
                                //find out the base folder - bottom most folder of the link
                                FileObject linkBase = findRelativeLinkBase(source, importedFileName);
                                FileReference ref = new FileReference(source, resolvedFileObjectInCanonicalForm, linkBase, importedFileName, FileReferenceType.RELATIVE);
                                return ref;
                            }
                        }
                    }
                } else {
                    //absolute web path
                    FileObject webRoot = ProjectWebRootQuery.getWebRoot(source); //find web root
                    if(UNIT_TESTING) {
                        webRoot = WEB_ROOT;
                    }
                    if(webRoot != null) {
                        //resolve the link relative to the web root
                        String path = file.getAbsolutePath();
                        if (path.length() > webRoot.getPath().length() && webRoot.getPath().equals(path.substring(0, webRoot.getPath().length()))) {
                            path = path.substring(webRoot.getPath().length());
                        }
                        FileObject resolved = webRoot.getFileObject(path);
                        if (resolved != null && resolved.isValid()) {
                            FileReference ref = new FileReference(source, resolved, webRoot, importedFileName, FileReferenceType.ABSOLUTE);
                            return ref;
                        }
                    }
                }
            }

        } catch (URISyntaxException ex) {
            //simply a bad link, return null, no need to report the exception
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.INFO, "Cannot resolve import '" + importedFileName + "' from file " + source.getPath(), e); //NOI18N
        }
        return null;
    }

    //windows File.isAbsolute() workaround
    private static boolean isAbsolute(File file, String link) {
        if(file.isAbsolute()) {
            return true; //will not be true on windows
        } else {
            return link.startsWith("/"); //NOI18N
        }
    }

    private static FileObject findRelativeLinkBase(FileObject source, String link) {
        //Example:
        //
        //  root
        //   +---A
        //       +---file0
        //       +---B
        //           +---C
        //           |   +---file1
        //           |
        //           +---D
        //               +---file2
        //
        //If there is a link ../C/file1 in file2 the bottom most folder is B
        //If there is a link ../../file0 in file2 the bottom most folder is A
        //If there is a link B/C/file1 in file0 the bottom most folder is A
        assert !source.isFolder() : "The source file " + source.getPath() + " is not a folder!"; //NOI18N
        assert !link.startsWith("/") : "The relative link " + link + "starts with a slash!"; //NOI18N
        if(link.startsWith("./")) { //NOI18N
            link = link.substring(2); //cut off the ./
        }
        StringTokenizer st = new StringTokenizer(link, "/");
        FileObject base = source.getParent();
        while(st.hasMoreTokens()) {
            String part = st.nextToken();
            if(part.equals("..")) {
                base = base.getParent();
                if(base == null) {
                    //cannot resolve
                    break;
                }
            } else {
                //we are in the ascending path part, return the current base folder
                return base;
            }
        }
        return null;

    }

    private static int getMimePathSize(ResultIterator ri) {
        return ri.getSnapshot().getMimePath().size();
    }

    /** Finds ResultIterator of the given mimetype with the shortest {@link MimePath}. */
    public static ResultIterator getResultIterator(ResultIterator ri, String mimetype) {
        if (ri.getSnapshot().getMimeType().equals(mimetype)) {
            return ri;
        }

        List<ResultIterator>resultIterators = new ArrayList<ResultIterator>();
        for (Embedding e : ri.getEmbeddings()) {
            ResultIterator eri = ri.getResultIterator(e);
            if (e.getMimeType().equals(mimetype)) {
                // not returned immediately to be able detect mimePaths with equal size
                resultIterators.add(eri);
            } else {
                ResultIterator eeri = getResultIterator(eri, mimetype);
                if (eeri != null) {
                    resultIterators.add(eeri);
                }
            }
        }

        // choses the one with the shortest MimePath
        ResultIterator shortestMimePathRI = null;
        for (ResultIterator resultIterator : resultIterators) {
            if (shortestMimePathRI == null || getMimePathSize(resultIterator) < getMimePathSize(shortestMimePathRI)) {
                shortestMimePathRI = resultIterator;
            } else if (getMimePathSize(resultIterator) == getMimePathSize(shortestMimePathRI)) {
                LOGGER.log(Level.INFO, "Equally long MimePaths for MimeType={0} found: {1}; {2}", new Object[]{
                    mimetype,
                    shortestMimePathRI.getSnapshot().getMimePath().getPath(),
                    resultIterator.getSnapshot().getMimePath().getPath()});
            }
        }
        return shortestMimePathRI;
    }

    public static String unquotedValue(CharSequence value) {
        CharSequence unquoted = isValueQuoted(value) ? value.subSequence(1, value.length() - 1) : value;
        return unquoted.toString();
    }

    public static boolean isValueQuoted(CharSequence value) {
        if (value.length() < 2) {
            return false;
        } else {
            return ((value.charAt(0) == '\'' || value.charAt(0) == '"')
                    && (value.charAt(value.length() - 1) == '\'' || value.charAt(value.length() - 1) == '"'));
        }
    }

    /**
     * Returns a relative path from source to target in one web-like project.
     * ProjectWebRootQuery must return the same folder for both arguments.
     *
     * @param source normalized FileObject in canonical form
     * @param target normalized FileObject in canonical form
     * @return
     */
    public static String getRelativePath(FileObject source, FileObject target) {
        if(!source.isData()) {
            throw new IllegalArgumentException("The source file " + source.getPath() + " is not a data file!");
        }
        if(!target.isData()) {
            throw new IllegalArgumentException("The target file " + target.getPath() + " is not a data file!");
        }

        //link: ../../folder/file.txt
        List<FileObject> targetPathFiles = new ArrayList<FileObject>();
        FileObject file = target;
        while ((file = file.getParent()) != null) {
            assert file.isFolder();
            targetPathFiles.add(0, file);
        }

        //now iterate the target parent's until we find a common folder
        FileObject common = null;
        file = source;
        StringBuilder link = new StringBuilder();
        while ((file = file.getParent()) != null) {
            if (targetPathFiles.contains(file)) {
                common = file;
                break;
            } else {
                link.append("../");//NOI18N
            }
        }
        if (common == null) {
            //no common ancestor
            return null;
        }

        int commonIndexInSourcePath = targetPathFiles.indexOf(common);
        assert commonIndexInSourcePath >= 0;
        assert targetPathFiles.size() > commonIndexInSourcePath;

        for (int i = commonIndexInSourcePath + 1; i < targetPathFiles.size(); i++) {
            FileObject pathMember = targetPathFiles.get(i);
            link.append(pathMember.getNameExt());
            link.append('/'); //NOI18N
        }

        link.append(target.getNameExt());

        return link.toString();
    }

    /**
     * Converts given string into URL with all values properly encoded. May
     * return null if conversion fails.
     */
    public static URL stringToUrl(String urlString) {
        try {
            // #216436:
            // use URL to split the string into individual URI parts first:
            URL u;
            try {
                u = new URL(urlString);
            } catch (MalformedURLException ex) {
                // #219686 - try to use http protocol if protocol is missing:
                if (!(urlString.startsWith("file:/") ||
                    urlString.startsWith("http:/") ||
                    urlString.startsWith("https:/"))) {
                    urlString = "http://" + urlString;
                }
                u = new URL(urlString);
            }

            // do not use URI to encode JAR url and simply return it as is:
            if (urlString.startsWith("jar:")) { // NOI18N
                return u;
            }
            // and now use URI to properly encode spaces in path:
            return new URI(u.getProtocol(), u.getAuthority(), u.getPath(), u.getQuery(), u.getRef()).toURL();
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /**
     * Converts given URL into a String with all values decoded.
     */
    public static String urlToString(URL url) {
        return urlToString(url, false);
    }

    public static String urlToString(URL url, boolean pathOnly) {
        URI uri;
        try {
            uri = url.toURI();
        } catch (URISyntaxException ex) {
            // fallback:
            LOGGER.log(Level.FINE, "URL '"+url+"' cannot be converted to URI.");
            String res = url.toExternalForm();
            int end = res.lastIndexOf('?');
            if (end == -1) {
                end = res.lastIndexOf('#');
            }
            if (pathOnly && end != -1) {
                res = res.substring(0, end);
            }
            return res;
        }
        // do not use URI to encode JAR url and simply return it as is:
        if ("jar".equals(uri.getScheme())) { // NOI18N
            return uri.toASCIIString();
        }
        StringBuilder sb = new StringBuilder();
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
     * Returns IP address of localhost in local network
     * @return
     */
    public static InetAddress getLocalhostInetAddress() {
        try {
            String inetAddr = System.getProperty("localhost.inet.address");
            if (inetAddr!=null) {
                return InetAddress.getByName(inetAddr);
            }
            InetAddress localHost = InetAddress.getLocalHost();
            if (!localHost.isLoopbackAddress()) {
                return localHost;
            }
            //workaround for strange behavior on debian, see #226087
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                final NetworkInterface netInterface = networkInterfaces.nextElement();
                if (netInterface.isUp() && !netInterface.isLoopback() && !netInterface.isVirtual() && !netInterface.getName().startsWith("vbox")) {
                    Enumeration<InetAddress> inetAddresses = netInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress nextElement = inetAddresses.nextElement();
                        if (!nextElement.isLoopbackAddress() && nextElement.isSiteLocalAddress()) {
                            return nextElement;
                        }

                    }
                }
            }
            return localHost;
        } catch (UnknownHostException uhex) {
            throw new IllegalStateException("Cannot resolve local host address, please check your network configuration.", uhex); // NOI18N
        } catch (SocketException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Checks whether the given project has web root.
     * @param project project to be checked
     * @return {@code true} if the given project has web root, {@code false} otherwise
     * @since 1.78
     */
    public static boolean hasWebRoot(Project project) {
        Parameters.notNull("project", project); // NOI18N
        ProjectWebRootProvider webRootProvider = project.getLookup().lookup(ProjectWebRootProvider.class);
        if (webRootProvider == null) {
            return false;
        }
        return !webRootProvider.getWebRoots().isEmpty();
    }

}
