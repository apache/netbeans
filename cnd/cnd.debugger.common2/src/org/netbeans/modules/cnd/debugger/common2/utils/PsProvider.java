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

package org.netbeans.modules.cnd.debugger.common2.utils;

import java.util.Vector;
import java.util.regex.Pattern;
import java.util.logging.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.openide.ErrorManager;


import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetUtils;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessInfo;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessInfoDescriptor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Exceptions;
import org.openide.util.Pair;


/**
 * A service for accessing 'ps'.
 *
 * <pre>
 * Typical usage:
 * 	PsProvider psProvider = PsProvider.getDefault(host);
 * 	jtableModel.setDataVector(psData.processes(filter), psData.header());
 * </pre>
 */

public abstract class PsProvider {

    private static boolean DISABLE_PARGS = Boolean.getBoolean("attach.pargs.disable"); //NOI18N


    private static final Logger logger =
	Logger.getLogger(PsProvider.class.getName());
    
    
    private static String whitespace_chars = "" /* dummy empty string for homogeneity */
            + "\\u0009" // CHARACTER TABULATION //NOI18N
            + "\\u000A" // LINE FEED (LF) //NOI18N
            + "\\u000B" // LINE TABULATION //NOI18N
            + "\\u000C" // FORM FEED (FF) //NOI18N
            + "\\u000D" // CARRIAGE RETURN (CR) //NOI18N
            + "\\u0020" // SPACE //NOI18N
            + "\\u0085" // NEXT LINE (NEL)  //NOI18N
            + "\\u00A0" // NO-BREAK SPACE //NOI18N
            + "\\u1680" // OGHAM SPACE MARK //NOI18N
            + "\\u180E" // MONGOLIAN VOWEL SEPARATOR //NOI18N
            + "\\u2000" // EN QUAD  //NOI18N
            + "\\u2001" // EM QUAD  //NOI18N
            + "\\u2002" // EN SPACE //NOI18N
            + "\\u2003" // EM SPACE //NOI18N
            + "\\u2004" // THREE-PER-EM SPACE //NOI18N
            + "\\u2005" // FOUR-PER-EM SPACE //NOI18N
            + "\\u2006" // SIX-PER-EM SPACE //NOI18N
            + "\\u2007" // FIGURE SPACE //NOI18N
            + "\\u2008" // PUNCTUATION SPACE //NOI18N
            + "\\u2009" // THIN SPACE //NOI18N
            + "\\u200A" // HAIR SPACE //NOI18N
            + "\\u2028" // LINE SEPARATOR //NOI18N
            + "\\u2029" // PARAGRAPH SEPARATOR //NOI18N
            + "\\u202F" // NARROW NO-BREAK SPACE //NOI18N
            + "\\u205F" // MEDIUM MATHEMATICAL SPACE //NOI18N
            + "\\u3000" // IDEOGRAPHIC SPACE //NOI18N
            ;
    /* A \s that actually works for Java’s native character set: Unicode */
    private static String whitespace_charclass = "[" + whitespace_chars + "]"; //NOI18N
    /* A \S that actually works for  Java’s native character set: Unicode */
    private static String not_whitespace_charclass = "[^" + whitespace_chars + "]"; //NOI18N
    
    
    private static String escapeString(String line) {
          return line.replaceAll("[\\<\\(\\[\\{\\\\\\^\\-\\=\\$\\!\\|\\]\\}\\)‌​\\?\\*\\+\\.\\>]", "\\\\$0");//NOI18N
     }    
    
    public final class PsData {

	//private Vector<Vector<String>> processes = new Vector<Vector<String>>();
        //TODO: use process list, UI can be changed
       // ProcessList processList;
        private final List<ProcessInfo> processes = new ArrayList<>();
        
        private  final List<ProcessInfoDescriptor> descriptors ;

	private Vector<String> header  = new Vector<>();

        public PsData() {
            this.descriptors = PsProvider.this.descriptors;
            header = new Vector<>();
            for (ProcessInfoDescriptor descriptor : descriptors) {
                if (descriptor.isUserVisible) {
                    header.add(descriptor.header);
                }
            }            
        }
        
        

	/**
	 * Translated header names in the table
	 */
	public Vector<String> header() {
	    return header;
	}
                
        public int commandColumnIdx() {
            return commandColumnIndex();        
        }
        
        public int pidColumnIdx() {
            return pidColumnIndex();
        }
        
        public FileMapper getFileMapper() {
            return PsProvider.this.getFileMapper();
        }

        
        public Collection<ProcessInfo> getProcessesInfo() {
            return processes;
        }
        
	/**
	 * filter lines and convert to columns
	 */
	public Vector<Vector<String>> processes(Pattern re) {
            Vector<Vector<String>> res = new Vector<Vector<String>>();
            List<ProcessInfo> result = new ArrayList<>();
            // Do filtering
            for (ProcessInfo proc : processes) {
                if (re.matcher(proc.getPID().toString()).find()) {
                    result.add(proc);
                    continue;
                }
                
                for (ProcessInfoDescriptor d : descriptors) {
                    Object data = proc.get(d.id, d.type);
                    if (data != null && re.matcher(data.toString()).find()) {
                        result.add(proc);
                        continue;
                    }
                }
            }
            for (ProcessInfo proc : result) {
               Vector<String> row = new Vector<>(); 
                for (ProcessInfoDescriptor d : descriptors) {
                    if (!d.isUserVisible) {
                        //skip
                        continue;
                    }
                    Object data = proc.get(d.id, d.type);
                    row.add(data == null ? "" : data + "");//NOI18N
                }   
                res.add(row);
            }
            return res;
	}
        
        void addProcess(String line) {
            addProcess(line, true);
        }
        
  
        void addProcess(String line, boolean allProcesses) {

            Collection<Pair<ProcessInfoDescriptor, String>> filters = allProcesses ? 
                Collections.<Pair<ProcessInfoDescriptor, String>>emptyList(): filterOutAll();
            List<Object> info = new ArrayList<>();
            String spacePattern = whitespace_charclass + "+";//NOI18N          
            final String output = line.substring(firstPosition()).trim();
            String[] vals =  output.split(spacePattern); // NOI18N 
            String escapedString = escapeString(line);
            String[] escapedValues =  escapedString.substring(firstPosition()).trim().split(spacePattern);
            final int length = descriptors.size();
            int valuesIdx = 0;
            String pid = null;
            for (int columnIdx = 0; columnIdx < length; columnIdx++) {
                final ProcessInfoDescriptor descriptor = descriptors.get(columnIdx);
                //see bz#271185
                if (vals.length == valuesIdx && ProcessInfoDescriptor.COMMAND_COLUMN_ID.equals(descriptor.id)) {
                    //no value for command
                    //sova     23486     1 июл28 
                    //skip?
                    String s = "";
                    info.add("");//an empty string
                    if (Log.Ps.debug) {
                        System.out.println("----------"); //NOI18N
                        System.out.println("column=" + descriptor.header); //NOI18N
                        System.out.println("cx=" + columnIdx); //NOI18N
                        System.out.println("s=_" + s + "_"); //NOI18N
                    }
                    break;
                }
                String s = vals[valuesIdx];
                boolean increaseAndNext = false;
                //in case previous descriptor was a STIME and on Windows there can be a space
                if (ProcessInfoDescriptor.STIME_WINDOWS_COLUMN_ID.equals(descriptor.id)) {
                    //there can be ":" or Space 
                    //in case there is no comma we need to append next symbol as it will be "Oct 16"
                    if (!s.contains(":") && valuesIdx + 1 <  vals.length  ){ //NOI18N
                        s += " " + vals[valuesIdx + 1];//NOI18N     
                        increaseAndNext = true;
                    }
                }
                if (!increaseAndNext) {
                    if (columnIdx == length - 1 && vals.length > length) {
                        StringBuilder begining = new StringBuilder("(");//NOI18N

                        begining.append(spacePattern);
                        for (int j = 0; j < valuesIdx; j++) {
                            begining.append(escapedValues[j]).append(spacePattern);
                        }
                        begining.append(")((.*))");//NOI18N
                        if (Log.Ps.debug) {
                            System.out.println("pattern = _" + begining.toString() + "_");//NOI18N
                        }
                        Pattern p = Pattern.compile(begining.toString());
                        //need to start with space to match pattern " +" from the begining
                        Matcher matcher = p.matcher(" " + output);//NOI18N
                        if (matcher.matches()) {
                            s = matcher.group(2);
                        } else {
                            if (Log.Ps.debug) {
                                System.out.println("something went wrong, will concatanate tail");//NOI18N
                            }
                            //concat to the end with space
                            for (int i = valuesIdx + 1; i < vals.length; i++) {
                                s += " " + vals[i];//NOI18N
                            }  
                        }

                    }                 
                }
                if (Log.Ps.debug) {
                    System.out.println("----------"); //NOI18N
                    System.out.println("column=" + descriptor.header); //NOI18N
                    System.out.println("cx=" + columnIdx); //NOI18N
                    System.out.println("s=_" + s + "_"); //NOI18N
                }
                if (increaseAndNext) {
                    valuesIdx++;
                }
                valuesIdx++;
                if (ProcessInfoDescriptor.PID_COLUMN_ID.equals(descriptor.id)) {
                    pid = s.trim();
                }
                info.add(s.trim());
            }
            String executable = getExecutable(pid);
            final ProcessInfo processInfo = ProcessInfo.create(descriptors, info, executable); 
            boolean isIncluded = true;
            //check filter
            for (Pair<ProcessInfoDescriptor, String> filter : filters) {
                if (!processInfo.equals(filter.first().id, filter.second())) {
                    isIncluded = false;
                    break;
                }
            }
            if (isIncluded) {
                processes.add(processInfo);
            }
        }

        private void updateCommand(String pid, String command) {
            for (ProcessInfo proc : processes) {
                if (pid.equals(proc.getPID() + "")) {
                    proc.updateInfo(ProcessInfoDescriptor.COMMAND_COLUMN_ID, command);
                }
            }
        }

    }

//    // can't be static, for format of ps output is different from host to host
//    private Vector<String> parsedHeader = null;

    protected static final String zero = "0";	// NOI18N
    private String uid = null;


    
    //package visibility for test purpose
    /*package*/  static <T> ProcessInfoDescriptor descriptor(Class<T> c, boolean isUserVisible,
            String id, String command, String header) {
        return new ProcessInfoDescriptor(isUserVisible, id, command, c,
                header, 
                isUserVisible ? Catalog.get("PS_HDR_" + header) : header);//NOI18N
                //loc("PsProvider." + id + ".desc")); // NOI18N
    }    
       

    /**
     * Specialization of PsProvider for Solaris
     */
    static class SolarisPsProvider extends PsProvider {
 
        private final String[] solarisPsFormat;
        
        public SolarisPsProvider(ExecutionEnvironment execEnv, List<ProcessInfoDescriptor> descriptors) {
            super(execEnv, descriptors);
            solarisPsFormat = new String[2];//we add -o first
            solarisPsFormat[0] = "-o";//NOI18N
            StringBuilder psCommandBuilder = new StringBuilder();
            for (int i = 0; i < descriptors.size(); i++) {                
                psCommandBuilder.append(descriptors.get(i).command);
                if (i < descriptors.size() - 1) {
                    psCommandBuilder.append(","); //NOI18N
                } else {
                    psCommandBuilder.append(" "); //NOI18N
                }
            };            
            solarisPsFormat[1] = psCommandBuilder.toString();            
        }

	public SolarisPsProvider(ExecutionEnvironment execEnv) {
	    this(execEnv, 
                    Arrays.<ProcessInfoDescriptor>asList(
                        descriptor(String.class, true, ProcessInfoDescriptor.UID_COLUMN_ID, "user", "USER"), // NOI18N
                        descriptor(String.class, true, ZONE_COLUMN_ID, "zone", "ZONE"), // NOI18N
                        descriptor(String.class, true,  ProcessInfoDescriptor.PID_COLUMN_ID, "pid", "PID"), // NOI18N
                        descriptor(String.class, true, ProcessInfoDescriptor.PPID_COLUMN_ID, "ppid", "PPID"), // NOI18N
                        descriptor(String.class, true, ProcessInfoDescriptor.STIME_COLUMN_ID, "stime", "STIME"), // NOI18N
                        descriptor(String.class, true, ProcessInfoDescriptor.COMMAND_COLUMN_ID, "comm", "COMMAND") // NOI18N
                    )
            );
            
	}
              
        private static final String ZONE_COLUMN_ID = "zone"; //NOI18N
        
        @Override
        protected boolean showAllProcesses() {
            return false;
        }

        @Override
	protected String uidCommand() {
	    return "/usr/xpg4/bin/id -u";	// NOI18N
	}

        @Override
        protected String psExecutable() {
            return "/usr/bin/ps";//NOI18N
        }

        @Override
        protected String[] psExecutableArgs(String uid) {
	    // SHOULD set LC_ALL=C here since we're depending
	    // on column widths to get to the individual ps items!
            // (moved to getData)

	    if (Log.Ps.null_uid) 
		uid = null;

	    if ( (uid == null) || (uid.equals(zero)) ) {
		// uid=0 => root; use ps -ef
                List<String> res = new ArrayList<>();
                res.add("-e");//NOI18N
                res.addAll(Arrays.asList(solarisPsFormat));
		return res.toArray(new String[0]);
                //return "/usr/bin/ps -e " + solarisPsFormat;	// NOI18N
	    } else {
                 //+ " -z `zonename`";	// NOI18N
                List<String> res = new ArrayList<>();                
                res.addAll(Arrays.asList(solarisPsFormat));
                res.add("-u");//NOI18N
                res.add(uid);
                //return "/usr/bin/ps " + solarisPsFormat + " -u " + uid ;//NOI18N
		return res.toArray(new String[0]);
	    }
        }
        
        private String zone = null;
        
        private String getZonename() {
            if (zone == null) {
                try {
                    NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(exEnv);
                    npb.setCommandLine("zonename");//NOI18N
                    ProcessUtils.ExitStatus status = ProcessUtils.execute(npb);
                    String res = status.getOutputString();
                    int exitCode = status.exitCode;
                    if (exitCode != 0) {
                        String msg = "zonename command failed with " + exitCode; // NOI18N
                        logger.log(Level.WARNING, msg);
                        return "global";//NOI18N
                    }
                    if (!res.isEmpty()) {
                        zone = res;
                    } else {
                        zone = "global";//NOI18N
                    }
                } catch (Exception e) {
                    ErrorManager.getDefault().annotate(e, "Failed to parse OutputStream of zonename command"); // NOI18N
                    ErrorManager.getDefault().notify(e);
                }
            }
            return zone;
        }   

        @Override
        protected Collection<Pair<ProcessInfoDescriptor, String>> filterOutAll() {
            String zonename = getZonename();
            return Collections.singleton(Pair.of(
                    descriptor(String.class, true, ZONE_COLUMN_ID, "zone", "ZONE"),//NOI18N
                    zonename));
        }
        
        

        @Override
        public PsData getData(boolean allProcesses) {
            PsData res = super.getData(allProcesses);

            // pargs call if needed
            if (res != null && !DISABLE_PARGS && !res.processes.isEmpty()) {
                NativeProcessBuilder pargsBuilder = NativeProcessBuilder.newProcessBuilder(exEnv);
                pargsBuilder.setExecutable("/usr/bin/pargs").redirectError(); // NOI18N
                pargsBuilder.getEnvironment().put("LC_ALL", "C"); // NOI18N
                String[] pargs_args = new String[res.processes.size()+1];
                pargs_args[0] = "-Fl"; // NOI18N
                int idx = 1;
                for (ProcessInfo proc : res.processes) {
                    pargs_args[idx++] = proc.getPID() + "";
                }
                pargsBuilder.setArguments(pargs_args);

                ProcessUtils.ExitStatus status = ProcessUtils.execute(pargsBuilder);
                List<String> pargsOutput = status.getOutputLines();
                updatePargsData(res, pargs_args, pargsOutput);
            }
            
            return res;
        }

    }
    
    static void updatePargsData(PsData res, String[] pargs_args, List<String> pargsOutput) {
        int idx = 1;
        final Map<String, String> updateTable = new HashMap();
        for (String procArgs : pargsOutput) {
            if (procArgs.isEmpty() ||
                    procArgs.startsWith("pargs: Warning") || // NOI18N
                    procArgs.startsWith("pargs: Couldn't determine locale of target process") || // NOI18N
                    procArgs.startsWith("pargs: Some strings may not be displayed properly")) { // NOI18N
                continue;
            }
            if (!procArgs.startsWith("pargs:")) { // NOI18N
                updateTable.put(pargs_args[idx], procArgs);
            }
            idx++;
        }
        if ( (idx-1) != res.processes.size()) {     // we should check if the operation has been applied to all processes
            logger.info("Process list:" + res.processes.toString() + "\npargs output:" + pargsOutput.toString()); // NOI18N
            DISABLE_PARGS = true;
            throw new AssertionError("PsProvider failed to match pargs output with ps output");
        } else {
            for (Map.Entry<String, String> entry : updateTable.entrySet()) {
                res.updateCommand(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Specialization of PsProvider for Linux
     */
    static class LinuxPsProvider extends PsProvider {

        private final String[] linuxPsFormat;
        public LinuxPsProvider(ExecutionEnvironment execEnv, List<ProcessInfoDescriptor> descriptors) {
            super(execEnv, descriptors);
            linuxPsFormat = new String[2];
            linuxPsFormat[0] = "-o";//NOI18N
            StringBuilder psCommandBuilder = new StringBuilder();
            for (int i = 0; i < descriptors.size(); i++) {
                psCommandBuilder.append(descriptors.get(i).command);
                if (i < descriptors.size() - 1) {
                    psCommandBuilder.append(","); //NOI18N
                } else {
                    psCommandBuilder.append(" "); //NOI18N
                }
            };            
            linuxPsFormat[1] = psCommandBuilder.toString();                   
        }

	public LinuxPsProvider(ExecutionEnvironment execEnv) {
            this(execEnv,
                    Arrays.<ProcessInfoDescriptor>asList(
                            descriptor(String.class, true, ProcessInfoDescriptor.UID_COLUMN_ID, "user", "USER"), // NOI18N
                            descriptor(Integer.class, true, ProcessInfoDescriptor.PID_COLUMN_ID, "pid", "PID"), // NOI18N
                            descriptor(Integer.class, true, ProcessInfoDescriptor.PPID_COLUMN_ID, "ppid", "PPID"), // NOI18N
                            descriptor(String.class, true, ProcessInfoDescriptor.STIME_COLUMN_ID, "stime", "STIME"), // NOI18N
                            descriptor(String.class, true, ProcessInfoDescriptor.COMMAND_COLUMN_ID, "cmd", "COMMAND") // NOI18N
                    )
            );     
	}

        @Override
	protected String uidCommand() {
	    return "/usr/bin/id -u";	// NOI18N
	}

        @Override
        protected String psExecutable() {
            return "/bin/ps";//NOI18N
        }

        @Override
        protected String[] psExecutableArgs(String uid) {
            // SHOULD set LC_ALL=C here since we're depending
	    // on column widths to get to the individual ps items!
            // (moved to getData)

	    if (Log.Ps.null_uid) 
		uid = null;

	    if ( (uid == null) || (uid.equals(zero)) ) {
		// uid=0 => root; use ps -ef
		// OLD return "LANG=C /bin/ps -www -o pid,tty,time,cmd";
                List<String> res = new ArrayList<>();
                res.add("-e");//NOI18N
                res.addAll(Arrays.asList(linuxPsFormat));
		return res.toArray(new String[0]);                
//		return "/bin/ps -e " + linuxPsFormat;	// NOI18N
	    } else {
                List<String> res = new ArrayList<>();
                res.addAll(Arrays.asList(linuxPsFormat));
                res.add("-u");//NOI18N
                res.add(uid);
                res.add("--width");//NOI18N
                res.add("1024");//NOI18N
		return res.toArray(new String[0]);                
		//return "/bin/ps " + linuxPsFormat + " -u " + uid + " --width 1024";		// NOI18N
	    }            
                
        }

        @Override
        protected boolean showAllProcesses() {
            return true;
        }

    }
    
    static class MacOSPsProvider extends LinuxPsProvider {
        private final String macOsPsFormat;
//        private final static String header_str_mac[] = {
//	    "  UID", // NOI18N
//	    "   PID", // NOI18N
//	    "  PPID", // NOI18N
//	    "   C",		// skipped // NOI18N
//	    "STIME", // NOI18N
//	    "TTY     ",		// skipped // NOI18N
//	    "    TIME",         // skipped // NOI18N
//	    "CMD", // NOI18N
//	};

//        @Override
//        public String[] headerStr() {
//            return header_str_mac;
//        }
        
        public MacOSPsProvider(ExecutionEnvironment execEnv, List<ProcessInfoDescriptor> descriptors) {
            super(execEnv, descriptors);
            StringBuilder psCommandBuilder = new StringBuilder();
            for (int i = 0; i < descriptors.size(); i++) {
                if (i == 0) {
                    psCommandBuilder.append(" -o "); //NOI18N
                }
                psCommandBuilder.append(descriptors.get(i).command);
                if (i < descriptors.size() - 1) {
                    psCommandBuilder.append(","); //NOI18N
                } else {
                    psCommandBuilder.append(" "); //NOI18N
                }
            };            
            macOsPsFormat = psCommandBuilder.toString();                
        }
        
        public MacOSPsProvider(ExecutionEnvironment execEnv) {
            this(execEnv, Arrays.<ProcessInfoDescriptor>asList(
                            descriptor(String.class, true, ProcessInfoDescriptor.UID_COLUMN_ID, "user", "USER"), // NOI18N
                            descriptor(Integer.class, true, ProcessInfoDescriptor.PID_COLUMN_ID, "pid", "PID"), // NOI18N
                            descriptor(Integer.class, true, ProcessInfoDescriptor.PPID_COLUMN_ID, "ppid", "PPID"), // NOI18N
                            descriptor(String.class, true, ProcessInfoDescriptor.STIME_COLUMN_ID, "stime", "STIME"), // NOI18N
                            descriptor(String.class, true, ProcessInfoDescriptor.COMMAND_COLUMN_ID, "command", "COMMAND") // NOI18N
                    )
            );
               
        }

        @Override
        protected String[] psExecutableArgs(String uid) {
            if ( (uid == null) || (uid.equals(zero)) ) {
                List<String> res = new ArrayList<>();
                res.add("-e");//NOI18N
                res.addAll(Arrays.asList(macOsPsFormat));
		return res.toArray(new String[0]);                 
//		return "/bin/ps -e " + macOsPsFormat;	// NOI18N
	    } else {
                List<String> res = new ArrayList<>();                
                res.addAll(Arrays.asList(macOsPsFormat));
                res.add("-u");//NOI18N
                res.add(uid);
		return res.toArray(new String[0]);                                 
		//return "/bin/ps " + macOsPsFormat + " -u " + uid;		// NOI18N
	    }
        }                
    }

    /**
     * Specialization of PsProvider for Windows
     */
    static class WindowsPsProvider extends PsProvider {
        private FileMapper fileMapper = FileMapper.getDefault();

//	private final static String header_str_windows[] = {
//	    "PID", // NOI18N
//	    "PPID", // NOI18N
//            "PGID", // NOI18N
//            "WINPID", // NOI18N
//	    "TTY",		// skipped // NOI18N
//            "UID", // NOI18N
//            "STIME", // NOI18N
//	    "COMMAND", // NOI18N
//	};

	public WindowsPsProvider(ExecutionEnvironment execEnv) {
	    super(execEnv,
                    Arrays.<ProcessInfoDescriptor>asList(
                        descriptor(String.class, true,  ProcessInfoDescriptor.PID_COLUMN_ID, "pid", "PID"), // NOI18N
                        descriptor(String.class, true, ProcessInfoDescriptor.PPID_COLUMN_ID, "ppid", "PPID"), // NOI18N
                        descriptor(String.class, true, "pgid", "ppid", "PGID"), // NOI18N
                        descriptor(String.class, true, "winpid", "winpid", "WINPID"), // NOI18N
                        descriptor(String.class, false, "tty", "tty", "TTY"), // NOI18N
                        descriptor(String.class, true, ProcessInfoDescriptor.UID_COLUMN_ID, "uid", "UID"), // NOI18N
                        descriptor(String.class, true, ProcessInfoDescriptor.STIME_WINDOWS_COLUMN_ID, "stime", "STIME"), // NOI18N
                        descriptor(String.class, true, ProcessInfoDescriptor.COMMAND_COLUMN_ID, "cmd", "COMMAND") // NOI18N
                    )

            );
	}

        @Override
        protected boolean showAllProcesses() {
            return true;
        }
              
        // see IZ 193741 - skip status column
        @Override
        protected int firstPosition() {
            return 1;
        }


        @Override
	protected String uidCommand() {
	    return getUtilityPath("id") + " -u";	// NOI18N
	}

        @Override
        protected String psExecutable() {
            return getUtilityPath("ps");//NOI18N
        }

        @Override
        protected String[] psExecutableArgs(String uid) {
//	    // SHOULD set LC_ALL=C here since we're depending
//	    // on column widths to get to the individual ps items!
//            // (moved to getData)
//
	    if (Log.Ps.null_uid)
		uid = null;

            // Always show all processes on Windows (-W option), see IZ 193743
	    if ( (uid == null) || (uid.equals(zero)) ) {
		// uid=0 => root; use ps -ef
                //return getUtilityPath("ps") + " -W";	// NOI18N
		return new String[]{"-W"};	// NOI18N
	    } else {
                //return getUtilityPath("ps") + " -u " + uid + " -W";	// NOI18N
		return new String[]{"-u",uid + "","-W"};	// NOI18N
	    }
        }

        private String getUtilityPath(String util) {
            File file = new File(CompilerSetUtils.getCygwinBase() + "/bin", util + ".exe"); // NOI18N
            if (file.exists()) {
                fileMapper = FileMapper.getByType(FileMapper.Type.CYGWIN);
            } else {
                fileMapper = FileMapper.getByType(FileMapper.Type.MSYS);
                file = new File(CompilerSetUtils.getCommandFolder(null), util + ".exe"); // NOI18N
            }
            if (file.exists()) {
                return file.getAbsolutePath();
            }
            return util;
        }

        @Override
        public FileMapper getFileMapper() {
            return fileMapper;
        }

    }
    
    public static synchronized PsProvider getDefault(ExecutionEnvironment exEnv) {
        PsProvider psProvider = null;
        if (!ConnectionManager.getInstance().connect(exEnv)) {
            return null;
        }
        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(exEnv);
            switch (hostInfo.getOSFamily()) {
                case LINUX:
                    psProvider = new LinuxPsProvider(exEnv);
                    break;
                case WINDOWS:
                    psProvider = new WindowsPsProvider(exEnv);
                    break;
                case MACOSX:
                case FREEBSD:
                    psProvider = new MacOSPsProvider(exEnv);
                    break;
                case SUNOS:
                    psProvider = new SolarisPsProvider(exEnv);
                    break;
                case UNKNOWN:
                default:
                    psProvider = new SolarisPsProvider(exEnv);
            }
        } catch (CancellationException e) {
            // user cancelled connection attempt
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return psProvider;
    }
    
    public static synchronized PsProvider getDefault(Host host) {
        PsProvider psProvider = host.getResource(PsProvider.class);
        if (psProvider == null) {
            ExecutionEnvironment exEnv = host.executionEnvironment();
            psProvider = getDefault(exEnv);
            host.putResource(PsProvider.class, psProvider);
        }
        return psProvider;
    }
    
    protected Collection<Pair<ProcessInfoDescriptor, String>> filterOutAll() {
        return Collections.emptyList();
    }
    
    protected abstract boolean showAllProcesses();
    /**
     * Return index of the CMD column.
     * @return 
     */
    protected  int commandColumnIndex() {
        int idx = 0;
        for (ProcessInfoDescriptor descriptor : descriptors) {
            if (ProcessInfoDescriptor.COMMAND_COLUMN_ID.equals(descriptor.id)) {
                return idx;
            }
            if (descriptor.isUserVisible) {
                idx++;
            }
        }
        return -1;       
    }
    
    protected  int pidColumnIndex() {
        int idx = 0;
        for (ProcessInfoDescriptor descriptor : descriptors) {
            if (ProcessInfoDescriptor.PID_COLUMN_ID.equals(descriptor.id)) {
                return idx;
            }
            if (descriptor.isUserVisible) {
                idx++;
            }
        }
        return -1;       
    }    


    protected abstract String psExecutable();

    protected abstract String[] psExecutableArgs(String root);


    protected abstract String uidCommand(); // for Runtime.exe
    
    protected int firstPosition() {
        return 0;
    }
    
    // return file mapper (important only on Windows)
    public FileMapper getFileMapper() {
        return FileMapper.getDefault();
    }
    
    protected final ExecutionEnvironment exEnv;
    protected final List<ProcessInfoDescriptor> descriptors;

    private PsProvider(ExecutionEnvironment execEnv, List<ProcessInfoDescriptor> descriptors) {
        exEnv = execEnv;
        this.descriptors = descriptors;
    }
        
    
    public List<ProcessInfoDescriptor> getDescriptors() {
        return descriptors;
    }
    
    private String getExecutable(String pid) {
        return null;
        //use proc to read executable path for Linux, Solaris , what to do for Windows have no idea 
//        
//        try {
//            if (pid == null) {
//                return null;
//            }
//            final String psExecutableCommand = psExecutableCommand(pid);
//            if (psExecutableCommand == null) {
//                return null;
//            }
//            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(exEnv);
//            
//            npb.setCommandLine(psExecutableCommand);
//            ProcessUtils.ExitStatus status = ProcessUtils.execute(npb);
//            String res = status.getOutputString();
//            int exitCode = status.exitCode;
//            if (exitCode != 0) {
//                String msg = "ps command failed with " + exitCode; // NOI18N
//                logger.log(Level.WARNING, msg);
//                return null;
//            }
//            if (!res.isEmpty()) {
//               return res;
//            } 
//        } catch (Exception e) {
//            ErrorManager.getDefault().annotate(e, "Failed to parse OutputStream of ps  command"); // NOI18N
//            ErrorManager.getDefault().notify(e);
//        }
//        return null;
    }
    
    // "host" for getUid is usually "localhost"
    private String getUid() {
        if (uid == null) {
            try {
                NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(exEnv);
                npb.setCommandLine(uidCommand());
                ProcessUtils.ExitStatus status = ProcessUtils.execute(npb);
                String res = status.getOutputString();
                int exitCode = status.exitCode;
                if (exitCode != 0) {
                    String msg = "id command failed with " + exitCode; // NOI18N
                    logger.log(Level.WARNING, msg);
                    return exEnv.getUser();
                }
                if (!res.isEmpty()) {
                    uid = res;
                } else {
                    uid = exEnv.getUser();
                }
            } catch (Exception e) {
                ErrorManager.getDefault().annotate(e, "Failed to parse OutputStream of uid command"); // NOI18N
                ErrorManager.getDefault().notify(e);
            }
        }
        return uid;
    }


    /**
     * Return a Vector of headers based on the first line emitted by 'ps' and
     * populate 'fields' as a side-effect.
     *
     * First lines look like this on solaris:
     *
     *	|     UID   PID  PPID  C    STIME TTY      TIME CMD
     *	|    ivan  1501  1483  0   Nov 26 console  0:07 xterm -name edit-left
     *
     * and like this on linux:
     *
     *	|UID        PID  PPID  C STIME TTY          TIME CMD
     *	|ivan     11585 11583  0 12:39 pts/2    00:00:00 -csh
     *
     * Field justifications are as follows:
     *
     * field	solaris		linux
     * ------------------------------
     * UID	right		left
     * PID	right		right
     * PPID	right		right
     * C	1 character	1 
     * STIME	right		left
     * TTY	left		left
     * TIME	right		right
     * CMD	left		left
     *
     * The current column boundry determination is as follows: for solaris
     *|------------------------------------------------
     *|     UID   PID  PPID  C    STIME TTY      TIME CMD
     *|0      01    12    23 34       45      56    67  7
     *|------------------------------------------------
     * ... and linux ...:
     *|------------------------------------------------
     *|UID        PID  PPID  C STIME TTY          TIME CMD
     *|0      01    12    23 34    45     56         67  7
     *|------------------------------------------------
     *
     * The left side of left-aligned columns is one column too much to the
     * left (STIME, C, TTY and CMD). This is no problem as long as the left
     * edge of this columns is next to a right-aligned column. It
     * should just be a space and get eaten up by 'trim'.
     * TTY on linux is the only one which doesn't abide by this. But I'm going
     * to wing it for now and postpone making this column discovery even more
     * involved.
     */
//    void parseHeader(String str) {
//
//	/* OLD 
//	// parsedHeader is static so we only do this once
//	if (parsedHeader != null)
//	    return parsedHeader;
//	*/
//
//        //!!!!all this is done to get fields[][] filled in
//
//	if (Log.Ps.debug) 
//	    System.out.printf("parseHeader: '%s'\n", str); // NOI18N
//
//	//parsedHeader = new Vector<String>(headerStr().length-columnIndexesToExclude().length);
//        int lastSearchIndex = 0;
//	for (int cx = 0; cx < descriptors.size(); cx++) {
//            ProcessInfoDescriptor desciptor = descriptors.get(cx);
//	    String s = null;
//	    int i;
//
//	    i = str.indexOf(desciptor.header, lastSearchIndex); 
//
//	    // fields[cx][0] the begining of this column
//	    // fields[cx][1] the end of this column
//
//	    if (i >= 0) { // found
//		if (cx == 0) {// first column
//		    fields[cx][0] = firstPosition();
//                }
//		fields[cx][1] = i + desciptor.header.length() - 1;
//	    }
//
//	    if (cx == descriptors.size() -1) {
//		// last one
//		s = str.substring(fields[cx][0]);
//	    } else {
//		s = str.substring(fields[cx][0], fields[cx][1]+1);
//		fields[cx+1][0] = i + desciptor.header.length();
//	    }
//            if (Log.Ps.debug)  {
//                System.out.println("fields : " + fields[cx][0] + " " + fields[cx][1]); // NOI18N
//            }
//            lastSearchIndex = fields[cx][1];
//
//	}
//
//	if (Log.Ps.debug) {
//	    printFields(str);
//        }
//
//    }

    /**
     * Execute a ps command and return the data.
     *
     * Executes a ps command, captures the output, remembers the first line
     * as the 'parsedHeader', stuffs the rest of the lines into 'PsData.lines'.
     * PsData will columnize lines later.
     * @param allProcesses
     * @return 
     */

    public PsData getData(boolean allProcesses) {
	PsData psData = new PsData();
        String luid = allProcesses ? null : getUid();
	try {
            //FIXME
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(exEnv);
            npb.setExecutable(psExecutable());
            npb.setArguments(psExecutableArgs(luid));
            npb.getEnvironment().put("LANG", "C"); //NOI18N
            ProcessUtils.ExitStatus res = ProcessUtils.execute(npb);
	    int lineNo = 0;
            Iterator<String> iterator = res.getOutputLines().iterator();

            // Skip the header ...
            iterator.next();

            while (iterator.hasNext()) {
                String line = iterator.next();
                psData.addProcess(line, allProcesses);

            }

            int exitCode = res.exitCode;
	    if (exitCode != 0) {
		String msg = "ps command failed with " + exitCode; // NOI18N
		logger.log(Level.WARNING, msg);
		return null;
	    }

	} catch (Exception e) {
	    ErrorManager.getDefault().annotate(e, "Failed to parse OutputStream of ps command"); // NOI18N
	    ErrorManager.getDefault().notify(e);
	} 

	if (psData.processes.isEmpty()) {
	    ErrorManager.getDefault().log(ErrorManager.EXCEPTION, 
		"No lines from "); // NOI18N
	}
	
	return psData;
    }
}
