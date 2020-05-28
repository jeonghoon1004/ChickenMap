package com.platformeight.meatmap;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class ApiRequest {
    private static final String TAG = "API";

    public static String run(LatLng latLng) {
        String clientId = "tW9cHK2x0aggXxB8ITwU"; //애플리케이션 클라이언트 아이디값"
        String clientSecret = "3mtiC8jcg2"; //애플리케이션 클라이언트 시크릿값"
        String text = null;
        try {
            String str = "고기 "+ gps(latLng);
            text = URLEncoder.encode(str, "UTF-8");
            Log.d(TAG, "run querry: "+str);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("검색어 인코딩 실패",e);
        }

        String apiURL = "https://openapi.naver.com/v1/search/local?query=" + text+"&display=30&start=1";    // json 결과
        //String apiURL = "https://openapi.naver.com/v1/search/blog?query=" + text;    // json 결과
        //String apiURL = "https://openapi.naver.com/v1/search/blog.xml?query="+ text; // xml 결과

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", clientSecret);

        String responseBody = get(apiURL,requestHeaders);
        Log.d(TAG, "run() : "+responseBody);
        return responseBody;
    }
    private static String gps(LatLng latLng){
        String clientId = "z1nfutbyps"; //애플리케이션 클라이언트 아이디값"
        String clientSecret = "tIFcSC0hs3B2vJd45gppVgrWtuNzyYbRz4D7HL5m"; //애플리케이션 클라이언트 시크릿값"

        //String position = "126.9783881,37.5666102";
        String position = latLng.longitude+","+latLng.latitude;
        //String apiURL = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query="+text+"&coordinate=37.5666102, 126.9683881";    // json 결과
        String apiURL = "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?request=coordsToaddr&coords="+position+"&sourcecrs=epsg:4326&output=json&orders=addr,admcode";    // json 결과
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-NCP-APIGW-API-KEY-ID", clientId);
        requestHeaders.put("X-NCP-APIGW-API-KEY", clientSecret);

        String responseBody = get(apiURL,requestHeaders);
        Log.d(TAG, "gps() : "+responseBody);
        try {
            JSONObject jsonObj = new JSONObject(responseBody);
            JSONArray array1 = new JSONArray(String.valueOf(jsonObj.get("results")));
            JSONObject obj1 = array1.getJSONObject(0);
            JSONObject obj2 = obj1.getJSONObject("region");
            JSONObject area1 = obj2.getJSONObject("area1");
            JSONObject area2 = obj2.getJSONObject("area2");
            String str = area1.getString("name") +" "+ area2.getString("name");
            return str;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String get(String apiUrl, Map<String, String> requestHeaders){
        HttpURLConnection con = connect(apiUrl);
        try {
            con.setRequestMethod("GET");
            for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
                return readBody(con.getInputStream());
            } else { // 에러 발생
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }

    private static HttpURLConnection connect(String apiUrl){
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection)url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }

    private static String readBody(InputStream body){
        InputStreamReader streamReader = new InputStreamReader(body);

        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();

            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }

            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
        }
    }
}
