class TodoApp {
    constructor() {
        this.todoCache = new Map();
        this.profileCallbacks = [];
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

    setProfileLoadCallback(callback) {
        this.profileCallbacks.push(callback);
    }

    fireProfileCallbacks() {
        for (let i = 0; i < this.profileCallbacks.length; i++) {
            this.profileCallbacks[i]();
        }
    }
}

const Todo = new TodoApp();
const months = ['янв.', 'фев.', 'мар.', 'апр.', 'мая', 'июня', 'июля', 'авг.', 'сен.', 'окт.', 'ноя.', 'дек.'];

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
        if (body || typeof body == "boolean") {
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

function replaceElementText(element, text) {
    if (element) {
        element.empty().append(text ? text : '');
    }
}

function fillBlockFrom(block, elementList) {
    if (block && elementList) {
        block.find('*').not('.persistent').remove();
        for (let i = 0; i < elementList.length; i++) {
            block.append(elementList[i]);
        }
    }
}

function resolveMonth(month) {
    if (!isNaN(month)) {
        return months[month];
    } else {
        console.error("ERROR: invalid month number " + month);
    }
}

function formatDatetime(datetime) {
    if (datetime) {
        if (!datetime.day) {
            datetime = new Date(datetime);
            return `${datetime.getDay()} ${resolveMonth(datetime.getMonth())} ${datetime.getFullYear()} г., ${datetime.getHours()}:${datetime.getMinutes()}`;
        }
        return `${datetime.day} ${resolveMonth(datetime.month)} ${datetime.year} г., ${datetime.hour}:${datetime.minute}`;
    } else {
        console.error("ERROR: invalid datetime");
    }
}

function showError(message) {
    Swal.fire({
        icon: 'error',
        text: message,
        title: 'Ошибка!',
        confirmButtonColor: '#fb2a79'
    });
}

function showOk(message) {
    Swal.fire({
        text: message,
        confirmButtonColor: '#195fd4'
    });
}

function showWarning(title, message) {
    Swal.fire({
        icon: 'warning',
        title: title,
        text: message,
        confirmButtonColor: '#195fd4'
    });
}

function showDone(message) {
    Swal.fire({
        icon: 'success',
        title: message,
        showConfirmButton: false,
        timer: 1500
    });
}

function showDiscreetDone(message) {
    Swal.fire({
        position: 'bottom-end',
        html: `<p class="swal-discreet-done"><img src="/static/images/ok-icon.png" alt=""/>${message}</p>`,
        showConfirmButton: false,
        width: 600,
        color: '#ffffff',
        background: '#4bb84e',
        backdrop: `transparent`,
        timer: 3000
    })
}

function showSimpleDialog(text, yesButton, onConfirm) {
    Swal.fire({
        text: text,
        showCancelButton: true,
        confirmButtonText: yesButton,
        cancelButtonText: 'Назад',
        confirmButtonColor: '#195fd4'
    }).then((result) => {
        if (result.isConfirmed) {
            onConfirm();
        }
    });
}

function showDialog(title, hint, yesButton, noButton, onConfirm, onDeny) {
    Swal.fire({
        title: title,
        text: hint,
        icon: 'warning',
        showDenyButton: true,
        showCancelButton: false,
        confirmButtonText: yesButton,
        denyButtonText: noButton,
        confirmButtonColor: '#195fd4',
        denyButtonColor: '#fb2a79'
    }).then((result) => {
        if (result.isConfirmed) {
            onConfirm();
        } else if (result.isDenied) {
            onDeny();
        }
    })
}

function showTemplateDialog(template, hint, yesButton, noButton, onConfirm, onDeny) {
    Swal.fire({
        html: template,
        text: hint,
        showDenyButton: true,
        showCancelButton: true,
        confirmButtonText: yesButton,
        denyButtonText: noButton,
        cancelButtonText: 'Назад',
        confirmButtonColor: '#195fd4',
        denyButtonColor: '#fb2a79',
    }).then((result) => {
        if (result.isConfirmed) {
            onConfirm();
        } else if (result.isDenied) {
            onDeny();
        }
    })
}

function showTemplateMessage(template, hint) {
    Swal.fire({
        html: template,
        text: hint
    })
}

function alertElement(data) {
    if (data) {
        if (data.errorAlert) {
            return `<div class="alert-container" jumphint="${data.errorAlert}">
                        <img src="/static/images/error.png" alt="">
                    </div>`;
        } else if (data.warningAlert) {
            return `<div class="alert-container" jumphint="${data.warningAlert}">
                        <img src="/static/images/warning.png" alt="">
                    </div>`;
        } else if (data.infoAlert) {
            return `<div class="alert-container" jumphint="${data.infoAlert}">
                        <img src="/static/images/info.png" alt="">
                    </div>`;
        }
    }
    return '';
}

function taskElement(data) {
    const person = data.person;
    const btnClass = (person ? '' : ' inactivated');
    return `
                        <li>
                            <div class="task-block" id="${data.id}">
                                <h3 class="task-status" id="task-status">
                                    ${data.status}
                                    ${alertElement(data.alert)}
                                    ${alertElement(data.responsesInfo)}
                                </h3>
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
                                            <p class="task-deadline" id="task-deadline">${data.deadline ? data.deadline.replace('T', ' ') : ''}</p>
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
    return `<div id="${data.id}"><p>${data.note ? data.note : ''}</p><p><i>${data.fireTime ? data.fireTime : ''}</i></p></div>`;
}

function taskRedirect(taskId) {
    const toUrl = root() + '/tasks/' + taskId;
    console.log(toUrl)
    if (!isNaN(taskId)) {
        document.location = toUrl;
    }
}

function extractNumber(line) {
    if (line) {
        let num = line.match(/\d+/)[0];
        if (!isNaN(num)) {
            return num;
        }
    }
    return 0;
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

    let read = [];

    $('#notification-list div').each(function () {
        const noteId = $(this).attr('id');
        if (!isNaN(noteId)) {
            read.push(noteId);
        }
    });

    rest("PUT", "/notifications", read, function (data) {
        if (data.error) {
            console.error("PUT " + api() + "/notifications", data.errors[0], data.error);
            return;
        }
        console.log(data);
    })
});

window.addEventListener("DOMContentLoaded", () => {
    rest("GET", "/profile", null, function (data) {
        if (data.error) {
            console.error("GET " + api() + "/profile", data.errors[0], data.error);
            return;
        }
        console.log(data);
        Todo.store("profileCache", data);
        Todo.fireProfileCallbacks();
    });

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