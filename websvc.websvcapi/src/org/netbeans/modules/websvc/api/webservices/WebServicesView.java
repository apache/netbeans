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

package org.netbeans.modules.websvc.api.webservices;

import java.util.Iterator;
import org.netbeans.modules.websvc.webservices.WebServicesViewAccessor;
import org.netbeans.modules.websvc.spi.webservices.*;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.nodes.Node;
import org.netbeans.api.project.Project;

/** WebServicesView should be used to retrieve information and display objects
 *  for the webservices in a project.
 * <p>
 * A client may obtain a WebServicesView instance using
 * <code>WebServicesView.getWebServicesView(fileObject)</code> static 
 * method, for any FileObject in the project directory structure.
 *
 * @author Peter Williams
 */
public final class WebServicesView {
    
    private WebServicesViewImpl impl;
    private static final Lookup.Result implementations =
        Lookup.getDefault().lookup(new Lookup.Template(WebServicesViewProvider.class));
    
    static  {
        WebServicesViewAccessor.DEFAULT = new WebServicesViewAccessor() {
            public WebServicesView createWebServicesView(WebServicesViewImpl spiWebServicesView) {
                return new WebServicesView(spiWebServicesView);
            }

            public WebServicesViewImpl getWebServicesViewImpl(WebServicesView wsv) {
                return wsv == null ? null : wsv.impl;
            }
        };
    }
    
    private WebServicesView(WebServicesViewImpl impl) {
        if (impl == null)
            throw new IllegalArgumentException ();
        this.impl = impl;
    }
    
    /** Find the WebServicesView for given file or null if the file does not belong
     * to any module support web services.
     */
    public static WebServicesView getWebServicesView(FileObject f) {
        if (f != null) {
           Iterator it = implementations.allInstances().iterator();
           while (it.hasNext()) {
              WebServicesViewProvider impl = (WebServicesViewProvider)it.next();
              WebServicesView wsv = impl.findWebServicesView (f);
              if (wsv != null) {
                return wsv;
              }
           }
        }
        return null;
    }

	// Delegated methods from WebServicesViewImpl
	
    public Node createWebServicesView(FileObject  srcRoot) {
		return impl.createWebServicesView(srcRoot);
	}
	
	
    
/* !! What to put here?
 *
	public boolean equals (Object obj) {
        if (!WebModule.class.isAssignableFrom(obj.getClass()))
            return false;
        WebModule wm = (WebModule) obj;
        return getDocumentBase().equals(wm.getDocumentBase())
            && getJ2eePlatformVersion().equals (wm.getJ2eePlatformVersion())
            && getContextPath().equals(wm.getContextPath());
    }
    
    public int hashCode () {
        return getDocumentBase ().getPath ().length () + getContextPath ().length ();
    }
 */
}
