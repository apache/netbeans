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

package org.netbeans.modules.masterfs.filebasedfs.utils;

import java.util.MissingResourceException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import java.io.IOException;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;

/**
 * Copy/pasted from org.openide.filesystems
 * <p/>
 * Localized IOException for filesystems.
 *
 * @author Jaroslav Tulach
 */
public final class FSException extends IOException {
    /** name of resource to use for localized message */
    //  private String resource;
    /**
     * arguments to pass to the resource
     */
    private final Object[] args;

    /**
     * Creates new FSException.
     */
    private FSException(final String resource, final Object... args) {
        super(resource);
        this.args = args;
    }

    /**
     * Message should be meaning full, but different from localized one.
     */
    @Override
    public String getMessage() {
        return " " + getLocalizedMessage(); // NOI18N
    }

    /**
     * Localized message.
     */
    @Override
    public String getLocalizedMessage() {
        final String res = super.getMessage();
        /*This call to getBundle should ensure that currentClassLoader is not used to load resources from. 
         This should prevent from deadlock, that occured: one waits for FileObject and has resource, 
         second one waits for resource and has FileObject*/
        String format = null;
        try{
            format = NbBundle.getBundle("org.netbeans.modules.masterfs.filebasedfs.Bundle", java.util.Locale.getDefault(), FileObjectFactory.class.getClassLoader()).getString(res);//NOI18N                        
        } catch (MissingResourceException mex) {
            if (format == null) {
                format = NbBundle.getBundle("org.openide.filesystems.Bundle", java.util.Locale.getDefault(), FileSystem.class.getClassLoader()).getString(res);//NOI18N    
            }
        }
                
        if (args != null) {
            return java.text.MessageFormat.format(format, args);
        } else {
            return format;
        }
    }

    /**
     * Creates the localized exception.
     *
     * @param resource to take localization string from
     * @throws the exception
     */
    public static void io(final String resource, final Object... args) throws IOException {
        final FSException fsExc = new FSException(resource, args);
        Exceptions.attachLocalizedMessage(fsExc, fsExc.getLocalizedMessage());
        throw fsExc;
    }

    public static void annotateException(final Throwable t) {
        Exceptions.attachLocalizedMessage(t, t.getLocalizedMessage());
    }
}
