function HudsonReader() {

    if (!(this instanceof HudsonReader)) {
        return new HudsonReader();
    }

    $("#addServerButton").on("click", addServer);
    $("#loadServerButton").on("click", loadCachedServer);
    var self = this;
    this.logger = new HudsonReader.utils.Logger();
    this.logger.printLevel = 1; // here is this purple as variable
    this.indexedDb = new HudsonReader.utils.DB(databaseListener, this.logger);
    var totalWorkers = 0;
}