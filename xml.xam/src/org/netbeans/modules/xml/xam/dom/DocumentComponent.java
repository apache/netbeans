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

package org.netbeans.modules.xml.xam.dom;

import org.netbeans.modules.xml.xam.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A component in model.
 * 
 */
public interface DocumentComponent<C extends DocumentComponent> extends Component<C> {
    public static final String TEXT_CONTENT_PROPERTY = "textContent";

    /**
     * Returns the DOM element corresponding to this component.
     */
    Element getPeer();

    /**
     * @return attribute string value or null if the attribute is currently undefined
     */
    String getAttribute(Attribute attribute);
    
    /**
     * Sets the attribute value.
     * @param eventPropertyName name property change event to fire.
     * @param attribute the attribute to set value for.
     * @param value for the attribute.
     */
    void setAttribute(String eventPropertyName, Attribute attribute, Object value);
    
    /**
     * Returns true if the component is part of the document model.
     */
    boolean isInDocumentModel();
    
    /**
     * Returns the position of this component in the schema document,
     * expressed as an offset from the start of the document.
     * @return the position of this component in the document
     */
    int findPosition();

    /**
     * Returns true if the node referenced by this component is n.
     */
    boolean referencesSameNode(Node n);

    /**
     * Returns child component backed by given element node.
     */
    public C findChildComponent(Element e);

    /**
     * Returns position of the attribute by the given name, or -1 if not found.
     */
    public int findAttributePosition(String attributeName);
}
