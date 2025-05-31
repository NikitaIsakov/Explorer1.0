package com.example.Explorer;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.Explorer.database.POIEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OverpassAPIService {
    private static final String TAG = "OverpassAPIService";
    private static final String OVERPASS_API_URL = "https://overpass-api.de/api/interpreter";
    private static final int TIMEOUT_SECONDS = 30;
    private static final int SEARCH_RADIUS_METERS = 5000;

    private final OkHttpClient client;

    public OverpassAPIService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
    }

    public interface OverpassCallback {
        void onSuccess(List<POIEntity> pois);
        void onError(String error);
    }

    public void getPOIsInCity(String city, double lat, double lon, OverpassCallback callback) {
        if (city == null || city.trim().isEmpty()) {
            callback.onError("Название города не может быть пустым");
            return;
        }

        if (!isValidCoordinate(lat, lon)) {
            callback.onError("Некорректные координаты");
            return;
        }


        String query = buildOverpassQuery(lat, lon);
        Log.d(TAG, "Executing Overpass query for city: " + city);


        new Thread(() -> {
            try {
                List<POIEntity> pois = executeOverpassQuery(query, city);

                new Handler(Looper.getMainLooper()).post(() -> {
                    Log.d(TAG, "Successfully loaded " + pois.size() + " POIs");
                    callback.onSuccess(pois);
                });

            } catch (Exception e) {
                Log.e(TAG, "Error executing Overpass query", e);
                new Handler(Looper.getMainLooper()).post(() ->
                        callback.onError("Ошибка загрузки данных: " + e.getMessage()));
            }
        }).start();
    }

    private String buildOverpassQuery(double lat, double lon) {
        return "[out:json][timeout:" + TIMEOUT_SECONDS + "];\n" +
                "(\n" +
                "  node[\"tourism\"~\"^(museum|gallery|attraction|viewpoint|artwork|information)$\"](around:" + SEARCH_RADIUS_METERS + "," + lat + "," + lon + ");\n" +
                "  node[\"historic\"~\"^(monument|memorial|castle|ruins|archaeological_site)$\"](around:" + SEARCH_RADIUS_METERS + "," + lat + "," + lon + ");\n" +
                "  node[\"leisure\"~\"^(park|garden|nature_reserve)$\"](around:" + SEARCH_RADIUS_METERS + "," + lat + "," + lon + ");\n" +
                "  node[\"amenity\"~\"^(place_of_worship|theatre|cinema|library|restaurant|cafe|bar|pub|fast_food|food_court|ice_cream)$\"](around:" + SEARCH_RADIUS_METERS + "," + lat + "," + lon + ");\n" +
                "  node[\"cuisine\"](around:" + SEARCH_RADIUS_METERS + "," + lat + "," + lon + ");\n" +
                ");\n" +
                "out center meta;";
    }

    private List<POIEntity> executeOverpassQuery(String query, String city) throws IOException, JSONException {
        RequestBody body = RequestBody.create(query, MediaType.parse("text/plain; charset=utf-8"));
        Request request = new Request.Builder()
                .url(OVERPASS_API_URL)
                .post(body)
                .addHeader("User-Agent", "Explorer Android App")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP Error: " + response.code() + " " + response.message());
            }

            String responseData = response.body().string();
            if (responseData == null || responseData.trim().isEmpty()) {
                throw new IOException("Пустой ответ от сервера");
            }

            return parseOverpassResponse(responseData, city);
        }
    }

    private List<POIEntity> parseOverpassResponse(String jsonResponse, String city) throws JSONException {
        List<POIEntity> pois = new ArrayList<>();

        JSONObject json = new JSONObject(jsonResponse);

        if (!json.has("elements")) {
            Log.w(TAG, "No elements found in response");
            return pois;
        }

        JSONArray elements = json.getJSONArray("elements");
        Log.d(TAG, "Parsing " + elements.length() + " elements");

        for (int i = 0; i < elements.length(); i++) {
            try {
                JSONObject element = elements.getJSONObject(i);
                POIEntity poi = parseElement(element, city);

                if (poi != null && isValidPOI(poi)) {
                    pois.add(poi);
                }
            } catch (JSONException e) {
                Log.w(TAG, "Error parsing element " + i, e);
            }
        }

        return pois;
    }

    private POIEntity parseElement(JSONObject element, String city) throws JSONException {
        if (!element.has("lat") || !element.has("lon")) {
            return null;
        }

        POIEntity poi = new POIEntity();
        poi.id = String.valueOf(element.getLong("id"));
        poi.latitude = element.getDouble("lat");
        poi.longitude = element.getDouble("lon");
        poi.city = city;
        poi.isVisited = false;
        poi.visitTimestamp = 0;

        // Парсим теги
        if (element.has("tags")) {
            JSONObject tags = element.getJSONObject("tags");
            poi.name = tags.optString("name", generateDefaultName(tags));
            poi.description = buildDescription(tags);
            poi.category = determinePOICategory(tags);
        } else {
            poi.name = "Неизвестное место";
            poi.description = "";
            poi.category = "other";
        }

        return poi;
    }

    private String generateDefaultName(JSONObject tags) {
        // Пытаемся создать имя на основе категории
        String tourism = tags.optString("tourism", "");
        String historic = tags.optString("historic", "");
        String leisure = tags.optString("leisure", "");
        String amenity = tags.optString("amenity", "");

        if (!tourism.isEmpty()) return capitalizeFirst(tourism);
        if (!historic.isEmpty()) return capitalizeFirst(historic);
        if (!leisure.isEmpty()) return capitalizeFirst(leisure);
        if (!amenity.isEmpty()) return getAmenityDisplayName(amenity);

        return "Интересное место";
    }

    private String getAmenityDisplayName(String amenity) {
        switch (amenity.toLowerCase()) {
            case "restaurant": return "Ресторан";
            case "cafe": return "Кафе";
            case "bar": return "Бар";
            case "pub": return "Паб";
            case "fast_food": return "Фастфуд";
            case "food_court": return "Фуд-корт";
            case "theatre": return "Театр";
            case "cinema": return "Кинотеатр";
            case "library": return "Библиотека";
            default: return capitalizeFirst(amenity);
        }
    }

    private String formatCuisine(String cuisine) {
        String[] cuisines = cuisine.split(";");
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < cuisines.length; i++) {
            String c = cuisines[i].trim().toLowerCase();
            if (i > 0) formatted.append(", ");

            switch (c) {
                case "pizza": formatted.append("Пицца"); break;
                case "burger": formatted.append("Бургеры"); break;
                case "sushi": formatted.append("Суши"); break;
                case "kebab": formatted.append("Кебаб"); break;
                case "seafood": formatted.append("Морепродукты"); break;
                case "regional": formatted.append("Местная"); break;
                default: formatted.append(capitalizeFirst(c)); break;
            }
        }

        return formatted.toString();
    }

    private String buildDescription(JSONObject tags) {
        StringBuilder description = new StringBuilder();

        String desc = tags.optString("description", "");
        if (!desc.isEmpty()) {
            description.append(desc);
        }

        String cuisine = tags.optString("cuisine", "");
        if (!cuisine.isEmpty()) {
            if (description.length() > 0) description.append("\n");
            description.append("Кухня: ").append(formatCuisine(cuisine));
        }

        String website = tags.optString("website", "");
        String phone = tags.optString("phone", "");
        String opening_hours = tags.optString("opening_hours", "");

        if (!website.isEmpty()) {
            if (description.length() > 0) description.append("\n");
            description.append("Веб-сайт: ").append(website);
        }

        if (!phone.isEmpty()) {
            if (description.length() > 0) description.append("\n");
            description.append("Телефон: ").append(phone);
        }

        if (!opening_hours.isEmpty()) {
            if (description.length() > 0) description.append("\n");
            description.append("Часы работы: ").append(opening_hours);
        }

        return description.toString();
    }

    private String determinePOICategory(JSONObject tags) {
        if (tags.has("tourism")) return tags.optString("tourism");
        if (tags.has("historic")) return tags.optString("historic");
        if (tags.has("leisure")) return tags.optString("leisure");
        if (tags.has("amenity")) return tags.optString("amenity");
        if (tags.has("cuisine")) return "restaurant";
        return "other";
    }

    private boolean isValidCoordinate(double lat, double lon) {
        return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180 && lat != 0 && lon != 0;
    }

    private boolean isValidPOI(POIEntity poi) {
        return poi != null &&
                poi.name != null && !poi.name.trim().isEmpty() &&
                isValidCoordinate(poi.latitude, poi.longitude);
    }

    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}