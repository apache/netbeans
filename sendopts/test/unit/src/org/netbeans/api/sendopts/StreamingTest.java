/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.api.sendopts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.sendopts.OptionGroups;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach
 */
public class StreamingTest extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
    }
    
    /** content to things to lookup to */
    private InstanceContent ic;

    private File tmpDir;
    
    public StreamingTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        tmpDir = getWorkDir();
        
        Lookup l = Lookup.getDefault();
        assertEquals(Lkp.class, l.getClass());
        Lkp lkp = (Lkp)l;
        
        this.ic = lkp.ic;
        // clear the lookup
        lkp.ic.set(Collections.emptyList(), null);
        lkp.ic.add(new P());
    }

    @Override
    protected void tearDown() throws Exception {
        Lookup l = Lookup.getDefault();
        assertEquals(Lkp.class, l.getClass());
        Lkp lkp = (Lkp)l;
        // clear the lookup
        lkp.ic.set(Collections.emptyList(), null);
    }

    public void testWeCanRegisterIOStream() throws Exception {
        StreamSinkProvider ssp = new StreamSinkProvider();
        ic.add(ssp);
        
        ByteArrayOutputStream osInner = new ByteArrayOutputStream();
        PrintStream os = new PrintStream(osInner);
        CommandLine.getDefault().process(new String[] { "--stream" }, null, os, null, tmpDir);
        
        assertNotNull("A sink was really created", ssp.created);
        ssp.created.close();
        assertEquals("Closed with right data", os, ssp.created.data);
    }
    
    public void testWeCanRegisterTwoSinks() throws Exception {
        StreamSinkProvider ssp = new StreamSinkProvider();
        ic.add(ssp);
        FileSinkProvider fsp = new FileSinkProvider();
        ic.add(fsp);
        
        CommandLine.getDefault().process(new String[] { "--file", "Ahoj.mpeg" }, null, null, null, tmpDir);
        
        assertNull("No stream", ssp.created);
        assertNotNull("A file sink was created", fsp.created);
        fsp.created.close();
        assertEquals("Closed with right data", new File(tmpDir, "Ahoj.mpeg"), fsp.created.data);
        
    }
    
    public static abstract class SinkProvider {
        /** associated option */
        final Option option;


        /** Constructor for subclasses to register their own {@link Sink}.
         * The option shall describe the content of command line that is necessary
         * for construction of the {@link Sink}.
         * 
         * @param option the option representing the command line part needed for
         *    construction of the {@link Sink}
         */
        protected SinkProvider(Option option) {
            this.option = option;
        }

        /** Returns an option that can be used to construct a "sink". The
         * option is required to parse the command line and process them
         * into an implementation of a "sink". The sink is then going to be
         * fed with read data.
         *
         * @return the sink
         */
        protected abstract Sink createSink(Env env, Map<Option,String[]> values)
        throws CommandException;
    }
    
    public static abstract class Sink {
        public static Sink create(String n, WritableByteChannel b, boolean x) {
            return null;
        }
    }
    
    private class StreamSinkProvider extends SinkProvider {
        WBC created;
        
        public StreamSinkProvider() {
            super(Option.withoutArgument(Option.NO_SHORT_NAME, "stream"));
        }

        protected Sink createSink(Env env, Map<Option, String[]> values) throws CommandException {
            created = new WBC(env.getOutputStream());
            return Sink.create("no name", created, true);
        }
        
        private class WBC implements WritableByteChannel {
            private OutputStream data;
            
            public WBC(OutputStream d) {
                this.data = d;
            }
            
            public void close() throws IOException {
                assertNotNull("Some data", this.data);
            }

            public int write(ByteBuffer src) throws IOException {
                return src.remaining();
            }

            public boolean isOpen() {
                return true;
            }
        }
    }

    private static Option file = Option.requiredArgument(Option.NO_SHORT_NAME, "file");
    private class FileSinkProvider extends SinkProvider {
        WBC created;
        

        protected FileSinkProvider() {
            super(file);
        }

        protected Sink createSink(Env env, Map<Option, String[]> values) throws CommandException {
            File f = new File(env.getCurrentDirectory(), values.get(file)[0]);
            created = new WBC(f);
            return Sink.create("some file", created, true);
        }
        
        private class WBC implements WritableByteChannel {
            private File data;
            
            public WBC(File d) {
                this.data = d;
            }
            
            public void close() throws IOException {
                assertNotNull("Some data", this.data);
            }

            public int write(ByteBuffer src) throws IOException {
                return src.remaining();
            }

            public boolean isOpen() {
                return true;
            }
        }
    }
    
    public static final class Lkp extends AbstractLookup {
        public InstanceContent ic;
        
        public Lkp() {
            this(new InstanceContent());
        }
        
        private Lkp(InstanceContent ic) {
            super(ic);
            this.ic = ic;
        }
        
    }

    public static final class P extends OptionProcessor {
        public P() {
        }
        
        private final List<Option> all() {
            List<Option> list = new ArrayList<Option>();
            for (SinkProvider sp: Lookup.getDefault().lookupAll(SinkProvider.class)) {
                list.add(sp.option);
            }
            return list;
        }

        protected Set<Option> getOptions() {
            Option o = OptionGroups.oneOf(all().toArray(new Option[0]));
            return Collections.singleton(o);
        }
        

        protected void process(Env env, Map<Option, String[]> values) throws CommandException {
            boolean was = false;
            for (SinkProvider sp: Lookup.getDefault().lookupAll(SinkProvider.class)) {
                if (values.containsKey(sp.option)) {
                    assertFalse("Not called yet", was);
                    sp.createSink(env, values);
                    was = true;
                }
            }
        }
    }
}
