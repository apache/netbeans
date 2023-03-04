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
 * NameValuePairLogElement.java
 *
 * Created on October 8, 2002, 3:01 PM
 */

package org.netbeans.performance.spi;
import org.netbeans.performance.spi.html.*;
/** Extension of NameValueLogElement with basic parsing of
 * lines containing a separator character (the default is
 * <code>=</code> unless a different argument is passed
 * to the appropriate constructor.  Also contains a method
 * for converting the parsed String representation of the
 * resulting object into some other object type if needed.
 * The default implementation just keeps the String.
 *
 * @author  Tim Boudreau
 */
public class NameValuePairLogElement extends ValueLogElement implements Named {
    private transient String separator = "=";
    /** A do nothing constructor for subclassers. */
    protected NameValuePairLogElement() {
        //do nothing constructor for subclassers.
    }

    /** Creates a new instance of NameValuePairLogElement */
    public NameValuePairLogElement(String s) {
        super (s.trim());
    }
    /**Constructor for name value pairs separated by some character
     * other than &quot;=&quot;.
     */
    public NameValuePairLogElement (String s, String separator) {
        this (s);
        this.separator = separator;
    }
    
    /**Parses the name value pair string passed to the constructor in
     * order to populate the name and value properties.  This method
     * will be called automatically by checkParsed() - subclasses should
     * not call this method directly, but simply make sure that all of
     * the field accessors on their classes call checkParsed before
     * returning a value.  Subclasses should populate any instance
     * fields representing properties of a log entry here.
     */
    protected void parse() throws ParseException {
        if (name.indexOf (separator) == -1) {
            throw new ParseException (line, "Separator " + separator + " not found in text " + line + " for " + getPath());
        }
        int idx = line.indexOf (separator);
        name = line.substring (0, idx).trim();
        value = processValue(line.substring (idx+1, line.length()-1).trim());
    }

    /**Method to allow further parsing of the value string - for example, 
     * integer or float log entries will want to override this method and
     * return an appropriate Integer or Float object by parsing the String.
     * This method is called by the default implementation of parse() in 
     * NameValuePairLogElement;  the base implementation does not perform
     * any conversion, but simply returns the String it is passed.
     */
    protected Object processValue (String stringval) {
        return stringval;
    }
    

    public synchronized String getName () {
        checkParsed();
        return name;
    }    
    
    public String toString() {
        checkParsed();
        return name + "=" + value;
    }
    
    public HTML toHTML() {
        checkParsed();
        HTMLTable result = new HTMLTable ();
        result.add (name);
        result.add (new HTMLTextItem (value));
        return result;
    }
    
    /*
     //XXX todo
    public int hashCode () {
        checkParsed();
        return name.hashCode() * value.hashCode() ^ 37;
    }
    
    public boolean equals (Object o) {
        return (o instanceof NameValuePairLogElement) && 
          (((NameValuePairLogElement) o).name.equals (name));
    }
     */
    
}
