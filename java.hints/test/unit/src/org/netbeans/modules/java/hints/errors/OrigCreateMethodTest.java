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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.errors;

import org.netbeans.modules.java.hints.infrastructure.HintsTestBase;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class OrigCreateMethodTest extends HintsTestBase {

    public OrigCreateMethodTest(String name) {
        super(name);
    }
    
    public void testCreateElement1() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement1", "Method", 23, 16);
    }

    public void testCreateElement2() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement2", "Method", 23, 16);
    }

    public void testCreateElement3() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement3", "Method", 24, 16);
    }

    public void testCreateElement4() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement4", "Method", 23, 16);
    }

    public void testCreateElement5() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement5", "Method", 23, 16);
    }

    public void testCreateElement6() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement6", "Method", 23, 16);
    }

    public void testCreateElement7() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement7", "Method", 23, 16);
    }

    public void testCreateElement8() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement8", "Method", 24, 16);
    }

    public void testCreateElement9() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement9", "Method", 23, 16);
    }

    public void testCreateElementa() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElementa", "Method", 23, 16);
    }
    
    public void testCreateConstructor1() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateConstructor1", "Create Constructor", 9, 16);
    }

    @Override
    protected String testDataExtension() {
        return "org/netbeans/test/java/hints/OrigCreateMethodTest/";
    }

    static {
        NbBundle.setBranding("test");
    }
}
