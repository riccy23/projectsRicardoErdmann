document.addEventListener('DOMContentLoaded', async () => {

  //load userData
  await loadUserData();

  const logoutButton = document.getElementById("logout");

  logoutButton.addEventListener('click', (event) => {
    event.preventDefault();
    logoutUser();
  })

  /**
   * Loads Userdata from server
   *
   * @description Sends a GET request to the server to retrieve the current user's data.
   *
   * @returns {Promise<void>} A Promise that resolves when the user data is loaded.
   *
   * @remarks
   * - If the response is successful, the user data is passed on to changeUI function.
   * - If the response is not successful, an error alert is displayed to the user.
   *
   * @example
   * await loadUserData();
   *
   * @see changeUI
   *
   */
  async function loadUserData() {
    try {
      const res = await fetch(`http://localhost:8888/users/curUser`, {
        method: 'GET',
        mode: 'cors',
        credentials: 'include',
        headers: {
          "Content-Type": "application/json"
        },

      });

      if (res.ok) {
        const user: User = await res.json();
        changeUI(user);
      } else {
        alert("No Album found");
      }
    } catch (error) {
      console.error("Error occured ", error);
    }
  }

  /**
   * Displays the current username and removes the user management link if the user is not an admin.
   *
   * @param {User} user - The user object that contains information about the user who is logged on.
   * @returns {void}
   *
   * @description
   * This function modifies the user interface to match the current status of the user. It
   * removes the User Management link if the user is not an admin. And updates the username label.
   *
   * @example
   * const currentUser: User = {
   *   username: "Ricardo",
   *   isAdmin: false,
   *   // ...
   * };
   * changeUI(currentUser);
   * // This will update the username label to "Ricardo" and remove the user management link
   *
   * @see User
   */
  function changeUI(user: User) {
    const usernameLabel = document.getElementById("usernameLabel");
    const usermanagmentLink = document.getElementById("usermanagmentLink");
    usernameLabel.textContent = user.username;
    if (!user.isAdmin){
      usermanagmentLink.remove()
    }
  }

});

/**
 * Logs out the current user by sending a request to the server.
 *
 * @returns {Promise<void>} A Promise that resolves when the logout process is complete.
 *
 * @description
 * This function sends a GET request to the server to log out the current user.
 *
 * @remarks
 * - If the response is a redirect, it follows the redirect.
 * - It redirects the user to the login page if the response is successful.
 * - If the response indicates a failure, it shows an alert.
 * - Network errors are logged to the console.
 *
 * @throws Logs any errors to the console but does not throw them.
 *
 * @example
 * try {
 *   await logoutUser();
 *   // If successful, the user will be redirected to the login page
 * } catch (error) {
 *   console.error("An error occurred during logout:", error);
 * }
 */
async function logoutUser() {
  try {
    const res = await fetch(`http://localhost:8888/logout`, {
      method: 'GET',
      mode: 'cors',
      credentials: 'include',
      headers: {
        "Content-Type": "application/json"
      }
    });

    if (res.redirected) {
      window.location.href = res.url;
    } else if (res.ok) {
      window.location.href = "/login";
    } else {
      alert("Failed to logout user")
    }
  } catch (error) {
    console.error("Error logout user:", error);
  }
}
