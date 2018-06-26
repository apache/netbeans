/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.deployment.impl.query;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;


/**
 * Finds the locations of sources for various libraries.
 * @since 1.5
 */
@org.netbeans.api.annotations.common.SuppressWarnings(value="DMI_COLLECTION_OF_URLS", justification="File URLs only")
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation.class)
public class J2eePlatformSourceForBinaryQuery implements SourceForBinaryQueryImplementation2 {

    private final Map<URL,SourceForBinaryQueryImplementation2.Result> cache = new HashMap<URL,SourceForBinaryQueryImplementation2.Result>();
    private final Map/*<URL,URL>*/ normalizedURLCache = new HashMap();

    /** Default constructor for lookup. */
    public J2eePlatformSourceForBinaryQuery() {}

    @org.netbeans.api.annotations.common.SuppressWarnings(value="DMI_BLOCKING_METHODS_ON_URL", justification="File URLs only")
    public SourceForBinaryQueryImplementation2.Result findSourceRoots2 (URL binaryRoot) {
        SourceForBinaryQueryImplementation2.Result res = this.cache.get (binaryRoot);
        if (res != null) {
            return res;
        }
        boolean isNormalizedURL = isNormalizedURL(binaryRoot);
        
        String[] serverInstanceIDs = Deployment.getDefault().getServerInstanceIDs();
        ServerRegistry servReg = ServerRegistry.getInstance();
        for (int i=0; i < serverInstanceIDs.length; i++) {
            ServerInstance serInst = servReg.getServerInstance(serverInstanceIDs[i]);
            if (serInst == null) {
                continue;
            }
            J2eePlatformImpl platformImpl = serInst.getJ2eePlatformImpl();
            if (platformImpl == null) {
                continue; // TODO this will be removed, when AppServPlg will be ready
            }
            LibraryImplementation[] libs = platformImpl.getLibraries();
            for (int j=0; j< libs.length; j++) {
                String type = libs[j].getType ();
                List classes = libs[j].getContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH);    //NOI18N
                for (Iterator it = classes.iterator(); it.hasNext();) {
                    URL entry = (URL) it.next();
                    URL normalizedEntry;
                    if (isNormalizedURL) {
                        normalizedEntry = getNormalizedURL(entry);
                    }
                    else {
                        normalizedEntry = entry;
                    }
                    if (normalizedEntry != null && normalizedEntry.equals(binaryRoot)) {
                        res =  new Result(entry, libs[j]);
                        cache.put (binaryRoot, res);
                        return res;
                    }
                }
            }
        }
        return null;
    }
    
    public SourceForBinaryQuery.Result findSourceRoots (final URL binaryRoot) {
        return this.findSourceRoots2(binaryRoot);
    }
    
    
    private URL getNormalizedURL (URL url) {
        //URL is already nornalized, return it
        if (isNormalizedURL(url)) {
            return url;
        }
        //Todo: Should listen on the LibrariesManager and cleanup cache        
        // in this case the search can use the cache onle and can be faster 
        // from O(n) to O(ln(n))
        URL normalizedURL = (URL) this.normalizedURLCache.get (url);
        if (normalizedURL == null) {
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                try {
                    normalizedURL = fo.getURL();
                    this.normalizedURLCache.put (url, normalizedURL);
                } catch (FileStateInvalidException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
        return normalizedURL;
    }
    
    /**
     * Returns true if the given URL is file based, it is already
     * resolved either into file URL or jar URL with file path.
     * @param URL url
     * @return true if  the URL is normal
     */
    private static boolean isNormalizedURL (URL url) {
        if ("jar".equals(url.getProtocol())) { //NOI18N
            url = FileUtil.getArchiveFile(url);
        }
        return "file".equals(url.getProtocol());    //NOI18N
    }
    
    
    private static class Result implements SourceForBinaryQueryImplementation2.Result, PropertyChangeListener {
        
        private LibraryImplementation lib;
        private URL entry;
        private ArrayList listeners;
        private FileObject[] cache;
        
        public Result (URL queryFor, LibraryImplementation aLib) {
            this.entry = queryFor;
            this.lib = aLib;
            this.lib.addPropertyChangeListener ((PropertyChangeListener)WeakListeners.create(PropertyChangeListener.class,this,this.lib));
        }
        
        public synchronized FileObject[] getRoots () {
            if (cache == null) {
                if (this.lib.getContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH).contains (entry)) {
                    List src = this.lib.getContent(J2eeLibraryTypeProvider.VOLUME_TYPE_SRC);
                    List result = new ArrayList ();
                    for (Iterator sit = src.iterator(); sit.hasNext();) {
                        FileObject sourceRootURL = URLMapper.findFileObject((URL) sit.next());
                        if (sourceRootURL!=null) {
                            result.add (sourceRootURL);
                        }
                    }
                    this.cache = (FileObject[]) result.toArray(new FileObject[result.size()]);
                }
                else {
                    this.cache = new FileObject[0];
                }
            }
            return this.cache;
        }
        
        public synchronized void addChangeListener (ChangeListener l) {
            assert l != null : "Listener cannot be null"; // NOI18N
            if (this.listeners == null) {
                this.listeners = new ArrayList ();
            }
            this.listeners.add (l);
        }
        
        public synchronized void removeChangeListener (ChangeListener l) {
            assert l != null : "Listener cannot be null"; // NOI18N
            if (this.listeners == null) {
                return;
            }
            this.listeners.remove (l);
        }
        
        public void propertyChange (PropertyChangeEvent event) {
            if (LibraryImplementation.PROP_CONTENT.equals(event.getPropertyName())) {
                synchronized (this) {
                    this.cache = null;
                }
                this.fireChange ();
            }
        }
        
        private void fireChange () {
            Iterator it = null;
            synchronized (this) {
                if (this.listeners == null) {
                    return;
                }
                it = ((ArrayList)this.listeners.clone()).iterator();
            }
            ChangeEvent event = new ChangeEvent (this);
            while (it.hasNext ()) {
                ((ChangeListener)it.next()).stateChanged(event);
            }
        }

        public boolean preferSources() {
            return false;
        }
        
    }
    
}
