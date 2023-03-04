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
package org.netbeans.modules.j2ee.sun.dd.impl;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.schema2beans.GraphManager;
import org.xml.sax.SAXParseException;


/**
 * Interface for internal methods applicable to all bean root proxies, but are
 * not public in RootInterface.
 * 
 * Formerly derived from RootInterface, but that meant these were implemented
 * in the schema2beans generated roots as well as the proxy classes and that
 * wasn't very nice, nor did it make sense.
 * 
 * Now used as a mixin class for the Proxy implementations.
 *
 * @author Peter Williams
 */
public interface RootInterfaceImpl
{    
    
    /** Converts to RootInterface
     * 
     * @return RootInterface instance for this proxy
     */
    public RootInterface getRootInterface();
    
    /** Sets parsing status
     * 
     * @param value parser state (valid, invalid, unparsable.  See RootInterface
     *   for flags.
     */
    public void setStatus(int value);
      
    /** Retrieve current parser error.
     * 
     * @return current parser exception, if any.
     */
    public SAXParseException getError();
    
    /** Sets error status
     *
     * @param error current parser exception or null if none.
     */
    public void setError(SAXParseException error);
    
    /** Returns whether this proxy is current wrapping a tree or not.
     * 
     * @return true if this proxy has a valid xml tree.
     */
    public boolean hasOriginal();
      
    /** Adds property change listener to particular CommonDDBean object (WebApp object).
     * 
     * @param pcl property change listener
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl);
    
    /** Removes property change listener from CommonDDBean object.
     * 
     * @param pcl property change listener
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl);
    
    /** Get ASDDVersion object for the current graph, if any
     * 
     * @return ASDDVersion object for the dtd used by this graph or null if it
     * cannot be determined.
     */
    public ASDDVersion getASDDVersion();
    
    /** Retrieve the graph manager for the document, or null if there is not
     *  current a document.
     * 
     * @return graph manager for current document, if any.
     */
    public GraphManager graphManager();
    
}
