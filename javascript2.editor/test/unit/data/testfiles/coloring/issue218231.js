var stylizeDisplayName = function(displayname, column, record) {
    var displayname = record.get("local display name");
    return displayname;
};

formatter.println("Start");
formatter.println(stylizeDisplayName("global display name", 10, 20));