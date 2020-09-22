package life.wanderinglocal;

import android.Manifest;

public class Constants {
    public static final String PREFS_NAME = "life.wanderinglocal.widget.WanderingWidget";
    public static final String PREF_PREFIX_KEY = "appwidget_";
    public static final String PREF_LOCATION_KEY = "location_";
    public static final String PREF_LAT_KEY = "location_lat_";
    public static final String PREF_LNG_KEY = "location_lng_";
    public static final String PREF_CATEGORY_KEY = "category_";
    public static final String PREF_LAST_SEARCHED_CATEGORY_KEY = "last_category_";
    public static final String DEFAULT_SEARCH_TERM = "Specialty Coffee";
    public static final String WL_ACTION_WIDGET_CLICK = "life.wanderinglocal.WIDGET_CLICK";

    public static final double DEFAULT_MIN_RATING = 4.0;

    public static final String[] PERMISSIONS = new String[]{Manifest.permission_group.LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    //todo: add category repo
    public static final String[] DEFAULT_SEARCH_CATEGORIES = new String[]{"American", "Bars", "Breweries", "Breakfast", "British", "Burmese", "Burgers", "Chinese", "Food Trucks", "Farmer's Market", "Specialty Coffee", "Dessert", "Ethiopian", "Indian", "Jamaican", "Mexican", "Ramen", "Spanish", "Sushi", "Wings"};
    public static final int PERMISSION_REQUEST_CODE = 71;
}
