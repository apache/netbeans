/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.makeproject.api.configurations;

import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;

public interface ConfigurationAuxObject {
    /**
     * Initializes the object to default values
     */
    public void initialize();

    public XMLDecoder getXMLDecoder();
    public XMLEncoder getXMLEncoder();

    /**
     * Returns a unique String id (key) used to retrive this object from the
     * pool of aux objects
     OLD: use getXMLDecoder.tag() for below
     * and for storing the object in xml form and
     * parsing the xml code to restore the object.
     * Debugger should use the id "dbxdebugger", for instance.
     */

    public String getId();

    /**
     * Responsible for saving the object in xml format.
     * It should save the object in the following format using the id string from getId():
     * <id-string>
     *     <...
     *     <...
     * </id-string>
     */
    /* OLD
    public void writeElement(PrintWriter pw, int indent, Object object);
    */

    /**
     * Responsible for parsing the xml code created from above and for restoring the state of
     * the object (but not the object itself).
     * Refer to the Sax parser documentation for details.
     */
    /* OLD
    public void startElement(String namespaceURI, String localName, String element, Attributes atts);
    public void endElement(String uri, String localName, String qName, String currentText);
    */

    /**
     * Returns true if object has changed and needs to be saved.
     */
    public boolean hasChanged();
    public void clearChanged();

    /**
     * Returns true if object should be stored in shared (public) part of configuration
     */
    public boolean shared();

    /**
     * Assign all values from a profileAuxObject to this object (reverse of clone)
     * Unsafe method. Make sure that method getID() returns equal string. 
     * Otherwise method can harm configuration storage (class Configuration).
     */
    public void assign(ConfigurationAuxObject profileAuxObject);

    /**
     * Clone itself to an identical (deep) copy.
     */
    public ConfigurationAuxObject clone(Configuration conf);
}
