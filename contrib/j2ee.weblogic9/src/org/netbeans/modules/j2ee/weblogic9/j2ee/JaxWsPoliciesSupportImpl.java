/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.j2ee.weblogic9.j2ee;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.javaee.specs.support.spi.JaxWsPoliciesSupportImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Denis Anisimov
 */
class JaxWsPoliciesSupportImpl implements JaxWsPoliciesSupportImplementation {
    private static final String ORACLE = "oracle";                                  // NOI18N

    private static final String ORACLE_COMMON_MODULES = "oracle_common/modules/"; // NOI18N
    private static final String ORACLE_WEBSERVICES = "oracle.webservices"; // NOI18N
    private static final String ORACLE_WEBSERVICES_STANDALONE_CLIENT = ORACLE_WEBSERVICES + ".standalone.client"; // NOI18N
    private static final String JAR = "jar"; // NOI18N

    JaxWsPoliciesSupportImpl(WLJ2eePlatformFactory.J2eePlatformImplImpl platformImpl) {
        this.platformImpl = platformImpl;
    }

    public String getId() {
        return ORACLE;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.javaee.specs.support.spi.JaxWsPoliciesSupportImplementation#getClientPolicyIds()
     */
    @Override
    public List<String> getClientPolicyIds() {
        // TODO : filter ids ( keep only client policies )
        return getAllPolicyIds( null );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.javaee.specs.support.spi.JaxWsPoliciesSupportImplementation#getServicePolicyIds()
     */
    @Override
    public List<String> getServicePolicyIds() {
        // TODO : filter ids ( keep only services policies )
        return getAllPolicyIds( null );
    }

    @Override
    public boolean supports(FileObject wsdl, Lookup loookup) {
        DefaultHandler handler = loookup.lookup(DefaultHandler.class);
        if (handler instanceof OraclePolicyHandler) {
            return ((OraclePolicyHandler) handler).hasOraclePolicy;
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.javaee.specs.support.spi.JaxWsPoliciesSupportImplementation#getPolicyDescriptions()
     */
    @Override
    public Map<String, String> getPolicyDescriptions() {
        Map<String,String> map = new HashMap<String, String>();
        getAllPolicyIds( map );
        return map;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.javaee.specs.support.spi.JaxWsPoliciesSupportImplementation#extendsProjectClasspath(org.netbeans.modules.javaee.specs.support.spi.Project, java.util.Collection)
     */
    @Override
    public void extendsProjectClasspath(Project project, Collection<String> fqns) {
        /*
         *  TODO : the current implementation cares ONLY about limited
         *  list of FQNs. The should be changed if <code>fqns</code>
         *  has more items than expected.
         *  Hardcoding of the selected FQNs is less expensive because only
         *  limited ( two ) jar files are required to check.
         *  Generic algorithm could be created but it will be a performance
         *  problem : one need to scan all jars in each subfolder.
         *
         */
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sgs == null || sgs.length < 1) {
            return;
        }
        FileObject sourceRoot = sgs[0].getRootFolder();
        List<FileObject> roots = getJarRoots(sgs);
        Map<FileObject, URL> archive2Url = new HashMap<FileObject, URL>();
        List<String> foundFqns = new LinkedList<String>(fqns);
        for (FileObject root : roots) {
            if (foundFqns.isEmpty()) {
                break;
            }
            for (Iterator<String> iterator = foundFqns.iterator(); iterator.hasNext();) {
                if (hasClassFile(root, iterator.next())) {
                    iterator.remove();
                }
                if (!archive2Url.containsKey(root)) {
                    try {
                        archive2Url.put(root, root.getURL());
                    } catch (FileStateInvalidException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.INFO, "Couldn't extends compile classpath with required jars " + "for WL policy support", ex); // NOI18N
                    }
                }
            }
        }
        List<URL> urls = new LinkedList<URL>(archive2Url.values());
        try {
            ProjectClassPathModifier.addRoots(urls.toArray(new URL[0]), sourceRoot, ClassPath.COMPILE);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Couldn't extends compile classpath with required jars " + "for WL policy support", ex); // NOI18N
        }
    }

    @Override
    public Lookup getLookup(FileObject wsdl) {
        DefaultHandler handler = new OraclePolicyHandler();
        return Lookups.fixed(handler);
    }

    protected List<FileObject> getJarRoots(SourceGroup[] sgs) {
        File home = platformImpl.getMiddlewareHome();
        FileObject middlewareHome = FileUtil.toFileObject(FileUtil.normalizeFile(home));
        FileObject modules = middlewareHome.getFileObject(ORACLE_COMMON_MODULES); //NOI18N
        if (modules == null) {
            return Collections.emptyList();
        }
        List<FileObject> roots = new LinkedList<FileObject>();
        for (FileObject child : modules.getChildren()) {
            String name = child.getName();
            if (name.startsWith(ORACLE_WEBSERVICES)) {
                FileObject jar = child.getFileObject(ORACLE_WEBSERVICES_STANDALONE_CLIENT, JAR);
                if (jar != null) {
                    addJar(roots, jar);
                }
                jar = child.getFileObject("wsclient-rt", JAR);
                if (jar != null) {
                    addJar(roots, jar);
                }
            } else if (name.startsWith("ws.api_") && child.getExt().equals(JAR)) {
                // NOI18N
                addJar(roots, child);
            }
        }
        return roots;
    }

    protected void addJar(List<FileObject> archiveRoots, FileObject jar) {
        if (FileUtil.isArchiveFile(jar)) {
            archiveRoots.add(FileUtil.getArchiveRoot(jar));
        }
    }

    private List<String> getAllPolicyIds(Map<String,String> descriptions) {
        File home = platformImpl.getMiddlewareHome();
        FileObject middlewareHome = FileUtil.toFileObject(FileUtil.normalizeFile(home));
        FileObject modules = middlewareHome.getFileObject(ORACLE_COMMON_MODULES); //NOI18N
        if (modules == null) {
            return Collections.emptyList();
        }
        FileObject policiesFolder = null;
        for (FileObject folder : modules.getChildren()) {
            if (folder.getName().startsWith("oracle.wsm.policies")) {
                // NOI18N
                policiesFolder = folder;
                break;
            }
        }
        if (policiesFolder == null) {
            return Collections.emptyList();
        }
        FileObject[] jars = policiesFolder.getChildren();
        FileObject policies = null;
        for (FileObject jar : jars) {
            FileObject archiveRoot = FileUtil.getArchiveRoot(jar);
            policies = archiveRoot.getFileObject("META-INF/policies/oracle/"); //      NOI18N
            if (policies != null) {
                break;
            }
        }
        List<String> allIds = new LinkedList<String>();
        if (policies != null) {
            for (FileObject fileObject : policies.getChildren()) {
                String name = fileObject.getName();
                allIds.add(name);
                if ( descriptions!= null ){ 
                    descriptions.put( name , readFile(fileObject) );
                }
            }
        }
        return allIds;
    }

    private boolean hasClassFile(FileObject root, String fqn) {
        String fileName = fqn.replace('.', '/');
        return root.getFileObject(fileName + ".class") != null; // NOi18N
    }
    
    private String readFile( FileObject fileObject ){
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = null;
            try {
                InputStream stream = fileObject.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                String line;
                while( (line = reader.readLine()) != null ){
                    builder.append( line );
                    builder.append( System.getProperty("line.separator"));  // NOI18N
                } 
            }
            catch( IOException e ){
                Logger.getLogger(getClass().getName()).log(Level.INFO, 
                                    null, e);      
            }
            finally {
                if ( reader != null ){
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.INFO, 
                                    null, ex);   
                    }
                }
            }
            return builder.toString();
        }
    
    private WLJ2eePlatformFactory.J2eePlatformImplImpl platformImpl;

    private static final class OraclePolicyHandler extends DefaultHandler {

        private static final String POLICY = "Policy";                  // NOI18N

        private static final String COLON_POLICY = ":"+POLICY;          // NOI18N


        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, c)
         */
        @Override
        public void startElement( String uri, String localName, String qName,
                org.xml.sax.Attributes attributes ) throws SAXException
        {
            super.startElement(uri, localName, qName, attributes);
            boolean policy = false;
            if ( localName != null && localName.equals(POLICY)){
                policy = true;
            }
            if ( qName != null && qName.endsWith(COLON_POLICY) ) {
                policy = true;
            }
            if ( !policy ){
                return;
            }
            int count = attributes.getLength();
            for (int i=0; i<count ; i++) {
                String value = attributes.getValue(i);
                if ( value.toLowerCase(Locale.ENGLISH).contains(ORACLE)) {
                    hasOraclePolicy = true;
                }
            }
        }

        boolean hasOraclePolicy(){
            return hasOraclePolicy;
        }

        private boolean hasOraclePolicy;

    }
}
