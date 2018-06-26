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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
    
    public String getShortDescription() {
        return NbBundle.getMessage(
                    TomcatInstanceNode.class, 
                    "LBL_TomcatInstanceNode", 
                    String.valueOf(tm.getCurrentServerPort()));
    }
    
    public boolean hasCustomizer() {
        return true;
    }
    
    public Component getCustomizer() {
        return new Customizer(tm);
    }
    
    /** Return the TomcatManager instance this node represents. */
    public TomcatManager getTomcatManager() {
        return tm;
    }

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
        return (SystemAction[])actions.toArray(new SystemAction[actions.size()]);
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
