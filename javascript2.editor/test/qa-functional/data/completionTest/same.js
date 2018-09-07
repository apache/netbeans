var rockband = {
    frontman: "Joe",
    "guitar": "Jim",
    "solo": "Jack",
    "guitar2": "Jim2",
    "single": function () {
    },
    albums: {
        "album1": {released: new Date(), ready: 1},
        "album2": {released: new Date(), ready: 1}
    }
};


function Folkband() {
    this.name = "";
    this.popularity = 1;
    this.name2 = 1;
    this.play = function () {
    };
}
Folkband.prototype.worth = 100;




/**
 * 
 * @param {rockband} bb
 * @returns {undefined}
 */
function topLevel1(bb) {

}

//cc;1;topLevel1({});2;frontman,guitar,solo,albums,guitar2;guitar2;topLevel1({guitar2});ba,foo


/**
 * 
 * @param {Folkband} aa
 * @returns {undefined}
 */
function topLevel2(aa) {

}

//cc;1;topLevel2({});2;name,popularity,name2,worth;name2;topLevel2({name2});ba,foo


function Container() {

    /**
     * 
     * @param {Folkband} obj
     * @returns {undefined}
     */
    this.contains = function (obj) {

    };
    /**
     * 
     * @param {rockband} obj2
     * @returns {undefined}
     */
    this.rcontains = function (obj2) {

    };
}

//cc;1;new Container().rcontains({});2;frontman,guitar,solo,albums,guitar2;guitar2;new Container().rcontains({guitar2});ba,foo


//cc;1;new Container().contains({});2;name,popularity,name2;name2;new Container().contains({name2});ba,foo



(function () {

    var attempt = function () {

//cc;1;new Container().rcontains({});2;frontman,guitar,solo,albums,guitar2;guitar2;new Container().rcontains({guitar2});ba,foo


//cc;1;new Container().contains({});2;name,popularity,name2;name2;new Container().contains({name2});ba,foo

    };




})();



/**
 * 
 * @param {Object} cc
 * @param {rockband} bb
 * @returns {undefined}
 */
function topLevel11(cc, bb) {

}

//cc;1;topLevel11({},{});2;frontman,guitar,solo,albums,guitar2;guitar2;topLevel1({guitar2});ba,foo


/**
 * 
 * @param {Object} qq
 * @param {Folkband} aa
 * @returns {undefined}
 */
function topLevel22(qq, aa) {

}

//cc;1;topLevel22({},{});2;name,popularity,name2;name2;topLevel2({name2});ba,foo


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



/**
 * @typedef   DatWidget
 * @type      {object}
 * @property  {String} keyField
 * @property  {String} kocka
 * @property  {String} [label]
 *            Label to add to select widget
 * @property  {(String|function|Array.<String>)} dataSource
 *            Source for option elements, may be: ...
 */

/**
 * @typedef   someProp
 * @type      {DefClass}
 * @property  {String} vlastnost
 * @property  {String} [popis]
 * @property  {Human} [clovek]
 * @property  {Array|Number} pokus
 */

/**
 * @typedef   MyObjectDot.someProp1
 * @property  {String} [popis1]
 * @property  {Human} [clovek1]
 * @property  {Array|DatWidget} pokus1
 */


/**
 * @typedef   MyObjectTilda~someProp11
 * @type      {DefClass}
 * @property  {String} [popis2]
 * @property  {Human} [clovek2]
 * @property  {DatWidget} pokus2
 */

/**
 * @typedef   AnotherOne
 * @type      {DefClass}
 * @property  {DatWidget} yet
 */


/**
 * Comment
 * @param {someProp} dd description
 */
function simpledef(dd) {

}


//cc;1;simpledef({});2;vlastnost,popis,clovek,counter,props;pokus;simpledef({pokus});ba,foo


/**
 * 
 * @param {MyObjectDot.someProp1} df
 * @returns {undefined}
 */
function dotdef(df) {

}

//cc;1;dotdef({});2;popis1,clovek1,pokus1;popis1;dotdef({popis1});ba,foo


/**
 * 
 * @param {MyObjectTilda~someProp11} dfb
 * @returns {undefined}
 */
function tildadef(dfb) {

}

//cc;1;tildadef({});2;counter,props,popis2,clovek2,pokus2;popis2;tildadef({popis2});ba,foo


/**
 * 
 * @param {AnotherOne~yet} df
 * @returns {undefined}
 */
function yetdef(df) {
}
//cc;1;yetdef({});2;keyField,label,dataSource,kocka;kocka;yetdef({kocka});ba,foo

/**
 * 
 * @param {AnotherOne.yet} dfa
 * @returns {undefined}
 */
function yetdef2(dfa) {

}

//cc;1;yetdef2({});2;keyField,label,dataSource,kocka;kocka;yetdef2({kocka});ba,foo


function Container2() {

    /**
     * 
     * @param {someProp} obj1
     * @returns {undefined}
     */
    this.contains2 = function (obj1) {

    };
    /**
     * 
     * @param {MyObjectDot~someProp1} obj11
     * @returns {undefined}
     */
    this.rcontains2 = function (obj11) {

    };

    /**
     * 
     * @param {MyObjectTilda.someProp11} obj2
     * @returns {undefined}
     */
    this.rcontains22 = function (obj2) {

    };
    /**
     * 
     * @param {AnotherOne~yet} obja
     * @returns {undefined}
     */
    this.rcontains23 = function (obja) {

    };
}
//cc;1;new Container2().contains2({});2;vlastnost,popis,clovek,counter,props;pokus;new Container2().contains2({pokus});ba,foo

//cc;1;new Container2().rcontains2({});2;popis1,clovek1,pokus1;popis1;new Container2().rcontains2({popis1});ba,foo

//cc;1;new Container2().rcontains22({});2;counter,props,popis2,clovek2,pokus2;popis2;new Container2().rcontains22({popis2});ba,foo




(function () {

    var attempt = function () {

//cc;1;new Container2().contains2({});2;vlastnost,popis,clovek,counter,props;pokus;new Container2().contains2({pokus});ba,foo

//cc;1;new Container2().rcontains2({});2;popis1,clovek1,pokus1;popis1;new Container2().rcontains2({popis1});ba,foo

//cc;1;new Container2().rcontains22({});2;counter,props,popis2,clovek2,pokus2;popis2;new Container2().rcontains22({popis2});ba,foo

//cc;1;new Container2().rcontains23({});2;keyField,label,dataSource,kocka;kocka;new Container2().rcontains23({kocka});ba,foo

    };




})();







