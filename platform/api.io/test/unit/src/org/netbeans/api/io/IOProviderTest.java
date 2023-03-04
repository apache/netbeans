/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.api.io;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.junit.MockServices;
import org.netbeans.spi.io.InputOutputProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author jhavlin
 */
public class IOProviderTest {

    public IOProviderTest() {
    }

    @Test
    public void useCase1() {
        InputOutput io = IOProvider.getDefault().getIO("UseCase1", true);
        io.getOut().println("This is a simple output");
        io.getOut().close();
    }

    @Test
    public void useCase2() {
        InputOutput io = IOProvider.getDefault().getIO("UseCase2", false);
        io.getOut().print("A line containing a ");
        io.getOut().print("hyperlink", Hyperlink.from(new Runnable() {

            @Override
            public void run() {
                // some action
            }
        }));
        io.getOut().println(" for a URI.");
        io.getOut().close();
    }

    @Test
    public void useCase3() {
        InputOutput io = IOProvider.getDefault().getIO("UseCase3", true);
        io.getOut().print("A line containing a ");
        io.getOut().print("hyperlink", Hyperlink.from(new Runnable() {
            @Override
            public void run() {
                System.gc();
            }
        }));
        io.getOut().println(" for invokation of custom code.");
        io.getOut().close();
    }

    @Test
    public void useCase4() {
        InputOutput io = IOProvider.getDefault().getIO("UseCase4", true);
        io.getOut().println("Let's print some info", OutputColor.debug());
        io.getOut().println("or warning with appropriate color",
                OutputColor.warning());
        io.getOut().println("Maybe also text with custom reddish color",
                OutputColor.rgb(255, 16, 16));
        io.getOut().close();
    }

    @Test
    public void useCase5() {
        InputOutput io = IOProvider.getDefault().getIO("UseCase5", true);
        io.getOut().println("Let's print some text");
        io.getErr().println("and reset the pane immediately.");
        io.reset();
        io.getOut().println("The pane is now empty and we can reuse it simply");
        io.getOut().close();
    }

    @Test
    public void testTrivialImplementationAlwaysAvailable() {
        assertEquals("Trivial", IOProvider.getDefault().getId());
        assertEquals("Trivial", IOProvider.get("Trivial").getId());
        assertEquals("Trivial", IOProvider.get("Another").getId());
    }

    @Test
    public void testGetFromLookup() {
        MockServices.setServices(MockInputOutputProvider.class);
        try {
            assertEquals("mock", IOProvider.getDefault().getId());
            assertEquals("mock", IOProvider.get("mock").getId());
            assertEquals("mock", IOProvider.get("wrong").getId());
        } finally {
            MockServices.setServices();
        }
    }

    @Test
    public void testAllMethodsAreDelegatedToSPI() {
        MockServices.setServices(MockInputOutputProvider.class);
        try {
            IOProvider.getDefault().getIO("test1", true);
            Lookup lkp = IOProvider.getDefault()
                    .getIO("test1", false, Lookup.EMPTY).getLookup();
            CalledMethodList list = lkp.lookup(CalledMethodList.class);
            assertEquals("getIO", list.get(0));
            assertEquals("getIO", list.get(1));
            assertEquals("getIOLookup", list.get(2));
            assertEquals(3, list.size());
        } finally {
            MockServices.setServices();
        }
    }

    @SuppressWarnings("PackageVisibleInnerClass")
    static class CalledMethodList extends ArrayList<String> {
    }

    @SuppressWarnings("PublicInnerClass")
    public static class MockInputOutputProvider implements
            InputOutputProvider<Object, PrintWriter, Object, Object> {

        private final CalledMethodList calledMethods = new CalledMethodList();
        private final StringWriter stringWriter = new StringWriter();
        private final Lookup lookup = Lookups.fixed(calledMethods, stringWriter);

        @Override
        public String getId() {
            return "mock";
        }

        @Override
        public Object getIO(String name, boolean newIO, Lookup lookup) {
            calledMethods.add("getIO");
            return new Object();
        }

        @Override
        public Reader getIn(Object io) {
            calledMethods.add("getIn");
            return new StringReader("");
        }

        @Override
        public PrintWriter getOut(Object io) {
            calledMethods.add("getOut");
            return new PrintWriter(stringWriter);
        }

        @Override
        public PrintWriter getErr(Object io) {
            calledMethods.add("getErr");
            return new PrintWriter(stringWriter);
        }

        @Override
        public void print(Object io, PrintWriter writer, String text,
                Hyperlink link, OutputColor color, boolean printLineEnd) {
            if (link != null || color != null) {
                stringWriter.append("<ext");
                stringWriter.append(color != null ? " color" : "");
                stringWriter.append(link != null ? " link" : "");
                stringWriter.append(">");
            }
            stringWriter.append(text);
            if (link != null || color != null) {
                stringWriter.append("</ext>");
            }
            calledMethods.add("print");
            if (printLineEnd) {
                stringWriter.append(System.getProperty("line.separator"));
            }
        }

        @Override
        public Lookup getIOLookup(Object io) {
            calledMethods.add("getIOLookup");
            return lookup;
        }

        @Override
        public void resetIO(Object io) {
            calledMethods.add("resetIO");
        }

        @Override
        public void showIO(Object io,
                Set<ShowOperation> operations) {
            calledMethods.add("showIO");
        }

        @Override
        public void closeIO(Object io) {
            calledMethods.add("closeIO");
        }

        @Override
        public boolean isIOClosed(Object io) {
            calledMethods.add("isIOClosed");
            return false;
        }

        @Override
        public Object getCurrentPosition(Object io, PrintWriter writer) {
            calledMethods.add("getCurrentPosition");
            return new Object();
        }

        @Override
        public void scrollTo(Object io, PrintWriter writer, Object position) {
            calledMethods.add("scrollTo");
        }

        @Override
        public Object startFold(Object io, PrintWriter writer, boolean expanded) {
            calledMethods.add("startFold");
            return new Object();
        }

        @Override
        public void endFold(Object io, PrintWriter writer, Object foldNumber) {
            calledMethods.add("endFold");
        }

        @Override
        public void setFoldExpanded(Object io, PrintWriter writer,
                Object foldNumber, boolean expanded) {
            calledMethods.add("setFoldExpanded");
        }

        @Override
        public String getIODescription(Object io) {
            calledMethods.add("getIODescription");
            return null;
        }

        @Override
        public void setIODescription(Object io, String description) {
            calledMethods.add("setIODescription");
        }
    }
}
