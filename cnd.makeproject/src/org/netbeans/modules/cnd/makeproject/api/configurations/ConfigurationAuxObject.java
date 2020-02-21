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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
