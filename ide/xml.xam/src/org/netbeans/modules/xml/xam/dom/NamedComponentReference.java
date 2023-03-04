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

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.Reference;

/**
 * Represents reference to a component that can be identified globally by QName.
 *
 * @author Chris Webster
 * @author Rico Cruz
 * @author Nam Nguyen
 */
public interface NamedComponentReference<T extends NamedReferenceable> extends Reference<T> {
    
    /**
     * Returns the effective namespace of the referenced component.
     * <p>
     * Note that in case of XML schema document, the effective namespace of a 
     * component could be different when the schema is included by another schema.
     *
     * @return referenced namespace that is effective in the current document.
     */
    String getEffectiveNamespace();
    
    /**
     * Returns full QName of the referenced component if the reference is not broken.
     * If reference has not been resolved or broken, the returned QNam could be 
     * partial (only local name) and implementation dependent.
     * @return QName of the referenced component.
     */
    QName getQName();
}
