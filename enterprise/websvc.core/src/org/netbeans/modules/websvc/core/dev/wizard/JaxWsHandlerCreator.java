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

package org.netbeans.modules.websvc.core.dev.wizard;

import java.io.IOException;
import org.netbeans.modules.websvc.core.HandlerCreator;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Rico, Milan Kuchtiak
 */
public class JaxWsHandlerCreator implements HandlerCreator {
    private WizardDescriptor wiz;
    /**
     * Creates a new instance of WebServiceClientCreator
     */
    public JaxWsHandlerCreator(WizardDescriptor wiz) {
        this.wiz = wiz;
    }
        
    public void createMessageHandler() throws IOException {
        String handlerName = Templates.getTargetName(wiz);
        FileObject pkg = Templates.getTargetFolder(wiz);
        DataFolder df = DataFolder.findFolder(pkg);
        FileObject template = Templates.getTemplate(wiz);
        DataObject dTemplate = DataObject.find(template);
        DataObject dobj = dTemplate.createFromTemplate(df, handlerName);

        //open in the editor
        final EditorCookie ec = dobj.getCookie(EditorCookie.class);
        RequestProcessor.getDefault().post(new Runnable(){
            public void run(){
                ec.open();
            }
        }, 1000);
    }
    
    public void createLogicalHandler() throws IOException {
        String handlerName = Templates.getTargetName(wiz);
        FileObject pkg = Templates.getTargetFolder(wiz);
        DataFolder df = DataFolder.findFolder(pkg);
        FileObject template = Templates.getTemplate(wiz);
        DataObject dTemplate = DataObject.find(template);
        DataObject dobj = dTemplate.createFromTemplate(df, handlerName);

        //open in the editor
        final EditorCookie ec = dobj.getCookie(EditorCookie.class);
        RequestProcessor.getDefault().post(new Runnable(){
            public void run(){
                ec.open();
            }
        }, 1000);
    }  
}
