window.addEventListener("DOMContentLoaded", () => {
    function responseOpenElement(response) {
        const task = response.task;
        const worker = response.worker;
        if (task && worker) {
            const statistics = worker.statistics;
            const stars = resolveStarSelectors(statistics.points ? statistics.points : 0.0);
            return `<div class="response-open-container">
                    <div class="response-open-task-block">
                        <a class="response-open-task-header" href="${'/tasks/' + task.id}">${task.header ? task.header : ''}</a>
                        <p class="response-open-task-stack">${task.stack ? task.stack : ''}</p>
                        <div class="response-open-deadline">${task.deadline ? 'Срок завершения: ' + formatDatetime(task.deadline) : ''}</div>
                    </div>
                    <div class="response-open-worker-block" id="${worker.id}">
                        <div class="response-worker-name">${worker.name ? worker.name : ' '}</div>
                        <img class="response-worker-avatar" src="${avatarEndpoint(worker.avatar)}" alt="Фото">
                        <div class="worker-statistics-container" id="response-worker-statistics">
                            <div class="stars">
                                <span class="${'star' + (statistics ? ' ' + stars[0] : '')}" id="star-1"></span>
                                <span class="${'star' + (statistics ? ' ' + stars[1] : '')}" id="star-2"></span>
                                <span class="${'star' + (statistics ? ' ' + stars[2] : '')}" id="star-3"></span>
                                <span class="${'star' + (statistics ? ' ' + stars[3] : '')}" id="star-4"></span>
                                <span class="${'star' + (statistics ? ' ' + stars[4] : '')}" id="star-5"></span>
                            </div>
                            <div class="star-rating" id="star-rating-counter">${statistics ? statistics.points : 0.0}</div>
                            <div class="${'success-scale' + (statistics ? ' ' + resolveShareSelector(statistics.doneShare) : '')}"></div>
                            <div class="success-rate-container">
                                <div class=success-rate-success id="response-tasks-done-counter">${statistics ? statistics.done : 0}</div>
                                <div class=success-rate-slash>/</div>
                                <div class=success-rate-fail id="response-tasks-failed-counter">${statistics ? statistics.failed : 0}</div>
                            </div>
                        </div>
                        <div class="response-worker-info-block">${worker.skills ? worker.skills : ' '}</div>
                    </div>`;
        }
    }

    function responseElement(response) {
        return `<div class="bottomless-stack-element response-block" id="${response.id}">
                    <div class="response-task-header">
                        <p>${response.task ? response.task : ''}</p>
                    </div>
                    <div class="response-worker-name">
                        <p>${response.worker ? response.worker : ''}</p>
                    </div>
                    <div class="response-time">${response.creationTime ? response.creationTime : ''}</div>
                </div>`;
    }

    function reloadResponses(initial) {
        const profile = Todo.getValue('profileCache');
        if (profile && profile.id && !isNaN(profile.id)) {
            const url = `/manager/${profile.id}/responses`;
            rest("GET", (initial ? addSortAndReq(url) : url), null, function (data) {
                if (data.error) {
                    console.error("GET " + api() + `/manager/${profile.id}/responses`, data.status.value, data.error);
                    showError(data.error);
                    return;
                }
                const elements = data.map(response => responseElement(response));
                fillBlockFrom($('#bottomless-stack-element-list'), elements);
            });
        } else {
            console.error("ERROR: ", profile);
        }
    }

    function processResponse(profileId, responseId, accepted) {
        rest(accepted ? "PUT" : "DELETE", `/manager/${profileId}/responses/${responseId}`, null, function (data) {
            if (data.error) {
                console.error((accepted ? "PUT " : "DELETE ") + api() + `/manager/${profileId}/responses`, data.status.value, data.error);
                showError(data.error);
                return;
            }
            reloadResponses();
            showDone(accepted ? 'Отклик принят!' : 'Отклик отклонен!');
        });
    }

    if (Todo.getValue('profileCache')) {
        reloadResponses(true);
    } else {
        Todo.setProfileLoadCallback(reloadResponses);
    }

    $("#refresh-button").click(function () {
        reloadResponses();
    });

    $(document).on('click', '.response-block', function () {
        const profile = Todo.getValue('profileCache');
        const responseId = $(this).attr('id');
        if (profile && profile.id && !isNaN(profile.id) && responseId && !isNaN(responseId)) {
            rest("GET", `/manager/${profile.id}/responses/${responseId}`, null, function (data) {
                if (data.error) {
                    console.error("GET " + api() + `/manager/${profile.id}/responses`, data.status.value, data.error);
                    showError(data.error);
                    return;
                }
                showTemplateDialog(responseOpenElement(data), 'Принять отклик на задачу?', 'Принять', 'Отклонить',
                    function () {
                        processResponse(profile.id, responseId, true);
                    }, function () {
                        processResponse(profile.id, responseId, false);
                });
            });
        } else {
            console.error("ERROR: ", profile);
        }
    });

    sortBtn().click(function () {
        reloadResponses();
    });

    searchBtn().click(function () {
        reloadResponses();
    });

    searchInput().on('keyup', function (e) {
        e.preventDefault();
        if (e.key === 'Enter' || e.keyCode === 13) {
            reloadResponses();
        }
    });
});