package com.example.client;

import android.os.AsyncTask;

import com.example.client.request.Request;
import com.example.client.response.Response;
import com.example.utility.Logcat;

import java.lang.ref.WeakReference;


public class APICallTask extends AsyncTask<Void, Void, Response<?>>
{
	private static final int RETRY_MAX_ATTEMPTS = 1; // default value for max number of retries
	private static final long RETRY_INIT_BACKOFF = 500L; // initial sleep time before retry

	private APICall mAPICall;
	private WeakReference<APICallListener> mListener;
	private int mMaxAttempts = RETRY_MAX_ATTEMPTS;


	public APICallTask(Request request, APICallListener listener)
	{
		mAPICall = new APICall(request, this);
		setListener(listener);
	}


	public APICallTask(Request request, APICallListener listener, int maxAttempts)
	{
		this(request, listener);
		setMaxAttempts(maxAttempts);
	}


	@Override
	protected Response<?> doInBackground(Void... params)
	{
		// response
		Response<?> response = null;

		// sleep time before retry
		long backoff = RETRY_INIT_BACKOFF;

		for(int i = 0; i < mMaxAttempts; i++)
		{
			// execute API call
			response = mAPICall.execute();

			// success
			if(response != null)
			{
				break;
			}

			// fail
			else
			{
				if(i == mMaxAttempts) break;

				try
				{
					Logcat.d("sleeping for %d ms before retry", backoff);
					Thread.sleep(backoff);
				}
				catch(InterruptedException e)
				{
					// activity finished before we complete
					Logcat.d("thread interrupted so abort remaining retries");
					Thread.currentThread().interrupt();
					break;
				}

				// increase backoff exponentially
				backoff *= 2;
			}
		}

		return response;
	}


	@Override
	protected void onPostExecute(Response<?> response)
	{
		if(isCancelled()) return;

		APICallListener listener = mListener.get();
		if(listener != null)
		{
			if(response != null)
			{
				listener.onAPICallRespond(this, mAPICall.getResponseStatus(), response);
			}
			else
			{
				listener.onAPICallFail(this, mAPICall.getResponseStatus(), mAPICall.getException());
			}
		}
	}


	@Override
	protected void onCancelled()
	{
		Logcat.d("");
	}


	public Request getRequest()
	{
		return mAPICall.getRequest();
	}


	public void setListener(APICallListener listener)
	{
		mListener = new WeakReference<>(listener);
	}


	public void setMaxAttempts(int maxAttempts)
	{
		mMaxAttempts = maxAttempts;
	}


	public void kill()
	{
		mAPICall.kill();
	}
}
