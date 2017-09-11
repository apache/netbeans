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
