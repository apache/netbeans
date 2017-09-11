/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.ui;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.nativeexecution.api.util.Authentication;
import org.openide.util.Exceptions;

/**
 *
 * @author akrasny
 */
public class SSHKeyFileFilter implements FileFilter {

    private static final Pattern p = Pattern.compile("-+ *BEGIN.*PRIVATE.*KEY *-+.*"); // NOI18N
    private static final Charset cs = Charset.forName("US-ASCII"); // NOI18N
    private static final SSHKeyFileFilter instance = new SSHKeyFileFilter();

    private SSHKeyFileFilter() {
    }

    public static SSHKeyFileFilter getInstance() {
        return instance;
    }

    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }

        if (!file.canRead()) {
            return false;
        }

        final long flen = file.length();

        // Will not consider too small or too big files ...
        if (flen < 100 || flen > 10000) {
            return false;
        }

        FileInputStream fis = null;
        byte[] buffer = new byte[30];

        // fast sanity check
        try {
            fis = new FileInputStream(file);
            fis.read(buffer, 0, 30);

            Matcher m = p.matcher(new String(buffer, 0, 30, cs));
            if (!m.matches()) {
                return false;
            }
        } catch (IOException ex) {
            return false;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return Authentication.isValidSSHKeyFile(file.getAbsolutePath());
    }
}
