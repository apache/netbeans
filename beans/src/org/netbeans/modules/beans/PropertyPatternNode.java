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

