window.addEventListener("DOMContentLoaded", () => {
    $('#restore-profile').click(function () {
        rest("POST", "/profile/restore", null, function (data) {
            if (data.error) {
                console.error("POST " + api() + "/profile/restore", data.errors[0], data.error);
                showError(data.error);
                return;
            }
            showDone(data.message);
            redirectTimer(3000, root());
        });
    });
});