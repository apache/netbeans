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

package org.netbeans.modules.websvc.design.view.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.lang.model.element.TypeElement;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.support.AddOperationCookie;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.core.AddWsOperationHelper;
import org.netbeans.modules.websvc.design.javamodel.ProjectService;
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit Bhate
 */
public class AddOperationAction extends AbstractAction implements AddOperationCookie {
    
    private ProjectService service;
    private DataObject dataObject;
    /**
     * Creates a new instance of AddOperationAction
     * @param implementationClass fileobject of service implementation class
     */
    public AddOperationAction(ProjectService service, FileObject implementationClass) {
        super(getName());
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/websvc/design/view/resources/operation.png", false));
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(AddOperationAction.class, "Hint_AddOperation"));
        putValue(MNEMONIC_KEY, Integer.valueOf(NbBundle.getMessage(AddOperationAction.class, "LBL_AddOperation_mnem_pos")));
        this.service=service;
        try {
            dataObject = DataObject.find( implementationClass );
        }
        catch (DataObjectNotFoundException  e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    public static String getMainClassName(final FileObject classFO) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(classFO);
        final String[] result = new String[1];
        javaSource.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement classEl = SourceUtils.getPublicTopLevelElement(controller);
                if (classEl != null) {
                    result[0] = classEl.getQualifiedName().toString();
                }
            }
        }, true);
        return result[0];
    }
    
    private static String getName() {
        return NbBundle.getMessage(AddOperationAction.class, "LBL_AddOperation");
    }
    
    public void actionPerformed(ActionEvent arg0) {
        try{
            // no need to create new task or progress handle, as strategy does it.
            addJavaMethod();
        }catch(IOException e){
            ErrorManager.getDefault().notify(e);
        }
    }
    
    private void saveImplementationClass() throws IOException{
        if(dataObject.isModified()) {
            SaveCookie cookie = dataObject.getCookie(SaveCookie.class);
            if(cookie!=null) cookie.save();
        }
    }
    
    private void addJavaMethod() throws IOException{
        AddWsOperationHelper strategy = new AddWsOperationHelper(getName());
        String className = getMainClassName(dataObject.getPrimaryFile());
        if (className != null) {
            strategy.addMethod(dataObject.getPrimaryFile(), className);
            saveImplementationClass();
        }
    }

    public void addOperation() {
        actionPerformed(null);
    }

    @Override
    public boolean isEnabledInEditor(Lookup nodeLookup) {
        return service != null && service.getWsdlUrl() == null;
    }
}
