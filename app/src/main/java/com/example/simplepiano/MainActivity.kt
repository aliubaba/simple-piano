package com.example.simplepiano

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val sampleRate = 44100
    private val activeControllers = mutableMapOf<Int, AudioTrackController>()
    private var volume = 0.6f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val container = findViewById<LinearLayout>(R.id.keyboardContainer)
        val seekBar = findViewById<SeekBar>(R.id.volumeSeek)
        seekBar.progress = (volume * 100).toInt()
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                volume = progress / 100f
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val startMidi = 60 // C4
        val keyCount = 24

        for (i in 0 until keyCount) {
            val midi = startMidi + i
            val freq = 440.0 * Math.pow(2.0, (midi - 69) / 12.0)
            val isBlack = isBlackKey(midi % 12)

            val widthDp = if (isBlack) 48f else 80f
            val heightDp = if (isBlack) 180f else 300f
            val params = LinearLayout.LayoutParams(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, widthDp, resources.displayMetrics).toInt(),
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightDp, resources.displayMetrics).toInt()
            )
            params.setMargins(2, 2, 2, 2)

            val keyView = View(this)
            keyView.layoutParams = params
            keyView.tag = midi
            keyView.setBackgroundColor(if (isBlack) 0xFF222222.toInt() else 0xFFFFFFFF.toInt())

            keyView.setOnTouchListener { v, event ->
                val pointerId = event.getPointerId(event.actionIndex)
                val mapKey = (v.hashCode() shl 16) or pointerId
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                        if (!activeControllers.containsKey(mapKey)) {
                            val controller = AudioTrackController(freq.toFloat(), volume)
                            controller.play()
                            activeControllers[mapKey] = controller
                            v.alpha = 0.6f
                        }
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                        activeControllers.remove(mapKey)?.stop()
                        v.alpha = 1.0f
                        true
                    }
                    else -> false
                }
            }

            container.addView(keyView)
        }
    }

    private fun isBlackKey(n: Int): Boolean {
        return when (n) {
            1, 3, 6, 8, 10 -> true
            else -> false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for ((_, c) in activeControllers) c.stop()
        activeControllers.clear()
    }

    inner class AudioTrackController(private val freq: Float, initVol: Float) {
        @Volatile private var playing = true
        private var track: AudioTrack? = null
        private var thread: Thread? = null

        fun play() {
            val minBuf = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
            track = AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBuf, AudioTrack.MODE_STREAM)
            try { track?.setVolume(initVol) } catch (_: Throwable) {}
            track?.play()

            thread = Thread {
                val buffer = ShortArray(1024)
                var phase = 0.0
                val twoPi = 2 * Math.PI
                val increment = twoPi * freq / sampleRate
                while (playing) {
                    for (i in buffer.indices) {
                        val v = Math.sin(phase)
                        buffer[i] = (v * Short.MAX_VALUE).toInt().toShort()
                        phase += increment
                        if (phase > twoPi) phase -= twoPi
                    }
                    track?.write(buffer, 0, buffer.size)
                }
                try { track?.stop(); track?.release() } catch (_: Throwable) {}
            }
            thread?.start()
        }

        fun stop() {
            playing = false
            try { thread?.join(100) } catch (_: InterruptedException) {}
        }
    }
}
