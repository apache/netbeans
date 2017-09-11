/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
