package life.wanderinglocal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.yelp.fusion.client.models.Business;
import com.yelp.fusion.client.models.SearchResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TimelineRepo {
    private static final double MIN_RATING = 4.0;
    private YelpApi yelpApi;

    public MutableLiveData<List<YelpData>> getData() {
        if (data == null) {
            data = new MutableLiveData<>();
            data.setValue(new ArrayList<>());
        }
        return data;
    }

    private Handler handler = new Handler(Looper.getMainLooper());
    private MutableLiveData<List<YelpData>> data = new MutableLiveData<>();
    private WLDatabase db;
    private Context context;
    private MutableLiveData<String> searchTerm;
    private String location, lat, lng;
    private Listener listener;
    private OkHttpClient client = new OkHttpClient();

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private String getLat() {
        if (lat == null) lat = "";
        return lat;
    }

    private String getLng() {
        if (lng == null) lng = "";
        return lng;
    }

    public String getLocation() {
        if (location == null) location = "";
        return location;
    }

    public void setLocation(String location) {
        if (location == null) location = "";
        this.location = location;
    }

    public void setLocation(String lat, String lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public MutableLiveData<String> getSearchTerm() {
        if (searchTerm == null) {
            searchTerm = new MutableLiveData<>();
            searchTerm.setValue(Constants.DEFAULT_SEARCH_TERM);
        }
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        getSearchTerm().setValue(searchTerm);
    }

    public interface Listener {
        void onDataLoaded();

        void onDataPersisted();
    }

    public TimelineRepo() {
        yelpApi = new YelpApi();
        db = ServiceLocator.getDb();
    }

    public TimelineRepo(Context context) {
        this.context = context;
        yelpApi = new YelpApi();
        if (ServiceLocator.getDb() == null) {
            db = ServiceLocator.buildDb(context);
        } else {
            db = ServiceLocator.getDb();
        }
        loadCached();
    }

    private MutableLiveData<List<YelpData>> search(YelpApi.SearchBuilder builder) {
        Log.d(getClass().getSimpleName(), String.format("Search: location=%s, lat=%s, lng=%s, searchTerm=%s", getLocation(), getLat(), getLng(), getSearchTerm().getValue()));
        if (getLat().length() == 0 && getLng().length() == 0 && getLocation().length() == 0 || getSearchTerm().getValue() == null)
            return data;
        yelpApi.search(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                SearchResponse searchResponse = response.body();
                ArrayList<Business> businesses = searchResponse.getBusinesses();
                List<YelpData> results = new ArrayList<>();
                for (Business b : businesses) {
                    YelpData data = new YelpData();
                    data.setBusinessName(b.getName());
                    data.setImageUrl(b.getImageUrl());
                    data.setYelpUrl(b.getUrl());
                    data.setRating(b.getRating());
                    data.setSearchTerm(getSearchTerm().getValue());
                    data.setLocation(b.getLocation());
                    data.setDistance(b.getDistance());
                    results.add(data);
                }
                Log.d(getClass().getSimpleName(), String.format("Yelp search has returned %d results", results.size()));
                Collections.sort(results, (t1, t2) -> Double.compare(t2.getRating(), t1.getRating()));
                if (results.size() > 0) {
                    data.postValue(results);
                    if (listener != null) listener.onDataLoaded();
                    persist(results);
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.e(getClass().getSimpleName(), call.request().toString());
                AsyncTask.execute(() -> {
                    if (db != null) {
                        List<YelpData> cached = db.dao().getDataWithParams(getSearchTerm().getValue(), MIN_RATING);
                        if (cached.size() > 0) {
                            Collections.sort(cached, (t1, t2) -> Double.compare(t2.getRating(), t1.getRating()));
                            data.postValue(cached);
                        }
                    }
                });
            }
        }, builder);
        return data;
    }

    public MutableLiveData<List<YelpData>> search() {
        return search(new YelpApi.SearchBuilder().setLimit(20).setLatLng(getLat(), getLng()).setLocation(getLocation()).setTerm(getSearchTerm().getValue()));
    }

    public MutableLiveData<List<YelpData>> searchWithOffset(int offset) {
        return search(new YelpApi.SearchBuilder().setLimit(20).setOffset(offset).setLatLng(getLat(), getLng()).setLocation(getLocation()).setTerm(getSearchTerm().getValue()));
    }

    private void persist(List<YelpData> entries) {
        if (ServiceLocator.getDb() == null)
            Log.d(getClass().getSimpleName(), "DB is null, not persisting results");
        AsyncTask.execute(() -> {
            ServiceLocator.getDb().dao().addEntries(entries);
            if (listener != null) listener.onDataPersisted();
            Log.d(getClass().getSimpleName(), String.format("Persisting %d items to db", entries.size()));
        });
    }

    private void loadCached() {
        Log.d(getClass().getSimpleName(), "loadCached");
        final String searchTerm = getSearchTerm().getValue();
        AsyncTask.execute(() -> {
            List<YelpData> cached = db.dao().getDataWithParams(searchTerm, MIN_RATING);
            if (cached != null && cached.size() > 0 && (data.getValue() == null || data.getValue().size() == 0)) {
                handler.post(() -> {
                    Collections.sort(cached, (t1, t2) -> Double.compare(t2.getRating(), t1.getRating()));
                    data.setValue(cached);
                });
            }
        });
    }

    private void loadImage(final YelpData yd) {
        Log.d(getClass().getSimpleName(), "loadImage()");
        Request request = new Request.Builder()
                .url(yd.getImageUrl())
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                byte[] img = IOUtils.byteArrFromInputStream(response.body().byteStream());
                Bitmap bmp = BitmapFactory.decodeByteArray(img, 0, img.length);
                if (bmp != null) {
                    yd.setBmp(bmp);
                }
            }
        });
    }
}