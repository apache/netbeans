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

package org.netbeans.modules.python.api;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.openide.filesystems.FileObject;
import org.openide.windows.OutputListener;

/**
 *
 * @author tor
 */
public class PythonLineConvertorFactoryTest {

    public PythonLineConvertorFactoryTest() {
    }

    /**
     * Test of create method, of class PythonLineConvertorFactory.
     */
    @Test
    public void testRecognizeBackTrace1() {
        final String[] fileNameRequest = new String[1];
        FileLocator locator = new FileLocator() {

            @Override
            public FileObject find(String filename) {
                fileNameRequest[0] = filename;
                return null;
            }
            
        };
        String line = "  File \"/Users/tor/NetBeansProjects/NewPythonProject33/src/NewPythonProject33.py\", line 23";
        line = "  File \"/Users/tor/NetBeansProjects/NewPythonProject33/src/NewPythonProject33.py\", line 29, in <module>";
        List<LineConvertor> standardConvertors = PythonLineConvertorFactory.getStandardConvertors(locator);
        for (LineConvertor c : standardConvertors) {
            List<ConvertedLine> lines = c.convert(line);
            if (lines != null) {
                for (ConvertedLine cl : lines) {
                    OutputListener listener = cl.getListener();
                    listener.outputLineAction(null);
                }
            }
            c.convert(line);
        }
        assertNotNull(fileNameRequest[0]);
        assertEquals("/Users/tor/NetBeansProjects/NewPythonProject33/src/NewPythonProject33.py", fileNameRequest[0]);
        // TODO - I need a way to check the line number too - but the APIs in the recognizers don't expose this
    }

    @Test
    public void testRecognizeBackTrace2() {
        final String[] fileNameRequest = new String[1];
        FileLocator locator = new FileLocator() {

            @Override
            public FileObject find(String filename) {
                fileNameRequest[0] = filename;
                return null;
            }

        };
        String line = "  File \"/Users/tor/NetBeansProjects/NewPythonProject33/src/NewPythonProject33.py\", line 23";
        List<LineConvertor> standardConvertors = PythonLineConvertorFactory.getStandardConvertors(locator);
        for (LineConvertor c : standardConvertors) {
            List<ConvertedLine> lines = c.convert(line);
            if (lines != null) {
                for (ConvertedLine cl : lines) {
                    OutputListener listener = cl.getListener();
                    listener.outputLineAction(null);
                }
            }
            c.convert(line);
        }
        assertNotNull(fileNameRequest[0]);
        assertEquals("/Users/tor/NetBeansProjects/NewPythonProject33/src/NewPythonProject33.py", fileNameRequest[0]);
        // TODO - I need a way to check the line number too - but the APIs in the recognizers don't expose this
    }

}
