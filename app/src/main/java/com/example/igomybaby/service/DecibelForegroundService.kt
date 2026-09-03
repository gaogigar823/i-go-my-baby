package com.example.igomybaby.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.igomybaby.notification.NoiseNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.sqrt

class DecibelForegroundService : Service() {

    companion object {
        private val _currentDb = MutableStateFlow(0f)
        val currentDb: StateFlow<Float> = _currentDb

        const val NOTIFICATION_ID = 1001
    }

    private var audioRecord: AudioRecord? = null
    private var measureJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        NoiseNotifications.createChannels(this)
        // RECORD_AUDIO 권한이 회수된 상태에서 호출되면 startForeground 가 SecurityException 던짐
        // → 크래시 대신 서비스만 조용히 중단
        val started = startForegroundCompat(buildServiceNotification())
        if (!started) {
            stopSelf()
            return START_NOT_STICKY
        }
        // 서비스가 이미 측정 중인데 액티비티가 재시작하며 onStartCommand 가 다시 호출되면
        // 두 번째 AudioRecord 가 만들어지고 동시 read 경쟁 → 네이티브 SIGABRT
        // → 이미 측정 중이면 startMeasuring 을 다시 부르지 않음
        if (measureJob?.isActive != true) {
            startMeasuring()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMeasuring()
        _currentDb.value = 0f
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundCompat(notification: Notification): Boolean = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        true
    } catch (e: SecurityException) {
        // RECORD_AUDIO 미허용 또는 백그라운드 FGS 시작 제한 위반
        Log.w("DecibelFGS", "startForeground denied — stopping service", e)
        false
    } catch (e: IllegalStateException) {
        // Android 12+ ForegroundServiceStartNotAllowedException (IllegalStateException 의 서브클래스)
        Log.w("DecibelFGS", "startForeground not allowed — stopping service", e)
        false
    }

    private fun startMeasuring() {
        // 이중 진입 방어 — 이미 돌고 있으면 정리하고 새로 시작
        stopMeasuring()

        val sampleRate = 44100
        val channelCfg = AudioFormat.CHANNEL_IN_MONO
        val audioFmt   = AudioFormat.ENCODING_PCM_16BIT
        // getMinBufferSize 는 바이트 단위. AudioRecord 생성자도 바이트.
        // 하지만 ShortArray 크기와 read() 의 sizeInShorts 는 SHORT 단위 → 2배 차이 주의
        val bufferSizeBytes  = AudioRecord.getMinBufferSize(sampleRate, channelCfg, audioFmt) * 4
        val bufferSizeShorts = bufferSizeBytes / 2   // 16bit PCM 이므로 1 short = 2 byte

        @Suppress("MissingPermission")
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate, channelCfg, audioFmt, bufferSizeBytes,
        ).also { it.startRecording() }

        measureJob = scope.launch {
            val buffer = ShortArray(bufferSizeShorts)
            while (isActive) {
                // 매 루프마다 필드를 다시 읽으면 stopMeasuring 중 race 발생 가능
                // → 로컬 캡처 후 사용
                val ar = audioRecord ?: break
                val read = try {
                    ar.read(buffer, 0, bufferSizeShorts)
                } catch (e: IllegalStateException) {
                    break
                }
                if (read > 0) {
                    _currentDb.value = calculateDb(buffer, read).toFloat().coerceAtLeast(0f)
                }
                delay(100)
            }
        }
    }

    private fun stopMeasuring() {
        measureJob?.cancel()
        measureJob = null
        // read() 가 진행 중인 상태에서 stop/release 를 부르면 AudioTrackShared 어설션 위험
        // → 코루틴 루프가 다음 iteration 에서 자연 종료되도록 audioRecord 를 먼저 null 처리
        val ar = audioRecord
        audioRecord = null
        try {
            ar?.stop()
        } catch (_: IllegalStateException) { /* already stopped */ }
        try {
            ar?.release()
        } catch (_: Exception) { /* best effort */ }
    }

    private fun calculateDb(buffer: ShortArray, readCount: Int): Double {
        var sum = 0.0
        for (i in 0 until readCount) sum += buffer[i] * buffer[i].toDouble()
        val rms = sqrt(sum / readCount)
        if (rms < 1.0) return 0.0
        return (20.0 * log10(rms / 32767.0) + 90.0).coerceAtLeast(0.0)
    }

    private fun buildServiceNotification(): Notification =
        NotificationCompat.Builder(this, NoiseNotifications.SERVICE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("데시벨 감지 중")
            .setContentText("소음을 모니터링하고 있습니다")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
}
