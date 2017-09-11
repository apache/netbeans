/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.usages;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.openide.util.Exceptions;
import org.openide.util.WeakSet;

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
            file2Listener.put(ext, ls = new WeakSet<ChangeListener>());
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
            file = file.replaceAll("\\\\d", ":"); //NOI18N
            file = file.replaceAll("\\\\\\\\", "\\\\"); //NOI18N

            result.add(file);
        }
        
        return result;
    }
    
    static String wrap(Set<String> values) {
        StringBuilder attribute = new StringBuilder();
        boolean first = true;

        for (String s : values) {
            if (!first) {
                attribute.append("::"); //NOI18N
            }
            s = s.replaceAll("\\\\", "\\\\\\\\"); //NOI18N
            s = s.replaceAll(":", "\\\\d"); //NOI18N

            attribute.append(s);

            first = false;
        }
        
        return attribute.toString();
    }
}
