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

package org.netbeans.modules.xml.wsdl.model;

import java.io.IOException;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.ComponentUpdater;

/**
 * Interface for wsdl extensibility elements
 *
 * @author rico
 * @author Nam Nguyen
 */
public interface ExtensibilityElement extends WSDLComponent {
	public static final String CONTENT_FRAGMENT_PROPERTY = "content";
    
    /**
     * Set/get attribute value.
     */
    public String getAttribute(String attribute);
    public void setAttribute(String attribute, String value);
    
    /**
     * Set/get attribute defined in given namespace.
     */
    public String getAnyAttribute(QName attr);
    public void setAnyAttribute(QName attr, String value);

    /**
     * Set/get content as XML fragment.
     * The XML fragment will be parsed and the resulting nodes will
     * replace the current children of this documentation element.
     * @param text XML fragment text.
     * @exception IOException if the fragment text is not well-form.
     */
    public String getContentFragment();
    public void setContentFragment(String fragment) throws IOException;

    /**
     * Adds child extensibility elements of unknown type.
     * @param anyElement any child component to add
     * @param index absolute index position in children list.
     */
    public void addAnyElement(ExtensibilityElement anyElement, int index);
    
    /**
     * Removes child extensibility element of unknown type.
     */
    public void removeAnyElement(ExtensibilityElement any);
    
    /**
     * @returns list of children extensibility elements of unknown type.
     */
    public List<ExtensibilityElement> getAnyElements();
    
    /**
     * Returns QName of the backing DOM element.
     */
    public QName getQName();
    
    /**
     * Interface for an extensibility element that could provide update visitor
     * to be used during sync from source.
     */
    interface UpdaterProvider extends ExtensibilityElement {
        /**
         * @return component updater to be used in merge operations when source sync happens.
         */
        <T extends ExtensibilityElement> ComponentUpdater<T> getComponentUpdater();
    }
    
    /**
     * Interface for an extensibility element that is a root of an embedded model.
     */
    interface EmbeddedModel extends ExtensibilityElement {
        DocumentModel getEmbeddedModel();
    }

    /**
     * An extensibility element with ability to select parent to be added to.
     */
    interface ParentSelector extends ExtensibilityElement {
        boolean canBeAddedTo(Component target);
    }
}
