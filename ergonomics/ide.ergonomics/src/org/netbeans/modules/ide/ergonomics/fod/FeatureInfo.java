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

package org.netbeans.modules.ide.ergonomics.fod;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpression;
import org.w3c.dom.Document;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;

/** Description of <em>Feature On Demand</em> capabilities and a 
 * factory to create new instances.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>, Jirka Rechtacek <jrechtacek@netbeans.org>
 */
public final class FeatureInfo {
    private final URL delegateLayer;
    private final Set<String> cnbs;
    private final Map<String,String> nbproject = new HashMap<String,String>();
    private final Map<Object[],String> files = new HashMap<Object[],String>();
    private Properties properties;
    final String clusterName;
    private Boolean cacheEnabled;
    private Boolean cachePresent;

    private FeatureInfo(String clusterName, Set<String> cnbs, URL delegateLayer, Properties p) {
        this.cnbs = cnbs;
        this.delegateLayer = delegateLayer;
        this.properties = p;
        this.clusterName = clusterName;
    }
    
    public String getClusterName() {
        return clusterName;
    }

    public static FeatureInfo create(String clusterName, URL delegateLayer, URL bundle) throws IOException {
        Properties p = new Properties();
        p.load(bundle.openStream());
        String cnbs = p.getProperty("cnbs");
        assert cnbs != null : "Error loading from " + bundle; // NOI18N
        TreeSet<String> s = new TreeSet<String>();
        s.addAll(Arrays.asList(cnbs.split(",")));

        FeatureInfo info = new FeatureInfo(clusterName, s, delegateLayer, p);
        final String prefix = "nbproject.";
        final String prefFile = "project.file.";
        final String prefXPath = "project.xpath.";
        for (Object k : p.keySet()) {
            String key = (String) k;
            if (key.startsWith(prefix)) {
                info.nbproject(
                    key.substring(prefix.length()),
                    p.getProperty(key)
                );
            }
            if (key.startsWith(prefFile)) {
                try {
                    info.projectFile(key.substring(prefFile.length()), null, p.getProperty(key));
                } catch (XPathExpressionException ex) {
                    IOException e = new IOException(ex.getMessage());
                    e.initCause(ex);
                    throw e;
                }
            }
            if (key.startsWith(prefXPath)) {
                try {
                    String xpaths = p.getProperty(key);
                    for (String xp : safeXPathSplit(xpaths)) {
                        info.projectFile(key.substring(prefXPath.length()), xp, "");
                    }
                } catch (XPathExpressionException ex) {
                    IOException e = new IOException(ex.getMessage());
                    e.initCause(ex);
                    throw e;
                }
            }
        }
        return info;
    }
    

    public Object getProjectImporter() {
        return properties.getProperty("projectImporter");
    }

    String getPreferredCodeNameBase() {
        return properties.getProperty("mainModule");
    }
    String getFeatureCodeNameBase() {
        String f = properties.getProperty("featureModule");
        if (f != null) {
            return f.length() == 0 ? null : f;
        }
        return getPreferredCodeNameBase();
    }

    public final boolean isEnabled() {
        Boolean e = cacheEnabled;
        if (e != null) {
            return e;
        }

        for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (cnbs.contains(mi.getCodeNameBase())) {
                if (!FeatureManager.showInAU(mi)) {
                    continue;
                }
                return cacheEnabled = mi.isEnabled();
            }
        }
        return cacheEnabled = false;
    }

    public final URL getLayerURL() {
        return delegateLayer;
    }

    /** @return 0 = no
     *          1 = yes
     *          2 = I am interested to be turned on when this project is opened
     */
    int isProject(FeatureProjectFactory.Data data) {
        FeatureProjectFactory.LOG.log(Level.FINE, "Checking project {0}", data);
        int toRet;
        if (isNbProject(data)) {
            toRet = 1;
        } else {
            if (files.isEmpty()) {
                toRet = 0;
            } else {
                toRet = 0;
                for (Object[] required : files.keySet()) {
                    String s = (String)required[0];
                    FeatureProjectFactory.LOG.log(Level.FINER, "    checking file {0}", s);
                    if (data.hasFile(s)) {
                        FeatureProjectFactory.LOG.log(Level.FINER, "    found", s);
                        Object r1 = required[1];
                        if (data.isDeepCheck() && r1 != null) {
                            XPathExpression e;
                            if (!(r1 instanceof XPathExpression)) {
                                try {
                                    final String path = (String) r1;
                                    if (path.isEmpty()) {
                                        toRet = 2;
                                        continue;
                                    }
                                    required[1] = e = XPathFactory.newInstance().newXPath().compile(path);
                                } catch (XPathExpressionException ex) {
                                    FoDLayersProvider.LOG.log(Level.WARNING, "Cannot parse " + r1, ex);
                                    continue;
                                }
                            } else {
                                e = (XPathExpression)r1;
                            }
                            Document content = data.dom(s);
                            try {
                                String res = e.evaluate(content);
                                FeatureProjectFactory.LOG.log(
                                    Level.FINER,
                                    "Parsed result {0} of type {1}",
                                    new Object[] {
                                        res, res == null ? null : res.getClass()
                                    }
                                );
                                if (res != null && res.length() > 0) {
                                    toRet = 2;
                                }
                            } catch (XPathExpressionException ex) {
                                FeatureProjectFactory.LOG.log(Level.INFO, "Cannot parse " + data, ex);
                            }
                        } else {
                            toRet = 1;
                        }
                        break;
                    }
                }
            }
        }
        FeatureProjectFactory.LOG.log(Level.FINE, "  isProject: {0}", toRet);
        return toRet;
    }

    public final Set<String> getCodeNames() {
        return Collections.unmodifiableSet(cnbs);
    }
    
    public final Set<ExtraModuleInfo> getExtraModules() {
        Set<ExtraModuleInfo> s = new LinkedHashSet<>();
        for (int i = 0; ; i++) {
            String propName = i == 0 ? "extra.modules" : "extra.modules." + i; // NOI18N
            String cnbPattern = properties.getProperty(propName);
            if (cnbPattern == null) {
                break;
            }
            String recMin = properties.getProperty(propName + ".recommended.min.jdk"); // NOI18N
            String recMax = properties.getProperty(propName + ".recommended.max.jdk"); // NOI18N
            s.add(new ExtraModuleInfo(cnbPattern, recMin, recMax));
        }
        return s;
    }

    public final String getExtraProjectMarkerClass() {
        return properties.getProperty("extra.project.marker.class");
    }

    static final class ExtraModuleInfo {
        private final Pattern cnb;
        private final SpecificationVersion recMinJDK;
        private final SpecificationVersion recMaxJDK;

        ExtraModuleInfo(String cnbPattern, String recMin, String recMax) {
            this.cnb = Pattern.compile(cnbPattern);
            this.recMinJDK = recMin != null ? new SpecificationVersion(recMin) : null;
            this.recMaxJDK = recMax != null ? new SpecificationVersion(recMax) : null;
        }
        
        String displayName() {
            return cnb.pattern();
        }
        
        /**
         * Returns the codename, if the pattern is actually a literal.
         * Literal pattern should not contain any metacharacters, except '.' and '/'.
         * @return codename or {@code null}
         */
        String explicitCodebase() {
            String pat = cnb.pattern();
            for (int a = pat.length() - 1; a >= 0; a--) {
                char c = pat.charAt(a);
                if (!(Character.isAlphabetic(c) || Character.isDigit(c))) {
                    if (c != '.' && c != '/') {
                        return null;
                    }
                }
            }
            return pat;
        }

        boolean matches(String cnb) {
            return this.cnb.matcher(cnb).matches();
        }

        final boolean isRequiredFor(SpecificationVersion jdk) {
            if (recMinJDK != null && jdk.compareTo(recMinJDK) < 0) {
                return true;
            }
            if (recMaxJDK != null && jdk.compareTo(recMaxJDK) >= 0) {
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "ExtraModuleInfo{" + "cnb=" + cnb + ", recMin=" + recMinJDK + ", recMax=" + recMaxJDK + '}';
        }
    }

    public final String getExtraModulesRequiredText() {
        return properties.getProperty("LBL_Ergonomics_Extra_Required"); // NOI18N
    }

    public final String getExtraModulesRecommendedText() {
        return properties.getProperty("LBL_Ergonomics_Extra_Recommended"); // NOI18N
    }

    public boolean isPresent() {
        Boolean p = cachePresent;
        if (p != null) {
            return p;
        }

        Set<String> codeNames = new HashSet<String>(getCodeNames());
        for (ModuleInfo moduleInfo : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            codeNames.remove(moduleInfo.getCodeNameBase());
        }
        return cachePresent = codeNames.isEmpty();
    }

    void clearCache() {
        cachePresent = null;
        cacheEnabled = null;
    }

    @Override
    public String toString() {
        return "FeatureInfo[" + clusterName + "]";
    }
    
    private boolean isNbProject(FeatureProjectFactory.Data data) {
        if (nbproject.isEmpty()) {
            return false;
        } else {
            if (!data.hasFile("nbproject/project.xml")) { // NOI18N
                FeatureProjectFactory.LOG.log(Level.FINEST, "    nbproject/project.xml not found"); // NOI18N
                return false;
            }
            if (!data.isDeepCheck()) {
                FeatureProjectFactory.LOG.log(Level.FINEST, "    no deep check, OK"); // NOI18N
                return true;
            }
            String text = data.is("nbproject/project.xml"); // NOI18N
            if (text == null) {
                return false;
            }
            for (String t : nbproject.keySet()) {
                final String pattern = "<type>" + t + "</type>"; // NOI18N
                if (text.indexOf(pattern) >= 0) { // NOI18N
                    FeatureProjectFactory.LOG.log(Level.FINEST, "    '" + pattern + "' found, OK"); // NOI18N
                    return true;
                } else {
                    FeatureProjectFactory.LOG.log(Level.FINEST, "    '" + pattern + "' not found"); // NOI18N
                }
            }
            FeatureProjectFactory.LOG.log(Level.FINEST, "    not accepting"); // NOI18N
            return false;
        }
    }

    final void nbproject(String prjType, String clazz) {
        nbproject.put(prjType, clazz);
    }
    final void projectFile(String file, String xpath, String clazz) throws XPathExpressionException {
        files.put(new Object[] { file, xpath }, clazz);
    }
    static Map<String,String> nbprojectTypes() {
        Map<String,String> map = new HashMap<String, String>();

        for (FeatureInfo info : FeatureManager.features()) {
            map.putAll(info.nbproject);
        }
        return map;
    }

    static Map<String,String> projectFiles() {
        Map<String,String> map = new HashMap<String, String>();

        for (FeatureInfo info : FeatureManager.features()) {
            for (Map.Entry<Object[], String> e : info.files.entrySet()) {
                if (e.getValue().length() > 0) {
                    map.put((String)(e.getKey()[0]), e.getValue());
                }
            }
        }
        return map;
    }

    private static String[] safeXPathSplit(String xpathList) {
        List<String> xpaths = new ArrayList<String>();
        boolean inSelector = false;
        int start = 0, i=0;
        for (i=0; i<xpathList.length(); i++) {
            char c = xpathList.charAt(i);
            switch (c) {
                case '[':   //NOI18N
                    inSelector = true;
                    break;
                case ']':   //NOI18N
                    inSelector = false;
                    break;
                case ',':   //NOI18N
                    if (!inSelector) {
                        String xpath = xpathList.substring(0, i);
                        start = i + 1;
                        xpaths.add(xpath);
                    }
                    break;
            }
        }
        xpaths.add(xpathList.substring(start, i));
        return xpaths.toArray(new String[0]);
    }

}
