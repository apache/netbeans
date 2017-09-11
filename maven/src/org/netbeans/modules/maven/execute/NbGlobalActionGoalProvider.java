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
            return toRet.toArray(new NetbeansActionMapping[toRet.size()]);
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
