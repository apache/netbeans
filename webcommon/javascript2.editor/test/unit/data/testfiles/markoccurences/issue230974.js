WidgetManager.prototype.nextP2 = data[0].p2limit; // place cursor inside p2limit
$("tr[id=\"sum_first\"] > td[data-category=\"P2\"]").each(function() {
    var n = old + "<br/><b>\u2264 " + data[0].p2limit + "</b> - Quality Criteria for " + data[1] + " (in " + data[2] + " days)";
});
