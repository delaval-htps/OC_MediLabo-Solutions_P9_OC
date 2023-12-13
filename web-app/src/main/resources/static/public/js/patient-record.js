const urlParams = new URLSearchParams(window.location.search)
/**
 * patient_update has two state one for edition only (value= false) and one for update (value= true)
 */
let patientUpdate = urlParams.get('patient_update')

/**
 * noteStage represents stage of notes:
 * - first one : 'all' , we have the list of all notes for the patient 
 * - second one : 'edition', we have just edit one note without be able to modify it
 * - third one : 'update' , we have one note for update it 
 */
let noteState = urlParams.get('note_state')


console.log('patientUpdate :' + patientUpdate)
console.log('noteState :' + noteState)
console.log('note: ' + JSON.stringify(note))

let formUpdatePatient = document.querySelector('#form-update-patient')

/* part for patient*/

if (patientUpdate === "true") {
    enablePersonalInformation()
}
if (patientUpdate === "false") {
    disablePersonalInformation()
}
function disablePersonalInformation() {
    document.querySelector('#div-submit-btn').style.display = "none"
    document.querySelector('#form-update-patient fieldset').setAttribute("disabled", "true")
    document.getElementById('genre').classList.replace('form-select', 'form-control')
    document.getElementById('dateOfBirth').disabled = true
    document.querySelector('.bi-pencil-square').style.display = 'block'
    document.querySelector('.bi-x-square').style.display = 'none'
    patientUpdate = false
    // TODO reset binding field in red
}

function enablePersonalInformation() {
    document.querySelector('#div-submit-btn').style.display = 'flex'
    document.querySelector('#form-update-patient fieldset').removeAttribute("disabled")
    document.getElementById('genre').classList.replace('form-control', 'form-select')
    document.getElementById('dateOfBirth').disabled = false
    document.querySelector('.bi-pencil-square').style.display = 'none'
    document.querySelector('.bi-x-square').style.display = 'block'
    patientUpdate = true
}

/* part for note creation */
let formNoteCreation = document.querySelector('#note-creation-form')
let noteTable = document.querySelector('#notes-table')
let noteDate = document.querySelector('#creation-note-date')
let creationNoteBtn = document.querySelector('#creation-note-btn')
let saveNoteBtn = document.querySelector('#save-note-btn')
let noteContent = document.querySelector('#creation-note-content')

if (noteState === 'all') {
    formNoteCreation.style.display = 'none'
    noteTable.style.display = 'block'
}

if (noteState === 'creation') {
    formNoteCreation.style.display = 'block'
    noteTable.style.display = 'none'
    noteDate.value = new Date(Date.now()).toISOString().replace('T', ' ').split('.')[0]
    formNoteCreation.action = "/notes/create?patient_update=" + patientUpdate
    creationNoteBtn.innerText = 'Cancel'
}

if (noteState === 'edition') {
    saveNoteBtn.style.display = 'none'
    noteContent.setAttribute('readonly', true)
    formNoteCreation.style.display = ''
    noteTable.style.display = 'none'
    creationNoteBtn.innerText = 'Cancel'
    noteDate.value = noteDate.value.length === 0 ? new Date(Date.now()).toISOString().replace('T', ' ').split('.')[0] : noteDate.value
}

if (noteState === 'update') {
    saveNoteBtn.innerText = 'Update note'
    formNoteCreation.action = "/notes/update/" + note.id + "?patient_update=" + patientUpdate
    saveNoteBtn.style.display = 'block'
    noteContent.removeAttribute('readonly')
    formNoteCreation.style.display = ''
    noteTable.style.display = 'none'
    creationNoteBtn.innerText = 'Cancel'
    // noteDate.value = noteDate.value
}

console.log("formNoteCreation_Action= " + formNoteCreation.action)
console.log("formUpdatePatient_Action= " + formUpdatePatient.action)

/**
 * function to display form to create a note 
 * @param {*} btn btn of creation of note that was clicked 
 */
function toggleNoteCreationForm(btn, patient_id) {
    // when click to cancel creation or update : reset form field

    if (btn.innerText === 'Cancel' || noteState === 'all') {
        resetFieldNoteForm()
    }

    noteState = noteState === 'all' ? 'creation' : 'all'
    formUpdatePatient.action = "/patients/update/" + patient_id + "?note_state=" + noteState

    btn.innerText = btn.innerText === 'Create a note' ? 'Cancel' : 'Create a note'
    noteDate.value = noteDate.value.length === 0 ? new Date(Date.now()).toISOString().replace('T', ' ').split('.')[0] : noteDate.value
    noteTable.style.display = noteTable.style.display === 'none' ? '' : 'none'
    formNoteCreation.style.display = formNoteCreation.style.display === 'none' ? '' : 'none'
    formNoteCreation.action = "/notes/create?patient_update=" + patientUpdate

}

/**
 * reset all value of form for creation or update note
 */
function resetFieldNoteForm(patient_id) {
    document.querySelectorAll("#note-creation-form .field-note").forEach(element => {
        element.value = ''
        console.log(element)
    })
    noteContent.removeAttribute('readonly')
    saveNoteBtn.style.display = 'block'
    saveNoteBtn.innerText = 'Save note'
}

/**
 * function to display selected note when its row was clicked using fields of creation form 
 * @param {*} noteId id of note selected
 */
function notesRowClicked(noteId) {
    location.href = "/notes/" + noteId + "?note_state=edition&patient_update=" + patientUpdate
}


function updateNote(urlNoteToUpdate) {
    location.href = urlNoteToUpdate + patientUpdate
}

function deleteNote(urlNoteToDelete) {
    const deleteModal = new bootstrap.Modal('#deleteNoteModal')
    document.querySelector('.modal-footer a').setAttribute("href", urlNoteToDelete + patientUpdate)
    deleteModal.show()
}

/**
 * function for btn delete for note that is on the same row that update button.
 * allow us to stop propagation of function on rowclick() 
 * @param {*} e 
 */
function myEventHandler(e) {
    if (!e)
        e = Event;

    //IE9 & Other Browsers
    if (e.stopPropagation) {
        e.stopPropagation();
    }
    //IE8 and Lower
    else {
        e.cancelBubble = true;
    }
}