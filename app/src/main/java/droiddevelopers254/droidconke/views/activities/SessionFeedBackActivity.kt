package droiddevelopers254.droidconke.views.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import butterknife.ButterKnife
import droiddevelopers254.droidconke.R
import droiddevelopers254.droidconke.models.SessionsModel
import droiddevelopers254.droidconke.models.SessionsUserFeedback
import droiddevelopers254.droidconke.viewmodels.SessionDataViewModel
import kotlinx.android.synthetic.main.activity_session_feed_back.*
import kotlinx.android.synthetic.main.content_session_feed_back.*
import org.jetbrains.anko.toast


class SessionFeedBackActivity : AppCompatActivity() {
    var sessionId:Int = 0;
    private var dayNumber :String = ""
    private lateinit var sessionDataViewModel : SessionDataViewModel
    private lateinit var sessionsModel1 : SessionsModel
    private lateinit var userFeedback : SessionsUserFeedback
    private var sessionFeedback : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_feed_back)
        ButterKnife.bind(this)
        setSupportActionBar(toolbar)

       supportActionBar?.let {
           with(it) {
               setDisplayHomeAsUpEnabled(true)
               setTitle("Sessions Feedback")
           }
       }

        //get extras
        val extraIntent = intent
        sessionId = extraIntent.getIntExtra("sessionId", 0)
        dayNumber = extraIntent.getStringExtra("dayNumber")

        sessionDataViewModel = ViewModelProviders.of(this).get(SessionDataViewModel::class.java)

        getSessionData(dayNumber, sessionId)

        //observe live data emitted by view model
        sessionDataViewModel.sessionData.observe(this, Observer{
            assert(it != null)
            when {
                it.databaseError != null -> handleDatabaseError(it.databaseError)
                else -> this.handleFetchSessionData(it.sessionsModel)
            }
        })
        sessionDataViewModel.getSessionFeedBackResponse().observe(this, Observer{
            when {
                it.responseString != null -> handleFeedbackResponse(it.responseString)
            }
        })

        fab.setOnClickListener {
            //get data from user and post them
            when {
                isFeedbackValid() -> postUserFeedback(userFeedback)
            }
        }

    }

    private fun handleFeedbackResponse(feedback: String) {
        loginProgressBarFeedBack.visibility = View.GONE

        txtSessionUserFeedback.setText("")

        toast("Thank you for your feedback")
    }

    private fun getSessionData(dayNumber: String, sessionId: Int) {
        sessionDataViewModel.getSessionDetails(dayNumber, sessionId)
    }

    private fun handleFetchSessionData(sessionsModel: SessionsModel?) {
        if (sessionsModel != null) {
            sessionsModel1 = sessionsModel
            //set the data on the view
            txtSessionFeedbackTitle.setText(sessionsModel.title)

        }
    }

    private fun handleDatabaseError(databaseError: String) {
        Toast.makeText(applicationContext, databaseError, Toast.LENGTH_SHORT).show()
    }

    private fun isFeedbackValid(): Boolean {

        sessionFeedback = txtSessionUserFeedback.getText().toString().trim()
        val isValid: Boolean

        when {
            sessionFeedback.isEmpty() -> {
                txtSessionUserFeedback.setError("Session feedback cannot be empty")
                isValid = false
            }
            else -> {

                isValid = true
                txtSessionUserFeedback.setError(null)

                userFeedback = SessionsUserFeedback(
                        user_id = "",
                        session_id = sessionId.toString(),
                        day_number = dayNumber,
                        session_title = sessionsModel1.title,
                        session_feedback = sessionFeedback
                )
            }
        }
        return isValid
    }

    private fun postUserFeedback(userFeedback: SessionsUserFeedback) {

        loginProgressBarFeedBack.setVisibility(View.VISIBLE)
        sessionDataViewModel.sendSessionFeedBack(userFeedback)

    }
}