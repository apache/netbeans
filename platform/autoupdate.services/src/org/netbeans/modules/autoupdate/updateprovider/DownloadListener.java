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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dmitry Lipin
 */
public class DownloadListener implements NetworkAccess.NetworkListener {

    private Exception storedException;
    private File dest;
    private URL sourceUrl;
    private boolean allowZeroLength;

    public DownloadListener(URL sourceUrl, File dest, boolean allowZeroLength) {
        this.sourceUrl = sourceUrl;
        this.dest = dest;
        this.allowZeroLength = allowZeroLength;
    }
    private Logger err = Logger.getLogger(this.getClass().getName());

    public void streamOpened(InputStream stream, int contentLength) {
        err.log(Level.FINE, "Successfully started reading URI " + sourceUrl);
        try {
            doCopy(sourceUrl, stream, dest, contentLength);
        } catch (IOException ex) {
            storeException(ex);
        }
    }

    public void accessCanceled() {
        err.log(Level.FINE, "Processing " + sourceUrl + " was cancelled.");
        storeException(new IOException("Processing " + sourceUrl + " was cancelled."));
    }

    public void accessTimeOut() {
        err.log(Level.FINE, "Timeout when processing " + sourceUrl);
        storeException(new IOException("Timeout when processing " + sourceUrl));
    }

    public void notifyException(Exception x) {
        err.log(Level.INFO,
                "Reading URL " + sourceUrl + " failed (" + x +
                ")");
        storeException(x);
    }

    public void notifyException() throws IOException {
        if (isExceptionStored()) {
            throw new IOException(getStoredException().getLocalizedMessage(), getStoredException());
        }

    }

    private boolean isExceptionStored() {
        return storedException != null;
    }

    private void storeException(Exception x) {
        storedException = x;
    }

    private Exception getStoredException() {
        return storedException;
    }

    private void doCopy(URL sourceUrl, InputStream is, File temp, int contentLength) throws IOException {
        OutputStream os = null;
        int read = 0;
        int totalRead = 0;

        try {
            os = new BufferedOutputStream(new FileOutputStream(temp));
            byte[] bytes = new byte[1024];
            while ((read = is.read(bytes)) != -1) {
                os.write(bytes, 0, read);
                totalRead += read;

            }
        } catch (IOException ex) {
            err.log(Level.INFO, "Writing content of URL " + sourceUrl + " failed.", ex);
            throw ex;
        } finally {
            try {
                is.close();
                if (os != null) {
                    os.flush();
                    os.close();
                }
            } catch (IOException ioe) {
                err.log(Level.INFO, "Closing streams failed.", ioe);
            }
        }

        if (contentLength != -1 && contentLength != totalRead) {
            err.log(Level.INFO, "Content length was reported as " + contentLength + " bytes, but read " + totalRead + " bytes from " + sourceUrl);
            throw new IOException("Unexpected closed connection to " + sourceUrl);
        }

        if (totalRead == 0 && !allowZeroLength) {
            err.log(Level.INFO, "Connection content length was " + contentLength + " bytes (read " + totalRead + "bytes), expected file size can`t be that size - likely server with file at " + sourceUrl + " is temporary down");
            throw new IOException("Zero sized file reported at " + sourceUrl);
        }
        err.log(Level.FINE, "Read " + totalRead + " bytes from file at " + sourceUrl);        
    }
}
