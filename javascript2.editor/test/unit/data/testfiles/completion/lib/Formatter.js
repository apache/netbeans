/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

var formatter = {
    indent: 0,
    space: "",

    // all properties and object function has to be referenced throgh this
    addIndent: function (count) {
        this.indent += count;
        this.space = this.addSpace();
    },

    removeIndent: function (count) {
        this.indent -= count;
        if(this.indent < 0) {
            this.indent = 0;
        }
        this.space = this.addSpace();
    },

    println: function (text) {
        document.writeln(this.space + text + "<br/>");
    },

    delimiter: function(title, emptyLine) {
        if (emptyLine == undefined || !(emptyLine instanceof Boolean)) {
            emptyLine = new Boolean(false);
        }
        if (emptyLine.valueOf()) {
            this.println("");
        }
        if (title != undefined) {
            this.println("------------------- " + title + " ------------------");
        } else {
            this.println("-------------------------------------");
        }
    },

    addSpace: function(){
        var space = "";
        for(var i = 0; i < this.indent; i++) {
            space += "&nbsp;";
        }
        return space;
    },
    
    print: function (text) {
        document.writeln(text);
    },

    printCode : function(code) {
        //if (typeof(code) != 'function' && typeof(code) != 'object') {
        //    this.println("The parameter code is not a function")
        //    return;
        //}
        var text = "" + code;
        text = "<pre>" + code + "</pre>";
        //text = text.replace(/\n/g, "</br>");
        //text = text.replace(/ /g, "&nbsp;");
        // counting strings
        var textParts = text.split("\"");
        var number = 0;
        if (textParts.length > 1){
            text = "";
            for (var i = 0; i < textParts.length; i++) {
                if (number == 0) {
                    text += textParts[i] + "<font color='#ce7b00'>\"";
                    number = 1;
                } else {
                    text += textParts[i]+ "\"</font>"; 
                    number = 0;
                }
            }
        }
        
        text = text.replace(/function/g, "<font color='blue'>function</font>");
        text = text.replace(/if/g, "<font color='blue'>if</font>");
        text = text.replace(/return/g, "<font color='blue'>return</font>");
        text = text.replace(/new/g, "<font color='blue'>new</font>");
        text = text.replace(/var/g, "<font color='blue'>var</font>");
        //this.println(text);
        document.write(text);
        this.println("");
    }

}


