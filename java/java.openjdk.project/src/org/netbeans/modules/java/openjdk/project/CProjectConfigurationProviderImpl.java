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
package org.netbeans.modules.java.openjdk.project;

import com.google.gson.Gson;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.java.openjdk.project.JDKProject.Root;
import org.netbeans.modules.java.openjdk.project.JDKProject.RootKind;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author lahvac
 */
public class CProjectConfigurationProviderImpl implements InvocationHandler {

    public static Object create(JDKProject project) {
        try {
            ClassLoader generalCL = Lookup.getDefault().lookup(ClassLoader.class);
            ClassLoader thisCL = CProjectConfigurationProviderImpl.class.getClassLoader();

            if (generalCL == null) generalCL = thisCL;

            Class<?> provider = generalCL.loadClass("org.netbeans.modules.cpplite.editor.spi.CProjectConfigurationProvider");
            Class<?> projectConfiguration = generalCL.loadClass("org.netbeans.modules.cpplite.editor.spi.CProjectConfigurationProvider$ProjectConfiguration");
            Constructor<?> newProjectConfiguration = projectConfiguration.getDeclaredConstructor(String.class, boolean.class);

            return Proxy.newProxyInstance(generalCL, new Class[] {provider}, new CProjectConfigurationProviderImpl(project, newProjectConfiguration));
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
            //ignore...
        }
        return "none";
    }

    private final ChangeSupport cs = new ChangeSupport(this);
    private final JDKProject project;
    private final Constructor<?> newProjectConfiguration;

    public CProjectConfigurationProviderImpl(JDKProject project, Constructor<?> newProjectConfiguration) {
        this.project = project;
        this.newProjectConfiguration = newProjectConfiguration;
        ConfigurationImpl.getProvider(project.moduleRepository.getJDKRoot()).addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() == null ||
                    ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE.equals(evt.getPropertyName())) {
                    cs.fireChange();
                }
            }
        });
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "getProjectConfiguration":
                return newProjectConfiguration.newInstance(configuration(project), true);
            case "addChangeListener":
                cs.addChangeListener((ChangeListener) args[0]);
                return null;
            case "removeChangeListener":
                cs.removeChangeListener((ChangeListener) args[0]);
                return null;
            default:
                throw new UnsupportedOperationException("Not supported yet: " + method.getName());
        }
    }

    private String configuration(JDKProject project) {
        Set<String> sourceFiles = new HashSet<>();

        for (Root root : project.getRoots()) {
            if (root.kind != RootKind.NATIVE_SOURCES)
                continue;

            URL location = root.getLocation();
            FileObject rootFO = URLMapper.findFileObject(location);

            if (rootFO == null)
                continue;

            Enumeration<? extends FileObject> children = rootFO.getChildren(true);

            while (children.hasMoreElements()) {
                FileObject child = children.nextElement();

                if (child.isData()) {
                    if (!child.getExt().equalsIgnoreCase("h") &&
                        !child.getExt().equalsIgnoreCase("hh")) {
                        //TODO: should include header files???
                        sourceFiles.add(FileUtil.toFile(child).getAbsolutePath());
                    }
                }
            }
        }

        if (sourceFiles.isEmpty()) {
            return null;
        }

        ConfigurationImpl activeConfig = ConfigurationImpl.getProvider(project.moduleRepository.getJDKRoot()).getActiveConfiguration();

        if (activeConfig != null) {
            File buildDir = activeConfig.getLocation();
            File spec = new File(buildDir, "spec.gmk");
            File compileCommands = new File(buildDir, "compile_commands.json");

            if (!compileCommands.canRead() ||
                compileCommands.lastModified() < spec.lastModified()) {
                try {
                    new ProcessBuilder("make", "compile_commands").inheritIO().start().waitFor();
                } catch (IOException ex) {
                    LOG.log(Level.FINE, null, ex);
                    return null;
                } catch (InterruptedException ex) {
                    LOG.log(Level.FINE, null, ex);
                }
            }

            if (!compileCommands.canRead()) {
                return null;
            }

            List<Map<String, Object>> commands;

            try (Reader r = Files.newBufferedReader(compileCommands.toPath())) {
                commands = new Gson().fromJson(r, ArrayList.class);
            } catch (IOException ex) {
                //should not happen normally?
                Exceptions.printStackTrace(ex);
                return null;
            }

            for (Iterator<Map<String, Object>> it = commands.iterator(); it.hasNext();) {
                Map<String, Object> fileDescription = it.next();

                if (!sourceFiles.contains(fileDescription.get("file"))) {
                    it.remove();
                }

            }

            if (commands.isEmpty()) {
                return null;
            }

            return new Gson().toJson(commands);
        }

        return null;
    }

    private static final Logger LOG = Logger.getLogger(CProjectConfigurationProviderImpl.class.getName());

}
