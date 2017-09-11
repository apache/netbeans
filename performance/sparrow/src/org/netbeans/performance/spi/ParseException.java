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
 * ParseException.java
 *
 * Created on October 8, 2002, 2:54 PM
 */

package org.netbeans.performance.spi;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.io.File;
/** An exception that can be thrown by objects attempting to parse logged
 *  data.  Not all parse exceptions could or should be fatal - they may
 *  be used as a mechanism to try a different parser, or can result in
 *  a name-value pair object simply returning its value as undefined.
 * @author  Tim Boudreau
 */
public class ParseException extends DataNotFoundException {
    String source;
    /** Creates a new instance of ParseException */
    public ParseException(String source, String message) {
        super (message);
        this.source = source;
    }

    public ParseException (String message) {
        super (message);
    }

    /** Creates a new instance of ParseException */
    public ParseException(String message, File f) {
        super (message);
        this.file = f;
    }

    public ParseException (String message, File f, Throwable t) {
        super (message, t);
        this.file = f;
    }
    
    public ParseException (String message, Throwable t) {
        super (message, t);
    }    

//--------------
    /** Creates a new instance of ParseException */
    public ParseException(String message, File f, String source) {
        super (message);
        this.file = f;
        this.source = source;
    }
    
    public ParseException (String message, File f, String source, Throwable t) {
        super (message, t);
        this.file = f;
        this.source = source;
    }
    
    public ParseException (String message, String source, Throwable t) {
        super (message, t);
        this.source = source;
    }    
    
    
    public String getSource () {
        return source;
    }
    
    public String getMessage() {
        StringBuffer result = new StringBuffer(super.getMessage());
        if (file != null) {
            result.append ("\nSource: " + source);
        }
        return result.toString();
    }
    
}
