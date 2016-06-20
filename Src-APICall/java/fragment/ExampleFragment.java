package com.example.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.R;
import com.example.adapter.ExampleAdapter;
import com.example.client.APICallListener;
import com.example.client.APICallManager;
import com.example.client.APICallTask;
import com.example.client.ResponseStatus;
import com.example.client.request.ExampleRequest;
import com.example.client.request.Request;
import com.example.client.response.Response;
import com.example.entity.ProductEntity;
import com.example.utility.Logcat;
import com.example.utility.NetworkUtility;
import com.example.view.StatefulLayout;

import org.codehaus.jackson.JsonParseException;

import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ExampleFragment extends TaskFragment implements APICallListener
{
	private static final String META_REFRESH = "refresh";
	private static final int LAZY_LOADING_TAKE = 16;
	private static final int LAZY_LOADING_OFFSET = 4;
	private static final int LAZY_LOADING_MAX = LAZY_LOADING_TAKE * 10;

	private boolean mLazyLoading = false;
	private View mRootView;
	private StatefulLayout mStatefulLayout;
	private View mFooterView;
	private ExampleAdapter mAdapter;
	private APICallManager mAPICallManager = new APICallManager();
	private List<ProductEntity> mProductList = new ArrayList<>();


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		mRootView = inflater.inflate(R.layout.fragment_example, container, false);
		return mRootView;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		// setup stateful layout
		setupStatefulLayout(savedInstanceState);

		// load data
		if(mProductList == null || mProductList.isEmpty()) loadData();

		// lazy loading progress
		if(mLazyLoading) showLazyLoadingProgress(true);
	}


	@Override
	public void onPause()
	{
		super.onPause();

		// stop adapter
		if(mAdapter != null) mAdapter.stop();
	}


	@Override
	public void onDestroy()
	{
		super.onDestroy();

		// cancel async tasks
		mAPICallManager.cancelAllTasks();
	}


	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		// save current instance state
		super.onSaveInstanceState(outState);
		setUserVisibleHint(true);

		// stateful layout state
		if(mStatefulLayout != null) mStatefulLayout.saveInstanceState(outState);
	}


	@Override
	public void onAPICallRespond(final APICallTask task, final ResponseStatus status, final Response<?> response)
	{
		runTaskCallback(new Runnable()
		{
			public void run()
			{
				if(mRootView == null) return; // view was destroyed

				if(task.getRequest().getClass().equals(ExampleRequest.class))
				{
					Response<List<ProductEntity>> exampleResponse = (Response<List<ProductEntity>>) response;

					// error
					if(exampleResponse.isError())
					{
						Logcat.d("ExampleRequest / " + status.getStatusCode() + " " + status.getStatusMessage() +
								" / error " + exampleResponse.getErrorType() + " / " + exampleResponse.getErrorMessage());

						// handle error
						handleError(exampleResponse.getErrorType(), exampleResponse.getErrorMessage());
					}

					// response
					else
					{
						Logcat.d("ExampleRequest / " + status.getStatusCode() + " " + status.getStatusMessage());

						// check meta data
						if(task.getRequest().getMetaData() != null && task.getRequest().getMetaData().getBoolean(META_REFRESH, false))
						{
							// refresh
							mProductList.clear();
						}

						// get data
						List<ProductEntity> productList = exampleResponse.getResponseObject();
						Iterator<ProductEntity> iterator = productList.iterator();
						while(iterator.hasNext())
						{
							ProductEntity product = iterator.next();
							mProductList.add(product);
						}
					}

					// show content
					mStatefulLayout.showContent();
					showLazyLoadingProgress(false);
				}

				// finish request
				mAPICallManager.finishTask(task);

				// hide progress popup
				if(mAPICallManager.getTasksCount() == 0) showProgress(false);
			}
		});
	}


	@Override
	public void onAPICallFail(final APICallTask task, final ResponseStatus status, final Exception exception)
	{
		runTaskCallback(new Runnable()
		{
			public void run()
			{
				if(mRootView == null) return; // view was destroyed

				if(task.getRequest().getClass().equals(ExampleRequest.class))
				{
					Logcat.d("ExampleRequest / " + status.getStatusCode() + " " + status.getStatusMessage() +
							" / exception " + exception.getClass().getSimpleName() + " / " + exception.getMessage());

					// handle fail
					handleFail(exception);

					// show content
					mStatefulLayout.showContent();
					showLazyLoadingProgress(false);
				}

				// finish request
				mAPICallManager.finishTask(task);

				// hide progress popup
				if(mAPICallManager.getTasksCount() == 0) showProgress(false);
			}
		});
	}


	public void refreshData()
	{
		if(NetworkUtility.isOnline(getActivity()))
		{
			if(!mAPICallManager.hasRunningTask(ExampleRequest.class))
			{
				// show progress popup
				showProgress(true);

				// meta data
				Bundle bundle = new Bundle();
				bundle.putBoolean(META_REFRESH, true);

				// execute request
				int take = (mProductList.size() <= LAZY_LOADING_MAX && mProductList.size() > 0) ? mProductList.size() : LAZY_LOADING_TAKE;
				Request request = new ExampleRequest(0, take);
				request.setMetaData(bundle);
				mAPICallManager.executeTask(request, this);
			}
		}
		else
		{
			Toast.makeText(getActivity(), R.string.global_network_offline, Toast.LENGTH_LONG).show();
		}
	}


	private void handleError(String errorType, String errorMessage)
	{
		// TODO: show dialog
	}


	private void handleFail(Exception exception)
	{
		int messageId;
		if(exception != null && exception.getClass().equals(UnknownHostException.class))
			messageId = R.string.global_network_unknown_host;
		else if(exception != null && exception.getClass().equals(FileNotFoundException.class))
			messageId = R.string.global_network_not_found;
		else if(exception != null && exception.getClass().equals(SocketTimeoutException.class))
			messageId = R.string.global_network_timeout;
		else if(exception != null && exception.getClass().equals(JsonParseException.class))
			messageId = R.string.global_network_parse_fail;
		else if(exception != null && exception.getClass().equals(ParseException.class))
			messageId = R.string.global_network_parse_fail;
		else if(exception != null && exception.getClass().equals(NumberFormatException.class))
			messageId = R.string.global_network_parse_fail;
		else if(exception != null && exception.getClass().equals(ClassCastException.class))
			messageId = R.string.global_network_parse_fail;
		else
			messageId = R.string.global_network_fail;
		Toast.makeText(getActivity(), messageId, Toast.LENGTH_LONG).show();
	}


	private void loadData()
	{
		if(NetworkUtility.isOnline(getActivity()))
		{
			if(!mAPICallManager.hasRunningTask(ExampleRequest.class))
			{
				// show progress
				mStatefulLayout.showProgress();

				// show progress popup
				showProgress(true);

				// execute request
				Request request = new ExampleRequest(0, LAZY_LOADING_TAKE);
				mAPICallManager.executeTask(request, this);
			}
		}
		else
		{
			mStatefulLayout.showOffline();
		}
	}


	private void lazyLoadData()
	{
		if(NetworkUtility.isOnline(getActivity()))
		{
			// show lazy loading progress
			showLazyLoadingProgress(true);

			// execute request
			Request request = new ExampleRequest(mProductList.size(), LAZY_LOADING_TAKE);
			mAPICallManager.executeTask(request, this);
		}
	}


	private void showLazyLoadingProgress(boolean visible)
	{
		if(visible)
		{
			mLazyLoading = true;

			// show footer
			ListView listView = getListView();
			listView.addFooterView(mFooterView);
		}
		else
		{
			// hide footer
			ListView listView = getListView();
			listView.removeFooterView(mFooterView);

			mLazyLoading = false;
		}
	}


	private void bindData()
	{
		// TODO
	}
}
