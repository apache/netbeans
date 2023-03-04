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
package org.netbeans.modules.web.beans.navigation.actions;


import java.awt.Toolkit;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import javax.swing.JDialog;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.api.model.BeansModel;
import org.netbeans.modules.web.beans.api.model.InterceptorsResult;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.navigation.BindingsPanel;
import org.netbeans.modules.web.beans.navigation.DecoratorsModel;
import org.netbeans.modules.web.beans.navigation.DecoratorsPanel;
import org.netbeans.modules.web.beans.navigation.EventsModel;
import org.netbeans.modules.web.beans.navigation.EventsPanel;
import org.netbeans.modules.web.beans.navigation.InjectablesModel;
import org.netbeans.modules.web.beans.navigation.InterceptorsModel;
import org.netbeans.modules.web.beans.navigation.InterceptorsPanel;
import org.netbeans.modules.web.beans.navigation.ObserversModel;
import org.netbeans.modules.web.beans.navigation.ObserversPanel;
import org.netbeans.modules.web.beans.navigation.ResizablePopup;
import org.netbeans.modules.web.beans.navigation.actions.ModelActionStrategy.InspectActionId;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;

/**
 * @author ads
 *
 */
public class WebBeansActionHelper {
    
    private static final String WAIT_NODE = "LBL_WaitNode";     // NOI18N

    static final String DELEGATE = "javax.decorator.Delegate";  // NOI18N

    static final String DECORATOR = "javax.decorator.Decorator";// NOI18N

    static final String FIRE = "fire";                          // NOI18N
    
    static final String EVENT_INTERFACE = 
        "javax.enterprise.event.Event";                         // NOI18N
    
    static final String OBSERVES_ANNOTATION = 
        "javax.enterprise.event.Observes";                      // NOI18N
    
    private static final Set<JavaTokenId> USABLE_TOKEN_IDS = 
        EnumSet.of(JavaTokenId.IDENTIFIER, JavaTokenId.THIS, JavaTokenId.SUPER);
    
    private static final PositionStrategy CARET_POSITION_STRATEGY = new PositionStrategy() {
        
        @Override
        public int getOffset( JTextComponent component ) {
            return component.getCaret().getDot();
        }
    };

    private WebBeansActionHelper(){
    }
    
    public static boolean isEnabled() {
        JTextComponent c = EditorRegistry.lastFocusedComponent();
        if (c == null || !c.isShowing())
        {
            return false;
        }
        if ( OpenProjects.getDefault().getOpenProjects().length == 0 ){
            return false;
        }
        final FileObject fileObject = NbEditorUtilities.getFileObject(c.getDocument());
        return isEnabled( fileObject );
        /*WebModule webModule = WebModule.getWebModule(fileObject);
        if ( webModule == null ){
            return false;
        }
        Profile profile = webModule.getJ2eeProfile();
        return Profile.JAVA_EE_6_WEB.equals( profile) || 
            Profile.JAVA_EE_6_FULL.equals( profile );*/
    }
    
    public static boolean isEnabled(FileObject fileObject){
        if ( fileObject == null ){
            return false;
        }
        Project project = FileOwnerQuery.getOwner( fileObject );
        if ( project == null ){
            return false;
        }
        boolean hasJsr330 = hasJsr330(project);
        if ( !hasJsr330 ){
            return false;
        }
        if ( !hasJsr299(project) ){
            return false;
        }
        return true;
    }
    
    public static boolean hasJsr330( Project project ){
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).
            getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups.length == 0) {
            return false;
        }
        boolean hasInject = false;
        boolean hasQualifier = false;
        for (SourceGroup sourceGroup : sourceGroups) {
            boolean injectFound = hasResource(sourceGroup, ClassPath.COMPILE, 
                    AnnotationUtil.INJECT_FQN) ||
                hasResource(sourceGroup, ClassPath.SOURCE, AnnotationUtil.INJECT_FQN);
            if ( injectFound ){
                hasInject = true;
            }
            boolean qualifierFound = hasResource(sourceGroup, ClassPath.COMPILE, 
                    AnnotationUtil.QUALIFIER_FQN) ||
                hasResource(sourceGroup, ClassPath.SOURCE, AnnotationUtil.QUALIFIER_FQN);
            if ( qualifierFound ){
                hasQualifier = true;
            }
        }
        
        return hasInject && hasQualifier;
    }
    
    public static boolean hasJsr299( Project project ){
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).
            getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups.length == 0) {
            return false;
        }
        boolean hasDefault = false;
        boolean hasProduces = false;
        boolean hasDependent = false;
        for (SourceGroup sourceGroup : sourceGroups) {
            boolean defaultFound = hasResource(sourceGroup, ClassPath.COMPILE, 
                    AnnotationUtil.DEFAULT_FQN) ||
                hasResource(sourceGroup, ClassPath.SOURCE, AnnotationUtil.DEFAULT_FQN);
            if ( defaultFound ){
                hasDefault = true;
            }
            boolean producesFound = hasResource(sourceGroup, ClassPath.COMPILE, 
                    AnnotationUtil.PRODUCES_FQN) ||
                hasResource(sourceGroup, ClassPath.SOURCE, AnnotationUtil.PRODUCES_FQN);
            if ( producesFound ){
                hasProduces = true;
            }
            boolean dependentFound = hasResource(sourceGroup, ClassPath.COMPILE, 
                    AnnotationUtil.DEPENDENT) ||
                hasResource(sourceGroup, ClassPath.SOURCE, AnnotationUtil.DEPENDENT);
            if ( dependentFound ){
                hasDependent = true;
            }
        }
        
        return hasDefault && hasProduces &&  hasDependent;
    }
    
    static boolean hasResource(SourceGroup group , String classPathType, String fqn){
        ClassPath classPath = ClassPath.getClassPath(group.getRootFolder(), classPathType);
        String path = fqn.replace('.', '/');
        if ( ClassPath.SOURCE.equals( classPathType ) ){
            path = path+".java";                    // NOI18N
        }
        else {
            path = path+".class";                   // NOI18N
        }
        if (classPath == null) {
            return false;
        }
        
        return classPath.findResource(path) != null;
    }
    
    /**
     * Compilation controller from metamodel could not be used for getting 
     * TreePath via dot because it is not based on one FileObject ( Document ).
     * So this method is required for searching Element at dot.
     * If appropriate element is found it's name is placed into list 
     * along with name of containing type.
     * Resulted element could not be used in metamodel for injectable
     * access. This is because element was gotten via other Compilation
     * controller so it is from other model.
     */
    static boolean getVariableElementAtDot( final JTextComponent component,
            final Object[] variable , final boolean showStatusOnError) 
    {
        return getVariableElementAtDot(component, variable, showStatusOnError, 
                CARET_POSITION_STRATEGY);
    }
    
    /**
     * Compilation controller from metamodel could not be used for getting 
     * TreePath via dot because it is not based on one FileObject ( Document ).
     * So this method is required for searching Element at dot.
     * If appropriate element is found it's name is placed into list 
     * along with name of containing type.
     * Resulted element could not be used in metamodel for injectable
     * access. This is because element was gotten via other Compilation
     * controller so it is from other model.
     */
    static boolean getVariableElementAtDot( final JTextComponent component,
            final Object[] variable , final boolean showStatusOnError, 
            final PositionStrategy strategy) 
    {
        
        JavaSource javaSource = JavaSource.forDocument(component.getDocument());
        if ( javaSource == null ){
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
        try {
            javaSource.runUserActionTask(  new Task<CompilationController>(){
                @Override
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase( Phase.ELEMENTS_RESOLVED );
                    int dot = strategy.getOffset(component);
                    TreePath tp = controller.getTreeUtilities().pathFor(dot);
                    Element contextElement = controller.getTrees().getElement(tp );
                    if ( contextElement == null ){
                        StatusDisplayer.getDefault().setStatusText(
                                NbBundle.getMessage(
                                        WebBeansActionHelper.class, 
                                "LBL_ElementNotFound"));
                        return;
                    }
                    Element element = getContextElement(contextElement, controller);
                    if ( element == null ){
                        return;
                    }
                    if ( !( element instanceof VariableElement) && showStatusOnError){
                        StatusDisplayer.getDefault().setStatusText(
                                NbBundle.getMessage(
                                WebBeansActionHelper.class, 
                                "LBL_NotVariableElement",
                                StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT));
                        return;
                    }
                    else {
                        if ( element.getKind() == ElementKind.FIELD ){
                            ElementHandle<VariableElement> handle = 
                                ElementHandle.create((VariableElement)element);
                            variable[0] = handle;
                            variable[1] = element.getSimpleName().toString();
                            variable[2] = InspectActionId.INJECTABLES_CONTEXT;
                        }
                        else {
                            setVariablePath(variable, controller, element);
                        }
                    }
                }
            }, true );
        }
        catch(IOException e ){
            Logger.getLogger( GoToInjectableAtCaretAction.class.getName()).
                log( Level.INFO, e.getMessage(), e);
        }
        return variable[1] !=null ;
    }
    
    /**
     * Based on the context element method chooses different element.
     * If element is not "injection" ( not injection point and has no
     * injection context ) method sets the error message in the status 
     * and return null.
     */
    static Element getContextElement(Element element , 
            CompilationController controller )
    {
        if ( element instanceof TypeElement ){
            if ( hasAnnotation(element, DECORATOR) ){
                List<VariableElement> fieldsIn = ElementFilter.fieldsIn( 
                        controller.getElements().getAllMembers((TypeElement)element));
                for (VariableElement variableElement : fieldsIn) {
                    if ( hasAnnotation(variableElement, DELEGATE)){
                        return variableElement;
                    }
                }
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(
                                WebBeansActionHelper.class, 
                        "LBL_NotDecorator"));
            }
            return null;
        }
        return element;
    }
    
    static boolean hasAnnotation(Element element,String fqn){
        List<? extends AnnotationMirror> annotationMirrors = 
            element.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            DeclaredType annotationType = annotationMirror.getAnnotationType();
            Element annotationElement = annotationType.asElement();
            if ( annotationElement instanceof TypeElement ){
                Name qualifiedName = ((TypeElement)annotationElement).
                    getQualifiedName();
                if ( qualifiedName.contentEquals(fqn)){
                    return true;
                }
            }
        }
        return false;
    }
    
    static boolean getClassAtDot(
            final JTextComponent component , final Object[] subject )
    {
        return getClassAtDot(component, subject, CARET_POSITION_STRATEGY);
    }
    
    static boolean getClassAtDot(
            final JTextComponent component , final Object[] subject, 
            final PositionStrategy strategy )
    {
        JavaSource javaSource = JavaSource.forDocument(component.getDocument());
        if ( javaSource == null ){
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
        try {
            javaSource.runUserActionTask( new Task<CompilationController>(){
                @Override
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase( Phase.ELEMENTS_RESOLVED );
                    int dot = strategy.getOffset(component);
                    TreePath tp = controller.getTreeUtilities()
                        .pathFor(dot);
                    Element element = controller.getTrees().getElement(tp );
                    if ( element == null ){
                        StatusDisplayer.getDefault().setStatusText(
                                NbBundle.getMessage(
                                        WebBeansActionHelper.class, 
                                "LBL_ElementNotFound"));
                        return;
                    }
                    if ( element instanceof TypeElement ){
                        subject[0] = ElementHandle.create(element);
                        subject[1] = element.getSimpleName();
                        subject[2] = InspectActionId.CLASS_CONTEXT;
                    }
                }
            }, true );
        }
        catch(IOException e ){
            Logger.getLogger( WebBeansActionHelper.class.getName()).
                log( Level.WARNING, e.getMessage(), e);
        }
                    
        return subject[0]!=null;
    }
    
    static boolean getMethodAtDot(
            final JTextComponent component , final Object[] subject , 
            final PositionStrategy strategy)
    {
        JavaSource javaSource = JavaSource.forDocument(component.getDocument());
        if ( javaSource == null ){
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
        try {
            javaSource.runUserActionTask( new Task<CompilationController>(){
                @Override
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase( Phase.ELEMENTS_RESOLVED );
                    int dot = strategy.getOffset(component);
                    TreePath tp = controller.getTreeUtilities()
                        .pathFor(dot);
                    Element element = controller.getTrees().getElement(tp );
                    if ( element == null ){
                        StatusDisplayer.getDefault().setStatusText(
                                NbBundle.getMessage(
                                        WebBeansActionHelper.class, 
                                "LBL_ElementNotFound"));
                        return;
                    }
                    if ( element instanceof ExecutableElement ){
                        subject[0] = ElementHandle.create(element);
                        subject[1] = element.getSimpleName();
                        subject[2] = InspectActionId.METHOD_CONTEXT;
                    }
                    else if ( element instanceof VariableElement ){
                        Element enclosingElement = element.getEnclosingElement();
                        if ( enclosingElement instanceof ExecutableElement && 
                                hasAnnotation(element, OBSERVES_ANNOTATION))
                        {
                            subject[0] = ElementHandle.create(enclosingElement);
                            subject[1] = enclosingElement.getSimpleName();
                            subject[2] = InspectActionId.METHOD_CONTEXT;
                        }
                    }
                }
            }, true );
        }
        catch(IOException e ){
            Logger.getLogger( WebBeansActionHelper.class.getName()).
                log( Level.WARNING, e.getMessage(), e);
        }
                    
        return subject[0]!=null;
    }
    
    static boolean getMethodAtDot(
            final JTextComponent component , final Object[] subject )
    {
        return getMethodAtDot(component, subject, CARET_POSITION_STRATEGY );
    }
    
    public static boolean getContextEventInjectionAtDot(
            final JTextComponent component, final Object[] variable , 
            final PositionStrategy strategy )
    {
        try {
            ParserManager.parse(Collections.singleton (Source.create(
                    component.getDocument())), new UserTask() 
            {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Result resuslt = resultIterator.getParserResult (
                            strategy.getOffset(component));
                    CompilationController controller = CompilationController.get(
                            resuslt);
                    if (controller == null || controller.toPhase(Phase.RESOLVED).
                            compareTo(Phase.RESOLVED) < 0)
                    {
                        return;
                    }
                    Token<JavaTokenId>[] token = new Token[1];
                    int[] span = getIdentifierSpan( component.getDocument(), 
                            strategy.getOffset(component), token);

                    if (span == null) {
                        return ;
                    }

                    int exactOffset = controller.getSnapshot().
                        getEmbeddedOffset(span[0] + 1);
                    TreePath path = controller.getTreeUtilities().pathFor(exactOffset);
                    TreePath parent = path.getParentPath();
                    if (parent != null) {
                        Tree parentLeaf = parent.getLeaf();
                        if ( parentLeaf.getKind() == Kind.METHOD_INVOCATION){
                            ExpressionTree select = ((MethodInvocationTree)parentLeaf).
                                getMethodSelect();
                            /*
                             *  Identifier case should be ignored because in this case
                             *  method is called on 'this' instance . Which is never
                             *  managed by J2EE container as Event injectable.
                             */
                            if ( select.getKind() == Kind.MEMBER_SELECT ){
                                Scope scope = controller.getTrees().getScope(path);
                                Element subjectClass = scope.getEnclosingClass();
                                Element method = controller.getTrees().getElement(
                                        new TreePath(path, select));
                                Element caller = controller.getTrees().getElement(
                                        new TreePath(path, ((MemberSelectTree)select).getExpression()));
                                String methodName = method.getSimpleName().toString();
                                if ( FIRE.equals( methodName) && 
                                        method instanceof ExecutableElement  &&
                                        caller instanceof VariableElement )
                                {
                                    String variableName = caller.getSimpleName().toString();
                                    TypeElement enclosingTypeElement = 
                                        controller.getElementUtilities().
                                        enclosingTypeElement( method);
                                    String fqnMethodClass = enclosingTypeElement.
                                        getQualifiedName().toString();
                                    if( EVENT_INTERFACE.equals(fqnMethodClass)){
                                        List<VariableElement> fields = 
                                            ElementFilter.fieldsIn
                                            ( controller.getElements().getAllMembers(
                                                (TypeElement)subjectClass));
                                        for (VariableElement var :  fields) {
                                            String varName = var.getSimpleName().toString();
                                            if ( variableName.equals( varName )){
                                                ElementHandle<VariableElement> handle = 
                                                    ElementHandle.create(var);
                                                variable[0]= handle;
                                                variable[1]= varName;   
                                                variable[2]= InspectActionId.OBSERVERS_CONTEXT;  
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
        catch (ParseException e) {
            throw new IllegalStateException(e);
        }
        return variable[1] !=null ;
    }
    
    public static boolean getContextEventInjectionAtDot(
            final JTextComponent component, final Object[] variable )
    {
        return getContextEventInjectionAtDot(component, variable, CARET_POSITION_STRATEGY);
    }
    
    static void showInjectablesDialog( MetadataModel<WebBeansModel> metamodel,
            WebBeansModel model, Object[] subject, 
            InjectablesModel uiModel , String name , 
            org.netbeans.modules.web.beans.api.model.Result result ) 
    {
        subject[2] = InspectActionId.INJECTABLES_CONTEXT;
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                InjectablesModel.class, WAIT_NODE));           // NOI18N
        JDialog dialog = ResizablePopup.getDialog();
        String title = NbBundle.getMessage(WebBeansActionHelper.class,
                "TITLE_Injectables" , name );//NOI18N
        dialog.setTitle( title );
        dialog.setContentPane( new BindingsPanel(subject, metamodel, model,
                uiModel, result ));
        dialog.setVisible( true );
    }
    
    static void showEventsDialog( MetadataModel<WebBeansModel> metaModel , 
            WebBeansModel model,Object[] subject, 
            EventsModel uiModel , String name ) 
    {
        subject[2] = InspectActionId.METHOD_CONTEXT;
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                InjectablesModel.class, WAIT_NODE));
        JDialog dialog = ResizablePopup.getDialog();
        String title = NbBundle.getMessage(WebBeansActionHelper.class,
                "TITLE_Events" , name );//NOI18N
        dialog.setTitle( title );
        dialog.setContentPane( new EventsPanel(subject, metaModel, 
                model ,uiModel ));
        dialog.setVisible( true );
    }
    
    static void showObserversDialog( List<ExecutableElement> methods , 
            MetadataModel<WebBeansModel> metaModel , WebBeansModel model,
            Object[] subject, ObserversModel uiModel ,
            String name ) 
    {
        subject[2] = InspectActionId.OBSERVERS_CONTEXT;
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                InjectablesModel.class, WAIT_NODE));
        JDialog dialog = ResizablePopup.getDialog();
        String title = NbBundle.getMessage(WebBeansActionHelper.class,
                "TITLE_Observers" , name );//NOI18N
        dialog.setTitle( title );
        dialog.setContentPane( new ObserversPanel(subject, metaModel, 
                model ,uiModel ));
        dialog.setVisible( true );
        
    }
    
    public static void showDecoratorsDialog(
            MetadataModel<WebBeansModel> metaModel, WebBeansModel model,
            Object[] subject, DecoratorsModel uiModel, String name )
    {
        subject[2] = InspectActionId.CLASS_CONTEXT;
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                InjectablesModel.class, WAIT_NODE));
        JDialog dialog = ResizablePopup.getDialog();
        String title = NbBundle.getMessage(WebBeansActionHelper.class,
                "TITLE_Decorators" , name );//NOI18N
        dialog.setTitle( title );
        dialog.setContentPane( new DecoratorsPanel(subject, metaModel, 
                model ,uiModel ));
        dialog.setVisible( true );             
    }
    
    static void showInterceptorsDialog(
            MetadataModel<WebBeansModel> metaModel, WebBeansModel model,
            Object[] subject, InterceptorsModel uiModel, String name , 
            InterceptorsResult result )
    {
        subject[2] = InspectActionId.CLASS_CONTEXT;
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                InjectablesModel.class, WAIT_NODE));
        JDialog dialog = ResizablePopup.getDialog();
        String title = NbBundle.getMessage(WebBeansActionHelper.class,
                "TITLE_Interceptors" , name );//NOI18N
        dialog.setTitle( title );
        dialog.setContentPane( new InterceptorsPanel(subject, metaModel, 
                model ,uiModel , result));
        dialog.setVisible( true );        
    }
    
    public static LinkedHashSet<TypeElement> getEnabledDecorators( 
            Collection<TypeElement> decorators,BeansModel beansModel, 
            LinkedHashSet<ElementHandle<TypeElement>> enabledHandles,
            CompilationController controller)
            
    {
        LinkedHashSet<TypeElement> enabled = new LinkedHashSet<TypeElement>();
        
        Set<TypeElement> foundDecorators = new HashSet<TypeElement>( decorators );
        LinkedHashSet<String> decoratorClasses = beansModel.getDecoratorClasses();
        for (String decorator : decoratorClasses) {
            TypeElement enabledDecorator = controller.getElements().
                getTypeElement( decorator );
            if ( foundDecorators.contains(enabledDecorator) ){
                enabled.add( enabledDecorator );
                if ( enabledHandles!= null){
                    enabledHandles.add( ElementHandle.create( enabledDecorator));
                }
            }
        }
        return enabled;
    }
    
    public static VariableElement findVariable( final WebBeansModel model,
            final Object[] variablePath )
    {
        if ( variablePath[0] == null ){
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                    WebBeansActionHelper.class, 
                    "LBL_VariableNotFound", variablePath[1]));
            return null ;
        }
        Element element = ((ElementHandle<?>)variablePath[0]).resolve(
                model.getCompilationController());
        if ( element == null ){
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                    WebBeansActionHelper.class, 
                    "LBL_VariableNotFound", variablePath[1]));
            return null ;
        }
        VariableElement var = null;
        ExecutableElement method = null;
        if ( element.getKind() == ElementKind.FIELD){
            var = (VariableElement)element;
        }
        else {
            method = (ExecutableElement)element;
            List<? extends VariableElement> parameters = method.getParameters();
            for (VariableElement variableElement : parameters) {
                if (variableElement.getSimpleName().contentEquals(
                        variablePath[1].toString())) 
                {
                    var = variableElement;
                }
            }
        }
        
        if (var == null) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                    WebBeansActionHelper.class, 
                    "LBL_VariableNotFound", variablePath[1]));
        }
        return var;
    }

    static int[] getIdentifierSpan(Document doc, int offset, Token<JavaTokenId>[] token) {
        FileObject fileObject = NbEditorUtilities.getFileObject( doc);
        if (fileObject== null) {
            //do nothing if FO is not attached to the document - the goto would not work anyway:
            return null;
        }
        
        TokenHierarchy<?> th = TokenHierarchy.get(doc);
        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(th, offset);
        
        if (ts == null)
            return null;
        
        ts.move(offset);
        if (!ts.moveNext())
            return null;
        
        Token<JavaTokenId> t = ts.token();
        
        if (JavaTokenId.JAVADOC_COMMENT == t.id()) {
            return null;
        } else if (!USABLE_TOKEN_IDS.contains(t.id())) {
            ts.move(offset - 1);
            if (!ts.moveNext())
                return null;
            t = ts.token();
            if (!USABLE_TOKEN_IDS.contains(t.id()))
                return null;
        }
        
        if (token != null)
            token[0] = t;
        
        return new int [] {ts.offset(), ts.offset() + t.length()};
    }
    
    private static void setVariablePath( Object[] variableAtCaret,
            CompilationController controller, Element element )
    {
        Element parent = element.getEnclosingElement();
        if ( parent instanceof ExecutableElement ){
            ElementHandle<ExecutableElement> handle = ElementHandle.create( 
                    (ExecutableElement)parent ) ;
            variableAtCaret[0] = handle;
            variableAtCaret[1] = element.getSimpleName().toString();
            variableAtCaret[2] = InspectActionId.INJECTABLES_CONTEXT;
        }
    }

}
