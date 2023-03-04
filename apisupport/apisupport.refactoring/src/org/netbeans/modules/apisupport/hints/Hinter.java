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

package org.netbeans.modules.apisupport.hints;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.CheckReturnValue;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.apisupport.project.api.LayerHandle;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import static org.netbeans.modules.apisupport.hints.Bundle.*;
import org.openide.util.NbBundle.Messages;

/**
 * One category of hint.
 * Register implementation into global lookup.
 */
public interface Hinter {

    /**
     * Check for hints.
     * @param ctx context of a single layer entry
     * @throws Exception in case of problem
     */
    void process(Context ctx) throws Exception;

    /**
     * Context supplied to a {@link Hinter}.
     */
    class Context {

        private static final Logger LOG = Logger.getLogger(Hinter.class.getName());

        private final Document doc;
        private final LayerHandle layer;
        private final FileObject file;
        private final RunnableFuture<Map<String,Integer>> lines;
        private final List<? super ErrorDescription> errors;

        Context(Document doc, LayerHandle layer, FileObject file, RunnableFuture<Map<String,Integer>> lines, List<? super ErrorDescription> errors) {
            this.doc = doc;
            this.layer = layer;
            this.file = file;
            this.lines = lines;
            this.errors = errors;
        }

        /**
         * Gets the layer entry you may offer hints for.
         * File attribute names like {@code literal:instanceCreate} may return values like {@code new:pkg.Clazz} or {@code method:pkg.Clazz.factory}.
         * @return a file (or folder) in the project's layer
         */
        public FileObject file() {
            return file;
        }

        /**
         * @return standard description to pass to {@link #addHint}
         */
        @Messages("Hinter.description=Use of layer entry where annotation is available")
        public String standardAnnotationDescription() {
            return Hinter_description();
        }

        /**
         * @return standard fix description to pass to {@link #addHint}
         */
        @Messages("Hinter.fix.description=Convert registration to Java annotation")
        public String standardAnnotationFixDescription() {
            return Hinter_fix_description();
        }

        /**
         * Add a hint.
         * @param severity whether to treat as a warning, etc.
         * @param description description of hint
         * @param fixes any fixes to offer
         * @see #addStandardAnnotationHint
         */
        public void addHint(Severity severity, String description, Fix... fixes) {
            Integer line = null;
            try {
                lines.run();
                line = lines.get().get(file.getPath());
            } catch (Exception x) {
                LOG.log(Level.INFO, null, x);
            }
            if (line != null) {
                errors.add(ErrorDescriptionFactory.createErrorDescription(severity, description, Arrays.asList(fixes), doc, line));
            } else {
                LOG.log(Level.WARNING, "no line found for {0}", file);
            }
        }

        /**
         * Add an annotation-oriented warning hint following the standard pattern.
         * @param fix what to do for a fix (see e.g. {@link #findAndModifyDeclaration}); no change info
         * @see #addHint
         * @see #standardAnnotationDescription
         * @see #standardAnnotationFixDescription
         */
        public void addStandardAnnotationHint(final Callable<Void> fix) {
            addHint(Severity.WARNING, standardAnnotationDescription(), new Fix() {
                public @Override String getText() {
                    return standardAnnotationFixDescription();
                }
                public @Override ChangeInfo implement() throws Exception {
                    fix.call();
                    return null;
                }
            });
        }

        /**
         * Checks whether a given API class can be accessed from the current classpath.
         * This can be used to control whether or not a given hint or fix is enabled.
         * @param api binary name of an API class
         * @return true if it is visible
         */
        public boolean canAccess(String api) {
            ClassPath cp = ClassPath.getClassPath(layer.getLayerFile(), ClassPath.COMPILE);
            if (cp == null) {
                return false;
            }
            return cp.findResource(api.replace('.', '/') + ".class") != null;
        }

        /**
         * Locate the declaration of an object declared as a newvalue or methodvalue attribute.
         * @param instanceAttribute the result of {@link FileObject#getAttribute} on a {@code literal:*} key
         * @return the source file containing the corresponding declaration, or null if not found
         */
        private @CheckForNull FileObject findDeclaringSource(@NullAllowed Object instanceAttribute) {
            if (!(instanceAttribute instanceof String)) {
                return null;
            }
            // XXX this will not find classes in a sister module; maybe look in ClassPath.EXECUTE, then use SFBQ to find it?
            ClassPath src = ClassPath.getClassPath(layer.getLayerFile(), ClassPath.SOURCE); // should work even for Maven src/main/resources/.../layer.xml
            if (src == null) {
                return null;
            }
            String attr = (String) instanceAttribute;
            if (attr.startsWith("new:")) {
                return src.findResource(attr.substring(4).replaceFirst("[$][^.]+$", "").replace('.', '/') + ".java");
            } else if (attr.startsWith("method:")) {
                return src.findResource(attr.substring(7, attr.lastIndexOf('.')).replaceFirst("[$][^.]+$", "").replace('.', '/') + ".java");
            } else {
                return null;
            }
        }

        /**
         * Locate the declaration of an object declared as a newvalue or methodvalue attribute.
         * @param wc context of a modification task
         * @param instanceAttribute the result of {@link FileObject#getAttribute} on a {@code literal:*} key
         * @return the corresponding declaration, or null if not found
         */
        private @CheckForNull Element findDeclaration(WorkingCopy wc, @NullAllowed Object instanceAttribute) {
            if (!(instanceAttribute instanceof String)) {
                return null;
            }
            String attr = (String) instanceAttribute;
            if (attr.startsWith("new:")) {
                return wc.getElements().getTypeElement(attr.substring(4).replace('$', '.'));
            } else if (attr.startsWith("method:")) {
                int dot = attr.lastIndexOf('.');
                TypeElement type = wc.getElements().getTypeElement(attr.substring(7, dot).replace('$', '.'));
                if (type != null) {
                    String meth = attr.substring(dot + 1);
                    for (Element check : type.getEnclosedElements()) {
                        if (check.getKind() == ElementKind.METHOD && check.getSimpleName().contentEquals(meth) && ((ExecutableElement) check).getParameters().isEmpty()) {
                            return check;
                        }
                    }
                }
            }
            return null;
        }

        /**
         * Saves the layer after some modifications to {@link #file()}.
         * @throws IOException if the layer could not be saved
         */
        public void saveLayer() throws IOException {
            layer.save();
        }

        /**
         * Task to be used from {@link #findAndModifyDeclaration}.
         */
        interface ModifyDeclarationTask {
            /**
             * Modify the original declaration.
             * @param wc Java source information
             * @param declaration the {@link TypeElement} or {@link ExecutableElement} that the instance attribute corresponds to
             * @param modifiers modifiers of the declaration that you might wish to add annotations to
             * @throws Exception in case of problem
             */
            void run(WorkingCopy wc, Element declaration, ModifiersTree modifiers) throws Exception;
        }

        /**
         * Tries to find the Java declaration of an instance attribute, and if successful, runs a task to modify it.
         * @param instanceAttribute the result of {@link FileObject#getAttribute} on a {@code literal:*} key (or {@link #instanceAttribute})
         * @param task a task to run (may modify Java sources and layer objects; all will be saved for you)
         * @throws IOException in case of problem (will instead show a message and return early if the type could not be found)
         */
        @Messages({
            "# {0} - layer attribute", "Hinter.missing_instance_class=Could not find Java source corresponding to {0}.",
            "Hinter.do_not_edit_layer=Do not edit layer.xml until the hint has had a chance to run."
        })
        public void findAndModifyDeclaration(@NullAllowed final Object instanceAttribute, final ModifyDeclarationTask task) throws IOException {
            FileObject java = findDeclaringSource(instanceAttribute);
            if (java == null) {
                DialogDisplayer.getDefault().notify(new Message(Hinter_missing_instance_class(instanceAttribute), NotifyDescriptor.WARNING_MESSAGE));
                return;
            }
            JavaSource js = JavaSource.forFileObject(java);
            if (js == null) {
                throw new IOException("No source info for " + java);
            }
            js.runModificationTask(new Task<WorkingCopy>() {
                public @Override void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(JavaSource.Phase.RESOLVED);
                    if (DataObject.find(layer.getLayerFile()).isModified()) { // #207077
                        DialogDisplayer.getDefault().notify(new Message(Hinter_do_not_edit_layer(), NotifyDescriptor.WARNING_MESSAGE));
                        return;
                    }
                    Element decl = findDeclaration(wc, instanceAttribute);
                    if (decl == null) {
                        DialogDisplayer.getDefault().notify(new Message(Hinter_missing_instance_class(instanceAttribute), NotifyDescriptor.WARNING_MESSAGE));
                        return;
                    }
                    ModifiersTree mods;
                    if (decl.getKind() == ElementKind.CLASS) {
                        mods = wc.getTrees().getTree((TypeElement) decl).getModifiers();
                    } else {
                        mods = wc.getTrees().getTree((ExecutableElement) decl).getModifiers();
                    }
                    task.run(wc, decl, mods);
                    saveLayer();
                }
            }).commit();
            SaveCookie sc = DataObject.find(java).getLookup().lookup(SaveCookie.class);
            if (sc != null) {
                sc.save();
            }
        }

        /**
         * Tries to find the instance associated with a file.
         * If it is a folder, or not a {@code *.instance} file, null is returned.
         * If it has an {@code instanceCreate} attribute, that is used, else
         * {@code instanceClass}, and finally the class implied by the filename.
         * @param file any file
         * @return a methodvalue or newvalue attribute, or null
         * @see #findAndModifyDeclaration
         */
        public @CheckForNull Object instanceAttribute(FileObject file) {
            if (!file.isData() || !file.hasExt("instance")) {
                return null; // not supporting *.settings etc. for now
            }
            Object instanceCreate = file.getAttribute("literal:instanceCreate");
            if (instanceCreate != null) {
                return instanceCreate;
            }
            Object clazz = file.getAttribute("instanceClass");
            if (clazz != null) {
                return "new:" + clazz;
            }
            return "new:" + file.getName().replace('-', '.');
        }

        /**
         * Converts a (possibly) localized string attribute in a layer into a value suitable for {@link org.openide.filesystems.annotations.LayerBuilder.File#bundlevalue(String,String)}.
         * @param attribute the result of {@link FileObject#getAttribute} on a {@code literal:*} key
         * @param declaration the declaring element (used to calculate package)
         * @return a string referring to the same (possibly) localized value (may be null)
         */
        public @CheckForNull String bundlevalue(@NullAllowed Object attribute, Element declaration) {
            if (attribute instanceof String) {
                String val = (String) attribute;
                if (val.startsWith("bundle:")) {
                    PackageElement pkg = findPackage(declaration);
                    if (pkg != null) {
                        String expected = "bundle:" + pkg.getQualifiedName() + ".Bundle#";
                        if (val.startsWith(expected)) {
                            return val.substring(expected.length() - 1); // keep '#'
                        }
                    }
                    return val.substring(7);
                }
                return val;
            } else {
                return null;
            }
        }
        private PackageElement findPackage(Element e) {
            if (e.getKind() == ElementKind.PACKAGE) {
                return ((PackageElement) e);
            }
            Element parent = e.getEnclosingElement();
            if (parent == null) {
                return null;
            }
            return findPackage(parent);
        }

        /**
         * Convenience method to add an annotation to an element.
         * @param wc Java source context
         * @param modifiers the element's modifiers to append to
         * @param type canonical name of the annotation type
         * @param pluralType if not null, canonical name of a plural variant of {@code type}
         * @param parameters simple parameters of String or primitive type or String[] (null values are skipped)
         * @return the expanded modifiers tree
         */
        // XXX ought to also accept CompilationUnitTree
        public @CheckReturnValue ModifiersTree addAnnotation(WorkingCopy wc, ModifiersTree modifiers, String type, @NullAllowed String pluralType, Map<String,Object> parameters) {
            TreeMaker make = wc.getTreeMaker();
            TypeElement ann = wc.getElements().getTypeElement(type);
            if (ann == null) {
                // XXX does this deserve a localized message? generally hint should have been disabled already if missing
                throw new IllegalArgumentException("Could not find " + type + " in classpath");
            }
            List<ExpressionTree> arguments = new ArrayList<ExpressionTree>();
            for (Map.Entry<String,Object> entry : parameters.entrySet()) {
                Object value = entry.getValue();
                ExpressionTree valueTree;
                if (value instanceof Object[]) {
                    Object[] array = (Object[]) value;
                    if (array.length == 1) {
                        valueTree = make.Literal(array[0]);
                    } else {
                        List<ExpressionTree> elements = new ArrayList<ExpressionTree>();
                        for (Object o : array) {
                            elements.add(make.Literal(o));
                        }
                        valueTree = make.NewArray(null, Collections.<ExpressionTree>emptyList(), elements);
                    }
                } else if (value != null) {
                    valueTree = make.Literal(value);
                } else {
                    continue;
                }
                arguments.add(make.Assignment(make.Identifier(entry.getKey()), valueTree));
            }
            AnnotationTree toAdd = make.Annotation(make.QualIdent(ann), arguments);
            if (pluralType != null) {
                List<? extends AnnotationTree> existingAnns = modifiers.getAnnotations();
                for (int i = 0; i < existingAnns.size(); i++) {
                    AnnotationTree existingAnn = existingAnns.get(i);
                    // XXX see UseNbBundleMessages; is there a better way to do this?
                    String existingType = existingAnn.getAnnotationType().toString();
                    // XXX this will not work if type is a nested class
                    if (existingType.equals(type) || existingType.equals(type.replaceFirst(".+[.]", ""))) {
                        return make.insertModifiersAnnotation(make.removeModifiersAnnotation(modifiers, i), i,
                                make.Annotation(make.QualIdent(pluralType),
                                Collections.singletonList(make.Assignment(make.Identifier("value"),
                                make.NewArray(null, Collections.<ExpressionTree>emptyList(), Arrays.asList(existingAnn, toAdd))))));
                    } else if (existingType.equals(pluralType) || existingType.equals(pluralType.replaceFirst(".+[.]", ""))) {
                        List<? extends ExpressionTree> args = existingAnn.getArguments();
                        if (args.size() != 1) {
                            throw new IllegalArgumentException("expecting just one arg for @" + pluralType);
                        }
                        AssignmentTree assign = (AssignmentTree) args.get(0);
                        if (!assign.getVariable().toString().equals("value")) {
                            throw new IllegalArgumentException("expected value=... for @" + pluralType);
                        }
                        ExpressionTree arg = assign.getExpression();
                        NewArrayTree arr;
                        if (arg.getKind() == Tree.Kind.STRING_LITERAL) {
                            arr = make.NewArray(null, Collections.<ExpressionTree>emptyList(), Collections.singletonList(arg));
                        } else if (arg.getKind() == Tree.Kind.NEW_ARRAY) {
                            arr = (NewArrayTree) arg;
                        } else {
                            throw new IllegalArgumentException("unknown arg kind " + arg.getKind() + ": " + arg);
                        }
                        return make.insertModifiersAnnotation(make.removeModifiersAnnotation(modifiers, i), i,
                                make.Annotation(existingAnn.getAnnotationType(),
                                Collections.singletonList(make.Assignment(assign.getVariable(), make.addNewArrayInitializer(arr, toAdd)))));
                    }
                }
            }
            return make.addModifiersAnnotation(modifiers, toAdd);
        }

        /**
         * Deletes an obsolete layer entry.
         * Also deletes empty parent directories.
         * @param entry a file to delete
         * @throws IOException in case of problem
         */
        public void delete(FileObject entry) throws IOException {
            entry.delete();
            FileObject parent = entry.getParent();
            if (parent.getChildren().length == 0 && !parent.getAttributes().hasMoreElements()) {
                if (parent.isRoot()) {
                    // XXX maybe delete the whole layer file! (and its reference in manifest.mf)
                } else {
                    delete(parent);
                }
            }
        }

    }

}
