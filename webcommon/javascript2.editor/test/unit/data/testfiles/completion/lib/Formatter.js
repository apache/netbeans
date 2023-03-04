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


