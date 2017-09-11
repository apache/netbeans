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
package org.netbeans.tax.io;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public final class StringUtil {

    public static boolean isWS (char ch) {
        return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
    }

    /**
     * @param nest list of characters that symetrically delimit some inner token that
     *  can contain stop delimiter. e.g. &lt;!ENTITY sdsd "sd>">
     */
    public static int skipDelimited (String text, int pos, char del1, char del2, String nest) {
        char ch = text.charAt (pos);
        if ( ch != del1) return -1;
        do {
            pos++;
            ch = text.charAt (pos);
            if (nest.indexOf (ch) >= 0) {
                pos = skipDelimited (text, pos, ch, ch, "");
                ch = text.charAt (pos);
            }
        } while (ch != del2);
        return pos + 1;
    }
    
    public static int skipDelimited (String text, int pos, String del1, String del2) {
        if (text.startsWith (del1, pos)) {
            int match = text.indexOf (del2, pos + del1.length ());
            if (match == -1) return -1;
            return match + del2.length ();
        } else {
            return -1;
        }
    }
    
    public static int skipWS (String text, int pos) {
        if (isWS (text.charAt (pos))) {
            return pos + 1;
        } else {
            return -1;
        }
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main (String args[]) {
        
        String idtd = "   <!-- klfh -->  <!hjk \"fdsf\"  ''>]>";
        int pos = 0;
        int now = 0;
        int last = -1;
        
        System.err.println ("SkipWs" + skipWS (" k", pos));
        
        System.err.println ("SkipDelinitedchar" + skipDelimited ("<  ' > '>", 0, '<', '>' ,"\"'"));
        
        System.err.println ("SkipDelinitedchar" + skipDelimited ("<!--  ' > '-->", 0, "<!--", "-->"));
        
        while (idtd.substring (pos).startsWith ("]>") == false && last != pos) {
            
            last = pos;
            
            for (now = 0; now != -1; now = skipWS (idtd, pos)) pos = now;
            
            for (now = 0; now != -1; now = skipDelimited (idtd, pos, "<!--", "-->")) {
                pos = now;
                for (now = 0; now != -1; now = skipWS (idtd, pos)) pos = now;
            }
            
            for (now = 0; now != -1; now = skipDelimited (idtd, pos, '<', '>' , "\"'")) pos = now;
            
            //            while(skipWS(idtd, pos));
            //            while(skipDelimited(idtd, pos, "<!--", "-->")) { while(skipWS(idtd, pos));};
            
            //            skipDelimited(idtd, pos, '%', ';' , "");
        }
        
    }
    
}
