(function($, _self)
{
    /**
     * @param {String} value HTML string.
     * @type {rawHtmlElement}
     * @returns {String} description
     */ 
    function rawHtmlElement(value) {
    }
    _self.form = new Object();
    /**
     * @returns {rawHtmlElement} HTML to be put in the form.
     */
    _self.form.endGroup = function()
    {
        return new rawHtmlElement('<blah>');
    };
})(jQuery, window.mJappisApplication);     
