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

package org.netbeans.modules.editor.lib2;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.netbeans.modules.editor.lib2.actions.EditorActionUtilities;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.Action;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.EditorActionRegistrations;
import org.netbeans.spi.editor.AbstractEditorAction;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 * Annotation processor for {@link EditorActionRegistration}
 * and {@link EditorActionRegistrations}.
 */
@ServiceProvider(service=Processor.class)
public final class EditorActionRegistrationProcessor extends LayerGeneratingProcessor {

    public @Override Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>(Arrays.asList(
            EditorActionRegistration.class.getCanonicalName(),
            EditorActionRegistrations.class.getCanonicalName()
        ));
    }

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) throws LayerGenerationException
    {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(EditorActionRegistration.class)) {
            EditorActionRegistration annotation = e.getAnnotation(EditorActionRegistration.class);
            register(e, annotation);
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(EditorActionRegistrations.class)) {
            EditorActionRegistrations annotationArray = e.getAnnotation(EditorActionRegistrations.class);
            for (EditorActionRegistration annotation : annotationArray.value()) {
                register(e, annotation);
            }
        }
        return true;
    }

    private void register(Element e, EditorActionRegistration annotation) throws LayerGenerationException {
        String actionClassName;
        String createActionMethodName;
        TypeMirror swingActionType = processingEnv.getTypeUtils().getDeclaredType(
                processingEnv.getElementUtils().getTypeElement("javax.swing.Action"));
        TypeMirror utilMapType = processingEnv.getTypeUtils().getDeclaredType(
                processingEnv.getElementUtils().getTypeElement("java.util.Map"));
        boolean directActionCreation = false; // Whether construct wrapper action or annotated action directly
        switch (e.getKind()) {
            case CLASS:
                actionClassName = processingEnv.getElementUtils().getBinaryName((TypeElement)e).toString();
                if (e.getModifiers().contains(Modifier.ABSTRACT)) {
                    throw new LayerGenerationException(actionClassName + " must not be abstract", e);
                }
                if (!e.getModifiers().contains(Modifier.PUBLIC)) {
                    throw new LayerGenerationException(actionClassName + " is not public", e);
                }
                ExecutableElement defaultCtor = null;
                ExecutableElement mapCtor = null;
                for (ExecutableElement constructor : ElementFilter.constructorsIn(e.getEnclosedElements())) {
                    List<? extends VariableElement> params = constructor.getParameters();
                    if (params.isEmpty()) {
                        defaultCtor = constructor;

                    } else if (params.size() == 1 &&
                        processingEnv.getTypeUtils().isAssignable(params.get(0).asType(), utilMapType))
                    {
                        mapCtor = constructor;
                    }
                }
                String msgBase = "No-argument constructor";
                if (defaultCtor == null) {
                    throw new LayerGenerationException(msgBase + " not present in " + actionClassName, e);
                }
                boolean defaultCtorPublic = (defaultCtor != null && defaultCtor.getModifiers().contains(Modifier.PUBLIC));
                boolean mapCtorPublic = (mapCtor != null && mapCtor.getModifiers().contains(Modifier.PUBLIC));
                if (!defaultCtorPublic && !mapCtorPublic) {
                    throw new LayerGenerationException(msgBase + " not public in " + actionClassName, e);
                }

                if (!processingEnv.getTypeUtils().isAssignable(e.asType(), swingActionType)) {
                    throw new LayerGenerationException(actionClassName + " is not assignable to javax.swing.Action", e);
                }
                createActionMethodName = null;
                break;

            case METHOD:
                actionClassName = processingEnv.getElementUtils().getBinaryName((TypeElement) e.getEnclosingElement()).toString();
                createActionMethodName = e.getSimpleName().toString();
                if (!e.getModifiers().contains(Modifier.STATIC)) {
                    throw new LayerGenerationException(actionClassName + "." + createActionMethodName + " must be static", e); // NOI18N
                }
                List<? extends VariableElement> params = ((ExecutableElement)e).getParameters();
                boolean emptyParams = params.isEmpty();
                boolean mapParam = (params.size() == 1 && processingEnv.getTypeUtils().isAssignable(
                        params.get(0).asType(), utilMapType));
                if (!emptyParams && !mapParam)
                {
                    throw new LayerGenerationException(actionClassName + "." + createActionMethodName +
                            " must not take arguments (or have a single-argument \"Map<String,?> attrs\")", e); // NOI18N
                }
                TypeMirror returnType = ((ExecutableElement)e).getReturnType();
                if (swingActionType != null && !processingEnv.getTypeUtils().isAssignable(returnType, swingActionType)) {
                    throw new LayerGenerationException(actionClassName + "." + createActionMethodName + " is not assignable to javax.swing.Action", e);
                }
                if (mapParam) {
                    directActionCreation = true;
                }
                break;

            default:
                throw new IllegalArgumentException("Annotated element is not loadable as an instance: " + e);

        }

        String actionName = annotation.name();
        StringBuilder actionFilePathBuilder = new StringBuilder(50);
        String mimeType = annotation.mimeType();
        actionFilePathBuilder.append("Editors");
        if (mimeType.length() > 0) {
            actionFilePathBuilder.append("/").append(mimeType);
        }
        actionFilePathBuilder.append("/Actions/");
        actionFilePathBuilder.append(actionName).append(".instance");
        LayerBuilder layer = layer(e);
        String actionFilePath = actionFilePathBuilder.toString();
        LayerBuilder.File actionFile = layer.file(actionFilePath);
        String preferencesKey = annotation.preferencesKey();

	// Resolve category for keymap
	String category = annotation.category();
	if (null != category && !category.isEmpty()) {
            StringBuilder pathBuilder = new StringBuilder(50);
	    pathBuilder.append("OptionsDialog/Actions/").append(category).append("/").append(annotation.name());
	    LayerBuilder.File file = layer.file(pathBuilder.toString());
	    file.write();
	}
        
        // Resolve icon resource
        String iconResource = annotation.iconResource();
        if (iconResource.length() > 0) {
            actionFile.stringvalue(AbstractEditorAction.ICON_RESOURCE_KEY, iconResource);
        }

        // Resolve short description bundle key
        String shortDescription = annotation.shortDescription();
        if (shortDescription.length() > 0) {
            if ("INHERIT".equals(shortDescription)) {
                actionFile.methodvalue(AbstractEditorAction.DISPLAY_NAME_KEY,
                        EditorActionUtilities.class.getName(), "getGlobalActionShortDescription");
                actionFile.methodvalue(Action.SHORT_DESCRIPTION,
                        EditorActionUtilities.class.getName(), "getGlobalActionShortDescription");
            } else {
                if ("BY_ACTION_NAME".equals(shortDescription)) {
                    shortDescription = "#" + actionName;
                }
                actionFile.bundlevalue(AbstractEditorAction.DISPLAY_NAME_KEY, shortDescription);
                actionFile.bundlevalue(Action.SHORT_DESCRIPTION, shortDescription);
            }
        }

        // Resolve menu text bundle key
        String menuText = annotation.menuText();
        if (menuText.length() > 0) {
            actionFile.bundlevalue("menuText", menuText);
        } else if (shortDescription.length() > 0) { // Use shortDesc instead
            menuText = shortDescription;
            actionFile.bundlevalue("menuText", menuText);
        }

        // Resolve popup menu text bundle key
        String popupText = annotation.popupText();
        if (popupText.length() > 0) {
            actionFile.bundlevalue("popupText", popupText);
        } else if (menuText.length() > 0) { // Use menuText instead
            popupText = menuText;
            actionFile.bundlevalue("popupText", popupText);
        }

        // Check menu path and generate menu presenter shadow file
        String menuPath = annotation.menuPath();
        int menuPosition = annotation.menuPosition();
        if (menuPosition != Integer.MAX_VALUE) {
            StringBuilder menuPresenterFilePath = new StringBuilder(50);
            menuPresenterFilePath.append("Menu/");
            if (menuPath.length() > 0) {
                menuPresenterFilePath.append(menuPath).append('/');
            }
            menuPresenterFilePath.append(actionName).append(".shadow");
            LayerBuilder.File menuPresenterShadowFile = layer.file(menuPresenterFilePath.toString());
            menuPresenterShadowFile.stringvalue("originalFile", actionFilePath);
            menuPresenterShadowFile.intvalue("position", menuPosition);
            menuPresenterShadowFile.write();
        }

        // Check popup path and generate popup presenter shadow file
        String popupPath = annotation.popupPath();
        int popupPosition = annotation.popupPosition();
        if (popupPosition != Integer.MAX_VALUE) {
            StringBuilder popupPresenterFilePath = new StringBuilder(50);
            popupPresenterFilePath.append("Editors/Popup/");
            if (mimeType.length() > 0) {
                popupPresenterFilePath.append(mimeType).append("/");
            }
            if (popupPath.length() > 0) {
                popupPresenterFilePath.append(popupPath).append('/');
            }
            popupPresenterFilePath.append(actionName).append(".shadow");
            LayerBuilder.File popupPresenterShadowFile = layer.file(popupPresenterFilePath.toString());
            popupPresenterShadowFile.stringvalue("originalFile", actionFilePath);
            popupPresenterShadowFile.intvalue("position", popupPosition);
            popupPresenterShadowFile.write();
        }
        
        // Generate toolBar presenter
        int toolBarPosition = annotation.toolBarPosition();
        if (toolBarPosition != Integer.MAX_VALUE) {
            StringBuilder toolBarPresenterFilePath = new StringBuilder(50);
            toolBarPresenterFilePath.append("Editors/");
            if (mimeType.length() > 0) {
                toolBarPresenterFilePath.append(mimeType).append("/");
            }
            toolBarPresenterFilePath.append("Toolbars/Default/");
            toolBarPresenterFilePath.append(actionName).append(".shadow");
            LayerBuilder.File toolBarPresenterShadowFile = layer.file(toolBarPresenterFilePath.toString());
            toolBarPresenterShadowFile.stringvalue("originalFile", actionFilePath);
            toolBarPresenterShadowFile.intvalue("position", toolBarPosition);
            toolBarPresenterShadowFile.write();
        }

        if (preferencesKey.length() > 0) {
            actionFile.stringvalue("preferencesKey", preferencesKey);
            actionFile.methodvalue("preferencesNode", EditorActionUtilities.class.getName(), "getGlobalPreferences");
            actionFile.boolvalue("preferencesDefault", annotation.preferencesDefault());
        }

        actionFile.stringvalue(Action.NAME, actionName);
        // Deafult helpID is action's name
        actionFile.stringvalue("helpID", actionName);

        // Accelerator filled with MULTI_ACCELERATOR_LIST_KEY property by infrastructure
//        file.methodvalue(Action.ACCELERATOR_KEY, EditorActionUtilities.class.getName(), "getAccelerator");

        boolean noIconInMenu = annotation.noIconInMenu();
        if (noIconInMenu) {
            actionFile.boolvalue(AbstractEditorAction.NO_ICON_IN_MENU, noIconInMenu);
        }
        boolean noKeyBinding = annotation.noKeyBinding();
        if (noKeyBinding) {
            actionFile.boolvalue(AbstractEditorAction.NO_KEY_BINDING, noKeyBinding);
        }

        // Resolve weight attribute that allows to override existing action
        int weight = annotation.weight();
        if (weight != 0) {
            actionFile.intvalue("weight", weight);
        }

        if (directActionCreation) {
            actionFile.methodvalue("instanceCreate", actionClassName, createActionMethodName);

        } else { // Create wrapper action
            actionFile.methodvalue("instanceCreate", "org.netbeans.modules.editor.lib2.actions.WrapperEditorAction", "create");
            actionFile.boolvalue(AbstractEditorAction.WRAPPER_ACTION_KEY, true);
            if (createActionMethodName != null) {
                actionFile.methodvalue("delegate", actionClassName, createActionMethodName);
            } else {
                actionFile.newvalue("delegate", actionClassName);
            }
        }
        actionFile.write();
    }

}
