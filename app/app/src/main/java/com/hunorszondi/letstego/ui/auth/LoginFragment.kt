package com.hunorszondi.letstego.ui.auth

import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.hunorszondi.letstego.R
import com.hunorszondi.letstego.ui.BaseFragment
import com.hunorszondi.letstego.ui.chat.ChatActivity
import com.hunorszondi.letstego.viewModels.LoginViewModel
import kotlinx.android.synthetic.main.login_fragment.*

/**
 * UI of login screen
 */
class LoginFragment : BaseFragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.login_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity()).get(LoginViewModel::class.java)

        registerLink.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_loginFragment_to_registerFragment, null))
        loginButton.setOnClickListener {
            showLoading()
            viewModel.login(usernameInput.text.toString(), passwordInput.text.toString(), ::loginCallback)
        }
    }

    /**
     * Handles UI update if the viewModel requires it
     */
    private fun loginCallback(status: Boolean, message: String) {
        requireActivity().runOnUiThread {
            cancelLoading()
            if(status) {
                startActivity(Intent(requireActivity(), ChatActivity::class.java))
                requireActivity().finish()
            } else {
                Toast.makeText(context, "Login failed: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
