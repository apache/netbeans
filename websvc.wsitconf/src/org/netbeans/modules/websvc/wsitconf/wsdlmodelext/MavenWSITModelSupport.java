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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.undo.UndoManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.wsitconf.util.UndoManagerHolder;
import org.netbeans.modules.websvc.wsitconf.projects.MavenWsitProvider;
import org.netbeans.modules.websvc.wsitconf.spi.WsitProvider;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Grebac
 */
public class MavenWSITModelSupport {
    
    private static final Logger logger = Logger.getLogger(MavenWSITModelSupport.class.getName());
    
    /** Creates a new instance of MavenWSITModelSupport */
    public MavenWSITModelSupport() { }
    
    public static WSDLModel getModel(Node node, Project project, JAXWSLightSupport jaxWsSupport, JaxWsService jaxService, UndoManagerHolder umHolder, boolean create, Collection<FileObject> createdFiles) throws MalformedURLException, Exception {

        WSDLModel model = null;
        boolean isClient = !jaxService.isServiceProvider();

        if (isClient) {
            model = getModelForClient(project, jaxWsSupport, jaxService, create, createdFiles);
        } else {  //it is a service
            FileObject implClass = node.getLookup().lookup(FileObject.class);
            try {
                String wsdlUrl = jaxService.getLocalWsdl();
                if (wsdlUrl == null) { // WS from Java
                    if ((implClass == null) || (!implClass.isValid() || implClass.isVirtual())) {
                        logger.log(Level.INFO, "Implementation class is null or not valid, or just virtual: " + implClass + ", service: " + jaxService);
                        return null;
                    }
                    return WSITModelSupport.getModelForServiceFromJava(implClass, project, create, createdFiles);
                } else {
                    if (project == null) return null;
                    return getModelForServiceFromWsdl(jaxWsSupport, jaxService);
                }
            } catch (Exception e) {
                logger.log(Level.INFO, null, e);
            }
        }

        if ((model != null) && (umHolder != null) && (umHolder.getUndoManager() == null)) {
            UndoManager undoManager = new UndoManager();
            model.addUndoableEditListener(undoManager);  //maybe use WeakListener instead
            umHolder.setUndoManager(undoManager);
        }
        return model;
    }

    /* Retrieves WSDL model for a WS client - always has a wsdl
     */
    public static WSDLModel getModelForClient(Project p, JAXWSLightSupport jaxWsSupport, JaxWsService jaxWsService, boolean create, Collection<FileObject> createdFiles) throws IOException {
        URI uri = null;
        try {
            uri = jaxWsSupport.getWsdlFolder(false).getFileObject(jaxWsService.getLocalWsdl()).getURL().toURI();
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (uri == null) return null;
        return WSITModelSupport.getModelForClient(p, uri, create, createdFiles);
    }
    
    /* Retrieves WSDL model for a WS from Java - if config file exists, reuses that one, otherwise generates new one
     */
    public static WSDLModel getServiceModelForClient(JAXWSLightSupport supp, JaxWsService client) throws IOException, Exception {
        FileObject originalWsdlFolder = supp.getWsdlFolder(false);
        FileObject originalWsdlFO = originalWsdlFolder.getFileObject(client.getLocalWsdl());

        if ((originalWsdlFO != null) && (originalWsdlFO.isValid())) {
            return WSITModelSupport.getModelFromFO(originalWsdlFO, true);
        }
        return null;
    }
    
    private static WSDLModel getModelForServiceFromWsdl(JAXWSLightSupport supp, JaxWsService service) throws IOException, Exception {
        String wsdlLocation = service.getLocalWsdl();
        FileObject wsdlFO = supp.getWsdlFolder(false).getFileObject(wsdlLocation);
        return WSITModelSupport.getModelFromFO(wsdlFO, true);
    }

    public static boolean isMavenProject(Project p) {
        WsitProvider provider = p.getLookup().lookup(WsitProvider.class);
        if (provider instanceof MavenWsitProvider) {
            return true;
        }
        return false;
    }
    
}
