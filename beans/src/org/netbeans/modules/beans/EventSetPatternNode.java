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

import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.openide.DialogDisplayer;

import org.openide.NotifyDescriptor;
import org.openide.nodes.*;
import org.openide.util.Utilities;

import static org.netbeans.modules.beans.BeanUtils.*;

/** Node representing a event set pattern.
* @see EventSetPattern
* @author Petr Hrebejk
*/
public final class EventSetPatternNode extends PatternNode {

    /** Create a new pattern node.
    * @param pattern pattern to represent
    * @param writeable <code>true</code> to be writable
    */
    public EventSetPatternNode( EventSetPattern pattern, boolean writeable) {
        super(pattern, Children.LEAF, writeable);
        superSetName( pattern.getName() );
    }

    @Override
    protected void setPatternName( String name ) {
//        
//        if ( pattern.getName().equals( name ) ) {
//            return;
//        }
//        
//        if ( testNameValidity(name) ) {
//            ((EventSetPattern)pattern).setName(name);
//        }
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

        if (name.indexOf( "Listener" ) <= 0 ) { // NOI18N
            String msg = MessageFormat.format( getString("FMT_InvalidEventSourceName"),
                                               new Object[] { name } );
            DialogDisplayer.getDefault().notify( new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE) );
            return false;
        }

        return true;
    }

    /** Gets the short description of this node.
    * @return A localized short description associated with this node.
    */
    @Override
    public String getShortDescription() {
        return (((EventSetPattern)pattern).isUnicast () ?
                getString( "HINT_UnicastEventSet" ) :
                getString( "HINT_MulticastEventSet" ) )
               + " : " + getName(); // NOI18N
    }

//    /** Creates property set for this node */
//    protected Sheet createSheet () {
//        Sheet sheet = Sheet.createDefault();
//        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
//
////        ps.put(createNameProperty( writeable ));
////        ps.put(createTypeProperty( writeable ));
////        ps.put(createIsUnicastProperty( writeable ));
////        ps.put(createAddListenerProperty( false ));
////        ps.put(createRemoveListenerProperty( false ));
//
//        return sheet;
//    }

    /** Overrides the default implementation of clone node
     */

    @Override
    public Node cloneNode() {
        return new EventSetPatternNode((EventSetPattern)pattern, writeable );
    }


//    /** Create a property for the field type.
//     * @param canW <code>false</code> to force property to be read-only
//     * @return the property
//     */
//    protected Node.Property createTypeProperty(boolean canW) {
//        return new PatternPropertySupport(PROP_TYPE, Type.class, canW) {
//
//                   /** Gets the value */
//
//                   public Object getValue () {
//                       return ((EventSetPattern)pattern).getType();
//                   }
//
//                   /** Sets the value */
//                   public void setValue(Object val) throws IllegalArgumentException,
//                       IllegalAccessException, InvocationTargetException {
//                       super.setValue(val);
//                       if (!(val instanceof Type))
//                           throw new IllegalArgumentException();
//
//                       try {
//                           pattern.patternAnalyser.setIgnore( true );
//                           ((EventSetPattern)pattern).setType((Type)val);
//                       } catch (JmiException e) {
//                           throw new InvocationTargetException(e);
//                       } finally {
//                           pattern.patternAnalyser.setIgnore( false );
//                       }
//                   }
//
//                   public PropertyEditor getPropertyEditor () {
//                       return new org.netbeans.modules.beans.EventTypeEditor();
//                   }
//               };
//    }
//

//    /** Create a property for the field type.
//     * @param canW <code>false</code> to force property to be read-only
//     * @return the property
//     */
//    protected Node.Property createIsUnicastProperty(boolean canW) {
//        return new PatternPropertySupport(PROP_ISUNICAST, boolean.class, canW) {
//
//                   /** Gets the value */
//
//                   public Object getValue () {
//                       return ((EventSetPattern)pattern).isUnicast() ? Boolean.TRUE : Boolean.FALSE;
//                   }
//
//                   /** Sets the value */
//                   public void setValue(Object val) throws IllegalArgumentException,
//                       IllegalAccessException, InvocationTargetException {
//                       super.setValue(val);
//                       if (!(val instanceof Boolean))
//                           throw new IllegalArgumentException();
//
//                       try {
//                           try {
//                               pattern.patternAnalyser.setIgnore( true );
//                               ((EventSetPattern)pattern).setIsUnicast(((Boolean)val).booleanValue());
//                               setIconBaseWithExtension( resolveIconBase() + ".gif");
//                           } finally {
//                               pattern.patternAnalyser.setIgnore( false );
//                           }
//                       } catch (JmiException e) {
//                           throw new InvocationTargetException(e);
//                       }
//                   }
//
//               };
//    }
//
//    /** Create a property for the addListener method.
//     * @param canW <code>false</code> to force property to be read-only
//     * @return the property
//     */
//
//    protected Node.Property createAddListenerProperty(boolean canW) {
//        return new PatternPropertySupport(PROP_ADDLISTENER, String.class, canW) {
//
//                   public Object getValue () {
//                       Method method = ((EventSetPattern) pattern).getAddListenerMethod();
//                       return getFormattedMethodName(method);
//                   }
//               };
//    }
//
//    /** Create a property for the removeListener method.
//     * @param canW <code>false</code> to force property to be read-only
//     * @return the property
//     */
//
//    protected Node.Property createRemoveListenerProperty(boolean canW) {
//        return new PatternPropertySupport(PROP_REMOVELISTENER, String.class, canW) {
//
//                   public Object getValue () {
//                       Method method = ((EventSetPattern) pattern).getRemoveListenerMethod();
//                       return getFormattedMethodName(method);
//                   }
//               };
//    }
}

