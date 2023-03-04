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

package org.netbeans.modules.beans;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.*;
import org.openide.util.Utilities;

import static org.netbeans.modules.beans.BeanUtils.*;



/** Node representing a field (variable).
* @see PropertyPattern
* @author Petr Hrebejk
*/
public class PropertyPatternNode extends PatternNode {

    /** Create a new pattern node.
    * @param pattern pattern to represent
    * @param writeable <code>true</code> to be writable
    */
    public PropertyPatternNode( PropertyPattern pattern, boolean writeable) {
        super(pattern, Children.LEAF, writeable);
        superSetName( pattern.getName() );
    }

    
    /** Gets the localized string name of property pattern type i.e.
     * "Indexed Property", "Property".
     */
    String getTypeForHint () {
        return getString ("HINT_Property");
    }


    /* Gets the short description of this node.
    * @return A localized short description associated with this node.
    */
    @Override
    public String getShortDescription() {
        String mode;

        switch( ((PropertyPattern)pattern).getMode() ) {
        case PropertyPattern.READ_WRITE:
            mode = getString("HINT_ReadWriteProperty") ;
            break;
        case PropertyPattern.READ_ONLY:
            mode = getString("HINT_ReadOnlyProperty");
            break;
        case PropertyPattern.WRITE_ONLY:
            mode = getString("HINT_WriteOnlyProperty");
            break;
        default:
            mode = ""; // NOI18N
            break;
        }
        return mode + " " + getTypeForHint() + " : " + getName(); // NOI18N
    }

    /** Overrides the default implementation of clone node
     */
    @Override
    public Node cloneNode() {
        return new PropertyPatternNode((PropertyPattern)pattern, writeable);
    }

    /** Sets the name of pattern
     */
    @Override
    protected void setPatternName( String name )  {
        
        if ( pattern.getName().equals( name ) ) {
            return;
        }
        
        if (testNameValidity(name)) {
            ((PropertyPattern)pattern).setName( name );
        }
    }

    /** Tests if the given string is valid name for associated pattern and if not, notifies
    * the user.
    * @return true if it is ok.
    */
    boolean testNameValidity( String name ) {

        if (! Utilities.isJavaIdentifier( name ) ) {
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(getString("MSG_Not_Valid_Identifier"),
                                             NotifyDescriptor.ERROR_MESSAGE) );
            return false;
        }

        return true;
    }

    void fire () {
        firePropertyChange( null, null, null );
    }
}

