NoteDao.prototype.edit = function (note) {
    
    return new Promise(function (resolve, reject) {
        tripDayCtrl
                .get(note.tripDayId)
                .then(function (day) {
                   re;
                   no;
                    var result = day.data.id(note.id).convert(note, true);
                    day.save(function (err) {
                        if (err) {
                            reject(new TPError(TPError.DatabaseError, "Unable to save data to db"));
                        } else {
                            resolve(result);
                        }
                    });
                }, reject);
    });
};

module.exports = new NoteDao();