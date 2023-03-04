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
package org.netbeans.api.sendopts;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.sendopts.OptionAnnotationProcessor;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class StandaloneTest extends NbTestCase {
    private URLClassLoader loader;
    private Class<?> classCommandLine;
    private Method methodProcess;
    private Class<?> classOptions;
    private Method methodUsage;
    
    public StandaloneTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        URL api = CommandLine.class.getProtectionDomain().getCodeSource().getLocation();
        URL options = Options.class.getProtectionDomain().getCodeSource().getLocation();
        
        loader = new URLClassLoader(
            new URL[] { api, options },
            CommandLine.class.getClassLoader().getParent()
        );
        Thread.currentThread().setContextClassLoader(loader);
        
        classCommandLine = loader.loadClass(CommandLine.class.getName());
        classOptions = loader.loadClass(Options.class.getName());
        methodProcess = classCommandLine.getMethod("process", String[].class);
        methodUsage = classCommandLine.getMethod("usage", PrintWriter.class);
    }
    
    
    
    public void testStandaloneUsage() throws Exception {
        Method factory = classCommandLine.getMethod("create", Class[].class);
        final Object arr = new Class[] { classOptions };
        Object cli = factory.invoke(null, arr);
        final Object param = new String[] { "--value=myvalue" };
        methodProcess.invoke(cli, param);
        
        assertEquals("myvalue", System.getProperty("key"));
    }

    public void testStandaloneUsageOfUsageMethod() throws Exception {
        Method factory = classCommandLine.getMethod("create", Class[].class);
        final Object arr = new Class[] { classOptions };
        Object cli = factory.invoke(null, arr);
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);
        methodUsage.invoke(cli, pw);
        
        if (w.toString().indexOf("--checkbox") == -1) {
            fail("--checkbox should be in the output:\n" + w);
        }
        if (w.toString().indexOf("Shows a value") == -1) {
            fail("'Shows a value' should be in the output:\n" + w);
        }
        if (w.toString().indexOf("Computes real value") == -1) {
            fail("'Computes real value' should be in the output:\n" + w);
        }
    }
    
    public void testStandaloneGetDefaultUsage() throws Exception {
        Method def = classCommandLine.getMethod("getDefault");
        Object cli = def.invoke(null);
        final Object param = new String[] { "--serviceloadervalue=defaultValue" };
        methodProcess.invoke(cli, param);
        
        assertEquals("defaultValue", System.getProperty("serviceloadervalue"));
    }
    
    public void testStandaloneProcessor() throws Exception {
        for (Object p : ServiceLoader.load(javax.annotation.processing.Processor.class, loader)) {
            if (p.getClass().getName().equals(OptionAnnotationProcessor.class.getName())) {
                return;
            }
        }
        fail("Our annotation processor not found!");
    }
    
    public static final class Options implements Runnable {
        @Description(
            displayName="org.netbeans.api.sendopts.TestBundle#SHOW",
            shortDescription="org.netbeans.api.sendopts.TestBundle#COMPUTE"
        )
        @Arg(longName="value")
        public String value;
        
        @Arg(longName="checkbox")
        public boolean check;
        
        @Override
        public void run() {
            System.setProperty("key", value);
        }
    }
    
    @ServiceProvider(service=OptionProcessor.class)
    public static final class SLProcessor extends OptionProcessor {
        private static final Option VALUE = Option.requiredArgument(Option.NO_SHORT_NAME, "serviceloadervalue");

        @Override
        protected Set<Option> getOptions() {
            return Collections.singleton(VALUE);
        }

        @Override
        protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
            System.setProperty("serviceloadervalue", optionValues.get(VALUE)[0]);
        }
        
    }
}
