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

package org.netbeans.modules.xml.xdm.visitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.namespace.NamespaceContext;

public class HashNamespaceResolver implements NamespaceContext {
	private Map<String, String> prefixes; // namespace, prefix
	private Map<String, String> namespaces;  // prefix, namespace
	
	public HashNamespaceResolver(Map<String,String> nsTable) {
		namespaces = nsTable;
		prefixes = new HashMap<String,String>();
		for (Entry<String,String> e : namespaces.entrySet()) {
			prefixes.put(e.getValue(), e.getKey());
		}
	}
	
	public HashNamespaceResolver(Map<String,String> namespaces, Map<String,String> prefixes) {
            this.namespaces = namespaces;
            this.prefixes = prefixes;
        }
        
	public Iterator getPrefixes(String namespaceURI) {
		return Collections.singletonList(getPrefix(namespaceURI)).iterator();
	}
	
	public String getPrefix(String namespaceURI) {
		return prefixes.get(namespaceURI);
	}
	
	public String getNamespaceURI(String prefix) {
		return namespaces.get(prefix);
	}
	
}
