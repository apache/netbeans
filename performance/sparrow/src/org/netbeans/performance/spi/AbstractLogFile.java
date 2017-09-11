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
 * AbstractLogFile.java
 *
 * Created on October 8, 2002, 12:57 PM
 */

package org.netbeans.performance.spi;
import java.io.*;
import org.netbeans.performance.spi.*;
import java.util.Iterator;
/** Convenience implementation of LogFile which extends AbstractDataAggregation.
 *  Subclassers should be able to simply implement parse(), processValue() and
 *  call addElement() to add new LogElements created during the parse process.
 *
 * @author  Tim Boudreau
 */
public abstract class AbstractLogFile extends AbstractDataAggregation implements LogFile, Named {
    private String filename;
    /**Temporary variable for the full text of the file.  Implementors should
     * null this value at the end of the parse() method, since it can be a
     * large amount of data. */
    protected String fullText=null;
    /**Date format used by the Ant script to log times.*/
    public static final String NBLOG_DATEFORMAT = "yyyy.MM.dd hh:mm:ss:S z";

    /** Creates a new instance of AbstractLogFile */
    protected AbstractLogFile(String filename) throws DataNotFoundException {
        this.filename = filename;
        /*
        File f = new File (filename);
        if (!f.exists()) {
            throw new DataNotFoundException ("Can't find log file.", f);
        }
         */
        name = "UNKNOWN_LOG_TYPE";
    }
    
    public String getFileName() {
        return filename;
    }
    
    /**AbstractLogFile overrides checkParsed() in AbstractLogElement to clear the
     * in-memory copy of the log file after parsing is completed.  
     */
    protected void checkParsed() {
        super.checkParsed();
        fullText = null;
    }
    
    public String getName() {
        //XXX hmm, work on windows?
        return filename.substring (filename.lastIndexOf ("/"), filename.length() -1);
    }
    
    public synchronized String getFullText() throws IOException {
        fullText = stringFromFile(filename);
        return fullText;
    }
    
    public String toString() {
        return filename;
    }
    
    public int hashCode() {
        //XXX a feeble attempt at unique hashcodes.  Check.
        return (filename.hashCode() * getPath().hashCode()) ^ 37;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof AbstractLogFile)) return false;
        return ((AbstractLogFile) o).filename.equals(filename);
    }
    
    public synchronized Iterator iterator() {
        checkParsed();
        //will return an empty iterator if the parse failed
        return super.iterator();
    }        
    
    protected abstract void parse() throws ParseException;
    
    protected static final String stringFromFile(String filename) throws IOException {
        //XXX quickndirty file reading - fix at some point
        FileInputStream fis=null;
        try {
            File f = new File(filename);
            long len = f.length();
            if (len > Integer.MAX_VALUE) 
              throw new IllegalArgumentException("Report file " + filename + " too big to process (> 32K)!"); //NOI18N
            int intlen = new Long(len).intValue();
            byte[] b = new byte[intlen];
            fis = new FileInputStream(f);
            fis.read(b);
            String s = new String(b);
            return s;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    throw e;
                }
            }
        }
    }
    
}
