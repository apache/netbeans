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

package org.netbeans.modules.websvc.api.jaxws.client;

import java.util.Iterator;
import org.netbeans.modules.websvc.jaxws.client.JAXWSClientViewAccessor;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientViewImpl;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientViewProvider;
import org.openide.util.Lookup;
import org.openide.nodes.Node;
import org.netbeans.api.project.Project;

/** JAXWSlientView should be used to display web service references in project.
 *  The wiew containes nodes representing web service references in project.
 * <p>
 * A client may obtain a JAXWClientView instance using 
 * <code>JAXWSClientView.getJAXWSClientView()</code> static 
 * method
 *
 * @author Peter Williams, Milan Kuchtiak  
 */
public final class JAXWSClientView {

	private JAXWSClientViewImpl impl;
	private static final Lookup.Result<?> implementations =
		Lookup.getDefault().lookupResult(JAXWSClientViewProvider.class);

	static  {
		JAXWSClientViewAccessor.DEFAULT = new JAXWSClientViewAccessor() {
			public JAXWSClientView createJAXWSClientView(JAXWSClientViewImpl spiWebServicesClientView) {
				return new JAXWSClientView(spiWebServicesClientView);
			}

			public JAXWSClientViewImpl getJAXWSClientViewImpl(JAXWSClientView wsv) {
				return wsv == null ? null : wsv.impl;
			}
		};
	}

	private JAXWSClientView(JAXWSClientViewImpl impl) {
		if (impl == null)
			throw new IllegalArgumentException ();
		this.impl = impl;
	}

	/** Lookup the IDE and find JAXWSClientView instance
     */
	public static JAXWSClientView getJAXWSClientView() {
		Iterator it = implementations.allInstances().iterator();
		while (it.hasNext()) {
			JAXWSClientViewProvider impl = (JAXWSClientViewProvider)it.next();
			JAXWSClientView wsv = impl.findJAXWSClientView ();
			if (wsv != null) {
				return wsv;
			}
		}

		JAXWSClientViewProvider impl = Lookup.getDefault().lookup(JAXWSClientViewProvider.class);
		if(impl != null) {
			JAXWSClientView wsv = impl.findJAXWSClientView();
			return wsv;
		}
		return null;
	}

	// Delegated methods from WebServicesClientViewImpl
    
	/** Create Node representing Web Service References for a given project
     *@param project project containing web service references (WS Clients) 
	*/ 
	public Node createJAXWSClientView(Project p) {
		return impl.createJAXWSClientView(p);
	}

}
