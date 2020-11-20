package com.example.myweatherapp;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class CityRepository {
    private CityDao myCityDao;
    private LiveData<List<City>> myAllCities;

    public CityRepository(Application application) {
        CityRoomDatabase db = CityRoomDatabase.getDatabase(application);
        myCityDao = db.cityDao();
        myAllCities = myCityDao.getAllCities();
    }

    LiveData<List<City>> getAllCities() {
        return myAllCities;
    }

    public void insert(City city) {
        new insertAsyncTask(myCityDao).execute(city);
    }

    public void deleteCity(City city) {
        new deleteCityAsyncTask(myCityDao).execute(city);
    }

    private static class insertAsyncTask extends AsyncTask<City, Void, Void> {
        private CityDao myAsyncTaskDao;

        insertAsyncTask(CityDao dao) {
            myAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final City... cities) {
            myAsyncTaskDao.insert(cities[0]);
            return null;
        }
    }

    private static class deleteCityAsyncTask extends AsyncTask<City, Void, Void> {

        private CityDao myAsyncTaskDao;

        deleteCityAsyncTask(CityDao dao) {
            myAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(City... cities) {
            myAsyncTaskDao.deleteCity(cities[0]);
            return null;
        }
    }
}
