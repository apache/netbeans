var jsfcrud = {};
jsfcrud.busyImagePath = '__WEB_BUSY_ICON_PATH__';
jsfcrud.isDynamicFacesAvailable = typeof DynaFaces != 'undefined';
jsfcrud.canAjaxEnableForm = true;
if (!jsfcrud.isDynamicFacesAvailable) {
    jsfcrud.canAjaxEnableForm = false;
}
if (jsfcrud.isDynamicFacesAvailable) {
    Form.Element.Serializers.selectOne = function(element) {
        var value = '', opt, index = element.selectedIndex;
        if (index >= 0) {
            opt = element.options[index];
            value = opt.value;
        }
        return [element.name, value];
    };
}
jsfcrud.postReplace = function(element, markup) {
    markup.evalScripts();
    setTimeout(function(){jsfcrud.ajaxEnableForm({options: {postReplace:jsfcrud.postReplace}});}, 20);
}
jsfcrud.ajaxEnableForm = function(args) {
    if (!jsfcrud.canAjaxEnableForm) {
        return;
    }
    
    if (typeof args == undefined || args == null) {
        args = {};
    }
    
    if (typeof args.options == 'undefined') {
        args.options = {};
    }
    
    var sourceElement = null;
    if (typeof args.sourceElementId != 'undefined' && args.sourceElementId != null) {
        sourceElement = document.getElementById(args.sourceElementId);
    }
    
    if (typeof args.formId == 'undefined' || args.formId == null) {
        args.formId = 0;
    }
    
    //insert busy image we'll display when sending an Ajax request
    jsfcrud.insertBusyImage();
    
    document.forms[args.formId].submit = function() {
        var busyImage = document.getElementById('busyImage');
        if (busyImage) {
            busyImage.style.display = 'block';
        }
        DynaFaces.fireAjaxTransaction(sourceElement, args.options);
    };
};

jsfcrud.insertBusyImage = function() {
    var busyImage = document.createElement('img');
    busyImage.id = 'busyImage';
    busyImage.src = jsfcrud.busyImagePath;
    busyImage.style.display = 'none';
    document.body.insertBefore(busyImage, document.forms[0]);
}

setTimeout(function(){jsfcrud.ajaxEnableForm({options: {postReplace:jsfcrud.postReplace}});}, 20);