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

package org.netbeans.modules.xml.xam.dom;

import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is used by the XDMModel for identification of elements from 2 documents,
 * by their establised attributes
 *
 * @author Ayub Khan
 */
public interface ElementIdentity {
	
	public List getIdentifiers();
	
	/* 
	 * add element identifiers like "id" "name", "ref" etc.,
	 *
	 * @param identifier
	 **/
	public void addIdentifier(String identifier);
	
	/* 
	 * callback for comparing e1 and e2. By default
	 * compares element localnames, then their namespace uri's, followed 
	 * by documents established identifying attributes for comparison
	 *
	 * @param e1
	 * @param e2
	 **/
	public boolean compareElement(Element e1, Element e2, Document doc1, Document doc2);
}
