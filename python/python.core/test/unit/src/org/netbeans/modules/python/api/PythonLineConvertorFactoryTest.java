/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
        String line = "  File \"/Users/user/NetBeansProjects/NewPythonProject33/src/NewPythonProject33.py\", line 23";
        line = "  File \"/Users/user/NetBeansProjects/NewPythonProject33/src/NewPythonProject33.py\", line 29, in <module>";
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
        assertEquals("/Users/user/NetBeansProjects/NewPythonProject33/src/NewPythonProject33.py", fileNameRequest[0]);
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
        String line = "  File \"/Users/user/NetBeansProjects/NewPythonProject33/src/NewPythonProject33.py\", line 23";
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
        assertEquals("/Users/user/NetBeansProjects/NewPythonProject33/src/NewPythonProject33.py", fileNameRequest[0]);
        // TODO - I need a way to check the line number too - but the APIs in the recognizers don't expose this
    }

}
