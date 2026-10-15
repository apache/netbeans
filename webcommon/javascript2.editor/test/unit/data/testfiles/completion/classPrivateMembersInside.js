class Counter {
    /**
     * @type {String}
     */
    name;

    /**
     * @type Number
     */
    #initialState;
    #state;

    /**
     * @param {number} initialState
     * @returns {Counter}
     */
    constructor(initialState) {
        this.#initialState = initialState;
    }

    reset() {
        this.#state = this.#initialState;
    }

    increment() {
        this.#state += 1;
    }
    
    /**
     * @returns {Number}
     */
    getValue() {
        return this.;
    }
}
