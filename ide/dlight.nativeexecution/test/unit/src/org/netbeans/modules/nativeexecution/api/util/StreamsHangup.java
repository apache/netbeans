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
package org.netbeans.modules.nativeexecution.api.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.accessibility.AccessibleRole;

/**
 *
 * @author vkvashin
 */

public class StreamsHangup {

    private static final String scriptName = "err_and_out.sh";

    enum Action {
        NONE,
        CALL_PROCESSUTILS_IGNORE,
        READ_ERR_THEN_OUT,
        READ_OUT_TTHEN_ERR,
    }

    public interface Delegate {
        void setup(File scriptFile) throws Exception;
        void cleanup() throws Exception;
        Process createProcess(String... arguments) throws IOException;
        String getScriptName();
        String getPrefix();
        void kill(Process process) throws IOException;
    }

    private final String prefix;
    private final Delegate delegate;

    private File scriptTempFile = null;
    private final Object scriptTempFileLock = new Object();

    public StreamsHangup(Delegate delegate) {
        this.delegate = delegate;
        this.prefix = delegate.getPrefix();
    }

    private void print(String text) {
        System.err.println(prefix + text);
    }

    private static String readStream(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        char buf[] = new char[4096];
        int cnt = 0;
        while ((cnt = reader.read(buf)) != -1) {
            String text = String.valueOf(buf, 0, cnt);
            sb.append(text);
        }
        reader.close();
        return sb.toString();
    }

    private void dotest(int cycles, int bufsize, int timeout, Action action) throws Exception {
        print("Starting process with cycles=" + cycles + ", bufsize=" + bufsize);
        Process process = delegate.createProcess("-c", ""+cycles, "-o", ""+bufsize, "-e", ""+bufsize);
        print("Waiting process " + process + " with timeout=" + timeout);
        long time = System.currentTimeMillis();
        switch (action) {
            case NONE:
                break;
            case CALL_PROCESSUTILS_IGNORE:
                //ProcessUtils.ignoreProcessOutputAndError(process);
                break;
            case READ_ERR_THEN_OUT:
                readStream(process.getErrorStream());
                readStream(process.getInputStream());
                break;
            case READ_OUT_TTHEN_ERR:
                readStream(process.getInputStream());
                readStream(process.getErrorStream());
                break;
            default:
                throw new AssertionError(action.name());
        }
        boolean exited = process.waitFor(timeout, TimeUnit.SECONDS);
        if (!exited) {
            delegate.kill(process);
            throw new TimeoutException("Process jas not finish in " + timeout + " seconds");
        }
        time = System.currentTimeMillis() - time;
        print("Waited " + time + " ms");
        assertEquals("Script exit code", 0, process.exitValue());        
    }

    private void findBufSize(Action action, int timeout) throws Exception {
        int sz = 32;
        while (true) {
            try {
                dotest(1, sz, timeout, action);
            } catch (TimeoutException ex) {
                String text = "Hung with bufsize=" + sz + "; action=" + action;
                print(text);
                TimeoutException ex2 = new TimeoutException(text);
                ex2.initCause(ex);
                throw ex2;
            }
            if (sz > 1024*16) {
                sz += 1024*4;
            } else if (sz > 1024) {
                sz += 1024;
            } else {
                sz *= 2;
            }
        }
    }

    private File getScriptTempFile() throws IOException {
        synchronized (scriptTempFileLock) {
            if (scriptTempFile == null) {
                File scriptTmpDir = File.createTempFile(getClass().getSimpleName(), ".tmp");
                scriptTmpDir.delete();
                scriptTmpDir.mkdirs();
                scriptTempFile = new File(scriptTmpDir, scriptName);
                writeFile(scriptTempFile, getScriptText());
                scriptTempFile.setExecutable(true);
            }
        }
        return scriptTempFile;
    }


    private static class TrivialDelegate implements Delegate {

        private volatile File scriptFile;

        @Override
        public void setup(File scriptFile) {
            this.scriptFile = scriptFile;
        }

        @Override
        public void cleanup() {
        }

        @Override
        public Process createProcess(String... args) throws IOException {
            String[] cmd = new String[args.length + 1];
            cmd[0] = scriptFile.getAbsolutePath();
            System.arraycopy(args, 0, cmd, 1, args.length);
            ProcessBuilder pb = new ProcessBuilder(cmd);
            return pb.start();
        }

        @Override
        public String getScriptName() {
            return scriptName;
        }

        @Override
        public String getPrefix() {
            return "";
        }

        @Override
        public void kill(Process process) {
            process.destroy();
        }
    }

    public static void main(String[] args) {
        try {
            new StreamsHangup(new TrivialDelegate()).test();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void assertEquals(String message, int expected, int actual) {
        if(expected != actual) {
            throw new AssertionError(message + " expected " + expected + " but was " + actual);
        }
    }

    public void test() throws Exception {
        delegate.setup(getScriptTempFile());
        //findBufSize(Action.NONE, 10);
        findBufSize(Action.READ_ERR_THEN_OUT, 10);

        // does not hang
        // dotest(1, 1024, 20, Action.READ_OUT_TTHEN_ERR);

        // hangs
        // dotest(1, 1024*1024, 20, Action.READ_OUT_TTHEN_ERR);

//        dotest(1, 1024*1024, 30, Action.NONE);
//        dotest(1, 1024*1024, 30, Action.CALL_PROCESSUTILS_IGNORE);
        delegate.cleanup();
    }

    public void cleanup() {
        synchronized (scriptTempFileLock) {
            if (scriptTempFile != null) {
                scriptTempFile.delete();
                scriptTempFile.getParentFile().delete();
            }
        }
    }

    private static File writeFile(File f, String body) throws IOException {
        f.getParentFile().mkdirs();
        OutputStream os = new FileOutputStream(f);
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
        pw.print(body);
        pw.flush();
        os.close();
        return f;
    }

    private static String getScriptText() {
        return 
            "#!/bin/bash\n" +
            "\n" +
            "function usage() {\n" +
            "    short_name=$1\n" +
            "    echo \"$short_name -\"\n" +
            "    echo \"    prints messages to stderr and stdout in cycle\"\n" +
            "    echo \"Usage:\"\n" +
            "    echo \"    $short_name -c <cycles> -e <err_msg_len> -o <out_msag_len>\"\n" +
            "    exit 1\n" +
            "}\n" +
            "\n" +
            "function message() {\n" +
            "    i=$1\n" +
            "    len=`expr $2 - 1` # reserve 1 position for '\\n'\n" +
            "    err_or_out=$3\n" +
            "    format=\"%-${len}d\\n\"\n" +
            "    if [ \"$err_or_out\" = \"out\" ]; then\n" +
            "        printf \"$format\" $i | tr ' ' '#'\n" +
            "    else\n" +
            "        if [ \"$err_or_out\" = \"err\" ]; then\n" +
            "            printf \"$format\" $i | tr ' ' '#' >&2\n" +
            "        else\n" +
            "            exit 8\n" +
            "        fi\n" +
            "    fi\n" +
            "}\n" +
            "\n" +
            "\n" +
            "my_short_name=`basename $0`\n" +
            "\n" +
            "#usage $my_short_name\n" +
            "\n" +
            "cnt=0\n" +
            "err_len=0\n" +
            "out_len=0\n" +
            "\n" +
            "while [ -n \"$1\" ]\n" +
            "do\n" +
            "    case \"$1\" in\n" +
            "        -c)\n" +
            "            cnt=$2\n" +
            "            shift\n" +
            "            ;;\n" +
            "        -e)\n" +
            "            err_len=$2\n" +
            "            shift\n" +
            "            ;;\n" +
            "        -o)\n" +
            "            out_len=$2\n" +
            "            shift\n" +
            "            ;;\n" +
            "        *)\n" +
            "            usage $my_short_name\n" +
            "            ;;\n" +
            "    esac\n" +
            "    shift\n" +
            "done\n" +
            "\n" +
            "if [ $cnt = 0 ]; then\n" +
            "    echo cycles can not be zero\n" +
            "    usage $my_short_name\n" +
            "fi\n" +
            "if [ $err_len = 0 -a  $out_len = 0 ]; then\n" +
            "    echo both error and out messages length can not be zero\n" +
            "    usage $my_short_name\n" +
            "fi\n" +
            "\n" +
            "i=0\n" +
            "while [ $i -lt $cnt ]; do\n" +
            "    #echo $i\n" +
            "    i=`expr $i + 1`\n" +
            "    if [ $out_len != 0 ]; then\n" +
            "	message $i $out_len \"out\"\n" +
            "    fi\n" +
            "    if [ $err_len != 0 ]; then\n" +
            "	message $i $err_len \"err\"\n" +
            "    fi\n" +
            "done" +
            "";
    }

}
