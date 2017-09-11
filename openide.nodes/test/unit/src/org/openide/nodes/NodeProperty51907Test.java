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

import java.util.Date;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Tests for issue 51907. For more information see the
 * <a href="http://openide.netbeans.org/issues/show_bug.cgi?id=51907">
 * descrition in issuezilla</a>
 *
 * @author mkrauskopf
 */
public class NodeProperty51907Test extends NbTestCase {
    
    public NodeProperty51907Test(String name) {
        super(name);
    }
        
    /**
     * Note that for this test it doesn't matter what isDefaultValue() methods
     * return.
     */
    public void testThatWarningIsLoggedForOldModulesProperty() {
        CharSequence log = Log.enable("", Level.WARNING);
        Node.Property property = new OldModulePropertyWithSDVReturningTrue();
        // ErrorManager should log warning
        property.isDefaultValue();
        String className = property.getClass().getName();
        assertTrue("The WARNING message should contain name of the property" +
                "class - " + className + " was log:\n" + log, log.toString().indexOf(className) >= 0);


        int len = log.length();
        
        // ErrorManager shouldn't log warning more than once per property
        property.isDefaultValue();
        assertEquals("No other message logged", len, log.length());

        Node.Property otherInstance = new OldModulePropertyWithSDVReturningTrue();
        otherInstance.isDefaultValue();
        assertEquals("No other message logged2", len, log.length());
    }
    
    public void testThatWarningIsNotLoggedForPropertyWithBothMethodsOverrided() {
        CharSequence log = Log.enable("", Level.WARNING);
        
        Node.Property property = new BothMethodsOverridedProperty();
        // ErrorManager shouldn't log warning for correct implementations
        property.isDefaultValue();
        assertEquals("There shouldn't be any WARNING message logged by the ErrorManager", 0, log.length());
    }
    
    public void testThatWarningIsNotLoggedForPropertyWithNoneMethodOverrided() {
        CharSequence log = Log.enable("", Level.WARNING);
        
        Node.Property property = new DefaultTestProperty();
        // ErrorManager shouldn't log warning for correct implementations
        property.isDefaultValue();
        assertEquals("There shouldn't be any WARNING message logged by the ErrorManager", 0, log.length());
    }

    
    /**
     * Simulates property for old modules which didn't know about
     * isDefaultValue() method but could overrode restoreDefaultValue() to 
     * returns true. Warning has to be logged for such properties.
     */
    private static final class OldModulePropertyWithSDVReturningTrue
            extends DefaultTestProperty {
        public boolean supportsDefaultValue()  {
            return true;
        }
    }
    
    /**
     * Simulates correctly implemented property which override both methods.
     */
    private static final class BothMethodsOverridedProperty
            extends DefaultTestProperty {
        public boolean supportsDefaultValue()  {
            return true;
        }
        public boolean isDefaultValue() {
            return false;
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
