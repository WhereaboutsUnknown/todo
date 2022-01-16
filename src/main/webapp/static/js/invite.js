window.addEventListener("DOMContentLoaded", () => {
    let choiceMade = false;

    function sendInviteAnswer(accept) {
        const invite = $("#invite-block").attr('value');

        rest("GET", "/invite/api?id=" + invite + "&accept=" + accept, null, function (data) {
            if (data.error) {
                console.error("GET " + api() + "/invite", "403", data.error);
                showError(data.error);
                choiceMade = false;
                return;
            }
            showDone(data.message);
            redirectTimer(3000, root());
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