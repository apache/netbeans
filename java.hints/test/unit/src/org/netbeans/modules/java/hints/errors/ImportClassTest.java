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
package org.netbeans.modules.java.hints.errors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.java.hints.errors.ImportClass.FixImport;
import org.netbeans.modules.java.hints.infrastructure.HintsTestBase;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan Lahoda
 */
public class ImportClassTest extends HintsTestBase {
    
    /** Creates a new instance of ImportClassEnablerTest */
    public ImportClassTest(String name) {
        super(name);
    }

    public void testImportHint() throws Exception {
        performTest("ImportTest", "java.util.List", 22, 13);
    }

    public void testImportHint2() throws Exception {
        performTest("ImportTest2", "java.util.List", 18, 13);
    }
    
    public void testImportHint3() throws Exception {
        performTest("ImportTest3", "java.util.ArrayList", 9, 13);
    }
    
    public void testImportHint4() throws Exception {
        performTest("ImportTest4", "java.util.Collections", 7, 13);
    }
    
    public void testImportHint5() throws Exception {
        performTest("ImportTest5", "java.util.Map", 7, 13);
    }
    
    public void testImportHint6() throws Exception {
        performTest("ImportTest6", "java.util.Collections", 7, 13);
    }
    
    public void testImportHintDoNotPropose1() throws Exception {
        performTestDoNotPerform("ImportHintDoNotPropose", 10, 24);
    }

    public void testImportHintDoNotPropose2() throws Exception {
        performTestDoNotPerform("ImportHintDoNotPropose", 11, 24);
    }

    public void testImportHint118714() throws Exception {
        performTestDoNotPerform("ImportTest118714", 8, 11);
    }

    public void testImportHint86932() throws Exception {
        performTestDoNotPerform("ImportTest86932", 6, 25);
    }

    public void testImportHint194018a() throws Exception {
        performTest("ImportInImport", "java.util.Map", 3, 8);
    }

    public void testImportHint194018b() throws Exception {
        performTest("ImportInImport", "java.util.Map", 4, 8);
    }

    public void testImportHint194018c() throws Exception {
        performTest("ImportInImport", "java.util.Collections", 5, 8);
    }

    public void testImportHint194018d() throws Exception {
        performTest("ImportInImport", "java.util.Collections", 6, 8);
    }
    
    @Override
    protected String testDataExtension() {
        return "org/netbeans/test/java/hints/ImportClassEnablerTest/";
    }
    
    @Override
    protected String layer() {
        return "org/netbeans/modules/java/hints/errors/only-imports-layer.xml";
    }

    private static final Set<String> IGNORED_IMPORTS = new HashSet<String>(Arrays.asList("com.sun.tools.javac.util.List", "com.sun.xml.internal.bind.v2.schemagen.xmlschema.List", "com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections"));
    @Override
    protected boolean includeFix(Fix f) {
        if (!(f instanceof FixImport)) {
            return true;
        }

        for (String ignore : IGNORED_IMPORTS) {
            if (f.getText().contains(ignore)) {
                return false;
            }
        }
        
        return true;
    }


}
