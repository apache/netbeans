function str2array (s) {
    var spaces = /\s+/, a1 = [""];
    if (typeof s == "string" || s instanceof String) {
        if (s.indexOf(" ") < 0) {
            a1[0] = s;
            return a1;
        } else {
            return s.split(spaces);
        }
    }
    return s;
};