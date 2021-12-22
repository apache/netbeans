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

package org.netbeans.modules.glassfish.common.wizards;

import org.netbeans.modules.glassfish.common.ServerDetails;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.common.*;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.RegisteredDerbyServer;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.modules.glassfish.spi.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;


/**
 * @author Ludo
 */
public class ServerWizardIterator extends PortCollection implements WizardDescriptor.InstantiatingIterator, ChangeListener {
    
    private transient AddServerLocationPanel locationPanel = null;
    private transient AddDomainLocationPanel locationPanel2 = null;
    
    private WizardDescriptor wizard;
    private transient int index = 0;
    private transient WizardDescriptor.Panel[] panels = null;
        
    private transient List<ChangeListener> listeners = new CopyOnWriteArrayList<>();
    private String domainsDir;
    private String domainName;
    private ServerDetails sd;
    private GlassfishInstanceProvider gip;
    ServerDetails[] acceptedValues;
    ServerDetails[] downloadableValues;
    private String targetValue;

    public String getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }

    public ServerWizardIterator(ServerDetails[] possibleValues, ServerDetails[] downloadableValues) {
        this.acceptedValues = possibleValues;
        this.downloadableValues = downloadableValues;
        this.gip = GlassfishInstanceProvider.getProvider();
        this.hostName = "localhost"; // NOI18N
    }
    
    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    @Override
    public void uninitialize(WizardDescriptor wizard) {
    }
    
    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }
    
    @Override
    public void previousPanel() {
        index--;
    }
    
    @Override
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    
    @Override
    public String name() {
        return "GlassFish Server AddInstanceIterator"; // NOI18N
    }
    
    public static void showInformation(final String msg,  final String title){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                d.setTitle(title);
                DialogDisplayer.getDefault().notify(d);
            }
        });
    }
    
    @Override
    public Set instantiate() throws IOException {
        Set<ServerInstance> result = new HashSet<>();
        File ir = new File(installRoot);
        ensureExecutable(ir);
        if (null != domainsDir) {
            handleLocalDomains(result, ir);
        } else {
            handleRemoteDomains(result,ir);
        }
        // lookup the javadb register service here and use it.
        RegisteredDerbyServer db = Lookup.getDefault().lookup(RegisteredDerbyServer.class);
        if (null != db) {
            File f = new File(ir, "javadb");
            if (f.exists() && f.isDirectory() && f.canRead()) {
                db.initialize(f.getAbsolutePath());
            }
        }
        NbPreferences.forModule(this.getClass()).put("INSTALL_ROOT_KEY", installRoot); // NOI18N
        return result;
    }
    
    @Override
    public boolean hasPrevious() {
        return index > 0;
    }
    
    @Override
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }
    
    protected String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(ServerWizardIterator.class, "STEP_ServerLocation"),  // NOI18N
            NbBundle.getMessage(ServerWizardIterator.class, "STEP_Domain"), // NOI18N
        };
    }
    
    protected final String[] getSteps() {
        if (steps == null) {
            steps = createSteps();
        }
        return steps;
    }
    
    protected final WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = createPanels();
        }
        return panels;
    }
    
    protected WizardDescriptor.Panel[] createPanels() {
        if (locationPanel == null) {
            locationPanel = new AddServerLocationPanel(this);
            locationPanel.addChangeListener(this);
        }
        if (locationPanel2 == null) {
            locationPanel2 = new AddDomainLocationPanel(this);
            locationPanel2.addChangeListener(this);
        }
        
        return new WizardDescriptor.Panel[] {
            (WizardDescriptor.Panel) locationPanel,
            (WizardDescriptor.Panel) locationPanel2,
//            (WizardDescriptor.Panel)propertiesPanel
        };
    }
    
    private transient String[] steps = null;
    
    protected final int getIndex() {
        return index;
    }
    
    @Override
    public WizardDescriptor.Panel current() {
        WizardDescriptor.Panel result = getPanels()[index];
        JComponent component = (JComponent)result.getComponent();
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, getSteps());  // NOI18N
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, getIndex());// NOI18N
        return result;
    }
    
    @Override
    public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {
        fireChangeEvent();
    }
    
    protected final void fireChangeEvent() {
        ChangeEvent ev = new ChangeEvent(this);
        for(ChangeListener listener: listeners) {
            listener.stateChanged(ev);
        }
    }
    
    /** GlassFish server administrator's user name. */
    private String userName;
    /** GlassFish server administrator's password. */
    private String password;
    private String installRoot;
    private String glassfishRoot;
    private String hostName;
    /** GlassFish server is local or remote. Value is <code>true</code>
     *  for local server and  <code>false</code> for remote server. */
    private boolean isLocal;
    private boolean useDefaultPorts;
    private boolean defaultJavaSESupported;

    public boolean isUseDefaultPorts() {
        return useDefaultPorts;
    }

    public void setUseDefaultPorts(boolean useDefaultPorts) {
        this.useDefaultPorts = useDefaultPorts;
    }

    public void serDefaultJavaSESupported(boolean defaultJavaSESupported) {
        this.defaultJavaSESupported = defaultJavaSESupported;
    }

    public boolean isDefaultJavaSESupported() {
        return defaultJavaSESupported;
    }

    /**
     * Is GlassFish server local or remote?
     * <p/>
     * @return Value is <code>true</code> for local server
     *         and  <code>false</code> for remote server.
     */
    public boolean isLocal() {
        return isLocal;
    }

    /**
     * Set GlassFish server as local or remote.
     * <p/>
     * @param isLocal Value is <code>true</code> for local server
     *                and  <code>false</code> for remote server.
     */
    public void setLocal(final boolean isLocal) {
        this.isLocal = isLocal;
    }

    public String formatUri(String host, int port, String target,
            String domainsD, String domainN) {
        String domainInfo = "";
        if (null != domainsD && domainsD.length() > 0 &&
                null != domainN && domainN.length() > 0) {
            domainInfo
                    = File.pathSeparator + domainsD + File.separator + domainN;
        }
        if (null == target || "".equals(target.trim())) {
            return null != sd
                    ? "[" + glassfishRoot + domainInfo + "]"
                    + sd.getUriFragment() + ":" + host + ":" + port
                    : "[" + glassfishRoot + domainInfo + "]null:"
                    + host + ":" + port;
        } else {
            return null != sd
                    ? "[" + glassfishRoot + domainInfo + "]"
                    + sd.getUriFragment() + ":" + host + ":" + port+":"+target
                    : "[" + glassfishRoot + domainInfo + "]null:"
                    + host + ":" + port+":"+target;
        }
    }

    /**
     * Set GlassFish server administrator's user name.
     * <p/>
     * @param userName GlassFish server administrator's user name to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Set GlassFish server administrator's password.
     * <p/>
     * @param password GlassFish server administrator's password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public void setInstallRoot(String installRoot) {
        this.installRoot = installRoot;
    }
    
    String getGlassfishRoot() {
        return this.glassfishRoot;
    }
    
    public void setGlassfishRoot(String glassfishRoot) {
        this.glassfishRoot = glassfishRoot;
    }

    boolean hasServer(String uri) {
        return gip.hasServer(uri);
    }

    ServerDetails isValidInstall(File installDir, File glassfishDir, WizardDescriptor wizard) {
        String errMsg = NbBundle.getMessage(AddServerLocationPanel.class, "ERR_InstallationInvalid", // NOI18N
                FileUtil.normalizeFile(installDir).getPath());
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, errMsg); // getSanitizedPath(installDir)));
        File jar = ServerUtilities.getJarName(glassfishDir.getAbsolutePath(), ServerUtilities.GFV3_JAR_MATCHER);
        if (jar == null || !jar.exists()) {
            return null;
        }

        File containerRef = new File(glassfishDir, "config" + File.separator + "glassfish.container");
        if (!containerRef.exists()) {
            return null;
        }
        for (ServerDetails candidate : acceptedValues) {
            if (candidate.isInstalledInDirectory(glassfishDir)) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "   ");
                this.sd = candidate;
                return candidate;
            }
        }
        return null;

    }

    /**
     * Set values for remote domain.
     * <p/>
     * Domains directory shall be <code>null</code> for remote domains.
     * <p/>
     * @param domainName Domain name to set.
     */
    public void setRemoteDomain(final String domainName) {
        this.domainsDir = null;
        this.domainName = domainName;
    }

    // expose for qa-functional tests
    public void setDomainLocation(String absolutePath) {
        if (null == absolutePath) {
            domainsDir = null;
            domainName = null;
        } else {
            int dex = absolutePath.lastIndexOf(File.separator);
            this.domainsDir = absolutePath.substring(0,dex);
            this.domainName = absolutePath.substring(dex+1);
        }
    }

    // Borrowed from RubyPlatform...
    private void ensureExecutable(File installDir) {
        // No excute permissions on Windows. On Unix and Mac, try.
        if(Utilities.isWindows()) {
            return;
        }

        if(!Utils.canWrite(installDir)) {
            // for unwritable installs (e.g root), don't even bother.
            return;
        }

        List<File> binList = new ArrayList<>();
        for(String binPath: new String[] { "bin", "glassfish/bin", "javadb/bin", // NOI18N
                "javadb/frameworks/NetworkServer/bin", "javadb/frameworks/embedded/bin" }) { // NOI18N
            File dir = new File(installDir, binPath);
            if(dir.exists()) {
                binList.add(dir);
            }
        }

        if(binList.isEmpty()) {
            return;
        }

        // Ensure that the binaries are installed as expected
        // The following logic is from CLIHandler in core/bootstrap:
        File chmod = new File("/bin/chmod"); // NOI18N

        if(!chmod.isFile()) {
            // Mac & Linux use /bin, Solaris /usr/bin, others hopefully one of those
            chmod = new File("/usr/bin/chmod"); // NOI18N
        }

        if(chmod.isFile()) {
            try {
                for(File binDir: binList) {
                    List<String> argv = new ArrayList<>();
                    argv.add(chmod.getAbsolutePath());
                    argv.add("u+rx"); // NOI18N

                    String[] files = binDir.list();
                    for(String file : files) {
                        if(file.indexOf('.') == -1 || file.endsWith(".ksh")) {
                            argv.add(file);
                        }
                    }

                    ProcessBuilder pb = new ProcessBuilder(argv);
                    pb.directory(binDir);
                    Process process = pb.start();
                    int chmoded = process.waitFor();

                    if(chmoded != 0) {
                        throw new IOException(NbBundle.getMessage(
                                Retriever.class, "ERR_ChmodFailed", argv, chmoded)); // NOI18N
                    }
                }
            } catch (IOException | InterruptedException | MissingResourceException ex) {
                Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex); // NOI18N
            }
        } else {
            String message = NbBundle.getMessage(Retriever.class, "ERR_ChmodNotFound"); // NOI18N
            StringBuilder builder = new StringBuilder(message.length() + 50 * binList.size());
            builder.append(message);
            for(File binDir: binList) {
                builder.append('\n'); // NOI18N
                builder.append(binDir);
            }
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    builder.toString(), NotifyDescriptor.WARNING_MESSAGE));
        }
    }

    private void handleLocalDomains(Set<ServerInstance> result, File ir) {
        File domainDir = new File(domainsDir, domainName);
        String canonicalPath = null;
        try {
            canonicalPath = domainDir.getCanonicalPath();
        } catch (IOException ioe) {
            Logger.getLogger("glassfish").log(Level.INFO, domainDir.getAbsolutePath(), ioe); // NOI18N
        }
        if (null != canonicalPath && !canonicalPath.equals(domainDir.getAbsolutePath())) {
            setDomainLocation(canonicalPath);
            domainDir = new File(domainsDir, domainName);
        }
        if (!domainDir.exists() && AddServerLocationPanel.canCreate(domainDir)) {
            // Need to create a domain right here!
            Map<String, String> ip = new HashMap<>();
            ip.put(GlassfishModule.INSTALL_FOLDER_ATTR, installRoot);
            ip.put(GlassfishModule.GLASSFISH_FOLDER_ATTR, glassfishRoot);
            ip.put(GlassfishModule.DISPLAY_NAME_ATTR, (String) wizard.getProperty("ServInstWizard_displayName")); // NOI18N
            ip.put(GlassfishModule.DOMAINS_FOLDER_ATTR, domainsDir);
            ip.put(GlassfishModule.DOMAIN_NAME_ATTR, domainName);
            CreateDomain cd = new CreateDomain("anonymous", "", new File(glassfishRoot), ip, gip,false, // NOI18N
                    useDefaultPorts,"INSTALL_ROOT_KEY"); // NOI18N
            int newHttpPort = cd.getHttpPort();
            int newAdminPort = cd.getAdminPort();
            cd.start();
            GlassfishInstance instance = GlassfishInstance.create(
                    (String) wizard.getProperty("ServInstWizard_displayName"),  // NOI18N
                    installRoot, glassfishRoot, domainsDir, domainName, 
                    newHttpPort, newAdminPort, userName, password, targetValue,
                    formatUri(hostName, newAdminPort, getTargetValue(),domainsDir,domainName), 
                    gip);
            result.add(instance.getCommonInstance());
        } else {
            GlassfishInstance instance = GlassfishInstance.create(
                    (String) wizard.getProperty("ServInstWizard_displayName"),  // NOI18N
                    installRoot, glassfishRoot, domainsDir, domainName,
                    getHttpPort(), getAdminPort(), userName, password, targetValue,
                    formatUri(hostName, getAdminPort(), getTargetValue(), domainsDir, domainName),
                    gip);
            result.add(instance.getCommonInstance());
        }
    }

    private void handleRemoteDomains(Set<ServerInstance> result, File ir) {
        String hn = getHostName();
        if ("localhost".equals(hn)) {
            hn = "127.0.0.1";
        }
        GlassfishInstance instance = GlassfishInstance.create(
                (String) wizard.getProperty("ServInstWizard_displayName"),   // NOI18N
                installRoot, glassfishRoot, null, domainName,
                getHttpPort(), getAdminPort(), userName, password, targetValue,
                formatUri(hn, getAdminPort(), getTargetValue(),null, domainName), gip);
        result.add(instance.getCommonInstance());
    }

    /**
     * @return the hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * @param hostName the hostName to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
