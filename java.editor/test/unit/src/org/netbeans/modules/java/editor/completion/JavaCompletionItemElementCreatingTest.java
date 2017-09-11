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

package org.netbeans.modules.java.editor.completion;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public class JavaCompletionItemElementCreatingTest extends CompletionTestBase {

    public JavaCompletionItemElementCreatingTest(String testName) {
        super(testName);
    }

    public void testOverrideAbstractListWithPrefix() throws Exception {
        performTest("OverrideAbstractList", 118, "to", "toSt.*override", "OverrideAbstractListWithPrefix.pass2");
    }
    
    public void testOverrideFinalize() throws Exception {
        performTest("OverrideAbstractList", 118, "fin", "finali.*override", "OverrideFinalize.pass2");
    }
    
    public void testOverrideAbstractList2a() throws Exception {
        performTest("OverrideAbstractList2", 139, "ad", "add.*override", "OverrideAbstractList2a.pass2");
    }
    
    public void testOverrideAbstractList2b() throws Exception {
        performTest("OverrideAbstractList2", 139, "ge", "ge.*implement", "OverrideAbstractList2b.pass2");
    }
    
    public void testOverrideAbstractList3a() throws Exception {
        performTest("OverrideAbstractList3", 126, "ad", "add.*override", "OverrideAbstractList3a.pass2");
    }
    
    public void testOverrideAbstractList3b() throws Exception {
        performTest("OverrideAbstractList3", 126, "ge", "ge.*implement", "OverrideAbstractList3b.pass2");
    }
    
    public void testOverrideTypedException1() throws Exception {
        performTest("OverrideTypedException", 209, "tes", "tes.*override", "OverrideTypedException1.pass2");
    }
    
    public void testOverrideTypedException2() throws Exception {
        performTest("OverrideTypedException", 305, "tes", "tes.*override", "OverrideTypedException2.pass2");
    }
    
    public void testOverrideInInnerClass() throws Exception {
        performTest("OverrideInInnerClass", 185, "pai", "paint\\(.*override", "OverrideInInnerClass.pass2");
    }
}
