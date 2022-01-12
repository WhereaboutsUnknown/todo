window.addEventListener("DOMContentLoaded", () => {
    let profileCache;
    let tasksCache = new Map();

    function tasks() {
        if (profileCache && !isNaN(profileCache.id)) {
            let list = $("#task-block-list");
            list.find('*').not('.persistent').remove();
            rest(
                "GET",
                "/worker/" + profileCache.id + "/tasks",
                null,
                function (data) {
                    console.log(data);
                    for (var i = 0; i < data.length; i++) {
                        tasksCache.set(data[i].id, data[i]);
                        list.append(taskElement(data[i]));
                    }
                }
            );
        }
    }

    function appendIfNonEmpty(element, value, needHref) {
        if (value ) {
            element.empty();
            element.append(needHref ? `<a href="${needHref + value}">${value}</a>` : `${value}`);
            element.parent('.contacts-info-item').attr('style', '');
        } else {
            element.parent('.contacts-info-item').attr('style', 'display: none');
        }
    }

    function updateStatistics(data) {
        if (data) {
            const workerStars = data.points;
            appendIfNonEmpty($('#star-rating-counter'), (workerStars ? Math.floor(workerStars * 100) / 100 : 0.0), false);
            const scale = $('#success-scale');
            if (data.doneShare) {
                scale.addClass(resolveShareSelector(data.doneShare));
            } else {
                scale.attr('background-image', 'none');
            }
            if (workerStars) {
                const starSelectors = resolveStarSelectors(workerStars);
                if (starSelectors.length === 5) {
                    for (let i = 0; i < 5; i++) {
                        $('#star-' + (i + 1)).addClass(starSelectors[i]);
                    }
                } else {
                    console.error("Error on rating rendering: ", starSelectors);
                }
            }
            appendIfNonEmpty($('#tasks-done-counter'), (data.done ? data.done : 0), false);
            appendIfNonEmpty($('#tasks-failed-counter'), (data.failed ? data.failed : 0), false);
        }
        else {
            console.error("No statistics found!");
        }
    }

    function updateWorkerProfile(data) {
        $("#profile-name").empty().append(`${data.name}`);
        $("#profile-age").empty().append(`${data.age}`);
        $("#work-skills-info").empty().append(`${data.skills}`);
        updateStatistics(data.statistics);
        appendIfNonEmpty($("#contacts-phone"), data.contacts.phoneNumber, 'tel:');
        appendIfNonEmpty($("#contacts-vk"),  data.contacts.vk, 'https://vk.com/');
        appendIfNonEmpty($("#contacts-email"), data.contacts.email, 'mailto:');
        appendIfNonEmpty($("#contacts-telegram"), data.contacts.telegram, 'https://t.me/');
        appendIfNonEmpty($("#contacts-facebook"), data.contacts.facebook, 'https://facebook.com/');
        appendIfNonEmpty($("#contacts-other"), data.contacts.other, false);
    }

    function updatePopupManager(data) {
        $("#popup-profile-name").empty().append(`${data.name}`);
        $("#popup-profile-age").empty().append(`${data.age}`);
        appendIfNonEmpty($("#popup-contacts-phone"), data.contacts.phoneNumber, 'tel:');
        appendIfNonEmpty($("#popup-contacts-vk"), data.contacts.vk, 'https://vk.com/');
        appendIfNonEmpty($("#popup-contacts-email"), data.contacts.email, 'mailto:');
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
            if (data.error) {
                console.error("GET " + api() + "/worker", data.status.value, data.error);
                showError(data.error);
                return;
            }
            console.log(data);
//                $(".profilePhoto").attr("src", `${data[0].photo}`);
            updateWorkerProfile(data);
            profileCache = data;
            tasks();
        });
    }
    req();

    $("#refresh-button").click(function () {
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
                showError(data.error);
                return;
            }
            console.log(data);

            updateWorkerProfile(data);
            updateProfileEditForm(data);
            profileCache = data;

            showOk('Изменения успешно сохранены');
        });
    });

    $(document).on('click', '#confirm-delete', function () {
        rest("DELETE", "/profile/worker/" + profileCache.id, null, function (data) {
            if (data.error) {
                console.error("DELETE " + api() + "/profile/worker/" + profileCache.id, data.errors[0], data.error);
                showError(data.error);
                return;
            }
            console.log(data);
            showDone(data.message);
            redirectTimer(3000, root());
        });
    });

    $(document).on('submit', '#avatar-edit-form', function (event) {
        event.preventDefault();

        let element = $("#avatar-submit");
        element.attr("disabled", "disabled");
        element.addClass("inactivated");
        setTimeout(function () {
            $("#profile-edit-submit").removeAttr("disabled").removeClass("inactivated");
        }, 5000);

        const selectedFile = document.getElementById('edit-avatar-file').files[0];
        let formData = new FormData(document.getElementById('avatar-edit-form'));

        submitForm("/file-service/upload/avatar?file=" + selectedFile.name, formData, function (data) {
            if (data.error) {
                console.error("POST " + api() + "/file-service/upload/avatar?=" + selectedFile.name, data.errors[0], data.error);
                showError(data.error);
                return;
            }
            showDone(data.message);
            redirectTimer(2000, root());
        })
    });
});