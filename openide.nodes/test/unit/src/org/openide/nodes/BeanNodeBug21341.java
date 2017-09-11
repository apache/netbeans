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
