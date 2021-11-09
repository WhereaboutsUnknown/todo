$('#password').blur(function()
{
    if( $(this).val().length > 0 ) {
        hideError();
        $('.input-field').removeClass('error-input');
    }
});

$('#username').blur(function()
{
    if( $(this).val().length > 0 ) {
        hideError();
        $('.input-field').removeClass('error-input');
    }
});

function hideError() {
    $('#error-message').addClass('invisible');
}