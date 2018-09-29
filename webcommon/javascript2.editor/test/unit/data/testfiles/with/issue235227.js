function Synergy() {
    var synergy = this;
    synergy.modal = {
        modalBody: "#modal-body",
        show: function() {
            $(this.modal).modal('toggle');
        },
        innerObject: {
            innerName: 2,
            show: function() {
            }
        }
    };

    with (this.modal.innerObject) {
        show();            
        innerName = 3;
    }
}
