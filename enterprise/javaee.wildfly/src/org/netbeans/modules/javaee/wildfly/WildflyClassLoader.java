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
package org.netbeans.modules.javaee.wildfly;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 *
 * @author Petr Hejl
 */
public class WildflyClassLoader extends URLClassLoader {

    private static final Logger LOGGER = Logger.getLogger(WildflyDeploymentFactory.class.getName());

    /**
     * Patching the xnio code to avoid bug #249135
     *
     * @see https://netbeans.org/bugzilla/show_bug.cgi?id=249135
     */
    private final boolean patchXnio;

    public WildflyClassLoader(URL[] urls, ClassLoader parent, boolean patchXnio) throws MalformedURLException, RuntimeException {
        super(urls, parent);
        this.patchXnio = patchXnio;
    }

    public static WildflyClassLoader createWildFlyClassLoader(String serverRoot) {
        try {
            List<URL> urlList = new ArrayList<>(2);

            Path serverRootPath = Paths.get(serverRoot);
            Path jbossCliClientJar = Paths.get(serverRootPath.toString(), "bin", "client", "jboss-cli-client.jar");
            urlList.add(jbossCliClientJar.toUri().toURL());

            Path modulePath = serverRootPath.resolve("modules");
            findJarAddUrl(urlList, modulePath, "glob:**/*/controller/main/wildfly-controller-*.jar");

            WildflyPluginUtils.Version version = WildflyPluginUtils.getServerVersion(new File(serverRoot));
            boolean shouldPatchXnio = WildflyPluginUtils.WILDFLY_8_0_0.compareToIgnoreUpdate(version) <= 0;

            return new WildflyClassLoader(
                    urlList.toArray(new URL[]{}),
                    WildflyDeploymentFactory.class.getClassLoader(),
                    shouldPatchXnio
            );
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, null, e);
        }
        return null;
    }

    private static void findJarAddUrl(List<URL> urlList, Path folder, String pathMatcherRaw) {
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(pathMatcherRaw);

        try (Stream<Path> walk = Files.walk(folder)) {
            Optional<Path> firstJarMatching = walk
                    .filter(pathMatcher::matches)
                    .findFirst();

            if (firstJarMatching.isPresent()) {
                LOGGER.log(Level.INFO, "Adding {0} to the classpath", firstJarMatching.get().toString());
                urlList.add(firstJarMatching.get().toUri().toURL());
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    @Override
    protected PermissionCollection getPermissions(CodeSource codeSource) {
        Permissions p = new Permissions();
        p.add(new AllPermission());
        return p;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        // get rid of annoying warnings
        if (name.contains("jndi.properties")) { // NOI18N
            return Collections.enumeration(Collections.<URL>emptyList());
        }

        return super.getResources(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // see issue #249135
        if (patchXnio && "org.xnio.nio.WorkerThread".equals(name)) { // NOI18N
            try {
                LOGGER.log(Level.INFO, "Patching the issue #249135");
                String path = name.replace('.', '/').concat(".class"); // NOI18N
                try (InputStream is = super.getResourceAsStream(path)) {
                    ClassReader cr = new ClassReader(is);
                    final ClassLoader ld = this;
                    ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES) {

                        @Override
                        protected String getCommonSuperClass(String string, String string1) {
                            if ("org/xnio/nio/NioHandle".equals(string) // NOI18N
                                    || "org/xnio/nio/NioHandle".equals(string1)) { // NOI18N
                                return "java/lang/Object"; // NOI18N
                            }
                            return super.getCommonSuperClass(string, string1);
                        }

                        @Override
                        protected ClassLoader getClassLoader() {
                            return ld;
                        }

                    };
                    ClassNode node = new ClassNode(Opcodes.ASM9);
                    cr.accept(node, 0);

                    for (MethodNode m : (Collection<MethodNode>) node.methods) {
                        if ("execute".equals(m.name) // NOI18N
                                && "(Ljava/lang/Runnable;)V".equals(m.desc)) { // NOI18N
                            InsnList list = m.instructions;
                            for (ListIterator it = list.iterator(); it.hasNext();) {
                                AbstractInsnNode n = (AbstractInsnNode) it.next();
                                if (n instanceof MethodInsnNode) {
                                    MethodInsnNode mn = (MethodInsnNode) n;
                                    if ("org/xnio/nio/Log".equals(mn.owner) // NOI18N
                                            && "threadExiting".equals(mn.name) // NOI18N
                                            && "()Ljava/util/concurrent/RejectedExecutionException;".equals(mn.desc)) { // NOI18N
                                        if (it.hasNext()) {
                                            AbstractInsnNode possibleThrow = (AbstractInsnNode) it.next();
                                            if (possibleThrow.getOpcode() == Opcodes.ATHROW) {
                                                it.set(new InsnNode(Opcodes.POP));
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }

                    node.accept(cw);
                    byte[] newBytecode = cw.toByteArray();
                    return super.defineClass(name, newBytecode, 0, newBytecode.length);
                }
            } catch (Exception ex) {
                // just fallback to original behavior
                LOGGER.log(Level.INFO, null, ex);
            }
        }
        return super.findClass(name);
    }
}
