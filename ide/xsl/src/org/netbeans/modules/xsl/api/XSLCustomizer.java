/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.xsl.api;

import org.w3c.dom.Node;
import java.awt.Component;
import org.openide.loaders.DataObject;

/**
 * The <code>XPathCustomizer</code> is an interface to enable editing an XPath in a source document.
 * By implementing the interface any editor can launch the <code>XPathCustomizer</code> when an XPath needs
 * to be customized.
  */
public interface XSLCustomizer {	
    /**
     * Returns the <code>Component</code> used to edit the XPath.
     * @param node the <code>Node</code> instance to be customized.
     * @param dataObject the <code>DataObject</code> representing the XSL document.
	 *            <code>ScenarioCookie</code> can be fetched from this DataObject using
	 *            <code>dataObject.getCookie(ScenarioCookie.class)</code>.
     * @return the <code>Component</code> used to edit the XPath.
     */
    public Component getCustomizer(Node node, DataObject dataObject);

    /**
     * Indicates if this customizer can return a customizing component for this
     * node.
     * @param node the <code>Node</code> instance to be customized.
     * @return true if a customizer is supported for this node, otherwise false.
     */
    public boolean hasCustomizer(Node node);
}
