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

package org.netbeans.modules.web.jsf.palette.items;

import java.awt.Dialog;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.JsfVersionUtils;
import org.netbeans.modules.web.jsf.palette.JSFPaletteUtilities;
import org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo;
import org.netbeans.modules.web.jsfapi.api.NamespaceUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public abstract class FromEntityBase {

    private static final String ITEM_VAR = "item";

    private boolean readOnly = false;

    protected JsfLibrariesSupport jsfLibrariesSupport;

    protected abstract boolean isCollectionComponent();

    protected abstract boolean showReadOnlyFormFlag();

    protected abstract String getDialogTitle();

    protected abstract String getTemplate(String templatesStyle);

    protected final boolean isReadOnlyForm() {
        return readOnly;
    }

    public boolean handleTransfer(JTextComponent targetComponent) {
        Project p = null;
        FileObject fo = JSFPaletteUtilities.getFileObject(targetComponent);
        jsfLibrariesSupport = PaletteUtils.getJsfLibrariesSupport(targetComponent);
        if (jsfLibrariesSupport == null) {
            return false;
        }
        if (fo != null) {
            p = FileOwnerQuery.getOwner(fo);
        }
        if (p == null) {
            return false;
        }

        ManagedBeanCustomizer mbc = new ManagedBeanCustomizer(p, isCollectionComponent(), showReadOnlyFormFlag());
        DialogDescriptor dd = new DialogDescriptor(mbc,
            getDialogTitle(),
            true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(dd);
            mbc.setDialog(dlg, dd);
            dlg.setVisible(true);
        } finally {
            if (dlg != null)
                dlg.dispose();
        }

        boolean accept = (dd.getValue() == DialogDescriptor.OK_OPTION && !mbc.isCancelled());
        readOnly = mbc.isReadOnly();
        if (accept) {
            try {
                boolean containsFView = isInViewTag(jsfLibrariesSupport, targetComponent);
                String managedBean = mbc.getManagedBeanProperty();
                if (managedBean != null && managedBean.lastIndexOf(".") != -1) {
                    managedBean = managedBean.substring(0, managedBean.lastIndexOf("."));
                }
                Charset encoding = FileEncodingQuery.getEncoding(fo);
                String body = expandTemplate(targetComponent, !containsFView, encoding,
                        mbc.getBeanClass(), managedBean, mbc.getManagedBeanProperty(), mbc.getTemplatesStyle());
                JSFPaletteUtilities.insert(body, targetComponent);
                jsfLibrariesSupport.importLibraries(DefaultLibraryInfo.HTML, DefaultLibraryInfo.JSF_CORE);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                accept = false;
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
                accept = false;
            }
        }
        return accept;
    }

    public static boolean isInViewTag(JsfLibrariesSupport jls, JTextComponent targetComponent) {
        try {
            Caret caret = targetComponent.getCaret();
            int position0 = Math.min(caret.getDot(), caret.getMark());
            int position1 = Math.max(caret.getDot(), caret.getMark());
            int len = targetComponent.getDocument().getLength() - position1;
            return targetComponent.getText(0, position0).contains(PaletteUtils.createViewTag(jls, targetComponent, false))
                    && targetComponent.getText(position1, len).contains(PaletteUtils.createViewTag(jls, targetComponent, true));
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
            // we don't know; let's assume we are:
            return true;
        }
    }

    public void insert(JTextComponent component) {
        handleTransfer(component);
    }

    private String expandTemplate(JTextComponent target, boolean surroundWithFView,
            Charset encoding, final String entityClass, final String managedBean,
            final String managedBeanProperty, String templatesStyle) throws IOException {
        final StringBuffer stringBuffer = new StringBuffer();
        if (surroundWithFView) {
            stringBuffer.append(PaletteUtils.createViewTag(jsfLibrariesSupport, target, false)).append("\n"); //NOI18N
        }
        FileObject targetJspFO = EntityClass.getFO(target);
        final Map<String, Object> params = createFieldParameters(targetJspFO, entityClass, 
                managedBean, managedBeanProperty, isCollectionComponent(), false, jsfLibrariesSupport);
        params.put("bundle", "bundle"); // NOI18N

        FileObject tableTemplate = FileUtil.getConfigRoot().getFileObject(getTemplate(templatesStyle));
        StringWriter w = new StringWriter();
        JSFPaletteUtilities.expandJSFTemplate(tableTemplate, params, encoding, w);
        stringBuffer.append(w.toString());

        if (surroundWithFView) {
            stringBuffer.append(PaletteUtils.createViewTag(jsfLibrariesSupport, target, true)).append("\n"); //NOI18N
        }
        return stringBuffer.toString();
    }

    public static Map<String, Object> createFieldParameters(FileObject targetJspFO, final String entityClass,
            final String managedBean, final String managedBeanProperty, final boolean collectionComponent,
            final boolean initValueGetters, JsfLibrariesSupport jls) throws IOException {
        final Map<String, Object> params = new HashMap<>();
        JavaSource javaSource = JavaSource.create(EntityClass.createClasspathInfo(targetJspFO));
        javaSource.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(entityClass);
                enumerateEntityFields(params, controller, typeElement, managedBeanProperty, collectionComponent, initValueGetters);
            }
        }, true);
        params.put("managedBean", managedBean); // NOI18N
        params.put("managedBeanProperty", managedBeanProperty); // NOI18N
        String entityName = entityClass;
        if (entityName.lastIndexOf(".") != -1) {
            entityName = entityName.substring(entityClass.lastIndexOf(".")+1);
        }
        params.put("entityName", entityName); // NOI18N
        if (jls != null) {
            params.put("prefixResolver", new PrefixResolver(jls));
        }

        // namespace location
        WebModule webModule = WebModule.getWebModule(targetJspFO);
        params.put("nsLocation", JSFUtils.getNamespaceDomain(webModule)); //NOI18N

        return params;
    }

    private static void enumerateEntityFields(Map<String, Object> params, CompilationController controller, 
            TypeElement bean, String managedBeanProperty, boolean collectionComponent, boolean initValueGetters) {
        List<TemplateData> templateData = new ArrayList<TemplateData>();
        List<FieldDesc> fields = new ArrayList<FieldDesc>();
        String idFieldName = "";
        if (bean != null) {
            ExecutableElement[] methods = JpaControllerUtil.getEntityMethods(bean);
            JpaControllerUtil.EmbeddedPkSupport embeddedPkSupport = null;
            for (ExecutableElement method : methods) {
                // filter out @Transient methods
                if (JpaControllerUtil.findAnnotation(method, "jakarta.persistence.Transient") != null //NOI18N
                        || JpaControllerUtil.findAnnotation(method, "javax.persistence.Transient") != null) { //NOI18N
                    continue;
                }

                FieldDesc fd = new FieldDesc(controller, method, bean, initValueGetters);
                if (fd.isValid()) {
                    int relationship = fd.getRelationship();
                    if (EntityClass.isId(method, fd.isFieldAccess())) {
                        fd.setPrimaryKey();
                        idFieldName = fd.getPropertyName();
                        TypeMirror rType = method.getReturnType();
                        if (TypeKind.DECLARED == rType.getKind()) {
                            DeclaredType rTypeDeclared = (DeclaredType)rType;
                            TypeElement rTypeElement = (TypeElement) rTypeDeclared.asElement();
                            if (JpaControllerUtil.isEmbeddableClass(rTypeElement)) {
                                if (embeddedPkSupport == null) {
                                    embeddedPkSupport = new JpaControllerUtil.EmbeddedPkSupport();
                                }
                                String propName = fd.getPropertyName();
                                for (ExecutableElement pkMethod : embeddedPkSupport.getPkAccessorMethods(bean)) {
                                    if (!embeddedPkSupport.isRedundantWithRelationshipField(bean, pkMethod)) {
                                        String pkMethodName = pkMethod.getSimpleName().toString();
                                        fd = new FieldDesc(controller, pkMethod, bean);
                                        fd.setLabel(pkMethodName.substring(3));
                                        fd.setPropertyName(propName + "." + JpaControllerUtil.getPropNameFromMethod(pkMethodName));
                                        fields.add(fd);
                                    }
                                }
                            } else {
                                fields.add(fd);
                            }
                            continue;
                        } else {
                            //primitive types
                            fields.add(fd);
                        }
                    } else if (fd.getDateTimeFormat().length() > 0) {
                        fields.add(fd);
                    } else if (relationship == JpaControllerUtil.REL_NONE || relationship == JpaControllerUtil.REL_TO_ONE) {
                        fields.add(fd);
                    }
                }
            }
        }

        processFields(params, templateData, controller, bean, fields, managedBeanProperty, collectionComponent);

        params.put("entityDescriptors", templateData); // NOI18N
        params.put("item", ITEM_VAR); // NOI18N
        params.put("comment", Boolean.FALSE); // NOI18N
        params.put("entityIdField", idFieldName); //NOI18N
    }

    private static ExecutableElement findPrimaryKeyGetter(CompilationController controller, TypeElement bean) {
        ExecutableElement[] methods = JpaControllerUtil.getEntityMethods(bean);
        for (ExecutableElement method : methods) {
            FieldDesc fd = new FieldDesc(controller, method, bean, false);
            if (fd.isValid()) {
                if (EntityClass.isId(method, fd.isFieldAccess())) {
                    return method;
                }
            }
        }
        return null;
    }

    private static void processFields(Map<String, Object> params, List<TemplateData> templateData,
            CompilationController controller, TypeElement bean, List<FieldDesc> fields, String managedBeanProperty,
            boolean collectionComponent) {
        for (FieldDesc fd : fields) {
            templateData.add(new TemplateData(fd, (collectionComponent ? ITEM_VAR : managedBeanProperty)+"."));
        }
    }

    public static void createParamsForConverterTemplate(final Map<String, Object> params, final FileObject targetJspFO,
            final String entityClass, final JpaControllerUtil.EmbeddedPkSupport embeddedPkSupport) throws IOException {
        JavaSource javaSource = JavaSource.create(EntityClass.createClasspathInfo(targetJspFO));
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(entityClass);
                createParamsForConverterTemplate(params, controller, typeElement, embeddedPkSupport);
            }
        }, true);
    }

    private static final String INDENT = "            "; // TODO: jsut reformat generated code
    
    private static void createParamsForConverterTemplate(Map<String, Object> params, CompilationController controller, 
            TypeElement bean, JpaControllerUtil.EmbeddedPkSupport embeddedPkSupport) throws IOException {
        // primary key type:
        ExecutableElement primaryGetter = findPrimaryKeyGetter(controller, bean);
        StringBuffer key = new StringBuffer();
        StringBuffer stringKey = new StringBuffer();
        String keyType;
        String keyTypeFQN;
        //
        String keyBodyValue = null;
        String keyStringBodyValue = null;
        String keyGetterValue = "UNDEFINED_PK_GETTER";
        String keyTypeValue = "UNDEFINED_PK_TYPE";
        Boolean keyEmbeddedValue = Boolean.FALSE;        //
        Boolean keyDerivedValue = Boolean.FALSE;
        List<EmbeddedDesc> embeddedFields = new ArrayList<EmbeddedDesc>();
        if(primaryGetter != null) {
            TypeMirror idType = primaryGetter.getReturnType();
            ExecutableElement primaryGetterDerived = null;
            if (TypeKind.DECLARED == idType.getKind()) {
                DeclaredType declaredType = (DeclaredType) idType;
                TypeElement idClass = (TypeElement) declaredType.asElement();
                boolean embeddable = idClass != null && JpaControllerUtil.isEmbeddableClass(idClass);
                boolean isDirevideId = false;
                if(!embeddable && JpaControllerUtil.haveId(idClass)){//NOI18N
                    isDirevideId = JpaControllerUtil.isRelationship(primaryGetter ,JpaControllerUtil.isFieldAccess(idClass)) != JpaControllerUtil.REL_NONE;
                }
                if(isDirevideId){
                    //it may be direved id, find id field in parent entity
                    primaryGetterDerived = findPrimaryKeyGetter(controller, idClass);
                    if(primaryGetterDerived !=null){
                        idType = primaryGetterDerived.getReturnType();
                        if (TypeKind.DECLARED == idType.getKind()){
                             declaredType = (DeclaredType) idType;
                             idClass = (TypeElement) declaredType.asElement();
                             embeddable = idClass != null && JpaControllerUtil.isEmbeddableClass(idClass);
                        }
                    } else {
                        idClass = null;//clean all, can't find getter in derived id
                    }
                }
                if(idClass !=null ){
                    keyType = idClass.getSimpleName().toString();
                    keyTypeFQN = idClass.getQualifiedName().toString();
                    if (embeddable) {
                        keyEmbeddedValue = Boolean.TRUE;
                        int index = 0;

                        // embeddedPkSupport handling
                        Set<ExecutableElement> methods = embeddedPkSupport.getPkAccessorMethods(bean);
                        for (ExecutableElement pkMethod : methods) {
                            if (embeddedPkSupport.isRedundantWithRelationshipField(bean, pkMethod)) {
                                embeddedFields.add(new EmbeddedDesc(
                                        "s" + pkMethod.getSimpleName().toString().substring(1),
                                        embeddedPkSupport.getCodeToPopulatePkField(bean, pkMethod)));
                            }
                        }

                        for (ExecutableElement method : ElementFilter.methodsIn(idClass.getEnclosedElements())) {
                            if (method.getSimpleName().toString().startsWith("set")) {
                                addParam(key, stringKey, method.getSimpleName().toString(), index,
                                        keyType, keyTypeFQN, method.getParameters().get(0).asType());
                                index++;
                            }
                        }
                        if (index == 0) {
                             key.append(NbBundle.getMessage(FromEntityBase.class, "ERR_NO_SETTERS", new String[]{INDENT, keyTypeFQN, "Converter.getKey()"}));//NOI18N;
                             stringKey.append(NbBundle.getMessage(FromEntityBase.class, "ERR_NO_SETTERS", new String[]{INDENT, keyTypeFQN, "Converter.getKey()"}));//NOI18N;
                        }
                    } else {
                        addParam(key, stringKey, null, -1, keyType, keyTypeFQN, idType);
                    }
                } else {
                    keyTypeFQN = null;
                }
            } else {
                //keyType = getCorrespondingType(idType);
                keyTypeFQN = keyType = idType.toString();
                addParam(key, stringKey, null, -1, keyType, keyTypeFQN, idType);
            }
            if(keyTypeFQN!=null){
                keyTypeValue = keyTypeFQN;
                if (key.toString().endsWith("\n")) {
                    key.setLength(key.length()-1);
                }
                keyBodyValue = key.toString();
                if (stringKey.toString().endsWith("\n")) {
                    stringKey.setLength(stringKey.length()-1);
                }
                keyStringBodyValue = stringKey.toString();
                keyGetterValue = primaryGetter.getSimpleName().toString() + (primaryGetterDerived != null ? "()."+primaryGetterDerived.getSimpleName().toString() : "");
            }
        } 
        //it's required to have getter for jsf creation
        params.put("keyBody", keyBodyValue!=null ? keyBodyValue : NbBundle.getMessage(FromEntityBase.class, "ERR_NO_GETTERS", new String[]{INDENT, bean.getQualifiedName().toString(), "Converter.getKey()"}));
        params.put("keyStringBody", keyStringBodyValue!=null ? keyStringBodyValue : NbBundle.getMessage(FromEntityBase.class, "ERR_NO_GETTERS", new String[]{INDENT, bean.getQualifiedName().toString(), "Converter.getKey()"}));
        params.put("keyGetter", keyGetterValue);//NOI18N
        params.put("keySetter", "s" + keyGetterValue.substring(1));//NOI18N
        params.put("keyType", keyTypeValue);//NOI18N
        params.put("keyEmbedded", keyEmbeddedValue);//NOI18N
        params.put("keyDerived", keyDerivedValue);//NOI18N
        params.put("embeddedIdFields", embeddedFields); //NOI18N
    }

    private static void addParam(StringBuffer key, StringBuffer stringKey, String setter,
            int index, String keyType, String keyTypeFQN, TypeMirror idType) {
        if (index == 0) {
            key.append(INDENT+"String values[] = value.split(SEPARATOR_ESCAPED);\n");
            key.append(INDENT+"key = new "+keyTypeFQN+"();\n");
        }
        if (index > 0) {
            stringKey.append(INDENT+"sb.append(SEPARATOR);\n");
        }

        // do conversion
        String conversion = getConversionFromString(idType, index, keyType);

        if (setter != null) {
            key.append(INDENT+"key."+setter+"("+conversion+");\n");
            stringKey.append(INDENT+"sb.append(value.g"+setter.substring(1)+"());\n");
        } else {
            key.append(INDENT+"key = "+conversion+";\n");
            stringKey.append(INDENT+"sb.append(value);\n");
        }
    }

    private static String getConversionFromString(TypeMirror idType, int index, String keyType) {
        String param = index == -1 ? "value" : "values["+index+"]";
        if (TypeKind.BOOLEAN == idType.getKind()) {
            return "Boolean.parseBoolean("+param+")";
        } else if (TypeKind.BYTE == idType.getKind()) {
            return "Byte.parseByte("+param+")";
        } else if (TypeKind.CHAR == idType.getKind()) {
            return param+".charAt(0)";
        } else if (TypeKind.DOUBLE == idType.getKind()) {
            return "Double.parseDouble("+param+")";
        } else if (TypeKind.FLOAT == idType.getKind()) {
            return "Float.parseFloat("+param+")";
        } else if (TypeKind.INT == idType.getKind()) {
            return "Integer.parseInt("+param+")";
        } else if (TypeKind.LONG == idType.getKind()) {
            return "Long.parseLong("+param+")";
        } else if (TypeKind.SHORT == idType.getKind()) {
            return "Short.parseShort("+param+")";
        } else if (TypeKind.DECLARED == idType.getKind()) {
            if ("Boolean".equals(idType.toString()) || "java.lang.Boolean".equals(idType.toString())) {
                return "Boolean.valueOf("+param+")";
            } else if ("Byte".equals(idType.toString()) || "java.lang.Byte".equals(idType.toString())) {
                return "Byte.valueOf("+param+")";
            } else if ("Character".equals(idType.toString()) || "java.lang.Character".equals(idType.toString())) {
                return "new Character("+param+".charAt(0))";
            } else if ("Double".equals(idType.toString()) || "java.lang.Double".equals(idType.toString())) {
                return "Double.valueOf("+param+")";
            } else if ("Float".equals(idType.toString()) || "java.lang.Float".equals(idType.toString())) {
                return "Float.valueOf("+param+")";
            } else if ("Integer".equals(idType.toString()) || "java.lang.Integer".equals(idType.toString())) {
                return "Integer.valueOf("+param+")";
            } else if ("Long".equals(idType.toString()) || "java.lang.Long".equals(idType.toString())) {
                return "Long.valueOf("+param+")";
            } else if ("Short".equals(idType.toString()) || "java.lang.Short".equals(idType.toString())) {
                return "Short.valueOf("+param+")";
            } else if ("BigDecimal".equals(idType.toString()) || "java.math.BigDecimal".equals(idType.toString())) {
                return "new java.math.BigDecimal("+param+")";
            } else if ("Date".equals(idType.toString()) || "java.util.Date".equals(idType.toString())) {
                return "java.sql.Date.valueOf("+param+")";
            }
        }
        return param;
    }

//    private static String getCorrespondingType(TypeMirror idType) {
//        if (TypeKind.BOOLEAN == idType.getKind()) {
//            return "boolean";
//        } else if (TypeKind.BYTE == idType.getKind()) {
//            return "byte";
//        } else if (TypeKind.CHAR == idType.getKind()) {
//            return "char";
//        } else if (TypeKind.DOUBLE == idType.getKind()) {
//            return "double";
//        } else if (TypeKind.FLOAT == idType.getKind()) {
//            return "float";
//        } else if (TypeKind.INT == idType.getKind()) {
//            return "int";
//        } else if (TypeKind.LONG == idType.getKind()) {
//            return "long";
//        } else if (TypeKind.SHORT == idType.getKind()) {
//            return "short";
//        } else {
//            return "UnknownType";
//        }
//    }

    public static final class FieldDesc {

        private ExecutableElement method;
        private String methodName;
        private String propertyName;
        private String label;
        private Boolean fieldAccess = null;
        private Integer relationship = null;
        private TypeElement bean;
        private CompilationController controller;
        private String dateTimeFormat = null;
        private String valuesGetter = "fixme";
        private boolean primaryKey;

        private FieldDesc(CompilationController controller, ExecutableElement method, TypeElement bean, boolean enableValueGetters) {
            this(controller, method, bean);
            if (enableValueGetters) {
                valuesGetter = null;
            }
        }

        private FieldDesc(CompilationController controller, ExecutableElement method, TypeElement bean) {
            this.controller = controller;
            this.method = method;
            this.bean = bean;
            this.methodName = method.getSimpleName().toString();
            this.propertyName = JpaControllerUtil.getPropNameFromMethod(getMethodName());
        }

        public boolean isPrimaryKey() {
            return primaryKey;
        }

        public void setPrimaryKey() {
            this.primaryKey = true;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getLabel() {
            if (label == null) {
                // there is check for getters, it should be long enough
                label = this.methodName.substring(3);
            }
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        private boolean isFieldAccess() {
            if (fieldAccess == null) {
                fieldAccess = JpaControllerUtil.isFieldAccess(bean);
            }
            return fieldAccess;
        }

        public boolean isValid() {
            return getMethodName().startsWith("get"); // NOI18N
        }

        public int getRelationship() {
            if (relationship == null) {
                relationship = JpaControllerUtil.isRelationship(method, isFieldAccess());
            }
            return relationship;
        }

        public String getDateTimeFormat() {
            if (dateTimeFormat == null) {
                dateTimeFormat = "";
                TypeMirror dateTypeMirror = controller.getElements().getTypeElement("java.util.Date").asType(); // NOI18N
                if (controller.getTypes().isSameType(dateTypeMirror, method.getReturnType())) {
                    String temporal = EntityClass.getTemporal(method, isFieldAccess());
                    if (temporal != null) {
                        dateTimeFormat = EntityClass.getDateTimeFormat(temporal);
                    }
                }
            }
            return dateTimeFormat;
        }

        private boolean isBlob() {
            Element fieldElement = isFieldAccess() ? JpaControllerUtil.guessField(method) : method;
            if (fieldElement == null) {
                fieldElement = method;
            }
            return JpaControllerUtil.isAnnotatedWith(fieldElement, "jakarta.persistence.Lob") // NOI18N
                    || JpaControllerUtil.isAnnotatedWith(fieldElement, "javax.persistence.Lob"); // NOI18N
        }

        @Override
        public String toString() {
            return "Field[" + // NOI18N
                    "methodName="+getMethodName()+ // NOI18N
                    ",propertyName="+getPropertyName()+ // NOI18N
                    ",label="+label+ // NOI18N
                    ",valid="+isValid()+ // NOI18N
                    ",field="+isFieldAccess()+ // NOI18N
                    ",relationship="+getRelationship()+ // NOI18N
                    ",datetime="+getDateTimeFormat()+ // NOI18N
                    ",valuesGetter="+getValuesGetter()+ // NOI18N
                    "]"; // NOI18N
        }

        private String getRelationClassName(CompilationController controller, ExecutableElement executableElement, boolean isFieldAccess) {
            TypeMirror passedReturnType = executableElement.getReturnType();
            if (TypeKind.DECLARED != passedReturnType.getKind() || !(passedReturnType instanceof DeclaredType)) {
                return null;
            }
            Types types = controller.getTypes();
            TypeMirror passedReturnTypeStripped = JpaControllerUtil.stripCollection((DeclaredType)passedReturnType, types);
            if (passedReturnTypeStripped == null) {
                return null;
            }
            TypeElement passedReturnTypeStrippedElement = (TypeElement) types.asElement(passedReturnTypeStripped);
            return passedReturnTypeStrippedElement.getSimpleName().toString();
        }

        public String getValuesGetter() {
            if (getRelationship() == JpaControllerUtil.REL_NONE) {
                return null;
            }
            if (valuesGetter == null) {
                String name = getRelationClassName(controller, method, isFieldAccess());
                if (name == null) {
                    valuesGetter = "";
                } else {
                    name = name.substring(0, 1).toLowerCase() + name.substring(1);
                    valuesGetter = name + "Controller." +
                        (getRelationship() == JpaControllerUtil.REL_TO_ONE ? "itemsAvailableSelectOne" : "itemsAvailableSelectMany");
                }
            }
            return valuesGetter;
        }

        private boolean isRequired() {
            return !JpaControllerUtil.isFieldOptionalAndNullable(method, isFieldAccess());
        }

        public String getReturnType() {
            return (String) method.getReturnType().toString();
        }
    }

    public static final class TemplateData {
        private FieldDesc fd;
        private String prefix;

        private TemplateData(FieldDesc fd, String prefix) {
            this.fd = fd;
            this.prefix = prefix;
        }

        public String getLabel() {
            return fd.getLabel();
        }

        public String getName() {
            return prefix+fd.getPropertyName();
        }

        public String getDateTimeFormat() {
            return fd.getDateTimeFormat();
        }

        public String getReturnType() {
            return fd.getReturnType();
        }

        public boolean isBlob() {
            return fd.isBlob();
        }

        public boolean isRelationshipOne() {
            return fd.getRelationship() == JpaControllerUtil.REL_TO_ONE;
        }

        public boolean isRelationshipMany() {
            return fd.getRelationship() == JpaControllerUtil.REL_TO_MANY;
        }

        public String getId() {
            return fd.getPropertyName();
        }

        public boolean isRequired() {
            return fd.isRequired();
        }

        public String getValuesGetter() {
            return fd.getValuesGetter();
        }

        @Override
        public String toString() {
            return "TemplateData[fd="+fd+",prefix="+prefix+"]"; // NOI18N
        }

    }

    public static final class EmbeddedDesc {

        private final String embeddedSetter;
        private final String codeToPopulate;

        public EmbeddedDesc(String embeddedSetter, String codeToPopulate) {
            this.embeddedSetter = embeddedSetter;
            this.codeToPopulate = codeToPopulate;
        }

        public String getEmbeddedSetter() {
            return embeddedSetter;
        }

        public String getCodeToPopulate() {
            return codeToPopulate;
        }
    }

}
