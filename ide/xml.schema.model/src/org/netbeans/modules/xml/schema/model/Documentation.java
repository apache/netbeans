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

package org.netbeans.modules.xml.schema.model;

import java.io.IOException;
import org.w3c.dom.Element;

/**
 * The interface represents human-readable documentation in plain text.
 * @author Chris Webster
 */
public interface Documentation extends SchemaComponent {
	
	public static final String LANGUAGE_PROPERTY = "language";
	public static final String SOURCE_PROPERTY = "source";
	public static final String CONTENT_PROPERTY = "content";
	
	String getSource();
	void setSource(String uri);
	
	//TODO low priority create enum for languages
	String getLanguage();
	void setLanguage(String lang);
        
    /**
     * @return text representation of the documentation element content.
     */
    String getContent();
    
    /**
     * Set the documentation element content to a text node with the given
     * string value.
     */
    void setContent(String content);
    
    /**
     * @return XML fragment text of documentation element content.
     */
    String getContentFragment();
    
    /**
     * Sets the XML fragment text of documentation element content.
     * The XML fragment will be parsed and the resulting nodes will
     * replace the current children of this documentation element.
     * @param text XML fragment text.
     * @exception IOException if the fragment text is not well-form.
     */
    void setContentFragment(String text) throws IOException;
    
    /**
     * @return a mutable clone of the documentation element itself.
     */
    Element getDocumentationElement();
    
    /**
     * Sets the documentation element to the given element.
     */
    void setDocumentationElement(Element documentationElement);
}
