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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.AnnotationProcessingQuery;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Result;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Trigger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Freeform implementation of  AnnotationProcessingQueryImplementation
 */
class AnnotationProcessingQueryImpl implements AnnotationProcessingQueryImplementation, AntProjectListener {

    static final String EL_ANNOTATION_PROCESSING = "annotation-processing"; //NOI18N
    static final String EL_PROCESSOR_PATH = "processor-path"; //NOI18N
    private static final String EL_SCAN_TRIGGER = "scan-trigger";   //NOI18N
    private static final String EL_EDITOR_TRIGGER = "editor-trigger"; //NOI18N
    private static final String EL_PROCESSOR = "processor"; //NOI18N
    private static final String EL_PROCESSOR_OPTION = "processor-option";  //NOI18N
    private static final String EL_SOURCE_OUTPUT ="source-output"; //NOI18N
    
    private static final Logger LOG = Logger.getLogger(AnnotationProcessingQueryImpl.class.getName());

    private final Object LCK = new Object();

    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final AuxiliaryConfiguration aux;
    private Map<FileObject, R> results;

    @SuppressWarnings("LeakingThisInConstructor")
    public AnnotationProcessingQueryImpl(AntProjectHelper helper, PropertyEvaluator eval, final AuxiliaryConfiguration aux) {
        assert helper != null;
        assert eval != null;
        assert aux != null;
        this.helper = helper;
        this.eval = eval;
        this.aux = aux;
        this.helper.addAntProjectListener(this);
    }

    public @Override Result getAnnotationProcessingOptions(final FileObject file) {
        assert file != null;
        return ProjectManager.mutex().readAccess(new Mutex.Action<Result>() {
            public Result run() {
                synchronized (LCK) {
                    init(true);
                    assert results != null;
                    for (Map.Entry<FileObject,R> entry : results.entrySet()) {
                        FileObject fo = entry.getKey();
                        if (fo.equals(file) || FileUtil.isParentOf(fo, file)) {
                            return entry.getValue();
                        }
                    }
                    return null;
                }
            }
        });
    }

    @Override
    public void configurationXmlChanged(AntProjectEvent ev) {
        init (false);
    }

    @Override
    public void propertiesChanged(AntProjectEvent ev) {
        //pass
    }

    private void init(final boolean force) {
        synchronized (LCK) {
            if (results == null) {
                if (force) {
                    results = new HashMap<FileObject, R>();
                } else {
                    return;
                }
            }
            final Map<FileObject,R> added = new HashMap<FileObject, R>();
            final Map<FileObject,R> retained = new HashMap<FileObject, R>();

            final Element java = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_LASTEST, true);
            if (java != null) {
                for (Element compilationUnit : XMLUtil.findSubElements(java)) {
                    assert compilationUnit.getLocalName().equals("compilation-unit") : compilationUnit;
                    final List<FileObject> packageRoots = Classpaths.findPackageRoots(helper, eval, compilationUnit);
                    for (FileObject source : packageRoots) {
                        final R r = results.remove(source);
                        if (r == null) {
                            // Create a new result
                            added.put(source, new R(compilationUnit));
                        } else {
                            //Recalculate and fire
                            retained.put(source,r);
                            r.update(compilationUnit);
                        }
                    }
                }
            }

            //Invalidate results for remvoed roots
            for (R r : results.values()) {
                r.update(null);
            }
            results.putAll(added);
            results.putAll(retained);
        }
    }



    private class R implements AnnotationProcessingQuery.Result {

        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private volatile Element ap;
        private volatile Set<Trigger> triggerCache;
        private volatile Map<String,String> optionsCache;
        private volatile Collection<String> processorsCache;
        private volatile List<URL> sourceOutputCache;

        private R(final Element cu) {
            assert cu != null;
            this.ap = findAP(cu);
        }

        public @Override Set<? extends Trigger> annotationProcessingEnabled() {
            Set<Trigger> result = triggerCache;
            if (result != null) {
                return result;
            }
            result = EnumSet.noneOf(Trigger.class);
            if (ap != null) {
                for (Element e : XMLUtil.findSubElements(ap)) {
                    if (e.getLocalName().equals(EL_SCAN_TRIGGER)) {
                        result.add(Trigger.ON_SCAN);
                    } else if (e.getLocalName().equals(EL_EDITOR_TRIGGER)) {
                        result.add(Trigger.IN_EDITOR);
                    }
                }
            }
            if (result.size() == 1 && result.contains(Trigger.IN_EDITOR)) {
                //Contains only editor-trigger
                //Add required scan-trigger and log
                result.add(Trigger.ON_SCAN);
                LOG.log(
                    Level.WARNING, 
                    "Project {0} project.xml contains annotation processing editor-trigger only. The scan-trigger is required as well, adding it into the APQ.Result.", //NOI18N
                    FileUtil.getFileDisplayName(helper.getProjectDirectory()));
            }
            synchronized (LCK) {
                if (triggerCache == null) {
                    triggerCache = result;
                }
            }
            return result;
        }

        public @Override Iterable<? extends String> annotationProcessorsToRun() {
            Collection<String> result = processorsCache;
            if (result != null) {
                return result.isEmpty() ? null : result;
            }
            result = new ArrayList<String>();
            if (ap != null) {
                for (Element e : XMLUtil.findSubElements(ap)) {
                    if (e.getLocalName().equals(EL_PROCESSOR)) {
                        result.add(eval.evaluate(XMLUtil.findText(e)));
                    }
                }
            }
            synchronized (LCK) {
                if (processorsCache == null) {
                    processorsCache = result;
                }
            }
            return result.isEmpty() ? null : result;
        }


        public @Override URL sourceOutputDirectory() {
            List<URL> result = sourceOutputCache;
            if (result != null) {
                return result.isEmpty() ? null : result.get(0);
            }
            result = new ArrayList<URL>(1);
            if (ap != null) {
                for (Element e : XMLUtil.findSubElements(ap)) {
                    if (e.getLocalName().equals(EL_SOURCE_OUTPUT)) {
                        try {
                            final String path = eval.evaluate(XMLUtil.findText(e));
                            result.add(Utilities.toURI(helper.resolveFile(path)).toURL());
                        } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        break;
                    }
                }
            }
            synchronized (LCK) {
                if (sourceOutputCache == null) {
                    sourceOutputCache = result;
                }
            }
            return result.isEmpty() ? null : result.get(0);
        }

        public @Override Map<? extends String, ? extends String> processorOptions() {
            Map<String,String> result = optionsCache;
            if (result != null) {
                return result;
            }
            result = new HashMap<String, String>();
            if (ap != null) {
                for (Element e : XMLUtil.findSubElements(ap)) {
                    if (e.getLocalName().equals(EL_PROCESSOR_OPTION)) {
                        final Element keyElement = XMLUtil.findElement(e, "key", JavaProjectNature.NS_JAVA_LASTEST);  //NOI18N
                        final Element valueElement = XMLUtil.findElement(e, "value", JavaProjectNature.NS_JAVA_LASTEST);  //NOI18N
                        if (keyElement == null || valueElement == null) {
                            continue;
                        }
                        final String key = XMLUtil.findText(keyElement);
                        final String value = XMLUtil.findText(valueElement);
                        result.put(
                            key,
                            value == null ? null : eval.evaluate(value));
                    }
                }
            }
            synchronized (LCK) {
                if (optionsCache == null) {
                    optionsCache = result;
                }
            }
            return result;
        }

        public @Override void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        public @Override void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        private void update (final Element cu) {
            synchronized(LCK) {
                triggerCache = null;
                optionsCache = null;
                processorsCache = null;
                sourceOutputCache = null;
                ap = findAP(cu);
            }
            this.changeSupport.fireChange();
        }

        private Element findAP(final Element cu) {
            return cu == null ? null : XMLUtil.findElement(cu, AnnotationProcessingQueryImpl.EL_ANNOTATION_PROCESSING, JavaProjectNature.NS_JAVA_LASTEST);
        }

    }

}
