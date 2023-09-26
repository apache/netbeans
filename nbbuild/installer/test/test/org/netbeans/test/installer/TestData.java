/**
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

package org.netbeans.test.installer;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *

 */
public class TestData implements Serializable {
    
    //private static final String NB_DOWNLOAD_PAGE = "http://bits.netbeans.org/netbeans/6.0/nightly/latest/";

    private File installerFile = null;
    private File uninstallerFile = null;
    private File workDir = null;
    private File bundleFile = null;
    private File uninstallerBundleFile = null;
    private Logger logger = null;
    private ClassLoader loader = null;
    private ClassLoader engineLoader = null;
    private Class installerMainClass = null;
    private Class uninstallerMainClass = null;
    private String workDirCanonicalPath = null;
    private String platformName = null;
    private String platformExt = null;
    private String installerType = null;
    private String buildNumber = null;
    private String installerURL = null;

    private String sExecutableName = null;

    private String sTestPackage = null;

    private String m_sNetBeansInstallPath;
    private String m_sApplicationServerInstallPath;
    private String m_sApplicationServerPreludeInstallPath;
    private String m_sTomcatInstallPath;

    public boolean m_bPreludePresents = false;

    public TestData(Logger logger) {
        assert logger != null;
        this.logger = logger;
        initPlatformVar();
    }

    public String getInstallerFileName() {
        return "E:/pub/Netbeans/6.0/installer.exe";
//        return "C:/work/test/TestInstaller/netbeans-6.0-nightly-200707100000-basic-windows.exe";
    }
    
    public String getNetbeansDownloadPage() {
        return System.getProperty("test.installer.url.prefix");
    }

    public String getInstallerURL() {
        return installerURL;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public void setInstallerURL(String URL) {
        installerURL = URL;
    }
    
    private void initPlatformVar() {

        if (System.getProperty("os.name").contains("Windows")) {
            platformName = "windows";
            platformExt = "exe";
        }

        if (System.getProperty("os.name").contains("Linux")) {
            platformName = "linux";
            platformExt = "sh";
        }

        if (System.getProperty("os.name").contains("Mac OS X")) {
            throw new Error("Mac OS not supported");
        }

        if (System.getProperty("os.name").contains("SunOS") && System.getProperty("os.arch").contains("sparc")) {
            platformName = "solaris-sparc";
            platformExt = "sh";
        }
        if (System.getProperty("os.name").contains("SunOS") && System.getProperty("os.arch").contains("x86")) {
            platformName = "solaris-x86";
            platformExt = "sh";
        }
    }

    public String getPlatformExt() {
        return platformExt;
    }

    public String getPlatformName() {
        return platformName;
    }

    public String getInstallerMainClassName() {
        return "org.netbeans.installer.Installer";
    }

    public String getUninstallerMainClassName() {
        return "org.netbeans.installer.Installer";
    }

    public Logger getLogger() {
        return logger;
    }

    public void setWorkDir(File workDir) throws IOException {
        assert workDir != null;
        this.workDir = workDir;
        workDirCanonicalPath = workDir.getCanonicalPath();
    }

    public File getTestWorkDir() {
        assert workDir != null;
        return workDir;
    }

    public String getWorkDirCanonicalPath() {
        return workDirCanonicalPath;
    }

    public File getInstallerFile() {
        return installerFile;
    }

    public void setInstallerFile(File installerFile) {
        if (canRead(installerFile)) {
            this.installerFile = installerFile;
        }
    }
    
    public File getUninstallerFile( )
    {
        return uninstallerFile;
    }

    public void setUninstallerFile( File uninstallerFile )
    {
        if (canRead(uninstallerFile)) {
            this.uninstallerFile = uninstallerFile;
        }
    }

    public void setBundleFile(File bundleFile) {
        if (canRead(bundleFile)) {
            this.bundleFile = bundleFile;
        }
    }

    public File getBundleFile() {
        return bundleFile;
    }
    
    public void setUninstallerBundleFile(File bundleFile) {
        if (canRead(bundleFile)) {
            this.bundleFile = bundleFile;
        }
    }

    public File getUninstallerBundleFile() {
        return bundleFile;
    }

    public void setClassLoader(ClassLoader loader) {
        assert loader != null;
        this.loader = loader;
    }

    public ClassLoader getClassLoader() {
        return loader;
    }

    public void setEngineClassLoader(ClassLoader loader) {
        assert loader != null;
        engineLoader = loader;
    }

    public ClassLoader getEngineClassLoader() {
        return engineLoader;
    }

    public void setInstallerMainClass(Class clazz) {
        assert clazz != null;
        this.installerMainClass = clazz;
    }

    public Class getInstallerMainClass() {
        return installerMainClass;
    }

    public void setUninstallerMainClass(Class clazz) {
        assert clazz != null;
        this.uninstallerMainClass = clazz;
    }

    public Class getUninstallerMainClass() {
        return uninstallerMainClass;
    }

    private boolean canRead(File file) {
        if (file != null) {
            if (!file.canRead()) {
                java.lang.String fileName = null;
                try {
                    fileName = file.getCanonicalPath();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Can't get cannonical path");
                }
                logger.log(Level.SEVERE, "Can't read file: " + fileName);
                return false;
            }
        } else {
            logger.log(Level.SEVERE, "Bundle file name can be null");
            return false;
        }
        return true;
    }

    public String getInstallerType() {
        return installerType;
    }

    public void setInstallerType(String installerType) {
        this.installerType = installerType;
    }

    public Proxy getProxy() {
        Proxy proxy = null;

        String proxyHost = System.getProperty("installer.proxy.host", null);
        String proxyPort = System.getProperty("installer.proxy.port", null);

        if (proxyHost != null && proxyPort != null) {
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
        } else {
            proxy = Proxy.NO_PROXY;
        }

        return proxy;
    }

  public String GetExecutableName( )
  {
    return sExecutableName;
  }

  public void SetExecutableName( String s )
  {
    sExecutableName = s;
    return;
  }

  public void SetTestPackage( String s )
  {
    sTestPackage = s;
  }

  public String GetTestPackage( )
  {
    return sTestPackage;
  }

  public void CreateInstallPaths( )
  {
    // NetBeans
    String sInstallBase = System.getProperty( "test.installer.custom.path" );
    if( null == sInstallBase )
    {
      m_sNetBeansInstallPath = null;
      m_sApplicationServerInstallPath = null;
      m_sApplicationServerPreludeInstallPath = null;
      m_sTomcatInstallPath = null;
    }
    else
    {
      m_sNetBeansInstallPath = sInstallBase + File.separator + Utils.NB_DIR_NAME;
      m_sApplicationServerInstallPath = sInstallBase + File.separator + Utils.GF2_DIR_NAME;
      m_sApplicationServerPreludeInstallPath = sInstallBase + File.separator + Utils.GF2_PRELUDE_DIR_NAME;
      m_sTomcatInstallPath = sInstallBase + File.separator + Utils.TOMCAT_DIR_NAME;
    }
  }

  public String GetNetBeansInstallPath( )
  {
    return m_sNetBeansInstallPath;
  }

  public String GetApplicationServerInstallPath( )
  {
    return m_sApplicationServerInstallPath;
  }

  public String GetApplicationServerPreludeInstallPath( )
  {
    return m_sApplicationServerPreludeInstallPath;
  }

  public String GetTomcatInstallPath( )
  {
    return m_sTomcatInstallPath;
  }

  public void SetDefaultPath( String s )
  {
    if( null == m_sNetBeansInstallPath )
    {
      m_sNetBeansInstallPath = s;
      return;
    }
    if( null == m_sApplicationServerInstallPath )
    {
      m_sApplicationServerInstallPath = s;
      return;
    }
    if( m_bPreludePresents )
    {
      if( null == m_sApplicationServerPreludeInstallPath )
      {
        m_sApplicationServerPreludeInstallPath = s;
        return;
      }
    }
    if( null == m_sTomcatInstallPath )
    {
      m_sTomcatInstallPath = s;
      return;
    }
    return;
  }
}
