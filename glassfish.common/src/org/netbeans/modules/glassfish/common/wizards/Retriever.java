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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.glassfish.common.wizards;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.netbeans.modules.glassfish.common.GlassFishLogger;
import org.openide.util.NbBundle;

/**
 *
 * @author Peter Williams
 */
public class Retriever implements Runnable {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Trusted certificates manager.
     * >p/>
     * Let's just trust any server that we connect to.
     */
    private static class RetrieverTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0,
                String arg1) throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] arg0,
                String arg1) throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }

    /**
     * Host name verification.
     * <p/>
     * Let's just trust any server that we connect to.
     */
    private static class RetrieverHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String string, SSLSession ssls) {
            return true;
        }
    }

    public interface Updater {
        public void updateMessageText(String msg);
        public void updateStatusText(String status);
        public void clearCancelState();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = GlassFishLogger.get(Retriever.class);

    public static final int LOCATION_DOWNLOAD_TIMEOUT = 20000;
    public static final int LOCATION_TRIES = 3;
    public static final int ZIP_DOWNLOAD_TIMEOUT = 120000;
    
    public static final int STATUS_START = 0;
    public static final int STATUS_CONNECTING = 1;
    public static final int STATUS_DOWNLOADING = 2;
    public static final int STATUS_COMPLETE = 3;
    public static final int STATUS_FAILED = 4;
    public static final int STATUS_TERMINATED = 5;
    public static final int STATUS_BAD_DOWNLOAD = 6;

    /** Trusted certificates manager. */
    private static final TrustManager[] trustManager
            = new TrustManager[]{new RetrieverTrustManager()};

    /** Host name verification. */
    private static final RetrieverHostnameVerifier hostnameVerifier
            = new RetrieverHostnameVerifier();

    private static final String [] STATUS_MESSAGE = {
        NbBundle.getMessage(Retriever.class, "STATUS_Ready"),  //NOI18N
        NbBundle.getMessage(Retriever.class, "STATUS_Connecting"),  //NOI18N
        NbBundle.getMessage(Retriever.class, "STATUS_Downloading"),  //NOI18N
        NbBundle.getMessage(Retriever.class, "STATUS_Complete"),  //NOI18N
        NbBundle.getMessage(Retriever.class, "STATUS_Failed"),  //NOI18N
        NbBundle.getMessage(Retriever.class, "STATUS_Terminated"),  //NOI18N
        NbBundle.getMessage(Retriever.class, "STATUS_InvalidWsdl")  //NOI18N
    };
    
    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Handle HTTPS connection.
     * <p/>
     * @param conn HTTPS connection.
     */
    protected static void handleSecureConnection(HttpsURLConnection conn) {
        SSLContext context;
        try {
            context = SSLContext.getInstance("SSL");
            context.init(null, trustManager, null);
            conn.setSSLSocketFactory(context.getSocketFactory());
            conn.setHostnameVerifier(hostnameVerifier);
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.log(Level.INFO, "Cannot handle HTTPS connection", ex);
        } catch (KeyManagementException ex) {
            LOGGER.log(Level.INFO, "Cannot handle HTTPS connection", ex);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////
    
    private Updater updater;
    private final String locationUrl;
    private final String targetUrlPrefix;
    private final String defaultTargetUrl;
    private File targetInstallDir;
    private String topLevelPrefix;
    
    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////
    
    public Retriever(File installDir, String locationUrl, String urlPrefix, 
            String defaultTargetUrl, Updater u, String topLevelPrefix) {
        this.targetInstallDir = installDir;
        this.locationUrl = locationUrl;
        this.targetUrlPrefix = urlPrefix;
        this.defaultTargetUrl = defaultTargetUrl;
        this.updater = u;
        this.topLevelPrefix = topLevelPrefix;
    }
    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    // Thread support for downloading...
    public void stopRetrieval() {
        shutdown = true;
    }
    
    public int getDownloadState() {
        return status;
    }
    
    private void setDownloadState(int newState) {
        setDownloadState(newState, true);
    }
    
    private void setDownloadState(int newState, boolean display) {
        status = newState;
        if(display) {
            updateMessage(STATUS_MESSAGE[newState]);
        }
    }

    private void setDownloadState(int newState, String msg, Exception ex) {
        status = newState;
        Object [] args = new Object [] { msg, ex.getMessage()};
        updateStatus(MessageFormat.format(STATUS_MESSAGE[newState], args));
    }
    
    private void updateMessage(final String msg) {
        updater.updateMessageText(msg);
    }
    
    private void updateStatus(final String status) {
        updater.updateStatusText(status);
    }
    
    private String countAsString(int c) {
        String size;  //NOI18N
        if(c < 1024) {
            size = NbBundle.getMessage(Retriever.class, "MSG_SizeBytes", c);  //NOI18N
        } else if(c < 1048676) {
            size = NbBundle.getMessage(Retriever.class, "MSG_SizeKb", c / 1024);  //NOI18N
        } else {
            int m = c / 1048676;
            int d = (c - m * 1048676)*10 / 1048676;
            size = NbBundle.getMessage(Retriever.class, "MSG_SizeMb", m, d);  //NOI18N
        }
        return size;
    }

    // Thread plumbing
    private volatile boolean shutdown;
    private volatile int status;
    
    @Override
    public void run() {
        // Set name of thread for easier debugging in case of deadlocks, etc.
        Thread.currentThread().setName("Downloader"); // NOI18N
        
        shutdown = false;
        status = STATUS_START;
        URL targetUrl = null;
        URLConnection connection = null;
        InputStream in = null;
        File backupDir = null;
        long start = System.currentTimeMillis();
        String message = null;

        try {
            backupDir = backupInstallDir(targetInstallDir);
            
            setDownloadState(STATUS_CONNECTING);
            targetUrl = new URL(getDownloadLocation());

            Logger.getLogger("glassfish").log(Level.FINE, "Downloading from {0}", targetUrl); // NOI18N
            connection = targetUrl.openConnection();
            connection.setConnectTimeout(ZIP_DOWNLOAD_TIMEOUT);
            connection.setReadTimeout(ZIP_DOWNLOAD_TIMEOUT);
            in = connection.getInputStream();
            setDownloadState(STATUS_DOWNLOADING);
            int len = connection.getContentLength();
            
            // Download and unzip the V3 archive.
            downloadAndInstall(in, targetInstallDir, len);
            
            if(!shutdown) {
                long end = System.currentTimeMillis();
                String duration = getDurationString((int) (end - start));
                setDownloadState(STATUS_COMPLETE, false);
                message = NbBundle.getMessage(Retriever.class, "MSG_DownloadComplete", duration); // NOI18N
            } else {
                setDownloadState(STATUS_TERMINATED, false);
                message = NbBundle.getMessage(Retriever.class, "MSG_DownloadCancelled"); // NOI18N
            }
        } catch(ConnectException ex) {
            Logger.getLogger("glassfish").log(Level.FINE, ex.getLocalizedMessage(), ex);  //NOI18N
            setDownloadState(STATUS_FAILED, "Connection Exception", ex); // NOI18N
        } catch(MalformedURLException ex) {
            Logger.getLogger("glassfish").log(Level.FINE, ex.getLocalizedMessage(), ex);  //NOI18N
            setDownloadState(STATUS_FAILED, "Badly formed URL", ex); // NOI18N
        } catch(IOException ex) {
            Logger.getLogger("glassfish").log(Level.FINE, ex.getLocalizedMessage(), ex);  //NOI18N
            Logger.getLogger("glassfish").log(Level.FINE, "backupDir =={0}", backupDir);  //NOI18N
            Logger.getLogger("glassfish").log(Level.FINE, "connection == {0}", connection);  //NOI18N
            Logger.getLogger("glassfish").log(Level.FINE, "in == {0}", in);  //NOI18N
            setDownloadState(STATUS_FAILED, "I/O Exception", ex); // NOI18N
            updateMessage(in != null
                    ? NbBundle.getMessage(Retriever.class, "MSG_FileProblem",
                    connection != null ? connection.getURL() : null)
                    : NbBundle.getMessage(Retriever.class, "MSG_InvalidUrl", 
                    null == connection ? targetUrl : connection.getURL()));
        } catch(RuntimeException ex) {
            Logger.getLogger("glassfish").log(Level.FINE, ex.getLocalizedMessage(), ex);  //NOI18N
            setDownloadState(STATUS_FAILED, "Runtime Exception", ex); // NOI18N
        } finally {
            if(shutdown || status != STATUS_COMPLETE) {
                restoreInstallDir(targetInstallDir, backupDir);
            }
            if(in != null) {
                try { in.close(); } catch(IOException ex) { }
            }
            if(message != null) {
                updateMessage(message);
            }
            updater.clearCancelState();
        }
    }
    
    /**
     * Retrieve GlassFish installation bundle download location.
     * <p/>
     * @return GlassFish installation bundle download location.
     */
    private String getDownloadLocation() {
        String location = locationUrl;
        String result = defaultTargetUrl;
        int retries = 0;
        boolean run = true;
        while(run) {
            URLConnection conn;
            BufferedReader in = null;
            try {
                URL url = new URL(location);
                conn = url.openConnection();
                if (conn instanceof HttpURLConnection) {
                    HttpURLConnection hconn = (HttpURLConnection) conn;
                    hconn.setConnectTimeout(LOCATION_DOWNLOAD_TIMEOUT);
                    hconn.setReadTimeout(LOCATION_DOWNLOAD_TIMEOUT);
                    if (hconn instanceof HttpsURLConnection) {
                        handleSecureConnection((HttpsURLConnection)hconn);
                    }
                    hconn.connect();
                    int responseCode = hconn.getResponseCode();
                    LOGGER.log(Level.FINE, "URL Response code: {0} {1}",
                            new String[] {Integer.toString(responseCode),
                                hconn.getResponseMessage()});
                    switch (responseCode) {
                        case 301: case 302:
                            location = hconn.getHeaderField("Location");
                            if (location == null || location.trim().isEmpty()) {
                                run = false;
                            } else {
                                LOGGER.log(Level.FINE, "URL Redirrection: {0}",
                                        location);
                                run = retries++ < LOCATION_TRIES;
                            }
                            break;
                        case 200:
                            in = new BufferedReader(new InputStreamReader(
                                    hconn.getInputStream()));
                            String path;
                            if ((path = in.readLine()) != null) {
                                result = targetUrlPrefix + path;
                                LOGGER.log(Level.FINE,
                                        "New Glassfish Location: {0}", result);
                            }
                        default:
                            run = false;
                    }
                } else {
                    LOGGER.log(Level.INFO,
                            "Unexpected connection type: {0}", location);
                }
            } catch (MalformedURLException mue) {
                LOGGER.log(Level.INFO, "Error opening URL connection", mue);
                run = false;
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO, "Error reading from URL", ioe);
                run = false;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ioe) {
                        LOGGER.log(Level.INFO, "Cannot close URL reader", ioe);
                    }
                }
            }
        }
        return result;
    }

    private static final int READ_BUF_SIZE = 131072; // 128k
    private static final int WRITE_BUF_SIZE = 131072; // 128k
    
    private boolean downloadAndInstall(final InputStream in, final File targetFolder, final int filelen) throws IOException {
        BufferedInputStream bufferedStream = null;
        JarInputStream jarStream = null;
        try {
            final byte [] buffer = new byte [WRITE_BUF_SIZE];
            bufferedStream = new BufferedInputStream(in, READ_BUF_SIZE);
            jarStream = new JarInputStream(bufferedStream);
            final InputStream entryStream = jarStream;
            int totalBytesRead = 0;
            JarEntry entry;
            while(!shutdown && jarStream != null && (entry = (JarEntry)jarStream.getNextEntry()) != null) {
                String entryName = stripTopLevelDir(entry.getName());
                if(entryName == null || entryName.length() == 0) {
                    continue;
                }
                final File entryFile = new File(targetFolder, entryName);
                if(entryFile.exists()) {
                    // !PW FIXME entry already exists, offer overwrite option...
                    throw new RuntimeException(NbBundle.getMessage(
                            Retriever.class, "ERR_TargetExists", entryFile.getPath())); // NOI18N
                } else if(entry.isDirectory()) {
                    if(!entryFile.mkdirs()) {
                        throw new RuntimeException(NbBundle.getMessage(
                                Retriever.class, "ERR_FolderCreationFailed", entryFile.getName())); // NOI18N
                    }
                } else {
                    File parentFile = entryFile.getParentFile();
                    if(!parentFile.exists() && !parentFile.mkdirs()) {
                        throw new RuntimeException(NbBundle.getMessage(
                                Retriever.class, "ERR_FolderCreationFailed", parentFile.getName())); // NOI18N
                    }
                    
                    int bytesRead = 0;
                    FileOutputStream os = null;
                    try {
                        os = new FileOutputStream(entryFile);
                        int len;
                        long lastUpdate = 1;
                        while(!shutdown && (len = entryStream.read(buffer)) >= 0) {
                            bytesRead += len;
                            long update = System.currentTimeMillis() / 333;
                            if(update != lastUpdate) {
                                if (filelen < 1) {
                                    // do not know the content length... thanks bogus hoster config
                                    updateMessage(NbBundle.getMessage(Retriever.class,
                                            "MSG_Installing", entryName, countAsString(bytesRead))); // NOI18N
                                } else {
                                    updateMessage(NbBundle.getMessage(Retriever.class,
                                            "MSG_Installing2", entryName, countAsString(totalBytesRead), countAsString(filelen))); // NOI18N
                                }
                                lastUpdate = update;
                            }
                            os.write(buffer, 0, len);
                        }
                    } finally {
                        if(os != null) {
                            try { os.close(); } catch(IOException ex) { }
                        }
                    }
                    totalBytesRead += entry.getCompressedSize();
                }
            }
        } finally {
            if(bufferedStream != null) {
                try { bufferedStream.close(); } catch(IOException ex) { }
            }
            if(jarStream != null) {
                try { jarStream.close(); } catch(IOException ex) { }
            }
        }

        // execute permissions on script files will be corrected in instantiate()
        return shutdown;
    }
    
    private String stripTopLevelDir(String name) {
        if(name.startsWith(topLevelPrefix)) {
            int slashIndex = slashIndexOf(name, topLevelPrefix.length());
            if(slashIndex >= 0) {
                name = name.substring(slashIndex + 1);
            }
        }
        return name;
    }
    
    private static int slashIndexOf(String s, int offset) {
        int len = s.length();
        for(int i = offset; i < len; i++) {
            char c = s.charAt(i);
            if(c == '/' || c == '\\') {
                return i;
            }
        }
        return -1;
    }
    
    private File backupInstallDir(File installDir) throws IOException {
        if(installDir.exists()) {
            File parent = installDir.getParentFile();
            String tempName = installDir.getName();
            for(int i = 1; i < 100; i++) {
                File target = new File(parent, tempName + i);
                if(!target.exists()) {
                    if(!installDir.renameTo(target)) {
                        throw new IOException(NbBundle.getMessage(Retriever.class,
                                installDir.isDirectory() ? "ERR_FolderCreationFailed" : "ERR_FileCreationFailed",  // NOI18N
                                installDir.getAbsolutePath()));
                    }
                    return target;
                }
            }
            throw new IOException(NbBundle.getMessage(
                    Retriever.class, "ERR_TooManyBackups", installDir.getAbsolutePath())); // NOI18N
        }
        return null;
    }
    
    private void restoreInstallDir(File installDir, File backupDir) {
        if(installDir != null && installDir.exists()) {
            Util.deleteFolder(installDir);
        }

        if(backupDir != null && backupDir.exists()) {
            backupDir.renameTo(installDir);
        }
    }
    
    static String getDurationString(int time) {
        // < 1000 -> XXX ms
        // > 1000 -> XX seconds
        // > 60000 -> XX minutes, XX seconds
        // > 3600000 -> XX hours, XX minutes, XX seconds
        StringBuilder builder = new StringBuilder(100);
        if(time < 0) {
            builder.append(NbBundle.getMessage(Retriever.class, "TIME_ETERNITY"));  //NOI18N
        } else if(time == 0) {
            builder.append(NbBundle.getMessage(Retriever.class, "TIME_NO_TIME"));  //NOI18N
        } else {
            String separator = NbBundle.getMessage(Retriever.class, "TIME_SEPARATOR"); //NOI18N
            if(time >= 3600000) {
                int hours = time / 3600000;
                time %= 3600000;
                builder.append(NbBundle.getMessage(Retriever.class, "TIME_HOURS", hours));  //NOI18N
            }
            if(time >= 60000) {
                if(builder.length() > 0) {
                    builder.append(separator);
                }
                int minutes = time / 60000;
                time %= 60000;
                builder.append(NbBundle.getMessage(Retriever.class, "TIME_MINUTES", minutes));  //NOI18N
            }
            if(time >= 1000) { //  || builder.length() > 0) {
                if(builder.length() > 0) {
                    builder.append(separator);
                }
                int seconds = (time + 500) / 1000;
                time %= 1000;
                if(seconds > 0) {
                    builder.append(NbBundle.getMessage(Retriever.class, "TIME_SECONDS", seconds));  //NOI18N
                }
            } else if (time > 0 && builder.length() < 1) {
                builder.append(NbBundle.getMessage(Retriever.class, "TIME_MILISECONDS", time));  //NOI18N
            }
        }
        
        return builder.toString();
    }
    
}
