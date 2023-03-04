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

package org.netbeans.modules.javadoc.search;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;


/** This class finds the source to show it instead of documentation.
 *
 */
public final class SrcFinder extends Object {

    /** SrcFinder is a singleton */
    private  SrcFinder() {
    }

    static Object[]/*FileObject, ElementHandle*/ findSource(String aPackage, URL url) {

        aPackage = aPackage.replace( '.', '/' ); // NOI18N
        String thePackage = null;
        final String member = url.getRef(); 
        String clazz = url.getFile();
        String filename = null;

        int pIndex;
        
        if ((pIndex = clazz.toLowerCase().lastIndexOf(aPackage.trim().toLowerCase())) != -1) {
            thePackage = clazz.substring(pIndex, pIndex + aPackage.trim().length()  - 1 );
            clazz = clazz.substring( pIndex + aPackage.trim().length(), clazz.length() - 5 );

            int ei;
            if ( ( ei = clazz.indexOf('.')) != -1 ) {
                filename = clazz.substring(0, ei );
            }
            else
                filename = clazz;

        }
        
//        System.out.println("================================");
//        System.out.println("URL     :" + url   );
//        System.out.println("aPCKG   :" + aPackage );
//        System.out.println("--------------------------------");
//        System.out.println("MEMBER  :" + member ); // NOI18N
//        System.out.println("CLASS   :" + clazz ); // NOI18N
//        System.out.println("PACKAGE :" + thePackage ); // NOI18N
//        System.out.println("FILENAME:" + filename ); // NOI18N

        String resourceName = thePackage + "/" + filename + ".java"; // NOI18N
        FileObject fo = searchResource(url, resourceName);
        
        final ElementHandle[] handles = new ElementHandle[1];
        
        if ( fo != null ) {
            final String className = clazz;
            JavaSource js = JavaSource.forFileObject(fo);
            
            try {
                js.runUserActionTask(new CancellableTask<CompilationController>() {

                    public void cancel() {
                    }

                    public void run(CompilationController ctrl) throws Exception {
                        ctrl.toPhase(Phase.ELEMENTS_RESOLVED);
                        TypeElement classElm = findClass(ctrl, className);
                        if (classElm == null) {
                            // bad luck
                        } else if (member == null) {
                            handles[0] = ElementHandle.create(classElm);
                        } else {
                            int pi = member.indexOf('(');
                            if (pi == -1) {
                                // we are looking for fields
                                handles[0] = findField(classElm, member);
                            } else {
                                // We are looking for method or constructor
                                handles[0] = findMethod(ctrl, classElm, member );
                            }
                        }
                    }
                }, true);
            } catch (IOException ex) {
                Logger.getLogger(SrcFinder.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
            
        }
        return handles[0] != null? new Object[]{fo, handles[0]}: null;
    }

    /**
     * searches the file corresponding to javadoc url on all source path.
     * {@link GlobalPathRegistry#findResource}
     * is insufficient due to returning just the first occurrence of the file. So having
     * two platforms installed would bring troubles.
     * @param url javadoc
     * @param respath resource in form java/lang/Character.java
     * @return the file
     */ 
    private static FileObject searchResource(URL url, String respath) {
        FileObject res = searchBinaryPath(ClassPath.BOOT, respath, url);
        
        if (res == null) {
            res = searchBinaryPath(ClassPath.COMPILE, respath, url);
        }
        
        if (res == null) {
            res = searchSourcePath(respath, url);
        }
        
        return res;
        
    }

    private static FileObject searchBinaryPath(String classPathID, String respath, URL url) {
        Set<ClassPath> cpaths = GlobalPathRegistry.getDefault().getPaths(classPathID);
        for (ClassPath cpath: cpaths) {
            FileObject[] cpRoots = cpath.getRoots();
            for (int i = 0; i < cpRoots.length; i++) {
                SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(URLMapper.findURL(cpRoots[i], URLMapper.EXTERNAL));
                FileObject[] srcRoots = result.getRoots();
                for (int j = 0; j < srcRoots.length; j++) {
                    FileObject fo = srcRoots[j].getFileObject(respath);
                    if (fo != null && isJavadocAssigned(cpath, url)) {
                        return fo; 
                    }
                }
            }
        }
        return null;
    }

    private static FileObject searchSourcePath(String respath, URL url) {
        Set<ClassPath> cpaths = GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE);
        for (ClassPath cpath: cpaths) {
            FileObject fo = cpath.findResource(respath);
            if (fo != null && isJavadocAssigned(cpath, url)) {
                return fo;
            }
        }
        
        return null;
    }
    
    /**
     * checks if the javadoc url is assigned to a given classpath 
     * @param cpath classpath
     * @param url javadoc
     * @return is assigned?
     */ 
    private static boolean isJavadocAssigned(ClassPath cpath, URL url) {
        FileObject[] cpRoots = cpath.getRoots();
        String urlPath = url.toExternalForm();
        for (int i = 0; i < cpRoots.length; i++) {
            JavadocForBinaryQuery.Result result = JavadocForBinaryQuery.findJavadoc(URLMapper.findURL(cpRoots[i], URLMapper.EXTERNAL));
            URL[] jdRoots = result.getRoots();
            for (int j = 0; j < jdRoots.length; j++) {
                String jdRootPath = jdRoots[j].toExternalForm();
                if (urlPath.indexOf(jdRootPath) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static TypeElement findClass(CompilationController ctrl, String className) {
        CompilationUnitTree cunit = ctrl.getCompilationUnit();
        for (Tree declTree : cunit.getTypeDecls()) {
            ClassTree classTree = (ClassTree) declTree;
            if (className.equals(classTree.getSimpleName().toString())) {
                Trees trees = ctrl.getTrees();
                TypeElement classElm = (TypeElement) trees.getElement(trees.getPath(cunit, classTree));
                return classElm;
            }
        }
        return null;
    }
    
    private static ElementHandle findField(TypeElement classElm, String name) {
        for (Element elm: classElm.getEnclosedElements()) {
            if (elm.getKind() == ElementKind.FIELD &&
                    name.equals(elm.getSimpleName().toString())) {
                return ElementHandle.create(elm);
            }
        }
        return null;
    }
    
    /** Gets the method we are looking for
     */
    private static ElementHandle findMethod(CompilationController ctrl, TypeElement ce, String member) {
        TreeUtilities utils = ctrl.getTreeUtilities();

        int pi = member.indexOf( '(' );
        String name = member.substring( 0, pi );

        StringTokenizer tokenizer = new StringTokenizer( member.substring( pi ), " ,()" ); // NOI18N
        List<TypeMirror> paramList = new ArrayList<TypeMirror>();

        while( tokenizer.hasMoreTokens() ) {
            String token = tokenizer.nextToken();
            if (token.endsWith("...")) { // NOI18N
                // translate varargs to array
                token = token.substring(0, token.length() - 3);
                token += "[]"; // NOI18N
            }
            TypeMirror type = utils.parseType(token, ce);
            paramList.add(type);
        }
        
        // search method or constructor
        for (Element e: ce.getEnclosedElements()) {
            if (e.getKind() == ElementKind.METHOD
                    && name.equals(e.getSimpleName().toString())
                    && compareTypes(ctrl, paramList, ((ExecutableElement) e).getParameters())) {
                return ElementHandle.create(e);
            } else if (e.getKind() == ElementKind.CONSTRUCTOR
                    && name.equals(ce.getSimpleName().toString())
                    && compareTypes(ctrl, paramList, ((ExecutableElement) e).getParameters())) {
                return ElementHandle.create(e);
            }
        }
        
        return null;
    }
    
    private static boolean compareTypes(CompilationController ctrl, List<TypeMirror> types, List<? extends VariableElement> params) {
        if (types.size() != params.size()) {
            return false;
        }
        
        Iterator<? extends VariableElement> itParams = params.iterator();
        Iterator<TypeMirror> itTypes = types.iterator();
        while (itParams.hasNext()) {
            VariableElement varEl = itParams.next();
            TypeMirror paramType = varEl.asType();
            TypeMirror type = itTypes.next();
            
            // check types are the same kind
            if (type.getKind() != paramType.getKind()) {
                return false;
            }
            
            // check elements since javadoc ignores generics
            if (type.getKind() == TypeKind.DECLARED) {
                Element paramElm = ((DeclaredType) paramType).asElement();
                Element typeElm = ((DeclaredType) type).asElement();
                if (paramElm != typeElm) {
                    return false;
                }
            } else if (!ctrl.getTypes().isSameType(type, paramType)) { // arrays, primitives
                return false;
            }
            
        }
        return true;
    }

}
