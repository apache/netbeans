function diff(data) {
    var offset = 0;
    for (var i = 0; i < rows; i++) {
        var team = data[i+offset]; // mark occurrences or rename|refactor team
        if (team.name === "Plugin Portal UC") {
            if (i + 1 < data.length) {
                team = data[i + 1];
                offset = offset+1;
            } else { // fallback
                team = {
                    "name": "",
                    "bugSets": []
                };
            }
        }
    }
}
