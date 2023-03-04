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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.awt.Dialog;
import java.awt.Dimension;
import org.netbeans.modules.xml.tools.generator.*;
import java.io.*;


import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.xml.tools.java.generator.ParsletBindings.Entry;
import org.xml.sax.*;

import org.openide.*;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.xml.*;

import org.netbeans.modules.xml.DTDDataObject;
import org.netbeans.modules.xml.lib.GuiUtil;
import org.netbeans.modules.xml.lib.FileUtilities;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;
import org.netbeans.tax.*;
import org.openide.util.NbBundle;

/**
 * Generates handler that traces context. It consists from:
 * <ul>
 * <li>HandlerInterface declaring handle{ElementName}({Element type}} methods
 * <li>HandlerParslet set of parse{format}(String param)
 * <li>HandlerStub a code dispatching to such methods.
 * <li>sample HandlerImpl
 * <li>sample ParsletImpl
 * </ul>
 *
 * <p>
 * The generator is driven by {@link SAXGeneratorModel}.
 * It contains all properties driving this code generator.
 *
 * @author  Petr Kuzel
 * @version 1.0, 12/7/2001
 */
public final class SAXGeneratorSupport implements XMLGenerateCookie {

    //TODO: Retouche
    private static final String JAVA_EXT = "java"; // NOI18N

    private static final String SAX_PACKAGE = "org.xml.sax."; // we import it // NOI18N
    private static final String SAX_EXCEPTION        = SAX_PACKAGE + "SAXException"; // NOI18N
    private static final String SAX_DOCUMENT_HANDLER = SAX_PACKAGE + "DocumentHandler"; // NOI18N
    private static final String SAX2_CONTENT_HANDLER  = SAX_PACKAGE + "ContentHandler"; // NOI18N
    private static final String SAX_LOCATOR          = SAX_PACKAGE + "Locator"; // NOI18N
    private static final String SAX_ATTRIBUTE_LIST   = SAX_PACKAGE + "AttributeList"; // NOI18N
    private static final String SAX2_ATTRIBUTES       = SAX_PACKAGE + "Attributes"; // NOI18N

    private static final String SAX_INPUT_SOURCE = SAX_PACKAGE + "InputSource"; // NOI18N

    private static final String JAXP_PACKAGE = "javax.xml.parsers."; // NOI18N
    private static final String JAXP_PARSER_CONFIGURATION_EXCEPTION = JAXP_PACKAGE + "ParserConfigurationException"; // NOI18N
    private static final String JAXP_FACTORY_CONFIGURATION_ERROR = JAXP_PACKAGE + "FactoryConfigurationRrror"; // NOI18N

    private static final String JAVA_IOEXCEPTION = "java.io.IOException"; // NOI18N

    // generated methods names

    private static final String M_SET_DOCUMENT_LOCATOR   = "setDocumentLocator"; // NOI18N
    private static final String M_START_DOCUMENT         = "startDocument"; // NOI18N
    private static final String M_END_DOCUMENT           = "endDocument"; // NOI18N
    private static final String M_START_ELEMENT          = "startElement"; // NOI18N
    private static final String M_END_ELEMENT            = "endElement"; // NOI18N
    private static final String M_CHARACTERS             = "characters"; // NOI18N
    private static final String M_IGNORABLE_WHITESPACE   = "ignorableWhitespace"; // NOI18N
    private static final String M_PROCESSING_INSTRUCTION = "processingInstruction"; // NOI18N
    private static final String M_SKIPPED_ENTITY         = "skippedEntity"; // NOI18N
    private static final String M_START_PREFIX_MAPPING   = "startPrefixMapping"; // NOI18N
    private static final String M_END_PREFIX_MAPPING     = "endPrefixMapping"; // NOI18N

    /** emmit (dispatch) method name.*/
    private static final String EMMIT_BUFFER = "dispatch"; // NOI18N
    private static final String M_PARSE = "parse"; // NOI18N
    private static final String HANDLE_PREFIX = "handle_";  // NOI18N
    private static final String START_PREFIX = "start_"; // NOI18N
    private static final String END_PREFIX = "end_"; // NOI18N

    private static final String FILE_COMMENT_MARK = "Mark"; // NOI18N
    
    ExpressionTree saxException, parserConfigException, ioException;
    List exceptions;

    //src hiearchy constants
  /*  private static final Type Type_STRING = Type.createFromClass (String.class);
    private static final MethodParameter[] STRING_PARAM = new MethodParameter[] {
        new MethodParameter("data",Type.createFromClass(String.class), true) // NOI18N
    };

    private static final Identifier[] JAXP_PARSE_EXCEPTIONS = new Identifier[] {
        Identifier.create(SAX_EXCEPTION),
        Identifier.create(JAXP_PARSER_CONFIGURATION_EXCEPTION),
        Identifier.create(JAVA_IOEXCEPTION)
    };*/

    private static final String JAXP_PARSE_EXCEPTIONS_DOC =
        "@throws " + JAVA_IOEXCEPTION + " on I/O error\n" + // NOI18N
        "@throws " + SAX_EXCEPTION + " propagated exception thrown by a DocumentHandler\n" + // NOI18N
        "@throws " + JAXP_PARSER_CONFIGURATION_EXCEPTION + " a parser satisfying the requested configuration cannot be created\n" + // NOI18N
        "@throws " + JAXP_FACTORY_CONFIGURATION_ERROR + " if the implementation cannot be instantiated\n"; // NOI18N


    // input fields - these control generation process

    private DTDDataObject DO;  //model DataObject
    private TreeDTDRoot dtd;    //model DTD

    private ElementBindings elementMapping = new ElementBindings();  //model mapping
    private ParsletBindings parsletsMap = new ParsletBindings();    //model mapping

    private int sax = 1; // SAX version to be used supported {1, 2}

    private SAXGeneratorModel model;  //holds strategy

    public SAXGeneratorSupport (DTDDataObject DO) {
        this (DO, null);
    }

    public SAXGeneratorSupport (DTDDataObject DO, TreeDTDRoot dtd) {
        if (DO == null) throw new IllegalArgumentException("null"); // NOI18N
        this.DO = DO;
        this.dtd = dtd;
    }
//
//    /**
//     * The entry method coresponding to GenerateCookie.
//     * It displays a customization dialog and then generate a code and opens it
//     * in editor mode.
//     */
    public void generate () {

        try {
            dtd = null;  // invalidate cache #26745
            if (getDTD() == null) {
                String msg = org.openide.util.NbBundle.getMessage(SAXGeneratorSupport.class, "MSG_invalid_dtd");
                GuiUtil.notifyWarning(msg);
                return;
            }

            FileObject primFile = DO.getPrimaryFile();
            String rawName = primFile.getName();
            String name = rawName.substring(0,1).toUpperCase() + rawName.substring(1);

            final FileObject folder = primFile.getParent();
            final String packageName = GenerationUtils.findJavaPackage(folder);

            // prepare initial model
            elementMapping.clear();
            parsletsMap.clear();
            initMappings();

            model = new SAXGeneratorModel(FileUtil.toFile(folder), name,
                    new ElementDeclarations (dtd.getElementDeclarations().iterator()),
                elementMapping, parsletsMap, packageName
            );

            // load previous settings
            loadPrevious(folder);

            // initialize wizard panels

            final WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[] {
                new SAXGeneratorAbstractPanel.WizardStep(SAXGeneratorVersionPanel.class),
                new SAXGeneratorAbstractPanel.WizardStep(SAXGeneratorMethodPanel.class),
                new SAXGeneratorAbstractPanel.WizardStep(SAXGeneratorParsletPanel.class),
                new SAXGeneratorAbstractPanel.WizardStep(SAXGeneratorFilePanel.class)
            };

            for (int i = 0; i< panels.length; i++) {
                ((SAXGeneratorAbstractPanel.WizardStep)panels[i]).setBean(model);
                ((SAXGeneratorAbstractPanel.WizardStep)panels[i]).setIndex(i);
            }

            // setup wizard properties

            WizardDescriptor descriptor = new WizardDescriptor(panels, model);

            descriptor.setTitle(NbBundle.getMessage (SAXGeneratorSupport.class, "SAXGeneratorSupport.title"));
            descriptor.putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
            descriptor.putProperty(WizardDescriptor.PROP_HELP_DISPLAYED, Boolean.TRUE); // NOI18N
            descriptor.putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
            descriptor.putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
            descriptor.putProperty(WizardDescriptor.PROP_LEFT_DIMENSION, new Dimension(500,400)); // NOI18N
            descriptor.putProperty(WizardDescriptor.PROP_CONTENT_DATA, new String[] { // NOI18N
                NbBundle.getMessage (SAXGeneratorSupport.class, "SAXGeneratorVersionPanel.step"),
                NbBundle.getMessage (SAXGeneratorSupport.class, "SAXGeneratorMethodPanel.step"),
                NbBundle.getMessage (SAXGeneratorSupport.class, "SAXGeneratorParsletPanel.step"),
                NbBundle.getMessage (SAXGeneratorSupport.class, "SAXGeneratorFilePanel.step")

            });

            String fmt = NbBundle.getMessage (SAXGeneratorSupport.class, "SAXGeneratorSupport.subtitle");
            descriptor.setTitleFormat(new java.text.MessageFormat(fmt));

            // launch the wizard

            Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.setVisible(true);

            if ( ( descriptor.CANCEL_OPTION.equals (descriptor.getValue()) ) ||
                 ( descriptor.CLOSED_OPTION.equals (descriptor.getValue()) ) ) {
                return;
            }

            // wizard finished

            GuiUtil.setStatusText(NbBundle.getMessage (SAXGeneratorSupport.class,"MSG_sax_progress_1"));

//            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug(model.toString());

            sax = model.getSAXversion();

            // prepare source elements and dataobjects

            //DataObject stubDataObject = FileUtilities.createDataObject(folder, model.getStub(), JAVA_EXT, true);
            FileObject fObj = GenerationUtils.createClass(folder, model.getStub(), null); 
            FileObject interfacefObj = GenerationUtils.createInterface( folder, model.getHandler(), null);
            FileObject interfaceImplfObj = GenerationUtils.createClass( folder, model.getHandlerImpl(), null);
           
            
              FileObject parsletsImplFileObj = null;
              FileObject parseltSrcFileObj = null;

            if (model.hasParslets()) {
                
                  parsletsImplFileObj = GenerationUtils.createClass( folder, model.getParsletImpl(), null);
                  
                  parseltSrcFileObj = GenerationUtils.createInterface( folder, model.getParslet(), null);

            }

            // generate code by a model

            GuiUtil.setStatusText(NbBundle.getMessage (SAXGeneratorSupport.class,"MSG_sax_progress_1_5"));

            CodeGenerator stubGenerator = new StubGenerator(model.getStub(), model.getHandler(), model.getParslet());
            stubGenerator.generate(fObj);//generateCode( stubGenerator, stubSrc, packageName);

            CodeGenerator interfaceGenerator = new InterfaceGenerator(model.getHandler());
            interfaceGenerator.generate( interfacefObj);

            CodeGenerator interfaceImplGenerator = new InterfaceImplGenerator(model.getHandlerImpl());
            interfaceImplGenerator.generate(interfaceImplfObj); //generateCode( interfaceImplGenerator, interfaceImplSrc, packageName);

            if (model.hasParslets()) {
                CodeGenerator parsletsGenerator = new ParsletGenerator(model.getParslet());
                parsletsGenerator.generate(parseltSrcFileObj);  // generateCode( parsletsGenerator, parsletsSrc, packageName);

                CodeGenerator parsletsImplGenerator = new ParsletImplGenerator(model.getParsletImpl());
                parsletsImplGenerator.generate(parsletsImplFileObj);//generateCode( parsletsImplGenerator, parsletsImplSrc, packageName);
            }

            // prepare settings data object

            DataObject settingsDataObject = null;
            String settings = "<!-- failed -->"; // NOI18N

            if (model.getBindings() != null) {
                settingsDataObject = FileUtilities.createDataObject(folder, model.getBindings(), "xml", true); // NOI18N
                settings = SAXBindingsGenerator.toXML(model);
            }

            // write generated code into filesystem

            GuiUtil.setStatusText(NbBundle.getMessage (SAXGeneratorSupport.class,"MSG_sax_progress_2"));

            trySave(DataObject.find(fObj), null);
            trySave(DataObject.find(interfaceImplfObj), null);
            trySave(DataObject.find(interfacefObj), null);

            if (model.hasParslets()) {
                trySave(DataObject.find(parseltSrcFileObj), null);
                trySave(DataObject.find(parsletsImplFileObj), null);
            }

            if (model.getBindings() != null) {
                trySave(settingsDataObject, settings);
            }

            // open files to be implemented in editor
            GuiUtil.setStatusText(NbBundle.getMessage (SAXGeneratorSupport.class,"MSG_sax_progress_3"));

            if (model.hasParslets()) {
                GuiUtil.performDefaultAction (folder.getFileObject(model.getParsletImpl(), JAVA_EXT));
            }
            GuiUtil.performDefaultAction (folder.getFileObject(model.getHandlerImpl(), JAVA_EXT));

        } catch (TreeException e) {
            String msg = NbBundle.getMessage(SAXGeneratorSupport.class,"MSG_wizard_fail");
            GuiUtil.notifyWarning(msg);
        } catch (IOException e) {
            String msg = NbBundle.getMessage(SAXGeneratorSupport.class,"MSG_wizard_fail", e);
            GuiUtil.notifyWarning(msg);
        } finally {
            String msg = org.openide.util.NbBundle.getMessage(SAXGeneratorSupport.class, "MSG_sax_progress_done");
            GuiUtil.setStatusText(msg); 
        }
    }

    /*
     * Try to locate previous settings and reuse it.
     */
    private void loadPrevious(FileObject folder) {
        InputStream in = null;

        try {
            FileObject previous = folder.getFileObject(model.getBindings(), "xml"); // NOI18N
            if (previous == null) return;

            if ( previous.isVirtual() ) {
                // file is virtual -- not available
                return;
            }

            in = previous.getInputStream();
            InputSource input = new InputSource(previous.toURL().toExternalForm());
            input.setByteStream(in);

            SAXBindingsHandlerImpl handler = new SAXBindingsHandlerImpl();
            SAXBindingsParser parser = new SAXBindingsParser(handler);

            XMLReader reader = XMLUtil.createXMLReader(true);
            reader.setEntityResolver(EntityCatalog.getDefault());
            reader.setContentHandler(parser);
            reader.parse(input);

            model.loadElementBindings(handler.getElementBindings());
            model.loadParsletBindings(handler.getParsletBindings());

        } catch (IOException ex) {
            // last settings are not restored
           // if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Cannot read settings", ex); // NOI18N
        } catch (SAXException ex) {
            // last settings are not restored
          //  if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Cannot read settings", ex); // NOI18N
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException e) {
                // let it be
            }
        }

    }

    /*
     * Prepend to document file header and save it.
     */
    private void trySave(DataObject obj, String data) throws IOException {
        if (obj == null) return;

        try {
            EditorCookie editor = obj.getCookie(EditorCookie.class);
            Document doc = editor.openDocument();

            if (data == null) {
                // file header can not be manipulated via src hiearchy
                data = GenerateSupportUtils.getJavaFileHeader (obj.getName(), null) + "\n"; // NOI18N
            } else {
                doc.remove(0, doc.getLength());
            }

            doc.insertString(0, data, null);
        } catch (IOException ex) {
            // ignore, there will be missing file header
        } catch (BadLocationException ex) {
            // ignore, there will be missing file header
        }

        SaveCookie cake = obj.getCookie(SaveCookie.class);
        if (cake != null) cake.save();
    }

    /*
     * Wait until source cookie and return SourceElement.
     */
   // private SourceElement openSource(DataObject obj) {
     //   if (obj == null) return null;

       // SourceCookie cake = null;
        //while (cake == null) {
          //  cake = (SourceCookie) obj.getCookie(SourceCookie.class);
        //}

        //return cake.getSource();
    //}

//    /**
//     * Generate code using given generator.
//     * @param target SourceElement where to place result, a null value indicates to skip
//     */
//    private void generateCode(CodeGenerator factory, SourceElement target, String packageName) throws IOException, SourceException {
//
//        if (target == null) return;
//
//        // kill all original stuff
//
//        if (target.getClasses().length > 0) {
//            target.removeClasses(target.getClasses());
//        }
//
//        // generate new one
//
//        if (packageName != null && packageName.length() > 0) {
//            target.setPackage(Identifier.create(packageName));
//        }
//
//        target.setImports(new Import[] {new Import(Identifier.create("org.xml.sax"), true)}); // NOI18N
//        factory.generate(target);
//
//    }
//
//
    /**
     * Generate stub using parslet and dispatching to given handler.
     */
    private void generateStub(FileObject clazz, String name, final String face, final String let) throws IOException  {

        final String constructorName = clazz.getName();
        JavaSource targetSource = JavaSource.forFileObject(clazz);
            
          CancellableTask task = new CancellableTask() {

                public void cancel() {
                  
                }

               
                public void run(Object parameter) throws Exception {
                    WorkingCopy workingCopy = (WorkingCopy)parameter;
                    workingCopy.toPhase(Phase.RESOLVED);
                    ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                    
                    if (javaClass!=null) {
                        TreeMaker make = workingCopy.getTreeMaker();
                        GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                        CompilationUnitTree cut = workingCopy.getCompilationUnit();
                        CompilationUnitTree copy = cut;
                        copy = make.addCompUnitImport(copy, make.Import(make.Identifier("org.xml.sax.*"), false));
                        workingCopy.rewrite(cut, copy);
                        
                        // add implementation clause
                        String interfaceName = getSAXHandlerInterface();
                        ClassTree modifiedClass = genUtils.addImplementsClause(javaClass, interfaceName);
                        
                        //add private class fields
                        List<VariableTree> varTree = new ArrayList<>();
                        ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PRIVATE));
                        Tree tree = make.Identifier(face);
                        VariableTree var = make.Variable(mods, "handler", tree, null);
                        varTree.add(var);
                        TypeElement type =  workingCopy.getElements().getTypeElement("java.util.Stack");// NOI18N
                        ExpressionTree etree = make.QualIdent(type);
                        var = make.Variable(mods, "context", etree, null);
                        varTree.add(var);      
                        type =  workingCopy.getElements().getTypeElement("java.lang.StringBuffer");// NOI18N
                        etree = make.QualIdent(type);
                        var = make.Variable(mods, "buffer", etree, null);
                        varTree.add(var);
                        type =  workingCopy.getElements().getTypeElement("org.xml.sax.EntityResolver");// NOI18N
                        etree= make.QualIdent(type);
                        //tree = make.Identifier("org.xml.sax.EntityResolver");
                        var = make.Variable(mods, "resolver", etree, null);
                        varTree.add(var);
                        if (model.hasParslets()) {
                            tree = make.Identifier(let);
                            var = make.Variable(mods, "parslet", tree, null);
                            varTree.add(var);
                        }
                        modifiedClass = genUtils.addClassFields(modifiedClass, varTree);                        
                        
                         //add Constructor
                        mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
                        StringBuffer sb = new StringBuffer();
                        String parsletInit = model.hasParslets() ? "\nthis.parslet = parslet;" : ""; // NOI18N
                        
                        sb.append("{\n" + parsletInit + "\nthis.handler = handler;\n" + // NOI18N
                                                 "this.resolver = resolver;\n" + // NOI18N
                                                  "buffer = new StringBuffer(111);\ncontext = new java.util.Stack();\n"    // NOI18N
                        );
                        sb.append("}");
                        
                        mods = make.Modifiers(EnumSet.of(Modifier.FINAL));
                        varTree = new ArrayList<>();
                        if (model.hasParslets()) {
                            tree = make.Identifier(face);
                            var = make.Variable(mods, "handler", tree, null);
                            varTree.add(var);
                            type =  workingCopy.getElements().getTypeElement("org.xml.sax.EntityResolver");// NOI18N
                            etree= make.QualIdent(type);
                            var = make.Variable(mods, "resolver", etree, null);
                            varTree.add(var);
                            tree = make.Identifier(let);
                            var = make.Variable(mods, "parslet", tree, null);
                            varTree.add(var);
                        } else {
                            tree = make.Identifier(face);
                            var = make.Variable(mods, "handler", tree, null);
                            varTree.add(var);
                            type =  workingCopy.getElements().getTypeElement("org.xml.sax.EntityResolver");// NOI18N
                            etree= make.QualIdent(type);
                            var = make.Variable(mods, "resolver", etree, null);
                            varTree.add(var);
                        }
                        
                        MethodTree newConstructor = genUtils.createConstructor(make.Modifiers(EnumSet.of(Modifier.PUBLIC)), constructorName, varTree, sb.toString());
                        modifiedClass = make.addClassMember(modifiedClass, newConstructor);
                        String commentText =  "\nCreates a parser instance.\n" +  // NOI18N
                                              "@param handler handler interface implementation (never <code>null</code>\n" +  // NOI18N
                                              "@param resolver SAX entity resolver implementation or <code>null</code>.\n" +  // NOI18N
                                              "It is recommended that it could be able to resolve at least the DTD.";  // NOI18N
                        if (model.hasParslets()) {
                            commentText += "@param parslet convertors implementation (never <code>null</code>\n"; //NOI18N
                        }
                        Comment comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, commentText);
                        make.addComment(newConstructor, comment, true);
                         
                       workingCopy.rewrite(javaClass, modifiedClass);
                    }
                }
       
           };
             targetSource.runModificationTask(task).commit();
//
//        clazz.getJavaDoc().setRawText(
//            "\nThe class reads XML documents according to specified DTD and " + // NOI18N
//            "\ntranslates all related events into " + face + " events." + // NOI18N
//            "\n<p>Usage sample:\n" + // NOI18N
//            "<pre>\n" + // NOI18N
//            "    " + name + " parser = new " + name + "(...);\n" + // NOI18N
//            "    parser.parse(new InputSource(\"...\"));\n" + // NOI18N
//            "</pre>\n" + // NOI18N
//            "<p><b>Warning:</b> the class is machine generated. DO NOT MODIFY</p>\n" // NOI18N
//        );
//



//
        genStubClass(clazz);
//
//        return clazz;
    }


    /**
     * Generate ClassElement representing interface to a handler.
     */
    private void generateInterface(FileObject clazz)  throws IOException{
        JavaSource targetSource = JavaSource.forFileObject(clazz);            
        CancellableTask task = new CancellableTask() {

            public void cancel() {
            }
               
            public void run(Object parameter) throws Exception {
                WorkingCopy workingCopy = (WorkingCopy)parameter;
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                if (javaClass!=null) {
                    TreeMaker make = workingCopy.getTreeMaker();
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                    
                    ClassTree modifiedClass   = javaClass; 
                    if (model.isPropagateSAX()) {
                       // add implementation clause
                        String interfaceName = getSAXHandlerInterface();
                        modifiedClass = genUtils.addImplementsClause(javaClass, interfaceName);
                    }
                    
                    Iterator it = model.getElementBindings().values().iterator();
                    while (it.hasNext()) {
                        ElementBindings.Entry next = (ElementBindings.Entry) it.next();
                        
                        
                        // create a method according mapping table:
                        // public void $name($type data, $SAXattrs meta) throws SAXException;
                        String methodName ;
                        final String handler = next.getType();
                        MethodTree method;
                        ExpressionTree tree =genUtils.makeQualIdent(getSAXAttributes());
                        List varTree = new ArrayList();
                        VariableTree var;
                        ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.FINAL));
                        String commentText;
                        Comment comment;
                        
                        saxException = genUtils.makeQualIdent(SAX_EXCEPTION);
                        
                        if (next.IGNORE.equals(handler)) {
                            continue;
                        } else if (next.EMPTY.equals(handler)) {
                            var = make.Variable(mods, "meta", tree, null);
                            varTree.add(var);    
                            methodName = HANDLE_PREFIX + next.getMethod();
                            
                            method = make.Method(
                                make.Modifiers(Collections.singleton(Modifier.PUBLIC)),
                                methodName,
                                make.PrimitiveType(TypeKind.VOID),
                                Collections.<TypeParameterTree>emptyList(),
                                varTree,
                                Collections.singletonList(saxException),
                                (BlockTree)null,
                                null
                            
                            );
                        
                            commentText = "\nAn empty element event handling method.\n@param data value or null\n ";
                            comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, commentText);
                            make.addComment(method, comment, true);
                            modifiedClass = make.addClassMember(modifiedClass, method);

                        }
                        
                        if (next.DATA.equals(handler) || next.MIXED.equals(handler)) {
                            methodName = HANDLE_PREFIX + next.getMethod();
                            varTree = new ArrayList();
                            
                            ParsletBindings.Entry entry = (ParsletBindings.Entry) parsletsMap.getEntry(next.getParslet());
                            if(entry == null ){
                                var = make.Variable(mods, ParsletBindings.DATA, genUtils.makeQualIdent(ParsletBindings.STRING_TYPE), null);
                              
                            }else {
                                var = make.Variable(mods, ParsletBindings.DATA, genUtils.makeQualIdent(entry.getType()), null);
                            }
                            varTree.add(var);
                            var = make.Variable(mods, "meta", tree, null);
                            varTree.add(var);
                          
                            method = createInterfaceMethod(make, methodName, varTree, saxException, null);                          
                            if(method != null) {
                                commentText = "\nA data element event handling method.\n@param data value or null \n@param meta attributes\n" ;
                                comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, commentText);
                                make.addComment(method, comment, true);
                                modifiedClass = make.addClassMember(modifiedClass, method);
                            }
                       }
                        
                       if (next.CONTAINER.equals(handler) || next.MIXED.equals(handler)) {
                           // start method
                          methodName = START_PREFIX + next.getMethod();
                          var = make.Variable(mods, "meta", tree, null);
                          varTree = new ArrayList();
                          varTree.add(var);   
                          method = createInterfaceMethod(make, methodName, varTree, saxException, null);
                          if (method != null ) {
                              commentText = "\nA container element start event handling method.\n@param meta attributes\n" ;
                              comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, commentText);
                              make.addComment(method, comment, true);
                              modifiedClass = make.addClassMember(modifiedClass, method);
                          }                   
                          // end method

                          methodName = END_PREFIX + next.getMethod();
                          method = createInterfaceMethod(make, methodName, null, saxException, null);
                          if(method != null) {
                              commentText = "\nA container element end event handling method.\n" ;
                              comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, commentText);
                              make.addComment(method, comment, true);
                              modifiedClass = make.addClassMember(modifiedClass, method);
                          }
                       }
                    }
                    
                    workingCopy.rewrite(javaClass, modifiedClass);
                }
            }
       
           };
           targetSource.runModificationTask(task).commit();
    }

    /**
     * Generates sample handler implementation.
     * The implementation contains debug support and attribute switch.  //??? attribute switch
     */
    private void generateInterfaceImpl(FileObject clazz) throws IOException {
        JavaSource targetSource = JavaSource.forFileObject(clazz);
            
        CancellableTask task = new CancellableTask() {

            public void cancel() {
            }
               
            public void run(Object parameter) throws Exception {
                WorkingCopy workingCopy = (WorkingCopy)parameter;
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                    
                if (javaClass!=null) {
                    TreeMaker make = workingCopy.getTreeMaker();
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                    String pkg = model.getJavaPackage();
                    String className =  (pkg == null || pkg.equals("")) ? model.getHandler() :
                        pkg + "." + model.getHandler();
                    ClassTree modifiedClass   = genUtils.addImplementsClause(
                            javaClass, className);                    
                     //add private class fields
                        List varTree = new ArrayList();
                        ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC));
                        
                        Tree tree = make.PrimitiveType(TypeKind.BOOLEAN);
                        ExpressionTree init = make.Identifier("false");
                        VariableTree var = make.Variable(mods, "DEBUG", tree, init);
                        varTree.add(var);      
                        modifiedClass = genUtils.addClassFields(modifiedClass, varTree);
                        
                        Iterator it = model.getElementBindings().values().iterator();
                        while (it.hasNext()) {
                            ElementBindings.Entry next = (ElementBindings.Entry) it.next();
                                                
                            // create a method according mapping table:
                            // public void $name($type data, $SAXattrs meta) throws SAXException;
                            String methodName ;
                            final String handler = next.getType();
                            MethodTree method;
                            TypeElement type;
                            tree =genUtils.makeQualIdent(getSAXAttributes());
                            mods = make.Modifiers(EnumSet.of(Modifier.FINAL));
                            String commentText;
                            Comment comment;
                            StringBuffer sb;
                        
                            saxException = genUtils.makeQualIdent(SAX_EXCEPTION);
                        
                            if (next.IGNORE.equals(handler)) {
                                continue;
                            } else if (next.EMPTY.equals(handler)) {
                               
                                  methodName = HANDLE_PREFIX + next.getMethod();
                                  sb = new StringBuffer("{");
                                  sb.append("\nif (DEBUG) System.err.println(\"" + methodName + ": \" + meta\n");
                                  sb.append("}\n");        
                                  varTree = new ArrayList();
                                  var = make.Variable(mods, "meta", tree, null);
                                  varTree.add(var);
                                  method = createInterfaceMethod(make, methodName, varTree, saxException, sb.toString());
                                  modifiedClass = make.addClassMember(modifiedClass, method);
                            }
                            if (next.DATA.equals(handler) || next.MIXED.equals(handler)) {
                                methodName = HANDLE_PREFIX + next.getMethod();
                                sb = new StringBuffer("{");
                                sb.append("\nif (DEBUG) System.err.println(\"" + methodName + ": \" + meta\n");
                                sb.append("}\n"); 
                                varTree = new ArrayList();
                            
                                ParsletBindings.Entry entry = (ParsletBindings.Entry) parsletsMap.getEntry(next.getParslet());
                               if(entry == null ){
                                    var = make.Variable(mods, ParsletBindings.DATA, genUtils.makeQualIdent(ParsletBindings.STRING_TYPE), null);
                               }else {
                                    var = make.Variable(mods, ParsletBindings.DATA, genUtils.makeQualIdent(entry.getType()), null);
                               }
                               varTree.add(var);
                               var = make.Variable(mods, "meta", tree, null);
                               varTree.add(var);

                              method = createInterfaceMethod(make, methodName, varTree, saxException, sb.toString());
                              modifiedClass = make.addClassMember(modifiedClass, method);
               
                            }
                            
                            if (next.CONTAINER.equals(handler) || next.MIXED.equals(handler)) {

                                // start method
                                methodName = START_PREFIX + next.getMethod();
                                varTree = new ArrayList();
                                var = make.Variable(mods, "meta", tree, null);
                                varTree.add(var); 
                                sb = new StringBuffer("{");
                                sb.append("\nif (DEBUG) System.err.println(\"" + methodName + ": \" + meta);\n");
                                sb.append("}\n");
               
                                method = createInterfaceMethod(make, methodName, varTree, saxException, sb.toString());
                                if(method != null)
                                    modifiedClass = make.addClassMember(modifiedClass, method);


                                // end method
                                methodName = END_PREFIX + next.getMethod();
                                sb = new StringBuffer("{");
                                sb.append("\nif (DEBUG) System.err.println(\"" + methodName + "()\");\n");
                                sb.append("}\n");
                                method = createInterfaceMethod(make, methodName, null, saxException, sb.toString());
                                if(method != null)
                                    modifiedClass = make.addClassMember(modifiedClass, method);
                
                             }
                        }         
                        
                        workingCopy.rewrite(javaClass, modifiedClass);
                   }
               }
           };
             targetSource.runModificationTask(task).commit();
            
    }


    /**
     * Generate a ClassElement representing interface for parslets
     */
    private void generateParslet(FileObject clazz) throws IOException {
         JavaSource targetSource = JavaSource.forFileObject(clazz);
            
         CancellableTask task = new CancellableTask() {

            public void cancel() {
            }

               
            public void run(Object parameter) throws Exception {
                WorkingCopy workingCopy = (WorkingCopy)parameter;
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                ClassTree modifiedClass = javaClass;
                
                if (javaClass!=null) {
                    TreeMaker make = workingCopy.getTreeMaker();
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                    
                    ParsletBindings parslets = model.getParsletBindings();
                    Iterator it = parslets.keySet().iterator();
                    ModifiersTree mod = make.Modifiers(Collections.singleton(Modifier.PUBLIC));
                    List varTree = new ArrayList();
                    VariableTree var = make.Variable(mod, ParsletBindings.DATA, genUtils.makeQualIdent(ParsletBindings.STRING_TYPE), null);
                    varTree.add(var);
                    while (it.hasNext()) {
                        Entry entry = parslets.getEntry((String)it.next());
                        if(entry == null)
                            continue;
                        String methodName = entry.getId();
                        Tree returnType = genUtils.makeQualIdent(entry.getType());
                        MethodTree method = make.Method(
                                                mod,
                                                methodName,
                                                returnType,
                                                Collections.<TypeParameterTree>emptyList(),
                                                varTree,
                                                Collections.singletonList(saxException),
                                                (BlockTree)null,
                                                null);
                        modifiedClass = make.addClassMember(modifiedClass,method);
                     }
                    
                     workingCopy.rewrite(javaClass, modifiedClass);
                   }
               }
           };
             targetSource.runModificationTask(task).commit();
        }

  


    /**
     * Generate sample parslet implementation for well known types.
     * Iterate over all customized parslets.
     */
    private void generateParsletImpl(FileObject clazz) throws IOException {
        
        JavaSource targetSource = JavaSource.forFileObject(clazz);
            
         CancellableTask task = new CancellableTask() {

            public void cancel() {
            }

               
            public void run(Object parameter) throws Exception {
                WorkingCopy workingCopy = (WorkingCopy)parameter;
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                
                
                if (javaClass!=null) {
                    TreeMaker make = workingCopy.getTreeMaker();
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                    
                    ClassTree modifiedClass = genUtils.addImplementsClause(javaClass, model.getParslet());
                    
                     Iterator it = parsletsMap.keySet().iterator();
                     ModifiersTree mod = make.Modifiers(Collections.singleton(Modifier.PUBLIC));
                     List varTree = new ArrayList();
                     VariableTree var = make.Variable(mod, ParsletBindings.DATA, genUtils.makeQualIdent(ParsletBindings.STRING_TYPE), null);
                     varTree.add(var);
                     while (it.hasNext()) {
                         ParsletBindings.Entry entry = parsletsMap.getEntry((String)it.next());
                         if(entry == null)
                            continue;
                         String code = createParsletCode(entry);
                         String methodName = entry.getId();
                         Tree returnType = genUtils.makeQualIdent(entry.getType());
                         MethodTree method = make.Method(
                                                mod,
                                                methodName,
                                                returnType,
                                                Collections.<TypeParameterTree>emptyList(),
                                                varTree,
                                                Collections.singletonList(saxException),
                                                code,
                                                null);
                         modifiedClass = make.addClassMember(modifiedClass,method);
                    }
                     
                    
                    workingCopy.rewrite(javaClass, modifiedClass);
                   }
               }
           };
             targetSource.runModificationTask(task).commit();
             
//
//        ClassElement clazz = new ClassElement();
//	clazz.setModifiers (Modifier.PUBLIC);
//	clazz.setName (Identifier.create (name));
//	clazz.setInterfaces (new Identifier[] { Identifier.create(model.getParslet()) });
//
//        MethodElement method = null;
//        String code = null;
//
//        Iterator it = parsletsMap.keySet().iterator();
//        while (it.hasNext()) {
//            method = parsletsMap.getMethod((String)it.next());
//            code = createParsletCode(method);
//            method.setBody(code);
//            clazz.addMethod(method);
//        }
//
//        return clazz;
//
    }

    /**
     * Create a sample convertor/parslet body.
     */
    private String createParsletCode(ParsletBindings.Entry parslet)  {
        String returnType = parslet.getType();
        String fragment = ""; // NOI18N
        String exception = "new SAXException(\"" + parslet.getId() + "(\" + data.trim() + \")\", ex)"; // NOI18N
        String catchBlock = "\n} catch (IllegalArgumentException ex) {\n throw " + exception + ";\n}"; // NOI18N

        if ("int".equals(returnType)) { // NOI18N
            fragment = "try {"; // NOI18N
            fragment+= "\nreturn Integer.parseInt(data.trim());"; // NOI18N
            fragment+= catchBlock;
        } else if ("boolean".equals(returnType)) { // NOI18N
            fragment = "return \"true\".equals(data.trim());"; // NOI18N
        } else if ("long".equals(returnType)) { // NOI18N
            fragment = "try {\nreturn Long.parseLong(data.trim());"; // NOI18N
            fragment+= catchBlock;
        } else if ("java.util.Date".equals(returnType)) { // NOI18N
            fragment = "try {"; // NOI18N
            fragment+= "\nreturn java.text.DateFormat.getDateInstance().parse(data.trim());"; // NOI18N
            fragment+= "\n}catch(java.text.ParseException ex) {"; // NOI18N
            fragment+= "\nthrow "+ exception + ";\n}"; // NOI18N
        } else if ("java.net.URL".equals(returnType)) { // NOI18N
            fragment = "try {"; // NOI18N
            fragment+= "\n  return new java.net.URL(data.trim());"; // NOI18N
            fragment+= "\n} catch (java.net.MalformedURLException ex) {"; // NOI18N
            fragment+= "\n throw " + exception +";\n}"; // NOI18N
        } else if ("java.lang.String[]".equals(returnType)) { // NOI18N
            fragment = "java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(data.trim());"; // NOI18N
            fragment+= "\njava.util.ArrayList list = new java.util.ArrayList();"; // NOI18N
            fragment+= "\nwhile (tokenizer.hasMoreTokens()) {"; // NOI18N
            fragment+= "\nlist.add(tokenizer.nextToken());"; // NOI18N
            fragment+= "\n}"; // NOI18N
            fragment+= "\nreturn (String[]) list.toArray(new String[0]);"; // NOI18N
        } else {
            fragment = "throw new SAXException(\"Not implemented yet.\");"; // NOI18N
        }

        return "\n" + fragment + "\n"; // NOI18N
    }

//    //~~~~~~~~~~~~~~~~~~~~ guess initial mapping ~~~~~~~~~~~~~~~~~~~~~~
//
    private void initMappings() {
        try {
            getDTD();

            Iterator it = dtd.getElementDeclarations().iterator();
            while (it.hasNext()) {
                TreeElementDecl next = (TreeElementDecl) it.next();
                addElementMapping(next);
            }
        } catch (IOException ex) {
            // let the map empty
        } catch (TreeException ex) {
            // let the map empty
        }
    }

    private void addElementMapping(TreeElementDecl decl) {
        String name = decl.getName();
        String javaName = GenerateSupportUtils.getJavaName(name);

        String defaultMapping = ElementBindings.Entry.DATA;

        if (decl.isMixed()) {
            defaultMapping = ElementBindings.Entry.MIXED;
        } else if (decl.allowElements()) {
            defaultMapping = ElementBindings.Entry.CONTAINER;
        } else if (decl.isEmpty()) {
            defaultMapping = ElementBindings.Entry.EMPTY;
        }

        elementMapping.put(name, javaName, null, defaultMapping);
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~ generator methods ~~~~~~~~~~~~~~~~~~~~~~~~


//    /**
//     * Stub's startElement() method has two forms one for SAX 1.0 and one for SAX 2.0
//     */
    private MethodTree genStartElementMethod(TreeMaker make)  {
        MethodTree method = null;
        ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC,Modifier.FINAL));
        Tree stree = make.Identifier("java.lang.String");
        if (sax == 1) {
            //first create the body
             StringBuffer code = new StringBuffer("{");
             code.append("\n" + EMMIT_BUFFER + "(true);"); // NOI18N
             code.append("\ncontext.push(new Object[] {name, new org.xml.sax.helpers.AttributeListImpl(attrs)});"); // NOI18N

            // generate start events for container methods

            code.append(createStartEndEvents(START_PREFIX, "attrs", HANDLE_PREFIX)); // NOI18N

            if (model.isPropagateSAX())
                code.append("\nhandler." + M_START_ELEMENT + "(name, attrs);"); // NOI18N

            code.append("}\n"); // NOI18N
           
            // make a variable trees - representing parameters
            VariableTree par1 = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "name", stree, null);
            Tree tree = make.Identifier(getSAXAttributes());
            VariableTree par2 = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "attrs", tree, null);
            List parList = new ArrayList(2);
            parList.add(par1);
            parList.add(par2);
            
            method = make.Method(
                            mods,
                            M_START_ELEMENT,
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            parList,
                            Collections.singletonList(make.Identifier(SAX_EXCEPTION)),
                            code.toString(),
                            null
                        );

        } else if (sax == 2) {
            StringBuffer code = new StringBuffer("{\n");
            code.append("\n" + EMMIT_BUFFER + "(true);"); // NOI18N
            code.append("\ncontext.push(new Object[] {qname, new org.xml.sax.helpers.AttributesImpl(attrs)});"); // NOI18N

            code.append(createStartEndEvents(START_PREFIX, "attrs", HANDLE_PREFIX)); // NOI18N

            if (model.isPropagateSAX())
                code.append("\nhandler." + M_START_ELEMENT + "(ns, name, qname, attrs);"); // NOI18N

            code.append("}\n"); // NOI18N
            // make a variable trees - representing parameters
            VariableTree par1 = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "ns", stree, null);
            List parList = new ArrayList();
            parList.add(par1);
            par1 = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "name", stree, null);
            parList.add(par1);
            par1 = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "qname", stree, null);
            parList.add(par1);
            Tree tree = make.Identifier(getSAXAttributes());
            VariableTree par2 = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "attrs", tree, null);
            parList.add(par2);
            
            method = make.Method(
                            mods,
                            M_START_ELEMENT,
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            parList,
                            Collections.singletonList(make.Identifier(SAX_EXCEPTION)),
                            code.toString(),
                            null
                        );
//
        }
//
        return method;
    }


    /**
     * Stub's endElement() method has two forms one for SAX 1.0 and one for SAX 2.0
     */
    private MethodTree genEndElementMethod(TreeMaker make)   {
        MethodTree method = null;
        ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC,Modifier.FINAL));
        Tree stree = make.Identifier("java.lang.String");
        if (sax == 1) {
            //generate the body
            StringBuffer code = new StringBuffer("{");
            code.append("\n" + EMMIT_BUFFER + "(false);"); // NOI18N
            code.append("\ncontext.pop();"); // NOI18N

            code.append(createStartEndEvents(END_PREFIX, "", null)); // NOI18N

            if (model.isPropagateSAX())
                code.append("\nhandler." + M_END_ELEMENT + "(name);"); // NOI18N

            code.append("}\n"); // NOI18N
            // make a variable trees - representing parameters
            VariableTree par1 = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "name", stree, null);
            List parList = new ArrayList();
            parList.add(par1);
                        
            method = make.Method(
                            mods,
                            M_END_ELEMENT,
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            parList,
                            Collections.singletonList(make.Identifier(SAX_EXCEPTION)),
                            code.toString(),
                            null
                        );
            
        } else if (sax == 2) {
            StringBuffer code = new StringBuffer("{\n");
            code.append("\n" + EMMIT_BUFFER + "(false);"); // NOI18N
            code.append("\ncontext.pop();"); // NOI18N

            code.append(createStartEndEvents(END_PREFIX, "", null)); // NOI18N

            if (model.isPropagateSAX())
                code.append("\nhandler." + M_END_ELEMENT + "(ns, name, qname);"); // NOI18N

            code.append("}\n"); // NOI18N
            // make a variable trees - representing parameters
            VariableTree par1 = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "ns", stree, null);
            List parList = new ArrayList();
            parList.add(par1);
            par1 =  make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "name", stree, null);
            parList.add(par1);
            par1 =  make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "qname", stree, null);
            parList.add(par1);
            
            method = make.Method(
                            mods,
                            M_END_ELEMENT,
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            parList,
                            Collections.singletonList(make.Identifier(SAX_EXCEPTION)),
                            code.toString(),
                            null
                        );
            
        }

        return method;
    }


    /*
     * @param prefix prefix of container method
     * @param meta name of passed meta parameter or ""
     * @param emptyPrefix name of empty element handler method or null
     */
    private String createStartEndEvents(String methodPrefix, String meta, String emptyPrefix) {

        StringBuffer code = new StringBuffer(233);

        Iterator it = model.getElementBindings().values().iterator();
        String prefix = "\nif"; // NOI18N
        while (it.hasNext()) {
            ElementBindings.Entry next = (ElementBindings.Entry) it.next();

            String handling = next.getType();
            String name = sax == 1 ? "name" : "qname"; // NOI18N

            if (next.CONTAINER.equals(handling) || next.MIXED.equals(handling)) {
                code.append(prefix + " (\"" + next.getElement() + "\".equals(" + name + ")) {"); // NOI18N
                code.append("\nhandler." + methodPrefix + next.getMethod() + "(" + meta + ");"); // NOI18N
                code.append("\n}"); // NOI18N
                prefix = " else if"; // NOI18N
            } else if (emptyPrefix != null && next.EMPTY.equals(handling)) {
                code.append(prefix + " (\"" + next.getElement() + "\".equals(" + name + ")) {"); // NOI18N
                code.append("\nhandler." + emptyPrefix + next.getMethod() + "(" + meta + ");"); // NOI18N
                code.append("\n}"); // NOI18N
                prefix = " else if"; // NOI18N
            }
        }

        return code.toString();
    }

    /**
     * Stub's method. It is SAX 2.0 method
     */
    private MethodTree genStartPrefixMappingMethod(TreeMaker make)  {
        MethodTree method = null;
        if (sax == 2) {
            //first create the body
             StringBuffer code = new StringBuffer("{\n");
             if (model.isPropagateSAX())
                code.append ("handler." + M_START_PREFIX_MAPPING + "(prefix, uri);\n"); // NOI18N
             code.append("}\n");
             
             //method params
             Tree stree = make.Identifier("java.lang.String");
             VariableTree par1 = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "prefix", stree, null);
             VariableTree par2 = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "uri", stree, null);
             List parList = new ArrayList(2);
             parList.add(par1);
             parList.add(par2);
             ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC,Modifier.FINAL));
        
              method = make.Method(
                            mods,
                            M_START_PREFIX_MAPPING,
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            parList,
                            Collections.singletonList(make.Identifier(SAX_EXCEPTION)),
                            code.toString(),
                            null
                        );
        }

        return method;
    }


    /**
     * Stub's method. It is SAX 2.0 method
     */
    private MethodTree genEndPrefixMappingMethod(TreeMaker make)   {
        MethodTree method = null;
        if (sax == 2) {
            //first create the body
             StringBuffer code = new StringBuffer("{\n");
             if (model.isPropagateSAX())
                code.append ("\nhandler." + M_END_PREFIX_MAPPING + "(prefix);\n"); // NOI18N
             code.append("}\n");
             
             //method params
             Tree stree = make.Identifier("java.lang.String");
             VariableTree par1 = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "prefix", stree, null);
             List parList = new ArrayList();
             parList.add(par1);
             ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC,Modifier.FINAL));
        
              method = make.Method(
                            mods,
                            M_END_PREFIX_MAPPING,
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            parList,
                            Collections.singletonList(make.Identifier(SAX_EXCEPTION)),
                            code.toString(),
                            null
                        );
        
        }
        return method;
    }

    /**
     * Stub's method. It is SAX 2.0 method
     */
    private MethodTree genSkippedEntityMethod(TreeMaker make)   {
        MethodTree method = null;
        if (sax == 2) {
            //first create the body
             StringBuffer code = new StringBuffer("{\n");
             if (model.isPropagateSAX())
                code.append("\nhandler." + M_SKIPPED_ENTITY + "(name);\n"); // NOI18N
             code.append("}\n");
             
             //method params
             Tree stree = make.Identifier("java.lang.String");
             VariableTree par1 = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "name", stree, null);
             List parList = new ArrayList();
             parList.add(par1);
             ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC,Modifier.FINAL));
        
              method = make.Method(
                            mods,
                            M_SKIPPED_ENTITY,
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            parList,
                            Collections.singletonList(make.Identifier(SAX_EXCEPTION)),
                            code.toString(),
                            null
                        );
        
        }
        return method;
    }

    /**
     * Generate stub's  handling methods.
     * @param clazz to be filled with stub methods
     */
    private void genStubClass (FileObject clazz)  throws IOException {
        
        JavaSource targetSource = JavaSource.forFileObject(clazz);
        
        CancellableTask task = new CancellableTask() {

                public void cancel() {
                  
                }

               
                public void run(Object parameter) throws Exception {
                    WorkingCopy workingCopy = (WorkingCopy)parameter;
                    workingCopy.toPhase(Phase.RESOLVED);
                    ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                    
                    if (javaClass!=null) {
                        TreeMaker make = workingCopy.getTreeMaker();
                        GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                                               
                        //create setDocumentLocator() method
                        ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC,Modifier.FINAL));
                        TypeElement type =  workingCopy.getElements().getTypeElement(SAX_LOCATOR);// NOI18N
                        ExpressionTree tree= make.QualIdent(type);
                        VariableTree var = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "locator", tree, null);
                        List varTree = new ArrayList();
                        varTree.add(var);
                        
                       //Create body of the method 
                        StringBuffer sb = new StringBuffer("{"); // NOI18N
                        if (model.isPropagateSAX())
                            sb.append("\nhandler." + M_SET_DOCUMENT_LOCATOR + "(locator);\n");
                        sb.append("}");
                        
                        MethodTree method = make.Method(
                            mods,
                            M_SET_DOCUMENT_LOCATOR,
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            varTree,
                            Collections.<ExpressionTree>emptyList(),
                            sb.toString(),
                            null
                        );
                        
                        String commentText = "\nThis SAX interface method is implemented by the parser. ";
                        Comment comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, commentText);
                        make.addComment(method, comment, true);
                        ClassTree modifiedClass = make.addClassMember(javaClass, method);
                        
                         // create startDocument() method
                        sb = new StringBuffer("{");
                         if (model.isPropagateSAX())
                            sb.append("\nhandler." + M_START_DOCUMENT + "();\n");
                        sb.append("}");
                        
                        type =  workingCopy.getElements().getTypeElement(SAX_EXCEPTION);// NOI18N
                        saxException = make.QualIdent(type);
                        
                        method = make.Method(
                            mods,
                            M_START_DOCUMENT,
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            Collections.<VariableTree>emptyList(),
                            Collections.singletonList(saxException),
                            sb.toString(),
                            null
                        );
                        make.addComment(method, comment, true);
                        modifiedClass = make.addClassMember(modifiedClass, method);
        
                        // create endDocument() method
                         sb = new StringBuffer("{");
                         if (model.isPropagateSAX())
                            sb.append("\nhandler." + M_END_DOCUMENT + "();\n");
                        sb.append("}");
                        method = make.Method(
                            mods,
                            M_END_DOCUMENT,
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            Collections.<VariableTree>emptyList(),
                            Collections.singletonList(saxException),
                            sb.toString(),
                            null
                        );
                        make.addComment(method, comment, true);
                        modifiedClass = make.addClassMember(modifiedClass, method);

                      // create startElement()
                      method = genStartElementMethod(make);
                      make.addComment(method, comment, true);
                      modifiedClass = make.addClassMember(modifiedClass, method);
            
                     //create endElement()
                     method = genEndElementMethod(make);
                     make.addComment(method, comment, true);
                     modifiedClass = make.addClassMember(modifiedClass, method);
      
                    //create characters() method
                     sb = new StringBuffer("{");
                     sb.append("\nbuffer.append(chars, start, len);"); // NOI18N
                     if (model.isPropagateSAX())
                         sb.append("handler." + M_CHARACTERS + "(chars, start, len);"); // NOI18N
                     sb.append("}\n"); // NOI18N
                     
                     //method params
                     varTree = new ArrayList();
                     TypeMirror charMirror = workingCopy.getTypes().getPrimitiveType(TypeKind.CHAR);
                     ArrayType arrayType = workingCopy.getTypes().getArrayType(charMirror);
                     Tree charArray = make.Type(arrayType);
                     var = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "chars", charArray, null);
                     varTree.add(var);
                     Tree ptree = make.PrimitiveType(TypeKind.INT);
                     var = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "start", ptree, null);
                     varTree.add(var);
                     var = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "len", ptree, null);
                     varTree.add(var);
                     method = make.Method(
                            mods,
                            M_CHARACTERS,
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            varTree,
                            Collections.singletonList(saxException),
                            sb.toString(),
                            null
                        );
                     make.addComment(method, comment, true);
                     modifiedClass = make.addClassMember(modifiedClass, method);
        
                     //create ignorableWhiteSpace() method
                     sb = new StringBuffer("{\n");
                     if (model.isPropagateSAX())
                         sb.append("handler." + M_IGNORABLE_WHITESPACE + "(chars, start, len);\n"); // NOI18N
                     sb.append("}\n");
                     //method params are same as the characters() method
                     
                     method = make.Method(
                            mods,
                            M_IGNORABLE_WHITESPACE,
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            varTree,
                            Collections.singletonList(saxException),
                            sb.toString(),
                            null
                        );
                     make.addComment(method, comment, true);
                     modifiedClass = make.addClassMember(modifiedClass, method);
                     
                     //create processingInstruction() methd
                      sb = new StringBuffer("{\n");
                     if (model.isPropagateSAX())
                         sb.append("handler." + M_PROCESSING_INSTRUCTION + "(target, data);\n");   // NOI18N
                      sb.append("}\n");
                      
                     //params
                     varTree = new ArrayList();
                     type =  workingCopy.getElements().getTypeElement("java.lang.String");// NOI18N
                     tree= make.QualIdent(type);
                     var = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "target", tree, null);
                     varTree.add(var);
                     var = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "data", tree, null);
                     varTree.add(var);
                     
                     method = make.Method(
                            mods,
                            M_PROCESSING_INSTRUCTION,
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            varTree,
                            Collections.singletonList(saxException),
                            sb.toString(),
                            null
                        );
                     make.addComment(method, comment, true);
                     modifiedClass = make.addClassMember(modifiedClass, method);
                      
                     // SAX 2.0 only methods

                    method = genStartPrefixMappingMethod(make);
                    if (method != null) modifiedClass = make.addClassMember(modifiedClass, method);

                    method = genEndPrefixMappingMethod(make);
                    if (method != null) modifiedClass = make.addClassMember(modifiedClass, method);

                    method = genSkippedEntityMethod(make);
                    if (method != null) modifiedClass = make.addClassMember(modifiedClass, method);
                    
                    // private dispatching method
                    method = genEmmitBufferMethod(make);
                    if (method != null) modifiedClass = make.addClassMember(modifiedClass, method);

                    // optional static and dynamic methods that a user can appreciate

                     initExceptions(workingCopy, make);
                     method = genJAXPParseInputSourceMethod(make);
                     modifiedClass = make.addClassMember(modifiedClass, method);
                     
                     method = genJAXPParseURLMethod(make);
                     modifiedClass = make.addClassMember(modifiedClass, method);

                     method = genJAXP_ParseInputSourceMethod(make);
                     modifiedClass = make.addClassMember(modifiedClass, method);
                     
                     method = genJAXP_ParseURLMethod(make);
                     modifiedClass = make.addClassMember(modifiedClass, method);

                     method = genJAXP_ParseSupportMethod(make);
                     modifiedClass = make.addClassMember(modifiedClass, method);
                     
                     method = genSampleErrorHandler(make);
                     modifiedClass = make.addClassMember(modifiedClass, method);
                     
                     workingCopy.rewrite(javaClass, modifiedClass);
                    }
                }

                   
           };
             targetSource.runModificationTask(task).commit();


    }


    /**
     * Generate stubs's switch dispatching to handler (an interface).
     */
    private MethodTree genEmmitBufferMethod(TreeMaker make) {

          MethodTree methodElement = null;
          
          ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PRIVATE));
          Tree tree = make.PrimitiveType(TypeKind.BOOLEAN);
          VariableTree par1 = make.Variable(make.Modifiers(EnumSet.of(Modifier.FINAL)), "fireOnlyIfMixed", tree, null);
          List parList = new ArrayList();
          parList.add(par1);
          
          //create the body of the method
          StringBuffer buf = new StringBuffer("{");

          buf.append("\nif (fireOnlyIfMixed && buffer.length() == 0) return; //skip it\n"); // NOI18N
          buf.append("\nObject[] ctx = (Object[]) context.peek();\n");  // NOI18N
          buf.append("String here = (String) ctx[0];\n");             // NOI18N

          buf.append(getSAXAttributes() + " attrs = (" + getSAXAttributes() + ") ctx[1];\n"); // NOI18N

          String switchPrefix = "if"; // NOI18N

          Iterator it = model.getElementBindings().values().iterator();
          while (it.hasNext()) {
              ElementBindings.Entry next = (ElementBindings.Entry) it.next();

              String name = next.getElement();
              String method = HANDLE_PREFIX + elementMapping.getMethod(name);
              String parslet = elementMapping.getParslet(name);

              String data = "buffer.length() == 0 ? null : buffer.toString()"; // NOI18N
              parslet = parslet == null ? data : "parslet." + parslet + "(" + data + ")"; // NOI18N

              String handling = next.getType();

              if (next.DATA.equals(handling) || next.MIXED.equals(handling)) {
                  buf.append(switchPrefix + " (\"" + name + "\".equals(here)) {\n" );   // NOI18N
                  if (next.DATA.equals(handling)) {
                      buf.append("if (fireOnlyIfMixed) throw new IllegalStateException(\"Unexpected characters() event! (Missing DTD?)\");\n"); // NOI18N
                  }
                  buf.append("handler." + method + "(" + parslet + ", attrs);\n");  // NOI18N

                  switchPrefix = "} else if"; // NOI18N
              }
          }

          if (switchPrefix.equals("if") == false) { // NOI18N
              buf.append("} else {\n //do not care\n}\n");        // NOI18N
          }
          buf.append("buffer.delete(0, buffer.length());\n"); // NOI18N
          
          buf.append("}\n");
          

         methodElement = make.Method(
                            mods,
                            EMMIT_BUFFER,
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            parList,
                            Collections.singletonList(saxException),
                            buf.toString(),
                            null
                        );
        return methodElement;

    }


    
    //  JAXP related methods.
    

    /**
     * Generate static JAXP support method
     */
    private MethodTree genJAXP_ParseSupportMethod(TreeMaker make)  {
        
        //create body
        StringBuffer sb = new StringBuffer("{");
        String parser = sax == 1 ? "Parser" : "XMLReader";
        sb.append("\n" + // NOI18N
            JAXP_PACKAGE + "SAXParserFactory factory = " + JAXP_PACKAGE + "SAXParserFactory.newInstance();\n" + // NOI18N
            "factory.setValidating(true);  //the code was generated according DTD\n" + // NOI18N
            "factory.setNamespaceAware(false);  //the code was generated according DTD\n" + // NOI18N
            parser + " parser = factory.newSAXParser().get" + parser + "();\n" + // NOI18N
            "parser.set" + (sax == 1 ? "Document" : "Content") + "Handler(recognizer);\n" + // NOI18N
            "parser.setErrorHandler(recognizer.getDefaultErrorHandler());\n" + // NOI18N
            "if (recognizer.resolver != null) parser.setEntityResolver(recognizer.resolver);\n" + // NOI18N
            "parser.parse(input);" + // NOI18N
            "\n" // NOI18N
        );
        sb.append("}\n");
        
        //method params
        Tree tree = make.Identifier(SAX_INPUT_SOURCE);
        VariableTree par1 = make.Variable(make.Modifiers(EnumSet.of(Modifier.FINAL)), "input", tree, null);
        List parList = new ArrayList();
        parList.add(par1);
        tree = make.Identifier(model.getStub());
        par1 = make.Variable(make.Modifiers(EnumSet.of(Modifier.FINAL)), "recognizer", tree, null);
        parList.add(par1);
        ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PRIVATE, Modifier.STATIC));
        
        MethodTree method = make.Method(
                            mods,
                            M_PARSE,
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            parList,
                            exceptions,
                            sb.toString(),
                            null
                        );

        return method;
    }


    private MethodTree genSampleErrorHandler(TreeMaker make)  {
        StringBuffer sb = new StringBuffer("{");
        sb.append("\n" + // NOI18N
            "return new ErrorHandler() { \n" + // NOI18N
            "public void error(SAXParseException ex) throws SAXException  {\n" + // NOI18N
            "if (context.isEmpty()) System.err.println(\"Missing DOCTYPE.\");\n" + // NOI18N
            "throw ex;\n" + // NOI18N
            "}\n" + // NOI18N
            "\n" + // NOI18N
            "public void fatalError(SAXParseException ex) throws SAXException {\n" + // NOI18N
            "throw ex;\n" + // NOI18N
            "}\n" + // NOI18N
            "\n" + // NOI18N
            "public void warning(SAXParseException ex) throws SAXException {\n" + // NOI18N
            "// ignore\n" + // NOI18N
            "}\n" + // NOI18N
            "};\n" + // NOI18N
            "\n" // NOI18N
        );
        sb.append("}\n");
        
        Tree returnType = make.Identifier("ErrorHandler");
        
        ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PROTECTED));
        MethodTree method = make.Method(
                            mods,
                            "getDefaultErrorHandler",
                            returnType,
                            Collections.<TypeParameterTree>emptyList(),
                            Collections.<VariableTree>emptyList(),
                            Collections.<ExpressionTree>emptyList(),
                            sb.toString(),
                            null
                        );
        
        String commentText ="\nCreates default error handler used by this parser.\n" + // NOI18N
                            "@return org.xml.sax.ErrorHandler implementation\n";  //NOI18N
        
        Comment comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, commentText);
        make.addComment(method, comment, true);
 
        return method;
    }

    /**
     * Generate JAXP compatible static method
     */
    private MethodTree genJAXP_ParseInputSourceMethod(TreeMaker make)  {
        
        //create body
        StringBuffer sb = new StringBuffer("{");
        String parsletParam = model.hasParslets() ? ", parslet" : ""; // NOI18N
        sb.append("\n" + // NOI18N
            M_PARSE + "(input, new " + model.getStub() + "(handler, null" + parsletParam + "));\n" // NOI18N
        );
        
        //method params
        List parList = new ArrayList();
        Tree tree = make.Identifier(SAX_INPUT_SOURCE);
        VariableTree par1 = make.Variable(make.Modifiers(EnumSet.of(Modifier.FINAL)), "input", tree, null);
        parList.add(par1);
        tree = make.Identifier(model.getHandler());
        par1 = make.Variable(make.Modifiers(EnumSet.of(Modifier.FINAL)), "handler", tree, null);
        parList.add(par1);
        if (model.hasParslets()) {
            tree = make.Identifier(model.getParslet());
            par1 = make.Variable(make.Modifiers(EnumSet.of(Modifier.FINAL)), "parslet", tree, null);
            parList.add(par1);
        } 
        ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC));
        
        MethodTree method = make.Method(
                            mods,
                            M_PARSE,
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            parList,
                            exceptions,
                            sb.toString(),
                            null
                        );
        
        String commentText ="\nThe recognizer entry method taking an Inputsource.\n" + // NOI18N
                            "@param input InputSource to be parsed.\n" + // NOI18N
                            JAXP_PARSE_EXCEPTIONS_DOC;
        
        Comment comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, commentText);
        make.addComment(method, comment, true);
 
        return method;
    }


    /**
     * Generate JAXP compatible static method
     */
    private MethodTree genJAXP_ParseURLMethod(TreeMaker make)  {
        
        String parsletParam = model.hasParslets() ? ", parslet" : ""; // NOI18N
        StringBuffer sb = new StringBuffer("{");
        sb.append("\n" + M_PARSE + "(new " + SAX_INPUT_SOURCE + "(url.toExternalForm()), handler" + parsletParam + ");" );
        sb.append("}\n");
        
        //method params
        List parList = new ArrayList();
        Tree tree = make.Identifier("java.net.URL");
        VariableTree par1 = make.Variable(make.Modifiers(EnumSet.of(Modifier.FINAL)), "url", tree, null);
        parList.add(par1);
        tree = make.Identifier(model.getHandler());
        par1 = make.Variable(make.Modifiers(EnumSet.of(Modifier.FINAL)), "handler", tree, null);
        parList.add(par1);
        if (model.hasParslets()) {
            tree = make.Identifier(model.getParslet());
            par1 = make.Variable(make.Modifiers(EnumSet.of(Modifier.FINAL)), "parslet", tree, null);
            parList.add(par1);
        } 
        ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC));
        
        MethodTree method = make.Method(
                            mods,
                            M_PARSE,
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            parList,
                            exceptions,
                            sb.toString(),
                            null
                        );
        
        String commentText ="\nThe recognizer entry method taking a URL.\n" + // NOI18N
                            "@param url URL source to be parsed.\n" + // NOI18N
                            JAXP_PARSE_EXCEPTIONS_DOC;
        
        Comment comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, commentText);
        make.addComment(method, comment, true);

        return method;
    }


    /**
     * Generate dynamic JAXP compatible method
     */
    private MethodTree genJAXPParseInputSourceMethod(TreeMaker make)  {
        
        //create the body
        StringBuffer sb = new StringBuffer("{");
        sb.append("\n" + M_PARSE + "(input, this);\n"); 
        sb.append("}\n");
         
        //method params
        Tree tree = make.Identifier(SAX_INPUT_SOURCE);
        VariableTree par1 = make.Variable(make.Modifiers(EnumSet.of(Modifier.FINAL)), "input", tree, null);
        List parList = new ArrayList();
        parList.add(par1);
        ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
        
        MethodTree method = make.Method(
                            mods,
                            M_PARSE,
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            parList,
                            exceptions,
                            sb.toString(),
                            null
                        );
        
        String commentText = "\nThe recognizer entry method taking an InputSource.\n" 
                            + "@param input InputSource to be parsed.\n" + JAXP_PARSE_EXCEPTIONS_DOC;
        Comment comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, commentText);
        make.addComment(method, comment, true);
        
        return method;

    }

    /**
     * Generate dynamic JAXP compatible method
     */
    private MethodTree genJAXPParseURLMethod(TreeMaker make)  {
        
        //create body
        StringBuffer sb = new StringBuffer("{");
        sb.append("\n" + M_PARSE + "(new " + SAX_INPUT_SOURCE + "(url.toExternalForm()), this);\n" );
        sb.append("}\n");
        
         //method params
        Tree tree = make.Identifier("java.net.URL");
        VariableTree par1 = make.Variable(make.Modifiers(EnumSet.of(Modifier.FINAL)), "url", tree, null);
        List parList = new ArrayList();
        parList.add(par1);
        ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
        
        MethodTree method = make.Method(
                            mods,
                            M_PARSE,
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.<TypeParameterTree>emptyList(),
                            parList,
                            exceptions,
                            sb.toString(),
                            null
                        );
        
        String commentText = "\nThe recognizer entry method taking a URL.\n" 
                            + "@param url URL Source to be parsed.\n" + JAXP_PARSE_EXCEPTIONS_DOC;
        Comment comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, commentText);
        make.addComment(method, comment, true);
        

        return method;
    }

    //~~~~~~~~~~~~~~~~~~~~ utility methods ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    
      private void initExceptions(WorkingCopy workingCopy, TreeMaker make) {
          TypeElement type =  workingCopy.getElements().getTypeElement(JAXP_PARSER_CONFIGURATION_EXCEPTION);// NOI18N
          parserConfigException = make.QualIdent(type);
          
          type =  workingCopy.getElements().getTypeElement(JAVA_IOEXCEPTION);// NOI18N
          ioException = make.QualIdent(type);  
          
          exceptions = new ArrayList();
          exceptions.add(saxException);
          exceptions.add(parserConfigException);
          exceptions.add(ioException);
      } 
      
      
//    /** Create specified field as private. */
//    private static FieldElement createField(String name, String clzz) throws SourceException {
//        FieldElement field = new FieldElement();
//        field.setName(Identifier.create(name));
//        field.setModifiers(Modifier.PRIVATE);
//        field.setType(Type.createClass(Identifier.create(clzz)));
//
//        return field;
//    }
//
    /** Utility method creating common MethodElement. */
    private static MethodTree createInterfaceMethod (TreeMaker make, String name, List params, ExpressionTree exception, String body)  {
        
         ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
         if(params == null)
             params = Collections.emptyList();
         List throwsException ; 
         if(exception == null )
             throwsException = Collections.emptyList();
         else {
             throwsException = Collections.singletonList(exception);
         }
         
         if(body == null) {                       
             MethodTree method = make.Method(
                mods,
                name,
                make.PrimitiveType(TypeKind.VOID),
                Collections.<TypeParameterTree>emptyList(),
                params,
                throwsException,
                (BlockTree)null,
                null);
             return method;
         } else {
              MethodTree method = make.Method(
                mods,
                name,
                make.PrimitiveType(TypeKind.VOID),
                Collections.<TypeParameterTree>emptyList(),
                params,
                throwsException,
                body,
                null);
             return method;
         }
//        MethodElement method = new MethodElement ();
//        method.setModifiers (Modifier.PUBLIC);
//        method.setReturn (Type.VOID);
//        method.setName (Identifier.create (name));
//        if (params != null)
//            method.setParameters (params);
//        if (exception != null)
//            method.setExceptions (new Identifier[] { org.openide.src.Identifier.create (exception) });
//        method.setBody ("\n"); // NOI18N
        
    }
//
//    /** Utility method creating common implementation MethodElement. */
//    private static MethodElement createImplementationMethod (String name, MethodParameter[] params, String exception) throws SourceException {
//        MethodElement method = createInterfaceMethod(name, params, exception);
//        method.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
//        String docText = "\nThis SAX interface method is implemented by the parser.\n"; // NOI18N
//        method.getJavaDoc().setRawText(docText);
//        return method;
//    }
//
    /** Get Schema. */
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

    /**
     * Return a Identifier of content handler interface in current sax version.
     */
    private String getSAXHandlerInterface() {
        if (sax == 1) {
            return SAX_DOCUMENT_HANDLER;
        } else if (sax == 2) {
            return SAX2_CONTENT_HANDLER;
        } else {
            return null;
        }
    }

    /**
     * Return a name of attributes class in current sax version.
     */
    private String  getSAXAttributes() {
        if (sax == 1) {
            return SAX_ATTRIBUTE_LIST;
        } else if (sax == 2) {
            return SAX2_ATTRIBUTES;
        } else {
            return null;
        }
    }




//
    /**
     * A factory of ClassElement producers used in code generation code.
     * @see generateCode
     */
    private interface CodeGenerator {

        public void generate(FileObject target) throws IOException;

    }

    private class StubGenerator implements CodeGenerator {
        private final String name;
        private final String face;
        private final String let;

        StubGenerator(String name, String face, String let) {
            this.name = name;
            this.face = face;
            this.let = let;
        }

        public void generate(FileObject target)  throws IOException {
            generateStub(target, name, face, let);
        }
    }

    private class InterfaceGenerator implements CodeGenerator {

        private final String name;

        InterfaceGenerator(String name) {
            this.name = name;
        }

        public void generate(FileObject target) throws IOException{
           generateInterface(target);
        }
    }

    private class InterfaceImplGenerator implements CodeGenerator {

        private final String name;

        InterfaceImplGenerator(String name) {
            this.name = name;
        }

        public void generate(FileObject target)  throws IOException{
            generateInterfaceImpl(target);
        }
    }

    private class ParsletGenerator implements CodeGenerator {

        private final String name;

        ParsletGenerator(String name) {
            this.name = name;
        }

        public void generate(FileObject target) throws IOException {
            generateParslet(target);
        }
    }

    private class ParsletImplGenerator implements CodeGenerator {

        private final String name;

        ParsletImplGenerator(String name) {
            this.name = name;
        }

        public void generate(FileObject target) throws IOException {
            generateParsletImpl(target);
        }
    }

}
