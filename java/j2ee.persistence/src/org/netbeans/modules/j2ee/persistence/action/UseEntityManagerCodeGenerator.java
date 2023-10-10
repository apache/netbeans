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

package org.netbeans.modules.j2ee.persistence.action;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;

import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;

/**
 * Generate persist method
 */
public class UseEntityManagerCodeGenerator implements CodeGenerator {

    private FileObject srcFile;

    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> ret = new ArrayList<>();
            JTextComponent component = context.lookup(JTextComponent.class);
            CompilationController controller = context.lookup(CompilationController.class);
            TreePath path = context.lookup(TreePath.class);
            path = path != null ? getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path) : null;
            if (component == null || controller == null || path == null) {
                return ret;
            }
            try {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                Element elem = controller.getTrees().getElement(path);
                if (elem != null) {
                    UseEntityManagerCodeGenerator gen = createUseEntityManagerCodeGenerator(component, controller, elem);
                    if (gen != null) {
                        ret.add(gen);
                    }
                }
            } catch (IOException ioe) {
            }
            return ret;
        }

    }

    static UseEntityManagerCodeGenerator createUseEntityManagerCodeGenerator(JTextComponent component, CompilationController cc, Element el) throws IOException {
        if (el.getKind() != ElementKind.CLASS) {
            return null;
        }
        TypeElement typeElement = (TypeElement)el;
        if (!isEnable(cc.getFileObject(), typeElement)) {
            return null;
        }
        return new UseEntityManagerCodeGenerator(cc.getFileObject());
    }

    public UseEntityManagerCodeGenerator(FileObject srcFile) {
        this.srcFile = srcFile;
    }

    @Override
    public void invoke() {
        EntityManagerGenerator emGenerator = new EntityManagerGenerator(srcFile, srcFile.getName());
        GenerationOptions options = new GenerationOptions();
        options.setParameterName("object");
        options.setParameterType("Object");
        options.setMethodName("persist");
        options.setOperation(GenerationOptions.Operation.PERSIST);
        options.setReturnType("void");
        try {
            emGenerator.generate(options);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(UseEntityManagerCodeGenerator.class, "CTL_UseEntityManagerAction");
    }
    
    private static boolean isEnable(FileObject fileObject, TypeElement typeElement) {
        Project project = FileOwnerQuery.getOwner(fileObject);
        if (project == null) {
            return false;
        }
        if(ElementKind.INTERFACE == typeElement.getKind()) {
            return false;
        }
        DataObject dObj = null;
        try {
            dObj = DataObject.find(fileObject);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        if(dObj == null) {
            return false;
        }

        // Enable it only if the EntityManager is in the project classpath
        // This check was motivated by issue 139333 - The Use Entity Manager action
        // breaks from left to right if the javax.persistence.EntityManager class is missing
        FileObject target = dObj.getCookie(DataObject.class).getPrimaryFile();
        ClassPath cp = ClassPath.getClassPath(target, ClassPath.COMPILE);
        if(cp == null) {
            return false;
        }

        if(PersistenceScope.getPersistenceScope(target) == null){
            return false;
        }

        FileObject entityMgrRes = cp.findResource("javax/persistence/EntityManager.class"); // NOI18N
        return entityMgrRes != null;
    }
    
    public static TreePath getPathElementOfKind(Tree.Kind kind, TreePath path) {
        return getPathElementOfKind(EnumSet.of(kind), path);
    }

    public static TreePath getPathElementOfKind(Set<Tree.Kind> kinds, TreePath path) {
        while (path != null) {
            if (kinds.contains(path.getLeaf().getKind())) {
                return path;
            }
            path = path.getParentPath();
        }
        return null;
    }

}
