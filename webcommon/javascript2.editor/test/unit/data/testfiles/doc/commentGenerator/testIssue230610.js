/**
 * WebWorkers are a different world from main.
 * They do not have even have access to console.
 * jQuery cannot load in the this world.
 */
(function() {
    'use strict';
    function postmessage(message, details) {
        self.postMessage({
            'cmd': message, 'opts': details
        });
    }

    var VERBOSE_LOGGING = false;
    /**
     * Used for optional verbose logging
     * @param message
     */
    function log(message) {
        if (VERBOSE_LOGGING)
            postmessage('@console', "Worker Log : " + message);
    }

    log('BackgroundWorker.js loaded');
    importScripts('../../../../common-config.js');
    log('common-config.js imported');
    importScripts('../../../require/require.js');
    log('require imported');
    //???RKM??? load up the plugin configuration

    require(['otool/framework/utilities/LogUtils'], function(logUtils) {
        log('require returned');
        logUtils.setAppender(function(str) {
            postmessage('@console', str);
        });
        log('log utils configured');
        // Everything is set up at this point

        var uniqueHandlerId = 0;
        var idToHandlerMap = {};
        var uniqueRequestId = 0;
        var requestIdToCallbackMap = {};
        var cachedUrlToTextMap = {};
        var getTextLoadingUrlToCallbacksMap = {};
        //
        // TaskContext
        //

        var TaskContext = function(taskId) {
            this._taskId = taskId;
        };
        TaskContext.prototype.getText = function(url, callback) {
            if (!url || !callback)
                throw new Error("getText requires url and callback arguments");
            log("getText called; url = " + url);
            var cachedText = cachedUrlToTextMap[url];
            if (cachedText) {
                log("  returning cached result");
                callback(cachedText);
                return;
            }

            // Enter loading state

            var callbacks = getTextLoadingUrlToCallbacksMap[url];
            if (callbacks) {
                log("  in loading state, caching callback, and returning immediately");
                callbacks.push(callback);
                return;
            }

            log("  entering loading state");
            callbacks = [];
            callbacks.push(callback);
            getTextLoadingUrlToCallbacksMap[url] = callbacks;
            var requestId = ++uniqueRequestId;
            requestIdToCallbackMap[requestId] = {taskId: this._taskId, url: url};
            postmessage("@textRequest", {url: url, requestId: requestId});
        };
        //
        // Message handling
        //

        var addHandlerMessageHandler = function(details, callback) {
            if (!details.modulePath || !details.messageHandlerFunctionName)
                throw new Error("addHandler error! modulePath | messageHandlerFunctionName missing");
            require([details.modulePath], function(handlerModule) {
                var handlerId = ++uniqueHandlerId;
                idToHandlerMap[handlerId] = {modulePath: details.modulePath, module:
                            handlerModule, functionName: details.messageHandlerFunctionName, taskContext:
                            new TaskContext(handlerId)};
                callback(handlerId);
            });
        };
        var removeHandlerMessageHandler = function(handlerId) {
            var handler = idToHandlerMap[handlerId];
            if (!handler)
                logUtils.severe("Attempt to remove an unknown handler " + handlerId);
            else {
                delete idToHandlerMap[handlerId];
            }
        };
        var textMessageHandler = function(details) {
            var requestId = details.requestId;
            var text = details.text;
            var requestCallBackInfo = requestIdToCallbackMap[requestId];
            if (!requestCallBackInfo) {
                logUtils.severe("Text message received for unknown request id: " +
                        requestId + ", text = " + text);
            }
            else {
                var url = requestCallBackInfo.url;
                //???RKM??? presumably we'll have error passing in here
                if (text) {

                    // Cache the results
                    cachedUrlToTextMap[url] = text;
                    // Exit loading state
                    var callbacks = getTextLoadingUrlToCallbacksMap[url];
                    delete getTextLoadingUrlToCallbacksMap[url];
                    // Call callbacks
                    if (!callbacks)
                        logUtils.severe("Text message received with NO callbacks requestid: " + requestId + ", text = " + text);
                    else {
                        for (var i = 0; i < callbacks.length; i++) {
                            var callback = callbacks[i];
                            try {
                                callback(text);
                            }
                            catch (e) {
                                logUtils.severe("Unexpeced exception calling callback, " +
                                        callback + ", when handling text message request id: " + requestId + ", text =" + text, e);
                            }
                        }
                    }
                }
            }
        };
        var returnResults = function(message, messageId, details, results) {
            if (!messageId)
                logUtils.severe("Results unhandled due to no messageId for " + message
                        + ", messageId = " + messageId + ", details = " + details, ", lost results = "
                        + results);
            else {
                postmessage("@results", {originalmessage: message, messageId:
                            messageId, results: results});
            }
        };
        var returnFailure = function(message, messageId, details, results) {
            log("returnFailure called");
            if (!messageId)
                logUtils.severe("Failure unhandled due to no messageId for " + message
                        + ", messageId = " + messageId + ", details = " + details, ", lost results = "
                        + results);
            else {
                postmessage("@failure", {originalmessage: message, messageId:
                            messageId, results: results});
            }
        };
        var returnProgress = function(message, messageId, details, results) {
            if (!messageId)
                logUtils.severe("Progress unhandled due to no messageId for " + message
                        + ", messageId = " + messageId + ", details = " + details, ", lost results = "
                        + results);
            else {
                postmessage("@progress", {originalmessage: message, messageId:
                            messageId, results: results});
            }
        };

        /**^
        var callHandler = function(taskId, message, messageId, details) {
            var handler = idToHandlerMap[taskId];
            if (!handler) {
                logUtils.severe("No handler for taskId " + taskId + ", message " +
                        message + ", messageId = " + messageId + ", details = " + details);
            }
            else {
                try {
                    return handler.module[handler.functionName](message, details,
                            handler.taskContext, function resolveCallback(results) {
                        returnResults(message, messageId, details, results);
                    }, function rejectCallback(failure) {
                        returnFailure(message, messageId, details, failure);
                    }, function progressCallback(info) {
                        returnProgress(message, messageId, details, info);
                    });
                }
                catch (e) {
                    var failureMessage = "Unexpected error occurred calling the handler for " + handler.modulePath + "." + handler.functionName;
                    logUtils.severe(failureMessage, e);
                    returnFailure(message, messageId, details, failureMessage);
                }

                return true;
            }

            return false;
        };
        var messageHandler = function(e) {
            try {
                var data = e.data;
                var message = data.cmd;
                var messageId = data.cmdId;
                var details = data.opts;
                var taskId = data.taskId;
                log("Worker received " + message + ", messageId = " + messageId + ", details = " + details);
                if (message) {
                    switch (message) {
                        case '@stop':
                            self.close();
                            break;
                        case '@addHandler':
                            addHandlerMessageHandler(details, function(handlerId) {
                                returnResults(message, messageId, details, handlerId);
                            });
                            break;
                        case '@removeHandler':
                            removeHandlerMessageHandler(details);
                            break;
                        case "@text":
                            textMessageHandler(details);
                            break;
                        default:
                            if (!callHandler(taskId, message, messageId, details)) {
                                logUtils.severe("Worker received unhandled " + message + ", messageId = " + messageId + ", details = " + details);
                                returnFailure(message, messageId, details, message + " message not handled");
                            }
                            break;
                    }

                    // Handle callbacks
                }
            }
            catch (e) {
                logUtils.severe("Exception occurred while processing message", e);
            }
        };
        // The world should be all set up at this point
        self.addEventListener('message', messageHandler, false);
        log('sending alive');
        // Send a message saying we're up
        postmessage('@alive');
    });
}
());
