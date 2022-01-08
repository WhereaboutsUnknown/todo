function root() {
    return 'http://localhost:8080';
}

function api() {
    return 'http://localhost:8080';
}

function avatarEndpoint(avatarId) {
    return api() + "/file-service/user/file/" + avatarId;
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