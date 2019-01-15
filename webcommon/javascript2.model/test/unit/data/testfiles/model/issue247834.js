define([], function () {
    
    /**
     * An experimental support for access to an asynchronously computed object.
     * 
     * The usage is following:
     *
     * @author Marek Fukala
     *
     * @constructor
     * @param {type} createResult - function which creates the result.
     * It needs to accept one argument - a function, which must be called with
     * the result as an argument, when the result is computed.
     *
     * @param {Object} initialResultValue - initial result, may be undefined.
     * Note: If you provide the initialValue parameter, the createResult
     * function will not be called until you invalidate the result.
     *
     * @param {function} resultChangeListener - function which gets called whenever
     * the result is recomputed.
     *
     * @param {boolean} eager - in another words "not lazy". If true,
     * the behavior of Asyc changes in following way:
     * 1) the Async will initially call the createResult function to compute
     * the value if no initialValue is provided,
     * 2) when the result gets invalidated by calling markResultDirty(),
     * the call will trigger the value recomputation automatically.
     *
     * @returns {Async}
     */
    function Async(createResult, initialResultValue, resultChangeListener, eager) {
        Breeze.checkThis(this);
        this._debugThisAsync = false;

        this.setEager(eager);

    }

    return Async;

});