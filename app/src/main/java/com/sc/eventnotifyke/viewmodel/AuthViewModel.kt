package com.sc.eventnotifyke.viewmodel

// viewmodel packages to allow state/ data management for our ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sc.eventnotifyke.models.UserRole
import com.sc.eventnotifyke.models.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// sealed class : it can not be used to create objects
// confined for usage inside our view model

sealed class AuthState{
    object Idle : AuthState() // state when user is not logged in
    object Loading: AuthState( ) // state when user attempts to login
    // when user is successfully logged in tag the profile
    data class Success(val profile: UserProfile) : AuthState()
    // if error occurred on login attempt
    data class Error(val message: String ): AuthState()
    //state on password reset process
    object PasswordResetSent : AuthState()
    // state when user logs out
    object Logout : AuthState()
}

class AuthViewModel : ViewModel(){
    // references to firebase auth classes
    // private means variable can only be used inside this script
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val _authState= MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> =_authState
    private val _currentProfile =
        MutableStateFlow<UserProfile?>(null)
    val currentProfile: StateFlow<UserProfile?> = _currentProfile

    // called on app start to check if user is already logged in if so skip auth screens and redirect to dashboard
    init {
        val user = auth.currentUser
        if (user != null){
            fetchUserProfile(user.uid)
        }

    }
    fun register(fullName: String,
                 email: String,
                 phone: String,
                 estate: String,
                 password: String,
                 role: String){
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // 1. create firebase account for the user
                val result = auth.createUserWithEmailAndPassword(
                    email,password
                ).await()
                val uid = result.user!!.uid
                // create profile and store in firestore
                val profile = UserProfile(
                    uid = uid,
                    fullname = fullName,
                    email = email,
                    phone = phone,
                    estate = estate,
                    role = role
                )
                db.collection("users")
                    .document(uid)
                    .set(profile.toMap())
                    .await()
                auth.signOut()

                _currentProfile.value = profile
                _authState.value = AuthState.Success(profile)
            }catch (e: Exception){
                _authState.value = AuthState.Error(
                    e.message ?: "Registration Failed"
                )


            }
        }
    }
    fun Login(email: String, password: String){
        viewModelScope.launch {
            _authState.value= AuthState.Loading
            try{
                val result = auth.signInWithEmailAndPassword(email,password).await()
                fetchUserProfile(result.user!!.uid)
            }catch (e: Exception){
                _authState.value = AuthState.Error(
                    e.message ?: "Login Failed"
                )
            }
        }
    }
    // fetch user profile from firestore
    fun fetchUserProfile(uid: String){
        viewModelScope.launch{
            try {
                _authState.value = AuthState.Loading
                // first we create a reference to our firestore collection
                val doc = db.collection("users")
                    .document(uid)
                    .get().await()
                val profile = UserProfile(
                    uid = uid,
                    fullname = doc.getString("fullname") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    estate = doc.getString("estate") ?: "",
                    role = doc.getString("role") ?: "user"
                )
                _currentProfile.value = profile
                _authState.value = AuthState.Success(profile)
            } catch (e: Exception){
                _authState.value = AuthState.Error(
                    e.message ?: "Failed to load profile"
                )
            }
        }

    }
    // forgot password
    fun sendPasswordReset(email: String){
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.PasswordResetSent
            }catch (e: Exception){
                _authState.value = AuthState.Error(
                    e.message ?: "Reset failed"
                )
            }
        }
    }
    // Logout
    fun logout(){
        auth.signOut()
        _currentProfile.value = null
        _authState.value = AuthState.Logout
    }

    // helper function to clear stale validation states manually from the UI
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
