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
package org.netbeans.modules.cordova.platforms.android;

import java.io.BufferedReader;
import org.netbeans.modules.cordova.platforms.spi.MobilePlatform;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.cordova.platforms.spi.Device;
import org.netbeans.modules.cordova.platforms.api.PlatformManager;
import org.netbeans.modules.cordova.platforms.api.ProcessUtilities;
import org.netbeans.modules.cordova.platforms.spi.ProvisioningProfile;
import org.netbeans.modules.cordova.platforms.spi.SDK;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Becicka
 */
@ServiceProvider(service=MobilePlatform.class)
public class AndroidPlatform implements MobilePlatform {
    
    private static String ANDROID_SDK_ROOT_PREF = "android.sdk.home"; //NOI18N
    
    public static int DEFAULT_TIMEOUT = 30000;

    private final transient java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
    
    public AndroidPlatform() {
    }
    
    public static AndroidPlatform getDefault() {
        return (AndroidPlatform) PlatformManager.getPlatform(PlatformManager.ANDROID_TYPE);
    }

//    public void createProject(File dir, String targetId, String projectName, String activityName, String packageName) throws IOException {
//        ProcessBuilder pb = ProcessBuilder.getLocal();
//        pb.setExecutable(getSdkLocation() + "/tools/android");
//        pb.setArguments(
//        Arrays.asList(
//                "create", "project", 
//                "--target", targetId,
//                "--name", projectName,
//                "--path", dir.getPath(),
//                "--activity", activityName,
//                "--package", packageName
//                ));
//        pb.setWorkingDirectory(dir.getParentFile().getAbsolutePath());
//        try {
//            Process call = pb.call();
//            call.waitFor();
//            InputStreamReader inputStreamReader = new InputStreamReader(new BufferedInputStream(call.getErrorStream()));
//            if (call.exitValue() != 0) {
//                StringBuilder error = new StringBuilder();
//                char[] ch = new char[1];
//                while (inputStreamReader.ready()) {
//                    inputStreamReader.read(ch);
//                    error.append(ch);
//                }
//                throw new IOException(error.toString());
//            }
//        } catch (InterruptedException ex) {
//            throw new IOException(ex);
//        }
//    }
    
    @Override
    public Collection<Device> getVirtualDevices() throws IOException {
        assert !SwingUtilities.isEventDispatchThread();
        String avdString = ProcessUtilities.callProcess(getAndroidCommand(), true, AndroidPlatform.DEFAULT_TIMEOUT, "list", "avd"); //NOI18N
        return AVD.parse(avdString);
    }
    
    private String getAndroidCommand() {
        if (Utilities.isWindows()) {
            return getSdkLocation() + "\\tools\\android.bat"; // NOI18N
        } else {
            return getSdkLocation() + "/tools/android"; // NOI18N
        }
    }
    
    String getAdbCommand() {
        if (Utilities.isWindows()) {
            return getSdkLocation() + "\\platform-tools\\adb.exe"; // NOI18N
        } else {
            return getSdkLocation() + "/platform-tools/adb"; // NOI18N

        }
    }
    

    @Override
    public Collection<SDK> getSDKs() throws IOException {
        //assert !SwingUtilities.isEventDispatchThread();
        String avdString = ProcessUtilities.callProcess(getAndroidCommand(), true, 30000, "list", "target");//NOI18N
        return Target.parse(avdString);
    }
    
    private final HashSet<String> targets = new HashSet<String>(Arrays.asList(new String[]{
            "android-14", //NOI18N
            "android-15", //NOI18N
            "android-16", //NOI18N
            "android-17", //NOI18N
            "android-18", //NOI18N
            "android-19", //NOI18N
            "android-20", //NOI18N
            "android-21", //NOI18N
            "android-22"})); //NOI18N
    
    
    @Override
    public SDK getPrefferedTarget() {
        try {
            final Collection<SDK> targets1 = getSDKs();
            for (SDK t: targets1) {
                if (targets.contains(t.getName())) {
                    return t;
                }
            }
            if (targets1.iterator().hasNext()) {
                return targets1.iterator().next();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    
    @Override
    public Collection<org.netbeans.modules.cordova.platforms.spi.Device> getConnectedDevices() throws IOException {
        //assert !SwingUtilities.isEventDispatchThread();
        String avdString = ProcessUtilities.callProcess(getAdbCommand(), true, AndroidPlatform.DEFAULT_TIMEOUT, "devices"); //NOI18N
        Collection<org.netbeans.modules.cordova.platforms.spi.Device> devices = AndroidDevice.parse(avdString);
        if (devices.isEmpty()) {
            //maybe adb is just down. try to restart adb
            try {
                ProcessUtilities.callProcess(getAdbCommand(), true, AndroidPlatform.DEFAULT_TIMEOUT, "kill-server"); //NOI18N
                ProcessUtilities.callProcess(getAdbCommand(), true, AndroidPlatform.DEFAULT_TIMEOUT, "start-server"); //NOI18N
            } catch (IOException ioe) {
                //ignore
            }
        }
        avdString = ProcessUtilities.callProcess(getAdbCommand(), true, AndroidPlatform.DEFAULT_TIMEOUT, "devices"); //NOI18N
        devices = AndroidDevice.parse(avdString);
        return devices;
    }
    
    
    public ExecutorTask buildProject(File dir, String... targets) throws IOException {
        File build = new File(dir.getAbsolutePath() + File.separator + "build.xml"); //NOI18N
        FileObject buildFo = FileUtil.toFileObject(build);
        return ActionUtils.runTarget(buildFo, targets, null);
    }
    
    /**
     * Deletes dir and all subdirectories/files!
     * @param dir
     * @throws IOException 
     */
    public void cleanProject(File dir) throws IOException {
        FileUtil.toFileObject(dir).delete();
    }

    @Override
    public String getSdkLocation() {
        String sdkLocation = NbPreferences.forModule(AndroidPlatform.class).get(ANDROID_SDK_ROOT_PREF, null);
        if (sdkLocation != null && !sdkLocation.isEmpty()) {
            return sdkLocation;
        } else {
            return getSdkFromAndroidHome();
        }
    }

    @Override
    public void setSdkLocation(String sdkLocation) {
        NbPreferences.forModule(AndroidPlatform.class).put(ANDROID_SDK_ROOT_PREF, sdkLocation);
        propertyChangeSupport.firePropertyChange("SDK", null, sdkLocation);//NOI18N
    }
    
    @Override
    public boolean waitEmulatorReady(int timeout) {
        try {
            return RequestProcessor.getDefault().invokeAny(Collections.singleton(new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws Exception {
                        return waitEmulatorReady();
                    }
                }), timeout, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
        } catch (ExecutionException ex) {
        } catch (TimeoutException ex) {
        }
        return false;
        
    }
    
    private boolean waitEmulatorReady() {
        try {
            String value;
            for(;;) {
                value = ProcessUtilities.callProcess(
                        getAdbCommand(), 
                        true, 
                        -1, 
                        "-e", // NOI18N
                        "wait-for-device", // NOI18N
                        "shell", // NOI18N
                        "getprop", // NOI18N
                        "init.svc.bootanim"); //NOI18N
                if ("stopped".equals(value.trim())) { //NOI18N
                    return true;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } 
            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
        
    }

    @Override
    public void manageDevices() {
        assert !SwingUtilities.isEventDispatchThread();
        try {
            ProcessUtilities.callProcess(getAndroidCommand(), true, -1, "avd"); //NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public boolean isReady() {
        return getSdkLocation() != null && !getSdkLocation().isEmpty();
    }
    
    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    @Override
    public void addPropertyChangeListener(java.beans.PropertyChangeListener listener ) {
        propertyChangeSupport.addPropertyChangeListener( listener );
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    @Override
    public void removePropertyChangeListener(java.beans.PropertyChangeListener listener ) {
        propertyChangeSupport.removePropertyChangeListener( listener );
    }


    @Override
    public String getType() {
        return PlatformManager.ANDROID_TYPE;
    }

    @Override
    public String getSimulatorPath() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public Device getDevice(String name, EditableProperties props) {
        return AndroidDevice.get(name, props);
    }

    @Override
    public String getCodeSignIdentity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates. // NOI18N
    }

    @Override
    public String getProvisioningProfilePath() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates. // NOI18N
    }

    @Override
    public void setCodeSignIdentity(String identity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates. // NOI18N
    }

    @Override
    public void setProvisioningProfilePath(String path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates. // NOI18N
    }

    @Override
    public Collection<? extends ProvisioningProfile> getProvisioningProfiles() {
        return Collections.emptyList();
    }

    public String getProcessIdByName(String appName) {
        try {
            String result = ProcessUtilities.callProcess(getAdbCommand(), true, AndroidPlatform.DEFAULT_TIMEOUT, "shell", "ps"); //NOI18N
            BufferedReader r = new BufferedReader(new StringReader(result));
            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().endsWith(appName)) {
                    Pattern column = Pattern.compile("(\\S+)(\\s+)(\\S+)(\\s+)(.+)");
                    Matcher matcher = column.matcher(line);
                    if (matcher.matches()) {
                        return matcher.group(3);
                    }
                }
            } 
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    boolean isWebViewDebugSupported(boolean emulator) {
        try {
            String version = ProcessUtilities.callProcess(
                    getAdbCommand(),
                    true,
                    -1,
                    emulator?"-e":"-d", // NOI18N
                    "wait-for-device", // NOI18N
                    "shell", // NOI18N
                    "getprop", //NOI18N
                    "ro.build.version.release"); //NOI18N
            
            return version.compareTo("4.4") >= 0;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
        
        
    }

    /**
     * Sets the Android SDK location from ANDROID_HOME environment variable, if
     * available.
     *
     * @return Android SDK Location or <code>null</code> if Android SDK could
     * not be found.
     */
    private String getSdkFromAndroidHome() {
        String androidHomeValue = System.getenv("ANDROID_HOME"); //NOI18N
        if (androidHomeValue != null && !androidHomeValue.isEmpty()) {
            File androidLoc = new File(androidHomeValue);
            File androidTools = new File(androidLoc, "platform-tools"); //NOI18N
            boolean validSdk = androidLoc.exists() && androidLoc.isDirectory()
                    && androidTools.exists() && androidTools.isDirectory();
            if (validSdk) {
                NbPreferences.forModule(AndroidPlatform.class).put(ANDROID_SDK_ROOT_PREF, androidHomeValue);
                return androidHomeValue;
            }
        }
        return null;
    }

    /**
     * Performs a check whether adb executable is available
     *
     * @return <code>true</code> if adb command has been found,
     * <code>false</code> otherwise
     */
    boolean adbCommandExists() {
        return Files.exists(Paths.get(getAdbCommand()));
    }
}
