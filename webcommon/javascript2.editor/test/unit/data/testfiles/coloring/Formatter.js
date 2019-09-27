/* 
 * Object that provides output to a page
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
        this.space =  this.addSpace();
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

    /**
     * Adding space to the indent
     * @type String
     */
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

        var text = "" + code;
        text = "<pre>" + code + "</pre>";
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

        document.write(text);
        this.println("");
    }

}