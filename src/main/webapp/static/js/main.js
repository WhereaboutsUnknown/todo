class TodoApp {
    constructor() {
        this.todoCache = new Map();
    }

    store(attrName, attrValue) {
        if (attrName) {
            this.todoCache.set(attrName, attrValue);
        }
    }

    removeAttr(attrName) {
        this.todoCache.remove(attrName);
    }

    getValue(attrName) {
        return this.todoCache.get(attrName);
    }

    getAvatarFileType() {
        return 1;
    }
}

const Todo = new TodoApp();

function root() {
    return 'http://localhost:8080';
}

function api() {
    return 'http://localhost:8080';
}

function avatarEndpoint(avatarId) {
    return api() + "/file-service/user/file/" + (avatarId ? avatarId : 0) + "?type=" + Todo.getAvatarFileType();
}

function rest(method, url, body, callback) {
    const token = $("meta[name='_csrf']").attr("content");
    const xhr = new XMLHttpRequest();

    try {
        xhr.open(method, api() + url);
        xhr.setRequestHeader("Content-type", "application/json; charset = utf-8");
        xhr.setRequestHeader("X-CSRF-TOKEN", token);
        if (body) {
            xhr.send(JSON.stringify(body));
        } else {
            xhr.send();
        }
        xhr.addEventListener("load", function () {
            if (xhr.status === 200) {
                let data = JSON.parse(xhr.response);
                callback(data);
            }
        });
    } catch (e) {
        console.log(method + " " + api() + url, e);
    }
}

function submitForm(url, body, callback) {
    const token = $("meta[name='_csrf']").attr("content");
    const xhr = new XMLHttpRequest();

    try {
        xhr.open("POST", api() + url);
        xhr.setRequestHeader("X-CSRF-TOKEN", token);
        xhr.send(body);
        xhr.addEventListener("load", function () {
            if (xhr.status === 200) {
                let data = JSON.parse(xhr.response);
                callback(data);
            }
        });
    } catch (e) {
        console.log("POST " + api() + url, e);
    }
}

function redirectTimer(time, domain) {
    setTimeout(function () {
        document.location = domain;
    }, time);
}

function showError(message) {
    Swal.fire({
        text: message,
        type: 'error',
        confirmButtonColor: '#fb2a79'
    });
}

function showOk(message) {
    Swal.fire({
        text: message,
        confirmButtonColor: '#195fd4'
    });
}

function showDone(message) {
    Swal.fire({
        type: 'success',
        text: message,
    });
}

function taskElement(data) {
    const person = data.person;
    const btnClass = (person ? '' : ' inactivated');
    return `
                        <li>
                            <div class="task-block" id="${data.id}">
                                <h3 class="task-status" id="task-status">${data.status}</h3>
                                <div class="task-block-info">
                                    <div class="task-block-header">
                                        <h3 class="task-header" id="task-header">${data.header}</h3>
                                    </div>
                                    <div class="task-block-stack">
                                        <p class="task-stack" id="task-stack">${data.stack}</p>
                                    </div>
                                    <div class="task-block-bottom-container">
                                        <div class="${'task-block-bottom-block task-block-person' + btnClass}">
                                            <button title="${person ? person.name : ''}" class="${'task-person' + btnClass}" id="${person ? person.id : ''}">
                                                <img src="/static/images/person-icon.png" alt="Пользователь">
                                            </button>
                                        </div>
                                        <div class="task-block-bottom-block task-block-deadline">
                                            <p class="task-deadline" id="task-deadline">${data.deadline.replace('T', ' ')}</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </li>
                        `;
}

function workerElement(data) {
    if (data) {

        const statistics = data.statistics;

        return `<li>
                <div class="worker-block" id="${data.id}">
                    <div class="worker-block-header-container">
                        <div class="worker-avatar-container">
                            <img class="worker-avatar" src="${avatarEndpoint(data.avatar)}" alt="Фото">
                        </div>
                        <div class="worker-personal-and-stats-container">
                            <div class="worker-name-block">${data.name ? data.name : ' '}</div>
                            <div class="worker-age-block">${data.age ? data.age : ' '}</div>
                            <div class="worker-statistics-inline-container">
                                <div class="stars">
                                    <span class="star rate100"></span>
                                </div>
                                <div class="star-rating">${statistics ? statistics.points : 0.0}</div>
                                <div class="${'success-scale' + (statistics ? ' ' + resolveShareSelector(statistics.doneShare) : '')}"></div>
                                <div class="success-rate-container">
                                    <div class=success-rate-success>${statistics ? statistics.done : 0}</div>
                                    <div class=success-rate-slash>/</div>
                                    <div class=success-rate-fail>${statistics ? statistics.failed : 0}</div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="worker-block-info-container">
                        <div class="worker-skills-info-block">${data.skills ? data.skills : ' '}</div>
                    </div>
                </div>
                </li>`
    }
}

function notificationElement(data) {
    return `<div id="${data.id}">${data.note}<i>   ${data.fireTime}</i></div>`;
}

function taskRedirect(taskId) {
    const toUrl = root() + '/tasks/' + taskId;
    console.log(toUrl)
    if (!isNaN(taskId)) {
        document.location = toUrl;
    }
}

$(document).on('click', '.task-block', function () {
    taskRedirect($(this).attr('id'));
});

$(document).on('mouseenter', '#notification-bell', function () {
    const counter = $('#notification-counter');
    if (counter.is(':empty')) {
        return;
    }
    counter.empty();

    let data = [];

    $('#notification-list div').each(function () {
        const noteId = $(this).attr('id');
        if (!isNaN(noteId)) {
            data.push(noteId);
        }
    });

    rest("PUT", "/notifications", data, function (data) {
        if (data.error) {
            console.error("PUT " + api() + "/notifications", data.errors[0], data.error);
            return;
        }
        console.log(data);
    })
});

window.addEventListener("DOMContentLoaded", () => {
    rest("GET", "/notifications", null, function (data) {
        if (data.error) {
            console.error("GET " + api() + "/notifications", data.errors[0], data.error);
            return;
        }
        console.log(data);

        const notifications = $('#notification-list');
        let unread = 0;

        for (var i = 0; i < data.length; i++) {
            notifications.append(notificationElement(data[i]));
            if (data[i].read === false) {
                unread++;
            }
        }
        const counter = $('#notification-counter');
        counter.empty();
        counter.append(unread > 0 ? unread : '');
    });
});