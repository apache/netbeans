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
