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
/*
 * ServerLocationManager
 *
 */

package org.netbeans.modules.j2ee.sun.api;


import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.modules.InstalledFileLocator;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;


public class ServerLocationManager  {
    
    public static final int SJSAS_90 = 900;
    
    public static final int SJSAS_91 = 910;
    
    public static final int SJSAS_82 = 820;
    
    public static final int GF_V1 = 900;
    
    public static final int GF_V2 = 910;

    public static final int GF_V2point1 = 911;

    public static final int GF_V2point1point1 = 912;

    public static final String INSTALL_ROOT_PROP_NAME = "com.sun.aas.installRoot"; //NOI18N
    private static final String JAR_BRIGDES_DEFINITION_LAYER="/J2EE/SunAppServer/Bridge"; //NOI18N
    private static Map serverLocationAndClassLoaderMap = Collections.synchronizedMap((Map)new HashMap(2,1));

    private static void updatePluginLoader(File platformLocation, ExtendedClassLoader loader) throws Exception{
        try {
            java.io.File f = platformLocation;
            if (null == f || !f.exists()){
                return;
            }
            String installRoot = f.getAbsolutePath();
            //if we are only 8.1 set the necessary property there:
//            if(!isGlassFish(f)){
//                System.setProperty(INSTALL_ROOT_PROP_NAME, installRoot);
//            }
            
            FileObject bridgesDir = FileUtil.getConfigFile(JAR_BRIGDES_DEFINITION_LAYER);
            FileObject[] ch =new  FileObject[0];
            if(bridgesDir!=null){
                ch = bridgesDir.getChildren();
            }
            
            for(int i = 0; i < ch.length; i++) {
                String location= (String)ch[i].getAttribute("jar.location");//NOI18N
                //System.out.println("Location is "+location + platformLocation);
                InstalledFileLocator fff= InstalledFileLocator.getDefault();
                f = fff.locate(location, null, true);
                if (f!=null){
                    loader.addURL(f);
                    loadLocaleSpecificJars(f, loader);
                } else
                    System.out.println("cannot locate file "+location);//NOI18N
                
            }
            
	    f = new File(installRoot+"/lib/appserver-deployment-client.jar");//NOI18N
	    loader.addURL(f);

            f = new File(installRoot+"/lib/appserv-admin.jar");//NOI18N
	    loader.addURL(f);
	    f = new File(installRoot+"/lib/appserv-ext.jar");//NOI18N
	    loader.addURL(f);
	    f = new File(installRoot+"/lib/appserv-rt.jar");//NOI18N
	    loader.addURL(f);
	    f = new File(installRoot+"/lib/appserv-cmp.jar");//NOI18N
	    loader.addURL(f);
	    f = new File(installRoot+"/lib/commons-logging.jar");//NOI18N
	    loader.addURL(f);
	    f = new File(installRoot+"/lib/admin-cli.jar");//NOI18N
	    loader.addURL(f);
	    f = new File(installRoot+"/lib/common-laucher.jar");//NOI18N
	    loader.addURL(f);
	    f = new File(installRoot+"/lib/j2ee.jar");//NOI18N
	    loader.addURL(f);
	    f = new File(installRoot+"/lib/install/applications/jmsra/imqjmsra.jar");//NOI18N
	    loader.addURL(f);
	    
	    //for AS 8.1: no more endorsed dir!!!
//////	    f = new File(installRoot+"/lib/xercesImpl.jar");
//////	    loader.addURL(f);
//////	    f = new File(installRoot+"/lib/dom.jar");
//////	    loader.addURL(f);
//////	    f = new File(installRoot+"/lib/xalan.jar");
	//    loader.addURL(f);
	    //for AS 8.1:
	    f = new File(installRoot+"/lib/jaxrpc-api.jar");//NOI18N
	    loader.addURL(f);
	    f = new File(installRoot+"/lib/jaxrpc-impl.jar");//NOI18N
	    loader.addURL(f);
	    	    
	} catch (Exception ex2) {
	    throw new Exception(ex2.getLocalizedMessage());
	}
    }
    
    

    
    /* return the latest available platform, i.e if 9 and 8.1 are regsitered,
     ** this will return the location of the AS 9 server
     ** this way we can access the latest DTDs, etc that cover also the 8.1 server
     ** because of the backward compatibility requirement.
     * may return null if no platform is available...
     **/
    
    static public File getLatestPlatformLocation(){
        Iterator i = serverLocationAndClassLoaderMap.entrySet().iterator();
        File ret =null;
        while (i.hasNext()){
            Map.Entry e = (Map.Entry)i.next();
            String loc = (String)e.getKey();
            File possibleOne = new File(loc);
            if (ret==null){
                ret =possibleOne;
            }
            if (isGlassFish(possibleOne)){
                ret =possibleOne;
            }
        }
        return ret;
        
    }
    
    /*
     *used to get the netbeans classload of this class.
     *
     **/
    static class Empty{
	
    }
    
    
    public static ClassLoader getServerOnlyClassLoader(File platformLocation){
	CacheData data =(CacheData) serverLocationAndClassLoaderMap.get(platformLocation.getAbsolutePath());
	if (data==null){// try to initialize it
	    getNetBeansAndServerClassLoader(platformLocation);
	    data =(CacheData) serverLocationAndClassLoaderMap.get(platformLocation.getAbsolutePath());
            if (data==null){
                return null;
            }
	}
        	return data.serverOnlyClassLoader;

    }
    public synchronized static DeploymentFactory getDeploymentFactory(File platformLocation) {
	CacheData data =(CacheData) serverLocationAndClassLoaderMap.get(platformLocation.getAbsolutePath());
	if (data==null){// try to initialize it
	    getNetBeansAndServerClassLoader(platformLocation);
	    data =(CacheData) serverLocationAndClassLoaderMap.get(platformLocation.getAbsolutePath());
            if (data==null){
                return null;
            }
	}
	return data.deploymentFactory;
    
    }
    
    public synchronized static ClassLoader getNetBeansAndServerClassLoader(File platformLocation) {
	CacheData data =(CacheData) serverLocationAndClassLoaderMap.get(platformLocation.getAbsolutePath());
	if (data==null){
	    if (!isGoodAppServerLocation(platformLocation)){
		return null;
            }
            data = new CacheData();
	    serverLocationAndClassLoaderMap.put(platformLocation.getAbsolutePath(), data);
	    
	}
	if(data.cachedClassLoader==null){
	    if (!isGoodAppServerLocation(platformLocation)){
		return null;
            }
	    try {
		data.cachedClassLoader =new ExtendedClassLoader( new Empty().getClass().getClassLoader());
		updatePluginLoader( platformLocation, data.cachedClassLoader);
		data.deploymentFactory =  (DeploymentFactory) data.cachedClassLoader.loadClass("com.sun.enterprise.deployapi.SunDeploymentFactory").newInstance();//NOI18N
                data.serverOnlyClassLoader = new ExtendedClassLoader();
                updatePluginLoader(platformLocation, data.serverOnlyClassLoader);
	    } catch (Exception ex2) {
		Logger.getLogger(ServerLocationManager.class.getName()).log(Level.FINER,null,ex2);     // NOI18N
	    }}
	
	return data.cachedClassLoader;
    }
    
    
    private static Collection fileColl = new java.util.ArrayList();
    
    static {
	fileColl.add("bin");                                                    //NOI18N
	fileColl.add("lib");                                                    //NOI18N
	fileColl.add("config");                                                 //NOI18N
    }
    
    public static boolean isGlassFish(File candidate){
	//now test for AS 9 (J2EE 5.0) which should work for this plugin
	File as9 = new File(candidate.getAbsolutePath()+
                "/lib/dtds/sun-web-app_2_5-0.dtd");                             //NOI18N
	return as9.exists();
    }
    /* return true if derby/javaDB is installed with the server
     **/
    public static boolean isJavaDBPresent(File installdir){
	//check for both names, derby or jaadb
        File derbyInstall = new File(installdir,"derby");//NOI18N
        if (!derbyInstall.exists()){
             derbyInstall = new File(installdir,"javadb");//NOI18N for latest Glassfish
        }	
	return derbyInstall.exists();
    }    
    // TODO remove isGoodAppServerLocation from PluginProperties.java???
    public static boolean isGoodAppServerLocation(File candidate){
	if (null == candidate || !candidate.exists() || !candidate.canRead() ||
		!candidate.isDirectory()  || !hasRequiredChildren(candidate, fileColl)) {
	    
	    return false;
	}
        File f = new File(candidate.getAbsolutePath()+"/lib/appserv-rt.jar");//NOI18N
        if (!f.exists()){
            return false;
        }
        
	//now test for AS 9 (J2EE 5.0) which should work for this plugin
	if(isGlassFish(candidate)){
	    return true;//we are as9
	}
	
	//one extra test to detect 8.0 versus 8.1: dom.jar has to be in lib not endorsed anymore:
////	File f = new File(candidate.getAbsolutePath()+"/lib/dom.jar");
////	return f.exists();
        return true;
	
    }
    
    
    
    
    
    private static void loadLocaleSpecificJars(File file, ExtendedClassLoader loader) {
	File parentDir = file.getParentFile();
	//System.out.println("parentDir: " + parentDir);
	File localeDir = new File(parentDir, "locale"); //NOI18N
	if(localeDir.exists()){
	    File[] localeFiles = localeDir.listFiles();
	    File localeFile; // = null;
	    String localeFileName;// = null;
	    String fileName = file.getName();
	    fileName = getFileNameWithoutExt(fileName);
	    //System.out.println("fineName: " + fileName);
	    assert(fileName.length() > 0);
	    for(int i=0; i<localeFiles.length; i++){
		localeFile = localeFiles[i];
		localeFileName = localeFile.getName();
		//System.out.println("localeFileName: " + localeFileName);
		assert(localeFileName.length() > 0);
		if(localeFileName.startsWith(fileName)){
		    try{
			loader.addURL(localeFile);
		    }catch (Exception ex2) {
			System.out.println(ex2.getLocalizedMessage());
		    }
		}
	    }
	}
    }
    
    private static String getFileNameWithoutExt(String fileName){
	int index = fileName.lastIndexOf("."); //NOI18N
	if(index != -1){
	    fileName = fileName.substring(0, index);
	}
	return fileName;
    }

    private static   boolean hasRequiredChildren(File candidate, Collection requiredChildren) {
        if (null == candidate){
            return false;
        }
        String[] children = candidate.list();
        if (null == children){
            return false;
        }
        if (null == requiredChildren){
            return true;
        }
        java.util.List kidsList = java.util.Arrays.asList(children);
        return kidsList.containsAll(requiredChildren);
    }
    
    static class CacheData{
	public CacheData(){
	    
	}
	public ExtendedClassLoader cachedClassLoader;
	public ExtendedClassLoader serverOnlyClassLoader;
        
	public DeploymentFactory deploymentFactory;
	
    }
    /** Attempt to discern the application server who's root directory was passed in.
     *
     * 9.0 uses sun-domain_1_0.dtd
     * 8.1 uses sun-domain_1_1.dtd (also includes the 1_0 version for backwards compatibility)
     *
     * @param asInstallRoot 
     * @return 
     */
    public static int getAppServerPlatformVersion(File asInstallRoot) {
        int version = 0;
        
        if(asInstallRoot != null && asInstallRoot.exists()) {
            File sunDomain11Dtd = new File(asInstallRoot, "lib/dtds/sun-domain_1_1.dtd"); // NOI18N
            //now test for AS 9 (J2EE 5.0) which should work for this plugin
            File as90 = new File((asInstallRoot)+"/lib/dtds/sun-domain_1_2.dtd");   // NOI18N
            File as91 = new File((asInstallRoot)+"/lib/dtds/sun-domain_1_3.dtd");   // NOI18N
            File as911 = new File((asInstallRoot)+"/lib/dtds/sun-ejb-jar_3_0-1.dtd");   // NOI18N
            File as211 = new File((asInstallRoot)+"/lib/install/applications/jmsra/imqstomp.jar"); // NOI18N
            if (as211.exists()) {
                version = GF_V2point1point1;
            } else if (as911.exists()) {
                version = GF_V2point1;
            } else if(as91.exists()){
                version = GF_V2; 
            } else if (as90.exists()) {
                version = GF_V1;
            } else    if(sunDomain11Dtd.exists()) {
                version = SJSAS_82;
            }
        }
        return version;
    }

    /**
     * Does this Sun AppServer install have an update center launcher?
     * 
     * @param asInstallRoot appserver install location
     * @return true if update center launcher was located, false otherwise.
     */
    public static boolean hasUpdateCenter(File asInstallRoot) {
        return getUpdateCenterLauncher(asInstallRoot) != null;
    }
    
    /**
     * Locate update center launcher within the glassfish installation
     *   [installRoot]/updatecenter/bin/updatetool[.BAT]
     * 
     * @param asInstallRoot appserver install location
     * @return File reference to launcher, or null if not found.
     */
    public static File getUpdateCenterLauncher(File asInstallRoot) {
        File result = null;
        if(asInstallRoot != null && asInstallRoot.exists()) {
            File updateCenterBin = new File(asInstallRoot, "updatecenter/bin"); // NOI18N
            if(updateCenterBin.exists()) {
                String launcher = "updatetool"; // NOI18N
                if(Utilities.isWindows()) {
                    launcher += ".BAT"; // NOI18N
                }
                File launcherPath = new File(updateCenterBin, launcher);
                result = (launcherPath.exists()) ? launcherPath : null;
            }
        }
        return result;
    }
    
}
