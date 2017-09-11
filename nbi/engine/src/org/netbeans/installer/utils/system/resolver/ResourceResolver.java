/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun Microsystems, Inc. All
 * Rights Reserved.
 *  
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */
package org.netbeans.installer.utils.system.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;

/**
 *
 * @author Dmitry Lipin
 */
public class ResourceResolver implements StringResolver {

    public String resolve(String string, ClassLoader loader) {
        String parsed = string;
        Matcher matcher = Pattern.compile("(?<!\\\\)\\$R\\{(.*?)(;(.*)?)?}").matcher(parsed);
        while (matcher.find()) {
            String path = matcher.group(1);
            String charset = matcher.group(3);
            if (charset != null) {
                charset = charset.trim();
                if (charset.equals(StringUtils.EMPTY_STRING)) {
                    charset = null;
                }
            }
            InputStream inputStream = null;
            try {
                inputStream = ResourceUtils.getResource(path, loader);
                if(inputStream !=null) {
                    parsed = parsed.replace(matcher.group(), StringUtils.readStream(inputStream, charset));
                } else {
                    LogManager.log("Cannot find resource " + path + " using classloader " + loader);
                    parsed = null;
                    break;
                }
            } catch (IOException e) {
                ErrorManager.notifyDebug(StringUtils.format(
                        ERROR_CANNOT_PARSE_PATTERN, matcher.group()), e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        ErrorManager.notifyDebug("Cannot close input stream after reading resource: " + matcher.group(), e);
                    }
                }
            }
        }
        return parsed;
    }
}
