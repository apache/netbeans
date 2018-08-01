function countBugs() {

    switch (1 * ($("#sum_first").attr('data-mode'))) {
        case 1:
            countBugsDashboardMain();
            break;
        case 2:
            countBugsDashBoardArea();
            break;
        case 3:
            countHistoryBugsDashboardMain();
            break;
    }
}

function countHistoryBugsDashboardMain() {

    $("td[data-id='sum']").each(function() {
        var name = $(this).data('category');
        if (name === "   ") {
            $(this).attr("id", "dummy");
        }
        var sum = 0;
        var limit = 0;
        var color = "";
        $("td[data-id='" + name + "']").each(function() {

            limit = limit + ($(this).attr('data-limit')) * 1;
            sum = sum + $(this).text() * 1;
            if ($(this).attr('data-color')) {
                color = $(this).attr('data-color');
            }
        });

        $(this).text(sum);
        if (sum != 0 && limit >= 0) {

            if (sum > limit) {
                if (color)
                    $(this).attr('class', color + "_sum");
            } else {
                if (color)
                    $(this).attr('class', color + "_sum");
            }
        }
    });

}


function countBugsDashboardMainE() {
    var size = $("td[data-id='sum']").size();
    var iterator = 0;
    $("td[data-id='sum']")
            .each(
            function() {
        iterator++;
        var name = $(this).data('category');

        if (name === "   ") {
            $(this).attr("id", "dummy");
        }

        var sum = 0;
        var limit = 0;
        var it = 0;
        var color = "";
        var sumLink = "http://netbeans.org/bugzilla/buglist.cgi?bug_id=";
        $("td[data-id='" + name + "']").each(function() {
            var a = $(this).parent().children()[1];

            if ($(a).text() !== "Plugin Portal UC" && $(a).text() !== "Certified UC") {
                if (name.indexOf("UP") > -1 || it < 1) { // UP are added and others are taken just once
                    limit = limit + ($(this).attr('data-limit')) * 1;
                }
                it = 1;
//                limit = limit + ($(this).attr('data-limit')) * 1;
                var link = ($(this).find('>:first-child').attr('href'));
                if (typeof link != "undefined") {
                    link = link.substring(link.indexOf("=") + 1);
                    sumLink += link;
                }
                sum = sum + $(this).text() * 1;
                if ($(this).attr('data-color')) {
                    color = $(this).attr('data-color');
                }
            }

        });
        if (sumLink.length > 51) {
            console.log("A");
            $(this).append("<a href=" + sumLink + ">" + sum + "</a>");
        }
        if (sum != 0 && limit >= 0) {
            $(this)
                    .attr(
                    'title',
                    ('\u2264'
                    + limit
                    + " - Quality Criteria for " + document
                    .getElementById('current_milestone').text));
            if (sum > limit) {
                if (color)
                    $(this).attr('class', color + "_sum");
            } else {
                if (color)
                    $(this).attr('class', color + "_sum");
            }
        } else {
            console.log("false");
        }
        if (iterator === size) {
            cacheIndex();
        }
    });


}

function countBugsDashboardMain() {
    var size = $("td[data-id='sum']").size();
    var iterator = 0;
    $("td[data-id='sum']")
            .each(
            function() {
        iterator++;
        var name = $(this).data('category');

        if (name === "   ") {
            $(this).attr("id", "dummy");
        }

        var sum = 0;
        var limit = 0;
        var color = "";
        var sumLink = "http://netbeans.org/bugzilla/buglist.cgi?bug_id=";
        $("td[data-id='" + name + "']").each(function() {
            var a = $(this).parent().children()[1];

            if ($(a).text() !== "Plugin Portal UC" && $(a).text() !== "Certified UC") {
                limit = limit + ($(this).attr('data-limit')) * 1;
                var link = ($(this).find('>:first-child').attr('href'));
                if (typeof link != "undefined") {
                    link = link.substring(link.indexOf("=") + 1);
                    sumLink += link;
                }
                sum = sum + $(this).text() * 1;
                if ($(this).attr('data-color')) {
                    color = $(this).attr('data-color');
                }
            }

        });
        if (sumLink.length > 51) {
            $(this).append("<a href=" + sumLink + ">" + sum + "</a>");
        }
        if (sum != 0 && limit >= 0) {
            $(this)
                    .attr(
                    'title',
                    ('\u2264'
                    + limit
                    + " - Quality Criteria for " + document
                    .getElementById('current_milestone').text));
            if (sum > limit) {
                if (color)
                    $(this).attr('class', color + "_sum");
            } else {
                if (color)
                    $(this).attr('class', color + "_sum");
            }
        }
        if (iterator === size) {
            cacheIndex();
        }
    });


}

function cacheIndex() {
    var content = $("#tabcontent").html();
    try {
        localStorage.setItem('nbdashboard_cache', content);
    } catch (e) {
    }

}

function countBugsDashBoardArea() {

    var limit = [];
    var iterator = 0;
    $("thead tr th").each(function() {
        limit[iterator] = 1 * $(this).attr('data-limit');
        iterator++;
    });


    iterator = 2;
    $("td[data-id='sum']")
            .each(
            function() {
        var name = $(this).data('category');
        var sum = 0;
        if (name === "   ") {
            $(this).attr("id", "dummy");
            return;
        }
        var limit2 = 0;
        var color = "";
        var sumLink = "http://netbeans.org/bugzilla/buglist.cgi?bug_id=";
        $("td[data-id='" + name + "']").each(
                function() {
            var link = ($(this).find('>:first-child').attr('href'));
            if (typeof link != "undefined") {
                link = link.substring(link.indexOf("=") + 1);
                sumLink += link;
            }
            limit2 += 1 * $(this).attr("data-limit");
            sum = sum + $(this).text() * 1;
            if ($(this).attr('data-color')
                    && color != 'red'
                    && color != 'light_red') {
                color = $(this).attr('data-color');
            }
        });
        if (sumLink.length > 51) {
            $(this).append("<a href=" + sumLink + ">" + sum + "</a>");
        }

        if (name.indexOf("UP") > -1) {
            limit[iterator] = limit2;
        }

        if (sum != 0 && limit[iterator] >= 0) {
            $(this)
                    .attr(
                    'title',
                    ('\u2264'
                    + limit[iterator]
                    + " - Quality Criteria for " + document
                    .getElementById('current_milestone').text));
            if (sum > limit[iterator]) {
                if (color)
                    $(this).attr('class', "red_sum");
            } else {
                if (color)
                    $(this).attr('class', "green_sum");
            }
        }
        iterator++;

    });

}

function resizeBody() {
//    document.body.style.minWidth = screen.width + 'px';
//console.log($(".content")[0].width());

}

function PlotContainer(div, data, showPoints) {
    this.div = div;
    this.data = data;
    this.placeholder;
    var p = true;
    if (typeof showPoints != "undefined") {
        p = showPoints;
    }
    this.options = {
        series : {
            lines : {
                show : true
            },
            points : {
                show : p
            }
        },
        legend:{
          position: "nw"
        },
        crosshair : {
            mode : "x"
        },
        xaxis : {
            mode : "time"
        },
        grid : {
            hoverable : true
        },
        selection : {
            mode : "x"
        }
    };

    this.plotGraph = function() {
        this.placeholder = $("" + this.div);
        var p = this.placeholder;
        var o = this.options;
        var d = this.data;

        this.placeholder.bind("plotselected", function(event, ranges) {
            plot = $.plot(p, d, $.extend(true, {}, o, {
                xaxis : {
                    min : ranges.xaxis.from,
                    max : ranges.xaxis.to
                }
            }));
        });

        this.placeholder.bind("dblclick", function(event, ranges) {
            plot = $.plot(p, d, o);
        });

        var previousPoint = null;
        this.placeholder.bind("plothover", function(event, pos, item) {
            $("#x").text(pos.x.toFixed(2));
            $("#y").text(pos.y.toFixed(2));
            if (item) {
                if (previousPoint != item.dataIndex) {
                    previousPoint = item.dataIndex;

                    $("#tooltip").remove();
                    var y = item.datapoint[1].toFixed(2);

                    $('<div id="tooltip">' + parseInt(y) + '</div>').css({
                        position : 'absolute',
                        display : 'none',
                        top : item.pageY - 20,
                        left : item.pageX + 10,
                        border : '1px solid #fdd',
                        padding : '2px',
                        'background-color' : '#fee',
                        opacity : 0.80
                    }).appendTo("body").fadeIn(200);
                }
            } else {
                $("#tooltip").remove();
                previousPoint = null;
            }
        });
        var plot = $.plot(this.placeholder, this.data, this.options);
    };
}

function toggleControls() {
    $('.showControl').each(function() {
        var t = $(this).text();
        if (t.indexOf("Show") > -1) {
            $('.hideArea').css('visibility', 'visible');
            $('.total_column').find('img').each(function() {
                $(this).css('visibility', 'visible');
            });

            $('th').find('img').each(function() {
                $(this).css('visibility', 'visible');
            });

            $(this).text('Hide controls')
        } else {
            $('.hideArea').css('visibility', 'hidden');
            $('.total_column').find('img').each(function() {
                $(this).css('visibility', 'hidden');
            });
            $('th').find('img').each(function() {
                $(this).css('visibility', 'hidden');
            });
            $(this).text('Show controls')
        }
    });


}

$(document).keyup(function(e) {

    if (e.keyCode == 27) {
        areas = [];
        languages = [];
        document.getElementById('loadingScreen').style.visibility = "hidden";
        try {
            document.getElementById('loadingScreen').removeChild(document.getElementById('tableContainerSum'));
            document.getElementById('progressBar').style.visibility = '';
        } catch (e) {
        }
        try {
            document.getElementById('loadingScreen').removeChild(document.getElementById('helpContainer'));
        } catch (e) {
        }

    }   // esc
});


function hide() {
    areas = [];
    languages = [];
    document.getElementById('loadingScreen').style.visibility = "hidden";
    try {
        document.getElementById('loadingScreen').removeChild(document.getElementById('tableContainerSum'));
        document.getElementById('progressBar').style.visibility = '';
    } catch (e) {
    }
    try {
        document.getElementById('loadingScreen').removeChild(document.getElementById('helpContainer'));
    } catch (e) {
    }
}

$(document).ready(function() {

    top_height = $($(".tableContainer")[0]).css("height");
       $("#visualization").css("height", top_height );
     var children = ($(".tabs > ul").children().length) * 1;
    $("#logo").css("top", ((-(children - 1)) * 40) + "px");
    $("#logo2").css("top", ((-(children - 1)) * 40) + "px");
//    $("#visualization").css("top", (480+(-(children - 1)) * 40) + "px");

        $(".tabs").each(function() {
        //        var children = ($(".tabs > ul").children().length) * 1;
        $(this).mouseenter(function(e) {
            if (e.target == $("span[data-trigger='true']")[0]) {
                $("#logo").animate({
                    top:"0"
                });
            children = ($(".tabs > ul").children().length) * 1;
//                $("#visualization").animate({top:(((children - 1)) * 40 + 20) + "px"});

                $("#logo2").animate({
                    top:"0px"
                });
            }

            //         document.getElementById("logo").style.left = "-1px";
        });

        $(this).mouseleave(function(e) {
                $("#logo").animate({
                    top:((-(children - 1)) * 40) + "px"
                });

//                $("#visualization").animate({top:(480+(-(children - 1)) * 40) });
                $("#logo2").animate({
                    top:((-(children - 1)) * 40) + "px"
                });
        });

    });
});

function getURLParameter(name) {
    return decodeURI(
            (RegExp(name + '=' + '(.+?)(&|$)').exec(location.search) || [, null])[1]
            );
}