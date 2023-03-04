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

package org.netbeans.modules.db.mysql.util;

import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Various utility methods
 * 
 * @author David Van Couvering
 */
public class Utils {
    private static Logger LOGGER = Logger.getLogger(Utils.class.getName());

    public static String getHostIpAddress(String hostname) throws UnknownHostException {
        InetAddress inetaddr = InetAddress.getByName(hostname);
        return inetaddr.getHostAddress();
    }
    
    public static RuntimeException launderThrowable(Throwable t) {
        assert (t != null);
        if (t instanceof RuntimeException)
            return (RuntimeException)t;
        else if (t instanceof Error)
            throw (Error)t;
        else
            throw new RuntimeException("Unexpected exception: " + t.getClass().getName() + ": " + t.getMessage(), t);
    }

    public static void displayError(String msg, Exception ex) {
        LOGGER.log(Level.INFO, msg, ex);
        
        String reason = ex.getMessage() != null ? ex.getMessage() : 
            Utils.getMessage( "MSG_SeeErrorLog");
        
        msg = msg + ": " + reason;
        
	NotifyDescriptor d = new NotifyDescriptor.Message(msg, 
                NotifyDescriptor.ERROR_MESSAGE);
        
	DialogDisplayer.getDefault().notify(d);        
    }
    
    
    /**
     * Return true if this is a valid directory
     * @param path path to validate
     * @param emptyOK set to true if an empty/null string is OK
     */
    public static boolean isValidDirectory(String path, boolean emptyOK) {
        return isValidPath(path, true, emptyOK);
    }
    
    /**
     * Return true if this is a valid executable file
     * @param path path to validate
     * @param emptyOK set to true if an empty/null string is OK
     */
    public static boolean isValidExecutable(String path, boolean emptyOK) {
        return isValidPath(path, false, emptyOK);
    }
    
    /** Return true if this is a valid, non-empty executable file */
    public static boolean isValidExecutable(String path) {
        return isValidExecutable(path, false);
    }

    private static boolean isValidPath(String path, boolean isDirectory, boolean emptyOK) {
        if ( isEmpty(path) ) {
            return emptyOK;
        }
        File file = new File(path).getAbsoluteFile();
        if ( ! file.exists() ) {
            return false;
        }
        
        return (isDirectory && file.isDirectory()) || 
                (!isDirectory && file.isFile()) ||
                (Utilities.isMac() && !isDirectory && path.endsWith(".app"));
    }
    
    /**
     * Return true if this is a valid URL
     * @param url url to validate
     * @param emptyOK set to true if an empty/null string is OK
     * @return
     */
    public static boolean isValidURL(String url, boolean emptyOK) {
        if ( isEmpty(url) ) {
            return emptyOK;
        }

        try {
            new URL(url);
        } catch (MalformedURLException ex) {
            return false;
        }
        
        return true;
    }
    
    public static boolean isEmpty(String val) {
        return val == null || val.length() == 0;
    }
    
    /**
     * Pop up a confirmation dialog
     * 
     * @param message
     *      The message to display
     *  
     * @return true if the user pressed [OK], false if they pressed [CANCEL]
     */
    public static boolean displayConfirmDialog(String message) {
        NotifyDescriptor ndesc = new NotifyDescriptor.Confirmation(
                message, NotifyDescriptor.OK_CANCEL_OPTION);

        Object result = DialogDisplayer.getDefault().notify(ndesc);

        return ( result == NotifyDescriptor.OK_OPTION );
    }

    public static boolean displayYesNoDialog(String message) {
        NotifyDescriptor ndesc = new NotifyDescriptor.Confirmation(
                message, NotifyDescriptor.YES_NO_OPTION);

        Object result = DialogDisplayer.getDefault().notify(ndesc);

        return ( result == NotifyDescriptor.YES_OPTION );
    }
    
    public static void displayErrorMessage(String message) {
        NotifyDescriptor ndesc = new NotifyDescriptor(
                message, 
                Utils.getBundle().getString("MSG_ErrorDialogTitle"),
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.ERROR_MESSAGE, 
                new Object[] { NotifyDescriptor.OK_OPTION },
                NotifyDescriptor.OK_OPTION);

        DialogDisplayer.getDefault().notify(ndesc);
    }
    
    /**
     * See if two strings are equal, taking into account possibility of
     * null
     */
    public static boolean stringEquals(String str1, String str2) {
        return  (str1 == null && str2 == null) ||
                (str2 != null && str1 != null && str1.equals(str2));
    }
    
    public static ResourceBundle getBundle() {
        return NbBundle.getBundle(DatabaseServer.class);
    }
    
    public static String getMessage(String key, Object ... args) {
        return NbBundle.getMessage(DatabaseServer.class, key, args);
    }
}
