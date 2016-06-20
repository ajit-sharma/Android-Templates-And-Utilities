package com.example.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.R;


public class ViewDialogFragment extends DialogFragment
{
	private static final String ARGUMENT_EXAMPLE = "example";

	private String mExample;
	private View mRootView;
	private String mUsername = "";
	private String mPassword = "";
	private ViewDialogListener mListener;


	public interface ViewDialogListener
	{
		void onViewDialogPositiveClick(DialogFragment dialog, String username, String password);
		void onViewDialogNegativeClick(DialogFragment dialog);
	}


	public static ViewDialogFragment newInstance(String example)
	{
		ViewDialogFragment fragment = new ViewDialogFragment();

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
			mListener = (ViewDialogListener) getTargetFragment();
		}
		catch(ClassCastException e)
		{
			throw new ClassCastException(getTargetFragment().toString() + " must implement " + ViewDialogListener.class.getName());
		}
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		// cancelable on touch outside
		if(getDialog() != null) getDialog().setCanceledOnTouchOutside(true);

		// restore saved state
		handleSavedInstanceState();
	}


	@Override
	public void onDestroyView()
	{
		// http://code.google.com/p/android/issues/detail?id=17423
		if(getDialog() != null && getRetainInstance()) getDialog().setDismissMessage(null);
		super.onDestroyView();
	}


	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		// save current instance state
		super.onSaveInstanceState(outState);

		EditText usernameEditText = (EditText) mRootView.findViewById(R.id.dialog_view_username);
		EditText passwordEditText = (EditText) mRootView.findViewById(R.id.dialog_view_password);

		mUsername = usernameEditText.getText().toString();
		mPassword = passwordEditText.getText().toString();
	}


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		mRootView = inflater.inflate(R.layout.dialog_view, null);

		builder
				.setTitle(mExample)
				.setIcon(R.mipmap.ic_launcher)
				.setView(mRootView)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						// overriden below
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						mListener.onViewDialogNegativeClick(ViewDialogFragment.this);
					}
				});

		// create dialog from builder
		final AppCompatDialog dialog = builder.create();

		// override positive button
		dialog.setOnShowListener(new DialogInterface.OnShowListener()
		{
			@Override
			public void onShow(DialogInterface dialogInterface)
			{
				Button button = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
				button.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getActivity(), R.mipmap.ic_launcher), null, null, null);
				button.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						EditText usernameEditText = (EditText) mRootView.findViewById(R.id.dialog_view_username);
						EditText passwordEditText = (EditText) mRootView.findViewById(R.id.dialog_view_password);

						String username = usernameEditText.getText().toString();
						String password = passwordEditText.getText().toString();

						// TODO: data validation

						mListener.onViewDialogPositiveClick(ViewDialogFragment.this, username, password);
						dialog.dismiss();
					}
				});
			}
		});

		return dialog;
	}


	private void handleArguments(Bundle arguments)
	{
		if(arguments.containsKey(ARGUMENT_EXAMPLE))
		{
			mExample = (String) arguments.get(ARGUMENT_EXAMPLE);
		}
	}


	private void handleSavedInstanceState()
	{
		EditText usernameEditText = (EditText) mRootView.findViewById(R.id.dialog_view_username);
		EditText passwordEditText = (EditText) mRootView.findViewById(R.id.dialog_view_password);

		usernameEditText.setText(mUsername);
		passwordEditText.setText(mPassword);
	}
}
