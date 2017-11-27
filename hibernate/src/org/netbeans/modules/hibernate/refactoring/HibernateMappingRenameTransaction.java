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
package org.netbeans.modules.hibernate.refactoring;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.cfg.model.SessionFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;

/**
 * Change the mapping resource name from the old to new
 * 
 * @author Dongmei Cao
 */
public class HibernateMappingRenameTransaction extends RenameTransaction {

    private final String rescrName = "Resource"; // NOI18N
    private boolean pathOnly = false; // Only replace the path part of it
    
    public HibernateMappingRenameTransaction(java.util.Set<FileObject> files, String origName, String newName) {
        this(files, origName, newName, false);
    }
    
    public HibernateMappingRenameTransaction(java.util.Set<FileObject> files, String origName, String newName, boolean pathOnly) {
        super(files, origName, newName);
        this.pathOnly = pathOnly;
    }

    /**
     * Do the actual changes
     * 
     */
    public void doChanges() {
        
        for(FileObject file : getToBeModifiedFiles()) {
            OutputStream outs = null;
            try {
                InputStream is = file.getInputStream();
                HibernateConfiguration configuration = HibernateConfiguration.createGraph(is);

                SessionFactory sfactory = configuration.getSessionFactory();
                if(sfactory != null) {
                    for(int i = 0; i < sfactory.sizeMapping(); i ++ ) {
                        String resourceName = sfactory.getAttributeValue(SessionFactory.MAPPING, i, rescrName);
                        if (resourceName != null) {
                            String comparePart = resourceName;
                            String newResourceName = newName;

                            // If we're only replace the directory part of the resource name, then...
                            if(pathOnly) {
                                int lastIndex = resourceName.lastIndexOf('/');
                                if (lastIndex > -1) {
                                    comparePart = resourceName.substring(0, lastIndex);
                                    newResourceName = newResourceName + "/" + resourceName.substring(resourceName.lastIndexOf('/')+1);
                                }
                            }
                            if(comparePart.equals(origName)) {
                                sfactory.setAttributeValue(SessionFactory.MAPPING, i, rescrName, newResourceName);
                            }
                        }
                    }
                }

                outs = file.getOutputStream();
                configuration.write(outs);
            } catch (FileAlreadyLockedException ex) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            } finally {
                try {
                    if (outs != null) {
                        outs.close();
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
    }
}
