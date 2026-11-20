package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.bridge.SafeFocusBridgeBus

class FloatingBubbleService : Service() {

    private var windowManager: WindowManager? = null
    private var bubbleContainer: LinearLayout? = null
    private var menuCard: LinearLayout? = null
    private var isMenuOpen = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        if (Settings.canDrawOverlays(this)) {
            attachFloatingBubble()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (bubbleContainer == null && Settings.canDrawOverlays(this)) {
            attachFloatingBubble()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        bubbleContainer?.let { view ->
            runCatching { windowManager?.removeView(view) }
        }
        bubbleContainer = null
    }

    private fun startForegroundNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
            }
            manager.createNotificationChannel(channel)
        }

        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("SafeFocus চালু আছে")
            .setContentText("ফোকাস শিল্ড ও ফ্লোটিং পপআপ সক্রিয় রয়েছে")
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun attachFloatingBubble() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 320
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.START
        }

        val bubbleSizePx = dp(52)
        val bubbleBadge = TextView(this).apply {
            text = "SF"
            setTextColor(Color.parseColor("#F5F3FF"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            gravity = Gravity.CENTER
            width = bubbleSizePx
            height = bubbleSizePx
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#7C3AED"))
                setStroke(dp(2), Color.parseColor("#C4B5FD"))
            }
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(18).toFloat()
                setColor(Color.parseColor("#EE1E0D38"))
                setStroke(dp(1), Color.parseColor("#C4B5FD"))
            }

            val toggleBtn = createMenuButton("সক্রিয় / বিরতি") {
                SafeFocusBridgeBus.requestToggleMasterFromOverlay()
                toggleMenu()
            }
            val openAppBtn = createMenuButton("SafeFocus খুলুন") {
                val intent = Intent(this@FloatingBubbleService, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                startActivity(intent)
                toggleMenu()
            }
            val closeOverlayBtn = createMenuButton("বাবল লুকান") {
                stopSelf()
            }

            addView(toggleBtn)
            addView(openAppBtn)
            addView(closeOverlayBtn)
        }

        menuCard = card
        container.addView(bubbleBadge)
        container.addView(card)

        var initialX = 0
        var initialY = 0
        var touchX = 0f
        var touchY = 0f
        var moved = false

        bubbleBadge.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - touchX).toInt()
                    val dy = (event.rawY - touchY).toInt()
                    if (kotlin.math.abs(dx) > 8 || kotlin.math.abs(dy) > 8) {
                        moved = true
                        params.x = initialX + dx
                        params.y = initialY + dy
                        runCatching { windowManager?.updateViewLayout(container, params) }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) toggleMenu()
                    true
                }
                else -> false
            }
        }

        bubbleContainer = container
        runCatching { windowManager?.addView(container, params) }
    }

    private fun toggleMenu() {
        isMenuOpen = !isMenuOpen
        menuCard?.visibility = if (isMenuOpen) View.VISIBLE else View.GONE
    }

    private fun createMenuButton(label: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            setTextColor(Color.parseColor("#DDD6FE"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setOnClickListener { onClick() }
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    companion object {
        private const val CHANNEL_ID = "safefocus_shield_channel"
        private const val NOTIFICATION_ID = 1042
    }
}
