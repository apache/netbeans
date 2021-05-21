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

package org.netbeans.modules.payara.spi;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.modules.payara.common.PayaraInstanceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * Utilities.
 * <p/>
 * @author Vince Kraemer
 */
public class Utils {

    /**
     * A canWrite test that may tell the truth on Windows.
     *
     * This is a work around for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4420020
     * @param f the file or directory to test
     * @return true when the file is writable...
     */
    public static boolean canWrite(File f) {
        if (org.openide.util.Utilities.isWindows()) {
            // File.canWrite() has lots of bogus return cases associated with
            // read-only directories and files...
            boolean retVal = true;
            java.io.File tmp = null;
            if (!f.exists()) {
                retVal = false;
            } else if (f.isDirectory()) {
                try             {
                    tmp = java.io.File.createTempFile("foo", ".tmp", f);
                }
                catch (IOException ex) {
                    // I hate using exceptions for flow of control
                    retVal = false;
                } finally {
                    if (null != tmp) {
                        tmp.delete();
                    }
                }
            } else {
                java.io.FileOutputStream fos = null;
                try {
                    fos = new java.io.FileOutputStream(f, true);
                }
                catch (FileNotFoundException ex) {
                    // I hate using exceptions for flow of control
                    retVal = false;
                } finally {
                    if (null != fos) {
                        try {
                            fos.close();
                        } catch (java.io.IOException ioe) {
                            Logger.getLogger(Utils.class.getName()).log(Level.FINEST,
                                    null, ioe);
                        }
                    }
                }
            }
            return retVal;
        } else {
            // we can actually trust the canWrite() implementation...
            return f.canWrite();
        }
    }

    public static final String VERSIONED_JAR_SUFFIX_MATCHER = "(?:-[0-9]+(?:\\.[0-9]+(?:_[0-9]+|)|).*|).jar"; // NOI18N

    /**
     *
     * @param jarNamePattern the name pattern to search for
     * @param modulesDir the place to look for the pattern
     * @return the jar file that matches the pattern, null otherwise.
     *
     * @since 1.5
     */
    public static File getFileFromPattern(String jarNamePattern, File modulesDir) {
        // if asserts are on... blame the caller
        assert jarNamePattern != null : "jarNamePattern should not be null";
        // if this is in production, asserts are off and we should handle this a bit more gracefully
        if (null == jarNamePattern) {
            // and log an error message
            Logger.getLogger("payara").log(Level.INFO, "caller passed invalid jarNamePattern",
                    new NullPointerException("jarNamePattern"));
            return null;
        }

        // if asserts are on... blame the caller
        assert modulesDir != null : "modulesDir  should not be null";
        // if this is in production, asserts are off and we should handle this a bit more gracefully
        if (null == modulesDir) {
            // and log an error message
            Logger.getLogger("payara").log(Level.INFO, "caller passed invalid param",
                    new NullPointerException("modulesDir"));
            return null;
        }

        int subindex = jarNamePattern.lastIndexOf("/");
        if (subindex != -1) {
            String subdir = jarNamePattern.substring(0, subindex);
            jarNamePattern = jarNamePattern.substring(subindex + 1);
            modulesDir = new File(modulesDir, subdir);
        }
        if (modulesDir.canRead() && modulesDir.isDirectory()) {
            // try the express check...
            String expressPattern = jarNamePattern.replace(ServerUtilities.VERSION_MATCHER, ".jar"); // NOI18N
            File candidate = new File(modulesDir, expressPattern);
            if (!"".equals(expressPattern) && candidate.exists()) {
                return candidate;
            }
            // try the longer check...
            File[] candidates = modulesDir.listFiles(new VersionFilter(jarNamePattern));
            if (candidates != null && candidates.length > 0) {
                return candidates[0]; // the first one
            }
        }
        return null;
    }

    private static class VersionFilter implements FileFilter {

        private final Pattern pattern;

        public VersionFilter(String namePattern) {
            pattern = Pattern.compile(namePattern);
        }

        @Override
        public boolean accept(File file) {
            return pattern.matcher(file.getName()).matches();
        }

    }

    public static String sanitizeName(String name) {
        if (null == name || name.matches("[\\p{L}\\p{N}_][\\p{L}\\p{N}\\-_./;#:]*")) {
            return name;
        }
        // the string is bad...
        return "_" + name.replaceAll("[^\\p{L}\\p{N}\\-_./;#:]", "_");
    }

    /**
     * Add escape characters for backslash and dollar sign characters in
     * path field.
     *
     * @param path file path in string form.
     * @return adjusted path with backslashes and dollar signs escaped with
     *   backslash character.
     */
    public static final String escapePath(String path) {
        return path.replace("\\", "\\\\").replace("$", "\\$"); // NOI18N
    }

    /**
     * Determine if a local port is occupied.
     *
     * @param port
     * @return true, if the local port is in use.
     */
    public static boolean isLocalPortOccupied(int port) {
        ServerSocket ss = null;
        boolean retVal = true;
        try {
            ss = new ServerSocket(port);
            retVal = false;
        } catch (IOException ioe) {
            // do nothing
        } finally {
            if (null != ss) {try { ss.close(); } catch (IOException ioe) {} }
        }
        return retVal;
    }

    /**
     * identify the http/https protocol designator for a port
     *
     */
    public static String getHttpListenerProtocol(String hostname, String port) {
        String retVal = "http";
        try {
            retVal = getHttpListenerProtocol(hostname, Integer.parseInt(port));
        } catch (NumberFormatException nfe) {
            Logger.getLogger("payara").log(Level.INFO, "returning http due to exception", nfe);
        }
        return retVal;
    }

    /**
     * identify the http/https protocol designator for a port
     *
     */
    public static String getHttpListenerProtocol(String hostname, int port) {
        String retVal = "http";
        try {
                    if (isSecurePort(hostname, port)) {
                        retVal = "https";
                    }
        } catch (ConnectException ex) {
            Logger.getLogger("payara").log(Level.INFO, null, ex);
        } catch (SocketException ex) {
            Logger.getLogger("payara").log(Level.FINE, null, ex);
        } catch (SocketTimeoutException ex) {
            Logger.getLogger("payara").log(Level.INFO, null, ex);
        } catch (IOException ex) {
            Logger.getLogger("payara").log(Level.INFO, null, ex);
        }
        return retVal;
    }

    private static final int PORT_CHECK_TIMEOUT = 2000; // Port check timeout in ms

    /**
     * Determine whether an http listener is secure or not..
     *
     *  This method accepts a hostname and port #.  It uses this information
     *  to attempt to connect to the port, send a test query, analyze the
     *  result to determine if the port is secure or unsecure (currently only
     *  http / https is supported).
     * it might emit a warning in the server log for Payara cases
     * No Harm, just an annoying warning, so we need to use this call only when really needed
     *
     * @param hostname the host for the http-listener
     * @param port the port for the http-listener
     * @throws IOException
     * @throws SocketTimeoutException
     * @throws ConnectException
     */
    public static boolean isSecurePort(String hostname, int port)
            throws IOException, ConnectException, SocketTimeoutException {
        return isSecurePort(hostname,port, 0);
    }

    private static boolean isSecurePort(String hostname, int port, int depth) 
            throws IOException, ConnectException, SocketTimeoutException {
        // Open the socket with a short timeout for connects and reads.
        Socket socket = new Socket();
        try {
            Logger.getLogger("payara-socket-connect-diagnostic").log(Level.FINE, "Using socket.connect", new Exception());
            socket.connect(new InetSocketAddress(hostname, port), PORT_CHECK_TIMEOUT);
            socket.setSoTimeout(PORT_CHECK_TIMEOUT);
        } catch(SocketException ex) { // this could be bug 70020 due to SOCKs proxy not having localhost
            String socksNonProxyHosts = System.getProperty("socksNonProxyHosts");
            if(socksNonProxyHosts != null && socksNonProxyHosts.indexOf("localhost") < 0) {
                String localhost = socksNonProxyHosts.length() > 0 ? "|localhost" : "localhost";
                System.setProperty("socksNonProxyHosts",  socksNonProxyHosts + localhost);
                ConnectException ce = new ConnectException();
                ce.initCause(ex);
                throw ce; //status unknow at this point
                //next call, we'll be ok and it will really detect if we are secure or not
            }
        }
        //This is the test query used to ping the server in an attempt to
        //determine if it is secure or not.
        InputStream is = socket.getInputStream();        
        String testQuery = "GET / HTTP/1.0";
        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.println(testQuery);
        pw.println();
        pw.flush();
        byte[] respArr = new byte[1024];
        boolean isSecure = true;
        while (is.read(respArr) != -1) {
            String resp = new String(respArr);
            if (checkHelper(resp) == false) {
                isSecure = false;
                break;
            }
        }
        // Close the socket
        socket.close();
        return isSecure;
    }

    private static boolean checkHelper(String respText) {
        boolean isSecure = true;
        if (respText.startsWith("http/1.") || respText.startsWith("HTTP/1.")) {
            isSecure = false;
        } else if (respText.contains("<html")) {
            isSecure = false;
        } else if (respText.contains("</html")) {
            // New test added to resolve 106245
            // when the user has the IDE use a proxy (like webcache.foo.bar.com),
            // the response comes back as "d><title>....</html>".  It looks like
            // something eats the "<html><hea" off the front of the data that
            // gets returned.
            //
            // This test makes an allowance for that behavior. I figure testing
            // the likely "last bit" is better than testing a bit that is close
            // to the data that seems to get eaten.
            //
            isSecure = false;
        } else if (respText.contains("connection: ")) {
            isSecure = false;
        }
        return isSecure;
    }

    public static void doCopy(FileObject from, FileObject toParent) throws IOException {
        if (null != from) {
            if (from.isFolder()) {
                //FileObject copy = toParent.getF
                FileObject copy = FileUtil.createFolder(toParent,from.getNameExt());
                FileObject[] kids = from.getChildren();
                for (int i = 0; i < kids.length; i++) {
                    doCopy(kids[i], copy);
                }
            } else {
                assert from.isData();
                FileObject target = toParent.getFileObject(from.getName(),from.getExt());
                if (null == target) {
                    FileUtil.copyFile(from, toParent, from.getName(), from.getExt());
                }
            }
        }
    }

    /** 
     * Use the server instance id for a project to decide whether the server specific DD/resource
     * file should use the {@code glassfish-} prefix.
     * 
     * @param serverInstanceID
     * @return
     */
    public static boolean useGlassFishPrefix(String serverInstanceID) {
        if (null == serverInstanceID) {
            return true;
        }
        if (serverInstanceID.contains(PayaraInstanceProvider.EE6WC_DEPLOYER_FRAGMENT)) {
            return true;
        }
        // this check must happen AFTER the EE6WC check...
        if (serverInstanceID.contains(PayaraInstanceProvider.EE6_DEPLOYER_FRAGMENT)) {
            return false;
        }
        return true;
    }
    
}
