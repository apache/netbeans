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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.projectopener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.jnlp.BasicService;
import javax.jnlp.FileContents;
import javax.jnlp.PersistenceService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 *
 * @author Milan Kubec
 */
public class Utils {
    
    private static final int BUFF_SIZE = 2048;
    
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.US);
    
    private static Logger LOGGER = WSProjectOpener.LOGGER;
    
    private static final String winLauncher  = "netbeans.exe";
    private static final String unixLauncher = "netbeans";
    private static final String macLauncher  = "netbeans";
    
    private static final int SEARCH_DEPTH = 2;
    
    private static Properties properties;
    
    private static boolean savedProxyUsed = false;
    
    public static File anotherNBDir = null;
    
    Utils() {}
    
    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            // nothing to do
        }
    }
    
    public static void unzip(File srcFile, File destDir) throws IOException {
        InputStream input = null;
        OutputStream output = null;
        ZipFile zipfile = new ZipFile(srcFile);
        Enumeration zipEntries = zipfile.entries();
        while(zipEntries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) zipEntries.nextElement();
            if (entry.isDirectory()) {
                new File(destDir, entry.getName()).mkdir();
                continue;
            }
            try {
                input = new BufferedInputStream(zipfile.getInputStream(entry));
                File destFile = new File(destDir, entry.getName());
                FileOutputStream fos = new FileOutputStream(destFile);
                output = new BufferedOutputStream(fos);
                copyStreams(input, output);
            } finally {
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.flush();
                    output.close();
                }
            }
        }
    }
    
    public static void download(String urlStr, File destFile) throws IOException {
        URL srcUrl = new URL(urlStr);
        try {
            List list = ProxySelector.getDefault().select(srcUrl.toURI());
            LOGGER.info("List of proxies from Proxy Selector: " + list);
        } catch (Throwable t) {
            // nothing happens, it's just for logging
        }
        InputStream input = null;
        OutputStream output = null;
        try {
            input = srcUrl.openStream();
            FileOutputStream fos = new FileOutputStream(destFile);
            output = new BufferedOutputStream(fos);
            copyStreams(input, output);
        } finally {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.flush();
                output.close();
            }
        }
    }
    
    private static void installProxySelector(final String hostName, final int portNum) {
        ProxySelector.setDefault(new ProxySelector() {
            public List select(URI uri) {
                List list = new ArrayList();
                list.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostName, portNum)));
                return list;
            }
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                //
            }
        });
    }
    
    private static void copyStreams(InputStream input, OutputStream output) throws IOException {
        int count;
        byte data[] = new byte[BUFF_SIZE];
        while ((count = input.read(data, 0, BUFF_SIZE)) != -1) {
            output.write(data, 0, count);
        }
    }
    
    public static File createTempFile(File dir, String prefix, String suffix, boolean delOnExit) throws IOException {
        File tempFile = File.createTempFile(prefix, suffix, dir);
        if (delOnExit) {
            tempFile.deleteOnExit();
        }
        return tempFile;
    }
    
    public static File createTempDir(File dir, String prefix) throws IOException {
        File tempDir = File.createTempFile(prefix, "", dir);
        if (!tempDir.delete()) {
            throw new IOException("Cannot delete file: " + tempDir.getAbsolutePath());
        }
        if (!tempDir.mkdir()) {
            throw new IOException("Cannot create folder: " + tempDir.getAbsolutePath());
        }
        return tempDir;
    }
    
    public static String getPlatformLauncher() {
        String retVal = "";
        if (OS_NAME.indexOf("win") != -1) {
            retVal = winLauncher;
        } else if (OS_NAME.indexOf("unix") != -1) {
            retVal = unixLauncher;
        } else if (OS_NAME.indexOf("linux") != -1) {
            retVal = unixLauncher;
        } else if (OS_NAME.indexOf("mac os") != -1) {
            retVal = macLauncher;
        } else if (OS_NAME.indexOf("solaris") != -1) {
            retVal = unixLauncher;
        } else if (OS_NAME.indexOf("sunos") != -1) {
            retVal = unixLauncher;
        }
        return retVal;
    }
    
    /**
     * 
     */
    public static String exc2String(Throwable t) {
        StringWriter writer = new StringWriter();
        t.printStackTrace(new PrintWriter(writer));
        writer.flush();
        return writer.toString();
    }
    
    /**
     * 
     */
    public static SavedProjects getSavedProjects(File dir) {
        Set projectTypes = new HashSet();
        List savedProjects = savedNbProjects(dir, 0, projectTypes);
        return new SavedProjects(savedProjects, projectTypes);
    }
    
    // searches also recursively to find nested projects
    private static List savedNbProjects(File dir, int depth, Set pTypes) {
        if (depth > SEARCH_DEPTH) {
            return Collections.EMPTY_LIST;
        }
        List sProjects = new ArrayList();
        File subdirs[] = dir.listFiles(new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                return false;
            }
        });
        for (int i = 0; i < subdirs.length; i++) {
            ProjectType pt = getNbProjectType(subdirs[i]);
            if (pt != null) {
                SavedProjects.OneProject sp = new SavedProjects.OneProject(subdirs[i]);
                sProjects.add(sp);
                pTypes.add(pt);
            }
            sProjects.addAll(savedNbProjects(subdirs[i], depth + 1, pTypes));
        }
        return sProjects;
    }
    
    private static ProjectType getNbProjectType(File f) {
        assert f != null;
        File prjXmlFile = new File(new File(f, "nbproject"), "project.xml");
        if (prjXmlFile.exists() && prjXmlFile.isFile()) {
            switch (getProjectType(f)) {
                case ProjectType.J2SE_TYPE: return ProjectType.J2SE;
                case ProjectType.FREEFORM_TYPE: return ProjectType.FREEFORM;
                case ProjectType.J2ME_TYPE: return ProjectType.J2ME;
                case ProjectType.WEB_TYPE: return ProjectType.WEB;
                case ProjectType.EJB_TYPE: return ProjectType.EJB;
                case ProjectType.EAR_TYPE: return ProjectType.EAR;
            }
        }
        // try other unusual projects:
        // 1) Maven
        File pomFile = new File(f, "pom.xml");
        if (pomFile.exists()) {
            return ProjectType.MAVEN;
        }
        return null;
    }
    
    private static int getProjectType(File f) {
        assert f != null;
        File prjXmlFile = new File(new File(f, "nbproject"), "project.xml");
        if (prjXmlFile.exists() && prjXmlFile.isFile()) {
            BufferedReader reader = null;
            try {
                reader = new java.io.BufferedReader(new java.io.FileReader(prjXmlFile));
                String line = reader.readLine();
                while (line != null) {
                    if (line.indexOf("<type>") != -1) {
                        return getTypeFromLine(line);
                    }
                    line = reader.readLine();
                }
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                try {
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return ProjectType.UNKNOWN_TYPE;
    }
    
    private static int getTypeFromLine(String s) {
        if (s.indexOf("<type>" + ProjectType.J2SE_NAME + "</type>") != -1) {
            return ProjectType.J2SE_TYPE;
        } else if (s.indexOf("<type>" + ProjectType.FREEFORM_NAME + "</type>") != -1) {
            return ProjectType.FREEFORM_TYPE;
        } else if (s.indexOf("<type>" + ProjectType.J2ME_NAME + "</type>") != -1) {
            return ProjectType.J2ME_TYPE;
        } else if (s.indexOf("<type>" + ProjectType.WEB_NAME + "</type>") != -1) {
            return ProjectType.WEB_TYPE;
        } else if (s.indexOf("<type>" + ProjectType.EJB_NAME + "</type>") != -1) {
            return ProjectType.EJB_TYPE;
        } else if (s.indexOf("<type>" + ProjectType.EAR_NAME + "</type>") != -1) {
            return ProjectType.EAR_TYPE;
        }
        return ProjectType.UNKNOWN_TYPE;
    }
    
    public static void showErrMessage(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    public static boolean maybeAnotherProxy() {
        
        if (!savedProxyUsed) {
            // get saved properties from PersistenceService
            String proxyHost = getProperty("jws.http.proxyHost");
            String proxyPort = getProperty("jws.http.proxyPort");
            
            // if there are some valid proxy settings saved already try them
            if (proxyHost != null && !proxyHost.equals("")
                    && proxyPort != null && !proxyPort.equals("")) {
                int portNum = Integer.parseInt(proxyPort);
                LOGGER.info("Using saved proxy settings: proxyHost = " +
                        proxyHost + ", proxyPort = " + proxyPort);
                installProxySelector(proxyHost, portNum);
                savedProxyUsed = true;
                return true;
            }
        }
        
        // ask user for better proxy settigns
        ProxySettings ps = new ProxySettings();
        DialogDescriptor dd = new DialogDescriptor();
        ProxySettingsDialog pd = new ProxySettingsDialog(ps, dd);
        pd.setVisible(true);
        pd.dispose();
        
        if (dd.getValue().equals(DialogDescriptor.CONTINUE)) {
            // try to install own ProxySelector
            String portNumStr = ps.getProxyPort();
            if (!portNumStr.equals("") && portNumStr != null) {
                String hostName = ps.getProxyHost();
                int portNum = Integer.parseInt(ps.getProxyPort());
                installProxySelector(hostName, portNum);
                // store proxy settings using PersistentService
                setProperty("jws.http.proxyHost", hostName);
                setProperty("jws.http.proxyPort", portNumStr);
            }
            return true;
        } else {
            return false;
        }
        
    }
    
    public static Integer getAnotherNBInstallDir(String nbv) {
        DialogDescriptor dd = new DialogDescriptor();
        NBInstallDir nbd = new NBInstallDir();
        BrowseNetBeansDialog browseNBDialog = new BrowseNetBeansDialog(dd, nbd, nbv);
        browseNBDialog.setVisible(true);
        browseNBDialog.dispose();
        if (dd.getValue() != null) {
            if (dd.getValue().equals(DialogDescriptor.CONTINUE)) {
                anotherNBDir = nbd.getInstallDir();
                return DialogDescriptor.CONTINUE;
            } else if (dd.getValue().equals(DialogDescriptor.DOWNLOAD)) {
                return DialogDescriptor.DOWNLOAD;
            }
        }
        return DialogDescriptor.EXIT;
    }
    
    // ---
    
    public static BasicService getBasicService() {
        BasicService service = null;
        try {
            service = (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
        } catch (UnavailableServiceException ex) {
            // no service => return null
        }
        return service;
    }
    
    public static PersistenceService getPersistenceService() {
        PersistenceService service = null;
        try {
            service = (PersistenceService) ServiceManager.lookup("javax.jnlp.PersistenceService");
        } catch (UnavailableServiceException ex) {
            // no service => return null
        }
        return service;
    }
    
    // ---
    
    public static String getProperty(String pName) {
        if (properties == null) {
            loadProperties();
        }
        String pVal = properties.getProperty(pName);
        LOGGER.info("Getting property: " + pName + " = " + pVal);
        return pVal;
    }
    
    public static void setProperty(String pName, String pVal) {
        LOGGER.info("Setting property: " + pName + " = " + pVal);
        if (properties == null) {
            loadProperties();
        }
        properties.setProperty(pName, pVal);
        saveProperties();
    }
    
    private static void loadProperties() {
        BasicService bService = getBasicService();
        PersistenceService pService = getPersistenceService();
        properties = new Properties();
        try {
            FileContents fc = pService.get(bService.getCodeBase());
            properties.load(fc.getInputStream());
        } catch (Exception ex) {
            // no props will be loaded
            LOGGER.info(Utils.exc2String(ex));
        }
    }
    
    private static void saveProperties() {
        BasicService bService = getBasicService();
        PersistenceService pService = getPersistenceService();
        URL codeBase = bService.getCodeBase();
        FileContents fc = null;
        try {
            fc = pService.get(codeBase);
        } catch (FileNotFoundException fnfe) {
            try {
                // Entry probably doesn't exist => create it
                // XXX the size is arbitrary
                pService.create(codeBase, 30000L);
                fc = pService.get(codeBase);
            } catch (Exception ex) {
                LOGGER.info(Utils.exc2String(ex));
            }
        } catch (Exception ex) {
            // no props will be saved
            LOGGER.info(Utils.exc2String(ex));
        }
        try {
            properties.store(fc.getOutputStream(true), null);
        } catch (IOException ioe) {
            LOGGER.info(Utils.exc2String(ioe));
        }
    }
    
    // ---
    
    /**
     * Tries to open passed URL in system browser 
     * using JNLP BasicService
     */
    public static boolean showDocument(String url) {
        URL url2Show = null;
        try {
            url2Show = new java.net.URL(url);
        } catch (MalformedURLException ex) {
            // nothing much to do here
        }
        BasicService service = getBasicService();
        if (service != null) {
            return service.showDocument(url2Show);
        }
        return false;
    }
    
    // ---
    
    /**
     * Returns parts of the NB version number if it matches the regexp
     * e.g. '1.2.3beta2' = > [0] == 1.2.3, [1] == beta, [2] == 2
     * if not matches returns null
     */
    public static String[] getVersionParts(String s) {
        if (s == null) {
            return null;
        }
        String retVal[] = new String[] { "", "", "" };
        Pattern p = Pattern.compile("(\\d*(\\.\\d+)*)([a-zA-Z]*)(\\d*)");
        Matcher m = p.matcher(s);
        if (m.matches()) {
            retVal[0] = m.group(1);
            retVal[1] = m.group(3);
            retVal[2] = m.group(4);
            return retVal;
        }
        return null;
    }
    
    /**
     * Compares two NB versions (only numbers), e.g. 5.5.1, 6.0,
     * returns negative if first version number parameter is lower than second,
     * positive if first is higher and 0 if both versions are the same
     */
    public static int compareVersions(String verStr1, String verStr2) {
        int vd1[] = parseVersionString(verStr1);
        int vd2[] = parseVersionString(verStr2);
        int len1 = vd1.length;
        int len2 = vd2.length;
        int max = Math.max(len1, len2);
        for (int i = 0; i < max; i++) {
            int d1 = ((i < len1) ? vd1[i] : 0);
            int d2 = ((i < len2) ? vd2[i] : 0);
            if (d1 != d2) {
                return d1 - d2;
            }
        }
        return 0;
    }
    
    /**
     * Compare release types in following way: dev < beta < rc < ""
     */
    public static int compareReleaseTypes(String relType1, String relType2) {
        int retVal = 0;
        if (relType1.equals(relType2)) {
            retVal = 0;
        } else if (relType1.equals("")) {
            retVal = 1;
        } else if (relType2.equals("")) {
            retVal = -1;
        } else if (relType1.equals("dev") && ((relType2.equals("beta") || relType2.equals("rc")))) {
            retVal = -1;
        } else if (relType2.equals("dev") && ((relType1.equals("beta") || relType1.equals("rc")))) {
            retVal = 1;
        } else if (relType1.equals("beta") && relType2.equals("rc")) {
            retVal = -1;
        } else if (relType2.equals("beta") && relType1.equals("rc")) {
            retVal = 1;
        }
        return retVal;
    }
    
    private static int[] parseVersionString(String s) throws NumberFormatException {
        StringTokenizer st = new StringTokenizer(s, ".", true);
        int len = st.countTokens();
        if ((len % 2) == 0) {
            throw new NumberFormatException("Even number of pieces in a spec version: '" + s + "'"); // NOI18N
        }
        int i = 0;
        int[] digits = new int[len / 2 + 1];
        boolean expectingNumber = true;
        while (st.hasMoreTokens()) {
            if (expectingNumber) {
                expectingNumber = false;
                int piece = Integer.parseInt(st.nextToken());
                if (piece < 0) {
                    throw new NumberFormatException("Spec version component < 0: " + piece); // NOI18N
                }
                digits[i++] = piece;
            } else {
                if (!".".equals(st.nextToken())) { // NOI18N
                    throw new NumberFormatException("Expected dot in spec version: '" + s + "'"); // NOI18N
                }
                expectingNumber = true;
            }
        }
        return digits;
    }
    
    // ---
    
    public static class ProxySettings {
        
        private String proxyHost;
        private String proxyPort;
        
        public ProxySettings() {
            proxyHost = "";
            proxyPort = "";
        }
        
        public ProxySettings(String host, String port) {
            proxyHost = host;
            proxyPort = port;
        }
        
        public String getProxyHost() {
            return proxyHost;
        }
        
        public String getProxyPort() {
            return proxyPort;
        }
        
        public void setProxyHost(String s) {
            proxyHost = s;
        }
        
        public void setProxyPort(String s) {
            proxyPort = s;
        }
        
    }
    
    public static class DialogDescriptor {
        
        public static final Integer EXIT = new Integer(0);
        public static final Integer CONTINUE = new Integer(1);
        public static final Integer DOWNLOAD = new Integer(2);
        
        private Object value;
        
        public DialogDescriptor() {}
        
        public Object getValue() {
            return value;
        }
        
        public void setValue(Object val) {
            value = val;
        }
        
    }
    
    public static class NBInstallDir {
        private File dir;
        public NBInstallDir() {}
        public File getInstallDir() {
            return dir;
        }
        public void setInstallDir(File d) {
            dir = d;
        }
    }
    
}
