window.addEventListener("DOMContentLoaded", () => {
    const taskId = extractNumber($('.kanban-header-container').attr('id'));
    Todo.store("currentTask", taskId);

    function comment(data) {
        const author = data.author;
        const isOwn = (typeof data.isOwn === 'boolean' && data.isOwn === true);
        return `<div class="${'comment-block' + (isOwn ? ' own-comment' : '')}">
                <div class="${'worker-level' + (!isOwn && author ? ' present' : '')}">
                    <img width="20" height="20" style="${author ? '' : 'display: none'}" src="${author ? avatarEndpoint(author.avatar) : ''}" alt="Avatar" title="${author ? (author.name ? author.name : '') : ''}">
                </div>
                <div class="comment-text">${data.text ? data.text : ''}</div>
                <div class="comment-time">${data.time ? formatDatetime(data.time) : ''}</div>
            </div>`
    }

    function kanbanTicket(data) {
        const worker = data.worker;
        return `<div class="ticket-block" draggable="true" id="${'ticket' + data.id}">
                <div class="${'worker-level' + worker ? ' present' : ''}">
                    <img width="20" height="20" style="${worker ? '' : 'display: none'}" src="${worker ? avatarEndpoint(worker.avatar) : ''}" alt="Avatar">
                </div>
                <div class="name-level">${data.name ? data.name : ''}</div>
                <div class="deadline-level">${data.deadline ? formatDatetime(data.deadline) : ''}</div>
            </div>`
    }

    function ticketEditForm(data) {
        const worker = data.worker;
        const creator = data.creator;
        return `<div class="open-ticket-block">
            <div class="open-ticket-header">
                <input type="text" class="ticket-input ticket-header-input" id="edit-header" name="header" value="${data.name ? data.name : ''}">
            </div>
            <div class="open-ticket-description">
                <textarea name="info" class="ticket-input ticket-description-input" id="edit-description" rows="5">${data.description ? data.description : ''}</textarea>
                <label for="edit-deadline">Дедлайн</label>
                <input type="datetime-local" class="ticket-input ticket-deadline-input" id="edit-deadline" name="deadline" min="2022-01-01T00:00" max="2070-01-01T00:00" value="${data.deadline ? data.deadline : new Date()}">
            </div>
            <div class="open-ticket-time">
                <div class="created-at" id="created-at">${data.creationDate ? ('Создано: ' + formatDatetime(data.creationDate)) : ''}</div>
                <div class="finished-at" id="finished-at">${data.finishTime ? ('Завершено: ' + formatDatetime(data.finishTime)) : ''}</div>
            </div>
            <div class="open-ticket-persons">
                <div class="ticket-creator ticket-person" id="ticket-creator">
                    <div class="ticket-person-name">${'Создано: ' + (creator ? creator.name : '')}</div>
                    <img width="20" height="20" style="${creator ? '' : 'display: none'}" src="${creator ? avatarEndpoint(creator.avatar) : ''}" alt="Avatar">
                </div>
                <div class="ticket-worker ticket-person" id="ticket-worker">
                    <div class="ticket-person-name">${'Исполнитель: ' + (worker ? worker.name : 'не назначен')}</div>
                    <img width="20" height="20" style="${worker ? '' : 'display: none'}" src="${worker ? avatarEndpoint(worker.avatar) : ''}" alt="Avatar">
                </div>
            </div>
            <div class="open-ticket-buttons">
                <div class="save-ticket-btn inactivated basic-button" id="save-ticket-btn" title="Сохранить"></div>
                <div class="${'claim-ticket-btn basic-button' + (worker ? ' disabled' : '')}" id="claim-ticket-btn" title="Взять в работу"></div>
                <div class="delete-ticket-btn basic-button" id="delete-ticket-btn" title="Удалить"></div>
            </div>
            <div class="open-ticket-comments-section">
                <div class="ticket-comments" id="ticket-comments"></div>
                <div class="ticket-comment-input">
                    <textarea name="comment" class="ticket-description-input" id="write-comment" rows="3"></textarea>
                    <div class="send-comment-btn basic-button" id="send-comment-btn"></div>
                </div>
            </div>
        </div>`
    }

    function ticketCreateForm() {
        return `<div class="open-ticket-block ticket-creation-form">
            <div class="open-ticket-header">
                <input type="text" class="ticket-input ticket-header-input" id="new-header" name="header" placeholder="Заголовок">
            </div>
            <div class="open-ticket-description">
                <textarea name="info" class="ticket-input ticket-description-input" id="new-description" rows="5" placeholder="Описание задачи"></textarea>
                <label for="new-deadline">Дедлайн</label>
                <input type="datetime-local" class="ticket-input ticket-deadline-input" id="new-deadline" name="deadline" min="2022-01-01T00:00" max="2070-01-01T00:00" value="${new Date()}">
            </div>
            <div class="open-ticket-buttons">
                <div class="create-ticket-btn basic-button" id="create-ticket-btn" title="Сохранить"></div>
            </div>
        </div>`
    }

    function refreshTicketEditForm(data) {
        if (data) {
            $('#edit-header').val(data.name ? data.name : '');
            $('#edit-description').val(data.description ? data.description : '');
            $('#edit-deadline').val(data.deadline ? formatDatetime(data.deadline) : '');
            const worker = data.worker;

            $('#ticket-worker').empty().append(
                `<div class="ticket-person-name">${'Исполнитель: ' + (worker ? worker.name : 'не назначен')}</div>
                <img width="20" height="20" style="${worker ? '' : 'display: none'}" src="${worker ? avatarEndpoint(worker.avatar) : ''}" alt="Avatar">`
            );
        }
    }

    function getTicketContent(mode) {
        if (mode !== 'edit' && mode !== 'new') {
            throw Error(`Wrong mode: ${mode}`);
        }
        const header = $(`#${mode}-header`).val();
        const description = $(`#${mode}-description`).val();
        const deadline = $(`#${mode}-deadline`).val();

        return {name: header, description: description, deadline: deadline};
    }

    function fillColumn(colNum, column) {
        Todo.store(`header-${colNum}`, column.id);

        const colElement = document.getElementById(`column-${colNum}`);
        $(colElement).removeClass('inactive-column');

        let header = document.getElementById(`header-${colNum}`)
        $(header).empty().append(column.name ? column.name : '');

        let container = $(colElement).find('.kanban-column-ticket-container');
        let tickets = column.tickets.map(ticket => kanbanTicket(ticket));
        fillBlockFrom(container, tickets);
    }

    function fillAllColumns(columns) {
        for (let colElement of columns) {
            if (colElement.order < 0) {
                colElement.order = Number.MAX_SAFE_INTEGER;
            }
        }
        columns = columns.sort((a, b) => a.order - b.order);
        for (let i = 0; i < columns.length; i++) {
            fillColumn(i === (columns.length -1) ? 5 : i, columns[i]);
        }
    }

    function fetchColumns() {
        const taskId = Todo.getValue('currentTask');
        rest("GET", `/tasks/${taskId}/kanban/ticket`, null, function (data) {
            if (data.error) {
                console.error(`GET ${api()}/tasks/${taskId}/kanban/ticket`, data.status.value, data.error);
                showDiscreetError(data.error);
                return;
            }
            fillAllColumns(data);
        });
    }

    function fetchComments(ticketId) {
        const taskId = Todo.getValue('currentTask');
        rest("GET", `/tasks/${taskId}/kanban/ticket/${ticketId}/comment`, null, function (data) {
            if (data.error) {
                console.error(`GET ${api()}/tasks/${taskId}/kanban/ticket/${ticketId}/comment`, data.status.value, data.error);
                return;
            }
            let commentSection = $('#ticket-comments');
            let comments = data.map(com => comment(com));
            fillBlockFrom(commentSection, comments);
            let textarea = document.getElementById("write-comment");
            textarea.scrollTop = textarea.scrollHeight;
        });
    }

    function closeTicketBox(e) {
        let openTicketBlock = $('.open-ticket-block');
        if ( !openTicketBlock.is(e.target) && openTicketBlock.has(e.target).length === 0 && !$.contains(openTicketBlock, e.target) ) {
            $('#ticket-opened').removeClass('lightbox-active');
            document.removeEventListener('mouseup', closeTicketBox);
        }
    }

    function activateTicketBox(content) {
        let box = document.getElementById('ticket-opened');
        $(box).addClass('lightbox-active');
        document.addEventListener('mouseup', closeTicketBox);
        $('#ticket-opened-inner').empty().append(content);
        return box;
    }

    fetchColumns();

    $(document).on('click', '.kanban-column-header', function (e) {
        const header = e.target;
        const headerValue = $(header).val();
        const taskId = Todo.getValue("currentTask");
        const columnId = Todo.getValue(`header-${header.id}`);
        $(header).empty().append(`<input type="text" class="kanban-column-header-input" id="kanban-column-header-input" value="${headerValue}">`);
        $(document).on('focusout', '#kanban-column-header-input', function (e) {
            const newHeader = $('#kanban-column-header-input').val();
            rest("PUT", `/tasks/${taskId}/kanban/manage/col/${columnId}`, {name: newHeader}, function () {
                if (data.error) {
                    console.error(`PUT ${api()}/tasks/${taskId}/kanban/manage/col/${columnId}`, data.status.value, data.error);
                    showDiscreetError(data.error);
                    return;
                }
                $(header).empty().append(data.name ? data.name : "");
            });
        });
    });

    $(document).on('click','#add-ticket-btn', function () {
        let createForm = ticketCreateForm();
        const box = activateTicketBox(createForm);

        $(document).on('click','#create-ticket-btn', function () {
            const taskId = Todo.getValue("currentTask");
            rest("POST", `/tasks/${taskId}/kanban/ticket`, getTicketContent('new'), function (data) {
                if (data.error) {
                    console.error(`GET ${api()}/tasks/${taskId}/kanban/ticket`, data.status.value, data.error);
                    showDiscreetError(data.error);
                    return;
                }
                $(box).removeClass('lightbox-active');
                $('#column-0').find('.kanban-column-ticket-container').append(kanbanTicket(data));
                showDiscreetDone("Задача успешно создана!");
            });
        });
    });

    $(document).on('click','.ticket-block', function () {
        const elementId = extractNumber($(this).attr('id'));
        const taskId = Todo.getValue("currentTask");

        function postComment() {
            const commentInput = $('#write-comment');
            const commentForm = {
                text: commentInput.val()
            };
            rest("POST", `/tasks/${taskId}/kanban/ticket/${elementId}/comment`, commentForm, function (data) {
                if (data.error) {
                    console.error(`POST ${api()}/tasks/${taskId}/kanban/ticket/${elementId}/comment`, data.status.value, data.error);
                    return;
                }
                commentInput.val('');
                fetchComments(elementId);
            });
        }

        rest("GET", `/tasks/${taskId}/kanban/ticket/${elementId}`, null, function (data) {
            if (data.error) {
                console.error(`GET ${api()}/tasks/${taskId}/kanban/ticket/${elementId}`, data.status.value, data.error);
                showError(data.error);
                return;
            }
            let editForm = ticketEditForm(data);
            const box = activateTicketBox(editForm);

            const saveBtn = $('#save-ticket-btn');

            fetchComments(elementId);

            $('.ticket-input').bind('input', function() {
                saveBtn.removeClass('inactivated');
            });

            $(document).on('click','#save-ticket-btn', function () {
                if (!$(this).hasClass('inactivated')) {
                    $(this).addClass('inactivated');
                    rest("PUT", `/tasks/${taskId}/kanban/ticket/${elementId}`, getTicketContent('edit'), function (data) {
                        if (data.error) {
                            console.error(`PUT ${api()}/tasks/${taskId}/kanban/ticket/${elementId}`, data.status.value, data.error);
                            showDiscreetError(data.error);
                            saveBtn.removeClass('inactivated');
                            return;
                        }
                        refreshTicketEditForm(data);
                    });
                }
            });

            $(document).on('click','#claim-ticket-btn', function () {
                rest("PUT", `/tasks/${taskId}/kanban/ticket/${elementId}/pick`, null, function (data) {
                    if (data.error) {
                        console.error(`PUT ${api()}/tasks/${taskId}/kanban/ticket/${elementId}/pick`, data.status.value, data.error);
                        showDiscreetError(data.error);
                        return;
                    }
                    $('#claim-ticket-btn').addClass('disabled');
                    refreshTicketEditForm(data);
                });
            });

            $(document).on('click', '#delete-ticket-btn', function () {
                rest("DELETE", `/tasks/${taskId}/kanban/ticket/${elementId}`, null, function (data) {
                    if (data.error) {
                        console.error(`DELETE ${api()}/tasks/${taskId}/kanban/ticket/${elementId}`, data.status.value, data.error);
                        showDiscreetError(data.error);
                        return;
                    }
                    $('#ticket-opened').removeClass('lightbox-active');
                    showDiscreetDone("Задача успешно удалена!");
                });
            })

            $(document).on('click', '#send-comment-btn', postComment);
            $("#write-comment").on('keyup', function (e) {
                if (e.key === 'Enter' || e.keyCode === 13) {
                    postComment();
                }
            });
        });
    });
});

