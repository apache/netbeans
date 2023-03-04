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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries.SendEmailCodeGenerator;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * Action that can always be invoked and work procedurally.
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public class AddMethodActions implements CodeGenerator {

    public abstract static class AbstractFactory implements CodeGenerator.Factory {

        private AbstractAddMethodStrategy strategy;

        private AbstractFactory(AbstractAddMethodStrategy strategy) {
            this.strategy = strategy;
        }

        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> ret = new ArrayList<CodeGenerator>();
            JTextComponent component = context.lookup(JTextComponent.class);
            CompilationController controller = context.lookup(CompilationController.class);
            TreePath path = context.lookup(TreePath.class);
            path = path != null ? SendEmailCodeGenerator.getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path) : null;
            if (component == null || controller == null || path == null)
                return ret;
            try {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            } catch (IOException ioe) {
                return ret;
            }
            
            TypeElement typeElement = (TypeElement) controller.getTrees().getElement(path);
            if (typeElement != null && typeElement.getKind().isClass()) {
                if (!isEnable(strategy, controller.getFileObject(), typeElement)) {
                    return ret;
                }
                ret.add(new AddMethodActions(strategy, controller.getFileObject(), typeElement));
            }
            
            return ret;
        }
    }

    public static class AddBusinessMethodCodeGenerator extends AbstractFactory {
        public AddBusinessMethodCodeGenerator() {
            super(new AddBusinessMethodStrategy());
        }
    }

    public static class AddCreateMethodCodeGenerator extends AbstractFactory {
        public AddCreateMethodCodeGenerator() {
            super(new AddCreateMethodStrategy());
        }
    }

    public static class AddFinderMethodCodeGenerator extends AbstractFactory {
        public AddFinderMethodCodeGenerator() {
            super(new AddFinderMethodStrategy());
        }
    }

    public static class AddHomeMethodCodeGenerator extends AbstractFactory {
        public AddHomeMethodCodeGenerator() {
            super(new AddHomeMethodStrategy());
        }
    }

    public static class AddSelectMethodCodeGenerator extends AbstractFactory {
        public AddSelectMethodCodeGenerator() {

            super(new AddSelectMethodStrategy());
        }
    }


    /** Action context. */
    private FileObject fileObject;
    private TypeElement beanClass;
    private final AbstractAddMethodStrategy strategy;

    public AddMethodActions(AbstractAddMethodStrategy strategy, FileObject fileObject, TypeElement beanClass) {
        this.fileObject = fileObject;
        this.beanClass = beanClass;
        this.strategy = strategy;
    }

    public String getDisplayName(){
        return strategy.getTitle();
    }

    public static boolean isEnable(AbstractAddMethodStrategy strategy, FileObject fileObject, TypeElement elementHandle) {
        return strategy.supportsEjb(fileObject, elementHandle.getQualifiedName().toString());
    }

    public void invoke() {
        if (strategy.supportsEjb(fileObject, beanClass.getQualifiedName().toString())) {
            try {
                strategy.addMethod(fileObject, beanClass.getQualifiedName().toString());
            } catch (IOException ex) {
                Logger.getLogger(AbstractAddMethodAction.class.getName()).log(Level.WARNING, null, ex);
            }
        }
    }

}
