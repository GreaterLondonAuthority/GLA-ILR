function closeCookie() {
    localStorage.setItem('cookieAccepted', 'true');
    $('gla-cookie-warning').addClass('hidden');
}

$(document).ready(function () {
    let cookieAccepted = localStorage.getItem('cookieAccepted');
    if (cookieAccepted !== 'true') {
        $('gla-cookie-warning').removeClass('hidden');
    }
});