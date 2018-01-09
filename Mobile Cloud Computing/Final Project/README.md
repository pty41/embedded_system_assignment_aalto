# Mobile Cloud Computing Project (SharePhoto)

## 1. Team

479 754 Jukka Laakko<br>
476 812 Juuso Jahnukainen<br>
596 226 Karina Karapetyan<br>
480 316 Raine Nieminen<br>
604 888 Shan Kuan

## 2. Planning and other stuff

### Person(s) in charge of functionalities

#### Frontend

* Welcome screen (authentication): **Raine** - Done
* Grid menu (links to the options): **Jukka** - Done
* Gallery (Private and group galleries): **Shan** Done
* Take picture (live camera + barcode detection): **Jukka**
* Group management (create, join, leave, etc. buttons): **Jukka**
* App settings (downloading settings): **Karina**

#### Backend

* Authorization (Used ID tokens): **Raine**
* Group management (the actual functions for frontend): **Juuso**
* Image labeling based on the content (for the Gallery): **Shan** Done
* Save taken pictures in low, high and original resolutions: **Karina**
* Access rules: **Raine**

#### Miscellaneous

* Put frontend and backend together: 
* Test:
* Design (icons, layout, etc.):

---
### Authentication

Authentication with Firebase should work now. It's based on FirebaseUI Auth and I only added the authentication with email. However, it's now quite easy to add other login ways if needed later. The code is in the *SignInActivity.java* and should be fairly easy to understand. The actual login is basically this peace of code here.

```java
startActivityForResult(
    // Get an instance of AuthUI based on the default app
    AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setIsSmartLockEnabled(!BuildConfig.DEBUG)
        .build(),
    RC_SIGN_IN);
```
         
---
### ID token

The full guide is [here](https://firebase.google.com/docs/auth/admin/verify-id-tokens).

#### Frontend

Creating the ID token is fairly simple and is demonstrated currently in the *UserInfoActivity.java*. **Always remember** to check that the user is signed in like this:

```java
FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
// Check if user is not signed in
if (user == null) {
    // Not signed in, go to sign up form
    startActivity(SignInActivity.createIntent(this));
    finish();
    return;
}
```

The sending part of the ID token via HTTPS is still missing and I will be working on that next. However, first I'd like to see the backend running, because this functionality needs to collaborate with that.

#### Backend

After *id_token* is received, it can be verified with a simple function call. However, the Admin SDK with a service account has to be initialized (see [instructions](https://firebase.google.com/docs/admin/setup))!

Start the backend using command:

```bash
FLASK_APP=server.py flask run
```

Test environment: *python test.py*

Here is the verify code for the backend in Python.

```python
# id_token comes from the client app

decoded_token = auth.verify_id_token(id_token)
uid = decoded_token['uid']
```

---

## 3. Implementation, features, build, etc.

### Implementation


#### Dependencies

```bash
sudo pip install firebase-admin flask flask-restful pyrebase requests pillow (required version 2.8+)
```

For the test environment also: *sudo pip install ipython*

#### Camera Functionality (Jukka)

Camera TODO:

* Barcode / Face detection []
* Barcode reader (for group joining) [x]
* Fix scale issue (division by 0) []
* Find out if there is a better way to do camera preview (accept/decline taken picture) [x]

#### Group management

TODO:

* Join group functionality []
* Combine frontend code with backend code []


