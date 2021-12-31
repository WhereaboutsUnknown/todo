let choiceMade = false;
const token = $("meta[name='_csrf']").attr("content");

function timer(time, domain) {
    let timer = setInterval(function() {
        if(time <= 0) {
            clearInterval(timer);
            document.location = domain;
        } else {
            time--;
        }
    }, time)
}

function sendInviteAnswer(accept) {
    const xhr = new XMLHttpRequest();
    const invite = $("#invite-block").attr('value');

    xhr.open("POST", window.location.origin + "/invite/api?id=" + invite + "&accept=" + accept)
    xhr.setRequestHeader("Content-type", "application/json; charset = utf-8");
    xhr.setRequestHeader("X-CSRF-TOKEN", token);
    xhr.send();
    xhr.addEventListener("load", function () {
        if (xhr.status === 200) {
            let data = JSON.parse(xhr.response);
            if (data.error) {
                console.error("POST " + window.location.origin + "/invite");
                Swal.fire({
                    text: data.error,
                    type: 'error',
                    confirmButtonColor: '#fb2a79'
                });
                choiceMade = false;
                return;
            }
            Swal.fire({
                type: 'success',
                text: data.message,
                //confirmButtonColor: '#195fd4'
            });
            timer(50, window.location.origin)
        }
    })
}

$("#accept").click(function () {
    if (!choiceMade) {
        sendInviteAnswer(true);
    }
});

$("#decline").click(function () {
    if (!choiceMade) {
        sendInviteAnswer(false);
    }
});

$("#error-ok").click(function () {
    document.location = window.location.origin;
});