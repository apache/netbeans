function WidgetMilestones(inHR) {
    this.template = "";

    function getDaysToMilestone(milestoneName) {
        for (var i = 0, max = ChartP3.prototype.milestones.length; i < max; i++) {
         
        }
    }


}

function ChartP3(container) {

    if (inArchive()) {
        return;
    }

    function inArchive() {
        var t = /.+\.([^?]+)(\?|$)/;
        var results = window.location.pathname.match(t);
        if (results && results.length > 1) {
            if (results[1] === "html") {
                return true;
            }
        }
        return false;
    }

    var self = this;
    self.predictionLinearData = {
        betaFound: true
    };

    self.codeFreezeQC = -1;
    self.milestones = [];

    for (var i = 0, max = ChartP3.prototype.milestones.length; i < max; i++) {
        self.milestones.push(cloneMilestone(ChartP3.prototype.milestones[i]));
    }



    function cloneMilestone(milestone) {
        return {
            name: milestone.name,
            timestamp: milestone.realTimestamp,
            date: milestone.dateWithoutTime
        };
    }
}
