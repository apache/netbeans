/**
 * @constructor
 * @returns {MyObject}
 */        
function MyObject () {
    
    this.publicMethodA = function () {};

    /**
     * @private
     */
    this._privateProp;
    
    /**
     * @private
     * @returns {Brouka}
     */
    this._privateMethodB = function () {};
}