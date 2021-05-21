
function test() {
    this.data = 10;
    this.data2 = [];
    this.context.data = 10;
    this.context.data2 = [];
}



angular.module("synergy.handlers", ["synergy.utils"])
        .factory("SynergyHandlers", ["SynergyUtils", function (SynergyUtils) {

                var Synergy = {control: {}};
                Synergy.control.ArchiveDataFilter = function (allData) {

                    var filteredAssignments = {};
                    this.allData.issues = []; // resolved as unknown global variable
                    var self = this;

                };

                return Synergy.control;
            }]);