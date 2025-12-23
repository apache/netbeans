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


/*
 * MemoryMeasurement.java
 *
 * Created on August 7, 2003, 4:53 PM
 */

package org.netbeans.junit;
import java.io.*;

/** Class with static methods for measuring memory footprint of a process. Since
 * this class required platform dependent code, there have to be a dll library
 * called 'lib.memory-measurement.win32.dll' present in a home directory of
 * NbJUnit (set by nbjunit.home property in either junit.properties file or
 * present as a system property).
 *
 * Please note, methods available in this class return a system specific values for
 * each supported platforms. The meanings of values are:
 *
 * On Solaris: resident memory size
 * On Linux: VmSize (virtual memory size)
 * On Windows: pagefile usage
 * @author mb115822
 */
public class MemoryMeasurement {
    
    /** only static methods **/
    private MemoryMeasurement() {
    }
    
    /** Name of the system property, which contains PID of running IDE. This property
     * is set by ide executor of XTest framework.
     */    
    public static final String IDE_PID_SYSTEM_PROPERTY = "netbeans.pid";
    
    /** Gets memory footprint of NetBeans IDE. This methods requires system property
     * 'netbeans.pid' to contain PID of the IDE process.
     * @throws MemoryMeasurementFailedException When measurement cannot be performed
     * @return memory size value
     */    
    public static long getIdeMemoryFootPrint() throws MemoryMeasurementFailedException {
        // pid should be stored as netbeans.pid system variabl
        String idePidString = System.getProperty(IDE_PID_SYSTEM_PROPERTY);        
        if (idePidString != null) {
            try {
                //System.out.println("Got idePidString = "+idePidString);
                long idePid = Long.parseLong(idePidString);
                //System.out.println("Got idePId = "+idePid);
                return getProcessMemoryFootPrint(idePid);
            } catch (NumberFormatException nfe) {
                // this should not happen -                 
            }
        }
        // unsuccessfull - throw MemoryMeasurementFailedException
        throw new MemoryMeasurementFailedException("Cannot get IDE PID - obtained value: "+idePidString);
    }
    
    /** Gets memory footprint of a process identified by PID. On each platform this
     * methods returns a platform specific value.
     * @param pid process identification for the process, which size is to be measured
     * @throws MemoryMeasurementFailedException When measurement cannot be performed
     * @return memory size value
     */    
    public static long getProcessMemoryFootPrint(long pid) throws MemoryMeasurementFailedException {
        String platform = getPlatform();
        //System.out.println("PLATFORM = "+getPlatform());
        if (platform.equals(LINUX)) {
            // call unix method
            return getProcessMemoryFootPrintOnUnix(pid);
        } else if (platform.equals(WINDOWS)) {
            // call windows method
            return getProcessMemoryFootPrintOnWindows(pid);
        }
        // unsupported platform - cannot measure memory
        throw new MemoryMeasurementFailedException("Cannot measure memory on unsupported platform "+platform);
    }
    
    
    /** */    
    private static final long UNKNOWN_VALUE = -1;
   
    private static final String LINUX   = "linux";
    private static final String WINDOWS = "win32";
    private static final String UNKNOWN = "unknown";
    
    private static final String [][] SUPPORTED_PLATFORMS = {
        {"Linux",LINUX},
        {"Windows NT",WINDOWS},
        {"Windows 2000",WINDOWS},
        {"Windows XP",WINDOWS}
    };
    
    private static String getPlatform() throws MemoryMeasurementFailedException {
        String osName = System.getProperty("os.name");
        for (int i=0; i < SUPPORTED_PLATFORMS.length; i++) {
            if (SUPPORTED_PLATFORMS[i][0].equalsIgnoreCase(osName)) {
                return SUPPORTED_PLATFORMS[i][1];
            }
        }
        throw new MemoryMeasurementFailedException("MemoryMeasurement does not support this operating system: "+osName);
    }
    
    
    private static long getProcessMemoryFootPrintOnUnix(long pid) throws MemoryMeasurementFailedException {
        try {
            File script = new File(Manager.getNbJUnitHome(),"memory-measurement.unix.sh");
            if (!script.exists()) {
                throw new MemoryMeasurementFailedException("Cannot locate script '"+script.getName()
                    +"', please make sure it is available in nbjunit.home");
            }
            String command="/bin/sh "+script.getAbsolutePath()+" "+pid;            
            //System.out.println("Running command "+command);
            Process process = Runtime.getRuntime().exec(command);
            long obtainedValue = getOutputValue(process);

            //OutputReader outputReader = new MemoryMeasurement.OutputReader(process.getInputStream());
            //System.out.println("Starting thread reader");
            //Thread readerThread = new Thread(outputReader);
            //readerThread.start();
            //process.getOutputStream().close();
            
            process.waitFor();
            
            //long obtainedValue = outputReader.getReadValue();
            
            if (obtainedValue == UNKNOWN_VALUE) {
                // had problem reading value - why
                //throw new MemoryMeasurementFailedException("Memory measurement call failed",outputReader.getCaughtException());
                throw new MemoryMeasurementFailedException("Memory measurement call failed "+obtainedValue);
            } else {
                // everything seem to be correct
                return obtainedValue;
            }
        } catch (IOException ioe) {
            throw new MemoryMeasurementFailedException("MemoryMeasurement failed, reason:"+ioe.getMessage(),ioe);
        } catch (InterruptedException ie) {
            throw new MemoryMeasurementFailedException("MemoryMeasurement failed, reason:"+ie.getMessage(),ie);
        }
    }
    
    // os depednent implementations - not used -> on unix we use the universal script supplied with XTest    
    // linux
    //private static long getProcessMemoryFootPrintOnLinux(long pid) throws MemoryMeasurementFailedException {
        //String command="/bin/sh -c \"cat /proc/"+pid+"status | grep VmSize | sed -e 's/VmSize: *\\t* *//' | sed -e 's/ .*//'\"";
        /*
         try {
            Process process = Runtime.getRuntime().exec(command);
            //return getOutputValue(process);
            return UNKNOWN_VALUE;
        } catch (IOException ioe) {
            throw new MemoryMeasurementFailedException("MemoryMeasurement failed, reason:"+ioe.getMessage(),ioe);
        }
    }
    */
    // solaris
    //private static long getProcessMemoryFootPrintOnSolaris(long pid) throws MemoryMeasurementFailedException {
        //String command="/bin/sh -c \"pmap -x "+pid+" | grep \\^total | sed -e 's/.*Kb *//' | sed -e 's/ .*//'\"";
        //String command="\"pmap -x "+pid+" | grep \\^total | sed -e 's/.*Kb *//' | sed -e 's/ .*//'\"";
        //String command="/bin/short -c \"ls -al\"";
    /*
        try {
            System.out.println("Running command "+command);
            Process process = Runtime.getRuntime().exec(command);
            //long obtainedValue = getOutputValue(process);
            OutputReader outputReader = new MemoryMeasurement.OutputReader(process.getInputStream());
            System.out.println("Starting thread reader");
            Thread readerThread = new Thread(outputReader);
            readerThread.start();
            //process.getOutputStream().close();
            //process.waitFor();
            long obtainedValue = outputReader.getReadValue();
            if (obtainedValue == UNKNOWN_VALUE) {
                // had problem reading value - why
                //throw new MemoryMeasurementFailedException("Memory measurement call failed",outputReader.getCaughtException());
                throw new MemoryMeasurementFailedException("Memory measurement call failed 1welnewgb");
            } else {
                // everything seem to be correct
                return obtainedValue;
            }
        } catch (IOException ioe) {
            throw new MemoryMeasurementFailedException("MemoryMeasurement failed, reason:"+ioe.getMessage(),ioe);
        } catch (InterruptedException ie) {
            throw new MemoryMeasurementFailedException("MemoryMeasurement failed, reason:"+ie.getMessage(),ie);
        }
    }    
    */
    
    
    // woknous
    private static long getProcessMemoryFootPrintOnWindows(long pid) throws MemoryMeasurementFailedException {
        loadMemoryMeasurementLibrary();
        long value = getProcessMemoryFootPrintNative(pid);
        if (value == UNKNOWN_VALUE) {
            // there was some problem when measuring the foot print
            throw new MemoryMeasurementFailedException("Memory measurement call to native library failed - could not measure memory of process with pid "+pid+".");
        } else {
            // everything seems to be ok
            return value;
        }
    }    
    
    private static native long getProcessMemoryFootPrintNative(long pid);
    
    
    private static long getOutputValue(Process process) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String outputString = br.readLine();
            //System.out.println("Outputstring is"+outputString);            
            try {
                return Long.parseLong(outputString);
            } catch (NumberFormatException nfe) {
                throw new IOException("Received String is not a number: "+outputString);
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }
    
    // load the library (if applicable) 
    private static boolean libraryLoaded = false;
    private static void loadMemoryMeasurementLibrary() throws MemoryMeasurementFailedException {        
        if (!libraryLoaded) {
            try {
                File dllLibrary = new File(Manager.getNbJUnitHome(),"lib.memory-measurement.win32.dll");
                //System.out.println("Libray path:"+dllLibrary);
                System.load(dllLibrary.getAbsolutePath());
                libraryLoaded = true;
               //System.out.println("Libraru loaded");
            } catch (IOException ioe) {
                throw new MemoryMeasurementFailedException("Cannot load native memory measurement library lib.memory-measurement.win32.dll, reason: "
                                        +ioe.getMessage(),ioe);
            } catch (UnsatisfiedLinkError ule) {
                // cannot load the library ....
                throw new MemoryMeasurementFailedException("Cannot load native memory measurement library lib.memory-measurement.win32.dll, reason: "
                                        +ule.getMessage(),ule);                
            }
        }
        
    }
    
    // out value reader class - not used - the value is read directly, without necessity to start another thread !!!!
    /*
    static class OutputReader implements Runnable {
        InputStream is;
        
        long readValue = UNKNOWN_VALUE;
        IOException caughtException;
        
        public OutputReader(InputStream is) {
            this.is = is;
            System.out.println("Reader ready - is = "+is);
        }
        
        public long getReadValue() {
            return readValue;
        }
        
        public IOException getCaughtException() {
            return caughtException;
        }
        
        public void run() {
            System.out.println("Reader Running ...");
            BufferedReader br = null;
            try {
                 br = new BufferedReader(new InputStreamReader(is));                 
                 String temp = null;
                 while ((temp = br.readLine()) != null) {
                     System.out.println("Read: "+temp);
                     if (temp.length() > 0) {
                         try {
                             readValue = Long.parseLong(temp);
                             System.out.println("Read value:"+readValue);
                         } catch (NumberFormatException nfe) {
                             throw new IOException("Process returned value '"+temp+"', which cannot be converted to a number. Reason: "+nfe.getMessage());
                         }
                     }
                 }
            } catch (IOException ioe) {
                caughtException = ioe;
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ioe) {
                        // who gives a ....
                    }
                }
            }
        }
    }*/
    
}
