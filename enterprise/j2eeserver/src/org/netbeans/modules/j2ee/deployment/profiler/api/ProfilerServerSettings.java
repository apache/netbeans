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

package org.netbeans.modules.j2ee.deployment.profiler.api;

import java.util.Arrays;
import java.util.List;
import org.netbeans.api.java.platform.JavaPlatform;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Settings that will be used for profiled server startup.
 *
 * @author sherold
 * 
 * @deprecated 
 */
@Deprecated
public final class ProfilerServerSettings {

    private final JavaPlatform    javaPlatform;
    private final String[]        jvmArgs;
    private final String[]        env;

    /**
     * Creates new ProfilerServerSettings.
     *
     * @param javaPlatform Java platform used to run profiled server.
     * @param jvmArgs      array of extra JVM arguments used for starting profiled 
     *                     server.
     * @param env          array of <code>name=value</code> pairs of extra variables 
     *                     to be set for profiled server environment.
     */
    public ProfilerServerSettings(JavaPlatform javaPlatform, String[] jvmArgs, String[] env) {
        if (javaPlatform == null) {
            throw new NullPointerException("The javaPlatform argument must not be null.");  // NOI18N
        }
        if (jvmArgs == null) {
            throw new NullPointerException("The jvmArgs argument must not be null.");       // NOI18N
        }
        if (env == null) {
            throw new NullPointerException("The env argument must not be null.");           // NOI18N
        }
        this.javaPlatform   = javaPlatform;
        this.jvmArgs        = jvmArgs.clone();
        this.env            = env.clone();
    }

    /**
     * Gets the Java platform that will be used for starting the profiled server.
     *
     * @return Java platform that will be used for starting the profiled server.
     */
    public JavaPlatform getJavaPlatform() {
        return javaPlatform;
    }

    /**
     * Gets the extra arguments that will be used for starting the profiled server.
     *
     * @return array of extra arguments that will be used for starting the profiled 
     *         server
     */
    public String[] getJvmArgs() {
        return jvmArgs;
    }

    /**
     * Gets extra variables that will be set for profiled server environment.
     *
     * @return array of <code>name=value</code> pairs describing extra variables 
     *         that will be set for profiled server environment
     */
    public String[] getEnv() {
        return env;
    }
    
    /**
     * Tests this ProfilerServerSettings for equality with the given object.
     *
     * @param o The object to be compared with this ProfilerServerSettings.
     *
     * @return  <code>true</code> if the other ProfilerServerSettings instance 
     *          defines the same settings; false otherwise.
     */
    public boolean equals(Object o) {
        if (o == this) {
	    return true;
	}
        if (!(o instanceof ProfilerServerSettings)) {
            return false;
        }
        ProfilerServerSettings other = (ProfilerServerSettings)o;
        FileObject javaHome = (FileObject)javaPlatform.getInstallFolders().iterator().next();
        FileObject otherJavaHome = (FileObject)other.javaPlatform.getInstallFolders().iterator().next();
        if (!FileUtil.toFile(javaHome).equals(FileUtil.toFile(otherJavaHome))) {
            return false;
        }
        if (jvmArgs.length != other.jvmArgs.length) {
            return false;
        }
        List jvmArgsList = Arrays.asList(jvmArgs);
        for (int i = 0; i < other.jvmArgs.length; i++) {
            if (!jvmArgsList.contains(other.jvmArgs[i])) {
                return false;
            }
        }
        if (env.length != other.env.length) {
            return false;
        }
        List envList = Arrays.asList(env);
        for (int i = 0; i < other.env.length; i++) {
            if (!envList.contains(other.env[i])) {
                return false;
            }
        }
        return true;
    }
    
    public int hashCode() {
        int result = 17;
        FileObject javaHome = (FileObject)javaPlatform.getInstallFolders().iterator().next();
        result = 37 * result + FileUtil.toFile(javaHome).hashCode();        
        String[] jvmArgsTmp = (String[])jvmArgs.clone();
        Arrays.sort(jvmArgsTmp);
        for (int i = 0; i < jvmArgsTmp.length; i++) {
            result = 37 * result + jvmArgsTmp[i].hashCode();
        }
        String[] envTmp = (String[])env.clone();
        Arrays.sort(envTmp);
        for (int i = 0; i < envTmp.length; i++) {
            result = 37 * result + envTmp[i].hashCode();
        }
        return result;
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ProfilerServerSettings [\n");   //  NOI18N
        buffer.append("  javaPlatform: " + javaPlatform.getDisplayName() + "\n"); //  NOI18N
        buffer.append("  jvmarg:   ");  //  NOI18N      //  NOI18N
        for (int i = 0; i < jvmArgs.length; i++) {
            buffer.append(jvmArgs[i] + " ");    //  NOI18N
        }
        buffer.append("\n");                    //  NOI18N
        buffer.append("  env:      ");          //  NOI18N
        for (int i = 0; i < env.length; i++) {  
            buffer.append(env[i] + " ");        //  NOI18N
        }
        buffer.append("]");                     //  NOI18N
        return buffer.toString();
    }
}
