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
