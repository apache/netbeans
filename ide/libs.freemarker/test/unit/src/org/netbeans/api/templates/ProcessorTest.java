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
package org.netbeans.api.templates;

import freemarker.core.Environment;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.awt.Color;
import java.awt.Panel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import junit.framework.TestCase;
import org.netbeans.api.scripting.Scripting;
import org.netbeans.junit.MockServices;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Jaroslav Tulach
 */
public class ProcessorTest extends TestCase {
    FileObject root;
    
    public ProcessorTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        root = FileUtil.getConfigRoot();
        for (FileObject f : root.getChildren()) {
            f.delete();
        }
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testWorksWithoutFileObjects() throws UnsupportedEncodingException, ScriptException {
        ScriptEngineManager manager = Scripting.createManager();
        ScriptEngine eng = manager.getEngineByName("freemarker");
        assertNotNull("engine found", eng);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Writer w = new OutputStreamWriter(out);
        ByteArrayInputStream in = new ByteArrayInputStream("Hi!".getBytes(StandardCharsets.UTF_8));

        eng.getContext().setWriter(w);

        InputStreamReader r = new InputStreamReader(in, StandardCharsets.UTF_8);
        eng.eval(r);


        assertTrue("Content shall be unchanged", Arrays.equals("Hi!".getBytes(StandardCharsets.UTF_8), out.toByteArray()));

    }

    public void testApply() throws Exception {
        FileObject template = FileUtil.createData(root, "some.txt");
        OutputStream os = template.getOutputStream();
        String txt = "<html><h1>${title}</h1></html>";
        os.write(txt.getBytes());
        os.close();
        template.setAttribute("title", "Nazdar");
        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "<html><h1>Nazdar</h1></html>";
        assertEquals(exp, w.toString());
    }

    public void ignoreTestFailTwice() throws Exception {
        FileObject template = FileUtil.createData(root, "some.txt");
        OutputStream os = template.getOutputStream();
        String txt = "<html><h1>${unknown}</h1></html>";
        os.write(txt.getBytes());
        os.close();

        StringWriter w1 = new StringWriter();
        TestFailTemplateExceptionHandler tfeh1 = new TestFailTemplateExceptionHandler();
        apply(template, w1, tfeh1);

        StringWriter w2 = new StringWriter();
        TestFailTemplateExceptionHandler tfeh2 = new TestFailTemplateExceptionHandler();
        apply(template, w2, tfeh2);

        String expText = "Expression unknown is undefined on line 1, column 13 in some.txt.";
        String exp = "<html><h1>"+expText+"</h1></html>";
        assertEquals(exp, w1.toString());
        assertEquals(exp, w2.toString());

        assertNotNull("An expected exception was reported:", tfeh1.getExceptions());
        assertEquals("Just two exception reported:", 2, tfeh1.getExceptions().size());
        assertEquals("The expected exception was reported:", expText, tfeh1.getExceptions().get(0).getMessage());
        assertEquals("The expected exception was reported:", expText, tfeh1.getExceptions().get(1).getMessage());
    }

    public void testCanHandleComplexData() throws Exception {
        Panel p = new Panel();
        p.setForeground(Color.BLUE);
        
        FileObject template = FileUtil.createData(root, "some.txt");
        OutputStream os = template.getOutputStream();
        String txt = "<html><h1>${panel.foreground.red} ${panel.foreground.green} ${panel.foreground.blue}</h1></html>";
        os.write(txt.getBytes());
        os.close();
        template.setAttribute("panel", p);
        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "<html><h1>0 0 255</h1></html>";
        assertEquals(exp, w.toString());
    }
    public void testCanImportSubpkgOfParentPkg() throws Exception {
        FileObject imp = FileUtil.createData(root, "Templates/Licenses/gpl.txt");
        {
            OutputStream os = imp.getOutputStream();
            String txt = "GPL";
            os.write(txt.getBytes());
            os.close();
        }
        
        FileObject template = FileUtil.createData(root, "Templates/Others/some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "<html><h1><#include \"*/Licenses/gpl.txt\"></h1></html>";
            os.write(txt.getBytes());
            os.close();
        }        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "<html><h1>GPL</h1></html>";
        assertEquals(exp, w.toString());
    }
    public void testCanImportRelative() throws Exception {
        FileObject imp = FileUtil.createData(root, "Templates/Licenses/gpl.txt");
        {
            OutputStream os = imp.getOutputStream();
            String txt = "GPL";
            os.write(txt.getBytes());
            os.close();
        }
        
        FileObject template = FileUtil.createData(root, "Templates/Others/some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "<html><h1><#include \"../Licenses/gpl.txt\"></h1></html>";
            os.write(txt.getBytes());
            os.close();
        }        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "<html><h1>GPL</h1></html>";
        assertEquals(exp, w.toString());
    }
    public void testImportHonorsFEQ() throws Exception {
        doImportHonorsFEQ("cp1250");
    }
    public void testImportHonorsFEQIso() throws Exception {
        doImportHonorsFEQ("iso-8859-2");
    }
    
    private void doImportHonorsFEQ(String enc) throws Exception {
        MockServices.setServices(FEQI.class);
        
        //String kun = "Žluťoučký kůň";
        String kun = "\u017Dlu\u0165ou\u010Dky k\u016F\u0148";
        
        FileObject imp = FileUtil.createData(root, "Templates/Licenses/gpl.txt");
        {
            OutputStream os = imp.getOutputStream();
            os.write(kun.getBytes(enc));
            os.close();
            imp.setAttribute("enc", enc);
        }
        
        FileObject template = FileUtil.createData(root, "Templates/Others/some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "<html><h1><#include \"../Licenses/gpl.txt\"></h1></html>";
            os.write(txt.getBytes("utf-8"));
            os.close();
        }        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "<html><h1>" + kun + "</h1></html>";
        assertEquals(exp, w.toString());
    }

    public void ignoreTestMissingVariablesAreJustLogged() throws Exception {
        FileObject template = FileUtil.createData(root, "Templates/Others/some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "<html><h1>${title}</h1></html>";
            os.write(txt.getBytes());
            os.close();
        }        
        StringWriter w = new StringWriter();
        
        TestFailTemplateExceptionHandler tfeh = new TestFailTemplateExceptionHandler();        
        apply(template, w, tfeh);
        
        if (!w.toString().matches("<html><h1>.*</h1></html>")) {
            fail("should survive the missing variable:\n" + w.toString());
        }
        if (w.toString().indexOf("title") == -1) {
            fail("There should be a note about title variable:\n" + w);
        }
        String expText = "Expression title is undefined on line 1, column 13 in Templates/Others/some.txt.";
        assertNotNull("An expected exception was reported:", tfeh.getExceptions());
        assertEquals("Just one exception reported:", 1, tfeh.getExceptions().size());
        assertEquals("The expected exception was reported:", expText, tfeh.getExceptions().get(0).getMessage());
    }

    public void ignoreTestMissingImportsAreJustLogged() throws Exception {
        FileObject template = FileUtil.createData(root, "Templates/Others/some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "<html><h1><#include \"*/Licenses/gpl.txt\"></h1></html>";
            os.write(txt.getBytes());
            os.close();
        }        
        StringWriter w = new StringWriter();
        
        TestFailTemplateExceptionHandler tfeh = new TestFailTemplateExceptionHandler();        
        apply(template, w, tfeh);
        
        if (!w.toString().matches("<html><h1>.*</h1></html>")) {
            fail("should survive the missing variable:\n" + w.toString());
        }
        if (w.toString().indexOf("gpl.txt") == -1) {
            fail("There should be a note about gpl include:\n" + w);
        }
        String expText = "Error reading included file Templates/Others/*/Licenses/gpl.txt";
        assertNotNull("An expected exception was reported:", tfeh.getExceptions());
        assertEquals("Just one exception reported:", 1, tfeh.getExceptions().size());
        assertEquals("The expected exception was reported:", expText, tfeh.getExceptions().get(0).getMessage());
    }
    
    public void testCanHandleImport() throws Exception {
        Panel p = new Panel();
        p.setForeground(Color.BLUE);

        FileObject imp = FileUtil.createData(root, "import.txt");
        {
            OutputStream os = imp.getOutputStream();
            String txt = "${panel.foreground.blue}";
            os.write(txt.getBytes());
            os.close();
        }
        
        FileObject template = FileUtil.createData(root, "some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "<html><h1><#include \"import.txt\"></h1></html>";
            os.write(txt.getBytes());
            os.close();
            template.setAttribute("panel", p);
        }        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "<html><h1>255</h1></html>";
        assertEquals(exp, w.toString());
    }
    public void testImportCanInheritVariable() throws Exception {
        Panel p = new Panel();
        p.setForeground(Color.BLUE);

        FileObject imp = FileUtil.createData(root, "import.txt");
        {
            OutputStream os = imp.getOutputStream();
            String txt = "${prefix} First Line\n" +
                         "${prefix} Second Line\n";
            os.write(txt.getBytes());
            os.close();
        }
        
        FileObject template = FileUtil.createData(root, "some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "<#assign prefix = \"#\">" +
                         "<#include \"import.txt\">";
            os.write(txt.getBytes());
            os.close();
            template.setAttribute("panel", p);
        }        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "# First Line\n" +
                     "# Second Line\n";
        assertEquals(exp, w.toString());
    }
    public void testImportCanInheritVariableInSubFolder() throws Exception {
        Panel p = new Panel();
        p.setForeground(Color.BLUE);

        FileObject imp = FileUtil.createData(root, "sub/import.txt");
        {
            OutputStream os = imp.getOutputStream();
            String txt = "${prefix} First Line\n" +
                         "${prefix} Second Line\n";
            os.write(txt.getBytes());
            os.close();
        }
        
        FileObject template = FileUtil.createData(root, "sub/some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "<#assign prefix=\"#\">" +
                         "<#include \"import.txt\">";
            os.write(txt.getBytes());
            os.close();
            template.setAttribute("panel", p);
        }        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "# First Line\n" +
                     "# Second Line\n";
        assertEquals(exp, w.toString());
    }
    public void testAbilityToSendOwnTemplate() throws Exception {
        Map<String,Object> myValues = new HashMap<String, Object>();
        myValues.put("prefix", "#");

        FileObject template = FileUtil.createData(root, "some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "${prefix} First Line\n" +
                         "${prefix} Second Line\n";
            os.write(txt.getBytes());
            os.close();
            template.setAttribute("prefix", " * ");
        }        
        StringWriter w = new StringWriter();
        
        apply(template, w, myValues);
        
        String exp = "# First Line\n" +
                     "# Second Line\n";
        assertEquals(exp, w.toString());
    }
    public void testShowItIsPossibleToPassInBeansWrappedObject() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = FileUtil.createData(root, "simpleObject.txt");
        OutputStream os = fo.getOutputStream();
        String txt = "<#if (classInfo.getMethods().size() > 0) >The size is greater than 0.</#if>";
        os.write(txt.getBytes());
        os.close();       
        
        
        StringWriter w = new StringWriter();
                        
        Map<String,Object> parameters = Collections.<String,Object>singletonMap(
            "classInfo", BeansWrapper.getDefaultInstance().wrap(new ClassInfo())
        );
        apply(fo, w, parameters);
        assertEquals("The size is greater than 0.", w.toString());
    }
    public void testShowHowToGetSizeOfASequenceWithoutWrapper() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = FileUtil.createData(root, "simpleObject.txt");
        OutputStream os = fo.getOutputStream();
        String txt = "<#if (classInfo.getMethods()?size > 0) >The size is greater than 0.</#if>";
        os.write(txt.getBytes());
        os.close();       
        
        
        StringWriter w = new StringWriter();
                        
        Map<String,Object> parameters = Collections.<String,Object>singletonMap(
            "classInfo", new ClassInfo()
        );
        apply(fo, w, parameters);
        assertEquals("The size is greater than 0.", w.toString());
    }
    public void testMissingClassInfoSimple() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = FileUtil.createData(root, "simpleObject.txt");
        OutputStream os = fo.getOutputStream();
       // String txt = "<#if (classInfo.getMethods().size() > 0) >The size is greater than 0.</#if>";
        String txt = "<#if (classInfo.getMethodsCount() > 0) >The size is greater than 0.</#if>";
        os.write(txt.getBytes());
        os.close();       
        
        
        StringWriter w = new StringWriter();
                        
        Map<String,ClassInfo> parameters = Collections.singletonMap("classInfo", new ClassInfo());
        apply(fo, w, parameters);
        assertEquals("The size is greater than 0.", w.toString());
    }

    public void testChangeOfTemplate() throws Exception {
        FileObject template = FileUtil.createData(root, "some.txt");
        OutputStream os = template.getOutputStream();
        String txt = "<html><h1>${title}</h1></html>";
        os.write(txt.getBytes());
        os.close();
        template.setAttribute("title", "Nazdar");
        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "<html><h1>Nazdar</h1></html>";
        assertEquals(exp, w.toString());
        
        os = template.getOutputStream();
        txt = "<html><h2>${title}</h2></html>";
        os.write(txt.getBytes());
        os.close();
      
        w = new StringWriter();
        apply(template, w);
        
        exp = "<html><h2>Nazdar</h2></html>";
        assertEquals("Second run", exp, w.toString());
    }

    public void testChangeOfParams() throws Exception {
        FileObject template = FileUtil.createData(root, "some.txt");
        OutputStream os = template.getOutputStream();
        String txt = "<${html}><h1>${title}</h1></${html}>";
        os.write(txt.getBytes());
        os.close();
        template.setAttribute("title", "Nazdar");
        
        StringWriter w = new StringWriter();
        
        Map<String,Object> param = new HashMap<String, Object>();
        param.put("html", "html");
        apply(template, w, param);
        
        String exp = "<html><h1>Nazdar</h1></html>";
        assertEquals(exp, w.toString());
        
        param.put("html", "xml");
        w = new StringWriter();
        apply(template, w, param);
        
        exp = "<xml><h1>Nazdar</h1></xml>";
        assertEquals("Second run", exp, w.toString());
    }
    
    
    static void apply(FileObject template, Writer w) throws Exception {
        apply(template, w, (TemplateExceptionHandler) null);
    }
    
    static void apply(FileObject template, Writer w, TemplateExceptionHandler teh) throws Exception {
        apply(template, w, Collections.<String,Object>emptyMap(), teh);
    }
    
    static void apply(FileObject template, Writer w, Map<String,? extends Object> values) throws Exception {
        apply(template, w, values, null);
    }
    
    static void apply(FileObject template, Writer w, Map<String,? extends Object> values, TemplateExceptionHandler teh) throws Exception {
        ScriptEngineManager mgr = Scripting.createManager();
        ScriptEngine eng = mgr.getEngineByName("freemarker");
        assertNotNull("We do have such engine", eng);
        eng.getContext().setWriter(w);
        eng.getContext().setAttribute(FileObject.class.getName(), template, ScriptContext.ENGINE_SCOPE);
        eng.getContext().getBindings(ScriptContext.ENGINE_SCOPE).putAll(values);
        if (teh != null) {
            eng.getContext().setAttribute("org.netbeans.libs.freemarker.exceptionHandler", teh, ScriptContext.ENGINE_SCOPE);
        }
        eng.eval(new InputStreamReader(template.getInputStream()));
    }

    public static final class FEQI extends FileEncodingQueryImplementation {
        @Override
        public Charset getEncoding(FileObject file) {
            Object obj = file.getAttribute("enc");
            if (obj instanceof String) {
                return Charset.forName((String)obj);
            }
            return null;
        }
        
    }
    
    private static final class TestFailTemplateExceptionHandler implements TemplateExceptionHandler {
        
        private List<TemplateException> exceptions;

        @Override
        public void handleTemplateException(TemplateException te, Environment e, Writer writer) throws TemplateException {
            if (exceptions == null) {
                exceptions = new ArrayList<TemplateException>();
            }
            exceptions.add(te);
            try {
                writer.append(te.getLocalizedMessage());
            } catch (IOException ioex) {
                Exceptions.printStackTrace(ioex);
            }
        }
        
        public List<TemplateException> getExceptions() {
            return exceptions;
        }
        
    }
}
