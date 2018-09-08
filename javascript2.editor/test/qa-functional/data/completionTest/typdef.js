/**
 * @typedef   MakeSelectWidgetArgs
 * @type      {object}
 * @property  {String} keyField
 *            Key field for property
 * @property  {String} [label]
 *            Label to add to select widget
 * @property  {(String|function|Array.<String>)} dataSource
 *            Source for option elements, may be: ...
 */

//cc;1;MakeSelectWidgetArgs.;0;keyField,label,dataSource;dataSource;MakeSelectWidgetArgs.dataSource;ba,foo

/**
 * Make a select widget.
 *
 * @param   {MakeSelectWidgetArgs} opts
 *          Object containing properties to create widget for.
 */
function makeSelectWidget(opts) {
//cc;1;opts.;0;keyField,label,dataSource;dataSource;opts.dataSource;ba,foo

}


(function () {

    function DefClass() {
        this.counter = 1;
        this.props = {};
    }

    function Human() {
        this.legs = 2;
        this.legs_long = 0;
    }
    Human.prototype.walk = function () {
    };


    DefClass.prototype.tryIt = function () {
    };
}());
/**
 * @typedef   samplePropT
 * @type      {DefClass|Date}
 * @property  {String} vlastnost
 * @property  {String} [popis]
 * @property  {Human} [clovek]
 * @property  {Array|Number} pokus
 */
//comments
/**
 * 
 * @param {samplePropT|Date} aaa
 * @param {ext} bb description
 * @returns {undefined}
 */
function pokus(aaa, bb) {
//cc;1;aaa.;0;vlastnost,popis,getDate,pokus,hasOwnProperty;pokus;aaa.pokus;keyField,label

//cc;1;aaa.pokus.;0;pop,push,MAX_VALUE,hasOwnProperty;pop;aaa.pokus.pop();keyField,label

//cc;1;aaa.clovek.;0;legs_long,legs,hasOwnProperty;legs;aaa.clovek.legs;getDate,label

//cc;1;bb.;0;jej,popis,getDate,pokus,hasOwnProperty;pokus;bb.pokus;keyField,label


}

//cc;1;samplePropT.;0;vlastnost,popis,getDate,pokus,hasOwnProperty;pokus;samplePropT.pokus;keyField,label

//cc;1;samplePropT.clovek.;0;legs_long,legs,hasOwnProperty;legs;samplePropT.clovek.legs;getDate,label

//////////


/**
 * @typedef   navTest1
 * @type      {DefClass|Date}
 * @property  {String} nav1
 * @property  {String} nav2
 * @property  {Human} nav3 
 */
//comments
/**
 * 
 * @param {navTest1|Date} eeee
 * @param {ext} navTest2 description
 * @returns {undefined}
 */
function pokus2(eeee, navTest2) {

    eeee.nav3;//gt;12;typdef.js;83;23

    navTest2.clovek;//gt;17;css.js;11;24

}

navTest1.nav1;//gt;6;typdef.js;79;15

navTest1.nav1;//gt;12;typdef.js;81;24