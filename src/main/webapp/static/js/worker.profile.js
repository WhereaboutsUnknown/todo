let profileCache;
let tasksCache = new Map();

function tasks() {
    if (profileCache && !isNaN(profileCache.id)) {
        let list = $("#task-block-list");
        list.empty();
        const xhrtask = new XMLHttpRequest();
        xhrtask.open("GET", "http://localhost:8080/worker/" + profileCache.id + "/tasks")
        xhrtask.setRequestHeader("Content-type", "application/json; charset = utf-8");
        xhrtask.send();
        xhrtask.addEventListener("load", function () {
            if (xhrtask.status === 200) {
                let data = JSON.parse(xhrtask.response);
                console.log(data);
                for (var i = 0; i < data.length; i++) {
                    tasksCache.set(data[i].id, data[i]);
                    list.append(
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
                                            <button title="${data[i].person.name}" class="task-person" id="${data[i].person.id}">
                                                <img src="/static/images/person-icon.png" alt="${data[i].person.name}">
                                            </button>
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
}

function appendIfNonEmpty(element, value, needHref) {
    if (value ) {
        element.empty();
        element.append(needHref ? `<a href="${value}">${value}</a>` : `${value}`);
        element.parent('.contacts-info-item').attr('style', '');
    } else {
        element.parent('.contacts-info-item').attr('style', 'display: none');
    }
}

function updateWorkerProfile(data) {
    $("#profile-name").empty().append(`${data.name}`);
    $("#profile-age").empty().append(`${data.age}`);
    $("#work-skills-info").empty().append(`${data.skills}`);
    appendIfNonEmpty($("#contacts-phone"), data.contacts.phoneNumber, false);
    appendIfNonEmpty($("#contacts-vk"), data.contacts.vk, true);
    appendIfNonEmpty($("#contacts-email"), data.contacts.email, false);
    appendIfNonEmpty($("#contacts-telegram"), data.contacts.telegram, true);
    appendIfNonEmpty($("#contacts-facebook"), data.contacts.facebook, true);
    appendIfNonEmpty($("#contacts-other"), data.contacts.other, false);
}

function updatePopupManager(data) {
    $("#popup-profile-name").empty().append(`${data.name}`);
    $("#popup-profile-age").empty().append(`${data.age}`);
    appendIfNonEmpty($("#popup-contacts-phone"), data.contacts.phoneNumber, true);
    appendIfNonEmpty($("#popup-contacts-vk"), data.contacts.vk, false);
    appendIfNonEmpty($("#popup-contacts-email"), data.contacts.email, true);
    appendIfNonEmpty($("#popup-contacts-telegram"), data.contacts.telegram, false);
    appendIfNonEmpty($("#popup-contacts-facebook"), data.contacts.facebook, false);
    appendIfNonEmpty($("#popup-contacts-other"), data.contacts.other, true);
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
//                $(".profilePhoto").attr("src", `${data[0].photo}`);
                updateWorkerProfile(data);
                profileCache = data;
                tasks();
            }
        })
    }
    req();
});

$("#refresh-button").click(function () {
    $("#task-block-list").empty();
    tasks();
});

$(document).on('click','.task-person', function (event) {
    event.preventDefault();
    event.stopPropagation();
    event.stopImmediatePropagation();

    console.log("Loading person...");

    function loadmanager(managerId) {
        const xhr = new XMLHttpRequest();
        xhr.open("GET", "http://localhost:8080/manager/" + managerId)
        xhr.setRequestHeader("Content-type", "application/json; charset = utf-8");
        xhr.send();
        xhr.addEventListener("load", function () {
            if (xhr.status === 200) {
                let data = JSON.parse(xhr.response);
                console.log(data);
//                $(".profilePhoto").attr("src", `${data[0].photo}`);
                updatePopupManager(data);
            }
        })
    }

    const personId = $(this).attr('id');
    console.log(personId);

    loadmanager(personId);
    $("#person-popup").attr('style', '');
    $("#person-popup .employee-card").attr('style', '');
});

$("#person-popup").click(function () {
    $(this).attr('style', 'display: none');
    $("#person-popup .employee-card").attr('style', 'display: none');
});

$("#person-popup .employee-card").click(function (event) {
    event.stopPropagation();
});