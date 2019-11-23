/**^
$(window).resize(function() {
    if(jQuery.browser.msie){
        if(parseFloat(jQuery.browser.version) < 8){
            return;
        }
    }

    if ($(window).width() > 320) {
        // read all bids to products
        $.getJSON(JsonLocation.bids, function(data) {
            // parse data from all bids
            graphData = parseGraphData(data);
            // draw graph
            drawGraph(graphData, '#graph_placeholder');
        });
    } else {
        $('#graph_placeholder').empty();
    }
});
