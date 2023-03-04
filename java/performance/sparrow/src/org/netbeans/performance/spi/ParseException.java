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
