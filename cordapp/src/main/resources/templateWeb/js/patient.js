"use strict";


const app = angular.module('MedicordAppModule', ['ui.bootstrap']);

// Fix for unhandled rejections bug.
app.config(['$qProvider', function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);

app.controller('PatientAppController', function($http, $location, $uibModal) {
    const patientApp = this;

    // We identify the node.
    const apiBaseURL = "/api/patient/";
    let peers = [];

    $http.get(apiBaseURL + "me").then((response) => patientApp.thisNode = response.data.me);

    $http.get(apiBaseURL + "peers").then((response) => peers = response.data.peers);

    patientApp.openModal = () => {
        const modalInstance = $uibModal.open({
            templateUrl: 'patient.html',
            controller: 'ModalInstanceCtrl',
            controllerAs: 'modalInstance',
            resolve: {
                patientApp: () => patientApp,
                apiBaseURL: () => apiBaseURL,
                peers: () => peers
            }
        });

        modalInstance.result.then(() => {}, () => {});
    };

    patientApp.getPrescriptions = () => $http.get(apiBaseURL + "prescriptions")
            .then((response) => patientApp.prescriptions = Objects.keys(response.data)
                .map((key) => response.data[key].state.data)
                .reverse());

    patientApp.getAppointments = () => $http.get(apiBaseURL + "appointments")
            .then((response) => patientApp.appointments = Object.keys(response.data)
                .map((key) => response.data[key].state.data)
                .reverse());

    patientApp.getAppointments();
    patientApp.getPrescriptions();
});

app.controller('ModalInstanceCtrl', function ($http, $location, $uibModalInstance, $uibModal, patientApp, apiBaseURL, peers) {
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
                    patientApp.getIOUs();
                    patientApp.getMyIOUs();
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