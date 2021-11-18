let workerId;
let tasksCache = [];

function tasks() {
    const xhrtask = new XMLHttpRequest();
    xhrtask.open("GET", "http://localhost:8080/worker/" + 1 + "/tasks")
    xhrtask.setRequestHeader("Content-type", "application/json; charset = utf-8");
    xhrtask.send();
    xhrtask.addEventListener("load", function () {
        if (xhrtask.status === 200) {
            let data = JSON.parse(xhrtask.response);
            console.log(data);
            for (var i = 0; i < data.length; i++) {
                tasksCache.push(data[i]);
                $("#task-block-list").append(
                    `
                        <li>
                            <div class="task-block" id="${data[i].id}">
                                <h3 class="task-status" id="task-status">${data[i].status}</h3>
                                <div class="task-block-info">
                                    <div class="task-block-header">
                                        <h3 class="task-header" id="task-header">${data[i].header}</h3>
                                    </div>
                                    <div class="task-block-stack">
                                        <p class="task-stack" id="task-stack">${data[i].stack}</p>
                                    </div>
                                    <div class="task-block-bottom-container">
                                        <div class="task-block-bottom-block task-block-person">
                                            <a href="#" title="${data[i].person.name}" class="task-person" id="task-person">
                                                <img src="/static/images/person-icon.png" alt="${data[i].person.name}">
                                            </a>
                                        </div>
                                        <div class="task-block-bottom-block task-block-deadline">
                                            <p class="task-deadline" id="task-deadline">${data[i].deadline.replace('T', ' ')}</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </li>
                        `
                );
            }
        }
    })
}

window.addEventListener("DOMContentLoaded", () => {
    function req() {
        const xhr = new XMLHttpRequest();
        xhr.open("GET", "http://localhost:8080/worker")
        xhr.setRequestHeader("Content-type", "application/json; charset = utf-8");
        xhr.send();
        xhr.addEventListener("load", function () {
            if (xhr.status === 200) {
                let data = JSON.parse(xhr.response);
                console.log(data);
                workerId = data.id;
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
    tasks();
});

$("#refresh-button").click(function () {
    $("#task-block-list").empty();
    tasks();
});