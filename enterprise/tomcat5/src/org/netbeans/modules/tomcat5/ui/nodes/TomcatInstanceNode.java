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

package org.netbeans.modules.tomcat5.ui.nodes;

import java.awt.Component;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.netbeans.modules.tomcat5.ui.nodes.actions.AdminConsoleAction;
import org.netbeans.modules.tomcat5.ui.nodes.actions.ServerLogAction;
import org.netbeans.modules.tomcat5.ui.nodes.actions.TerminateAction;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.tomcat5.customizer.Customizer;
import org.netbeans.modules.tomcat5.ui.nodes.actions.SharedContextLogAction;
import org.netbeans.modules.tomcat5.ui.nodes.actions.EditServerXmlAction;
import org.netbeans.modules.tomcat5.ui.nodes.actions.OpenServerOutputAction;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.cookies.EditorCookie;
import org.openide.util.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;


/**
 *
 * @author  Petr Pisl
 */

public class TomcatInstanceNode extends AbstractNode implements Node.Cookie {
    
    private TomcatManager tm;
    
    /** Creates a new instance of TomcatInstanceNode 
      @param lookup will contain DeploymentFactory, DeploymentManager, Management objects. 
     */
    public TomcatInstanceNode(Children children, Lookup lookup) {
        super(children);
        tm = (TomcatManager)lookup.lookup(TomcatManager.class);
        setIconBaseWithExtension("org/netbeans/modules/tomcat5/resources/tomcat.png"); // NOI18N
        getCookieSet().add(this);
    }
    
    @Override
    public String getShortDescription() {
        return NbBundle.getMessage(
                    TomcatInstanceNode.class, 
                    "LBL_TomcatInstanceNode", 
                    String.valueOf(tm.getCurrentServerPort()));
    }
    
    @Override
    public boolean hasCustomizer() {
        return true;
    }
    
    @Override
    public Component getCustomizer() {
        return new Customizer(tm);
    }
    
    /** Return the TomcatManager instance this node represents. */
    public TomcatManager getTomcatManager() {
        return tm;
    }

    @Override
    public javax.swing.Action[] getActions(boolean context) {
        java.util.List actions = new LinkedList();
        // terminate does not work on Windows, see issue #63157
        if (!Utilities.isWindows()) {
            actions.add(null);
            actions.add(SystemAction.get(TerminateAction.class));
        }
        actions.add(null);
        actions.add(SystemAction.get(EditServerXmlAction.class));
        if (tm.isTomcat50() || tm.isTomcat55()) {
            actions.add(SystemAction.get(AdminConsoleAction.class));
        }
        if (tm.isTomcat50()) {
            actions.add(SystemAction.get(SharedContextLogAction.class));
        }
        if (!tm.isTomcat50()) {
            actions.add(SystemAction.get(ServerLogAction.class));
        }
        actions.add(SystemAction.get(OpenServerOutputAction.class));
        return (SystemAction[])actions.toArray(new SystemAction[0]);
    }
        
    private FileObject getTomcatConf() {
        tm.ensureCatalinaBaseReady(); // generated the catalina base folder if empty
        TomcatProperties tp = tm.getTomcatProperties();
        return FileUtil.toFileObject(tp.getServerXml());
    }
    
    /**
     * Open server.xml file in editor.
     */
    public void editServerXml() {
        FileObject fileObject = getTomcatConf();
        if (fileObject != null) {
            DataObject dataObject = null;
            try {
                dataObject = DataObject.find(fileObject);
            } catch(DataObjectNotFoundException ex) {
                Logger.getLogger(TomcatInstanceNode.class.getName()).log(Level.INFO, null, ex);
            }
            if (dataObject != null) {
                EditorCookie editorCookie = (EditorCookie)dataObject.getCookie(EditorCookie.class);
                if (editorCookie != null) {
                    editorCookie.open();
                } else {
                    Logger.getLogger(TomcatInstanceNode.class.getName()).log(Level.INFO, "Cannot find EditorCookie."); // NOI18N
                }
            }
        }
    }

    /**
     * Open the server log (output).
     */
    public void openServerLog() {
        tm.logManager().openServerLog();
    }
    
    /**
     * Can be the server log (output) displayed?
     *
     * @return <code>true</code> if the server log can be displayed, <code>false</code>
     *         otherwise.
     */
    public boolean hasServerLog() {
        return tm.logManager().hasServerLog();
    }
    
    /**
     * Overrides the compatible XML DO behaviour for files without data objects
     * @param context
     * @return 
     */
    @MIMEResolver.Registration(
        displayName="org.netbeans.modules.tomcat5.resources.Bundle#TomcatResolver",
        position=380,
        resource="../../resources/tomcat-mime-resolver.xml"
    )
    @MultiViewElement.Registration(
        displayName="org.netbeans.modules.tomcat5.ui.nodes.Bundle#CTL_SourceTabCaption",
        iconBase="org/netbeans/modules/tomcat5/resources/tomcat5.gif",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="xml.text",
        mimeType="text/tomcat5+xml",
        position=1
    )
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }
        

}
