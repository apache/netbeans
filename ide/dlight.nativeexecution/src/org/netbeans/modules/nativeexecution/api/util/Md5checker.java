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

package org.netbeans.modules.nativeexecution.api.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;

/**
 *
 * @author Vladimir Kvashin
 */
/*package-local*/ class Md5checker {

    public static enum Result {
        INEXISTENT,
        UPTODATE,
        DIFFERS
    }

    public static class CheckSumException extends Exception {
        public CheckSumException(String message) {
            super(message);
        }
    }

    private final ExecutionEnvironment executionEnvironment;

    public Md5checker(ExecutionEnvironment env) {
        this.executionEnvironment = env;
    }

    public Result check(File localFile, String remotePath)
            throws NoSuchAlgorithmException, IOException, CheckSumException, InterruptedException, ExecutionException, CancellationException {

        // Find out remote command for calculating md5 sum

        String cmd;
        String[] args;
        boolean first;

        final HostInfo hostIinfo = HostInfoUtils.getHostInfo(executionEnvironment);
        if (hostIinfo == null) {
            throw new CheckSumException("Can not get HostInfo for " + executionEnvironment); // NOI18N
        }
        final OSFamily oSFamily = hostIinfo.getOSFamily();
        switch (oSFamily) {
            case LINUX:
                cmd = "/usr/bin/md5sum"; // NOI18N
                args = new String[] { "-b", remotePath }; // NOI18N
                first = true;
                break;
            case FREEBSD:
                cmd = "/sbin/md5"; //NOI18N
                args = new String[] { remotePath }; // NOI18N
                first = false;
                break;
            case MACOSX:
                cmd = "sh"; // NOI18N
                args = new String [] {"-c", String.format("md5 %s || openssl -md5 %s", remotePath, remotePath)}; //NOI18N
                first = false;
                break;
            default:
                throw new NoSuchAlgorithmException("Unexpected OS: " + oSFamily); // NOI18N
        }

        // Get remote check sum        
        ExitStatus result = ProcessUtils.execute(executionEnvironment, cmd, args);

        // On MacOSX - sh -c "md5 non-existent || openssl -md5 non-existent" 
        //             has status 0.

        if (!result.isOK() || result.getOutputString().isEmpty()) {
            //throw new CheckSumException("The output of the '" + cmd + "' command is empty"); //NOI18N
            // TODO: should we check existence via a separate command?
            // it's easy to do, but will take some execution timem while result will be the same: copy the file
            return Result.INEXISTENT;
        }

        // calculate local sum while remote process is running
        String localCheckSum = getLocalChecksum(localFile);
        String[] parts = result.getOutputString().split(" "); // NOI18N
        if (parts.length == 0) {
            throw new CheckSumException("Line shouldn't be empty"); // NOI18N
        }
        String remoteCheckSum = first ? parts[0] : parts[parts.length - 1];
        if (remoteCheckSum.equals(localCheckSum)) {
            return Result.UPTODATE;
        }
        return Result.DIFFERS;
    }

    private String getLocalChecksum(File file) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
        if (file != null && file.exists()) {
            MessageDigest md5 = MessageDigest.getInstance("MD5"); // NOI18N
            InputStream is = new BufferedInputStream(new FileInputStream(file));
            try {
                byte[] buf = new byte[8192];
                int read;
                while ((read = is.read(buf)) != -1) {
                    md5.update(buf, 0, read);
                }
            } finally {
                is.close();
            }
            byte[] checkSum = md5.digest();
            return toHexString(checkSum);
        } else {
            return null;
        }
    }


    private static String toHexString(byte[] data) {
        char[] result = new char[data.length*2];
        for (int i = 0; i < data.length; i++) {
            //buf.append(String.format("%x", data[i]));
            for (int j = 0; j < 2; j++) {
                int half = (j == 0) ? (data[i] & 0x0F0) >>> 4 : data[i] & 0x0F;
                if (0 <= half && half <= 9) {
                    result[2*i+j] = (char) ('0' + half);
                } else {
                    result[2*i+j] = (char) ('a' + (half - 10));
                }
            }
        }
        return new String(result);
    }
}
