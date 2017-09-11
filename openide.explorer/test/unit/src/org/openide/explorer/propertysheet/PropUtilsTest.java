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

import junit.framework.TestCase;
import org.openide.nodes.Node;

/**
 * @author mkrauskopf
 */
public class PropUtilsTest extends TestCase {

    public PropUtilsTest(String testName) {
        super(testName);
    }

    public void testCreateHtmlTooltip() {
        System.out.println("testCreateHtmlTooltip");
        // slash-separated
        String expectedResult = "<html><b><u>TitleTest</u></b><br>/usr/share/" +
                "java/netbeans-cvs-current/openide/test/unit/src/org/<br>" +
                "openide/explorer/propertysheet/SomeTest.java</html>";
        String result = PropUtils.createHtmlTooltip("TitleTest",
                "/usr/share/java/netbeans-cvs-current/openide/test/unit/src" +
                "/org/openide/explorer/propertysheet/SomeTest.java");
        assertEquals("Unexpected result. "
                + "\n  Expected: " + expectedResult
                + "\n  Actual  : " + result,
                expectedResult, result);
        
        // comma-separated
        expectedResult = "<html><b><u>TitleTest</u></b><br>Overridden to " +
                "supply different tooltips depending on mouse position<br> " +
                "(name, value, custom editor button).  Will HTML-ize long " +
                "tooltips<br></html>";
        result = PropUtils.createHtmlTooltip("TitleTest", "Overridden to supply " +
                "different tooltips depending on mouse position (name, value, " +
                "custom editor button).  Will HTML-ize long tooltips");
        assertEquals("Unexpected result. " +
                "\n  Expected: " + expectedResult +
                "\n  Actual  : " + result,
                expectedResult, result);
    }
    
    /* Tests whether "Restore Default Value" enabling/disabling works well. */
    public void testRestoreDefaultValueBehaviour() {
        System.out.println("testRestoreDefaultValueBehaviour");
        
        Node.Property trueProp = new OldModulePropertyWithSDVReturningTrue();
        assertTrue("OldModuleProperty doesn't know about Node.Property.isDefaultValue()" +
                " therefore it should be enabled in every case.",
                PropUtils.shallBeRDVEnabled(trueProp));
        
        Node.Property falseProp = new PropertyWithSDVReturningFalse();
        assertFalse("Property doesn't support default value. It should be " +
                "disabled", PropUtils.shallBeRDVEnabled(falseProp));
        
        Node.Property newIDVFalseProp = new BothMethodsOverridedPropertyWithIDSReturningFalse();
        assertTrue("Correctly implemented property with isDefaultValue() " +
                "returning false should be enable.",
                PropUtils.shallBeRDVEnabled(newIDVFalseProp));
        
        Node.Property newIDVTrueProp = new BothMethodsOverridedPropertyWithIDSReturningTrue();
        assertFalse("Correctly implemented property with isDefaultValue() " +
                "returning true should be disabled.",
                PropUtils.shallBeRDVEnabled(newIDVTrueProp));
        
        Node.Property noneOverrided = new DefaultTestProperty();
        assertFalse("Correctly implemented property which doesn't override any " +
                "of the two method should be disabled",
                PropUtils.shallBeRDVEnabled(noneOverrided));
    }
    
    /**
     * Simulates property for old modules which didn't know about
     * isDefaultValue() method but could overrode restoreDefaultValue().
     */
    private static final class OldModulePropertyWithSDVReturningTrue extends DefaultTestProperty {
        public boolean supportsDefaultValue()  {
            return true;
        }
    }
    
    private static final class PropertyWithSDVReturningFalse extends DefaultTestProperty {
        public boolean supportsDefaultValue()  {
            return false;
        }
    }
    
    /**
     * Simulates correctly implemented property which override both methods.
     */
    private static final class BothMethodsOverridedPropertyWithIDSReturningFalse
            extends DefaultTestProperty {
        public boolean supportsDefaultValue()  {
            return true;
        }
        public boolean isDefaultValue() {
            return false;
        }
    }
    
    private static final class BothMethodsOverridedPropertyWithIDSReturningTrue
            extends DefaultTestProperty {
        public boolean supportsDefaultValue()  {
            return true;
        }
        public boolean isDefaultValue() {
            return true;
        }
    }
    
    /**
     * Simulates correctly implemented property which doesn't override any of
     * the methods (supportsDefaultValue(), isDefaultValue()).
     */
    private static class DefaultTestProperty extends Node.Property {
        /** We don't need any of these method (or constructor) for our testing. */
        public DefaultTestProperty() { super(Object.class); }
        public void setValue(Object val) {}
        public Object getValue() { return null; }
        public boolean canWrite() { return false; }
        public boolean canRead() { return false; }
    }
}
