package com.example.favour

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_front_signin.*


class FrontSigninFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_front_signin, container, false)
    }

    override fun onViewCreated(
        @NonNull view: View,
        @Nullable savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        takeMeLogin.setOnClickListener(View.OnClickListener {
            startActivity(Intent(requireContext(), Login::class.java))
            requireActivity().finish()
        })
        signUp.setOnClickListener(View.OnClickListener {
            if (CheckerMatcher().checkEmptyNamePhone(signInName, mob_number)) {
                val session = Session(requireContext())
                session.setUsername(signInName.text.toString())
                session.setMobile(mob_number.text.toString())

                lateinit var frag: Fragment
                frag = if (session.getVerifiedState()!!) PasswordFragment()
                else OtpFragment()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fml_signin, frag).addToBackStack("FragFrontSignIn")
                    .commit()

            }
        })
    }

}