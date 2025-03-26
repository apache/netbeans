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

package org.netbeans.modules.nativeexecution.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 *
 * @author vk155633
 */
public class NativeExecutionTestSupport {

    private static ExecutionEnvironment defaultTestExecutionEnvironment;
    private static RcFile localRcFile;
    private static Map<ExecutionEnvironment, RcFile> remoteRcFiles = new HashMap<>();
    private static final Map<String, ExecutionEnvironment> spec2env = new LinkedHashMap<>();
    private static final Map<ExecutionEnvironment, String> env2spec = new LinkedHashMap<>();

    private NativeExecutionTestSupport() {
    }

    public static synchronized RcFile getRcFile() throws IOException, RcFile.FormatException {
        if (localRcFile == null) {
            String rcFileName = System.getProperty("cnd.remote.rcfile"); // NOI18N
            if (rcFileName == null) {
                String homePath = System.getProperty("user.home");                
                if (homePath != null) {
                    File homeDir = new File(homePath);
                    localRcFile = RcFile.create(new File(homeDir, ".cndtestrc"));
                }
            } else {
                localRcFile = RcFile.create(new File(rcFileName));
            }
        }
        return localRcFile;
    }

    public static synchronized RcFile getRemoteRcFile(ExecutionEnvironment env)
            throws IOException, RcFile.FormatException, ConnectException, 
            CancellationException, InterruptedException, InterruptedException,
            ExecutionException {
        if (env == null) {
            new Exception("WARNING: null ExecutionEnvironment; returning dummy remote rc file").printStackTrace();
            return RcFile.createDummy();
        }
        RcFile rcFile = remoteRcFiles.get(env);
        if (rcFile == null) {
            rcFile = createRemoteRcFile(env);
            remoteRcFiles.put(env, rcFile);
        }
        return rcFile;
    }

    private static RcFile createRemoteRcFile(ExecutionEnvironment env)
            throws IOException, RcFile.FormatException, ConnectException, CancellationException, InterruptedException, ExecutionException {
        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
            new Exception("WARNING: getRemoteRcFile changes connection state for " + env).printStackTrace();
            ConnectionManager.getInstance().connectTo(env);
        }
        String envText = ExecutionEnvironmentFactory.toUniqueID(env).replace(':', '-').replace('@', '-');
        String tmpName = "cnd_remote_test_rc_" + envText;
        File tmpFile = File.createTempFile(tmpName, "");
        tmpFile.deleteOnExit();
        String remoteFilePath = HostInfoUtils.getHostInfo(env).getUserDir() + "/.cnd-remote-test-rc";
        if (fileExists(env, remoteFilePath)) {
            int rc = CommonTasksSupport.downloadFile(remoteFilePath, env, tmpFile, new PrintWriter(System.err)).get();
            if (rc != 0) {
                throw new IOException("Can't download file " + remoteFilePath + " from " + env);
            }
            return RcFile.create(tmpFile);
        } else {
            return RcFile.createDummy();
        }
    }

    public static boolean fileExists(ExecutionEnvironment env, String remoteFilePath)
            throws ExecutionException, InterruptedException {
        try {
            FileInfoProvider.StatInfo stat = FileInfoProvider.lstat(env, remoteFilePath).get();
        } catch (ExecutionException ex) {
            if (notExist(ex)) {
                return false;
            } else {
                throw ex;
            }
        }
        return true;
    }

    private static boolean notExist(ExecutionException e) {
        Throwable ex = e;
        while (ex != null) {
            if (ex instanceof FileInfoProvider.SftpIOException) {
                switch(((FileInfoProvider.SftpIOException)ex).getId()) {
                    case FileInfoProvider.SftpIOException.SSH_FX_NO_SUCH_FILE:
                    case FileInfoProvider.SftpIOException.SSH_FX_PERMISSION_DENIED:
                    return true;
                }
                break;
            }
            ex = ex.getCause();
        }
        return false;
    }

    /**
     * Gets old-style default test execution environment -
     * i.e. the one that is set via -J-Dcnd.remote.testuserinfo
     * or CND_REMOTE_TESTUSERINFO environment variable
     */
    public static ExecutionEnvironment getDefaultTestExecutionEnvironment(boolean connect) throws IOException, CancellationException {
        synchronized(NativeExecutionBaseTestCase.class) {
            if (defaultTestExecutionEnvironment == null) {
                String ui = System.getProperty("cnd.remote.testuserinfo"); // NOI18N
                char[] passwd = null;
                if( ui == null ) {
                    ui = System.getenv("CND_REMOTE_TESTUSERINFO"); // NOI18N
                }
                if (ui != null) {
                    int m = ui.indexOf(':');
                    if (m>-1) {
                        int n = ui.indexOf('@');
                        String strPwd = ui.substring(m+1, n);
                        String remoteHKey = ui.substring(0,m) + ui.substring(n);
                        defaultTestExecutionEnvironment = ExecutionEnvironmentFactory.fromUniqueID(remoteHKey);
                        passwd = strPwd.toCharArray();                        
                    } else {
                        String remoteHKey = ui;
                        defaultTestExecutionEnvironment = ExecutionEnvironmentFactory.fromUniqueID(remoteHKey);
                    }
                } else {
                    defaultTestExecutionEnvironment = ExecutionEnvironmentFactory.createNew(System.getProperty("user.name"), "127.0.0.1"); // NOI18N
                }
                if (defaultTestExecutionEnvironment != null) {
                    if(passwd != null && passwd.length > 0) {
                        PasswordManager.getInstance().storePassword(defaultTestExecutionEnvironment, passwd, false);
                    }
                    
                    if (connect) {
                        ConnectionManager.getInstance().connectTo(defaultTestExecutionEnvironment);
                    } 
                }
            }
        }
        return defaultTestExecutionEnvironment;
    }

    private interface UsetrInfoProcessor {
        /** @return true to proceed, false to cancel */
        boolean processLine(String spec, ExecutionEnvironment env, char[] passwd);
    }

    private static void processTestUserInfo(UsetrInfoProcessor processor) throws IOException {
        String rcFileName = System.getProperty("cnd.remote.testuserinfo.rcfile"); // NOI18N
        File userInfoFile = null;

        if (rcFileName == null) {
            String homePath = System.getProperty("user.home");
            if (homePath != null) {
                File homeDir = new File(homePath);
                userInfoFile = new File(homeDir, ".testuserinfo");
            }
        } else {
            userInfoFile = new File(rcFileName);
        }

        if (userInfoFile == null || ! userInfoFile.exists()) {
            return;
        }

        BufferedReader rcReader = new BufferedReader(new FileReader(userInfoFile));
        String str;
        Pattern infoPattern = Pattern.compile("^([^#].*)[ \t]+(.*)"); // NOI18N
        Pattern pwdPattern = Pattern.compile("([^:]+):(.*)@(.*)"); // NOI18N
        char[] passwd = null;

        while ((str = rcReader.readLine()) != null) {
            Matcher m = infoPattern.matcher(str);
            String spec = null;
            String loginInfo;

            if (m.matches()) {
                spec = m.group(1).trim();
                loginInfo = m.group(2).trim();
            } else {
                continue;
            }

            m = pwdPattern.matcher(loginInfo);
            String remoteHKey = null;

            if (m.matches()) {
                passwd = m.group(2).toCharArray();
                remoteHKey = m.group(1) + "@" + m.group(3); // NOI18N
            } else {
                remoteHKey = loginInfo;
            }

            ExecutionEnvironment env = ExecutionEnvironmentFactory.fromUniqueID(remoteHKey);
            if (!processor.processLine(spec, env, passwd)) {
                break;
            }
        }
    }

    public static ExecutionEnvironment[] getTestExecutionEnvironmentsFromSection(String section) throws IOException {
        String[] platforms = getPlatforms(section, null);
        ExecutionEnvironment[] environments = new ExecutionEnvironment[platforms.length];
        for (int i = 0; i < platforms.length; i++) {
            environments[i] = NativeExecutionTestSupport.getTestExecutionEnvironment(platforms[i]);
        }
        return environments;
    }

    public static String[] getPlatforms(String section, NbTestSuite suite) {
        try {
            try {
                RcFile rcFile = NativeExecutionTestSupport.getRcFile();
                List<String> result = new ArrayList<>();
                // We specify environments as just keys in the given section - without values.
                // We also allow specifying some other parameters in the same sections.
                // So we treat a key=value pair as another parameter, not an execution environment
                for (String key : rcFile.getKeys(section)) {
                    String value = rcFile.get(section, key, null);
                    if (value == null) {
                        result.add(key);
                    }
                }
                Collections.sort(result);
                return result.toArray(new String[0]);
            } catch (FileNotFoundException ex) {
                // rcfile does not exists - no tests to run
            }
        } catch (IOException ex) {
            if (suite != null) {
                suite.addTest(TestSuite.warning("Cannot get execution environment: " + exceptionToString(ex)));
            }
        } catch (FormatException ex) {
            if (suite != null) {
                suite.addTest(TestSuite.warning("Cannot get execution environment: " + exceptionToString(ex)));
            }
        }
        return new String[0];
    }

    protected static String exceptionToString(Throwable t) {
            StringWriter stringWriter= new StringWriter();
            PrintWriter writer= new PrintWriter(stringWriter);
            t.printStackTrace(writer);
            return stringWriter.toString();
    }

    public static ExecutionEnvironment getTestExecutionEnvironment(final String mspec) throws IOException {
        if (mspec == null) {
            return null;
        }
        final AtomicReference<ExecutionEnvironment> result = new AtomicReference<>();
        final AtomicReference<char[]> passwd = new AtomicReference<>();
        processTestUserInfo(new UsetrInfoProcessor() {
            @Override
            public boolean processLine(String spec, ExecutionEnvironment e, char[] p) {
                if (mspec.equals(spec)) {
                    result.set(e);
                    passwd.set(p);
                    return false;
                }
                return true;
            }
        });
        if (result.get() != null) {
            if (passwd.get() != null) {
                PasswordManager.getInstance().storePassword(result.get(), passwd.get(), false);
            }
        }

        spec2env.put(mspec, result.get());
        env2spec.put(result.get(), mspec);
        return result.get();
    }

    public static char[] getTestPassword(final ExecutionEnvironment env) throws IOException {
        if (env == null) {
            return null;
        }
        final AtomicReference<char[]> passwd = new AtomicReference<>();
        processTestUserInfo(new UsetrInfoProcessor() {
            @Override
            public boolean processLine(String spec, ExecutionEnvironment e, char[] p) {
                if (env.equals(e)) {
                    passwd.set(p);
                    return false;
                }
                return true;
            }
        });
        return passwd.get();
    }


    /**
     * Gets an MSpec string, which was used for getting the given environment
     * (i.e. it's an inverse of getTestExecutionEnvironment(String))
     */
    public static String getMspec(ExecutionEnvironment execEnv) {
        return env2spec.get(execEnv);
    }

    public static boolean getBoolean(String condSection, String condKey) {
        return getBoolean(condSection, condKey, false);
    }

    public static boolean getBoolean(String condSection, String condKey, boolean defaultValue) {
        try {
            String value = getRcFile().get(condSection, condKey);
            return (value == null) ? defaultValue : Boolean.parseBoolean(value);
        } catch (FileNotFoundException ex) {
            // silently: just no file => condition is false, that's it
            return defaultValue;
        } catch (IOException ex) {
            return defaultValue;
        } catch (RcFile.FormatException ex) {
            return defaultValue;
        }
    }
    
    public static String mkTemp(ExecutionEnvironment execEnv, boolean directory) throws Exception {        
        String[] mkTempArgs;
        OSFamily osFamily = HostInfoUtils.getHostInfo(execEnv).getOSFamily();
        if (osFamily == OSFamily.MACOSX || osFamily == OSFamily.FREEBSD) {
            mkTempArgs = directory ? new String[] { "-t", "tmp", "-d" } : new String[] { "-t", "tmp" };
        } else {
            mkTempArgs = directory ? new String[] { "-d" } : new String[0];
        }        
        ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "mktemp", mkTempArgs);
        if (!res.isOK()) {
            throw new AssertionError("mktemp failed on " + execEnv + ": " + res.getErrorString() + " return code: " + res.exitCode);
        }
        if (Boolean.getBoolean("trace.mktemp")) {   // trace all mkTemp
            StackTraceElement caller = null;
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (int i = 1; i < stackTrace.length; i++) {
                if (!stackTrace[i].getMethodName().contains("mkTemp")) {
                    caller = stackTrace[i];
                    break;
                }
            }
            System.err.printf("mktemp -> %s called by %s\n", res.getOutputString(), caller.getClassName() + '.' + caller.getMethodName());
        }
        return res.getOutputString();
    }    
    
    public static void threadsDump(String header, String footer) {
        final Set<Map.Entry<Thread, StackTraceElement[]>> stack = Thread.getAllStackTraces().entrySet();
        System.err.println(header);
        for (Map.Entry<Thread, StackTraceElement[]> entry : stack) {
            System.err.println(entry.getKey().getName());
            for (StackTraceElement element : entry.getValue()) {
                System.err.println("\tat " + element.toString());
            }
            System.err.println();
        }
        System.err.println(footer);
    }

}
