var test0223121 = {a:1}; // correct
var test1a223121 = $.extend( {}, {a:1} ); // test1a: $ 
var test1b223121 = $.extend( {}, test0 ); // test1b: $

          
//IF you add bind to the mix, it becomes even more weird:
var test2b223121 = $.extend.bind(this); // test2b: $|bind

//And with JSDoc it also adds annotated type:
 /**
 * @return {Promise} Deferred object with reference to request
 */
var api_request223121 = api_server.request.bind( api_server ); // api_request: bind|Promise     
