// let updateQueryString = location.search.substring(1)
// console.log(updateQueryString)
// let update = updateQueryString.split('=')[1]
const urlParams = new URLSearchParams(window.location.search)
const update = urlParams.get('update')
console.log(update)

if (update == 'false') {
    togglePersonalInformationForm()
}else{
   
}

function togglePersonalInformationForm() {
    document.getElementById('submit-personal-information').style.display = "none"
    document.querySelector('#form-update-patient fieldset').setAttribute("disabled", "disabled")
    document.getElementById('genre').classList.replace('form-select','form-control')
}