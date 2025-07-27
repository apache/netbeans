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

package org.netbeans.modules.java.source.usages;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class ExecutableFilesIndex {
    
    public static final ExecutableFilesIndex DEFAULT = new ExecutableFilesIndex();
    
    private URL lastLoadURL;
    private Set<String> mainSources;
    
    public synchronized boolean isMainClass(URL root, URL source) {
        ensureLoad(root);
        
        return mainSources.contains(source.toExternalForm());
    }
    
    public synchronized Iterable<? extends URL> getMainClasses (URL root) {
        ensureLoad(root);
        List<URL> result = new ArrayList<URL>(mainSources.size());
        for (String surl : mainSources) {
            try {
                result.add(new URL(surl));
            } catch (MalformedURLException ex) {
                //Report and ignore broken url
                Exceptions.printStackTrace(ex);
            }
        }
        return result;
    }
    
    public synchronized void setMainClass(URL root, URL source, boolean value) {
        ensureLoad(root);
        
        String ext = source.toExternalForm();
        boolean changed;
        
        if (value) {
            changed = mainSources.add(ext);
        } else {
            changed = mainSources.remove(ext);
        }
        
        if (changed) {
            save(root);
            
            Set<ChangeListener> ls = file2Listener.get(ext);
            
            if (ls != null) {
                ChangeEvent e = null;
                
                for (ChangeListener l : ls) {
                    if (e == null)
                        e = new ChangeEvent(source);
                    
                    l.stateChanged(e);
                }
            }
        }
    }
    
    private Map<ChangeListener, String> listener2File = new WeakHashMap<ChangeListener, String>();
    private Map<String, Set<ChangeListener>> file2Listener = new WeakHashMap<String, Set<ChangeListener>>();
    
    public synchronized void addChangeListener(URL source, ChangeListener l) {
        String ext = source.toExternalForm();
        
        listener2File.put(l, ext);
        
        Set<ChangeListener> ls = file2Listener.get(ext);
        
        if (ls == null) {
            file2Listener.put(ext, ls = Collections.newSetFromMap(new WeakHashMap<>()));
        }
        
        ls.add(l);
        
        file2Listener.put(ext, ls);
    }
    
    private void ensureLoad(URL root) {
        if (lastLoadURL != null && lastLoadURL.equals(root)) {
            return;
        }

        try {
            mainSources = unwrap(JavaIndex.getAttribute(root, "executable-files", "")); //NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            mainSources = new HashSet<String>();
        } finally {
            lastLoadURL = root;
        }
    }

    private void save(URL root) {
        try {
            JavaIndex.setAttribute(root, "executable-files", wrap(mainSources)); //NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    static Set<String> unwrap(String value) {
        if (value.length() == 0) {
            return new HashSet<String>();
        }
        
        String[] executableFiles = value.split("::"); //NOI18N

        Set<String> result = new HashSet<String>();

        for (String file : executableFiles) {
            result.add(file.replace("\\d", ":") //NOI18N
                           .replace("\\\\", "\\")); //NOI18N
        }
        
        return result;
    }
    
    static String wrap(Set<String> values) {
        StringBuilder attribute = new StringBuilder();
        boolean first = true;

        for (String value : values) {
            if (!first) {
                attribute.append("::"); //NOI18N
            }
            attribute.append(value.replace("\\", "\\\\") //NOI18N
                                  .replace(":", "\\d")); //NOI18N
            first = false;
        }
        
        return attribute.toString();
    }
}
