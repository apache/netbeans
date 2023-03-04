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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.selectors.BaseExtendSelector;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Selector that accepts modules based on their state.
 *
 * @author Jaroslav Tulach
 */
public final class ModuleStateSelector extends BaseExtendSelector {
    private boolean acceptAutoload;
    private boolean acceptEager;
    private boolean acceptEnabled;
    private boolean acceptDisabled;
    private HashMap<String, String> fileToOwningModule;
    
    /** Creates a new instance of ModuleSelector */
    public ModuleStateSelector() {
    }

    public boolean isSelected(File dir, String filename, File file) throws BuildException {
        validate();
     
        Boolean check = checkSelected(dir, file);
        if (check == null) {
            return false;
        }
        return check;
    }
    
    private Boolean checkSelected(File dir, File file) throws BuildException {
        if (file.isDirectory()) {
            log("Skipping directory: " + file, Project.MSG_VERBOSE);
            return null;
        }
        
        String module = null;
        if (file.getName().endsWith(".jar")) {
            module = file.getName().substring(0, file.getName().length() - 4).replace('-', '.');
            int slash = module.indexOf('/');
            if (slash >= 0) {
                module = module.substring(0, slash);
            }
        }
        try {
            return readConfig(getProject(), module, file);
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            throw new BuildException(ex);
        }
    }
    // Copied from apisupport.project.ui.customizer.SingleModuleProperties:
    static boolean clusterMatch(Collection<String> enabledClusters, String clusterName) { // #73706
        String baseName = clusterBaseName(clusterName);
        for (String c : enabledClusters) {
            if (clusterBaseName(c).equals(baseName)) {
                return true;
            }
        }
        return false;
    }
    static String clusterBaseName(String clusterName) {
        return clusterName.replaceFirst("[0-9.]+$", ""); // NOI18N
    }

    @Override
    public void verifySettings() {
        Parameter[] arr = getParameters();
        if (arr == null) {
            return;
        }
        
        for (Parameter p : arr) {
            if ("acceptAutoload".equals(p.getName())) {
                acceptAutoload = Boolean.valueOf(p.getValue());
                continue;
            }
            if ("acceptEager".equals(p.getName())) {
                acceptEager = Boolean.valueOf(p.getValue());
                continue;
            }
            if ("acceptEnabled".equals(p.getName())) {
                acceptEnabled = Boolean.valueOf(p.getValue());
                continue;
            }
            if ("acceptDisabled".equals(p.getName())) {
                acceptDisabled = Boolean.valueOf(p.getValue());
                continue;
            }
            setError("Unknown parameter: " + p.getName());
        }
    }
    
    private boolean readConfig(final Project p, String cnb, final File jar)
    throws SAXException, IOException, ParserConfigurationException {
        File info = new File(new File(
            new File(jar.getParentFile().getParentFile(), "config"), "Modules"),
            cnb.replace('.', '-') + ".xml"
        );

        if (!info.exists()) {
            throw new BuildException("Cannot find " + info + " for " + jar);
        }

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        final SAXParser parser = factory.newSAXParser();

        class MyHandler extends DefaultHandler implements EntityResolver {
            Boolean eager;
            Boolean autoload;
            Boolean enabled;
            Boolean lastResult;
            String  lastName;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                lastResult = null;
                lastName = attributes.getValue("name");
            }



            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                String t = new String(ch, start, length).trim();
                if ("true".equals(t)) {
                    lastResult = Boolean.TRUE;
                }
                if ("false".equals(t)) {
                    lastResult = Boolean.FALSE;
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                if (lastName == null) {
                    return;
                }

                if (lastName.equals("eager")) {
                    eager = lastResult;
                }
                if (lastName.equals("autoload")) {
                    autoload = lastResult;
                }
                if (lastName.equals("enabled")) {
                    enabled = lastResult;
                }
                lastName = null;
            }

            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
                return new InputSource(new ByteArrayInputStream(new byte[0]));
            }
        }
        MyHandler handler = new MyHandler();
        parser.parse(info, handler);
        
        if (acceptAutoload && Boolean.TRUE.equals(handler.autoload)) {
            return true;
        }
        if (acceptEager && Boolean.TRUE.equals(handler.eager)) {
            return true;
        }
        if (acceptEnabled && Boolean.TRUE.equals(handler.enabled)) {
            return true;
        }
        if (acceptDisabled && Boolean.FALSE.equals(handler.enabled)) {
            return true;
        }
        return false;
    }
}
