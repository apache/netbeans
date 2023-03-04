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
package org.netbeans.nbbuild;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Task to process Arch questions and answers document.
 * @author Jaroslav Tulach, Jesse Glick
 */
public class Arch extends Task implements ErrorHandler, EntityResolver, URIResolver {

    /** map from String ids -> Elements */
    private Map<String,Element> answers;
    private Map<String,Element> questions;
    /** exception during parsing */
    private SAXParseException parseException;
    
    public Arch() {
    }

    //
    // itself
    //

    private File questionsFile;
    /** Questions and answers file */
    public void setAnswers (File f) {
        questionsFile = f;
    }
    
    
    private File output;
    /** Output file
     */
    public void setOutput (File f) {
        output = f;
    }
    
    // For use when generating API documentation:
    private String stylesheet = null;
    public void setStylesheet(String s) {
        stylesheet = s;
    }
    private String overviewlink = null;
    public void setOverviewlink(String s) {
        overviewlink = s;
    }
    private String footer = null;
    public void setFooter(String s) {
        footer = s;
    }
    private File xsl = null;
    public void setXSL (File xsl) {
        this.xsl = xsl;
    }
    
    private File apichanges = null;
    public void setApichanges (File apichanges) {
        this.apichanges = apichanges;
    }

    private File project = null;
    public void setProject (File x) {
        this.project = x;
    }
    
    /** Run the conversion */
    public void execute () throws BuildException {        
        if ( questionsFile == null ) {
            throw new BuildException ("questions file must be provided");
        }
        
        if ( output == null ) {
            throw new BuildException ("output file must be specified");
        }
        
        boolean generateTemplate = !questionsFile.exists();
        
        if (
            !generateTemplate && 
            output.exists() && 
            questionsFile.lastModified() <= output.lastModified() &&
            this.getProject().getProperty ("arch.generate") == null
        ) {
            // nothing needs to be generated. everything is up to date
            return;
        }
        
        
        Document q;
        Source qSource;
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        try {
            factory = DocumentBuilderFactory.newInstance ();
            factory.setValidating(!generateTemplate && !"true".equals(this.getProject().getProperty ("arch.private.disable.validation.for.test.purposes"))); // NOI18N
            
            builder = factory.newDocumentBuilder();
            builder.setErrorHandler(this);
            builder.setEntityResolver(this);

            if (generateTemplate) {
                try (InputStream resource = Arch.class.getResourceAsStream("Arch-api-questions.xml")) {
                    q = builder.parse(resource);
                    qSource = new DOMSource (q);
                }
            } else {
                q = builder.parse (questionsFile);
				qSource = new DOMSource (q);
            }
            
            if (parseException != null) {
                throw parseException;
            }
            
        } catch (SAXParseException ex) {
            log(ex.getSystemId() + ":" + ex.getLineNumber() + ": " + ex.getLocalizedMessage(), Project.MSG_ERR);
            throw new BuildException(questionsFile.getAbsolutePath() + " is malformed or invalid", ex, getLocation());
        } catch (Exception ex) {
            throw new BuildException ("File " + questionsFile + " cannot be parsed: " + ex.getLocalizedMessage(), ex, getLocation());
        }

        questions = readElements (q, "question");
        
        String questionsVersion;
        {
            NodeList apiQuestions = q.getElementsByTagName("api-questions");
            if (apiQuestions.getLength () != 1) {
                throw new BuildException ("No element api-questions");
            }
            questionsVersion = ((Element)apiQuestions.item (0)).getAttribute ("version");
            if (questionsVersion == null) {
                throw new BuildException ("Element api-questions does not have attribute version");
            }
        }
        
        if (questions.size () == 0) {
            throw new BuildException ("There are no <question> elements in the file!");
        }
        
        if (generateTemplate) {
            log ("Input file " + questionsFile + " does not exist. Generating it with skeleton answers.");
            try {
                SortedSet<String> s = new TreeSet<>(questions.keySet());
                generateTemplateFile(questionsVersion, s);
            } catch (IOException ex) {
                throw new BuildException (ex);
            }
            
            return;
        }
        
        answers = readElements (q, "answer");
        
        
        {
            //System.out.println("doc:\n" + q.getDocumentElement());
            
            // version of answers and version of questions
            NodeList apiAnswers = q.getElementsByTagName("api-answers");
            
            if (apiAnswers.getLength() != 1) {
                throw new BuildException ("No element api-answers");
            }
            
            String answersVersion = ((Element)apiAnswers.item (0)).getAttribute ("question-version");
            
            if (answersVersion == null) {
                throw new BuildException ("Element api-answers does not have attribute question-version");
            }
            
            if (!answersVersion.equals(questionsVersion)) {
                String msg = questionsFile.getAbsolutePath() + ": answers were created for questions version \"" + answersVersion + "\" but current version of questions is \"" + questionsVersion + "\"";
                if ("false".equals (this.getProject().getProperty("arch.warn"))) {
                    throw new BuildException (msg);
                } else {
                    log (msg, Project.MSG_WARN);
                }
            }
        }
        
        {
            // check all answers have their questions
            SortedSet<String> s = new TreeSet<>(questions.keySet());
            s.removeAll (answers.keySet ());
            if (!s.isEmpty()) {
                if ("true".equals (this.getProject().getProperty ("arch.generate"))) {
                    log ("Missing answers to questions: " + s);
                    log ("Generating the answers to end of file " + questionsFile);
                    try {
                        generateMissingQuestions(questionsVersion, s);
                    } catch (IOException ex) {
                        throw new BuildException (ex);
                    }
                    qSource = new StreamSource (questionsFile);
                    try {
                        q = builder.parse(questionsFile);
                    } catch (IOException | SAXException ex) {
                        throw new BuildException(ex);
                    }
                } else {
                    log (
                        questionsFile.getAbsolutePath() + ": some questions have not been answered: " + s + "\n" + 
                        "Run with -Darch.generate=true to add missing questions into the end of question file"
                    , Project.MSG_WARN);
                }
            }
        }

        
        if (apichanges != null) {
            // read also apichanges and add them to the document
            log("Reading apichanges from " + apichanges);

            Document api;
            try {
                api = builder.parse (apichanges);
            } catch (SAXParseException ex) {
                log(ex.getSystemId() + ":" + ex.getLineNumber() + ": " + ex.getLocalizedMessage(), Project.MSG_ERR);
                throw new BuildException(apichanges.getAbsolutePath() + " is malformed or invalid", ex, getLocation());
            } catch (Exception ex) {
                throw new BuildException ("File " + apichanges + " cannot be parsed: " + ex.getLocalizedMessage(), ex, getLocation());
            }
            
            NodeList node = api.getElementsByTagName("apichanges");
            if (node.getLength() != 1) {
                throw new BuildException("Expected one element <apichanges/> in " + apichanges + "but was: " + node.getLength());
            }

            Node n = node.item(0);
            Node el = q.getElementsByTagName("api-answers").item(0);
            
            el.appendChild(q.importNode(n, true));
            
            
            qSource = new DOMSource(q);
            qSource.setSystemId(questionsFile.toURI().toString());
        }

        
        if (project != null) {
            // read also project file and apply transformation on defaultanswer tags
            log("Reading project from " + project);

            
            
            Document prj;
            try {
                DocumentBuilderFactory fack = DocumentBuilderFactory.newInstance();
                fack.setNamespaceAware(false);
                prj = fack.newDocumentBuilder().parse (project);
            } catch (SAXParseException ex) {
                log(ex.getSystemId() + ":" + ex.getLineNumber() + ": " + ex.getLocalizedMessage(), Project.MSG_ERR);
                throw new BuildException(project.getAbsolutePath() + " is malformed or invalid", ex, getLocation());
            } catch (Exception ex) {
                throw new BuildException ("File " + project + " cannot be parsed: " + ex.getLocalizedMessage(), ex, getLocation());
            }
            
            // enhance the project document with info about stability and logical name of an API
            // use arch.code-name-base.name and arch.code-name-base.category
            // to modify regular:
            //    <dependency>
            //        <code-name-base>org.openide.util</code-name-base>
            //        <build-prerequisite/>
            //        <compile-dependency/>
            //        <run-dependency>
            //            <specification-version>6.2</specification-version>
            //        </run-dependency>
            //    </dependency>
            // to include additional items like:
            //    <dependency>
            //        <code-name-base>org.openide.util</code-name-base>
            //        <api-name>UtilitiesAPI</api-name>
            //        <api-category>official</api-category>
            //        <build-prerequisite/>
            //        <compile-dependency/>
            //        <run-dependency>
            //            <specification-version>6.2</specification-version>
            //        </run-dependency>
            //    </dependency>
            
            {
                NodeList deps = prj.getElementsByTagName("code-name-base");
                for (int i = 0; i < deps.getLength(); i++) {
                    Node name = deps.item(i);
                    String api = name.getChildNodes().item(0).getNodeValue();
                    String human = this.getProject().getProperty("arch." + api + ".name");
                    if (human != null) {
                        if (human.equals("")) {
                            throw new BuildException("Empty name for " + api + " from " + project);
                        }
                        
                        Element e = prj.createElement("api-name");
                        e.appendChild(prj.createTextNode(human));
                        name.getParentNode().insertBefore(e, name);
                    }
                    String category = this.getProject().getProperty("arch." + api + ".category");
                    if (category != null) {
                        if (category.equals("")) {
                            throw new BuildException("Empty category for " + api + " from " + project);
                        }
                        Element e = prj.createElement("api-category");
                        e.appendChild(prj.createTextNode(category));
                        name.getParentNode().insertBefore(e, name);
                    }
                    
                }
            }

            DOMSource prjSrc = new DOMSource(prj);
            
            NodeList node = prj.getElementsByTagName("project");
            if (node.getLength() != 1) {
                throw new BuildException("Expected one element <project/> in " + project + "but was: " + node.getLength());
            }

            NodeList list= q.getElementsByTagName("answer");
            for (int i = 0; i < list.getLength(); i++) {
                Node n = list.item(i);
                String id = n.getAttributes().getNamedItem("id").getNodeValue();
                URL u = Arch.class.getResource("Arch-default-" + id + ".xsl");
                if (u != null) {
                    log("Found default answer to " + id + " question", Project.MSG_VERBOSE);
                    Node defaultAnswer = findDefaultAnswer(n);
                    if (defaultAnswer != null && 
                        "none".equals(defaultAnswer.getAttributes().getNamedItem("generate").getNodeValue())
                    ) {
                        log("Skipping answer as there is <defaultanswer generate='none'", Project.MSG_VERBOSE);
                        // ok, this default answer is requested to be skipped
                        continue;
                    }
                    
                    DOMResult res = new DOMResult(q.createElement("p"));
                    try {
                        StreamSource defXSL = new StreamSource(u.openStream());
                    
                        TransformerFactory fack = TransformerFactory.newInstance();
                        Transformer t = fack.newTransformer(defXSL);
                        t.transform(prjSrc, res);
                    } catch (IOException | TransformerException ex) {
                        throw new BuildException (ex);
                    }
                    
                    if (defaultAnswer != null) {
                        log("Replacing default answer", Project.MSG_VERBOSE);
                        defaultAnswer.getParentNode().replaceChild(res.getNode(), defaultAnswer);
                    } else {
                        log("Adding default answer to the end of previous one", Project.MSG_VERBOSE);
                        Element para = q.createElement("p");
                        para.appendChild(q.createTextNode("The default answer to this question is:"));
                        para.appendChild(q.createComment("If you do not want default answer to be generated you can use <defaultanswer generate='none' /> here"));
                        para.appendChild(q.createElement("br"));
                        para.appendChild(res.getNode());
                        n.appendChild(para);
                    }
                }
            }
            
            
            qSource = new DOMSource(q);
            qSource.setSystemId(questionsFile.toURI().toString());
        }

        if (this.getProject().getProperty("javadoc.title") != null) {
            // verify we have the api-answers@module and possibly add it
            NodeList deps = q.getElementsByTagName("api-answers");
            if (deps.getLength() != 1) {
                throw new BuildException("Strange number of api-answers elements: " + deps.getLength());
            }
            
            Node module = deps.item(0).getAttributes().getNamedItem("module");
            if (module == null) {
                Attr attr = q.createAttribute("module");
                deps.item(0).getAttributes().setNamedItem(attr);
                attr.setValue(this.getProject().getProperty("javadoc.title"));
            }

            qSource = new DOMSource(q);
            qSource.setSystemId(questionsFile.toURI().toString());
        }            
        
        
        // apply the transform operation
        try {
            StreamSource ss;
            String file = this.xsl != null ? this.xsl.toString() : getProject().getProperty ("arch.xsl");
            
            if (file != null) {
                log ("Using " + file + " as the XSL stylesheet");
                ss = new StreamSource (file);
            } else {
                ss = new StreamSource (
                    getClass ().getResourceAsStream ("Arch.xsl")
                );
            }
            
            log("Transforming " + questionsFile + " into " + output);

            TransformerFactory trans;
            trans = TransformerFactory.newInstance();
            trans.setURIResolver(this);
            Transformer t = trans.newTransformer(ss);
            try (OutputStream os = new BufferedOutputStream (new FileOutputStream (output))) {
                StreamResult r = new StreamResult (os);
                if (stylesheet == null) {
                    stylesheet = this.getProject ().getProperty ("arch.stylesheet");
                }
                if (stylesheet != null) {
                    t.setParameter("arch.stylesheet", stylesheet);
                }
                if (overviewlink != null) {
                    t.setParameter("arch.overviewlink", overviewlink);
                }
                if (footer != null) {
                    t.setParameter("arch.footer", footer);
                }
                t.setParameter("arch.answers.date", DateFormat.getDateInstance().format(new Date(questionsFile.lastModified())));
                
                String archTarget = output.toString();
                int slash = archTarget.lastIndexOf(File.separatorChar);
                if (slash > 0) {
                    archTarget = archTarget.substring (slash + 1);
                }
                String archPref = getProject ().getProperty ("arch.target");
                if (archPref != null) {
                    archTarget = archPref + "/" + archTarget;
                }
                
                t.setParameter("arch.target", archTarget);
                String when = getProject().getProperty("arch.when");
                if (when != null) {
                    t.setParameter("arch.when", when);
                }
                t.transform(qSource, r);
            }
        } catch (IOException | TransformerException ex) {
            throw new BuildException (ex);
        }
    }
    
    private void generateMissingQuestions(String version, SortedSet<String> missing) throws IOException, BuildException {
        StringBuffer sb = new StringBuffer();
        InputStreamReader is = new InputStreamReader(new FileInputStream(questionsFile.toString()));
        char[] arr = new char[4096];
        for (;;) {
            int len = is.read(arr);
            if (len == -1) break;
            
            sb.append(arr, 0, len);
        }
        
        int indx = sb.indexOf("</api-answers>");
        if (indx == -1) {
            throw new BuildException("There is no </api-answers> in " + questionsFile);
        }
        
        sb.delete (indx, indx + "</api-answers>".length());

        Matcher m = Pattern.compile("question-version='([0-9\\.]*)'").matcher(sb);
        if (m.find()) {
            sb.delete(m.start(1), m.end(1));
            sb.insert(m.start(1), version);
        }
        
        try (Writer w = new OutputStreamWriter (new FileOutputStream (questionsFile.toString ()))) {
            w.write(sb.toString());
            writeQuestions (w, missing);
            w.write("</api-answers>\n");
        }
    }

    private void writeQuestions(Writer w, SortedSet<String> missing) throws IOException {
        for (String s : missing) {
            Element n = questions.get(s);
            
            //w.write("\n\n<!-- Question: " + s + "\n");
            w.write("\n\n<!--\n        ");
            w.write(elementToString(n));
            w.write("\n-->\n");
            
            URL u = Arch.class.getResource("Arch-default-" + s + ".xsl");
            if (u != null) {
                // there is default answer
                w.write(" <answer id=\"" + s + "\">\n  <defaultanswer generate='here' />\n </answer>\n\n");
            } else {
                w.write(" <answer id=\"" + s + "\">\n  <p>\n   XXX no answer for " + s + "\n  </p>\n </answer>\n\n");
            }
        }
    }
        
    
    private static String findNbRoot (File f) {
        StringBuffer result = new StringBuffer ();
        f = f.getParentFile();
        
        while (f != null) {
            File x = new File (f, 
                "nbbuild" + File.separatorChar + 
                "antsrc" + File.separatorChar + 
                "org" + File.separatorChar + 
                "netbeans" + File.separatorChar +
                "nbbuild" + File.separatorChar +
                "Arch.dtd"
            );
            if (x.exists ()) {
                return result.toString();
            }
            result.append("../"); // URI, so pathsep is /
            f = f.getParentFile();
        }
        return null;
    }
    
    private void generateTemplateFile(String versionOfQuestions, SortedSet<String> missing) throws IOException {
        String nbRoot = findNbRoot(questionsFile);
        if (nbRoot == null) {
            nbRoot = "http://hg.netbeans.org/main/raw-file/tip/";
        }
        
        try (Writer w = new FileWriter (questionsFile)) {
            w.write ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            w.write ("<!DOCTYPE api-answers PUBLIC \"-//NetBeans//DTD Arch Answers//EN\" \""); w.write (nbRoot); w.write ("nbbuild/antsrc/org/netbeans/nbbuild/Arch.dtd\" [\n");
            w.write ("  <!ENTITY api-questions SYSTEM \""); w.write (nbRoot); w.write ("nbbuild/antsrc/org/netbeans/nbbuild/Arch-api-questions.xml\">\n");
            w.write ("]>\n");
            w.write ("\n");
            w.write ("<api-answers\n");
            w.write ("  question-version=\""); w.write (versionOfQuestions); w.write ("\"\n");
            w.write ("  author=\"yourname@netbeans.org\"\n");
            w.write (">\n\n");
            w.write ("  &api-questions;\n");
            
            writeQuestions (w, missing);
            
            w.write ("</api-answers>\n");
        }
    }

    private static Map<String,Element> readElements (Document q, String name) {
        Map<String,Element> map = new HashMap<>();
       
        NodeList list = q.getElementsByTagName(name);
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item (i).getAttributes().getNamedItem("id");
            if (n == null) {
                throw new BuildException ("Question without id tag");
            }
            String id = n.getNodeValue();

            map.put(id, (Element) list.item(i));
        }
        
        return map;
    }

    public void error(SAXParseException exception) throws SAXException {
        if (parseException != null) {
            log(parseException.getSystemId() + ":" + parseException.getLineNumber() + ": " + parseException.getLocalizedMessage(), Project.MSG_ERR);
        }
        parseException = exception;
    }
    
    public void fatalError(SAXParseException exception) throws SAXException {
        throw exception;
    }
    
    public void warning(SAXParseException exception) throws SAXException {
        if (exception.getLocalizedMessage().startsWith("Using original entity definition for")) {
            // Pointless message, always logged when using XHTML. Ignore.
            return;
        }
        log(exception.getSystemId() + ":" + exception.getLineNumber() + ": " + exception.getLocalizedMessage(), Project.MSG_WARN);
    }
    
    private static String elementToString(Element e) throws IOException {
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "no"); // NOI18N
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); // NOI18N
            Source source = new DOMSource(e);
            StringWriter w = new StringWriter();
            Result result = new StreamResult(w);
            t.transform(source, result);
            return w.toString();
        } catch (Exception x) {
            throw (IOException)new IOException(x.toString()).initCause(x);
        }
    }

    private static Node findDefaultAnswer(Node n) {
        if (n.getNodeName().equals ("defaultanswer")) {
            return n;
        }
        
        NodeList arr = n.getChildNodes();
        for (int i = 0; i < arr.getLength(); i++) {
            Node found = findDefaultAnswer(arr.item(i));
            if (found != null) {
                return found;
            }
        }
        return null;
    }
    
    private static Set<String> localDTDs;
    static {
        localDTDs = new HashSet<>();
        localDTDs.add("Arch.dtd");
        localDTDs.add("Arch-api-questions.xml");
        localDTDs.add("xhtml1-strict.dtd");
        localDTDs.add("xhtml-lat1.ent");
        localDTDs.add("xhtml-special.ent");
        localDTDs.add("xhtml-symbol.ent");
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        log("publicId: " + publicId + " systemId: " + systemId, Project.MSG_VERBOSE);
        
        int idx = systemId.lastIndexOf('/');
        String last = systemId.substring(idx + 1);
        
        if (localDTDs.contains(last)) {
            InputSource is = new InputSource(Arch.class.getResourceAsStream(last));
            is.setSystemId(systemId);
            return is;
        } else {
            log("Not resolved");
            return null;
        }
    }

    public Source resolve(String href, String base) throws TransformerException {
        return null;
    }
}
