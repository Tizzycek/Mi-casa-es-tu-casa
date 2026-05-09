package it.tizzycek.micasaestucasa.ui.utente

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.DecelerateInterpolator
import androidx.lifecycle.lifecycleScope
import it.tizzycek.micasaestucasa.R
import it.tizzycek.micasaestucasa.data.utente.Stato
import it.tizzycek.micasaestucasa.databinding.ActivityBannedBinding
import it.tizzycek.micasaestucasa.repo.MainRepo
import it.tizzycek.micasaestucasa.utils.DateValidator
import it.tizzycek.micasaestucasa.utils.mostraErrore
import kotlinx.coroutines.launch

class BannedActivity: AppCompatActivity() {
    private lateinit var binding: ActivityBannedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_banned)

        binding = ActivityBannedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setContents()

        // Avvia animazione dopo un breve delay
        Handler(Looper.getMainLooper()).postDelayed({
            startBanAnimation()
        }, 300)
    }

    private fun startBanAnimation() {
        val lockIcon = binding.lockIcon
        val lockRing = binding.lockRing

        // 1. ANIMAZIONE LUCCHETTO: da aperto a chiuso con vibrazione
        val shakeAnim = ObjectAnimator.ofFloat(lockIcon, "translationX", 0f, 8f, -8f, 8f, -8f, 4f, -2f, 0f)
        shakeAnim.duration = 900
        shakeAnim.startDelay = 200

        shakeAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                // Vibrazione dispositivo
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        200,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }

            override fun onAnimationEnd(animation: Animator) {
                // Cambia icona lucchetto: da aperto a chiuso
                lockIcon.setImageResource(R.drawable.ic_lock_closed)

                // Scala il lucchetto chiuso
                lockIcon.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(200)
                    .withEndAction {
                        lockIcon.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start()
                    }
                    .start()

                // 2. ESPANSIONE ANELLO ROSSO
                lockRing.animate()
                    .alpha(0.8f)
                    .scaleX(1.3f)
                    .scaleY(1.3f)
                    .setDuration(600)
                    .withEndAction {
                        lockRing.animate().alpha(0.6f).scaleX(1.1f).scaleY(1.1f).setDuration(400).start()
                    }
                    .start()

                // 3. MOSTRA TESTO CON FADE IN SEQUENZIALE
                showTextSequentially()
            }
        })

        shakeAnim.start()
    }

    private fun showTextSequentially() {
        val title = binding.titleBanned
        val divider = binding.divider
        val message = binding.messageBanned
        val button = binding.btnSupport
        val infoSuspension = binding.suspendInfo

        // Titolo con fade in + slide down
        title.animate()
            .alpha(1f)
            .translationYBy(-20f)
            .setDuration(600)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                // Divider si espande orizzontalmente
                val anim = ValueAnimator.ofInt(0, 200)
                anim.addUpdateListener { valueAnimator ->
                    val params = divider.layoutParams
                    params.width = valueAnimator.animatedValue as Int
                    divider.layoutParams = params
                }
                anim.duration = 400
                anim.start()

                // Messaggio fade in
                message.animate()
                    .alpha(1f)
                    .setDuration(800)
                    .setStartDelay(200)
                    .withEndAction {
                        // Bottone fade in con slide up
                        button.animate()
                            .alpha(1f)
                            .translationYBy(10f)
                            .setDuration(500)
                            .setInterpolator(DecelerateInterpolator())
                            .start()

                        infoSuspension.animate()
                            .alpha(1f)
                            .translationYBy(10f)
                            .setDuration(500)
                            .setInterpolator(DecelerateInterpolator())
                            .start()
                    }
                    .start()
            }
            .start()
    }

    private fun setContents() {
        val status = MainRepo.getUserRepository().getCurrentUser()?.stato
        binding.titleBanned.setText(if (status == Stato.BANDITO) R.string.sei_bandito else R.string.sei_sospeso)
        binding.btnSupport.setBackgroundResource(R.drawable.btn_support_bg)

        if (status == Stato.SOSPESO)
            binding.suspendInfo.text = getString(R.string.info_sospeso, DateValidator.formatDate(MainRepo.getUserRepository().getCurrentUser()?.suspension?.toDate()?.toInstant()))
        else
            binding.suspendInfo.visibility = View.GONE

        binding.btnSupport.setOnClickListener {
            binding.btnSupport.isEnabled = false
            lifecycleScope.launch {
                val result = MainRepo.getUserRepository().logout()
                result.fold(
                    onSuccess = {
                        startActivity(Intent(this@BannedActivity, LoginActivity::class.java))
                        finish()
                    },
                    onFailure = { e ->
                        mostraErrore(e.message ?: getString(R.string.unknown_error))
                        binding.btnSupport.isEnabled = true
                    }
                )
            }
        }
    }
}