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

package org.netbeans.modules.gradle.java.execute;

import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import org.netbeans.modules.gradle.spi.actions.AfterBuildActionHook;
import org.netbeans.modules.gradle.spi.actions.BeforeBuildActionHook;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.util.Lookup;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

import static org.netbeans.api.java.project.JavaProjectConstants.*;
/**
 *
 * @author Laszlo Kishalmi
 */
public class DebugFixHooks implements BeforeBuildActionHook, AfterBuildActionHook {

    final Project project;

    public DebugFixHooks(Project project) {
        this.project = project;
    }

    @Override
    public Lookup beforeAction(String action, Lookup context, PrintWriter out) {
        if (COMMAND_DEBUG_FIX.equals(action)) {
            Set<GradleJavaSourceSet> sourceSets = affectedSourceSets(fetchFilesToReload(context));
            StringBuilder classesTasks = new StringBuilder();
            String delim = ""; //NOI18N
            for (GradleJavaSourceSet sourceSet : sourceSets) {
                classesTasks.append(delim).append(sourceSet.getClassesTaskName());
                delim = " "; //NOI18N
            }
//            Lookup params = Lookups.singleton(RunUtils.simpleActionParameterProvider(classesTasks));
//            return new ProxyLookup(params, context);
        }
        return context;
    }

    @Override
    public void afterAction(String action, Lookup context, int result, PrintWriter out) {
        if (COMMAND_DEBUG_FIX.equals(action) && (result == 0)) {
            GradleJavaProject jp = GradleJavaProject.get(project);
            out.println();
            for (FileObject fo : fetchFilesToReload(context)) {
                GradleJavaSourceSet ss = jp.containingSourceSet(FileUtil.toFile(fo));
                String pkg = null;
                for (File dir : ss.getAvailableDirs()) {
                    FileObject foDir = FileUtil.toFileObject(dir);
                    if (FileUtil.isParentOf(foDir, fo)) {
                        pkg = FileUtil.getRelativePath(foDir, fo.getParent()).replace('/', '.');
                        break;
                    }
                }
                String cname = pkg + '.' + fo.getName();
                applyChanges(out, ss, cname);
            }

        }
    }

    private void applyChanges(PrintWriter logger, GradleJavaSourceSet sourceSet, String classname) {
        // check debugger state
        DebuggerEngine debuggerEngine = DebuggerManager.getDebuggerManager().
                getCurrentEngine();
        if (debuggerEngine == null) {
            logger.println("NetBeans: No debugging sessions was found.");
            return;
        }
        JPDADebugger debugger = debuggerEngine.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            logger.println("NetBeans: Current debugger is not JPDA one.");
            return;
        }
        if (!debugger.canFixClasses()) {
            logger.println("NetBeans: The debugger does not support Fix action.");
            return;
        }
        if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED) {
            logger.println("NetBeans: The debugger is not running");
            return;
        }

        Map<String, byte[]> map = new HashMap<>();
        EditorContext editorContext = DebuggerManager.
                getDebuggerManager().lookupFirst(null, EditorContext.class);

        String clazz = classname.replace('.', '/') + ".class"; //NOI18N
        FileObject fo2 = FileUtil.toFileObject(sourceSet.findResource(clazz));

        if (fo2 != null) {
            try {
                String basename = fo2.getName();
                for (FileObject classfile : fo2.getParent().getChildren()) {
                    String basename2 = classfile.getName();
                    if (/*#220338*/!"class".equals(classfile.getExt()) || (!basename2.equals(basename) && !basename2.startsWith(basename + '$'))) {
                        continue;
                    }
                    String url = classToSourceURL(classfile, logger);
                    if (url != null) {
                        editorContext.updateTimeStamp(debugger, url);
                    }
                    map.put(classname + basename2.substring(basename.length()), classfile.asBytes());
                }
            } catch (IOException ex) {
                //TODO: Handle IO Exception
            }
        }

        if (map.isEmpty()) {
            logger.println("NetBeans: No class to reload");
            return;
        } else {
            logger.println("NetBeans: classes to reload: ");
            for (String cls : map.keySet()) {
                logger.println("    " + cls);
            }
        }
        String error = null;
        try {
            debugger.fixClasses(map);
        } catch (UnsupportedOperationException uoex) {
            error = "The virtual machine does not support this operation: " + uoex.getLocalizedMessage();
        } catch (NoClassDefFoundError ncdfex) {
            error = "The bytes don't correspond to the class type (the names don't match): " + ncdfex.getLocalizedMessage();
        } catch (VerifyError ver) {
            error = "A \"verifier\" detects that a class, though well formed, contains an internal inconsistency or security problem: " + ver.getLocalizedMessage();
        } catch (UnsupportedClassVersionError ucver) {
            error = "The major and minor version numbers in bytes are not supported by the VM. " + ucver.getLocalizedMessage();
        } catch (ClassFormatError cfer) {
            error = "The bytes do not represent a valid class. " + cfer.getLocalizedMessage();
        } catch (ClassCircularityError ccer) {
            error = "A circularity has been detected while initializing a class: " + ccer.getLocalizedMessage();
        }
        if (error != null) {
            logger.println("NetBeans:" + error);
        } else {
            logger.println("Code Updated.");
        }
    }

    private static String classToSourceURL(FileObject fo, PrintWriter logger) {
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.EXECUTE);
        if (cp == null) {
            return null;
        }
        FileObject root = cp.findOwnerRoot(fo);
        String resourceName = cp.getResourceName(fo, '/', false);
        if (resourceName == null || root == null) {
            logger.println("Can not find classpath resource for " + fo + ", skipping...");
            return null;
        }
        int i = resourceName.indexOf('$');
        if (i > 0) {
            resourceName = resourceName.substring(0, i);
        }
        FileObject[] sRoots = SourceForBinaryQuery.findSourceRoots(root.toURL()).getRoots();
        ClassPath sourcePath = ClassPathSupport.createClassPath(sRoots);
        FileObject rfo = sourcePath.findResource(resourceName + ".java");
        if (rfo == null) {
            return null;
        }
        return rfo.toURL().toExternalForm();
    }

    private Set<GradleJavaSourceSet> affectedSourceSets(Set<FileObject> files) {
        GradleJavaProject jp = GradleJavaProject.get(project);
        Set<GradleJavaSourceSet> ret = new HashSet<>();
        if (jp != null) {
            for (FileObject fo : files) {
                GradleJavaSourceSet ss = jp.containingSourceSet(FileUtil.toFile(fo));
                if (ss != null) {
                    ret.add(ss);
                }
            }
        }
        return ret;
    }

    private Set<FileObject> fetchFilesToReload(Lookup context) {
        Set<FileObject> ret = new LinkedHashSet<>();
        ret.addAll(context.lookupAll(FileObject.class));
        for (DataObject data : context.lookupAll(DataObject.class)) {
            ret.add(data.getPrimaryFile());
        }
        return ret;
    }
}
