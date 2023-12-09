const urlParams = new URLSearchParams(window.location.search)
const update = urlParams.get('update')
const noteUpdate = urlParams.get('note_update')
console.log('update :' + update)
console.log('note_update :' + noteUpdate)
console.log('note: ' + JSON.stringify(note))

/* part for patient*/
if (update == 'true' || fieldsOnError == {}) {
    document.querySelector('.bi-pencil-square').style.display = 'none'
} else {
    disablePersonalInformation()
}

function disablePersonalInformation() {
    document.querySelector('#div-submit-btn').style.display = "none"
    document.querySelector('#form-update-patient fieldset').setAttribute("disabled", "true")
    document.getElementById('genre').classList.replace('form-select', 'form-control')
    document.getElementById('dateOfBirth').disabled = true
    document.querySelector('.bi-pencil-square').style.display = 'block'
    document.querySelector('.bi-x-square').style.display = 'none'
}

function enablePersonalInformation() {
    document.querySelector('#div-submit-btn').style.display = 'flex'
    document.querySelector('#form-update-patient fieldset').removeAttribute("disabled")
    document.getElementById('genre').classList.replace('form-control', 'form-select')
    document.getElementById('dateOfBirth').disabled = false
    document.querySelector('.bi-pencil-square').style.display = 'none'
    document.querySelector('.bi-x-square').style.display = 'block'
}

/* part for note creation */
var noteForm = document.querySelector('#note-creation-form')
var noteTable = document.querySelector('#notes-table')
var noteDate = document.querySelector('#creation-note-date')
var creationNoteBtn = document.querySelector('#creation-note-btn')
var saveNoteBtn = document.querySelector('#save-note-btn')
var noteContent = document.querySelector('#creation-note-content')

console.log("noteDate.value: " + noteDate.value);


/**
 * case of bindingResult we have to keep form open with new date
 * case of note not null (row clicked to view note selected) display information of note
 */
if (Object.keys(fieldsOnError).length != 0 || note.id != null) {
    noteForm.style.display = ''
    noteTable.style.display = 'none'
    creationNoteBtn.innerText = 'Cancel'
    noteDate.value = noteDate.value.length === 0 ? new Date(Date.now()).toISOString().replace('T', ' ').split('.')[0] : noteDate.value
    /**
     * case of just display a note 
     */
    if (noteUpdate === 'false') {
        saveNoteBtn.style.display = 'none'
        noteContent.setAttribute('readonly', true)
    }
    /**
     * case of update note
     */
    if (noteUpdate === 'true') {
        saveNoteBtn.innerText = 'Update note'
        noteForm.action = "/notes/update/" + note.id
    }
}

/**
 * function to display form to create a note 
 * @param {*} btn btn of creation of note that was clicked 
 */
function toggleNoteCreationForm(btn) {
    // when click to cancel creation or update : reset form field
    if (btn.innerText === 'Cancel' || successMessage != undefined) {
        resetFieldNoteForm()
    }
    btn.innerText = btn.innerText === 'Create a note' ? 'Cancel' : 'Create a note'
    noteDate.value = noteDate.value.length === 0 ? new Date(Date.now()).toISOString().replace('T', ' ').split('.')[0] : noteDate.value
    noteTable.style.display = noteTable.style.display === 'none' ? '' : 'none'
    noteForm.style.display = noteForm.style.display === 'none' ? '' : 'none'
}

/**
 * function to display selected note when its row was clicked using fields of creation form 
 * @param {*} patientId id of related patient of note
 * @param {*} noteId id of note selected
 */
function notesRowClicked(patientId, noteId) {
    location.href = "/notes/" + noteId + "/patient/" + patientId + "?note_update=false"
}

/**
 * reset all value of form for creation or update note
 */
function resetFieldNoteForm() {
    document.querySelectorAll("#note-creation-form .field-note").forEach(element => {
        element.value = ''
        console.log(element)
    })
    noteContent.removeAttribute('readonly')
    saveNoteBtn.style.display = 'block'
    saveNoteBtn.innerText = 'Save note'
}

function deleteNote(urlNoteToDelete){
    const deleteModal = new bootstrap.Modal('#deleteNoteModal')
    document.querySelector('.modal-footer a').setAttribute("href",urlNoteToDelete)
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