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
package org.netbeans.modules.java.platform.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.modules.SpecificationVersion;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;


/**
 * This implementation of the SourceForBinaryQueryImplementation
 * provides sources for the active platform.
 */

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation.class, position=90)
public class PlatformSourceForBinaryQuery implements SourceForBinaryQueryImplementation2 {

    private static final Logger LOG = Logger.getLogger(PlatformSourceForBinaryQuery.class.getName());

    private static final String JAR_FILE = "jar:file:";                 //NOI18N
    private static final String RTJAR_PATH = "/jre/lib/rt.jar!/";       //NOI18N
    private static final String SRC_ZIP = "/src.zip";                    //NOI18N

    private Map<URL,SourceForBinaryQueryImplementation2.Result> cache = new HashMap<>();

    public PlatformSourceForBinaryQuery () {
    }

    /**
     * Tries to locate the source root for given classpath root.
     * @param binaryRoot the URL of a classpath root (platform supports file and jar protocol)
     * @return FileObject[], never returns null
     */
    @Override
    public SourceForBinaryQueryImplementation2.Result findSourceRoots2(URL binaryRoot) {
        SourceForBinaryQueryImplementation2.Result res = this.cache.get (binaryRoot);
        if (res != null) {
            return res;
        }
        final JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        final Collection<JavaPlatform> candidates = new ArrayDeque<>();
        for (JavaPlatform platform : jpm.getInstalledPlatforms()) {
            if (contains(platform, binaryRoot)) {
                candidates.add(platform);
            }
        }
        if (!candidates.isEmpty()) {
            res = new Result(
                jpm,
                binaryRoot,
                candidates);
            this.cache.put (binaryRoot, res);
            return res;
        }
        return searchUnregisteredPlatform(binaryRoot.toExternalForm());
    }

    static SourceForBinaryQueryImplementation2.Result searchUnregisteredPlatform(String binaryRootS) {
        String srcZipS = null;
        String srcZipIn = null;
        if (binaryRootS.startsWith(JAR_FILE)) {
            if (binaryRootS.endsWith(RTJAR_PATH)) {
                //Unregistered platform
                srcZipS = binaryRootS.substring(4, binaryRootS.length() - RTJAR_PATH.length()) + SRC_ZIP;
            }
        } else if (binaryRootS.startsWith("nbjrt:")) {
            int end = binaryRootS.indexOf('!');
            if (end >= 0) {
                srcZipS = binaryRootS.substring(6, end) + "lib/" + SRC_ZIP;
                String reminder = binaryRootS.substring(end + 1);
                final String prefix = "/modules/";
                if (reminder.startsWith(prefix)) {
                    srcZipIn = reminder.substring(prefix.length());
                }
            }
        }
        if (srcZipS != null) {
            try {
                URL srcZip = FileUtil.getArchiveRoot(new URL(srcZipS));
                FileObject fo = URLMapper.findFileObject(srcZip);
                if (fo != null) {
                    if (srcZipIn != null) {
                        fo = fo.getFileObject(srcZipIn);
                    }
                    if (fo != null) {
                        return new UnregisteredPlatformResult(fo);
                    }
                }
            } catch (MalformedURLException mue) {
                Exceptions.printStackTrace(mue);
            }
        }
        return null;
    }

    @Override
    public SourceForBinaryQuery.Result findSourceRoots (URL binaryRoot) {
        return this.findSourceRoots2(binaryRoot);
    }

    static boolean contains(
        @NonNull final JavaPlatform platform,
        @NonNull final URL artifact) {
        for (ClassPath.Entry entry : platform.getBootstrapLibraries().entries()) {
            if (entry.getURL().equals (artifact)) {
                return true;
            }
        }
        return false;
    }

    private static final class Result implements SourceForBinaryQueryImplementation2.Result, PropertyChangeListener {
        private static final String J2SE = "j2se";  //NOI18N
        private static final SpecificationVersion JAVA_9 = new SpecificationVersion("9");   //NOI18N

        private final JavaPlatformManager jpm;
        private final URL artifact;
        private final ChangeSupport cs = new ChangeSupport(this);
        //@GuardedBy("this")
        private Map<JavaPlatform,PropertyChangeListener> platforms;


        public Result (
            @NonNull final JavaPlatformManager jpm,
            @NonNull final URL artifact,
            @NonNull final Collection<? extends JavaPlatform> platforms) {
            Parameters.notNull("jpm", jpm); //NOI18N
            Parameters.notNull("artifact", artifact);   //NOI18N
            Parameters.notNull("platforms", platforms); //NOI18N
            this.jpm = jpm;
            this.artifact = artifact;
            synchronized (this) {
                this.platforms = new LinkedHashMap<>();
                for (JavaPlatform platform : platforms) {
                    final PropertyChangeListener l = WeakListeners.propertyChange(this, platform);
                    platform.addPropertyChangeListener(l);
                    this.platforms.put(platform, l);
                }
                this.jpm.addPropertyChangeListener(WeakListeners.propertyChange(this, this.jpm));
            }
        }

        @Override
        @NonNull
        public FileObject[] getRoots () {       //No need for caching, platforms does.
            for (JavaPlatform platform : platforms.keySet()) {
                final ClassPath sourcePath = platform.getSourceFolders();
                final FileObject[] sourceRoots = sourcePath.getRoots();
                if (sourceRoots.length > 0) {
                    if (isModular(platform)) {
                        final String moduleName = getModuleName(artifact);
                        if (moduleName != null) {
                            FileObject moduleRoot = null;
                            for (FileObject sourceRoot : sourceRoots) {
                                if (moduleName.equals(sourceRoot.getNameExt())) {
                                    moduleRoot = sourceRoot;
                                    break;
                                }
                            }
                            if (moduleRoot != null) {
                                return new FileObject[] {moduleRoot};
                            }
                        }
                    }
                    return sourceRoots;
                }
            }
            return new FileObject[0];
        }

        @Override
        public void addChangeListener (@NonNull final ChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            cs.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener (@NonNull final ChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            cs.removeChangeListener(listener);
        }

        @Override
        public void propertyChange (@NonNull final PropertyChangeEvent event) {
            if (JavaPlatform.PROP_SOURCE_FOLDER.equals(event.getPropertyName())) {
                cs.fireChange();
            } else if (JavaPlatformManager.PROP_INSTALLED_PLATFORMS.equals(event.getPropertyName())) {
                if (updateCandidates()) {
                    cs.fireChange();
                }
            }
        }

        @Override
        public boolean preferSources() {
            return false;
        }

        private synchronized boolean updateCandidates() {
            boolean affected = false;
            final JavaPlatform[] newPlatforms = jpm.getInstalledPlatforms();
            final Map<JavaPlatform, PropertyChangeListener> oldPlatforms = new HashMap<>(platforms);
            final Map<JavaPlatform, PropertyChangeListener> newState = new LinkedHashMap<>(newPlatforms.length);
            for (JavaPlatform jp : newPlatforms) {
                PropertyChangeListener l;
                if ((l=oldPlatforms.remove(jp))!=null) {
                    newState.put(jp,l);
                } else if (contains(jp,artifact)) {
                    affected = true;
                    l = WeakListeners.propertyChange(this, this.jpm);
                    jp.addPropertyChangeListener(l);
                    newState.put(jp,l);
                }
            }
            for (Map.Entry<JavaPlatform,PropertyChangeListener> e : oldPlatforms.entrySet()) {
                affected = true;
                e.getKey().removePropertyChangeListener(e.getValue());
            }
            platforms = newState;
            return affected;
        }

        //Todo: SPI will be required when more platforms than J2SE will be modular
        private static boolean isModular(@NonNull final JavaPlatform platform) {
            final Specification spec = platform.getSpecification();
            return J2SE.equals(spec.getName()) && JAVA_9.compareTo(spec.getVersion()) <= 0;
        }

        @CheckForNull
        private String getModuleName(URL root) {
            try {
                final String[] nameComponents = root.getPath().split("/");  //NOI18N
                if (nameComponents.length > 0) {
                    return URLDecoder.decode(nameComponents[nameComponents.length-1], "UTF-8");    //NOI18N
                }
            } catch (UnsupportedEncodingException e) {
                LOG.warning(e.getMessage());
            }
            return null;
        }

    }

    private static class UnregisteredPlatformResult implements SourceForBinaryQueryImplementation2.Result {

        private final FileObject srcRoot;

        private UnregisteredPlatformResult (FileObject fo) {
            Parameters.notNull("fo", fo);   //NOI18N
            srcRoot = fo;
        }

        @Override
        public FileObject[] getRoots() {
            return srcRoot.isValid() ? new FileObject[] {srcRoot} : new FileObject[0];
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            //Not supported, no listening.
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            //Not supported, no listening.
        }

        @Override
        public boolean preferSources() {
            return false;
        }
    }}

