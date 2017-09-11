/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.model.pom;

import java.io.IOException;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.ComponentUpdater;

/**
 * Interface for the extensibility elements
 *
 * @author mkleint
 */
public interface POMExtensibilityElement extends POMComponent {
  
    public static final String CONTENT_FRAGMENT_PROPERTY = "content";       // NOI18N
    
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
    public void addAnyElement(POMExtensibilityElement anyElement, int index);
    
    /**
     * Removes child extensibility element of unknown type.
     */
    public void removeAnyElement(POMExtensibilityElement any);
    
    /**
     * @returns list of children extensibility elements of unknown type.
     */
    public List<POMExtensibilityElement> getAnyElements();

    public String getElementText();
    void setElementText(String text);
    
    /**
     * Returns QName of the backing DOM element.
     */
    public QName getQName();
    
    /**
     * Interface for an extensibility element that could provide update visitor
     * to be used during sync from source.
     */
    interface UpdaterProvider extends POMExtensibilityElement {
        /**
         * @return component updater to be used in merge operations when source sync happens.
         */
        <T extends POMExtensibilityElement> ComponentUpdater<T> getComponentUpdater();
    }
    
    /**
     * Interface for an extensibility element that is a root of an embedded model.
     */
    interface EmbeddedModel extends POMExtensibilityElement {
        DocumentModel getEmbeddedModel();
    }
}
