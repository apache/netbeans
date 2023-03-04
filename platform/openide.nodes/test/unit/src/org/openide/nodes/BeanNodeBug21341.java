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

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.Introspector;
import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;

/**
 * Regression test for bug #21285<br>
 * For more info please see the
 * <a href="http://openide.netbeans.org/issues/show_bug.cgi?id=21285">
 * descrition in issuezilla</a>
 *
 * @author  Petr Hrebejk
 */
public class BeanNodeBug21341 extends NbTestCase {


    /** Creates new TextTest */
    public BeanNodeBug21341(String s) {
        super(s);
    }
    
    public static void main(String[] args)throws Exception {

         
        BeanInfo bi = Introspector.getBeanInfo( Bean21341Hidden.class );
        BeanDescriptor bd = bi.getBeanDescriptor();
        
        System.out.println("  shortDescription : " + bd.getShortDescription() );
        System.out.println("  helpID           : " + bd.getValue( "HelpID" ) );
        //junit.textui.TestRunner.run(new NbTestSuite(BeanNodeBug21285.class));
    }


    /** Regression test to reproduce bug #21858. */
    public void testHelpID() throws Exception {

        BeanNode bn = new BeanNode( new Bean21341Hidden() );
        HelpCtx hCtx = bn.getHelpCtx();
        
        // System.out.println("HCTX " + hCtx.getHelpID() );
               
        assertTrue( "HelpID".equals( hCtx.getHelpID() ) );
    }
    
    public void testPropertiesHelpID() throws Exception {

        BeanNode bn = new BeanNode( new Bean21341Hidden() );
        Node.PropertySet[] ps = bn.getPropertySets();
        Node.PropertySet propertySet = null;
        
        for( int i = 0; i < ps.length; i++ ) {
            if ( Sheet.PROPERTIES.equals( ps[i].getName() ) ) {
                propertySet = ps[i];
                break;
            }
        }
        
        // System.out.println("PsHelpId " + propertySet.getValue( "helpID" ) );
        
        if ( propertySet == null ) {
            fail( "Property set not found" );
        }
        else {
            assertTrue( "PropertiesHelpID".equals( propertySet.getValue( "helpID" ) ) );
        }
    }
    
    public void testExpertHelpID() throws Exception {

        BeanNode bn = new BeanNode( new Bean21341Hidden() );
        Node.PropertySet[] ps = bn.getPropertySets();
        Node.PropertySet propertySet = null;
        
        for( int i = 0; i < ps.length; i++ ) {
            if ( Sheet.EXPERT.equals( ps[i].getName() ) ) {
                propertySet = ps[i];
                break;
            }
        }

        // System.out.println("ExHelpId " + propertySet.getValue( "helpID" ) );
        
        if ( propertySet == null ) {
            fail( "Property set not found" );
        }
        else {
            assertTrue( "ExpertHelpID".equals( propertySet.getValue( "helpID" ) ) );
        }
    }
    
}
