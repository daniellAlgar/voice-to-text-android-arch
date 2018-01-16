package com.varvet.voicetotextarch

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.varvet.voicetotextarch.SpeechRecognizerViewModel.ViewState

@Suppress("PrivatePropertyName")
class MainActivity : AppCompatActivity() {

    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    private lateinit var textField: TextView
    private lateinit var micButton: Button
    private lateinit var rippleView: RippleView

    private lateinit var speechRecognizerViewModel: SpeechRecognizerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textField = findViewById(R.id.spoken_result_text_field)
        micButton = findViewById<Button>(R.id.mic_button).apply {
            setOnClickListener(micClickListener)
        }
        rippleView = findViewById(R.id.circle_ripple)

        setupSpeechViewModel()
    }

    private val micClickListener = View.OnClickListener {
        if (!speechRecognizerViewModel.permissionToRecordAudio) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
            return@OnClickListener
        }

        if (speechRecognizerViewModel.isListening) {
            speechRecognizerViewModel.stopListening()
        } else {
            speechRecognizerViewModel.startListening()
        }
    }

    private fun setupSpeechViewModel() {
        speechRecognizerViewModel = ViewModelProviders.of(this).get(SpeechRecognizerViewModel::class.java)
        speechRecognizerViewModel.getViewState().observe(this, Observer<ViewState> { viewState ->
            render(viewState)
        })
    }

    private fun render(uiOutput: ViewState?) {
        if (uiOutput == null) return

        textField.text = uiOutput.spokenText

        micButton.background  = if (uiOutput.isListening) {
            ContextCompat.getDrawable(this, R.drawable.mic_red)
        } else {
            ContextCompat.getDrawable(this, R.drawable.mic_black)
        }

        if (uiOutput.rmsDbChanged) {
            rippleView.newRipple()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            speechRecognizerViewModel.permissionToRecordAudio = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }

        if (speechRecognizerViewModel.permissionToRecordAudio) {
            micButton.performClick()
        }
    }
}
