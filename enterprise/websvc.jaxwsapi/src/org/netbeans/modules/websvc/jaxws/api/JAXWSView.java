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
