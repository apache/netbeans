jQuery(function($){
    $.timepicker.regional["cs"] = {
        closeText: 'Zavřít',
        prevText: '&#x3c;Dříve',
        nextText: 'Později&#x3e;',
        currentText: 'Nyní',
        hourText:"Hodiny",
        minuteText:"Minuty",
        secondText:"Vteřiny",
        monthNames: ['leden','únor','březen','duben','květen','červen',
            'červenec','srpen','září','říjen','listopad','prosinec'],
        monthNamesShort: ['led','úno','bře','dub','kvě','čer',
            'čvc','srp','zář','říj','lis','pro'],
        dayNames: ['neděle', 'pondělí', 'úterý', 'středa', 'čtvrtek', 'pátek', 'sobota'],
        dayNamesShort: ['ne', 'po', 'út', 'st', 'čt', 'pá', 'so'],
        dayNamesMin: ['ne','po','út','st','čt','pá','so'],
        weekHeader: 'Týd',
        dateFormat: 'dd.mm.yy',
        firstDay: 1,
        isRTL: false,
        showMonthAfterYear: false,
        yearSuffix: ''};
    $.timepicker.setDefaults($.timepicker.regional['cs']);
    
    var test = {}
    test["myProperty"] = 20;
    test.anotherProperty = test.myProperty;
});


