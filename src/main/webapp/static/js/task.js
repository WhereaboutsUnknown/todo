function renderHistory() {
    const history = $('#history');
    const records = history.text().split('[/]');
    history.empty();
    for (var i = 0; i < records.length; i++) {
        const element = `<div class="history-record">${records[i]}</div>`
        history.append(element);
    }
}

function reloadHistory(data) {
    if (data) {
        replaceElementText($('#history'), data);
        renderHistory();
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

function reloadGeneral(data) {
    replaceElementText($('#general-header'), data.header ? data.header : '');
    replaceElementText($('#general-status'), data.status ? data.status : '');
    replaceElementText($('#general-start'), data.plannedStart ? data.plannedStart : '');
    replaceElementText($('#general-creation'), data.creationTime ? data.creationTime : '');
    replaceElementText($('#general-deadline'), data.deadline ? data.deadline : '');
    replaceElementText($('#general-skills'), data.stack ? data.stack : '');
    replaceElementText($('#general-description'), data.description ? data.description : '');
}

function reloadStaff(data) {
    replaceElementText($('#creator-manager-name'), data.creator ? data.creator : '');
    replaceElementText($('#current-manager-name'), data.manager ? data.manager : '');
    replaceElementText($('#unit-name'), data.unit ? data.unit : '');
}

function reloadFileSection(data) {

}

function reloadAll(task) {
    reloadHistory(task.history);
    reloadGeneral(task);
    reloadStaff(task);
    reloadFileSection(task);
}

window.addEventListener("DOMContentLoaded", () => {
    renderHistory();
    const taskId = $('.main-task-container').attr('id');
    if (taskId && !isNaN(taskId)) {
        Todo.store("currentTask", taskId);
    } else {
        console.error("Ошибка: не удалось сохранить ID выбранной задачи!");
    }
    $('#general')[0].scrollIntoView();

    $(document).on('submit', '#task-file-form', function (event) {
        event.preventDefault();

        showDialog('Сохранить файл?', '', 'Сохранить', 'Отменить',
            function () {
                let element = $("#submit-task-file");
                element.attr("disabled", "disabled");
                element.addClass("inactivated");
                setTimeout(function () {
                    element.removeAttr("disabled").removeClass("inactivated");
                }, 5000);

                const selectedFile = document.getElementById('file-input').files[0];
                let formData = new FormData(document.getElementById('task-file-form'));

                submitForm("/file-service/upload?file=" + selectedFile.name + "&taskId=" + taskId, formData, function (data) {
                    if (data.error) {
                        console.error("POST " + api() + "/file-service/upload", selectedFile.name, data.errors[0], data.error);
                        showError(data.error);
                        return;
                    }
                    showDone(data.message);
                    setTimeout(function () {
                        location.reload();
                    }, 2000);
                })
            }, function () {

        });
    });
});