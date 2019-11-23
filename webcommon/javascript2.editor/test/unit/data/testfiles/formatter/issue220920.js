function parseLinks() {
    $("a").click(function(evt) {
        var href = $(this).attr("href");
        if (href.indexOf("http://netbeans.org/bugzilla/buglist.cgi") === 0 || href.indexOf("https://netbeans.org/bugzilla/buglist.cgi") === 0) {

              var allLinks = href.substr(href.indexOf("=") + 1);
            var ids_raw = allLinks.split(",");
            var ids = [];
            for (var i = 0; i < ids_raw.length; i++) {
                if (ids_raw[i].length > 0)
                    ids.push(ids_raw[i]);

            }


            if(href.length > 1500 ){

            var limit = ids.length;
            var current = 0;
            var base = "http://netbeans.org/bugzilla/buglist.cgi?bug_id=";
            while (current < limit) {
                var _link = "";
                for (var i = current; i < (current + 250); i++) {
                    if (typeof ids[i] !== "undefined")
                        _link += ids[i] + ",";
                }
                current += 250;

                window.open(base + _link, '_blank');
                window.focus();
            }

            evt.preventDefault();
        }else if
        }
    });
}