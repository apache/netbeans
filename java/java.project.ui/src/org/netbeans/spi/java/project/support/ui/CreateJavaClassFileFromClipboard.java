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
package org.netbeans.spi.java.project.support.ui;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.Trees;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author aksinsin
 */
public class CreateJavaClassFileFromClipboard extends PasteType {

    private static final String PUBLIC_MODIFIER = "public"; //NOI18N

    private final DataFolder context;
    private final Transferable t;

    public CreateJavaClassFileFromClipboard(DataFolder context, Transferable t) {
        this.context = context;
        this.t = t;
    }

    @NbBundle.Messages({
        "ERR_NotValidClass=Code is not a valid class",
        "ERR_ClassAlreadyPresent=Class {0} already present",
        "ERR_UnableToCreateFile=Unable to create file {0}",})
    @Override
    public Transferable paste() throws IOException {
        try {
            Clipboard c = Lookup.getDefault().lookup(Clipboard.class);
            if (c == null) {
                c = Toolkit.getDefaultToolkit().getSystemClipboard();
            }
            if (!c.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                return t;
            }
            String copiedData = (String) c.getData(DataFlavor.stringFlavor);
            CreateJavaClassFileFromClipboard.ClassContent classContent = extractPackageAndClassName(copiedData);
            if (classContent == null) {
                NotifyDescriptor.Message notifyMsg = new DialogDescriptor.Message(
                        Bundle.ERR_NotValidClass(),
                        DialogDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(notifyMsg);
                return t;
            }
            Set<FileObject> files = this.context.files();
            if (files.size() != 1) {
                return t;
            }
            String path = files.iterator().next().getPath();
            File fileName = new File(path + File.separator + classContent.getClassName() + ".java"); //NOI18N
            if (fileName.exists()) {
                NotifyDescriptor.Message notifyMsg = new DialogDescriptor.Message(
                        Bundle.ERR_ClassAlreadyPresent(classContent.getClassName() + ".java"),
                        DialogDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(notifyMsg);
                return t;
            }
            if (!fileName.createNewFile()) {
                NotifyDescriptor.Message notifyMsg = new DialogDescriptor.Message(
                        Bundle.ERR_UnableToCreateFile(classContent.getClassName() + ".java"),
                        DialogDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(notifyMsg);
                return t;
            }

            if (classContent.getPackageStartOffset() != -1 && classContent.getPackageEndOffset() != -1) {
                copiedData = removePackage(copiedData, classContent.getPackageStartOffset(), classContent.getPackageEndOffset());
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
                String packageLocation = getPackageNameFromFile(fileName);
                if (packageLocation != null && !packageLocation.isEmpty()) {
                    copiedData = "package " + packageLocation + ";\n" + copiedData;// NOI18N
                }
                bw.write(copiedData);
            }

        } catch (UnsupportedFlavorException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return t;
    }

    private ClassContent extractPackageAndClassName(String copiedData) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        String publicFirstClassName = null;
        String nonPublicFirstClassName = null;
        long packageStartOffset = -1;
        long packageEndOffset = -1;
        int counter = 0;
        JavaCompiler.CompilationTask task = compiler.getTask(null, null, null, null, null, Arrays.asList(new MyFileObject(copiedData)));
        SourcePositions sourcePositions = Trees.instance(task).getSourcePositions();
        parse:
        try {
            for (CompilationUnitTree compilationUnitTree : ((JavacTask) task).parse()) {
                packageStartOffset = sourcePositions.getStartPosition(compilationUnitTree, compilationUnitTree.getPackage());
                packageEndOffset = sourcePositions.getEndPosition(compilationUnitTree, compilationUnitTree.getPackage());
                for (Tree tree : compilationUnitTree.getTypeDecls()) {
                    if (tree instanceof ClassTree) {
                        final ClassTree classTree = (ClassTree) tree;
                        if (classTree.toString().trim().startsWith(PUBLIC_MODIFIER)) {
                            publicFirstClassName = classTree.getSimpleName().toString();
                            break parse;
                        }
                        if (counter == 0) {
                            nonPublicFirstClassName = classTree.getSimpleName().toString();
                            counter++;
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }

        if (publicFirstClassName != null && !publicFirstClassName.equals("<error>")) { //NOI18N
            return new ClassContent(publicFirstClassName, packageStartOffset, packageEndOffset);
        } else if (nonPublicFirstClassName != null && !nonPublicFirstClassName.equals("<error>")) { //NOI18N
            return new ClassContent(nonPublicFirstClassName, packageStartOffset, packageEndOffset);
        } else {
            return null;
        }

    }

    private String removePackage(String copiedCode, long packageStartOffset, long packageEndOffset) {
        return new StringBuilder(copiedCode).delete((int) packageStartOffset, (int) packageEndOffset).toString();
    }

    private String getPackageNameFromFile(File fileName) {
        String packageLocation = null;
        try {
            FileObject data = FileUtil.createData(fileName);
            ClassPath classPath = ClassPath.getClassPath(data, ClassPath.SOURCE);
            if (classPath != null) {
                packageLocation = classPath.getResourceName(data.getParent(), '.', false);
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }

        return packageLocation;
    }

    private static class MyFileObject extends SimpleJavaFileObject {

        private String text;

        public MyFileObject(String text) {
            super(URI.create("myfo:/Test.java"), JavaFileObject.Kind.SOURCE); //NOI18N
            this.text = text;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return text;
        }
    }

    private class ClassContent {

        private final String className;
        private final long packageStartOffset;
        private final long packageEndOffset;

        public ClassContent(String className, long packageStartOffset, long packageEndOffset) {
            this.className = className;
            this.packageStartOffset = packageStartOffset;
            this.packageEndOffset = packageEndOffset;
        }

        public String getClassName() {
            return className;
        }

        public long getPackageStartOffset() {
            return packageStartOffset;
        }

        public long getPackageEndOffset() {
            return packageEndOffset;
        }
    }
}
