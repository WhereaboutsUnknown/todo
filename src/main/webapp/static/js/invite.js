window.addEventListener("DOMContentLoaded", () => {
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
        const invite = $("#invite-block").attr('value');

        rest("POST", "/invite/api?id=" + invite + "&accept=" + accept, null, function (data) {
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
        });
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
});