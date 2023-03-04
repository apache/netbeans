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
