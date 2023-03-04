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

/*
 * Common.java
 *
 * Created on May 19, 2000, 1:56 PM
 */

package org.netbeans.test.java;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
//import org.openide.src.MethodParameter;
//import org.openide.src.Type;


/** Common static methods. Useful for creating new JavaElements
 * @author Jan Becicka
 * @version 1.1
 */

public class Common extends Object {
    
    private static final String cr=System.getProperty("line.separator");
    private static final String METHODS = "newMethod";
    private static final String FIELDS = "newField";
    //private static final int DEFAULTMODIFIERS = Modifier.PUBLIC;
    private static final String DEFAULTBODY = "for (int i=0;i<100;i++){\n\tSystem.out.println(new Integer(i).toString());\n}\nreturn 0;\n";
    public  static final String DEFAULTINITIALIZERBODY = "\n/*somebody*/\n";
    
    /** int parameter1
     */
    public static Map<String,String> PARS1;
    /** int parameter1, int parameter2
     */
    public static Map<String,String> PARS2;
    /** float parameter1, float parameter2, float parameter 3
     */
    public static Map<String,String> PARS3;
    
    static {
        PARS1 = new TreeMap<String, String>();
        PARS1.put("param1","int");
        PARS2 = new TreeMap<String, String>();
        PARS2.put("param1","int");
        PARS2.put("param2","int");
        PARS3 = new TreeMap<String, String>();
        PARS3.put("param1","float");
        PARS3.put("param2","int");
        PARS3.put("param3","String");
        
    }
    
    private static java.io.PrintWriter pw = null;
    
    public static void setPrintWriter(java.io.PrintWriter pr){
        pw=pr;
    }
       
    public Common() {
    }
    
    /** returns string from in and int
     * @param s
     * @param i
     * @return Makes String from String and int
     */
    public static String concat(String s, int i){
        return s+new Integer(i).toString();
    }
    
    /**
     * @param i
     * @return for i=1 "newMethod1"
     */
    public static String getMethodName(int i){
        return concat(METHODS,i);
    }
    
    /** Concats String and int
     * @param name
     * @param i
     * @return
     */
    public static String getFieldName(String name,int i){
        return concat(name,i);
    }
    
    /**
     * @param i
     * @return for i=1 "newField1"
     */
    public static String getFieldName(int i){
        return concat(FIELDS,i);
    }

    private static Tree getTreeForType(String paramType, TreeMaker make) {
        Tree param = null;
        try {
            param = make.PrimitiveType(TypeKind.valueOf(paramType.toUpperCase()));
        } catch (IllegalArgumentException iae) {
            param = make.Identifier(paramType);
        }
        return param;
    }
    
    public static MethodTree createMethod(TreeMaker make,String name, Map<String,String> params) {
        ModifiersTree methodModifiers = make.Modifiers(
                Collections.<Modifier>singleton(Modifier.PUBLIC),
                Collections.<AnnotationTree>emptyList()
                );
        List<VariableTree> paramList = new LinkedList<VariableTree>();
        for(String paramName: params.keySet()) {
            Tree paramType = getTreeForType(params.get(paramName), make);
            VariableTree parameter = make.Variable(
                    make.Modifiers(
                    Collections.<Modifier>emptySet(),
                    Collections.<AnnotationTree>emptyList()
                    ),
                    paramName, // name
                    paramType, // parameter type
                    null // initializer - does not make sense in parameters.
                    );
            paramList.add(parameter);
        }
        MethodTree newMethod = make.Method(
                methodModifiers, // public
                name, // name
                make.PrimitiveType(TypeKind.VOID), // return type "void"
                Collections.<TypeParameterTree>emptyList(), // type parameters - none
                paramList, // final ObjectOutput arg0
                Collections.<ExpressionTree>emptyList(), // throws
                "{ throw new UnsupportedOperationException(\"Not supported yet.\") }", // body text
                null // default value - not applicable here, used by annotations
                );
        return newMethod;
    }
    
    public static void addMethod(JavaSource js,
            final String name,
            final Map<String,String> params,
            final String returnType,
            final Set<Modifier> modifiers) throws IOException {
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void cancel() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = null;
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        clazz = (ClassTree) typeDecl;
                    }
                } // end for
                MethodTree newMethod = createMethod(make, name, params);
                newMethod = make.Method(make.Modifiers(modifiers),
                        newMethod.getName(),
                        getTreeForType(returnType, make),
                        newMethod.getTypeParameters(),
                        newMethod.getParameters(),
                        newMethod.getThrows(),
                        newMethod.getBody(),
                        (ExpressionTree)newMethod.getDefaultValue());
                ClassTree modifiedClazz = make.addClassMember(clazz, newMethod);
                workingCopy.rewrite(clazz, modifiedClazz);
            }
        };
        js.runModificationTask(task).commit();
    }
    
    public static void addConstructor(JavaSource js, final Map<String,String> params) throws IOException {
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void cancel() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = null;
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        clazz = (ClassTree) typeDecl;
                    }
                } // end for
                MethodTree newMethod = createMethod(make, "<init>", params);
                ClassTree modifiedClazz = make.addClassMember(clazz, newMethod);
                workingCopy.rewrite(clazz, modifiedClazz);
            }
        };
        js.runModificationTask(task).commit();
    }
    
    public static void removeConstructors(JavaSource js) throws IOException {
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void cancel() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            ClassTree orig;
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = null;
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        clazz = (ClassTree) typeDecl;
                    }
                } // end for
                orig = clazz;
                for(Tree el : clazz.getMembers()) {
                    if(el.getKind().equals(Kind.METHOD)) {
                        MethodTree method = (MethodTree) el;
                        if(method.getName().toString().equals("<init>")) {
                            clazz =  make.removeClassMember(clazz, method);
                        }
                        
                    }
                }
                workingCopy.rewrite(orig, clazz);
            }
        };
        js.runModificationTask(task).commit();
    }
    
    public static void addInitializer(JavaSource js, final boolean isStatic) throws IOException {
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void cancel() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = null;
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        clazz = (ClassTree) typeDecl;
                    }
                } // end for
                
                BlockTree bt = make.Block(Collections.EMPTY_LIST, isStatic);
                ClassTree modifiedClazz = make.addClassMember(clazz, bt);
                workingCopy.rewrite(clazz,modifiedClazz);
            }
        };
        js.runModificationTask(task).commit();
    }
    
    
    public static  void addExtendImplementClause(JavaSource js,final String superClass,final List<String> ifaces) throws IOException {
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void cancel() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = null;
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        clazz = (ClassTree) typeDecl;
                    }
                } // end for
                ClassTree origClazz = clazz;
                for(String iface :ifaces) {
                    TypeElement element = workingCopy.getElements().getTypeElement(iface);
                    ExpressionTree implementsClause = implementsClause = make.QualIdent(element);
                    clazz = make.addClassImplementsClause(clazz, implementsClause);
                }
                Tree extendsTree = make.QualIdent(workingCopy.getElements().getTypeElement(superClass));
                clazz = make.Class(clazz.getModifiers(),
                        clazz.getSimpleName(),
                        clazz.getTypeParameters(),
                        extendsTree,
                        (List<ExpressionTree>) clazz.getImplementsClause(),
                        clazz.getMembers());
                workingCopy.rewrite(origClazz,clazz);
            }
        };
        js.runModificationTask(task).commit();
        
    }
    
    /** Creates new Class from package
     * @param packageName destination
     * @param className name
     * @throws Exception
     * @return
     */
    
    
    public static void addImport(JavaSource js,final String importText,final boolean isStatic) throws IOException {
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void cancel() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree copy = make.addCompUnitImport(cut,make.Import(make.Identifier(importText), isStatic));
                workingCopy.rewrite(cut, copy);
            }
        };
        js.runModificationTask(task).commit();
    }
    
    
    public static void setPackage(JavaSource js, final String pack) throws IOException {
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void cancel() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree copy = make.CompilationUnit(make.Identifier(pack),cut.getImports(), cut.getTypeDecls(), cut.getSourceFile());
                workingCopy.rewrite(cut, copy);
            }
        };
        js.runModificationTask(task).commit();
    }
    
    public static FileObject createClass(FileObject target, String packageName, String className) throws Exception   {
        DataObject dob = getSystemDO("Templates/Classes", "Class", "java");
        DataObject mdob = dob.createFromTemplate(org.openide.loaders.DataFolder.findFolder(getFO(target,packageName,null,null)), className);
        return mdob.getPrimaryFile();
    }
        
    /** Get a file object by name. */
    public static FileObject getFO(FileObject file, String pkg, String name, String ext) throws Exception {
        ClassPath cp=ClassPath.getClassPath(file, ClassPath.SOURCE);
        String nam=(pkg != null && pkg.length() > 0)?pkg:"";
        nam+=(name != null && name.length() > 0)?".":"";
        nam=nam.replace('.', '/');
        nam+=(name != null && name.length() > 0)?name:"";
        nam+=(ext != null && ext.length() > 0)?"."+ext:"";
        //check
        FileObject[] root=cp.getRoots();
        FileObject ret=cp.findResource(nam);
        return ret;
    }
    
    /** Get a data object by name. */
    public static DataObject getSystemDO(String pkg, String name, String ext) throws Exception {
        return DataObject.find(FileUtil.getConfigFile(pkg+"/"+name+"."+ext));
    }
       
    /** Compares two Arrays
     * @param l
     * @param r
     * @return
     */
    public static boolean arrayEquals(Object[] l,Object[] r) {
        if (l.length!=r.length) return false;
        for (int i=0; i < l.length; i++){
            if (!l[i].equals(r[i])) return false;
        }
        return true;
    }
    
    /** Removes time and author's name
     * @param result
     * @return
     */
    public static String unify(String result) {        
        int left=result.indexOf("@author");
        int right=result.indexOf('\n',left);
        if (left > -1)
            result=result.substring(0,left+"@author".length())+result.substring(right);
        result = result.replaceAll("\\r\\n", "\n"); // Get rid of win EOL
        return result;
    }
    
    /**
     * @param str
     * @return
     */
    public static String firstCharToUpper(String str) {
        String    first, rest;
        
        if( str==null || str.equals("") )
            return str;
        
        first = str.substring(0, 1).toUpperCase();
        rest = str.substring( 1 );
        return first+rest;
    }
    
    /**
     * @param str
     * @return
     */
    public static String firstCharToLower(String str) {
        String first, rest;
        
        if( str==null || str.equals("") )
            return( str );
        
        first = str.substring(0, 1).toLowerCase();
        rest = str.substring( 1 );
        
        return first+rest;
    }
        
    public  static VariableTree createField(TreeMaker make,String name,Set<Modifier> modifiers, String type) {
        Tree fieldType = getTreeForType(type, make);
        VariableTree vt = make.Variable(
                make.Modifiers(modifiers),
                name,
                fieldType,
                null);
        return vt;
    }
    
    
    public static void addField(JavaSource js, final String name, final Set<Modifier> modifiers, final String type) throws IOException {
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void cancel() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = null;
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        clazz = (ClassTree) typeDecl;
                    }
                } // end for
                VariableTree vt = createField(make, name, modifiers, type);
                ClassTree modifiedClazz = make.addClassMember(clazz, vt);
                workingCopy.rewrite(clazz, modifiedClazz);
            }
        };
        js.runModificationTask(task).commit();
    }
    
    
    public static ClassTree getClassTree(
            TreeMaker make,
            WorkingCopy workingCopy,
            String name,
            String superClass,
            List<ExpressionTree> implementsList,
            Set<Modifier> modifiers) {        
            
        Tree extendsTree = make.QualIdent(workingCopy.getElements().getTypeElement(superClass));        
        Map<String,String> params = new HashMap<String, String>();
        params.put("param1", "String");
        MethodTree mt = Common.createMethod(make, "method", params);
        VariableTree vt = Common.createField(make, "variable", EnumSet.of(Modifier.PROTECTED), "double");
        List<Tree> members = new ArrayList<Tree>();
        members.add(mt);
        members.add(vt);
        members.add(make.Block(Collections.EMPTY_LIST, false));
        ClassTree innerClass = make.Class(
                make.Modifiers(modifiers),
                name,
                Collections.EMPTY_LIST,
                extendsTree,
                implementsList,
                members);        
        return innerClass;
    }
    
    public static void addTopLevelClass(JavaSource js) throws IOException {
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void cancel() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = getClassTree(make, workingCopy, "TopLevel", "java.util.List", Collections.<ExpressionTree>emptyList(), EnumSet.noneOf(Modifier.class));                
                CompilationUnitTree copy = make.addCompUnitTypeDecl(cut, clazz);

                workingCopy.rewrite(cut, copy);
            }
        };
        js.runModificationTask(task).commit();
    }
    
    
    
    public static void addClassComment(JavaSource js, final String text) throws IOException {
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void cancel() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = null;
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        clazz = (ClassTree) typeDecl;                        
                    }
                } // end for
                
            }
        };
        js.runModificationTask(task).commit();
    }           
}
