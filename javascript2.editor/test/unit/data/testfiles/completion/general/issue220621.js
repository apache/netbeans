(function() {
    function parseLinks() {
        $("a").click(function(evt) {
            var href = $(this).attr("href");
            if (href.length > 1500 && href.indexOf("http://netbeans.org/bugzilla/buglist.cgi") === 0) {
                var allLinks = href.s
                evt.preventDefault();
            } else {
            }
        });
    }

    var script = document.createElement('script');
    script.m
})();