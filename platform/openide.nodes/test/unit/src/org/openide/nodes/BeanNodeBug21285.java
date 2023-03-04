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
