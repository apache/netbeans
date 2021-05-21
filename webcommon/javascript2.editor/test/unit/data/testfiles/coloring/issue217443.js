
var areas = [];
var languages = [];
var components = [];

  
function countComponentTables() { 
    $(".sortable").each(function() { // each table
        var index = 0;
        var sums = new Array();
        var id = $(this).attr("id");
        $(this).find("thead th").each(function() {
            index++;
            if (index > 1 && (!$(this).attr("id") || $(this).attr("id").indexOf("dummy") > -1))
                sums.push(sumOfColumns(id, index, true));

        });

        var tfoot = "<tfoot><tr class=\"bug_total_row\"><td>Total</td>";
        for (var i = 0; i < sums.length; i++) {
            tfoot += "<td>" + sums[i] + "</td>";
        }

        $(this).append(tfoot + "</tr></tfoot>");


    });


}

