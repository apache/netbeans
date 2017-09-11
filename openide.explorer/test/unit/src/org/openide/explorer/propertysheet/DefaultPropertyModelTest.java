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

package org.openide.explorer.propertysheet;

import java.awt.IllegalComponentStateException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import org.netbeans.junit.NbTestCase;

/** A test of a property model.
 */
public class DefaultPropertyModelTest extends NbTestCase {

    public DefaultPropertyModelTest(String name) {
        super(name);
    }

    public void testLookupOfAPropertyReadOnlyProperty() throws Exception {
        Object obj = new Object();
        DefaultPropertyModel model = new DefaultPropertyModel(obj, "class");
        
        
        assertEquals("Calls the get method", model.getValue(), obj.getClass());
    }
    
    public void testLookupOfAPropertyReadWriteProperty() throws Exception {
        ServerSocket obj = new ServerSocket(0);
        
        DefaultPropertyModel model = new DefaultPropertyModel(obj, "soTimeout");
        
        
        assertEquals("Calls the get method", model.getValue(), new Integer(obj.getSoTimeout()));
        
        model.setValue(new Integer(100));
        
        assertEquals("Value change", 100, obj.getSoTimeout());
        assertEquals("Model updated", model.getValue(), new Integer(obj.getSoTimeout()));
    }
    
    //
    // Test of explicit beaninfo
    //
    
    public void testUsageOfExplicitPropertyDescriptor() throws Exception {
        PropertyDescriptor pd = new PropertyDescriptor(
                "myProp", this.getClass(),
                "getterUsageOfExplicitPropertyDescriptor",
                "setterUsageOfExplicitPropertyDescriptor"
                );
        
        DefaultPropertyModel model = new DefaultPropertyModel(this, pd);
        
        assertEquals("Getter returns this", model.getValue(), this);
        
        String msgToThrow = "msgToThrow";
        try {
            model.setValue(msgToThrow);
            fail("Setter should throw an exception");
        } catch (InvocationTargetException ex) {
            // when an exception occurs it should throw InvocationTargetException
            assertEquals("The right message", msgToThrow, ex.getTargetException().getMessage());
        }
    }
    
    public Object getterUsageOfExplicitPropertyDescriptor() {
        return this;
    }
    
    public void setterUsageOfExplicitPropertyDescriptor(Object any) {
        throw new IllegalComponentStateException(any.toString());
    }
    
    //
    // End of explicit beaninfo
    //
}

