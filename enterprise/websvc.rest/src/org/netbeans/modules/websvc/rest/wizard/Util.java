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

package org.netbeans.modules.websvc.rest.wizard;

import com.sun.jersey.core.impl.provider.entity.Inflector;
import java.awt.Component;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import java.awt.Container;
import javax.swing.JComponent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.EnumSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.FacadeGenerator;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.FacadeGeneratorProvider;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.rest.codegen.Constants;
import org.netbeans.modules.websvc.rest.codegen.EntityResourcesGenerator;
import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBeanModel;
import org.netbeans.modules.websvc.rest.codegen.model.TypeUtil;
import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo.FieldInfo;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.support.PersistenceHelper;
import org.netbeans.modules.websvc.rest.support.PersistenceHelper.PersistenceUnit;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.modules.websvc.rest.wizard.fromdb.EjbFacadeGeneratorProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import org.netbeans.modules.websvc.rest.spi.MiscUtilities;

/**
 * Copy of j2ee/utilities Util class
 *  
 * TODO: Should move some of the methods into o.n.m.w.r.support.Utils class
 * since that's the package used for sharing all the utility classes.
 * 
 */
public class Util {
    
    public static final String XMLROOT_ANNOTATION = 
        "javax.xml.bind.annotation.XmlRootElement";         // NOI18N
    
    public static final String XML_TRANSIENT = 
            "javax.xml.bind.annotation.XmlTransient";       // NOI18N
    
    public static final String TYPE_DOC_ROOT="doc_root"; //NOI18N
    
    /*
     * Changes the text of a JLabel in component from oldLabel to newLabel
     */
    public static void changeLabelInComponent(JComponent component, String oldLabel, String newLabel) {
        JLabel label = findLabel(component, oldLabel);
        if(label != null) {
            label.setText(newLabel);
        }
    }
    
    /*
     * Hides a JLabel and the component that it is designated to labelFor, if any
     */
    public static void hideLabelAndLabelFor(JComponent component, String lab) {
        JLabel label = findLabel(component, lab);
        if(label != null) {
            label.setVisible(false);
            Component c = label.getLabelFor();
            if(c != null) {
                c.setVisible(false);
            }
        }
    }
    
    /*
     * Recursively gets all components in the components array and puts it in allComponents
     */
    public static void getAllComponents(Component[] components, Collection<Component> allComponents) {
        for( int i = 0; i < components.length; i++ ) {
            if( components[i] != null ) {
                allComponents.add( components[i] );
                if( ( ( Container )components[i] ).getComponentCount() != 0 ) {
                    getAllComponents( ( ( Container )components[i] ).getComponents(), allComponents );
                }
            }
        }
    }

    /*
     *  Recursively finds a JLabel that has labelText in comp
     */
    public static JLabel findLabel(JComponent comp, String labelText) {
        List<Component> allComponents = new ArrayList<Component>();
        getAllComponents(comp.getComponents(), allComponents);
        for (Component c : allComponents) {
            if(c instanceof JLabel) {
                JLabel label = (JLabel)c;
                if(label.getText().equals(labelText)) {
                    return label;
                }
            }
        }
        return null;
    }

    /**
     * Returns the simple class for the passed fully-qualified class name.
     */
    public static String getClassName(String fqClassName) {
        int dot = fqClassName.lastIndexOf("."); // NOI18N
        if (dot >= 0 && dot < fqClassName.length() - 1) {
            return fqClassName.substring(dot + 1);
        } else {
            return fqClassName;
        }
    }
    
    /**
     * Returns the package name of the passed fully-qualified class name.
     */
    public static String getPackageName(String fqClassName) {
        int dot = fqClassName.lastIndexOf("."); // NOI18N
        if (dot >= 0 && dot < fqClassName.length() - 1) {
            return fqClassName.substring(0, dot);
        } else {
            return ""; // NOI18N
        }
    }
    
    /**
     * Returns the SourceGroup of the passesd project which contains the
     * fully-qualified class name.
     */
    public static SourceGroup getClassSourceGroup(Project project, String fqClassName) {
        String classFile = fqClassName.replace('.', '/') + ".java"; // NOI18N
        SourceGroup[] sourceGroups = SourceGroupSupport.getJavaSourceGroups(project);
        
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject classFO = sourceGroup.getRootFolder().getFileObject(classFile);
            if (classFO != null) {
                return sourceGroup;
            }
        }
        return null;
    }
    
    static final String WIZARD_PANEL_CONTENT_DATA = WizardDescriptor.PROP_CONTENT_DATA; // NOI18N
    static final String WIZARD_PANEL_CONTENT_SELECTED_INDEX = WizardDescriptor.PROP_CONTENT_SELECTED_INDEX; //NOI18N;
    
    public static void mergeSteps(WizardDescriptor wizard, WizardDescriptor.Panel[] panels, String[] steps) {
        Object prop = wizard.getProperty(WIZARD_PANEL_CONTENT_DATA);
        String[] beforeSteps;
        int offset;
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
            offset = beforeSteps.length;
            if (offset > 0 && ("...".equals(beforeSteps[offset - 1]))) {// NOI18N
                offset--;
            }
        } else {
            beforeSteps = null;
            offset = 0;
        }
        String[] resultSteps = new String[ (offset) + panels.length];
        for (int i = 0; i < offset; i++) {
            resultSteps[i] = beforeSteps[i];
        }
        setSteps(panels, steps, resultSteps, offset);
    }
    
    private static void setSteps(WizardDescriptor.Panel[] panels, String[] steps, String[] resultSteps, int offset) {
        int n = steps == null ? 0 : steps.length;
        for (int i = 0; i < panels.length; i++) {
            final JComponent component = (JComponent) panels[i].getComponent();
            String step = i < n ? steps[i] : null;
            if (step == null) {
                step = component.getName();
            }
            component.putClientProperty(WIZARD_PANEL_CONTENT_DATA, resultSteps);
            component.putClientProperty(WIZARD_PANEL_CONTENT_SELECTED_INDEX, i);
            component.getAccessibleContext().setAccessibleDescription(step);
            resultSteps[i + offset] = step;
        }
    }
    
    public static void setSteps(WizardDescriptor.Panel[] panels, String[] steps) {
        setSteps(panels, steps, steps, 0);
    }
    
    public static String lowerFirstChar(String name) {
        if (name.length() == 0) {
            return name;
        }

        StringBuilder sb = new StringBuilder(name);
        sb.setCharAt(0, Character.toLowerCase(name.charAt(0)));
        return sb.toString();
    }
    
    public static String upperFirstChar(String name) {
        if (name.length() == 0) {
            return name;
        }

        StringBuilder sb = new StringBuilder(name);
        sb.setCharAt(0, Character.toUpperCase(name.charAt(0)));
        return sb.toString();
    }
    
    public static String deriveResourceClassName(String resourceName) {
        return upperFirstChar(resourceName) + EntityResourcesGenerator.RESOURCE_SUFFIX;
    }

    public static String deriveUri(String resourceName, String currentUri) {
        if (resourceName.length() == 0 || currentUri == null || currentUri.length() == 0 || currentUri.charAt(0) != '/') {
            return currentUri;
        }
        resourceName = lowerFirstChar(resourceName);
        resourceName = pluralize(resourceName);
        String root = currentUri;
        String params = null;
        int lastIndex = currentUri.indexOf('{');
        if (lastIndex > -1) {
            params = root.substring(lastIndex-1);
            root = root.substring(0, lastIndex-1); /* ../{id} we are excluding the ending '/' */
            if (root.length() == 0) {
                return currentUri;
            }
        }


        lastIndex = root.lastIndexOf('/');
        if (lastIndex == -1) {
            return currentUri;
        }

        root = root.substring(0, lastIndex);
        String ret = root + "/" + resourceName;
        if (params != null) {
            ret += params;
        }
        return ret;
    }

    public static String deriveContainerClassName(String resourceName) {
        return deriveResourceClassName(Inflector.getInstance().pluralize((resourceName)));
    }

    public static String pluralize(String name) {
        String pluralName = Inflector.getInstance().pluralize(name);
        
        if (name.equals(pluralName)) {
            return name + Constants.COLLECTION;         //NOI18N
        } else {
            return pluralName;
        }
    }

    public static String[] ensureTypes(String[] types) {
        if (types == null || types.length == 0 || types[0].length() == 0) {
            types = new String[] { String.class.getName() };
        }
        return types;
    }
    
    public static SourceGroup[] getSourceGroups(Project project) {
        SourceGroup[] sourceGroups = null;

        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] docRoot = sources.getSourceGroups(TYPE_DOC_ROOT);
        SourceGroup[] srcRoots = SourceGroupSupport.getJavaSourceGroups(project);
            
        if (docRoot != null && srcRoots != null) {
            sourceGroups = new SourceGroup[docRoot.length + srcRoots.length];
            System.arraycopy(docRoot, 0, sourceGroups, 0, docRoot.length);
            System.arraycopy(srcRoots, 0, sourceGroups, docRoot.length, srcRoots.length);
        }
            
        if (sourceGroups==null || sourceGroups.length==0) {
            sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        }
        return sourceGroups;
    }

    public static Class getType(Project project, String typeName) {    
        List<ClassPath> classPaths = SourceGroupSupport.gerClassPath(project);
        
        for (ClassPath cp : classPaths) {
            try {
                Class ret = Util.getPrimitiveType(typeName);
                if (ret != null) {
                    return ret;
                }
                ClassLoader cl = cp.getClassLoader(true);
                ret = getGenericRawType(typeName, cl);
                if (ret != null) {
                    return ret;
                }
                if (cl != null) {
                    return cl.loadClass(typeName);
                }
            } catch (ClassNotFoundException ex) {
                //Logger.global.log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }
        return null; 
    }
    
    public static Class<?> getPrimitiveType(String typeName) {
        return Lazy.primitiveTypes.get(typeName);
    }
    
    public static Class getGenericRawType(String typeName, ClassLoader loader) {
        int i = typeName.indexOf('<');
        if (i < 1) {
            return null;
        }
        String raw = typeName.substring(0, i);
        try {
            return loader.loadClass(raw);
        } catch(ClassNotFoundException ex) {
            Logger.global.log(Level.INFO, "", ex);
            return null;
        }
    }
    
    public static boolean isValidPackageName(String packageName) {
        if (packageName == null || packageName.endsWith(".")) {
            return false;
        }
        
        String[] segments = packageName.split("\\.");
        for (String s : segments) {
            if (! Utilities.isJavaIdentifier(s)) {
                return false;
            }
        }
        return true;
    }
    
    public static ClasspathInfo getClasspathInfo(Project p) {
        FileObject fileObject = p.getProjectDirectory();
        return ClasspathInfo.create(
                ClassPath.getClassPath(fileObject, ClassPath.BOOT), // JDK classes
                ClassPath.getClassPath(fileObject, ClassPath.COMPILE), // classpath from dependent projects and libraries
                ClassPath.getClassPath(fileObject, ClassPath.SOURCE)); // source classpath
    }

    public static PersistenceUnit getPersistenceUnit(WizardDescriptor wizard, Project project) {
        PersistenceUnit pu = (PersistenceUnit) wizard.getProperty(WizardProperties.PERSISTENCE_UNIT);
        if (pu == null) {
            pu = new PersistenceHelper(project).getPersistenceUnit();
            wizard.putProperty(WizardProperties.PERSISTENCE_UNIT, pu);
        }
        return pu;
    }
    
    public static boolean isValidUri(String uri) {
        StringTokenizer segments = new StringTokenizer(uri, "/ "); //NOI18N
        Set<String> uriParts = new HashSet<String>();
        while (segments.hasMoreTokens()) {
            String segment = segments.nextToken();
            if (segment.startsWith("{")) { //NOI18N
                if (segment.length() > 2 && segment.endsWith("}")) { //NOI18N
                    String uriPart = segment.substring(1, segment.length() - 1);
                    if (!Utilities.isJavaIdentifier(uriPart)) {
                        return false;
                    }
                    if (uriParts.contains(uriPart)) {
                        return false;
                    } else {
                        uriParts.add(uriPart);
                    }

                } else {
                    return false;
                }
            } else {
                if (segment.contains("{") || segment.contains("}")) { //NOI18N
                    return false;
                }
            }
        }
        return true;
    }
    
    public static boolean isCDIEnabled(FileObject fileObject) {
        Project project = FileOwnerQuery.getOwner(fileObject);
        if ( project != null ){
            return isCDIEnabled( project );
        }
        return false;
    }
    
    public static boolean isCDIEnabled(Project project) {
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            if (  !MiscUtilities.isJavaEE6AndHigher(project))
            {
                return false;
            }
            FileObject confRoot = wm.getWebInf();
            if (confRoot!=null && confRoot.getFileObject("beans.xml")!=null) {  //NOI18N
                return true;
            }
        }
        return false;
    }
    
    public static void generatePrimaryKeyMethod(final FileObject restResourceClass,
            String entityFqn, EntityResourceBeanModel model ) throws IOException
    {
        EntityClassInfo entityInfo = model.getEntityInfo(entityFqn);
        // bug 234460 : don't generate primary key method for not resolved entity class
        if (entityInfo == null) {
            return;
        }
        final FieldInfo idFieldInfo = entityInfo.getIdFieldInfo();
        if ( idFieldInfo!= null && idFieldInfo.isEmbeddedId() && idFieldInfo.getType()!= null){
            final String idType = idFieldInfo.getType();
            JavaSource javaSource = JavaSource.forFileObject( restResourceClass );
            Task<WorkingCopy> task = new Task<WorkingCopy>() {
                
                @Override
                public void run(WorkingCopy workingCopy) throws Exception {
                    workingCopy.toPhase(Phase.RESOLVED);
                    CompilationUnitTree tree = workingCopy.getCompilationUnit();
                    
                    TreeMaker maker = workingCopy.getTreeMaker();
                    Tree returnTypeTree = JavaSourceHelper.createTypeTree(workingCopy, 
                            idType);      
                    ModifiersTree modifiersTree = JavaSourceHelper.createModifiersTree(
                            workingCopy,new Modifier[]{Modifier.PRIVATE} , 
                            null, null);
                    List<VariableTree> vars = new ArrayList<VariableTree>();
                    
                    VariableTree var = maker.Variable(maker.Modifiers(
                            EnumSet.noneOf(Modifier.class)), 
                            "pathSegment", JavaSourceHelper.createTypeTree(workingCopy, 
                                    "javax.ws.rs.core.PathSegment"), null);     // NOI18N
                    vars.add(var);
                    
                    MethodTree methodTree = maker.Method(modifiersTree, 
                            "getPrimaryKey", returnTypeTree, 
                            Collections.<TypeParameterTree>emptyList(), 
                            vars, 
                            Collections.<ExpressionTree>emptyList(), 
                            getBody(idFieldInfo, workingCopy), null);

                    for (Tree typeDeclaration : tree.getTypeDecls()){
                        if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDeclaration.getKind())){
                            ClassTree classTree = (ClassTree) typeDeclaration;
                            ClassTree newTree = maker.addClassMember(classTree, methodTree);
                            workingCopy.rewrite(classTree, newTree);
                        }
                    }
                }

                private String getBody( FieldInfo idField, WorkingCopy workingCopy ) {
                    StringBuilder builder = new StringBuilder("{ ");              // NOI18N
                    builder.append(" /* \n");                                     // NOI18N
                    builder.append(" * pathSemgent represents a URI path segment ");// NOI18N
                    builder.append("and any associated matrix parameters.\n");    // NOI18N
                    builder.append(" * URI path part is supposed to be in ");     // NOI18N
                    builder.append("form of 'somePath");
                    Collection<FieldInfo> fieldInfos = idField.getFieldInfos();
                    for (FieldInfo fieldInfo : fieldInfos) {
                        String name = fieldInfo.getName();
                        builder.append(';');
                        builder.append(name);
                        builder.append('=');
                        builder.append(name);
                        builder.append("Value");
                    }
                    builder.append("'.\n");                                        // NOI18N
                    builder.append(" * Here 'somePath' is a result of getPath() ");// NOI18N
                    builder.append("method invocation and \n");                   // NOI18N
                    builder.append(" * it is ignored in the following code.\n");  // NOI18N
                    builder.append(" * Matrix parameters are used as field names");// NOI18N
                    builder.append(" to build a primary key instance.\n");         // NOI18N
                    builder.append(" */");
                    if ( idField.hasEmptyCtor() ){
                        builder.append(idField.getType());
                        builder.append(" key=new ");                        // NOI18N
                        builder.append(idField.getType());
                        builder.append("();");                              // NOI18N

                        boolean constructed = true;
                        StringBuilder keyBuidler = new StringBuilder(
                                "javax.ws.rs.core.MultivaluedMap<String,String>");// NOI18N
                        keyBuidler.append(" map = pathSegment.getMatrixParameters();");// NOI18N
                        for (FieldInfo fieldInfo : fieldInfos) {
                            String name = fieldInfo.getName();
                            keyBuidler.append("java.util.List<String> ");    // NOI18N   
                            keyBuidler.append( name );
                            keyBuidler.append( "=map.get(\"");               // NOI18N 
                            keyBuidler.append( name );
                            keyBuidler.append( "\");" );                     // NOI18N 
                            keyBuidler.append("if ( ");
                            keyBuidler.append( name );
                            keyBuidler.append("!=null && !");                // NOI18N
                            keyBuidler.append(name);
                            keyBuidler.append(".isEmpty()){");               // NOI18N 
                            String stringConverter = fieldInfo.getStringConverterMethod();
                            if ( stringConverter == null ){
                                constructed = false;
                                keyBuidler.append(" // TODO : set ");        // NOI18N 
                                keyBuidler.append(name);
                                keyBuidler.append(" field value for key\n"); // NOI18N 
                                continue;
                            }
                            if ( fieldInfo.isArray()){
                                keyBuidler.append(fieldInfo.getType());
                                keyBuidler.append(" field=new ");            // NOI18N
                                keyBuidler.append(fieldInfo.getType());
                                keyBuidler.deleteCharAt( keyBuidler.length()-1);
                                keyBuidler.append(name);
                                keyBuidler.append(".size()];");               // NOI18N
                                keyBuidler.append("for( int i=0;i<");         // NOI18N
                                keyBuidler.append(name);
                                keyBuidler.append(".size();i++){");           // NOI18N
                                keyBuidler.append("field[i]=");               // NOI18N
                                keyBuidler.append(stringConverter);
                                keyBuidler.append('(');
                                keyBuidler.append(name);
                                keyBuidler.append(".get(i));");               // NOI18N
                                keyBuidler.append('}');
                                keyBuidler.append("key.");                    // NOI18N
                                keyBuidler.append(getSetterName(fieldInfo));
                                keyBuidler.append("(field);");                // NOI18N        
                            }
                            else {
                                keyBuidler.append("key.");                    // NOI18N
                                keyBuidler.append(getSetterName(fieldInfo));
                                keyBuidler.append('(');
                                if ( String.class.getCanonicalName().
                                        equals(fieldInfo.getType()))
                                {
                                    keyBuidler.append(name);
                                    keyBuidler.append(".get(0));");               // NOI18N
                                }
                                else {
                                    keyBuidler.append(stringConverter);
                                    keyBuidler.append('(');
                                    keyBuidler.append(name);
                                    keyBuidler.append(".get(0)));");              // NOI18N
                                }
                            }
                            keyBuidler.append('}');
                        }
                        if ( constructed ){
                            builder.append( keyBuidler );
                        }
                        else {
                            builder.append("/*\n");
                            builder.append(" * TODO: put your code here to build");// NOI18N
                            builder.append(" a primary key instance.\n");          // NOI18N
                            builder.append(" * See below the possible algorithm ");// NOI18N
                            builder.append("to do it.\n */");                      // NOI18N
                        }
                        builder.append("return key;");                             // NOI18N
                    }
                    else {
                        addToDo(builder);
                        builder.append("return null;");                            // NOI18N
                    }
                    builder.append(" }");                                          // NOI18N
                    return builder.toString();
                }
                
                private void addToDo(StringBuilder builder ){
                    builder.append(" // TODO: put your code here to create ");  // NOI18N
                    builder.append("a primary key instance based on requested");// NOI18N
                    builder.append(" URI represented by pathSegment\n");        // NOI18N
                }
            };
            javaSource.runModificationTask(task).commit();
        }
    }
    
    public static String getGetterName(FieldInfo fieldInfo) {
        return "get" + capitalizeFirstLetter(fieldInfo.getName());      //NOI18N
    }
    
    public static String getSetterName(FieldInfo fieldInfo) {
        return "set" + capitalizeFirstLetter(fieldInfo.getName());      //NOI18N
    }
    
    public static void generateRESTFacades(Project project, Set<String> entities,
            EntityResourceBeanModel model, FileObject targetFolder, 
            String resourcePackage ) throws IOException
    {
        generateRESTFacades(project, entities, model, targetFolder, 
                resourcePackage, getEjbFacadeGenerator(model));
    }
    
    public static void generateRESTFacades(Project project, Set<String> entities,
            EntityResourceBeanModel model, FileObject targetFolder, 
            String resourcePackage, FacadeGenerator generator ) throws IOException
    {
        Map<String,String> beanMap = new HashMap<String,String>();
        for (EntityClassInfo classInfo : model.getEntityInfos()) {
            EntityClassInfo.FieldInfo fieldInfo = classInfo.getIdFieldInfo();
            if (fieldInfo != null) {
                beanMap.put(classInfo.getType(), fieldInfo.getType());
            }
        }

        Map<String, String> selectedEntityNames = new HashMap<String, String>();
        for( String entity : entities ){
            String primaryKeyType = beanMap.get(entity);
            selectedEntityNames.put(entity,
                    (primaryKeyType == null ? String.class.getName() : primaryKeyType)); 
        }
        
        Map<String, String> entityNames = initEntityNames(project);

        for (Entry<String, String> entry : selectedEntityNames.entrySet()) {
            generator.generate(project, entityNames, targetFolder, 
                    entry.getKey(), entry.getValue(), resourcePackage, false, 
                    false, true);
        }
    }
    
    public static Set<String> getEntities(Project project, Set<FileObject> files) 
        throws IOException 
    {
        final Set<String> entities = new HashSet<String>();
        for (FileObject file : files) {
            final JavaSource source = JavaSource.forFileObject(file);
            if (source == null) {
                continue;
            }
            final EntityCollector collector = new EntityCollector();
            source.runUserActionTask( collector, true);
            if ( collector.isIncomplete() && 
                    org.netbeans.api.java.source.SourceUtils.isScanInProgress())
            {
                final Runnable runnable = new Runnable(){
                    @Override
                    public void run() {
                        try {
                            source.runUserActionTask(collector, true);
                            if (collector.getEntityFqn()!=null) {
                                entities.add( collector.getEntityFqn());
                            }
                        }
                        catch( IOException e ){
                            Logger.getLogger(Util.class.getCanonicalName()).log(
                                    Level.WARNING, null , e);
                        }
                    }
                };
                if ( SwingUtilities.isEventDispatchThread()){
                    ScanDialog.runWhenScanFinished(runnable, NbBundle.getMessage(
                            Util.class, "LBL_AnalyzeEntities"));        // NOI18N
                }
                else {
                    SwingUtilities.invokeLater( new Runnable(){
                        @Override
                        public void run() {
                            ScanDialog.runWhenScanFinished(runnable, NbBundle.getMessage(
                                    Util.class, "LBL_AnalyzeEntities"));        // NOI18N
                        }
                    });
                }
            }
            else if (collector.getEntityFqn()!=null) {
                entities.add( collector.getEntityFqn());
            }
        }
        return entities;
    }

    
    public static Map<String, String> initEntityNames(Project project) throws IOException {
        final Map<String, String> entityNames = new HashMap<String, String>();
        
        //XXX should probably be using MetadataModelReadHelper. needs a progress indicator as well (#113874).
            EntityClassScope entityClassScope = EntityClassScope.
                getEntityClassScope(project.getProjectDirectory());
            MetadataModel<EntityMappingsMetadata> entityMappingsModel = 
                entityClassScope.getEntityMappingsModel(true);
            entityMappingsModel.runReadAction(
                    new MetadataModelAction<EntityMappingsMetadata, Void>() {

                @Override
                public Void run(EntityMappingsMetadata metadata) throws Exception {
                    for (Entity entity : metadata.getRoot().getEntity()) {
                        entityNames.put(entity.getClass2(), entity.getName());
                    }
                    return null;
                }
            });
        return entityNames;
    }
    
    public static void modifyEntity( final String entityFqn , Project project) {
        try {
            FileObject entityFileObject = SourceGroupSupport.
                getFileObjectFromClassName(entityFqn, project);

            if (entityFileObject == null) {
                return; 
            }
            if ( !entityFileObject.canWrite()) {
                return;
            }
            final JavaSource javaSource = JavaSource.forFileObject(entityFileObject);
            if (javaSource == null) {
                return;
            }
            final boolean isIncomplete[] = new boolean[1];
            final Task<CompilationController> task = new Task<CompilationController>(){
                @Override
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase(Phase.RESOLVED);
                    
                    isIncomplete[0] = controller.getElements().getTypeElement(
                            XMLROOT_ANNOTATION) == null || controller.getElements().
                                getTypeElement(XML_TRANSIENT) == null;
                }
            };
            
            javaSource.runUserActionTask(task, true);
            if ( isIncomplete[0] && 
                    org.netbeans.api.java.source.SourceUtils.isScanInProgress())
            {
                final Runnable runnable = new Runnable(){
                    @Override
                    public void run() {
                        try {
                            javaSource.runUserActionTask(task, true);
                        }
                        catch( IOException e ){
                            Logger.getLogger( Util.class.getCanonicalName()).
                                log(Level.WARNING, null, e );
                        }
                    }
                };
                if ( SwingUtilities.isEventDispatchThread() ){
                    ScanDialog.runWhenScanFinished( runnable, NbBundle.
                            getMessage(Util.class, "LBL_EntityModification"));      // NOI18N
                }
                else {
                    try {
                        SwingUtilities.invokeAndWait( new Runnable(){
                            @Override
                            public void run() {
                                ScanDialog.runWhenScanFinished( runnable, NbBundle.
                                   getMessage(Util.class, "LBL_EntityModification"));      // NOI18N
                            }
                        });
                    }
                    catch( InterruptedException e ){
                        return;
                    }
                    catch( InvocationTargetException e ){
                        return;
                    }
                }
            }
            if (isIncomplete[0]) {
                return;
            }
            
            ModificationResult result = javaSource
                    .runModificationTask(new Task<WorkingCopy>() {

                        @Override
                        public void run( final WorkingCopy working )
                                throws IOException
                        {
                            working.toPhase(Phase.RESOLVED);

                            TreeMaker maker = working.getTreeMaker();
                            addXmlRootAnnotation(working, maker );
                            addXmlTransientAnnotation(working, maker);
                        }
                    });
            result.commit();
        }
        catch (IOException e) {
            Logger.getLogger(Util.class.getName()).
                log( Level.SEVERE, null, e);
        }
    }
    
    private static String capitalizeFirstLetter(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    private static void addXmlTransientAnnotation(WorkingCopy workingCopy, 
            TreeMaker maker)
    {
        GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
        AnnotationTree xmlTransientAn = genUtils.createAnnotation(XML_TRANSIENT);
        TypeElement jsonIgnore = workingCopy.getElements().getTypeElement(
            "org.codehaus.jackson.annotate.JsonIgnore");    // NOI18N
        List<AnnotationTree> annotationTrees;
        if ( jsonIgnore == null ){
            annotationTrees = Collections.singletonList(xmlTransientAn);
        }
        else {
            AnnotationTree jsonIgnoreAn = genUtils.createAnnotation(
                jsonIgnore.getQualifiedName().toString());
            annotationTrees = new ArrayList<AnnotationTree>(2);
            annotationTrees.add( xmlTransientAn);
            annotationTrees.add(jsonIgnoreAn);
        }
        TypeElement entityElement = 
                workingCopy.getTopLevelElements().get(0);
        List<ExecutableElement> methods = ElementFilter.methodsIn(
                workingCopy.getElements().getAllMembers(entityElement));
        List<VariableElement> fields = ElementFilter.fieldsIn(
                workingCopy.getElements().getAllMembers(entityElement));
        Map<String,VariableElement> fieldsMap = new HashMap<String, VariableElement>();
        for (VariableElement variableElement : fields) {
            fieldsMap.put( variableElement.getSimpleName().toString(), variableElement);
        }
        for (ExecutableElement method : methods) {
            if ( !method.getModifiers().contains( Modifier.PUBLIC)){
                continue;
            }
            List<? extends AnnotationMirror> annotations = method.getAnnotationMirrors();
            boolean foundXmlTransient = false;
            for (AnnotationMirror annotationMirror : annotations) {
                Element annotation = annotationMirror.getAnnotationType().asElement();
                if ( annotation instanceof TypeElement ){
                    if ( ((TypeElement)annotation).getQualifiedName().
                            contentEquals(XML_TRANSIENT))
                    {
                        foundXmlTransient = true;
                        break;
                    }
                }
            }
            if (foundXmlTransient){
                continue;
            }
            VariableElement field = getField(method, fieldsMap, workingCopy);
            if ( field == null ){
                continue;
            }
            List<? extends AnnotationMirror> annotation = workingCopy.getElements().
                    getAllAnnotationMirrors(field);
            for (AnnotationMirror annotationMirror : annotation) {
                Element element =annotationMirror.getAnnotationType().asElement();
                if ( element instanceof TypeElement){
                    String fqn = ((TypeElement)element).getQualifiedName().toString();
                    if ( fqn.equals("javax.persistence.OneToMany")
                            ||fqn.equals("javax.persistence.ManyToMany"))
                    {
                        MethodTree methodTree = workingCopy.getTrees().getTree(
                                method);
                        MethodTree newMethod = (MethodTree) methodTree;
                        for (AnnotationTree annTree : annotationTrees) {
                            newMethod = genUtils.addAnnotation(newMethod,
                                    annTree);
                        }
                        workingCopy.rewrite(methodTree, newMethod);
                    }
                }
            }
        }
    }
    
    private static VariableElement getField(ExecutableElement method, 
            Map<String,VariableElement> fields, CompilationController controller)
    {
        String name = method.getSimpleName().toString();
        TypeMirror returnType = method.getReturnType();
        if ( returnType.getKind()== TypeKind.VOID){
            return null;
        }
        if ( !method.getParameters().isEmpty()){
            return null;
        }
        int start =0;
        if ( name.startsWith("get")){                                   // NOI18N
            start =3;
        }
        else if ( name.startsWith( "is")){                              // NOI18N
            start =2;
        }
        String fieldName = lowerFirstLetter(name.substring(start));
        VariableElement field = fields.get(fieldName);
        if ( field == null){
            return null;
        }
        if ( controller.getTypes().isSameType(field.asType(),returnType)){
            return field;
        }
        return null;
    }
    
    private static String lowerFirstLetter( String name ){
        if ( name.length() <=1){
            return name;
        }
        char firstLetter = name.charAt(0);
        if ( Character.isUpperCase(firstLetter)){
            return Character.toLowerCase(firstLetter) +name.substring(1);
        }
        return name;
    }
    
    private static void addXmlRootAnnotation(WorkingCopy working, TreeMaker make){
        if ( working.getElements().getTypeElement(
                XMLROOT_ANNOTATION) == null)
        {
            return;
        }
        
        TypeElement entityElement = 
            working.getTopLevelElements().get(0);
        List<? extends AnnotationMirror> annotationMirrors = 
            working.getElements().getAllAnnotationMirrors(
                    entityElement);
        boolean hasXmlRootAnnotation = false;
        for (AnnotationMirror annotationMirror : annotationMirrors)
        {
            DeclaredType type = annotationMirror.getAnnotationType();
            Element annotationElement = type.asElement();
            if ( annotationElement instanceof TypeElement ){
                Name annotationName = ((TypeElement)annotationElement).
                    getQualifiedName();
                if ( annotationName.contentEquals(XMLROOT_ANNOTATION))
                {
                    hasXmlRootAnnotation = true;
                }
            }
        }
        if ( !hasXmlRootAnnotation ){
            ClassTree classTree = working.getTrees().getTree(
                    entityElement);
            GenerationUtils genUtils = GenerationUtils.
                newInstance(working);
            ModifiersTree modifiersTree = make.addModifiersAnnotation(
                    classTree.getModifiers(),
                    genUtils.createAnnotation(XMLROOT_ANNOTATION));

            working.rewrite( classTree.getModifiers(), 
                    modifiersTree);
        }
    }
    
    private static FacadeGenerator getEjbFacadeGenerator(EntityResourceBeanModel model){
        if ( FACADE_GENERATOR instanceof EjbFacadeGeneratorProvider ){
            return ((EjbFacadeGeneratorProvider)FACADE_GENERATOR).
                    createGenerator(model);
        }
        return FACADE_GENERATOR.createGenerator();
    }
    
    private static final FacadeGeneratorProvider FACADE_GENERATOR =
        Lookup.getDefault().lookup(FacadeGeneratorProvider.class);
    
    private static class EntityCollector implements Task<CompilationController>{
        
        /* (non-Javadoc)
         * @see org.netbeans.api.java.source.Task#run(java.lang.Object)
         */
        @Override
        public void run( CompilationController controller ) throws Exception {
            entityFqn = null;
            controller.toPhase(Phase.ELEMENTS_RESOLVED);
            TypeElement classElement = SourceUtils.
                getPublicTopLevelElement(controller);
            if (classElement == null) {
                return;
            }
            String entityName = null;
            TypeElement annotationElement = controller.getElements()
                    .getTypeElement(Constants.PERSISTENCE_TABLE_JAKARTA);
            if(annotationElement == null) {
                annotationElement = controller.getElements().getTypeElement(Constants.PERSISTENCE_TABLE);
            }
            if (annotationElement == null) {
                isIncomplete = true;
            }
            else {
                entityName = TypeUtil.getAnnotationValueName(controller,
                        classElement, annotationElement);
            }
            if (entityName == null) {
                annotationElement = controller.getElements().getTypeElement(
                        Constants.PERSISTENCE_ENTITY_JAKARTA);
                if (annotationElement == null) {
                    annotationElement = controller.getElements().getTypeElement(
                            Constants.PERSISTENCE_ENTITY);
                }
                if (annotationElement == null) {
                    isIncomplete = true;
                    return;
                }
                entityName = TypeUtil.getAnnotationValueName(controller,
                        classElement, annotationElement);
            }
            if (entityName != null) {
                entityFqn = classElement.getQualifiedName().toString();
            }
        }
                
        boolean isIncomplete(){
            return isIncomplete;
        }
        
        String getEntityFqn(){
            return entityFqn;
        }
    
        private boolean isIncomplete;
        private String entityFqn;
    }    
    
    private static class Lazy {
        private static final Map<String,Class<?>> primitiveTypes = new HashMap<String,Class<?>>();
        
        static {
            primitiveTypes.put("int", Integer.class);
            primitiveTypes.put("int[]", int[].class);
            primitiveTypes.put("java.lang.Integer[]", Integer[].class);
            primitiveTypes.put("boolean", Boolean.class);
            primitiveTypes.put("boolean[]", boolean[].class);
            primitiveTypes.put("java.lang.Boolean[]", Boolean[].class);
            primitiveTypes.put("byte", Byte.class);
            primitiveTypes.put("byte[]", byte[].class);
            primitiveTypes.put("java.lang.Byte[]", Byte[].class);
            primitiveTypes.put("char", Character.class);
            primitiveTypes.put("char[]", char[].class);
            primitiveTypes.put("java.lang.Character[]", Character[].class);
            primitiveTypes.put("double", Double.class);
            primitiveTypes.put("double[]", double[].class);
            primitiveTypes.put("java.lang.Double[]", Double[].class);
            primitiveTypes.put("float", Float.class);
            primitiveTypes.put("float[]", float[].class);
            primitiveTypes.put("java.lang.Float[]", Float[].class);
            primitiveTypes.put("long", Long.class);
            primitiveTypes.put("long[]", long[].class);
            primitiveTypes.put("java.lang.Long[]", Long[].class);
            primitiveTypes.put("short", Short.class);
            primitiveTypes.put("short[]", short[].class);
            primitiveTypes.put("java.lang.Short[]", Short[].class);
        }
    }
}
