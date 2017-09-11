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

// See #13931.

package org.netbeans.nbbuild;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.help.HelpSet;
import javax.help.IndexItem;
import javax.help.IndexView;
import javax.help.NavigatorView;
import javax.help.TOCItem;
import javax.help.TOCView;
import javax.help.TreeItem;
import javax.help.TreeItemFactory;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Mapper;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Task to check various aspects of JavaHelp helpsets.
 * <ol>
 * <li>General parsability as far as JavaHelp is concerned.
 * <li>Map IDs are not duplicated.
 * <li>Map IDs point to real HTML files (and anchors where specified).
 * <li>TOC/Index navigators refer to real map IDs.
 * <li>HTML links in reachable HTML files point to valid places (including anchors).
 * </ol>
 * @author Jesse Glick
 */
public class CheckHelpSets extends Task {
    
    private List<FileSet> filesets = new ArrayList<FileSet>();
    
    /** Add a fileset with one or more helpsets in it.
     * <strong>Only</strong> the <samp>*.hs</samp> should match!
     * All other files will be found from it.
     */
    public void addFileset(FileSet fs) {
        filesets.add(fs);
    }
    
    public void execute() throws BuildException {
        Iterator it = filesets.iterator();
        while (it.hasNext()) {
            FileSet fs = (FileSet)it.next();
            FileScanner scanner = fs.getDirectoryScanner(getProject());
            File dir = scanner.getBasedir();
            String[] files = scanner.getIncludedFiles();
            for (int i = 0; i < files.length; i++) {
                File helpset = new File(dir, files[i]);
                try {
                    checkHelpSet(helpset);
                } catch (BuildException be) {
                    throw be;
                } catch (Exception e) {
                    throw new BuildException("Error checking helpset", e, new Location(helpset.getAbsolutePath()));
                }
            }
        }
    }
    
    private void checkHelpSet(File hsfile) throws Exception {
        log("Checking helpset: " + hsfile);
        HelpSet hs = new HelpSet(null, hsfile.toURI().toURL());
        javax.help.Map map = hs.getCombinedMap();
        log("Parsed helpset, checking map IDs in TOC/Index navigators...");
        NavigatorView[] navs = hs.getNavigatorViews();
        for (int i = 0; i < navs.length; i++) {
            String name = navs[i].getName();
            File navfile = new File(hsfile.getParentFile(), (String)navs[i].getParameters().get("data"));
            if (! navfile.exists()) throw new BuildException("Navigator " + name + " not found", new Location(navfile.getAbsolutePath()));
            if (navs[i] instanceof IndexView) {
                log("Checking index navigator " + name, Project.MSG_VERBOSE);
                IndexView.parse(navfile.toURI().toURL(), hs, Locale.getDefault(), new VerifyTIFactory(hs, map, navfile, false));
            } else if (navs[i] instanceof TOCView) {
                log("Checking TOC navigator " + name, Project.MSG_VERBOSE);
                TOCView.parse(navfile.toURI().toURL(), hs, Locale.getDefault(), new VerifyTIFactory(hs, map, navfile, true));
            } else {
                log("Skipping non-TOC/Index view: " + name, Project.MSG_VERBOSE);
            }
        }
        log("Checking for duplicate map IDs...");
        HelpSet.parse(hsfile.toURI().toURL(), null, new VerifyHSFactory());
        log("Checking links from help map and between HTML files...");
        Enumeration e = map.getAllIDs();
        Set<URI> okurls = new HashSet<URI>(1000);
        Set<URI> badurls = new HashSet<URI>(1000);
        Set<URI> cleanurls = new HashSet<URI>(1000);
        while (e.hasMoreElements()) {
            javax.help.Map.ID id = (javax.help.Map.ID)e.nextElement();
            URL u = map.getURLFromID(id);
            if (u == null) {
                throw new BuildException("Bogus map ID: " + id.id, new Location(hsfile.getAbsolutePath()));
            }
            log("Checking ID " + id.id, Project.MSG_VERBOSE);
            List<String> errors = new ArrayList<String>();
            CheckLinks.scan(this, null, null, id.id, "", 
            u.toURI(), okurls, badurls, cleanurls, false, false, false, 2, 
            Collections.<Mapper>emptyList(), errors);
            for (String error : errors) {
                log(error, Project.MSG_WARN);
            }
        }
    }
    
    private final class VerifyTIFactory implements TreeItemFactory {
        
        private final HelpSet hs;
        private final javax.help.Map map;
        private final File navfile;
        private final boolean toc;
        public VerifyTIFactory(HelpSet hs, javax.help.Map map, File navfile, boolean toc) {
            this.hs = hs;
            this.map = map;
            this.navfile = navfile;
            this.toc = toc;
        }
        
        // The useful method:
        
        public TreeItem createItem(String str, Hashtable hashtable, HelpSet helpSet, Locale locale) {
            String target = (String)hashtable.get("target");
            if (target != null) {
                if (! map.isValidID(target, hs)) {
                    log(navfile + ": invalid map ID: " + target, Project.MSG_WARN);
                } else {
                    log("OK map ID: " + target, Project.MSG_VERBOSE);
                }
            }
            return createItem();
        }
        
        // Filler methods:
        
        public Enumeration listMessages() {
            return Collections.enumeration(Collections.<String>emptyList());
        }
        
        public void processPI(HelpSet helpSet, String str, String str2) {
        }
        
        public void reportMessage(String str, boolean param) {
            log(str, param ? Project.MSG_VERBOSE : Project.MSG_WARN);
        }
        
        public void processDOCTYPE(String str, String str1, String str2) {
        }
        
        public void parsingStarted(URL uRL) {
        }
        
        public DefaultMutableTreeNode parsingEnded(DefaultMutableTreeNode defaultMutableTreeNode) {
            return defaultMutableTreeNode;
        }
        
        public TreeItem createItem() {
            if (toc) {
                return new TOCItem();
            } else {
                return new IndexItem();
            }
        }
        
    }
    
    private final class VerifyHSFactory extends HelpSet.DefaultHelpSetFactory {
        
        private Set<String> ids = new HashSet<String>(1000);
        
        public void processMapRef(HelpSet hs, Hashtable attrs) {
            try {
                URL map = new URL(hs.getHelpSetURL(), (String)attrs.get("location"));
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setValidating(false);
                factory.setNamespaceAware(false);
                SAXParser parser = factory.newSAXParser();
                parser.parse(new InputSource(map.toExternalForm()), new Handler(map.getFile()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        private final class Handler extends DefaultHandler {
            
            private final String map;
            public Handler(String map) {
                this.map = map;
            }
            
            public void startElement(String uri, String lname, String name, Attributes attributes) throws SAXException {
                if (name.equals("mapID")) {
                    String target = attributes.getValue("target");
                    if (target != null) {
                        if (ids.add(target)) {
                            log("Found map ID: " + target, Project.MSG_DEBUG);
                        } else {
                            log(map + ": duplicated ID: " + target, Project.MSG_WARN);
                        }
                    }
                }
            }
            
            public InputSource resolveEntity(String pub, String sys) throws SAXException {
                if (pub.equals("-//Sun Microsystems Inc.//DTD JavaHelp Map Version 1.0//EN") ||
                        pub.equals("-//Sun Microsystems Inc.//DTD JavaHelp Map Version 2.0//EN")) {
                    // Ignore.
                    return new InputSource(new ByteArrayInputStream(new byte[0]));
                } else {
                    return null;
                }
            }
            
        }
        
    }
    
}
