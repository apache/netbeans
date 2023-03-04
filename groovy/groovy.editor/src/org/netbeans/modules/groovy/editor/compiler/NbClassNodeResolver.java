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
package org.netbeans.modules.groovy.editor.compiler;

import groovy.lang.GroovyClassLoader;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.decompiled.AsmDecompiler;
import org.codehaus.groovy.ast.decompiled.AsmReferenceResolver;
import org.codehaus.groovy.ast.decompiled.ClassStub;
import org.codehaus.groovy.ast.decompiled.DecompiledClassNode;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.control.ClassNodeResolver;
import org.codehaus.groovy.control.SourceUnit;

/**
 * A copy of {@link ClassNodeResolver}. I needed to use a different implementation of {@link #AsmReferenceResolver}, but
 * it's used in a private method, so the call chain from the 1st public method up to that changed behaviour was copied.
 * An alternative would be a protected factory method in Groovy.
 * 
 * @author sdedic
 */
public class NbClassNodeResolver extends ClassNodeResolver {
    public LookupResult findClassNode(String name, org.codehaus.groovy.control.CompilationUnit compilationUnit) {
        return tryAsLoaderClassOrScript(name, compilationUnit);
    }

    /**
     * This method is used to realize the lookup of a class using the compilation
     * unit class loader. Should no class be found we fall back to a script lookup.
     * If a class is found we check if there is also a script and maybe use that
     * one in case it is newer.<p/>
     *
     * Two class search strategies are possible: by ASM decompilation or by usual Java classloading.
     * The latter is slower but is unavoidable for scripts executed in dynamic environments where
     * the referenced classes might only be available in the classloader, not on disk.
     */
    private LookupResult tryAsLoaderClassOrScript(String name, org.codehaus.groovy.control.CompilationUnit compilationUnit) {
        GroovyClassLoader loader = compilationUnit.getClassLoader();

        Map<String, Boolean> options = compilationUnit.getConfiguration().getOptimizationOptions();
        boolean useAsm = !Boolean.FALSE.equals(options.get("asmResolving"));
        boolean useClassLoader = !Boolean.FALSE.equals(options.get("classLoaderResolving"));

        LookupResult result = useAsm ? findDecompiled(name, compilationUnit, loader) : null;
        if (result != null) {
            return result;
        }

        // the options always mandate disable class loading, so simplifying the original
        // implementation:
        return tryAsScript(name, compilationUnit, null);
    }

    protected AsmReferenceResolver createReferencesResolver(org.codehaus.groovy.control.CompilationUnit unit) {
        return new AsmReferenceResolver(this, unit);
    }
    
    protected ClassStub parseClass(URL resource) throws IOException {
        return AsmDecompiler.parseClass(resource);
    }
    
    /**
     * Holds timestamps of compilation from the original archive entry's timestamp. The original
     * implementation relies on __TIMESTAMP__ field to be present, but we know the time from
     * the archive's entry.
     */
    private static class TimestampedClassNode extends DecompiledClassNode {
        private final long timestamp;
        
        public TimestampedClassNode(long timestamp, ClassStub data, AsmReferenceResolver resolver) {
            super(data, resolver);
            this.timestamp = timestamp;
        }

        @Override
        public long getCompilationTimeStamp() {
            return timestamp;
        }
    }
    
    /**
     * Search for classes using ASM decompiler
     */
    private LookupResult findDecompiled(String name, org.codehaus.groovy.control.CompilationUnit compilationUnit, GroovyClassLoader loader) {
        ClassNode node = ClassHelper.make(name);
        if (node.isResolved()) {
            return new LookupResult(null, node);
        }

        DecompiledClassNode asmClass = null;
        String fileName = name.replace('.', '/') + ".class";
        URL resource = loader.getResource(fileName);
        long t = System.currentTimeMillis();
        long t2;
        if (resource != null) {
            URLConnection c;
            try {
                c = resource.openConnection();
                asmClass = new TimestampedClassNode(c.getLastModified(), parseClass(resource), createReferencesResolver(compilationUnit));
                if (!asmClass.getName().equals(name)) {
                    // this may happen under Windows because getResource is case insensitive under that OS!
                    asmClass = null;
                }
            } catch (IOException e) {
                // fall through and attempt other search strategies
            } finally {
                t2 = System.currentTimeMillis();
                PerfData.context().addPerfCounter("findDecompiled - parsing", t2 - t);
            }
        }
        if (asmClass != null) {
            // originally there was a check that the class may have come from a different ClassLoader; it's now always the case.
            // TBD: should we even test for the class coming from a source ??
            long t3 = System.currentTimeMillis();
            long t5 = 0;
            try {
                LookupResult lr = tryAsScript(name, compilationUnit, asmClass);
                t5 = System.currentTimeMillis();
                return lr;
            } finally {
                if (t5 > 0) {
                    PerfData.context().addPerfCounter("findDecompiled - tryAsScrupt", t5 - t3);
                }
            }
        }
        return null;
    }

    /**
     * try to find a script using the compilation unit class loader.
     */
    private static LookupResult tryAsScript(String name, org.codehaus.groovy.control.CompilationUnit compilationUnit, ClassNode oldClass) {
        LookupResult lr = null;
        if (oldClass!=null) {
            lr = new LookupResult(null, oldClass);
        }
        
        if (name.startsWith("java.")) return lr;
        //TODO: don't ignore inner static classes completely
        if (name.indexOf('$') != -1) return lr;
        
        // try to find a script from classpath*/
        GroovyClassLoader gcl = compilationUnit.getClassLoader();
        URL url = null;
        try {
            url = gcl.getResourceLoader().loadGroovySource(name);
        } catch (MalformedURLException e) {
            // fall through and let the URL be null
        }
        if (url != null && ( oldClass==null || isSourceNewer(url, oldClass))) {
            SourceUnit su = compilationUnit.addSource(url);
            return new LookupResult(su,null);
        }
        return lr;
    }

    /**
     * returns true if the source in URL is newer than the class
     * NOTE: copied from GroovyClassLoader
     */
    private static boolean isSourceNewer(URL source, ClassNode cls) {
        try {
            long lastMod;

            // Special handling for file:// protocol, as getLastModified() often reports
            // incorrect results (-1)
            if (source.getProtocol().equals("file")) {
                // Coerce the file URL to a File
                String path = source.getPath().replace('/', File.separatorChar).replace('|', ':');
                File file = new File(path);
                lastMod = file.lastModified();
            } else {
                URLConnection conn = source.openConnection();
                lastMod = conn.getLastModified();
                conn.getInputStream().close();
            }
            return lastMod > getTimeStamp(cls);
        } catch (IOException e) {
            // if the stream can't be opened, let's keep the old reference
            return false;
        }
    }

    /**
     * get the time stamp of a class
     * NOTE: copied from GroovyClassLoader
     */
    private static long getTimeStamp(ClassNode cls) {
        if (!(cls instanceof DecompiledClassNode)) {
            return Verifier.getTimestamp(cls.getTypeClass());
        }

        return ((DecompiledClassNode) cls).getCompilationTimeStamp();
    }

}
