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
