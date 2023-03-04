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

package org.netbeans.modules.maven.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.spi.queries.ForeignClassBundler;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * SourceForBinary and JavadocForBinary query impls.
 * @author  Milos Kleint 
 */
@ProjectServiceProvider(service={SourceForBinaryQueryImplementation.class, SourceForBinaryQueryImplementation2.class, JavadocForBinaryQueryImplementation.class}, projectType="org-netbeans-modules-maven")
public class MavenForBinaryQueryImpl extends AbstractMavenForBinaryQueryImpl {
    
    private final Project p;
    private final HashMap<String, BinResult> map;
    private static final Logger LOGGER = Logger.getLogger(MavenForBinaryQueryImpl.class.getName());
    

    public MavenForBinaryQueryImpl(Project proj) {
        p = proj;
        map = new HashMap<String, BinResult>();
        NbMavenProject.addPropertyChangeListener(proj, new PropertyChangeListener() {
            public @Override void propertyChange(PropertyChangeEvent event) {
                if (NbMavenProject.PROP_PROJECT.equals(event.getPropertyName())) {
                    if (p.getLookup().lookup(NbMavenProject.class).isUnloadable()) {
                        return; //let's just continue with the old value, reloading classpath for broken project and re-creating it later serves no greater good.
                    }
                    ForeignClassBundler bundler = p.getLookup().lookup(ForeignClassBundler.class);
                    boolean oldprefer = bundler.preferSources();
                    bundler.resetCachedValue();
                    boolean preferChanged = oldprefer != bundler.preferSources();
                    synchronized (map) {
                        for (BinResult res : map.values()) {
                            FileObject[] cached = res.getCached();
                            res.cached = null; // force refresh to see if we have to fire changes
                            FileObject[] current = res.getRoots();
                            if (preferChanged || !Arrays.equals(cached, current)) {
                                LOGGER.log(Level.FINE, "SFBQ.Result changed from {0} to {1}", new Object[]{Arrays.toString(cached), Arrays.toString(current)});
                                res.fireChanged();
                            }
                        }
                    }
                }
            }
        });
    }

    public @Override SourceForBinaryQueryImplementation2.Result findSourceRoots2(URL url) {
        synchronized (map) {
            BinResult toReturn = map.get(url.toString());
            if (toReturn != null) {
                return toReturn;
            }
            if (url.getProtocol().equals("file")) { //NOI18N
                int result = checkURL(url);
                if (result == 1 || result == 0) {
                    toReturn = new BinResult(url);
                }
            }
            if (toReturn != null) {
                map.put(url.toString(), toReturn);
            }
            return toReturn;
        }
    }
    
    /**
     * Find any Javadoc corresponding to the given classpath root containing
     * Java classes.
     * <p>
     * Any absolute URL may be used but typically it will use the <code>file</code>
     * protocol for directory entries and <code>jar</code> protocol for JAR entries
     * (e.g. <samp>jar:file:/tmp/foo.jar!/</samp>).
     * </p>
     * @param binaryRoot the class path root of Java class files
     * @return a result object encapsulating the roots and permitting changes to
     *         be listened to, or null if the binary root is not recognized
     */
    public @Override JavadocForBinaryQuery.Result findJavadoc(URL url) {
        if (checkURL(url) != -1) {
            return new DocResult(url);
        }
        return null;
    }
    
    /**
     * -1 - not found
     * 0 - source
     * 1 - test source
     */
    protected int checkURL(URL url) {
        NbMavenProjectImpl project = p.getLookup().lookup(NbMavenProjectImpl.class);
        if ("file".equals(url.getProtocol())) { //NOI18N
            // true for directories.
            if (url.equals(FileUtil.urlForArchiveOrDir(project.getProjectWatcher().getOutputDirectory(false)))) {
                return 0;
            } else if (url.equals(FileUtil.urlForArchiveOrDir(project.getProjectWatcher().getOutputDirectory(true)))) {
                return 1;
            } else {
                return -1;
            }
        }
            return -1;
        }
    
    private URL[] getJavadocRoot() {
        //TODO shall we delegate to "possibly" generated javadoc in project or in site?
        return new URL[0];
    }
    
    
    private class BinResult implements SourceForBinaryQueryImplementation2.Result  {
        private URL url;
        private final List<ChangeListener> listeners;
        private FileObject[] results;
        private FileObject[] cached = null;
        
        public BinResult(URL urlParam) {
            url = urlParam;
            listeners = new ArrayList<ChangeListener>();
        }
        
        public @Override FileObject[] getRoots() {
            if(cached != null) {
                return cached;
            }

            int xxx = checkURL(url);
            if (xxx == 0) {
                results = getProjectSrcRoots(p);
            } else if (xxx == 1) {
                results = getProjectTestSrcRoots(p);
            } else {
                results = new FileObject[0];
            }
//            System.out.println("src bin result for =" + url + " length=" + results.length);
            cached = results;
            return results;
        }
        
        public FileObject[] getCached() {
            return cached;
        }
        
        public @Override void addChangeListener(ChangeListener changeListener) {
            synchronized (listeners) {
                listeners.add(changeListener);
            }
        }
        
        public @Override void removeChangeListener(ChangeListener changeListener) {
            synchronized (listeners) {
                listeners.remove(changeListener);
            }
        }
        
        void fireChanged() {
            List<ChangeListener> lists = new ArrayList<ChangeListener>();
            synchronized(listeners) {
                lists.addAll(listeners);
            }
            for (ChangeListener listen : lists) {
                listen.stateChanged(new ChangeEvent(this));
            }
        }

        @Override public boolean preferSources() {
            if ("file".equals(url.getProtocol())) { //#215242
                return true;
            }
            return p.getLookup().lookup(ForeignClassBundler.class).preferSources();
        }
        
    }
    
    private class DocResult implements JavadocForBinaryQuery.Result  {
        private URL url;
        private URL[] results;
        private final List<ChangeListener> listeners;
        
        public DocResult(URL urlParam) {
            url = urlParam;
            listeners = new ArrayList<ChangeListener>();
        }
        public @Override void addChangeListener(ChangeListener changeListener) {
            synchronized (listeners) {
                listeners.add(changeListener);
            }
        }
        
        public @Override void removeChangeListener(ChangeListener changeListener) {
            synchronized (listeners) {
                listeners.remove(changeListener);
            }
        }
        
        void fireChanged() {
            List<ChangeListener> lists = new ArrayList<ChangeListener>();
            synchronized(listeners) {
                lists.addAll(listeners);
            }
            for (ChangeListener listen : lists) {
                listen.stateChanged(new ChangeEvent(this));
            }
        }
        
        public @Override URL[] getRoots() {
            if (checkURL(url) != -1) {
                results = getJavadocRoot();
            } else {
                results = new URL[0];
            }
            return results;
        }
        
    }
    
}
