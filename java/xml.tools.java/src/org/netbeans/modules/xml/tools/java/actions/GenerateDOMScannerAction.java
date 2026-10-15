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
package org.netbeans.modules.xml.tools.java.actions;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.xml.actions.CollectDTDAction;
import org.netbeans.modules.xml.tools.generator.XMLGenerateAction;
import org.netbeans.modules.xml.tools.java.generator.GenerateDOMScannerSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Sonali
 */
public class GenerateDOMScannerAction extends XMLGenerateAction implements CollectDTDAction.DTDAction {
    /** generated Serialized Version UID */
    private static final long serialVersionUID = 2567846356902367312L;

/**********
    public static synchronized GenerateDOMScannerAction getInstance() {
        GenerateDOMScannerAction actionInstance = null;
        String thisClassName = GenerateDOMScannerAction.class.getName();
        try {
            Class actionInstanceClass = Class.forName(thisClassName);
            actionInstance = (GenerateDOMScannerAction) actionInstanceClass.newInstance();
        } catch(Exception e) {
            Logger.getLogger(thisClassName).log(Level.SEVERE, "", e);
        }
        return actionInstance;
    }
**********/

    /* Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        return NbBundle.getMessage(GenerateDOMScannerAction.class, "PROP_GenerateDOMScanner");
    }

    /* Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(GenerateDOMScannerAction.class);
    }

    protected Class getOwnCookieClass() {
        return GenerateDOMScannerSupport.class;
    }

    protected boolean enable(Node[] node) {
        if (node.length == 0) {
            return false;
        }
        DataObject dobj = node[0].getLookup().lookup(DataObject.class);
        if (dobj == null) {
            return false;
        }
        FileObject fo = dobj.getPrimaryFile();
        Project project = FileOwnerQuery.getOwner(fo);
        if(project == null)
            return false;
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] srcGrps = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (srcGrps == null || srcGrps.length == 0) {
            return false;
        } else {
            return true;
        }
    } 
    
}
