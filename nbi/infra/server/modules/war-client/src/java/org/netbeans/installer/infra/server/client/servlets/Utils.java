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
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
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

package org.netbeans.installer.infra.server.client.servlets;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.netbeans.installer.utils.StreamUtils;

public final class Utils {
    public static void transfer(
            HttpServletRequest request,
            HttpServletResponse response,
            OutputStream output,
            File file) throws IOException {
        RandomAccessFile input = null;
        try {
            input = new RandomAccessFile(file, "r");
            
            final String range = request.getHeader("Range");
            if (range == null) {
                response.setStatus(HttpServletResponse.SC_OK);
                
                input.seek(0);
                
                response.setHeader(
                        "Content-Length",
                        Long.toString(file.length()));
                
                StreamUtils.transferData(input, output);
            } else {
                Matcher matcher = Pattern.compile("^bytes=([0-9]*)-([0-9]*)$").matcher(range);
                
                if (!matcher.find()) {
                    response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return;
                } else {
                    long start = -1;
                    long finish = -1;
                    
                    if (!matcher.group(1).equals("")) {
                        start = Long.parseLong(matcher.group(1));
                    }
                    
                    if (!matcher.group(2).equals("")) {
                        finish = Long.parseLong(matcher.group(2));
                    }
                    
                    if ((start != -1) &&
                            (finish != -1) &&
                            ((start > finish) || (finish > file.length()))) {
                        response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                        return;
                    }
                    
                    response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                    
                    if (start == -1) {
                        start = file.length() - finish;
                        finish = -1;
                    }
                    
                    input.seek(start);
                    
                    long length = (finish == -1 ? file.length() - start : finish - start) + 1;
                    
                    response.setHeader("Content-Length", Long.toString(length));
                    response.setHeader("Content-Range", "bytes " + start + "-" + (finish == -1 ? file.length() - 1 : finish) + "/" + file.length());
                    
                    if (finish == -1) {
                        StreamUtils.transferData(input, output);
                    } else {
                        StreamUtils.transferData(input, output, finish - start + 1);
                    }
                }
            }
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            e.printStackTrace(new PrintStream(output));
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }
    
}
