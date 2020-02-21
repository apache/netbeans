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

package org.netbeans.modules.cnd.qnavigator.navigator;


import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmCompoundClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmErrorDirective;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVariableDefinition;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.AbstractCsmNode;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.api.ui.CsmRefactoringActionsFactory;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.util.CharSequences;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Navigator Tree node.
 */
public class CppDeclarationNode extends AbstractCsmNode implements Comparable<CppDeclarationNode> {
    private static final String FONT_COLORCONTROLSHADOW = "<font color='!textInactiveText'>  "; // NOI18N
    private static final byte INCLUDE_WEIGHT = 5*10+0;
    private static final byte FUNCTION_DECLARATION_WEIGHT = 3*10+0;
    private static final byte FUNCTION_DEFINITION_WEIGHT = 3*10+1;
    private Image icon;
    private CsmObject object;
    private CsmFile file;
    private boolean isFriend;
    private boolean isSpecialization;
    private final CsmFileModel model;
    private boolean needInitHTML = true;
    private CharSequence name;
    private CharSequence htmlDisplayName;
    private CharSequence scopeName = CharSequences.empty();
    private CharSequence definitionScopeName = CharSequences.empty();
    private byte weight;
    private final InstanceContent ic;

    private static CppDeclarationNode createNamespaseContainer(CsmOffsetableDeclaration element, CsmFileModel model, List<IndexOffsetNode> lineNumberIndex, AtomicBoolean canceled) {
        CppDeclarationNode res = new CppDeclarationNode(new NavigatorChildren(element, model, null, lineNumberIndex, canceled), new InstanceContent(), element, element.getContainingFile(), model);
        res.icon = res.getIcon(0);
        return res;
    }
    
    private static CppDeclarationNode createClassifierContainer(CsmOffsetableDeclaration element, CsmFileModel model, CsmCompoundClassifier classifier, List<IndexOffsetNode> lineNumberIndex, AtomicBoolean canceled) {
        CppDeclarationNode res = new CppDeclarationNode(new NavigatorChildren(element, model, classifier, lineNumberIndex, canceled), new InstanceContent(), element, element.getContainingFile(), model);
        res.icon = res.getIcon(0);
        return res;
    }

    private static CppDeclarationNode createFile(CsmFile csmFile, CsmFileModel model) {
        CppDeclarationNode res = new CppDeclarationNode(Children.LEAF, new InstanceContent(), csmFile, csmFile, model);
        res.icon = res.getIcon(0);
        return res;
    }

    private static CppDeclarationNode createDeclaration(CsmOffsetableDeclaration element, CsmFileModel model, boolean isFriend) {
        CppDeclarationNode res = new CppDeclarationNode(Children.LEAF, new InstanceContent(), element, element.getContainingFile(), model);
        res.isFriend = isFriend;
        res.icon = res.getIcon(0);
        return res;
    }

    private static CppDeclarationNode createObject(CsmOffsetable element, CsmFileModel model) {
        CppDeclarationNode res = new CppDeclarationNode(Children.LEAF, new InstanceContent(), element, element.getContainingFile(), model);
        res.icon = res.getIcon(0);
        return res;
    }

    private CppDeclarationNode(Children children, InstanceContent ic, CsmObject element, CsmFile file, CsmFileModel model) {
        super(children, new AbstractLookup(ic));
        this.object = element;
        this.file = file;
        this.model = model;
        this.weight = getObjectWeight();
        ic.add(element);
        ic.add(model.getFileObject());
        ic.add(model.getDataObject());
        this.ic = ic;
    }

    private CharSequence createFunctionSpecializationHtmlDisplayName() {
        return CharSequences.create(CharSequenceUtils.concatenate(CsmDisplayUtilities.htmlize(getDisplayName()), FONT_COLORCONTROLSHADOW, scopeName)); 
    }
    
    private CharSequence createMemberHtmlDisplayName() {
        String aName = CsmDisplayUtilities.htmlize(scopeName); 
        String displayName = CsmDisplayUtilities.htmlize(getDisplayName()); // NOI18N
        String in = NbBundle.getMessage(getClass(), "LBL_inClass", aName); //NOI18N
        return CharSequences.create(CharSequenceUtils.concatenate(displayName, FONT_COLORCONTROLSHADOW, in));
    }

    private CharSequence getFunctionSpecializationName(CsmObject csmObject) throws MissingResourceException {
        CsmFunction fun = (CsmFunction)csmObject;
        String specializationContainerName = fun.getQualifiedName().toString();
        int endInd = specializationContainerName.lastIndexOf("::");//NOI18N
        String in;
        if (endInd > 0) {
            specializationContainerName = CsmDisplayUtilities.htmlize(specializationContainerName.substring(0, endInd));
            in = NbBundle.getMessage(getClass(), "LBL_forClassSpecialization", specializationContainerName); //NOI18N
        } else {
            in = "";//NOI18N
        }
        return CharSequences.create(in);
    }

    private static CharSequence getClassifierName(CsmClassifier cls) {
        CharSequence clsName = cls.getName();
        if (CsmKindUtilities.isClass(cls) && CsmKindUtilities.isTemplate(cls)) {
            clsName = ((CsmTemplate)cls).getDisplayName();
        }
        return clsName;
    }

    private byte getObjectWeight(){
        try {
            if (CsmKindUtilities.isFunctionDefinition(object)) {
                CsmFunction function = ((CsmFunctionDefinition) object).getDeclaration();
                if (function != null && !function.equals(object) && CsmKindUtilities.isClassMember(function)) {
                    CsmClass cls = ((CsmMember) function).getContainingClass();
                    if (cls != null && cls.getName().length() > 0) {
                        scopeName = getClassifierName(cls);
                        definitionScopeName = scopeName;
                    } else if (CsmKindUtilities.isSpecialization(function)) {
                        isSpecialization = true;
                        scopeName = getFunctionSpecializationName(function);
                    }
                } else if (CsmKindUtilities.isSpecialization(object)) {
                    isSpecialization = true;
                    scopeName = getFunctionSpecializationName(object);
                }
            } else if (CsmKindUtilities.isVariableDefinition(object)) {
                CsmVariable variable = ((CsmVariableDefinition) object).getDeclaration();
                if (variable != null && !variable.equals(object) && CsmKindUtilities.isClassMember(variable)) {
                    CsmClass cls = ((CsmMember) variable).getContainingClass();
                    if (cls != null && cls.getName().length() > 0) {
                        scopeName = getClassifierName(cls);
                    }
                }
            } else if (CsmKindUtilities.isFunction(object) && CsmKindUtilities.isSpecialization(object)) {
                isSpecialization = true;
                scopeName = getFunctionSpecializationName(object);
            }
        } catch (AssertionError ex) {
            ex.printStackTrace(System.err);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }

        if(CsmKindUtilities.isNamespaceDefinition(object)) {
            return 0*10+2;
        } else if(CsmKindUtilities.isFile(object)) {
            return 0*10+0;
        } else if(CsmKindUtilities.isNamespaceAlias(object)) {
            return 0*10+0;
        } else if(CsmKindUtilities.isUsing(object)) {
            return 0*10+1;
        } else if(CsmKindUtilities.isModule(object)) {
            return 0*10+3;
        } else if(CsmKindUtilities.isProgram(object)) {
            return 0*10+4;
        } else if(CsmKindUtilities.isClass(object)) {
            return 1*10+1;
        } else if(CsmKindUtilities.isFriendClass(object)) {
            return 1*10+0;
        } else if(CsmKindUtilities.isClassForwardDeclaration(object)) {
            return 1*10+0;
        } else if (CsmKindUtilities.isEnumForwardDeclaration(object)) {
            return 1*10 + 0;
        } else if(CsmKindUtilities.isEnum(object)) {
            return 1*10+1;
        } else if(CsmKindUtilities.isTypedef(object)) {
            return 1*10+2;
        } else if(CsmKindUtilities.isVariableDeclaration(object)) {
            return 2*10+0;
        } else if(CsmKindUtilities.isVariableDefinition(object)) {
            return 2*10+1;
        } else if(CsmKindUtilities.isFunctionDeclaration(object)) {
            return FUNCTION_DECLARATION_WEIGHT;//3*10+0;
        } else if(CsmKindUtilities.isFunctionDefinition(object)) {
            return FUNCTION_DEFINITION_WEIGHT;//3*10+1;
        } else if(CsmKindUtilities.isMacro(object)) {
            return 4*10+0;
        } else if(CsmKindUtilities.isInclude(object)) {
            return INCLUDE_WEIGHT;//5*10+0;
        }
        return 9*10+0;
    }

    @Override
    public CsmObject getCsmObject() {
        if (CsmKindUtilities.isCsmObject(object)) {
            return object;
        }
        return null;
    }

    int getOffset() {
        if (CsmKindUtilities.isOffsetable(object)) {
            return ((CsmOffsetable)object).getStartOffset();
        } else {
            return 0;
        }
    }

    void resetNode(CppDeclarationNode node){
        if (object != node.object) {
            ic.remove(object);
            object = node.object;
            ic.add(object);
        }
        if (object instanceof CsmFile) {
            file = (CsmFile) object;
        } else {
            file = ((CsmOffsetable)object).getContainingFile();
        }
        weight = node.weight;
        scopeName = node.scopeName;
        definitionScopeName = node.definitionScopeName;
        isFriend = node.isFriend;
        isSpecialization = node.isSpecialization;
        needInitHTML = node.needInitHTML;
        htmlDisplayName = node.htmlDisplayName;
        icon = node.icon;
        fireDisplayNameChange(null, null);
        fireIconChange();
    }
    
    CharSequence getScopeName() {
        return scopeName;
    }

    CharSequence getMethodDefinitionScopeName() {
        return definitionScopeName;
    }
    
    @Override
    public int compareTo(CppDeclarationNode o) {
        int res = compareToWithoutOffset(o);
        if (res == 0) {
            int start1 = 0;
            if (object instanceof CsmOffsetable) {
                start1 = ((CsmOffsetable)object).getStartOffset();
            }
            int start2 = 0;
            if (o.object instanceof CsmOffsetable) {
                start2 = ((CsmOffsetable)o.object).getStartOffset();
            }
            res = start1 - start2;
        }
        return res;
    }

    public int compareToWithoutOffset(CppDeclarationNode o) {
        int res = 0;
        switch(model.getFilter().getSortMode()) {
            case Name:
                res = CharSequences.comparator().compare(scopeName, o.scopeName);
                if (res == 0) {
                    if (model.getFilter().isGroupByKind()) {
                        res = weight/10 - o.weight/10;
                        if (res == 0) {
                            res = getDisplayName().compareTo(o.getDisplayName());
                            if (res == 0) {
                                res = weight - o.weight;
                            }
                        }
                    } else {
                        res = getDisplayName().compareTo(o.getDisplayName());
                        if (res == 0) {
                            res = weight - o.weight;
                        }
                    }
                }
                break;
            case Offset:
                if (model.getFilter().isGroupByKind()) {
                    res = weight/10 - o.weight/10;
                }
                break;
        }
        return res;
    }

    @Override
    public String getName() {
        return name.toString();
    }

    @Override
    public String getHtmlDisplayName() {
        CharSequence html = htmlDisplayName;
        if(needInitHTML) {
            html = createHtmlDisplayName();
            htmlDisplayName = html;
            needInitHTML = false;
        }
        if (html != null) {
            return html.toString();
        }
        return null;
    }

    private CharSequence createHtmlDisplayName() {
        try {
            final CsmObject csmObject = getCsmObject();
            if (CsmKindUtilities.isFunctionDefinition(csmObject)) {
                if (scopeName.length() > 0) {
                    if (isSpecialization) {
                        return createFunctionSpecializationHtmlDisplayName();
                    } else {
                        return createMemberHtmlDisplayName();
                    }
                }
            } else if (CsmKindUtilities.isVariableDefinition(csmObject)) {
                if (scopeName.length() > 0) {
                    return createMemberHtmlDisplayName();
                }
            } else if (csmObject instanceof CsmFile) {
                if (model.getUnopenedProject() != null) {
                    // unopened project
                    return CharSequences.create(new StringBuilder("<font color='").append(CsmDisplayUtilities.getHTMLColor(Color.red)).append(">") // NOI18N
                            .append(NbBundle.getMessage(CppDeclarationNode.class, "UnopenedProject",  // NOI18N
                            ProjectUtils.getInformation(model.getUnopenedProject()).getDisplayName())));
                } else {
                    //Restricted code assistance
                    return CharSequences.create(new StringBuilder("<font color='").append(CsmDisplayUtilities.getHTMLColor(Color.red)).append(">") // NOI18N
                            .append(NbBundle.getMessage(CppDeclarationNode.class, "StandAloneFile"))); // NOI18N
                }
            } else if (CsmKindUtilities.isFunction(csmObject) && CsmKindUtilities.isSpecialization(csmObject)) {
                if (scopeName.length() > 0) {
                    if (isSpecialization) {
                        return createFunctionSpecializationHtmlDisplayName();
                    }
                }
            }

        } catch (AssertionError ex) {
            ex.printStackTrace(System.err);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        if (object instanceof CsmFile) {
            return " "+super.getDisplayName(); // NOI18N
        }
        return super.getDisplayName();
    }

    
    @Override
    public Image getIcon(int param) {
        if (icon != null){
            return icon;
        }
        if (file != null && !file.isValid()){
            if (object instanceof CsmOffsetable) {
                return super.superGetIcon(param);
            } else {
                return ImageUtilities.loadImage("org/netbeans/modules/cnd/qnavigator/resources/exclamation.gif");
            }
        }
        if (isFriend) {
            CsmFriend csmObj = (CsmFriend)object;
            return (csmObj == null) ? super.getIcon(param) : CsmImageLoader.getFriendFunctionImage(csmObj);
        } else {
            if (object instanceof CsmOffsetable) {
                return super.getIcon(param);
            } else {
                return ImageUtilities.loadImage("org/netbeans/modules/cnd/qnavigator/resources/exclamation.gif");
            }
        }
    }
    
    @Override
    public Image getOpenedIcon(int param) {
        return getIcon(param);
    }
    
    @Override
    public Action getPreferredAction() {
        if (CsmKindUtilities.isOffsetable(object)){
            return new GoToDeclarationAction((CsmOffsetable) object);
        } else if (object instanceof CsmFile) {
            Project project = model.getUnopenedProject();
            if (project != null) {
                return new OpenContainingProjectAction(project);
            }
        }
        return null;
    }

    private Action getGoToIncludedFileAction(CsmInclude include) {
        Action a = FileUtil.getConfigObject("Editors/text/x-c++/Actions/goto-declaration.instance", Action.class); //NOI18N
        if (a != null) {
            Object value = a.getValue("displayName"); //NOI18N
            if (value instanceof String) {
                return new GoToDefinitionAction((String)value, include);
            }
        }
        return null;
    }

    private Action getGoToDeclaration(CsmFunctionDefinition definition) {
        Action a = FileUtil.getConfigObject("Editors/text/x-c++/Actions/goto-declaration.instance", Action.class); //NOI18N
        if (a != null) {
            Object value = a.getValue("displayName"); //NOI18N
            if (value instanceof String) {
                return new GoToDefinitionAction((String)value, definition);
            }
        }
        return null;
    }

    private Action getGoToDefinition(CsmFunction declaration) {
        Action a = FileUtil.getConfigObject("Editors/text/x-c++/Actions/goto-declaration.instance", Action.class); //NOI18N
        if (a != null) {
            Object value = a.getValue("displayName"); //NOI18N
            if (value instanceof String) {
                return new GoToDefinitionAction((String)value, declaration);
            }
        }
        return null;
    }

    @Override
    public Action[] getActions(boolean context) {
        Action action = getPreferredAction();
        if (action != null){
            List<Action> list = new ArrayList<Action>();
            list.add(action);
            if (weight == INCLUDE_WEIGHT && CsmKindUtilities.isInclude(object)) {
                Action goToIncludedFileAction = getGoToIncludedFileAction((CsmInclude)object);
                if (goToIncludedFileAction != null) {
                    list.add(goToIncludedFileAction);
                }
            } else if (weight == FUNCTION_DECLARATION_WEIGHT && CsmKindUtilities.isFunction(object)) {
                Action goToDefinition = getGoToDefinition((CsmFunction)object);
                if (goToDefinition != null) {
                    list.add(goToDefinition);
                }
            } else if (weight == FUNCTION_DEFINITION_WEIGHT && CsmKindUtilities.isFunctionDefinition(object)) {
                Action goToDeclaration = getGoToDeclaration((CsmFunctionDefinition)object);
                if (goToDeclaration != null) {
                    list.add(goToDeclaration);
                }
            }
            if (CsmRefactoringActionsFactory.supportRefactoring(file)) {
                list.add(RefactoringActionsFactory.renameAction());
                list.add(RefactoringActionsFactory.whereUsedAction());
                CsmObject obj = this.getCsmObject();
                if (CsmKindUtilities.isField(obj) || CsmKindUtilities.isClass(obj)) {
                    list.add(CsmRefactoringActionsFactory.encapsulateFieldsAction());
                } else if (CsmKindUtilities.isFunction(obj) && !CsmKindUtilities.isDestructor(obj)) {
                    list.add(CsmRefactoringActionsFactory.changeParametersAction());
                }
            }
            list.add(null);
            list.addAll(Arrays.asList(model.getActions()));
            return list.toArray(new Action[list.size()]);
        }
        return model.getActions();
    }

    public static CppDeclarationNode nodeFactory(CsmObject element, CsmFileModel model, boolean isFriend, List<IndexOffsetNode> lineNumberIndex, AtomicBoolean canceled){
        if (canceled.get()) {
            return null;
        }
        if (!(element instanceof CsmFile)) {
            if (!model.getFilter().isApplicable((CsmOffsetable)element)){
                return null;
            }
        }
        CppDeclarationNode node = null;
        if (CsmKindUtilities.isTypedef(element)){
            CsmTypedef def = (CsmTypedef) element;
            if (def.isTypeUnnamed()) {
                CsmClassifier cls = def.getType().getClassifier();
                if (cls != null && cls.getName().length()==0 &&
                   (cls instanceof CsmCompoundClassifier)) {
                    node = createClassifierContainer((CsmOffsetableDeclaration)element, model, (CsmCompoundClassifier) cls, lineNumberIndex, canceled);
                    node.name = ((CsmDeclaration)element).getName();
                    return node;
                }
            }
            node = createDeclaration((CsmOffsetableDeclaration)element,model,isFriend);
            node.name = ((CsmDeclaration)element).getName();
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        } else if (CsmKindUtilities.isClassifier(element)){
            CharSequence name = ((CsmClassifier)element).getName();
            if (name.length()==0 && (element instanceof CsmCompoundClassifier)) {
                Collection<CsmTypedef> list = ((CsmCompoundClassifier)element).getEnclosingTypedefs();
                if (list.size() > 0) {
                    return null;
                }
            }
            if (CsmKindUtilities.isTemplate(element)) {
                if (((CsmTemplate)element).isExplicitSpecialization()) {
                    return null;
                }
            }
            if (CsmKindUtilities.isClassForwardDeclaration(element) || CsmKindUtilities.isEnumForwardDeclaration(element)) {
                node = createObject((CsmOffsetableDeclaration)element, model);
            } else {
                node = createNamespaseContainer((CsmOffsetableDeclaration)element, model,lineNumberIndex, canceled);
            }
            node.name = getClassifierName((CsmClassifier)element);
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        } else if(CsmKindUtilities.isNamespaceDefinition(element)){
            node = createNamespaseContainer((CsmNamespaceDefinition)element, model, lineNumberIndex, canceled);
            node.name = ((CsmNamespaceDefinition)element).getName();
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        } else if(CsmKindUtilities.isDeclaration(element)){
            if(CsmKindUtilities.isFunction(element)){
                node = createDeclaration((CsmOffsetableDeclaration)element,model,isFriend);
                String abstractDecorator = ""; //NOI18N
                if (CsmKindUtilities.isMethodDeclaration(element) && ((CsmMethod)element).isAbstract()) {
                    abstractDecorator = " =0"; //NOI18N
                }
                node.name = CharSequences.create(CsmUtilities.getSignature((CsmFunction)element, true)+abstractDecorator);
            } else if(CsmKindUtilities.isFunctionExplicitInstantiation(element)) {
                return null;
            } else {
                CharSequence name = ((CsmDeclaration)element).getName();
                if (name.length() == 0 && CsmKindUtilities.isVariable(element)){
                    return node;
                }
                node = createDeclaration((CsmOffsetableDeclaration)element,model,isFriend);
                node.name = name;
            }
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        } else if(CsmKindUtilities.isEnumerator(element)){
            node = createObject((CsmEnumerator)element,model);
            node.name = ((CsmEnumerator)element).getName();
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        } else if (CsmKindUtilities.isErrorDirective(element)) {
            node = createObject((CsmErrorDirective) element, model);
            node.name = ((CsmErrorDirective) element).getErrorMessage();
            model.addOffset(node, (CsmOffsetable) element, lineNumberIndex);
            return node;            
        } else if(CsmKindUtilities.isMacro(element)){
            node = createObject((CsmMacro)element,model);
            node.name = ((CsmMacro)element).getName();
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        } else if(element instanceof CsmInclude){
            node = createObject((CsmInclude)element,model);
            node.name = ((CsmInclude)element).getIncludeName();
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        } else if(element instanceof CsmFile) {
            node = createFile((CsmFile)element,model);
            node.name = ((CsmFile)element).getName();
            model.addFileOffset(node, (CsmFile)element, lineNumberIndex);
            return node;
        }
        return node;
    }
}
