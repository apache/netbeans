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

package org.netbeans.modules.xml.jaxb.util;

//import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
//import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author gpatil
 */
public class JavaSourceUtil {
    public static CancellableTask createMarshalMethod(final boolean marshal, final String arg1, final String arg2){
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = null;
                
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        clazz = (ClassTree) typeDecl;
                        break;
                    }
                }

//                ModifiersTree methodModifiers = make.Modifiers(
//                        Collections.<Modifier>singleton(Modifier.PRIVATE),
//                        Collections.<AnnotationTree>emptyList()
//                        );
                // create method.      
                MethodTree newMethod = null;
                if (marshal) {
                    newMethod = getMarshalMethod(make, arg1, arg2);
                } else {
                    newMethod = getUnmarshalMethod(make, arg1, arg2);
                }
                ClassTree modifiedClazz = make.addClassMember(clazz, newMethod);
                workingCopy.rewrite(clazz, modifiedClazz);
            }
            
            public void cancel() {
                //... cancel code
            }
        };
        
        return task;
    }
    
    
    private static MethodTree getMarshalMethod(TreeMaker make, String arg1, String arg2) throws IOException {        
        MethodTree method = make.Method(
                make.Modifiers(Collections.<Modifier>singleton(Modifier.PRIVATE)),
                // XXX TODO get unique method name.
                "marshal", //NO I18N
                make.PrimitiveType(TypeKind.BOOLEAN),
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                getMarshalMethodBody(arg1, arg2),
                null);
        return method;
    }

    private static MethodTree getUnmarshalMethod(TreeMaker make, String arg1, String arg2) throws IOException {        
        MethodTree method = make.Method(
                make.Modifiers(Collections.<Modifier>singleton(Modifier.PRIVATE)),
                // XXX TODO get unique method name.                
                "unmarshal", //NO I18N
                make.PrimitiveType(TypeKind.BOOLEAN),
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                getUnmarshalMethodBody(arg1, arg2),
                null);        
        return method;
    }
    
    private static String getMarshalMethodBody(String arg1, String arg2){
        String fileName = (arg2 == null) ? "File name here" : arg2 ;        
        StringBuffer sb = new StringBuffer();
        sb.append("{\n");  
        sb.append("  Object obj = null;\n"); // No I18N
        sb.append("  try {\n"); // No I18N
        sb.append("      JAXBContext jaxbctx = JAXBContext.newInstance(\""); // No I18N
        sb.append(arg1); 
        sb.append("\"\n"); 
        sb.append("      Marshaller marshaller = jaxbctx.createMarshaller();\n"); // No I18N
        sb.append("      marshaller.marshal(new File(\""); // No I18N
        sb.append(arg2); 
        sb.append("\");\n"); 
        sb.append("  } catch (JAXBException e) {\n"); // No I18N
        sb.append("      e.printStackTrace();\n"); // No I18N
        sb.append("  }\n");
        sb.append("}\n");
        return sb.toString();
    }
    
    private static String getUnmarshalMethodBody(String arg1, String arg2){
        String fileName = arg2 == null ? "File name here" : arg2;        
        StringBuffer sb = new StringBuffer();
        sb.append( "{Object obj = null;\n" ); // No I18N
        sb.append( "try {\n" ); // No I18N
        sb.append( "JAXBContext jaxbctx = JAXBContext.newInstance(\"" +  // No I18N
                    arg1 + "\");\n" ); // No I18N
        sb.append( "Unmarshaller unmarshaller = jaxbctx.createUnmarshaller();\n" ); // No I18N
        sb.append( "obj = (Object) unmarshaller.unmarshal(new File(\"" +  // No I18N
                    fileName + "\"));\n" ); // No I18N
        sb.append( "} catch (JAXBException e) {\n" ); // No I18N
        sb.append( "            // TODO handle exception \n"); // No I18N
        sb.append( "        e.printStackTrace();\n" ); // No I18N
        sb.append( "}}" );
        
        return sb.toString();
    }
    
}
