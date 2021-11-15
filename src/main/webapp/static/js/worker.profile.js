window.addEventListener("DOMContentLoaded", () => {
    //получить const userid = ########## (после прохождения окна логина)
    function req() {
        const xhr = new XMLHttpRequest();
        xhr.open("GET", "http://localhost:8080/worker") //заменить на url-сервера/file.json?id=userid
        xhr.setRequestHeader("Content-type", "application/json; charset = utf-8");
        xhr.send();
        xhr.addEventListener("load", function () {
            if (xhr.status === 200) {
                let data = JSON.parse(xhr.response);
                console.log(data);
//                $(".profilePhoto").attr("src", `${data[0].photo}`);
                $("#profile-name").append(`${data.name}`);
                $("#profile-age").append(`${data.age}`);
                $("#work-skills-info").append(`${data.skills}`);
                $("#contacts-email").append(`${data.contacts.email}`);
                $("#contacts-telegram").append(`${data.contacts.telegram}`);
            }
        })
    }
    req();
});