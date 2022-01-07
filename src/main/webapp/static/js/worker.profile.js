window.addEventListener("DOMContentLoaded", () => {
    let profileCache;
    let tasksCache = new Map();

    function tasks() {
        if (profileCache && !isNaN(profileCache.id)) {
            let list = $("#task-block-list");
            list.empty();
            rest(
                "GET",
                "/worker/" + profileCache.id + "/tasks",
                null,
                function (data) {
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
            );
        }
    }

    function appendIfNonEmpty(element, value, needHref) {
        if (value ) {
            element.empty();
            element.append(needHref ? `<a href="${needHref + value}">${needHref + value}</a>` : `${value}`);
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
        appendIfNonEmpty($("#contacts-vk"),  data.contacts.vk, 'https://vk.com/');
        appendIfNonEmpty($("#contacts-email"), data.contacts.email, false);
        appendIfNonEmpty($("#contacts-telegram"), data.contacts.telegram, 'https://t.me/');
        appendIfNonEmpty($("#contacts-facebook"), data.contacts.facebook, 'https://facebook.com/');
        appendIfNonEmpty($("#contacts-other"), data.contacts.other, false);
    }

    function updatePopupManager(data) {
        $("#popup-profile-name").empty().append(`${data.name}`);
        $("#popup-profile-age").empty().append(`${data.age}`);
        appendIfNonEmpty($("#popup-contacts-phone"), data.contacts.phoneNumber, false);
        appendIfNonEmpty($("#popup-contacts-vk"), data.contacts.vk, 'https://vk.com/');
        appendIfNonEmpty($("#popup-contacts-email"), data.contacts.email, false);
        appendIfNonEmpty($("#popup-contacts-telegram"), data.contacts.telegram, 'https://t.me/');
        appendIfNonEmpty($("#popup-contacts-facebook"), data.contacts.facebook, 'https://facebook.com/');
        appendIfNonEmpty($("#popup-contacts-other"), data.contacts.other, false);
    }

    function updateProfileEditForm(data) {
        $("#edit-name").val(data.name);
        $("#edit-firstname").val(data.firstName);
        $("#edit-patronym").val(data.patronym);
        $("#edit-surname").val(data.surname);
        $("#edit-birthdate").val(data.birthDate);
        $("#edit-skills").val(data.skills);
        $("#edit-phone").val(data.contacts.phoneNumber);
        $("#edit-email").val(data.contacts.email);
        $("#edit-vk").val(data.contacts.vk);
        $("#edit-telegram").val(data.contacts.telegram);
        $("#edit-facebook").val(data.contacts.facebook);
        $("#edit-other").val(data.contacts.other);
    }

    function updateProfile() {
        if ($(document).ready() && profileCache && !isNaN(profileCache.id)) {
            rest("GET", "/" + profileCache.id + "/profile", null, function (data) {
                console.log(data);
                updateWorkerProfile(data);
                updateProfileEditForm(data);
                profileCache = data;
            });
        }
    }

    function req() {
        rest("GET", "/worker", null, function (data) {
            console.log(data);
//                $(".profilePhoto").attr("src", `${data[0].photo}`);
            updateWorkerProfile(data);
            profileCache = data;
            tasks();
        });
    }
    req();

    $("#refresh-button").click(function () {
        $("#task-block-list").empty();
        tasks();
    });

    $(document).on('click','.task-person', function (event) {
        event.preventDefault();
        event.stopPropagation();
        event.stopImmediatePropagation();

        console.log("Loading person...");

        function loadManager(managerId) {
            rest("GET", "/manager/" + managerId, null, function (data) {
                console.log(data);
//                $(".profilePhoto").attr("src", `${data[0].photo}`);
                updatePopupManager(data);
            })
        }

        const personId = $(this).attr('id');
        console.log(personId);

        loadManager(personId);
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

    $(document).on('click', '#edit-profile-button', function () {
        if (profileCache && !isNaN(profileCache.id)) {
            updateProfileEditForm(profileCache);
        } else {
            updateProfile();
        }
    });

    $(document).on('submit','#profile-edit-form', function (event) {
        event.preventDefault();

        let element = $("#profile-edit-submit");
        element.attr("disabled", "disabled");
        element.addClass("inactivated");
        setTimeout(function () {
            $("#profile-edit-submit").removeAttr("disabled").removeClass("inactivated");
        }, 5000);

        let form = {};

        form['profileName'] = $("#edit-name").val();
        form['firstName'] = $("#edit-firstname").val();
        form['patronym'] = $("#edit-patronym").val();
        form['surname'] = $("#edit-surname").val();
        form['birthDate'] = $("#edit-birthdate").val();
        form['sex'] = $("input[type='radio'][name='sex']:checked").val();
        form['skills'] = $("#edit-skills").val();
        form['phoneNumber'] = $("#edit-phone").val();
        form['email'] = $("#edit-email").val();
        form['vk'] = $("#edit-vk").val();
        form['telegram'] = $("#edit-telegram").val();
        form['facebook'] = $("#edit-facebook").val();
        form['other'] = $("#edit-other").val();

        rest("POST", "/worker/" + profileCache.id, form, function (data) {
            if (data.error) {
                console.error("POST " + api() + "/worker/" + profileCache.id, data.errors[0], data.error);
                Swal.fire({
                    text: data.error,
                    type: 'error',
                    confirmButtonColor: '#fb2a79'
                });
                return;
            }
            console.log(data);

            updateWorkerProfile(data);
            updateProfileEditForm(data);
            profileCache = data;

            Swal.fire({
                text: 'Изменения успешно сохранены',
                confirmButtonColor: '#195fd4'
            });
        });
    });
});