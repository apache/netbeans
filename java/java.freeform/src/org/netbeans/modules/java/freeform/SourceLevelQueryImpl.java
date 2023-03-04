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

package org.netbeans.modules.java.freeform;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Specifies the Java source level (for example 1.4) to use for freeform sources.
 * @author Jesse Glick
 * @author Tomas Zezula
 */
final class SourceLevelQueryImpl implements SourceLevelQueryImplementation2, AntProjectListener {
    
    private AntProjectHelper helper;
    private PropertyEvaluator evaluator;
    private AuxiliaryConfiguration aux;
    
    /**
     * Map from package roots to source levels.
     */
    private Map<FileObject,R> sourceLevels;
    
    public SourceLevelQueryImpl(AntProjectHelper helper, PropertyEvaluator evaluator, AuxiliaryConfiguration aux) {
        this.helper = helper;
        this.evaluator = evaluator;
        this.aux = aux;
        this.helper.addAntProjectListener(this);
    }


    @Override
    public Result getSourceLevel(final FileObject file) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Result>() {
            @Override
            public Result run() {
                final Map<FileObject,R> data = init(true);
                for (Map.Entry<FileObject,R> entry : data.entrySet()) {
                    FileObject root = entry.getKey();
                    if (root == file || FileUtil.isParentOf(root, file)) {
                        return entry.getValue();
                    }
                }
                return null;
            }
        });
    }    
    
    @Override
    public void propertiesChanged(org.netbeans.spi.project.support.ant.AntProjectEvent ev) {
    }

    @Override
    public void configurationXmlChanged(org.netbeans.spi.project.support.ant.AntProjectEvent ev) {
        init(false);
    }


    private Map<FileObject,R> init (final boolean query) {
        final Map<FileObject,R> added = new HashMap<FileObject, R>();
        final Map<FileObject,Object[]> update = new HashMap<FileObject,Object[]>();
        final List<R> remove = new LinkedList<R>();
        Map<FileObject,R> retVal;
        synchronized (this) {
            if (sourceLevels == null) {
                if (query) {
                    //Not initialized + Called by query
                    sourceLevels = new HashMap<FileObject, R>();
                } else {
                    //Not initialized + Called by litener
                    return null;
                }
            } else {
                if (query) {
                    //Initialized + Called by query
                    return Collections.unmodifiableMap(new HashMap<FileObject,R>(sourceLevels));
                }
            }
            Element java = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_LASTEST, true);
            if (java != null) {
                for (Element compilationUnitEl : XMLUtil.findSubElements(java)) {
                    assert compilationUnitEl.getLocalName().equals("compilation-unit") : compilationUnitEl;
                    final String lvl = getLevel(compilationUnitEl);
                    final List<FileObject> packageRoots = Classpaths.findPackageRoots(helper, evaluator, compilationUnitEl);
                    for (FileObject root : packageRoots) {
                        final Result result = sourceLevels.remove(root);
                        if (result != null) {
                            update.put(root, new Object[] {result,lvl});
                        } else {
                            added.put(root,new R(lvl));
                        }
                    }
                }
            }
            remove.addAll(sourceLevels.values());
            sourceLevels.clear();
            sourceLevels.putAll(added);
            for (Map.Entry<FileObject,Object[]> entry : update.entrySet()) {
                sourceLevels.put(entry.getKey(), (R)entry.getValue()[0]);
            }
            retVal = Collections.unmodifiableMap(new HashMap<FileObject,R>(sourceLevels));
        }
        //Fire
        for (Object[] toUpdate : update.values()) {
            ((R)toUpdate[0]).update((String)toUpdate[1]);
        }
        for (R toRemove : remove) {
            toRemove.update(null);
        }
        return retVal;
    }
    
    /**
     * Get the source level indicated in a compilation unit (or null if none is indicated).
     */
    private String getLevel(Element compilationUnitEl) {
        Element sourceLevelEl = XMLUtil.findElement(compilationUnitEl, "source-level", JavaProjectNature.NS_JAVA_LASTEST);
        if (sourceLevelEl != null) {
            return XMLUtil.findText(sourceLevelEl);
        } else {
            return null;
        }
    }

    private static class R implements Result {

        private final ChangeSupport changeSuppport = new ChangeSupport(this);
        private volatile String sourceLevel;

        private R(final String sourceLevel) {
            this.sourceLevel = sourceLevel;
        }

        private void update(final String sourceLevel) {
            this.sourceLevel = sourceLevel;
            this.changeSuppport.fireChange();
        }

        @Override
        public String getSourceLevel() {
            return sourceLevel;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            this.changeSuppport.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            this.changeSuppport.removeChangeListener(listener);
        }

    }
    
}
