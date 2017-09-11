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

package org.netbeans.test.java.editor.completion;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.spi.editor.completion.CompletionProvider;

/**
 *
 * @author jp159440
 */
public class SmartCCTests extends CompletionTestPerformer{
    
   
    public SmartCCTests(String name) {
        super(name);
    }
    
     
    public void testsmartassign() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "Double x = ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Assign.java", 11,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartassign2() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "Number x = ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Assign.java", 11,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartassign3() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "Number x = new ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Assign.java", 11,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartassign4() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "List x = new ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Assign.java", 11,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartassign5() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "String x = ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Assign.java", 11,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartExtends() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "class A extends ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Types.java", 32,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartImplements() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "class B implements ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Types.java", 32,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartThrows() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "public void method() throws ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Types.java", 32,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartAnnotation() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "@", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Types.java", 32,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartImport() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "import ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Types.java", 21,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartImportStatic() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "import static java.awt.Color.", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Types.java", 21,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartSuperParameter() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "super(", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/SmartCC.java", 31,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartInnerClassAsParameter() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "method(", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/SmartCC.java", 41,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartInnerClassAsParameter2() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "method( new ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/SmartCC.java", 41,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartReturn() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "return ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/SmartCC.java", 41,CompletionProvider.COMPLETION_QUERY_TYPE);                
    }
    
    public void testsmartReturn2() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "return new ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/SmartCC.java", 41,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartCatch() throws Exception {
        new CompletionTestCase(this).test(outputWriter, logWriter, "catch (", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/SmartCC.java", 51,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(SmartCCTests.class).enableModules(".*").clusters(".*"));
    }
    
}
