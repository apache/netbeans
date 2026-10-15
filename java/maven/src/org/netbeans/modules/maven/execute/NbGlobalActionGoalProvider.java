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

package org.netbeans.modules.maven.execute;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider;
import org.netbeans.modules.maven.spi.actions.MavenActionsProvider;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * user defined global definitions, to be found in the layers.
 * @author mkleint
 */
@ServiceProviders({@ServiceProvider(service=MavenActionsProvider.class, position=121), @ServiceProvider(service=NbGlobalActionGoalProvider.class)})
public class NbGlobalActionGoalProvider extends AbstractMavenActionsProvider {
    private static final String FILENAME_FOLDER = "Projects/org-netbeans-modules-maven"; //NOI18N
    private static final String FILENAME = "nbactions.xml";
    private static final String FILE_NAME_PATH = FILENAME_FOLDER + "/" + FILENAME;

    private static final Logger LOG = Logger.getLogger(NbGlobalActionGoalProvider.class.getName());
    
    private final AtomicBoolean resetCache = new AtomicBoolean(false);
    private FileChangeListener listener = null;
    
    @Override
    public InputStream getActionDefinitionStream() {
        checkListener();
        FileObject fo = FileUtil.getConfigFile(FILE_NAME_PATH);
        resetCache.set(false);
        if (fo != null) {
            try {
                return fo.getInputStream();
            } catch (FileNotFoundException ex) {
                LOG.log(Level.FINE, "File not found: " + FileUtil.getFileDisplayName(fo), ex);
            }
        }
        return null;
    }

    private synchronized void checkListener() {
        if (listener == null) {
            listener = new FileChangeAdapter() {

                @Override
                public void fileRenamed(FileRenameEvent fe) {
                    if (FILENAME.equals(fe.getName() + "." + fe.getExt())) {
                        resetCache();
                    }
                }

                @Override
                public void fileDeleted(FileEvent fe) {
                    if (FILENAME.equals(fe.getFile().getNameExt())) {
                        resetCache();
                    }
                }

                @Override
                public void fileChanged(FileEvent fe) {
                    if (FILENAME.equals(fe.getFile().getNameExt())) {
                        resetCache();
                    }
                }

                @Override
                public void fileDataCreated(FileEvent fe) {
                    if (FILENAME.equals(fe.getFile().getNameExt())) {
                        resetCache();
                    }
                }
                
            };
            //we call from static context want to be always notified since first calling I guess, no need for weak listener here
            FileUtil.getConfigFile(FILENAME_FOLDER).addFileChangeListener(listener);
        }
    }
    
    private void resetCache() {
        resetCache.compareAndSet(false, true);
    }
   
    
   /**
     * get custom action maven mapping configuration
     * No replacements happen.
     * The instances returned is always a new copy, can be modified or reused.
     * Same method in UserActionGoalProvider
     */
    public NetbeansActionMapping[] getCustomMappings() {
        NetbeansActionMapping[] fallbackActions = new NetbeansActionMapping[0];
        
        try {
            List<NetbeansActionMapping> toRet = new ArrayList<NetbeansActionMapping>();
            // just a converter for the To-Object reader..
            Reader read = performDynamicSubstitutions(Collections.<String,String>emptyMap(), getRawMappingsAsString());
            // basically doing a copy here..
            ActionToGoalMapping mapping = reader.read(read);    
            List lst = mapping.getActions();
            if (lst != null) {
                Iterator it = lst.iterator();
                while(it.hasNext()) {
                    NetbeansActionMapping mapp = (NetbeansActionMapping) it.next();
                    if (mapp.getActionName().startsWith("CUSTOM-")) { //NOI18N
                        toRet.add(mapp);
                    }
                }
            }
            return toRet.toArray(new NetbeansActionMapping[0]);
        } catch (XmlPullParserException ex) {
            LOG.log(Level.FINE, "cannot parse", ex);
        } catch (IOException ex) {
            LOG.log(Level.FINE, "", ex);
        }
        return fallbackActions;
    }
    
    @Override
    protected boolean reloadStream() {
        return resetCache.get();
    }
}
