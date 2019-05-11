"use strict";


const app = angular.module('MedicordAppModule', ['ui.bootstrap']);

// Fix for unhandled rejections bug.
app.config(['$qProvider', function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);

app.controller('PatientAppController', function($http, $location, $uibModal) {
    const doctorApp = this;

    // We identify the node.
    const apiBaseURL = "/api/doctor/";
    let peers = [];

    $http.get(apiBaseURL + "me").then((response) => doctorApp.thisNode = response.data.me);

    $http.get(apiBaseURL + "peers").then((response) => peers = response.data.peers);

    doctorApp.openModal = () => {
        const modalInstance = $uibModal.open({
            templateUrl: 'doctor.html',
            controller: 'ModalInstanceCtrl',
            controllerAs: 'modalInstance',
            resolve: {
                doctorApp: () => doctorApp,
                apiBaseURL: () => apiBaseURL,
                peers: () => peers
            }
        });

        modalInstance.result.then(() => {}, () => {});
    };

    doctorApp.getPrescriptions = () => $http.get(apiBaseURL + "prescriptions")
            .then((response) => doctorApp.prescriptions = Objects.keys(response.data)
                .map((key) => response.data[key].state.data)
                .reverse());

    doctorApp.getAppointments = () => $http.get(apiBaseURL + "appointments")
            .then((response) => doctorApp.appointments = Object.keys(response.data)
                .map((key) => response.data[key].state.data)
                .reverse());

    doctorApp.getAppointments();
    doctorApp.getPrescriptions();
});

app.controller('ModalInstanceCtrl', function ($http, $location, $uibModalInstance, $uibModal, doctorApp, apiBaseURL, peers) {
    const modalInstance = this;

    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;

    // Validate and create IOU.
    modalInstance.create = () => {
        if (invalidFormInput()) {
            modalInstance.formError = true;
        } else {
            modalInstance.formError = false;

            $uibModalInstance.close();

            const createIOUEndpoint = `${apiBaseURL}create-iou?partyName=${modalInstance.form.counterparty}&iouValue=${modalInstance.form.value}`;

            // Create PO and handle success / fail responses.
            $http.put(createIOUEndpoint).then(
                (result) => {
                    modalInstance.displayMessage(result);
                    doctorApp.getIOUs();
                    doctorApp.getMyIOUs();
                },
                (result) => {
                    modalInstance.displayMessage(result);
                }
            );
        }
    };

    modalInstance.displayMessage = (message) => {
        const modalInstanceTwo = $uibModal.open({
            templateUrl: 'messageContent.html',
            controller: 'messageCtrl',
            controllerAs: 'modalInstanceTwo',
            resolve: { message: () => message }
        });

        // No behaviour on close / dismiss.
        modalInstanceTwo.result.then(() => {}, () => {});
    };

    // Close create IOU modal dialogue.
    modalInstance.cancel = () => $uibModalInstance.dismiss();

    // Validate the IOU.
    function invalidFormInput() {
        return isNaN(modalInstance.form.value) || (modalInstance.form.counterparty === undefined);
    }
});

// Controller for success/fail modal dialogue.
app.controller('messageCtrl', function ($uibModalInstance, message) {
    const modalInstanceTwo = this;
    modalInstanceTwo.message = message.data;
});