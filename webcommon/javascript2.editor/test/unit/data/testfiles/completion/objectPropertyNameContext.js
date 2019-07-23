Ext.application({
    name: 'Panda',
    autoCrea: "sranda"
});

$.ajax({
    url: "test.html",
    context: document.body,
    converters: {},
    complete: function(jqXHR, textStatus) {

    },
    beforeSend: function(xhr) {
        // try here
    },
    crossDomain: false,
    dataFilter: function(data, type) {

    }
});



