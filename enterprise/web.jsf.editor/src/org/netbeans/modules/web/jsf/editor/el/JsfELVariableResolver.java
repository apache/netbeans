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
package org.netbeans.modules.web.jsf.editor.el;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.html.editor.lib.api.elements.ElementVisitor;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.el.ELTypeUtilities;
import org.netbeans.modules.web.el.spi.ELVariableResolver;
import org.netbeans.modules.web.el.spi.ELVariableResolver.VariableInfo;
import org.netbeans.modules.web.el.spi.ResolverContext;
import org.netbeans.modules.web.jsf.api.editor.JSFBeanCache;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.api.metamodel.ManagedProperty;
import org.netbeans.modules.web.jsf.editor.JsfSupportImpl;
import org.netbeans.modules.web.jsf.editor.JsfUtils;
import org.netbeans.modules.web.jsf.editor.index.CompositeComponentModel;
import org.netbeans.modules.web.jsf.editor.index.JsfPageModelFactory;
import org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.lookup.ServiceProvider;

/**
 * TODO: getRawObjectProperties() handling is a bit hacky - currently just the actual node
 * image is passed to the method which may represent either the raw object itself or
 * one of its properties. Ideally the actual Node should be passed - API change.
 *
 */
@ServiceProvider(service = org.netbeans.modules.web.el.spi.ELVariableResolver.class)
public final class JsfELVariableResolver implements ELVariableResolver {

    private static final String CONTENT_NAME = "JsfBeans"; //NOI18N

    private static final String OBJECT_NAME__CC = "cc"; //NOI18N
    private static final String ATTR_NAME__ATTRS = "attrs"; //NOI18N
    private static final String ATTR_NAME__ID = "id"; //NOI18N
    private static final String ATTR_NAME__RENDERED = "rendered"; //NOI18N

    private static final String ATTR_NAME = "name"; //NOI18N
    private static final String ATTR_TYPE = "type"; //NOI18N
    
    private static final VariableInfo VARIABLE_INFO__ATTRS = VariableInfo.createResolvedVariable(ATTR_NAME__ATTRS, Object.class.getName());
    private static final VariableInfo VARIABLE_INFO__ID = VariableInfo.createResolvedVariable(ATTR_NAME__ID, Object.class.getName());
    private static final VariableInfo VARIABLE_INFO__RENDERED = VariableInfo.createResolvedVariable(ATTR_NAME__RENDERED, Object.class.getName());
    
    @Override
    public FieldInfo getInjectableField(String beanName, FileObject target, ResolverContext context) {
        for (FacesManagedBean bean : getJsfManagedBeans(target, context)) {
            if (beanName.equals(bean.getManagedBeanName())) {
                return new FieldInfo(bean.getManagedBeanClass());
            }
        }
        return null;
    }

    @Override
    public String getBeanName(String clazz, FileObject target, ResolverContext context) {
        for (FacesManagedBean bean : getJsfManagedBeans(target, context)) {
            if (clazz.equals(bean.getManagedBeanClass())) {
                return bean.getManagedBeanName();
            }
        }
        return null;
    }

//    @Override
//    public String getReferredExpression(Snapshot snapshot, final int offset) {
//        List<JsfVariableContext> allJsfVariables = getAllJsfVariables(snapshot, offset);
//        return allJsfVariables.isEmpty() ? null : allJsfVariables.get(0).getResolvedExpression();
//    }
    
    @Override
    public List<VariableInfo> getManagedBeans(FileObject target, ResolverContext context) {
        List<FacesManagedBean> beans = getJsfManagedBeans(target, context);
        List<VariableInfo> result = new ArrayList<>(beans.size());
        for (FacesManagedBean bean : beans) {
            if(bean.getManagedBeanClass() != null && bean.getManagedBeanName() != null) {
                result.add(VariableInfo.createResolvedVariable(bean.getManagedBeanName(), bean.getManagedBeanClass()));
            }
        }
        return result;
    }

    @Override
    public List<VariableInfo> getVariables(Snapshot snapshot, final int offset, ResolverContext context) {
        List<JsfVariableContext> allJsfVariables = getAllJsfVariables(snapshot, offset);
        List<VariableInfo> result = new ArrayList<>(allJsfVariables.size());
        for (JsfVariableContext jsfVariable : allJsfVariables) {
            //gets the generated expression from the el variables chain, see the JsfVariablesModel for more info
            String expression = jsfVariable.getResolvedExpression();
            if (expression == null) {
                continue;
            }
            result.add(VariableInfo.createUnresolvedVariable(jsfVariable.getVariableName(), expression));
        }
        return result;
    }

    @Override
    public List<VariableInfo> getRawObjectProperties(String objectName, Snapshot snapshot, ResolverContext context) {
        List<VariableInfo> variables = new ArrayList<> (3);
        
        //composite component object's properties handling
        if(OBJECT_NAME__CC.equals(objectName)) { //NOI18N
            variables.add(VARIABLE_INFO__ID);
            variables.add(VARIABLE_INFO__RENDERED);
            variables.add(VARIABLE_INFO__ATTRS);
            proposeFromComponentType(snapshot, context, variables);
        } else if (ATTR_NAME__ATTRS.equals(objectName)) { //NOI18N
            variables.add(VARIABLE_INFO__ID);
            variables.add(VARIABLE_INFO__RENDERED);
            final JsfPageModelFactory modelFactory = JsfPageModelFactory.getFactory(CompositeComponentModel.Factory.class);
            assert modelFactory != null;
            final AtomicReference<CompositeComponentModel> ccModelRef = new AtomicReference<>();
            try {
                ParserManager.parse(Collections.singleton(snapshot.getSource()), new UserTask() {

                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        //one level - works only if xhtml is top level
                        Result parseResult = JsfUtils.getEmbeddedParserResult(resultIterator, "text/html"); //NOI18N
                        if (parseResult instanceof HtmlParserResult) {
                            ccModelRef.set((CompositeComponentModel) modelFactory.getModel((HtmlParserResult) parseResult));
                        }
                    }
                });

                CompositeComponentModel ccmodel = ccModelRef.get();
                if(ccmodel != null) {
                    //the page represents a composite component
                    Collection<Map<String, String>> allCCInterfaceAttrs = ccmodel.getExistingInterfaceAttributes();
                    for (Map<String, String> attrsMap : allCCInterfaceAttrs) {
                        String name = attrsMap.get(ATTR_NAME); //NOI18N
                        if (name == null) {
                            continue;
                        }
                        String clazz = attrsMap.get(ATTR_TYPE) == null ? Object.class.getName() : attrsMap.get(ATTR_TYPE); //NOI18N
                        variables.add(VariableInfo.createResolvedVariable(name, clazz));
                    }
                }
            } catch (ParseException e) {
                Exceptions.printStackTrace(e);
            }
        }

        return variables;
    }



    @Override
    public List<VariableInfo> getBeansInScope(String scope, Snapshot snapshot, ResolverContext context) {
        List<VariableInfo> result = new ArrayList<>();
        for (FacesManagedBean bean : getJsfManagedBeans(snapshot.getSource().getFileObject(), context)) {
            if(bean.getManagedBeanClass() != null && bean.getManagedBeanName() == null) {
                if (scope.equals(bean.getManagedBeanScopeString())) {
                    result.add(VariableInfo.createResolvedVariable(bean.getManagedBeanName(), bean.getManagedBeanClass()));
                }
            }
        }
        return result;
    }

    private List<FacesManagedBean> getJsfManagedBeans(FileObject target, ResolverContext context) {
        List<FacesManagedBean> result = new ArrayList<>();
        Project project = FileOwnerQuery.getOwner(target);
        if (project == null) {
            return result;
        } else {
            if (context.getContent(CONTENT_NAME) == null) {
                context.setContent(CONTENT_NAME, JSFBeanCache.getBeans(project));
            }
            List<FacesManagedBean> beans = (List<FacesManagedBean>) context.getContent(CONTENT_NAME);
            result.addAll(beans);

            // issue #225844 - get beans defined via ui:param tag
            result.addAll(getFaceletParameters(target, beans));

            return result;
        }
    }

    private List<JsfVariableContext> getAllJsfVariables(Snapshot snapshot, final int offset) {
        final List<JsfVariableContext> result = new ArrayList<>();
        try {
            ParserManager.parse(Collections.singleton(snapshot.getSource()), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    //one level - works only if xhtml is top level
                    Result parseResult = JsfUtils.getEmbeddedParserResult(resultIterator, "text/html"); //NOI18N
                    if (parseResult instanceof HtmlParserResult) {
                        JsfVariablesModel model = JsfVariablesModel.getModel((HtmlParserResult) parseResult, resultIterator.getSnapshot());
                        List<JsfVariableContext> contexts = model.getAllAvailableVariables(offset, false);
                        result.addAll(contexts);
                    }
                }
            });
        } catch (ParseException e) {
            Exceptions.printStackTrace(e);
        }
        return result;
    }

    private static Collection<? extends FacesManagedBean> getFaceletParameters(FileObject target, final List<FacesManagedBean> managedBeans) {
        final List<FacesManagedBean> result = new ArrayList<>(managedBeans);
        try {
            ParserManager.parse(Arrays.asList(Source.create(target)), new UserTask() {
                @Override
                public void run(final ResultIterator resultIterator) throws Exception {
                    for (Embedding e : resultIterator.getEmbeddings()) {
                        if (e.getMimeType().equals("text/html")) { //NOI18N
                            final HtmlParserResult parserResult = (HtmlParserResult) resultIterator.getResultIterator(e).getParserResult();
                            if (parserResult == null) {
                                continue;
                            }
                            Node root = DefaultLibraryInfo.FACELETS.getValidNamespaces().stream()
                                    .map(parserResult::root)
                                    .filter(Objects::nonNull)
                                    .findFirst()
                                    .orElse(null);
                            ElementUtils.visitChildren(root, new ElementVisitor() {
                                @Override
                                public void visit(Element node) {
                                    OpenTag ot = (OpenTag) node;
                                    if (LexerUtils.equals("param", ot.unqualifiedName(), true, true)) { //NOI18N
                                        Attribute nameAttr = ot.getAttribute("name");   //NOI18N
                                        Attribute valueAttr = ot.getAttribute("value"); //NOI18N
                                        if (nameAttr != null && valueAttr != null) {
                                            int doc_from = parserResult.getSnapshot().getOriginalOffset(valueAttr.valueOffset());
                                            int doc_to = parserResult.getSnapshot().getOriginalOffset(valueAttr.valueOffset() + valueAttr.value().length());
                                            if (doc_from == -1 || doc_to == -1 || doc_from > doc_to) {
                                                return;
                                            }
                                            CharSequence topLevelSnapshotText = resultIterator.getSnapshot().getText();
                                            String documentValueContent = topLevelSnapshotText.subSequence(doc_from, doc_to).toString();
                                            for (FacesManagedBean managedBean : managedBeans) {
                                                if (documentValueContent.contains(managedBean.getManagedBeanName())) {
                                                    result.add(new ParamDefinedManagedBean(managedBean, (String) nameAttr.unquotedValue()));
                                                }
                                            }
                                        }
                                    }
                                }
                            }, ElementType.OPEN_TAG);
                        }
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }

    private static void proposeFromComponentType(final Snapshot snapshot, ResolverContext context, final List<VariableInfo> variables) {
        try {
            ParserManager.parse(Collections.singleton(snapshot.getSource()), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Result parseResult = JsfUtils.getEmbeddedParserResult(resultIterator, "text/html"); //NOI18N
                    if (parseResult instanceof HtmlParserResult) {
                        HtmlParserResult result = (HtmlParserResult) parseResult;
                        Node root = DefaultLibraryInfo.COMPOSITE.getValidNamespaces().stream()
                                    .map(result::root)
                                    .filter(Objects::nonNull)
                                    .findFirst()
                                    .orElse(null);
                        Collection<Element> children = root.children(ElementType.OPEN_TAG);
                        for (Element child : children) {
                            OpenTag ot = (OpenTag) child;
                            if ("interface".equals(ot.unqualifiedName())) { //NOI18N
                                final Attribute attribute = ot.getAttribute("componentType"); //NOI18N
                                if (attribute != null) {
                                    JavaSource js = JavaSource.create(ClasspathInfo.create(snapshot.getSource().getFileObject()));
                                    if (js != null) {
                                        js.runUserActionTask(new Task<CompilationController>() {
                                            @Override
                                            public void run(CompilationController parameter) throws Exception {
                                                parameter.toPhase(JavaSource.Phase.RESOLVED);
                                                TypeElement element = parameter.getElements().getTypeElement(attribute.unquotedValue());
                                                if (element != null) {
                                                    proposeJavaMethodsForElements(parameter, element, variables);
                                                }
                                            }
                                        }, true);
                                    }
                                }
                            }
                        }
                    }
                }
            });
        } catch (ParseException e) {
            Exceptions.printStackTrace(e);
        }
    }

    private static void proposeJavaMethodsForElements(CompilationController info, javax.lang.model.element.Element element, List<VariableInfo> variables) {
        for (ExecutableElement enclosed : ElementFilter.methodsIn(element.getEnclosedElements())) {
            //do not propose Object's members
            if(element.getSimpleName().contentEquals("Object")) { //NOI18N
                //XXX not an ideal non-fqn check
                continue;
            }

            if (!enclosed.getModifiers().contains(Modifier.PUBLIC) ||
                    enclosed.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }
            boolean hasParameters = !enclosed.getParameters().isEmpty();

            String methodName = enclosed.getSimpleName().toString();
            String propertyName = getPropertyName(methodName, enclosed.getReturnType(), true);
            if (hasParameters) {
                propertyName = methodName;
            }
            variables.add(VariableInfo.createResolvedVariable(propertyName, enclosed.getReturnType().toString()));
        }
    }

    public static String getPropertyName(String accessor, TypeMirror returnType, boolean includeSetter) {
        Parameters.notEmpty("accessor", accessor); //NO18N
        int prefixLength = getPrefixLength(accessor, includeSetter);
        String withoutPrefix = accessor.substring(prefixLength);
        if (withoutPrefix.isEmpty()) { // method name is simply is/get/set
            return accessor;
        }
        char firstChar = withoutPrefix.charAt(0);

        if (!Character.isUpperCase(firstChar)) {
            return accessor;
        }

        //method property which is prefixed by 'is' but doesn't return boolean
        if (returnType != null && accessor.startsWith("is") && returnType.getKind() != TypeKind.BOOLEAN) { //NOI18N
            return accessor;
        }

        //check the second char, if its also uppercase, the property name must be preserved
        if(withoutPrefix.length() > 1 && Character.isUpperCase(withoutPrefix.charAt(1))) {
            return withoutPrefix;
        }

        return Character.toLowerCase(firstChar) + withoutPrefix.substring(1);
    }

    private static int getPrefixLength(String accessor, boolean includeSetter) {
        List<String> accessorPrefixes = new ArrayList<>();
        accessorPrefixes.add("get");
        if (includeSetter) {
            accessorPrefixes.add("set");
        }
        accessorPrefixes.add("is");

        for (String prefix : accessorPrefixes) {
            if (accessor.startsWith(prefix)) {
                return prefix.length();
            }
        }
        return 0;
    }

    private static class ParamDefinedManagedBean implements FacesManagedBean {

        private final FacesManagedBean managedBean;
        private final String name;

        public ParamDefinedManagedBean(FacesManagedBean managedBean, String name) {
            this.managedBean = managedBean;
            this.name = name;
        }

        @Override
        public Boolean getEager() {
            return managedBean.getEager();
        }

        @Override
        public String getManagedBeanName() {
            return name;
        }

        @Override
        public String getManagedBeanClass() {
            return managedBean.getManagedBeanClass();
        }

        @Override
        public ManagedBean.Scope getManagedBeanScope() {
            return managedBean.getManagedBeanScope();
        }

        @Override
        public String getManagedBeanScopeString() {
            return managedBean.getManagedBeanScopeString();
        }

        @Override
        public List<ManagedProperty> getManagedProperties() {
            return managedBean.getManagedProperties();
        }
    }
}
