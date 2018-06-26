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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.websvc.editor.hints.fixes;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;

import org.netbeans.modules.websvc.api.support.AddOperationCookie;
import org.netbeans.modules.websvc.core.WebServiceActionProvider;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

/**
 * @author Ajit.Bhate@Sun.COM, milan.kuchtiak@sun.com
 */
public class AddWSOperation implements Fix {
    private FileObject fileObject;
    
    /** Creates a new instance of AddWSOperation */
    public AddWSOperation(FileObject fileObject) {
        this.fileObject = fileObject;
    }
    
    public ChangeInfo implement() {
        AddOperationCookie addOperationCookie = WebServiceActionProvider.getAddOperationAction(fileObject);
        if (addOperationCookie == null) {
            Project prj = FileOwnerQuery.getOwner(fileObject);
            final JaxWsModel model = prj.getLookup().lookup(JaxWsModel.class);
            if (model != null) {
                try {
                    DataObject dObj = DataObject.find(fileObject);
                    SaveCookie saveCookie = dObj.getCookie(SaveCookie.class);
                    if (saveCookie != null) {
                        model.addServiceListener(new JaxWsModel.ServiceListener() {
                            @Override
                            public void serviceAdded(String name, String implementationClass) {
                                detachListener(model, this);
                                AddOperationCookie cookie = WebServiceActionProvider.getAddOperationAction(fileObject);
                                if (cookie !=null) {
                                    cookie.addOperation();
                                }
                            }

                            @Override
                            public void serviceRemoved(String name) {
                            }
                        });
                        saveCookie.save();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(AddWSOperation.class.getName()).log(Level.INFO, "Cannot save file", ex);
                }
            }
        } else {
            addOperationCookie.addOperation();
        }
        
        return null;
    }

    private void detachListener(final JaxWsModel model, final JaxWsModel.ServiceListener l) {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                model.removeServiceListener(l);
            }
        });
    }
    
    public int hashCode(){
        return 1;
    }
    
    public boolean equals(Object o){
        // TODO: implement equals properly
        return super.equals(o);
    }
    
    public String getText() {
        if (WebServiceActionProvider.getAddOperationAction(fileObject) == null) {
            return NbBundle.getMessage(RemoveAnnotation.class, "LBL_SaveAndAddWSOperation");
        } else {
            return NbBundle.getMessage(RemoveAnnotation.class, "LBL_AddWSOperation");
        }
    }
}
