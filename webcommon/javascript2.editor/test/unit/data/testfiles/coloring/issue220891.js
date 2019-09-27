var hiddenComponents = true;

function toggleComponents() {
    if (hiddenComponents) {
        $("tr[data-hidden=\"1\"]").css("display", "table-row");
        hiddenComponents = false;
    }
    else {
        $("tr[data-hidden=\"1\"]").css("display", "none");
        hiddenComponents = true;
    }
}