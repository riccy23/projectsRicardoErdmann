document.addEventListener('DOMContentLoaded', async () => {
  // Handle Login form submission
  const loginForm = document.querySelector('form');

  loginForm?.addEventListener('submit', async (event) => {
    event.preventDefault();
    const userName = (document.getElementById('floatingInput') as HTMLInputElement).value;
    const password = (document.getElementById('floatingPassword') as HTMLInputElement).value;
    console.log(`Username: ${userName}, Password: ${password}`);

    // Send login request
    const res = await fetch('http://localhost:8888/login', {
      method: 'POST',
      mode: 'cors',
      credentials: 'include',
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({username: userName, password: password})
    });

    // If user successfully logged in, then redirect him to home
    if (res.ok) {
      const data = await res.json();
      console.log(data);

      window.location.href = `/home`;

    } else { //show error message when login failed
      console.error('Login failed');
      const data = await res.json();

      showError(data.error)
    }
  });


  /**
   * Displays an error message to the user.
   *
   * @param {string} message - The error message to be displayed.
   * @return {void} No return value.
   */
  function showError(message: string): void {
    const alertContainer = document.getElementById('alertContainer');
    if (alertContainer) {
      alertContainer.innerHTML = `
            <div class="alert alert-danger fade show" role="alert">
                ${message}
            </div>
        `;
    }
  }

});
