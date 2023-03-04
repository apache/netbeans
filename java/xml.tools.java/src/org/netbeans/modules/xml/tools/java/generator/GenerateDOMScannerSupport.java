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
package org.netbeans.modules.xml.tools.java.generator;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.*;
import java.util.*;


import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.filesystems.*;
import org.openide.util.*;

import org.netbeans.tax.*;
import org.netbeans.tax.decl.*;
import org.netbeans.modules.xml.DTDDataObject;
import org.netbeans.modules.xml.lib.GuiUtil;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.Comment;
import org.netbeans.modules.xml.tools.generator.SelectFileDialog;
import org.netbeans.modules.xml.tools.generator.XMLGenerateCookie;

public class GenerateDOMScannerSupport implements XMLGenerateCookie {
    
    private static final String JAVA_EXT = "java"; // NOI18N

    private static final String DOM_PACKAGE  = "org.w3c.dom."; // NOI18N
    private static final String DOM_DOCUMENT = DOM_PACKAGE + "Document"; // NOI18N
    private static final String DOM_ELEMENT  = DOM_PACKAGE + "Element"; // NOI18N
    private static final String DOM_NAMED_NODE_MAP = DOM_PACKAGE + "NamedNodeMap"; // NOI18N

    private static final String VARIABLE_DOCUMENT = "document"; // NOI18N
    private static final String VARIABLE_ELEMENT  = "element"; // NOI18N
    private static final String VARIABLE_ATTRS    = "attrs"; // NOI18N

    private static final String METHOD_SCAN_DOCUMENT = "visitDocument"; // NOI18N
    private static final String METHOD_SCAN_ELEMENT  = "visitElement"; // NOI18N

    //TODO: Retouche
//    private static final Type Type_STRING = Type.createFromClass (String.class);


    private DTDDataObject DO;
    private TreeDTDRoot dtd;

    public GenerateDOMScannerSupport (DTDDataObject DO) {
	this (DO, null);
    }

    public GenerateDOMScannerSupport (DTDDataObject DO, TreeDTDRoot dtd) {
        if (DO == null) throw new IllegalArgumentException("null"); // NOI18N
        this.DO = DO;
        this.dtd = dtd;
    }

   
    public void generate () {
        try {                        
            
            if (getDTD() == null)
                return;
            FileObject primFile = DO.getPrimaryFile();
            
            String rawName = primFile.getName();
            String name = rawName.substring(0,1).toUpperCase() + rawName.substring(1) + NbBundle.getMessage(GenerateDOMScannerSupport.class, "NAME_SUFFIX_Scanner");
            FileObject folder = primFile.getParent();
            FileObject generFile = (new SelectFileDialog (folder, name, JAVA_EXT)).getFileObject();            
            name = generFile.getName();
            //SelectFileDialog creates an empty file on disk but the generation 
            //requires an empty java class file, hence it must be deleted.
            //The createClass call will create a Java class.
            generFile.delete();
            generFile = GenerationUtils.createClass(folder, name, null);
            try {
                GuiUtil.setStatusText(NbBundle.getMessage(GenerateDOMScannerSupport.class, "MSG_DOM_1"));
                prepareDOMScannerClass(generFile);
            } finally {
                GuiUtil.setStatusText(""); // NOI18N               
            }
            GuiUtil.performDefaultAction (generFile);
        } catch (UserCancelException e) {
            //ignore: user chose to cancel
        } catch (TreeException e) {
            // can not get tree representaion
           GuiUtil.notifyError(NbBundle.getMessage(GenerateDOMScannerSupport.class, "MSG_DOM_ERR_1"));
        } catch (IOException e) {
            // can not get tree representaion or write            
            GuiUtil.notifyError(NbBundle.getMessage(GenerateDOMScannerSupport.class, "MSG_DOM_ERR_2"));
        }
    }

    private TreeDTDRoot getDTD () throws IOException, TreeException {
	if (dtd == null) {
        TreeDocumentRoot result;

        TreeEditorCookie cake = (TreeEditorCookie)DO.getCookie(TreeEditorCookie.class);
        if (cake != null) {
            result = cake.openDocumentRoot();
        } else {
            throw new TreeException("DTDDataObject:INTERNAL ERROR"); // NOI18N
        }
        dtd = (TreeDTDRoot)result;
	}
        return dtd;
    }
        
    
    /*
     * Generate top level class content.
     *
     */
    private void prepareDOMScannerClass (FileObject clazz) throws IOException  {
//	ClassElement clazz = new ClassElement ();
//	JavaDoc javadoc = clazz.getJavaDoc();
//	javadoc.setRawText ("\n"+ // NOI18N
//			    " This is a scanner of DOM tree.\n"+ // NOI18N
//			    "\n"+ // NOI18N
//			    " Example:\n"+ // NOI18N
//			    " <pre>\n"+ // NOI18N
//			    "     javax.xml.parsers.DocumentBuilderFactory builderFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance();\n"+ // NOI18N
//			    "     javax.xml.parsers.DocumentBuilder builder = builderFactory.newDocumentBuilder();\n"+ // NOI18N
//			    "     org.w3c.dom.Document document = builder.parse (new org.xml.sax.InputSource (???));\n"+ // NOI18N
//			    "     <font color=\"blue\">"+name+" scanner = new "+name+" (document);</font>\n"+ // NOI18N
//			    "     <font color=\"blue\">scanner."+METHOD_SCAN_DOCUMENT+"();</font>\n"+ // NOI18N
//			    " </pre>\n"+ // NOI18N
//			    "\n"+ // NOI18N
//			    " @see org.w3c.dom.Document\n"+ // NOI18N
//			    " @see org.w3c.dom.Element\n"+ // NOI18N
//			    " @see org.w3c.dom.NamedNodeMap\n"); // NOI18N
//	clazz.setModifiers (Modifier.PUBLIC);
//	clazz.setName (Identifier.create (name));
//
	dtd2java (clazz, findRootTagName());
//	    
//	return (clazz);
    }

    /*
     * Generate scanner methods.
     *
     */
    private void dtd2java (FileObject clazz, String tempRootName) throws IOException  {
          final String constructorName = clazz.getName();
      
          JavaSource targetSource = JavaSource.forFileObject(clazz);
            
          CancellableTask task = new CancellableTask() {

                public void cancel() {
                  
                }

               
                public void run(Object parameter) throws Exception {
                    //throw new UnsupportedOperationException("Not supported yet.");
                    WorkingCopy workingCopy = (WorkingCopy)parameter;
                    workingCopy.toPhase(Phase.RESOLVED);
                    ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                    
                    if (javaClass!=null) {
                        TreeMaker make = workingCopy.getTreeMaker();
                        GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                        //add comment to class
                        String commentText = "TESTTTT";
                        Comment comment = Comment.create(Comment.Style.JAVADOC, 0, 0, 0, commentText);
                        make.addComment(javaClass, comment, true);
                        
                        //add document field
                        Tree tree = make.Identifier(DOM_DOCUMENT);
                        VariableTree var = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), VARIABLE_DOCUMENT, tree, null);
                        List<VariableTree> varTree = new ArrayList<VariableTree>();
                        varTree.add(var);
                        ClassTree modifiedClass = genUtils.addClassFields(javaClass, varTree);
                        commentText = "org.w3c.dom.Document document";
                        comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, commentText);
                        make.addComment(var, comment, true);
                        
                                            //constructor
                        ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
                        MethodTree newConstructor = genUtils.createAssignmentConstructor(mods, constructorName, varTree);
                        modifiedClass = make.addClassMember(modifiedClass, newConstructor);
                        commentText = "Create new " + constructorName + " with org.w3c.dom.Document.";
                        comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, commentText);
                        make.addComment(newConstructor, comment, true);
                        
                        //scanDocument method
                        //Create body of the method first
                        StringBuffer sb = new StringBuffer ("{\n"); // NOI18N
                        sb.append (DOM_ELEMENT).append (" ").append (VARIABLE_ELEMENT).append (" = "). // NOI18N
                        append (VARIABLE_DOCUMENT).append (".getDocumentElement();\n"); // NOI18N
        
                        // no root element is obvious, go over all declated elements.
        
                       Iterator<TreeElementDecl> it = dtd.getElementDeclarations().iterator();
                       while (it.hasNext()) {
                           String tagName = it.next().getName();
                           sb.append ("if ((").append (VARIABLE_ELEMENT).append (" != null) && "). // NOI18N
                           append (VARIABLE_ELEMENT).append (".getTagName().equals (\"").append (tagName).append ("\")) {\n"); // NOI18N
                           sb.append (METHOD_SCAN_ELEMENT).append ("_").append (GenerateSupportUtils.getJavaName (tagName)).append (" (").append (VARIABLE_ELEMENT). // NOI18N
                           append (");\n}\n"); // NOI18N
                       }
                       sb.append("}\n");
                       
                        MethodTree method = make.Method(
                            make.Modifiers(Collections.singleton(Modifier.PUBLIC)),
                            METHOD_SCAN_DOCUMENT,
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            Collections.<VariableTree>emptyList(),
                            Collections.<ExpressionTree>emptyList(),
                            sb.toString(),
                            null
                        );
                        commentText = "Scan through org.w3c.dom.Document " + VARIABLE_DOCUMENT + ".";
                        comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, commentText);
                        make.addComment(method, comment, true);
                        
                        modifiedClass = make.addClassMember(modifiedClass,method);
                        
                        // add set of scan_ methods
                        it = dtd.getElementDeclarations().iterator();
                        mods = make.Modifiers(EnumSet.noneOf(Modifier.class));
                        tree = make.Identifier(DOM_ELEMENT);
                        var = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), VARIABLE_ELEMENT, tree, null);
                        varTree = new ArrayList<VariableTree>();
                        varTree.add(var);
                        while (it.hasNext()) {
                            TreeElementDecl next = it.next();
                            String tagName = next.getName();
                            String methodName = GenerateSupportUtils.getJavaName (METHOD_SCAN_ELEMENT + "_" + tagName);
                          
                          
                            //create the bpdy
                            sb = new StringBuffer ();
                            sb.append("{");
                            sb.append (" // <").append (tagName).append (">\n// element.getValue();\n"); // NOI18N
                            Iterator<TreeAttlistDeclAttributeDef> it2;
                            if ((it2 = dtd.getAttributeDeclarations (tagName).iterator()).hasNext()) {
                                sb.append (DOM_NAMED_NODE_MAP).append (" ").append (VARIABLE_ATTRS).append (" = "). // NOI18N
                                append (VARIABLE_ELEMENT).append (".getAttributes();\n"); // NOI18N
                                sb.append ("for (int i = 0; i < ").append (VARIABLE_ATTRS).append (".getLength(); i++) {\n"); // NOI18N
                                sb.append ("org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);\n"); // NOI18N
                                while (it2.hasNext()) {
                                    TreeAttlistDeclAttributeDef attr = it2.next();
                                    sb.append ("if (attr.getName().equals (\"").append (attr.getName()).append ("\")) { // <"). // NOI18N
                                    append (tagName).append (" ").append (attr.getName()).append ("=\"???\">\n"); // NOI18N
                                    sb.append ("// attr.getValue();\n}\n"); // NOI18N
                                }
                                sb.append ("}\n"); // NOI18N
                            }
                            sb.append (generateElementScanner(next));
                            sb.append("}\n");
                            method = make.Method(
                                mods,
                                methodName,
                                make.PrimitiveType(TypeKind.VOID),
                                Collections.<TypeParameterTree>emptyList(),
                                varTree,
                                Collections.<ExpressionTree>emptyList(),
                                sb.toString(),
                                null );
                            commentText = "Scan through org.w3c.dom.Element named " + tagName + ".";
                            comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, commentText);
                            make.addComment(method, comment, true);
                        
                            modifiedClass = make.addClassMember(modifiedClass,method);
                        }
                        workingCopy.rewrite(javaClass, modifiedClass);
                    } 
                }
                 
             };
             targetSource.runModificationTask(task).commit();


    }

    /*
     * Generate fragment of code that goes over element content model
     * (and calls nested scanners/visitors).
     */
    private String generateElementScanner(TreeElementDecl element) {
        
        Iterator<TreeElementDecl> it;
        Set<String> elements = new HashSet<>();
        
        TreeElementDecl.ContentType type = element.getContentType();
        
        if (type instanceof ANYType) {
            it = dtd.getElementDeclarations().iterator();
            while (it.hasNext()) {
                String tagName = it.next().getName();
                elements.add(tagName);
            }
            
        } else {
            addElements(type, elements);
        }
        
        StringBuffer sb2 = new StringBuffer();
        sb2.append ("org.w3c.dom.NodeList nodes = element.getChildNodes();\n"); // NOI18N
        sb2.append ("for (int i = 0; i < nodes.getLength(); i++) {\n"); // NOI18N
        sb2.append ("org.w3c.dom.Node node = nodes.item (i);\n"); // NOI18N
        sb2.append ("switch (node.getNodeType()) {\n"); // NOI18N
        sb2.append ("case org.w3c.dom.Node.CDATA_SECTION_NODE:\n"); // NOI18N
        sb2.append ("// ((org.w3c.dom.CDATASection)node).getData();\nbreak;\n"); // NOI18N
        sb2.append ("case org.w3c.dom.Node.ELEMENT_NODE:\n"); // NOI18N
        sb2.append ("org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;\n"); // NOI18N
        it = dtd.getElementDeclarations().iterator();
        while (it.hasNext()) {
            String tagName = it.next().getName();
            if (elements.contains(tagName) == false) continue;
            sb2.append ("if (nodeElement.getTagName().equals (\"").append (tagName).append ("\")) {\n"); // NOI18N
            sb2.append (METHOD_SCAN_ELEMENT).append ("_").append (GenerateSupportUtils.getJavaName (tagName)).append (" (nodeElement);\n}\n"); // NOI18N
        }
        sb2.append ("break;\n"); // NOI18N
        sb2.append ("case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:\n"); // NOI18N
        sb2.append ("// ((org.w3c.dom.ProcessingInstruction)node).getTarget();\n"); // NOI18N
        sb2.append ("// ((org.w3c.dom.ProcessingInstruction)node).getData();\n"); // NOI18N
        sb2.append ("break;\n"); // NOI18N
        if (type.allowText()) {
            sb2.append ("case org.w3c.dom.Node.TEXT_NODE:\n"); // NOI18N
            sb2.append ("// ((org.w3c.dom.Text)node).getData();\n"); // NOI18N
            sb2.append ("break;\n"); // NOI18N
        }
        sb2.append ("}\n}\n"); // NOI18N        

        return sb2.toString();
    }

    
    /*
     * Recursive descend looking for all declared children of type
     * Takes into account just ChildrenType and NameType.
     */
    private void addElements(TreeElementDecl.ContentType type, Set elements) {
        
        if (type instanceof ChildrenType) {
            for (Iterator<TreeElementDecl.ContentType> it = ((ChildrenType)type).getTypes().iterator(); it.hasNext(); ) {
                TreeElementDecl.ContentType next = it.next();
                if (next instanceof ChildrenType) {
                    addElements(next, elements);
                } else if ( next instanceof NameType) {
                    elements.add(((NameType)next).getName());                    
                }
            }
        }
    }
    
    private String findRootTagName () {
        return null;
        //      SelectTagNamePanel panel = new SelectTagNamePanel (dtd);
        //      DialogDescriptor dd = new DialogDescriptor
        //        (panel, Util.THIS.getString ("PROP_rootElementNameTitle"), true, // NOI18N
        //         new Object[] { DialogDescriptor.OK_OPTION },
        //         DialogDescriptor.OK_OPTION,
        //         DialogDescriptor.BOTTOM_ALIGN, null, null);
        //      TopManager.getDefault().createDialog (dd).show();
        //      return panel.getRootName();
    }
}
