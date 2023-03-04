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

package org.netbeans.modules.xml.jaxb.actions;

import java.io.File;
import java.net.URL;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.jaxb.util.FileSysUtil;
import org.netbeans.modules.xml.jaxb.spi.JAXBWizModuleConstants;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author lgao
 */
public class JAXBRefreshAction extends NodeAction  {
    
    /** Creates a new instance of JAXBRefreshAction */
    public JAXBRefreshAction() {
    }

    protected void performAction(Node[] nodes) {
        Node node = nodes[ 0 ];
        FileObject fo = node.getLookup().lookup( FileObject.class );
        Project proj = node.getLookup().lookup(Project.class);
        String origLoc = (String) node.getValue(
                JAXBWizModuleConstants.ORIG_LOCATION);
        Boolean origLocIsURL = (Boolean) node.getValue(
                JAXBWizModuleConstants.ORIG_LOCATION_TYPE);
        FileObject locSchemaRoot = (FileObject) node.getValue(
                JAXBWizModuleConstants.LOC_SCHEMA_ROOT);
        
        if ( ( fo != null ) && ( origLoc != null ) ) {
            // XXX TODO run in separate non-awt thread.
             try {
                 if (fo.canWrite()){
                     if (origLocIsURL){
                        URL url = new URL(origLoc);
                         ProjectHelper.retrieveResource(locSchemaRoot, 
                                 url.toURI());                        
                     } else {
                         File projDir = FileUtil.toFile(
                                 proj.getProjectDirectory());
                         //File srcFile = new File(origLoc);
                         File srcFile = FileSysUtil.Relative2AbsolutePath(
                                 projDir, origLoc);
                         ProjectHelper.retrieveResource(fo.getParent(), 
                                 srcFile.toURI());
                     }
                 } else {
                     String msg = NbBundle.getMessage(this.getClass(),
                             "MSG_CanNotRefreshFile"); //NOI18N
                     NotifyDescriptor d = new NotifyDescriptor.Message(
                             msg, NotifyDescriptor.INFORMATION_MESSAGE);
                     d.setTitle(NbBundle.getMessage(this.getClass(), 
                             "LBL_RefreshFile")); //NOI18N
                     DialogDisplayer.getDefault().notify(d);
                 }
             } catch (Exception ex){
                 log(ex);
             } 
        }
    }
    
    private static void log(Exception ex){
        Exceptions.printStackTrace(ex);
    }
    
    public String getName() {
        return NbBundle.getMessage(this.getClass(), "LBL_NodeRefresh");//NOI18N
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    @Override 
    protected boolean enable(Node[] node) {
        return true;
    }
}
