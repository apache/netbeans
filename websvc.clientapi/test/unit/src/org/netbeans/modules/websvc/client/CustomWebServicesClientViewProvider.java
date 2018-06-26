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

package org.netbeans.modules.websvc.client;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.api.client.WebServicesClientView;
import org.netbeans.modules.websvc.spi.client.WebServicesClientViewFactory;
import org.netbeans.modules.websvc.spi.client.WebServicesClientViewImpl;
import org.netbeans.modules.websvc.spi.client.WebServicesClientViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author Lukas Jungmann
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.spi.client.WebServicesClientViewProvider.class)
public class CustomWebServicesClientViewProvider implements WebServicesClientViewProvider {
    
    private Map<FileObject, WebServicesClientView> cache = new HashMap<FileObject, WebServicesClientView>();
    
    /** Creates a new instance of WebServicesClientViewProvider */
    public CustomWebServicesClientViewProvider() {
    }
    
    public WebServicesClientView findWebServicesClientView(FileObject file) {
        if (file.getExt().equals("ws")) {
            WebServicesClientView em = cache.get(file.getParent());
            if (em == null) {
                em = WebServicesClientViewFactory.createWebServicesClientView(new CustomWebServicesClientViewImpl(file));
                cache.put(file.getParent(), em);
            }
            return em;
        }
        return null;
    }
    
    private static final class CustomWebServicesClientViewImpl implements WebServicesClientViewImpl {
        private FileObject fo;
        
        CustomWebServicesClientViewImpl(FileObject fo) {
            this.fo = fo;
        }
        
        public Node createWebServiceClientView(Project p) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public Node createWebServiceClientView(SourceGroup sg) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public Node createWebServiceClientView(FileObject wsdlFolder) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
