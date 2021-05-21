define([], function() {

    /**
     * Load and stores pages definitions from and to the backend.
     * This class just delegates to the registered instance of the PagesManager.
     *
     * Please do not confuse this object with PagesManager from V1 - use
     * Pages object for the same purpose as PagesManager in V1
     *
     * TODO: lazy pages loading
     * TODO: add some prefix to the page keys in the storage
     *
     * @singleton.
     */
    var PagesManager = function() {
        Breeze.checkThis(this);
        Breeze.checkSingleton(PagesManager);
        this._implementation = null;
    };

    PagesManager.NO_IMPLEMENTATION_ERROR = new Error('No implementation of PagesManagerImpl registered!');
    PagesManager.INVALID_ARGUMENT = new Error('Invalid argument!');
    PagesManager.ALREADY_REGISTERED = new Error('Already registered!');

       
    PagesManager.prototype.loadPage = function(id, callback) {
        if (!this._implementation) {
            throw PagesManager.NO_IMPLEMENTATION_ERROR;
        }
        this._implementation.loadPage(id, callback);
    };

    PagesManager.prototype.getIds = function(callback) {
        if (!this._implementation) {
            throw PagesManager.NO_IMPLEMENTATION_ERROR;
        }
        this._implementation.getIds(callback);
    };

    PagesManager.prototype.storePage = function(page) {
        if (!this._implementation) {
            throw PagesManager.NoImplementationError;
        }
        this._implementation.storePage(page);
    };

    PagesManager.prototype.registerImplementation = function(impl) {
        if (!impl) {
            throw PagesManager.INVALID_ARGUMENT;
        }
        if (this._implementation) {
            throw PagesManager.ALREADY_REGISTERED;
        }
        this._implementation = impl;
    };

    return Breeze.initSingleton(PagesManager);

});
