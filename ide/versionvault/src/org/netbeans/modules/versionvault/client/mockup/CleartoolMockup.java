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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.netbeans.modules.versionvault.client.mockup;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Stupka
 */
public class CleartoolMockup extends Process implements Runnable {
    
    private DelegateInputStream inputStream;    
    private ByteArrayOutputStream outputStream;
    private DelegateInputStream errorStream;

    private Exception throwable;
    private Thread thread;
    
    static final Logger LOG = Logger.getLogger("org.netbeans.modules.versionvault");
    private final String vobRoot;
    
    private String curPath = null;
    private String SELECTOR_CHECKEDOUT_FROM_MAIN = 
            File.separator + 
            "main" + 
            File.separator + 
            "CHECKEDOUT from " + 
            File.separator + 
            "main"  + 
            File.separator;
    private String SELECTOR_MAIN = 
            File.separator + 
            "main" + 
            File.separator;
    private String RULE = 
            "Rule: element * " + 
            File.separator + 
            "main" + 
            File.separator + 
            "LATEST";
    
    private static int counter = 0;
    
    public CleartoolMockup(String vobRoot) {
        outputStream = new ByteArrayOutputStream(200);            
        inputStream = new DelegateInputStream();        
        errorStream = new DelegateInputStream();
        this.vobRoot = vobRoot.endsWith(File.separator) ? vobRoot.substring(0, vobRoot.length() - 1) : vobRoot;
        if(counter++ == 0) {
            init();
        }
    }
    
    public void start() {
        thread = new Thread(this);
        thread.start();
    }
    
    private void init() {
        fixWritable(new File(vobRoot));
    }

    private void fixWritable(File file) {
        if(!file.canWrite()) {
            Repository.setFileReadOnly(file, false);
        }
        if(!file.isDirectory()) {
            return;
        }
        File[] files = file.listFiles();
        if(files != null) {
            for (File f : files) {
                fixWritable(f);
            }
        }
    }
    
    private void process(String cmd) {         
        if(cmd == null) {
            return;
        }
        cmd = cmd.trim();
        if (cmd.indexOf("i-am-finished-with-previous-command-sir") > -1) {                        
            LOG.finer("Processing: " + cmd);
            try {
                while (inputStream.available() > 0) {
                    Thread.sleep(10);
                }
                errorStream.setDelegate(new ByteArrayInputStream("cleartool: Error: Unrecognized command: \"i-am-finished-with-previous-command-sir\"\n".getBytes()));
                return;
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        if(cmd.equals("")) {
            return;
        }
        
        LOG.fine("Processing: " + cmd);
                        
        String[] args = cmd.split(" ");        
        String ctCommand = args[0];
        
        slowDown(20);
        
        if(ctCommand.equals("ls")) {
             processLS(args);            
        } else if (ctCommand.equals("cd")) {
            processCD(args);    
        } else if(ctCommand.equals("checkin")) {
             processCI(args);            
        } else if(ctCommand.equals("checkout")) {
             processCO(args);            
        } else if(ctCommand.equals("reserve")) {
             processRESERVE(args, true);            
        } else if(ctCommand.equals("unreserve")) {
             processRESERVE(args, false);            
        } else if(ctCommand.equals("lsco")) {
             processLSCO(args);            
        } else if(ctCommand.equals("mkelem")) {
             processMkElem(args);            
        } else if(ctCommand.equals("uncheckout")) {
             processUNCO(args);            
        } else if(ctCommand.equals("rmname")) {
             processRM(args);            
        } else if(ctCommand.equals("get")) {
             processGET(args);            
        } else if(ctCommand.equals("mv")) {
             processMV(args);            
        } else if(ctCommand.equals("annotate")) {
             processANNOTATE(args);            
        } else if(ctCommand.equals("lstype")) {
             processLSTYPE(args);            
        } else if(ctCommand.equals("lsvtree") ||
                ctCommand.equals("describe")  ||
                ctCommand.equals("merge")     ||
                ctCommand.equals("lshistory")) 
        {
             processUnsupported(args);            
        } else if (ctCommand.equals("quit")) {
            if(thread != null) {
                thread.interrupt();
            }
        }    
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;   
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public InputStream getErrorStream() {
        return errorStream;
    }

    @Override
    public int waitFor() throws InterruptedException {
        return 0;
    }

    @Override
    public int exitValue() {
        return 0;
    }

    @Override
    public void destroy() {        
        if(throwable != null) {
            LOG.log(Level.SEVERE, null, throwable);
        }
    }

    public void run() {
        try {
            while(true) {
                try {                
                    StringBuffer sb = null;
                    byte[] byteArray = outputStream.toByteArray();
                    boolean done = true;
                    if(byteArray.length > 0) {
                        outputStream.reset();
                        if(done) {
                            sb = new StringBuffer(byteArray.length);
                        }
                        for (byte b : byteArray) {
                            if(b == '\n') {                                   
                                process(sb.toString());                      
                                sb = new StringBuffer();
                                done = true;
                            } else {
                                sb.append(Character.toChars(b));
                                done = false;    
                            }                        
                        }
                    }
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    break;
                }
            }
        } finally {
            try { inputStream.close();  } catch (IOException alreadyClosed) { }            
            try { outputStream.close(); } catch (IOException alreadyClosed) { }            
            try { errorStream.close();  } catch (IOException alreadyClosed) { }            
        }
    }

    private void processCD(String[] args) {
        curPath = args[1].trim().substring(1);
        curPath = curPath.substring(0, curPath.length() - 1);
    }

    private void processCI(String[] args) {
        List<File> files = new ArrayList<File>();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if(arg.equals("-ptime") || 
               arg.equals("-identical") ||
               arg.equals("-ncomment")) 
            {
                // ignore
            } else if(arg.equals("-cfile") || arg.equals("-comment")) {
                i++; // skip the next arg
                continue;
            } else {
                files.add(new File(curPath + File.separator + arg));
            }
        }
        for (File file : files) {
            Repository.getInstance().ci(file, false);
        }
    }

    private void processCO(String[] args) {
        List<File> files = new ArrayList<File>();
        boolean reserved = true;
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if(arg.equals("-ptime") || 
               arg.equals("-nquery") ||
               arg.equals("-ncomment")) 
            {
                // ignore
            } else if(arg.equals("-cfile") || arg.equals("-comment")) {
                i++; // skip the next arg
                continue;
            } else if(arg.equals("-reserved")) {
                reserved = true;
            } else if(arg.equals("-unreserved")) {
                reserved = false;
            } else {
                files.add(new File(curPath + File.separator + arg));
            }
        }
        for (File file : files) {
            Repository.getInstance().co(file, reserved);
        }                
    }

    private void processLS(String[] args) {
        boolean directory = false;
        File file = null;
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if(arg.startsWith("-d")) {
                directory = true;
            } else if (!arg.equals("-long")) {
                file = new File(arg);
            }
        }
                
        if(!file.getAbsolutePath().startsWith(vobRoot)) {
            errorStream.setDelegate(
                    new ByteArrayInputStream(
                        ("cleartool: Error: Pathname is not within a VOB: \"" + file.getAbsolutePath() + "\"\n").getBytes()));    
        } else {
            if(!file.exists()) {
                FileEntry entry = Repository.getInstance().getEntry(file);
                if(entry == null) {
                    inputStream.setDelegate(new ByteArrayInputStream(("\n").getBytes()));    
                } else {
                    // XXX could be something else than checkedout?
                    inputStream.setDelegate(
                            new ByteArrayInputStream(
                                ("version                " + 
                                 file.getAbsolutePath() + 
                                 "@@" + SELECTOR_CHECKEDOUT_FROM_MAIN + entry.getVersion() + 
                                 " [checkedout but removed]\n").getBytes()));    
                }                
            } else {
                if(!directory && file.isDirectory()) {
                    File[] files = file.listFiles();                    
                    if(files == null) {
                        inputStream.setDelegate(new ByteArrayInputStream(("\n").getBytes()));    
                    } else {
                        for (File f : files) {       
                            slowDown(10);
                            inputStream.setDelegate(new ByteArrayInputStream(lsFile(f).toString().getBytes()));        
                        }                        
                    }                    
                } else {
                    inputStream.setDelegate(new ByteArrayInputStream(lsFile(file).toString().getBytes()));    
                }                
            }            
        }               
    }

    private StringBuffer lsFile(File file) {
        StringBuffer sb = new StringBuffer();
        FileEntry fe = Repository.getInstance().getEntry(file);
        if(fe == null) {
            sb.append("view private object    ");
            sb.append(file.getAbsolutePath());
            sb.append('\n');    
        } else {                                
            sb.append("version                ");
            sb.append(file.getAbsolutePath());
            sb.append("@@");
            sb.append(fe.isCheckedout() ? SELECTOR_CHECKEDOUT_FROM_MAIN : SELECTOR_MAIN);
            sb.append(fe.getVersion());
            if(file.isFile() && file.canWrite() && !fe.isCheckedout()) {
                sb.append("[hijacked]");
            }
            sb.append("                     " + RULE);                                
            sb.append('\n');                                                    
        }
        return sb;
    }   
    
    private void processLSCO(String[] args) {
        boolean directory = false;
        File file = null;
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if(arg.startsWith("-d")) {
                directory = true;
            } else if (arg.equals("-fmt") || arg.equals("-me") || arg.equals("-cview")) {
                i++;
                continue;
            } else {
                file = new File(arg);
            }
        }

        if(!file.getAbsolutePath().startsWith(vobRoot)) {
            errorStream.setDelegate(
                    new ByteArrayInputStream(
                        ("cleartool: Error: Pathname is not within a VOB: \"" + file.getAbsolutePath() + "\"\n").getBytes()));    
        } else {
            if(!file.exists()) {
                FileEntry entry = Repository.getInstance().getEntry(file);
                if(entry == null) {
                    inputStream.setDelegate(new ByteArrayInputStream(("\n").getBytes()));    
                } 
            } else {
                StringBuffer sb = new StringBuffer();
                sb.append(lscoFile(file));
                
                if(!directory && file.isDirectory()) {                    
                    File[] files = file.listFiles();
                    if(files != null) {
                        for (File f : files) {
                            slowDown(10);
                            sb.append(lscoFile(f));                               
                        }
                    }                    
                }                
                if(sb.length() > 0) {
                    inputStream.setDelegate(new ByteArrayInputStream(sb.toString().getBytes()));            
                } else {
                    inputStream.setDelegate(new ByteArrayInputStream("\n".getBytes()));    
                }                
            }            
        }
    }

    private StringBuffer lscoFile(File file) {
        StringBuffer sb = new StringBuffer();
        FileEntry fe = Repository.getInstance().getEntry(file);
        if(fe != null && fe.isCheckedout()) {
            sb.append(file.getAbsolutePath());
            sb.append("<~=~>amigo<~=~>");
            sb.append(fe.isReserved() ? "reserved\n" : "unreserved\n");    
        }
        return sb;
    }

    private void processLSTYPE(String[] args) {
        String kind = null;
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if(arg.equals("-short")) {              
                // ignore
            } else if(arg.equals("-kind")) {
                kind = args[++i];
            } 
        }
        if(kind != null && kind.equals("lbtype")) {
            inputStream.setDelegate(new ByteArrayInputStream("BORING\nSAD\nLIFE\n".getBytes()));            
        } else {
            /*
            switch(kind) {
                case Attribute: arguments.add("attype"); break;
                case Branch:    arguments.add("brtype"); break;
                case Element:   arguments.add("eltype"); break;
                case Hyperlink: arguments.add("hltype"); break;
                case Label:     arguments.add("lbtype"); break;
                case Trigger:   arguments.add("trtype"); break;          
            }
            */
            inputStream.setDelegate(new ByteArrayInputStream("\n".getBytes()));            
        }
    }

    private void processMV(String[] args) {
        
        List<File> files = new ArrayList<File>();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if(arg.equals("-nc")) {
                // ignore
            } else {
                files.add(new File(curPath + File.separator + arg));
            }
        }
        File from = files.get(0);
        File to = files.get(1);
        
        File toParent = to.getParentFile();
        FileEntry entry = Repository.getInstance().getEntry(toParent);
        if(entry == null) {
            errorStream.setDelegate(
                    new ByteArrayInputStream(
                        ("cleartool: Error: Not a vob object: \"" + toParent.getAbsolutePath() + "\"\n").getBytes()));    
            return;
        }
        
        from.renameTo(to);
        
        // XXX not sure if this is the way clearcase does. 
        Repository.getInstance().removeEntry(from);
        Repository.getInstance().add(to, false);   
        
    }
    
    private void processMkElem(String[] args) {
        boolean checkin = false;
        List<File> files = new ArrayList<File>();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if(arg.equals("-ptime") || 
               arg.equals("-mkpath") ||
               arg.equals("-nco") ||
               arg.equals("-ncomment")) 
            {
                // ignore
            } else if(arg.equals("-cfile") || arg.equals("-comment")) {
                i++; // skip the next arg
                continue;
            } else if(arg.equals("-ci")) {
                checkin = true;
            } else {
                files.add(new File(curPath + File.separator + arg));
            }
        }        
        for (File file : files) {
            Repository.getInstance().add(file, checkin);
        }            
    }

    private void processRESERVE(String[] args, boolean value) {
        List<File> files = new ArrayList<File>();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if(arg.equals("-ncomment")) {
                // ignore
            } else if(arg.equals("-cfile") || arg.equals("-comment")) {
                i++; // skip the next arg
                continue;
            } else {
                files.add(new File(curPath + File.separator + arg));
            }
        }
        for (File file : files) {
            Repository.getInstance().reserve(file, value);
        }        
    }                

    private void processRM(String[] args) {
        List<File> files = new ArrayList<File>();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if(arg.equals("-force")) {
                // ignore
            } else {
                files.add(new File(curPath + File.separator + arg));
            }
        }                
        for (File file : files) {
            file.delete();
            Repository.getInstance().removeEntry(file);
        }
    }

    private void processGET(String[] args) {
        String destination = "";
        String revisionSpec = "";
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if(arg.equals("-to")) {                
                destination = args[++i];
                revisionSpec = args[++i];
            } 
        }     
        String[] revisionSpecArray = revisionSpec.split("@@") ;
        File file = new File(revisionSpecArray[0]);
        
        int idx = revisionSpecArray[1].lastIndexOf(File.separator);
        long version = Long.parseLong(revisionSpecArray[1].substring(idx + 1));
        
        FileEntry fe = Repository.getInstance().getEntry(file);        
        try {            
            Utils.copyStreamsCloseAll(new FileOutputStream(new File(destination)), new FileInputStream(fe.getVersions().get((int) version)));
        } catch (IOException ex) {
            CleartoolMockup.LOG.log(Level.WARNING, null, ex);
        }
    }
    
    private void processUNCO(String[] args) {
        boolean keep = false;
        List<File> files = new ArrayList<File>();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if(arg.equals("-rm")) {
                // ignore
            } else if(arg.equals("-keep")) {
                keep = true;
            } else {
                files.add(new File(curPath + File.separator + arg));
            }
        }                
        for (File file : files) {
            Repository.getInstance().unco(file);
            if(keep && file.isFile()) {
                try {
                    Utils.copyStreamsCloseAll(new FileOutputStream(new File(file.getAbsolutePath() + ".keep")), new FileInputStream(file));
                } catch (IOException ex) {
                    CleartoolMockup.LOG.log(Level.WARNING, null, ex);
                } 
            }
        }
    }

    private void processANNOTATE(String[] args) {
        String destination = "";
        List<File> files = new ArrayList<File>();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if(arg.equals("-nco") ||
               arg.equals("-nheader") ||
               arg.equals("-force")) {
                // ignore
            } else if(arg.equals("-out")) {
                destination = args[++i];
            } else {
                files.add(new File(curPath + File.separator + arg));
            }
        }
        File file = files.get(0);
        BufferedReader br = null;
        FileWriter fw = null;
        try {
            br = new BufferedReader(new FileReader(file));
            fw = new FileWriter(new File(destination));
            
            FileEntry fe = Repository.getInstance().getEntry(file);
            int max = -1;
            Random g = null;
            if(fe != null) {
                max = (int) fe.getVersion();
                g = new Random();
            }
            
            String line = null;
            StringBuffer sb = new StringBuffer();
            while((line = br.readLine()) != null) {
                sb.append("####  2008-04-01 Arnold    ");
                sb.append(File.separator);
                sb.append("main");
                sb.append(File.separator);
                sb.append(g != null ? g.nextInt(max) : 1);
                sb.append("              | ");
                sb.append(line);                
                sb.append('\n');                
            }            
            fw.write(sb.substring(0, sb.length() - 1));
            fw.flush();
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
        } finally {
            if(br != null) try { br.close(); } catch (Exception e) {  }
            if(fw != null) try { fw.close(); } catch (Exception e) {  }
        }        
    }
    
    private void processUnsupported(String[] args) {
        NotifyDescriptor nd = 
                new NotifyDescriptor(
                    "You are running with the mockup cleartool. Deal with it!", 
                    "Hey!", 
                    NotifyDescriptor.DEFAULT_OPTION, 
                    NotifyDescriptor.WARNING_MESSAGE, 
                    new Object[]{ NotifyDescriptor.OK_OPTION }, 
                    null);        
        DialogDisplayer.getDefault().notify(nd);
    }

    private void slowDown(long l) {
        try {
            Thread.sleep(l);    // this is so slow ...
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
