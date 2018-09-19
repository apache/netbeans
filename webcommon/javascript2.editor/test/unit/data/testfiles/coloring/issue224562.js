function print() {
    $('body').on('focus.typeahead.data-api', '[data-provide="typeahead"]',
function(e) {
        var text = $(this);
         if (text.data('typeahead'))
            return;
    });
}   
