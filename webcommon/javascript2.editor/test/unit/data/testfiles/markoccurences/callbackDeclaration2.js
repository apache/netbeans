/**
 * @class
 */
function Requester() {}

/**
 * Send a request.
 * @param {requestCallback} cb - The callback that handles the response.
 */
Requester.prototype.send = function(cb) {
    // code
};

/**
 * This callback is displayed as a global member.
 * @callback requestCallback
 * @param {number} responseCode
 * @param {string} responseMessage
 */