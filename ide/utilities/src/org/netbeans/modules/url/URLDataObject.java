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


package org.netbeans.modules.url;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.DialogDisplayer;
import org.openide.cookies.InstanceCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;


/** Data object that represents one bookmark, one .url file containing url.
 *
 * @author Ian Formanek
 * @see org.openide.Places.Folders#bookmarks
 */
@MIMEResolver.ExtensionRegistration(
    extension="url",
    mimeType="text/url",
    position=360,
    displayName="#URLResolver"
)
public class URLDataObject extends MultiDataObject
                           implements OpenCookie, InstanceCookie {

    /** Name for url property. */
    static final String PROP_URL = "url";                       //NOI18N
    
    /** Generated serial version UID. */
    static final long serialVersionUID = 6829522922370124627L;

    /** Try to find URL string in the first 10 lines of the .url file */
    private static final int LINES_LIMIT = 10;

    /** */
    private Lookup lookup;

    /**
     * Constructs a new URL data object.
     *
     * @param  file  file to create an object from
     * @param  loader  <code>DataLoader</code> which recognized the file
     *                 and initiated calling this constructor
     */
    public URLDataObject(final FileObject file, MultiFileLoader loader)
            throws DataObjectExistsException {
        super(file, loader);
        getCookieSet().add(this);
    }
    
    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            FileEncodingQueryImplementation encodingImpl
                    = ((URLDataLoader) getLoader()).getEncoding();
            lookup = (encodingImpl != null)
                     ? Lookups.fixed(this, encodingImpl)
                     : Lookups.singleton(this);
        }
        return lookup;
    }
    
    /*
     * PENDING: it would be neat to have get/setURL methods 
     * but, there is a problem(at least at jdk1.3 for linux) with URL.equals
     * (too much time consuming in underlying native method).
     */
    
    /**
     * Gets a <code>URL</code> string from the underlying .url file.
     * The user is notified if an error occures during reading the file.
     * If there are multiple lines of text in the file, only the first one is
     * returned and no error is reported.
     *
     * @return  <code>URL</code> string stored in the file,
     *          an empty string if the file is empty,
     *          or <code>null</code> if an error occured while reading the file
     */
    String getURLString() {
        FileObject urlFile = getPrimaryFile();
        if (!urlFile.isValid()) {
            return null;
        }
        String urlString = null;
        
        InputStream is = null;
        try {
            is = urlFile.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            urlString = findUrlInFileContent(br);
        } catch (FileNotFoundException fne) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, fne);
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close ();
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(
                            ErrorManager.INFORMATIONAL, e);
                }
            }
        }
        
        if (urlString == null) {
            /*
             * If the file is empty, return an empty string.
             * <null> is reserved for notifications of failures.
             */
            urlString = "";                                             //NOI18N
        }
        return urlString;
    }

    /**
     * Find URL string in URL file content. Only read first LINES_LIMIT lines.
     * See bug #204972.
     *
     * @return If any found, string on separate line after "url=", otherwise the
     * first line.
     */
    @SuppressWarnings("NestedAssignment")
    static String findUrlInFileContent(BufferedReader reader) {
        String line;
        String firstLine = null;
        int tries = 0;
        try {
            while ((line = reader.readLine()) != null && tries < LINES_LIMIT) {
                if (firstLine == null) {
                    firstLine = line;
                }
                if (line.length() > 3
                        && line.substring(0, 4).equalsIgnoreCase("url=")) {
                    return line.substring(4);
                }
                tries++;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return firstLine;
    }

    /**
     * Stores a specified URL into the file backing up this URL object.
     *
     * @param  newUrlString  URL to be stored in the file
     */
    void setURLString(String newUrlString) {
        FileObject urlFile = getPrimaryFile();
        if (!urlFile.isValid()) {
            return;
        }
        FileLock lock = null;
        try {
            lock = urlFile.lock();
            OutputStream os = urlFile.getOutputStream(lock);
            os.write(newUrlString.getBytes());
            os.close();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
        } finally {
            if (lock != null) {
                lock.releaseLock();
            }
        }
    }

    /** */
    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx(URLDataObject.class);
    }

    /* implements interface OpenCookie */
    public void open() {
        String urlString = getURLString();
        if (urlString == null) {
            return;
        }
        URL url = getURLFromString(urlString);
        if (url == null) {
            return;
        }
        org.openide.awt.HtmlBrowser.URLDisplayer.getDefault().showURL(url);
    }

    /**
     * Converts an URL string to an <code>URL</code> object.
     * Notifies the user in case of failure.
     *
     * @param  urlString  string to convert to <code>URL</code>
     * @return  <code>URL</code> object representing the specified URL;
     *          or <code>null</code> in case of failure
     */
    private static URL getURLFromString(String urlString) {
        try {
            return new URL(urlString);
        } catch (MalformedURLException mue1) {
        }
        

        /* failed - try to prepend 'http://' */
        try {
            return new URL("http://" + urlString);                      //NOI18N
        } catch (MalformedURLException mue1) {
        }
        
        /* failed again - notify about the failure and return null: */
        String msg;
        if (urlString.length() > 50) {          //too long URL
            msg = NbBundle.getMessage(URLDataObject.class,
                                      "MSG_MalformedURLError");         //NOI18N
        } else {
            msg = NbBundle.getMessage(URLDataObject.class,
                                      "MSG_FMT_MalformedURLError",      //NOI18N
                                      urlString);
        }
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                msg,
                NotifyDescriptor.ERROR_MESSAGE));
        return null;
    }

    /* implements interface InstanceCookie */
    public String instanceName () {
        return getName();
    }

    /* implements interface InstanceCookie */
    /**
     * @return  class <code>URLPresenter</code>
     * @see  URLPresenter
     */
    public Class instanceClass () throws IOException, ClassNotFoundException {
        return URLPresenter.class;
    }

    /* implements interface InstanceCookie */
    /**
     * Creates an instance of <code>URLPresenter</code>.
     *
     * @return  instance of class <code>URLPresenter</code>
     * @see URLPresenter
     */
    public Object instanceCreate() throws IOException, ClassNotFoundException {
        return new URLPresenter(this);
    }
    
}
