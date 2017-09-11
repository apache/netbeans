/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
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
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Selector that accepts modules based on their code base name.
 *
 * @author Jaroslav Tulach
 */
public final class ModuleSelector extends BaseExtendSelector {
    private Set<String> excludeModules;
    private Set<String> includeClusters;
    private Set<String> excludeClusters;
    private Map<String,String> fileToOwningModule;
    private boolean acceptExcluded;
    
    /** Creates a new instance of ModuleSelector */
    public ModuleSelector() {
    }

    public boolean isSelected(File dir, String filename, File file) throws BuildException {
        validate();
     
        Boolean check = checkSelected(dir, file);
        if (check == null) {
            return false;
        }
        
        if (acceptExcluded) {
            log("Reverting the accepted state", Project.MSG_VERBOSE);
            return !check;
        } else {
            return check;
        }
    }
    
    private Boolean checkSelected(File dir, File file) throws BuildException {
        if (file.isDirectory()) {
            log("Skipping directory: " + file, Project.MSG_VERBOSE);
            return null;
        }
        
        String module = null;
        if (file.getName().endsWith(".jar")) {
            try {
                JarFile jar = new JarFile(file);
                Manifest m = jar.getManifest();
                if (m != null) {
                    module = m.getMainAttributes().getValue("OpenIDE-Module"); // NOI18N
                    if (module == null && !isExt(file)) {
                        module = m.getMainAttributes().getValue("Bundle-SymbolicName"); // NOI18N
                        int semicolon = module == null ? -1 : module.indexOf(';');
                        if (semicolon >= 0) {
                            module = module.substring(0, semicolon);
                        }
                    }
                }
                jar.close();
            } catch (IOException ex) {
                throw new BuildException("Problem with " + file + ": " + ex, ex, getLocation());
            }
        }

        String name = file.getName();
        File p = file.getParentFile();
        for(;;) {

            if (new File(p, "update_tracking").isDirectory()) { // else includeClusters does not work
                String cluster = p.getName();
                
                if (!includeClusters.isEmpty() && !clusterMatch(includeClusters, cluster)) {
                    log("Not included cluster: " + cluster + " for " + file, Project.MSG_VERBOSE);
                    return null;
                }

                if (includeClusters.isEmpty() && excludeClusters.contains(cluster)) {
                    log("Excluded cluster: " + cluster + " for " + file, Project.MSG_VERBOSE);
                    return null;
                }
            }
            
            if (module == null && fileToOwningModule != null) {
                module = fileToOwningModule.get(name);
            }
            
            if (dir.equals(p)) {
                break;
            }
            name = p.getName() + '/' + name;
            p = p.getParentFile();
        }
        
        if (module == null) {
            log("No module in: " + file, Project.MSG_VERBOSE);
            return null;
        }
        int slash = module.indexOf('/');
        if (slash >= 0) {
            module = module.substring(0, slash);
        }
        
        if (excludeModules.contains(module)) {
            log("Excluded module: " + file, Project.MSG_VERBOSE);
            return false;
        }

        log("Accepted file: " + file, Project.MSG_VERBOSE);
        return true;
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
        if (includeClusters != null) {
            return;
        }
        
        includeClusters = new HashSet<String>();
        excludeClusters = new HashSet<String>();
        excludeModules = new HashSet<String>();
        
        Parameter[] arr = getParameters();
        if (arr == null) {
            return;
        }
        
        for (Parameter p : arr) {
            if ("excludeModules".equals(p.getName())) {
                parse(p.getValue(), excludeModules);
                log("Will excludeModules: " + excludeModules, Project.MSG_VERBOSE);
                continue;
            }
            if ("includeClusters".equals(p.getName())) {
                parse(p.getValue(), includeClusters);
                log("Will includeClusters: " + includeClusters, Project.MSG_VERBOSE);
                continue;
            }
            if ("excludeClusters".equals(p.getName())) {
                parse(p.getValue(), excludeClusters);
                log("Will excludeClusters: " + excludeClusters, Project.MSG_VERBOSE);
                continue;
            }
            if ("excluded".equals(p.getName())) {
                acceptExcluded = Boolean.parseBoolean(p.getValue());
                log("Will acceptExcluded: " + acceptExcluded, Project.MSG_VERBOSE);
                continue;
            }
            if ("updateTrackingFiles".equals(p.getName())) {
                fileToOwningModule = new HashMap<String,String>();
                try {
                    readUpdateTracking(getProject(), p.getValue(), fileToOwningModule);
                } catch (IOException ex) {
                    throw new BuildException(ex);
                } catch (ParserConfigurationException ex) {
                    throw new BuildException(ex);
                } catch (SAXException ex) {
                    throw new BuildException(ex);
                }
                log("Will accept these files: " + fileToOwningModule.keySet(), Project.MSG_VERBOSE);
                continue;
            }
            setError("Unknown parameter: " + p.getName());
        }
    }
    
    private static void parse(String tokens, Set<String> to) {
        StringTokenizer tok = new StringTokenizer(tokens, ", \n");
        
        while(tok.hasMoreElements()) {
            to.add(tok.nextToken());
        }
    }

    static void readUpdateTracking(final Project p, String tokens, final Map<String,String> files) throws SAXException, IOException, ParserConfigurationException {
        StringTokenizer tok = new StringTokenizer(tokens, File.pathSeparator);
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        final SAXParser parser = factory.newSAXParser();

        class MyHandler extends DefaultHandler {
            public File where;
            public String module;
            
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if (qName.equals("file")) {
                    String file = attributes.getValue("name");
                    if (file == null) {
                        throw new BuildException("<file/> without name attribute in " + where);
                    }
                    
                    files.put(file.replace(File.separatorChar, '/'), module);
                }
            }
            
            public void iterate(StringTokenizer tok) throws SAXException, IOException {
                while(tok.hasMoreElements()) {
                    where = new File(tok.nextToken());
                    
                    module = where.getName();
                    if (module.endsWith(".xml")) {
                        module = module.substring(0, module.length() - 4);
                    }
                    module = module.replace('-', '.');

                    try {
                        if (p != null) {
                            p.log("Parsing " + where, Project.MSG_VERBOSE);
                        }
                        parser.parse(where, this);
                    } catch (SAXException ex) {
                        throw new BuildException("Wrong file " + where, ex);
                    }
                    
                    // the update tracking file belongs to the moduel as well
                    files.put(where.getParentFile().getName() + '/' + where.getName(), module);
                }
            }
        }
        MyHandler handler = new MyHandler();
        handler.iterate (tok);
        
        
    }

    private static boolean isExt(final File archive) {
        final String path = archive.getPath();
        return
            path.contains("modules" + File.separator + "ext") ||    //NOI18N
            path.contains("lib" + File.separator + "ext");          //NOI18N
    }
}
