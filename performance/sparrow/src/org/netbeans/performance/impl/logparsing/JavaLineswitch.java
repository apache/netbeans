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
            identifier = identifier.substring(1, identifier.length());
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

