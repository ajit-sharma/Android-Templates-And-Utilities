package com.example.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.example.R;


public class SimpleDialogFragment extends DialogFragment
{
	private static final String ARGUMENT_EXAMPLE = "example";

	private String mExample;
	private SimpleDialogListener mListener;


	public interface SimpleDialogListener
	{
		void onSimpleDialogPositiveClick(DialogFragment dialog);
		void onSimpleDialogNegativeClick(DialogFragment dialog);
	}


	public static SimpleDialogFragment newInstance(String example)
	{
		SimpleDialogFragment fragment = new SimpleDialogFragment();

		// arguments
		Bundle arguments = new Bundle();
		arguments.putString(ARGUMENT_EXAMPLE, example);
		fragment.setArguments(arguments);

		return fragment;
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setCancelable(true);
		setRetainInstance(true);

		// handle fragment arguments
		Bundle arguments = getArguments();
		if(arguments != null)
		{
			handleArguments(arguments);
		}

		// set callback listener
		try
		{
			mListener = (SimpleDialogListener) getTargetFragment();
		}
		catch(ClassCastException e)
		{
			throw new ClassCastException(getTargetFragment().toString() + " must implement " + SimpleDialogListener.class.getName());
		}
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		// cancelable on touch outside
		if(getDialog() != null) getDialog().setCanceledOnTouchOutside(true);
	}


	@Override
	public void onDestroyView()
	{
		// http://code.google.com/p/android/issues/detail?id=17423
		if(getDialog() != null && getRetainInstance()) getDialog().setDismissMessage(null);
		super.onDestroyView();
	}


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder
				.setTitle("title")
				.setIcon(R.mipmap.ic_launcher)
				.setMessage(mExample)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						mListener.onSimpleDialogPositiveClick(SimpleDialogFragment.this);
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						mListener.onSimpleDialogNegativeClick(SimpleDialogFragment.this);
					}
				});

		return builder.create();
	}


	private void handleArguments(Bundle arguments)
	{
		if(arguments.containsKey(ARGUMENT_EXAMPLE))
		{
			mExample = (String) arguments.get(ARGUMENT_EXAMPLE);
		}
	}
}
