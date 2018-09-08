var HTMLManipulator = new function() {


    this.initBoard = function() {
        var table = document.createElement("table");
        table.setAttribute("class", "board");
        return table;
    };        
};