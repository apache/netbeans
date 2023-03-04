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
 * JavaLineswitch.java
 *
 * Created on October 8, 2002, 4:06 PM
 */

package org.netbeans.performance.impl.logparsing;
import org.netbeans.performance.spi.html.*;
import org.netbeans.performance.spi.*;
import java.util.*;
/**Wrapper class for a single lineswitch on the Java command
 * line.
 *
 * @author  Tim Boudreau
 */
public class JavaLineswitch extends ValueLogElement implements Comparable, Valued {
    int intValue = -1;
    boolean nonStandard;
    String identifier;
    static HashMap descs;

    static {
        descs = new HashMap();
        descs.put("mx", "Maximum heap size");
        descs.put("ms", "Minimum heap size");
        descs.put("NewSize", "New area size");
        descs.put("MaxNewSize", "Maximum new area size");
        descs.put("PermSize", "Permanent area size");
        descs.put("TargetSurvivorRatio", "Target survivor ratio");
        descs.put("UsePerfDat", "Use performance data");
        descs.put("verify:none", "Disable bytcode verification");

    }

    public JavaLineswitch(String s) {
        super(s);
    }
    
    protected void parse() {
        nonStandard = line.startsWith("-XX:");
        int numberStart=-1;
        int numberEnd=-1;
        char[] ch = new char[line.length()];
        line.getChars(0, line.length(), ch, 0);
        for (int i=0; i < ch.length; i++) {
            if (numberStart == -1) {
                if (Character.isDigit(ch[i])) numberStart=i;
            } else {
                if (!(Character.isDigit(ch[i]))) {
                    numberEnd = i;
                }
            }
        }
        if ((numberStart != -1) && (numberEnd == -1)) numberEnd = ch.length;
        if (numberStart != -1) {
            intValue = Integer.parseInt(line.substring(numberStart, numberEnd));
        }
        int identifierStart = 2;
        if (nonStandard) {
            //skip the -XX:
            identifierStart = 4;
        }
        if (numberStart == -1) numberStart = line.length();
        if (nonStandard) numberStart -= 1; //get rid of trailing = e.g. PermSize=blah
        identifier = line.substring(identifierStart, numberStart);
        if (identifier.startsWith("+") || identifier.startsWith("-")) {
            Boolean val = Boolean.valueOf(identifier.startsWith("+"));
            value = val;
            identifier = identifier.substring(1);
        } else {
            value = new Integer(intValue);
        }
        name=identifier;
    }
    
    public synchronized String getIdentifier() {
        checkParsed();
        return identifier;
    }
        /*
        public synchronized Object getValue() {
            checkParsed();
            return new Integer(intValue);
        }*/
    
    public synchronized int getIntValue() {
        checkParsed();
        return intValue;
    }
    
    public synchronized String getDescription() {
        checkParsed();
        String result = (String) descs.get(identifier);
        if (result == null) result = identifier;
        return result;
    }
    
    public boolean equals(Object o) {
        return (o instanceof JavaLineswitch) && (((JavaLineswitch)o).line.equals(line));
    }
    
    public boolean isNonStandard() {
        checkParsed();
        return nonStandard;
    }
    
    public int compareTo(Object o) {
        if (!(o instanceof JavaLineswitch)) throw new IllegalArgumentException("Not a JavaLineswitch object");
        JavaLineswitch jl = (JavaLineswitch) o;
        checkParsed();
        return jl.getIntValue() - intValue;
    }
    
    public String toString() {
        //            return getDescription() + " = " + getValue();
        return line;
    }
    
    public String getName() {
        checkParsed();
        return getIdentifier();
    }
    
    public String getPath() {
        checkParsed();
        return super.getPath();
    }
    
    public HTML toHTML() {
        checkParsed();
        HTMLTable result = new HTMLTable(3); 
        result.add(getDescription());
        result.add(new HTMLTextItem(value));
        result.add("(" + line + ")");
        return result;
    }
    
}

