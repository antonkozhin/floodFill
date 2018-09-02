package kozhin.floodfill.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.*
import android.widget.*
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxAdapterView
import com.jakewharton.rxbinding2.widget.RxSeekBar
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_main.*
import kozhin.floodfill.App
import kozhin.floodfill.R
import kozhin.floodfill.model.AlgorithmResult
import kozhin.floodfill.model.Position
import kozhin.floodfill.presenter.MainPresenter
import javax.inject.Inject

class MainFragment : Fragment(), MainView {

    companion object {
        const val TAG = "MainFragment"
    }

    private lateinit var image1: ImageView
    private lateinit var image2: ImageView
    private lateinit var spinner1: Spinner
    private lateinit var spinner2: Spinner
    private lateinit var speedSeekbar: SeekBar
    private lateinit var dialog: AlertDialog

    private var isLockUI = false

    @Inject
    lateinit var presenter: MainPresenter

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onAttach(context: Context) {
        App.applicationComponent?.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.bindView(this)

        image1 = container1.findViewById(R.id.image)
        image2 = container2.findViewById(R.id.image)
        spinner1 = container1.findViewById(R.id.spinner)
        spinner2 = container2.findViewById(R.id.spinner)
        speedSeekbar = controls.findViewById(R.id.speed_seekbar)

        setupImages()
        setupSpinners()
        setupSizeDialog()
        setupSpeedSeekbar()

        observeImageTouches(image1)
        observeImageTouches(image2)

        val generateButton = controls.findViewById<Button>(R.id.generate_button)

        RxView.clicks(generateButton)
                .filter { !isLockUI }
                .doOnNext {
                    image1.setImageDrawable(null)
                    image2.setImageDrawable(null)
                }
                .map { presenter.generateBitmap() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    showResult(it)
                }

        val sizeButton = controls.findViewById<Button>(R.id.size_button)

        RxView.clicks(sizeButton)
                .filter { !isLockUI }
                .subscribe {

                    dialog.show() }

        RxAdapterView.itemSelections(spinner1)
                .filter { !isLockUI }
                .subscribe { presenter.selectFirstAlgorithm(it) }

        RxAdapterView.itemSelections(spinner2)
                .filter { !isLockUI }
                .subscribe { presenter.selectSecondAlgorithm(it) }

        RxSeekBar.userChanges(speedSeekbar)
                .subscribe { presenter.fillSpeed = it }

    }

    private fun setupImages() {
        image1.setImageBitmap(presenter.bitmap1)
        image2.setImageBitmap(presenter.bitmap2)
    }

    private fun setupSpinners() {
        val adapter = ArrayAdapter.createFromResource(context,
                R.array.algorithms_array, R.layout.spinner_item)
        spinner1.adapter = adapter
        spinner1.setSelection(presenter.algorithmType1.ordinal)
        spinner2.adapter = adapter
        spinner2.setSelection(presenter.algorithmType2.ordinal)
    }

    private fun setupSizeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_size, null)
        val dialogBuilder = AlertDialog.Builder(context!!)
        dialog = dialogBuilder.create()
        dialog.setView(dialogView)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancel_button)
        val okButton = dialogView.findViewById<Button>(R.id.ok_button)
        val width = dialogView.findViewById<EditText>(R.id.edit_width)
        val height = dialogView.findViewById<EditText>(R.id.edit_height)
        width.setText(presenter.width.toString())
        height.setText(presenter.height.toString())
        okButton.isEnabled = false
        Observable.merge(RxTextView.textChanges(width), RxTextView.textChanges(height))
                .subscribe {
                    okButton.isEnabled = width.text.isNotEmpty() && height.text.isNotEmpty()
                }
        RxView.clicks(okButton)
                .flatMap { presenter.setSize(width.text.toString(), height.text.toString()) }
                .subscribe {
                    width.setText(it.width.toString())
                    height.setText(it.height.toString())
                    dialog.dismiss()
                }
        RxView.clicks(cancelButton).subscribe {
            width.setText(presenter.width.toString())
            height.setText(presenter.height.toString())
            dialog.dismiss() }
    }

    private fun setupSpeedSeekbar() {
        speedSeekbar.progress = presenter.fillSpeed
    }

    private fun observeImageTouches(imageView: ImageView) {
        RxView.touches(imageView)
                .filter { !isLockUI }
                .filter { imageView.drawable != null }
                .filter { it.action == MotionEvent.ACTION_DOWN }
                .observeOn(Schedulers.computation())
                .flatMap {
                    startFloodFilling(imageView, it)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            when (it.position) {
                                Position.FIRST -> showFirstResult(it.bitmap)
                                Position.SECOND -> showSecondResult(it.bitmap)
                            }
                        },
                        { showMessage(R.string.error_algorithm) }
                )
    }

    private fun startFloodFilling(view: ImageView, event: MotionEvent) : Observable<AlgorithmResult>? {
        val bitmap = getBitmap(view)
        bitmap?.let {
            val outLocation = IntArray(2)
            view.getLocationOnScreen(outLocation)
            val maxImgX = view.width
            val maxImgY = view.height
            val absX = event.rawX
            val absY = event.rawY
            return presenter.startFloodFilling(outLocation, maxImgX, maxImgY, bitmap, absX, absY)
        }
        return null
    }

    private fun getBitmap(view: ImageView): Bitmap? {
        return (view.drawable as? BitmapDrawable)?.bitmap
    }

    override fun showResult(bitmap: Bitmap) {
        image1.setImageBitmap(bitmap)
        image2.setImageBitmap(bitmap)
    }

    override fun showFirstResult(bitmap: Bitmap) {
        image1.setImageBitmap(bitmap)
    }

    override fun showSecondResult(bitmap: Bitmap) {
        image2.setImageBitmap(bitmap)
    }

    override fun showMessage(resId: Int, vararg formatArgs: Any) {
        val message = getString(resId, *formatArgs)
        showMessage(message)
    }

    override fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun lockUI() {
        isLockUI = true
    }

    override fun unlockUI() {
        isLockUI = false
    }

}
