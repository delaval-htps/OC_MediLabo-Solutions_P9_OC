console.log("errorMessage = " + errorMessage)
console.log("successMessage = " + successMessage)
console.log("bindingResult  = " + JSON.stringify(bindingResult))

let btnToCreatePatient = document.getElementById('btn-creation-patient')
let formToCreatePatient = document.getElementById('form-creation-patient')


//management of toast in page

const indexToast = document.getElementById('index-toast')
const toastBootstrap = bootstrap.Toast.getOrCreateInstance(indexToast)

if (successMessage != null && successMessage != '') {
    fillInToast('text-bg-danger', 'text-bg-success', successMessage);
}

if (errorMessage != null && errorMessage != '') {
    fillInToast('text-bg-success', 'text-bg-danger', errorMessage);
}

function fillInToast(classToRemove, classToAdd, message) {
    indexToast.classList.remove(classToRemove)
    indexToast.classList.add(classToAdd)
    document.getElementById('toast-content').textContent = message
    toastBootstrap.show()
}

// display patient record when click on table row

function rowClicked(patientId) {
    location.href = "/patient-record/" + patientId
}

// toggle form to create new patient

function toggleFormPatient() {
    if (formToCreatePatient.style.display === 'none') {
        formToCreatePatient.style.display = "block"
        btnToCreatePatient.classList.remove('btn-primary')
        btnToCreatePatient.classList.add('btn-outline-primary')
        btnToCreatePatient.innerText = 'Cancel'
    } else if (bindingResult != ''){
        formToCreatePatient.style.display = "none";
        btnToCreatePatient.classList.add('btn-primary')
        btnToCreatePatient.classList.remove('btn-outline-primary')
        btnToCreatePatient.innerText = 'Add new patient'
    }
}
