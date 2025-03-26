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

package org.netbeans.junit.diff;
import java.io.*;
import java.nio.file.Files;
import java.util.StringTokenizer;

/** Implementation of native OS diff.
 */
public class NativeDiff implements Diff {

    String diffcmd;

    /** Creates new NativeDiff */
    public NativeDiff() {
    }

    public void setCmdLine(String cmdLine) {
        diffcmd = cmdLine;
    }

    public String getCmdLine() {
        return diffcmd;
    }

    /**
     * @param first first file to compare
     * @param second second file to compare
     * @param diff difference file
     * @return true iff files differ
     */
    public boolean diff(java.io.File first, java.io.File second, java.io.File diff) throws java.io.IOException {
        boolean result;
        if (null != diff)
            result = diff(first.getAbsolutePath(), second.getAbsolutePath(), diff.getAbsolutePath());
        else
            result = diff(first.getAbsolutePath(), second.getAbsolutePath(), null);
        
        return result;
    }
    
    /**
     * @param first first file to compare
     * @param second second file to compare
     * @param diff difference file
     * @return true iff files differ
     */
    public boolean diff(final String first, final String second, String diff) throws java.io.IOException {
        Process prs = null;
        File    diffFile = null;
        
        if (null == diff)
            diffFile = Files.createTempFile("~diff", "tmp~").toFile();
        else
            diffFile = new File(diff);
        
        FileOutputStream fos = new FileOutputStream(diffFile);
        prs = Runtime.getRuntime().exec(prepareCommand(new File(first).getAbsolutePath(), new File(second).getAbsolutePath()));
        StreamGobbler outputGobbler = new StreamGobbler(prs.getInputStream(), fos);
        outputGobbler.start();

        try {
            prs.waitFor();
            outputGobbler.join();
        }
        catch (java.lang.InterruptedException e) {}

        try {
            fos.flush();
            fos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (0 == prs.exitValue() || null == diff) {
            diffFile.delete();
        }        
        return prs.exitValue()!=0;
    }
    
    private String[] prepareCommand(String firstFile, String secondFile) {
        StringTokenizer tok = new StringTokenizer(diffcmd);
        int tokensCount = tok.countTokens();
        String[] cmdarray = new String[tokensCount];
        for(int i=0;i<tokensCount;i++) {
            String token = tok.nextToken();
            if (token.equals("%TESTFILE%")) {
                cmdarray[i] = firstFile;
            } else if (token.equals("%PASSFILE%")) {
                cmdarray[i] = secondFile;
            } else {
                cmdarray[i] = token;
            }
        }
        return cmdarray;
    }
    
    class StreamGobbler extends Thread {
        InputStream is;
        OutputStream os;
        
        StreamGobbler(InputStream is,OutputStream redirect) {
            this.is = is;
            this.os = redirect;
        }
        
        public void run() {
            try
            {
                PrintWriter pw = null;
                if (os != null)
                    pw = new PrintWriter(os);
                
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line=null;
                while ( (line = br.readLine()) != null)
                {
                    if (pw != null)
                        pw.println(line);
                }
                if (pw != null) {
                    pw.flush();
                    pw.close();
                }
            } catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
    }
}
