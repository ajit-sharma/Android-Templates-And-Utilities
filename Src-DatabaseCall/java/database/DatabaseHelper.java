package com.example.database;

import android.database.sqlite.SQLiteDatabase;

import com.example.ExampleApplication;
import com.example.database.model.ProductModel;
import com.example.utility.Logcat;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;


public class DatabaseHelper extends OrmLiteSqliteOpenHelper
{
	private static final String DATABASE_NAME = "example.db";
	private static final int DATABASE_VERSION = 1;

	private static DatabaseHelper sInstance;

	private Dao<ProductModel, Long> mProductDao = null;


	private DatabaseHelper()
	{
		super(ExampleApplication.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
	}


	// singleton
	public static synchronized DatabaseHelper getInstance()
	{
		if(sInstance == null) sInstance = new DatabaseHelper();
		return sInstance;
	}


	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource)
	{
		try
		{
			Logcat.d("");
			TableUtils.createTable(connectionSource, ProductModel.class);
		}
		catch(android.database.SQLException e)
		{
			Logcat.e(e, "can't create database");
			e.printStackTrace();
		}
		catch(java.sql.SQLException e)
		{
			Logcat.e(e, "can't create database");
			e.printStackTrace();
		}
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion)
	{
		try
		{
			Logcat.d("");

			// TODO: database migration
		}
		catch(android.database.SQLException e)
		{
			Logcat.e(e, "can't upgrade database");
			e.printStackTrace();
		}
	}


	@Override
	public void close()
	{
		super.close();
		mProductDao = null;
	}


	public synchronized void clearDatabase()
	{
		try
		{
			Logcat.d("");
			TableUtils.dropTable(getConnectionSource(), ProductModel.class, true);
			TableUtils.createTable(getConnectionSource(), ProductModel.class);
		}
		catch(android.database.SQLException e)
		{
			Logcat.e(e, "can't clear database");
			e.printStackTrace();
		}
		catch(java.sql.SQLException e)
		{
			Logcat.e(e, "can't clear database");
			e.printStackTrace();
		}
	}


	public synchronized Dao<ProductModel, Long> getProductDao() throws java.sql.SQLException
	{
		if(mProductDao == null)
		{
			mProductDao = getDao(ProductModel.class);
		}
		return mProductDao;
	}
}
