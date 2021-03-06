package com.danhtran.androidbaseproject.ui.fragment

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.danhtran.androidbaseproject.services.extras.ErrorHandler
import com.danhtran.androidbaseproject.ui.activity.BaseAppCompatActivity
import com.danhtran.androidbaseproject.utils.UIUtils

/**
 * Created by danhtran on 2/12/17.
 */
abstract class BaseFragment : Fragment() {

    /**
     * get View Data Binding
     *
     * @return ViewDataBinding
     */
    protected var binding: ViewDataBinding? = null
        protected set

    /**
     * get view model
     */
    var viewModel: BaseFragmentViewModel? = null
        protected set


    //nav controller
    val navController: NavController?
        get() = view?.let { Navigation.findNavController(it) }

    // set reuse binding
    var isReUseBinding = false

    /**
     * Get root view
     *
     * @return root view
     */
    val rootView: View?
        get() = binding?.root

    /**
     * Get base activity if it is exits.
     *
     * @return BaseAppCompatActivity
     */
    val baseActivity: BaseAppCompatActivity?
        get() = if (activity is BaseAppCompatActivity) activity as BaseAppCompatActivity? else null

    val baseFragment: BaseFragment
        get() = this

    /**
     * set layout for this activity
     *
     * @return init
     */
    abstract fun setLayout(): Int

    /**
     * Set handler + execute view binding
     */
    abstract fun initUI()

    /**
     * Init view model
     */
    abstract fun initViewModel(): BaseFragmentViewModel?

    /**
     * Binding and initialize data into layout
     */
    abstract fun initData()

    /**
     * initialize the listener event
     */
    abstract fun initListener()

    /**
     * change local config
     */
    abstract fun onConfigurationChanged()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            loadPassedParamsIfNeeded(it)
        }

        //clear binding if not re-use
        if (!isReUseBinding) {
            binding = null
        }
        val xml = setLayout()
        if (xml != 0) {
            binding = DataBindingUtil.inflate(inflater, xml, container, false)

            initUI()

            //listen live event
            viewModel = initViewModel()
            viewModel?.progressState?.observe(this, Observer {
                if (it) {
                    showProgress()
                } else {
                    hideProgress()
                }
            })
            viewModel?.errorHandler?.observe(this, Observer {
                it?.let {
                    ErrorHandler.showError(it, context)
                }
            })

            //hide keyboard after click outside of edit text
            rootView?.isClickable = true
            rootView?.isFocusableInTouchMode = true
            baseActivity?.let { UIUtils.addKeyboardEvents(it, binding?.root, binding?.root) }

            //enable options menu
            setHasOptionsMenu(true)
        }
        return binding?.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData()
        initListener()
    }

    override fun onDestroy() {
        UIUtils.removeKeyboardEvents(rootView)
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        onConfigurationChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        //hide all item menu
        for (i in 0 until menu.size()) {
            menu.getItem(i).isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * load passed params
     */
    protected open fun loadPassedParamsIfNeeded(extras: Bundle) {

    }

    /**
     * Show progress layout
     */
    fun showProgress() {
        baseActivity?.showProgress()
    }

    /**
     * Hide progress layout
     */
    fun hideProgress() {
        baseActivity?.hideProgress()
    }

    /**
     * Back pressed with delay time
     *
     * @param delayTime delay time
     */
    fun onBackPressed(delayTime: Int) {
        var delayTime = delayTime
        if (delayTime < 0) delayTime = 0
        val handler = Handler()
        handler.postDelayed({ onBackPressed() }, delayTime.toLong())
    }

    /**
     * Back pressed
     */
    fun onBackPressed() {
        activity?.onBackPressed()
    }

    /**
     * Set title for appbar
     *
     * @param title title
     */
    fun setTitle(title: Int) {
        baseActivity?.setTitle(title)
    }

    /**
     * Set title for appbar
     *
     * @param title title
     */
    fun setTitle(title: String) {
        baseActivity?.title = title
    }
}