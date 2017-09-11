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
