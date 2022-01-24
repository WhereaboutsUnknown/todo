window.addEventListener("DOMContentLoaded", () => {
    function invitedFragment(worker) {
        return `<div class="task-invited-worker subdivision-inline-element">
                        <img class="invited-avatar user-avatar" alt="" src="${'/file-service/user/file/' + worker.avatar + '?type=1'}">
                        <div class="invited-worker-name">${worker.name}</div>
                        <div class="cancel-invite-btn task-inline-icon" title="Отменить" id="${'cancel-' + worker.id}"></div>
                    </div>`;
    }

    function groupWorkerFragment(worker, editWorkers, responsible) {
        if (editWorkers) {
            return `<div class="task-group-worker subdivision-inline-element">
                        <img class="group-worker-avatar user-avatar" alt="" src="${'/file-service/user/file/' + worker.avatar + '?type=1'}">
                        <div class="group-worker-name">${worker.name}</div>
                        <div class="${(responsible ? 'responsible-star ' : 'worker-set-authority-btn ') + 'task-inline-icon'}" title="${responsible ? 'Ответственный исполнитель' : 'Назначить ответственным'}" id="${'set-' + worker.id}"></div>
                        <div class="worker-delete-btn task-inline-icon" title="Исключить" id="${'delete-' + worker.id}"></div>
                    </div>`;
        }
        return `<div class="task-group-worker subdivision-inline-element">
                        <img class="group-worker-avatar user-avatar" alt="" src="${'/file-service/user/file/' + worker.avatar + '?type=1'}">
                        <div class="group-worker-name">${worker.name}</div>
                        <div class="${(responsible ? 'responsible-star ' : '') + 'task-inline-icon'}" title="${responsible ? 'Ответственный исполнитель' : ''}" id=""></div>
                    </div>`;
    }

    function reloadGroup(workerList, responsibleId, editWorkers) {
        let elementList = [];
        for (let i = 0; i < workerList.length; i++) {
            let currentWorker = workerList[i];
            const isResponsible = (currentWorker.id === responsibleId);
            const element = groupWorkerFragment(workerList[i], editWorkers, isResponsible);
            elementList.push(element);
        }
        const block = $('#task-group-list');
        fillBlockFrom(block, elementList);
    }

    function reloadInvited(invitedList) {
        let elementList = [];
        for (let i = 0; i < invitedList.length; i++) {
            const element = invitedFragment(invitedList[i]);
            elementList.push(element);
        }
        const block = $('#task-invites-list');
        fillBlockFrom(block, elementList);
    }

    function reloadDependent(task) {
        const workers = task.group;
        const invited = task.invited;
        const responsibleId = task.worker ? task.worker.id : null;
        const editWorkers = task.editWorkers;

        reloadGroup(workers, responsibleId, editWorkers);
        reloadInvited(invited);
    }

    function performTaskAction(method, url, body, okMessage) {
        rest(method, url, body, function (data) {
            if (data.error) {
                console.error(method + ' ', api() + url, data.errors[0], data.error);
                return;
            }
            console.log(data);

            reloadDependent(data);
            reloadAll(data);
            if (okMessage) {
                showDiscreetDone(okMessage);
            }
        });
    }

    function setWorkerResponsible(workerId) {
        const managerId = Todo.getValue('profileCache').id;
        const taskId = Todo.getValue('currentTask');
        if (isNaN(managerId) || isNaN(taskId) || isNaN(workerId)) {
            console.error("Некорректный идентификатор");
            return;
        }
        performTaskAction("PUT", `/manager/${managerId}/tasks/${taskId}/worker?id=${workerId}`, null, 'Ответственный исполнитель изменен!');
    }

    function removeWorkerFromTask(workerId) {
        const managerId = Todo.getValue('profileCache').id;
        const taskId = Todo.getValue('currentTask');
        if (isNaN(managerId) || isNaN(taskId) || isNaN(workerId)) {
            console.error("Некорректный идентификатор");
            return;
        }
        performTaskAction("DELETE", `/manager/${managerId}/tasks/${taskId}/worker?id=${workerId}`, null, 'Исполнитель удален!');
    }

    function cancelTaskInvite(workerId) {
        const managerId = Todo.getValue('profileCache').id;
        const taskId = Todo.getValue('currentTask');
        if (isNaN(managerId) || isNaN(taskId) || isNaN(workerId)) {
            console.error("Некорректный идентификатор");
            return;
        }
        performTaskAction("DELETE", `/manager/${managerId}/tasks/${taskId}/invites?worker=${workerId}`, null, 'Приглашение отменено!');
    }

    $(document).on('click','#send-invites-btn', function () {
        const taskId = Todo.getValue("currentTask");
        loadWorkers(taskId, true);
    });

    $(document).on('click','#show-all-checkbox', function () {
        const needAll = !!$(this).hasClass('checked');
        if (needAll) {
            $(this).removeClass('checked');
        } else {
            $(this).addClass('checked');
        }
        const taskId = Todo.getValue("currentTask");
        loadWorkers(taskId, needAll);
    });

    $(document).on('click','#invite-workers-btn', function () {
        const taskId = Todo.getValue("currentTask");
        const profile = Todo.getValue("profileCache");
        if (profile) {
            showDialog("Пригласить исполнителей?", "Выбранным сотрудникам будут отправлены приглашения", "Отправить", "Отменить",
                function () {
                    const chosenWorkers = getChosenWorkerIds();
                    if (taskId && !isNaN(taskId) && profile.id && !isNaN(profile.id)) {
                        performTaskAction("POST", `/manager/${profile.id}/tasks/${taskId}/invites`, chosenWorkers, (chosenWorkers.length > 0 ? 'Приглашения отправлены!' : false));
                    } else {
                        console.error("Current task ID: ", taskId);
                    }
                }, function () {

                });
        } else {
            console.error("ERROR: ", profile);
        }
    });

    $(document).on('click','#cancel-task-btn', function () {
        const taskId = Todo.getValue("currentTask");
        const profile = Todo.getValue("profileCache");
        if (profile) {
            cancelTask(profile.id, taskId)
        } else {
            console.error("ERROR: ", profile);
        }
    });

    $(document).on('click','#review-task-btn', function () {
        const taskId = Todo.getValue("currentTask");
        const profile = Todo.getValue("profileCache");
        if (profile) {
            reviewTask(profile.id, taskId, function (data, approved) {
                if (data) {
                    reloadDependent(data);
                    reloadAll(data);
                    showDone(approved ? 'Задача выполнена!' : 'Задача возвращена на доработку!');
                } else {
                    console.error("Wrong server response: no data returned!");
                }
            });
        } else {
            console.error("ERROR: ", profile);
        }
    });

    $(document).on('click','#archive-task-btn', function () {

    });

    $(document).on('click','.worker-set-authority-btn', function () {
        const elementId = $(this).attr('id');
        const workerId = extractNumber(elementId);
        setWorkerResponsible(workerId);
    });

    $(document).on('click','.worker-delete-btn', function () {
        const elementId = $(this).attr('id');
        showSimpleDialog('Исключить сотрудника из группы исполнителей задачи?', 'Исключить', function () {
            const workerId = extractNumber(elementId);
            removeWorkerFromTask(workerId);
        });
    });

    $(document).on('click','.cancel-invite-btn', function () {
        const elementId = $(this).attr('id');
        showSimpleDialog('Аннулировать приглашение, отправленное сотруднику?', 'Аннулировать', function () {
            const workerId = extractNumber(elementId);
            cancelTaskInvite(workerId);
        });
    });
});