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
package org.netbeans.modules.php.dbgp;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Utilities;

/**
 * Converts remote (or also local) URI to local project File an vice versa.
 *
 * @author Radek Matous
 */
abstract class URIMapper {
    private static final Logger LOGGER = Logger.getLogger(URIMapper.class.getName());

    abstract File toSourceFile(URI remoteURI);

    abstract URI toWebServerURI(File localFile, boolean includeHostPart);

    URI toWebServerURI(File localFile) {
        return toWebServerURI(localFile, true);
    }

    static URIMapper.MultiMapper createMultiMapper(URI webServerURI, FileObject sourceFileObj,
            FileObject sourceRoot, List<Pair<String, String>> pathMapping) {
        //typicaly should be mappers called in this order:
        //- 1. mapper provided by user via project UI if any
        //- 2. base mapper
        //- 3. last resort mapper (one to one mapper)
        //TODO: we could also implement mapper with UI asking the user to add info for
        //mapping instead of lsat resort mapper implemented by one to one
        MultiMapper mergedMapper = new MultiMapper();
        for (Pair<String, String> pair : pathMapping) {
            //1. mapper provided by user via project UI if any
            pair = encodedPathMappingPair(pair);
            String uriPath = pair.first();
            String filePath = pair.second();
            if (uriPath.length() > 0 && filePath.length() > 0) {
                if (!uriPath.startsWith("file:")) { //NOI18N
                    if (!uriPath.startsWith("/")) {
                        uriPath = "file:/" + uriPath; //NOI18N
                    } else {
                        uriPath = "file:" + uriPath; //NOI18N
                    }
                }
                if (!uriPath.endsWith("/")) { //NOI18N
                    uriPath += "/"; //NOI18N
                }
                URI remoteURI = URI.create(uriPath);
                File localFile = new File(filePath);
                FileObject localFo = FileUtil.toFileObject(localFile);
                if (localFo != null && localFo.isFolder()) {
                    URIMapper customMapper = URIMapper.createBasedInstance(remoteURI, localFile);
                    mergedMapper.addAsLastMapper(customMapper);
                }
            }
        }

        //2. base mapper that checks sourceFileObj && webServerURI to create webServerURIBase and  sourceFileObjBase
        //used for conversions
        URIMapper defaultMapper = createDefaultMapper(webServerURI, sourceFileObj, sourceRoot);
        if (defaultMapper != null) {
            mergedMapper.addAsLastMapper(defaultMapper);
        }
        //3. last resort just one to one mapper (should be called as last)
        mergedMapper.addAsLastMapper(createOneToOne());

        return mergedMapper;
    }

    static URIMapper createDefaultMapper(URI webServerURI, FileObject sourceFileObj, FileObject sourceRoot) {
        if (sourceRoot == null) {
            return null;
        }
        if (!"file".equals(webServerURI.getScheme())) { //NOI18N
            return null;
        }
        File webServerFile = Utilities.toFile(webServerURI);
        File sourceFile = FileUtil.toFile(sourceFileObj);
        String sourcePath = FileUtil.getRelativePath(sourceRoot, sourceFileObj);
        //debugged file must be part of the debugged project (for now)
        if (sourcePath != null) {
            if (sourceFile.isDirectory()) {
                //TODO: not sure about this (should be reviewed)
                sourceFile = new File(sourceFile, webServerFile.getName());
                if (!sourceFile.exists()) {
                    LOGGER.log(
                            Level.FINE,
                            "No default path mapping: " + "webServerURI: {0} sourceFile: {1}", //NOI18N
                            new Object[]{webServerURI.toString(),
                                sourceFile.getAbsolutePath()});
                    return null;
                }
            }
            if (!sourceFile.equals(webServerFile)) {
                File sourceRootFile = FileUtil.toFile(sourceRoot);
                assert sourceRootFile != null;
                URI[] bases = findBases(webServerURI, sourceFile, sourceRootFile);
                if (bases.length > 0) {
                    URI webServerBase = bases[0];
                    File sourceBase = Utilities.toFile(bases[1]);
                    assert webServerBase != null;
                    assert sourceBase != null;
                    return new BaseMapper(webServerBase, sourceBase);
                }
            }
        }
        //no decision how to map - must exist user's defined mapping
        LOGGER.log(
                Level.FINE,
                "No default path mapping: " + "webServerURI: {0} sourceFile: {1}", //NOI18N
                new Object[]{webServerURI.toString(),
                    sourceFile.getAbsolutePath()});
        return null;
    }

    static URIMapper createOneToOne() {
        return new URIMapper() {
            private Map<File, File> can2AbsFile = new HashMap<>();

            @Override
            File toSourceFile(URI remoteURI) {
                File retval = Utilities.toFile(remoteURI);
                File absFile = can2AbsFile.get(retval);
                retval = (absFile != null) ? absFile : retval;
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, String.format("%s: %s -> %s", getClass().toString(), remoteURI, retval));
                }
                return retval;
            }

            @Override
            URI toWebServerURI(File localFile, boolean includeHostPart) {
                File canonicalFile = null;
                try {
                    canonicalFile = localFile.getCanonicalFile();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (!localFile.equals(canonicalFile)) {
                    can2AbsFile.put(canonicalFile, localFile);
                    localFile = canonicalFile;
                }
                final URI retval = toURI(localFile, includeHostPart);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, String.format("%s: %s -> %s", getClass().toString(), localFile, retval));
                }
                return retval;
            }
        };
    }

    static URIMapper createBasedInstance(URI baseRemoteURI, File baseLocalFolder) {
        return new BaseMapper(baseRemoteURI, baseLocalFolder);
    }

    private static URI[] findBases(URI webServerURI, File sourceFile, File sourceRoot) {
        File baseFile = sourceFile;
        boolean nullRetVal = true;
        List<String> pathFragments = new ArrayList<>();
        Collections.addAll(pathFragments, webServerURI.getPath().split("/")); //NOI18N
        Collections.reverse(pathFragments);
        for (String path : pathFragments) {
            if (baseFile != null && path.equals(baseFile.getName()) && !baseFile.equals(sourceRoot)) {
                nullRetVal = false;
                if (baseFile.equals(sourceRoot)) {
                    break;
                }
                baseFile = baseFile.getParentFile();
            } else {
                break;
            }
        }
        if (nullRetVal) {
            return new URI[0];
        }
        assert baseFile.isDirectory();
        int basePathLen = webServerURI.getPath().length()
                - (sourceFile.getAbsolutePath().length() - baseFile.getAbsolutePath().length());
        String basePath = webServerURI.getPath().substring(0, basePathLen);
        URI baseURI = createURI(webServerURI.getScheme(), webServerURI.getHost(),
                basePath, webServerURI.getFragment(),
                true, true);
        return new URI[]{baseURI, Utilities.toURI(baseFile)};
    }

    private static class BaseMapper extends URIMapper {
        private static final String FILE_SCHEME = "file";
        private URI baseWebServerURI;
        private URI baseSourceURI;
        private File baseSourceFolder;

        BaseMapper(URI baseWebServerURI, File baseSourceFolder) {
            if (!baseSourceFolder.exists()) {
                throw new IllegalArgumentException();
            }
            if (!baseSourceFolder.isDirectory()) {
                throw new IllegalArgumentException();
            }

            this.baseSourceFolder = baseSourceFolder;
            this.baseWebServerURI = baseWebServerURI;
            boolean isLoggable = LOGGER.isLoggable(Level.FINE);
            if (isLoggable) {
                if (!FILE_SCHEME.equals(baseWebServerURI.getScheme())) {
                    LOGGER.log(Level.FINE, "Unexpected scheme: {0}", baseWebServerURI.toString()); //NOI18N
                }
                if (baseWebServerURI.getPath() == null) {
                    LOGGER.log(Level.FINE, "URI.getPath() == null: {0}", baseWebServerURI.toString()); //NOI18N
                }
                if (baseWebServerURI.getPath() == null) {
                    LOGGER.log(Level.FINE, "URI.getPath() == null: {0}", baseWebServerURI.toString()); //NOI18N
                } else if (!baseWebServerURI.getPath().endsWith("/")) {
                    LOGGER.log(Level.FINE, "Not \"/\" at the end of URI.getPath(): {0}", baseWebServerURI.toString()); //NOI18N
                }
                if (!baseWebServerURI.isAbsolute()) {
                    LOGGER.log(Level.FINE, "URI not absolute: {0}", baseWebServerURI.toString()); //NOI18N
                }
            }
            assert FILE_SCHEME.equals(baseWebServerURI.getScheme());
            assert baseWebServerURI.getPath() != null;
            assert baseWebServerURI.getPath().endsWith("/") : baseWebServerURI.getPath(); //NOI18N
            assert baseWebServerURI.isAbsolute();
            this.baseSourceURI = Utilities.toURI(baseSourceFolder);
            assert baseSourceURI.isAbsolute();
        }

        @Override
        File toSourceFile(URI webServerURI) {
            URI relativizedURI = baseWebServerURI.relativize(webServerURI);
            File retval = null;
            if (!relativizedURI.isAbsolute()) {
                assert FILE_SCHEME.equals(webServerURI.getScheme());
                retval = Utilities.toFile(baseSourceURI.resolve(relativizedURI));
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, String.format("%s: %s -> %s", getClass().toString(), webServerURI, retval));
            }
            return retval;
        }

        @Override
        URI toWebServerURI(File sourceFile, boolean includeHostPart) {
            URI retval = null;
            if (sourceFile.equals(baseSourceFolder)) {
                retval = baseWebServerURI;
            } else {
                URI relativizedURI = baseSourceURI.relativize(Utilities.toURI(sourceFile));
                if (!relativizedURI.isAbsolute()) {
                    URI uri = baseWebServerURI.resolve(relativizedURI);
                    retval = createURI(uri.getScheme(), uri.getHost(),
                            uri.getPath(), uri.getFragment(),
                            true, false);
                }
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, String.format("%s: %s -> %s", getClass().toString(), sourceFile, retval));
            }
            return retval;
        }

    }

    static class MultiMapper extends URIMapper {
        private LinkedList<URIMapper> mappers = new LinkedList<>();

        MultiMapper addAsFirstMapper(URIMapper mapper) {
            mappers.addFirst(mapper);
            return this;
        }

        MultiMapper addAsLastMapper(URIMapper mapper) {
            mappers.addLast(mapper);
            return this;
        }

        @Override
        File toSourceFile(URI remoteURI) {
            if ("file".equals(remoteURI.getScheme())) { //NOI18N
                for (URIMapper mapperInstance : mappers) {
                    File sourceFile = mapperInstance.toSourceFile(remoteURI);
                    if (sourceFile != null) {
                        return sourceFile;
                    }
                }
            }
            return null;
        }

        @Override
        URI toWebServerURI(File localFile, boolean includeHostPart) {
            for (URIMapper mapperInstance : mappers) {
                URI toWebServerURI = mapperInstance.toWebServerURI(localFile, includeHostPart);
                if (toWebServerURI != null) {
                    return toWebServerURI;
                }
            }
            return null;
        }

    }

    private static URI createURI(String scheme, String host, String path, String fragment,
            boolean includeHostPart, boolean pathEndsWithSlash) {
        if (pathEndsWithSlash && !path.endsWith("/")) { //NOI18N
            path = path + "/"; //NOI18N
        }
        if (host == null && includeHostPart) {
            host = ""; //NOI18N
        }
        try {
            return new URI(scheme, host, path, fragment);
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    private static URI toURI(File webServerBase, boolean includeHostPart) {
        URI webServerBaseURI = Utilities.toURI(webServerBase);
        return createURI(webServerBaseURI.getScheme(), webServerBaseURI.getHost(),
                webServerBaseURI.getPath(), webServerBaseURI.getFragment(),
                includeHostPart, webServerBase.exists() && webServerBase.isDirectory());
    }

    private static Pair<String, String> encodedPathMappingPair(Pair<String, String> pathMapping) {
        String resName = pathMapping.first();
        resName = resName.replace('\\', '/'); //NOI18N
        final String[] elements = resName.split("/"); //NOI18N
        final StringBuilder sb = new StringBuilder(200);
        for (int i = 0; i < elements.length; i++) {
            String element = elements[i];
            boolean skip = false;
            if (i == 0 && element.length() == 2 && element.charAt(1) == ':') { //NOI18N
                skip = true;
            }
            if (!skip) {
                try {
                    element = URLEncoder.encode(element, "UTF-8"); // NOI18N
                    element = element.replace("+", "%20"); // NOI18N
                } catch (UnsupportedEncodingException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            sb.append(element);
            if (i < elements.length - 1) {
                sb.append('/');
            }
        }
        return Pair.of(sb.toString(), pathMapping.second());
    }

}
