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

package org.netbeans.junit.diff;
import java.io.*;
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
            diffFile = File.createTempFile("~diff", "tmp~");
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
