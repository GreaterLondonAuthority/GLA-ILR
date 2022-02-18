function allowEnterEvent(){
    $(".allow-enter").keypress(function(event) {
        if (event.keyCode === 13) {
            $(this).click();
            event.stopPropagation();
        }
    });
}

$(document).ready(function() {
    allowEnterEvent();
});