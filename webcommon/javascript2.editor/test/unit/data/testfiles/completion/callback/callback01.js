/**
 * @class
 */
function Requester1() {}

/**
 * Send a request.
 * @param {requestCallback} cb - The callback that handles the response.
 */
Requester1.prototype.send = function(cb) {
    // code
};

/**
 * This callback is displayed as a global member.
 * @callback requestCallback
 * @param {number} responseCode
 * @param {string} responseMessage
 */

var r = new Requester1();
r.send()