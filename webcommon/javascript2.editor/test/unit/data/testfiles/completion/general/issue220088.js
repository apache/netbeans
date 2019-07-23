function Synergy() {

    this.modal = {
        modal: "#modal",
        modalBody: "#modal-body",
        modalHeader: "#myModalLabel"
    };

    this.modal.modalBody = "aaa"; // cc here

    this.updateModal = function(header, body) {
        $(this.modal.modalHeader).text(header); // cc here
        $(this.modal.modalBody).text(body);
    };
}

var issue220088 = new Synergy();
issue220088.modal.modalBody;