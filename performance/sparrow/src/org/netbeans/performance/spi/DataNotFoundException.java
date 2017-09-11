/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
/*
 * DataNotFoundException.java
 *
 * Created on October 13, 2002, 6:05 AM
 */

package org.netbeans.performance.spi;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.io.File;
/**An exception thrown when no test data is found (e.g. a log
 * file is missing or by client code that expects to find data
 * from a query and finds none.  For some tests, that may be
 * normal and the exception should be handled.  In other cases,
 * the test should treat it as fatal.
 *
 * @author  Tim Boudreau
 */
public class DataNotFoundException extends BuildException {
    File file=null;

    public DataNotFoundException (String message) {
        super (message);
    }

    /** Creates a new instance of DataNotFoundException */
    public DataNotFoundException(String message, File f) {
        super (message);
        this.file = f;
    }

    public DataNotFoundException (String message, File f, Throwable t) {
        super (message, t);
        this.file = f;
    }

    public DataNotFoundException (String message, Throwable t) {
        super (message, t);
    }
    
    public String getMessage() {
        StringBuffer result = new StringBuffer(getClass().getName());
        result.append (": ");
        result.append(super.getMessage());
        if (file != null) {
            result.append ("\nFile: " + file.toString());
        }
        return result.toString();
    }
    
    public File getFile () {
        return file;
    }
}
