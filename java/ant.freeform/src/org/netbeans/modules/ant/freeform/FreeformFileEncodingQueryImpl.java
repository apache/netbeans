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

package org.netbeans.modules.ant.freeform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Element;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.xml.XMLUtil;

/**
 * Implementation of FileEncodingQuery for Freeform project, its instance can be 
 * obtained from project lookup
 * 
 * @author Milan Kubec
 * @author Tomas Zezula
 */
public class FreeformFileEncodingQueryImpl extends FileEncodingQueryImplementation 
        implements AntProjectListener, PropertyChangeListener {
    
    private AntProjectHelper helper;
    private PropertyEvaluator evaluator;
    //@GuardedBy("this")
    private Map<File,Charset> encodingsCache;
    
    @SuppressWarnings("LeakingThisInConstructor")
    public FreeformFileEncodingQueryImpl(AntProjectHelper aph, PropertyEvaluator eval) {
        helper = aph;
        evaluator = eval;
        evaluator.addPropertyChangeListener(this);
    }
    
    @Override
    public Charset getEncoding(final FileObject file) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Charset>() {
            @Override
            public Charset run() {
                Charset toReturn = null;
                synchronized (FreeformFileEncodingQueryImpl.this) {
                    if (encodingsCache == null) {
                        computeEncodingsCache();
                    }
                    if (encodingsCache.size() > 0) {
                        Set<File> roots = encodingsCache.keySet();
                        File parent = getNearestParent(roots, FileUtil.toFile(file));
                        if (parent != null) {
                            toReturn = encodingsCache.get(parent);
                        }
                    }
                }
                return toReturn;
            }
        });
    }

    private File getNearestParent(Set<File> parents, File file) {
        while (file != null) {
             if (parents.contains(file)) {
                 return file;
             }
             file = file.getParentFile();
         }
        return null;
    }
    
    private void computeEncodingsCache() {
        assert Thread.holdsLock(this);
        Map<File,Charset> cache = new HashMap<File,Charset>(3);
        Element data = Util.getPrimaryConfigurationData(helper);
        Element foldersEl = XMLUtil.findElement(data, "folders", Util.NAMESPACE); // NOI18N
        if (foldersEl != null) {
            for (Element sourceFolderEl : XMLUtil.findSubElements(foldersEl)) {
                if (!sourceFolderEl.getLocalName().equals("source-folder")) { // NOI18N
                    continue;
                }
                File srcRoot = null;
                Element locationEl = XMLUtil.findElement(sourceFolderEl, "location", Util.NAMESPACE); // NOI18N
                if (locationEl != null) {
                    String location = evaluator.evaluate(XMLUtil.findText(locationEl));
                    if (location != null) {
                        srcRoot = helper.resolveFile(location);
                    }
                }
                Element encodingEl = XMLUtil.findElement(sourceFolderEl, "encoding", Util.NAMESPACE); // NOI18N
                if (encodingEl != null && srcRoot != null) {
                    String encoding = evaluator.evaluate(XMLUtil.findText(encodingEl));
                    Charset charset = null;
                    if (encoding != null) {
                        try {
                            charset = Charset.forName(encoding);
                        } catch (IllegalCharsetNameException icne) {
                            Exceptions.printStackTrace(icne);
                        }
                        cache.put(srcRoot, charset);
                    }
                }
            }
        }
        if (cache.size() > 0) {
            encodingsCache = cache;
        } else {
            encodingsCache = Collections.<File,Charset>emptyMap();
        }
    }
    
    // ---
    
    @Override
    public void configurationXmlChanged(AntProjectEvent ev) {
        invalidateCache();
    }
    
    @Override
    public void propertiesChanged(AntProjectEvent ev) {
        invalidateCache();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        invalidateCache();
    }
    
    private synchronized void invalidateCache() {
        encodingsCache = null;
    }
    
}
