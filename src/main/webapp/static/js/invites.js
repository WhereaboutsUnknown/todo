window.addEventListener("DOMContentLoaded", () => {
    function inviteOpenElement(task) {
        if (task) {
            return `<div class="invite-open-container">
                    <div class="invite-open-task-block">
                        <a class="invite-open-task-header" href="${'/tasks/' + task.id}">${task.header ? task.header : ''}</a>
                        <p class="invite-open-task-status">${task.status ? task.status : ''}</p>
                        <p class="invite-open-task-stack">${task.stack ? task.stack : ''}</p>
                        <p class="invite-open-task-stack">${task.description ? task.description : ''}</p>
                        <div class="invite-open-task-deadline">${task.deadline ? 'Срок завершения: ' + formatDatetime(task.deadline) : ''}</div>
                    </div>
                    </div>`;
        }
    }

    function inviteElement(invite) {
        return `<div class="bottomless-stack-element invite-block" id="${invite.id}">
                    <div class="invite-task-header">
                        <p>${invite.task ? invite.task : ''}</p>
                    </div>
                    <div class="invite-time">${invite.creationTime ? invite.creationTime : ''}</div>
                </div>`;
    }

    function reloadInvites(initial) {
        const profile = Todo.getValue('profileCache');
        if (profile && profile.id && !isNaN(profile.id)) {
            const url = `/invite/api/worker/${profile.id}`;
            rest("GET", url, null, function (data) {
                if (data.error) {
                    console.error("GET " + api() + `/invite/api/worker/${profile.id}`, data.status.value, data.error);
                    showError(data.error);
                    return;
                }
                const elements = data.map(invite => inviteElement(invite));
                fillBlockFrom($('#bottomless-stack-element-list'), elements);
            });
        } else {
            console.error("ERROR: ", profile);
        }
    }

    function processInvite(inviteId, accepted) {
        rest("GET", `/invite/api?id=${inviteId}&accept=${accepted}`, null, function (data) {
            if (data.error) {
                console.error("GET " + api() + `/invite/api`, data.status.value, data.error);
                showError(data.error);
                return;
            }
            reloadInvites();
            showDone(data.message);
        });
    }

    if (Todo.getValue('profileCache')) {
        reloadInvites(true);
    } else {
        Todo.setProfileLoadCallback(reloadInvites);
    }

    $(document).on('click', '.invite-block', function () {
        const profile = Todo.getValue('profileCache');
        const inviteId = $(this).attr('id');
        if (profile && profile.id && !isNaN(profile.id) && inviteId && !isNaN(inviteId)) {
            rest("GET", `/invite/api/worker/${profile.id}/${inviteId}`, null, function (data) {
                if (data.error) {
                    console.error("GET " + api() + `/invite/api/worker/${profile.id}/${inviteId}`, data.status.value, data.error);
                    showError(data.error);
                    return;
                }
                showTemplateDialog(inviteOpenElement(data), 'Принять приглашение на задачу?', 'Принять', 'Отклонить',
                    function () {
                        processInvite(inviteId, true);
                    }, function () {
                        processInvite(inviteId, false);
                    });
            });
        } else {
            console.error("ERROR: ", profile);
        }
    });

    sortBtn().click(function () {
        reloadInvites();
    });

    searchBtn().click(function () {
        reloadInvites();
    });

    searchInput().on('keyup', function (e) {
        e.preventDefault();
        if (e.key === 'Enter' || e.keyCode === 13) {
            reloadInvites();
        }
    });
});