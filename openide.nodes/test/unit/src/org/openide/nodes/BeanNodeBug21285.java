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

package org.openide.nodes;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Node.Property;

/**
 * Regression test for bug #21285<br>
 * For more info please see the
 * <a href="http://openide.netbeans.org/issues/show_bug.cgi?id=21285">
 * descrition in issuezilla</a>
 *
 * @author  Petr Hrebejk
 */
public class BeanNodeBug21285 extends NbTestCase {


    /** Creates new TextTest */
    public BeanNodeBug21285(String s) {
        super(s);
    }
    
    public static void main(String[] args)throws Exception {

         
        BeanInfo bi = Introspector.getBeanInfo( BadBeanHidden.class );
        PropertyDescriptor[] ps = bi.getPropertyDescriptors();
        
        for ( int i = 0; i < ps.length; i++ ) {
            System.out.println( i + " : " + ps[i]);
            System.out.println("  Read : " + ps[i].getReadMethod() );
            System.out.println("  Write : " + ps[i].getWriteMethod() );
            System.out.println(" TYPE " + ps[i].getPropertyType() );
            if ( ps[i] instanceof IndexedPropertyDescriptor ) {
                System.out.println("  I Read : " + ((IndexedPropertyDescriptor)ps[i]).getIndexedReadMethod() );
                System.out.println("  I Write : " +((IndexedPropertyDescriptor)ps[i]).getIndexedWriteMethod() );
                System.out.println(" TYPE " + ((IndexedPropertyDescriptor)ps[i]).getIndexedPropertyType() );
            }
            
            
        }
    }


    /** Regression test to reproduce bug #21858. */
    public void testBadBean() throws Exception {

        BeanNode bn = new BeanNode( new BadBeanHidden() );
        Node.PropertySet ps[] = bn.getPropertySets();
        
        try {
            for (int i = 0; i < ps.length; i++) {
                 Set<Property> props = new HashSet<Property>( 
                    Arrays.asList(ps[i].getProperties()));
            }
        }
        catch ( NullPointerException e ) {
            assertTrue( "The NullPointerException thrown", false );
        }
        
        assertTrue( true );
    }
    
}
