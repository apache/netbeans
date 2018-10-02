/**
 * @param {uri:user} user the user
 * @returns {undefined}
 */
var onUserLeft = function(user) {
    console.log("User left: " + user);
    var peerConnection = peerConnections[remoteUser];
    if (peerConnection)
    {
        peerConnection.close();
        delete peerConnections[remoteUser];
    }
};
