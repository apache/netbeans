(function( window, undefined ) {

    // Use the correct document accordingly with window argument (sandbox)
    var document = window.document,
            navigator = window.navigator,
            location = window.location;

    var jQuery = (function() {
            var jQuery = function( selector, context ) {
                    // The jQuery object is actually just the init constructor 'enhanced'
                    return new jQuery.fn.init( selector, context, rootjQuery );
            },
            _jQuery = window.jQuery,
            _$ = window.$,
            rootjQuery;
    })();
})(window);
