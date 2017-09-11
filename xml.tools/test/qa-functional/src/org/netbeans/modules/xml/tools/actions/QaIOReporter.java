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

package org.netbeans.modules.xml.tools.actions;

import java.util.ArrayList;
import java.util.Arrays;
import org.netbeans.api.xml.cookies.CookieMessage;
import org.netbeans.api.xml.cookies.CookieObserver;
import org.netbeans.api.xml.cookies.XMLProcessorDetail;

/**
 * QA CookieObserver intended for testinng Validate and Check actions.
 * @author  mschovanek
 */
public class QaIOReporter implements CookieObserver {
    ArrayList messages = new ArrayList(5);

    /** Creates a new instance of QaIOReporter */
    public QaIOReporter() {
    }

    /** Receive a cookie message. Implementation (handling code) must not
     * invoke directly or indirecly any source cookie method.
     * Implementation should be as fast as possible.
     * @param msg Received cookie message never <code>null</code>.
     */
    public void receive(CookieMessage msg) {
        messages.add(msg);
    }
    
    /** Returns received messages
     * @return  */
    protected CookieMessage[] getMessages() {
        return (CookieMessage[]) messages.toArray();
    }
    
    /** Creates report from received messages.
     * @return report */
    protected String getReport() {
        int[] lines = getErrLines();
        Arrays.sort(lines);
        StringBuffer buf = new StringBuffer();
        if (lines.length > 1) {
            buf.append("There are errors at lines: ");
        } else if (lines.length > 0) {
            buf.append("There is error at line: ");
        } else {
            buf.append("There is not errors.");
        }
        for (int i = 0; i < lines.length; i++) {
            buf.append("" + lines[i] + "; ");
        }
        return buf.toString();
    }
    
    /** Returns array of errors' line numbers.
     * @return  */    
    protected int[] getErrLines() {
        int[] tmp = new int[messages.size()];
        int index = 0;
        for (int i = 0; i < messages.size(); i++) {
            CookieMessage msg = (CookieMessage) messages.get(i);
            int level = msg.getLevel();
            if (level == msg.ERROR_LEVEL || level == msg.FATAL_ERROR_LEVEL) {
                Object detail = msg.getDetail(XMLProcessorDetail.class);
                if (detail instanceof XMLProcessorDetail) {
                    tmp[index] = ((XMLProcessorDetail) detail).getLineNumber();
                } else {
                    tmp[index] = -2; // unknown line number
                }
                index++;
            }
        }
        int[] lines = new int[index];
        System.arraycopy(tmp, 0, lines, 0, lines.length);
        return lines;
    }
    
    /** Returns number of errors.
     * @return  */    
    protected int getBugCount() {
        return getErrLines().length;
    }
}
