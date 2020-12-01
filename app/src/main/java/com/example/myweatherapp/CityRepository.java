package com.example.myweatherapp;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class CityRepository {
    private CityDao myCityDao;
    private LiveData<List<City>> myAllCities;
    private List<City> myListCities;

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

    public void deleteAllCities() {
        new deleteAllCitiesAsyncTask(myCityDao).execute();
    }

    List<City> getListCities() {
        try {
            return new getCityListAsyncTask(myCityDao).execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return myListCities;
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

    private static class getCityListAsyncTask extends AsyncTask<Void, Void, List<City>> {

        private CityDao myAsyncTaskDao;

        getCityListAsyncTask(CityDao dao) {
            myAsyncTaskDao = dao;
        }

        @Override
        protected List<City> doInBackground(Void... voids) {
            return myAsyncTaskDao.getListCities();
        }
    }

    private static class deleteAllCitiesAsyncTask extends AsyncTask<Void, Void, Void> {
        private CityDao myAsyncTaskDao;

        deleteAllCitiesAsyncTask(CityDao dao) {
            myAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            myAsyncTaskDao.deleteAllCities();
            return null;
        }
    }
}
