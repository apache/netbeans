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
