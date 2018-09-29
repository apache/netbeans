function IDSyncLock(toDelete, toUpdate, toInsert, callback, lecture, course,
originalCallback, originalResponse) {

    this.toDelete = toDelete;
    this.deleted = 0;
    this.response = originalResponse;
    this.originalCallback = originalCallback;
    this.notifyDeleted = function() {
        this.deleted++;
        if (this.deleted === this.toDelete)
            this.globalNotify();
    };

    this.globalNotify = function() {
        callback(this.response, course, lecture, false, this.originalCallback);
    };
};
