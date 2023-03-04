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
package org.netbeans.modules.maven.model.pom.spi;

import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.w3c.dom.Element;


/**
 * Factory for creating domain components. This factory must be provided by 
 * ElementFactoryProvider to be able to plugin to the domain model.
 *
 * @author mkleint
 * 
 */
public interface ElementFactory {

    /**
     * Returns the QNames of the elements this factory is for.
     */
    Set<QName> getElementQNames();
    
    /**
     * Creates a POMComponent from a DOM element given the 
     * container component.
     *
     * @param container component requesting creation
     * @param element DOM element from which to create the component
     *
     * @return the domain component being created
     */
    POMComponent create(POMComponent container, Element element);
}
