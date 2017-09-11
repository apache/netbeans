/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.customizer;

import junit.framework.TestCase;

/**
 * @author mkleint
 */
public class RunJarPanelTest extends TestCase {
    
    public RunJarPanelTest(String testName) {
        super(testName);
    }            

    /**
     * Test of split* method, of class RunJarPanel.
     */
    public void testParams() {
        String line = "-Xmx256m org.milos.Main arg1";
        assertEquals("-Xmx256m", RunJarPanel.splitJVMParams(line));
        assertEquals("org.milos.Main", RunJarPanel.splitMainClass(line));
        assertEquals("arg1", RunJarPanel.splitParams(line));
        
        line = "-Xdebug -Djava.compiler=none -Xnoagent -Xrunjdwp:transport=dt_socket,server=n,address=${jpda.address} -classpath %classpath ${packageClassName}";
        assertEquals("-Xdebug -Djava.compiler=none -Xnoagent -Xrunjdwp:transport=dt_socket,server=n,address=${jpda.address} -classpath %classpath", RunJarPanel.splitJVMParams(line));
        assertEquals("${packageClassName}", RunJarPanel.splitMainClass(line));
        assertEquals("", RunJarPanel.splitParams(line));
        
        line = "-classpath %classpath ${packageClassName} %classpath ${packageClassName}";
        assertEquals("-classpath %classpath", RunJarPanel.splitJVMParams(line));
        assertEquals("${packageClassName}", RunJarPanel.splitMainClass(line));
        assertEquals("%classpath ${packageClassName}", RunJarPanel.splitParams(line));
        
        line = "Main arg1 arg2.xsjs.xjsj.MainParam";
        assertEquals("", RunJarPanel.splitJVMParams(line));
        assertEquals("Main", RunJarPanel.splitMainClass(line));
        assertEquals("arg1 arg2.xsjs.xjsj.MainParam", RunJarPanel.splitParams(line));
        
        //non trimmed line
        line = " -classpath %classpath ${packageClassName} %classpath ${packageClassName} ";
        assertEquals("-classpath %classpath", RunJarPanel.splitJVMParams(line));
        assertEquals("${packageClassName}", RunJarPanel.splitMainClass(line));
        assertEquals("%classpath ${packageClassName}", RunJarPanel.splitParams(line));

        //param with quotes and spaces..
        line = "-Dparam1=\"one two three\" -classpath %classpath ${packageClassName} %classpath ${packageClassName} ";
        assertEquals("-Dparam1=\"one two three\" -classpath %classpath", RunJarPanel.splitJVMParams(line));
        assertEquals("${packageClassName}", RunJarPanel.splitMainClass(line));
        assertEquals("%classpath ${packageClassName}", RunJarPanel.splitParams(line));
        line = "-D\"foo bar=baz quux\" -classpath %classpath my.App";
        assertEquals("-D\"foo bar=baz quux\" -classpath %classpath", RunJarPanel.splitJVMParams(line));
        assertEquals("my.App", RunJarPanel.splitMainClass(line));
        assertEquals("", RunJarPanel.splitParams(line));
        line = "\"-Dfoo bar=baz quux\" -classpath %classpath my.App"; // #199411
        assertEquals("\"-Dfoo bar=baz quux\" -classpath %classpath", RunJarPanel.splitJVMParams(line));
        assertEquals("my.App", RunJarPanel.splitMainClass(line));
        assertEquals("", RunJarPanel.splitParams(line));
    }


}
