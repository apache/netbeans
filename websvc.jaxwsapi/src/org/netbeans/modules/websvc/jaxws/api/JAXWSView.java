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

package org.netbeans.modules.websvc.jaxws.api;

import java.util.Iterator;
import org.netbeans.modules.websvc.jaxws.JAXWSViewAccessor;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSViewProvider;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSViewImpl;
import org.openide.util.Lookup;
import org.openide.nodes.Node;
import org.netbeans.api.project.Project;

/** JAXWSView should be used to display JAX-WS services in project.
 * <p>
 * A client may obtain a JAXWSView instance using 
 * <code>JAXWSView.getJAXWSView()</code> static 
 * method.
 *
 * @author Peter Williams, Milan Kuchtiak
 */
public final class JAXWSView {
    
    private JAXWSViewImpl impl;
    private static final Lookup.Result<JAXWSViewProvider> implementations =
        Lookup.getDefault().lookup(new Lookup.Template<JAXWSViewProvider>(JAXWSViewProvider.class));
    
    static  {
        JAXWSViewAccessor.DEFAULT = new JAXWSViewAccessor() {
            public JAXWSView createJAXWSView(JAXWSViewImpl spiWebServicesView) {
                return new JAXWSView(spiWebServicesView);
            }

            public JAXWSViewImpl getJAXWSViewImpl(JAXWSView wsv) {
                return wsv == null ? null : wsv.impl;
            }
        };
    }
    
    private JAXWSView(JAXWSViewImpl impl) {
        if (impl == null)
            throw new IllegalArgumentException ();
        this.impl = impl;
    }
    
    /** Lookup the IDE to find the JAXWSView instance
     */
    public static JAXWSView getJAXWSView() {
       Iterator it = implementations.allInstances().iterator();
       while (it.hasNext()) {
          JAXWSViewProvider impl = (JAXWSViewProvider)it.next();
          JAXWSView wsv = impl.findJAXWSView ();
          if (wsv != null) {
            return wsv;
          }
       }
        return null;
    }

    // Delegated methods from JAXWSViewImpl
    
    /** Create JAXWSView node for given project
     * @param project project containing JAX-WS services
     * @return root node for web JAX-WS services representations
     */
    public Node createJAXWSView(Project project) {
        return impl.createJAXWSView(project);
    }

}
