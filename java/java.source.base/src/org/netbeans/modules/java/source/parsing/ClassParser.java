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

package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Class file parser.
 * Threading: Not thread safe, concurrency handled by caller (TaskProcessor).
 * @author Tomas Zezula
 */
//@NotThreadSafe
public class ClassParser extends Parser {
    
    public static final String MIME_TYPE = "application/x-class-file";  //NOI18N
    private static final ClassPath EMPTY_PATH = ClassPathSupport.createClassPath(new URL[0]);
    private static final Logger LOGGER = Logger.getLogger(Parser.class.getName());
    
    private final ChangeSupport changeSupport;
    private final ClasspathInfoListener cpInfoListener;
    private CompilationInfoImpl ciImpl;
    private Snapshot lastSnapshot;
    private ClasspathInfo info;
    private ChangeListener wl;

    ClassParser() {
        this.changeSupport = new ChangeSupport(this);
        this.cpInfoListener = new ClasspathInfoListener(this.changeSupport, null);
    }

    @Override
    public void parse(final Snapshot snapshot, Task task, final SourceModificationEvent event) throws ParseException {
        assert snapshot != null;
        lastSnapshot = snapshot;
        final Source source = snapshot.getSource();
        assert source != null;
        final FileObject file = source.getFileObject();
        assert file != null;
        if (info == null) {
            if ((task instanceof ClasspathInfo.Provider)) {
                info =((ClasspathInfo.Provider)task).getClasspathInfo();
            }
            if (info == null) {
                ClassPath bootPath = ClassPath.getClassPath(file, ClassPath.BOOT);
                if (bootPath == null) {
                    //javac requires at least java.lang
                    bootPath = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
                }
                ClassPath compilePath = ClassPath.getClassPath(file, ClassPath.COMPILE);
                if (compilePath == null) {
                    compilePath = EMPTY_PATH;
                }
                ClassPath executePath = ClassPath.getClassPath(file, ClassPath.EXECUTE);
                if (executePath == null) {
                    executePath = EMPTY_PATH;
                }
                ClassPath srcPath = ClassPath.getClassPath(file, ClassPath.SOURCE);
                if (srcPath == null) {
                    srcPath = EMPTY_PATH;
                }
                info = ClasspathInfo.create(
                    bootPath,
                    ClassPathSupport.createProxyClassPath(compilePath,executePath),
                    srcPath);
            }
            assert info != null;
            info.addChangeListener(wl=WeakListeners.change(this.cpInfoListener, info));
        }
        final ClassPath bootPath = info.getClassPath(ClasspathInfo.PathKind.BOOT);
        final ClassPath compilePath = info.getClassPath(ClasspathInfo.PathKind.COMPILE);
        final ClassPath srcPath = info.getClassPath(ClasspathInfo.PathKind.SOURCE);
        final FileObject root = ClassPathSupport.createProxyClassPath(
                ClassPathSupport.createClassPath(CachingArchiveProvider.getDefault().ctSymRootsFor(bootPath)),
                bootPath,
                compilePath,
                srcPath).findOwnerRoot(file);
        if (root == null) {
            throw new ParseException(
                String.format("The file %s is not owned by provided classpaths, boot: %s, compile: %s, src: %s",    //NOI18N
                    FileUtil.getFileDisplayName(file),
                    bootPath.toString(),
                    compilePath.toString(),
                    srcPath.toString()));
        }
        try {
            this.ciImpl = new CompilationInfoImpl(info,file,root);
        } catch (final IOException ioe) {
            throw new ParseException ("ClassParser failure", ioe);            //NOI18N
        }
    }

    @Override
    public Result getResult(Task task) throws ParseException {
        assert ciImpl != null;
        final boolean isParserResultTask = task instanceof ParserResultTask;
        final boolean isJavaParserResultTask = task instanceof JavaParserResultTask;
        final boolean isUserTask = task instanceof UserTask;
        JavacParserResult result = null;
        if (isParserResultTask) {
            final JavaSource.Phase currentPhase = ciImpl.getPhase();
            JavaSource.Phase requiredPhase;
            if (isJavaParserResultTask) {
                requiredPhase = ((JavaParserResultTask)task).getPhase();
            } else {
                requiredPhase = JavaSource.Phase.RESOLVED;
            }
            if (task instanceof ClasspathInfo.Provider) {
                final ClasspathInfo taskProvidedCpInfo = ((ClasspathInfo.Provider)task).getClasspathInfo();
                if (taskProvidedCpInfo != null && !taskProvidedCpInfo.equals(info)) {
                    assert info != null;
                    assert wl != null;
                    info.removeChangeListener(wl);
                    info = null;
                    parse(lastSnapshot, task, null);
                }
            }
            if (currentPhase.compareTo(requiredPhase)<0) {
                ciImpl.setPhase(requiredPhase);
                if (currentPhase == JavaSource.Phase.MODIFIED) {
                    ciImpl.getJavacTask().analyze(); // Ensure proper javac initialization
                }
            }
            result = new JavacParserResult(JavaSourceAccessor.getINSTANCE().createCompilationInfo(ciImpl));
        }
        else if (isUserTask) {
            result = new JavacParserResult(JavaSourceAccessor.getINSTANCE().createCompilationController(ciImpl));
        }
        else {
            LOGGER.warning("Ignoring unknown task: " + task);                   //NOI18N
        }
        return result;
    }

    @Override
    public void cancel() {
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        Parameters.notNull("changeListener", changeListener);   //NOI18N
        this.changeSupport.addChangeListener(changeListener);
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        Parameters.notNull("changeListener", changeListener);   //NOI18N
        this.changeSupport.removeChangeListener(changeListener);
    }

}
