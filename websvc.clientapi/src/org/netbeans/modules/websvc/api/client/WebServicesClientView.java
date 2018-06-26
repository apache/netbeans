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

package org.netbeans.modules.websvc.api.client;

import java.util.Iterator;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.nodes.Node;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.spi.client.WebServicesClientViewImpl;
import org.netbeans.modules.websvc.spi.client.WebServicesClientViewProvider;
import org.netbeans.modules.websvc.client.WebServicesClientViewAccessor;

/** WebServicesClientView should be used to retrieve information and display objects
 *  for the webservices in a project.
 * <p>
 * A client may obtain a WebServicesClientView instance using 
 * <code>WebServicesClientView.getWebServicesClientView(fileObject)</code> static 
 * method, for any FileObject in the project directory structure.
 *
 * @author Peter Williams
 */
public final class WebServicesClientView {

	private WebServicesClientViewImpl impl;
	private static final Lookup.Result implementations =
		Lookup.getDefault().lookupResult(WebServicesClientViewProvider.class);

	static  {
		WebServicesClientViewAccessor.DEFAULT = new WebServicesClientViewAccessor() {
			public WebServicesClientView createWebServicesClientView(WebServicesClientViewImpl spiWebServicesClientView) {
				return new WebServicesClientView(spiWebServicesClientView);
			}

			public WebServicesClientViewImpl getWebServicesClientViewImpl(WebServicesClientView wsv) {
				return wsv == null ? null : wsv.impl;
			}
		};
	}

	private WebServicesClientView(WebServicesClientViewImpl impl) {
		if (impl == null)
			throw new IllegalArgumentException ();
		this.impl = impl;
	}

	/** Find the WebServicesClientView for given file or null if the file does 
	 *  not belong to any module support web services.
	 */
	public static WebServicesClientView getWebServicesClientView(FileObject f) {
		if (f == null) {
			throw new NullPointerException("Passed null to WebServicesClientView.getWebServicesClientView(FileObject)"); // NOI18N
		}
		Iterator it = implementations.allInstances().iterator();
		while (it.hasNext()) {
			WebServicesClientViewProvider impl = (WebServicesClientViewProvider)it.next();
			WebServicesClientView wsv = impl.findWebServicesClientView (f);
			if (wsv != null) {
				return wsv;
			}
		}

		WebServicesClientViewProvider impl = (WebServicesClientViewProvider) Lookup.getDefault().lookup(WebServicesClientViewProvider.class);
		if(impl != null) {
			WebServicesClientView wsv = impl.findWebServicesClientView(f);
			return wsv;
		}
		return null;
	}

	// Delegated methods from WebServicesClientViewImpl

	/** This method is not implemented.
	 */
	public Node createWebServiceClientView(Project p) {
		return impl.createWebServiceClientView(p);
	}

	/** This method is not implemented.
	 */
	public Node createWebServiceClientView(SourceGroup sg) {
		return impl.createWebServiceClientView(sg);
	}

	/**
	 * 1. Returns a parent node that the project's logical view can use to display
	 *    the services consumed by this project/module.
	 * 2, Parent node is prepopulated with children representing the services 
	 *    found in the WSDL files in the WSDL folder.
	 *
	 * ISSUE: Does J2ME even have a WSDL folder concept?
	 *
	 * @param wsdlFolder FileObject representing the wsdl folder of the module
	 * containing these web service clients.
	 * @return Node The root node of the web service client subtree intended for
	 * display in the project logical view in the explorer.
	 */
	public Node createWebServiceClientView(FileObject wsdlFolder) {
		return impl.createWebServiceClientView(wsdlFolder);
	}

    
/* !PW FIXME What to put here?  (commented code came from WebModule API)
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
