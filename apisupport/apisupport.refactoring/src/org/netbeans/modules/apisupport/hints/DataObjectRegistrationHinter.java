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
package org.netbeans.modules.apisupport.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import static org.netbeans.modules.apisupport.hints.Bundle.*;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbCollections;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;

/**
 * #207219: {@code DataObject.Registration} conversion.
 *
 * This Hinter must take care of
 *
 * Loader/$mimetype/Factory option 1: dataloader option 2: factory and Loader
 */
@ServiceProvider(service = Hinter.class)
public class DataObjectRegistrationHinter implements Hinter {

    public static final String ACTIONS_FOLDER = "Actions";
    public static final String METHOD_DOPOOL_FACTORY = "method:org.openide.loaders.DataLoaderPool.factory";
    private static final String LOADERS_FOLDER = "Loaders/";
    private static final String FACTORIES_FOLDER = "Factories";

    @Override
    public void process(final Context ctx) throws Exception {
        final FileObject file = ctx.file();
        // check every file path starting with Loaders
        if (file.getPath().startsWith(LOADERS_FOLDER)) {
            if (file.getPath().contains(FACTORIES_FOLDER)) { // its a factory
                processFactories(ctx, file);
            }
            ///// Actions
            if (file.getPath().endsWith(ACTIONS_FOLDER)) {
                processActions(ctx, file);
            }
        }
    }

    // not always of the same mimetype of related dataobject
    @Messages({
        "DataObjectRegistrationHinter.no_DataObject=No visible dataObject, please try to convert Factories folder first",
        "# {0} - Java source file name", "# {1} - list of MIME types", "DataObjectRegistrationHinter.fix.regular=Convert registration to Java annotation in {0} ({1})",
        "# {0} - Java source file name", "# {1} - list of MIME types", "DataObjectRegistrationHinter.fix.special=Convert registration to Java annotation in {0} ({1}) <b>be careful mimeType not matching</b>"
    })
    private void processActions(final Context ctx, final FileObject file) throws Exception {

        if (annotationsActionsDataObjectAvailable(ctx)) {
            // infer mimetype from path
            String actionsMime = Utility.getMimeTypeFromActionsPath(file.getPath());
            //
            // try get DataObject / DataObject.Factory available in this project
            Map<String, List<String>> visibleLoaderFactories = new HashMap<String, List<String>>();
            FileObject startingPath = file.getFileSystem().findResource(LOADERS_FOLDER);
            if (startingPath != null) {
                for (FileObject aLoadersFileObject : NbCollections.iterable(startingPath.getChildren(true))) {
                    if (aLoadersFileObject.getPath().contains(FACTORIES_FOLDER)) { // its a factory

                        final Object instanceCreate = ctx.instanceAttribute(aLoadersFileObject);
                        if (instanceCreate != null) {

                            if (!METHOD_DOPOOL_FACTORY.equals(instanceCreate)) {

                                String ic = instanceCreate.toString();

                                if (visibleLoaderFactories.containsKey(ic)) {
                                    visibleLoaderFactories.get(ic).add(Utility.getMimeTypeFromFactoryPath(aLoadersFileObject.getPath()));
                                } else {
                                    List<String> mime = new LinkedList<String>();
                                    mime.add(Utility.getMimeTypeFromFactoryPath(aLoadersFileObject.getPath()));
                                    visibleLoaderFactories.put(ic, mime);
                                }
                            }
                        }
                    }
                }
                if (visibleLoaderFactories.isEmpty()) {
                    ctx.addHint(Severity.VERIFIER, DataObjectRegistrationHinter_no_DataObject());
                } else {
                    List<Fix> fixes = new ArrayList<Fix>(); // prepare list of fixes
                    boolean mimeMatch = false;
                    for (Map.Entry<String, List<String>> loader : visibleLoaderFactories.entrySet()) {
                        for (String aMime : loader.getValue()) {
                            if (actionsMime.equals(aMime)) {
                                mimeMatch = true;
                            }
                        }
                    }
                    for (Map.Entry<String, List<String>> loader : visibleLoaderFactories.entrySet()) { // 
                        boolean restrict = true;
                        // mime list
                        StringBuilder sbMime = new StringBuilder();
                        for (String aMime : loader.getValue()) {
                            if (actionsMime.equals(aMime)) {
                                restrict = false;
                            }
                            sbMime.append(aMime);

                            sbMime.append(",");
                        }
                        sbMime.deleteCharAt(sbMime.length() - 1);
                        String fname = loader.getKey().substring(loader.getKey().lastIndexOf(".") + 1) + ".java";
                        final String text = mimeMatch ? DataObjectRegistrationHinter_fix_regular(fname, sbMime) : DataObjectRegistrationHinter_fix_special(fname, sbMime);
                        final String fixParam = loader.getKey();
                        if (!mimeMatch) {
                            restrict = false;
                        }
                        if (!restrict) {
                            fixes.add(new Fix() {

                                @Override
                                public String getText() {
                                    return text;
                                }

                                @Override
                                public ChangeInfo implement() throws Exception {
                                    ctx.findAndModifyDeclaration(fixParam, new RegisterActionsDataObject(ctx));
                                    return null;
                                }
                            });
                        }

                    }
                    if (!fixes.isEmpty()) {
                        ctx.addHint(Severity.WARNING, ctx.standardAnnotationDescription(), fixes.toArray(new Fix[0]));
                    }
                }

            }
        }
    }

    private Object getInstanceDataObject(Object instanceCreate, FileObject file) {
        if (METHOD_DOPOOL_FACTORY.equals(instanceCreate)) {
            return "new:" + file.getAttribute("literal:dataObjectClass");
        } else {
            return instanceCreate;
        }
    }

    private void processFactories(final Context ctx, final FileObject file) throws Exception {
        final Object instanceCreate = ctx.instanceAttribute(file);
        if (instanceCreate == null) {
            return;
        }
        if (checkAttributes(file, ctx)) {
            ctx.addStandardAnnotationHint(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    if (!annotationsDataObjectAvailable(ctx)) {
                        return null;
                    }
                    ctx.findAndModifyDeclaration(getInstanceDataObject(instanceCreate, file), new RegisterDataObject(ctx));
                    return null;
                }
            });
        }
    }

    @Messages("DataObjectRegistrationHinter.missing_dep=You must add a dependency on org.openide.loaders (7.36+) before using this fix.")
    private boolean annotationsDataObjectAvailable(Context ctx) {
        if (ctx.canAccess(DataObject.Registration.class.getName())
                && ctx.canAccess(DataObject.Registrations.class.getName())) {
            return true;
        } else {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(DataObjectRegistrationHinter_missing_dep(), NotifyDescriptor.WARNING_MESSAGE));
            return false;
        }
    }

    //@Messages("DataObjectRegistrationHinter.missing_org.openide.awt=You must add a dependency on org.openide.awt (7.27+) before using this fix.")
    private boolean annotationsActionsDataObjectAvailable(Context ctx) {
        if (ctx.canAccess("org.openide.awt.ActionReferences")) {
            return true;
        } else {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ActionRegistrationHinter_missing_org_openide_awt(), NotifyDescriptor.WARNING_MESSAGE));
            return false;
        }
    }

    @Messages({
        "# {0} - file attribute name", "DataObjectHinter_unrecognized_attr=Unrecognized DataObject attribute: {0}",
        "DataObjectRegistrationHinter.use.displayName=Please convert to displayName to be able to use Loader hinter"
    })
    private boolean checkAttributes(FileObject file, Context ctx) {
        boolean attributesCompatible = true;
        for (String attr : NbCollections.iterable(file.getAttributes())) {
            if (!attr.matches("mimeType|position|displayName|iconBase|dataObjectClass|instanceCreate|SystemFileSystem.localizingBundle")) {
                ctx.addHint(Severity.WARNING, DataObjectHinter_unrecognized_attr(attr));
                attributesCompatible = false;
            }
        }
        if (file.getAttribute("literal:SystemFileSystem.localizingBundle") != null) {
            attributesCompatible = false;
            ctx.addHint(Severity.HINT, DataObjectRegistrationHinter_use_displayName());
        }
        return attributesCompatible;
    }

    //Context.ModifyDeclarationTask 
    private static class RegisterDataObject implements Context.ModifyDeclarationTask {

        private static final String DATAOBJECT_REGISTRATION = "org.openide.loaders.DataObject.Registration";
        private static final String DATAOBJECT_REGISTRATIONS = "org.openide.loaders.DataObject.Registrations";
        private static final String MIME_TYPE = "mimeType";
        private final Context ctx;

        private RegisterDataObject(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run(WorkingCopy wc, Element declaration, ModifiersTree modifiers) throws Exception {
            Map<String, Object> params = new HashMap<String, Object>();
            FileObject file = ctx.file();

            String displayName = ctx.bundlevalue(file.getAttribute("literal:displayName"), declaration);

            if (displayName == null) {
                // checkAttributes method tries to warn user to avoid this fallback
                // unaware if fix can be chained
                displayName = "#TODO";
            }

            // parameters of annotation

            // mimeType is mandatory
            if (file.getAttribute(MIME_TYPE) != null) {
                params.put(MIME_TYPE, file.getAttribute(MIME_TYPE));
            } else {
                if (Utility.getMimeTypeFromFactoryPath(file.getPath()).isEmpty()) {
                    throw new IllegalArgumentException("Could not find a non empty mimetype");
                }
                params.put(MIME_TYPE, Utility.getMimeTypeFromFactoryPath(file.getPath()));

            }
            // rest of annotation parameter
            params.put("position", file.getAttribute("position"));
            params.put("displayName", displayName);
            params.put("iconBase", file.getAttribute("iconBase"));

            ModifiersTree mt = ctx.addAnnotation(wc, modifiers, DATAOBJECT_REGISTRATION, DATAOBJECT_REGISTRATIONS, params);
            wc.rewrite(modifiers, GeneratorUtilities.get(wc).importFQNs(mt));

            ctx.delete(file);
        }
    }

    private static class RegisterActionsDataObject implements Context.ModifyDeclarationTask {

        private final Context ctx;

        private RegisterActionsDataObject(Context ctx) {
            this.ctx = ctx;
        }

        private boolean isSeparator(FileObject fo) {
            return (fo != null) && (fo.hasExt("instance") && ("javax.swing.JSeparator".equals(fo.getAttribute("instanceClass"))));
        }

        private boolean isSameLayer(FileObject fo) { // thanks to Jesse
            return Utilities.compareObjects(ctx.file().getAttribute("layers"), fo.getAttribute("layers"));
        }

        @Override
        public void run(WorkingCopy wc, Element declaration, ModifiersTree modifiers) throws Exception {
            FileObject file = ctx.file();
            List<FileObject> toDelete = new ArrayList<FileObject>(); // store fileobject to delete to allow delete at once
// precheck classpath
            TypeElement annActionID = wc.getElements().getTypeElement("org.openide.awt.ActionID");
            if (annActionID == null) {
                throw new IllegalArgumentException("Could not find ActionID in classpath");
            }
            TypeElement annActionRef = wc.getElements().getTypeElement("org.openide.awt.ActionReference");
            if (annActionRef == null) {
                throw new IllegalArgumentException("Could not find ActionReference in classpath");
            }
            TypeElement annActionRefs = wc.getElements().getTypeElement("org.openide.awt.ActionReferences");
            if (annActionRefs == null) {
                throw new IllegalArgumentException("Could not find ActionReferences in classpath");
            }


            if (!file.isData()) { // if data we are not well placed in the filesystem

                //list the children registred all layer important for dealing with separator
                List<FileObject> foList = new ArrayList<FileObject>();
                for (FileObject achildren : NbCollections.iterable(file.getData(true))) {
                    foList.add(achildren);

                }
                foList = FileUtil.getOrder(foList, true); // order them


                ListIterator<FileObject> iter = foList.listIterator(); // if first element is a separator prepare

                TreeMaker make = wc.getTreeMaker();
                List<AnnotationTree> anns = new ArrayList<AnnotationTree>();
                while (iter.hasNext()) {
                    FileObject fo = iter.next();
                    if (fo.hasExt("shadow") && isSameLayer(fo)) { // iterate only is the layer
                        // ActionID
                        List<ExpressionTree> argumentsActionID = new ArrayList<ExpressionTree>();
                        //   get Original file
                        FileObject originalFile = FileUtil.getConfigFile(fo.getAttribute("originalFile").toString());
                        // do category and identifier the way action registration hinter do
                        argumentsActionID.add(make.Assignment(make.Identifier("category"), make.Literal(originalFile.getParent().getPath().substring("Actions/".length()))));
                        argumentsActionID.add(make.Assignment(make.Identifier("id"), make.Literal(originalFile.getName().replace('-', '.'))));

                        // ActionReference

                        List<ExpressionTree> arguments = new ArrayList<ExpressionTree>();

                        arguments.add(make.Assignment(make.Identifier("id"), make.Annotation(make.QualIdent(annActionID), argumentsActionID)));
                        arguments.add(make.Assignment(make.Identifier("path"), make.Literal(fo.getParent().getPath())));
                        arguments.add(make.Assignment(make.Identifier("position"), make.Literal(fo.getAttribute("position"))));


// separator before ?
                        if (iter.hasPrevious()) {
                            iter.previous();

                            if (iter.hasPrevious()) {
                                FileObject candidateSep = iter.previous();

                                iter.next();
                                if ((isSeparator(candidateSep))
                                        && !toDelete.contains(candidateSep)) {
                                    arguments.add(make.Assignment(make.Identifier("separatorBefore"), make.Literal(candidateSep.getAttribute("position"))));
                                    toDelete.add(candidateSep);  // delete separator
                                }

                            }
                            iter.next();
                        }
// separator after ?
                        if (iter.hasNext()) {
                            FileObject candidateSep = iter.next();
                            iter.previous();

                            if (isSeparator(candidateSep)) {
                                arguments.add(make.Assignment(make.Identifier("separatorAfter"), make.Literal(candidateSep.getAttribute("position"))));
                                toDelete.add(candidateSep);  // delete separator
                            }
                        }


                        anns.add(make.Annotation(make.QualIdent(annActionRef), arguments));
                        toDelete.add(fo);

                    }
// Missing separator waring in comment ? 
                }


                if (!anns.isEmpty()) {
                    ModifiersTree nue = null;
                    boolean existingActionReference = false;
                    List<? extends AnnotationTree> existanns = modifiers.getAnnotations();
                    for (int i = 0; i < existanns.size(); i++) {
                        AnnotationTree ann = existanns.get(i);
                        Tree annotationType = ann.getAnnotationType();
                        if (annotationType.toString().matches("ActionReferences")) {
                            existingActionReference = true;
                            List<? extends ExpressionTree> args = ann.getArguments();
                            AssignmentTree assign = (AssignmentTree) args.get(0);
                            if (!assign.getVariable().toString().equals("value")) {
                                throw new Exception("expected value=... for @ActionReference");
                            }
                            ExpressionTree arg = assign.getExpression();
                            NewArrayTree arr;
                            if (arg.getKind() == Tree.Kind.STRING_LITERAL) {
                                arr = make.NewArray(null, Collections.<ExpressionTree>emptyList(), Collections.singletonList(arg));
                            } else if (arg.getKind() == Tree.Kind.NEW_ARRAY) {
                                arr = (NewArrayTree) arg;
                            } else {
                                throw new Exception("unknown arg kind " + arg.getKind() + ": " + arg);
                            }
                            for (ExpressionTree line : anns) {
                                arr = make.addNewArrayInitializer(arr, line);
                            }
                            ann = make.Annotation(annotationType, Collections.singletonList(arr));


                            nue = make.insertModifiersAnnotation(make.removeModifiersAnnotation(modifiers, i), i, ann);
                        }
                    }

                    if (!existingActionReference) {
                        nue =
                                make.addModifiersAnnotation(modifiers,
                                make.Annotation(make.QualIdent(annActionRefs),
                                Collections.singletonList(make.Assignment(make.Identifier("value"),
                                make.NewArray(null, Collections.<ExpressionTree>emptyList(),
                                anns)))));

                    }
                    wc.rewrite(modifiers,
                            GeneratorUtilities.get(wc).importFQNs(nue));
                }

                for (FileObject fo : toDelete) {
                    ctx.delete(fo);
                }
            }
        }
    }

    static class Utility {

        private static String getMime(String path, String right) {
            String mimeType = path.replace(LOADERS_FOLDER, "");
            mimeType = mimeType.substring(0, mimeType.indexOf(right) - 1);// -1 to remove last file separator
            return mimeType;
        }

        private static String getMimeTypeFromFactoryPath(String path) {
            return getMime(path, FACTORIES_FOLDER);
        }

        private static String getMimeTypeFromActionsPath(String path) {
            return getMime(path, ACTIONS_FOLDER);
        }
    }
}
