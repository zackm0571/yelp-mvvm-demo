package life.wanderinglocal;

import android.graphics.Bitmap;

import com.yelp.fusion.client.models.Location;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Update;

/**
 * Model for timeline results. Room uses this as an Entity for database schema.
 * todo: abstract all yelp specific language
 */
@Entity(tableName = "timelineEntries")
public class WLTimelineEntry {
    @Dao
    public interface TimelineEntryDAO {
        @Query("SELECT * FROM timelineEntries LIMIT 20")
        List<WLTimelineEntry> getAll();

        @Query("SELECT * FROM timelineEntries WHERE searchTerm = :searchTerm AND rating > :rating LIMIT 10")
        List<WLTimelineEntry> getDataWithParams(String searchTerm, double rating);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void addEntries(List<WLTimelineEntry> data);

        @Update
        void updateEntries(List<WLTimelineEntry> data);

        @Query("DELETE FROM timelineEntries")
        void deleteEntries();
    }

    @Ignore
    private Bitmap bmp;
    public Bitmap getBmp() {
        return bmp;
    }
    public void setBmp(Bitmap bmp) {
        this.bmp = bmp;
    }

    @PrimaryKey
    @NonNull
    private String businessName = "";
    public String getBusinessName() {
        return businessName;
    }
    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    @ColumnInfo(name = "image_url")
    private String imageUrl;
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @ColumnInfo(name = "yelp_url")
    private String yelpUrl;
    public void setYelpUrl(String url) {
        this.yelpUrl = url;
    }
    public String getYelpUrl() {
        return yelpUrl;
    }

    @ColumnInfo(name = "rating")
    private double rating;
    public double getRating() {
        return rating;
    }
    public void setRating(double rating) {
        this.rating = rating;
    }

    @ColumnInfo(name = "searchTerm")
    private String searchTerm;
    public String getSearchTerm() {
        return searchTerm;
    }
    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    @Ignore
    private Location location;
    public Location getLocation() {
        return location;
    }
    public String getLocationString(){
        if(location == null) return "";
        return String.format("%s\n%s, %s %s", location.getAddress1(), location.getCity(), location.getState(), location.getZipCode());
    }
    public void setLocation(Location location) {
        this.location = location;
    }

    @ColumnInfo(name = "distance")
    private double distance;
    public void setDistance(double distance) {
        this.distance = distance;
    }
    public double getDistance() {
        return distance;
    }

}
