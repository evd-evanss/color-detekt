package com.sugarspoon.colordetekt.ui.color

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ScaleGestureDetector
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.sugarspoon.colordetekt.R
import com.sugarspoon.colordetekt.data.PreferencesHelper
import com.sugarspoon.colordetekt.model.ColorAnalyzer
import com.sugarspoon.colordetekt.model.ColorChosen
import com.sugarspoon.colordetekt.ui.BaseActivity
import com.sugarspoon.colordetekt.ui.widget.GenericDialog
import com.sugarspoon.colordetekt.utils.PermissionDispatcherHelper
import com.sugarspoon.housebook.extensions.setVisible
import com.sugarspoon.housebook.extensions.setup
import com.sugarspoon.housebook.extensions.snack
import kotlinx.android.synthetic.main.activity_color.*
import kotlinx.android.synthetic.main.activity_color.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class MainActivity : BaseActivity(), PermissionDispatcherHelper.OnPermissionResult {

    private val factory = ColorViewModel.Factory()
    private val viewModel by viewModels<ColorViewModel> { factory }
    private var preferencesHelper : PreferencesHelper? = null
    private var colorChosen: ColorChosen = ColorChosen.createEmpty()
    private lateinit var preview: Preview
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var imageCapture = ImageCapture.Builder().build()
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var previewBuilder: Preview.Builder? = null
    private var imageCaptureBuilder: ImageCapture.Builder? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null

    private val adapter by lazy {
        ColorAdapter(
            context = this,
            onColorListener = object : ColorAdapter.OnColorListener {
                override fun onCopyClicked(value: String) {
                    val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText("text", value)
                    clipboardManager.setPrimaryClip(clipData)
                    snack(
                        coordinatorView,
                        getString(R.string.color_activity_copy),
                        true
                    )
                }
            }
        )
    }

    private val permissionDispatcherHelper by lazy {
        PermissionDispatcherHelper(
            activity = this,
            requestCode = REQUEST_CODE_PERMISSIONS,
            permissions = REQUIRED_PERMISSIONS,
            onPermissionResult = this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color)
        PreferencesHelper.initializeInstance(this)
        preferencesHelper = PreferencesHelper.instance
    }

    private fun displayPermissionsDialog() {
        GenericDialog(
            context = this,
            title = R.string.generic_dialog_title,
            body = R.string.generic_dialog_body,
            confirmText = R.string.generic_dialog_action_ok,
            cancelText = R.string.generic_dialog_action_cancel,
            listener = object : GenericDialog.GenericDialogListener {
                override fun onConfirm() {
                    permissionDispatcherHelper.dispatchPermissions()
                }

                override fun onCancel() {
                    finish()
                }
            }
        ).showIfPermissionsGranted(preferencesHelper?.getPermissionsGranted ?: false)
    }

    override fun onStart() {
        super.onStart()
        displayPermissionsDialog()
        bindCameraUseCases(preferencesHelper?.getPermissionsGranted ?: false)
        setupUi()
        setupRead()
        setupObservers()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionDispatcherHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setupUi() = view?.let{ root ->
        root.choseColorBt.isEnabled = false
        colorChosen.run {
            root.colorNameTv.text =
                getString(R.string.color_chooser_all_values, hex, y, u, v, r, g, b)
        }
        root.flashIv.setOnClickListener {
            viewModel.handle(
                ColorIntent.ToggleFlash(
                    camera?.cameraInfo?.torchState?.value == TorchState.OFF
                )
            )
        }
        root.colorListRv.setup(
            adapter = adapter,
            hasFixedSize = true
        )
        root.zoomIv.setOnClickListener {
            enableZoomFeature(true)
        }
        root.bottomAppBar.replaceMenu(R.menu.menu)
        setSupportActionBar(root.bottomAppBar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_items -> adapter.delete()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRead() {
        choseColorBt.setOnClickListener {
            viewModel.handle(ColorIntent.ChosenColor(colorChosen))
            adapter.addColor(colorChosen)
        }
    }

    private fun setupObservers() {
        viewModel.state.observe(this, Observer { state ->
            bindStateForReadColor(state.colorRead)
            bindStateForChoseColor(state.colorChosen)
            bindStateForToggleFlash(state.isEnable)
        })
    }

    private fun bindStateForReadColor(color: ColorChosen?) {
        color ?: return
        color.run {
            colorChosen = ColorChosen(y, u, v, r, g, b, hex)
            colorNameTv.text = getString(R.string.color_chooser_all_values, hex, y, u, v, r, g, b)
        }
        try {
            colorChooserIv.setBackgroundColor(Color.parseColor(color.hex))
        } catch (e: IllegalArgumentException) {
            print(e.stackTrace)
        }
    }

    private fun bindStateForChoseColor(color: ColorChosen?) {
        color ?: return
        try {
            color.run {
                colorNameTv.text =
                    getString(R.string.color_chooser_all_values, hex, y, u, v, r, g, b)
                colorChooserIv.setBackgroundColor(Color.parseColor(hex))
            }
        } catch (e: IllegalArgumentException) {
            print(e.stackTrace)
        }
    }

    private fun bindStateForToggleFlash(isEnable: Boolean?) {
        isEnable ?: return
        toggleFlash(isEnable)
    }

    private fun bindCameraUseCases(permissionsGranted: Boolean) {
        loadingPb.setVisible(true)
        if(permissionsGranted) {
            view?.let { root ->
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing).build()
                val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

                cameraProviderFuture.addListener( Runnable {
                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                    previewBuilder = Preview.Builder()
                    preview = previewBuilder?.build()!!

                    preview.setSurfaceProvider(root.imagePreview.surfaceProvider)

                    imageCaptureBuilder = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)

                    imageCaptureBuilder?.build()?.let {
                        imageCapture = it
                    }

                    imageAnalyzer = ImageAnalysis.Builder()
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor, ColorAnalyzer { flow ->
                                CoroutineScope(Main).launch {
                                    flow.collect { color ->
                                        viewModel.handle(ColorIntent.ReadColor(color))
                                        loadingPb.setVisible(false)
                                        choseColorBt.isEnabled = true
                                    }
                                }
                            })
                        }

                    cameraProvider.unbindAll()

                    try {
                        camera = cameraProvider.bindToLifecycle(
                            this,
                            cameraSelector,
                            preview,
                            imageCapture,
                            imageAnalyzer
                        )
                    } catch (exception: Exception) {
                        loadingPb.setVisible(false)
                    }
                }, ContextCompat.getMainExecutor(this))
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun enableZoomFeature(enable: Boolean = false) = view?.imagePreview?.run {
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio: Float = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 0F
                val delta = detector.scaleFactor
                camera?.cameraControl?.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        }
        val scaleGestureDetector = ScaleGestureDetector(context, listener)
        if(enable) {
            this.setOnTouchListener { _, event ->
                scaleGestureDetector.onTouchEvent(event)
                return@setOnTouchListener true
            }
        } else {
            this.setOnTouchListener(null)
        }
    }

    private fun toggleFlash(isEnable: Boolean) {
        camera?.cameraControl?.enableTorch(isEnable)
        flashIv.setImageDrawable(ContextCompat.getDrawable(
            this,
            if(isEnable) R.drawable.ic_flash_off else R.drawable.ic_flash_on
        ))
    }

    override fun onAllPermissionsGranted(requestCode: Int) {
        if(requestCode == REQUEST_CODE_PERMISSIONS) {
            preferencesHelper?.setPermissionsGranted(true)
            bindCameraUseCases(preferencesHelper?.getPermissionsGranted ?: false)
        }
    }

    override fun onPermissionsDenied(
        requestCode: Int,
        deniedPermissions: List<String>,
        deniedPermissionsWithNeverAskAgainOption: List<String>
    ) {
        preferencesHelper?.setPermissionsGranted(false)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}