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

package org.netbeans.modules.form.binding;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.text.JTextComponent;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.Converter;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.beansbinding.ObjectProperty;
import org.jdesktop.beansbinding.Property;
import org.jdesktop.beansbinding.Validator;
import org.jdesktop.beansbinding.ext.BeanAdapterFactory;
import org.jdesktop.swingbinding.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.form.BindingDescriptor;
import org.netbeans.modules.form.BindingDesignSupport;
import org.netbeans.modules.form.BindingProperty;
import org.netbeans.modules.form.ComponentContainer;
import org.netbeans.modules.form.FormDesignValue;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormModelEvent;
import org.netbeans.modules.form.FormModelListener;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.FormUtils.TypeHelper;
import org.netbeans.modules.form.MetaBinding;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.project.ClassPathUtils;
import org.netbeans.modules.form.project.ClassSource;
import org.netbeans.modules.nbform.project.ClassSourceResolver;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/**
 * Design support for beans binding.
 *
 * @author Jan Stola, Tomas Pavek
 */
public class BindingDesignSupportImpl implements BindingDesignSupport {
    /** Form model. */
    private FormModel formModel;

    /** Realizations of bindings among replicated components. */
    private Map<MetaBinding, List<Binding>> bindingsMap = new HashMap<MetaBinding, List<Binding>>();
    /** Realizations of bindings among metacomponents. */
    private Map<MetaBinding, Binding> modelBindings = new HashMap<MetaBinding, Binding>();
    /** Binding to BindingGroup mapping. */
    private Map<Binding, BindingGroup> bindingToGroup = new HashMap<Binding, BindingGroup>();
    /** Binding group for reference instances in metacomponents. */
    private BindingGroup bindingGroup;

    /**
     * Create binding design support for the given form model.
     *
     * @param model form model to create the binding support for.
     */
    BindingDesignSupportImpl(FormModel model) {
        formModel = model;

        bindingGroup = new BindingGroup();
        bindingGroup.bind();

        formModel.addFormModelListener(new ModelListener());
    }

    /**
     * Changes the binding between two components (affects only replicated components).
     * 
     * @param oldBinding the old definition of the binding.
     * @param newBinding the new definition of the binding.
     */
    private void changeBinding(MetaBinding oldBinding, MetaBinding newBinding) {
        if (oldBinding != null) {
            removeBindings(oldBinding);
        }
        // non-model bindings are added from VisualReplicator
    }

    /**
     * Changes the binding between two components (affects only reference instances in the model).
     * 
     * @param oldBinding the old definition of the binding.
     * @param newBinding the new definition of the binding.
     */
    @Override
    public void changeBindingInModel(MetaBinding oldBinding, MetaBinding newBinding) {
        if (oldBinding != null) {
            removeBindingInModel(oldBinding);
        }
        if (newBinding != null) {
            addBindingInModel(newBinding);
        }
    }

    @Override
    public String elWrap(String path) {
        return (path == null) ? null : "${" + path + "}"; // NOI18N
    }


    @Override
    public boolean isSimpleExpression(String expression) {
        return isSimpleExpression0(expression);
    }

    private static boolean isSimpleExpression0(String expression) {
        return (expression.startsWith("${") && expression.endsWith("}")); // NOI18N
    }

    @Override
    public String unwrapSimpleExpression(String expression) {
        return unwrapSimpleExpression0(expression);
    }

    private static String unwrapSimpleExpression0(String expression) {
        if (isSimpleExpression0(expression)) {
            expression = expression.substring(2, expression.length()-1);
        }
        return expression;        
    }

    private static boolean hasRelativeType(Class clazz, String property) {
        // selectedElement(_...), selectedElements(_...)
        return (("elements".equals(property) || property.startsWith("selectedElement")) // NOI18N
                && (javax.swing.JTable.class.isAssignableFrom(clazz) || javax.swing.JList.class.isAssignableFrom(clazz)))
            || (("selectedItem".equals(property)) && javax.swing.JComboBox.class.isAssignableFrom(clazz)); // NOI18N
    }

    // Used to determine binding properties only
    @Override
    public List<BindingDescriptor>[] getBindingDescriptors(RADComponent component) {
        BeanDescriptor beanDescriptor = component.getBeanInfo().getBeanDescriptor();
        List<BindingDescriptor>[] descs = getBindingDescriptors(null, beanDescriptor, false);
        Class<?> beanClass = component.getBeanClass();
        if (JTextComponent.class.isAssignableFrom(beanClass)) {
            // get rid of text_... descriptors
            descs[0] = filterDescriptors(descs[0], "text_"); // NOI18N
        } else if (JTable.class.isAssignableFrom(beanClass)
                || JList.class.isAssignableFrom(beanClass)
                || JComboBox.class.isAssignableFrom(beanClass)) {
            // get rid of selectedElement(s)_... descriptors
            descs[0] = filterDescriptors(descs[0], "selectedElement_"); // NOI18N
            descs[0] = filterDescriptors(descs[0], "selectedElements_"); // NOI18N
            // add elements descriptor
            BindingDescriptor desc = new BindingDescriptor("elements", List.class); // NOI18N
            descs[0].add(0, desc);
        } else if (JSlider.class.isAssignableFrom(beanClass)) {
            // get rid of value_... descriptor
            descs[0] = filterDescriptors(descs[0], "value_"); // NOI18N
        }
        return descs;
    }

    private List<BindingDescriptor> filterDescriptors(List<BindingDescriptor> descs, String forbiddenPrefix) {
        List<BindingDescriptor> filtered = new LinkedList<BindingDescriptor>();
        for (BindingDescriptor bd : descs) {
            if (!bd.getPath().startsWith(forbiddenPrefix)) { // NOI18N
                filtered.add(bd);
            }
        }
        return filtered;
    }

    private List<PropertyDescriptor> getSpecialBindingDescriptors(Class clazz) {
        List<PropertyDescriptor> descs = BeanAdapterFactory.getAdapterPropertyDescriptors(clazz);
        try {
            if (JComboBox.class.isAssignableFrom(clazz)) {
                PropertyDescriptor desc = new PropertyDescriptor("selectedItem", JComboBox.class); // NOI18N
                descs.add(desc);
            } else if (JSpinner.class.isAssignableFrom(clazz)) {
                PropertyDescriptor desc = new PropertyDescriptor("value", JSpinner.class); // NOI18N
                descs.add(desc);                
            } else if (JFormattedTextField.class.isAssignableFrom(clazz)) {
                for (PropertyDescriptor pd : descs) {
                    if ("text".equals(pd.getName())) { // NOI18N
                        descs.remove(pd);
                        break;
                    }
                }
                PropertyDescriptor desc = new PropertyDescriptor("value", JFormattedTextField.class); // NOI18N
                descs.add(desc);
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
        return descs;
    }

    private List<BindingDescriptor>[] getBindingDescriptors(TypeHelper type, BeanDescriptor beanDescriptor, boolean includeReadOnly) {
        Class<?> beanClass = beanDescriptor.getBeanClass();
        List<BindingDescriptor> bindingList = new LinkedList<BindingDescriptor>();
        List<BindingDescriptor> prefList = new LinkedList<BindingDescriptor>();
        List<BindingDescriptor> observableList = new LinkedList<BindingDescriptor>();
        List<BindingDescriptor> nonObservableList = new LinkedList<BindingDescriptor>();
        List<BindingDescriptor> list;
        Object[] propsCats = FormUtils.getPropertiesCategoryClsf(beanClass, beanDescriptor);
        PropertyDescriptor[] pds;
        try {
             pds = FormUtils.getBeanInfo(beanClass).getPropertyDescriptors();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
            pds = new PropertyDescriptor[0];
        }
        List<PropertyDescriptor> specialPds = getSpecialBindingDescriptors(beanClass);
        Map<String,PropertyDescriptor> pathToDesc = new HashMap<String,PropertyDescriptor>();
        if (Utilities.isMac()) {
            try {
                for (PropertyDescriptor pd : FormUtils.getBeanInfo(beanClass, Introspector.IGNORE_ALL_BEANINFO).getPropertyDescriptors()) {
                    pathToDesc.put(pd.getName(), pd);
                }
            } catch (IntrospectionException iex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, iex.getMessage(), iex);
            }
        }
        for (PropertyDescriptor pd : pds) {
            pathToDesc.put(pd.getName(), pd);
        }
        for (PropertyDescriptor pd : specialPds) {
            if (pathToDesc.get(pd.getName()) != null) {
                pathToDesc.remove(pd.getName());
            }
        }
        List<PropertyDescriptor> allPds = new LinkedList<PropertyDescriptor>(specialPds);
        allPds.addAll(pathToDesc.values());
        int count = 0;
        for (PropertyDescriptor pd : allPds) {
            if (count++<specialPds.size()) {
                list = bindingList;
            } else {
                if (!includeReadOnly && (pd.getWriteMethod() == null)) {
                    continue;
                }
                Object propCat = FormUtils.getPropertyCategory(pd, propsCats);                
                if (propCat == FormUtils.PROP_HIDDEN) {
                    // hidden property => hide also the binding property
                    continue;
                } else {
                    if (pd.isBound()) {
                        // observable property
                        if (propCat == FormUtils.PROP_PREFERRED) {
                            list = prefList;
                        } else {
                            list = observableList;
                        }
                    } else {
                        // non-observable property
                        list = nonObservableList;
                    }
                }
            }

            Method method = pd.getReadMethod();
            if ((method != null) && ("getClass".equals(method.getName()))) continue; // NOI18N
            Type retType = (method == null) ? pd.getPropertyType() : method.getGenericReturnType();
            if (retType == null) continue;
            BindingDescriptor bd;
            if (type == null) {
                bd = new BindingDescriptor(pd.getName(), retType);
            } else {
                TypeHelper t = new TypeHelper(retType, type.getActualTypeArgs()).normalize();
                bd = new BindingDescriptor(pd.getName(), t);
            }
            bd.setDisplayName(pd.getDisplayName());
            bd.setShortDescription(pd.getShortDescription());

            if (hasRelativeType(beanClass, bd.getPath())) {
                bd.markTypeAsRelative();
            }
            
            list.add(bd);
        }

        if (bindingList.isEmpty()) {
            bindingList = prefList;
        } else {
            observableList.addAll(prefList);
        }
        Comparator<BindingDescriptor> bdComparator = new Comparator<BindingDescriptor>() {
            @Override
            public int compare(BindingDescriptor o1, BindingDescriptor o2) {
                String path1 = o1.getPath();
                String path2 = o2.getPath();
                return path1.compareToIgnoreCase(path2);
            }
        };
        Collections.sort(bindingList, bdComparator);
        Collections.sort(observableList, bdComparator);
        Collections.sort(nonObservableList, bdComparator);

        return new List[] {bindingList, observableList, nonObservableList};
    }

    @Override
    public List<BindingDescriptor> getAllBindingDescriptors(TypeHelper type) {
        List<BindingDescriptor>[] descs = getBindingDescriptors(type);
        List<BindingDescriptor> list = new LinkedList<BindingDescriptor>();
        for (int i=0; i<descs.length; i++ ){
            list.addAll(descs[i]);
        }
        return list;
    }
    
    /**
     * Returns possible bindings for the given type.
     *
     * @param type type whose possible bindings should be returned.
     * @return list of <code>BindingDescriptor</code>s describing possible bindings.
     */
    @Override
    @SuppressWarnings("unchecked") // generic array creation NOI18N
    public List<BindingDescriptor>[] getBindingDescriptors(TypeHelper type) {
        List<BindingDescriptor> typesFromSource = Collections.emptyList();
        Class binarySuperClass = null;
        if (type.getType() == null) {
            FileObject fileInProject = FormEditor.getFormDataObject(formModel).getPrimaryFile();
            ClassPath cp = ClassPath.getClassPath(fileInProject, ClassPath.SOURCE);
            final List<BindingDescriptor> types = new LinkedList<BindingDescriptor>();
            final String[] superClass = new String[1];
            superClass[0] = type.getName();
            do {
                String typeName = superClass[0];
                final String resourceName = typeName.replace('.', '/') + ".java"; // NOI18N
                int lastDot = typeName.lastIndexOf('.');
                lastDot = (lastDot == -1) ? 0 : lastDot;
                final String simpleTypeName = typeName.substring(lastDot+1);
                FileObject fob = cp.findResource(resourceName);
                if (fob == null) {
                    try {
                        binarySuperClass = ClassPathUtils.loadClass(typeName, fileInProject);
                    } catch (ClassNotFoundException cnfex) {}
                    break;
                }
                JavaSource source = JavaSource.forFileObject(fob);
                try {
                    source.runUserActionTask(new CancellableTask<CompilationController>() {
                        @Override
                        public void run(CompilationController cc) throws Exception {
                            cc.toPhase(JavaSource.Phase.RESOLVED);
                            CompilationUnitTree cu = cc.getCompilationUnit();
                            ClassTree clazz = null;
                            for (Tree typeDecl : cu.getTypeDecls()) {
                                if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                                    ClassTree candidate = (ClassTree)typeDecl;
                                    if (candidate.getSimpleName().toString().equals(simpleTypeName)) {
                                        clazz = candidate;
                                        break;
                                    }
                                }
                            }
                            if (clazz == null) { // issue 118690
                                // should not happen
                                Logger.getLogger(getClass().getName()).log(Level.INFO, "ClassTree not found in {0}", resourceName); // NOI18N
                                superClass[0] = Object.class.getName();
                                return;
                            }
                            for (Tree clMember : clazz.getMembers()) {
                                if (clMember.getKind() == Tree.Kind.METHOD) {
                                    MethodTree method = (MethodTree)clMember;
                                    if (!method.getParameters().isEmpty()) continue;
                                    Set<javax.lang.model.element.Modifier> modifiers = method.getModifiers().getFlags();
                                    if (modifiers.contains(javax.lang.model.element.Modifier.STATIC)
                                            || !modifiers.contains(javax.lang.model.element.Modifier.PUBLIC)) {
                                        continue;
                                    }
                                    String methodName = method.getName().toString();
                                    Tree returnType = method.getReturnType();

                                    String propName;
                                    if (methodName.startsWith("get")) { // NOI18N
                                        propName = methodName.substring(3);
                                    } else if (methodName.startsWith("is")) { // NOI18N
                                        if ((returnType.getKind() == Tree.Kind.PRIMITIVE_TYPE)
                                                && (((PrimitiveTypeTree)returnType).getPrimitiveTypeKind() == TypeKind.BOOLEAN)) {
                                            propName = methodName.substring(2);
                                        } else {
                                            continue;
                                        }
                                    } else {
                                        continue;
                                    }
                                    if (propName.length() == 0) continue;
                                    if ((propName.length() == 1) || (Character.isLowerCase(propName.charAt(1)))) {
                                        propName = Character.toLowerCase(propName.charAt(0)) + propName.substring(1);
                                    }

                                    TypeHelper type;
                                    if (returnType.getKind() == Tree.Kind.PRIMITIVE_TYPE) {
                                        PrimitiveTypeTree ptree = (PrimitiveTypeTree)returnType;
                                        if (ptree.getPrimitiveTypeKind() == TypeKind.VOID) {
                                            continue; // void return type
                                        }
                                        type = new TypeHelper(ptree.toString());
                                    } else {
                                        type = treeToType(cc, returnType, formModel);
                                    }
                                    types.add(0, new BindingDescriptor(propName, type));
                                }
                            }
                            Tree superTree = clazz.getExtendsClause();
                            TypeHelper type = treeToType(cc, superTree, formModel);
                            String typeName = type.getName();
                            superClass[0] = (typeName == null) ? FormUtils.typeToClass(type).getName() : typeName;
                        }

                        @Override
                        public void cancel() {
                        }

                    }, true);
                } catch (IOException ioex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, ioex.getMessage(), ioex);
                }
            } while (!Object.class.getName().equals(superClass[0]));
            typesFromSource = types;
        }
        List<BindingDescriptor>[] list = new List[] {Collections.emptyList(), typesFromSource, Collections.emptyList()};
        Class clazz = (type.getType() == null) ? binarySuperClass : FormUtils.typeToClass(type);
        if ((clazz != null) && !clazz.getName().startsWith("java.lang.") // NOI18N
                && !Collection.class.isAssignableFrom(clazz)
                && !java.util.Date.class.isAssignableFrom(clazz)
                && !clazz.isArray()) {
            try {
                BeanInfo beanInfo = FormUtils.getBeanInfo(clazz);
                List<BindingDescriptor>[] typesFromBinary = getBindingDescriptors(type, beanInfo.getBeanDescriptor(), true);
                Map<String,BindingDescriptor>[] maps = new Map[3];
                for (int i=0; i<3; i++) {
                    maps[i] = listToMap(typesFromBinary[i]);
                }
                for (BindingDescriptor descriptor : typesFromSource) {
                    String path = descriptor.getPath();
                    int i;
                    for (i=0; i<3; i++) {
                        if (maps[i].containsKey(path)) break;
                    }
                    if (i == 3) {
                        i = 1; // put into observablle properties by default
                    }
                    maps[i].put(path, descriptor);
                }
                for (int i=0; i<3; i++) {
                    list[i] = new LinkedList<BindingDescriptor>(maps[i].values());
                }
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
            }
        }
        return list;
    }

    private static TypeHelper treeToType(CompilationController cc, Tree tree, FormModel model) {
        String typeName = Object.class.getName();
        Map<String,TypeHelper> map = null;
        if (tree != null) {
            CompilationUnitTree cu = cc.getCompilationUnit();
            Trees trees = cc.getTrees();
            if (tree.getKind() == Tree.Kind.EXTENDS_WILDCARD) {
                tree = ((WildcardTree)tree).getBound();
            }
            TreePath path = trees.getPath(cu, tree);
            Element el = trees.getElement(path);
            if ((el != null) && ((el.getKind() == ElementKind.CLASS) || (el.getKind() == ElementKind.INTERFACE))) {
                TypeElement tel = (TypeElement) el;
                typeName = tel.getQualifiedName().toString();
                if (tree.getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
                    List<? extends Tree> params = ((ParameterizedTypeTree)tree).getTypeArguments();
                    List<? extends TypeParameterElement> elems = tel.getTypeParameters();
                    map = new HashMap<String,TypeHelper>();
                    for (int i=0; i<params.size() && i<elems.size(); i++) {
                        Tree param = params.get(0);
                        TypeHelper paramType = treeToType(cc, param, model);
                        TypeParameterElement elem = elems.get(0);
                        map.put(elem.toString(), paramType);
                    }
                }
            }
        }
        TypeHelper type = new TypeHelper(typeName, map);
        if (typeName.indexOf('.') != -1) {
            try {
                Class clazz = FormUtils.loadClass(typeName, model);
                type = new TypeHelper(clazz, map);
            } catch (ClassNotFoundException cnfex) {
                // not compiled - use just the name
            }
        }
        return type;
    }

    private static Map<String,BindingDescriptor> listToMap(List<BindingDescriptor> list) {
        Map<String,BindingDescriptor> map = new TreeMap<String,BindingDescriptor>();
        for (BindingDescriptor descriptor : list) {
            String path = descriptor.getPath();
            map.put(path, descriptor);
        }
        return map;
    }

    @Override
    public TypeHelper determineType(RADComponent comp) {
        TypeHelper type;
        if (comp.getFormModel().getTopRADComponent() == comp) {
            FileObject fob = FormEditor.getFormDataObject(comp.getFormModel()).getPrimaryFile();
            ClassPath cp = ClassPath.getClassPath(fob, ClassPath.SOURCE);
            String className = cp.getResourceName(fob, '.', false);
            type = new TypeHelper(className);
        } else {
            Type t = null;
            Map<String,TypeHelper> newMap = null;
            Class clazz = comp.getBeanClass();
            t = clazz;
            if (clazz.getTypeParameters().length == 1) {
                try {
                    TypeHelper elemType = determineTypeParameter(comp);
                    if (elemType != null) {
                        newMap = new HashMap<String,TypeHelper>();
                        newMap.put(clazz.getTypeParameters()[0].getName(), elemType);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(BindingDesignSupportImpl.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                }
            }
            type = new TypeHelper(t, newMap);
        }
        return type;
    }

    static TypeHelper determineTypeParameter(final RADComponent comp) {
        FileObject fob = FormEditor.getFormDataObject(comp.getFormModel()).getPrimaryFile();
        JavaSource source = JavaSource.forFileObject(fob);
        final String varName = comp.getName();
        final TypeHelper[] result = new TypeHelper[1];
        try {
            source.runUserActionTask(new CancellableTask<CompilationController>() {
                @Override
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cu = cc.getCompilationUnit();
                    ClassTree clazz = null;
                    for (Tree typeDecl : cu.getTypeDecls()) {
                        if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                            clazz = (ClassTree) typeDecl;
                            break;
                        }
                    }
                    Node.Property prop = comp.getSyntheticProperty("useLocalVariable"); // NOI18N
                    Object value = prop.getValue();
                    VariableTree variable = null;
                    if (Boolean.TRUE.equals(value)) {
                        // local variable in initComponents()
                        for (Tree clMember : clazz.getMembers()) {
                            if (clMember.getKind() == Tree.Kind.METHOD) {
                                MethodTree method = (MethodTree)clMember;
                                String methodName = method.getName().toString();
                                if ("initComponents".equals(methodName)) { // NOI18N
                                    for (StatementTree statement : method.getBody().getStatements()) {
                                        if (statement.getKind() == Tree.Kind.VARIABLE) {
                                            VariableTree var = (VariableTree)statement;
                                            if (varName.equals(var.getName().toString())) {
                                                variable = var;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // fields in class
                        for (Tree clMember : clazz.getMembers()) {
                            if (clMember.getKind() == Tree.Kind.VARIABLE) {
                                VariableTree var = (VariableTree)clMember;
                                if (varName.equals(var.getName().toString())) {
                                    variable = var;
                                }
                            }
                        }
                    }
                    if (variable != null) {
                        Tree type = variable.getType();
                        if (type.getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
                            ParameterizedTypeTree params = (ParameterizedTypeTree)type;
                            List<? extends Tree> args = params.getTypeArguments();
                            if (args.size() == 1) {
                                Tree tree = args.get(0);
                                result[0] = treeToType(cc, tree, comp.getFormModel());
                            }
                        }
                    }
                }

                @Override
                public void cancel() {
                }
            }, true);
        } catch (IOException ioex) {
            Logger.getLogger(BindingDesignSupportImpl.class.getName()).log(Level.INFO, ioex.getMessage(), ioex);
        }
        if (result[0] == null) {
            // fallback - covers the situation where the component
            // has been added but the code hasn't been generated yet
            Class clazz = comp.getBeanClass();
            if (clazz.getTypeParameters().length == 1) {
                try {
                    Object value = comp.getSyntheticProperty("typeParameters").getValue(); // NOI18N
                    if (value instanceof String) {
                        String type = (String)value;
                        if (type.startsWith("<")) { // NOI18N
                            type = type.substring(1, type.length()-1);
                            Map<String,TypeHelper> newMap = new HashMap<String,TypeHelper>();
                            try {
                                Class elemType = ClassPathUtils.loadClass(type, FormEditor.getFormDataObject(comp.getFormModel()).getFormFile());
                                newMap.put(clazz.getTypeParameters()[0].getName(), new TypeHelper(elemType));
                            } catch (ClassNotFoundException cnfex) {
                                newMap.put(clazz.getTypeParameters()[0].getName(), new TypeHelper(type));
                            }
                            result[0] = new TypeHelper(type, newMap);
                        }
                    }
                } catch (IllegalAccessException iaex) {
                    Logger.getLogger(BindingDesignSupportImpl.class.getName()).log(Level.INFO, null, iaex);
                } catch (InvocationTargetException itex) {
                    Logger.getLogger(BindingDesignSupportImpl.class.getName()).log(Level.INFO, null, itex);
                }
            }
        }
        return result[0];
    }

    @Override
    public TypeHelper determineType(RADComponent comp, String sourcePath) {
        String[] path = parsePath(sourcePath);
        TypeHelper type = determineType(comp);
        for (int i=0; i<path.length; i++) {
            String pathItem = path[i];
            List<BindingDescriptor> descriptors = getAllBindingDescriptors(type);
            BindingDescriptor descriptor = findDescriptor(descriptors, pathItem);
            if (descriptor == null) return new TypeHelper();
            type = descriptor.getGenericValueType();
            if (type == null) {
                if (javax.swing.JTable.class.isAssignableFrom(comp.getBeanClass())
                        || javax.swing.JList.class.isAssignableFrom(comp.getBeanClass())
                        || javax.swing.JComboBox.class.isAssignableFrom(comp.getBeanClass())) {
                    MetaBinding binding = comp.getBindingProperty("elements").getValue(); // NOI18N
                    if (binding != null) {
                        RADComponent subComp = binding.getSource();
                        String subSourcePath = binding.getSourcePath();
                        subSourcePath = (subSourcePath == null) ? null : unwrapSimpleExpression(subSourcePath);
                        // PENDING beware of stack overflow
                        TypeHelper t = determineType(subComp, subSourcePath);
                        if (javax.swing.JComboBox.class.isAssignableFrom(comp.getBeanClass())) {
                            if ("selectedItem".equals(pathItem)) { // NOI18N
                                type = t.typeOfElement();
                            }
                        } else {
                            if ("selectedElement".equals(pathItem) || pathItem.startsWith("selectedElement_")) { // NOI18N
                                type = t.typeOfElement();
                            } else if (pathItem.startsWith("selectedElements") || "elements".equals(pathItem)) { // NOI18N
                                type = t;
                            }
                        }
                    } else {
                        type = new TypeHelper();
                    }
                }
            }
        }
        return type;
    }

    /**
     * Finds descriptor that corresponds to the given binding path.
     *
     * @param descriptors list of descriptors that should be searched.
     * @param path binding path to find descriptor for.
     * @return descriptor that corresponds to the given binding path.
     */
    private static BindingDescriptor findDescriptor(List<BindingDescriptor> descriptors, String path) {
        for (BindingDescriptor descriptor : descriptors) {
            if (descriptor.getPath().equals(path)) return descriptor;
        }
        return null;
    }

    /**
     * Parses binding path into segments.
     *
     * @param path path to parse.
     * @return segments of the binding path. The returned value cannot be <code>null</code>.
     */
    private static String[] parsePath(String path) {
        if (path == null) return new String[0];
        List<String> pathItems = new LinkedList<String>();
        int index;
        while ((index = path.indexOf('.')) != -1) {
            pathItems.add(path.substring(0,index));
            path = path.substring(index+1);
        }
        pathItems.add(path);
        return pathItems.toArray(new String[pathItems.size()]);
    }

    void establishUpdatedBindings(RADComponent metacomp, boolean recursive,
            Map map, BindingGroup group, boolean inModel) {
        for (MetaBinding bindingDef : collectBindingDefs(metacomp, recursive)) {
            RADComponent sourceComp = bindingDef.getSource();
            RADComponent targetComp = bindingDef.getTarget();
            if (sourceComp.isInModel() && targetComp.isInModel()) {
                if (inModel) {
                    addBindingInModel(bindingDef);
                } else {
                    Object source = null;
                    if (map != null)
                        source = map.get(sourceComp.getId());
                    if (source == null)
                        source = sourceComp.getBeanInstance(); // also used if clone not available
                    Object target = map != null ?
                        map.get(targetComp.getId()) : targetComp.getBeanInstance();
                    if (source != null && target != null)
                        addBinding(bindingDef, source, target, group, false);
                }
            }
        }
    }

    static void establishOneOffBindings(RADComponent metacomp,
            boolean recursive, Map map, BindingGroup group) {
        for (MetaBinding bindingDef : collectBindingDefs(metacomp, recursive)) {
            RADComponent sourceComp = bindingDef.getSource();
            RADComponent targetComp = bindingDef.getTarget();
            Object source = null;
            if (map != null)
                source = map.get(sourceComp.getId());
            if (source == null)
                source = sourceComp.getBeanInstance(); // also used if clone not available
            Object target = map != null ?
                map.get(targetComp.getId()) : targetComp.getBeanInstance();
            if (source != null && target != null)
                createBinding(bindingDef, source, target, group, null);
        }
    }

    private void releaseBindings(RADComponent metacomp, boolean recursive) {
        for (MetaBinding bindingDef : collectBindingDefs(metacomp, recursive)) {
            removeBindings(bindingDef); // unbinds and removes all bindings
                                        // created according to this definition
        }
    }

    private static Collection<MetaBinding> collectBindingDefs(RADComponent metacomp, boolean recursive) {
        Collection<MetaBinding> col = collectBindingDefs(metacomp, recursive, null);
        if (col == null)
            col = Collections.emptyList();
        return col;
    }

    private static Collection<MetaBinding> collectBindingDefs(
            RADComponent metacomp, boolean recursive, Collection<MetaBinding> col)
    {
        for (BindingProperty bProp : metacomp.getKnownBindingProperties()) {
            MetaBinding bindingDef = bProp.getValue();
            if (bindingDef != null) {
                if (col == null)
                    col = new LinkedList<MetaBinding>();
                col.add(bindingDef);
            }
        }

        if (recursive && metacomp instanceof ComponentContainer) {
            for (RADComponent subcomp : ((ComponentContainer)metacomp).getSubBeans()) {
                col = collectBindingDefs(subcomp, recursive, col);
            }
        }

        return col;
    }

    private void addBindingInModel(MetaBinding bindingDef) {
        addBinding(bindingDef,
            bindingDef.getSource().getBeanInstance(),
            bindingDef.getTarget().getBeanInstance(),
            bindingGroup, true);
    }
    
    /**
     * Creates binding according to given MetaBinding between given source and
     * target objects. The binding is registered, so it is automatically unbound
     * and removed when the MetaBinding is removed (or the source/target component).
     * 
     * @param bindingDef description of the binding
     * @param source binding source
     * @param target binding target
     * @param group binding group where the binding should be added
     * @param inModel determines whether we are creating binding in the model
     */
    void addBinding(MetaBinding bindingDef, Object source, Object target,
            BindingGroup group, boolean inModel) {
        if (inModel) {
            if (modelBindings.get(bindingDef) == null) {
                modelBindings.put(bindingDef, createBinding(bindingDef, source, target, group, bindingToGroup));
            }
        } else {
            List<Binding> establishedBindings = bindingsMap.get(bindingDef);
            if (establishedBindings != null) {
                for (Binding binding : establishedBindings) {
                    if (binding.getSourceObject() == source
                        && binding.getTargetObject() == target)
                        return; // this binding already exists
                }
            }
            else {
                establishedBindings = new LinkedList<Binding>();
                bindingsMap.put(bindingDef, establishedBindings);
            }
            establishedBindings.add(createBinding(bindingDef, source, target, group, bindingToGroup));
        }
    }
    
    private static String actualTargetPath(MetaBinding bindingDef) {
        String targetPath = bindingDef.getTargetPath();
        if ("text".equals(targetPath)) { // NOI18N
            Class<?> targetClass = bindingDef.getTarget().getBeanClass();
            if (JTextComponent.class.isAssignableFrom(targetClass)) {
                String strategy = bindingDef.getParameter(MetaBinding.TEXT_CHANGE_STRATEGY);
                if (MetaBinding.TEXT_CHANGE_ON_ACTION_OR_FOCUS_LOST.equals(strategy)) {
                    targetPath += "_ON_ACTION_OR_FOCUS_LOST"; // NOI18N
                } else if (MetaBinding.TEXT_CHANGE_ON_FOCUS_LOST.equals(strategy)) {
                    targetPath += "_ON_FOCUS_LOST"; // NOI18N
                }
            }
        } else if ("selectedElement".equals(targetPath) || "selectedElements".equals(targetPath)) { // NOI18N
            Class<?> targetClass = bindingDef.getTarget().getBeanClass();
            if (JList.class.isAssignableFrom(targetClass)
                || JTable.class.isAssignableFrom(targetClass)
                || JComboBox.class.isAssignableFrom(targetClass)) {
                String value = bindingDef.getParameter(MetaBinding.IGNORE_ADJUSTING_PARAMETER);
                if ("Y".equals(value)) { // NOI18N
                    targetPath += "_IGNORE_ADJUSTING"; // NOI18N
                }
            }
        } else if ("value".equals(targetPath)) { // NOI18N
            Class<?> targetClass = bindingDef.getTarget().getBeanClass();
            if (JSlider.class.isAssignableFrom(targetClass)) {
                String value = bindingDef.getParameter(MetaBinding.IGNORE_ADJUSTING_PARAMETER);
                if ("Y".equals(value)) { // NOI18N
                    targetPath += "_IGNORE_ADJUSTING"; // NOI18N
                }
            }
        }
        return targetPath;
    }

    private static void generateTargetProperty(MetaBinding bindingDef, StringBuilder buf) {
        String targetPath = actualTargetPath(bindingDef);
        String property = BeanProperty.class.getName() + ".create(\"" + targetPath + "\")"; // NOI18N
        buf.append(property);
    }

    private static Property createTargetProperty(MetaBinding bindingDef) {
        String targetPath = actualTargetPath(bindingDef);
        Property property = BeanProperty.create(targetPath);
        return property;
    }

    @Override
    public String generateBinding(BindingProperty prop, StringBuilder buf, CodeGeneratorContext generator) {
        String variable;
        MetaBinding bindingDef = prop.getValue();
        // Update strategy
        int updateStrategy = bindingDef.getUpdateStrategy();
        String strategy = AutoBinding.class.getName() + ".UpdateStrategy."; // NOI18N
        if (updateStrategy == MetaBinding.UPDATE_STRATEGY_READ) {
            strategy += "READ"; // NOI18N
        } else if (updateStrategy == MetaBinding.UPDATE_STRATEGY_READ_ONCE) {
            strategy += "READ_ONCE"; // NOI18N
        } else {
            strategy += "READ_WRITE"; // NOI18N
        }
        strategy += ", "; // NOI18N
        
        RADComponent target = bindingDef.getTarget();
        Class targetClass = target.getBeanClass();
        String targetPath = bindingDef.getTargetPath();
        String sourcePath = bindingDef.getSourcePath();
        Class<?> sourceClass = bindingDef.getSource().getBeanClass();
        if ("elements".equals(targetPath) && JTable.class.isAssignableFrom(targetClass)
                && (List.class.isAssignableFrom(sourceClass) || (sourcePath != null))) { // NOI18N
            String elVariable = elVariableHelper(sourcePath, buf, generator);
            variable = generator.getBindingDescriptionVariable(JTableBinding.class, buf, false);
            if (variable == null) {
                variable = generator.getBindingDescriptionVariable(JTableBinding.class, buf, true);
                buf.append(' ');
            }
            buf.append(variable);
            buf.append(" = "); // NOI18N
            buf.append(SwingBindings.class.getName()).append(".createJTableBinding("); // NOI18N
            buf.append(strategy);
            buf.append(generator.getExpressionJavaString(bindingDef.getSource().getCodeExpression(), "this")); // NOI18N
            buf.append(", "); // NOI18N
            if (sourcePath != null) {
                buf.append(elVariable);
                buf.append(", "); // NOI18N
            }
            buf.append(generator.getExpressionJavaString(bindingDef.getTarget().getCodeExpression(), "this")); // NOI18N
            buildBindingNameCode(prop, buf);
            buf.append(");\n"); // NOI18N
            if (bindingDef.hasSubBindings()) {
                for (MetaBinding sub : bindingDef.getSubBindings()) {
                    String columnVariable = generator.getBindingDescriptionVariable(JTableBinding.ColumnBinding.class, buf, false);
                    if (columnVariable == null) {
                        columnVariable = generator.getBindingDescriptionVariable(JTableBinding.ColumnBinding.class, buf, true);
                        buf.append(' ');
                    }
                    buf.append(columnVariable);
                    buf.append(" = "); // NOI18N
                    buf.append(variable);
                    String subSourcePath = sub.getSourcePath();
                    buf.append(".addColumnBinding("); // NOI18N
                    if ((subSourcePath == null) || "null".equals(subSourcePath)) { // NOI18N
                        buf.append(ObjectProperty.class.getName());
                        buf.append(".create()"); // NOI18N
                    } else {
                        buf.append(ELProperty.class.getName());
                        buf.append(".create(\""); // NOI18N
                        buf.append(subSourcePath);
                        buf.append("\")"); // NOI18N
                    }
                    buf.append(");\n"); // NOI18N
                    String title = sub.getParameter(MetaBinding.NAME_PARAMETER);
                    if (title == null) {
                        title = subSourcePath;
                        if (isSimpleExpression(title)) {
                            title = unwrapSimpleExpression(title);
                            title = capitalize(title);
                        }
                    }
                    if ((title != null) && (!"null".equals(title))) { // NOI18N
                        buf.append(columnVariable);
                        buf.append(".setColumnName(\""); // NOI18N
                        buf.append(title);
                        buf.append("\");\n"); // NOI18N
                    }
                    String columnClass = sub.getParameter(MetaBinding.TABLE_COLUMN_CLASS_PARAMETER);
                    if (columnClass != null) {
                        buf.append(columnVariable);
                        buf.append(".setColumnClass("); // NOI18N
                        buf.append(columnClass);
                        buf.append(");\n"); // NOI18N
                    }
                    String editable = sub.getParameter(MetaBinding.EDITABLE_PARAMETER);
                    if (editable != null) {
                        buf.append(columnVariable);
                        buf.append(".setEditable("); // NOI18N
                        buf.append(editable);
                        buf.append(");\n"); // NOI18N
                    }
                }
            }
        } else if ("elements".equals(targetPath) && javax.swing.JList.class.isAssignableFrom(targetClass)
                && (List.class.isAssignableFrom(sourceClass) || (sourcePath != null))) { // NOI18N
            String elVariable = elVariableHelper(sourcePath, buf, generator);
            variable = generator.getBindingDescriptionVariable(JListBinding.class, buf, false);
            if (variable == null) {
                variable = generator.getBindingDescriptionVariable(JListBinding.class, buf, true);
                buf.append(' ');
            }
            buf.append(variable);
            buf.append(" = "); // NOI18N
            buf.append(SwingBindings.class.getName()).append(".createJListBinding("); // NOI18N
            buf.append(strategy);
            buf.append(generator.getExpressionJavaString(bindingDef.getSource().getCodeExpression(), "this")); // NOI18N
            buf.append(", "); // NOI18N
            if (sourcePath != null) {
                buf.append(elVariable);
                buf.append(", "); // NOI18N
            }
            buf.append(generator.getExpressionJavaString(bindingDef.getTarget().getCodeExpression(), "this")); // NOI18N
            buildBindingNameCode(prop, buf);
            buf.append(");\n"); // NOI18N
            String detailPath = bindingDef.getParameter(MetaBinding.DISPLAY_PARAMETER);
            if (detailPath != null) {
                buf.append(variable);
                buf.append(".setDetailBinding("); // NOI18N
                buf.append(ELProperty.class.getName());
                buf.append(".create(\""); // NOI18N
                buf.append(detailPath);
                buf.append("\"));\n"); // NOI18N
            }
        } else if ("elements".equals(targetPath) && javax.swing.JComboBox.class.isAssignableFrom(targetClass)
                && (List.class.isAssignableFrom(sourceClass) || (sourcePath != null))) { // NOI18N
            String elVariable = elVariableHelper(sourcePath, buf, generator);
            variable = generator.getBindingDescriptionVariable(JComboBoxBinding.class, buf, false);
            if (variable == null) {
                variable = generator.getBindingDescriptionVariable(JComboBoxBinding.class, buf, true);
                buf.append(' ');
            }
            buf.append(variable);
            buf.append(" = "); // NOI18N
            buf.append(SwingBindings.class.getName()).append(".createJComboBoxBinding("); // NOI18N
            buf.append(strategy);
            buf.append(generator.getExpressionJavaString(bindingDef.getSource().getCodeExpression(), "this")); // NOI18N
            buf.append(", "); // NOI18N
            if (sourcePath != null) {
                buf.append(elVariable);
                buf.append(", "); // NOI18N
            }
            buf.append(generator.getExpressionJavaString(bindingDef.getTarget().getCodeExpression(), "this")); // NOI18N
            buildBindingNameCode(prop, buf);
            buf.append(");\n"); // NOI18N
        } else {
            variable = generator.getBindingDescriptionVariable(Binding.class, buf, false);
            StringBuilder sb = new StringBuilder();
            if (variable == null) {
                variable = generator.getBindingDescriptionVariable(Binding.class, buf, true);
                buf.append(' ');
                buf.append(sb);
            }
            buf.append(variable);
            buf.append(" = "); // NOI18N
            buf.append(Bindings.class.getName()).append(sb).append(".createAutoBinding("); // NOI18N
            buf.append(strategy);
            buildBindingParamsCode(prop, buf, generator);
        }
        return variable;
    }
    
    private static ELProperty createELProperty(String path) {
        ELProperty property;
        try {
            property = ELProperty.create(path);
        } catch (Exception ex) {
            Logger.getLogger(BindingDesignSupportImpl.class.getName()).log(Level.INFO, ex.getMessage(), ex);
            // fallback
            property = ELProperty.create("error"); // NOI18N
        }
        return property;
    }

    private static String elVariableHelper(String sourcePath, StringBuilder buf, CodeGeneratorContext generator) {
        String elVariable = null;
        if (sourcePath != null) {
            elVariable = generator.getBindingDescriptionVariable(ELProperty.class, buf, false);
            if (elVariable == null) {
                elVariable = generator.getBindingDescriptionVariable(ELProperty.class, buf, true);
                buf.append(' ');
            }
            buf.append(elVariable);
            buf.append(" = "); // NOI18N
            buf.append(ELProperty.class.getName());
            buf.append(".create(\""); // NOI18N
            buf.append(sourcePath);
            buf.append("\");\n"); // NOI18N
        }
        return elVariable;
    }

    private static void buildBindingParamsCode(BindingProperty prop, StringBuilder buf, CodeGeneratorContext generator) {
        MetaBinding bindingDef = prop.getValue();
        String sourcePath = bindingDef.getSourcePath();
        String targetPath = bindingDef.getTargetPath();
        buf.append(generator.getExpressionJavaString(bindingDef.getSource().getCodeExpression(), "this")); // NOI18N
        buf.append(", "); // NOI18N
        if (sourcePath != null) {
            buf.append(ELProperty.class.getName());
            buf.append(".create(\""); // NOI18N
            buf.append(sourcePath);
            buf.append("\")"); // NOI18N
        } else {
            buf.append(ObjectProperty.class.getName());
            buf.append(".create()"); // NOI18N
        }
        buf.append(", "); // NOI18N
        buf.append(generator.getExpressionJavaString(bindingDef.getTarget().getCodeExpression(), "this")); // NOI18N
        buf.append(", "); // NOI18N
        if (targetPath != null) {
            generateTargetProperty(bindingDef, buf);
        } else {
            buf.append(ObjectProperty.class.getName());
            buf.append(".create()"); // NOI18N
        }
        buildBindingNameCode(prop, buf);
        buf.append(");\n"); // NOI18N
    }

    private static void buildBindingNameCode(BindingProperty prop, StringBuilder buf) {
        MetaBinding bindingDef = prop.getValue();
        if (bindingDef.isNameSpecified()) {
            try {
                FormProperty property = prop.getNameProperty();
                Object value = property.getValue();
                if (value != null) {
                    buf.append(", "); // NOI18N
                    buf.append(property.getJavaInitializationString());
                }
            } catch (IllegalAccessException iaex) {
                Logger.getLogger(BindingDesignSupportImpl.class.getName()).log(Level.INFO, iaex.getMessage(), iaex);
            } catch (InvocationTargetException itex) {
                Logger.getLogger(BindingDesignSupportImpl.class.getName()).log(Level.INFO, itex.getMessage(), itex);
            }
        }
    }

    private static Binding createBinding0(MetaBinding bindingDef, Object source, Object target, BindingGroup group) {
        String name = null;
        if (bindingDef.isNameSpecified()) {
            BindingProperty prop = bindingDef.getTarget().getBindingProperty(bindingDef.getTargetPath());
            FormProperty nameProp = prop.getNameProperty();
            try {
                Object value = nameProp.getRealValue();
                if ((value != null) && (value instanceof String)) {
                    name = (String)value;
                }
            } catch (IllegalAccessException iaex) {
                Logger.getLogger(BindingDesignSupportImpl.class.getName()).log(Level.INFO, iaex.getMessage(), iaex);
            } catch (InvocationTargetException itex) {
                Logger.getLogger(BindingDesignSupportImpl.class.getName()).log(Level.INFO, itex.getMessage(), itex);
            }
            if ((name != null) && group.getBinding(name) != null) {
                Logger.getLogger(BindingDesignSupportImpl.class.getName()).log(Level.INFO, "More than one binding with name: {0}", name); // NOI18N
                name = null; // ignore name parameter
            }
        }
        AutoBinding.UpdateStrategy updateStrategy = AutoBinding.UpdateStrategy.READ_WRITE;
        switch (bindingDef.getUpdateStrategy()) {
            case MetaBinding.UPDATE_STRATEGY_READ_WRITE:
                updateStrategy = AutoBinding.UpdateStrategy.READ_WRITE;
                break;
            case MetaBinding.UPDATE_STRATEGY_READ:
                updateStrategy = AutoBinding.UpdateStrategy.READ;
                break;
            case MetaBinding.UPDATE_STRATEGY_READ_ONCE:
                updateStrategy = AutoBinding.UpdateStrategy.READ_ONCE;
                break;
            default: assert false;
        }
        Binding<Object,?,Object,?> binding;
        Property targetProperty = createTargetProperty(bindingDef);
        Property sourceProperty = (bindingDef.getSourcePath() == null) ? ObjectProperty.create() : createELProperty(bindingDef.getSourcePath());
        RADComponent targetComp = bindingDef.getTarget();
        String targetPath = bindingDef.getTargetPath();
        String sourcePath = bindingDef.getSourcePath();
        if ("elements".equals(targetPath) && javax.swing.JTable.class.isAssignableFrom(targetComp.getBeanClass())
                && ((source instanceof List) || (sourcePath != null))) { // NOI18N
            JTableBinding<Object,Object,Object> tableBinding;
            if (sourcePath == null) {
                tableBinding = SwingBindings.createJTableBinding(updateStrategy, (List)source, (JTable)target, name);
            } else {
                tableBinding = SwingBindings.createJTableBinding(updateStrategy, source, sourceProperty, (JTable)target, name);
            }
            if (bindingDef.hasSubBindings()) {
                Collection<MetaBinding> subBindings = bindingDef.getSubBindings();
                for (MetaBinding sub : subBindings) {
                    Property prop;
                    String subSourcePath = sub.getSourcePath();
                    if ((subSourcePath == null) || ("null".equals(subSourcePath))) { // NOI18N
                        prop = ObjectProperty.create();
                    } else {
                        prop = createELProperty(subSourcePath);
                    }
                    JTableBinding.ColumnBinding columnBinding = tableBinding.addColumnBinding(prop);
                    String title = sub.getParameter(MetaBinding.NAME_PARAMETER);
                    if (title == null) {
                        title = sub.getSourcePath();
                        if (isSimpleExpression0(title)) {
                            title = unwrapSimpleExpression0(title);
                            title = capitalize0(title);
                        }
                    }
                    columnBinding.setColumnName(title);
                    String columnClass = sub.getParameter(MetaBinding.TABLE_COLUMN_CLASS_PARAMETER);
                    if (columnClass != null) {
                        try {
                            if (columnClass.trim().endsWith(".class")) { // NOI18N
                                columnClass = columnClass.trim();
                                columnClass = columnClass.substring(0, columnClass.length()-6);
                            }
                            if (columnClass.indexOf('.') == -1) {
                                StringBuilder sb = new StringBuilder();
                                while (columnClass.endsWith("[]")) { // NOI18N
                                    columnClass = columnClass.substring(0, columnClass.length()-2);
                                    sb.append("["); // NOI18N
                                }
                                String prefix = sb.toString();
                                if ("".equals(prefix)) { // NOI18N
                                    columnClass = "java.lang." + columnClass; // NOI18N
                                } else {
                                    String suffix = columnClass;
                                    if (columnClass.equals("boolean")) { // NOI18N
                                        suffix = "Z"; // NOI18N
                                    } else if (columnClass.equals("byte")) { // NOI18N
                                        suffix = "B"; // NOI18N
                                    } else if (columnClass.equals("char")) { // NOI18N
                                        suffix = "C"; // NOI18N
                                    } else if (columnClass.equals("char")) { // NOI18N
                                        suffix = "D"; // NOI18N
                                    } else if (columnClass.equals("float")) { // NOI18N
                                        suffix = "F"; // NOI18N
                                    } else if (columnClass.equals("int")) { // NOI18N
                                        suffix = "I"; // NOI18N
                                    } else if (columnClass.equals("long")) { // NOI18N
                                        suffix = "J"; // NOI18N
                                    } else if (columnClass.equals("short")) { // NOI18N
                                        suffix = "S"; // NOI18N
                                    } else {
                                        prefix += "L"; // NOI18N
                                        if (suffix.indexOf('.') == -1) {
                                            suffix = "java.lang." + suffix; // NOI18N
                                        }
                                        suffix += ";"; // NOI18N
                                    }
                                    columnClass = prefix + suffix;
                                }
                            }
                            Class<?> clazz = FormUtils.loadClass(columnClass, bindingDef.getSource().getFormModel());
                            columnBinding.setColumnClass(clazz);
                        } catch (ClassNotFoundException cnfex) {
                            Logger.getLogger(BindingDesignSupportImpl.class.getName()).log(Level.INFO, cnfex.getMessage(), cnfex);
                        }
                    }
                    String editable = sub.getParameter(MetaBinding.EDITABLE_PARAMETER);
                    if (editable != null) {
                        Boolean value = "false".equals(editable) ? Boolean.FALSE : Boolean.TRUE; // NOI18N
                        columnBinding.setEditable(value);
                    }
                }
            }
            binding = tableBinding;
        } else if ("elements".equals(targetPath) && javax.swing.JList.class.isAssignableFrom(targetComp.getBeanClass())
                && ((source instanceof List) || (sourcePath != null))) { // NOI18N
            JListBinding listBinding;
            if (sourcePath == null) {
                listBinding = SwingBindings.createJListBinding(updateStrategy, (List)source, (JList)target, name);
            } else {
                listBinding = SwingBindings.createJListBinding(updateStrategy, source, sourceProperty, (JList)target, name);
            }
            String detailPath = bindingDef.getParameter(MetaBinding.DISPLAY_PARAMETER);
            if (detailPath != null) {
                listBinding.setDetailBinding(createELProperty(detailPath));
            }
            binding = listBinding;
        } else if ("elements".equals(targetPath) && javax.swing.JComboBox.class.isAssignableFrom(targetComp.getBeanClass())
                && ((source instanceof List) || (sourcePath != null))) { // NOI18N
            JComboBoxBinding comboBinding;
            if (sourcePath == null) {
                comboBinding = SwingBindings.createJComboBoxBinding(updateStrategy, (List)source, (JComboBox)target, name);
            } else {
                comboBinding = SwingBindings.createJComboBoxBinding(updateStrategy, source, sourceProperty, (JComboBox)target, name);
            }
//            String detailPath = bindingDef.getParameter(MetaBinding.DISPLAY_PARAMETER);
//            if (detailPath != null) {
//                comboBinding.setDetailBinding(createELProperty(detailPath));
//            }
            binding = comboBinding;
        } else {
            binding = Bindings.createAutoBinding(updateStrategy, source, sourceProperty, target, targetProperty, name);
        }
        return binding;
    }

    private static Binding createBinding(MetaBinding bindingDef,
                                         Object source, Object target,
                                         BindingGroup group,
                                         Map<Binding,BindingGroup> bindingToGroup) {
        Binding<Object,Object,Object,Object> binding = (Binding<Object,Object,Object,Object>)createBinding0(bindingDef, source, target, group);
        if (bindingDef.isNullValueSpecified()) {
            BindingProperty prop = bindingDef.getTarget().getBindingProperty(bindingDef.getTargetPath());
            FormProperty nullProp = prop.getNullValueProperty();
            try {
                Object value = nullProp.getRealValue();
                if ((value != null) && (value != FormDesignValue.IGNORED_VALUE)) {
                    binding.setSourceNullValue(value);
                }
            } catch (IllegalAccessException iaex) {
                Logger.getLogger(BindingDesignSupportImpl.class.getName()).log(Level.INFO, iaex.getMessage(), iaex);
            } catch (InvocationTargetException itex) {
                Logger.getLogger(BindingDesignSupportImpl.class.getName()).log(Level.INFO, itex.getMessage(), itex);
            }
        }
        if (bindingDef.isIncompletePathValueSpecified()) {
            BindingProperty prop = bindingDef.getTarget().getBindingProperty(bindingDef.getTargetPath());
            FormProperty incompleteProp = prop.getIncompleteValueProperty();
            try {
                Object value = incompleteProp.getRealValue();
                if ((value != null) && (value != FormDesignValue.IGNORED_VALUE)) {
                    binding.setSourceUnreadableValue(value);
                }
            } catch (IllegalAccessException iaex) {
                Logger.getLogger(BindingDesignSupportImpl.class.getName()).log(Level.INFO, iaex.getMessage(), iaex);
            } catch (InvocationTargetException itex) {
                Logger.getLogger(BindingDesignSupportImpl.class.getName()).log(Level.INFO, itex.getMessage(), itex);
            }
        }
        if (bindingDef.isConverterSpecified()) {
            BindingProperty prop = bindingDef.getTarget().getBindingProperty(bindingDef.getTargetPath());
            FormProperty converterProp = prop.getConverterProperty();
            try {
                Object value = converterProp.getRealValue();
                if ((value != null) && (value instanceof Converter)) {
                    binding.setConverter((Converter)value);
                }
            } catch (IllegalAccessException iaex) {
                Logger.getLogger(BindingDesignSupportImpl.class.getName()).log(Level.INFO, iaex.getMessage(), iaex);
            } catch (InvocationTargetException itex) {
                Logger.getLogger(BindingDesignSupportImpl.class.getName()).log(Level.INFO, itex.getMessage(), itex);
            }
        }
        if (bindingDef.isValidatorSpecified()) {
            BindingProperty prop = bindingDef.getTarget().getBindingProperty(bindingDef.getTargetPath());
            FormProperty validatorProp = prop.getValidatorProperty();
            try {
                Object value = validatorProp.getRealValue();
                if ((value != null) && (value instanceof Validator)) {
                    binding.setValidator((Validator)value);
                }
            } catch (IllegalAccessException iaex) {
                Logger.getLogger(BindingDesignSupportImpl.class.getName()).log(Level.INFO, iaex.getMessage(), iaex);
            } catch (InvocationTargetException itex) {
                Logger.getLogger(BindingDesignSupportImpl.class.getName()).log(Level.INFO, itex.getMessage(), itex);
            }
        }
        group.addBinding(binding);
        if (bindingToGroup != null) {
            bindingToGroup.put(binding, group);
        }
        
        try {
            binding.bind();
        } catch (Exception ex) {
            Logger.getLogger(BindingDesignSupportImpl.class.getName()).log(Level.INFO, ex.getMessage(), ex);
        }
        return binding;
    }

    private void removeBindings(MetaBinding bindingDef) {
        removeBindingInModel(bindingDef);
        List<Binding> establishedBindings = bindingsMap.get(bindingDef);
        if (establishedBindings != null) {
            for (Binding binding : establishedBindings) {
                removeBinding(binding);
            }
            bindingsMap.remove(bindingDef);
        }
    }

    private void removeBinding(Binding binding) {
        BindingGroup group = bindingToGroup.remove(binding);
        // It may happen that binding.bind() fails. Binding may
        // stay unbound in such situation and binding.unbind()
        // throws exception is this case
        if (binding.isBound()) {
            binding.unbind();
        }
        group.removeBinding(binding);
    }

    private void removeBindingInModel(MetaBinding bindingDef) {
        Binding binding = modelBindings.remove(bindingDef);
        if (binding != null) {
            removeBinding(binding);
        }
    }

    @Override
    public String capitalize(String title) {
        return capitalize0(title);
    }
    
    static String capitalize0(String title) {
        StringBuilder builder = new StringBuilder(title);
        boolean lastWasUpper = false;
        for (int i = 0; i < builder.length(); i++) {
            char aChar = builder.charAt(i);
            if (i == 0) {
                builder.setCharAt(i, Character.toUpperCase(aChar));
                lastWasUpper = true;
            } else if (Character.isUpperCase(aChar)) {
                if (!lastWasUpper) {
                    builder.insert(i, ' ');
                }
                lastWasUpper = true;
                i++;
            } else {
                lastWasUpper = false;
            }
        }
        return builder.toString();
    }

    @Override
    public BindingVisualReplicator createReplicator() {
        return new BindingVisualReplicatorImpl();
    }

    @Override
    public Class getBindingGroupClass() {
        return BindingGroup.class;
    }

    @Override
    public Class getConverterClass() {
        return Converter.class;
    }
    
    @Override
    public Class getValidatorClass() {
        return Validator.class;
    }

    @Override
    public boolean updateProjectForBeansBinding() {
        FormEditor formEditor = FormEditor.getFormEditor(formModel);
        if (formEditor != null
                && !ClassPathUtils.isOnClassPath(formEditor.getFormDataObject().getFormFile(), Binding.class.getName())) {
            try {
                Library lib = LibraryManager.getDefault().getLibrary("beans-binding"); // NOI18N
                if (lib == null) {
                    return false;
                }
                ClassSource cs = new ClassSource("", // class name is not needed // NOI18N
                                                 new ClassSourceResolver.LibraryEntry(lib));
                ClassPathUtils.updateProject(formEditor.getFormDataObject().getFormFile(), cs, true);
                return true;
            }
            catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        return false;
    }

    private class BindingVisualReplicatorImpl implements BindingVisualReplicator {
        private BindingGroup group;
        
        BindingVisualReplicatorImpl() {
            group = new BindingGroup();
            group.bind();
        }

        @Override
        public void addBinding(MetaBinding bindingDef, Object source, Object target, boolean inModel) {
            BindingDesignSupportImpl.this.addBinding(bindingDef, source, target, group, inModel);
        }

        @Override
        public void establishUpdatedBindings(RADComponent metacomp, boolean recursive, Map map, boolean inModel) {
            BindingDesignSupportImpl.this.establishUpdatedBindings(metacomp, recursive, map, group, inModel);
        }

        @Override
        public void establishOneOffBindings(RADComponent metacomp, boolean recursive, Map map) {
            BindingDesignSupportImpl.establishOneOffBindings(metacomp, recursive, map, group);
        }
        
    }

    /**
     * Form model listener that updates the bindings.
     */
    private class ModelListener implements FormModelListener {
        @Override
        public void formChanged(FormModelEvent[] events) {
            if (events == null)
                return;

            for (int i=0; i < events.length; i++) {
                FormModelEvent ev = events[i];
                switch (ev.getChangeType()) {
                    case FormModelEvent.BINDING_PROPERTY_CHANGED:
                        if (ev.getSubPropertyName() == null) {
                            changeBinding(ev.getOldBinding(), ev.getNewBinding());
                        }
                        break;
                    case FormModelEvent.COMPONENT_REMOVED:
                        releaseBindings(ev.getComponent(), true);
                        break;
                    case FormModelEvent.COMPONENT_ADDED:
                        if (!ev.getCreatedDeleted()) {
                            establishUpdatedBindings(ev.getComponent(), true, null, bindingGroup, true);
                        }
                        break;
                    default:
                }
            }
        }
    }

    static class ModifiableBoolean {
        boolean value;
    }
    
}
